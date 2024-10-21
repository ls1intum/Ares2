package de.tum.cit.ase.ares.api.architecture;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import de.tum.cit.ase.ares.api.architecture.java.archunit.JavaArchUnitSecurityTestCase;
import de.tum.cit.ase.ares.api.architecture.java.archunit.JavaArchUnitTestCaseSupported;
import de.tum.cit.ase.ares.api.policy.SecurityPolicy.PackagePermission;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class JavaArchUnitSecurityTestCaseTest {

    private JavaArchUnitSecurityTestCase testCase;
    private JavaClasses classes;

    @BeforeEach
    void setUp() {
        classes = new ClassFileImporter().importPackages("com.example");
    }

    @Test
    void testConstructorWithSingleParameter() {
        testCase = new JavaArchUnitSecurityTestCase(JavaArchUnitTestCaseSupported.FILESYSTEM_INTERACTION);
        assertNotNull(testCase);
    }

    @Test
    void testConstructorWithTwoParameters() {
        Set<PackagePermission> packagePermissions = Set.of(new PackagePermission("com.example"));
        testCase = new JavaArchUnitSecurityTestCase(JavaArchUnitTestCaseSupported.PACKAGE_IMPORT, packagePermissions);
        assertNotNull(testCase);
    }

    @Test
    void testWriteArchitectureTestCase() {
        testCase = new JavaArchUnitSecurityTestCase(JavaArchUnitTestCaseSupported.FILESYSTEM_INTERACTION);
        String content = testCase.writeArchitectureTestCase();
        assertNotNull(content);
    }

    @Test
    void testExecuteArchitectureTestCaseFilesystemInteraction() {
        testCase = new JavaArchUnitSecurityTestCase(JavaArchUnitTestCaseSupported.FILESYSTEM_INTERACTION);
        assertDoesNotThrow(() -> testCase.executeArchitectureTestCase(classes));
    }

    @Test
    void testExecuteArchitectureTestCaseNetworkConnection() {
        testCase = new JavaArchUnitSecurityTestCase(JavaArchUnitTestCaseSupported.NETWORK_CONNECTION);
        assertDoesNotThrow(() -> testCase.executeArchitectureTestCase(classes));
    }

    @Test
    void testExecuteArchitectureTestCaseThreadCreation() {
        testCase = new JavaArchUnitSecurityTestCase(JavaArchUnitTestCaseSupported.THREAD_CREATION);
        assertDoesNotThrow(() -> testCase.executeArchitectureTestCase(classes));
    }

    @Test
    void testExecuteArchitectureTestCaseCommandExecution() {
        testCase = new JavaArchUnitSecurityTestCase(JavaArchUnitTestCaseSupported.COMMAND_EXECUTION);
        assertDoesNotThrow(() -> testCase.executeArchitectureTestCase(classes));
    }

    @Test
    void testExecuteArchitectureTestCasePackageImport() {
        Set<PackagePermission> packagePermissions = Set.of(new PackagePermission("com.example"));
        testCase = new JavaArchUnitSecurityTestCase(JavaArchUnitTestCaseSupported.PACKAGE_IMPORT, packagePermissions);
        assertDoesNotThrow(() -> testCase.executeArchitectureTestCase(classes));
    }
}
