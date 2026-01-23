package de.tum.cit.ase.ares.integration.testuser;

import org.junit.jupiter.api.extension.ExtendWith;

import de.tum.cit.ase.ares.api.Policy;
import de.tum.cit.ase.ares.api.StrictTimeout;
import de.tum.cit.ase.ares.api.jupiter.BenchmarkExtension;
import de.tum.cit.ase.ares.api.jupiter.PublicTest;
import de.tum.cit.ase.ares.api.localization.UseLocale;

@UseLocale("en")
@StrictTimeout(5)
@ExtendWith(BenchmarkExtension.class)
public class ArchitectureSecurityUser {

	// <editor-fold desc="File System Rules">
	@PublicTest
	@Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/PolicyEverythingForbidden.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/testuser/subject/architectureTests/fileSystem")
	void testArchunitFileAccess() {
		// do nothing
	}

	@PublicTest
	@Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/PolicyEverythingForbidden.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/testuser/subject/architectureTests/fileSystem")
	void testWalaFileAccess() {
		// do nothing
	}
	// </editor-fold>

	// <editor-fold desc="Network Rules">
	@PublicTest
	@Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/PolicyEverythingForbidden.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/testuser/subject/architectureTests/network")
	void testArchunitNetworkAccess() {
		// do nothing
	}

	@PublicTest
	@Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/PolicyEverythingForbidden.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/testuser/subject/architectureTests/network")
	void testWalaNetworkAccess() {
		// do nothing
	}
	// </editor-fold>

	// <editor-fold desc="Command Execution Rules">
	@PublicTest
	@Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/PolicyEverythingForbidden.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/testuser/subject/architectureTests/commandexecution")
	void testArchunitCommandExecution() {
		// do nothing
	}

	@PublicTest
	@Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/PolicyEverythingForbidden.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/testuser/subject/architectureTests/commandexecution")
	void testWalaCommandExecution() {
		// do nothing
	}
	// </editor-fold>

	// <editor-fold desc="Thread Creation Rules">
	@PublicTest
	@Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/PolicyEverythingForbidden.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/testuser/subject/architectureTests/thread_manipulation")
	void testArchunitThreadCreation() {
		// do nothing
	}

	@PublicTest
	@Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/PolicyEverythingForbidden.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/testuser/subject/architectureTests/thread_manipulation")
	void testWalaThreadCreation() {
		// do nothing
	}
	// </editor-fold>

	// <editor-fold desc="Package Import Rules">
	@PublicTest
	@Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/PolicyEverythingForbidden.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/testuser/subject/architectureTests/packageImport")
	void testArchunitPackageImport() {
		// do nothing
	}

	@PublicTest
	@Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/PolicyEverythingForbidden.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/testuser/subject/architectureTests/packageImport")
	void testWalaPackageImport() {
		// do nothing
	}
	// </editor-fold>

	// <editor-fold desc="JVMTermination Rules">
	@PublicTest
	@Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/PolicyEverythingForbidden.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/testuser/subject/architectureTests/jvmTermination")
	void testArchunitJVMTermination() {
		// do nothing
	}

	@PublicTest
	@Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/PolicyEverythingForbidden.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/testuser/subject/architectureTests/jvmTermination")
	void testWalaJVMTermination() {
		// do nothing
	}
	// </editor-fold>

	// <editor-fold desc="Reflection Rules">
	@PublicTest
	@Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/PolicyEverythingForbidden.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/testuser/subject/architectureTests/reflection")
	void testArchunitReflection() {
		// do nothing
	}

	@PublicTest
	@Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/PolicyEverythingForbidden.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/testuser/subject/architectureTests/reflection")
	void testWalaReflection() {
		// do nothing
	}
	// </editor-fold>

	// <editor-fold desc="Serialization Rules">
	@PublicTest
	@Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/PolicyEverythingForbidden.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/testuser/subject/architectureTests/serialization")
	void testArchunitSerialization() {
		// do nothing
	}

	@PublicTest
	@Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/PolicyEverythingForbidden.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/testuser/subject/architectureTests/serialization")
	void testWalaSerialization() {
		// do nothing
	}
	// </editor-fold>

	// <editor-fold desc="Classloading Rules">
	@PublicTest
	@Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/PolicyEverythingForbidden.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/testuser/subject/architectureTests/classloading")
	void testArchunitClassloading() {
		// do nothing
	}

	@PublicTest
	@Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/PolicyEverythingForbidden.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/testuser/subject/architectureTests/classloading")
	void testWalaClassloading() {
		// do nothing
	}
	// </editor-fold>

	// <editor-fold desc="Third Party Package Access Rules">

	/**
	 * Archunit does not support third party package access rules, as it is too slow
	 * in that case
	 */
	// @PublicTest
	// @Policy(value =
	// "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/PolicyEverythingForbidden.yaml",
	// withinPath =
	// "test-classes/de/tum/cit/ase/ares/integration/testuser/subject/architectureTests/thirdPartyAccess")
	// void testArchunitThirdPartyPackageAccess() {
	// // do nothing
	// }

	@PublicTest
	@Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/PolicyEverythingForbidden.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/testuser/subject/architectureTests/thirdPartyAccess")
	void testWalaThirdPartyPackageAccess() {
		// do nothing
	}
	// </editor-fold>
}
