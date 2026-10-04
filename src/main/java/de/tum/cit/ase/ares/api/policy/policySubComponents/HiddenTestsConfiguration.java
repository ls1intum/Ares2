package de.tum.cit.ase.ares.api.policy.policySubComponents;

import java.lang.annotation.AnnotationFormatError;
import java.time.DateTimeException;
import java.time.Duration;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import de.tum.cit.ase.ares.api.internal.TestGuardUtils;
import de.tum.cit.ase.ares.api.localization.Messages;

/**
 * The policy-wide hidden-test schedule: the deadline after which hidden tests
 * run, how long it is extended, a date before which they always run, and which
 * tests are hidden. It mirrors {@code @Deadline}, {@code @ExtendedDeadline} and
 * {@code @ActivateHiddenBefore}, which still win on a class or method.
 *
 * @since 2.1.5
 * @author Luka Petrovic
 * @param theDeadlineIs              the deadline, with a time zone; required.
 * @param theDeadlineIsExtendedBy    how long the deadline is extended, or null.
 * @param hiddenTestsAlwaysRunBefore a date before which hidden tests always
 *                                   run, with a time zone, or null.
 * @param theFollowingTestsAreHidden the hidden tests, as {@code pkg.Class} or
 *                                   {@code pkg.Class#method}; null means none.
 */
public record HiddenTestsConfiguration(@Nullable String theDeadlineIs, @Nullable String theDeadlineIsExtendedBy,
		@Nullable String hiddenTestsAlwaysRunBefore, @Nullable List<String> theFollowingTestsAreHidden) {

	/** One Java identifier. */
	private static final String IDENTIFIER = "\\p{javaJavaIdentifierStart}\\p{javaJavaIdentifierPart}*";

	/** A class name with dots, optionally followed by {@code #method}. */
	private static final Pattern ENTRY = Pattern
			.compile(IDENTIFIER + "(?:\\." + IDENTIFIER + ")*(?:#" + IDENTIFIER + ")?");

	/**
	 * Constructs a HiddenTestsConfiguration, rejecting a missing deadline, a date
	 * without a time zone, a malformed value and a malformed list entry.
	 *
	 * @throws IllegalArgumentException naming the field or entry
	 */
	public HiddenTestsConfiguration {
		if (theDeadlineIs == null || theDeadlineIs.isBlank()) {
			throw new IllegalArgumentException(Messages.localized("policy.behavior.hidden.tests.deadline.missing"));
		}
		parseDate("theDeadlineIs", theDeadlineIs);
		if (theDeadlineIsExtendedBy != null) {
			parseExtension(theDeadlineIsExtendedBy);
		}
		if (hiddenTestsAlwaysRunBefore != null) {
			parseDate("hiddenTestsAlwaysRunBefore", hiddenTestsAlwaysRunBefore);
		}
		theFollowingTestsAreHidden = theFollowingTestsAreHidden == null ? List.of()
				: List.copyOf(theFollowingTestsAreHidden);
		for (String entry : theFollowingTestsAreHidden) {
			if (entry == null || !ENTRY.matcher(entry).matches()) {
				throw new IllegalArgumentException(
						Messages.localized("policy.behavior.hidden.tests.entry.invalid", entry));
			}
		}
	}

	/**
	 * The deadline plus its extension: hidden tests run once now is past it.
	 *
	 * @return the effective deadline
	 */
	@Nonnull
	public ZonedDateTime effectiveDeadline() {
		ZonedDateTime deadline = parseDate("theDeadlineIs", theDeadlineIs);
		return extension().map(deadline::plus).orElse(deadline);
	}

	/**
	 * How long the policy extends its own deadline.
	 *
	 * @return the extension, if configured
	 */
	@Nonnull
	public Optional<Duration> extension() {
		return Optional.ofNullable(theDeadlineIsExtendedBy).map(HiddenTestsConfiguration::parseExtension);
	}

	/**
	 * The date before which hidden tests always run.
	 *
	 * @return the date, if configured
	 */
	@Nonnull
	public Optional<ZonedDateTime> alwaysRunBefore() {
		return Optional.ofNullable(hiddenTestsAlwaysRunBefore)
				.map(value -> parseDate("hiddenTestsAlwaysRunBefore", value));
	}

	/**
	 * Whether the list names a test: its class, a class enclosing it, or that class
	 * together with the test's method.
	 *
	 * @param testClass  the test's class.
	 * @param methodName the test's method, or empty.
	 * @return true when an entry covers the test
	 */
	public boolean covers(@Nonnull Class<?> testClass, @Nonnull Optional<String> methodName) {
		for (Class<?> current = testClass; current != null; current = current.getEnclosingClass()) {
			String className = current.getCanonicalName();
			if (className != null && (theFollowingTestsAreHidden.contains(className) || methodName
					.map(name -> theFollowingTestsAreHidden.contains(className + "#" + name)).orElse(false))) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Parses a date in the {@code @Deadline} format, requiring a time zone.
	 *
	 * @param field the field the value comes from.
	 * @param value the value.
	 * @return the date
	 * @throws IllegalArgumentException naming the field
	 */
	@Nonnull
	private static ZonedDateTime parseDate(@Nonnull String field, @Nonnull String value) {
		if (!hasZone(value)) {
			throw new IllegalArgumentException(
					Messages.localized("policy.behavior.hidden.tests.zone.missing", field, value));
		}
		try {
			return TestGuardUtils.parseDeadline(value.strip());
		} catch (AnnotationFormatError | DateTimeException malformed) {
			throw new IllegalArgumentException(
					Messages.localized("policy.behavior.hidden.tests.value.invalid", field, value), malformed);
		}
	}

	/**
	 * Parses a duration in the {@code @ExtendedDeadline} format.
	 *
	 * @param value the value.
	 * @return the positive duration
	 * @throws IllegalArgumentException naming the field
	 */
	@Nonnull
	private static Duration parseExtension(@Nonnull String value) {
		try {
			return TestGuardUtils.parseDuration(value.strip());
		} catch (AnnotationFormatError malformed) {
			throw new IllegalArgumentException(
					Messages.localized("policy.behavior.hidden.tests.value.invalid", "theDeadlineIsExtendedBy", value),
					malformed);
		}
	}

	/**
	 * Whether a date ends in a time zone, as its last space-separated part.
	 *
	 * @param value the date.
	 * @return true when the last part is a zone or offset
	 */
	private static boolean hasZone(@Nonnull String value) {
		String stripped = value.strip();
		int lastSpace = stripped.lastIndexOf(' ');
		if (lastSpace < 0) {
			return false;
		}
		try {
			ZoneId.of(stripped.substring(lastSpace + 1), ZoneId.SHORT_IDS);
			return true;
		} catch (DateTimeException notAZone) {
			return false;
		}
	}

	/**
	 * Returns a builder for creating a HiddenTestsConfiguration instance.
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
	 * Builder for HiddenTestsConfiguration.
	 *
	 * @since 2.1.5
	 * @author Luka Petrovic
	 */
	public static class Builder {

		/** The deadline, or null when not set. */
		@Nullable
		private String theDeadlineIs;

		/** The extension, or null when not set. */
		@Nullable
		private String theDeadlineIsExtendedBy;

		/** The always-run-before date, or null when not set. */
		@Nullable
		private String hiddenTestsAlwaysRunBefore;

		/** The hidden tests collected so far. */
		@Nonnull
		private final List<String> theFollowingTestsAreHidden = new ArrayList<>();

		/**
		 * Sets the deadline.
		 *
		 * @since 2.1.5
		 * @author Luka Petrovic
		 * @param theDeadlineIs the deadline, with a time zone.
		 * @return the updated Builder.
		 */
		@Nonnull
		public Builder theDeadlineIs(@Nullable String theDeadlineIs) {
			this.theDeadlineIs = theDeadlineIs;
			return this;
		}

		/**
		 * Sets how long the deadline is extended.
		 *
		 * @since 2.1.5
		 * @author Luka Petrovic
		 * @param theDeadlineIsExtendedBy the extension, such as {@code 1d 12h}.
		 * @return the updated Builder.
		 */
		@Nonnull
		public Builder theDeadlineIsExtendedBy(@Nullable String theDeadlineIsExtendedBy) {
			this.theDeadlineIsExtendedBy = theDeadlineIsExtendedBy;
			return this;
		}

		/**
		 * Sets the date before which hidden tests always run.
		 *
		 * @since 2.1.5
		 * @author Luka Petrovic
		 * @param hiddenTestsAlwaysRunBefore the date, with a time zone.
		 * @return the updated Builder.
		 */
		@Nonnull
		public Builder hiddenTestsAlwaysRunBefore(@Nullable String hiddenTestsAlwaysRunBefore) {
			this.hiddenTestsAlwaysRunBefore = hiddenTestsAlwaysRunBefore;
			return this;
		}

		/**
		 * Adds hidden tests.
		 *
		 * @since 2.1.5
		 * @author Luka Petrovic
		 * @param entries entries such as {@code pkg.Class} or {@code pkg.Class#m}.
		 * @return the updated Builder.
		 */
		@Nonnull
		public Builder hiddenTests(@Nonnull String... entries) {
			theFollowingTestsAreHidden.addAll(List.of(entries));
			return this;
		}

		/**
		 * Builds a new HiddenTestsConfiguration instance.
		 *
		 * @since 2.1.5
		 * @author Luka Petrovic
		 * @return a new HiddenTestsConfiguration instance.
		 * @throws IllegalArgumentException if a value is missing or malformed
		 */
		@Nonnull
		public HiddenTestsConfiguration build() {
			return new HiddenTestsConfiguration(theDeadlineIs, theDeadlineIsExtendedBy, hiddenTestsAlwaysRunBefore,
					theFollowingTestsAreHidden);
		}
	}
}
