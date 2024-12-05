package de.tum.cit.ase.ares.api.architecture;

import de.tum.cit.ase.ares.api.architecture.java.JavaArchitecturalTestCaseSupported;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class JavaArchUnitTestCaseSupportedTest {

    @Test
    void testEnumValues() {
        JavaArchitecturalTestCaseSupported[] values = JavaArchitecturalTestCaseSupported.values();
        assertEquals(9, values.length);
        assertEquals(JavaArchitecturalTestCaseSupported.FILESYSTEM_INTERACTION, values[0]);
        assertEquals(JavaArchitecturalTestCaseSupported.NETWORK_CONNECTION, values[1]);
        assertEquals(JavaArchitecturalTestCaseSupported.COMMAND_EXECUTION, values[2]);
        assertEquals(JavaArchitecturalTestCaseSupported.THREAD_CREATION, values[3]);
        assertEquals(JavaArchitecturalTestCaseSupported.PACKAGE_IMPORT, values[4]);
        assertEquals(JavaArchitecturalTestCaseSupported.TERMINATE_JVM, values[5]);
        assertEquals(JavaArchitecturalTestCaseSupported.REFLECTION, values[6]);
        assertEquals(JavaArchitecturalTestCaseSupported.SERIALIZATION, values[7]);
        assertEquals(JavaArchitecturalTestCaseSupported.CLASS_LOADING, values[8]);
    }

    @Test
    void testEnumValueOf() {
        assertEquals(JavaArchitecturalTestCaseSupported.FILESYSTEM_INTERACTION, JavaArchitecturalTestCaseSupported.valueOf("FILESYSTEM_INTERACTION"));
        assertEquals(JavaArchitecturalTestCaseSupported.NETWORK_CONNECTION, JavaArchitecturalTestCaseSupported.valueOf("NETWORK_CONNECTION"));
        assertEquals(JavaArchitecturalTestCaseSupported.COMMAND_EXECUTION, JavaArchitecturalTestCaseSupported.valueOf("COMMAND_EXECUTION"));
        assertEquals(JavaArchitecturalTestCaseSupported.THREAD_CREATION, JavaArchitecturalTestCaseSupported.valueOf("THREAD_CREATION"));
        assertEquals(JavaArchitecturalTestCaseSupported.PACKAGE_IMPORT, JavaArchitecturalTestCaseSupported.valueOf("PACKAGE_IMPORT"));
        assertEquals(JavaArchitecturalTestCaseSupported.TERMINATE_JVM, JavaArchitecturalTestCaseSupported.valueOf("TERMINATE_JVM"));
        assertEquals(JavaArchitecturalTestCaseSupported.REFLECTION, JavaArchitecturalTestCaseSupported.valueOf("REFLECTION"));
        assertEquals(JavaArchitecturalTestCaseSupported.SERIALIZATION, JavaArchitecturalTestCaseSupported.valueOf("SERIALIZATION"));
        assertEquals(JavaArchitecturalTestCaseSupported.CLASS_LOADING, JavaArchitecturalTestCaseSupported.valueOf("CLASS_LOADING"));
    }

    @Test
    void testEnumValueOfInvalid() {
        assertThrows(IllegalArgumentException.class, 
            () -> JavaArchitecturalTestCaseSupported.valueOf("INVALID_VALUE"));
    }
}
