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
 * backing store is a fixed location no directory-scoped rule can name. Every
 * policy used grants some filesystem access, so the reach check is off and the
 * runtime check (AspectJ or instrumentation) is what must reject these calls,
 * across all four modes.
 */
class PreferencesAccessTest extends SystemAccessTest {

	/**
	 * The folder of compiled classes, below the build output, that holds the
	 * student code under test.
	 */
	private static final String WITHIN_PATH = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/preferences";

	/**
	 * Policy permitting read and overwrite on every path but no creation, in the
	 * ArchUnit and AspectJ combination.
	 */
	private static final String ARCHUNIT_ASPECTJ_POLICY_ALL_PATHS_ALLOWED_READ_OVERWRITE = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/aspectj/PolicyAllPathsAllowedReadOverwrite.yaml";

	/**
	 * Policy permitting read and overwrite on every path but no creation, in the
	 * ArchUnit and instrumentation combination.
	 */
	private static final String ARCHUNIT_INSTRUMENTATION_POLICY_ALL_PATHS_ALLOWED_READ_OVERWRITE = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/instrumentation/PolicyAllPathsAllowedReadOverwrite.yaml";

	/**
	 * Policy permitting read and overwrite on every path but no creation, in the
	 * WALA and AspectJ combination.
	 */
	private static final String WALA_ASPECTJ_POLICY_ALL_PATHS_ALLOWED_READ_OVERWRITE = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/aspectj/PolicyAllPathsAllowedReadOverwrite.yaml";

	/**
	 * Policy permitting read and overwrite on every path but no creation, in the
	 * WALA and instrumentation combination.
	 */
	private static final String WALA_INSTRUMENTATION_POLICY_ALL_PATHS_ALLOWED_READ_OVERWRITE = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/instrumentation/PolicyAllPathsAllowedReadOverwrite.yaml";

	/**
	 * Runs the call and expects a SecurityException whose message names
	 * java.util.prefs.
	 *
	 * @param executable the student call to run
	 */
	private static void assertPreferencesDenied(Executable executable) {
		SecurityException exception = assertThrows(SecurityException.class, executable);
		assertTrue(exception.getMessage() != null && exception.getMessage().contains("java.util.prefs"),
				() -> "Denial should name the Preferences store, but was:\n" + exception.getMessage());
	}

	/**
	 * Runs the import and expects a SecurityException that names java.util.prefs
	 * and the create action, so the refusal comes from the missing creation
	 * permission rather than from overwrite, which the policy permits.
	 *
	 * @param executable the student import to run
	 */
	private static void assertPreferencesImportDeniedAsCreate(Executable executable) {
		SecurityException exception = assertThrows(SecurityException.class, executable);
		assertTrue(
				exception.getMessage() != null && exception.getMessage().contains("java.util.prefs")
						&& exception.getMessage().contains(" create "),
				() -> "Denial should name the Preferences store and the create action, but was:\n"
						+ exception.getMessage());
	}

	// <editor-fold desc="Preferences.userRoot (read)">
	/**
	 * Calls Preferences.userRoot() as student code under a policy that permits one
	 * path only, in the ArchUnit and AspectJ combination, and expects Ares to
	 * refuse it with a message naming java.util.prefs.
	 */
	@PublicTest
	@Policy(value = ARCHUNIT_ASPECTJ_POLICY_ONE_PATH_ALLOWED_OVERWRITE, withinPath = WITHIN_PATH)
	void test_accessViaPreferencesUserRootMavenArchunitAspectJ() {
		assertPreferencesDenied(PreferencesMain::accessFileSystemViaPreferencesUserRoot);
	}

	/**
	 * Calls Preferences.userRoot() as student code under a policy that permits one
	 * path only, in the ArchUnit and instrumentation combination, and expects Ares
	 * to refuse it with a message naming java.util.prefs.
	 */
	@PublicTest
	@Policy(value = ARCHUNIT_INSTRUMENTATION_POLICY_ONE_PATH_ALLOWED_OVERWRITE, withinPath = WITHIN_PATH)
	void test_accessViaPreferencesUserRootMavenArchunitInstrumentation() {
		assertPreferencesDenied(PreferencesMain::accessFileSystemViaPreferencesUserRoot);
	}

	/**
	 * Calls Preferences.userRoot() as student code under a policy that permits one
	 * path only, in the WALA and AspectJ combination, and expects Ares to refuse it
	 * with a message naming java.util.prefs.
	 */
	@PublicTest
	@Policy(value = WALA_ASPECTJ_POLICY_ONE_PATH_ALLOWED_OVERWRITE, withinPath = WITHIN_PATH)
	void test_accessViaPreferencesUserRootMavenWalaAspectJ() {
		assertPreferencesDenied(PreferencesMain::accessFileSystemViaPreferencesUserRoot);
	}

	/**
	 * Calls Preferences.userRoot() as student code under a policy that permits one
	 * path only, in the WALA and instrumentation combination, and expects Ares to
	 * refuse it with a message naming java.util.prefs.
	 */
	@PublicTest
	@Policy(value = WALA_INSTRUMENTATION_POLICY_ONE_PATH_ALLOWED_OVERWRITE, withinPath = WITHIN_PATH)
	void test_accessViaPreferencesUserRootMavenWalaInstrumentation() {
		assertPreferencesDenied(PreferencesMain::accessFileSystemViaPreferencesUserRoot);
	}
	// </editor-fold>

	// <editor-fold desc="Preferences.userNodeForPackage (create)">
	/**
	 * Calls Preferences.userNodeForPackage as student code under a policy that
	 * permits one path only, in the ArchUnit and AspectJ combination, and expects
	 * Ares to refuse it with a message naming java.util.prefs.
	 */
	@PublicTest
	@Policy(value = ARCHUNIT_ASPECTJ_POLICY_ONE_PATH_ALLOWED_OVERWRITE, withinPath = WITHIN_PATH)
	void test_accessViaPreferencesUserNodeForPackageMavenArchunitAspectJ() {
		assertPreferencesDenied(PreferencesMain::accessFileSystemViaPreferencesUserNodeForPackage);
	}

	/**
	 * Calls Preferences.userNodeForPackage as student code under a policy that
	 * permits one path only, in the ArchUnit and instrumentation combination, and
	 * expects Ares to refuse it with a message naming java.util.prefs.
	 */
	@PublicTest
	@Policy(value = ARCHUNIT_INSTRUMENTATION_POLICY_ONE_PATH_ALLOWED_OVERWRITE, withinPath = WITHIN_PATH)
	void test_accessViaPreferencesUserNodeForPackageMavenArchunitInstrumentation() {
		assertPreferencesDenied(PreferencesMain::accessFileSystemViaPreferencesUserNodeForPackage);
	}

	/**
	 * Calls Preferences.userNodeForPackage as student code under a policy that
	 * permits one path only, in the WALA and AspectJ combination, and expects Ares
	 * to refuse it with a message naming java.util.prefs.
	 */
	@PublicTest
	@Policy(value = WALA_ASPECTJ_POLICY_ONE_PATH_ALLOWED_OVERWRITE, withinPath = WITHIN_PATH)
	void test_accessViaPreferencesUserNodeForPackageMavenWalaAspectJ() {
		assertPreferencesDenied(PreferencesMain::accessFileSystemViaPreferencesUserNodeForPackage);
	}

	/**
	 * Calls Preferences.userNodeForPackage as student code under a policy that
	 * permits one path only, in the WALA and instrumentation combination, and
	 * expects Ares to refuse it with a message naming java.util.prefs.
	 */
	@PublicTest
	@Policy(value = WALA_INSTRUMENTATION_POLICY_ONE_PATH_ALLOWED_OVERWRITE, withinPath = WITHIN_PATH)
	void test_accessViaPreferencesUserNodeForPackageMavenWalaInstrumentation() {
		assertPreferencesDenied(PreferencesMain::accessFileSystemViaPreferencesUserNodeForPackage);
	}
	// </editor-fold>

	// <editor-fold desc="Preferences.importPreferences (create)">
	/**
	 * Imports a missing node as student code under a policy that permits read and
	 * overwrite everywhere but no creation, in the ArchUnit and AspectJ
	 * combination, and expects Ares to refuse it as a create.
	 */
	@PublicTest
	@Policy(value = ARCHUNIT_ASPECTJ_POLICY_ALL_PATHS_ALLOWED_READ_OVERWRITE, withinPath = WITHIN_PATH)
	void test_accessViaPreferencesImportMavenArchunitAspectJ() {
		assertPreferencesImportDeniedAsCreate(PreferencesMain::accessFileSystemViaPreferencesImport);
	}

	/**
	 * Imports a missing node as student code under a policy that permits read and
	 * overwrite everywhere but no creation, in the ArchUnit and instrumentation
	 * combination, and expects Ares to refuse it as a create.
	 */
	@PublicTest
	@Policy(value = ARCHUNIT_INSTRUMENTATION_POLICY_ALL_PATHS_ALLOWED_READ_OVERWRITE, withinPath = WITHIN_PATH)
	void test_accessViaPreferencesImportMavenArchunitInstrumentation() {
		assertPreferencesImportDeniedAsCreate(PreferencesMain::accessFileSystemViaPreferencesImport);
	}

	/**
	 * Imports a missing node as student code under a policy that permits read and
	 * overwrite everywhere but no creation, in the WALA and AspectJ combination,
	 * and expects Ares to refuse it as a create.
	 */
	@PublicTest
	@Policy(value = WALA_ASPECTJ_POLICY_ALL_PATHS_ALLOWED_READ_OVERWRITE, withinPath = WITHIN_PATH)
	void test_accessViaPreferencesImportMavenWalaAspectJ() {
		assertPreferencesImportDeniedAsCreate(PreferencesMain::accessFileSystemViaPreferencesImport);
	}

	/**
	 * Imports a missing node as student code under a policy that permits read and
	 * overwrite everywhere but no creation, in the WALA and instrumentation
	 * combination, and expects Ares to refuse it as a create.
	 */
	@PublicTest
	@Policy(value = WALA_INSTRUMENTATION_POLICY_ALL_PATHS_ALLOWED_READ_OVERWRITE, withinPath = WITHIN_PATH)
	void test_accessViaPreferencesImportMavenWalaInstrumentation() {
		assertPreferencesImportDeniedAsCreate(PreferencesMain::accessFileSystemViaPreferencesImport);
	}
	// </editor-fold>
}
