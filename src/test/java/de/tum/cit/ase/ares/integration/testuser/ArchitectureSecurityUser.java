package de.tum.cit.ase.ares.integration.testuser;

import de.tum.cit.ase.ares.api.Policy;
import de.tum.cit.ase.ares.api.StrictTimeout;
import de.tum.cit.ase.ares.api.jupiter.BenchmarkExtension;
import de.tum.cit.ase.ares.api.jupiter.PublicTest;
import de.tum.cit.ase.ares.api.localization.UseLocale;
import org.junit.jupiter.api.extension.ExtendWith;

@UseLocale("en")
@StrictTimeout(5)
@ExtendWith(BenchmarkExtension.class)
public class ArchitectureSecurityUser {

    // <editor-fold desc="File System Rules">
    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/PolicyEverythingForbidden.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/testuser/subject/architectureTests/fileSystem")
    void testArchUnitFileAccess() {
        // do nothing
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/PolicyEverythingForbidden.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/testuser/subject/architectureTests/fileSystem")
    void testWalaFileAccess() {
        var x = 0;
        // do nothing
    }
    // </editor-fold>

    // <editor-fold desc="Network Rules">
    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/PolicyEverythingForbidden.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/testuser/subject/architectureTests/network")
    void testArchUnitNetworkAccess() {
        // do nothing
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/PolicyEverythingForbidden.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/testuser/subject/architectureTests/network")
    void testWalaNetworkAccess() {
        var x = 0;
        // do nothing
    }
    // </editor-fold>

    // <editor-fold desc="Command Execution Rules">
    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/PolicyEverythingForbidden.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/testuser/subject/architectureTests/commandexecution")
    void testArchUnitCommandExecution() {
        // do nothing
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/PolicyEverythingForbidden.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/testuser/subject/architectureTests/commandexecution")
    void testWalaCommandExecution() {
        var x = 0;
        // do nothing
    }
    // </editor-fold>

    // <editor-fold desc="Thread Creation Rules">
    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/PolicyEverythingForbidden.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/testuser/subject/architectureTests/thread_manipulation")
    void testArchUnitThreadCreation() {
        // do nothing
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/PolicyEverythingForbidden.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/testuser/subject/architectureTests/thread_manipulation")
    void testWalaThreadCreation() {
        var x = 0;
        // do nothing
    }
    // </editor-fold>

    // <editor-fold desc="Package Import Rules">
    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/PolicyEverythingForbidden.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/testuser/subject/architectureTests/packageImport")
    void testArchUnitPackageImport() {
        // do nothing
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/PolicyEverythingForbidden.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/testuser/subject/architectureTests/packageImport")
    void testWalaPackageImport() {
        var x = 0;
        // do nothing
    }
    // </editor-fold>

    // <editor-fold desc="JVMTermination Rules">
    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/PolicyEverythingForbidden.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/testuser/subject/architectureTests/jvmTermination")
    void testArchUnitJVMTermination() {
        // do nothing
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/PolicyEverythingForbidden.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/testuser/subject/architectureTests/jvmTermination")
    void testWalaJVMTermination() {
        var x = 0;
        // do nothing
    }
    // </editor-fold>

    // <editor-fold desc="Reflection Rules">
    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/PolicyEverythingForbidden.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/testuser/subject/architectureTests/reflection")
    void testArchUnitReflection() {
        // do nothing
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/PolicyEverythingForbidden.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/testuser/subject/architectureTests/reflection")
    void testWalaReflection() {
        var x = 0;
        // do nothing
    }
    // </editor-fold>

    // <editor-fold desc="Serialization Rules">
    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/PolicyEverythingForbidden.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/testuser/subject/architectureTests/serialization")
    void testArchUnitSerialization() {
        // do nothing
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/PolicyEverythingForbidden.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/testuser/subject/architectureTests/serialization")
    void testWalaSerialization() {
        var x = 0;
        // do nothing
    }
    // </editor-fold>

    // <editor-fold desc="Classloading Rules">
    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/PolicyEverythingForbidden.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/testuser/subject/architectureTests/classloading")
    void testArchUnitClassloading() {
        // do nothing
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/PolicyEverythingForbidden.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/testuser/subject/architectureTests/classloading")
    void testWalaClassloading() {
        var x = 0;
        // do nothing
    }
    // </editor-fold>

    // <editor-fold desc="Third Party Package Access Rules">

    /**
     * ArchUnit does not support third party package access rules, as it is too slow in that case
     */
//    @PublicTest
//    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/PolicyEverythingForbidden.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/testuser/subject/architectureTests/thirdPartyAccess")
//    void testArchUnitThirdPartyPackageAccess() {
//        // do nothing
//    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/PolicyEverythingForbidden.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/testuser/subject/architectureTests/thirdPartyAccess")
    void testWalaThirdPartyPackageAccess() {
        var x = 0;
        // do nothing
    }
    // </editor-fold>

}
