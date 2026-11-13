package de.tum.cit.ase.ares.api.policy.policySubComponents;

import java.time.Duration;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import de.tum.cit.ase.ares.api.localization.Messages;

/**
 * Reads the durations a policy writes as text, such as {@code "2s"},
 * {@code "500ms"} or {@code "1d 12h"}. Days, hours, minutes, seconds and
 * milliseconds may each appear once, largest first, in any letter case. The
 * same rules serve the strict timeout, its grace and the deadline extension.
 *
 * @since 2.3.0
 * @author Luka Petrovic
 */
public final class PolicyDuration {

	/**
	 * The grammar. {@code ms} is tried before {@code m}, so {@code "5MS"} is five
	 * milliseconds and {@code "5M"} five minutes.
	 */
	private static final Pattern GRAMMAR = Pattern.compile("(?:(?<d>\\d++)d)?\\s*+(?:(?<h>\\d++)h)?\\s*+"
			+ "(?:(?<m>\\d++)m(?!s))?\\s*+(?:(?<s>\\d++)s)?\\s*+(?:(?<ms>\\d++)ms)?", Pattern.CASE_INSENSITIVE);

	/** Not instantiable. */
	private PolicyDuration() {
		throw new UnsupportedOperationException("PolicyDuration is a utility class");
	}

	/**
	 * Reads a duration, or nothing when the text does not follow the grammar, has
	 * no unit at all, or is too long for a {@link Duration}.
	 *
	 * @since 2.3.0
	 * @author Luka Petrovic
	 * @param text the text to read; may be null.
	 * @return the duration, never negative; empty when the text is not one.
	 */
	@Nonnull
	public static Optional<Duration> tryParse(@Nullable String text) {
		if (text == null) {
			return Optional.empty();
		}
		Matcher matcher = GRAMMAR.matcher(text.strip());
		if (!matcher.matches() || matcher.group("d") == null && matcher.group("h") == null && matcher.group("m") == null
				&& matcher.group("s") == null && matcher.group("ms") == null) {
			return Optional.empty();
		}
		try {
			Duration duration = Duration.ofDays(amount(matcher, "d")).plusHours(amount(matcher, "h"))
					.plusMinutes(amount(matcher, "m")).plusSeconds(amount(matcher, "s"))
					.plusMillis(amount(matcher, "ms"));
			return Optional.of(duration);
		} catch (ArithmeticException | NumberFormatException tooLong) {
			return Optional.empty();
		}
	}

	/**
	 * Reads a duration a policy field holds, refusing text that is not one.
	 *
	 * @since 2.3.0
	 * @author Luka Petrovic
	 * @param field the field's name, for the refusal message.
	 * @param text  the text to read.
	 * @return the duration, never negative.
	 * @throws IllegalArgumentException naming the field when the text is not a
	 *                                  duration.
	 */
	@Nonnull
	public static Duration parse(@Nonnull String field, @Nullable String text) {
		return tryParse(text).orElseThrow(() -> new IllegalArgumentException(
				Messages.localized("policy.behavior.duration.invalid", field, String.valueOf(text))));
	}

	/**
	 * Returns one unit's amount, zero when that unit is absent.
	 *
	 * @param matcher the successful match.
	 * @param unit    the group's name.
	 * @return the amount.
	 */
	private static long amount(@Nonnull Matcher matcher, @Nonnull String unit) {
		String value = matcher.group(unit);
		return value == null ? 0 : Long.parseLong(value);
	}
}
