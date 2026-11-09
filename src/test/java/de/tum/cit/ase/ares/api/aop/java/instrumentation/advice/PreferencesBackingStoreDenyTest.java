package de.tum.cit.ase.ares.api.aop.java.instrumentation.advice;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Method;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.junit.jupiter.api.io.TempDir;

import de.tum.cit.ase.ares.api.aop.java.JavaAOPTestCase;
import de.tum.cit.ase.ares.api.aop.java.JavaAOPTestCaseSettings;

import example.student.InstrumentationSecurityProbe;

/**
 * Verifies the runtime rule for {@code java.util.prefs.Preferences}: because
 * the backing store is a disk location the student cannot name, a policy that
 * permits filesystem access only within a directory must still reject
 * Preferences, while a policy that permits all paths ("*") allows it. The call
 * is routed through a probe in {@code example.student} so a restricted-package
 * frame is on the stack, as the advice requires.
 */
class PreferencesBackingStoreDenyTest {

	/**
	 * The name Ares reports for the Preferences store when it refuses a call.
	 */
	private static final String PREFERENCES_STORE = "<java.util.prefs backing store>";

	/**
	 * Resets both copies of the Ares advice settings, the one the bootstrap
	 * classloader holds and the one the application classloader holds, so no test
	 * leaves settings behind for the next.
	 *
	 * @throws Exception if a reset method cannot be called
	 */
	private static void resetSettings() throws Exception {
		resetSettingsOf(null);
		resetSettingsOf(Thread.currentThread().getContextClassLoader());
	}

	/**
	 * Resets the copy of the Ares advice settings that this classloader sees, and
	 * does nothing when it has none.
	 *
	 * @param loader the classloader to ask, null for the bootstrap classloader
	 * @throws Exception if the reset method cannot be called
	 */
	private static void resetSettingsOf(ClassLoader loader) throws Exception {
		Class<?> settings;
		try {
			settings = Class.forName(JavaAOPTestCaseSettings.class.getName(), true, loader);
		} catch (ClassNotFoundException noCopyInThisLoader) {
			return;
		}
		Method reset = settings.getDeclaredMethod("reset");
		reset.setAccessible(true);
		reset.invoke(null);
		reset.setAccessible(false);
	}

	/**
	 * Runs the call and expects Ares to refuse this action on the Preferences
	 * store, so a failure in the test setup cannot pass for a denial.
	 *
	 * @param action the action the denial must name
	 * @param call   the call to run
	 */
	private static void assertDeniedAs(String action, Executable call) {
		SecurityException exception = assertThrows(SecurityException.class, call);
		assertTrue(
				exception.getMessage().contains(" " + action + " ")
						&& exception.getMessage().contains(PREFERENCES_STORE),
				() -> "Denial should name the " + action + " action and the Preferences store, but was:\n"
						+ exception.getMessage());
	}

	/**
	 * Resets the settings, then turns on the instrumentation checks for the package
	 * example.student, with an empty list of allowed classes.
	 *
	 * @throws Exception if the settings cannot be set or reset
	 */
	private static void configure() throws Exception {
		resetSettings();
		JavaAOPTestCase.setJavaAdviceSettingValue("aopMode", "INSTRUMENTATION", "ARCH", "INSTRUMENTATION");
		JavaAOPTestCase.setJavaAdviceSettingValue("restrictedPackage", "example.student", "ARCH", "INSTRUMENTATION");
		JavaAOPTestCase.setJavaAdviceSettingValue("allowedListedClasses", new String[0], "ARCH", "INSTRUMENTATION");
	}

	/**
	 * Scopes overwrite to one directory: a Preferences put must still be refused,
	 * with a message naming java.util.prefs.
	 *
	 * @param tempDir the directory overwrite is scoped to
	 * @throws Exception if the settings cannot be set or reset
	 */
	@Test
	void testScopedOverwritePolicyStillBlocksPreferencesPut(@TempDir Path tempDir) throws Exception {
		try {
			configure();
			JavaAOPTestCase.setJavaAdviceSettingValue("pathsAllowedToBeOverwritten",
					new String[] { tempDir.toString() }, "ARCH", "INSTRUMENTATION");
			assertDeniedAs("overwrite", () -> InstrumentationSecurityProbe.checkPreferencesPut("key", "value"));
		} finally {
			resetSettings();
		}
	}

	/**
	 * Scopes read to one directory: a Preferences get must still be refused.
	 *
	 * @param tempDir the directory read is scoped to
	 * @throws Exception if the settings cannot be set or reset
	 */
	@Test
	void testScopedReadPolicyStillBlocksPreferencesGet(@TempDir Path tempDir) throws Exception {
		try {
			configure();
			JavaAOPTestCase.setJavaAdviceSettingValue("pathsAllowedToBeRead", new String[] { tempDir.toString() },
					"ARCH", "INSTRUMENTATION");
			assertDeniedAs("read", () -> InstrumentationSecurityProbe.checkPreferencesGet("key"));
		} finally {
			resetSettings();
		}
	}

	/**
	 * Permits overwrite on all paths: a Preferences put must then pass.
	 *
	 * @throws Exception if the settings cannot be set or reset
	 */
	@Test
	void testWildcardOverwritePolicyAllowsPreferencesPut() throws Exception {
		try {
			configure();
			JavaAOPTestCase.setJavaAdviceSettingValue("pathsAllowedToBeOverwritten", new String[] { "*" }, "ARCH",
					"INSTRUMENTATION");
			assertDoesNotThrow(() -> InstrumentationSecurityProbe.checkPreferencesPut("key", "value"));
		} finally {
			resetSettings();
		}
	}

	/**
	 * Scopes create to one directory: a Preferences node call must still be
	 * refused.
	 *
	 * @param tempDir the directory create is scoped to
	 * @throws Exception if the settings cannot be set or reset
	 */
	@Test
	void testScopedCreatePolicyStillBlocksNodeCreation(@TempDir Path tempDir) throws Exception {
		try {
			configure();
			JavaAOPTestCase.setJavaAdviceSettingValue("pathsAllowedToBeCreated", new String[] { tempDir.toString() },
					"ARCH", "INSTRUMENTATION");
			assertDeniedAs("create", () -> InstrumentationSecurityProbe.checkPreferencesNode("child"));
		} finally {
			resetSettings();
		}
	}

	/**
	 * Permits create on all paths: a Preferences node call must then pass.
	 *
	 * @throws Exception if the settings cannot be set or reset
	 */
	@Test
	void testWildcardCreatePolicyAllowsNodeCreation() throws Exception {
		try {
			configure();
			JavaAOPTestCase.setJavaAdviceSettingValue("pathsAllowedToBeCreated", new String[] { "*" }, "ARCH",
					"INSTRUMENTATION");
			assertDoesNotThrow(() -> InstrumentationSecurityProbe.checkPreferencesNode("child"));
		} finally {
			resetSettings();
		}
	}

	/**
	 * Opens overwrite fully but scopes delete to a directory: removeNode must still
	 * be rejected, proving it is checked against the delete allow-list.
	 *
	 * @param tempDir the directory delete is scoped to
	 * @throws Exception if the settings cannot be reset
	 */
	@Test
	void testRemoveNodeChecksDeletePermissionNotOverwrite(@TempDir Path tempDir) throws Exception {
		try {
			configure();
			JavaAOPTestCase.setJavaAdviceSettingValue("pathsAllowedToBeOverwritten", new String[] { "*" }, "ARCH",
					"INSTRUMENTATION");
			JavaAOPTestCase.setJavaAdviceSettingValue("pathsAllowedToBeDeleted", new String[] { tempDir.toString() },
					"ARCH", "INSTRUMENTATION");
			assertDeniedAs("delete", InstrumentationSecurityProbe::checkPreferencesRemoveNode);
		} finally {
			resetSettings();
		}
	}

	/**
	 * Opens read fully but scopes create to a directory: userNodeForPackage creates
	 * the package node, so a read wildcard must not permit it.
	 *
	 * @param tempDir the directory create is scoped to
	 * @throws Exception if the settings cannot be reset
	 */
	@Test
	void testReadWildcardDoesNotAllowPackageNodeCreation(@TempDir Path tempDir) throws Exception {
		try {
			configure();
			JavaAOPTestCase.setJavaAdviceSettingValue("pathsAllowedToBeRead", new String[] { "*" }, "ARCH",
					"INSTRUMENTATION");
			JavaAOPTestCase.setJavaAdviceSettingValue("pathsAllowedToBeCreated", new String[] { tempDir.toString() },
					"ARCH", "INSTRUMENTATION");
			assertDeniedAs("create", InstrumentationSecurityProbe::checkPreferencesUserNodeForPackage);
		} finally {
			resetSettings();
		}
	}

	/**
	 * Permits create on all paths: a Preferences userNodeForPackage call must then
	 * pass.
	 *
	 * @throws Exception if the settings cannot be set or reset
	 */
	@Test
	void testWildcardCreatePolicyAllowsPackageNode() throws Exception {
		try {
			configure();
			JavaAOPTestCase.setJavaAdviceSettingValue("pathsAllowedToBeCreated", new String[] { "*" }, "ARCH",
					"INSTRUMENTATION");
			assertDoesNotThrow(() -> InstrumentationSecurityProbe.checkPreferencesUserNodeForPackage());
		} finally {
			resetSettings();
		}
	}

	/**
	 * Opens read fully but scopes create to a directory: userRoot can create the
	 * store on first access, so its create check must fail although its read check
	 * passes.
	 *
	 * @param tempDir the directory create is scoped to
	 * @throws Exception if the settings cannot be set or reset
	 */
	@Test
	void testReadWildcardDoesNotAllowRootAccessWithoutCreate(@TempDir Path tempDir) throws Exception {
		try {
			configure();
			JavaAOPTestCase.setJavaAdviceSettingValue("pathsAllowedToBeRead", new String[] { "*" }, "ARCH",
					"INSTRUMENTATION");
			JavaAOPTestCase.setJavaAdviceSettingValue("pathsAllowedToBeCreated", new String[] { tempDir.toString() },
					"ARCH", "INSTRUMENTATION");
			assertDoesNotThrow(() -> InstrumentationSecurityProbe.checkPreferencesUserRoot("read"));
			assertDeniedAs("create", () -> InstrumentationSecurityProbe.checkPreferencesUserRoot("create"));
		} finally {
			resetSettings();
		}
	}

	/**
	 * Permits read and create on all paths: both checks of a Preferences userRoot
	 * call must then pass.
	 *
	 * @throws Exception if the settings cannot be set or reset
	 */
	@Test
	void testReadAndCreateWildcardsAllowRootAccess() throws Exception {
		try {
			configure();
			JavaAOPTestCase.setJavaAdviceSettingValue("pathsAllowedToBeRead", new String[] { "*" }, "ARCH",
					"INSTRUMENTATION");
			JavaAOPTestCase.setJavaAdviceSettingValue("pathsAllowedToBeCreated", new String[] { "*" }, "ARCH",
					"INSTRUMENTATION");
			assertDoesNotThrow(() -> InstrumentationSecurityProbe.checkPreferencesUserRoot("read"));
			assertDoesNotThrow(() -> InstrumentationSecurityProbe.checkPreferencesUserRoot("create"));
		} finally {
			resetSettings();
		}
	}

	/**
	 * Permits delete on all paths: a Preferences removeNode call must then pass.
	 *
	 * @throws Exception if the settings cannot be set or reset
	 */
	@Test
	void testWildcardDeletePolicyAllowsRemoveNode() throws Exception {
		try {
			configure();
			JavaAOPTestCase.setJavaAdviceSettingValue("pathsAllowedToBeDeleted", new String[] { "*" }, "ARCH",
					"INSTRUMENTATION");
			assertDoesNotThrow(() -> InstrumentationSecurityProbe.checkPreferencesRemoveNode());
		} finally {
			resetSettings();
		}
	}

	/**
	 * Uses a key equal to the allowed directory: it must not be taken for the path
	 * being written, since the real store is elsewhere, so the put must still fail.
	 *
	 * @param tempDir the directory overwrite is scoped to, also used as the key
	 * @throws Exception if the settings cannot be reset
	 */
	@Test
	void testKeyMatchingAllowedDirIsNotMistakenForAnAllowedPath(@TempDir Path tempDir) throws Exception {
		try {
			configure();
			JavaAOPTestCase.setJavaAdviceSettingValue("pathsAllowedToBeOverwritten",
					new String[] { tempDir.toString() }, "ARCH", "INSTRUMENTATION");
			assertDeniedAs("overwrite",
					() -> InstrumentationSecurityProbe.checkPreferencesPut(tempDir.toString(), "value"));
		} finally {
			resetSettings();
		}
	}
}
