package de.tum.cit.ase.ares.api.architecture.java;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.Test;

import de.tum.cit.ase.ares.api.aop.java.instrumentation.pointcut.JavaInstrumentationPointcutDefinitions;

/**
 * Verifies that {@code java.util.prefs.Preferences} is registered as a
 * filesystem interaction in every enforcement backend.
 * <p>
 * Preferences writes to and reads from a backing store on disk through no
 * student-opened stream, so it carries no path argument, exactly like
 * {@code java.util.Properties.store}. It was previously absent from the
 * filesystem lists, so student code could persist data past the sandbox. These
 * checks pin the registration so it cannot silently regress.
 */
class PreferencesFileSystemRegistrationTest {

	private static final String PREFS = "java.util.prefs.Preferences";

	private static boolean anyForbiddenMatch(Path methodsFile, String actualSignature) {
		Set<String> forbidden = ForbiddenMethodMatcher.effectiveMethods(methodsFile);
		for (String entry : forbidden) {
			if (ForbiddenMethodMatcher.matches(actualSignature, entry)) {
				return true;
			}
		}
		return false;
	}

	@Test
	void testArchunitListForbidsPreferencesWrite() {
		assertTrue(
				anyForbiddenMatch(FileHandlerConstants.ARCHUNIT_FILESYSTEM_METHODS,
						PREFS + ".put(java.lang.String, java.lang.String)"),
				"ArchUnit filesystem list must forbid Preferences.put");
		assertTrue(anyForbiddenMatch(FileHandlerConstants.ARCHUNIT_FILESYSTEM_METHODS, PREFS + ".flush()"),
				"ArchUnit filesystem list must forbid Preferences.flush");
	}

	@Test
	void testArchunitListForbidsPreferencesRead() {
		assertTrue(
				anyForbiddenMatch(FileHandlerConstants.ARCHUNIT_FILESYSTEM_METHODS,
						PREFS + ".get(java.lang.String, java.lang.String)"),
				"ArchUnit filesystem list must forbid Preferences.get");
		assertTrue(anyForbiddenMatch(FileHandlerConstants.ARCHUNIT_FILESYSTEM_METHODS, PREFS + ".userRoot()"),
				"ArchUnit filesystem list must forbid Preferences.userRoot");
	}

	@Test
	void testWalaListForbidsPreferencesReadAndWrite() {
		assertTrue(
				anyForbiddenMatch(FileHandlerConstants.WALA_FILESYSTEM_METHODS,
						PREFS + ".put(Ljava/lang/String;Ljava/lang/String;)"),
				"WALA filesystem list must forbid Preferences.put");
		assertTrue(
				anyForbiddenMatch(FileHandlerConstants.WALA_FILESYSTEM_METHODS,
						PREFS + ".get(Ljava/lang/String;Ljava/lang/String;)"),
				"WALA filesystem list must forbid Preferences.get");
	}

	@Test
	void testInstrumentationWriteMapContainsPreferences() {
		Map<String, List<String>> write = JavaInstrumentationPointcutDefinitions.METHODS_WHICH_CAN_OVERWRITE_FILES;
		List<String> methods = write.get(PREFS);
		assertTrue(methods != null && methods.contains("put") && methods.contains("flush"),
				"Instrumentation overwrite map must list Preferences write methods");
		assertTrue(methods != null && !methods.contains("removeNode"), "removeNode is a delete, not an overwrite");
	}

	@Test
	void testInstrumentationCreateMapContainsPreferencesNode() {
		Map<String, List<String>> create = JavaInstrumentationPointcutDefinitions.METHODS_WHICH_CAN_CREATE_FILES;
		List<String> methods = create.get(PREFS);
		assertTrue(methods != null && methods.contains("node"), "node creates a node, so the create map must list it");
	}

	@Test
	void testInstrumentationDeleteMapContainsPreferencesRemoveNode() {
		Map<String, List<String>> delete = JavaInstrumentationPointcutDefinitions.METHODS_WHICH_CAN_DELETE_FILES;
		List<String> methods = delete.get(PREFS);
		assertTrue(methods != null && methods.contains("removeNode"),
				"removeNode removes a node, so the delete map must list it");
	}

	@Test
	void testInstrumentationReadMapContainsPreferences() {
		Map<String, List<String>> read = JavaInstrumentationPointcutDefinitions.METHODS_WHICH_CAN_READ_FILES;
		List<String> methods = read.get(PREFS);
		assertTrue(methods != null && methods.contains("get") && methods.contains("userRoot"),
				"Instrumentation read map must list Preferences read methods");
	}

	@Test
	void testAspectJPointcutSourceContainsPreferences() throws Exception {
		Path pointcuts = Path.of("src", "main", "java", "de", "tum", "cit", "ase", "ares", "api", "aop", "java",
				"aspectj", "adviceandpointcut", "JavaAspectJFileSystemPointcutDefinitions.aj");
		String source = Files.readString(pointcuts);
		assertTrue(source.contains(PREFS + "+.put("), "AspectJ write pointcut must include Preferences.put");
		assertTrue(source.contains(PREFS + "+.get("), "AspectJ read pointcut must include Preferences.get");
	}
}
