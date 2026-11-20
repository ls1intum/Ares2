package de.tum.cit.ase.ares.api.securitytest.java.writer;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.function.UnaryOperator;

import org.junit.jupiter.api.Test;

import de.tum.cit.ase.ares.api.buildtoolconfiguration.BuildToolConfiguration;
import de.tum.cit.ase.ares.api.policy.policySubComponents.FailureFeedbackConfiguration;
import de.tum.cit.ase.ares.api.policy.policySubComponents.SecurityViolationFeedbackLevel;
import de.tum.cit.ase.ares.api.policy.policySubComponents.TestBehaviorConfiguration;

/**
 * Checks what the failure-reporting writer generates for a precompile exercise,
 * and that it removes exactly that again.
 */
class FailureReportingWriterTest extends GeneratedHookFilesTest.Project {

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
						.isEqualTo("Der Test hat das Zeitlimit \u00fcberschritten.");
	}

	/**
	 * Switching the setting off removes the hooks, source and compiled.
	 *
	 * @throws IOException if a file cannot be written
	 */
	@Test
	void switchingTheSettingOffRemovesTheHooks() throws IOException {
		BuildToolConfiguration layout = mavenLayout();
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
	 * The hook is written when privileged-only reporting is on or the feedback
	 * level is not HIGH, and removed again once both are back to their defaults.
	 */
	@org.junit.jupiter.params.ParameterizedTest
	@org.junit.jupiter.params.provider.CsvSource({ "false, HIGH, false", "true, HIGH, true", "false, LOW, true",
			"false, DEBUG, true", "true, LOW, true" })
	void theHookFollowsBothSettings(boolean privilegedOnly, SecurityViolationFeedbackLevel level, boolean written) {
		writer(null)
				.write(TestBehaviorConfiguration.builder()
						.regardingTheFailureFeedbackForTheTestCases(FailureFeedbackConfiguration.builder()
								.onlyPrivilegedExceptionsAreReported(privilegedOnly)
								.theSecurityViolationFeedbackLevelIs(level).build())
						.build(), PACKAGE, testFolder);
		assertThat(Files.exists(source(FailureReportingSources.JUPITER_HOOK))).isEqualTo(written);

		writer(null).write(configuration(false), PACKAGE, testFolder);
		assertThat(source(FailureReportingSources.JUPITER_HOOK)).doesNotExist();
	}

	/**
	 * The generated hook reads the level and knows the copied violation classes.
	 */
	@Test
	void theHookReadsTheLevelAndTheCopiedViolations() throws IOException {
		writer(null).write(TestBehaviorConfiguration.builder()
				.regardingTheFailureFeedbackForTheTestCases(FailureFeedbackConfiguration.builder()
						.theSecurityViolationFeedbackLevelIs(SecurityViolationFeedbackLevel.LOW).build())
				.build(), PACKAGE, testFolder);
		assertThat(Files.readString(source(FailureReportingSources.JUPITER_HOOK)))
				.contains(TestBehaviorConfiguration.FAILURE_FEEDBACK_LEVEL_FIELD_NAME)
				.contains(PACKAGE + ".ares.api.aop.java.aspectj.adviceandpointcut.AresSecurityViolation")
				.contains(PACKAGE + ".ares.api.aop.java.instrumentation.advice.AresSecurityViolation")
				.contains(PACKAGE
						+ ".ares.api.aop.java.instrumentation.advice.JavaInstrumentationAdviceThreadSystemToolbox")
				.contains("registerGeneratedHookLoader").contains("generated.hook.registration.failed")
				.doesNotContain("@PACKAGE@").doesNotContain("@LEVEL@");
	}

	/**
	 * The DEBUG hints name the policy file when the generator knows it, and say
	 * only "the security policy" otherwise.
	 */
	@Test
	void theHookNamesThePolicyFileWhenKnown() throws IOException {
		TestBehaviorConfiguration debug = TestBehaviorConfiguration.builder()
				.regardingTheFailureFeedbackForTheTestCases(FailureFeedbackConfiguration.builder()
						.theSecurityViolationFeedbackLevelIs(SecurityViolationFeedbackLevel.DEBUG).build())
				.build();
		writer(null).write(debug, PACKAGE, testFolder, Optional.of("Exercise.yaml"));
		assertThat(Files.readString(source(FailureReportingSources.JUPITER_HOOK)))
				.contains("String file = \"Exercise.yaml\";").doesNotContain("@POLICY_FILE@");
		writer(null).write(debug, PACKAGE, testFolder);
		assertThat(Files.readString(source(FailureReportingSources.JUPITER_HOOK))).contains("String file = null;")
				.doesNotContain("@POLICY_FILE@");
	}

	/**
	 * A file name with a quote, a backslash, a line break or other characters
	 * becomes escapes, so it cannot end the generated literal or add code.
	 */
	@Test
	void aPolicyFileNameBecomesASafeLiteral() {
		assertThat(FailureReportingSources.javaLiteral("a\"b\\c\nd\u00e9\u4e2d (1).yaml"))
				.isEqualTo("\"a\\042b\\134c\\012d\\351\\u4e2d (1).yaml\"");
	}

	/**
	 * A configuration with privileged-exceptions-only reporting switched on or off.
	 *
	 * @param enabled whether reporting is restricted.
	 * @return the configuration
	 */
	private static TestBehaviorConfiguration configuration(boolean enabled) {
		return TestBehaviorConfiguration.builder()
				.regardingTheFailureFeedbackForTheTestCases(
						FailureFeedbackConfiguration.builder().onlyPrivilegedExceptionsAreReported(enabled)
								.theFailureMessageForOtherExceptionsIs("Ask your tutor.").build())
				.build();
	}
}
