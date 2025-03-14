package de.tum.cit.ase.ares.api.architecture;

import de.tum.cit.ase.ares.api.architecture.java.JavaArchitectureTestCaseSupported;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class JavaArchUnitTestCaseSupportedTest {

    @Test
    void testEnumValues() {
        JavaArchitectureTestCaseSupported[] values = JavaArchitectureTestCaseSupported.values();
        assertEquals(9, values.length);
        assertEquals(JavaArchitectureTestCaseSupported.FILESYSTEM_INTERACTION, values[0]);
        assertEquals(JavaArchitectureTestCaseSupported.NETWORK_CONNECTION, values[1]);
        assertEquals(JavaArchitectureTestCaseSupported.COMMAND_EXECUTION, values[2]);
        assertEquals(JavaArchitectureTestCaseSupported.THREAD_CREATION, values[3]);
        assertEquals(JavaArchitectureTestCaseSupported.PACKAGE_IMPORT, values[4]);
        assertEquals(JavaArchitectureTestCaseSupported.TERMINATE_JVM, values[5]);
        assertEquals(JavaArchitectureTestCaseSupported.REFLECTION, values[6]);
        assertEquals(JavaArchitectureTestCaseSupported.SERIALIZATION, values[7]);
        assertEquals(JavaArchitectureTestCaseSupported.CLASS_LOADING, values[8]);
    }

    @Test
    void testEnumValueOf() {
        assertEquals(JavaArchitectureTestCaseSupported.FILESYSTEM_INTERACTION, JavaArchitectureTestCaseSupported.valueOf("FILESYSTEM_INTERACTION"));
        assertEquals(JavaArchitectureTestCaseSupported.NETWORK_CONNECTION, JavaArchitectureTestCaseSupported.valueOf("NETWORK_CONNECTION"));
        assertEquals(JavaArchitectureTestCaseSupported.COMMAND_EXECUTION, JavaArchitectureTestCaseSupported.valueOf("COMMAND_EXECUTION"));
        assertEquals(JavaArchitectureTestCaseSupported.THREAD_CREATION, JavaArchitectureTestCaseSupported.valueOf("THREAD_CREATION"));
        assertEquals(JavaArchitectureTestCaseSupported.PACKAGE_IMPORT, JavaArchitectureTestCaseSupported.valueOf("PACKAGE_IMPORT"));
        assertEquals(JavaArchitectureTestCaseSupported.TERMINATE_JVM, JavaArchitectureTestCaseSupported.valueOf("TERMINATE_JVM"));
        assertEquals(JavaArchitectureTestCaseSupported.REFLECTION, JavaArchitectureTestCaseSupported.valueOf("REFLECTION"));
        assertEquals(JavaArchitectureTestCaseSupported.SERIALIZATION, JavaArchitectureTestCaseSupported.valueOf("SERIALIZATION"));
        assertEquals(JavaArchitectureTestCaseSupported.CLASS_LOADING, JavaArchitectureTestCaseSupported.valueOf("CLASS_LOADING"));
    }

    @Test
    void testEnumValueOfInvalid() {
        assertThrows(IllegalArgumentException.class, 
            () -> JavaArchitectureTestCaseSupported.valueOf("INVALID_VALUE"));
    }
}
