package de.tum.cit.ase.ares.api.policy;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import de.tum.cit.ase.ares.api.policy.policySubComponents.ProgrammingLanguageConfiguration;
import de.tum.cit.ase.ares.api.policy.policySubComponents.ResourceAccesses;
import de.tum.cit.ase.ares.api.policy.policySubComponents.SupervisedCode;

class PolicyValueValidatorTest {

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

	@Test
	void rejectsControlCharactersInPaths() {
		for (int codePoint : new int[] { 10, 9, 13, 12, 11 }) {
			assertFalse(PolicyValueValidator.matches("/tmp/foo" + (char) codePoint + "bar",
					PolicyValueValidator.FILE_PATH_PATTERN));
		}
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
	void appliesLanguageValidationToSupervisedCode() {
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
