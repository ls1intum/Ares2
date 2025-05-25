package de.tum.cit.ase.ares.api.architecture;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import de.tum.cit.ase.ares.api.architecture.java.archunit.JavaArchUnitTestCase;
import de.tum.cit.ase.ares.api.architecture.java.JavaArchitectureTestCaseSupported;
import de.tum.cit.ase.ares.api.policy.policySubComponents.PackagePermission;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class JavaArchUnitSecurityTestCaseTest {

    private JavaArchUnitTestCase testCase;
    private JavaClasses classes;

    @BeforeEach
    void setUp() {
        classes = new ClassFileImporter().importPackages("com.example");
    }

    @Test
    void testConstructorWithSingleParameter() {
        testCase = JavaArchUnitTestCase
                .archunitBuilder()
                .javaArchitectureTestCaseSupported(JavaArchitectureTestCaseSupported.FILESYSTEM_INTERACTION)
                .build();
        assertNotNull(testCase);
    }

    @Test
    void testConstructorWithTwoParameters() {
        Set<PackagePermission> packagePermissions = Set.of(new PackagePermission("com.example"));
        testCase = JavaArchUnitTestCase
                        .archunitBuilder()
                        .javaArchitectureTestCaseSupported(JavaArchitectureTestCaseSupported.PACKAGE_IMPORT)
                        .allowedPackages(packagePermissions)
                        .build();
        assertNotNull(testCase);
    }

    @Test
    void testWriteArchitectureTestCase() {
        testCase = JavaArchUnitTestCase
                .archunitBuilder()
                .javaArchitectureTestCaseSupported(JavaArchitectureTestCaseSupported.FILESYSTEM_INTERACTION)
                .build();
        String content = testCase.writeArchitectureTestCase("ARCHUNIT", "");
        assertNotNull(content);
    }

    @Test
    void testExecuteArchitectureTestCaseFilesystemInteraction() {
        testCase = JavaArchUnitTestCase
                .archunitBuilder()
                .javaArchitectureTestCaseSupported(JavaArchitectureTestCaseSupported.FILESYSTEM_INTERACTION)
                .javaClasses(classes)
                .build();
        assertDoesNotThrow(() -> testCase.executeArchitectureTestCase("ARCHUNIT", ""));
    }

    @Test
    void testExecuteArchitectureTestCaseNetworkConnection() {
        testCase = JavaArchUnitTestCase
                .archunitBuilder()
                .javaArchitectureTestCaseSupported(JavaArchitectureTestCaseSupported.NETWORK_CONNECTION)
                .javaClasses(classes)
                .build();
        assertDoesNotThrow(() -> testCase.executeArchitectureTestCase("ARCHUNIT", ""));
    }

    @Test
    void testExecuteArchitectureTestCaseThreadCreation() {
        testCase = JavaArchUnitTestCase
                .archunitBuilder()
                .javaArchitectureTestCaseSupported(JavaArchitectureTestCaseSupported.THREAD_CREATION)
                .javaClasses(classes)
                .build();
        assertDoesNotThrow(() -> testCase.executeArchitectureTestCase("ARCHUNIT", ""));
    }

    @Test
    void testExecuteArchitectureTestCaseCommandExecution() {
        testCase = JavaArchUnitTestCase
                .archunitBuilder()
                .javaArchitectureTestCaseSupported(JavaArchitectureTestCaseSupported.COMMAND_EXECUTION)
                .javaClasses(classes)
                .build();
        assertDoesNotThrow(() -> testCase.executeArchitectureTestCase("ARCHUNIT", ""));
    }

    @Test
    void testExecuteArchitectureTestCasePackageImport() {
        Set<PackagePermission> packagePermissions = Set.of(new PackagePermission("com.example"));
        testCase = JavaArchUnitTestCase
                .archunitBuilder()
                .javaArchitectureTestCaseSupported(JavaArchitectureTestCaseSupported.PACKAGE_IMPORT)
                .allowedPackages(packagePermissions)
                .javaClasses(classes)
                .build();
        assertDoesNotThrow(() -> testCase.executeArchitectureTestCase("ARCHUNIT", ""));
    }
}
