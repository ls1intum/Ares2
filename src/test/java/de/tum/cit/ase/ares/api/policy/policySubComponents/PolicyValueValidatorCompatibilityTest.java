package de.tum.cit.ase.ares.api.policy.policySubComponents;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.regex.Pattern;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * Pins the public surface that {@code PolicyValueValidator} released in this
 * package up to 2.1.2.
 * <p>
 * Every member the facade republishes is exercised, so removing or renaming one
 * fails here instead of breaking downstream code this repository never
 * compiles. Identity checks prove the facade forwards to the component that now
 * owns each rule, not to a copy.
 * <p>
 * "removal" is suppressed beside "deprecation" because javac reports uses of a
 * class deprecated for removal under that separate warning.
 */
@SuppressWarnings({ "deprecation", "removal" })
class PolicyValueValidatorCompatibilityTest {

	/**
	 * The seven constants the released class published, by their released names.
	 */
	private static final List<String> RELEASED_PATTERN_FIELDS = List.of("PROGRAMMING_LANGUAGE_CONFIGURATION_PATTERN",
			"JAVA_PACKAGE_PATTERN", "JAVA_CLASS_NAME_PATTERN", "JAVA_CLASS_PATH_PATTERN", "FILE_PATH_PATTERN",
			"HOST_PATTERN", "THREAD_CLASS_PATTERN");

	/**
	 * The configuration language the released pattern recognised, written out
	 * rather than read from {@link ProgrammingLanguageConfiguration}.
	 * <p>
	 * Deriving the expectation from the enum that also builds the pattern would
	 * only prove the constant agrees with itself. These eight strings are what
	 * 2.1.2 accepted, so a rename or removal in the enum narrows the released
	 * language and fails here instead of silently rejecting a downstream caller's
	 * value.
	 */
	private static final List<String> RELEASED_CONFIGURATIONS = List.of("JAVA_USING_MAVEN_ARCHUNIT_AND_ASPECTJ",
			"JAVA_USING_MAVEN_ARCHUNIT_AND_INSTRUMENTATION", "JAVA_USING_MAVEN_WALA_AND_ASPECTJ",
			"JAVA_USING_MAVEN_WALA_AND_INSTRUMENTATION", "JAVA_USING_GRADLE_ARCHUNIT_AND_ASPECTJ",
			"JAVA_USING_GRADLE_ARCHUNIT_AND_INSTRUMENTATION", "JAVA_USING_GRADLE_WALA_AND_ASPECTJ",
			"JAVA_USING_GRADLE_WALA_AND_INSTRUMENTATION");

	/**
	 * Checks that the class is still public and final, its seven pattern constants
	 * are public static final patterns and its four methods are public and static
	 * with the released return types. getField and getMethod find public members
	 * only, so a member demoted to package-private fails here instead of compiling
	 * quietly in this package.
	 */
	@Test
	void publishesTheReleasedMembersWithTheReleasedAccessibility() throws NoSuchFieldException, NoSuchMethodException {
		int classModifiers = PolicyValueValidator.class.getModifiers();
		assertTrue(Modifier.isPublic(classModifiers), "the released type was public");
		assertTrue(Modifier.isFinal(classModifiers), "the released type was final");
		for (String name : RELEASED_PATTERN_FIELDS) {
			Field field = PolicyValueValidator.class.getField(name);
			assertTrue(Modifier.isStatic(field.getModifiers()), name);
			assertTrue(Modifier.isFinal(field.getModifiers()), name);
			assertSame(Pattern.class, field.getType(), name);
		}
		List<Method> released = List.of(PolicyValueValidator.class.getMethod("matches", String.class, Pattern.class),
				PolicyValueValidator.class.getMethod("matchesPackageImport", String.class),
				PolicyValueValidator.class.getMethod("requireMatch", String.class, String.class, Pattern.class),
				PolicyValueValidator.class.getMethod("requirePackageImport", String.class));
		for (Method method : released) {
			assertTrue(Modifier.isStatic(method.getModifiers()), method.getName());
		}
		assertSame(boolean.class, released.get(0).getReturnType());
		assertSame(boolean.class, released.get(1).getReturnType());
		assertSame(void.class, released.get(2).getReturnType());
		assertSame(void.class, released.get(3).getReturnType());
	}

	/**
	 * Checks that every configuration name 2.1.2 accepted is still accepted.
	 */
	@Test
	void acceptsEveryConfigurationTheReleaseRecognised() {
		for (String configuration : RELEASED_CONFIGURATIONS) {
			assertTrue(PolicyValueValidator.matches(configuration,
					PolicyValueValidator.PROGRAMMING_LANGUAGE_CONFIGURATION_PATTERN), configuration);
		}
	}

	/**
	 * Checks that the Java name constants are the very same objects JavaNameRules
	 * enforces, not copies.
	 */
	@Test
	void javaPatternsAreTheVeryPatternsJavaNameRulesEnforces() {
		assertSame(JavaNameRules.packagePattern(), PolicyValueValidator.JAVA_PACKAGE_PATTERN);
		assertSame(JavaNameRules.typeNamePattern(), PolicyValueValidator.JAVA_CLASS_NAME_PATTERN);
		assertSame(JavaNameRules.classPathPattern(), PolicyValueValidator.JAVA_CLASS_PATH_PATTERN);
		assertSame(JavaNameRules.threadConstructPattern(), PolicyValueValidator.THREAD_CLASS_PATTERN);
	}

	/**
	 * Checks that the file path and host constants are the very same objects the
	 * relocated validator enforces, not copies.
	 */
	@Test
	void neutralPatternsAreTheVeryPatternsTheRelocatedValidatorEnforces() {
		assertSame(de.tum.cit.ase.ares.api.policy.PolicyValueValidator.FILE_PATH_PATTERN,
				PolicyValueValidator.FILE_PATH_PATTERN);
		assertSame(de.tum.cit.ase.ares.api.policy.PolicyValueValidator.HOST_PATTERN, PolicyValueValidator.HOST_PATTERN);
	}

	/**
	 * Checks that every configuration the enum declares is accepted.
	 */
	@ParameterizedTest
	@EnumSource(ProgrammingLanguageConfiguration.class)
	void acceptsEveryConfigurationTheEnumDeclares(ProgrammingLanguageConfiguration configuration) {
		assertTrue(PolicyValueValidator.matches(configuration.name(),
				PolicyValueValidator.PROGRAMMING_LANGUAGE_CONFIGURATION_PATTERN));
	}

	/**
	 * Checks that an empty, unknown, padded or other-language configuration name is
	 * rejected.
	 */
	@ParameterizedTest
	@ValueSource(strings = { "", "JAVA_USING_ANT_ARCHUNIT_AND_ASPECTJ", "JAVA_USING_MAVEN_ARCHUNIT_AND_ASPECTJ ",
			"PYTHON_USING_MAVEN_ARCHUNIT_AND_ASPECTJ" })
	void rejectsAConfigurationTheEnumDoesNotDeclare(String value) {
		assertFalse(
				PolicyValueValidator.matches(value, PolicyValueValidator.PROGRAMMING_LANGUAGE_CONFIGURATION_PATTERN));
	}

	/**
	 * Checks that matches accepts a valid package, rejects an invalid one and
	 * returns false for null.
	 */
	@Test
	void matchesAnswersForAReleasedPatternAndToleratesNull() {
		assertTrue(PolicyValueValidator.matches("de.tum.cit.ase", PolicyValueValidator.JAVA_PACKAGE_PATTERN));
		assertFalse(PolicyValueValidator.matches("de..tum", PolicyValueValidator.JAVA_PACKAGE_PATTERN));
		assertFalse(PolicyValueValidator.matches(null, PolicyValueValidator.JAVA_PACKAGE_PATTERN));
	}

	/**
	 * Checks that requireMatch passes a valid host and throws an
	 * IllegalArgumentException for an invalid one.
	 */
	@Test
	void requireMatchStillThrowsIllegalArgumentExceptionOnAMismatch() {
		assertDoesNotThrow(
				() -> PolicyValueValidator.requireMatch("onTheHost", "localhost", PolicyValueValidator.HOST_PATTERN));
		assertThrows(IllegalArgumentException.class,
				() -> PolicyValueValidator.requireMatch("onTheHost", "not a host", PolicyValueValidator.HOST_PATTERN));
	}

	/**
	 * Checks that both package import methods accept the wildcard and plain package
	 * names.
	 */
	@ParameterizedTest
	@ValueSource(strings = { "*", "java.util", "de.tum.cit.ase.ares" })
	void acceptsAReleasedPackageImport(String value) {
		assertTrue(PolicyValueValidator.matchesPackageImport(value));
		assertDoesNotThrow(() -> PolicyValueValidator.requirePackageImport(value));
	}

	/**
	 * Checks that both package import methods reject an empty value, a doubled dot,
	 * a trailing wildcard and a keyword.
	 */
	@ParameterizedTest
	@ValueSource(strings = { "", "de..tum", "de.tum.*", "int.foo" })
	void rejectsAReleasedPackageImport(String value) {
		assertFalse(PolicyValueValidator.matchesPackageImport(value));
		assertThrows(IllegalArgumentException.class, () -> PolicyValueValidator.requirePackageImport(value));
	}

	/**
	 * Checks that the class still refuses instantiation, even through reflection,
	 * with a SecurityException.
	 */
	@Test
	void refusesInstantiationJustAsTheReleasedUtilityDid() throws NoSuchMethodException {
		Constructor<PolicyValueValidator> constructor = PolicyValueValidator.class.getDeclaredConstructor();
		constructor.setAccessible(true);
		InvocationTargetException thrown = assertThrows(InvocationTargetException.class, constructor::newInstance);
		assertInstanceOf(SecurityException.class, thrown.getCause());
	}

	/**
	 * Checks that the thread pattern still accepts the wildcard, the lambda token
	 * and a class name.
	 */
	@Test
	void keepsTheReleasedThreadTokensMatching() {
		Pattern threadPattern = PolicyValueValidator.THREAD_CLASS_PATTERN;
		assertTrue(PolicyValueValidator.matches("*", threadPattern));
		assertTrue(PolicyValueValidator.matches("Lambda-Expression", threadPattern));
		assertTrue(PolicyValueValidator.matches("java.lang.Thread", threadPattern));
	}
}
