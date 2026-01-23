package de.tum.cit.ase.ares.integration;

import static de.tum.cit.ase.ares.testutilities.CustomConditions.testFailedWith;

import org.junit.platform.testkit.engine.Events;

import de.tum.cit.ase.ares.integration.testuser.ArchitectureSecurityUser;
import de.tum.cit.ase.ares.testutilities.TestTest;
import de.tum.cit.ase.ares.testutilities.UserBased;
import de.tum.cit.ase.ares.testutilities.UserTestResults;

@UserBased(ArchitectureSecurityUser.class)
public class ArchitectureSecurityTest {

	@UserTestResults
	private static Events tests;

	// <editor-fold desc="File System Rules">
	@TestTest
	void testArchunitFileAccess() {
		tests.assertThatEvents().haveExactly(1, testFailedWith("testArchunitFileAccess", SecurityException.class));
	}

	@TestTest
	void testWalaFileAccess() {
		tests.assertThatEvents().haveExactly(1, testFailedWith("testWalaFileAccess", SecurityException.class));
	}
	// </editor-fold>

	// <editor-fold desc="Network Rules">
	@TestTest
	void testArchunitNetworkAccess() {
		tests.assertThatEvents().haveExactly(1, testFailedWith("testArchunitNetworkAccess", SecurityException.class));
	}

	@TestTest
	void testWalaNetworkAccess() {
		tests.assertThatEvents().haveExactly(1, testFailedWith("testWalaNetworkAccess", SecurityException.class));
	}
	// </editor-fold>

	// <editor-fold desc="Command Execution Rules">
	@TestTest
	void testArchunitCommandExecution() {
		tests.assertThatEvents().haveExactly(1, testFailedWith("testArchunitCommandExecution", SecurityException.class));
	}

	@TestTest
	void testWalaCommandExecution() {
		tests.assertThatEvents().haveExactly(1, testFailedWith("testWalaCommandExecution", SecurityException.class));
	}
	// </editor-fold>

	// <editor-fold desc="Thread Creation Rules">
	@TestTest
	void testArchunitThreadCreation() {
		tests.assertThatEvents().haveExactly(1, testFailedWith("testArchunitThreadCreation", SecurityException.class));
	}

	@TestTest
	void testWalaThreadCreation() {
		tests.assertThatEvents().haveExactly(1, testFailedWith("testWalaThreadCreation", SecurityException.class));
	}
	// </editor-fold>

	// <editor-fold desc="Package Import Rules">
	@TestTest
	void testArchunitPackageImport() {
		tests.assertThatEvents().haveExactly(1, testFailedWith("testArchunitPackageImport", SecurityException.class));
	}

	@TestTest
	void testWalaPackageImport() {
		tests.assertThatEvents().haveExactly(1, testFailedWith("testWalaPackageImport", SecurityException.class));
	}
	// </editor-fold>

	// <editor-fold desc="JVMTermination Rules">
	@TestTest
	void testArchunitJVMTermination() {
		tests.assertThatEvents().haveExactly(1, testFailedWith("testArchunitJVMTermination", SecurityException.class));
	}

	@TestTest
	void testWalaJVMTermination() {
		tests.assertThatEvents().haveExactly(1, testFailedWith("testWalaJVMTermination", SecurityException.class));
	}
	// </editor-fold>

	// <editor-fold desc="Reflection Rules">
	@TestTest
	void testArchunitReflection() {
		tests.assertThatEvents().haveExactly(1, testFailedWith("testArchunitReflection", SecurityException.class));
	}

	@TestTest
	void testWalaReflection() {
		tests.assertThatEvents().haveExactly(1, testFailedWith("testWalaReflection", SecurityException.class));
	}
	// </editor-fold>

	// <editor-fold desc="Serialization Rules">
	@TestTest
	void testArchunitSerialization() {
		tests.assertThatEvents().haveExactly(1, testFailedWith("testArchunitSerialization", SecurityException.class));
	}

	@TestTest
	void testWalaSerialization() {
		tests.assertThatEvents().haveExactly(1, testFailedWith("testWalaSerialization", SecurityException.class));
	}
	// </editor-fold>

	// <editor-fold desc="Classloading Rules">
	@TestTest
	void testArchunitClassloading() {
		tests.assertThatEvents().haveExactly(1, testFailedWith("testArchunitClassloading", SecurityException.class));
	}

	@TestTest
	void testWalaClassloading() {
		tests.assertThatEvents().haveExactly(1, testFailedWith("testWalaClassloading", SecurityException.class));
	}
	// </editor-fold>

	// <editor-fold desc="Third Party Package Access Rules">

	/**
	 * This test is disabled because it is not possible to test the access to
	 * third-party packages with Archunit
	 */
	// @TestTest
	// void testArchunitThirdPartyPackageAccess() {
	// tests.assertThatEvents().haveExactly(1,
	// testFailedWith("testArchunitThirdPartyPackageAccess",
	// SecurityException.class));
	// }

	@TestTest
	void testWalaThirdPartyPackageAccess() {
		tests.assertThatEvents().haveExactly(1, testFailedWith("testWalaThirdPartyPackageAccess", SecurityException.class));
	}
	// </editor-fold>
}
