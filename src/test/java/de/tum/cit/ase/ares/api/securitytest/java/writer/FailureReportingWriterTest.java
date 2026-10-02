package de.tum.cit.ase.ares.api.securitytest.java.writer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.function.UnaryOperator;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import de.tum.cit.ase.ares.api.buildtoolconfiguration.BuildMode;
import de.tum.cit.ase.ares.api.buildtoolconfiguration.BuildToolConfiguration;
import de.tum.cit.ase.ares.api.policy.policySubComponents.PrivilegedExceptionsConfiguration;
import de.tum.cit.ase.ares.api.policy.policySubComponents.TestBehaviorConfiguration;

/**
 * Checks what the precompile failure-reporting writer adds to an exercise, and
 * that it removes exactly that again without touching the instructor's files.
 */
class FailureReportingWriterTest {

	/** The exercise package the copied Ares classes live in. */
	private static final String PACKAGE = "com.example";

	/** Fully qualified name of the generated JUnit extension. */
	private static final String JUPITER_HOOK = "de.tum.cit.ase.ares.generated.GeneratedFailureReporting";

	/** Fully qualified name of the generated jqwik hook. */
	private static final String JQWIK_HOOK = "de.tum.cit.ase.ares.generated.GeneratedJqwikFailureReporting";

	/** An extension the instructor registered before generation. */
	private static final String INSTRUCTOR_EXTENSION = "com.example.InstructorExtension";

	/** The exercise root of each test. */
	@TempDir
	Path projectRoot;

	/** The exercise's test source root. */
	private Path testFolder;

	/** The exercise's test resources root. */
	private Path resources;

	/**
	 * Creates the exercise folders and a Gradle build without jqwik.
	 *
	 * @throws IOException if the folders cannot be created
	 */
	@BeforeEach
	void setUp() throws IOException {
		testFolder = Files.createDirectories(projectRoot.resolve("src/test/java"));
		resources = Files.createDirectories(projectRoot.resolve("src/test/resources"));
		Files.writeString(projectRoot.resolve("build.gradle"), "plugins { id 'java' }");
	}

	/** With the setting absent, nothing is written. */
	@Test
	void writesNothingWhenTheSettingIsAbsent() {
		List<Path> written = writer(null).write(TestBehaviorConfiguration.builder().build(), PACKAGE, testFolder,
				resources);

		assertThat(written).isEmpty();
		assertThat(generatedSource("GeneratedFailureReporting")).doesNotExist();
		assertThat(resources.resolve("junit-platform.properties")).doesNotExist();
	}

	/** With the setting false, nothing is written. */
	@Test
	void writesNothingWhenTheSettingIsFalse() {
		List<Path> written = writer(null).write(configuration(false), PACKAGE, testFolder, resources);

		assertThat(written).isEmpty();
		assertThat(generatedSource("GeneratedFailureReporting")).doesNotExist();
	}

	/**
	 * With the setting on, the JUnit hook, its sentinel, its service entry and the
	 * restricted auto-detection are written, and no jqwik file without jqwik.
	 *
	 * @throws IOException if a written file cannot be read
	 */
	@Test
	void writesTheJupiterHookWhenTheSettingIsOn() throws IOException {
		writer(null).write(configuration(true), PACKAGE, testFolder, resources);

		assertThat(generatedSource("GeneratedFailureReporting")).content()
				.contains("implements TestExecutionExceptionHandler, LifecycleMethodExecutionExceptionHandler")
				.contains("com.example.ares.api.localization.Messages.localized");
		assertThat(generatedSource("GeneratedFailureReportingSentinelTest")).exists();
		assertThat(Files.readString(jupiterServices())).contains(JUPITER_HOOK);
		assertThat(Files.readString(resources.resolve("junit-platform.properties")))
				.contains("junit.jupiter.extensions.autodetection.enabled=true")
				.contains("junit.jupiter.extensions.autodetection.include=" + JUPITER_HOOK);
		assertThat(generatedSource("GeneratedJqwikFailureReporting")).doesNotExist();
		assertThat(resources.resolve(FailureReportingWriter.JQWIK_SERVICE_FILE)).doesNotExist();
	}

	/**
	 * A build that uses jqwik also gets the jqwik hook, its sentinel and its
	 * service entry.
	 *
	 * @throws IOException if a file cannot be written or read
	 */
	@Test
	void writesTheJqwikHookWhenTheBuildUsesJqwik() throws IOException {
		Files.writeString(projectRoot.resolve("build.gradle"), "dependencies { testImplementation 'net.jqwik:jqwik' }");

		writer(null).write(configuration(true), PACKAGE, testFolder, resources);

		assertThat(generatedSource("GeneratedJqwikFailureReporting")).content()
				.contains("implements AroundPropertyHook");
		assertThat(generatedSource("GeneratedJqwikFailureReportingSentinelTest")).exists();
		assertThat(Files.readString(resources.resolve(FailureReportingWriter.JQWIK_SERVICE_FILE))).contains(JQWIK_HOOK);
	}

	/**
	 * A mention of jqwik that is not its group id, such as in a comment, writes no
	 * jqwik hook, and the JUnit sentinel then checks for a jqwik it was not told
	 * of.
	 *
	 * @throws IOException if a file cannot be written or read
	 */
	@Test
	void aMereMentionOfJqwikWritesNoJqwikHook() throws IOException {
		Files.writeString(projectRoot.resolve("build.gradle"), "// jqwik later, maybe");

		writer(null).write(configuration(true), PACKAGE, testFolder, resources);

		assertThat(generatedSource("GeneratedJqwikFailureReporting")).doesNotExist();
		assertThat(generatedSource("GeneratedFailureReportingSentinelTest")).content()
				.contains("JQWIK_HOOK_GENERATED = false;").contains("generated.failure.reporting.jqwik.missing");
	}

	/**
	 * jqwik named only in the Gradle version catalogue still gets its hook, and the
	 * JUnit sentinel knows it.
	 *
	 * @throws IOException if a file cannot be written or read
	 */
	@Test
	void jqwikInTheVersionCatalogueWritesTheJqwikHook() throws IOException {
		Files.createDirectories(projectRoot.resolve("gradle"));
		Files.writeString(projectRoot.resolve("gradle/libs.versions.toml"),
				"[libraries]\njqwik = { module = \"net.jqwik:jqwik\", version = \"1.9.3\" }\n");

		writer(null).write(configuration(true), PACKAGE, testFolder, resources);

		assertThat(generatedSource("GeneratedJqwikFailureReporting")).exists();
		assertThat(generatedSource("GeneratedFailureReportingSentinelTest")).content()
				.contains("JQWIK_HOOK_GENERATED = true;");
	}

	/**
	 * Running the generator twice leaves exactly the files of one run.
	 *
	 * @throws IOException if a written file cannot be read
	 */
	@Test
	void aSecondRunDuplicatesNothing() throws IOException {
		writer(null).write(configuration(true), PACKAGE, testFolder, resources);
		String services = Files.readString(jupiterServices());
		String properties = Files.readString(resources.resolve("junit-platform.properties"));

		writer(null).write(configuration(true), PACKAGE, testFolder, resources);

		assertThat(Files.readString(jupiterServices())).isEqualTo(services);
		assertThat(Files.readString(resources.resolve("junit-platform.properties"))).isEqualTo(properties);
	}

	/**
	 * An instructor's own auto-detected extension stays registered and is added to
	 * the include filter, so it keeps running.
	 *
	 * @throws IOException if a file cannot be written or read
	 */
	@Test
	void keepsTheInstructorsExtensionsAndIncludesThem() throws IOException {
		Files.createDirectories(jupiterServices().getParent());
		Files.writeString(jupiterServices(), INSTRUCTOR_EXTENSION + System.lineSeparator());

		writer(null).write(configuration(true), PACKAGE, testFolder, resources);

		assertThat(Files.readString(jupiterServices())).contains(INSTRUCTOR_EXTENSION).contains(JUPITER_HOOK);
		assertThat(Files.readString(resources.resolve("junit-platform.properties")))
				.contains("autodetection.include=" + JUPITER_HOOK + "," + INSTRUCTOR_EXTENSION);
	}

	/**
	 * The instructor's other JUnit settings stay in the merged settings file.
	 *
	 * @throws IOException if a file cannot be written or read
	 */
	@Test
	void keepsTheInstructorsOtherSettings() throws IOException {
		Files.writeString(resources.resolve("junit-platform.properties"),
				"junit.jupiter.execution.parallel.enabled=false" + System.lineSeparator());

		writer(null).write(configuration(true), PACKAGE, testFolder, resources);

		assertThat(Files.readString(resources.resolve("junit-platform.properties")))
				.contains("junit.jupiter.execution.parallel.enabled=false")
				.contains("junit.jupiter.extensions.autodetection.enabled=true");
	}

	/**
	 * A setting that contradicts the generated auto-detection stops generation,
	 * names the file and the key, and writes nothing.
	 *
	 * @param conflictingLine the instructor's conflicting setting.
	 * @throws IOException if the settings file cannot be written
	 */
	@ParameterizedTest
	@ValueSource(strings = { "junit.jupiter.extensions.autodetection.enabled=false",
			"junit.jupiter.extensions.autodetection.include=com.example.Other",
			"junit.jupiter.extensions.autodetection.exclude=com.example.Other" })
	void refusesASettingThatContradictsTheGeneratedOne(String conflictingLine) throws IOException {
		Files.writeString(resources.resolve("junit-platform.properties"), conflictingLine + System.lineSeparator());
		String key = conflictingLine.substring(0, conflictingLine.indexOf('='));

		SecurityException failure = assertThrows(SecurityException.class,
				() -> writer(null).write(configuration(true), PACKAGE, testFolder, resources));

		assertThat(failure.getMessage()).contains("junit-platform.properties").contains(key);
		assertThat(generatedSource("GeneratedFailureReporting")).doesNotExist();
		assertThat(jupiterServices()).doesNotExist();
	}

	/**
	 * The refusal is localised: in German it still names the file and the key.
	 *
	 * @throws IOException if the settings file cannot be written
	 */
	@Test
	void theRefusalIsLocalisedInGerman() throws IOException {
		Files.writeString(resources.resolve("junit-platform.properties"),
				"junit.jupiter.extensions.autodetection.enabled=false" + System.lineSeparator());
		Locale original = Locale.getDefault(Locale.Category.DISPLAY);
		try {
			Locale.setDefault(Locale.Category.DISPLAY, Locale.GERMAN);

			SecurityException failure = assertThrows(SecurityException.class,
					() -> writer(null).write(configuration(true), PACKAGE, testFolder, resources));

			assertThat(failure.getMessage()).startsWith("Ares Sicherheitsfehler").contains("junit-platform.properties")
					.contains("junit.jupiter.extensions.autodetection.enabled");
		} finally {
			Locale.setDefault(Locale.Category.DISPLAY, original);
		}
	}

	/**
	 * The fixed timeout text and the sentinel's text exist in both bundles, since
	 * precompile copies both into the exercise.
	 */
	@Test
	void theGeneratedHooksTextsExistInBothLanguages() {
		for (Locale locale : List.of(Locale.ENGLISH, Locale.GERMAN)) {
			ResourceBundle bundle = ResourceBundle.getBundle("de.tum.cit.ase.ares.api.localization.messages", locale);
			assertThat(bundle.getString(FailureReportingSources.TIMEOUT_KEY)).isNotBlank();
			assertThat(bundle.getString(FailureReportingSources.INACTIVE_KEY)).isNotBlank();
		}
		assertThat(ResourceBundle.getBundle("de.tum.cit.ase.ares.api.localization.messages", Locale.GERMAN)
				.getString(FailureReportingSources.TIMEOUT_KEY))
						.isEqualTo("Der Test hat das Zeitlimit u\u0308berschritten.");
	}

	/**
	 * Turning the setting off removes the generated files, and only the generated
	 * lines from files the instructor shares.
	 *
	 * @throws IOException if a file cannot be written or read
	 */
	@Test
	void turningTheSettingOffRemovesOnlyWhatWasGenerated() throws IOException {
		Files.createDirectories(jupiterServices().getParent());
		Files.writeString(jupiterServices(), INSTRUCTOR_EXTENSION + System.lineSeparator());
		writer(null).write(configuration(true), PACKAGE, testFolder, resources);

		List<Path> written = writer(null).write(configuration(false), PACKAGE, testFolder, resources);

		assertThat(written).isEmpty();
		assertThat(generatedSource("GeneratedFailureReporting")).doesNotExist();
		assertThat(generatedSource("GeneratedFailureReportingSentinelTest")).doesNotExist();
		assertThat(Files.readString(jupiterServices())).contains(INSTRUCTOR_EXTENSION).doesNotContain(JUPITER_HOOK)
				.doesNotContain(FailureReportingWriter.BLOCK_BEGIN);
		assertThat(resources.resolve("junit-platform.properties")).doesNotExist();
	}

	/**
	 * With a known build layout, turning the setting off deletes the compiled hooks
	 * an earlier build left in the test output.
	 *
	 * @throws IOException if a file cannot be written
	 */
	@Test
	void turningTheSettingOffDeletesTheCompiledHooks() throws IOException {
		for (String directory : List.of("src/main/java", "target/classes", "target/test-classes")) {
			Files.createDirectories(projectRoot.resolve(directory));
		}
		Files.writeString(projectRoot.resolve("pom.xml"), "<project/>");
		BuildToolConfiguration mavenLayout = new BuildToolConfiguration(BuildMode.MAVEN, projectRoot,
				List.of(projectRoot.resolve("src/main/java")), List.of(testFolder),
				projectRoot.resolve("target/classes"), projectRoot.resolve("target/test-classes"));
		Path compiledHook = projectRoot.resolve("target/test-classes/de/tum/cit/ase/ares/generated")
				.resolve("GeneratedFailureReporting.class");
		Files.createDirectories(compiledHook.getParent());
		Files.write(compiledHook, new byte[] { (byte) 0xCA, (byte) 0xFE });

		writer(mavenLayout).write(configuration(false), PACKAGE, testFolder, resources);

		assertThat(compiledHook).doesNotExist();
	}

	/**
	 * In the Gradle layout too, turning the setting off deletes the compiled hooks.
	 *
	 * @throws IOException if a file cannot be written
	 */
	@Test
	void turningTheSettingOffDeletesTheCompiledHooksInTheGradleLayout() throws IOException {
		for (String directory : List.of("src/main/java", "build/classes/java/main", "build/classes/java/test")) {
			Files.createDirectories(projectRoot.resolve(directory));
		}
		BuildToolConfiguration gradleLayout = new BuildToolConfiguration(BuildMode.GRADLE, projectRoot,
				List.of(projectRoot.resolve("src/main/java")), List.of(testFolder),
				projectRoot.resolve("build/classes/java/main"), projectRoot.resolve("build/classes/java/test"));
		Path compiledSentinel = projectRoot.resolve("build/classes/java/test/de/tum/cit/ase/ares/generated")
				.resolve("GeneratedFailureReportingSentinelTest.class");
		Files.createDirectories(compiledSentinel.getParent());
		Files.write(compiledSentinel, new byte[] { (byte) 0xCA, (byte) 0xFE });

		writer(gradleLayout).write(configuration(false), PACKAGE, testFolder, resources);

		assertThat(compiledSentinel).doesNotExist();
	}

	/**
	 * A writer for this test's exercise, confining nothing.
	 *
	 * @param layout the build layout, or null.
	 * @return the writer
	 */
	private FailureReportingWriter writer(BuildToolConfiguration layout) {
		return new FailureReportingWriter(projectRoot, layout, UnaryOperator.identity());
	}

	/**
	 * A behaviour configuration with the privileged-exceptions category.
	 *
	 * @param enabled the category's switch.
	 * @return the configuration
	 */
	private static TestBehaviorConfiguration configuration(boolean enabled) {
		return TestBehaviorConfiguration.builder()
				.regardingPrivilegedExceptions(PrivilegedExceptionsConfiguration.builder()
						.onlyPrivilegedExceptionsAreReported(enabled).theFailureMessageIs("Ask your tutor.").build())
				.build();
	}

	/**
	 * Where a generated class's source belongs.
	 *
	 * @param simpleName the class's simple name.
	 * @return the source path
	 */
	private Path generatedSource(String simpleName) {
		return testFolder.resolve("de/tum/cit/ase/ares/generated").resolve(simpleName + ".java");
	}

	/**
	 * The JUnit service file in the test resources.
	 *
	 * @return its path
	 */
	private Path jupiterServices() {
		return resources.resolve(FailureReportingWriter.JUPITER_SERVICE_FILE);
	}
}
