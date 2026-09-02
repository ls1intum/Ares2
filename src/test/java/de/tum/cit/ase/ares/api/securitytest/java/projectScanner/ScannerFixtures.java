package de.tum.cit.ase.ares.api.securitytest.java.projectScanner;

import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;

import de.tum.cit.ase.ares.api.buildtoolconfiguration.BuildMode;
import de.tum.cit.ase.ares.api.buildtoolconfiguration.BuildToolConfiguration;

/**
 * The compiled fixtures the scanner tests are built from.
 * <p>
 * Both scanner test classes need real class files rather than hand-written
 * bytes, because what they assert is read from the class file itself: the
 * package a class declares, and whether it is top-level. They also both need
 * the conventional Gradle output layout, since that is where the scanner looks.
 * Keeping one copy means a change to that layout is made once rather than
 * twice, and the second copy cannot quietly drift.
 * <p>
 * The environment assumptions live here for the same reason, and they are
 * deliberately assumptions rather than failures: a file system that cannot
 * withdraw read permission cannot exercise an unreadable root, and that is the
 * sandbox being unable to pose the question, not the answer being wrong.
 */
final class ScannerFixtures {

	/** Where a Gradle build writes its production classes. */
	static final String PRODUCTION_OUTPUT = "build/classes/java/main";
	/** Where a Gradle build writes its test classes. */
	static final String TEST_OUTPUT = "build/classes/java/test";

	private ScannerFixtures() {
		throw new IllegalStateException("ScannerFixtures is a fixture holder and should not be instantiated");
	}

	/**
	 * Compiles one source into a fresh production output root and answers that
	 * root.
	 *
	 * @param projectRoot        the temporary project
	 * @param source             the Java source to compile
	 * @param relativeSourcePath where the source sits, relative to the source tree
	 * @return the production output root
	 * @throws IOException if the fixture cannot be written
	 */
	static Path compile(Path projectRoot, String source, String relativeSourcePath) throws IOException {
		Path outputRoot = Files.createDirectories(projectRoot.resolve(PRODUCTION_OUTPUT));
		compileInto(projectRoot, outputRoot, source, relativeSourcePath);
		return outputRoot;
	}

	/**
	 * Compiles one more source into an output root that already exists, so a test
	 * can build an output tree out of several classes.
	 *
	 * @param projectRoot        the temporary project
	 * @param outputRoot         the output root to compile into
	 * @param source             the Java source to compile
	 * @param relativeSourcePath where the source sits, relative to the source tree
	 * @throws IOException if the fixture cannot be written
	 */
	static void compileInto(Path projectRoot, Path outputRoot, String source, String relativeSourcePath)
			throws IOException {
		Path sourceFile = projectRoot.resolve("sources").resolve(relativeSourcePath);
		Files.createDirectories(sourceFile.getParent());
		Files.writeString(sourceFile, source);
		JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
		int status = compiler.run(null, null, null, "-d", outputRoot.toString(), sourceFile.toString());
		if (status != 0) {
			throw new IllegalStateException("Could not compile the fixture " + relativeSourcePath);
		}
	}

	/**
	 * A Gradle configuration with no production source roots, so that the scanner
	 * has nothing to read but the compiled output.
	 *
	 * @param projectRoot the temporary project
	 * @param outputRoot  the production output root
	 * @return the configuration
	 * @throws IOException if the test output root cannot be created
	 */
	static BuildToolConfiguration gradleConfigurationWithoutSourceRoots(Path projectRoot, Path outputRoot)
			throws IOException {
		Path testRoot = Files.createDirectories(projectRoot.resolve("test"));
		return new BuildToolConfiguration(BuildMode.GRADLE, projectRoot, List.of(), List.of(testRoot), outputRoot,
				Files.createDirectories(projectRoot.resolve(TEST_OUTPUT)));
	}

	/**
	 * Skips the calling test where the file system cannot express an unreadable
	 * directory, which distinguishes a sandbox that cannot pose the question from a
	 * fixture that failed to answer it.
	 */
	static void assumePosix() {
		assumeTrue(FileSystems.getDefault().supportedFileAttributeViews().contains("posix"),
				"withdrawing read permission needs POSIX file attributes");
	}

	/**
	 * Skips the calling test where the path is still readable after permission was
	 * withdrawn, which happens when the tests run as a user that ignores it.
	 *
	 * @param path the path that should have become unreadable
	 */
	static void assumeUnreadable(Path path) {
		assumeTrue(!Files.isReadable(path), () -> "cannot make " + path + " unreadable in this environment");
	}
}
