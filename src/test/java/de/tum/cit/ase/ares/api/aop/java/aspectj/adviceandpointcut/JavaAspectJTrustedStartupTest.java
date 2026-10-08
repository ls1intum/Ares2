package de.tum.cit.ase.ares.api.aop.java.aspectj.adviceandpointcut;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Locale;

import org.junit.jupiter.api.parallel.Isolated;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import de.tum.cit.ase.ares.api.aop.java.instrumentation.JavaInstrumentationAgent;
import de.tum.cit.ase.ares.api.localization.Messages;

/**
 * Checks that the AspectJ file checks fail closed when the trusted start-up did
 * not run: the agent was not attached, so the start-up values cannot be trusted
 * and every supervised file access is refused, with the message in English and
 * German.
 */
@Isolated
class JavaAspectJTrustedStartupTest {

	/**
	 * With the agent's start-up marked as not finished, the aspect's start-up check
	 * refuses with the localised start-up message.
	 *
	 * @param language the language the message is shown in
	 * @throws Exception if the agent's state cannot be changed
	 */
	@ParameterizedTest
	@ValueSource(strings = { "en", "de" })
	void fileAccessWithoutTrustedStartupIsRefused(String language) throws Exception {
		Field complete = JavaInstrumentationAgent.class.getDeclaredField("trustedStartupComplete");
		complete.setAccessible(true);
		boolean before = complete.getBoolean(null);
		Locale displayBefore = Locale.getDefault(Locale.Category.DISPLAY);
		try {
			complete.setBoolean(null, false);
			Locale.setDefault(Locale.Category.DISPLAY, Locale.forLanguageTag(language));
			Messages.init();
			Method requireTrustedStartup = JavaAspectJFileSystemAdviceDefinitions.class
					.getDeclaredMethod("requireTrustedStartup");
			requireTrustedStartup.setAccessible(true);

			InvocationTargetException thrown = assertThrows(InvocationTargetException.class,
					() -> requireTrustedStartup.invoke(null));

			assertEquals(SecurityException.class, thrown.getCause().getClass());
			assertEquals(Messages.localized("security.advice.trusted.startup.missing"), thrown.getCause().getMessage());
		} finally {
			complete.setBoolean(null, before);
			Locale.setDefault(Locale.Category.DISPLAY, displayBefore);
			Messages.init();
		}
	}
}
