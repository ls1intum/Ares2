package de.tum.cit.ase.ares.api.architecture;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import de.tum.cit.ase.ares.api.architecture.java.archunit.JavaArchUnitSecurityTestCase;
import de.tum.cit.ase.ares.api.architecture.java.JavaArchitectureTestCaseSupported;
import de.tum.cit.ase.ares.api.policy.policySubComponents.PackagePermission;
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
        testCase = JavaArchUnitSecurityTestCase
                .builder()
                .javaArchitectureTestCaseSupported(JavaArchitectureTestCaseSupported.FILESYSTEM_INTERACTION)
                .build();
        assertNotNull(testCase);
    }

    @Test
    void testConstructorWithTwoParameters() {
        Set<PackagePermission> packagePermissions = Set.of(new PackagePermission("com.example"));
        testCase = JavaArchUnitSecurityTestCase
                        .builder()
                        .javaArchitectureTestCaseSupported(JavaArchitectureTestCaseSupported.PACKAGE_IMPORT)
                        .allowedPackages(packagePermissions)
                        .build();
        assertNotNull(testCase);
    }

    @Test
    void testWriteArchitectureTestCase() {
        testCase = JavaArchUnitSecurityTestCase
                .builder()
                .javaArchitectureTestCaseSupported(JavaArchitectureTestCaseSupported.FILESYSTEM_INTERACTION)
                .build();
        String content = testCase.writeArchitectureTestCase();
        assertNotNull(content);
    }

    @Test
    void testExecuteArchitectureTestCaseFilesystemInteraction() {
        testCase = JavaArchUnitSecurityTestCase
                .builder()
                .javaArchitectureTestCaseSupported(JavaArchitectureTestCaseSupported.FILESYSTEM_INTERACTION)
                .javaClasses(classes)
                .build();
        assertDoesNotThrow(() -> testCase.executeArchitectureTestCase());
    }

    @Test
    void testExecuteArchitectureTestCaseNetworkConnection() {
        testCase = JavaArchUnitSecurityTestCase
                .builder()
                .javaArchitectureTestCaseSupported(JavaArchitectureTestCaseSupported.NETWORK_CONNECTION)
                .javaClasses(classes)
                .build();
        assertDoesNotThrow(() -> testCase.executeArchitectureTestCase());
    }

    @Test
    void testExecuteArchitectureTestCaseThreadCreation() {
        testCase = JavaArchUnitSecurityTestCase
                .builder()
                .javaArchitectureTestCaseSupported(JavaArchitectureTestCaseSupported.THREAD_CREATION)
                .javaClasses(classes)
                .build();
        assertDoesNotThrow(() -> testCase.executeArchitectureTestCase());
    }

    @Test
    void testExecuteArchitectureTestCaseCommandExecution() {
        testCase = JavaArchUnitSecurityTestCase
                .builder()
                .javaArchitectureTestCaseSupported(JavaArchitectureTestCaseSupported.COMMAND_EXECUTION)
                .javaClasses(classes)
                .build();
        assertDoesNotThrow(() -> testCase.executeArchitectureTestCase());
    }

    @Test
    void testExecuteArchitectureTestCasePackageImport() {
        Set<PackagePermission> packagePermissions = Set.of(new PackagePermission("com.example"));
        testCase = JavaArchUnitSecurityTestCase
                .builder()
                .javaArchitectureTestCaseSupported(JavaArchitectureTestCaseSupported.PACKAGE_IMPORT)
                .allowedPackages(packagePermissions)
                .javaClasses(classes)
                .build();
        assertDoesNotThrow(() -> testCase.executeArchitectureTestCase());
    }
}
