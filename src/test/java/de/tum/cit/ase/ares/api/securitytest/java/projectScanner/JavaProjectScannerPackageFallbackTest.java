package de.tum.cit.ase.ares.api.securitytest.java.projectScanner;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import de.tum.cit.ase.ares.api.buildtoolconfiguration.BuildMode;
import de.tum.cit.ase.ares.api.buildtoolconfiguration.BuildToolConfiguration;

/**
 * Covers the supervised-package fallback of {@link JavaProjectScanner}.
 * <p>
 * The scanner must prefer what the project declares over what it is configured
 * to assume, at every step: a package the project does not contain resolves to
 * an analysis directory that does not exist, so no class is imported and no
 * resource domain is enforced, without anything failing. The configured default
 * is reached only where nothing eligible is declared at all, and the cases
 * below pin both halves of that, the detection and the last resort.
 */
@DisplayName("JavaProjectScanner supervised-package fallback")
class JavaProjectScannerPackageFallbackTest {

	@TempDir
	Path projectRoot;

	@Test
	@DisplayName("Derives the package from the compiled output when no source root is discoverable")
	void derivesPackageFromCompiledOutputWhenSourceRootsAreUndiscoverable() throws IOException {
		Path outputRoot = compile("""
				package de.tum.cit.aet;

				public class Calculator {
					public Runnable inner() {
						return new Runnable() {
							@Override
							public void run() {
							}
						};
					}
				}
				""", "de/tum/cit/aet/Calculator.java");

		String packageName = new JavaProjectScanner(configurationWithoutSourceRoots(outputRoot)).scanForPackageName();

		assertEquals("de.tum.cit.aet", packageName);
	}

	@Test
	@DisplayName("Weights the compiled output by top-level class, not by class file")
	void ignoresNestedAndAnonymousClassesWhenWeightingPackages() throws IOException {
		// 'crowded' holds one top-level class plus two extra class files; 'sparse'
		// holds two top-level classes. Counting class files would pick 'crowded'.
		Path outputRoot = compile("""
				package crowded;

				public class Busy {
					class Inner {
					}

					Runnable anonymous() {
						return new Runnable() {
							@Override
							public void run() {
							}
						};
					}
				}
				""", "crowded/Busy.java");
		compileInto(outputRoot, """
				package sparse;

				public class First {
				}
				""", "sparse/First.java");
		compileInto(outputRoot, """
				package sparse;

				public class Second {
				}
				""", "sparse/Second.java");

		String packageName = new JavaProjectScanner(configurationWithoutSourceRoots(outputRoot)).scanForPackageName();

		assertEquals("sparse", packageName);
	}

	/**
	 * A dollar sign in a binary name does not mean the class is nested. It is a
	 * legal identifier character, so a name-based test discards a top-level class
	 * that carries one, and the package it belongs to is undercounted by exactly
	 * the classes an author chose to name that way.
	 */
	@Test
	@DisplayName("Counts a top-level class whose own name contains a dollar sign")
	void countsTopLevelClassesWhoseNameContainsADollarSign() throws IOException {
		// 'zeta' holds two top-level classes, one of them dollar-named; 'alpha' holds
		// one. Discarding the dollar-named class makes it a tie, which the ordering
		// then resolves to 'alpha', so this fixture separates the two behaviours
		// rather than relying on the tie-break.
		Path outputRoot = compile("""
				package zeta;

				public class Payload$Hidden {
				}
				""", "zeta/Payload$Hidden.java");
		compileInto(outputRoot, """
				package zeta;

				public class Plain {
				}
				""", "zeta/Plain.java");
		compileInto(outputRoot, """
				package alpha;

				public class Only {
				}
				""", "alpha/Only.java");

		String packageName = new JavaProjectScanner(configurationWithoutSourceRoots(outputRoot)).scanForPackageName();

		assertEquals("zeta", packageName);
	}

	/**
	 * The compiled output must win over the configured default, so that a project
	 * whose build descriptor cannot be parsed is still scoped to its own package
	 * rather than to a name it may not contain.
	 */
	@Test
	@DisplayName("Prefers the compiled output over the configured default package")
	void prefersCompiledOutputOverTheConfiguredDefault() throws IOException {
		// The compiled package must differ from the configured default, or the
		// assertion would hold just as well when the fallback fired and the test would
		// prove nothing. The other cases here drive the base scanner, whose default is
		// the empty string, so they can use any package name.
		Path outputRoot = compile("""
				package de.tum.cit.detected;

				public class Calculator {
				}
				""", "de/tum/cit/detected/Calculator.java");

		String packageName = new JavaProgrammingExerciseProjectScanner(configurationWithoutSourceRoots(outputRoot))
				.scanForPackageName();

		assertEquals("de.tum.cit.detected", packageName);
		assertNotEquals("de.tum.cit.aet", packageName);
	}

	/**
	 * Only when the project declares nothing at all does the configured default
	 * apply. It is the last resort, not the first answer.
	 */
	@Test
	@DisplayName("Falls back to the configured default only when nothing is detectable")
	void fallsBackToTheConfiguredDefaultOnlyWhenNothingIsDetectable() throws IOException {
		Path outputRoot = Files.createDirectories(projectRoot.resolve("build/classes/java/main"));
		BuildToolConfiguration configuration = configurationWithoutSourceRoots(outputRoot);

		assertEquals("", new JavaProjectScanner(configuration).scanForPackageName());
		assertEquals("de.tum.cit.aet", new JavaProgrammingExerciseProjectScanner(configuration).scanForPackageName());
	}

	@Test
	@DisplayName("Ignores compiled classes in the default package and in reserved packages")
	void ignoresDefaultAndReservedPackagesInTheCompiledOutput() throws IOException {
		Path outputRoot = compile("""
				public class RootLevel {
				}
				""", "RootLevel.java");
		compileInto(outputRoot, """
				package de.tum.cit.ase.ares.api.fake;

				public class Impostor {
				}
				""", "de/tum/cit/ase/ares/api/fake/Impostor.java");
		compileInto(outputRoot, """
				package de.tum.cit.aet;

				public class Calculator {
				}
				""", "de/tum/cit/aet/Calculator.java");

		String packageName = new JavaProjectScanner(configurationWithoutSourceRoots(outputRoot)).scanForPackageName();

		assertEquals("de.tum.cit.aet", packageName);
	}

	@Test
	@DisplayName("Treats an absent or unreadable output root as nothing to detect")
	void treatsAbsentOrUnreadableOutputRootAsNothingToDetect() throws IOException {
		Path absent = projectRoot.resolve("build/classes/java/main");
		assertEquals("", new JavaProjectScanner(configurationWithoutSourceRoots(absent)).scanForPackageName());

		Path outputRoot = compile("""
				package de.tum.cit.aet;

				public class Calculator {
				}
				""", "de/tum/cit/aet/Calculator.java");
		assumeTrue(FileSystems.getDefault().supportedFileAttributeViews().contains("posix"),
				"needs POSIX permissions to make a directory unreadable");
		BuildToolConfiguration configuration = configurationWithoutSourceRoots(outputRoot);
		Files.setPosixFilePermissions(outputRoot, PosixFilePermissions.fromString("---------"));
		try {
			assumeUnreadable(outputRoot);
			assertEquals("", new JavaProjectScanner(configuration).scanForPackageName());
		} finally {
			Files.setPosixFilePermissions(outputRoot, PosixFilePermissions.fromString("rwxr-xr-x"));
		}
	}

	@Test
	@DisplayName("Reports the build mode of the configuration it was given")
	void reportsTheConfiguredBuildMode() throws IOException {
		Path outputRoot = Files.createDirectories(projectRoot.resolve("build/classes/java/main"));

		assertEquals(BuildMode.GRADLE,
				new JavaProjectScanner(configurationWithoutSourceRoots(outputRoot)).scanForBuildMode());
	}

	@Test
	@DisplayName("Rejects a source root that is not a readable directory")
	void rejectsUnreadableSourceRoot() throws IOException {
		Path sourceRoot = Files.createDirectories(projectRoot.resolve("sources-unreadable"));
		Path outputRoot = Files.createDirectories(projectRoot.resolve("build/classes/java/main"));
		BuildToolConfiguration configuration = new BuildToolConfiguration(BuildMode.GRADLE, projectRoot,
				List.of(sourceRoot), List.of(sourceRoot), outputRoot,
				Files.createDirectories(projectRoot.resolve("build/classes/java/test")));
		assumeTrue(FileSystems.getDefault().supportedFileAttributeViews().contains("posix"),
				"needs POSIX permissions to make a directory unreadable");
		Files.setPosixFilePermissions(sourceRoot, PosixFilePermissions.fromString("--x--x--x"));
		try {
			assumeUnreadable(sourceRoot);
			assertThrows(IllegalStateException.class, () -> new JavaProjectScanner(configuration).scanForPackageName());
		} finally {
			Files.setPosixFilePermissions(sourceRoot, PosixFilePermissions.fromString("rwxr-xr-x"));
		}
	}

	/**
	 * Permission bits do not bind a superuser, so clearing them is not by itself a
	 * denial. Without this check such a run would report a failure of the code
	 * under test where the fixture is what could not be established.
	 */
	private static void assumeUnreadable(Path path) {
		ScannerFixtures.assumeUnreadable(path);
	}

	/**
	 * A configuration whose production source roots are empty, which is what an
	 * unparsed build descriptor leaves behind. The test source root doubles as the
	 * required non-empty root list.
	 */
	/**
	 * A source root that is present but only part of the project must not decide
	 * the supervised package. Counting declarations across part of a project
	 * produces an answer indistinguishable from one taken across all of it, so an
	 * incomplete set is skipped in favour of the compiled output, which the build
	 * tool writes whatever the descriptor says.
	 */
	@Test
	@DisplayName("Ignores a present source root the finder reported as only part of the project")
	void ignoresAPresentSourceRootReportedAsIncomplete() throws IOException {
		Path outputRoot = compile("""
				package de.tum.cit.aet;

				public class Compiled {
				}
				""", "de/tum/cit/aet/Compiled.java");
		Path sourceRoot = Files.createDirectories(projectRoot.resolve("partial"));
		Files.writeString(sourceRoot.resolve("Elsewhere.java"), """
				package somewhere.other.entirely;

				public class Elsewhere {
				}
				""");
		BuildToolConfiguration incomplete = new BuildToolConfiguration(BuildMode.GRADLE, projectRoot,
				List.of(sourceRoot), List.of(Files.createDirectories(projectRoot.resolve("test"))), outputRoot,
				Files.createDirectories(projectRoot.resolve("build/classes/java/test")), false);

		String packageName = new JavaProjectScanner(incomplete).scanForPackageName();

		assertEquals("de.tum.cit.aet", packageName);
		assertNotEquals("somewhere.other.entirely", packageName);
	}

	private BuildToolConfiguration configurationWithoutSourceRoots(Path outputRoot) throws IOException {
		return ScannerFixtures.gradleConfigurationWithoutSourceRoots(projectRoot, outputRoot);
	}

	private Path compile(String source, String relativeSourcePath) throws IOException {
		return ScannerFixtures.compile(projectRoot, source, relativeSourcePath);
	}

	private void compileInto(Path outputRoot, String source, String relativeSourcePath) throws IOException {
		ScannerFixtures.compileInto(projectRoot, outputRoot, source, relativeSourcePath);
	}
}
