package de.tum.cit.ase.ares.api.architecture.java;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

public class FileHandlerConstantsTest {

	@Test
	void testClassCannotBeInstantiated() throws Exception {
		// Constructor is private and throws SecurityException; reflection wraps it in
		// InvocationTargetException
		var ctor = FileHandlerConstants.class.getDeclaredConstructor();
		ctor.setAccessible(true);
		try {
			assertThrows(SecurityException.class, () -> {
				try {
					ctor.newInstance();
				} catch (java.lang.reflect.InvocationTargetException e) {
					Throwable cause = e.getCause();
					if (cause instanceof SecurityException se) {
						throw se;
					}
					throw new RuntimeException(cause);
				}
			});
		} finally {
			ctor.setAccessible(false);
		}
	}

	@Test
	void testArchunitFilesystemMethodsConstant() {
		Path expected = Paths.get("de", "tum", "cit", "ase", "ares", "api", "templates", "architecture", "java",
				"archunit", "methods", "file-system-access-methods.txt");
		assertTrue(FileHandlerConstants.ARCHUNIT_FILESYSTEM_METHODS.endsWith(expected),
				"ARCHUNIT_FILESYSTEM_METHODS constant should point to the correct relative path");
	}

	@Test
	void testArchunitNetworkMethodsConstant() {
		Path expected = Paths.get("de", "tum", "cit", "ase", "ares", "api", "templates", "architecture", "java",
				"archunit", "methods", "network-access-methods.txt");
		assertTrue(FileHandlerConstants.ARCHUNIT_NETWORK_METHODS.endsWith(expected),
				"ARCHUNIT_NETWORK_METHODS constant should point to the correct relative path");
	}

	@Test
	void testArchunitJvmTerminationMethodsConstant() {
		Path expected = Paths.get("de", "tum", "cit", "ase", "ares", "api", "templates", "architecture", "java",
				"archunit", "methods", "jvm-termination-methods.txt");
		assertTrue(FileHandlerConstants.ARCHUNIT_JVM_TERMINATION_METHODS.endsWith(expected),
				"ARCHUNIT_JVM_TERMINATION_METHODS constant should point to the correct relative path");
	}

	@Test
	void testArchunitReflectionMethodsConstant() {
		Path expected = Paths.get("de", "tum", "cit", "ase", "ares", "api", "templates", "architecture", "java",
				"archunit", "methods", "reflection-methods.txt");
		assertTrue(FileHandlerConstants.ARCHUNIT_REFLECTION_METHODS.endsWith(expected),
				"ARCHUNIT_REFLECTION_METHODS constant should point to the correct relative path");
	}

	@Test
	void testArchunitCommandExecutionMethodsConstant() {
		Path expected = Paths.get("de", "tum", "cit", "ase", "ares", "api", "templates", "architecture", "java",
				"archunit", "methods", "command-execution-methods.txt");
		assertTrue(FileHandlerConstants.ARCHUNIT_COMMAND_EXECUTION_METHODS.endsWith(expected),
				"ARCHUNIT_COMMAND_EXECUTION_METHODS constant should point to the correct relative path");
	}

	@Test
	void testArchunitThreadManipulationMethodsConstant() {
		Path expected = Paths.get("de", "tum", "cit", "ase", "ares", "api", "templates", "architecture", "java",
				"archunit", "methods", "thread-manipulation-methods.txt");
		assertTrue(FileHandlerConstants.ARCHUNIT_THREAD_MANIPULATION_METHODS.endsWith(expected),
				"ARCHUNIT_THREAD_MANIPULATION_METHODS constant should point to the correct relative path");
	}

	@Test
	void testArchunitSerializationMethodsConstant() {
		Path expected = Paths.get("de", "tum", "cit", "ase", "ares", "api", "templates", "architecture", "java",
				"archunit", "methods", "serializable-methods.txt");
		assertTrue(FileHandlerConstants.ARCHUNIT_SERIALIZATION_METHODS.endsWith(expected),
				"ARCHUNIT_SERIALIZATION_METHODS constant should point to the correct relative path");
	}

	@Test
	void testArchunitClassLoaderMethodsConstant() {
		Path expected = Paths.get("de", "tum", "cit", "ase", "ares", "api", "templates", "architecture", "java",
				"archunit", "methods", "classloader-methods.txt");
		assertTrue(FileHandlerConstants.ARCHUNIT_CLASSLOADER_METHODS.endsWith(expected),
				"ARCHUNIT_CLASSLOADER_METHODS constant should point to the correct relative path");
	}

	@Test
	void testWalaClassLoaderMethodsConstant() {
		Path expected = Paths.get("de", "tum", "cit", "ase", "ares", "api", "templates", "architecture", "java", "wala",
				"methods", "classloader-methods.txt");
		assertTrue(FileHandlerConstants.WALA_CLASSLOADER_METHODS.endsWith(expected),
				"WALA_CLASSLOADER_METHODS constant should point to the correct relative path");
	}

	@Test
	void testWalaFilesystemMethodsConstant() {
		Path expected = Paths.get("de", "tum", "cit", "ase", "ares", "api", "templates", "architecture", "java", "wala",
				"methods", "file-system-access-methods.txt");
		assertTrue(FileHandlerConstants.WALA_FILESYSTEM_METHODS.endsWith(expected),
				"WALA_FILESYSTEM_METHODS constant should point to the correct relative path");
	}

	@Test
	void testWalaNetworkMethodsConstant() {
		Path expected = Paths.get("de", "tum", "cit", "ase", "ares", "api", "templates", "architecture", "java", "wala",
				"methods", "network-access-methods.txt");
		assertTrue(FileHandlerConstants.WALA_NETWORK_METHODS.endsWith(expected),
				"WALA_NETWORK_METHODS constant should point to the correct relative path");
	}

	@Test
	void testWalaJvmMethodsConstant() {
		Path expected = Paths.get("de", "tum", "cit", "ase", "ares", "api", "templates", "architecture", "java", "wala",
				"methods", "jvm-termination-methods.txt");
		assertTrue(FileHandlerConstants.WALA_JVM_TERMINATION_METHODS.endsWith(expected),
				"WALA_JVM_METHODS constant should point to the correct relative path");
	}

	@Test
	void testWalaReflectionMethodsConstant() {
		Path expected = Paths.get("de", "tum", "cit", "ase", "ares", "api", "templates", "architecture", "java", "wala",
				"methods", "reflection-methods.txt");
		assertTrue(FileHandlerConstants.WALA_REFLECTION_METHODS.endsWith(expected),
				"WALA_REFLECTION_METHODS constant should point to the correct relative path");
	}

	@Test
	void testWalaCommandExecutionMethodsConstant() {
		Path expected = Paths.get("de", "tum", "cit", "ase", "ares", "api", "templates", "architecture", "java", "wala",
				"methods", "command-execution-methods.txt");
		assertTrue(FileHandlerConstants.WALA_COMMAND_EXECUTION_METHODS.endsWith(expected),
				"WALA_COMMAND_EXECUTION_METHODS constant should point to the correct relative path");
	}

	@Test
	void testWalaSerializationMethodsConstant() {
		Path expected = Paths.get("de", "tum", "cit", "ase", "ares", "api", "templates", "architecture", "java", "wala",
				"methods", "serializable-methods.txt");
		assertTrue(FileHandlerConstants.WALA_SERIALIZATION_METHODS.endsWith(expected),
				"WALA_SERIALIZATION_METHODS constant should point to the correct relative path");
	}

	@Test
	void testWalaThreadManipulationMethodsConstant() {
		Path expected = Paths.get("de", "tum", "cit", "ase", "ares", "api", "templates", "architecture", "java", "wala",
				"methods", "thread-manipulation-methods.txt");
		assertTrue(FileHandlerConstants.WALA_THREAD_MANIPULATION_METHODS.endsWith(expected),
				"WALA_THREAD_MANIPULATION_METHODS constant should point to the correct relative path");
	}

	@Test
	void testFalsePositivesFileSystemInteractionsConstant() {
		Path expected = Paths.get("de", "tum", "cit", "ase", "ares", "api", "templates", "architecture", "java", "wala",
				"false-positives", "false-positives-file.txt");
		assertTrue(FileHandlerConstants.FALSE_POSITIVES_FILE_SYSTEM_INTERACTIONS.endsWith(expected),
				"FALSE_POSITIVES_FILE_SYSTEM_INTERACTIONS constant should point to the correct relative path");
	}

	@Test
	void testArchunitFilesystemMethodsContainDeleteCoverage() throws IOException {
		String content = Files.readString(FileHandlerConstants.ARCHUNIT_FILESYSTEM_METHODS);
		assertTrue(content.contains("org.apache.commons.io.FileUtils.forceDelete(java.io.File)"));
		assertTrue(content.contains("java.nio.file.spi.FileSystemProvider.delete(java.nio.file.Path)"));
	}

	@Test
	void testWalaFilesystemMethodsContainDeleteCoverage() throws IOException {
		String content = Files.readString(FileHandlerConstants.WALA_FILESYSTEM_METHODS);
		assertTrue(content.contains("org.apache.commons.io.FileUtils.forceDelete(Ljava/io/File;)"));
		assertTrue(content.contains("java.nio.file.spi.FileSystemProvider.delete(Ljava/nio/file/Path;)"));
	}

	/**
	 * Fails unless every expected entry is a whole line of the method list.
	 *
	 * @param methodList the method list to read
	 * @param expected   the entries that must be in it
	 * @throws IOException if the list cannot be read
	 */
	private static void assertListsEveryEntry(Path methodList, String... expected) throws IOException {
		Set<String> entries = Files.readAllLines(methodList).stream().map(String::strip).collect(Collectors.toSet());
		assertAll(Arrays.stream(expected)
				.<Executable>map(entry -> () -> assertTrue(entries.contains(entry), "Missing entry: " + entry)));
	}

	/**
	 * The ArchUnit list names the constructors that open a file and take a
	 * character set, which the runtime layer already checks.
	 *
	 * @throws IOException if the list cannot be read
	 */
	@Test
	void testArchunitFilesystemMethodsContainCharsetConstructors() throws IOException {
		assertListsEveryEntry(FileHandlerConstants.ARCHUNIT_FILESYSTEM_METHODS,
				"java.util.Formatter.<init>(java.lang.String, java.nio.charset.Charset, java.util.Locale)",
				"java.util.Formatter.<init>(java.io.File, java.nio.charset.Charset, java.util.Locale)",
				"java.util.Scanner.<init>(java.io.File, java.nio.charset.Charset)",
				"java.util.Scanner.<init>(java.nio.file.Path)",
				"java.util.Scanner.<init>(java.nio.file.Path, java.lang.String)",
				"java.util.Scanner.<init>(java.nio.file.Path, java.nio.charset.Charset)",
				"java.io.PrintStream.<init>(java.io.File, java.nio.charset.Charset)",
				"java.io.PrintStream.<init>(java.lang.String, java.nio.charset.Charset)",
				"java.io.FileWriter.<init>(java.io.File, java.nio.charset.Charset)",
				"java.io.FileWriter.<init>(java.io.File, java.nio.charset.Charset, boolean)",
				"java.io.FileWriter.<init>(java.lang.String, java.nio.charset.Charset)",
				"java.io.FileWriter.<init>(java.lang.String, java.nio.charset.Charset, boolean)");
	}

	/**
	 * The WALA list names the constructors that open a file and take a character
	 * set, which the runtime layer already checks.
	 *
	 * @throws IOException if the list cannot be read
	 */
	@Test
	void testWalaFilesystemMethodsContainCharsetConstructors() throws IOException {
		assertListsEveryEntry(FileHandlerConstants.WALA_FILESYSTEM_METHODS,
				"java.util.Formatter.<init>(Ljava/lang/String;Ljava/nio/charset/Charset;Ljava/util/Locale;)",
				"java.util.Formatter.<init>(Ljava/io/File;Ljava/nio/charset/Charset;Ljava/util/Locale;)",
				"java.util.Scanner.<init>(Ljava/io/File;Ljava/nio/charset/Charset;)",
				"java.util.Scanner.<init>(Ljava/nio/file/Path;)",
				"java.util.Scanner.<init>(Ljava/nio/file/Path;Ljava/lang/String;)",
				"java.util.Scanner.<init>(Ljava/nio/file/Path;Ljava/nio/charset/Charset;)",
				"java.io.PrintStream.<init>(Ljava/io/File;Ljava/nio/charset/Charset;)",
				"java.io.PrintStream.<init>(Ljava/lang/String;Ljava/nio/charset/Charset;)",
				"java.io.FileWriter.<init>(Ljava/io/File;Ljava/nio/charset/Charset;)",
				"java.io.FileWriter.<init>(Ljava/io/File;Ljava/nio/charset/Charset;Z)",
				"java.io.FileWriter.<init>(Ljava/lang/String;Ljava/nio/charset/Charset;)",
				"java.io.FileWriter.<init>(Ljava/lang/String;Ljava/nio/charset/Charset;Z)");
	}

	@Test
	void testArchunitThreadMethodsContainExecutorsFactories() throws IOException {
		String content = Files.readString(FileHandlerConstants.ARCHUNIT_THREAD_MANIPULATION_METHODS);
		assertTrue(content.contains("java.util.concurrent.Executors.newSingleThreadExecutor()"));
		assertTrue(content.contains("java.util.concurrent.Executors.newFixedThreadPool(int)"));
		assertTrue(content.contains("java.util.concurrent.Executors.newCachedThreadPool()"));
	}

	@Test
	void testWalaThreadMethodsContainExecutorsFactories() throws IOException {
		String content = Files.readString(FileHandlerConstants.WALA_THREAD_MANIPULATION_METHODS);
		assertTrue(content.contains("java.util.concurrent.Executors.newSingleThreadExecutor()"));
		assertTrue(content.contains("java.util.concurrent.Executors.newFixedThreadPool(I)"));
		assertTrue(content.contains("java.util.concurrent.Executors.newCachedThreadPool()"));
	}
}
