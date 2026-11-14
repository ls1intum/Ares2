package de.tum.cit.ase.ares.integration.aop.forbidden;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

import javax.swing.filechooser.FileSystemView;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;

import de.tum.cit.ase.ares.api.Policy;
import de.tum.cit.ase.ares.api.jupiter.PublicTest;
import de.tum.cit.ase.ares.integration.aop.forbidden.subject.fileSystem.fileSystemView.FileSystemViewAccess;

/**
 * Checks that listing and creating folders through FileSystemView obeys the
 * policy the same way in all four mode combinations: refused outside the
 * permitted folder, allowed inside it.
 */
class FileSystemViewAccessTest extends SystemAccessTest {

	/** The compiled subjects the policies supervise. */
	private static final String WITHIN_PATH = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/fileSystemView";

	/** The forbidden folder as the refusal names it. */
	private static final Path FORBIDDEN = FileSystemViewAccess.FORBIDDEN.toPath();

	/** The policy for archunit with aspectj. */
	private static final String ARCHUNIT_ASPECTJ_POLICY = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/aspectj/PolicyFileSystemView.yaml";

	/** The policy for archunit with instrumentation. */
	private static final String ARCHUNIT_INSTRUMENTATION_POLICY = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/instrumentation/PolicyFileSystemView.yaml";

	/** The policy for wala with aspectj. */
	private static final String WALA_ASPECTJ_POLICY = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/aspectj/PolicyFileSystemView.yaml";

	/** The policy for wala with instrumentation. */
	private static final String WALA_INSTRUMENTATION_POLICY = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/instrumentation/PolicyFileSystemView.yaml";

	/**
	 * Sets up the JDK's file system view once, before any policy is armed. On a
	 * desktop JVM that first call starts the graphics system and a thread, which a
	 * policy without thread creations rightly refuses, and the JDK would then never
	 * finish setting the view up. That start-up is not what these tests check.
	 */
	@BeforeAll
	static void setUpTheFileSystemView() {
		FileSystemView.getFileSystemView();
	}

	/**
	 * Removes the folders the create tests made, so every run starts from the
	 * committed entry alone.
	 *
	 * @throws IOException if a folder cannot be removed
	 */
	@AfterEach
	void removeCreatedFolders() throws IOException {
		for (File folder : List.of(FileSystemViewAccess.PERMITTED, FileSystemViewAccess.FORBIDDEN)) {
			try (Stream<Path> entries = Files.list(folder.toPath())) {
				for (Path entry : entries.filter(Files::isDirectory).toList()) {
					try (Stream<Path> tree = Files.walk(entry)) {
						for (Path path : tree.sorted(Comparator.reverseOrder()).toList()) {
							Files.delete(path);
						}
					}
				}
			}
		}
	}

	/**
	 * Asserts that a folder still holds its committed entry and nothing else.
	 *
	 * @param folder the folder to look in
	 */
	private static void assertOnlyTheEntryIn(File folder) {
		assertEquals(List.of("entry.txt"), Arrays.stream(folder.list()).sorted().toList());
	}

	// <editor-fold desc="listsAForbiddenFolder">
	/** Checks that getFiles on a forbidden folder is refused. */
	@PublicTest
	@Policy(value = ARCHUNIT_ASPECTJ_POLICY, withinPath = WITHIN_PATH)
	void test_listsAForbiddenFolderMavenArchunitAspectJ() {
		assertAresSecurityExceptionRead(() -> FileSystemViewAccess.list(FileSystemViewAccess.FORBIDDEN),
				FileSystemViewAccess.class, FORBIDDEN);
	}

	/** Checks that getFiles on a forbidden folder is refused. */
	@PublicTest
	@Policy(value = ARCHUNIT_INSTRUMENTATION_POLICY, withinPath = WITHIN_PATH)
	void test_listsAForbiddenFolderMavenArchunitInstrumentation() {
		assertAresSecurityExceptionRead(() -> FileSystemViewAccess.list(FileSystemViewAccess.FORBIDDEN),
				FileSystemViewAccess.class, FORBIDDEN);
	}

	/** Checks that getFiles on a forbidden folder is refused. */
	@PublicTest
	@Policy(value = WALA_ASPECTJ_POLICY, withinPath = WITHIN_PATH)
	void test_listsAForbiddenFolderMavenWalaAspectJ() {
		assertAresSecurityExceptionRead(() -> FileSystemViewAccess.list(FileSystemViewAccess.FORBIDDEN),
				FileSystemViewAccess.class, FORBIDDEN);
	}

	/** Checks that getFiles on a forbidden folder is refused. */
	@PublicTest
	@Policy(value = WALA_INSTRUMENTATION_POLICY, withinPath = WITHIN_PATH)
	void test_listsAForbiddenFolderMavenWalaInstrumentation() {
		assertAresSecurityExceptionRead(() -> FileSystemViewAccess.list(FileSystemViewAccess.FORBIDDEN),
				FileSystemViewAccess.class, FORBIDDEN);
	}

	// </editor-fold>

	// <editor-fold desc="looksInsideTheDefaultDirectory">
	/** Checks that getChild on a special folder outside the policy is refused. */
	@PublicTest
	@Policy(value = ARCHUNIT_ASPECTJ_POLICY, withinPath = WITHIN_PATH)
	void test_looksInsideTheDefaultDirectoryMavenArchunitAspectJ() {
		assertAresSecurityExceptionRead(() -> FileSystemViewAccess.childOfDefaultDirectory("x"),
				FileSystemViewAccess.class, FileSystemView.getFileSystemView().getDefaultDirectory().toPath());
	}

	/** Checks that getChild on a special folder outside the policy is refused. */
	@PublicTest
	@Policy(value = ARCHUNIT_INSTRUMENTATION_POLICY, withinPath = WITHIN_PATH)
	void test_looksInsideTheDefaultDirectoryMavenArchunitInstrumentation() {
		assertAresSecurityExceptionRead(() -> FileSystemViewAccess.childOfDefaultDirectory("x"),
				FileSystemViewAccess.class, FileSystemView.getFileSystemView().getDefaultDirectory().toPath());
	}

	/** Checks that getChild on a special folder outside the policy is refused. */
	@PublicTest
	@Policy(value = WALA_ASPECTJ_POLICY, withinPath = WITHIN_PATH)
	void test_looksInsideTheDefaultDirectoryMavenWalaAspectJ() {
		assertAresSecurityExceptionRead(() -> FileSystemViewAccess.childOfDefaultDirectory("x"),
				FileSystemViewAccess.class, FileSystemView.getFileSystemView().getDefaultDirectory().toPath());
	}

	/** Checks that getChild on a special folder outside the policy is refused. */
	@PublicTest
	@Policy(value = WALA_INSTRUMENTATION_POLICY, withinPath = WITHIN_PATH)
	void test_looksInsideTheDefaultDirectoryMavenWalaInstrumentation() {
		assertAresSecurityExceptionRead(() -> FileSystemViewAccess.childOfDefaultDirectory("x"),
				FileSystemViewAccess.class, FileSystemView.getFileSystemView().getDefaultDirectory().toPath());
	}

	// </editor-fold>

	// <editor-fold desc="looksInsideAForbiddenFolder">
	/** Checks that getChild on a forbidden folder is refused. */
	@PublicTest
	@Policy(value = ARCHUNIT_ASPECTJ_POLICY, withinPath = WITHIN_PATH)
	void test_looksInsideAForbiddenFolderMavenArchunitAspectJ() {
		assertAresSecurityExceptionRead(() -> FileSystemViewAccess.child(FileSystemViewAccess.FORBIDDEN, "entry.txt"),
				FileSystemViewAccess.class, FORBIDDEN);
	}

	/** Checks that getChild on a forbidden folder is refused. */
	@PublicTest
	@Policy(value = ARCHUNIT_INSTRUMENTATION_POLICY, withinPath = WITHIN_PATH)
	void test_looksInsideAForbiddenFolderMavenArchunitInstrumentation() {
		assertAresSecurityExceptionRead(() -> FileSystemViewAccess.child(FileSystemViewAccess.FORBIDDEN, "entry.txt"),
				FileSystemViewAccess.class, FORBIDDEN);
	}

	/** Checks that getChild on a forbidden folder is refused. */
	@PublicTest
	@Policy(value = WALA_ASPECTJ_POLICY, withinPath = WITHIN_PATH)
	void test_looksInsideAForbiddenFolderMavenWalaAspectJ() {
		assertAresSecurityExceptionRead(() -> FileSystemViewAccess.child(FileSystemViewAccess.FORBIDDEN, "entry.txt"),
				FileSystemViewAccess.class, FORBIDDEN);
	}

	/** Checks that getChild on a forbidden folder is refused. */
	@PublicTest
	@Policy(value = WALA_INSTRUMENTATION_POLICY, withinPath = WITHIN_PATH)
	void test_looksInsideAForbiddenFolderMavenWalaInstrumentation() {
		assertAresSecurityExceptionRead(() -> FileSystemViewAccess.child(FileSystemViewAccess.FORBIDDEN, "entry.txt"),
				FileSystemViewAccess.class, FORBIDDEN);
	}

	// </editor-fold>

	// <editor-fold desc="asksWhetherAForbiddenFolderContainsAnEntry">
	/** Checks that isParent on a forbidden folder is refused. */
	@PublicTest
	@Policy(value = ARCHUNIT_ASPECTJ_POLICY, withinPath = WITHIN_PATH)
	void test_asksWhetherAForbiddenFolderContainsAnEntryMavenArchunitAspectJ() {
		assertAresSecurityExceptionRead(() -> FileSystemViewAccess.containsEntry(FileSystemViewAccess.FORBIDDEN),
				FileSystemViewAccess.class, FORBIDDEN);
	}

	/** Checks that isParent on a forbidden folder is refused. */
	@PublicTest
	@Policy(value = ARCHUNIT_INSTRUMENTATION_POLICY, withinPath = WITHIN_PATH)
	void test_asksWhetherAForbiddenFolderContainsAnEntryMavenArchunitInstrumentation() {
		assertAresSecurityExceptionRead(() -> FileSystemViewAccess.containsEntry(FileSystemViewAccess.FORBIDDEN),
				FileSystemViewAccess.class, FORBIDDEN);
	}

	/** Checks that isParent on a forbidden folder is refused. */
	@PublicTest
	@Policy(value = WALA_ASPECTJ_POLICY, withinPath = WITHIN_PATH)
	void test_asksWhetherAForbiddenFolderContainsAnEntryMavenWalaAspectJ() {
		assertAresSecurityExceptionRead(() -> FileSystemViewAccess.containsEntry(FileSystemViewAccess.FORBIDDEN),
				FileSystemViewAccess.class, FORBIDDEN);
	}

	/** Checks that isParent on a forbidden folder is refused. */
	@PublicTest
	@Policy(value = WALA_INSTRUMENTATION_POLICY, withinPath = WITHIN_PATH)
	void test_asksWhetherAForbiddenFolderContainsAnEntryMavenWalaInstrumentation() {
		assertAresSecurityExceptionRead(() -> FileSystemViewAccess.containsEntry(FileSystemViewAccess.FORBIDDEN),
				FileSystemViewAccess.class, FORBIDDEN);
	}

	// </editor-fold>

	// <editor-fold desc="createsAFolderInAForbiddenFolder">
	/**
	 * Checks that createNewFolder in a forbidden folder is refused and creates
	 * nothing.
	 */
	@PublicTest
	@Policy(value = ARCHUNIT_ASPECTJ_POLICY, withinPath = WITHIN_PATH)
	void test_createsAFolderInAForbiddenFolderMavenArchunitAspectJ() {
		assertAresSecurityExceptionCreate(() -> FileSystemViewAccess.newFolderIn(FileSystemViewAccess.FORBIDDEN),
				FileSystemViewAccess.class, FORBIDDEN);
		assertOnlyTheEntryIn(FileSystemViewAccess.FORBIDDEN);
	}

	/**
	 * Checks that createNewFolder in a forbidden folder is refused and creates
	 * nothing.
	 */
	@PublicTest
	@Policy(value = ARCHUNIT_INSTRUMENTATION_POLICY, withinPath = WITHIN_PATH)
	void test_createsAFolderInAForbiddenFolderMavenArchunitInstrumentation() {
		assertAresSecurityExceptionCreate(() -> FileSystemViewAccess.newFolderIn(FileSystemViewAccess.FORBIDDEN),
				FileSystemViewAccess.class, FORBIDDEN);
		assertOnlyTheEntryIn(FileSystemViewAccess.FORBIDDEN);
	}

	/**
	 * Checks that createNewFolder in a forbidden folder is refused and creates
	 * nothing.
	 */
	@PublicTest
	@Policy(value = WALA_ASPECTJ_POLICY, withinPath = WITHIN_PATH)
	void test_createsAFolderInAForbiddenFolderMavenWalaAspectJ() {
		assertAresSecurityExceptionCreate(() -> FileSystemViewAccess.newFolderIn(FileSystemViewAccess.FORBIDDEN),
				FileSystemViewAccess.class, FORBIDDEN);
		assertOnlyTheEntryIn(FileSystemViewAccess.FORBIDDEN);
	}

	/**
	 * Checks that createNewFolder in a forbidden folder is refused and creates
	 * nothing.
	 */
	@PublicTest
	@Policy(value = WALA_INSTRUMENTATION_POLICY, withinPath = WITHIN_PATH)
	void test_createsAFolderInAForbiddenFolderMavenWalaInstrumentation() {
		assertAresSecurityExceptionCreate(() -> FileSystemViewAccess.newFolderIn(FileSystemViewAccess.FORBIDDEN),
				FileSystemViewAccess.class, FORBIDDEN);
		assertOnlyTheEntryIn(FileSystemViewAccess.FORBIDDEN);
	}

	// </editor-fold>

	// <editor-fold desc="looksInsideAForbiddenFolderThroughItsOwnView">
	/**
	 * Checks that getChild through a student subclass on a forbidden folder is
	 * refused.
	 */
	@PublicTest
	@Policy(value = ARCHUNIT_ASPECTJ_POLICY, withinPath = WITHIN_PATH)
	void test_looksInsideAForbiddenFolderThroughItsOwnViewMavenArchunitAspectJ() {
		assertAresSecurityExceptionRead(
				() -> FileSystemViewAccess.childThroughOwnView(FileSystemViewAccess.FORBIDDEN, "entry.txt"),
				FileSystemViewAccess.class, FORBIDDEN);
	}

	/**
	 * Checks that getChild through a student subclass on a forbidden folder is
	 * refused.
	 */
	@PublicTest
	@Policy(value = ARCHUNIT_INSTRUMENTATION_POLICY, withinPath = WITHIN_PATH)
	void test_looksInsideAForbiddenFolderThroughItsOwnViewMavenArchunitInstrumentation() {
		assertAresSecurityExceptionRead(
				() -> FileSystemViewAccess.childThroughOwnView(FileSystemViewAccess.FORBIDDEN, "entry.txt"),
				FileSystemViewAccess.class, FORBIDDEN);
	}

	/**
	 * Checks that getChild through a student subclass on a forbidden folder is
	 * refused.
	 */
	@PublicTest
	@Policy(value = WALA_ASPECTJ_POLICY, withinPath = WITHIN_PATH)
	void test_looksInsideAForbiddenFolderThroughItsOwnViewMavenWalaAspectJ() {
		assertAresSecurityExceptionRead(
				() -> FileSystemViewAccess.childThroughOwnView(FileSystemViewAccess.FORBIDDEN, "entry.txt"),
				FileSystemViewAccess.class, FORBIDDEN);
	}

	/**
	 * Checks that getChild through a student subclass on a forbidden folder is
	 * refused.
	 */
	@PublicTest
	@Policy(value = WALA_INSTRUMENTATION_POLICY, withinPath = WITHIN_PATH)
	void test_looksInsideAForbiddenFolderThroughItsOwnViewMavenWalaInstrumentation() {
		assertAresSecurityExceptionRead(
				() -> FileSystemViewAccess.childThroughOwnView(FileSystemViewAccess.FORBIDDEN, "entry.txt"),
				FileSystemViewAccess.class, FORBIDDEN);
	}

	// </editor-fold>

	// <editor-fold desc="listsAPermittedFolder">
	/** Checks that getFiles on a permitted folder lists it. */
	@PublicTest
	@Policy(value = ARCHUNIT_ASPECTJ_POLICY, withinPath = WITHIN_PATH)
	void test_listsAPermittedFolderMavenArchunitAspectJ() {
		File[] entries = assertDoesNotThrow(() -> FileSystemViewAccess.list(FileSystemViewAccess.PERMITTED));
		assertTrue(Arrays.stream(entries).anyMatch(entry -> "entry.txt".equals(entry.getName())));
	}

	/** Checks that getFiles on a permitted folder lists it. */
	@PublicTest
	@Policy(value = ARCHUNIT_INSTRUMENTATION_POLICY, withinPath = WITHIN_PATH)
	void test_listsAPermittedFolderMavenArchunitInstrumentation() {
		File[] entries = assertDoesNotThrow(() -> FileSystemViewAccess.list(FileSystemViewAccess.PERMITTED));
		assertTrue(Arrays.stream(entries).anyMatch(entry -> "entry.txt".equals(entry.getName())));
	}

	/** Checks that getFiles on a permitted folder lists it. */
	@PublicTest
	@Policy(value = WALA_ASPECTJ_POLICY, withinPath = WITHIN_PATH)
	void test_listsAPermittedFolderMavenWalaAspectJ() {
		File[] entries = assertDoesNotThrow(() -> FileSystemViewAccess.list(FileSystemViewAccess.PERMITTED));
		assertTrue(Arrays.stream(entries).anyMatch(entry -> "entry.txt".equals(entry.getName())));
	}

	/** Checks that getFiles on a permitted folder lists it. */
	@PublicTest
	@Policy(value = WALA_INSTRUMENTATION_POLICY, withinPath = WITHIN_PATH)
	void test_listsAPermittedFolderMavenWalaInstrumentation() {
		File[] entries = assertDoesNotThrow(() -> FileSystemViewAccess.list(FileSystemViewAccess.PERMITTED));
		assertTrue(Arrays.stream(entries).anyMatch(entry -> "entry.txt".equals(entry.getName())));
	}

	// </editor-fold>

	// <editor-fold desc="looksInsideAPermittedFolder">
	/** Checks that getChild on a permitted folder finds the entry. */
	@PublicTest
	@Policy(value = ARCHUNIT_ASPECTJ_POLICY, withinPath = WITHIN_PATH)
	void test_looksInsideAPermittedFolderMavenArchunitAspectJ() {
		assertEquals("entry.txt",
				assertDoesNotThrow(() -> FileSystemViewAccess.child(FileSystemViewAccess.PERMITTED, "entry.txt"))
						.getName());
	}

	/** Checks that getChild on a permitted folder finds the entry. */
	@PublicTest
	@Policy(value = ARCHUNIT_INSTRUMENTATION_POLICY, withinPath = WITHIN_PATH)
	void test_looksInsideAPermittedFolderMavenArchunitInstrumentation() {
		assertEquals("entry.txt",
				assertDoesNotThrow(() -> FileSystemViewAccess.child(FileSystemViewAccess.PERMITTED, "entry.txt"))
						.getName());
	}

	/** Checks that getChild on a permitted folder finds the entry. */
	@PublicTest
	@Policy(value = WALA_ASPECTJ_POLICY, withinPath = WITHIN_PATH)
	void test_looksInsideAPermittedFolderMavenWalaAspectJ() {
		assertEquals("entry.txt",
				assertDoesNotThrow(() -> FileSystemViewAccess.child(FileSystemViewAccess.PERMITTED, "entry.txt"))
						.getName());
	}

	/** Checks that getChild on a permitted folder finds the entry. */
	@PublicTest
	@Policy(value = WALA_INSTRUMENTATION_POLICY, withinPath = WITHIN_PATH)
	void test_looksInsideAPermittedFolderMavenWalaInstrumentation() {
		assertEquals("entry.txt",
				assertDoesNotThrow(() -> FileSystemViewAccess.child(FileSystemViewAccess.PERMITTED, "entry.txt"))
						.getName());
	}

	// </editor-fold>

	// <editor-fold desc="asksWhetherAPermittedFolderContainsAnEntry">
	/** Checks that isParent on a permitted folder answers. */
	@PublicTest
	@Policy(value = ARCHUNIT_ASPECTJ_POLICY, withinPath = WITHIN_PATH)
	void test_asksWhetherAPermittedFolderContainsAnEntryMavenArchunitAspectJ() {
		assertTrue(assertDoesNotThrow(() -> FileSystemViewAccess.containsEntry(FileSystemViewAccess.PERMITTED)));
	}

	/** Checks that isParent on a permitted folder answers. */
	@PublicTest
	@Policy(value = ARCHUNIT_INSTRUMENTATION_POLICY, withinPath = WITHIN_PATH)
	void test_asksWhetherAPermittedFolderContainsAnEntryMavenArchunitInstrumentation() {
		assertTrue(assertDoesNotThrow(() -> FileSystemViewAccess.containsEntry(FileSystemViewAccess.PERMITTED)));
	}

	/** Checks that isParent on a permitted folder answers. */
	@PublicTest
	@Policy(value = WALA_ASPECTJ_POLICY, withinPath = WITHIN_PATH)
	void test_asksWhetherAPermittedFolderContainsAnEntryMavenWalaAspectJ() {
		assertTrue(assertDoesNotThrow(() -> FileSystemViewAccess.containsEntry(FileSystemViewAccess.PERMITTED)));
	}

	/** Checks that isParent on a permitted folder answers. */
	@PublicTest
	@Policy(value = WALA_INSTRUMENTATION_POLICY, withinPath = WITHIN_PATH)
	void test_asksWhetherAPermittedFolderContainsAnEntryMavenWalaInstrumentation() {
		assertTrue(assertDoesNotThrow(() -> FileSystemViewAccess.containsEntry(FileSystemViewAccess.PERMITTED)));
	}

	// </editor-fold>

	// <editor-fold desc="createsAFolderInAPermittedFolder">
	/** Checks that createNewFolder in a permitted folder creates it. */
	@PublicTest
	@Policy(value = ARCHUNIT_ASPECTJ_POLICY, withinPath = WITHIN_PATH)
	void test_createsAFolderInAPermittedFolderMavenArchunitAspectJ() {
		assertTrue(assertDoesNotThrow(() -> FileSystemViewAccess.newFolderIn(FileSystemViewAccess.PERMITTED))
				.isDirectory());
	}

	/** Checks that createNewFolder in a permitted folder creates it. */
	@PublicTest
	@Policy(value = ARCHUNIT_INSTRUMENTATION_POLICY, withinPath = WITHIN_PATH)
	void test_createsAFolderInAPermittedFolderMavenArchunitInstrumentation() {
		assertTrue(assertDoesNotThrow(() -> FileSystemViewAccess.newFolderIn(FileSystemViewAccess.PERMITTED))
				.isDirectory());
	}

	/** Checks that createNewFolder in a permitted folder creates it. */
	@PublicTest
	@Policy(value = WALA_ASPECTJ_POLICY, withinPath = WITHIN_PATH)
	void test_createsAFolderInAPermittedFolderMavenWalaAspectJ() {
		assertTrue(assertDoesNotThrow(() -> FileSystemViewAccess.newFolderIn(FileSystemViewAccess.PERMITTED))
				.isDirectory());
	}

	/** Checks that createNewFolder in a permitted folder creates it. */
	@PublicTest
	@Policy(value = WALA_INSTRUMENTATION_POLICY, withinPath = WITHIN_PATH)
	void test_createsAFolderInAPermittedFolderMavenWalaInstrumentation() {
		assertTrue(assertDoesNotThrow(() -> FileSystemViewAccess.newFolderIn(FileSystemViewAccess.PERMITTED))
				.isDirectory());
	}

	// </editor-fold>

	// <editor-fold desc="looksInsideAPermittedFolderThroughItsOwnView">
	/**
	 * Checks that getChild through a student subclass reads only the folder, not
	 * the name.
	 */
	@PublicTest
	@Policy(value = ARCHUNIT_ASPECTJ_POLICY, withinPath = WITHIN_PATH)
	void test_looksInsideAPermittedFolderThroughItsOwnViewMavenArchunitAspectJ() {
		assertEquals("not-a-permitted-path", assertDoesNotThrow(
				() -> FileSystemViewAccess.childThroughOwnView(FileSystemViewAccess.PERMITTED, "not-a-permitted-path"))
						.getName());
	}

	/**
	 * Checks that getChild through a student subclass reads only the folder, not
	 * the name.
	 */
	@PublicTest
	@Policy(value = ARCHUNIT_INSTRUMENTATION_POLICY, withinPath = WITHIN_PATH)
	void test_looksInsideAPermittedFolderThroughItsOwnViewMavenArchunitInstrumentation() {
		assertEquals("not-a-permitted-path", assertDoesNotThrow(
				() -> FileSystemViewAccess.childThroughOwnView(FileSystemViewAccess.PERMITTED, "not-a-permitted-path"))
						.getName());
	}

	/**
	 * Checks that getChild through a student subclass reads only the folder, not
	 * the name.
	 */
	@PublicTest
	@Policy(value = WALA_ASPECTJ_POLICY, withinPath = WITHIN_PATH)
	void test_looksInsideAPermittedFolderThroughItsOwnViewMavenWalaAspectJ() {
		assertEquals("not-a-permitted-path", assertDoesNotThrow(
				() -> FileSystemViewAccess.childThroughOwnView(FileSystemViewAccess.PERMITTED, "not-a-permitted-path"))
						.getName());
	}

	/**
	 * Checks that getChild through a student subclass reads only the folder, not
	 * the name.
	 */
	@PublicTest
	@Policy(value = WALA_INSTRUMENTATION_POLICY, withinPath = WITHIN_PATH)
	void test_looksInsideAPermittedFolderThroughItsOwnViewMavenWalaInstrumentation() {
		assertEquals("not-a-permitted-path", assertDoesNotThrow(
				() -> FileSystemViewAccess.childThroughOwnView(FileSystemViewAccess.PERMITTED, "not-a-permitted-path"))
						.getName());
	}

	// </editor-fold>
}
