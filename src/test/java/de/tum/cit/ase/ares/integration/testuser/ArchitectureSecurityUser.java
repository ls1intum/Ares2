package de.tum.cit.ase.ares.integration.testuser;

import de.tum.cit.ase.ares.api.Policy;
import de.tum.cit.ase.ares.api.StrictTimeout;
import de.tum.cit.ase.ares.api.jupiter.PublicTest;
import de.tum.cit.ase.ares.api.localization.UseLocale;

@UseLocale("en")
@StrictTimeout(5)
public class ArchitectureSecurityUser {

    @PublicTest
    void testArchUnitClassloading() {
        // do nothing
    }

    @PublicTest
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
    void testArchUnitFileAccess() {
        // do nothing
    }

    @PublicTest
    void testWalaFileAccess() {
        // do nothing
    }

    @PublicTest
    void testArchUnitNetworkAccess() {
        // do nothing
    }

    @PublicTest
    void testWalaNetworkAccess() {
        // do nothing
    }

    @PublicTest
    void testArchUnitReflection() {
        // do nothing
    }

    @PublicTest
    void testWalaReflection() {
        // do nothing
    }

    @PublicTest
    void testArchUnitSerialization() {
        // do nothing
    }

    @PublicTest
    void testWalaSerialization() {
        // do nothing
    }

    @PublicTest
    void testArchUnitThirdPartyPackageAccess() {
        // do nothing
    }

    @PublicTest
    void testWalaThirdPartyPackageAccess() {
        // do nothing
    }

    @PublicTest
    void testArchUnitJVMTermination() {
        // do nothing
    }

    @PublicTest
    void testWalaJVMTermination() {
        // do nothing
    }

}
