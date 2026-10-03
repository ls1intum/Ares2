package de.tum.cit.ase.ares.api.policy.policySubComponents;

import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import de.tum.cit.ase.ares.api.localization.Messages;

/**
 * The policy-wide default of {@code @StrictTimeout}: how long a supervised test
 * may run, and how long a timed-out test may take to stop before Ares halts the
 * test process.
 *
 * @since 2.1.5
 * @author Luka Petrovic
 * @param theTimeoutIs              the timeout; must be positive.
 * @param theTimeUnitIs             the timeout's unit; defaults to seconds.
 * @param theTerminationGraceIs     the grace period, or null when not
 *                                  configured; zero means stop at once.
 * @param theTerminationGraceUnitIs the grace period's unit; defaults to
 *                                  milliseconds.
 */
public record StrictTimeoutsConfiguration(long theTimeoutIs, @Nonnull TimeUnit theTimeUnitIs,
		@Nullable Long theTerminationGraceIs, @Nonnull TimeUnit theTerminationGraceUnitIs) {

	/** The timeout's unit when the policy names none, as {@code @StrictTimeout}. */
	public static final TimeUnit DEFAULT_TIME_UNIT = TimeUnit.SECONDS;

	/** The grace period's unit when the policy names none. */
	public static final TimeUnit DEFAULT_TERMINATION_GRACE_UNIT = TimeUnit.MILLISECONDS;

	/** The longest grace period a policy may configure. */
	public static final Duration MAXIMUM_TERMINATION_GRACE = Duration.ofDays(1);

	/**
	 * Constructs a StrictTimeoutsConfiguration, defaulting missing units and
	 * rejecting a timeout or grace period out of range.
	 *
	 * @throws IllegalArgumentException naming the field out of range
	 */
	public StrictTimeoutsConfiguration {
		theTimeUnitIs = theTimeUnitIs == null ? DEFAULT_TIME_UNIT : theTimeUnitIs;
		theTerminationGraceUnitIs = theTerminationGraceUnitIs == null ? DEFAULT_TERMINATION_GRACE_UNIT
				: theTerminationGraceUnitIs;
		if (theTimeoutIs <= 0 || durationOf(theTimeoutIs, theTimeUnitIs).isEmpty()) {
			throw new IllegalArgumentException(
					Messages.localized("policy.behavior.strict.timeouts.timeout.range", theTimeoutIs, theTimeUnitIs));
		}
		if (theTerminationGraceIs != null && !isAllowedGrace(theTerminationGraceIs, theTerminationGraceUnitIs)) {
			throw new IllegalArgumentException(Messages.localized("policy.behavior.strict.timeouts.grace.range",
					theTerminationGraceIs, theTerminationGraceUnitIs));
		}
	}

	/**
	 * The timeout as a duration.
	 *
	 * @return the timeout
	 */
	@Nonnull
	public Duration timeout() {
		return durationOf(theTimeoutIs, theTimeUnitIs).orElseThrow();
	}

	/**
	 * The grace period as a duration, if the policy configures one.
	 *
	 * @return the grace period, or empty when not configured
	 */
	@Nonnull
	public Optional<Duration> terminationGrace() {
		return Optional.ofNullable(theTerminationGraceIs)
				.flatMap(grace -> durationOf(grace, theTerminationGraceUnitIs));
	}

	/**
	 * Whether a grace period lies between zero and
	 * {@link #MAXIMUM_TERMINATION_GRACE}.
	 *
	 * @param grace the grace period's amount.
	 * @param unit  its unit.
	 * @return true when it is allowed
	 */
	private static boolean isAllowedGrace(long grace, @Nonnull TimeUnit unit) {
		return grace >= 0 && durationOf(grace, unit).filter(period -> period.compareTo(MAXIMUM_TERMINATION_GRACE) <= 0)
				.isPresent();
	}

	/**
	 * An amount in a unit as a duration, or empty when it does not fit one.
	 *
	 * @param amount the amount.
	 * @param unit   its unit.
	 * @return the duration, or empty on overflow
	 */
	@Nonnull
	private static Optional<Duration> durationOf(long amount, @Nonnull TimeUnit unit) {
		try {
			return Optional.of(Duration.of(amount, unit.toChronoUnit()));
		} catch (ArithmeticException tooLarge) {
			return Optional.empty();
		}
	}

	/**
	 * Returns a builder for creating a StrictTimeoutsConfiguration instance.
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
	 * Builder for StrictTimeoutsConfiguration.
	 *
	 * @since 2.1.5
	 * @author Luka Petrovic
	 */
	public static class Builder {

		/** The timeout to build with. */
		private long theTimeoutIs;

		/** The timeout's unit, or null to accept the default. */
		@Nullable
		private TimeUnit theTimeUnitIs;

		/** The grace period, or null when not configured. */
		@Nullable
		private Long theTerminationGraceIs;

		/** The grace period's unit, or null to accept the default. */
		@Nullable
		private TimeUnit theTerminationGraceUnitIs;

		/**
		 * Sets the timeout.
		 *
		 * @since 2.1.5
		 * @author Luka Petrovic
		 * @param theTimeoutIs the timeout; must be positive.
		 * @return the updated Builder.
		 */
		@Nonnull
		public Builder theTimeoutIs(long theTimeoutIs) {
			this.theTimeoutIs = theTimeoutIs;
			return this;
		}

		/**
		 * Sets the timeout's unit.
		 *
		 * @since 2.1.5
		 * @author Luka Petrovic
		 * @param theTimeUnitIs the unit, or null for the default.
		 * @return the updated Builder.
		 */
		@Nonnull
		public Builder theTimeUnitIs(@Nullable TimeUnit theTimeUnitIs) {
			this.theTimeUnitIs = theTimeUnitIs;
			return this;
		}

		/**
		 * Sets the grace period.
		 *
		 * @since 2.1.5
		 * @author Luka Petrovic
		 * @param theTerminationGraceIs the grace period, or null when not configured.
		 * @return the updated Builder.
		 */
		@Nonnull
		public Builder theTerminationGraceIs(@Nullable Long theTerminationGraceIs) {
			this.theTerminationGraceIs = theTerminationGraceIs;
			return this;
		}

		/**
		 * Sets the grace period's unit.
		 *
		 * @since 2.1.5
		 * @author Luka Petrovic
		 * @param theTerminationGraceUnitIs the unit, or null for the default.
		 * @return the updated Builder.
		 */
		@Nonnull
		public Builder theTerminationGraceUnitIs(@Nullable TimeUnit theTerminationGraceUnitIs) {
			this.theTerminationGraceUnitIs = theTerminationGraceUnitIs;
			return this;
		}

		/**
		 * Builds a new StrictTimeoutsConfiguration instance.
		 *
		 * @since 2.1.5
		 * @author Luka Petrovic
		 * @return a new StrictTimeoutsConfiguration instance.
		 * @throws IllegalArgumentException naming the field out of range
		 */
		@Nonnull
		public StrictTimeoutsConfiguration build() {
			return new StrictTimeoutsConfiguration(theTimeoutIs, theTimeUnitIs, theTerminationGraceIs,
					theTerminationGraceUnitIs);
		}
	}
}
