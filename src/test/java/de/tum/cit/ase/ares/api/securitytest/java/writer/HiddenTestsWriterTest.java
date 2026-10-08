package de.tum.cit.ase.ares.api.securitytest.java.writer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
import java.util.function.UnaryOperator;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import com.github.javaparser.ParserConfiguration.LanguageLevel;

import de.tum.cit.ase.ares.api.buildtoolconfiguration.BuildMode;
import de.tum.cit.ase.ares.api.buildtoolconfiguration.BuildToolConfiguration;
import de.tum.cit.ase.ares.api.policy.policySubComponents.HiddenTestsConfiguration;
import de.tum.cit.ase.ares.api.policy.policySubComponents.TestBehaviorConfiguration;

/**
 * Checks what the hidden-test writer generates for a precompile exercise, that
 * it refuses entries no test source matches, and that it removes exactly what
 * it wrote again.
 */
class HiddenTestsWriterTest {

	/** The exercise package the copied Ares classes live in. */
	private static final String PACKAGE = "com.example";

	/** The exercise root of each test. */
	@TempDir
	Path projectRoot;

	/** The exercise's test source root. */
	private Path testFolder;

	/**
	 * Creates the exercise's test source folder: a test class holding a nested
	 * class, a comment and a call, and a test class inheriting methods from a
	 * superclass in another package and from interfaces.
	 *
	 * @throws IOException if it cannot be created
	 */
	@BeforeEach
	void setUp() throws IOException {
		testFolder = Files.createDirectories(projectRoot.resolve("src/test/java"));
		Path folder = Files.createDirectories(testFolder.resolve("com/example"));
		Files.writeString(folder.resolve("PenguinTest.java"), """
				package com.example;

				class PenguinTest {
					// ghost() was removed.
					@org.junit.jupiter.api.Test
					void name() {
						Helper.called();
					}

					@org.junit.jupiter.api.Nested
					class Inner {
						@org.junit.jupiter.api.Test
						void deep() {
						}
					}
				}
				""");
		Path base = Files.createDirectories(folder.resolve("base"));
		Files.writeString(base.resolve("AbstractBirdTest.java"), """
				package com.example.base;

				public abstract class AbstractBirdTest {
					@org.junit.jupiter.api.Test
					void flies() {
					}
				}
				""");
		Files.writeString(folder.resolve("Gliding.java"), """
				package com.example;

				public interface Gliding {
					@org.junit.jupiter.api.Test
					default void glides() {
					}
				}
				""");
		Path birds = Files.createDirectories(folder.resolve("birds"));
		Files.writeString(birds.resolve("Flying.java"), """
				package com.example.birds;

				import com.example.*;

				public interface Flying extends Gliding {
				}
				""");
		Files.writeString(folder.resolve("ParrotTest.java"), """
				package com.example;

				import com.example.base.AbstractBirdTest;
				import com.example.birds.Flying;

				class ParrotTest extends AbstractBirdTest implements Flying {
				}
				""");
	}

	/** Without the setting, nothing is generated or registered. */
	@Test
	void writesNothingWithoutTheSetting() {
		GeneratedHookFiles.Contribution generated = writer(null).write(TestBehaviorConfiguration.builder().build(),
				PACKAGE, testFolder);

		assertThat(generated.written()).isEmpty();
		assertThat(generated.jupiterHooks()).isEmpty();
		assertThat(source(HiddenTestsSources.JUPITER_HOOK)).doesNotExist();
	}

	/**
	 * With the setting, the extension and its sentinel are written and the
	 * extension registered.
	 *
	 * @throws IOException if a written file cannot be read
	 */
	@Test
	void writesTheExtensionAndItsSentinel() throws IOException {
		GeneratedHookFiles.Contribution generated = writer(null).write(configured("com.example.PenguinTest#name"),
				PACKAGE, testFolder);

		assertThat(generated.jupiterHooks()).containsExactly(HiddenTestsSources.JUPITER_HOOK);
		assertThat(source(HiddenTestsSources.JUPITER_HOOK)).content().contains("implements InvocationInterceptor")
				.contains(
						"com.example.ares.api.localization.Messages.localized(\"test_guard.hidden_test_before_deadline_message\")")
				.contains("REGARDING_HIDDEN_TESTS_THE_FOLLOWING_TESTS_ARE_HIDDEN");
		assertThat(source(HiddenTestsSources.JUPITER_SENTINEL)).exists();
	}

	/**
	 * Entries naming a class, a method, a nested class, a method in a nested class,
	 * a method inherited from a superclass in another package and one inherited
	 * from an interface through another interface all match.
	 *
	 * @param entry the entry.
	 */
	@ParameterizedTest
	@ValueSource(strings = { "com.example.PenguinTest", "com.example.PenguinTest#name", "com.example.PenguinTest.Inner",
			"com.example.PenguinTest.Inner#deep", "com.example.ParrotTest#flies", "com.example.ParrotTest#glides" })
	void matchingEntriesAreAccepted(String entry) {
		assertThat(writer(null).write(configured(entry), PACKAGE, testFolder).jupiterHooks()).isNotEmpty();
	}

	/**
	 * An entry no test source matches stops the generator, naming the entry: a
	 * method of the enclosing class is not a method of the nested one, and a name
	 * in a comment or a call is not a declaration.
	 *
	 * @param entry the entry.
	 */
	@ParameterizedTest
	@ValueSource(strings = { "com.example.PenguinTset", "com.example.PenguinTest#nmae", "com.example.PenguinTest.Outer",
			"org.example.PenguinTest", "com.example.PenguinTest.Inner#name", "com.example.PenguinTest#ghost",
			"com.example.PenguinTest#called" })
	void anUnmatchedEntryStopsTheGenerator(String entry) {
		assertThatThrownBy(() -> writer(null).write(configured(entry), PACKAGE, testFolder))
				.isInstanceOf(SecurityException.class).hasMessageContaining(entry);
		assertThat(source(HiddenTestsSources.JUPITER_HOOK)).doesNotExist();
	}

	/**
	 * A source whose folder says one package but which declares another stops the
	 * generator under either name, so the hook can never see a different class than
	 * the one the entry was checked against.
	 *
	 * @throws IOException if the source cannot be written
	 */
	@Test
	void aSourceWhosePackageDiffersFromItsFolderIsRefused() throws IOException {
		Path folder = Files.createDirectories(testFolder.resolve("org/example"));
		Files.writeString(folder.resolve("PenguinTest.java"), "package com.example; class PenguinTest {}");
		Files.delete(testFolder.resolve("com/example/PenguinTest.java"));

		assertThatThrownBy(() -> writer(null).write(configured("org.example.PenguinTest"), PACKAGE, testFolder))
				.isInstanceOf(SecurityException.class).hasMessageContaining("org.example.PenguinTest");
		assertThatThrownBy(() -> writer(null).write(configured("com.example.PenguinTest"), PACKAGE, testFolder))
				.isInstanceOf(SecurityException.class).hasMessageContaining("com.example.PenguinTest");
	}

	/**
	 * A listed test using a Java 21 record pattern is read at the version the
	 * exercise's build names.
	 *
	 * @throws IOException if a file cannot be written
	 */
	@Test
	void aListedTestUsingARecordPatternIsReadAtTheExercisesVersion() throws IOException {
		Files.writeString(projectRoot.resolve("pom.xml"),
				"<project><properties><maven.compiler.release>21</maven.compiler.release></properties></project>");
		Files.writeString(testFolder.resolve("com/example/PointTest.java"), """
				package com.example;

				class PointTest {
					record Point(int x, int y) {}

					int sum(Object value) {
						return value instanceof Point(int x, int y) ? x + y : 0;
					}
				}
				""");

		assertThat(ExerciseLanguageLevel.of(projectRoot)).isEqualTo(LanguageLevel.JAVA_21);
		assertThat(writer(null).write(configured("com.example.PointTest#sum"), PACKAGE, testFolder).jupiterHooks())
				.isNotEmpty();
	}

	/**
	 * Without a version in the build, or with one newer than the parser knows, the
	 * sources are read at Java 25; Gradle's toolchain and Maven's source setting
	 * are read as well.
	 *
	 * @throws IOException if a build file cannot be written
	 */
	@Test
	void theLanguageLevelFallsBackToJava25() throws IOException {
		assertThat(ExerciseLanguageLevel.of(projectRoot)).isEqualTo(LanguageLevel.JAVA_25);
		Files.writeString(projectRoot.resolve("build.gradle"),
				"java { toolchain { languageVersion = JavaLanguageVersion.of(17) } }");
		assertThat(ExerciseLanguageLevel.of(projectRoot)).isEqualTo(LanguageLevel.JAVA_17);
		Files.writeString(projectRoot.resolve("pom.xml"),
				"<project><properties><maven.compiler.source>99</maven.compiler.source></properties></project>");
		assertThat(ExerciseLanguageLevel.of(projectRoot)).isEqualTo(LanguageLevel.JAVA_25);
	}

	/**
	 * A test source that is not valid Java stops the generator, naming the file,
	 * rather than letting an entry pass unchecked.
	 *
	 * @throws IOException if the source cannot be written
	 */
	@Test
	void anUnparsableSourceStopsTheGenerator() throws IOException {
		Files.writeString(testFolder.resolve("com/example/BrokenTest.java"), "class BrokenTest {");

		assertThatThrownBy(() -> writer(null).write(configured("com.example.BrokenTest"), PACKAGE, testFolder))
				.isInstanceOf(SecurityException.class).hasMessageContaining("BrokenTest.java");
	}

	/**
	 * Public entries are checked like hidden ones: a matching one is accepted, an
	 * unmatched one stops the generator, naming the list and the entry.
	 */
	@Test
	void publicEntriesAreCheckedLikeHiddenOnes() {
		assertThat(writer(null).write(configuredPublic("com.example.ParrotTest#glides"), PACKAGE, testFolder)
				.jupiterHooks()).isNotEmpty();
		assertThatThrownBy(
				() -> writer(null).write(configuredPublic("com.example.PenguinTest.Inner#name"), PACKAGE, testFolder))
						.isInstanceOf(SecurityException.class)
						.hasMessageContaining("com.example.PenguinTest.Inner#name")
						.hasMessageContaining("theFollowingTestsArePublic");
	}

	/**
	 * The public-list refusal is localised: in German it names the list and the
	 * entry.
	 */
	@Test
	void thePublicRefusalIsLocalisedInGerman() {
		Locale original = Locale.getDefault(Locale.Category.DISPLAY);
		try {
			Locale.setDefault(Locale.Category.DISPLAY, Locale.GERMAN);

			assertThatThrownBy(() -> writer(null).write(configuredPublic("com.example.Nothing"), PACKAGE, testFolder))
					.hasMessageStartingWith("Ares Sicherheitsfehler").hasMessageContaining("com.example.Nothing")
					.hasMessageContaining("theFollowingTestsArePublic");
		} finally {
			Locale.setDefault(Locale.Category.DISPLAY, original);
		}
	}

	/**
	 * The generated hook reads the public list and the switch, and names the
	 * exercise's own generated security tests by their exact names.
	 *
	 * @throws IOException if the written hook cannot be read
	 */
	@Test
	void theHookReadsTheVisibilitySettings() throws IOException {
		writer(null).write(configuredPublic(), PACKAGE, testFolder);

		assertThat(source(HiddenTestsSources.JUPITER_HOOK)).content()
				.contains("REGARDING_HIDDEN_TESTS_THE_FOLLOWING_TESTS_ARE_PUBLIC")
				.contains("REGARDING_HIDDEN_TESTS_UNLISTED_TESTS_ARE_HIDDEN")
				.contains("\"com.example.ares.api.architecture.java.archunit.JavaArchunitTestCase\"")
				.contains("\"com.example.ares.api.architecture.java.wala.JavaWalaTestCase\"")
				.doesNotContain("@EXERCISE_PACKAGE@").doesNotContain("@PUBLIC@").doesNotContain("@UNLISTED@");
	}

	/** The refusal is localised: in German it is the German text. */
	@Test
	void theRefusalIsLocalisedInGerman() {
		Locale original = Locale.getDefault(Locale.Category.DISPLAY);
		try {
			Locale.setDefault(Locale.Category.DISPLAY, Locale.GERMAN);

			assertThatThrownBy(() -> writer(null).write(configured("com.example.Nothing"), PACKAGE, testFolder))
					.hasMessageStartingWith("Ares Sicherheitsfehler").hasMessageContaining("com.example.Nothing");
		} finally {
			Locale.setDefault(Locale.Category.DISPLAY, original);
		}
	}

	/**
	 * Generating twice writes the same files.
	 *
	 * @throws IOException if a written file cannot be read
	 */
	@Test
	void aSecondRunWritesTheSameFiles() throws IOException {
		writer(null).write(configured("com.example.PenguinTest"), PACKAGE, testFolder);
		String hook = Files.readString(source(HiddenTestsSources.JUPITER_HOOK));

		writer(null).write(configured("com.example.PenguinTest"), PACKAGE, testFolder);

		assertThat(Files.readString(source(HiddenTestsSources.JUPITER_HOOK))).isEqualTo(hook);
	}

	/**
	 * Removing the setting removes the extension and its sentinel, source and
	 * compiled, in the Maven layout.
	 *
	 * @throws IOException if a file cannot be written
	 */
	@Test
	void removingTheSettingRemovesTheHookUnderMaven() throws IOException {
		assertRemoval(BuildMode.MAVEN, "pom.xml", "target/classes", "target/test-classes");
	}

	/**
	 * Removing the setting removes the extension and its sentinel, source and
	 * compiled, in the Gradle layout.
	 *
	 * @throws IOException if a file cannot be written
	 */
	@Test
	void removingTheSettingRemovesTheHookUnderGradle() throws IOException {
		assertRemoval(BuildMode.GRADLE, "build.gradle", "build/classes/java/main", "build/classes/java/test");
	}

	/**
	 * Generates the hook in a build layout, then removes the setting and checks
	 * that source and compiled class are gone.
	 *
	 * @param buildMode  the build tool.
	 * @param descriptor the build file.
	 * @param classes    the production output folder.
	 * @param tests      the test output folder.
	 * @throws IOException if a file cannot be written
	 */
	private void assertRemoval(BuildMode buildMode, String descriptor, String classes, String tests)
			throws IOException {
		for (String directory : List.of("src/main/java", classes, tests)) {
			Files.createDirectories(projectRoot.resolve(directory));
		}
		Files.writeString(projectRoot.resolve(descriptor), "");
		BuildToolConfiguration layout = new BuildToolConfiguration(buildMode, projectRoot,
				List.of(projectRoot.resolve("src/main/java")), List.of(testFolder), projectRoot.resolve(classes),
				projectRoot.resolve(tests));
		writer(layout).write(configured("com.example.PenguinTest"), PACKAGE, testFolder);
		Path compiled = projectRoot.resolve(tests + "/de/tum/cit/ase/ares/generated/GeneratedHiddenTests.class");
		Files.createDirectories(compiled.getParent());
		Files.write(compiled, new byte[] { (byte) 0xCA, (byte) 0xFE });

		writer(layout).write(TestBehaviorConfiguration.builder().build(), PACKAGE, testFolder);

		assertThat(source(HiddenTestsSources.JUPITER_HOOK)).doesNotExist();
		assertThat(source(HiddenTestsSources.JUPITER_SENTINEL)).doesNotExist();
		assertThat(compiled).doesNotExist();
	}

	/**
	 * The writer for this test's exercise, confining nothing.
	 *
	 * @param layout the build layout, or null.
	 * @return the writer
	 */
	private HiddenTestsWriter writer(BuildToolConfiguration layout) {
		return new HiddenTestsWriter(new GeneratedHookFiles(projectRoot, layout, UnaryOperator.identity()),
				ExerciseLanguageLevel.of(projectRoot));
	}

	/**
	 * A configuration hiding the given tests.
	 *
	 * @param entries the hidden-test entries.
	 * @return the configuration
	 */
	private static TestBehaviorConfiguration configured(String... entries) {
		return TestBehaviorConfiguration.builder().regardingHiddenTests(HiddenTestsConfiguration.builder()
				.theDeadlineIs("2000-01-01 00:00 UTC").unlistedTestsAreHidden(false).hiddenTests(entries).build())
				.build();
	}

	/**
	 * A configuration hiding unlisted tests and making the given tests public.
	 *
	 * @param entries the public-test entries.
	 * @return the configuration
	 */
	private static TestBehaviorConfiguration configuredPublic(String... entries) {
		return TestBehaviorConfiguration.builder().regardingHiddenTests(HiddenTestsConfiguration.builder()
				.theDeadlineIs("2000-01-01 00:00 UTC").unlistedTestsAreHidden(true).publicTests(entries).build())
				.build();
	}

	/**
	 * Where a generated class's source belongs.
	 *
	 * @param simpleName the class's simple name.
	 * @return the source path
	 */
	private Path source(String simpleName) {
		return testFolder.resolve("de/tum/cit/ase/ares/generated").resolve(simpleName + ".java");
	}
}
