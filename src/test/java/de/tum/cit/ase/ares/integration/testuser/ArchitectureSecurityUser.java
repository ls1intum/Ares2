package de.tum.cit.ase.ares.integration.testuser;

import de.tum.cit.ase.ares.api.Policy;
import de.tum.cit.ase.ares.api.StrictTimeout;
import de.tum.cit.ase.ares.api.jupiter.PublicTest;
import de.tum.cit.ase.ares.api.localization.UseLocale;

@UseLocale("en")
@StrictTimeout(5)
public class ArchitectureSecurityUser {

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/EverythingForbiddenPolicy.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/testuser/subject/architectureTests/classloading")
    void testArchUnitClassloading() {
        // do nothing
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/EverythingForbiddenPolicyWala.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/testuser/subject/architectureTests/classloading")
    void testWalaClassloading() {
        // do nothing
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/EverythingForbiddenPolicy.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/testuser/subject/architectureTests/commandexecution")
    void testArchUnitCommandExecution() {
        // do nothing
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/EverythingForbiddenPolicyWala.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/testuser/subject/architectureTests/commandexecution")
    void testWalaCommandExecution() {
        // do nothing
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/EverythingForbiddenPolicy.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/testuser/subject/architectureTests/fileSystem")
    void testArchUnitFileAccess() {
        // do nothing
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/EverythingForbiddenPolicyWala.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/testuser/subject/architectureTests/fileSystem")
    void testWalaFileAccess() {
        // do nothing
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/EverythingForbiddenPolicy.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/testuser/subject/architectureTests/network")
    void testArchUnitNetworkAccess() {
        // do nothing
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/EverythingForbiddenPolicyWala.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/testuser/subject/architectureTests/network")
    void testWalaNetworkAccess() {
        // do nothing
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/EverythingForbiddenPolicy.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/testuser/subject/architectureTests/reflection")
    void testArchUnitReflection() {
        // do nothing
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/EverythingForbiddenPolicyWala.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/testuser/subject/architectureTests/reflection")
    void testWalaReflection() {
        // do nothing
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/EverythingForbiddenPolicy.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/testuser/subject/architectureTests/serialization")
    void testArchUnitSerialization() {
        // do nothing
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/EverythingForbiddenPolicyWala.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/testuser/subject/architectureTests/serialization")
    void testWalaSerialization() {
        // do nothing
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/EverythingForbiddenPolicy.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/testuser/subject/architectureTests/thirdpartypackage")
    void testArchUnitThirdPartyPackageAccess() {
        // do nothing
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/EverythingForbiddenPolicyWala.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/testuser/subject/architectureTests/thirdpartypackage")
    void testWalaThirdPartyPackageAccess() {
        // do nothing
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/EverythingForbiddenPolicy.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/testuser/subject/architectureTests/jvmTermination")
    void testArchUnitJVMTermination() {
        // do nothing
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/EverythingForbiddenPolicyWala.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/testuser/subject/architectureTests/jvmTermination")
    void testWalaJVMTermination() {
        // do nothing
    }

}
