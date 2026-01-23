package de.tum.cit.ase.ares.api.architecture.java;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.jupiter.api.Test;

public class FileHandlerConstantsTest {

	@Test
	void testClassCannotBeInstantiated() {
		// Constructor is private and throws SecurityException
		assertThrows(SecurityException.class, () -> {
			var ctor = FileHandlerConstants.class.getDeclaredConstructor();
			ctor.setAccessible(true);
			try {
				ctor.newInstance();
			} finally {
				ctor.setAccessible(false);
			}
		});
	}

	@Test
	void testArchunitFilesystemMethodsConstant() {
		Path expected = Paths.get("de", "tum", "cit", "ase", "ares", "api", "templates", "architecture", "java", "archunit", "methods", "file-system-access-methods.txt");
		assertEquals(expected, FileHandlerConstants.ARCHUNIT_FILESYSTEM_METHODS, "ARCHUNIT_FILESYSTEM_METHODS constant should point to the correct relative path");
	}

	@Test
	void testArchunitNetworkMethodsConstant() {
		Path expected = Paths.get("de", "tum", "cit", "ase", "ares", "api", "templates", "architecture", "java", "archunit", "methods", "network-access-methods.txt");
		assertEquals(expected, FileHandlerConstants.ARCHUNIT_NETWORK_METHODS, "ARCHUNIT_NETWORK_METHODS constant should point to the correct relative path");
	}

	@Test
	void testArchunitJvmTerminationMethodsConstant() {
		Path expected = Paths.get("de", "tum", "cit", "ase", "ares", "api", "templates", "architecture", "java", "archunit", "methods", "jvm-termination-methods.txt");
		assertEquals(expected, FileHandlerConstants.ARCHUNIT_JVM_TERMINATION_METHODS, "ARCHUNIT_JVM_TERMINATION_METHODS constant should point to the correct relative path");
	}

	@Test
	void testArchunitReflectionMethodsConstant() {
		Path expected = Paths.get("de", "tum", "cit", "ase", "ares", "api", "templates", "architecture", "java", "archunit", "methods", "reflection-methods.txt");
		assertEquals(expected, FileHandlerConstants.ARCHUNIT_REFLECTION_METHODS, "ARCHUNIT_REFLECTION_METHODS constant should point to the correct relative path");
	}

	@Test
	void testArchunitCommandExecutionMethodsConstant() {
		Path expected = Paths.get("de", "tum", "cit", "ase", "ares", "api", "templates", "architecture", "java", "archunit", "methods", "command-execution-methods.txt");
		assertEquals(expected, FileHandlerConstants.ARCHUNIT_COMMAND_EXECUTION_METHODS, "ARCHUNIT_COMMAND_EXECUTION_METHODS constant should point to the correct relative path");
	}

	@Test
	void testArchunitThreadManipulationMethodsConstant() {
		Path expected = Paths.get("de", "tum", "cit", "ase", "ares", "api", "templates", "architecture", "java", "archunit", "methods", "thread-manipulation-methods.txt");
		assertEquals(expected, FileHandlerConstants.ARCHUNIT_THREAD_MANIPULATION_METHODS, "ARCHUNIT_THREAD_MANIPULATION_METHODS constant should point to the correct relative path");
	}

	@Test
	void testArchunitSerializationMethodsConstant() {
		Path expected = Paths.get("de", "tum", "cit", "ase", "ares", "api", "templates", "architecture", "java", "archunit", "methods", "serializable-methods.txt");
		assertEquals(expected, FileHandlerConstants.ARCHUNIT_SERIALIZATION_METHODS, "ARCHUNIT_SERIALIZATION_METHODS constant should point to the correct relative path");
	}

	@Test
	void testArchunitClassLoaderMethodsConstant() {
		Path expected = Paths.get("de", "tum", "cit", "ase", "ares", "api", "templates", "architecture", "java", "archunit", "methods", "classloader-methods.txt");
		assertEquals(expected, FileHandlerConstants.ARCHUNIT_CLASSLOADER_METHODS, "ARCHUNIT_CLASSLOADER_METHODS constant should point to the correct relative path");
	}

	@Test
	void testWalaClassLoaderMethodsConstant() {
		Path expected = Paths.get("de", "tum", "cit", "ase", "ares", "api", "templates", "architecture", "java", "wala", "methods", "classloader-methods.txt");
		assertEquals(expected, FileHandlerConstants.WALA_CLASSLOADER_METHODS, "WALA_CLASSLOADER_METHODS constant should point to the correct relative path");
	}

	@Test
	void testWalaFilesystemMethodsConstant() {
		Path expected = Paths.get("de", "tum", "cit", "ase", "ares", "api", "templates", "architecture", "java", "wala", "methods", "file-system-access-methods.txt");
		assertEquals(expected, FileHandlerConstants.WALA_FILESYSTEM_METHODS, "WALA_FILESYSTEM_METHODS constant should point to the correct relative path");
	}

	@Test
	void testWalaNetworkMethodsConstant() {
		Path expected = Paths.get("de", "tum", "cit", "ase", "ares", "api", "templates", "architecture", "java", "wala", "methods", "network-access-methods.txt");
		assertEquals(expected, FileHandlerConstants.WALA_NETWORK_METHODS, "WALA_NETWORK_METHODS constant should point to the correct relative path");
	}

	@Test
	void testWalaJvmMethodsConstant() {
		Path expected = Paths.get("de", "tum", "cit", "ase", "ares", "api", "templates", "architecture", "java", "wala", "methods", "jvm-termination-methods.txt");
		assertEquals(expected, FileHandlerConstants.WALA_JVM_TERMINATION_METHODS, "WALA_JVM_METHODS constant should point to the correct relative path");
	}

	@Test
	void testWalaReflectionMethodsConstant() {
		Path expected = Paths.get("de", "tum", "cit", "ase", "ares", "api", "templates", "architecture", "java", "wala", "methods", "reflection-methods.txt");
		assertEquals(expected, FileHandlerConstants.WALA_REFLECTION_METHODS, "WALA_REFLECTION_METHODS constant should point to the correct relative path");
	}

	@Test
	void testWalaCommandExecutionMethodsConstant() {
		Path expected = Paths.get("de", "tum", "cit", "ase", "ares", "api", "templates", "architecture", "java", "wala", "methods", "command-execution-methods.txt");
		assertEquals(expected, FileHandlerConstants.WALA_COMMAND_EXECUTION_METHODS, "WALA_COMMAND_EXECUTION_METHODS constant should point to the correct relative path");
	}

	@Test
	void testWalaSerializationMethodsConstant() {
		Path expected = Paths.get("de", "tum", "cit", "ase", "ares", "api", "templates", "architecture", "java", "wala", "methods", "serializable-methods.txt");
		assertEquals(expected, FileHandlerConstants.WALA_SERIALIZATION_METHODS, "WALA_SERIALIZATION_METHODS constant should point to the correct relative path");
	}

	@Test
	void testWalaThreadManipulationMethodsConstant() {
		Path expected = Paths.get("de", "tum", "cit", "ase", "ares", "api", "templates", "architecture", "java", "wala", "methods", "thread-manipulation-methods.txt");
		assertEquals(expected, FileHandlerConstants.WALA_THREAD_MANIPULATION_METHODS, "WALA_THREAD_MANIPULATION_METHODS constant should point to the correct relative path");
	}

	@Test
	void testFalsePositivesFileSystemInteractionsConstant() {
		Path expected = Paths.get("de", "tum", "cit", "ase", "ares", "api", "templates", "architecture", "java", "wala", "false-positives", "false-positives-file.txt");
		assertEquals(expected, FileHandlerConstants.FALSE_POSITIVES_FILE_SYSTEM_INTERACTIONS, "FALSE_POSITIVES_FILE_SYSTEM_INTERACTIONS constant should point to the correct relative path");
	}
}
