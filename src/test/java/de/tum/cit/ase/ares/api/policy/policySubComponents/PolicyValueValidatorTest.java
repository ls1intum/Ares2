package de.tum.cit.ase.ares.api.policy.policySubComponents;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class PolicyValueValidatorTest {

	@Test
	void acceptsEverySupportedProgrammingLanguageConfiguration() {
		for (ProgrammingLanguageConfiguration configuration : ProgrammingLanguageConfiguration.values()) {
			assertTrue(PolicyValueValidator.matches(configuration.name(),
					PolicyValueValidator.PROGRAMMING_LANGUAGE_CONFIGURATION_PATTERN));
		}
	}

	@ParameterizedTest
	@ValueSource(strings = { "JAVA_MAVEN_ARCHUNIT_ASPECTJ", "JAVA_USING_MAVEN_ARCHUNIT_ASPECTJ",
			"JAVA_USING_MAVEN_ARCHUNIT_AND_BYTEBUDDY", "KOTLIN_USING_GRADLE_ARCHUNIT_AND_ASPECTJ" })
	void rejectsUnsupportedProgrammingLanguageConfigurations(String value) {
		assertFalse(
				PolicyValueValidator.matches(value, PolicyValueValidator.PROGRAMMING_LANGUAGE_CONFIGURATION_PATTERN));
	}

	@ParameterizedTest
	@ValueSource(strings = { "com.example", "de.übung.例", "$generated._internal" })
	void acceptsCompleteJavaPackageIdentifiers(String value) {
		assertTrue(PolicyValueValidator.matches(value, PolicyValueValidator.JAVA_PACKAGE_PATTERN));
	}

	@ParameterizedTest
	@ValueSource(strings = { "", ".example", "com..example", "com.example.", "com.class.example", "1example" })
	void rejectsInvalidJavaPackageIdentifiers(String value) {
		assertFalse(PolicyValueValidator.matches(value, PolicyValueValidator.JAVA_PACKAGE_PATTERN));
	}

	@ParameterizedTest
	@ValueSource(strings = { "Main", "Übung", "$Generated", "Outer$Inner" })
	void acceptsCompleteJavaClassNames(String value) {
		assertTrue(PolicyValueValidator.matches(value, PolicyValueValidator.JAVA_CLASS_NAME_PATTERN));
	}

	@ParameterizedTest
	@ValueSource(strings = { "", "com.example.Main", "1Main", "class", "record", "sealed", "permits", "yield", "var",
			"Main-Class" })
	void rejectsInvalidJavaClassNames(String value) {
		assertFalse(PolicyValueValidator.matches(value, PolicyValueValidator.JAVA_CLASS_NAME_PATTERN));
	}

	@ParameterizedTest
	@ValueSource(strings = { "MainTest", "com.example.MainTest", "de.übung.例Test", "com.example.Outer$Inner" })
	void acceptsJavaClassPaths(String value) {
		assertTrue(PolicyValueValidator.matches(value, PolicyValueValidator.JAVA_CLASS_PATH_PATTERN));
	}

	@ParameterizedTest
	@ValueSource(strings = { "com.example.", "com..example.Test", "com.example.class", "com/example/Test" })
	void rejectsInvalidJavaClassPaths(String value) {
		assertFalse(PolicyValueValidator.matches(value, PolicyValueValidator.JAVA_CLASS_PATH_PATTERN));
	}

	@ParameterizedTest
	@ValueSource(strings = { "*", "/var/lib/ares", "src/main/java", "relative/path", "archive..bak", "path/.../file",
			"path/..suffix", "C:\\Users\\Ares", "C:/Users/Ares", "\\\\server\\share\\file", "${PROJECT_ROOT}/target",
			"${java.home}/bin", "${user.home}/.ares", "${java.io.tmpdir}/ares" })
	void acceptsSupportedPathsAndPlaceholders(String value) {
		assertTrue(PolicyValueValidator.matches(value, PolicyValueValidator.FILE_PATH_PATTERN));
	}

	@ParameterizedTest
	@ValueSource(strings = { "", " ", "${UNKNOWN}/target", "/tmp/*", "src/*/java", "..", "../relative/path",
			"..\\relative\\path", "path/../file", "path\\..\\file", "path/..", "path\\..", "/../etc", "C:\\..\\Windows",
			"${PROJECT_ROOT}/../outside" })
	void rejectsInvalidPathsAndEmbeddedWildcards(String value) {
		assertFalse(PolicyValueValidator.matches(value, PolicyValueValidator.FILE_PATH_PATTERN));
	}

	@ParameterizedTest
	@ValueSource(strings = { "*", "localhost", "example.org", "api.example.org.", "127.0.0.1", "255.255.255.255", "::1",
			"2001:db8::1", "::ffff:192.0.2.128" })
	void acceptsSupportedHostForms(String value) {
		assertTrue(PolicyValueValidator.matches(value, PolicyValueValidator.HOST_PATTERN));
	}

	@ParameterizedTest
	@ValueSource(strings = { "", "-example.org", "example..org", "256.1.1.1", "2001:db8:::1", "host name" })
	void rejectsInvalidHosts(String value) {
		assertFalse(PolicyValueValidator.matches(value, PolicyValueValidator.HOST_PATTERN));
	}

	@Test
	void acceptsPackageAndThreadWildcardsAndRecognisedThreadTokens() {
		assertDoesNotThrow(() -> new PackagePermission("*"));
		assertThrows(IllegalArgumentException.class, () -> new PackagePermission("java.*"));
		Stream.of("*", "Lambda-Expression", "<implicit-thread-op:parallelStream>", "<implicit-thread-op:parallel>",
				"<implicit-thread-op:Thread.sleep>", "<implicit-thread-op:SubmissionPublisher.submit>",
				"<implicit-thread-op:SubmissionPublisher.offer>", "java.lang.Thread")
				.forEach(value -> assertDoesNotThrow(() -> new ThreadPermission(1, value)));
		assertThrows(IllegalArgumentException.class, () -> new ThreadPermission(1, "<implicit-thread-op:unknown>"));
	}

	@Test
	void appliesJavaValidationToSupervisedCode() {
		ResourceAccesses resources = ResourceAccesses.createRestrictive();
		assertDoesNotThrow(
				() -> new SupervisedCode(ProgrammingLanguageConfiguration.JAVA_USING_MAVEN_ARCHUNIT_AND_ASPECTJ,
						"de.übung", "Main", List.of("de.übung.MainTest"), resources));
		assertThrows(IllegalArgumentException.class,
				() -> new SupervisedCode(ProgrammingLanguageConfiguration.JAVA_USING_MAVEN_ARCHUNIT_AND_ASPECTJ,
						"de.class", "Main", List.of("de.übung.MainTest"), resources));
		assertThrows(IllegalArgumentException.class,
				() -> new SupervisedCode(ProgrammingLanguageConfiguration.JAVA_USING_MAVEN_ARCHUNIT_AND_ASPECTJ,
						"de.übung", "main.Class", List.of("de.übung.MainTest"), resources));
		assertThrows(IllegalArgumentException.class,
				() -> new SupervisedCode(ProgrammingLanguageConfiguration.JAVA_USING_MAVEN_ARCHUNIT_AND_ASPECTJ,
						"de.übung", "Main", List.of("de.übung."), resources));
	}
}
