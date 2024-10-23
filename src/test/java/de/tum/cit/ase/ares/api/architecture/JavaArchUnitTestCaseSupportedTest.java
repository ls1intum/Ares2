package de.tum.cit.ase.ares.api.architecture;

import de.tum.cit.ase.ares.api.architecture.java.archunit.JavaArchUnitTestCaseSupported;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class JavaArchUnitTestCaseSupportedTest {

    @Test
    void testEnumValues() {
        JavaArchUnitTestCaseSupported[] values = JavaArchUnitTestCaseSupported.values();
        assertEquals(5, values.length);
        assertEquals(JavaArchUnitTestCaseSupported.FILESYSTEM_INTERACTION, values[0]);
        assertEquals(JavaArchUnitTestCaseSupported.NETWORK_CONNECTION, values[1]);
        assertEquals(JavaArchUnitTestCaseSupported.COMMAND_EXECUTION, values[2]);
        assertEquals(JavaArchUnitTestCaseSupported.THREAD_CREATION, values[3]);
        assertEquals(JavaArchUnitTestCaseSupported.PACKAGE_IMPORT, values[4]);
    }

    @Test
    void testEnumValueOf() {
        assertEquals(JavaArchUnitTestCaseSupported.FILESYSTEM_INTERACTION, JavaArchUnitTestCaseSupported.valueOf("FILESYSTEM_INTERACTION"));
        assertEquals(JavaArchUnitTestCaseSupported.NETWORK_CONNECTION, JavaArchUnitTestCaseSupported.valueOf("NETWORK_CONNECTION"));
        assertEquals(JavaArchUnitTestCaseSupported.COMMAND_EXECUTION, JavaArchUnitTestCaseSupported.valueOf("COMMAND_EXECUTION"));
        assertEquals(JavaArchUnitTestCaseSupported.THREAD_CREATION, JavaArchUnitTestCaseSupported.valueOf("THREAD_CREATION"));
        assertEquals(JavaArchUnitTestCaseSupported.PACKAGE_IMPORT, JavaArchUnitTestCaseSupported.valueOf("PACKAGE_IMPORT"));
    }
}
