package de.tum.cit.ase.ares.api.policy.policySubComponents;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import de.tum.cit.ase.ares.api.AresConstants;
import de.tum.cit.ase.ares.api.localization.Messages;
import de.tum.cit.ase.ares.api.policy.SecurityPolicy;

/**
 * Checks the behavioural wrapper and the literal constants a precompile run
 * generates from each of its four categories.
 */
class TestBehaviorConfigurationTest {

	/** The released no-argument constructor still creates an empty setting. */
	@Test
	void noArgumentConstructorPreservesCompatibility() {
		TestBehaviorConfiguration configuration = new TestBehaviorConfiguration();
		assertThat(configuration.regardingTheStrictTimeoutForTheTestCases()).isNull();
		assertThat(configuration.regardingTheOutputOfTheTestCases()).isNull();
		assertThat(configuration.regardingTheFailureFeedbackForTheTestCases()).isNull();
		assertThat(configuration.regardingTheVisibilityAndScheduleOfTheTestCases()).isNull();
		assertThat(configuration.literalFieldAssignments()).isEmpty();
	}

	/** Without a category, the builder holds none and contributes no constant. */
	@Test
	void anEmptyConfigurationContributesNothing() {
		assertThat(TestBehaviorConfiguration.builder().build().literalFieldAssignments()).isEmpty();
	}

	/** The carrier lives outside the sealed Ares packages, in the reserved one. */
	@Test
	void theCarrierLivesInTheGeneratedPackage() {
		assertThat(TestBehaviorConfiguration.GENERATED_CLASS_NAME)
				.isEqualTo("de.tum.cit.ase.ares.generated.GeneratedTestBehaviorSettings");
	}

	/** Durations are written in nanoseconds; a missing grace is written as -1. */
	@Test
	void theStrictTimeoutIsWrittenInNanoseconds() {
		String withGrace = assignments(TestBehaviorConfiguration.builder()
				.regardingTheStrictTimeoutForTheTestCases(StrictTimeoutConfiguration.builder().theTimeoutIs("2 SECONDS")
						.theTerminationGraceIs("500 MILLISECONDS").build())
				.build());
		assertThat(withGrace)
				.contains("REGARDING_THE_STRICT_TIMEOUT_THE_TIMEOUT_NANOS = " + Duration.ofSeconds(2).toNanos() + "L;")
				.contains("REGARDING_THE_STRICT_TIMEOUT_THE_TERMINATION_GRACE_NANOS = "
						+ Duration.ofMillis(500).toNanos() + "L;");
		String withoutGrace = assignments(TestBehaviorConfiguration.builder().regardingTheStrictTimeoutForTheTestCases(
				StrictTimeoutConfiguration.builder().theTimeoutIs("1 MINUTES").build()).build());
		assertThat(withoutGrace).contains("REGARDING_THE_STRICT_TIMEOUT_THE_TERMINATION_GRACE_NANOS = -1L;");
	}

	/** An empty output category writes today's defaults. */
	@Test
	void theOutputCategoryWritesItsDefaults() {
		String written = assignments(TestBehaviorConfiguration.builder()
				.regardingTheOutputOfTheTestCases(OutputConfiguration.builder().build()).build());
		assertThat(written).contains("REGARDING_THE_OUTPUT_THE_OUTPUT_IS_MIRRORED = false;")
				.contains("REGARDING_THE_OUTPUT_THE_MAXIMUM_NUMBER_OF_OUTPUT_CHARACTERS = "
						+ de.tum.cit.ase.ares.api.MirrorOutput.DEFAULT_MAX_STD_OUT + "L;");
	}

	/** An empty failure-feedback category writes off, the default text and HIGH. */
	@Test
	void theFailureFeedbackCategoryWritesItsDefaults() {
		String written = assignments(TestBehaviorConfiguration.builder()
				.regardingTheFailureFeedbackForTheTestCases(FailureFeedbackConfiguration.builder().build()).build());
		assertThat(written).contains("REGARDING_THE_FAILURE_FEEDBACK_ONLY_PRIVILEGED_EXCEPTIONS_ARE_REPORTED = false;")
				.contains("REGARDING_THE_FAILURE_FEEDBACK_THE_FAILURE_MESSAGE_FOR_OTHER_EXCEPTIONS = \"Test failed.\";")
				.contains("REGARDING_THE_FAILURE_FEEDBACK_THE_SECURITY_VIOLATION_FEEDBACK_LEVEL = \"HIGH\";");
	}

	/** A configured failure-feedback category writes its values. */
	@Test
	void theFailureFeedbackCategoryWritesItsValues() {
		String written = assignments(TestBehaviorConfiguration.builder()
				.regardingTheFailureFeedbackForTheTestCases(FailureFeedbackConfiguration.builder()
						.onlyPrivilegedExceptionsAreReported(true).theFailureMessageForOtherExceptionsIs("Nope.")
						.theSecurityViolationFeedbackLevelIs(SecurityViolationFeedbackLevel.LOW).build())
				.build());
		assertThat(written).contains("ONLY_PRIVILEGED_EXCEPTIONS_ARE_REPORTED = true;")
				.contains("THE_FAILURE_MESSAGE_FOR_OTHER_EXCEPTIONS = \"Nope.\";")
				.contains("THE_SECURITY_VIOLATION_FEEDBACK_LEVEL = \"LOW\";");
	}

	/**
	 * The extension is folded into the deadline and the switch is written as given;
	 * the test-name lists stay out of the settings class, whose fields any test can
	 * read.
	 */
	@Test
	void theVisibilityCategoryWritesItsValues() {
		String written = assignments(TestBehaviorConfiguration.builder()
				.regardingTheVisibilityAndScheduleOfTheTestCases(VisibilityAndScheduleConfiguration.builder()
						.theDeadlineIs("2000-01-01 00:00 UTC").theDeadlineIsExtendedBy("1 DAYS")
						.theHiddenTestsAreRunBefore("1990-01-01 00:00 UTC").testsNotListedHereAreHidden(true)
						.hiddenTests("org.example.A", "org.example.B#m").publicTests("org.example.C").build())
				.build());
		assertThat(written)
				.contains("REGARDING_THE_VISIBILITY_AND_SCHEDULE_THE_DEADLINE = "
						+ ZonedDateTime.parse("2000-01-02T00:00Z").toInstant().toEpochMilli() + "L;")
				.contains("REGARDING_THE_VISIBILITY_AND_SCHEDULE_THE_HIDDEN_TESTS_ARE_RUN_BEFORE = "
						+ ZonedDateTime.parse("1990-01-01T00:00Z").toInstant().toEpochMilli() + "L;")
				.contains("TESTS_NOT_LISTED_HERE_ARE_HIDDEN = true;").doesNotContain("THE_FOLLOWING_TESTS_ARE")
				.doesNotContain("org.example.A").doesNotContain("org.example.C");
	}

	/**
	 * A hidden list too long for one Java string literal still compiles, and the
	 * expression holds every entry in order.
	 *
	 * @param folder where the generated class is written and compiled.
	 * @throws Exception if writing, compiling or loading fails
	 */
	@Test
	void aLongHiddenListStillCompiles(@TempDir Path folder) throws Exception {
		List<String> hidden = IntStream.range(0, 4000)
				.mapToObj(i -> "org.example.longlist.SomeRatherLongTestClassName" + i + "#method").toList();
		String literal = TestBehaviorConfiguration.listLiteral(hidden);
		assertThat(literal).startsWith("String.join(");
		assertThat(String.join("\n", hidden)).hasSizeGreaterThan(65_535);
		Object field = compileAndRead(folder, "public static final String LIST = " + literal + ";", "LIST");
		assertThat(List.of(((String) field).split("\n"))).containsExactlyElementsOf(hidden);
	}

	/**
	 * Compiles the assignments into a class and reads one of its fields.
	 *
	 * @param folder      where the class is written and compiled.
	 * @param assignments the field assignments.
	 * @param fieldName   the field to read.
	 * @return the field's value
	 * @throws Exception if writing, compiling or loading fails
	 */
	private static Object compileAndRead(Path folder, String assignments, String fieldName) throws Exception {
		Path source = Files.writeString(folder.resolve("LongSettings.java"),
				"public final class LongSettings {" + System.lineSeparator() + assignments + "}");
		Path output = Files.createDirectories(folder.resolve("classes"));
		de.tum.cit.ase.ares.testutilities.GeneratedCodeRuns.compile(output, List.of(), source);
		try (URLClassLoader loader = new URLClassLoader(
				de.tum.cit.ase.ares.testutilities.GeneratedCodeRuns.classPath(List.of(output)), null)) {
			return loader.loadClass("LongSettings").getField(fieldName).get(null);
		}
	}

	/** All four categories together write every field once. */
	@Test
	void allFourCategoriesWriteEveryField() {
		TestBehaviorConfiguration configuration = TestBehaviorConfiguration.builder()
				.regardingTheStrictTimeoutForTheTestCases(
						StrictTimeoutConfiguration.builder().theTimeoutIs("2 SECONDS").build())
				.regardingTheOutputOfTheTestCases(OutputConfiguration.builder().build())
				.regardingTheFailureFeedbackForTheTestCases(FailureFeedbackConfiguration.builder().build())
				.regardingTheVisibilityAndScheduleOfTheTestCases(VisibilityAndScheduleConfiguration.builder()
						.theDeadlineIs("2000-01-01 00:00 UTC").testsNotListedHereAreHidden(false).build())
				.build();
		assertThat(configuration.literalFieldAssignments()).hasSize(2 + 2 + 3 + 3);
	}

	/**
	 * Joins the configuration's assignments.
	 *
	 * @param configuration the configuration.
	 * @return the assignments as one text.
	 */
	private static String assignments(TestBehaviorConfiguration configuration) {
		return String.join("", configuration.literalFieldAssignments());
	}

	/** An empty configuration names no category. */
	@Test
	void anEmptyConfigurationNamesNoCategory() {
		assertThat(TestBehaviorConfiguration.builder().build().firstConfiguredCategory()).isEmpty();
	}

	/** Each category this branch reads, alone in a configuration. */
	static Stream<Arguments> categories() {
		return Stream.of(
				Arguments.of(TestBehaviorFieldNames.STRICT_TIMEOUT, TestBehaviorConfiguration.builder()
						.regardingTheStrictTimeoutForTheTestCases(
								StrictTimeoutConfiguration.builder().theTimeoutIs("2 SECONDS").build())
						.build()),
				Arguments.of(TestBehaviorFieldNames.OUTPUT,
						TestBehaviorConfiguration.builder()
								.regardingTheOutputOfTheTestCases(OutputConfiguration.builder().build()).build()),
				Arguments.of(TestBehaviorFieldNames.FAILURE_FEEDBACK,
						TestBehaviorConfiguration.builder()
								.regardingTheFailureFeedbackForTheTestCases(
										FailureFeedbackConfiguration.builder().build())
								.build()),
				Arguments.of(TestBehaviorFieldNames.VISIBILITY_AND_SCHEDULE, TestBehaviorConfiguration.builder()
						.regardingTheVisibilityAndScheduleOfTheTestCases(VisibilityAndScheduleConfiguration.builder()
								.theDeadlineIs("2000-01-01 00:00 UTC").testsNotListedHereAreHidden(false).build())
						.build()));
	}

	/**
	 * A version 1 policy built in code refuses a test-behaviour category, naming
	 * the version, the category and the version it needs.
	 *
	 * @param category the category's policy key.
	 * @param behavior a configuration setting only that category.
	 */
	@ParameterizedTest
	@MethodSource("categories")
	void aVersionOnePolicyRefusesTheCategory(String category, TestBehaviorConfiguration behavior) {
		assertThat(behavior.firstConfiguredCategory()).contains(category);
		assertThatThrownBy(() -> policy(1, behavior)).isInstanceOf(IllegalArgumentException.class).hasMessage(Messages
				.localized("policy.version.test.behavior", 1, category, AresConstants.TEST_BEHAVIOR_POLICY_VERSION));
	}

	/**
	 * A version 2 policy built in code accepts the category.
	 *
	 * @param category the category's policy key.
	 * @param behavior a configuration setting only that category.
	 */
	@ParameterizedTest
	@MethodSource("categories")
	void aVersionTwoPolicyAcceptsTheCategory(String category, TestBehaviorConfiguration behavior) {
		assertThat(policy(2, behavior).thisPolicyFileCompliesToThePolicyVersion()).isEqualTo(2);
	}

	/** A version 1 policy may carry an empty test-behaviour section. */
	@Test
	void aVersionOnePolicyAcceptsAnEmptySection() {
		assertThat(policy(1, TestBehaviorConfiguration.builder().build()).thisPolicyFileCompliesToThePolicyVersion())
				.isEqualTo(1);
	}

	/** A policy built in code without a version declares the newest one. */
	@Test
	void aPolicyBuiltInCodeDeclaresTheNewestVersion() {
		assertThat(
				SecurityPolicy.createRestrictive(ProgrammingLanguageConfiguration.JAVA_USING_MAVEN_ARCHUNIT_AND_ASPECTJ)
						.thisPolicyFileCompliesToThePolicyVersion()).isEqualTo(AresConstants.MAXIMUM_POLICY_VERSION);
	}

	/**
	 * Builds a policy with the given version and test behaviour.
	 *
	 * @param version  the declared version.
	 * @param behavior the test behaviour.
	 * @return the policy.
	 */
	private static SecurityPolicy policy(int version, TestBehaviorConfiguration behavior) {
		return new SecurityPolicy(version,
				SupervisedCode.builder()
						.theFollowingProgrammingLanguageConfigurationIsUsed(
								ProgrammingLanguageConfiguration.JAVA_USING_MAVEN_ARCHUNIT_AND_ASPECTJ)
						.theFollowingClassesAreTestClasses(List.of())
						.theFollowingResourceAccessesArePermitted(ResourceAccesses.createRestrictive())
						.theFollowingTestBehaviorIsConfigured(behavior).build());
	}
}
