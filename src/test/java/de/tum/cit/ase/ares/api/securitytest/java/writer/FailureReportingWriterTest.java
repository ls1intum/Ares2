package de.tum.cit.ase.ares.api.securitytest.java.writer;

import static org.assertj.core.api.Assertions.assertThat;

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

import de.tum.cit.ase.ares.api.buildtoolconfiguration.BuildMode;
import de.tum.cit.ase.ares.api.buildtoolconfiguration.BuildToolConfiguration;
import de.tum.cit.ase.ares.api.policy.policySubComponents.PrivilegedExceptionsConfiguration;
import de.tum.cit.ase.ares.api.policy.policySubComponents.TestBehaviorConfiguration;

/**
 * Checks what the failure-reporting writer generates for a precompile exercise,
 * and that it removes exactly that again.
 */
class FailureReportingWriterTest {

	/** The exercise package the copied Ares classes live in. */
	private static final String PACKAGE = "com.example";

	/** The exercise root of each test. */
	@TempDir
	Path projectRoot;

	/** The exercise's test source root. */
	private Path testFolder;

	/**
	 * Creates the exercise folders and a plain Gradle build.
	 *
	 * @throws IOException if the folders cannot be created
	 */
	@BeforeEach
	void setUp() throws IOException {
		testFolder = Files.createDirectories(projectRoot.resolve("src/test/java"));
		Files.writeString(projectRoot.resolve("build.gradle"), "plugins { id 'java' }");
	}

	/** Without the setting, nothing is generated or registered. */
	@Test
	void writesNothingWithoutTheSetting() {
		GeneratedHookFiles.Contribution generated = writer(null).write(TestBehaviorConfiguration.builder().build(),
				PACKAGE, testFolder);

		assertThat(generated.written()).isEmpty();
		assertThat(generated.jupiterHooks()).isEmpty();
		assertThat(source(FailureReportingSources.JUPITER_HOOK)).doesNotExist();
	}

	/** With the setting switched off, nothing is generated or registered. */
	@Test
	void writesNothingWhenTheSettingIsFalse() {
		GeneratedHookFiles.Contribution generated = writer(null).write(configuration(false), PACKAGE, testFolder);

		assertThat(generated.written()).isEmpty();
		assertThat(generated.jupiterHooks()).isEmpty();
		assertThat(source(FailureReportingSources.JUPITER_HOOK)).doesNotExist();
	}

	/**
	 * With the setting on, the JUnit extension and its sentinel are written and
	 * registered.
	 *
	 * @throws IOException if a written file cannot be read
	 */
	@Test
	void writesTheJupiterExtension() throws IOException {
		GeneratedHookFiles.Contribution generated = writer(null).write(configuration(true), PACKAGE, testFolder);

		assertThat(generated.jupiterHooks()).containsExactly(FailureReportingSources.JUPITER_HOOK);
		assertThat(source(FailureReportingSources.JUPITER_HOOK)).content()
				.contains("implements TestExecutionExceptionHandler, LifecycleMethodExecutionExceptionHandler")
				.contains("com.example.ares.api.localization.Messages.localized");
		assertThat(source(FailureReportingSources.JUPITER_SENTINEL)).content()
				.contains("class GeneratedFailureReportingSentinelTest");
	}

	/**
	 * Generating twice writes the same files.
	 *
	 * @throws IOException if a written file cannot be read
	 */
	@Test
	void aSecondRunWritesTheSameFiles() throws IOException {
		writer(null).write(configuration(true), PACKAGE, testFolder);
		String hook = Files.readString(source(FailureReportingSources.JUPITER_HOOK));

		writer(null).write(configuration(true), PACKAGE, testFolder);

		assertThat(Files.readString(source(FailureReportingSources.JUPITER_HOOK))).isEqualTo(hook);
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
	 * Switching the setting off removes the hooks, source and compiled.
	 *
	 * @throws IOException if a file cannot be written
	 */
	@Test
	void switchingTheSettingOffRemovesTheHooks() throws IOException {
		for (String directory : List.of("src/main/java", "target/classes", "target/test-classes")) {
			Files.createDirectories(projectRoot.resolve(directory));
		}
		Files.writeString(projectRoot.resolve("pom.xml"), "<project/>");
		BuildToolConfiguration layout = new BuildToolConfiguration(BuildMode.MAVEN, projectRoot,
				List.of(projectRoot.resolve("src/main/java")), List.of(testFolder),
				projectRoot.resolve("target/classes"), projectRoot.resolve("target/test-classes"));
		writer(layout).write(configuration(true), PACKAGE, testFolder);
		Path compiled = projectRoot
				.resolve("target/test-classes/de/tum/cit/ase/ares/generated/GeneratedFailureReporting.class");
		Files.createDirectories(compiled.getParent());
		Files.write(compiled, new byte[] { (byte) 0xCA, (byte) 0xFE });

		writer(layout).write(configuration(false), PACKAGE, testFolder);

		assertThat(source(FailureReportingSources.JUPITER_HOOK)).doesNotExist();
		assertThat(source(FailureReportingSources.JUPITER_SENTINEL)).doesNotExist();
		assertThat(compiled).doesNotExist();
	}

	/**
	 * The writer for this test's exercise, confining nothing.
	 *
	 * @param layout the build layout, or null.
	 * @return the writer
	 */
	private FailureReportingWriter writer(BuildToolConfiguration layout) {
		return new FailureReportingWriter(new GeneratedHookFiles(projectRoot, layout, UnaryOperator.identity()));
	}

	/**
	 * A configuration with privileged-exceptions-only reporting switched on or off.
	 *
	 * @param enabled whether reporting is restricted.
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
	private Path source(String simpleName) {
		return testFolder.resolve("de/tum/cit/ase/ares/generated").resolve(simpleName + ".java");
	}
}
