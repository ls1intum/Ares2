package de.tum.cit.ase.ares.api.securitytest.java.projectScanner;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.List;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import de.tum.cit.ase.ares.api.buildtoolconfiguration.BuildMode;
import de.tum.cit.ase.ares.api.buildtoolconfiguration.BuildToolConfiguration;

/**
 * Covers the check that turns the derived supervised package into a boundary.
 * <p>
 * {@code scanForPackageName()} counts declarations, so whoever can add files to
 * the project can influence what it answers. That is tolerable while test cases
 * are being written and intolerable once enforcement is armed, which is why the
 * two are separate: this check runs immediately before the arming, against the
 * compiled output, and refuses a scope the project contradicts.
 * <p>
 * Refusing is the behaviour under test. The alternative is the failure this
 * exists to prevent: a run scoped to a package the assignment does not live in
 * enforces nothing, reports nothing and passes.
 *
 * @since 2.1.2
 * @author Markus Paulsen
 */
@DisplayName("JavaProjectScanner derived-scope coverage")
class JavaProjectScannerScopeCoverageTest {

	@TempDir
	Path projectRoot;

	@Test
	@DisplayName("Accepts a scope that covers every compiled production class")
	void acceptsAScopeCoveringEveryCompiledClass() throws IOException {
		Path outputRoot = compile("""
				package de.tum.cit.aet;

				public class Calculator {
				}
				""", "de/tum/cit/aet/Calculator.java");
		compileInto(outputRoot, """
				package de.tum.cit.aet.helper;

				public class Support {
				}
				""", "de/tum/cit/aet/helper/Support.java");

		assertDoesNotThrow(() -> scanner(outputRoot).requireDerivedScopeToCoverTheProject("de.tum.cit.aet"));
	}

	/**
	 * The attack the check exists for. Enough classes under a package of the
	 * submitter's choosing make it the derived scope, and the assignment then sits
	 * outside the boundary drawn around it.
	 */
	@Test
	@DisplayName("Refuses a scope that leaves the assignment package outside it")
	void refusesAScopeThatExcludesTheAssignmentPackage() throws IOException {
		Path outputRoot = compile("""
				package de.tum.cit.aet;

				public class Assignment {
				}
				""", "de/tum/cit/aet/Assignment.java");
		compileInto(outputRoot, """
				package decoy;

				public class Filler {
				}
				""", "decoy/Filler.java");

		SecurityException refusal = assertThrows(SecurityException.class,
				() -> scanner(outputRoot).requireDerivedScopeToCoverTheProject("decoy"));

		assertTrue(refusal.getMessage().contains("de.tum.cit.aet"),
				"the diagnostic must name the package that would have gone unsupervised, or it tells the "
						+ "instructor nothing actionable: " + refusal.getMessage());
	}

	/**
	 * A prefix test would accept this. The scopes differ at a segment boundary, so
	 * de.tum.cit.aetevil is not below de.tum.cit.aet and its classes would run
	 * unsupervised.
	 */
	@Test
	@DisplayName("Compares on segment boundaries rather than by prefix")
	void refusesAPackageThatOnlyLooksLikeADescendant() throws IOException {
		Path outputRoot = compile("""
				package de.tum.cit.aet;

				public class Assignment {
				}
				""", "de/tum/cit/aet/Assignment.java");
		compileInto(outputRoot, """
				package de.tum.cit.aetevil;

				public class Elsewhere {
				}
				""", "de/tum/cit/aetevil/Elsewhere.java");

		SecurityException refusal = assertThrows(SecurityException.class,
				() -> scanner(outputRoot).requireDerivedScopeToCoverTheProject("de.tum.cit.aet"));

		assertTrue(refusal.getMessage().contains("de.tum.cit.aetevil"), refusal.getMessage());
	}

	@Test
	@DisplayName("Refuses a production class in the default package")
	void refusesAClassInTheDefaultPackage() throws IOException {
		Path outputRoot = compile("""
				package de.tum.cit.aet;

				public class Assignment {
				}
				""", "de/tum/cit/aet/Assignment.java");
		compileInto(outputRoot, """
				public class Unpackaged {
				}
				""", "Unpackaged.java");

		SecurityException refusal = assertThrows(SecurityException.class,
				() -> scanner(outputRoot).requireDerivedScopeToCoverTheProject("de.tum.cit.aet"));

		assertTrue(refusal.getMessage().contains("Unpackaged"),
				"a class in the default package lies outside every scope, and the diagnostic must name it: "
						+ refusal.getMessage());
	}

	/**
	 * Leaving these out of the inventory was justified as sparing a project whose
	 * production output is nothing but infrastructure, which is what Ares' own
	 * build is. The justification does not hold: package names are chosen by
	 * whoever writes the classes, so "every class is reserved" is a state a
	 * submission can produce, and it then passed with enforcement aimed at nothing.
	 * A project that really is infrastructure declares its scope in a policy, which
	 * is what Ares' own self-tests now do.
	 */
	@Test
	@DisplayName("Refuses a reserved-package class rather than leaving it out of the inventory")
	void refusesClassesInReservedPackages() throws IOException {
		Path outputRoot = compile("""
				package de.tum.cit.aet;

				public class Assignment {
				}
				""", "de/tum/cit/aet/Assignment.java");
		compileInto(outputRoot, """
				package de.tum.cit.ase.ares.api;

				public class Infrastructure {
				}
				""", "de/tum/cit/ase/ares/api/Infrastructure.java");

		SecurityException refusal = assertThrows(SecurityException.class,
				() -> scanner(outputRoot).requireDerivedScopeToCoverTheProject("de.tum.cit.aet"));

		assertTrue(refusal.getMessage().contains("Infrastructure"),
				"the diagnostic must name the class that took a trusted name: " + refusal.getMessage());
	}

	/**
	 * The reserved class is refused before coverage is even considered, so a
	 * submission cannot use one to stop the check reaching a package the scope
	 * leaves out. Either way the run refuses; what matters is that it never
	 * silently proceeds.
	 */
	@Test
	@DisplayName("A reserved-package class is refused before an uncovered one is reached")
	void refusesTheReservedClassBeforeTheUncoveredOne() throws IOException {
		Path outputRoot = compile("""
				package de.tum.cit.ase.ares.api;

				public class Infrastructure {
				}
				""", "de/tum/cit/ase/ares/api/Infrastructure.java");
		compileInto(outputRoot, """
				package de.tum.cit.aet;

				public class Assignment {
				}
				""", "de/tum/cit/aet/Assignment.java");

		SecurityException refusal = assertThrows(SecurityException.class,
				() -> scanner(outputRoot).requireDerivedScopeToCoverTheProject("decoy"));

		assertTrue(refusal.getMessage().contains("Infrastructure"),
				"the reserved class is the first thing wrong with this output: " + refusal.getMessage());
	}

	/**
	 * An output root holding no class at all cannot confirm anything about the
	 * scope. Passing here used to look like tolerance for a project with no
	 * supervisable code, but this check runs immediately before enforcement is
	 * armed, by which point the compiled output is the whole truth about what will
	 * run: nothing compiled means nothing verified.
	 */
	@Test
	@DisplayName("Refuses when nothing at all is compiled")
	void refusesWhenNothingAtAllIsCompiled() throws IOException {
		Path outputRoot = Files.createDirectories(projectRoot.resolve("build/classes/java/main"));

		SecurityException refusal = assertThrows(SecurityException.class,
				() -> scanner(outputRoot).requireDerivedScopeToCoverTheProject("de.tum.cit.aet"));

		assertTrue(refusal.getMessage().contains(outputRoot.toString()),
				"the diagnostic must name where it looked: " + refusal.getMessage());
	}

	@Test
	@DisplayName("Refuses an empty scope while classes are compiled")
	void refusesABlankScope() throws IOException {
		Path outputRoot = compile("""
				package de.tum.cit.aet;

				public class Assignment {
				}
				""", "de/tum/cit/aet/Assignment.java");

		assertThrows(SecurityException.class, () -> scanner(outputRoot).requireDerivedScopeToCoverTheProject(""));
	}

	@Test
	@DisplayName("Refuses an output root it cannot read")
	void refusesAnUnreadableOutputRoot() throws IOException {
		assumeTrue(FileSystems.getDefault().supportedFileAttributeViews().contains("posix"),
				"POSIX permissions are needed to make the output root unreadable");
		Path outputRoot = compile("""
				package de.tum.cit.aet;

				public class Assignment {
				}
				""", "de/tum/cit/aet/Assignment.java");
		Files.setPosixFilePermissions(outputRoot, PosixFilePermissions.fromString("---------"));
		try {
			// Permission bits do not bind a superuser, so clearing them is not by itself
			// a denial. Without this the run would report the code under test where the
			// fixture is what could not be established.
			assumeTrue(!Files.isReadable(outputRoot), () -> "cannot make " + outputRoot + " unreadable here");

			assertThrows(SecurityException.class,
					() -> scanner(outputRoot).requireDerivedScopeToCoverTheProject("de.tum.cit.aet"));
		} finally {
			Files.setPosixFilePermissions(outputRoot, PosixFilePermissions.fromString("rwxr-xr-x"));
		}
	}

	/**
	 * A package declaration is not code that can run, so it says nothing about the
	 * scope. Counting it would refuse a project for describing its own packages.
	 */
	@Test
	@DisplayName("Ignores package metadata")
	void ignoresPackageMetadata() throws IOException {
		Path outputRoot = compile("""
				package de.tum.cit.aet;

				public class Assignment {
				}
				""", "de/tum/cit/aet/Assignment.java");
		compileInto(outputRoot, """
				@Deprecated
				package de.tum.cit.aet;
				""", "de/tum/cit/aet/package-info.java");

		assertDoesNotThrow(() -> scanner(outputRoot).requireDerivedScopeToCoverTheProject("de.tum.cit.aet"));
	}

	private JavaProjectScanner scanner(Path outputRoot) throws IOException {
		return new JavaProjectScanner(ScannerFixtures.gradleConfigurationWithoutSourceRoots(projectRoot, outputRoot));
	}

	private Path compile(String source, String relativeSourcePath) throws IOException {
		return ScannerFixtures.compile(projectRoot, source, relativeSourcePath);
	}

	private void compileInto(Path outputRoot, String source, String relativeSourcePath) throws IOException {
		ScannerFixtures.compileInto(projectRoot, outputRoot, source, relativeSourcePath);
	}
}
