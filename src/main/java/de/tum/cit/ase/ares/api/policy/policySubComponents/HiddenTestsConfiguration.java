package de.tum.cit.ase.ares.api.policy.policySubComponents;

import java.lang.annotation.AnnotationFormatError;
import java.time.DateTimeException;
import java.time.Duration;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import de.tum.cit.ase.ares.api.context.TestType;
import de.tum.cit.ase.ares.api.internal.TestGuardUtils;
import de.tum.cit.ase.ares.api.localization.Messages;

/**
 * The policy-wide hidden-test schedule and test visibility: the deadline after
 * which hidden tests run, its extension, a date before which they always run,
 * which tests are hidden or public, and what an unlisted test is. It mirrors
 * the deadline annotations and {@code @Hidden}/{@code @Public}, which still
 * win.
 *
 * @since 2.1.5
 * @author Luka Petrovic
 * @param theDeadlineIs              the deadline, with a time zone; required.
 * @param theDeadlineIsExtendedBy    how long the deadline is extended, or null.
 * @param hiddenTestsAlwaysRunBefore a date before which hidden tests always
 *                                   run, with a time zone, or null.
 * @param unlistedTestsAreHidden     whether a test no entry covers is hidden;
 *                                   required.
 * @param theFollowingTestsAreHidden the hidden tests, as {@code pkg.Class} or
 *                                   {@code pkg.Class#method}; null means none.
 * @param theFollowingTestsArePublic the public tests, in the same form; null
 *                                   means none.
 */
public record HiddenTestsConfiguration(@Nullable String theDeadlineIs, @Nullable String theDeadlineIsExtendedBy,
		@Nullable String hiddenTestsAlwaysRunBefore, @Nullable Boolean unlistedTestsAreHidden,
		@Nullable List<String> theFollowingTestsAreHidden, @Nullable List<String> theFollowingTestsArePublic) {

	/**
	 * The package every generated class lives in. Its tests are never hidden, so
	 * the generated sentinels run even when unlisted tests are.
	 */
	public static final String GENERATED_PACKAGE = "de.tum.cit.ase.ares.generated";

	/** One Java identifier. */
	private static final String IDENTIFIER = "\\p{javaJavaIdentifierStart}\\p{javaJavaIdentifierPart}*";

	/** A class name with dots, optionally followed by {@code #method}. */
	private static final Pattern ENTRY = Pattern
			.compile(IDENTIFIER + "(?:\\." + IDENTIFIER + ")*(?:#" + IDENTIFIER + ")?");

	/**
	 * Constructs a HiddenTestsConfiguration, rejecting a missing deadline or
	 * visibility switch, a date without a time zone, a malformed value, a malformed
	 * list entry and an entry in both lists.
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
		if (unlistedTestsAreHidden == null) {
			throw new IllegalArgumentException(Messages.localized("policy.behavior.hidden.tests.unlisted.missing"));
		}
		theFollowingTestsAreHidden = checkedEntries("theFollowingTestsAreHidden", theFollowingTestsAreHidden);
		theFollowingTestsArePublic = checkedEntries("theFollowingTestsArePublic", theFollowingTestsArePublic);
		Set<String> inBoth = new HashSet<>(theFollowingTestsAreHidden);
		inBoth.retainAll(theFollowingTestsArePublic);
		if (!inBoth.isEmpty()) {
			throw new IllegalArgumentException(Messages.localized("policy.behavior.hidden.tests.entry.conflict",
					inBoth.stream().sorted().findFirst().orElseThrow()));
		}
	}

	/**
	 * An entry list as an unmodifiable copy, empty for null, each entry checked.
	 *
	 * @param field   the list's field name.
	 * @param entries the entries, or null.
	 * @return the checked entries
	 * @throws IllegalArgumentException naming the field and the malformed entry
	 */
	@Nonnull
	private static List<String> checkedEntries(@Nonnull String field, @Nullable List<String> entries) {
		List<String> given = entries == null ? List.of() : entries;
		for (String entry : given) {
			if (entry == null || !ENTRY.matcher(entry).matches()) {
				throw new IllegalArgumentException(
						Messages.localized("policy.behavior.hidden.tests.entry.invalid", field, entry));
			}
		}
		return List.copyOf(given);
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
	 * The visibility the policy gives a test. The nearest entry wins: the test's
	 * own class with its method, then that class, then each enclosing class. A
	 * method entry never reaches into a nested class. Without an entry,
	 * {@code unlistedTestsAreHidden} decides. A generated class is public.
	 *
	 * @param testClass  the test's class.
	 * @param methodName the test's method, or empty.
	 * @return hidden or public, or empty when the policy leaves the test untyped
	 */
	@Nonnull
	public Optional<TestType> visibilityOf(@Nonnull Class<?> testClass, @Nonnull Optional<String> methodName) {
		if (testClass.getName().startsWith(GENERATED_PACKAGE + ".")) {
			return Optional.of(TestType.PUBLIC);
		}
		Optional<String> ownClass = Optional.ofNullable(testClass.getCanonicalName());
		Optional<TestType> byMethod = ownClass
				.flatMap(name -> methodName.flatMap(method -> listed(name + "#" + method)));
		if (byMethod.isPresent()) {
			return byMethod;
		}
		for (Class<?> current = testClass; current != null; current = current.getEnclosingClass()) {
			Optional<TestType> byClass = Optional.ofNullable(current.getCanonicalName()).flatMap(this::listed);
			if (byClass.isPresent()) {
				return byClass;
			}
		}
		return Boolean.TRUE.equals(unlistedTestsAreHidden) ? Optional.of(TestType.HIDDEN) : Optional.empty();
	}

	/**
	 * Which list names an entry exactly.
	 *
	 * @param entry the entry.
	 * @return hidden or public, or empty when neither list names it
	 */
	@Nonnull
	private Optional<TestType> listed(@Nonnull String entry) {
		if (theFollowingTestsAreHidden.contains(entry)) {
			return Optional.of(TestType.HIDDEN);
		}
		return theFollowingTestsArePublic.contains(entry) ? Optional.of(TestType.PUBLIC) : Optional.empty();
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

		/** Whether unlisted tests are hidden, or null when not set. */
		@Nullable
		private Boolean unlistedTestsAreHidden;

		/** The hidden tests collected so far. */
		@Nonnull
		private final List<String> theFollowingTestsAreHidden = new ArrayList<>();

		/** The public tests collected so far. */
		@Nonnull
		private final List<String> theFollowingTestsArePublic = new ArrayList<>();

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
		 * Sets whether a test no entry covers is hidden.
		 *
		 * @since 2.1.5
		 * @author Luka Petrovic
		 * @param unlistedTestsAreHidden true to hide unlisted tests.
		 * @return the updated Builder.
		 */
		@Nonnull
		public Builder unlistedTestsAreHidden(@Nullable Boolean unlistedTestsAreHidden) {
			this.unlistedTestsAreHidden = unlistedTestsAreHidden;
			return this;
		}

		/**
		 * Adds public tests.
		 *
		 * @since 2.1.5
		 * @author Luka Petrovic
		 * @param entries entries such as {@code pkg.Class} or {@code pkg.Class#m}.
		 * @return the updated Builder.
		 */
		@Nonnull
		public Builder publicTests(@Nonnull String... entries) {
			theFollowingTestsArePublic.addAll(List.of(entries));
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
					unlistedTestsAreHidden, theFollowingTestsAreHidden, theFollowingTestsArePublic);
		}
	}
}
