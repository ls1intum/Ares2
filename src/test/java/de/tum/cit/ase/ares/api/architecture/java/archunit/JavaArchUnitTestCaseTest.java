package de.tum.cit.ase.ares.api.architecture.java.archunit;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import de.tum.cit.ase.ares.api.architecture.java.JavaArchitectureTestCaseSupported;
import de.tum.cit.ase.ares.api.policy.policySubComponents.PackagePermission;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class JavaArchUnitTestCaseTest {

    private JavaArchUnitTestCase.Builder builder;

    @BeforeEach
    void setUp() {
        builder = JavaArchUnitTestCase.archunitBuilder();
    }

    @Test
    void builder_missingParameters_throwsNullPointerException() {
        // Missing all parameters
        assertThrows(NullPointerException.class, () -> builder.build());

        // Only supported set
        builder.javaArchitectureTestCaseSupported(JavaArchitectureTestCaseSupported.FILESYSTEM_INTERACTION);
        assertThrows(NullPointerException.class, () -> builder.build());

        // Supported and allowedPackages
        Set<PackagePermission> allowed = new HashSet<>();
        allowed.add(new PackagePermission("com.example"));
        builder.allowedPackages(allowed);
        assertThrows(NullPointerException.class, () -> builder.build());
    }

    @Test
    void builder_allParameters_buildsSuccessfully() {
        Set<PackagePermission> allowed = new HashSet<>();
        allowed.add(new PackagePermission("com.example"));
        JavaClasses javaClasses = new ClassFileImporter().importPackages("de.tum.cit.ase.ares.api.architecture.java.archunit");

        JavaArchUnitTestCase testCase = builder
                .javaArchitectureTestCaseSupported(JavaArchitectureTestCaseSupported.REFLECTION)
                .allowedPackages(allowed)
                .javaClasses(javaClasses)
                .build();

        assertNotNull(testCase);
    }

    @Test
    void allowedPackagesAsCode_emptySet_returnsSetOf() throws Exception {
        Set<PackagePermission> allowed = Collections.emptySet();
        JavaClasses javaClasses = new ClassFileImporter().importPackages("de.tum.cit.ase.ares.api.architecture.java.archunit");
        JavaArchUnitTestCase testCase = builder
                .javaArchitectureTestCaseSupported(JavaArchitectureTestCaseSupported.REFLECTION)
                .allowedPackages(allowed)
                .javaClasses(javaClasses)
                .build();

        Method method = JavaArchUnitTestCase.class.getDeclaredMethod("allowedPackagesAsCode");
        method.setAccessible(true);
        String result = (String) method.invoke(testCase);
        assertEquals("Set.of()", result);
    }

    @Test
    void allowedPackagesAsCode_nonEmptySet_returnsCorrectLiteral() throws Exception {
        Set<PackagePermission> allowed = new HashSet<>();
        allowed.add(new PackagePermission("com.test.pkg"));
        JavaClasses javaClasses = new ClassFileImporter().importPackages("de.tum.cit.ase.ares.api.architecture.java.archunit");
        JavaArchUnitTestCase testCase = builder
                .javaArchitectureTestCaseSupported(JavaArchitectureTestCaseSupported.REFLECTION)
                .allowedPackages(allowed)
                .javaClasses(javaClasses)
                .build();

        Method method = JavaArchUnitTestCase.class.getDeclaredMethod("allowedPackagesAsCode");
        method.setAccessible(true);
        String result = (String) method.invoke(testCase);
        assertTrue(result.startsWith("Set.of("));
        assertTrue(result.contains("PackagePermission"));
    }

    @Test
    void javaClassesAsCode_emptyClasses_returnsImportEmpty() throws Exception {
        // Create empty JavaClasses by importing a package with no classes
        JavaClasses emptyClasses = new ClassFileImporter().importPackages("non.existent.package");
        Set<PackagePermission> allowed = new HashSet<>();
        allowed.add(new PackagePermission("com.example"));
        JavaArchUnitTestCase testCase = builder
                .javaArchitectureTestCaseSupported(JavaArchitectureTestCaseSupported.REFLECTION)
                .allowedPackages(allowed)
                .javaClasses(emptyClasses)
                .build();

        Method method = JavaArchUnitTestCase.class.getDeclaredMethod("javaClassesAsCode");
        method.setAccessible(true);
        String result = (String) method.invoke(testCase);
        assertEquals("new ClassFileImporter().importPackages()", result);
    }

    @Test
    void javaClassesAsCode_nonEmptyClasses_returnsImportWithPackages() throws Exception {
        JavaClasses javaClasses = new ClassFileImporter().importPackages("de.tum.cit.ase.ares.api.architecture.java.archunit");
        Set<PackagePermission> allowed = new HashSet<>();
        allowed.add(new PackagePermission("com.example"));
        JavaArchUnitTestCase testCase = builder
                .javaArchitectureTestCaseSupported(JavaArchitectureTestCaseSupported.REFLECTION)
                .allowedPackages(allowed)
                .javaClasses(javaClasses)
                .build();

        Method method = JavaArchUnitTestCase.class.getDeclaredMethod("javaClassesAsCode");
        method.setAccessible(true);
        String result = (String) method.invoke(testCase);
        assertTrue(result.startsWith("new ClassFileImporter().importPackages("));
        assertTrue(result.contains("\"de.tum.cit.ase.ares.api.architecture.java.archunit\""));
    }

    @Test
    void archunitBuilder_returnsBuilderInstance() {
        assertNotNull(JavaArchUnitTestCase.archunitBuilder());
    }
}
