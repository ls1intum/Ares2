package de.tum.cit.ase.ares.integration.aop.allowed;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.function.Executable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import de.tum.cit.ase.ares.api.Policy;
import de.tum.cit.ase.ares.api.jupiter.Public;
import de.tum.cit.ase.ares.integration.aop.allowed.subject.fileSystem.openingRoles.FileOpeningRolesMain;

/**
 * Checks that a call needs the permission for each thing it really does to each
 * file, in all four modes: the provider's opening methods, a
 * {@code RandomAccessFile} in mode {@code "rw"}, which also reads, and a move,
 * which reads and deletes its source and creates or overwrites its target. The
 * writers that take a charset name check only the file.
 */
class FileOpeningRolesTest {

	/**
	 * The folder the policies govern, with one subfolder per set of permissions.
	 */
	private static final Path FOLDER = Path.of("target", "ares-opening-roles");

	/**
	 * Read only.
	 */
	private static final Path READ_ONLY = FOLDER.resolve("readOnly");

	/**
	 * Overwrite, create and delete, but no read.
	 */
	private static final Path WRITE_NO_READ = FOLDER.resolve("writeNoRead");

	/**
	 * Read and create.
	 */
	private static final Path READ_CREATE = FOLDER.resolve("readCreate");

	/**
	 * Read and overwrite.
	 */
	private static final Path READ_OVERWRITE = FOLDER.resolve("readOverwrite");

	/**
	 * Read and delete.
	 */
	private static final Path READ_DELETE = FOLDER.resolve("readDelete");

	/**
	 * Read, overwrite, create and delete.
	 */
	private static final Path ALL = FOLDER.resolve("all");

	/**
	 * The content every prepared file starts with.
	 */
	private static final String OLD_CONTENT = "A old content that is longer than the new one";

	/**
	 * The content the subject writes.
	 */
	private static final String NEW_CONTENT = "new";

	/**
	 * Where the supervised subject's classes are.
	 */
	private static final String WITHIN_PATH = "test-classes/de/tum/cit/ase/ares/integration/aop/allowed/subject/fileSystem/openingRoles";

	/**
	 * The folder holding the policies of all four modes.
	 */
	private static final String POLICY_BASE = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/";

	/**
	 * The policy for ArchunitAspectJ.
	 */
	private static final String ARCHUNITASPECTJ = POLICY_BASE + "archunit/aspectj/PolicyFileOpeningRoles.yaml";

	/**
	 * The policy for ArchunitInstrumentation.
	 */
	private static final String ARCHUNITINSTRUMENTATION = POLICY_BASE
			+ "archunit/instrumentation/PolicyFileOpeningRoles.yaml";

	/**
	 * The policy for WalaAspectJ.
	 */
	private static final String WALAASPECTJ = POLICY_BASE + "wala/aspectj/PolicyFileOpeningRoles.yaml";

	/**
	 * The policy for WalaInstrumentation.
	 */
	private static final String WALAINSTRUMENTATION = POLICY_BASE + "wala/instrumentation/PolicyFileOpeningRoles.yaml";

	/**
	 * Gives every test the subfolders, empty.
	 *
	 * @throws IOException if the folders cannot be prepared
	 */
	@BeforeEach
	void prepareFolders() throws IOException {
		if (Files.exists(FOLDER)) {
			try (Stream<Path> paths = Files.walk(FOLDER)) {
				for (Path path : paths.sorted(Comparator.reverseOrder()).toList()) {
					Files.delete(path);
				}
			}
		}
		for (Path folder : List.of(READ_ONLY, WRITE_NO_READ, READ_CREATE, READ_OVERWRITE, READ_DELETE, ALL)) {
			Files.createDirectories(folder);
		}
	}

	/**
	 * Gives a file the old content.
	 *
	 * @param file the file
	 * @return the file
	 */
	private static Path existing(Path file) {
		try {
			Files.writeString(file, OLD_CONTENT, StandardCharsets.UTF_8);
		} catch (IOException e) {
			throw new IllegalStateException(e);
		}
		return file;
	}

	/**
	 * Reads a file's content.
	 *
	 * @param file the file
	 * @return the content
	 */
	private static String content(Path file) {
		try {
			return Files.readString(file, StandardCharsets.UTF_8);
		} catch (IOException e) {
			throw new IllegalStateException(e);
		}
	}

	/**
	 * Asserts that the call works and the file then holds the new content.
	 *
	 * @param call the call
	 * @param file the file it writes
	 */
	private static void assertWritten(Executable call, Path file) {
		assertDoesNotThrow(call);
		assertEquals(NEW_CONTENT, content(file));
	}

	/**
	 * Asserts that the call moves the source to the target.
	 *
	 * @param call   the call
	 * @param source the file moved
	 * @param target where it goes
	 */
	private static void assertMoved(Executable call, Path source, Path target) {
		assertDoesNotThrow(call);
		assertFalse(Files.exists(source));
		assertEquals(OLD_CONTENT, content(target));
	}

	/**
	 * Asserts that Ares refuses the call and the file keeps its old content.
	 *
	 * @param call the call
	 * @param file the existing file it would change
	 */
	private static void assertRefusedUnchanged(Executable call, Path file) {
		assertThrows(SecurityException.class, call);
		assertEquals(OLD_CONTENT, content(file));
	}

	/**
	 * Asserts that Ares refuses the call and the file is not created.
	 *
	 * @param call the call
	 * @param file the missing file it would create
	 */
	private static void assertRefusedNotCreated(Executable call, Path file) {
		assertThrows(SecurityException.class, call);
		assertFalse(Files.exists(file), "the refused call still created the file");
	}

	/**
	 * Asserts that Ares refuses a move or copy and both files stay as they were.
	 *
	 * @param call   the call
	 * @param source the source, which exists
	 * @param target the target, which keeps whether it exists
	 */
	private static void assertRefusedKeepsBoth(Executable call, Path source, Path target) {
		boolean targetExisted = Files.exists(target);
		assertThrows(SecurityException.class, call);
		assertTrue(Files.exists(source));
		assertEquals(targetExisted, Files.exists(target));
	}

	/**
	 * Runs one call the policy allows and checks its effect.
	 *
	 * @param scenario the name of the call
	 */
	private static void runAllowed(String scenario) {
		switch (scenario) {
		case "providerRead" -> assertEquals('A',
				assertDoesNotThrow(() -> FileOpeningRolesMain.providerRead(existing(READ_ONLY.resolve("in.txt")))));
		case "providerByteChannelRead" -> assertEquals('A', assertDoesNotThrow(
				() -> FileOpeningRolesMain.providerByteChannelRead(existing(READ_ONLY.resolve("in.txt")))));
		case "providerFileChannelRead" -> assertEquals('A', assertDoesNotThrow(
				() -> FileOpeningRolesMain.providerFileChannelRead(existing(READ_ONLY.resolve("in.txt")))));
		case "providerWriteNew" -> assertWritten(
				() -> FileOpeningRolesMain.providerWrite(WRITE_NO_READ.resolve("new.txt"), NEW_CONTENT),
				WRITE_NO_READ.resolve("new.txt"));
		case "providerWriteExisting" -> assertWritten(
				() -> FileOpeningRolesMain.providerWrite(existing(WRITE_NO_READ.resolve("old.txt")), NEW_CONTENT),
				WRITE_NO_READ.resolve("old.txt"));
		case "providerByteChannelWrite" -> assertWritten(
				() -> FileOpeningRolesMain.providerByteChannelWrite(WRITE_NO_READ.resolve("new.txt"), NEW_CONTENT),
				WRITE_NO_READ.resolve("new.txt"));
		case "randomAccessFileAll" -> assertWritten(
				() -> FileOpeningRolesMain.randomAccessFileWrite(existing(ALL.resolve("old.txt")), NEW_CONTENT),
				ALL.resolve("old.txt"));
		case "randomAccessFileNewWithReadCreate" -> assertWritten(
				() -> FileOpeningRolesMain.randomAccessFileWrite(READ_CREATE.resolve("new.txt"), NEW_CONTENT),
				READ_CREATE.resolve("new.txt"));
		case "randomAccessFileExistingWithReadOverwrite" -> assertWritten(() -> FileOpeningRolesMain
				.randomAccessFileWrite(existing(READ_OVERWRITE.resolve("old.txt")), NEW_CONTENT),
				READ_OVERWRITE.resolve("old.txt"));
		case "filesMove" -> assertMoved(
				() -> FileOpeningRolesMain.filesMove(existing(ALL.resolve("src.txt")), ALL.resolve("dst.txt"), false),
				ALL.resolve("src.txt"), ALL.resolve("dst.txt"));
		case "filesMoveReplace" -> {
			existing(ALL.resolve("dst.txt"));
			assertMoved(() -> FileOpeningRolesMain.filesMove(existing(ALL.resolve("src.txt")), ALL.resolve("dst.txt"),
					true), ALL.resolve("src.txt"), ALL.resolve("dst.txt"));
		}
		case "filesMoveNewTargetWithoutOverwrite" -> assertMoved(
				() -> FileOpeningRolesMain.filesMove(existing(READ_DELETE.resolve("src.txt")),
						READ_CREATE.resolve("dst.txt"), false),
				READ_DELETE.resolve("src.txt"), READ_CREATE.resolve("dst.txt"));
		case "filesMoveReplaceWithoutCreate" -> {
			existing(READ_OVERWRITE.resolve("dst.txt"));
			assertMoved(
					() -> FileOpeningRolesMain.filesMove(existing(READ_DELETE.resolve("src.txt")),
							READ_OVERWRITE.resolve("dst.txt"), true),
					READ_DELETE.resolve("src.txt"), READ_OVERWRITE.resolve("dst.txt"));
		}
		case "providerMove" -> assertMoved(() -> FileOpeningRolesMain.providerMove(existing(ALL.resolve("src.txt")),
				ALL.resolve("dst.txt"), false), ALL.resolve("src.txt"), ALL.resolve("dst.txt"));
		case "providerCopy" -> {
			assertDoesNotThrow(() -> FileOpeningRolesMain.providerCopy(existing(READ_ONLY.resolve("src.txt")),
					READ_CREATE.resolve("dst.txt")));
			assertEquals(OLD_CONTENT, content(READ_CREATE.resolve("dst.txt")));
		}
		case "printWriterCharset" -> assertWritten(
				() -> FileOpeningRolesMain.printWriterCharset(ALL.resolve("new.txt"), NEW_CONTENT),
				ALL.resolve("new.txt"));
		case "printWriterFileCharset" -> assertWritten(
				() -> FileOpeningRolesMain.printWriterFileCharset(ALL.resolve("new.txt"), NEW_CONTENT),
				ALL.resolve("new.txt"));
		case "formatterCharset" -> assertWritten(
				() -> FileOpeningRolesMain.formatterCharset(ALL.resolve("new.txt"), NEW_CONTENT),
				ALL.resolve("new.txt"));
		case "formatterFileCharset" -> assertWritten(
				() -> FileOpeningRolesMain.formatterFileCharset(ALL.resolve("new.txt"), NEW_CONTENT),
				ALL.resolve("new.txt"));
		case "formatterWithoutFile" -> assertEquals(NEW_CONTENT,
				assertDoesNotThrow(() -> FileOpeningRolesMain.formatterWithoutFile(NEW_CONTENT)));
		default -> throw new IllegalArgumentException(scenario);
		}
	}

	/**
	 * Runs one call the policy forbids and checks that nothing changed.
	 *
	 * @param scenario the name of the call
	 */
	private static void runRefused(String scenario) {
		switch (scenario) {
		case "providerReadUnreadable" -> assertRefusedUnchanged(
				() -> FileOpeningRolesMain.providerRead(existing(WRITE_NO_READ.resolve("secret.txt"))),
				WRITE_NO_READ.resolve("secret.txt"));
		case "providerByteChannelReadUnreadable" -> assertRefusedUnchanged(
				() -> FileOpeningRolesMain.providerByteChannelRead(existing(WRITE_NO_READ.resolve("secret.txt"))),
				WRITE_NO_READ.resolve("secret.txt"));
		case "providerFileChannelReadUnreadable" -> assertRefusedUnchanged(
				() -> FileOpeningRolesMain.providerFileChannelRead(existing(WRITE_NO_READ.resolve("secret.txt"))),
				WRITE_NO_READ.resolve("secret.txt"));
		case "providerWriteNewReadOnly" -> assertRefusedNotCreated(
				() -> FileOpeningRolesMain.providerWrite(READ_ONLY.resolve("new.txt"), NEW_CONTENT),
				READ_ONLY.resolve("new.txt"));
		case "providerWriteExistingReadOnly" -> assertRefusedUnchanged(
				() -> FileOpeningRolesMain.providerWrite(existing(READ_ONLY.resolve("old.txt")), NEW_CONTENT),
				READ_ONLY.resolve("old.txt"));
		case "providerByteChannelWriteReadOnly" -> assertRefusedUnchanged(() -> FileOpeningRolesMain
				.providerByteChannelWrite(existing(READ_ONLY.resolve("old.txt")), NEW_CONTENT),
				READ_ONLY.resolve("old.txt"));
		case "randomAccessFileUnreadable" -> assertRefusedUnchanged(() -> FileOpeningRolesMain
				.randomAccessFileWrite(existing(WRITE_NO_READ.resolve("old.txt")), NEW_CONTENT),
				WRITE_NO_READ.resolve("old.txt"));
		case "randomAccessFileReadOnly" -> assertRefusedUnchanged(
				() -> FileOpeningRolesMain.randomAccessFileWrite(existing(READ_ONLY.resolve("old.txt")), NEW_CONTENT),
				READ_ONLY.resolve("old.txt"));
		case "randomAccessFileNewWithoutCreate" -> assertRefusedNotCreated(
				() -> FileOpeningRolesMain.randomAccessFileWrite(READ_OVERWRITE.resolve("new.txt"), NEW_CONTENT),
				READ_OVERWRITE.resolve("new.txt"));
		case "randomAccessFileExistingWithoutOverwrite" -> assertRefusedUnchanged(
				() -> FileOpeningRolesMain.randomAccessFileWrite(existing(READ_CREATE.resolve("old.txt")), NEW_CONTENT),
				READ_CREATE.resolve("old.txt"));
		case "filesMoveUnreadableSource" -> assertRefusedKeepsBoth(
				() -> FileOpeningRolesMain.filesMove(existing(WRITE_NO_READ.resolve("secret.txt")),
						WRITE_NO_READ.resolve("moved.txt"), false),
				WRITE_NO_READ.resolve("secret.txt"), WRITE_NO_READ.resolve("moved.txt"));
		case "filesMoveWithoutDelete" -> assertRefusedKeepsBoth(() -> FileOpeningRolesMain
				.filesMove(existing(READ_ONLY.resolve("src.txt")), ALL.resolve("dst.txt"), false),
				READ_ONLY.resolve("src.txt"), ALL.resolve("dst.txt"));
		case "filesMoveNewTargetWithoutCreate" -> assertRefusedKeepsBoth(
				() -> FileOpeningRolesMain.filesMove(existing(READ_DELETE.resolve("src.txt")),
						READ_OVERWRITE.resolve("dst.txt"), false),
				READ_DELETE.resolve("src.txt"), READ_OVERWRITE.resolve("dst.txt"));
		case "filesMoveReplaceWithoutOverwrite" -> {
			existing(READ_CREATE.resolve("dst.txt"));
			assertRefusedKeepsBoth(
					() -> FileOpeningRolesMain.filesMove(existing(READ_DELETE.resolve("src.txt")),
							READ_CREATE.resolve("dst.txt"), true),
					READ_DELETE.resolve("src.txt"), READ_CREATE.resolve("dst.txt"));
			assertEquals(OLD_CONTENT, content(READ_CREATE.resolve("dst.txt")));
		}
		case "providerMoveUnreadableSource" -> assertRefusedKeepsBoth(
				() -> FileOpeningRolesMain.providerMove(existing(WRITE_NO_READ.resolve("secret.txt")),
						WRITE_NO_READ.resolve("moved.txt"), false),
				WRITE_NO_READ.resolve("secret.txt"), WRITE_NO_READ.resolve("moved.txt"));
		case "providerCopyUnreadableSource" -> assertRefusedKeepsBoth(
				() -> FileOpeningRolesMain.providerCopy(existing(WRITE_NO_READ.resolve("secret.txt")),
						WRITE_NO_READ.resolve("copy.txt")),
				WRITE_NO_READ.resolve("secret.txt"), WRITE_NO_READ.resolve("copy.txt"));
		case "printWriterCharsetReadOnly" -> assertRefusedNotCreated(
				() -> FileOpeningRolesMain.printWriterCharset(READ_ONLY.resolve("new.txt"), NEW_CONTENT),
				READ_ONLY.resolve("new.txt"));
		case "formatterCharsetReadOnly" -> assertRefusedNotCreated(
				() -> FileOpeningRolesMain.formatterCharset(READ_ONLY.resolve("new.txt"), NEW_CONTENT),
				READ_ONLY.resolve("new.txt"));
		case "newInputStreamDeleteOnCloseUnreadable" -> assertRefusedUnchanged(
				() -> FileOpeningRolesMain.newInputStreamDeleteOnClose(existing(WRITE_NO_READ.resolve("secret.txt"))),
				WRITE_NO_READ.resolve("secret.txt"));
		default -> throw new IllegalArgumentException(scenario);
		}
	}

	/**
	 * ArchunitAspectJ: each call the policy allows works.
	 *
	 * @param scenario the call
	 */
	@Public
	@ParameterizedTest
	@ValueSource(strings = { "providerRead", "providerByteChannelRead", "providerFileChannelRead", "providerWriteNew",
			"providerWriteExisting", "providerByteChannelWrite", "randomAccessFileAll",
			"randomAccessFileNewWithReadCreate", "randomAccessFileExistingWithReadOverwrite", "filesMove",
			"filesMoveReplace", "filesMoveNewTargetWithoutOverwrite", "filesMoveReplaceWithoutCreate", "providerMove",
			"providerCopy", "printWriterCharset", "printWriterFileCharset", "formatterCharset", "formatterFileCharset",
			"formatterWithoutFile" })
	@Policy(value = ARCHUNITASPECTJ, withinPath = WITHIN_PATH)
	void allowedMavenArchunitAspectJ(String scenario) {
		runAllowed(scenario);
	}

	/**
	 * ArchunitAspectJ: each call the policy forbids is refused.
	 *
	 * @param scenario the call
	 */
	@Public
	@ParameterizedTest
	@ValueSource(strings = { "providerReadUnreadable", "providerByteChannelReadUnreadable",
			"providerFileChannelReadUnreadable", "providerWriteNewReadOnly", "providerWriteExistingReadOnly",
			"providerByteChannelWriteReadOnly", "randomAccessFileUnreadable", "randomAccessFileReadOnly",
			"randomAccessFileNewWithoutCreate", "randomAccessFileExistingWithoutOverwrite", "filesMoveUnreadableSource",
			"filesMoveWithoutDelete", "filesMoveNewTargetWithoutCreate", "filesMoveReplaceWithoutOverwrite",
			"providerMoveUnreadableSource", "providerCopyUnreadableSource", "printWriterCharsetReadOnly",
			"formatterCharsetReadOnly", "newInputStreamDeleteOnCloseUnreadable" })
	@Policy(value = ARCHUNITASPECTJ, withinPath = WITHIN_PATH)
	void refusedMavenArchunitAspectJ(String scenario) {
		runRefused(scenario);
	}

	/**
	 * ArchunitInstrumentation: each call the policy allows works.
	 *
	 * @param scenario the call
	 */
	@Public
	@ParameterizedTest
	@ValueSource(strings = { "providerRead", "providerByteChannelRead", "providerFileChannelRead", "providerWriteNew",
			"providerWriteExisting", "providerByteChannelWrite", "randomAccessFileAll",
			"randomAccessFileNewWithReadCreate", "randomAccessFileExistingWithReadOverwrite", "filesMove",
			"filesMoveReplace", "filesMoveNewTargetWithoutOverwrite", "filesMoveReplaceWithoutCreate", "providerMove",
			"providerCopy", "printWriterCharset", "printWriterFileCharset", "formatterCharset", "formatterFileCharset",
			"formatterWithoutFile" })
	@Policy(value = ARCHUNITINSTRUMENTATION, withinPath = WITHIN_PATH)
	void allowedMavenArchunitInstrumentation(String scenario) {
		runAllowed(scenario);
	}

	/**
	 * ArchunitInstrumentation: each call the policy forbids is refused.
	 *
	 * @param scenario the call
	 */
	@Public
	@ParameterizedTest
	@ValueSource(strings = { "providerReadUnreadable", "providerByteChannelReadUnreadable",
			"providerFileChannelReadUnreadable", "providerWriteNewReadOnly", "providerWriteExistingReadOnly",
			"providerByteChannelWriteReadOnly", "randomAccessFileUnreadable", "randomAccessFileReadOnly",
			"randomAccessFileNewWithoutCreate", "randomAccessFileExistingWithoutOverwrite", "filesMoveUnreadableSource",
			"filesMoveWithoutDelete", "filesMoveNewTargetWithoutCreate", "filesMoveReplaceWithoutOverwrite",
			"providerMoveUnreadableSource", "providerCopyUnreadableSource", "printWriterCharsetReadOnly",
			"formatterCharsetReadOnly", "newInputStreamDeleteOnCloseUnreadable" })
	@Policy(value = ARCHUNITINSTRUMENTATION, withinPath = WITHIN_PATH)
	void refusedMavenArchunitInstrumentation(String scenario) {
		runRefused(scenario);
	}

	/**
	 * WalaAspectJ: each call the policy allows works.
	 *
	 * @param scenario the call
	 */
	@Public
	@ParameterizedTest
	@ValueSource(strings = { "providerRead", "providerByteChannelRead", "providerFileChannelRead", "providerWriteNew",
			"providerWriteExisting", "providerByteChannelWrite", "randomAccessFileAll",
			"randomAccessFileNewWithReadCreate", "randomAccessFileExistingWithReadOverwrite", "filesMove",
			"filesMoveReplace", "filesMoveNewTargetWithoutOverwrite", "filesMoveReplaceWithoutCreate", "providerMove",
			"providerCopy", "printWriterCharset", "printWriterFileCharset", "formatterCharset", "formatterFileCharset",
			"formatterWithoutFile" })
	@Policy(value = WALAASPECTJ, withinPath = WITHIN_PATH)
	void allowedMavenWalaAspectJ(String scenario) {
		runAllowed(scenario);
	}

	/**
	 * WalaAspectJ: each call the policy forbids is refused.
	 *
	 * @param scenario the call
	 */
	@Public
	@ParameterizedTest
	@ValueSource(strings = { "providerReadUnreadable", "providerByteChannelReadUnreadable",
			"providerFileChannelReadUnreadable", "providerWriteNewReadOnly", "providerWriteExistingReadOnly",
			"providerByteChannelWriteReadOnly", "randomAccessFileUnreadable", "randomAccessFileReadOnly",
			"randomAccessFileNewWithoutCreate", "randomAccessFileExistingWithoutOverwrite", "filesMoveUnreadableSource",
			"filesMoveWithoutDelete", "filesMoveNewTargetWithoutCreate", "filesMoveReplaceWithoutOverwrite",
			"providerMoveUnreadableSource", "providerCopyUnreadableSource", "printWriterCharsetReadOnly",
			"formatterCharsetReadOnly", "newInputStreamDeleteOnCloseUnreadable" })
	@Policy(value = WALAASPECTJ, withinPath = WITHIN_PATH)
	void refusedMavenWalaAspectJ(String scenario) {
		runRefused(scenario);
	}

	/**
	 * WalaInstrumentation: each call the policy allows works.
	 *
	 * @param scenario the call
	 */
	@Public
	@ParameterizedTest
	@ValueSource(strings = { "providerRead", "providerByteChannelRead", "providerFileChannelRead", "providerWriteNew",
			"providerWriteExisting", "providerByteChannelWrite", "randomAccessFileAll",
			"randomAccessFileNewWithReadCreate", "randomAccessFileExistingWithReadOverwrite", "filesMove",
			"filesMoveReplace", "filesMoveNewTargetWithoutOverwrite", "filesMoveReplaceWithoutCreate", "providerMove",
			"providerCopy", "printWriterCharset", "printWriterFileCharset", "formatterCharset", "formatterFileCharset",
			"formatterWithoutFile" })
	@Policy(value = WALAINSTRUMENTATION, withinPath = WITHIN_PATH)
	void allowedMavenWalaInstrumentation(String scenario) {
		runAllowed(scenario);
	}

	/**
	 * WalaInstrumentation: each call the policy forbids is refused.
	 *
	 * @param scenario the call
	 */
	@Public
	@ParameterizedTest
	@ValueSource(strings = { "providerReadUnreadable", "providerByteChannelReadUnreadable",
			"providerFileChannelReadUnreadable", "providerWriteNewReadOnly", "providerWriteExistingReadOnly",
			"providerByteChannelWriteReadOnly", "randomAccessFileUnreadable", "randomAccessFileReadOnly",
			"randomAccessFileNewWithoutCreate", "randomAccessFileExistingWithoutOverwrite", "filesMoveUnreadableSource",
			"filesMoveWithoutDelete", "filesMoveNewTargetWithoutCreate", "filesMoveReplaceWithoutOverwrite",
			"providerMoveUnreadableSource", "providerCopyUnreadableSource", "printWriterCharsetReadOnly",
			"formatterCharsetReadOnly", "newInputStreamDeleteOnCloseUnreadable" })
	@Policy(value = WALAINSTRUMENTATION, withinPath = WITHIN_PATH)
	void refusedMavenWalaInstrumentation(String scenario) {
		runRefused(scenario);
	}
}
