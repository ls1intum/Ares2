package de.tum.cit.ase.ares.api.policy.policySubComponents;

import javax.annotation.Nullable;

/**
 * Language-specific validation of the names a policy refers to.
 * <p>
 * Description: How a package, type, class or thread name may look depends on
 * the language of the supervised code, so the rules come from
 * {@link ProgrammingLanguageConfiguration#nameRules()}.
 * <p>
 * Design Rationale: A new language adds an implementation and leaves the shared
 * code alone. {@code matches*} returns a boolean for callers with their own
 * error; {@code require*} throws one.
 *
 * @since 2.1.0
 * @author Markus Paulsen
 */
public interface LanguageNameRules {

	/**
	 * Tests whether a value is a valid package (namespace) name for this language.
	 *
	 * @param value the value to test; may be {@code null}.
	 * @return {@code true} when the value is a valid package name.
	 */
	boolean matchesPackage(@Nullable String value);

	/**
	 * Tests whether a value is a valid single type (class) name for this language.
	 *
	 * @param value the value to test; may be {@code null}.
	 * @return {@code true} when the value is a valid type name.
	 */
	boolean matchesTypeName(@Nullable String value);

	/**
	 * Tests whether a value is a valid qualified class name for this language.
	 *
	 * @param value the value to test; may be {@code null}.
	 * @return {@code true} when the value is a valid qualified class name.
	 */
	boolean matchesClassPath(@Nullable String value);

	/**
	 * Tests whether a value is a valid thread-construct identifier for this
	 * language, covering both a class name and the recognised concurrency tokens.
	 *
	 * @param value the value to test; may be {@code null}.
	 * @return {@code true} when the value is a valid thread construct.
	 */
	boolean matchesThreadConstruct(@Nullable String value);

	/**
	 * Tests whether a value is a valid package import for this language, that is a
	 * package name or the wildcard {@code *}.
	 *
	 * @param value the value to test; may be {@code null}.
	 * @return {@code true} when the value is a valid package import.
	 */
	boolean matchesPackageImport(@Nullable String value);

	/**
	 * Requires a value to be a valid package (namespace) name for this language.
	 *
	 * @param field the policy-field name used in the failure message.
	 * @param value the value to validate; may be {@code null}.
	 * @throws IllegalArgumentException if the value is not a valid package name.
	 */
	void requirePackage(String field, @Nullable String value);

	/**
	 * Requires a value to be a valid package import for this language, that is a
	 * package name or the wildcard {@code *}.
	 * <p>
	 * Unlike {@link #requirePackage(String, String)}, it accepts the wildcard,
	 * which is a legal import but not a legal package name.
	 *
	 * @param field the policy-field name used in the failure message.
	 * @param value the value to validate; may be {@code null}.
	 * @throws IllegalArgumentException if the value is neither a valid package name
	 *                                  nor the wildcard.
	 */
	void requirePackageImport(String field, @Nullable String value);

	/**
	 * Requires a value to be a valid single type (class) name for this language.
	 *
	 * @param field the policy-field name used in the failure message.
	 * @param value the value to validate; may be {@code null}.
	 * @throws IllegalArgumentException if the value is not a valid type name.
	 */
	void requireTypeName(String field, @Nullable String value);

	/**
	 * Requires a value to be a valid qualified class name for this language.
	 *
	 * @param field the policy-field name used in the failure message.
	 * @param value the value to validate; may be {@code null}.
	 * @throws IllegalArgumentException if the value is not a valid qualified class
	 *                                  name.
	 */
	void requireClassPath(String field, @Nullable String value);
}
