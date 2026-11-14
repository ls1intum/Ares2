package de.tum.cit.ase.ares.api.securitytest.java.writer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.Locale;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

/**
 * Checks that the generated hooks are refused for an exercise whose build file
 * names a JUnit older than 5.12 or a version that is not a number, or cannot be
 * read, under Maven and Gradle, and accepted at 5.12, above it, and when the
 * version comes from elsewhere.
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
	 * A version taken from a property, a range, a catalogue or a parent, and a
	 * project without a build file, are accepted.
	 *
	 * @param buildFile the build file's name.
	 * @param text      the build file's text.
	 * @throws IOException if a build file cannot be written
	 */
	@ParameterizedTest
	@CsvSource(delimiter = '|', value = {
			"pom.xml|<project><dependencies><dependency><artifactId>junit-jupiter</artifactId><version>${junit.version}</version></dependency></dependencies></project>",
			"pom.xml|<project><dependencies><dependency><artifactId>junit-jupiter</artifactId><version>[5.12,6)</version></dependency></dependencies></project>",
			"pom.xml|<project><dependencies><dependency><artifactId>junit-jupiter</artifactId><version>5.${junit.minor}.0</version></dependency></dependencies></project>",
			"pom.xml|<project><dependencies><dependency><artifactId>junit-jupiter</artifactId><scope>test</scope></dependency></dependencies></project>",
			"build.gradle|dependencies { testImplementation libs.junit.jupiter }",
			"build.gradle|dependencies { testImplementation \"org.junit.jupiter:junit-jupiter:$junitVersion\" }",
			"build.gradle.kts|dependencies { testImplementation(\"org.junit.jupiter:junit-jupiter:${junitVersion}\") }" })
	void aVersionFromElsewhereIsAccepted(String buildFile, String text) throws IOException {
		assertThatCode(() -> ExerciseJUnitVersion.requireIncludeFilterSupport(projectRoot)).doesNotThrowAnyException();
		write(buildFile, text);
		assertThatCode(() -> ExerciseJUnitVersion.requireIncludeFilterSupport(projectRoot)).doesNotThrowAnyException();
	}

	/**
	 * A version the exercise sets in its own pom properties, Gradle variables,
	 * {@code gradle.properties} or catalogue is looked up: below 5.12 it is refused
	 * in English and German, at 5.12 it is accepted.
	 *
	 * @param firstFile  the first file's path.
	 * @param firstText  its text, with {@code %s} for the version.
	 * @param secondFile the second file's path, or {@code -} for none.
	 * @param secondText its text, with {@code %s} for the version.
	 * @throws IOException if a file cannot be written
	 */
	@ParameterizedTest
	@CsvSource(delimiter = '|', value = {
			"pom.xml|<project><properties><junit.version>%s</junit.version></properties><dependencies><dependency><artifactId>junit-jupiter</artifactId><version>${junit.version}</version></dependency></dependencies></project>|-|-",
			"pom.xml|<project><properties><junit.version>${junit.base}</junit.version><junit.base>%s</junit.base></properties><dependencies><dependency><artifactId>junit-bom</artifactId><version>${junit.version}</version></dependency></dependencies></project>|-|-",
			"build.gradle|dependencies { testImplementation \"org.junit.jupiter:junit-jupiter:$junitVersion\" }|gradle.properties|junitVersion=%s",
			"build.gradle|ext.junitVersion = '%s'\\ndependencies { testImplementation \"org.junit.jupiter:junit-jupiter:${junitVersion}\" }|-|-",
			"build.gradle.kts|val junitVersion = \"%s\"\\ndependencies { testImplementation(\"org.junit.jupiter:junit-jupiter:$junitVersion\") }|-|-",
			"build.gradle|dependencies { testImplementation libs.junit.jupiter }|gradle/libs.versions.toml|[versions]\\njunit = \"%s\"\\n[libraries]\\njunit-jupiter = { module = \"org.junit.jupiter:junit-jupiter\", version.ref = \"junit\" }",
			"build.gradle.kts|dependencies { testImplementation(\"org.junit.jupiter:junit-jupiter:${libs.versions.junit.get()}\") }|gradle/libs.versions.toml|[versions]\\njunit = \"%s\"",
			"build.gradle|dependencies { testImplementation platform(libs.junit.bom) }|gradle/libs.versions.toml|[libraries]\\njunit-bom = { group = \"org.junit\", name = \"junit-bom\", version = \"%s\" }" })
	void aVersionTheExerciseSetsIsLookedUp(String firstFile, String firstText, String secondFile, String secondText)
			throws IOException {
		writeFiles(firstFile, firstText.formatted("5.11.4"), secondFile, secondText.formatted("5.11.4"));
		assertThatThrownBy(() -> ExerciseJUnitVersion.requireIncludeFilterSupport(projectRoot))
				.isInstanceOf(SecurityException.class).hasMessageStartingWith("Ares Security Error")
				.hasMessageContaining("5.11.4");
		inGerman(() -> assertThatThrownBy(() -> ExerciseJUnitVersion.requireIncludeFilterSupport(projectRoot))
				.hasMessageStartingWith("Ares Sicherheitsfehler").hasMessageContaining("5.11.4"));
		writeFiles(firstFile, firstText.formatted("5.12.0"), secondFile, secondText.formatted("5.12.0"));
		assertThatCode(() -> ExerciseJUnitVersion.requireIncludeFilterSupport(projectRoot)).doesNotThrowAnyException();
	}

	/**
	 * A reference the exercise's own files do not define, or one that loops, is
	 * accepted, since Ares cannot tell the version.
	 *
	 * @param firstFile  the first file's path.
	 * @param firstText  its text.
	 * @param secondFile the second file's path, or {@code -} for none.
	 * @param secondText its text.
	 * @throws IOException if a file cannot be written
	 */
	@ParameterizedTest
	@CsvSource(delimiter = '|', value = {
			"pom.xml|<project><properties><a>${b}</a><b>${a}</b></properties><dependencies><dependency><artifactId>junit-jupiter</artifactId><version>${a}</version></dependency></dependencies></project>|-|-",
			"pom.xml|<project><dependencies><dependency><artifactId>junit-jupiter</artifactId><version>${project.parent.version}</version></dependency></dependencies></project>|-|-",
			"build.gradle|ext.a = \"$b\"\\next.b = \"$a\"\\ndependencies { testImplementation \"org.junit.jupiter:junit-jupiter:$a\" }|-|-",
			"build.gradle|dependencies { testImplementation libs.junit.jupiter }|gradle/libs.versions.toml|[libraries]\\njunit-jupiter = { module = \"org.junit.jupiter:junit-jupiter\", version.ref = \"missing\" }" })
	void aReferenceAresCannotLookUpIsAccepted(String firstFile, String firstText, String secondFile, String secondText)
			throws IOException {
		writeFiles(firstFile, firstText, secondFile, secondText);
		assertThatCode(() -> ExerciseJUnitVersion.requireIncludeFilterSupport(projectRoot)).doesNotThrowAnyException();
	}

	/**
	 * A commented-out example naming a placeholder is ignored, while the active
	 * dependency still decides.
	 *
	 * @param buildFile the build file's name.
	 * @param text      the build file's text, with the active version as a slot.
	 * @throws IOException if the build file cannot be written
	 */
	@ParameterizedTest
	@CsvSource(delimiter = '|', value = {
			"pom.xml|<project><!-- <dependency><artifactId>junit-jupiter</artifactId><version>VERSION</version></dependency> --><dependencies><dependency><artifactId>junit-jupiter</artifactId><version>%s</version></dependency></dependencies></project>",
			"build.gradle|dependencies { testImplementation 'org.junit.jupiter:junit-jupiter:%s' } // testImplementation 'org.junit.jupiter:junit-jupiter:VERSION'",
			"build.gradle.kts|/* testImplementation(\"org.junit.jupiter:junit-jupiter:VERSION\") */ dependencies { testImplementation(\"org.junit.jupiter:junit-jupiter:%s\") }" })
	void aCommentedOutExampleIsIgnored(String buildFile, String text) throws IOException {
		write(buildFile, text.formatted("5.12.2"));
		assertThatCode(() -> ExerciseJUnitVersion.requireIncludeFilterSupport(projectRoot)).doesNotThrowAnyException();
		write(buildFile, text.formatted("5.10.0"));
		assertThatThrownBy(() -> ExerciseJUnitVersion.requireIncludeFilterSupport(projectRoot))
				.isInstanceOf(SecurityException.class).hasMessageContaining("5.10.0");
	}

	/**
	 * The slashes of a URL or a file pattern do not start a comment, so a version
	 * after them on the same line is still read.
	 *
	 * @throws IOException if the build file cannot be written
	 */
	@Test
	void slashesOutsideACommentHideNothing() throws IOException {
		write("build.gradle",
				"repositories { maven { url 'https://repo.example/m2' } } "
						+ "sourceSets { test { java { include '**/*.java' } } } "
						+ "dependencies { testImplementation 'org.junit.jupiter:junit-jupiter:5.10.0' } // */");
		assertThatThrownBy(() -> ExerciseJUnitVersion.requireIncludeFilterSupport(projectRoot))
				.isInstanceOf(SecurityException.class).hasMessageContaining("5.10.0");
	}

	/**
	 * A Maven or Gradle build naming a JUnit version that is not a number is
	 * refused, naming the build file and the value, in English and German.
	 *
	 * @param buildFile the build file's name.
	 * @param text      the build file's text.
	 * @param value     the value it names.
	 * @throws IOException if the build file cannot be written
	 */
	@ParameterizedTest
	@CsvSource(delimiter = '|', value = {
			"pom.xml|<project><dependencies><dependency><artifactId>junit-jupiter</artifactId><version>five</version></dependency></dependencies></project>|five",
			"pom.xml|<project><dependencies><dependency><artifactId>junit-bom</artifactId><version>99999999999.1</version></dependency></dependencies></project>|99999999999.1",
			"build.gradle|dependencies { testImplementation 'org.junit.jupiter:junit-jupiter:latest.release' }|latest.release",
			"build.gradle.kts|dependencies { testImplementation(platform(\"org.junit:junit-bom:5.x\")) }|5.x" })
	void aVersionThatIsNotANumberIsRefused(String buildFile, String text, String value) throws IOException {
		write(buildFile, text);
		assertThatThrownBy(() -> ExerciseJUnitVersion.requireIncludeFilterSupport(projectRoot))
				.isInstanceOf(SecurityException.class).hasMessageStartingWith("Ares Security Error")
				.hasMessageContaining(buildFile).hasMessageContaining("\"" + value + "\"");
		inGerman(() -> assertThatThrownBy(() -> ExerciseJUnitVersion.requireIncludeFilterSupport(projectRoot))
				.hasMessageStartingWith("Ares Sicherheitsfehler").hasMessageContaining(buildFile)
				.hasMessageContaining("\"" + value + "\""));
	}

	/**
	 * A Maven or Gradle build file that exists but is not a readable file is
	 * refused, naming it, in English and German.
	 *
	 * @param buildFile the build file's name.
	 * @throws IOException if the folder cannot be created
	 */
	@ParameterizedTest
	@CsvSource({ "pom.xml", "build.gradle", "build.gradle.kts" })
	void aBuildFileThatCannotBeReadIsRefused(String buildFile) throws IOException {
		Files.createDirectory(projectRoot.resolve(buildFile));
		assertThatThrownBy(() -> ExerciseJUnitVersion.requireIncludeFilterSupport(projectRoot))
				.isInstanceOf(SecurityException.class).hasMessageStartingWith("Ares Security Error")
				.hasMessageContaining(buildFile).hasMessageContaining("cannot be read");
		inGerman(() -> assertThatThrownBy(() -> ExerciseJUnitVersion.requireIncludeFilterSupport(projectRoot))
				.hasMessageStartingWith("Ares Sicherheitsfehler").hasMessageContaining(buildFile)
				.hasMessageContaining("nicht gelesen"));
	}

	/**
	 * The refusal is localised in German too.
	 *
	 * @throws IOException if the build file cannot be written
	 */
	@Test
	void theRefusalIsLocalisedInGerman() throws IOException {
		write("build.gradle", "dependencies { testImplementation 'org.junit.jupiter:junit-jupiter:5.10.2' }");
		inGerman(() -> assertThatThrownBy(() -> ExerciseJUnitVersion.requireIncludeFilterSupport(projectRoot))
				.hasMessageStartingWith("Ares Sicherheitsfehler").hasMessageContaining("5.10.2"));
	}

	/**
	 * Version numbers compare by major and minor, a qualifier does not count, and
	 * one too long to read is refused.
	 */
	@Test
	void versionsCompareByMajorAndMinor() {
		assertThat(ExerciseJUnitVersion.isTooOld("4.13")).isTrue();
		assertThat(ExerciseJUnitVersion.isTooOld("5.9.3")).isTrue();
		assertThat(ExerciseJUnitVersion.isTooOld("5.12")).isFalse();
		assertThat(ExerciseJUnitVersion.isTooOld("5.13.0")).isFalse();
		assertThat(ExerciseJUnitVersion.isTooOld("5.12.0-M1")).isFalse();
		assertThat(ExerciseJUnitVersion.isTooOld("5.11-RC1")).isTrue();
		assertThat(ExerciseJUnitVersion.isTooOld("99999999999999999999.1")).isTrue();
	}

	/**
	 * Runs a check with German as the display language, then restores the one
	 * before.
	 *
	 * @param check the check to run.
	 */
	private static void inGerman(Runnable check) {
		Locale original = Locale.getDefault(Locale.Category.DISPLAY);
		try {
			Locale.setDefault(Locale.Category.DISPLAY, Locale.GERMAN);
			check.run();
		} finally {
			Locale.setDefault(Locale.Category.DISPLAY, original);
		}
	}

	/**
	 * Empties the project folder, then writes up to two files, turning each written
	 * {@code \\n} into a line break.
	 *
	 * @param firstFile  the first file's path.
	 * @param firstText  its text.
	 * @param secondFile the second file's path, or {@code -} for none.
	 * @param secondText its text.
	 * @throws IOException if a file cannot be written or deleted
	 */
	private void writeFiles(String firstFile, String firstText, String secondFile, String secondText)
			throws IOException {
		try (Stream<Path> existing = Files.walk(projectRoot)) {
			for (Path path : existing.sorted(Comparator.reverseOrder()).toList()) {
				if (!path.equals(projectRoot)) {
					Files.delete(path);
				}
			}
		}
		writeOne(firstFile, firstText);
		if (!"-".equals(secondFile)) {
			writeOne(secondFile, secondText);
		}
	}

	/**
	 * Writes one file below the project folder, creating its folders.
	 *
	 * @param file the file's path.
	 * @param text its text, with {@code \\n} for a line break.
	 * @throws IOException if the file cannot be written
	 */
	private void writeOne(String file, String text) throws IOException {
		Path path = projectRoot.resolve(file);
		Files.createDirectories(path.getParent());
		Files.writeString(path, text.replace("\\n", "\n"));
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
			Path existing = projectRoot.resolve(other);
			if (!Files.isDirectory(existing)) {
				Files.deleteIfExists(existing);
			}
		}
		Files.writeString(projectRoot.resolve(buildFile), text);
	}
}
