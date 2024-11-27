package de.tum.cit.ase.ares.integration;

import de.tum.cit.ase.ares.integration.testuser.ArchitectureSecurityUser;
import de.tum.cit.ase.ares.testutilities.TestTest;
import de.tum.cit.ase.ares.testutilities.UserBased;
import de.tum.cit.ase.ares.testutilities.UserTestResults;
import org.junit.platform.testkit.engine.Events;

import static de.tum.cit.ase.ares.testutilities.CustomConditions.testFailedWith;

@UserBased(ArchitectureSecurityUser.class)
public class ArchitectureSecurityTest {

    @UserTestResults
    private static Events tests;

    // <editor-fold desc="File System Rules">
    @TestTest
    void testArchUnitFileAccess() {
        tests.assertThatEvents().haveExactly(1,
                testFailedWith("testArchUnitFileAccess", SecurityException.class));
    }

    @TestTest
    void testWalaFileAccess() {
        tests.assertThatEvents().haveExactly(1,
                testFailedWith("testWalaFileAccess", SecurityException.class));
    }
    // </editor-fold>

    // <editor-fold desc="Network Rules">
    @TestTest
    void testArchUnitNetworkAccess() {
        tests.assertThatEvents().haveExactly(1,
                testFailedWith("testArchUnitNetworkAccess", SecurityException.class));
    }

    @TestTest
    void testWalaNetworkAccess() {
        tests.assertThatEvents().haveExactly(1,
                testFailedWith("testWalaNetworkAccess", SecurityException.class));
    }
    // </editor-fold>

    // <editor-fold desc="Command Execution Rules">
    @TestTest
    void testArchUnitCommandExecution() {
        tests.assertThatEvents().haveExactly(1,
                testFailedWith("testArchUnitCommandExecution", SecurityException.class));
    }

    @TestTest
    void testWalaCommandExecution() {
        tests.assertThatEvents().haveExactly(1,
                testFailedWith("testWalaCommandExecution", SecurityException.class));
    }
    // </editor-fold>

    // <editor-fold desc="Thread Creation Rules">
    @TestTest
    void testArchUnitThreadCreation() {
        tests.assertThatEvents().haveExactly(1,
                testFailedWith("testArchUnitThreadCreation", SecurityException.class));
    }

    @TestTest
    void testWalaThreadCreation() {
        tests.assertThatEvents().haveExactly(1,
                testFailedWith("testWaLaThreadCreation", SecurityException.class));
    }
    // </editor-fold>

    // <editor-fold desc="Package Import Rules">
    @TestTest
    void testArchUnitPackageImport() {
        tests.assertThatEvents().haveExactly(1,
                testFailedWith("testArchUnitPackageImport", SecurityException.class));
    }

    @TestTest
    void testWalaPackageImport() {
        tests.assertThatEvents().haveExactly(1,
                testFailedWith("testWalaPackageImport", SecurityException.class));
    }
    // </editor-fold>

    // <editor-fold desc="JVMTermination Rules">
    @TestTest
    void testArchUnitJVMTermination() {
        tests.assertThatEvents().haveExactly(1,
                testFailedWith("testArchUnitJVMTermination", SecurityException.class));
    }

    @TestTest
    void testWalaJVMTermination() {
        tests.assertThatEvents().haveExactly(1,
                testFailedWith("testWalaJVMTermination", SecurityException.class));
    }
    // </editor-fold>

    // <editor-fold desc="Reflection Rules">
    @TestTest
    void testArchUnitReflection() {
        tests.assertThatEvents().haveExactly(1,
                testFailedWith("testArchUnitReflection", SecurityException.class));
    }

    @TestTest
    void testWalaReflection() {
        tests.assertThatEvents().haveExactly(1,
                testFailedWith("testWalaReflection", SecurityException.class));
    }
    // </editor-fold>

    // <editor-fold desc="Serialization Rules">
    @TestTest
    void testArchUnitSerialization() {
        tests.assertThatEvents().haveExactly(1,
                testFailedWith("testArchUnitSerialization", SecurityException.class));
    }

    @TestTest
    void testWalaSerialization() {
        tests.assertThatEvents().haveExactly(1,
                testFailedWith("testWalaSerialization", SecurityException.class));
    }
    // </editor-fold>

    // <editor-fold desc="Classloading Rules">
    @TestTest
    void testArchUnitClassloading() {
        tests.assertThatEvents().haveExactly(1,
                testFailedWith("testArchUnitClassloading", SecurityException.class));
    }

    @TestTest
    void testWalaClassloading() {
        tests.assertThatEvents().haveExactly(1,
                testFailedWith("testWalaClassloading", SecurityException.class));
    }
    // </editor-fold>

    // <editor-fold desc="Third Party Package Access Rules">
    @TestTest
    void testArchUnitThirdPartyPackageAccess() {
        tests.assertThatEvents().haveExactly(1,
                testFailedWith("testArchUnitThirdPartyPackageAccess", SecurityException.class));
    }

    @TestTest
    void testWalaThirdPartyPackageAccess() {
        tests.assertThatEvents().haveExactly(1,
                testFailedWith("testWalaThirdPartyPackageAccess", SecurityException.class));
    }
    // </editor-fold>
}
