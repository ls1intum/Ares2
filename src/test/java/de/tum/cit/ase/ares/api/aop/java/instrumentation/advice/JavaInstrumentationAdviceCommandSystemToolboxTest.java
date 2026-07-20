package de.tum.cit.ase.ares.api.aop.java.instrumentation.advice;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Method;

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
		JavaInstrumentationAdviceAbstractToolbox.localize("security.advice.denial.reason.no.allowlist");
		JavaInstrumentationAdviceAbstractToolbox.localize("security.advice.denial.reason.not.in.allowlist");
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
			assertTrue(exception.getMessage().contains(" | Reason:") || exception.getMessage().contains(" | Grund:"),
					() -> "Command exception should carry a denial reason suffix, but was:\n" + exception.getMessage());
			assertTrue(
					exception.getMessage().contains("No allow rule configured")
							|| exception.getMessage().contains("Keine Erlaubnisregel"),
					() -> "Expected the no-allowlist reason, but was:\n" + exception.getMessage());
			assertFalse(
					exception.getMessage().contains("No configured allow rule permits this access") || exception
							.getMessage().contains("Keine konfigurierte Erlaubnisregel gestattet diesen Zugriff"),
					() -> "Did not expect the not-permitted reason, but was:\n" + exception.getMessage());
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
			assertTrue(exception.getMessage().contains(" | Reason:") || exception.getMessage().contains(" | Grund:"),
					() -> "Command exception should carry a denial reason suffix, but was:\n" + exception.getMessage());
			assertTrue(
					exception.getMessage().contains("No configured allow rule permits this access") || exception
							.getMessage().contains("Keine konfigurierte Erlaubnisregel gestattet diesen Zugriff"),
					() -> "Expected the not-permitted reason, but was:\n" + exception.getMessage());
			assertFalse(
					exception.getMessage().contains("No allow rule configured")
							|| exception.getMessage().contains("Keine Erlaubnisregel"),
					() -> "Did not expect the no-allowlist reason, but was:\n" + exception.getMessage());
		} finally {
			resetSettings();
		}
	}
}
