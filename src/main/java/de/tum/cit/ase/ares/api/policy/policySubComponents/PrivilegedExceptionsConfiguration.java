package de.tum.cit.ase.ares.api.policy.policySubComponents;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Policy-level default for hiding non-privileged test-failure details.
 * <p>
 * Description: Mirrors what the {@code @PrivilegedExceptionsOnly} annotation
 * already controls per test, as a policy-wide default a supervised test falls
 * back to when it carries no such annotation itself.
 * <p>
 * Design Rationale: A plain, self-validating record like every other policy
 * value in this package. Nothing here is read by woven runtime advice, only by
 * ordinary reporting code, so it carries none of
 * {@code JavaAOPTestCaseSettings}'s classloader-crossing machinery.
 *
 * @since 2.1.5
 * @author Luka Petrovic
 * @param onlyPrivilegedExceptionsAreReported whether only privileged exceptions
 *                                            are reported by default for
 *                                            supervised tests without their own
 *                                            annotation.
 * @param theFailureMessageIs                 the message shown for a
 *                                            non-privileged failure; defaults
 *                                            to
 *                                            {@value #DEFAULT_FAILURE_MESSAGE}
 *                                            when null or blank.
 */
public record PrivilegedExceptionsConfiguration(boolean onlyPrivilegedExceptionsAreReported,
		@Nonnull String theFailureMessageIs) {

	/**
	 * Default failure message, matching {@code @PrivilegedExceptionsOnly}'s own
	 * annotation default.
	 */
	public static final String DEFAULT_FAILURE_MESSAGE = "Test failed.";

	/**
	 * Constructs a PrivilegedExceptionsConfiguration, defaulting a missing or blank
	 * message.
	 */
	public PrivilegedExceptionsConfiguration {
		if (theFailureMessageIs == null || theFailureMessageIs.isBlank()) {
			theFailureMessageIs = DEFAULT_FAILURE_MESSAGE;
		}
	}

	/**
	 * Returns a builder for creating a PrivilegedExceptionsConfiguration instance.
	 *
	 * @since 2.1.5
	 * @author Luka Petrovic
	 * @return a new Builder instance.
	 */
	@Nonnull
	public static Builder builder() {
		return new Builder();
	}

	/**
	 * Builder for PrivilegedExceptionsConfiguration.
	 *
	 * @since 2.1.5
	 * @author Luka Petrovic
	 */
	public static class Builder {
		private boolean onlyPrivilegedExceptionsAreReported;
		@Nullable
		private String theFailureMessageIs;

		/**
		 * Sets whether only privileged exceptions are reported by default.
		 *
		 * @since 2.1.5
		 * @author Luka Petrovic
		 * @param onlyPrivilegedExceptionsAreReported the on/off switch.
		 * @return the updated Builder.
		 */
		@Nonnull
		public Builder onlyPrivilegedExceptionsAreReported(boolean onlyPrivilegedExceptionsAreReported) {
			this.onlyPrivilegedExceptionsAreReported = onlyPrivilegedExceptionsAreReported;
			return this;
		}

		/**
		 * Sets the non-privileged failure message.
		 *
		 * @since 2.1.5
		 * @author Luka Petrovic
		 * @param theFailureMessageIs the failure message; may be null to accept the
		 *                            default.
		 * @return the updated Builder.
		 */
		@Nonnull
		public Builder theFailureMessageIs(@Nullable String theFailureMessageIs) {
			this.theFailureMessageIs = theFailureMessageIs;
			return this;
		}

		/**
		 * Builds a new PrivilegedExceptionsConfiguration instance.
		 *
		 * @since 2.1.5
		 * @author Luka Petrovic
		 * @return a new PrivilegedExceptionsConfiguration instance.
		 */
		@Nonnull
		public PrivilegedExceptionsConfiguration build() {
			return new PrivilegedExceptionsConfiguration(onlyPrivilegedExceptionsAreReported, theFailureMessageIs);
		}
	}
}
