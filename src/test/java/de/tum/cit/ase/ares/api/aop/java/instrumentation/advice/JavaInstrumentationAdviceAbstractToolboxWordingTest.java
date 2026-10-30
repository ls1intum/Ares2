package de.tum.cit.ase.ares.api.aop.java.instrumentation.advice;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Locale;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import de.tum.cit.ase.ares.api.localization.AdviceActionWording;

/**
 * Checks how the instrumentation back-end words actions and callers in its
 * violation messages. The German message used to contain the raw English action
 * and an English caller note.
 */
class JavaInstrumentationAdviceAbstractToolboxWordingTest {

	/**
	 * Every action is translated for the message, and an unknown one is kept as it
	 * is.
	 *
	 * @param action  the raw action name
	 * @param english the expected English wording
	 * @param german  the expected German wording
	 */
	@ParameterizedTest
	@MethodSource("de.tum.cit.ase.ares.api.localization.AdviceActionWording#actions")
	void actionIsWordedInTheActiveLanguage(String action, String english, String german) {
		assertEquals(english, AdviceActionWording.inLocale(Locale.ENGLISH,
				() -> JavaInstrumentationAdviceAbstractToolbox.localizeAction(action)));
		assertEquals(german, AdviceActionWording.inLocale(Locale.GERMAN,
				() -> JavaInstrumentationAdviceAbstractToolbox.localizeAction(action)));
	}

	/**
	 * The caller note follows the active language and disappears when the caller is
	 * unknown.
	 *
	 * @param caller  the calling student method, or {@code null}
	 * @param english the expected English note
	 * @param german  the expected German note
	 */
	@ParameterizedTest
	@MethodSource("de.tum.cit.ase.ares.api.localization.AdviceActionWording#callers")
	void callerNoteIsWordedInTheActiveLanguage(String caller, String english, String german) {
		assertEquals(english, AdviceActionWording.inLocale(Locale.ENGLISH,
				() -> JavaInstrumentationAdviceAbstractToolbox.describeCaller(caller)));
		assertEquals(german, AdviceActionWording.inLocale(Locale.GERMAN,
				() -> JavaInstrumentationAdviceAbstractToolbox.describeCaller(caller)));
	}
}
