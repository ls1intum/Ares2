package de.tum.cit.ase.ares.api.securitytest.java.projectScanner;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermissions;

import javax.tools.ToolProvider;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import de.tum.cit.ase.ares.api.buildtoolconfiguration.BuildMode;
import de.tum.cit.ase.ares.api.util.ProjectSourcesFinder;

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
	 * Version 3 restored the Ares 1 breadth, so a test-harness name is reserved
	 * again. A class compiled into {@code org.junit.jupiter.api} would shadow the
	 * assertions that judge the submission; the coverage check refuses it before
	 * enforcement is armed, exactly as it does an Ares-api name.
	 */
	@Test
	@DisplayName("Refuses a class that takes a restored test-harness package")
	void refusesAClassInARestoredHarnessPackage() throws IOException {
		Path outputRoot = compile("""
				package de.tum.cit.aet;

				public class Assignment {
				}
				""", "de/tum/cit/aet/Assignment.java");
		compileInto(outputRoot, """
				package org.junit.jupiter.api;

				public class Assertions {
				}
				""", "org/junit/jupiter/api/Assertions.java");

		SecurityException refusal = assertThrows(SecurityException.class,
				() -> scanner(outputRoot).requireDerivedScopeToCoverTheProject("de.tum.cit.aet"));

		assertTrue(refusal.getMessage().contains("org.junit"),
				"the diagnostic must name the restored namespace the class took: " + refusal.getMessage());
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
	 * An output root holding no class at all is an exercise whose supervised
	 * package is still empty, which is the ordinary state of one that hands the
	 * student a package to fill.
	 * <p>
	 * This check exists to refuse a scope that supervises the wrong code, and there
	 * is no wrong code here: no class exists that could sit outside the boundary.
	 * Refusing turned "you have written nothing yet" into an Ares security error
	 * and stopped the student's own tests from ever reporting. Unreadable is a
	 * different matter and is still refused, because not knowing what is there is
	 * not the same as knowing nothing is.
	 */
	@Test
	@DisplayName("Accepts an empty supervised package the student has yet to fill")
	void acceptsWhenNothingAtAllIsCompiled() throws IOException {
		Path outputRoot = Files.createDirectories(projectRoot.resolve("build/classes/java/main"));

		assertDoesNotThrow(() -> scanner(outputRoot).requireDerivedScopeToCoverTheProject("de.tum.cit.aet"));
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

	/**
	 * The importer skips a file it cannot read with only a warning, so the check
	 * would otherwise inspect a smaller inventory than the one on disk and report
	 * it complete.
	 */
	@Test
	@DisplayName("Refuses a class file the importer cannot read")
	void refusesAClassFileTheImporterCannotRead() throws IOException {
		Path outputRoot = assignmentOutput();
		Path unreadable = Files.createDirectories(outputRoot.resolve("decoy")).resolve("Hidden.class");
		Files.writeString(unreadable, "not a class file");

		SecurityException refusal = assertThrows(SecurityException.class,
				() -> scanner(outputRoot).requireDerivedScopeToCoverTheProject("de.tum.cit.aet"));

		assertTrue(refusal.getMessage().contains("Hidden.class"), refusal.getMessage());
	}

	/**
	 * The importer keeps one class per name, so a second file defining the same
	 * class is dropped from the inventory, and what it holds is never checked.
	 */
	@Test
	@DisplayName("Refuses two class files that define the same class")
	void refusesTwoFilesDefiningOneClass() throws IOException {
		Path outputRoot = assignmentOutput();
		Path copy = Files.createDirectories(outputRoot.resolve("decoy")).resolve("Assignment.class");
		Files.copy(outputRoot.resolve("de/tum/cit/aet/Assignment.class"), copy);

		SecurityException refusal = assertThrows(SecurityException.class,
				() -> scanner(outputRoot).requireDerivedScopeToCoverTheProject("de.tum.cit.aet"));

		String message = refusal.getMessage().replace('\\', '/');
		assertTrue(message.contains("de/tum/cit/aet/Assignment.class") && message.contains("decoy/Assignment.class"),
				"the diagnostic must name both files, whichever one the importer kept: " + message);
	}

	/**
	 * Where several classes break the rule, the one named is the first by name, so
	 * the same output always produces the same diagnostic.
	 */
	@Test
	@DisplayName("Names the alphabetically first class outside the scope")
	void namesTheFirstOffenderByName() throws IOException {
		Path outputRoot = assignmentOutput();
		compileInto(outputRoot, "package decoy; public class Zulu { }", "decoy/Zulu.java");
		compileInto(outputRoot, "package decoy; public class Alpha { }", "decoy/Alpha.java");

		SecurityException refusal = assertThrows(SecurityException.class,
				() -> scanner(outputRoot).requireDerivedScopeToCoverTheProject("de.tum.cit.aet"));

		assertTrue(refusal.getMessage().contains("decoy.Alpha"), refusal.getMessage());
	}

	/**
	 * A package descriptor alone is nothing that can run, so the output is treated
	 * as empty and accepted with a warning, like an empty package.
	 */
	@Test
	@DisplayName("Accepts an output holding only a package descriptor")
	void acceptsAnOutputHoldingOnlyAPackageDescriptor() throws IOException {
		Path outputRoot = compile("""
				@Deprecated
				package de.tum.cit.aet;
				""", "de/tum/cit/aet/package-info.java");

		assertDoesNotThrow(() -> scanner(outputRoot).requireDerivedScopeToCoverTheProject("decoy"));
	}

	/**
	 * The scope derived through real discovery of an Artemis layout, whose sources
	 * sit in an assignment folder the descriptor names, is checked like any other.
	 */
	@ParameterizedTest
	@CsvSource({ "MAVEN,false", "MAVEN,true", "GRADLE,false", "GRADLE,true" })
	void checksAScopeDerivedFromAnArtemisLayout(BuildMode buildMode, boolean decoy) throws IOException {
		Path sources = Files.createDirectories(projectRoot.resolve("assignment/src"));
		if (buildMode == BuildMode.MAVEN) {
			Files.writeString(projectRoot.resolve("pom.xml"),
					"""
							<project><build><sourceDirectory>${project.basedir}/assignment/src</sourceDirectory></build></project>
							""");
		} else {
			Files.writeString(projectRoot.resolve("build.gradle"), """
					def assignmentSrcDir = "assignment/src"
					sourceSets { main { java { srcDirs = [assignmentSrcDir] } } }
					""");
		}
		Path outputRoot = Files.createDirectories(
				projectRoot.resolve(buildMode == BuildMode.MAVEN ? "target/classes" : "build/classes/java/main"));
		compileSource(sources, outputRoot, "de/tum/cit/aet/Assignment.java",
				"package de.tum.cit.aet; public class Assignment { }");
		if (decoy) {
			compileSource(sources, outputRoot, "decoy/First.java", "package decoy; public class First { }");
			compileSource(sources, outputRoot, "decoy/Second.java", "package decoy; public class Second { }");
		}
		JavaProjectScanner scanner = new JavaProjectScanner(ProjectSourcesFinder.discover(projectRoot));
		String derived = scanner.scanForPackageName();

		if (decoy) {
			assertEquals("decoy", derived);
			assertThrows(SecurityException.class, () -> scanner.requireDerivedScopeToCoverTheProject(derived));
		} else {
			assertEquals("de.tum.cit.aet", derived);
			assertDoesNotThrow(() -> scanner.requireDerivedScopeToCoverTheProject(derived));
		}
	}

	/**
	 * Writes a source below a source root and compiles it into the output root.
	 *
	 * @param sources      the source root
	 * @param outputRoot   the output root
	 * @param relativePath the source path below the root
	 * @param source       the Java source
	 * @throws IOException if the fixture cannot be written
	 */
	private static void compileSource(Path sources, Path outputRoot, String relativePath, String source)
			throws IOException {
		Path file = sources.resolve(relativePath);
		Files.createDirectories(file.getParent());
		Files.writeString(file, source);
		assertEquals(0, ToolProvider.getSystemJavaCompiler().run(null, null, null, "-d", outputRoot.toString(),
				file.toString()), "the fixture did not compile: " + relativePath);
	}

	/**
	 * A versioned class is refused even when it defines a class of its own, since
	 * the check does not model which version the JVM picks.
	 */
	@Test
	@DisplayName("Refuses a class file in a versioned directory")
	void refusesAVersionedClassFile() throws IOException {
		Path outputRoot = assignmentOutput();
		Path versioned = Files.createDirectories(outputRoot.resolve("META-INF/versions/17"));
		compileInto(versioned, """
				package de.tum.cit.aet;

				public class OnlyHere {
				}
				""", "versioned/de/tum/cit/aet/OnlyHere.java");

		SecurityException refusal = assertThrows(SecurityException.class,
				() -> scanner(outputRoot).requireDerivedScopeToCoverTheProject("de.tum.cit.aet"));

		assertTrue(refusal.getMessage().contains("OnlyHere.class"), refusal.getMessage());
	}

	/**
	 * A nested class is code that runs like any other. Its enclosing class being
	 * absent, or elsewhere, is no reason to leave its own package unchecked.
	 */
	@Test
	@DisplayName("Refuses a nested class outside the scope whose enclosing class is gone")
	void refusesAnUncoveredOrphanNestedClass() throws IOException {
		Path outputRoot = assignmentOutput();
		compileInto(outputRoot, """
				package decoy;

				public class Outer {
					public static class Inner {
					}
				}
				""", "decoy/Outer.java");
		Files.delete(outputRoot.resolve("decoy/Outer.class"));

		SecurityException refusal = assertThrows(SecurityException.class,
				() -> scanner(outputRoot).requireDerivedScopeToCoverTheProject("de.tum.cit.aet"));

		assertTrue(refusal.getMessage().contains("decoy"), refusal.getMessage());
	}

	/**
	 * What an ordinary build writes beside a class: its nested classes, an
	 * annotated package-info and a module-info. None of it may be mistaken for an
	 * unreadable or duplicate file.
	 */
	@Test
	@DisplayName("Accepts nested classes, package-info and module-info")
	void acceptsNestedClassesAndDescriptors() throws IOException {
		Path outputRoot = Files.createDirectories(projectRoot.resolve("build/classes/java/main"));
		Path sources = Files.createDirectories(projectRoot.resolve("module-sources/de/tum/cit/aet"));
		Files.writeString(sources.resolve("Assignment.java"), """
				package de.tum.cit.aet;

				public class Assignment {
					class Step {
					}

					Runnable task = () -> new Object() {
					};
				}
				""");
		Files.writeString(sources.resolve("package-info.java"), """
				@Deprecated
				package de.tum.cit.aet;
				""");
		Path moduleInfo = Files.writeString(projectRoot.resolve("module-sources/module-info.java"),
				"module assignment { exports de.tum.cit.aet; }");
		int status = ToolProvider.getSystemJavaCompiler().run(null, null, null, "-d", outputRoot.toString(),
				moduleInfo.toString(), sources.resolve("Assignment.java").toString(),
				sources.resolve("package-info.java").toString());
		assertTrue(status == 0, "the fixture did not compile");

		assertDoesNotThrow(() -> scanner(outputRoot).requireDerivedScopeToCoverTheProject("de.tum.cit.aet"));
	}

	/**
	 * Compiles the one assignment class every scope here is meant to cover.
	 *
	 * @return the production output root
	 * @throws IOException if the fixture cannot be written
	 */
	private Path assignmentOutput() throws IOException {
		return compile("""
				package de.tum.cit.aet;

				public class Assignment {
				}
				""", "de/tum/cit/aet/Assignment.java");
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
