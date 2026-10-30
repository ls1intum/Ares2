package de.tum.cit.ase.ares.api.policy.policySubComponents;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * Checks which Java package, type, class, package import and thread construct
 * names {@link JavaNameRules} accepts and which it rejects.
 */
class JavaNameRulesTest {

	/**
	 * Checks that complete package names are accepted, including non-ASCII letters,
	 * dollar signs and underscores.
	 */
	@ParameterizedTest
	@ValueSource(strings = { "com.example", "de.übung.例", "$generated._internal" })
	void acceptsCompletePackageIdentifiers(String value) {
		assertTrue(JavaNameRules.INSTANCE.matchesPackage(value));
	}

	/**
	 * Checks that empty, malformed and keyword-containing package names are
	 * rejected.
	 */
	@ParameterizedTest
	@ValueSource(strings = { "", ".example", "com..example", "com.example.", "com.class.example", "1example" })
	void rejectsInvalidPackageIdentifiers(String value) {
		assertFalse(JavaNameRules.INSTANCE.matchesPackage(value));
	}

	/**
	 * Checks that simple type names, including non-ASCII and nested ones written
	 * with a dollar sign, are accepted.
	 */
	@ParameterizedTest
	@ValueSource(strings = { "Main", "Übung", "$Generated", "Outer$Inner" })
	void acceptsCompleteTypeNames(String value) {
		assertTrue(JavaNameRules.INSTANCE.matchesTypeName(value));
	}

	/**
	 * Checks that empty, qualified, digit-led, keyword and hyphenated type names
	 * are rejected, including the restricted names such as record and var.
	 */
	@ParameterizedTest
	@ValueSource(strings = { "", "com.example.Main", "1Main", "class", "record", "sealed", "permits", "yield", "var",
			"Main-Class" })
	void rejectsInvalidTypeNames(String value) {
		assertFalse(JavaNameRules.INSTANCE.matchesTypeName(value));
	}

	/**
	 * Checks that simple and fully qualified class names, including non-ASCII and
	 * nested ones, are accepted.
	 */
	@ParameterizedTest
	@ValueSource(strings = { "MainTest", "com.example.MainTest", "de.übung.例Test", "com.example.Outer$Inner" })
	void acceptsClassPaths(String value) {
		assertTrue(JavaNameRules.INSTANCE.matchesClassPath(value));
	}

	/**
	 * Checks that malformed class names, slash-separated paths and names containing
	 * a keyword are rejected.
	 */
	@ParameterizedTest
	@ValueSource(strings = { "com.example.", "com..example.Test", "com.example.class", "com/example/Test",
			"not a class name", "com.class.Trusted" })
	void rejectsInvalidClassPaths(String value) {
		assertFalse(JavaNameRules.INSTANCE.matchesClassPath(value));
	}

	/**
	 * Checks that a class name with a line feed inside it is rejected.
	 */
	@Test
	void rejectsControlCharactersInClassPaths() {
		assertFalse(JavaNameRules.INSTANCE.matchesClassPath("com.example.Trusted" + (char) 10 + "Evil"));
	}

	/**
	 * Checks that every kind of name rejects the C0 and C1 controls and the format
	 * characters. It first confirms that {@link Character#isJavaIdentifierPart}
	 * accepts each one; without that the test would prove nothing, since Java
	 * treating them as identifier parts is why the policy must not.
	 */
	@ParameterizedTest
	@ValueSource(ints = { 0x0000, 0x0008, 0x000E, 0x001B, 0x007F, 0x009F, 0x00AD, 0x200B, 0x200D, 0xFEFF })
	void rejectsIdentifierIgnorableCharactersInEveryKindOfName(int codePoint) {
		assertTrue(Character.isJavaIdentifierPart(codePoint), () -> String.format("U+%04X", codePoint));
		String ignorable = new String(Character.toChars(codePoint));
		assertFalse(JavaNameRules.INSTANCE.matchesPackage("com.exam" + ignorable + "ple"));
		assertFalse(JavaNameRules.INSTANCE.matchesPackageImport("com.exam" + ignorable + "ple"));
		assertFalse(JavaNameRules.INSTANCE.matchesTypeName("Ma" + ignorable + "in"));
		assertFalse(JavaNameRules.INSTANCE.matchesClassPath("com.example.Ma" + ignorable + "in"));
		assertFalse(JavaNameRules.INSTANCE.matchesThreadConstruct("com.example.Ma" + ignorable + "in"));
	}

	/**
	 * Checks that the wildcard and plain package names are accepted as package
	 * imports.
	 */
	@ParameterizedTest
	@ValueSource(strings = { "*", "com.example", "com.example.sub" })
	void acceptsPackageImports(String value) {
		assertTrue(JavaNameRules.INSTANCE.matchesPackageImport(value));
	}

	/**
	 * Checks that an empty value, a trailing wildcard and a doubled dot are
	 * rejected as package imports.
	 */
	@ParameterizedTest
	@ValueSource(strings = { "", "java.*", "com..example" })
	void rejectsInvalidPackageImports(String value) {
		assertFalse(JavaNameRules.INSTANCE.matchesPackageImport(value));
	}

	/**
	 * Checks that the wildcard, the lambda token, every supported implicit thread
	 * operation token and a class name are accepted as thread constructs.
	 */
	@ParameterizedTest
	@ValueSource(strings = { "*", "Lambda-Expression", "<implicit-thread-op:parallelStream>",
			"<implicit-thread-op:parallel>", "<implicit-thread-op:Thread.sleep>",
			"<implicit-thread-op:SubmissionPublisher.submit>", "<implicit-thread-op:SubmissionPublisher.offer>",
			"java.lang.Thread" })
	void acceptsThreadConstructs(String value) {
		assertTrue(JavaNameRules.INSTANCE.matchesThreadConstruct(value));
	}

	/**
	 * Checks that an empty value, an unknown implicit thread operation token and a
	 * hyphenated name are rejected as thread constructs.
	 */
	@ParameterizedTest
	@ValueSource(strings = { "", "<implicit-thread-op:unknown>", "Main-Class" })
	void rejectsInvalidThreadConstructs(String value) {
		assertFalse(JavaNameRules.INSTANCE.matchesThreadConstruct(value));
	}
}
