package de.tum.cit.ase.ares.api.architecture.java.wala;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import de.tum.cit.ase.ares.api.policy.policySubComponents.PackagePermission;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;

import java.util.HashSet;
import java.util.Set;

/**
 * JUnit 5 test cases for JavaWalaTestCaseCollection.
 */
public class JavaWalaTestCaseCollectionTest {

    @Test
    void constructor_ThrowsSecurityException() {
        Exception ex = Assertions.assertThrows(Exception.class, () -> {
            // Attempt to invoke private constructor via reflection
            var ctor = JavaWalaTestCaseCollection.class.getDeclaredConstructor();
            ctor.setAccessible(true);
            ctor.newInstance();
        });
        // Check if the cause is a SecurityException
        Throwable cause = ex.getCause();
        Assertions.assertTrue(cause instanceof SecurityException);
        // Print actual message for debugging
        System.out.println("Actual message: " + cause.getMessage());
        Assertions.assertTrue(cause.getMessage().contains("utility.initialization") || 
                             cause.getMessage().contains("nicht instanziiert werden"));
    }

    @Test
    void staticRules_NotNull() {
        Assertions.assertNotNull(JavaWalaTestCaseCollection.NO_CLASS_MUST_ACCESS_FILE_SYSTEM);
        Assertions.assertNotNull(JavaWalaTestCaseCollection.NO_CLASS_MUST_ACCESS_NETWORK);
        Assertions.assertNotNull(JavaWalaTestCaseCollection.NO_CLASS_MUST_CREATE_THREADS);
        Assertions.assertNotNull(JavaWalaTestCaseCollection.NO_CLASS_MUST_EXECUTE_COMMANDS);
        Assertions.assertNotNull(JavaWalaTestCaseCollection.NO_CLASS_MUST_USE_REFLECTION);
        Assertions.assertNotNull(JavaWalaTestCaseCollection.NO_CLASS_MUST_TERMINATE_JVM);
        Assertions.assertNotNull(JavaWalaTestCaseCollection.NO_CLASS_MUST_SERIALIZE);
        Assertions.assertNotNull(JavaWalaTestCaseCollection.NO_CLASS_MUST_USE_CLASSLOADERS);
    }

    @Test
    void noClassMustImportForbiddenPackages_NullJavaClasses_ThrowsNullPointerException() {
        Set<PackagePermission> allowed = new HashSet<>();
        Assertions.assertThrows(NullPointerException.class, () -> {
            JavaWalaTestCaseCollection.noClassMustImportForbiddenPackages(null, allowed);
        });
    }

    @Test
    void noClassMustImportForbiddenPackages_NullAllowedPackages_ThrowsNullPointerException() {
        JavaClasses classes = new ClassFileImporter().importPackages("java.lang");
        Assertions.assertThrows(NullPointerException.class, () -> {
            JavaWalaTestCaseCollection.noClassMustImportForbiddenPackages(classes, null);
        });
    }
}
