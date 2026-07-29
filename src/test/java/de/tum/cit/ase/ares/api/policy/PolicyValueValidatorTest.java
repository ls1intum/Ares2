package de.tum.cit.ase.ares.api.policy;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.regex.Pattern;

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
		// 0x85 NEL, 0x2028 line separator and 0x2029 paragraph separator are Unicode
		// controls and line separators that the default \p{Cntrl} would not catch.
		for (int codePoint : new int[] { 0, 7, 9, 10, 11, 12, 13, 0x85, 0x2028, 0x2029 }) {
			assertFalse(PolicyValueValidator.matches("/tmp/foo" + (char) codePoint + "bar",
					PolicyValueValidator.FILE_PATH_PATTERN));
		}
	}

	@ParameterizedTest
	@ValueSource(strings = { "*", "echo", "/usr/bin/echo", "src/test/resources/trustedExecute.sh", "my program" })
	void acceptsSupportedCommands(String value) {
		assertTrue(PolicyValueValidator.matches(value, PolicyValueValidator.COMMAND_PATTERN));
	}

	// A leading or trailing em space (U+2003) is a Unicode space separator that
	// plain \S does not treat as whitespace, so the boundary must exclude \p{Z}.
	@ParameterizedTest
	@ValueSource(strings = { "", " ", "echo ", " echo", "\techo", "echo\t", "\u2003echo", "echo\u2003" })
	void rejectsCommandsWithSurroundingWhitespace(String value) {
		assertFalse(PolicyValueValidator.matches(value, PolicyValueValidator.COMMAND_PATTERN));
	}

	@Test
	void rejectsControlAndLineSeparatorCharactersInCommands() {
		for (int codePoint : new int[] { 0, 7, 10, 13, 0x85, 0x2028, 0x2029 }) {
			assertFalse(
					PolicyValueValidator.matches("ec" + (char) codePoint + "ho", PolicyValueValidator.COMMAND_PATTERN));
			assertFalse(PolicyValueValidator.matches((char) codePoint + "echo", PolicyValueValidator.COMMAND_PATTERN));
			assertFalse(PolicyValueValidator.matches("echo" + (char) codePoint, PolicyValueValidator.COMMAND_PATTERN));
		}
	}

	// An argument, unlike a command, may legitimately be or contain whitespace,
	// including the em space \u2003; only controls and line separators are
	// rejected.
	@ParameterizedTest
	@ValueSource(strings = { "*", "", " ", "\u2003", "--flag", "with space", "value=1" })
	void acceptsSupportedCommandArguments(String value) {
		assertTrue(PolicyValueValidator.matches(value, PolicyValueValidator.COMMAND_ARGUMENT_PATTERN));
	}

	@Test
	void rejectsControlAndLineSeparatorCharactersInCommandArguments() {
		for (int codePoint : new int[] { 0, 7, 10, 13, 0x85, 0x2028, 0x2029 }) {
			assertFalse(PolicyValueValidator.matches("ar" + (char) codePoint + "g",
					PolicyValueValidator.COMMAND_ARGUMENT_PATTERN));
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

	@Test
	void matchesReturnsFalseForANullValue() {
		assertFalse(PolicyValueValidator.matches(null, PolicyValueValidator.FILE_PATH_PATTERN));
	}

	@Test
	void requireMatchAcceptsAValueMatchingThePattern() {
		assertDoesNotThrow(
				() -> PolicyValueValidator.requireMatch("path", "/tmp/data", PolicyValueValidator.FILE_PATH_PATTERN));
	}

	@Test
	void requireMatchReportsTheFieldSpecificExpectationForAKnownPattern() {
		IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class,
				() -> PolicyValueValidator.requireMatch("onTheHost", "not a host", PolicyValueValidator.HOST_PATTERN));
		// The mapped, human-readable expectation is used, not the raw pattern source
		// that
		// the unmapped fallback would emit.
		assertTrue(thrown.getMessage().contains("not a host"));
		assertFalse(thrown.getMessage().contains(PolicyValueValidator.HOST_PATTERN.pattern()));
	}

	@Test
	void requireMatchFallsBackToThePatternSourceForAnUnmappedPattern() {
		Pattern unmapped = Pattern.compile("^[a-z]+$");
		IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class,
				() -> PolicyValueValidator.requireMatch("field", "BAD", unmapped));
		assertTrue(thrown.getMessage().contains(unmapped.pattern()));
	}

	@Test
	void cannotBeInstantiatedReflectively() throws NoSuchMethodException {
		Constructor<PolicyValueValidator> constructor = PolicyValueValidator.class.getDeclaredConstructor();
		constructor.setAccessible(true);
		InvocationTargetException thrown = assertThrows(InvocationTargetException.class, constructor::newInstance);
		assertInstanceOf(SecurityException.class, thrown.getCause());
	}
}
