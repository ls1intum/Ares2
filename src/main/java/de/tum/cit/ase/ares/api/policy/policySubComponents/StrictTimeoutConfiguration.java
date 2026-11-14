package de.tum.cit.ase.ares.api.policy.policySubComponents;

import java.time.Duration;
import java.util.Optional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import de.tum.cit.ase.ares.api.localization.Messages;

/**
 * The policy-wide default of {@code @StrictTimeout}: how long a supervised test
 * may run, and how long a timed-out test may take to stop before Ares halts the
 * test process. Both are durations such as {@code "2s"} or {@code "500ms"},
 * read by {@link PolicyDuration}.
 *
 * @since 2.3.0
 * @author Luka Petrovic
 * @param theTimeoutIs          the timeout; required and above zero.
 * @param theTerminationGraceIs the grace period, or null when not configured;
 *                              zero means stop at once.
 */
public record StrictTimeoutConfiguration(@Nonnull String theTimeoutIs, @Nullable String theTerminationGraceIs) {

	/** The longest grace period a policy may configure. */
	public static final Duration MAXIMUM_TERMINATION_GRACE = Duration.ofDays(1);

	/**
	 * Constructs a StrictTimeoutConfiguration, refusing a timeout that is missing,
	 * not a duration or zero, and a grace period above one day.
	 *
	 * @since 2.3.0
	 * @author Luka Petrovic
	 * @throws IllegalArgumentException naming the field out of range.
	 */
	public StrictTimeoutConfiguration {
		Duration timeout = PolicyDuration.parse("theTimeoutIs", theTimeoutIs);
		if (timeout.isZero() || !fitsInNanoseconds(timeout)) {
			throw new IllegalArgumentException(
					Messages.localized("policy.behavior.strict.timeouts.timeout.range", theTimeoutIs));
		}
		if (theTerminationGraceIs != null && PolicyDuration.parse("theTerminationGraceIs", theTerminationGraceIs)
				.compareTo(MAXIMUM_TERMINATION_GRACE) > 0) {
			throw new IllegalArgumentException(
					Messages.localized("policy.behavior.strict.timeouts.grace.range", theTerminationGraceIs));
		}
	}

	/**
	 * Tells whether a duration can be counted in nanoseconds, the precision the
	 * timeout is enforced with.
	 *
	 * @param duration the duration.
	 * @return whether it fits.
	 */
	private static boolean fitsInNanoseconds(@Nonnull Duration duration) {
		try {
			return duration.toNanos() >= 0;
		} catch (ArithmeticException tooLong) {
			return false;
		}
	}

	/**
	 * Returns the timeout.
	 *
	 * @since 2.3.0
	 * @author Luka Petrovic
	 * @return the timeout, above zero.
	 */
	@Nonnull
	public Duration timeout() {
		return PolicyDuration.parse("theTimeoutIs", theTimeoutIs);
	}

	/**
	 * Returns the grace period, if the policy configures one.
	 *
	 * @since 2.3.0
	 * @author Luka Petrovic
	 * @return the grace period; empty when not configured.
	 */
	@Nonnull
	public Optional<Duration> terminationGrace() {
		return Optional.ofNullable(theTerminationGraceIs)
				.map(grace -> PolicyDuration.parse("theTerminationGraceIs", grace));
	}

	/**
	 * Returns a builder for creating a StrictTimeoutConfiguration instance.
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
	 * Builder for StrictTimeoutConfiguration.
	 *
	 * @since 2.3.0
	 * @author Luka Petrovic
	 */
	public static final class Builder {

		/** The timeout to build with. */
		@Nullable
		private String theTimeoutIs;

		/** The grace period to build with, or null for none. */
		@Nullable
		private String theTerminationGraceIs;

		/** Use {@link StrictTimeoutConfiguration#builder()}. */
		private Builder() {
		}

		/**
		 * Sets the timeout.
		 *
		 * @since 2.3.0
		 * @author Luka Petrovic
		 * @param theTimeoutIs the timeout, such as {@code "2s"}.
		 * @return the updated Builder.
		 */
		@Nonnull
		public Builder theTimeoutIs(@Nullable String theTimeoutIs) {
			this.theTimeoutIs = theTimeoutIs;
			return this;
		}

		/**
		 * Sets the grace period.
		 *
		 * @since 2.3.0
		 * @author Luka Petrovic
		 * @param theTerminationGraceIs the grace period, such as {@code "500ms"}; may
		 *                              be null.
		 * @return the updated Builder.
		 */
		@Nonnull
		public Builder theTerminationGraceIs(@Nullable String theTerminationGraceIs) {
			this.theTerminationGraceIs = theTerminationGraceIs;
			return this;
		}

		/**
		 * Builds a new StrictTimeoutConfiguration instance.
		 *
		 * @since 2.3.0
		 * @author Luka Petrovic
		 * @return a new StrictTimeoutConfiguration instance.
		 * @throws IllegalArgumentException when a value is out of range.
		 */
		@Nonnull
		public StrictTimeoutConfiguration build() {
			return new StrictTimeoutConfiguration(theTimeoutIs, theTerminationGraceIs);
		}
	}
}
