package de.tum.cit.ase.ares.api.aop.java.instrumentation.advice;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Method;
import java.util.List;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import de.tum.cit.ase.ares.api.aop.java.JavaAOPTestCase;
import de.tum.cit.ase.ares.api.aop.java.JavaAOPTestCaseSettings;

import example.student.InstrumentationSecurityProbe;

class JavaInstrumentationAdviceCommandSystemToolboxTest {

	@Test
	void wildcardCommandAndArgumentsMatchAnyRuntimeValues() throws Exception {
		Method commandMatches = JavaInstrumentationAdviceCommandSystemToolbox.class.getDeclaredMethod("commandMatches",
				String.class, String.class);
		commandMatches.setAccessible(true);
		Method argumentsMatch = JavaInstrumentationAdviceCommandSystemToolbox.class.getDeclaredMethod("argumentsMatch",
				String[].class, String[].class);
		argumentsMatch.setAccessible(true);

		assertTrue((boolean) commandMatches.invoke(null, "java", "*"));
		assertTrue((boolean) argumentsMatch.invoke(null, (Object) new String[] { "*" },
				(Object) new String[] { "--version", "--help" }));
		assertTrue((boolean) argumentsMatch.invoke(null, (Object) new String[] { "run", "*" },
				(Object) new String[] { "run", "anything" }));
		assertFalse((boolean) argumentsMatch.invoke(null, (Object) new String[] { "run", "*" },
				(Object) new String[] { "run", "anything", "else" }));
	}

	/**
	 * Loads the localization bundle while still unrestricted, so that building a
	 * denial message under INSTRUMENTATION mode hits the cached bundle instead of
	 * reading messages.properties, which the file-system advice would otherwise
	 * block. Without this, the message would degrade to the {@code !key!} fallback
	 * and the reason assertions would fail on a cold bundle.
	 */
	@BeforeAll
	static void warmLocalizationBundle() {
		JavaInstrumentationAdviceAbstractToolbox.localize("security.advice.denial.reason.no.rule.command");
		JavaInstrumentationAdviceAbstractToolbox.localize("security.advice.denial.reason.target.not.allowed.command");
		JavaInstrumentationAdviceAbstractToolbox.localize("security.advice.illegal.command.execution");
	}

	private static void resetSettings() throws Exception {
		Method reset = JavaAOPTestCaseSettings.class.getDeclaredMethod("reset");
		reset.setAccessible(true);
		reset.invoke(null);
	}

	private static void configureInstrumentationMode() {
		JavaAOPTestCase.setJavaAdviceSettingValue("aopMode", "INSTRUMENTATION", "ARCH", "INSTRUMENTATION");
		JavaAOPTestCase.setJavaAdviceSettingValue("restrictedPackage", "example.student", "ARCH", "INSTRUMENTATION");
		JavaAOPTestCase.setJavaAdviceSettingValue("allowedListedClasses", new String[0], "ARCH", "INSTRUMENTATION");
	}

	@Test
	void checkCommandSystemInteraction_appendsNoAllowlistReasonWhenNoRuleConfigured() throws Exception {
		try {
			resetSettings();
			configureInstrumentationMode();
			JavaAOPTestCase.setJavaAdviceSettingValue("commandsAllowedToBeExecuted", new String[0], "ARCH",
					"INSTRUMENTATION");
			JavaAOPTestCase.setJavaAdviceSettingValue("argumentsAllowedToBePassed", new String[0][], "ARCH",
					"INSTRUMENTATION");

			SecurityException exception = assertThrows(SecurityException.class,
					() -> InstrumentationSecurityProbe.checkCommandExecution("forbidden-command"));
			assertDenialReason(exception, "security.advice.denial.reason.no.rule.command");
		} finally {
			resetSettings();
		}
	}

	@Test
	void checkCommandSystemInteraction_appendsNotPermittedReasonWhenConfiguredButNotAllowed() throws Exception {
		try {
			resetSettings();
			configureInstrumentationMode();
			JavaAOPTestCase.setJavaAdviceSettingValue("commandsAllowedToBeExecuted", new String[] { "echo" }, "ARCH",
					"INSTRUMENTATION");
			JavaAOPTestCase.setJavaAdviceSettingValue("argumentsAllowedToBePassed", new String[][] { { "hello" } },
					"ARCH", "INSTRUMENTATION");

			SecurityException exception = assertThrows(SecurityException.class,
					() -> InstrumentationSecurityProbe.checkCommandExecution("forbidden-command"));
			assertDenialReason(exception, "security.advice.denial.reason.target.not.allowed.command");
		} finally {
			resetSettings();
		}
	}

	@Test
	void checkCommandSystemInteraction_blocksStartPipelineCommandMutatedAfterConstruction() throws Exception {
		try {
			resetSettings();
			configureInstrumentationMode();
			JavaAOPTestCase.setJavaAdviceSettingValue("commandsAllowedToBeExecuted", new String[] { "echo" }, "ARCH",
					"INSTRUMENTATION");
			JavaAOPTestCase.setJavaAdviceSettingValue("argumentsAllowedToBePassed", new String[][] { { "hello" } },
					"ARCH", "INSTRUMENTATION");

			ProcessBuilder producer = new ProcessBuilder("echo", "hello");
			ProcessBuilder consumer = new ProcessBuilder("echo", "hello");
			// Allowed at construction time, mutated to a forbidden command before
			// startPipeline is invoked - the exact TD-019 bypass this test defends against.
			consumer.command(List.of("forbidden-command"));

			SecurityException exception = assertThrows(SecurityException.class,
					() -> InstrumentationSecurityProbe.checkProcessBuilderStartPipeline(List.of(producer, consumer)));
			assertTrue(exception.getMessage().contains("forbidden-command"),
					() -> "Expected the mutated forbidden command in the message, but was:\n" + exception.getMessage());
		} finally {
			resetSettings();
		}
	}

	@Test
	void checkCommandSystemInteraction_allowsStartPipelineWhenEveryBuilderCommandIsAllowed() throws Exception {
		try {
			resetSettings();
			configureInstrumentationMode();
			JavaAOPTestCase.setJavaAdviceSettingValue("commandsAllowedToBeExecuted", new String[] { "echo" }, "ARCH",
					"INSTRUMENTATION");
			JavaAOPTestCase.setJavaAdviceSettingValue("argumentsAllowedToBePassed", new String[][] { { "hello" } },
					"ARCH", "INSTRUMENTATION");

			ProcessBuilder producer = new ProcessBuilder("echo", "hello");
			ProcessBuilder consumer = new ProcessBuilder("echo", "hello");

			assertDoesNotThrow(
					() -> InstrumentationSecurityProbe.checkProcessBuilderStartPipeline(List.of(producer, consumer)));
		} finally {
			resetSettings();
		}
	}

	/**
	 * Requires the denial to be the one denial sentence ending with the given
	 * reason, with no raw message key and no old reason splice left in it, which
	 * would show that instrumentation could not translate the message.
	 *
	 * @param exception the denial
	 * @param reasonKey the key of the expected reason clause
	 */
	private static void assertDenialReason(SecurityException exception, String reasonKey) {
		String message = exception.getMessage();
		String reason = JavaInstrumentationAdviceAbstractToolbox.localize(reasonKey);
		assertTrue(message.endsWith(reason + "."), () -> "Expected the reason '" + reason + "', but was:\n" + message);
		assertFalse(message.contains(" | "), () -> "Did not expect the old reason splice, but was:\n" + message);
		assertFalse(message.contains("security.advice."),
				() -> "Did not expect a raw message key, but was:\n" + message);
	}
}
