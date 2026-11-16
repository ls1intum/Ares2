package de.tum.cit.ase.ares.api.policy.policySubComponents;

import java.util.Arrays;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import de.tum.cit.ase.ares.api.localization.Messages;

/**
 * Keeps the public members this validator had up to 2.1.2, so older code still
 * compiles. Its methods only forward. The name and path patterns are read back
 * from where they now live, never copied, so they cannot drift from what Ares
 * enforces.
 *
 * @since 2.1.0
 * @author Markus Paulsen
 * @deprecated Use {@link de.tum.cit.ase.ares.api.policy.PolicyValueValidator}
 *             for paths, hosts and matching, and
 *             {@link ProgrammingLanguageConfiguration#nameRules()} for names.
 */
@Deprecated(forRemoval = true)
public final class PolicyValueValidator {

	/**
	 * Matches the complete supported programming-language configuration format.
	 * <p>
	 * Derived from {@link ProgrammingLanguageConfiguration} rather than restated as
	 * a literal, so a configuration added to the enum cannot leave this constant
	 * behind. The constant names are plain uppercase identifiers and so need no
	 * quoting, which keeps the expression readable when a failure message quotes
	 * it.
	 */
	@Nonnull
	public static final Pattern PROGRAMMING_LANGUAGE_CONFIGURATION_PATTERN = Pattern
			.compile(Arrays.stream(ProgrammingLanguageConfiguration.values()).map(Enum::name)
					.collect(Collectors.joining("|", "^(?:", ")$")));

	/**
	 * Matches a dot-separated Java package name.
	 * <p>
	 * This and the three name patterns below no longer accept the
	 * identifier-ignorable characters the released expressions did, for the reason
	 * given on {@code JavaNameRules.JAVA_IDENTIFIER_PART}. As with
	 * {@link #FILE_PATH_PATTERN}, republishing the laxer released expression would
	 * let a caller pre-validate a name that Ares then refuses.
	 */
	@Nonnull
	public static final Pattern JAVA_PACKAGE_PATTERN = JavaNameRules.packagePattern();

	/** Matches a single Java type name. */
	@Nonnull
	public static final Pattern JAVA_CLASS_NAME_PATTERN = JavaNameRules.typeNamePattern();

	/** Matches a fully qualified Java class name. */
	@Nonnull
	public static final Pattern JAVA_CLASS_PATH_PATTERN = JavaNameRules.classPathPattern();

	/**
	 * Matches a supported file path, recognised placeholder expression, or
	 * wildcard.
	 * <p>
	 * This is the relocated validator's constant, which since 2.1.2 also rejects
	 * control characters and Unicode line separators. The facade deliberately does
	 * not preserve the laxer released expression: a path carrying a line break is
	 * accepted by the old pattern but refused by Ares, so republishing the old one
	 * would let a caller pre-validate a value that enforcement then rejects.
	 */
	@Nonnull
	public static final Pattern FILE_PATH_PATTERN = de.tum.cit.ase.ares.api.policy.PolicyValueValidator.FILE_PATH_PATTERN;

	/** Matches a DNS name, IP address, localhost, or host wildcard. */
	@Nonnull
	public static final Pattern HOST_PATTERN = de.tum.cit.ase.ares.api.policy.PolicyValueValidator.HOST_PATTERN;

	/** Matches a Java class path or one of the supported special thread tokens. */
	@Nonnull
	public static final Pattern THREAD_CLASS_PATTERN = JavaNameRules.threadConstructPattern();

	private PolicyValueValidator() {
		throw new SecurityException(
				Messages.localized("security.general.utility.initialization", "PolicyValueValidator"));
	}

	/**
	 * Tests whether a nullable value fully matches a validation pattern.
	 *
	 * @since 2.1.0
	 * @author Markus Paulsen
	 * @param value   the value to test; may be {@code null}
	 * @param pattern the validation pattern
	 * @return {@code true} when the value is non-null and fully matches the pattern
	 * @deprecated Use
	 *             {@link de.tum.cit.ase.ares.api.policy.PolicyValueValidator#matches}.
	 */
	@Deprecated(forRemoval = true)
	public static boolean matches(@Nullable String value, @Nonnull Pattern pattern) {
		return de.tum.cit.ase.ares.api.policy.PolicyValueValidator.matches(value, pattern);
	}

	/**
	 * Tests whether a value is a Java package name or the package wildcard.
	 *
	 * @since 2.1.0
	 * @author Markus Paulsen
	 * @param value the package import value; may be {@code null}
	 * @return {@code true} when the value is a valid package import
	 * @deprecated Use {@link LanguageNameRules#matchesPackageImport} on the rules
	 *             of the configured language, obtained from
	 *             {@link ProgrammingLanguageConfiguration#nameRules()}. This method
	 *             answers for Java whatever language the policy selects.
	 */
	@Deprecated(forRemoval = true)
	public static boolean matchesPackageImport(@Nullable String value) {
		return JavaNameRules.INSTANCE.matchesPackageImport(value);
	}

	/**
	 * Requires a nullable value to fully match a validation pattern.
	 *
	 * @since 2.1.0
	 * @author Markus Paulsen
	 * @param field   the policy-field name used in the failure message
	 * @param value   the value to validate; may be {@code null}
	 * @param pattern the required validation pattern
	 * @throws IllegalArgumentException if the value does not match the pattern, now
	 *                                  with the relocated validator's localised
	 *                                  message.
	 * @deprecated Use
	 *             {@link de.tum.cit.ase.ares.api.policy.PolicyValueValidator#requireMatch}.
	 */
	@Deprecated(forRemoval = true)
	public static void requireMatch(@Nonnull String field, @Nullable String value, @Nonnull Pattern pattern) {
		de.tum.cit.ase.ares.api.policy.PolicyValueValidator.requireMatch(field, value, pattern);
	}

	/**
	 * Requires a value to be a Java package name or the package wildcard.
	 *
	 * @since 2.1.0
	 * @author Markus Paulsen
	 * @param value the package import value; may be {@code null}
	 * @throws IllegalArgumentException if the value is not a valid package import,
	 *                                  with a localised message.
	 * @deprecated Use {@link LanguageNameRules#requirePackageImport} on
	 *             {@link ProgrammingLanguageConfiguration#nameRules()}. This method
	 *             always applies the Java rules.
	 */
	@Deprecated(forRemoval = true)
	public static void requirePackageImport(@Nullable String value) {
		JavaNameRules.INSTANCE.requirePackageImport("importTheFollowingPackage", value);
	}
}
