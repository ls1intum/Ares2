package de.tum.cit.ase.ares.api.aop.java.instrumentation.advice;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Method;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import de.tum.cit.ase.ares.api.aop.java.JavaAOPTestCase;
import de.tum.cit.ase.ares.api.aop.java.JavaAOPTestCaseSettings;

import example.student.InstrumentationSecurityProbe;

/**
 * Verifies the runtime rule for {@code java.util.prefs.Preferences}: because
 * the backing store is a disk location the student cannot name, a policy that
 * permits filesystem access only within a directory must still reject
 * Preferences, while a policy that permits all paths ("*") allows it. This is
 * the instrumentation counterpart of the AspectJ advice and guards against the
 * directory-scoped bypass. The call is routed through a probe in
 * {@code example.student} so a restricted-package frame is on the stack, as the
 * advice requires.
 */
class PreferencesBackingStoreDenyTest {

	private static void resetSettings() throws Exception {
		Method reset = JavaAOPTestCaseSettings.class.getDeclaredMethod("reset");
		reset.setAccessible(true);
		reset.invoke(null);
		reset.setAccessible(false);
	}

	private static void configure() throws Exception {
		resetSettings();
		JavaAOPTestCase.setJavaAdviceSettingValue("aopMode", "INSTRUMENTATION", "ARCH", "INSTRUMENTATION");
		JavaAOPTestCase.setJavaAdviceSettingValue("restrictedPackage", "example.student", "ARCH", "INSTRUMENTATION");
		JavaAOPTestCase.setJavaAdviceSettingValue("allowedListedClasses", new String[0], "ARCH", "INSTRUMENTATION");
	}

	@Test
	void testScopedOverwritePolicyStillBlocksPreferencesPut(@TempDir Path tempDir) throws Exception {
		try {
			configure();
			JavaAOPTestCase.setJavaAdviceSettingValue("pathsAllowedToBeOverwritten",
					new String[] { tempDir.toString() }, "ARCH", "INSTRUMENTATION");
			SecurityException exception = assertThrows(SecurityException.class,
					() -> InstrumentationSecurityProbe.checkPreferencesPut("key", "value"));
			assertTrue(exception.getMessage().contains("java.util.prefs"),
					() -> "Denial should name the Preferences store, but was:\n" + exception.getMessage());
		} finally {
			resetSettings();
		}
	}

	@Test
	void testScopedReadPolicyStillBlocksPreferencesGet(@TempDir Path tempDir) throws Exception {
		try {
			configure();
			JavaAOPTestCase.setJavaAdviceSettingValue("pathsAllowedToBeRead", new String[] { tempDir.toString() },
					"ARCH", "INSTRUMENTATION");
			assertThrows(SecurityException.class, () -> InstrumentationSecurityProbe.checkPreferencesGet("key"));
		} finally {
			resetSettings();
		}
	}

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

	@Test
	void testScopedCreatePolicyStillBlocksNodeCreation(@TempDir Path tempDir) throws Exception {
		try {
			configure();
			JavaAOPTestCase.setJavaAdviceSettingValue("pathsAllowedToBeCreated", new String[] { tempDir.toString() },
					"ARCH", "INSTRUMENTATION");
			assertThrows(SecurityException.class, () -> InstrumentationSecurityProbe.checkPreferencesNode("child"));
		} finally {
			resetSettings();
		}
	}

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
			assertThrows(SecurityException.class, () -> InstrumentationSecurityProbe.checkPreferencesRemoveNode());
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
			assertThrows(SecurityException.class,
					() -> InstrumentationSecurityProbe.checkPreferencesUserNodeForPackage());
		} finally {
			resetSettings();
		}
	}

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
			assertThrows(SecurityException.class,
					() -> InstrumentationSecurityProbe.checkPreferencesPut(tempDir.toString(), "value"));
		} finally {
			resetSettings();
		}
	}
}
