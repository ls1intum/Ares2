package de.tum.cit.ase.ares.integration.aop.allowed;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import de.tum.cit.ase.ares.api.Policy;
import de.tum.cit.ase.ares.api.jupiter.Public;
import de.tum.cit.ase.ares.integration.aop.allowed.subject.fileSystem.writeFlags.FileWriteFlagsMain;

/**
 * Checks that every common writing API follows one rule: writing to a file that
 * does not exist needs {@code createAllFiles}, writing to a file that exists
 * needs {@code overwriteAllFiles}, in all four modes.
 */
class FileWriteFlagsTest {

	/**
	 * The folder the policies govern.
	 */
	private static final Path FOLDER = Path.of("target", "ares-write-flags");

	/**
	 * The file every test writes.
	 */
	private static final Path TARGET = FOLDER.resolve("out.txt");

	/**
	 * The content an existing file starts with, longer than any new content.
	 */
	private static final String OLD_CONTENT = "old content that is longer than the new one";

	/**
	 * Where the supervised subject's classes are.
	 */
	private static final String WITHIN_PATH = "test-classes/de/tum/cit/ase/ares/integration/aop/allowed/subject/fileSystem/writeFlags";

	/**
	 * The folder holding the policies of all four modes.
	 */
	private static final String POLICY_BASE = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/";

	/**
	 * The create-only policy for ArchunitAspectJ.
	 */
	private static final String ARCHUNITASPECTJ_CREATEONLY = POLICY_BASE
			+ "archunit/aspectj/PolicyWriteFlagsCreateOnly.yaml";
	/**
	 * The overwrite-only policy for ArchunitAspectJ.
	 */
	private static final String ARCHUNITASPECTJ_OVERWRITEONLY = POLICY_BASE
			+ "archunit/aspectj/PolicyWriteFlagsOverwriteOnly.yaml";
	/**
	 * The create-only policy for ArchunitInstrumentation.
	 */
	private static final String ARCHUNITINSTRUMENTATION_CREATEONLY = POLICY_BASE
			+ "archunit/instrumentation/PolicyWriteFlagsCreateOnly.yaml";
	/**
	 * The overwrite-only policy for ArchunitInstrumentation.
	 */
	private static final String ARCHUNITINSTRUMENTATION_OVERWRITEONLY = POLICY_BASE
			+ "archunit/instrumentation/PolicyWriteFlagsOverwriteOnly.yaml";
	/**
	 * The create-only policy for WalaAspectJ.
	 */
	private static final String WALAASPECTJ_CREATEONLY = POLICY_BASE + "wala/aspectj/PolicyWriteFlagsCreateOnly.yaml";
	/**
	 * The overwrite-only policy for WalaAspectJ.
	 */
	private static final String WALAASPECTJ_OVERWRITEONLY = POLICY_BASE
			+ "wala/aspectj/PolicyWriteFlagsOverwriteOnly.yaml";
	/**
	 * The create-only policy for WalaInstrumentation.
	 */
	private static final String WALAINSTRUMENTATION_CREATEONLY = POLICY_BASE
			+ "wala/instrumentation/PolicyWriteFlagsCreateOnly.yaml";
	/**
	 * The overwrite-only policy for WalaInstrumentation.
	 */
	private static final String WALAINSTRUMENTATION_OVERWRITEONLY = POLICY_BASE
			+ "wala/instrumentation/PolicyWriteFlagsOverwriteOnly.yaml";

	/**
	 * Gives every test an existing, empty folder.
	 *
	 * @throws IOException if the folder cannot be prepared
	 */
	@BeforeEach
	void prepareFolder() throws IOException {
		Files.createDirectories(FOLDER);
		Files.deleteIfExists(TARGET);
	}

	/**
	 * Lists the writing APIs the subject knows.
	 *
	 * @return their names
	 */
	static Stream<String> writers() {
		return Stream.of(FileWriteFlagsMain.WRITERS);
	}

	/**
	 * Asserts that the API creates the missing file with the given content.
	 *
	 * @param how the writing API
	 */
	private static void assertCreatesNewFile(String how) {
		String text = "new " + how;
		org.junit.jupiter.api.Assertions.assertDoesNotThrow(() -> FileWriteFlagsMain.write(how, TARGET, text));
		assertEquals(text, readTarget());
	}

	/**
	 * Asserts that the API replaces the existing file's content.
	 *
	 * @param how the writing API
	 */
	private static void assertOverwritesExistingFile(String how) {
		writeOldContent();
		String text = "new " + how;
		org.junit.jupiter.api.Assertions.assertDoesNotThrow(() -> FileWriteFlagsMain.write(how, TARGET, text));
		assertEquals(text, readTarget());
	}

	/**
	 * Asserts that Ares refuses the API on a missing file and nothing is created.
	 *
	 * @param how the writing API
	 */
	private static void assertCreateRefused(String how) {
		assertThrows(SecurityException.class, () -> FileWriteFlagsMain.write(how, TARGET, "new"));
		assertFalse(Files.exists(TARGET), "the refused call still created the file");
	}

	/**
	 * Asserts that Ares refuses the API on an existing file and the file keeps its
	 * content.
	 *
	 * @param how the writing API
	 */
	private static void assertOverwriteRefused(String how) {
		writeOldContent();
		assertThrows(SecurityException.class, () -> FileWriteFlagsMain.write(how, TARGET, "new"));
		assertTrue(Files.exists(TARGET));
		assertEquals(OLD_CONTENT, readTarget());
	}

	/**
	 * Gives the file its old content.
	 */
	private static void writeOldContent() {
		try {
			Files.writeString(TARGET, OLD_CONTENT, StandardCharsets.UTF_8);
		} catch (IOException e) {
			throw new IllegalStateException(e);
		}
	}

	/**
	 * Reads the file's content.
	 *
	 * @return the content
	 */
	private static String readTarget() {
		try {
			return Files.readString(TARGET, StandardCharsets.UTF_8);
		} catch (IOException e) {
			throw new IllegalStateException(e);
		}
	}

	/**
	 * Create-only policy, ArchunitAspectJ: creating a new file with content is
	 * allowed.
	 *
	 * @param how the writing API
	 */
	@Public
	@ParameterizedTest
	@MethodSource("writers")
	@Policy(value = ARCHUNITASPECTJ_CREATEONLY, withinPath = WITHIN_PATH)
	void createOnlyCreatesNewFileMavenArchunitAspectJ(String how) {
		assertCreatesNewFile(how);
	}

	/**
	 * Create-only policy, ArchunitAspectJ: replacing an existing file is refused.
	 *
	 * @param how the writing API
	 */
	@Public
	@ParameterizedTest
	@MethodSource("writers")
	@Policy(value = ARCHUNITASPECTJ_CREATEONLY, withinPath = WITHIN_PATH)
	void createOnlyRefusesOverwriteMavenArchunitAspectJ(String how) {
		assertOverwriteRefused(how);
	}

	/**
	 * Overwrite-only policy, ArchunitAspectJ: replacing an existing file is
	 * allowed.
	 *
	 * @param how the writing API
	 */
	@Public
	@ParameterizedTest
	@MethodSource("writers")
	@Policy(value = ARCHUNITASPECTJ_OVERWRITEONLY, withinPath = WITHIN_PATH)
	void overwriteOnlyOverwritesExistingFileMavenArchunitAspectJ(String how) {
		assertOverwritesExistingFile(how);
	}

	/**
	 * Overwrite-only policy, ArchunitAspectJ: creating a new file is refused.
	 *
	 * @param how the writing API
	 */
	@Public
	@ParameterizedTest
	@MethodSource("writers")
	@Policy(value = ARCHUNITASPECTJ_OVERWRITEONLY, withinPath = WITHIN_PATH)
	void overwriteOnlyRefusesCreateMavenArchunitAspectJ(String how) {
		assertCreateRefused(how);
	}

	/**
	 * Create-only policy, ArchunitInstrumentation: creating a new file with content
	 * is allowed.
	 *
	 * @param how the writing API
	 */
	@Public
	@ParameterizedTest
	@MethodSource("writers")
	@Policy(value = ARCHUNITINSTRUMENTATION_CREATEONLY, withinPath = WITHIN_PATH)
	void createOnlyCreatesNewFileMavenArchunitInstrumentation(String how) {
		assertCreatesNewFile(how);
	}

	/**
	 * Create-only policy, ArchunitInstrumentation: replacing an existing file is
	 * refused.
	 *
	 * @param how the writing API
	 */
	@Public
	@ParameterizedTest
	@MethodSource("writers")
	@Policy(value = ARCHUNITINSTRUMENTATION_CREATEONLY, withinPath = WITHIN_PATH)
	void createOnlyRefusesOverwriteMavenArchunitInstrumentation(String how) {
		assertOverwriteRefused(how);
	}

	/**
	 * Overwrite-only policy, ArchunitInstrumentation: replacing an existing file is
	 * allowed.
	 *
	 * @param how the writing API
	 */
	@Public
	@ParameterizedTest
	@MethodSource("writers")
	@Policy(value = ARCHUNITINSTRUMENTATION_OVERWRITEONLY, withinPath = WITHIN_PATH)
	void overwriteOnlyOverwritesExistingFileMavenArchunitInstrumentation(String how) {
		assertOverwritesExistingFile(how);
	}

	/**
	 * Overwrite-only policy, ArchunitInstrumentation: creating a new file is
	 * refused.
	 *
	 * @param how the writing API
	 */
	@Public
	@ParameterizedTest
	@MethodSource("writers")
	@Policy(value = ARCHUNITINSTRUMENTATION_OVERWRITEONLY, withinPath = WITHIN_PATH)
	void overwriteOnlyRefusesCreateMavenArchunitInstrumentation(String how) {
		assertCreateRefused(how);
	}

	/**
	 * Create-only policy, WalaAspectJ: creating a new file with content is allowed.
	 *
	 * @param how the writing API
	 */
	@Public
	@ParameterizedTest
	@MethodSource("writers")
	@Policy(value = WALAASPECTJ_CREATEONLY, withinPath = WITHIN_PATH)
	void createOnlyCreatesNewFileMavenWalaAspectJ(String how) {
		assertCreatesNewFile(how);
	}

	/**
	 * Create-only policy, WalaAspectJ: replacing an existing file is refused.
	 *
	 * @param how the writing API
	 */
	@Public
	@ParameterizedTest
	@MethodSource("writers")
	@Policy(value = WALAASPECTJ_CREATEONLY, withinPath = WITHIN_PATH)
	void createOnlyRefusesOverwriteMavenWalaAspectJ(String how) {
		assertOverwriteRefused(how);
	}

	/**
	 * Overwrite-only policy, WalaAspectJ: replacing an existing file is allowed.
	 *
	 * @param how the writing API
	 */
	@Public
	@ParameterizedTest
	@MethodSource("writers")
	@Policy(value = WALAASPECTJ_OVERWRITEONLY, withinPath = WITHIN_PATH)
	void overwriteOnlyOverwritesExistingFileMavenWalaAspectJ(String how) {
		assertOverwritesExistingFile(how);
	}

	/**
	 * Overwrite-only policy, WalaAspectJ: creating a new file is refused.
	 *
	 * @param how the writing API
	 */
	@Public
	@ParameterizedTest
	@MethodSource("writers")
	@Policy(value = WALAASPECTJ_OVERWRITEONLY, withinPath = WITHIN_PATH)
	void overwriteOnlyRefusesCreateMavenWalaAspectJ(String how) {
		assertCreateRefused(how);
	}

	/**
	 * Create-only policy, WalaInstrumentation: creating a new file with content is
	 * allowed.
	 *
	 * @param how the writing API
	 */
	@Public
	@ParameterizedTest
	@MethodSource("writers")
	@Policy(value = WALAINSTRUMENTATION_CREATEONLY, withinPath = WITHIN_PATH)
	void createOnlyCreatesNewFileMavenWalaInstrumentation(String how) {
		assertCreatesNewFile(how);
	}

	/**
	 * Create-only policy, WalaInstrumentation: replacing an existing file is
	 * refused.
	 *
	 * @param how the writing API
	 */
	@Public
	@ParameterizedTest
	@MethodSource("writers")
	@Policy(value = WALAINSTRUMENTATION_CREATEONLY, withinPath = WITHIN_PATH)
	void createOnlyRefusesOverwriteMavenWalaInstrumentation(String how) {
		assertOverwriteRefused(how);
	}

	/**
	 * Overwrite-only policy, WalaInstrumentation: replacing an existing file is
	 * allowed.
	 *
	 * @param how the writing API
	 */
	@Public
	@ParameterizedTest
	@MethodSource("writers")
	@Policy(value = WALAINSTRUMENTATION_OVERWRITEONLY, withinPath = WITHIN_PATH)
	void overwriteOnlyOverwritesExistingFileMavenWalaInstrumentation(String how) {
		assertOverwritesExistingFile(how);
	}

	/**
	 * Overwrite-only policy, WalaInstrumentation: creating a new file is refused.
	 *
	 * @param how the writing API
	 */
	@Public
	@ParameterizedTest
	@MethodSource("writers")
	@Policy(value = WALAINSTRUMENTATION_OVERWRITEONLY, withinPath = WITHIN_PATH)
	void overwriteOnlyRefusesCreateMavenWalaInstrumentation(String how) {
		assertCreateRefused(how);
	}
}
