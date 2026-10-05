package de.tum.cit.ase.ares.integration.aop.forbidden;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.function.Executable;

import de.tum.cit.ase.ares.api.Policy;
import de.tum.cit.ase.ares.api.jupiter.PublicTest;
import de.tum.cit.ase.ares.integration.aop.forbidden.subject.preferences.PreferencesMain;

/**
 * Real {@code java.util.prefs.Preferences} calls must be rejected under a
 * policy that permits filesystem access only within a directory, because the
 * backing store is a fixed location no directory-scoped rule can name. The
 * policy used grants overwrite and create for one path only, so the reach check
 * is off and the runtime check (AspectJ or instrumentation) is what must reject
 * these calls, across all four modes.
 */
class PreferencesAccessTest extends SystemAccessTest {

	private static final String WITHIN_PATH = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/preferences";

	private static void assertPreferencesDenied(Executable executable) {
		SecurityException exception = assertThrows(SecurityException.class, executable);
		assertTrue(exception.getMessage() != null && exception.getMessage().contains("java.util.prefs"),
				() -> "Denial should name the Preferences store, but was:\n" + exception.getMessage());
	}

	// <editor-fold desc="Preferences.userRoot (read)">
	@PublicTest
	@Policy(value = ARCHUNIT_ASPECTJ_POLICY_ONE_PATH_ALLOWED_OVERWRITE, withinPath = WITHIN_PATH)
	void test_accessViaPreferencesUserRootMavenArchunitAspectJ() {
		assertPreferencesDenied(PreferencesMain::accessFileSystemViaPreferencesUserRoot);
	}

	@PublicTest
	@Policy(value = ARCHUNIT_INSTRUMENTATION_POLICY_ONE_PATH_ALLOWED_OVERWRITE, withinPath = WITHIN_PATH)
	void test_accessViaPreferencesUserRootMavenArchunitInstrumentation() {
		assertPreferencesDenied(PreferencesMain::accessFileSystemViaPreferencesUserRoot);
	}

	@PublicTest
	@Policy(value = WALA_ASPECTJ_POLICY_ONE_PATH_ALLOWED_OVERWRITE, withinPath = WITHIN_PATH)
	void test_accessViaPreferencesUserRootMavenWalaAspectJ() {
		assertPreferencesDenied(PreferencesMain::accessFileSystemViaPreferencesUserRoot);
	}

	@PublicTest
	@Policy(value = WALA_INSTRUMENTATION_POLICY_ONE_PATH_ALLOWED_OVERWRITE, withinPath = WITHIN_PATH)
	void test_accessViaPreferencesUserRootMavenWalaInstrumentation() {
		assertPreferencesDenied(PreferencesMain::accessFileSystemViaPreferencesUserRoot);
	}
	// </editor-fold>

	// <editor-fold desc="Preferences.userNodeForPackage (create)">
	@PublicTest
	@Policy(value = ARCHUNIT_ASPECTJ_POLICY_ONE_PATH_ALLOWED_OVERWRITE, withinPath = WITHIN_PATH)
	void test_accessViaPreferencesUserNodeForPackageMavenArchunitAspectJ() {
		assertPreferencesDenied(PreferencesMain::accessFileSystemViaPreferencesUserNodeForPackage);
	}

	@PublicTest
	@Policy(value = ARCHUNIT_INSTRUMENTATION_POLICY_ONE_PATH_ALLOWED_OVERWRITE, withinPath = WITHIN_PATH)
	void test_accessViaPreferencesUserNodeForPackageMavenArchunitInstrumentation() {
		assertPreferencesDenied(PreferencesMain::accessFileSystemViaPreferencesUserNodeForPackage);
	}

	@PublicTest
	@Policy(value = WALA_ASPECTJ_POLICY_ONE_PATH_ALLOWED_OVERWRITE, withinPath = WITHIN_PATH)
	void test_accessViaPreferencesUserNodeForPackageMavenWalaAspectJ() {
		assertPreferencesDenied(PreferencesMain::accessFileSystemViaPreferencesUserNodeForPackage);
	}

	@PublicTest
	@Policy(value = WALA_INSTRUMENTATION_POLICY_ONE_PATH_ALLOWED_OVERWRITE, withinPath = WITHIN_PATH)
	void test_accessViaPreferencesUserNodeForPackageMavenWalaInstrumentation() {
		assertPreferencesDenied(PreferencesMain::accessFileSystemViaPreferencesUserNodeForPackage);
	}
	// </editor-fold>
}
