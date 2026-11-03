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

/**
 * Checks which paths, commands, command arguments and hosts
 * {@link PolicyValueValidator} accepts and which it rejects. The validator is
 * fed literal IP addresses on purpose, valid and invalid ones alike, which is
 * why the PMD warning about hard-coded addresses is suppressed.
 */
@SuppressWarnings("PMD.AvoidUsingHardCodedIP")
class PolicyValueValidatorTest {

	/**
	 * Checks that the wildcard, absolute, relative, Windows and network paths and
	 * the supported placeholders are accepted as file paths.
	 */
	@ParameterizedTest
	@ValueSource(strings = { "*", "/var/lib/ares", "src/main/java", "relative/path", "archive..bak", "path/.../file",
			"path/..suffix", "C:\\Users\\Ares", "C:/Users/Ares", "\\\\server\\share\\file", "${PROJECT_ROOT}/target",
			"${java.home}/bin", "${user.home}/.ares", "${java.io.tmpdir}/ares" })
	void acceptsSupportedPathsAndPlaceholders(String value) {
		assertTrue(PolicyValueValidator.matches(value, PolicyValueValidator.FILE_PATH_PATTERN));
	}

	/**
	 * Checks that blank paths, unknown placeholders, wildcards inside a path and
	 * every form of parent-directory step are rejected.
	 */
	@ParameterizedTest
	@ValueSource(strings = { "", " ", "${UNKNOWN}/target", "/tmp/*", "src/*/java", "..", "../relative/path",
			"..\\relative\\path", "path/../file", "path\\..\\file", "path/..", "path\\..", "/../etc", "C:\\..\\Windows",
			"${PROJECT_ROOT}/../outside" })
	void rejectsInvalidPathsAndEmbeddedWildcards(String value) {
		assertFalse(PolicyValueValidator.matches(value, PolicyValueValidator.FILE_PATH_PATTERN));
	}

	/**
	 * Checks that control characters and line separators inside a path are
	 * rejected, including NEL (U+0085) and the line and paragraph separators
	 * (U+2028, U+2029), which the default {@code \p{Cntrl}} would not catch.
	 */
	@Test
	void rejectsControlCharactersInPaths() {
		for (int codePoint : new int[] { 0, 7, 9, 10, 11, 12, 13, 0x85, 0x2028, 0x2029 }) {
			assertFalse(PolicyValueValidator.matches("/tmp/foo" + (char) codePoint + "bar",
					PolicyValueValidator.FILE_PATH_PATTERN));
		}
	}

	/**
	 * Checks that the wildcard, a bare command, absolute and relative paths to one
	 * and a name with an inner space are accepted as commands.
	 */
	@ParameterizedTest
	@ValueSource(strings = { "*", "echo", "/usr/bin/echo", "src/test/resources/trustedExecute.sh", "my program" })
	void acceptsSupportedCommands(String value) {
		assertTrue(PolicyValueValidator.matches(value, PolicyValueValidator.COMMAND_PATTERN));
	}

	/**
	 * Checks that a command with leading or trailing whitespace is rejected. The em
	 * space (U+2003) is included because plain {@code \S} does not treat it as
	 * whitespace, so the boundary must also exclude {@code \p{Z}}.
	 */
	@ParameterizedTest
	@ValueSource(strings = { "", " ", "echo ", " echo", "\techo", "echo\t", "\u2003echo", "echo\u2003" })
	void rejectsCommandsWithSurroundingWhitespace(String value) {
		assertFalse(PolicyValueValidator.matches(value, PolicyValueValidator.COMMAND_PATTERN));
	}

	/**
	 * Checks that a control character or line separator is rejected at the start,
	 * in the middle and at the end of a command.
	 */
	@Test
	void rejectsControlAndLineSeparatorCharactersInCommands() {
		for (int codePoint : new int[] { 0, 7, 10, 13, 0x85, 0x2028, 0x2029 }) {
			assertFalse(
					PolicyValueValidator.matches("ec" + (char) codePoint + "ho", PolicyValueValidator.COMMAND_PATTERN));
			assertFalse(PolicyValueValidator.matches((char) codePoint + "echo", PolicyValueValidator.COMMAND_PATTERN));
			assertFalse(PolicyValueValidator.matches("echo" + (char) codePoint, PolicyValueValidator.COMMAND_PATTERN));
		}
	}

	/**
	 * Checks that command arguments are accepted, including empty ones and ones
	 * that are or contain whitespace such as the em space (U+2003). Unlike a
	 * command, an argument may legitimately hold whitespace.
	 */
	@ParameterizedTest
	@ValueSource(strings = { "*", "", " ", "\u2003", "--flag", "with space", "value=1" })
	void acceptsSupportedCommandArguments(String value) {
		assertTrue(PolicyValueValidator.matches(value, PolicyValueValidator.COMMAND_ARGUMENT_PATTERN));
	}

	/**
	 * Checks that a control character or line separator inside a command argument
	 * is rejected.
	 */
	@Test
	void rejectsControlAndLineSeparatorCharactersInCommandArguments() {
		for (int codePoint : new int[] { 0, 7, 10, 13, 0x85, 0x2028, 0x2029 }) {
			assertFalse(PolicyValueValidator.matches("ar" + (char) codePoint + "g",
					PolicyValueValidator.COMMAND_ARGUMENT_PATTERN));
		}
	}

	/**
	 * Checks that the wildcard, host names, a fully qualified name with a trailing
	 * dot and IPv4 and IPv6 addresses are accepted as hosts.
	 */
	@ParameterizedTest
	@ValueSource(strings = { "*", "localhost", "example.org", "api.example.org.", "127.0.0.1", "255.255.255.255", "::1",
			"2001:db8::1", "::ffff:192.0.2.128" })
	void acceptsSupportedHostForms(String value) {
		assertTrue(PolicyValueValidator.matches(value, PolicyValueValidator.HOST_PATTERN));
	}

	/**
	 * Checks that empty, malformed and out-of-range host names and addresses are
	 * rejected.
	 */
	@ParameterizedTest
	@ValueSource(strings = { "", "-example.org", "example..org", "256.1.1.1", "2001:db8:::1", "host name" })
	void rejectsInvalidHosts(String value) {
		assertFalse(PolicyValueValidator.matches(value, PolicyValueValidator.HOST_PATTERN));
	}

	/**
	 * Checks that SupervisedCode applies the Java naming rules to its package, main
	 * class and test classes, accepting non-ASCII names and rejecting a keyword, a
	 * qualified main class and an incomplete test class.
	 */
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

	/**
	 * Checks that a null value never matches.
	 */
	@Test
	void matchesReturnsFalseForANullValue() {
		assertFalse(PolicyValueValidator.matches(null, PolicyValueValidator.FILE_PATH_PATTERN));
	}

	/**
	 * Checks that requireMatch does not throw for a matching value.
	 */
	@Test
	void requireMatchAcceptsAValueMatchingThePattern() {
		assertDoesNotThrow(
				() -> PolicyValueValidator.requireMatch("path", "/tmp/data", PolicyValueValidator.FILE_PATH_PATTERN));
	}

	/**
	 * Checks that a mismatch on a known pattern names the rejected value and uses
	 * the readable expectation for that pattern, not the raw pattern source that
	 * the fallback for an unknown pattern would show.
	 */
	@Test
	void requireMatchReportsTheFieldSpecificExpectationForAKnownPattern() {
		IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class,
				() -> PolicyValueValidator.requireMatch("onTheHost", "not a host", PolicyValueValidator.HOST_PATTERN));
		assertTrue(thrown.getMessage().contains("not a host"));
		assertFalse(thrown.getMessage().contains(PolicyValueValidator.HOST_PATTERN.pattern()));
	}

	/**
	 * Checks that a mismatch on a pattern without a readable expectation reports
	 * the raw pattern source instead.
	 */
	@Test
	void requireMatchFallsBackToThePatternSourceForAnUnmappedPattern() {
		Pattern unmapped = Pattern.compile("^[a-z]+$");
		IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class,
				() -> PolicyValueValidator.requireMatch("field", "BAD", unmapped));
		assertTrue(thrown.getMessage().contains(unmapped.pattern()));
	}

	/**
	 * Checks that the utility class refuses instantiation, even through reflection,
	 * with a SecurityException.
	 */
	@Test
	void cannotBeInstantiatedReflectively() throws NoSuchMethodException {
		Constructor<PolicyValueValidator> constructor = PolicyValueValidator.class.getDeclaredConstructor();
		constructor.setAccessible(true);
		InvocationTargetException thrown = assertThrows(InvocationTargetException.class, constructor::newInstance);
		assertInstanceOf(SecurityException.class, thrown.getCause());
	}
}
