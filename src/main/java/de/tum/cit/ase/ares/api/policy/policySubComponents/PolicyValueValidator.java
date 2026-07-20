package de.tum.cit.ase.ares.api.policy.policySubComponents;

import java.util.regex.Pattern;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import de.tum.cit.ase.ares.api.localization.Messages;

/** Regular expressions and helpers for validating security-policy values. */
public final class PolicyValueValidator {

	private static final String JAVA_RESERVED_WORD = "abstract|assert|boolean|break|byte|case|catch|char|class|const|continue|default|do|double|else|enum|extends|false|final|finally|float|for|goto|if|implements|import|instanceof|int|interface|long|native|new|null|package|private|protected|public|return|short|static|strictfp|super|switch|synchronized|this|throw|throws|transient|true|try|void|volatile|while|_";
	private static final String JAVA_IDENTIFIER = "(?!(?:" + JAVA_RESERVED_WORD
			+ ")(?=\\.|$))\\p{javaJavaIdentifierStart}\\p{javaJavaIdentifierPart}*";
	private static final String JAVA_TYPE_IDENTIFIER = "(?!(?:var|yield|record|sealed|permits)$)" + JAVA_IDENTIFIER;
	private static final String JAVA_QUALIFIED_IDENTIFIER = JAVA_IDENTIFIER + "(?:\\." + JAVA_IDENTIFIER + ")*";
	private static final String JAVA_CLASS_PATH = "(?:" + JAVA_IDENTIFIER + "\\.)*" + JAVA_TYPE_IDENTIFIER;
	private static final String IPV4_COMPONENT = "(?:25[0-5]|2[0-4]\\d|1\\d{2}|[1-9]?\\d)";
	private static final String IPV4_ADDRESS = "(?:" + IPV4_COMPONENT + "\\.){3}" + IPV4_COMPONENT;
	private static final String IPV6_COMPONENT = "[0-9A-Fa-f]{1,4}";
	private static final String IPV6_ADDRESS = "(?:" + "(?:" + IPV6_COMPONENT + ":){7}" + IPV6_COMPONENT + "|(?:"
			+ IPV6_COMPONENT + ":){1,7}:" + "|(?:" + IPV6_COMPONENT + ":){1,6}:" + IPV6_COMPONENT + "|(?:"
			+ IPV6_COMPONENT + ":){1,5}(?::" + IPV6_COMPONENT + "){1,2}" + "|(?:" + IPV6_COMPONENT + ":){1,4}(?::"
			+ IPV6_COMPONENT + "){1,3}" + "|(?:" + IPV6_COMPONENT + ":){1,3}(?::" + IPV6_COMPONENT + "){1,4}" + "|(?:"
			+ IPV6_COMPONENT + ":){1,2}(?::" + IPV6_COMPONENT + "){1,5}" + "|" + IPV6_COMPONENT + ":(?:(?::"
			+ IPV6_COMPONENT + "){1,6})" + "|:(?:(?::" + IPV6_COMPONENT + "){1,7}|:)" + "|(?:" + IPV6_COMPONENT
			+ ":){6}" + IPV4_ADDRESS + "|::(?:ffff(?::0{1,4})?:)?" + IPV4_ADDRESS + "|(?:" + IPV6_COMPONENT + ":){1,4}:"
			+ IPV4_ADDRESS + ")";
	private static final String DNS_LABEL = "[A-Za-z0-9](?:[A-Za-z0-9-]{0,61}[A-Za-z0-9])?";
	private static final String DNS_NAME = "(?!\\d+\\.\\d+\\.\\d+\\.\\d+\\.?$)(?=.{1,253}\\.?$)" + DNS_LABEL + "(?:\\."
			+ DNS_LABEL + ")*\\.?";
	private static final String RECOGNISED_PLACEHOLDER = "\\$\\{(?:PROJECT_ROOT|java\\.home|user\\.home|java\\.io\\.tmpdir)\\}";

	/** Matches the complete supported programming-language configuration format. */
	public static final Pattern PROGRAMMING_LANGUAGE_CONFIGURATION_PATTERN = Pattern
			.compile("^JAVA_USING_(?:MAVEN|GRADLE)_(?:ARCHUNIT|WALA)_AND_(?:ASPECTJ|INSTRUMENTATION)$");

	/** Matches a dot-separated Java package name. */
	public static final Pattern JAVA_PACKAGE_PATTERN = Pattern.compile("^" + JAVA_QUALIFIED_IDENTIFIER + "$");

	/** Matches a single Java type name. */
	public static final Pattern JAVA_CLASS_NAME_PATTERN = Pattern.compile("^" + JAVA_TYPE_IDENTIFIER + "$");

	/** Matches a fully qualified Java class name. */
	public static final Pattern JAVA_CLASS_PATH_PATTERN = Pattern.compile("^" + JAVA_CLASS_PATH + "$");

	/**
	 * Matches a supported file path, recognised placeholder expression, or
	 * wildcard.
	 */
	public static final Pattern FILE_PATH_PATTERN = Pattern.compile(
			"^(?:\\*|(?=.+$)(?=.*\\S)(?:(?:" + RECOGNISED_PLACEHOLDER + ")|(?!\\$\\{)[^*\\x00])+)$", Pattern.DOTALL);

	/** Matches a DNS name, IP address, localhost, or host wildcard. */
	public static final Pattern HOST_PATTERN = Pattern
			.compile("^(?:\\*|localhost|" + IPV4_ADDRESS + "|" + IPV6_ADDRESS + "|" + DNS_NAME + ")$");

	/** Matches a Java class path or one of the supported special thread tokens. */
	public static final Pattern THREAD_CLASS_PATTERN = Pattern.compile("^(?:" + JAVA_CLASS_PATH
			+ "|\\*|Lambda-Expression|<implicit-thread-op:(?:parallelStream|parallel|Thread\\.sleep|SubmissionPublisher\\.(?:submit|offer))>)$");

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
	 * Tests whether a value is a Java package name or the package wildcard.
	 *
	 * @param value the package import value; may be {@code null}
	 * @return {@code true} when the value is a valid package import
	 */
	public static boolean matchesPackageImport(@Nullable String value) {
		return "*".equals(value) || matches(value, JAVA_PACKAGE_PATTERN);
	}

	/**
	 * Requires a nullable value to fully match a validation pattern.
	 *
	 * @param field   the policy-field name used in the failure message
	 * @param value   the value to validate; may be {@code null}
	 * @param pattern the required validation pattern
	 * @throws IllegalArgumentException if the value does not match the pattern
	 */
	public static void requireMatch(@Nonnull String field, @Nullable String value, @Nonnull Pattern pattern) {
		if (!matches(value, pattern)) {
			throw new IllegalArgumentException(field + " does not match " + pattern.pattern());
		}
	}

	/**
	 * Requires a value to be a Java package name or the package wildcard.
	 *
	 * @param value the package import value; may be {@code null}
	 * @throws IllegalArgumentException if the value is not a valid package import
	 */
	public static void requirePackageImport(@Nullable String value) {
		if (!matchesPackageImport(value)) {
			throw new IllegalArgumentException(
					"importTheFollowingPackage does not match " + JAVA_PACKAGE_PATTERN.pattern() + " or *");
		}
	}
}
