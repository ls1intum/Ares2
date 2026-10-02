package de.tum.cit.ase.ares.api.policy.policySubComponents;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * The policy-wide default of {@code @PrivilegedExceptionsOnly}: whether a
 * failed test without that annotation shows a fixed message instead of its real
 * error.
 *
 * @since 2.1.5
 * @author Luka Petrovic
 * @param onlyPrivilegedExceptionsAreReported whether the fixed message is
 *                                            shown.
 * @param theFailureMessageIs                 the fixed message; defaults to
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
		/** The on/off switch to build with. */
		private boolean onlyPrivilegedExceptionsAreReported;
		/** The failure message to build with, or null to accept the default. */
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
