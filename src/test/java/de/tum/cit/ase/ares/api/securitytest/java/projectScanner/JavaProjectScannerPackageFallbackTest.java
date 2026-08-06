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

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import de.tum.cit.ase.ares.api.buildtoolconfiguration.BuildMode;
import de.tum.cit.ase.ares.api.buildtoolconfiguration.BuildToolConfiguration;

/**
 * Covers the supervised-package fallback of {@link JavaProjectScanner}.
 * <p>
 * The scanner must derive the supervised package from the project and never
 * invent one. A package that the project does not contain resolves to an
 * analysis directory that does not exist, so no class is imported and no
 * resource domain is enforced, without anything failing.
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
	 * The compiled output must win over the configured default, so that a project
	 * whose build descriptor cannot be parsed is still scoped to its own package
	 * rather than to a name it may not contain.
	 */
	@Test
	@DisplayName("Prefers the compiled output over the configured default package")
	void prefersCompiledOutputOverTheConfiguredDefault() throws IOException {
		Path outputRoot = compile("""
				package de.tum.cit.aet;

				public class Calculator {
				}
				""", "de/tum/cit/aet/Calculator.java");

		String packageName = new JavaProgrammingExerciseProjectScanner(configurationWithoutSourceRoots(outputRoot))
				.scanForPackageName();

		assertEquals("de.tum.cit.aet", packageName);
		assertNotEquals("de.tum.cit.ase", packageName);
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
		assertEquals("de.tum.cit.ase", new JavaProgrammingExerciseProjectScanner(configuration).scanForPackageName());
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
			assertThrows(IllegalStateException.class, () -> new JavaProjectScanner(configuration).scanForPackageName());
		} finally {
			Files.setPosixFilePermissions(sourceRoot, PosixFilePermissions.fromString("rwxr-xr-x"));
		}
	}

	/**
	 * A configuration whose production source roots are empty, which is what an
	 * unparsed build descriptor leaves behind. The test source root doubles as the
	 * required non-empty root list.
	 */
	private BuildToolConfiguration configurationWithoutSourceRoots(Path outputRoot) throws IOException {
		Path testRoot = Files.createDirectories(projectRoot.resolve("test"));
		return new BuildToolConfiguration(BuildMode.GRADLE, projectRoot, List.of(), List.of(testRoot), outputRoot,
				Files.createDirectories(projectRoot.resolve("build/classes/java/test")));
	}

	private Path compile(String source, String relativeSourcePath) throws IOException {
		Path outputRoot = Files.createDirectories(projectRoot.resolve("build/classes/java/main"));
		compileInto(outputRoot, source, relativeSourcePath);
		return outputRoot;
	}

	private void compileInto(Path outputRoot, String source, String relativeSourcePath) throws IOException {
		Path sourceFile = projectRoot.resolve("sources").resolve(relativeSourcePath);
		Files.createDirectories(sourceFile.getParent());
		Files.writeString(sourceFile, source);
		JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
		int status = compiler.run(null, null, null, "-d", outputRoot.toString(), sourceFile.toString());
		if (status != 0) {
			throw new IllegalStateException("Could not compile the fixture " + relativeSourcePath);
		}
	}
}
