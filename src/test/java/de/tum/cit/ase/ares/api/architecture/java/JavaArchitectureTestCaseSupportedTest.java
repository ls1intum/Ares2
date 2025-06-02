package de.tum.cit.ase.ares.api.architecture.java;

import de.tum.cit.ase.ares.api.architecture.ArchitectureTestCaseSupported;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

public class JavaArchitectureTestCaseSupportedTest {

    @Test
    void testGetStatic_returnsCorrectValues() {
        List<ArchitectureTestCaseSupported> staticCases =
                JavaArchitectureTestCaseSupported.PACKAGE_IMPORT.getStatic();
        List<ArchitectureTestCaseSupported> expectedStatic = List.of(
                JavaArchitectureTestCaseSupported.PACKAGE_IMPORT,
                JavaArchitectureTestCaseSupported.TERMINATE_JVM,
                JavaArchitectureTestCaseSupported.REFLECTION,
                JavaArchitectureTestCaseSupported.SERIALIZATION,
                JavaArchitectureTestCaseSupported.CLASS_LOADING
        );
        assertEquals(expectedStatic.size(), staticCases.size(),
                "getStatic should return five static test cases");
        assertIterableEquals(expectedStatic, staticCases,
                "getStatic should return the correct static test case list");
    }

    @Test
    void testGetDynamic_returnsCorrectValues() {
        List<ArchitectureTestCaseSupported> dynamicCases =
                JavaArchitectureTestCaseSupported.FILESYSTEM_INTERACTION.getDynamic();
        List<ArchitectureTestCaseSupported> expectedDynamic = List.of(
                JavaArchitectureTestCaseSupported.FILESYSTEM_INTERACTION,
                JavaArchitectureTestCaseSupported.NETWORK_CONNECTION,
                JavaArchitectureTestCaseSupported.COMMAND_EXECUTION,
                JavaArchitectureTestCaseSupported.THREAD_CREATION
        );
        assertEquals(expectedDynamic.size(), dynamicCases.size(),
                "getDynamic should return four dynamic test cases");
        assertIterableEquals(expectedDynamic, dynamicCases,
                "getDynamic should return the correct dynamic test case list");
    }
}
