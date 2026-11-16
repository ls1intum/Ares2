package de.tum.cit.ase.ares.api.policy;

import java.util.Map;
import java.util.regex.Pattern;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import de.tum.cit.ase.ares.api.localization.Messages;

/**
 * Regular expressions and helpers for validating language-independent
 * security-policy values.
 * <p>
 * Description: Holds the patterns that describe values which do not depend on
 * the supervised code's programming language, namely file paths, hosts and
 * commands, together with the generic matching helpers. Names that do depend on
 * the language, such as packages, classes and thread constructs, are validated
 * through {@code LanguageNameRules} instead, so nothing Java-specific lives
 * here.
 */
public final class PolicyValueValidator {

	/** One decimal part of an IPv4 address, 0 to 255 without leading zeros. */
	private static final String IPV4_COMPONENT = "(?:25[0-5]|2[0-4]\\d|1\\d{2}|[1-9]?\\d)";

	/** A dotted IPv4 address made of four such parts. */
	private static final String IPV4_ADDRESS = "(?:" + IPV4_COMPONENT + "\\.){3}" + IPV4_COMPONENT;

	/** One group of an IPv6 address, one to four hexadecimal digits. */
	private static final String IPV6_COMPONENT = "[0-9A-Fa-f]{1,4}";

	/**
	 * An IPv6 address in full or {@code ::}-compressed form, including the forms
	 * that end in an embedded IPv4 address.
	 */
	private static final String IPV6_ADDRESS = "(?:" + "(?:" + IPV6_COMPONENT + ":){7}" + IPV6_COMPONENT + "|(?:"
			+ IPV6_COMPONENT + ":){1,7}:" + "|(?:" + IPV6_COMPONENT + ":){1,6}:" + IPV6_COMPONENT + "|(?:"
			+ IPV6_COMPONENT + ":){1,5}(?::" + IPV6_COMPONENT + "){1,2}" + "|(?:" + IPV6_COMPONENT + ":){1,4}(?::"
			+ IPV6_COMPONENT + "){1,3}" + "|(?:" + IPV6_COMPONENT + ":){1,3}(?::" + IPV6_COMPONENT + "){1,4}" + "|(?:"
			+ IPV6_COMPONENT + ":){1,2}(?::" + IPV6_COMPONENT + "){1,5}" + "|" + IPV6_COMPONENT + ":(?:(?::"
			+ IPV6_COMPONENT + "){1,6})" + "|:(?:(?::" + IPV6_COMPONENT + "){1,7}|:)" + "|(?:" + IPV6_COMPONENT
			+ ":){6}" + IPV4_ADDRESS + "|::(?:ffff(?::0{1,4})?:)?" + IPV4_ADDRESS + "|(?:" + IPV6_COMPONENT + ":){1,4}:"
			+ IPV4_ADDRESS + ")";

	/**
	 * One label of a DNS name: letters, digits and inner hyphens, at most 63
	 * characters.
	 */
	private static final String DNS_LABEL = "[A-Za-z0-9](?:[A-Za-z0-9-]{0,61}[A-Za-z0-9])?";

	/**
	 * A DNS name of dot-separated labels, at most 253 characters before an optional
	 * trailing dot. Four dot-separated groups of digits are excluded, so an
	 * out-of-range IPv4 address is not accepted as a name.
	 */
	private static final String DNS_NAME = "(?!\\d+\\.\\d+\\.\\d+\\.\\d+\\.?$)(?=.{1,253}\\.?$)" + DNS_LABEL + "(?:\\."
			+ DNS_LABEL + ")*\\.?";

	/**
	 * The placeholders a file path may contain: {@code ${PROJECT_ROOT}},
	 * {@code ${java.home}}, {@code ${user.home}} and {@code ${java.io.tmpdir}}.
	 */
	private static final String RECOGNISED_PLACEHOLDER = "\\$\\{(?:PROJECT_ROOT|java\\.home|user\\.home|java\\.io\\.tmpdir)\\}";

	/**
	 * The characters that must never appear inside a value which ends up on a
	 * single line of a generated artefact.
	 * <p>
	 * {@code \p{Cc}} is used rather than {@code \p{Cntrl}} on purpose: Java's
	 * {@code \p{Cntrl}} matches only the ASCII controls, so U+0085 (NEL) would slip
	 * through and still break a line downstream. {@code \p{Zl}} and {@code \p{Zp}}
	 * add the Unicode line and paragraph separators U+2028 and U+2029.
	 */
	private static final String CONTROL_OR_LINE_SEPARATOR = "\\p{Cc}\\p{Zl}\\p{Zp}";

	/**
	 * Matches a supported file path, recognised placeholder expression, or
	 * wildcard.
	 * <p>
	 * Control characters and line separators (see
	 * {@link #CONTROL_OR_LINE_SEPARATOR}) are rejected, because a path travels into
	 * generated sources, settings and failure reports that assume a single line.
	 * Excluding them also keeps the {@code ..} check and the anchoring unambiguous,
	 * which is why the pattern needs no {@link Pattern#DOTALL}.
	 */
	public static final Pattern FILE_PATH_PATTERN = Pattern
			.compile("^(?:\\*|(?=.+$)(?=.*\\S)(?!(?:.*[\\\\/])?\\.\\.(?:[\\\\/]|$))(?:(?:" + RECOGNISED_PLACEHOLDER
					+ ")|(?!\\$\\{)[^*" + CONTROL_OR_LINE_SEPARATOR + "])+)$");

	/** Matches a DNS name, IP address, localhost, or host wildcard. */
	public static final Pattern HOST_PATTERN = Pattern
			.compile("^(?:\\*|localhost|" + IPV4_ADDRESS + "|" + IPV6_ADDRESS + "|" + DNS_NAME + ")$");

	/**
	 * Matches an executable command, or the command wildcard.
	 * <p>
	 * A command is an executable name or a path to one, so its shape cannot be
	 * pinned down further. Rejected are control characters and line separators
	 * anywhere, because the value travels into artefacts that assume a single line,
	 * and a first or last character that is a control or any Unicode separator,
	 * because {@code "echo "} would otherwise be accepted and then silently match
	 * nothing.
	 */
	public static final Pattern COMMAND_PATTERN = Pattern
			.compile("^(?:\\*|[^\\p{Cc}\\p{Z}](?:[^" + CONTROL_OR_LINE_SEPARATOR + "]*[^\\p{Cc}\\p{Z}])?)$");

	/**
	 * Matches one argument of a command permission, or the argument wildcard.
	 * <p>
	 * Same reasoning as {@link #COMMAND_PATTERN}, except that the empty string is
	 * accepted: passing an empty argument is meaningful, whereas an empty command
	 * is not. Surrounding whitespace is not rejected here, because an argument,
	 * unlike a command, may legitimately be whitespace.
	 */
	public static final Pattern COMMAND_ARGUMENT_PATTERN = Pattern.compile("^[^" + CONTROL_OR_LINE_SEPARATOR + "]*$");

	/** Prevents instances of this utility. */
	private PolicyValueValidator() {
		throw new SecurityException(
				Messages.localized("security.general.utility.initialization", "PolicyValueValidator"));
	}

	/**
	 * Tests whether a nullable value fully matches a validation pattern.
	 *
	 * @param value   the value to test; may be {@code null}
	 * @param pattern the validation pattern
	 * @return {@code true} when the value is non-null and fully matches the pattern
	 */
	public static boolean matches(@Nullable String value, @Nonnull Pattern pattern) {
		return value != null && pattern.matcher(value).matches();
	}

	/**
	 * Maps each pattern to the message describing, in words, what it accepts.
	 * <p>
	 * A failure message used to quote the pattern source instead. For the host and
	 * path patterns that meant several hundred characters of alternation, which
	 * tells a policy author what the expression is but not what to write. Keyed by
	 * identity because the values are the constants declared above, and every
	 * pattern that reaches {@link #requireMatch} is one of them.
	 */
	private static final Map<Pattern, String> EXPECTATION_KEYS = Map.of(FILE_PATH_PATTERN, "policy.value.file.path",
			HOST_PATTERN, "policy.value.host", COMMAND_PATTERN, "policy.value.command", COMMAND_ARGUMENT_PATTERN,
			"policy.value.command.argument");

	/**
	 * Requires a nullable value to fully match a validation pattern.
	 *
	 * @param field   the policy-field name used in the failure message
	 * @param value   the value to validate; may be {@code null}
	 * @param pattern the required validation pattern
	 * @throws IllegalArgumentException if the value does not match the pattern. The
	 *                                  message states what the field accepts, and
	 *                                  quotes the offending value.
	 */
	public static void requireMatch(@Nonnull String field, @Nullable String value, @Nonnull Pattern pattern) {
		if (!matches(value, pattern)) {
			String expectationKey = EXPECTATION_KEYS.get(pattern);
			throw new IllegalArgumentException(expectationKey == null
					? Messages.localized("policy.value.pattern", field, String.valueOf(value), pattern.pattern())
					: Messages.localized(expectationKey, field, String.valueOf(value)));
		}
	}
}
