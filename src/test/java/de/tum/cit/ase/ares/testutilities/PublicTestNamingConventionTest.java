package de.tum.cit.ase.ares.testutilities;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;

/**
 * Build-time safety net for I-111/TD-060: a test source using
 * {@code @PublicTest} whose name does not end in {@code Test.java} is never run
 * by Surefire, which is how {@code FileSystemAccessReadTestOld.java} went
 * unnoticed. Such a file passes only if another test runs it, through
 * {@code @UserBased(Name.class)} or a JUnit launcher's {@code selectClass}. A
 * plain text scan, kept simple on purpose.
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
			if (allSourcesConcatenated.contains("@UserBased(" + simpleClassName + ".class)")
					|| isSelectedByName(allSourcesConcatenated, simpleClassName)) {
				continue;
			}
			offendingFiles.add(path.toString());
		}

		assertTrue(offendingFiles.isEmpty(),
				() -> "The following files use @PublicTest but their filename does not end in 'Test.java' "
						+ "and no test runs them via @UserBased(...) or a launcher's selectClass(...), so they silently never run "
						+ "(see I-111/TD-060 - this is exactly how FileSystemAccessReadTestOld.java went "
						+ "undiscovered): " + offendingFiles);
	}

	/**
	 * Tells whether a test source hands the class to a JUnit launcher by name, as
	 * the fork probes do, through {@code selectClass(Name.class)} or
	 * {@code selectClass("pkg.Name")}. Such a class runs in that launcher's JVM, so
	 * it must not end in {@code Test}, or Surefire would run it a second time.
	 *
	 * @param sources         every test source, concatenated
	 * @param simpleClassName the class's simple name
	 * @return {@code true} if a launcher selects the class by name
	 */
	private static boolean isSelectedByName(String sources, String simpleClassName) {
		return sources.contains("selectClass(" + simpleClassName + ".class)")
				|| Pattern.compile("selectClass\\(\"(?:[\\w$]+\\.)*" + Pattern.quote(simpleClassName) + "\"\\)")
						.matcher(sources).find();
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
