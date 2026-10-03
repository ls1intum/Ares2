package de.tum.cit.ase.ares.api.policy.policySubComponents;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class JavaNameRulesTest {

	@ParameterizedTest
	@ValueSource(strings = { "com.example", "de.übung.例", "$generated._internal" })
	void acceptsCompletePackageIdentifiers(String value) {
		assertTrue(JavaNameRules.INSTANCE.matchesPackage(value));
	}

	@ParameterizedTest
	@ValueSource(strings = { "", ".example", "com..example", "com.example.", "com.class.example", "1example" })
	void rejectsInvalidPackageIdentifiers(String value) {
		assertFalse(JavaNameRules.INSTANCE.matchesPackage(value));
	}

	@ParameterizedTest
	@ValueSource(strings = { "Main", "Übung", "$Generated", "Outer$Inner" })
	void acceptsCompleteTypeNames(String value) {
		assertTrue(JavaNameRules.INSTANCE.matchesTypeName(value));
	}

	@ParameterizedTest
	@ValueSource(strings = { "", "com.example.Main", "1Main", "class", "record", "sealed", "permits", "yield", "var",
			"Main-Class" })
	void rejectsInvalidTypeNames(String value) {
		assertFalse(JavaNameRules.INSTANCE.matchesTypeName(value));
	}

	@ParameterizedTest
	@ValueSource(strings = { "MainTest", "com.example.MainTest", "de.übung.例Test", "com.example.Outer$Inner" })
	void acceptsClassPaths(String value) {
		assertTrue(JavaNameRules.INSTANCE.matchesClassPath(value));
	}

	@ParameterizedTest
	@ValueSource(strings = { "com.example.", "com..example.Test", "com.example.class", "com/example/Test",
			"not a class name", "com.class.Trusted" })
	void rejectsInvalidClassPaths(String value) {
		assertFalse(JavaNameRules.INSTANCE.matchesClassPath(value));
	}

	@Test
	void rejectsControlCharactersInClassPaths() {
		assertFalse(JavaNameRules.INSTANCE.matchesClassPath("com.example.Trusted" + (char) 10 + "Evil"));
	}

	/**
	 * The C0 and C1 controls and the format characters, every one of which
	 * {@link Character#isJavaIdentifierPart} accepts.
	 */
	@ParameterizedTest
	@ValueSource(ints = { 0x0000, 0x0008, 0x000E, 0x001B, 0x007F, 0x009F, 0x00AD, 0x200B, 0x200D, 0xFEFF })
	void rejectsIdentifierIgnorableCharactersInEveryKindOfName(int codePoint) {
		// Without this the test would prove nothing: the point is that Java itself
		// treats these as identifier parts, which is why the policy must not.
		assertTrue(Character.isJavaIdentifierPart(codePoint), () -> String.format("U+%04X", codePoint));
		String ignorable = new String(Character.toChars(codePoint));
		assertFalse(JavaNameRules.INSTANCE.matchesPackage("com.exam" + ignorable + "ple"));
		assertFalse(JavaNameRules.INSTANCE.matchesPackageImport("com.exam" + ignorable + "ple"));
		assertFalse(JavaNameRules.INSTANCE.matchesTypeName("Ma" + ignorable + "in"));
		assertFalse(JavaNameRules.INSTANCE.matchesClassPath("com.example.Ma" + ignorable + "in"));
		assertFalse(JavaNameRules.INSTANCE.matchesThreadConstruct("com.example.Ma" + ignorable + "in"));
	}

	@ParameterizedTest
	@ValueSource(strings = { "*", "com.example", "com.example.sub" })
	void acceptsPackageImports(String value) {
		assertTrue(JavaNameRules.INSTANCE.matchesPackageImport(value));
	}

	@ParameterizedTest
	@ValueSource(strings = { "", "java.*", "com..example" })
	void rejectsInvalidPackageImports(String value) {
		assertFalse(JavaNameRules.INSTANCE.matchesPackageImport(value));
	}

	@ParameterizedTest
	@ValueSource(strings = { "*", "Lambda-Expression", "<implicit-thread-op:parallelStream>",
			"<implicit-thread-op:parallel>", "<implicit-thread-op:Thread.sleep>",
			"<implicit-thread-op:SubmissionPublisher.submit>", "<implicit-thread-op:SubmissionPublisher.offer>",
			"java.lang.Thread" })
	void acceptsThreadConstructs(String value) {
		assertTrue(JavaNameRules.INSTANCE.matchesThreadConstruct(value));
	}

	@ParameterizedTest
	@ValueSource(strings = { "", "<implicit-thread-op:unknown>", "Main-Class" })
	void rejectsInvalidThreadConstructs(String value) {
		assertFalse(JavaNameRules.INSTANCE.matchesThreadConstruct(value));
	}
}
