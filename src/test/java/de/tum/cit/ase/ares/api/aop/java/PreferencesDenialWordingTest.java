package de.tum.cit.ase.ares.api.aop.java;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Locale;
import java.util.prefs.AbstractPreferences;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import de.tum.cit.ase.ares.api.aop.AOPMode;
import de.tum.cit.ase.ares.api.localization.AdviceActionWording;

import example.student.BaselineAdviceProbe;
import example.student.BaselineAdviceProbe.Call;

/**
 * Checks that both runtime backends word a refused {@code java.util.prefs} call
 * in the active language, like every other file-system refusal, instead of
 * mixing English words into a German message.
 */
class PreferencesDenialWordingTest {

	/** The settings that hold one allow-list per file-system action. */
	private static final String[] ALLOW_LIST_SETTINGS = { "pathsAllowedToBeRead", "pathsAllowedToBeOverwritten",
			"pathsAllowedToBeCreated", "pathsAllowedToBeExecuted", "pathsAllowedToBeDeleted" };

	/** Clears the policy values before each test. */
	@BeforeEach
	void clearBefore() {
		AOPMode.ASPECTJ.reset();
	}

	/** Clears the policy values after each test, even after a failure. */
	@AfterEach
	void clearAfter() {
		AOPMode.ASPECTJ.reset();
	}

	/**
	 * A Preferences put refused under an empty overwrite allow-list names the
	 * action and the caller in German when the locale is German.
	 *
	 * @param mode the backend that judges the call
	 */
	@ParameterizedTest(name = "{0}")
	@EnumSource(value = AOPMode.class, names = { "INSTRUMENTATION", "ASPECTJ" })
	void germanDenialOfPreferencesPutIsFullyGerman(AOPMode mode) {
		configure(mode);
		String message = AdviceActionWording.inLocale(Locale.GERMAN, () -> denialOf(mode));
		assertThat(message).contains("illegal zu überschreiben").contains("<java.util.prefs backing store>")
				.contains("(aufgerufen von ").doesNotContain(" overwrite ").doesNotContain("called by");
	}

	/**
	 * Publishes the policy values of the given backend, with every allow-list
	 * empty, so every file-system action is refused.
	 *
	 * @param mode the backend
	 */
	private static void configure(AOPMode mode) {
		JavaAOPTestCase.setJavaAdviceSettingValue("aopMode", mode.name(), "ARCHUNIT", mode.name());
		JavaAOPTestCase.setJavaAdviceSettingValue("restrictedPackage", "example.student", "ARCHUNIT", mode.name());
		JavaAOPTestCase.setJavaAdviceSettingValue("allowedListedClasses", new String[0], "ARCHUNIT", mode.name());
		for (String setting : ALLOW_LIST_SETTINGS) {
			JavaAOPTestCase.setJavaAdviceSettingValue(setting, new String[0], "ARCHUNIT", mode.name());
		}
	}

	/**
	 * Runs a Preferences put through the backend's check and returns the refusal
	 * message.
	 *
	 * @param mode the backend
	 * @return the message of the refusal
	 * @throws AssertionError if the backend allows the call
	 */
	private static String denialOf(AOPMode mode) {
		Call put = new Call(AbstractPreferences.class, "put", "(Ljava/lang/String;Ljava/lang/String;)V",
				new Class<?>[] { String.class, String.class }, new Object[] { "key", "value" }, null);
		try {
			BaselineAdviceProbe.check(mode, "overwrite", put);
		} catch (SecurityException denied) {
			return denied.getMessage();
		}
		throw new AssertionError(mode + " allowed a Preferences put it should refuse");
	}
}
