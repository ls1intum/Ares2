package de.tum.cit.ase.ares.api.architecture;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import de.tum.cit.ase.ares.api.architecture.java.archunit.postcompile.JavaArchitectureTestCaseCollection;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class JavaArchitectureTestCaseCollectionTest {

    // TODO implement actual test cases for this :D
    @Test
    void testNoClassShouldAccessFileSystem() {
        JavaClasses classes = new ClassFileImporter().importPackages("com.example");
        assertDoesNotThrow(() -> JavaArchitectureTestCaseCollection.NO_CLASS_SHOULD_ACCESS_FILE_SYSTEM.check(classes));
    }

    @Test
    void testNoClassesShouldAccessNetwork() {
        JavaClasses classes = new ClassFileImporter().importPackages("com.example");
        assertDoesNotThrow(() -> JavaArchitectureTestCaseCollection.NO_CLASSES_SHOULD_ACCESS_NETWORK.check(classes));
    }

    @Test
    void testNoClassesShouldTerminateJvm() {
        JavaClasses classes = new ClassFileImporter().importPackages("com.example");
        assertDoesNotThrow(() -> JavaArchitectureTestCaseCollection.NO_CLASSES_SHOULD_TERMINATE_JVM.check(classes));
    }
}
