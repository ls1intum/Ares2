package de.tum.cit.ase.ares.documentation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;

/**
 * Checks that the Gradle example carries the source-root snippet exactly as
 * Ares ships it, and applies it, so the copy instructors take from the example
 * is the one Ares reads.
 */
class SourceRootsSnippetCopiesTest {

	/** The repository root, since Maven runs the tests from there. */
	private static final Path REPOSITORY = Path.of("").toAbsolutePath().normalize();

	/** The snippet Ares ships. */
	private static final Path SHIPPED = REPOSITORY.resolve(Path.of("src", "main", "resources", "de", "tum", "cit",
			"ase", "ares", "api", "configuration", "sourceRoots", "GradleSourceRoots.gradle"));

	/** The Gradle example. */
	private static final Path EXAMPLE = REPOSITORY.resolve(Path.of("examples", "ares-exercise-gradle"));

	/**
	 * A file's text with line endings unified, which Git may change on checkout.
	 *
	 * @param file the file
	 * @return its text with LF line endings
	 * @throws IOException if it cannot be read
	 */
	private static String text(Path file) throws IOException {
		return Files.readString(file).replace("\r\n", "\n");
	}

	/**
	 * The example's copy equals the shipped snippet.
	 *
	 * @throws IOException if a file cannot be read
	 */
	@Test
	void theExampleCopyEqualsTheShippedSnippet() throws IOException {
		assertEquals(text(SHIPPED), text(EXAMPLE.resolve(Path.of("gradle", "AresSourceRoots.gradle"))));
	}

	/**
	 * The example's build applies its copy.
	 *
	 * @throws IOException if the build file cannot be read
	 */
	@Test
	void theExampleBuildAppliesTheSnippet() throws IOException {
		assertTrue(text(EXAMPLE.resolve("build.gradle")).contains("apply from: 'gradle/AresSourceRoots.gradle'"));
	}
}
