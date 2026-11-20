package de.tum.cit.ase.ares.api.policy.policySubComponents;

import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import de.tum.cit.ase.ares.api.localization.Messages;

/**
 * Reads the durations a policy writes as text: a whole number, one space and
 * the exact name of a {@link TimeUnit} constant, such as {@code "2 SECONDS"} or
 * {@code "36 HOURS"}. The unit is read with {@link TimeUnit#valueOf(String)},
 * so only the seven upper-case names are accepted. The same rules serve the
 * strict timeout, its grace and the deadline extension.
 *
 * @since 2.3.0
 * @author Luka Petrovic
 */
public final class PolicyDuration {

	/** The grammar: digits, exactly one space, upper-case letters. */
	private static final Pattern GRAMMAR = Pattern.compile("^(\\d+) ([A-Z]+)$");

	/** Not instantiable. */
	private PolicyDuration() {
		throw new UnsupportedOperationException("PolicyDuration is a utility class");
	}

	/**
	 * Reads a duration, or nothing when the text does not follow the grammar, names
	 * no {@link TimeUnit}, or is too long to count in nanoseconds.
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
		Matcher matcher = GRAMMAR.matcher(text);
		if (!matcher.matches()) {
			return Optional.empty();
		}
		try {
			long amount = Long.parseLong(matcher.group(1));
			TimeUnit unit = TimeUnit.valueOf(matcher.group(2));
			if (unit.toNanos(amount) == Long.MAX_VALUE) {
				return Optional.empty();
			}
			return Optional.of(Duration.of(amount, unit.toChronoUnit()));
		} catch (IllegalArgumentException | ArithmeticException notADuration) {
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
}
