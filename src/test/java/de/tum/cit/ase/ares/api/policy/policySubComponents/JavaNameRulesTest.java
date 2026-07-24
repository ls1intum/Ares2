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
