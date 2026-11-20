package de.tum.cit.ase.ares.api.internal;

import java.util.Optional;

import de.tum.cit.ase.ares.api.localization.Messages;

/**
 * The texts a student sees for a blocked action at each feedback level, built
 * from the message files, so the runtime report and the generated report are
 * checked against the same strings in any language.
 */
public final class SecurityViolationFeedbackTexts {

	/** Not instantiable. */
	private SecurityViolationFeedbackTexts() {
	}

	/**
	 * The {@code MEDIUM} text.
	 *
	 * @param phrase the phrase key's last part, such as {@code file.read}.
	 * @return the text.
	 */
	public static String medium(String phrase) {
		return Messages.localized("reporting.security.violation.medium",
				Messages.localized("reporting.security.violation.phrase." + phrase));
	}

	/**
	 * The {@code LOW} text.
	 *
	 * @return the text.
	 */
	public static String low() {
		return Messages.localized("reporting.security.violation.low");
	}

	/**
	 * The policy as the hint names it.
	 *
	 * @param file the policy file's name, if known.
	 * @return the words naming the policy.
	 */
	public static String policy(Optional<String> file) {
		return file.map(name -> Messages.localized("reporting.security.violation.policy.named", name))
				.orElseGet(() -> Messages.localized("reporting.security.violation.policy.unnamed"));
	}

	/**
	 * The {@code DEBUG} text with an entry to paste.
	 *
	 * @param high       the {@code HIGH} text.
	 * @param permission the permission key's last part, such as {@code read}.
	 * @param target     what the action targeted.
	 * @param file       the policy file's name, if known.
	 * @param entry      the entry, lines joined with the line separator.
	 * @return the text.
	 */
	public static String debug(String high, String permission, String target, Optional<String> file, String entry) {
		return high + System.lineSeparator() + Messages.localized("reporting.security.violation.hint",
				Messages.localized("reporting.security.violation.permission." + permission), target, policy(file))
				+ System.lineSeparator() + entry;
	}

	/**
	 * The {@code DEBUG} text that names only the category.
	 *
	 * @param high     the {@code HIGH} text.
	 * @param category the category's key.
	 * @param file     the policy file's name, if known.
	 * @return the text.
	 */
	public static String debugCategory(String high, String category, Optional<String> file) {
		return high + System.lineSeparator()
				+ Messages.localized("reporting.security.violation.hint.category", category, policy(file));
	}

	/**
	 * Joins entry lines with the line separator, as the report writes them.
	 *
	 * @param lines the lines.
	 * @return the entry.
	 */
	public static String lines(String... lines) {
		return String.join(System.lineSeparator(), lines);
	}
}
