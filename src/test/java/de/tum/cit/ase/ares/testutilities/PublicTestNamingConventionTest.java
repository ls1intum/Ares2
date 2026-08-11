package de.tum.cit.ase.ares.testutilities;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;

/**
 * Build-time safety net for I-111/TD-060: a test source file that uses
 * {@code @PublicTest} but whose filename does not match Surefire's configured
 * {@code *Test.java} include pattern (used by every profile in {@code pom.xml}
 * that runs this test tree) silently never runs. This is exactly how
 * {@code FileSystemAccessReadTestOld.java} (suffixed {@code TestOld}, 800
 * {@code @PublicTest} methods) went undiscovered for an extended period.
 * <p>
 * One legitimate exception exists: files under {@code integration/testuser/}
 * are not run by Surefire directly but by a {@code *Test.java} wrapper
 * elsewhere in the tree that launches them in-process via
 * {@code @UserBased(...)} ({@link TestUserExtension}, the JUnit Platform
 * Launcher). This check treats such a file as covered only if some
 * {@code *Test.java} file actually references it via
 * {@code @UserBased(<SimpleName>.class)} - if that wiring is missing, the file
 * is flagged just like any other orphan. This is precisely how this test caught
 * {@code MavenConfigurationUser.java}, a second, previously undocumented
 * instance of the same defect class as I-111/TD-060.
 * <p>
 * Description: Scans {@code src/test/java} for {@code .java} files that
 * reference {@code @PublicTest} and asserts each such file's name ends with
 * {@code Test.java}, unless it is demonstrably launched via an
 * {@code @UserBased} wrapper. This is a plain text scan (not a
 * classpath/annotation scan) deliberately kept simple: it only needs to catch a
 * filename that would silently fall outside every profile's
 * {@code <include>**&#47;*Test.java</include>} pattern with no other execution
 * path wired up, not to understand Java semantics precisely.
 *
 * @since 2.0.0
 * @author Markus Paulsen
 */
class PublicTestNamingConventionTest {

	private static final Path TEST_SOURCE_ROOT = Path.of("src", "test", "java");

	@Test
	void everyFileUsingPublicTestHasATestSuffixedFilenameOrAUserBasedWrapper() throws IOException {
		List<Path> allJavaFiles;
		try (Stream<Path> files = Files.walk(TEST_SOURCE_ROOT)) {
			allJavaFiles = files.filter(path -> path.toString().endsWith(".java")).collect(Collectors.toList());
		}

		String allSourcesConcatenated = concatenateSources(allJavaFiles);

		List<String> offendingFiles = new ArrayList<>();
		for (Path path : allJavaFiles) {
			String filename = path.getFileName().toString();
			if (!usesPublicTestAnnotation(path) || filename.endsWith("Test.java")) {
				continue;
			}
			String simpleClassName = filename.substring(0, filename.length() - ".java".length());
			if (allSourcesConcatenated.contains("@UserBased(" + simpleClassName + ".class)")) {
				continue;
			}
			offendingFiles.add(path.toString());
		}

		assertTrue(offendingFiles.isEmpty(),
				() -> "The following files use @PublicTest but their filename does not end in 'Test.java' "
						+ "and no *Test.java file wires them up via @UserBased(...), so they silently never run "
						+ "(see I-111/TD-060 - this is exactly how FileSystemAccessReadTestOld.java went "
						+ "undiscovered): " + offendingFiles);
	}

	private static String concatenateSources(List<Path> paths) {
		StringBuilder builder = new StringBuilder();
		for (Path path : paths) {
			try {
				builder.append(Files.readString(path, StandardCharsets.UTF_8));
			} catch (IOException unreadable) {
				throw new UncheckedIOException("Could not read Java source file: " + path, unreadable);
			}
		}
		return builder.toString();
	}

	private static boolean usesPublicTestAnnotation(Path path) {
		try {
			String content = Files.readString(path, StandardCharsets.UTF_8);
			return content.contains("@PublicTest");
		} catch (IOException unreadable) {
			throw new UncheckedIOException("Could not read Java source file: " + path, unreadable);
		}
	}
}
