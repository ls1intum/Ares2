package de.tum.cit.ase.ares.api.aop.java.instrumentation;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.lang.reflect.Field;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import de.tum.cit.ase.ares.api.aop.java.JavaAOPTestCaseSettings;

import example.student.InstrumentationSecurityProbe;

/**
 * Checks how the agent fixes the default temp directory: once, by the first
 * trusted call, never by a call with student code on the stack, and without
 * failing where the settings or the aspect are missing.
 */
class JavaInstrumentationAgentFreezeTest {

	/**
	 * Name of the settings field holding the fixed default temp directory.
	 */
	private static final String FROZEN_FIELD = "frozenDefaultTempDirectory";

	/**
	 * Name of the settings field holding the package of the student code.
	 */
	private static final String RESTRICTED_PACKAGE_FIELD = "restrictedPackage";

	/**
	 * The first freeze wins: a second one, after the temp directory property has
	 * changed, keeps the first value.
	 */
	@Test
	void freezingTwiceKeepsTheFirstValue(@TempDir Path first, @TempDir Path second) throws Exception {
		withApplicationSetting(FROZEN_FIELD, null, () -> withTempDirectoryProperty(first, () -> {
			JavaInstrumentationAgent.freezeTrustedStartupValues(applicationLoader());
			withTempDirectoryProperty(second,
					() -> JavaInstrumentationAgent.freezeTrustedStartupValues(applicationLoader()));
			assertEquals(first.toRealPath().toString(), applicationSetting(FROZEN_FIELD));
		}));
	}

	/**
	 * A directory fixed earlier is kept when the property changes later and a new
	 * freeze is attempted, as happens before every test.
	 */
	@Test
	void aFrozenDirectoryOutlivesALaterPropertyChange(@TempDir Path elsewhere) throws Exception {
		JavaInstrumentationAgent.freezeTrustedStartupValues(applicationLoader());
		Object frozenBefore = applicationSetting(FROZEN_FIELD);
		withTempDirectoryProperty(elsewhere,
				() -> JavaInstrumentationAgent.freezeTrustedStartupValues(applicationLoader()));
		assertEquals(frozenBefore, applicationSetting(FROZEN_FIELD));
	}

	/**
	 * With student code on the call stack the freeze does nothing, so student code
	 * cannot fix a directory of its choosing where nothing was fixed yet.
	 */
	@Test
	void freezeDoesNothingWhileSupervisedCodeIsOnTheStack(@TempDir Path studentChosen) throws Exception {
		withApplicationSetting(FROZEN_FIELD, null, () -> withApplicationSetting(RESTRICTED_PACKAGE_FIELD,
				"example.student", () -> withTempDirectoryProperty(studentChosen, () -> {
					InstrumentationSecurityProbe.freezeTrustedStartupValues(applicationLoader());
					assertNull(applicationSetting(FROZEN_FIELD));
				})));
	}

	/**
	 * A supervised package that encloses Ares itself does not stop Ares's own
	 * freeze, because Ares's classes are never taken for student code.
	 */
	@Test
	void aSupervisedPackageEnclosingAresStillLetsAresFreeze(@TempDir Path startUpDirectory) throws Exception {
		withApplicationSetting(FROZEN_FIELD, null, () -> withApplicationSetting(RESTRICTED_PACKAGE_FIELD,
				"de.tum.cit.ase.ares", () -> withTempDirectoryProperty(startUpDirectory, () -> {
					JavaInstrumentationAgent.freezeTrustedStartupValues(applicationLoader());
					assertEquals(startUpDirectory.toRealPath().toString(), applicationSetting(FROZEN_FIELD));
				})));
	}

	/**
	 * A loader that sees neither the application settings nor the aspect makes the
	 * freeze skip them instead of failing, as in an exercise without AspectJ.
	 */
	@Test
	void freezeSkipsWhatTheLoaderCannotSee() throws Exception {
		Object frozenBefore = applicationSetting(FROZEN_FIELD);
		try (URLClassLoader isolated = new URLClassLoader(new URL[0], null)) {
			assertDoesNotThrow(() -> JavaInstrumentationAgent.freezeTrustedStartupValues(isolated));
		}
		assertEquals(frozenBefore, applicationSetting(FROZEN_FIELD));
	}

	/**
	 * Something run while a setting or property is temporarily changed.
	 */
	@FunctionalInterface
	private interface Body {

		/**
		 * Runs the body.
		 *
		 * @throws Exception whatever the body throws
		 */
		void run() throws Exception;
	}

	/**
	 * Runs a body with one field of the application settings copy set to a value,
	 * then restores it.
	 *
	 * @param fieldName the field to change
	 * @param value     the value to use
	 * @param body      the body to run
	 * @throws Exception whatever the body throws
	 */
	private static void withApplicationSetting(String fieldName, Object value, Body body) throws Exception {
		Field field = applicationSettingField(fieldName);
		Object before = field.get(null);
		try {
			field.set(null, value);
			body.run();
		} finally {
			field.set(null, before);
		}
	}

	/**
	 * Runs a body with {@code java.io.tmpdir} pointing at a directory, then
	 * restores it.
	 *
	 * @param directory the directory to use
	 * @param body      the body to run
	 * @throws Exception whatever the body throws
	 */
	private static void withTempDirectoryProperty(Path directory, Body body) throws Exception {
		String before = System.getProperty("java.io.tmpdir");
		try {
			Files.createDirectories(directory);
			System.setProperty("java.io.tmpdir", directory.toString());
			body.run();
		} finally {
			System.setProperty("java.io.tmpdir", before);
		}
	}

	/**
	 * Reads one field of the application settings copy.
	 *
	 * @param fieldName the field to read
	 * @return its value
	 * @throws ReflectiveOperationException if the field cannot be read
	 */
	private static Object applicationSetting(String fieldName) throws ReflectiveOperationException {
		return applicationSettingField(fieldName).get(null);
	}

	/**
	 * Returns one accessible field of the application settings copy.
	 *
	 * @param fieldName the field to look up
	 * @return the field
	 * @throws NoSuchFieldException if it does not exist
	 */
	private static Field applicationSettingField(String fieldName) throws NoSuchFieldException {
		Field field = JavaAOPTestCaseSettings.class.getDeclaredField(fieldName);
		field.setAccessible(true);
		return field;
	}

	/**
	 * Returns the loader of the application settings copy.
	 *
	 * @return the application loader
	 */
	private static ClassLoader applicationLoader() {
		return JavaAOPTestCaseSettings.class.getClassLoader();
	}
}
