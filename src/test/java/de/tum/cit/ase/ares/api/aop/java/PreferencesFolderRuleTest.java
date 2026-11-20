package de.tum.cit.ase.ares.api.aop.java;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

import java.nio.file.Path;
import java.util.Locale;
import java.util.prefs.AbstractPreferences;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import de.tum.cit.ase.ares.api.aop.AOPMode;
import de.tum.cit.ase.ares.api.localization.AdviceActionWording;

import example.student.BaselineAdviceProbe;
import example.student.BaselineAdviceProbe.Call;

/**
 * Checks, for both runtime backends, that a write rule scoped to a folder does
 * not let student code write to the {@code java.util.prefs} store, whose files
 * the student cannot name, while a write rule for all paths ("*") does.
 */
class PreferencesFolderRuleTest {

	/** The settings that hold one allow-list per file-system action. */
	private static final String[] ALLOW_LIST_SETTINGS = { "pathsAllowedToBeRead", "pathsAllowedToBeOverwritten",
			"pathsAllowedToBeCreated", "pathsAllowedToBeExecuted", "pathsAllowedToBeDeleted" };

	/** The name Ares reports for the Preferences store when it refuses a call. */
	private static final String PREFERENCES_STORE = "<java.util.prefs backing store>";

	/** The reason Ares gives when allow rules exist but none of them matches. */
	private static final String NO_MATCHING_RULE = "No configured allow rule permits this access.";

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
	 * Permits writing inside one folder only: a Preferences put must still be
	 * refused as a write to the store, because no rule names it.
	 *
	 * @param mode   the backend that judges the call
	 * @param folder the only folder writing is permitted in
	 */
	@ParameterizedTest(name = "{0}")
	@EnumSource(value = AOPMode.class, names = { "INSTRUMENTATION", "ASPECTJ" })
	void folderWriteRuleDoesNotPermitPreferencesPut(AOPMode mode, @TempDir Path folder) {
		configure(mode, folder.toString());
		String message = AdviceActionWording.inLocale(Locale.ENGLISH, () -> denialOf(mode));
		assertThat(message).contains(" overwrite ").contains(PREFERENCES_STORE).contains(NO_MATCHING_RULE);
	}

	/**
	 * Permits writing on all paths: a Preferences put must then pass, so the
	 * refusal above comes from the folder rule rather than from the test setup.
	 *
	 * @param mode the backend that judges the call
	 */
	@ParameterizedTest(name = "{0}")
	@EnumSource(value = AOPMode.class, names = { "INSTRUMENTATION", "ASPECTJ" })
	void allPathsWriteRulePermitsPreferencesPut(AOPMode mode) {
		configure(mode, "*");
		assertThatCode(() -> BaselineAdviceProbe.check(mode, "overwrite", put())).doesNotThrowAnyException();
	}

	/**
	 * Publishes the policy values of the given backend with every allow-list empty
	 * except the write list, which holds the one given entry.
	 *
	 * @param mode          the backend
	 * @param writableEntry the only entry of the write allow-list
	 */
	private static void configure(AOPMode mode, String writableEntry) {
		JavaAOPTestCase.setJavaAdviceSettingValue("aopMode", mode.name(), "ARCHUNIT", mode.name());
		JavaAOPTestCase.setJavaAdviceSettingValue("restrictedPackage", "example.student", "ARCHUNIT", mode.name());
		JavaAOPTestCase.setJavaAdviceSettingValue("allowedListedClasses", new String[0], "ARCHUNIT", mode.name());
		for (String setting : ALLOW_LIST_SETTINGS) {
			JavaAOPTestCase.setJavaAdviceSettingValue(setting, new String[0], "ARCHUNIT", mode.name());
		}
		JavaAOPTestCase.setJavaAdviceSettingValue("pathsAllowedToBeOverwritten", new String[] { writableEntry },
				"ARCHUNIT", mode.name());
	}

	/**
	 * Describes a Preferences put the way both backends see it.
	 *
	 * @return the call
	 */
	private static Call put() {
		return new Call(AbstractPreferences.class, "put", "(Ljava/lang/String;Ljava/lang/String;)V",
				new Class<?>[] { String.class, String.class }, new Object[] { "key", "value" }, null);
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
		try {
			BaselineAdviceProbe.check(mode, "overwrite", put());
		} catch (SecurityException denied) {
			return denied.getMessage();
		}
		throw new AssertionError(mode + " allowed a Preferences put it should refuse");
	}
}
