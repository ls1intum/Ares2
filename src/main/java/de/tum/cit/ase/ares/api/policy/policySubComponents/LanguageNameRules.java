package de.tum.cit.ase.ares.api.policy.policySubComponents;

import javax.annotation.Nullable;

/**
 * Language-specific validation of the names a policy refers to.
 * <p>
 * Description: A policy names packages, types, classes and thread constructs of
 * the supervised code. What shape those names may take depends on the
 * programming language the supervised code is written in, so this contract is
 * resolved per language from {@link ProgrammingLanguageConfiguration}. The
 * neutral policy core (the schema validator, the permission records) validates
 * such names only through this interface and never hard-codes one language's
 * rules.
 * <p>
 * Design Rationale: Keeping the language-specific name rules behind a small
 * interface lets a second language be added by supplying another implementation
 * and one more case in {@link ProgrammingLanguageConfiguration}, without
 * touching the neutral core. The {@code matches*} methods report a boolean so a
 * caller can raise its own error in its own style (a schema validator points at
 * a JSON path, a record throws), while the {@code require*} methods throw a
 * ready-made {@link IllegalArgumentException} where that is more convenient.
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
	 * The throwing counterpart of {@link #matchesPackageImport(String)}, and
	 * distinct from {@link #requirePackage(String, String)} because the wildcard is
	 * a legal package <em>import</em> and not a legal package <em>name</em>.
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
