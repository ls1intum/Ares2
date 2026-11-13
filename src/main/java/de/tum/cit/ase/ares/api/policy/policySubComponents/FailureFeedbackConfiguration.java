package de.tum.cit.ase.ares.api.policy.policySubComponents;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * What a student sees when a public test fails: either the real error or a
 * fixed message, and how much a blocked action reveals. Every field is
 * optional; the defaults are the behaviour without this setting.
 *
 * @since 2.3.0
 * @author Luka Petrovic
 * @param onlyPrivilegedExceptionsAreReported   whether other failures show the
 *                                              fixed message; null means no.
 * @param theFailureMessageForOtherExceptionsIs the fixed message; null or blank
 *                                              means
 *                                              {@value #DEFAULT_FAILURE_MESSAGE}.
 * @param theSecurityViolationFeedbackLevelIs   how much a blocked action
 *                                              reveals; null means
 *                                              {@code FULL}.
 */
public record FailureFeedbackConfiguration(@Nullable Boolean onlyPrivilegedExceptionsAreReported,
		@Nullable String theFailureMessageForOtherExceptionsIs,
		@Nullable SecurityViolationFeedbackLevel theSecurityViolationFeedbackLevelIs) {

	/** Default message for a non-privileged public failure. */
	public static final String DEFAULT_FAILURE_MESSAGE = "Test failed.";

	/**
	 * Constructs a FailureFeedbackConfiguration, defaulting a missing or blank
	 * message.
	 *
	 * @since 2.3.0
	 * @author Luka Petrovic
	 */
	public FailureFeedbackConfiguration {
		if (theFailureMessageForOtherExceptionsIs == null || theFailureMessageForOtherExceptionsIs.isBlank()) {
			theFailureMessageForOtherExceptionsIs = DEFAULT_FAILURE_MESSAGE;
		}
	}

	/**
	 * Returns whether other failures show the fixed message, false when not
	 * configured.
	 *
	 * @since 2.3.0
	 * @author Luka Petrovic
	 * @return whether privileged-only reporting is on.
	 */
	public boolean privilegedOnly() {
		return Boolean.TRUE.equals(onlyPrivilegedExceptionsAreReported);
	}

	/**
	 * Returns how much a blocked action reveals, {@code FULL} when not configured.
	 *
	 * @since 2.3.0
	 * @author Luka Petrovic
	 * @return the feedback level.
	 */
	@Nonnull
	public SecurityViolationFeedbackLevel feedbackLevel() {
		return theSecurityViolationFeedbackLevelIs == null ? SecurityViolationFeedbackLevel.FULL
				: theSecurityViolationFeedbackLevelIs;
	}

	/**
	 * Returns a builder for creating a FailureFeedbackConfiguration instance.
	 *
	 * @since 2.3.0
	 * @author Luka Petrovic
	 * @return a new Builder instance.
	 */
	@Nonnull
	public static Builder builder() {
		return new Builder();
	}

	/**
	 * Builder for FailureFeedbackConfiguration.
	 *
	 * @since 2.3.0
	 * @author Luka Petrovic
	 */
	public static final class Builder {

		/** Whether privileged-only reporting is on, or null. */
		@Nullable
		private Boolean onlyPrivilegedExceptionsAreReported;

		/** The fixed message, or null for the default. */
		@Nullable
		private String theFailureMessageForOtherExceptionsIs;

		/** The feedback level, or null for {@code FULL}. */
		@Nullable
		private SecurityViolationFeedbackLevel theSecurityViolationFeedbackLevelIs;

		/** Use {@link FailureFeedbackConfiguration#builder()}. */
		private Builder() {
		}

		/**
		 * Sets whether other failures show the fixed message.
		 *
		 * @since 2.3.0
		 * @author Luka Petrovic
		 * @param onlyPrivilegedExceptionsAreReported the switch; may be null.
		 * @return the updated Builder.
		 */
		@Nonnull
		public Builder onlyPrivilegedExceptionsAreReported(@Nullable Boolean onlyPrivilegedExceptionsAreReported) {
			this.onlyPrivilegedExceptionsAreReported = onlyPrivilegedExceptionsAreReported;
			return this;
		}

		/**
		 * Sets the fixed message.
		 *
		 * @since 2.3.0
		 * @author Luka Petrovic
		 * @param theFailureMessageForOtherExceptionsIs the message; may be null.
		 * @return the updated Builder.
		 */
		@Nonnull
		public Builder theFailureMessageForOtherExceptionsIs(@Nullable String theFailureMessageForOtherExceptionsIs) {
			this.theFailureMessageForOtherExceptionsIs = theFailureMessageForOtherExceptionsIs;
			return this;
		}

		/**
		 * Sets how much a blocked action reveals.
		 *
		 * @since 2.3.0
		 * @author Luka Petrovic
		 * @param theSecurityViolationFeedbackLevelIs the level; may be null.
		 * @return the updated Builder.
		 */
		@Nonnull
		public Builder theSecurityViolationFeedbackLevelIs(
				@Nullable SecurityViolationFeedbackLevel theSecurityViolationFeedbackLevelIs) {
			this.theSecurityViolationFeedbackLevelIs = theSecurityViolationFeedbackLevelIs;
			return this;
		}

		/**
		 * Builds a new FailureFeedbackConfiguration instance.
		 *
		 * @since 2.3.0
		 * @author Luka Petrovic
		 * @return a new FailureFeedbackConfiguration instance.
		 */
		@Nonnull
		public FailureFeedbackConfiguration build() {
			return new FailureFeedbackConfiguration(onlyPrivilegedExceptionsAreReported,
					theFailureMessageForOtherExceptionsIs, theSecurityViolationFeedbackLevelIs);
		}
	}
}
