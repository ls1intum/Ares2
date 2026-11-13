package de.tum.cit.ase.ares.api.securitytest.java.writer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

/**
 * Checks that the generated hooks are refused for an exercise whose build file
 * names a JUnit older than 5.12, under Maven and Gradle, and accepted at 5.12,
 * above it, and when the build names no version.
 */
class ExerciseJUnitVersionTest {

	/** The exercise's root folder. */
	@TempDir
	Path projectRoot;

	/**
	 * A Maven or Gradle build naming JUnit below 5.12 is refused, naming the
	 * version; at 5.12 and above it is accepted.
	 *
	 * @param buildFile the build file's name.
	 * @param text      the build file's text, with {@code %s} for the version.
	 * @throws IOException if the build file cannot be written
	 */
	@ParameterizedTest
	@CsvSource(delimiter = '|', value = {
			"pom.xml|<project><dependencies><dependency><groupId>org.junit.jupiter</groupId><artifactId>junit-jupiter</artifactId><version>%s</version></dependency></dependencies></project>",
			"pom.xml|<project><dependencyManagement><dependencies><dependency><groupId>org.junit</groupId><artifactId>junit-bom</artifactId><version>%s</version><type>pom</type></dependency></dependencies></dependencyManagement></project>",
			"build.gradle|dependencies { testImplementation 'org.junit.jupiter:junit-jupiter:%s' }",
			"build.gradle.kts|dependencies { testImplementation(platform(\"org.junit:junit-bom:%s\")) }" })
	void theVersionDecides(String buildFile, String text) throws IOException {
		write(buildFile, text.formatted("5.11.4"));
		assertThatThrownBy(() -> ExerciseJUnitVersion.requireIncludeFilterSupport(projectRoot))
				.isInstanceOf(SecurityException.class).hasMessageContaining("5.11.4");
		write(buildFile, text.formatted("5.12.0"));
		assertThatCode(() -> ExerciseJUnitVersion.requireIncludeFilterSupport(projectRoot)).doesNotThrowAnyException();
		write(buildFile, text.formatted("6.0.1"));
		assertThatCode(() -> ExerciseJUnitVersion.requireIncludeFilterSupport(projectRoot)).doesNotThrowAnyException();
	}

	/**
	 * A build without a version it names directly, such as one taken from a
	 * property or a catalogue, and a project without a build file, are accepted.
	 *
	 * @throws IOException if a build file cannot be written
	 */
	@Test
	void anUnreadableVersionIsAccepted() throws IOException {
		assertThatCode(() -> ExerciseJUnitVersion.requireIncludeFilterSupport(projectRoot)).doesNotThrowAnyException();
		write("pom.xml",
				"<project><dependencies><dependency><artifactId>junit-jupiter</artifactId><version>${junit.version}</version></dependency></dependencies></project>");
		write("build.gradle", "dependencies { testImplementation libs.junit.jupiter }");
		assertThatCode(() -> ExerciseJUnitVersion.requireIncludeFilterSupport(projectRoot)).doesNotThrowAnyException();
	}

	/**
	 * The refusal is localised in German too.
	 *
	 * @throws IOException if the build file cannot be written
	 */
	@Test
	void theRefusalIsLocalisedInGerman() throws IOException {
		write("build.gradle", "dependencies { testImplementation 'org.junit.jupiter:junit-jupiter:5.10.2' }");
		Locale original = Locale.getDefault(Locale.Category.DISPLAY);
		try {
			Locale.setDefault(Locale.Category.DISPLAY, Locale.GERMAN);
			assertThatThrownBy(() -> ExerciseJUnitVersion.requireIncludeFilterSupport(projectRoot))
					.hasMessageStartingWith("Ares Sicherheitsfehler").hasMessageContaining("5.10.2");
		} finally {
			Locale.setDefault(Locale.Category.DISPLAY, original);
		}
	}

	/**
	 * Version numbers compare by major and minor; one too long to read is not
	 * refused.
	 */
	@Test
	void versionsCompareByMajorAndMinor() {
		assertThat(ExerciseJUnitVersion.isTooOld("4.13")).isTrue();
		assertThat(ExerciseJUnitVersion.isTooOld("5.9.3")).isTrue();
		assertThat(ExerciseJUnitVersion.isTooOld("5.12")).isFalse();
		assertThat(ExerciseJUnitVersion.isTooOld("5.13.0")).isFalse();
		assertThat(ExerciseJUnitVersion.isTooOld("99999999999999999999.1")).isFalse();
	}

	/**
	 * Writes one build file and removes the others.
	 *
	 * @param buildFile the build file's name.
	 * @param text      its text.
	 * @throws IOException if a file cannot be written or deleted
	 */
	private void write(String buildFile, String text) throws IOException {
		for (String other : new String[] { "pom.xml", "build.gradle", "build.gradle.kts" }) {
			Files.deleteIfExists(projectRoot.resolve(other));
		}
		Files.writeString(projectRoot.resolve(buildFile), text);
	}
}
