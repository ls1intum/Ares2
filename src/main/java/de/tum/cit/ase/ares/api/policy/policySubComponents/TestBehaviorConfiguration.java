package de.tum.cit.ase.ares.api.policy.policySubComponents;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import de.tum.cit.ase.ares.api.aop.java.javaAOPTestCaseToolbox.JavaAOPTestCaseToolbox;

/**
 * Wraps the behavioural settings a policy configures for its tests, parallel to
 * {@link ResourceAccesses} on {@link SupervisedCode}. Every category is
 * optional, and omitting one means that feature was never configured.
 *
 * @since 2.2.0
 * @author Luka Petrovic
 * @param regardingTheStrictTimeoutForTheTestCases        the strict timeout;
 *                                                        null when not
 *                                                        configured.
 * @param regardingTheOutputOfTheTestCases                the I/O manager and
 *                                                        output settings; null
 *                                                        when not configured.
 * @param regardingTheFailureFeedbackForTheTestCases      what a failure shows;
 *                                                        null when not
 *                                                        configured.
 * @param regardingTheVisibilityAndScheduleOfTheTestCases hidden tests and their
 *                                                        deadline; null when
 *                                                        not configured.
 */
public record TestBehaviorConfiguration(@Nullable StrictTimeoutConfiguration regardingTheStrictTimeoutForTheTestCases,
		@Nullable OutputConfiguration regardingTheOutputOfTheTestCases,
		@Nullable FailureFeedbackConfiguration regardingTheFailureFeedbackForTheTestCases,
		@Nullable VisibilityAndScheduleConfiguration regardingTheVisibilityAndScheduleOfTheTestCases) {

	/**
	 * Preserves the public empty configuration constructor for existing callers.
	 */
	public TestBehaviorConfiguration() {
		this(null, null, null, null);
	}

	/**
	 * Fully-qualified name of the class a precompile run generates to carry this
	 * configuration as literal constants. Its package is deliberately not one of
	 * the packages inside the Ares JAR: those are sealed, so a class compiled into
	 * the exercise could not be loaded there next to the JAR.
	 */
	public static final String GENERATED_CLASS_NAME = "de.tum.cit.ase.ares.generated.GeneratedTestBehaviorSettings";

	/** Name of the generated field holding the timeout in nanoseconds. */
	public static final String STRICT_TIMEOUT_TIMEOUT_FIELD_NAME = "REGARDING_THE_STRICT_TIMEOUT_THE_TIMEOUT_NANOS";

	/**
	 * Name of the generated field holding the grace period in nanoseconds,
	 * {@value #NOT_CONFIGURED} when not configured.
	 */
	public static final String STRICT_TIMEOUT_GRACE_FIELD_NAME = "REGARDING_THE_STRICT_TIMEOUT_THE_TERMINATION_GRACE_NANOS";

	/** The generated value of a duration that is not configured. */
	public static final long NOT_CONFIGURED = -1;

	/** Name of the generated field saying whether output is mirrored. */
	public static final String OUTPUT_MIRRORED_FIELD_NAME = "REGARDING_THE_OUTPUT_THE_OUTPUT_IS_MIRRORED";

	/** Name of the generated field holding the output limit. */
	public static final String OUTPUT_LIMIT_FIELD_NAME = "REGARDING_THE_OUTPUT_THE_MAXIMUM_NUMBER_OF_OUTPUT_CHARACTERS";

	/**
	 * Name of the generated field saying whether privileged-only reporting is on.
	 */
	public static final String FAILURE_FEEDBACK_PRIVILEGED_ONLY_FIELD_NAME = "REGARDING_THE_FAILURE_FEEDBACK_ONLY_PRIVILEGED_EXCEPTIONS_ARE_REPORTED";

	/** Name of the generated field holding the message for other failures. */
	public static final String FAILURE_FEEDBACK_MESSAGE_FIELD_NAME = "REGARDING_THE_FAILURE_FEEDBACK_THE_FAILURE_MESSAGE_FOR_OTHER_EXCEPTIONS";

	/** Name of the generated field holding the feedback level's name. */
	public static final String FAILURE_FEEDBACK_LEVEL_FIELD_NAME = "REGARDING_THE_FAILURE_FEEDBACK_THE_SECURITY_VIOLATION_FEEDBACK_LEVEL";

	/**
	 * Name of the generated field holding the effective deadline, extension
	 * included, in epoch milliseconds.
	 */
	public static final String VISIBILITY_DEADLINE_FIELD_NAME = "REGARDING_THE_VISIBILITY_AND_SCHEDULE_THE_DEADLINE";

	/**
	 * Name of the generated field holding the early-run date in epoch milliseconds,
	 * {@link Long#MIN_VALUE} when not configured.
	 */
	public static final String VISIBILITY_RUN_BEFORE_FIELD_NAME = "REGARDING_THE_VISIBILITY_AND_SCHEDULE_THE_HIDDEN_TESTS_ARE_RUN_BEFORE";

	/**
	 * Name of the generated field holding the hidden tests, one entry per line. A
	 * single string, because only a constant is copied into the generated hook.
	 */
	public static final String VISIBILITY_HIDDEN_LIST_FIELD_NAME = "REGARDING_THE_VISIBILITY_AND_SCHEDULE_THE_FOLLOWING_TESTS_ARE_HIDDEN";

	/**
	 * Name of the generated field holding the public tests, one entry per line, a
	 * single string for the same reason as the hidden tests.
	 */
	public static final String VISIBILITY_PUBLIC_LIST_FIELD_NAME = "REGARDING_THE_VISIBILITY_AND_SCHEDULE_THE_FOLLOWING_TESTS_ARE_PUBLIC";

	/** Name of the generated field saying whether unlisted tests are hidden. */
	public static final String VISIBILITY_UNLISTED_HIDDEN_FIELD_NAME = "REGARDING_THE_VISIBILITY_AND_SCHEDULE_TESTS_NOT_LISTED_HERE_ARE_HIDDEN";

	/**
	 * Literal field assignments every configured category contributes, one entry
	 * per field, each already formatted by {@code JavaAOPTestCaseToolbox}'s
	 * public-static-final assignment helpers, defaults applied and durations in
	 * nanoseconds. Empty when nothing is configured.
	 *
	 * @since 2.2.0
	 * @author Luka Petrovic
	 * @return the literal field assignments to write into the generated settings
	 *         class; empty when nothing is configured.
	 */
	@Nonnull
	public List<String> literalFieldAssignments() {
		List<String> assignments = new ArrayList<>();
		if (regardingTheStrictTimeoutForTheTestCases != null) {
			assignments.add(JavaAOPTestCaseToolbox.getPublicStaticFinalLongAssignment(STRICT_TIMEOUT_TIMEOUT_FIELD_NAME,
					regardingTheStrictTimeoutForTheTestCases.timeout().toNanos()));
			assignments.add(JavaAOPTestCaseToolbox.getPublicStaticFinalLongAssignment(STRICT_TIMEOUT_GRACE_FIELD_NAME,
					regardingTheStrictTimeoutForTheTestCases.terminationGrace().map(grace -> grace.toNanos())
							.orElse(NOT_CONFIGURED)));
		}
		if (regardingTheOutputOfTheTestCases != null) {
			assignments.add(JavaAOPTestCaseToolbox.getPublicStaticFinalBooleanAssignment(OUTPUT_MIRRORED_FIELD_NAME,
					regardingTheOutputOfTheTestCases.mirrored()));
			assignments.add(JavaAOPTestCaseToolbox.getPublicStaticFinalLongAssignment(OUTPUT_LIMIT_FIELD_NAME,
					regardingTheOutputOfTheTestCases.maximumCharacterCount()));
		}
		if (regardingTheFailureFeedbackForTheTestCases != null) {
			assignments.add(JavaAOPTestCaseToolbox.getPublicStaticFinalBooleanAssignment(
					FAILURE_FEEDBACK_PRIVILEGED_ONLY_FIELD_NAME,
					regardingTheFailureFeedbackForTheTestCases.privilegedOnly()));
			assignments.add(
					JavaAOPTestCaseToolbox.getPublicStaticFinalStringAssignment(FAILURE_FEEDBACK_MESSAGE_FIELD_NAME,
							regardingTheFailureFeedbackForTheTestCases.theFailureMessageForOtherExceptionsIs()));
			assignments
					.add(JavaAOPTestCaseToolbox.getPublicStaticFinalStringAssignment(FAILURE_FEEDBACK_LEVEL_FIELD_NAME,
							regardingTheFailureFeedbackForTheTestCases.feedbackLevel().name()));
		}
		VisibilityAndScheduleConfiguration visibility = regardingTheVisibilityAndScheduleOfTheTestCases;
		if (visibility != null) {
			assignments.add(JavaAOPTestCaseToolbox.getPublicStaticFinalLongAssignment(VISIBILITY_DEADLINE_FIELD_NAME,
					visibility.effectiveDeadline().toInstant().toEpochMilli()));
			assignments.add(JavaAOPTestCaseToolbox.getPublicStaticFinalLongAssignment(VISIBILITY_RUN_BEFORE_FIELD_NAME,
					visibility.alwaysRunBefore().map(date -> date.toInstant().toEpochMilli()).orElse(Long.MIN_VALUE)));
			assignments.add(listAssignment(VISIBILITY_HIDDEN_LIST_FIELD_NAME, visibility.theFollowingTestsAreHidden()));
			assignments.add(listAssignment(VISIBILITY_PUBLIC_LIST_FIELD_NAME, visibility.theFollowingTestsArePublic()));
			assignments.add(JavaAOPTestCaseToolbox.getPublicStaticFinalBooleanAssignment(
					VISIBILITY_UNLISTED_HIDDEN_FIELD_NAME, visibility.testsNotListedHereAreHidden()));
		}
		return List.copyOf(assignments);
	}

	/**
	 * The most characters one generated string literal holds. A Java class stores a
	 * literal in at most 65,535 bytes and a character takes up to three, so this
	 * leaves room to spare.
	 */
	private static final int LITERAL_CHARACTER_LIMIT = 20_000;

	/**
	 * The suffix numbering the constant parts of a list too long for one literal.
	 */
	private static final String PART_SUFFIX = "_PART_";

	/**
	 * Writes a list of test entries as one line-separated text field. A list too
	 * long for one literal is also written as numbered constant parts, each within
	 * the limit, so the generated class still compiles and a generated hook can
	 * join the parts it inlines.
	 *
	 * @param fieldName the field to assign.
	 * @param entries   the entries, in order.
	 * @return the assignments, each terminated with a newline
	 */
	@Nonnull
	private static String listAssignment(@Nonnull String fieldName, @Nonnull List<String> entries) {
		List<String> chunks = chunks(entries);
		if (chunks.size() == 1) {
			return JavaAOPTestCaseToolbox.getPublicStaticFinalStringAssignment(fieldName, chunks.get(0));
		}
		StringBuilder assignments = new StringBuilder();
		for (int index = 0; index < chunks.size(); index++) {
			assignments.append(JavaAOPTestCaseToolbox
					.getPublicStaticFinalStringAssignment(fieldName + PART_SUFFIX + index, chunks.get(index)));
		}
		return assignments.append("public static final String ").append(fieldName).append(" = ")
				.append(joinExpression(fieldName, chunks.size(), "")).append(';').append(System.lineSeparator())
				.toString();
	}

	/**
	 * The Java expression a generated hook reads a list field with. A short list is
	 * the constant itself; a long one joins its constant parts, which the compiler
	 * inlines into the hook, so replacing the settings class at run time changes
	 * nothing.
	 *
	 * @param settingsClass the settings class's name.
	 * @param fieldName     the list field.
	 * @param entries       the entries the field holds.
	 * @return the expression
	 */
	@Nonnull
	public static String listExpression(@Nonnull String settingsClass, @Nonnull String fieldName,
			@Nonnull List<String> entries) {
		int parts = chunks(entries).size();
		return parts == 1 ? settingsClass + "." + fieldName : joinExpression(fieldName, parts, settingsClass + ".");
	}

	/**
	 * Joins numbered parts of a list field with line breaks.
	 *
	 * @param fieldName the list field.
	 * @param parts     how many parts it has.
	 * @param qualifier what precedes each part's name, possibly empty.
	 * @return the expression
	 */
	@Nonnull
	private static String joinExpression(@Nonnull String fieldName, int parts, @Nonnull String qualifier) {
		List<String> names = new ArrayList<>();
		for (int index = 0; index < parts; index++) {
			names.add(qualifier + fieldName + PART_SUFFIX + index);
		}
		return "String.join(\"\\n\", " + String.join(", ", names) + ")";
	}

	/**
	 * Splits a list's entries, joined with line breaks, into texts within the
	 * literal limit; never splits an entry.
	 *
	 * @param entries the entries, in order.
	 * @return at least one text
	 */
	@Nonnull
	private static List<String> chunks(@Nonnull List<String> entries) {
		List<String> chunks = new ArrayList<>();
		StringBuilder chunk = new StringBuilder();
		for (String entry : entries) {
			if (chunk.length() > 0 && chunk.length() + 1 + entry.length() > LITERAL_CHARACTER_LIMIT) {
				chunks.add(chunk.toString());
				chunk.setLength(0);
			}
			chunk.append(chunk.length() > 0 ? "\n" : "").append(entry);
		}
		chunks.add(chunk.toString());
		return chunks;
	}

	/**
	 * Returns a builder for creating a TestBehaviorConfiguration instance.
	 *
	 * @since 2.2.0
	 * @author Luka Petrovic
	 * @return a new Builder instance.
	 */
	@Nonnull
	public static Builder builder() {
		return new Builder();
	}

	/**
	 * Builder for TestBehaviorConfiguration.
	 *
	 * @since 2.2.0
	 * @author Luka Petrovic
	 */
	public static class Builder {

		/** The strict-timeout category, or null for none. */
		@Nullable
		private StrictTimeoutConfiguration regardingTheStrictTimeoutForTheTestCases;

		/** The output category, or null for none. */
		@Nullable
		private OutputConfiguration regardingTheOutputOfTheTestCases;

		/** The failure-feedback category, or null for none. */
		@Nullable
		private FailureFeedbackConfiguration regardingTheFailureFeedbackForTheTestCases;

		/** The visibility-and-schedule category, or null for none. */
		@Nullable
		private VisibilityAndScheduleConfiguration regardingTheVisibilityAndScheduleOfTheTestCases;

		/**
		 * Sets the strict-timeout category.
		 *
		 * @since 2.3.0
		 * @author Luka Petrovic
		 * @param category the category; may be null.
		 * @return the updated Builder.
		 */
		@Nonnull
		public Builder regardingTheStrictTimeoutForTheTestCases(@Nullable StrictTimeoutConfiguration category) {
			this.regardingTheStrictTimeoutForTheTestCases = category;
			return this;
		}

		/**
		 * Sets the output category.
		 *
		 * @since 2.3.0
		 * @author Luka Petrovic
		 * @param category the category; may be null.
		 * @return the updated Builder.
		 */
		@Nonnull
		public Builder regardingTheOutputOfTheTestCases(@Nullable OutputConfiguration category) {
			this.regardingTheOutputOfTheTestCases = category;
			return this;
		}

		/**
		 * Sets the failure-feedback category.
		 *
		 * @since 2.3.0
		 * @author Luka Petrovic
		 * @param category the category; may be null.
		 * @return the updated Builder.
		 */
		@Nonnull
		public Builder regardingTheFailureFeedbackForTheTestCases(@Nullable FailureFeedbackConfiguration category) {
			this.regardingTheFailureFeedbackForTheTestCases = category;
			return this;
		}

		/**
		 * Sets the visibility-and-schedule category.
		 *
		 * @since 2.3.0
		 * @author Luka Petrovic
		 * @param category the category; may be null.
		 * @return the updated Builder.
		 */
		@Nonnull
		public Builder regardingTheVisibilityAndScheduleOfTheTestCases(
				@Nullable VisibilityAndScheduleConfiguration category) {
			this.regardingTheVisibilityAndScheduleOfTheTestCases = category;
			return this;
		}

		/**
		 * Builds a new TestBehaviorConfiguration instance.
		 *
		 * @since 2.2.0
		 * @author Luka Petrovic
		 * @return a new TestBehaviorConfiguration instance.
		 */
		@Nonnull
		public TestBehaviorConfiguration build() {
			return new TestBehaviorConfiguration(regardingTheStrictTimeoutForTheTestCases,
					regardingTheOutputOfTheTestCases, regardingTheFailureFeedbackForTheTestCases,
					regardingTheVisibilityAndScheduleOfTheTestCases);
		}
	}
}
