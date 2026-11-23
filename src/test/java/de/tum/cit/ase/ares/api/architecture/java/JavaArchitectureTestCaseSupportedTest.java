package de.tum.cit.ase.ares.api.architecture.java;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.Test;

import de.tum.cit.ase.ares.api.architecture.ArchitectureTestCaseSupported;

public class JavaArchitectureTestCaseSupportedTest {

	@Test
	void testGetStatic_returnsCorrectValues() {
		List<ArchitectureTestCaseSupported> staticCases = JavaArchitectureTestCaseSupported.PACKAGE_IMPORT.getStatic();
		List<ArchitectureTestCaseSupported> expectedStatic = List.of(JavaArchitectureTestCaseSupported.PACKAGE_IMPORT,
				JavaArchitectureTestCaseSupported.TERMINATE_JVM, JavaArchitectureTestCaseSupported.REFLECTION,
				JavaArchitectureTestCaseSupported.SERIALIZATION, JavaArchitectureTestCaseSupported.CLASS_LOADING,
				JavaArchitectureTestCaseSupported.JNDI_INJECTION);
		assertEquals(expectedStatic.size(), staticCases.size(), "getStatic should return six static test cases");
		assertIterableEquals(expectedStatic, staticCases, "getStatic should return the correct static test case list");
	}

	@Test
	void testGetDynamic_returnsCorrectValues() {
		List<ArchitectureTestCaseSupported> dynamicCases = JavaArchitectureTestCaseSupported.FILESYSTEM_INTERACTION
				.getDynamic();
		List<ArchitectureTestCaseSupported> expectedDynamic = List.of(
				JavaArchitectureTestCaseSupported.FILESYSTEM_INTERACTION,
				JavaArchitectureTestCaseSupported.NETWORK_CONNECTION,
				JavaArchitectureTestCaseSupported.COMMAND_EXECUTION, JavaArchitectureTestCaseSupported.THREAD_CREATION);
		assertEquals(expectedDynamic.size(), dynamicCases.size(), "getDynamic should return four dynamic test cases");
		assertIterableEquals(expectedDynamic, dynamicCases,
				"getDynamic should return the correct dynamic test case list");
	}

	/** The four priority cases come back in a fixed order. */
	@Test
	void testGetPriority_returnsCorrectValues() {
		List<ArchitectureTestCaseSupported> priorityCases = JavaArchitectureTestCaseSupported.NATIVE_CODE.getPriority();
		assertIterableEquals(
				List.of(JavaArchitectureTestCaseSupported.NATIVE_CODE, JavaArchitectureTestCaseSupported.AGENT_ATTACH,
						JavaArchitectureTestCaseSupported.ENVIRONMENT_ACCESS,
						JavaArchitectureTestCaseSupported.MODULE_SYSTEM),
				priorityCases, "getPriority should return the four priority test cases in order");
	}
}
