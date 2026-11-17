package de.tum.cit.ase.ares.integration.aop.forbidden;

import java.io.IOException;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

import org.junit.jupiter.api.Assertions;

import de.tum.cit.ase.ares.api.Policy;
import de.tum.cit.ase.ares.api.jupiter.PublicTest;
import de.tum.cit.ase.ares.integration.aop.forbidden.subject.fileSystem.copyRoles.CopyRolesMain;

/**
 * Checks that a file copy is judged side by side in all four mode combinations,
 * through {@code Files.copy} and through the file system provider it hands the
 * work to: the source for reading, the target for creating, or for overwriting
 * when it is replaced. The policy lets the source only be read, a second file
 * only be overwritten, one folder only be created in and one only be
 * overwritten, and grants nothing for a last folder.
 */
class FileSystemCopyRolesTest extends SystemAccessTest {

	/** Folder of the student code and of the files it copies. */
	private static final String WITHIN_PATH = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/copyRoles";

	/** Folder holding the files the policy names. */
	private static final Path FILES = Path.of("src", "test", "java", "de", "tum", "cit", "ase", "ares", "integration",
			"aop", "forbidden", "subject", "fileSystem", "copyRoles");

	/** The source, which may only be read. */
	private static final Path SOURCE = FILES.resolve("source.txt");

	/** A file that may be overwritten but not read. */
	private static final Path SECRET = FILES.resolve("secret.txt");

	/** A folder that may only be created in. */
	private static final Path CREATED = FILES.resolve("created");

	/** An existing file in the folder that may only be created in. */
	private static final Path CREATE_ONLY_EXISTING = CREATED.resolve("existing.txt");

	/**
	 * An existing file that may only be overwritten.
	 */
	private static final Path OVERWRITABLE_EXISTING = FILES.resolve("replaced").resolve("existing.txt");

	/** A folder the policy grants nothing for. */
	private static final Path FORBIDDEN = FILES.resolve("forbidden");

	/** The copy-roles policy for MavenArchunitAspectJ. */
	private static final String ARCHUNIT_ASPECTJ_COPY_ROLES_POLICY = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/aspectj/PolicyCopyRoles.yaml";

	/** The copy-roles policy for MavenArchunitInstrumentation. */
	private static final String ARCHUNIT_INSTRUMENTATION_COPY_ROLES_POLICY = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/instrumentation/PolicyCopyRoles.yaml";

	/** The copy-roles policy for MavenWalaAspectJ. */
	private static final String WALA_ASPECTJ_COPY_ROLES_POLICY = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/aspectj/PolicyCopyRoles.yaml";

	/** The copy-roles policy for MavenWalaInstrumentation. */
	private static final String WALA_INSTRUMENTATION_COPY_ROLES_POLICY = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/instrumentation/PolicyCopyRoles.yaml";

	/**
	 * A copy taking a source, a target and options, as the student code offers it.
	 */
	@FunctionalInterface
	private interface Copy {

		/**
		 * Copies the source to the target.
		 *
		 * @param source  the file to copy
		 * @param target  the copy
		 * @param options how to copy
		 * @throws IOException if the copy fails
		 */
		void copy(Path source, Path target, CopyOption... options) throws IOException;
	}

	// <editor-fold desc="newCopyWithFiles">
	/**
	 * A readable source is copied with {@code Files.copy} into a folder that may
	 * only be created in, in MavenArchunitAspectJ.
	 */
	@PublicTest
	@Policy(value = ARCHUNIT_ASPECTJ_COPY_ROLES_POLICY, withinPath = WITHIN_PATH)
	void test_newCopyWithFilesMavenArchunitAspectJ() throws IOException {
		assertNewCopySucceeds(CopyRolesMain::copyWithFiles, "test_newCopyWithFilesMavenArchunitAspectJ");
	}

	/**
	 * A readable source is copied with {@code Files.copy} into a folder that may
	 * only be created in, in MavenArchunitInstrumentation.
	 */
	@PublicTest
	@Policy(value = ARCHUNIT_INSTRUMENTATION_COPY_ROLES_POLICY, withinPath = WITHIN_PATH)
	void test_newCopyWithFilesMavenArchunitInstrumentation() throws IOException {
		assertNewCopySucceeds(CopyRolesMain::copyWithFiles, "test_newCopyWithFilesMavenArchunitInstrumentation");
	}

	/**
	 * A readable source is copied with {@code Files.copy} into a folder that may
	 * only be created in, in MavenWalaAspectJ.
	 */
	@PublicTest
	@Policy(value = WALA_ASPECTJ_COPY_ROLES_POLICY, withinPath = WITHIN_PATH)
	void test_newCopyWithFilesMavenWalaAspectJ() throws IOException {
		assertNewCopySucceeds(CopyRolesMain::copyWithFiles, "test_newCopyWithFilesMavenWalaAspectJ");
	}

	/**
	 * A readable source is copied with {@code Files.copy} into a folder that may
	 * only be created in, in MavenWalaInstrumentation.
	 */
	@PublicTest
	@Policy(value = WALA_INSTRUMENTATION_COPY_ROLES_POLICY, withinPath = WITHIN_PATH)
	void test_newCopyWithFilesMavenWalaInstrumentation() throws IOException {
		assertNewCopySucceeds(CopyRolesMain::copyWithFiles, "test_newCopyWithFilesMavenWalaInstrumentation");
	}
	// </editor-fold>

	// <editor-fold desc="newCopyWithProvider">
	/**
	 * A readable source is copied with the provider into a folder that may only be
	 * created in, in MavenArchunitAspectJ.
	 */
	@PublicTest
	@Policy(value = ARCHUNIT_ASPECTJ_COPY_ROLES_POLICY, withinPath = WITHIN_PATH)
	void test_newCopyWithProviderMavenArchunitAspectJ() throws IOException {
		assertNewCopySucceeds(CopyRolesMain::copyWithProvider, "test_newCopyWithProviderMavenArchunitAspectJ");
	}

	/**
	 * A readable source is copied with the provider into a folder that may only be
	 * created in, in MavenArchunitInstrumentation.
	 */
	@PublicTest
	@Policy(value = ARCHUNIT_INSTRUMENTATION_COPY_ROLES_POLICY, withinPath = WITHIN_PATH)
	void test_newCopyWithProviderMavenArchunitInstrumentation() throws IOException {
		assertNewCopySucceeds(CopyRolesMain::copyWithProvider, "test_newCopyWithProviderMavenArchunitInstrumentation");
	}

	/**
	 * A readable source is copied with the provider into a folder that may only be
	 * created in, in MavenWalaAspectJ.
	 */
	@PublicTest
	@Policy(value = WALA_ASPECTJ_COPY_ROLES_POLICY, withinPath = WITHIN_PATH)
	void test_newCopyWithProviderMavenWalaAspectJ() throws IOException {
		assertNewCopySucceeds(CopyRolesMain::copyWithProvider, "test_newCopyWithProviderMavenWalaAspectJ");
	}

	/**
	 * A readable source is copied with the provider into a folder that may only be
	 * created in, in MavenWalaInstrumentation.
	 */
	@PublicTest
	@Policy(value = WALA_INSTRUMENTATION_COPY_ROLES_POLICY, withinPath = WITHIN_PATH)
	void test_newCopyWithProviderMavenWalaInstrumentation() throws IOException {
		assertNewCopySucceeds(CopyRolesMain::copyWithProvider, "test_newCopyWithProviderMavenWalaInstrumentation");
	}
	// </editor-fold>

	// <editor-fold desc="replaceWithFiles">
	/**
	 * A readable source replaces an overwritable target through {@code Files.copy},
	 * in MavenArchunitAspectJ.
	 */
	@PublicTest
	@Policy(value = ARCHUNIT_ASPECTJ_COPY_ROLES_POLICY, withinPath = WITHIN_PATH)
	void test_replaceWithFilesMavenArchunitAspectJ() throws IOException {
		assertReplacementSucceeds(CopyRolesMain::copyWithFiles);
	}

	/**
	 * A readable source replaces an overwritable target through {@code Files.copy},
	 * in MavenArchunitInstrumentation.
	 */
	@PublicTest
	@Policy(value = ARCHUNIT_INSTRUMENTATION_COPY_ROLES_POLICY, withinPath = WITHIN_PATH)
	void test_replaceWithFilesMavenArchunitInstrumentation() throws IOException {
		assertReplacementSucceeds(CopyRolesMain::copyWithFiles);
	}

	/**
	 * A readable source replaces an overwritable target through {@code Files.copy},
	 * in MavenWalaAspectJ.
	 */
	@PublicTest
	@Policy(value = WALA_ASPECTJ_COPY_ROLES_POLICY, withinPath = WITHIN_PATH)
	void test_replaceWithFilesMavenWalaAspectJ() throws IOException {
		assertReplacementSucceeds(CopyRolesMain::copyWithFiles);
	}

	/**
	 * A readable source replaces an overwritable target through {@code Files.copy},
	 * in MavenWalaInstrumentation.
	 */
	@PublicTest
	@Policy(value = WALA_INSTRUMENTATION_COPY_ROLES_POLICY, withinPath = WITHIN_PATH)
	void test_replaceWithFilesMavenWalaInstrumentation() throws IOException {
		assertReplacementSucceeds(CopyRolesMain::copyWithFiles);
	}
	// </editor-fold>

	// <editor-fold desc="replaceWithProvider">
	/**
	 * A readable source replaces an overwritable target through the provider, in
	 * MavenArchunitAspectJ.
	 */
	@PublicTest
	@Policy(value = ARCHUNIT_ASPECTJ_COPY_ROLES_POLICY, withinPath = WITHIN_PATH)
	void test_replaceWithProviderMavenArchunitAspectJ() throws IOException {
		assertReplacementSucceeds(CopyRolesMain::copyWithProvider);
	}

	/**
	 * A readable source replaces an overwritable target through the provider, in
	 * MavenArchunitInstrumentation.
	 */
	@PublicTest
	@Policy(value = ARCHUNIT_INSTRUMENTATION_COPY_ROLES_POLICY, withinPath = WITHIN_PATH)
	void test_replaceWithProviderMavenArchunitInstrumentation() throws IOException {
		assertReplacementSucceeds(CopyRolesMain::copyWithProvider);
	}

	/**
	 * A readable source replaces an overwritable target through the provider, in
	 * MavenWalaAspectJ.
	 */
	@PublicTest
	@Policy(value = WALA_ASPECTJ_COPY_ROLES_POLICY, withinPath = WITHIN_PATH)
	void test_replaceWithProviderMavenWalaAspectJ() throws IOException {
		assertReplacementSucceeds(CopyRolesMain::copyWithProvider);
	}

	/**
	 * A readable source replaces an overwritable target through the provider, in
	 * MavenWalaInstrumentation.
	 */
	@PublicTest
	@Policy(value = WALA_INSTRUMENTATION_COPY_ROLES_POLICY, withinPath = WITHIN_PATH)
	void test_replaceWithProviderMavenWalaInstrumentation() throws IOException {
		assertReplacementSucceeds(CopyRolesMain::copyWithProvider);
	}
	// </editor-fold>

	// <editor-fold desc="replaceCreateOnlyWithFiles">
	/**
	 * Replacing a target that may only be created is refused through
	 * {@code Files.copy}, in MavenArchunitAspectJ.
	 */
	@PublicTest
	@Policy(value = ARCHUNIT_ASPECTJ_COPY_ROLES_POLICY, withinPath = WITHIN_PATH)
	void test_replaceCreateOnlyWithFilesMavenArchunitAspectJ() throws IOException {
		assertReplacingCreateOnlyTargetDenied(CopyRolesMain::copyWithFiles);
	}

	/**
	 * Replacing a target that may only be created is refused through
	 * {@code Files.copy}, in MavenArchunitInstrumentation.
	 */
	@PublicTest
	@Policy(value = ARCHUNIT_INSTRUMENTATION_COPY_ROLES_POLICY, withinPath = WITHIN_PATH)
	void test_replaceCreateOnlyWithFilesMavenArchunitInstrumentation() throws IOException {
		assertReplacingCreateOnlyTargetDenied(CopyRolesMain::copyWithFiles);
	}

	/**
	 * Replacing a target that may only be created is refused through
	 * {@code Files.copy}, in MavenWalaAspectJ.
	 */
	@PublicTest
	@Policy(value = WALA_ASPECTJ_COPY_ROLES_POLICY, withinPath = WITHIN_PATH)
	void test_replaceCreateOnlyWithFilesMavenWalaAspectJ() throws IOException {
		assertReplacingCreateOnlyTargetDenied(CopyRolesMain::copyWithFiles);
	}

	/**
	 * Replacing a target that may only be created is refused through
	 * {@code Files.copy}, in MavenWalaInstrumentation.
	 */
	@PublicTest
	@Policy(value = WALA_INSTRUMENTATION_COPY_ROLES_POLICY, withinPath = WITHIN_PATH)
	void test_replaceCreateOnlyWithFilesMavenWalaInstrumentation() throws IOException {
		assertReplacingCreateOnlyTargetDenied(CopyRolesMain::copyWithFiles);
	}
	// </editor-fold>

	// <editor-fold desc="replaceCreateOnlyWithProvider">
	/**
	 * Replacing a target that may only be created is refused through the provider,
	 * in MavenArchunitAspectJ.
	 */
	@PublicTest
	@Policy(value = ARCHUNIT_ASPECTJ_COPY_ROLES_POLICY, withinPath = WITHIN_PATH)
	void test_replaceCreateOnlyWithProviderMavenArchunitAspectJ() throws IOException {
		assertReplacingCreateOnlyTargetDenied(CopyRolesMain::copyWithProvider);
	}

	/**
	 * Replacing a target that may only be created is refused through the provider,
	 * in MavenArchunitInstrumentation.
	 */
	@PublicTest
	@Policy(value = ARCHUNIT_INSTRUMENTATION_COPY_ROLES_POLICY, withinPath = WITHIN_PATH)
	void test_replaceCreateOnlyWithProviderMavenArchunitInstrumentation() throws IOException {
		assertReplacingCreateOnlyTargetDenied(CopyRolesMain::copyWithProvider);
	}

	/**
	 * Replacing a target that may only be created is refused through the provider,
	 * in MavenWalaAspectJ.
	 */
	@PublicTest
	@Policy(value = WALA_ASPECTJ_COPY_ROLES_POLICY, withinPath = WITHIN_PATH)
	void test_replaceCreateOnlyWithProviderMavenWalaAspectJ() throws IOException {
		assertReplacingCreateOnlyTargetDenied(CopyRolesMain::copyWithProvider);
	}

	/**
	 * Replacing a target that may only be created is refused through the provider,
	 * in MavenWalaInstrumentation.
	 */
	@PublicTest
	@Policy(value = WALA_INSTRUMENTATION_COPY_ROLES_POLICY, withinPath = WITHIN_PATH)
	void test_replaceCreateOnlyWithProviderMavenWalaInstrumentation() throws IOException {
		assertReplacingCreateOnlyTargetDenied(CopyRolesMain::copyWithProvider);
	}
	// </editor-fold>

	// <editor-fold desc="forbiddenTargetWithFiles">
	/**
	 * Copying into a folder without any permission is refused through
	 * {@code Files.copy}, in MavenArchunitAspectJ.
	 */
	@PublicTest
	@Policy(value = ARCHUNIT_ASPECTJ_COPY_ROLES_POLICY, withinPath = WITHIN_PATH)
	void test_forbiddenTargetWithFilesMavenArchunitAspectJ() throws IOException {
		assertForbiddenTargetDenied(CopyRolesMain::copyWithFiles, "test_forbiddenTargetWithFilesMavenArchunitAspectJ");
	}

	/**
	 * Copying into a folder without any permission is refused through
	 * {@code Files.copy}, in MavenArchunitInstrumentation.
	 */
	@PublicTest
	@Policy(value = ARCHUNIT_INSTRUMENTATION_COPY_ROLES_POLICY, withinPath = WITHIN_PATH)
	void test_forbiddenTargetWithFilesMavenArchunitInstrumentation() throws IOException {
		assertForbiddenTargetDenied(CopyRolesMain::copyWithFiles,
				"test_forbiddenTargetWithFilesMavenArchunitInstrumentation");
	}

	/**
	 * Copying into a folder without any permission is refused through
	 * {@code Files.copy}, in MavenWalaAspectJ.
	 */
	@PublicTest
	@Policy(value = WALA_ASPECTJ_COPY_ROLES_POLICY, withinPath = WITHIN_PATH)
	void test_forbiddenTargetWithFilesMavenWalaAspectJ() throws IOException {
		assertForbiddenTargetDenied(CopyRolesMain::copyWithFiles, "test_forbiddenTargetWithFilesMavenWalaAspectJ");
	}

	/**
	 * Copying into a folder without any permission is refused through
	 * {@code Files.copy}, in MavenWalaInstrumentation.
	 */
	@PublicTest
	@Policy(value = WALA_INSTRUMENTATION_COPY_ROLES_POLICY, withinPath = WITHIN_PATH)
	void test_forbiddenTargetWithFilesMavenWalaInstrumentation() throws IOException {
		assertForbiddenTargetDenied(CopyRolesMain::copyWithFiles,
				"test_forbiddenTargetWithFilesMavenWalaInstrumentation");
	}
	// </editor-fold>

	// <editor-fold desc="forbiddenTargetWithProvider">
	/**
	 * Copying into a folder without any permission is refused through the provider,
	 * in MavenArchunitAspectJ.
	 */
	@PublicTest
	@Policy(value = ARCHUNIT_ASPECTJ_COPY_ROLES_POLICY, withinPath = WITHIN_PATH)
	void test_forbiddenTargetWithProviderMavenArchunitAspectJ() throws IOException {
		assertForbiddenTargetDenied(CopyRolesMain::copyWithProvider,
				"test_forbiddenTargetWithProviderMavenArchunitAspectJ");
	}

	/**
	 * Copying into a folder without any permission is refused through the provider,
	 * in MavenArchunitInstrumentation.
	 */
	@PublicTest
	@Policy(value = ARCHUNIT_INSTRUMENTATION_COPY_ROLES_POLICY, withinPath = WITHIN_PATH)
	void test_forbiddenTargetWithProviderMavenArchunitInstrumentation() throws IOException {
		assertForbiddenTargetDenied(CopyRolesMain::copyWithProvider,
				"test_forbiddenTargetWithProviderMavenArchunitInstrumentation");
	}

	/**
	 * Copying into a folder without any permission is refused through the provider,
	 * in MavenWalaAspectJ.
	 */
	@PublicTest
	@Policy(value = WALA_ASPECTJ_COPY_ROLES_POLICY, withinPath = WITHIN_PATH)
	void test_forbiddenTargetWithProviderMavenWalaAspectJ() throws IOException {
		assertForbiddenTargetDenied(CopyRolesMain::copyWithProvider,
				"test_forbiddenTargetWithProviderMavenWalaAspectJ");
	}

	/**
	 * Copying into a folder without any permission is refused through the provider,
	 * in MavenWalaInstrumentation.
	 */
	@PublicTest
	@Policy(value = WALA_INSTRUMENTATION_COPY_ROLES_POLICY, withinPath = WITHIN_PATH)
	void test_forbiddenTargetWithProviderMavenWalaInstrumentation() throws IOException {
		assertForbiddenTargetDenied(CopyRolesMain::copyWithProvider,
				"test_forbiddenTargetWithProviderMavenWalaInstrumentation");
	}
	// </editor-fold>

	// <editor-fold desc="unreadableSourceWithFiles">
	/**
	 * A source that may be overwritten but not read is refused for reading through
	 * {@code Files.copy}, in MavenArchunitAspectJ.
	 */
	@PublicTest
	@Policy(value = ARCHUNIT_ASPECTJ_COPY_ROLES_POLICY, withinPath = WITHIN_PATH)
	void test_unreadableSourceWithFilesMavenArchunitAspectJ() throws IOException {
		assertUnreadableSourceDenied(CopyRolesMain::copyWithFiles,
				"test_unreadableSourceWithFilesMavenArchunitAspectJ");
	}

	/**
	 * A source that may be overwritten but not read is refused for reading through
	 * {@code Files.copy}, in MavenArchunitInstrumentation.
	 */
	@PublicTest
	@Policy(value = ARCHUNIT_INSTRUMENTATION_COPY_ROLES_POLICY, withinPath = WITHIN_PATH)
	void test_unreadableSourceWithFilesMavenArchunitInstrumentation() throws IOException {
		assertUnreadableSourceDenied(CopyRolesMain::copyWithFiles,
				"test_unreadableSourceWithFilesMavenArchunitInstrumentation");
	}

	/**
	 * A source that may be overwritten but not read is refused for reading through
	 * {@code Files.copy}, in MavenWalaAspectJ.
	 */
	@PublicTest
	@Policy(value = WALA_ASPECTJ_COPY_ROLES_POLICY, withinPath = WITHIN_PATH)
	void test_unreadableSourceWithFilesMavenWalaAspectJ() throws IOException {
		assertUnreadableSourceDenied(CopyRolesMain::copyWithFiles, "test_unreadableSourceWithFilesMavenWalaAspectJ");
	}

	/**
	 * A source that may be overwritten but not read is refused for reading through
	 * {@code Files.copy}, in MavenWalaInstrumentation.
	 */
	@PublicTest
	@Policy(value = WALA_INSTRUMENTATION_COPY_ROLES_POLICY, withinPath = WITHIN_PATH)
	void test_unreadableSourceWithFilesMavenWalaInstrumentation() throws IOException {
		assertUnreadableSourceDenied(CopyRolesMain::copyWithFiles,
				"test_unreadableSourceWithFilesMavenWalaInstrumentation");
	}
	// </editor-fold>

	// <editor-fold desc="unreadableSourceWithProvider">
	/**
	 * A source that may be overwritten but not read is refused for reading through
	 * the provider, in MavenArchunitAspectJ.
	 */
	@PublicTest
	@Policy(value = ARCHUNIT_ASPECTJ_COPY_ROLES_POLICY, withinPath = WITHIN_PATH)
	void test_unreadableSourceWithProviderMavenArchunitAspectJ() throws IOException {
		assertUnreadableSourceDenied(CopyRolesMain::copyWithProvider,
				"test_unreadableSourceWithProviderMavenArchunitAspectJ");
	}

	/**
	 * A source that may be overwritten but not read is refused for reading through
	 * the provider, in MavenArchunitInstrumentation.
	 */
	@PublicTest
	@Policy(value = ARCHUNIT_INSTRUMENTATION_COPY_ROLES_POLICY, withinPath = WITHIN_PATH)
	void test_unreadableSourceWithProviderMavenArchunitInstrumentation() throws IOException {
		assertUnreadableSourceDenied(CopyRolesMain::copyWithProvider,
				"test_unreadableSourceWithProviderMavenArchunitInstrumentation");
	}

	/**
	 * A source that may be overwritten but not read is refused for reading through
	 * the provider, in MavenWalaAspectJ.
	 */
	@PublicTest
	@Policy(value = WALA_ASPECTJ_COPY_ROLES_POLICY, withinPath = WITHIN_PATH)
	void test_unreadableSourceWithProviderMavenWalaAspectJ() throws IOException {
		assertUnreadableSourceDenied(CopyRolesMain::copyWithProvider,
				"test_unreadableSourceWithProviderMavenWalaAspectJ");
	}

	/**
	 * A source that may be overwritten but not read is refused for reading through
	 * the provider, in MavenWalaInstrumentation.
	 */
	@PublicTest
	@Policy(value = WALA_INSTRUMENTATION_COPY_ROLES_POLICY, withinPath = WITHIN_PATH)
	void test_unreadableSourceWithProviderMavenWalaInstrumentation() throws IOException {
		assertUnreadableSourceDenied(CopyRolesMain::copyWithProvider,
				"test_unreadableSourceWithProviderMavenWalaInstrumentation");
	}
	// </editor-fold>

	/**
	 * Copies the source to a new file in the folder that may only be created in,
	 * expects no refusal and the source content, and deletes the copy.
	 *
	 * @param copy     the way of copying
	 * @param fileName a name no other test uses
	 * @throws IOException if the copy cannot be read or deleted
	 */
	private static void assertNewCopySucceeds(Copy copy, String fileName) throws IOException {
		Path target = CREATED.resolve(fileName + ".txt");
		Files.deleteIfExists(target);
		try {
			Assertions.assertDoesNotThrow(() -> copy.copy(SOURCE, target));
			Assertions.assertEquals(Files.readString(SOURCE), Files.readString(target));
		} finally {
			Files.deleteIfExists(target);
		}
	}

	/**
	 * Gives the file that may only be overwritten other content, replaces it with
	 * the source, expects no refusal and the source content, and puts the file back
	 * as it was.
	 *
	 * @param copy the way of copying
	 * @throws IOException if the file cannot be read or written
	 */
	private static void assertReplacementSucceeds(Copy copy) throws IOException {
		String checkedIn = Files.readString(OVERWRITABLE_EXISTING);
		Files.writeString(OVERWRITABLE_EXISTING, "content to be replaced");
		try {
			Assertions.assertDoesNotThrow(
					() -> copy.copy(SOURCE, OVERWRITABLE_EXISTING, StandardCopyOption.REPLACE_EXISTING));
			Assertions.assertEquals(Files.readString(SOURCE), Files.readString(OVERWRITABLE_EXISTING));
		} finally {
			Files.writeString(OVERWRITABLE_EXISTING, checkedIn);
		}
	}

	/**
	 * Tries to replace an existing file that may only be created and expects a
	 * refusal to overwrite it, with the file unchanged.
	 *
	 * @param copy the way of copying
	 * @throws IOException if the file cannot be read
	 */
	private void assertReplacingCreateOnlyTargetDenied(Copy copy) throws IOException {
		String before = Files.readString(CREATE_ONLY_EXISTING);
		assertAresSecurityExceptionOverwrite(
				() -> copy.copy(SOURCE, CREATE_ONLY_EXISTING, StandardCopyOption.REPLACE_EXISTING), CopyRolesMain.class,
				CREATE_ONLY_EXISTING);
		Assertions.assertEquals(before, Files.readString(CREATE_ONLY_EXISTING));
	}

	/**
	 * Tries to copy into the folder without permissions and expects a refusal to
	 * create the target, with no target left behind.
	 *
	 * @param copy     the way of copying
	 * @param fileName a name no other test uses
	 * @throws IOException if a left-over target cannot be deleted
	 */
	private void assertForbiddenTargetDenied(Copy copy, String fileName) throws IOException {
		Path target = FORBIDDEN.resolve(fileName + ".txt");
		try {
			assertAresSecurityExceptionCreate(() -> copy.copy(SOURCE, target), CopyRolesMain.class, target);
			Assertions.assertFalse(Files.exists(target), "the refused copy must not exist");
		} finally {
			Files.deleteIfExists(target);
		}
	}

	/**
	 * Tries to copy the file that may be overwritten but not read and expects a
	 * refusal to read it, with no target left behind.
	 *
	 * @param copy     the way of copying
	 * @param fileName a name no other test uses
	 * @throws IOException if a left-over target cannot be deleted
	 */
	private void assertUnreadableSourceDenied(Copy copy, String fileName) throws IOException {
		Path target = CREATED.resolve(fileName + ".txt");
		try {
			assertAresSecurityExceptionRead(() -> copy.copy(SECRET, target), CopyRolesMain.class, SECRET);
			Assertions.assertFalse(Files.exists(target), "the refused copy must not exist");
		} finally {
			Files.deleteIfExists(target);
		}
	}
}
