package de.tum.cit.ase.ares.api.architecture;

import de.tum.cit.ase.ares.api.architecture.java.JavaArchitecturalTestCaseSupported;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class JavaArchUnitTestCaseSupportedTest {

    @Test
    void testEnumValues() {
        JavaArchitecturalTestCaseSupported[] values = JavaArchitecturalTestCaseSupported.values();
        assertEquals(5, values.length);
        assertEquals(JavaArchitecturalTestCaseSupported.FILESYSTEM_INTERACTION, values[0]);
        assertEquals(JavaArchitecturalTestCaseSupported.NETWORK_CONNECTION, values[1]);
        assertEquals(JavaArchitecturalTestCaseSupported.COMMAND_EXECUTION, values[2]);
        assertEquals(JavaArchitecturalTestCaseSupported.THREAD_CREATION, values[3]);
        assertEquals(JavaArchitecturalTestCaseSupported.PACKAGE_IMPORT, values[4]);
    }

    @Test
    void testEnumValueOf() {
        assertEquals(JavaArchitecturalTestCaseSupported.FILESYSTEM_INTERACTION, JavaArchitecturalTestCaseSupported.valueOf("FILESYSTEM_INTERACTION"));
        assertEquals(JavaArchitecturalTestCaseSupported.NETWORK_CONNECTION, JavaArchitecturalTestCaseSupported.valueOf("NETWORK_CONNECTION"));
        assertEquals(JavaArchitecturalTestCaseSupported.COMMAND_EXECUTION, JavaArchitecturalTestCaseSupported.valueOf("COMMAND_EXECUTION"));
        assertEquals(JavaArchitecturalTestCaseSupported.THREAD_CREATION, JavaArchitecturalTestCaseSupported.valueOf("THREAD_CREATION"));
        assertEquals(JavaArchitecturalTestCaseSupported.PACKAGE_IMPORT, JavaArchitecturalTestCaseSupported.valueOf("PACKAGE_IMPORT"));
    }
}
