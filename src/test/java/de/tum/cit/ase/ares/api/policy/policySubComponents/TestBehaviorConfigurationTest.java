package de.tum.cit.ase.ares.api.policy.policySubComponents;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.stream.IntStream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

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
				.regardingTheStrictTimeoutForTheTestCases(
						StrictTimeoutConfiguration.builder().theTimeoutIs("2s").theTerminationGraceIs("500ms").build())
				.build());
		assertThat(withGrace)
				.contains("REGARDING_THE_STRICT_TIMEOUT_THE_TIMEOUT_NANOS = " + Duration.ofSeconds(2).toNanos() + "L;")
				.contains("REGARDING_THE_STRICT_TIMEOUT_THE_TERMINATION_GRACE_NANOS = "
						+ Duration.ofMillis(500).toNanos() + "L;");
		String withoutGrace = assignments(TestBehaviorConfiguration.builder().regardingTheStrictTimeoutForTheTestCases(
				StrictTimeoutConfiguration.builder().theTimeoutIs("1m").build()).build());
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

	/** An empty failure-feedback category writes off, the default text and FULL. */
	@Test
	void theFailureFeedbackCategoryWritesItsDefaults() {
		String written = assignments(TestBehaviorConfiguration.builder()
				.regardingTheFailureFeedbackForTheTestCases(FailureFeedbackConfiguration.builder().build()).build());
		assertThat(written).contains("REGARDING_THE_FAILURE_FEEDBACK_ONLY_PRIVILEGED_EXCEPTIONS_ARE_REPORTED = false;")
				.contains("REGARDING_THE_FAILURE_FEEDBACK_THE_FAILURE_MESSAGE_FOR_OTHER_EXCEPTIONS = \"Test failed.\";")
				.contains("REGARDING_THE_FAILURE_FEEDBACK_THE_SECURITY_VIOLATION_FEEDBACK_LEVEL = \"FULL\";");
	}

	/** A configured failure-feedback category writes its values. */
	@Test
	void theFailureFeedbackCategoryWritesItsValues() {
		String written = assignments(TestBehaviorConfiguration.builder()
				.regardingTheFailureFeedbackForTheTestCases(FailureFeedbackConfiguration.builder()
						.onlyPrivilegedExceptionsAreReported(true).theFailureMessageForOtherExceptionsIs("Nope.")
						.theSecurityViolationFeedbackLevelIs(SecurityViolationFeedbackLevel.MINIMIZED).build())
				.build());
		assertThat(written).contains("ONLY_PRIVILEGED_EXCEPTIONS_ARE_REPORTED = true;")
				.contains("THE_FAILURE_MESSAGE_FOR_OTHER_EXCEPTIONS = \"Nope.\";")
				.contains("THE_SECURITY_VIOLATION_FEEDBACK_LEVEL = \"MINIMIZED\";");
	}

	/**
	 * The extension is folded into the deadline, each list is one line per entry,
	 * and the switch is written as given.
	 */
	@Test
	void theVisibilityCategoryWritesItsValues() {
		String written = assignments(TestBehaviorConfiguration.builder()
				.regardingTheVisibilityAndScheduleOfTheTestCases(VisibilityAndScheduleConfiguration.builder()
						.theDeadlineIs("2000-01-01 00:00 UTC").theDeadlineIsExtendedBy("1d")
						.theHiddenTestsAreRunBefore("1990-01-01 00:00 UTC").testsNotListedHereAreHidden(true)
						.hiddenTests("org.example.A", "org.example.B#m").publicTests("org.example.C").build())
				.build());
		assertThat(written)
				.contains("REGARDING_THE_VISIBILITY_AND_SCHEDULE_THE_DEADLINE = "
						+ ZonedDateTime.parse("2000-01-02T00:00Z").toInstant().toEpochMilli() + "L;")
				.contains("REGARDING_THE_VISIBILITY_AND_SCHEDULE_THE_HIDDEN_TESTS_ARE_RUN_BEFORE = "
						+ ZonedDateTime.parse("1990-01-01T00:00Z").toInstant().toEpochMilli() + "L;")
				.contains("THE_FOLLOWING_TESTS_ARE_HIDDEN = \"org.example.A\\norg.example.B#m\";")
				.contains("THE_FOLLOWING_TESTS_ARE_PUBLIC = \"org.example.C\";")
				.contains("TESTS_NOT_LISTED_HERE_ARE_HIDDEN = true;");
	}

	/**
	 * A hidden list too long for one Java string literal still compiles, and the
	 * generated field holds every entry in order.
	 *
	 * @param folder where the generated class is written and compiled.
	 * @throws Exception if writing, compiling or loading fails
	 */
	@Test
	void aLongHiddenListStillCompiles(@TempDir Path folder) throws Exception {
		List<String> hidden = IntStream.range(0, 4000)
				.mapToObj(i -> "org.example.longlist.SomeRatherLongTestClassName" + i + "#method").toList();
		String written = assignments(
				TestBehaviorConfiguration.builder()
						.regardingTheVisibilityAndScheduleOfTheTestCases(VisibilityAndScheduleConfiguration.builder()
								.theDeadlineIs("2000-01-01 00:00 UTC").testsNotListedHereAreHidden(false)
								.hiddenTests(hidden.toArray(String[]::new)).build())
						.build());
		assertThat(written).contains("THE_FOLLOWING_TESTS_ARE_HIDDEN = String.join(");
		assertThat(String.join("\n", hidden)).hasSizeGreaterThan(65_535);
		Object field = compileAndRead(folder, written,
				"REGARDING_THE_VISIBILITY_AND_SCHEDULE_THE_FOLLOWING_TESTS_ARE_HIDDEN");
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
						StrictTimeoutConfiguration.builder().theTimeoutIs("2s").build())
				.regardingTheOutputOfTheTestCases(OutputConfiguration.builder().build())
				.regardingTheFailureFeedbackForTheTestCases(FailureFeedbackConfiguration.builder().build())
				.regardingTheVisibilityAndScheduleOfTheTestCases(VisibilityAndScheduleConfiguration.builder()
						.theDeadlineIs("2000-01-01 00:00 UTC").testsNotListedHereAreHidden(false).build())
				.build();
		assertThat(configuration.literalFieldAssignments()).hasSize(2 + 2 + 3 + 5);
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
}
