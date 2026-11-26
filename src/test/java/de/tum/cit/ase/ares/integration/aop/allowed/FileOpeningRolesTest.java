package de.tum.cit.ase.ares.integration.aop.allowed;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.DatagramChannel;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.SecureDirectoryStream;
import java.nio.file.StandardOpenOption;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.function.Executable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import de.tum.cit.ase.ares.api.Policy;
import de.tum.cit.ase.ares.api.jupiter.Public;
import de.tum.cit.ase.ares.integration.aop.allowed.subject.fileSystem.openingRoles.FileOpeningRolesMain;
import de.tum.cit.ase.ares.integration.aop.allowed.subject.fileSystem.openingRoles.imageOutput.ImageOutputMain;

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
	 * Where the supervised image-output subject's classes are, kept apart so its
	 * use of {@code javax.imageio} is analysed on its own.
	 */
	private static final String IMAGE_WITHIN_PATH = WITHIN_PATH + "/imageOutput";

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
	 * The full path of the program {@code cat}, looked up once on the search path,
	 * or {@code null} where there is none, as on Windows without Git.
	 */
	private static final String CAT = findOnSearchPath("cat");

	/**
	 * Starts the JDK's image-stream clean-up thread as the test class. The JDK
	 * starts it the first time any file image stream is made, which would otherwise
	 * charge the subject with creating a thread.
	 *
	 * @throws IOException if the warm-up file cannot be written
	 */
	@BeforeAll
	static void startImageStreamCleanUpThread() throws IOException {
		Files.createDirectories(FOLDER);
		Path warmUp = FOLDER.resolve("warm-up.img");
		new javax.imageio.stream.FileImageOutputStream(warmUp.toFile()).close();
		Files.delete(warmUp);
	}

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
	 * Finds a program in the folders of the {@code PATH} variable, under its name
	 * or with {@code .exe} added.
	 *
	 * @param program the program name
	 * @return its full path, or {@code null} if no folder holds it
	 */
	private static String findOnSearchPath(String program) {
		String searchPath = System.getenv("PATH");
		if (searchPath == null) {
			return null;
		}
		for (String folder : searchPath.split(File.pathSeparator)) {
			for (String name : List.of(program, program + ".exe")) {
				try {
					Path candidate = Path.of(folder, name);
					if (Files.isRegularFile(candidate) && Files.isExecutable(candidate)) {
						return candidate.toAbsolutePath().toString();
					}
				} catch (InvalidPathException unusableFolder) {
					break;
				}
			}
		}
		return null;
	}

	/**
	 * Returns the full path of {@code cat}, skipping the test where it is missing.
	 *
	 * @return the full path
	 */
	private static String cat() {
		if (CAT == null) {
			Assumptions.abort("The program cat is not on the search path.");
		}
		return CAT;
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
	 * Lists the calls the policy allows.
	 *
	 * @return their names
	 */
	static Stream<String> allowedScenarios() {
		return Stream.of("providerRead", "providerByteChannelRead", "providerFileChannelRead", "providerWriteNew",
				"providerWriteExisting", "providerByteChannelWrite", "randomAccessFileAll",
				"randomAccessFileNewWithReadCreate", "randomAccessFileExistingWithReadOverwrite",
				"randomAccessFileNewWithoutRead", "filesMove", "filesMoveReplace", "filesMoveNewTargetWithoutOverwrite",
				"filesMoveReplaceWithoutCreate", "providerMove", "providerCopy", "printWriterCharset",
				"printWriterFileCharset", "formatterCharset", "formatterFileCharset", "formatterWithoutFile",
				"providerSymbolicLink", "providerHardLink", "providerDeleteIfExists", "secureDirectoryReadAbsolute",
				"secureDirectoryMove", "secureDirectoryList", "transferToFile", "transferFromFile",
				"transferFromIntoOwnNewFile", "fileOutputStreamNewWithReadCreate", "redirectToNew",
				"redirectAppendToExisting", "redirectFromReadable", "processBuilderRedirectOutput",
				"processBuilderRedirectInput");
	}

	/**
	 * Lists the calls the policy forbids.
	 *
	 * @return their names
	 */
	static Stream<String> refusedScenarios() {
		return Stream.of("providerReadUnreadable", "providerByteChannelReadUnreadable",
				"providerFileChannelReadUnreadable", "providerWriteNewReadOnly", "providerWriteExistingReadOnly",
				"providerByteChannelWriteReadOnly", "randomAccessFileUnreadable", "randomAccessFileReadOnly",
				"randomAccessFileNewWithoutCreate", "randomAccessFileExistingWithoutOverwrite",
				"filesMoveUnreadableSource", "filesMoveWithoutDelete", "filesMoveNewTargetWithoutCreate",
				"filesMoveReplaceWithoutOverwrite", "providerMoveUnreadableSource", "providerCopyUnreadableSource",
				"printWriterCharsetReadOnly", "formatterCharsetReadOnly", "newInputStreamDeleteOnCloseUnreadable",
				"providerSymbolicLinkReadOnly", "providerHardLinkReadOnly", "providerDeleteIfExistsReadOnly",
				"secureDirectoryReadUnreadable", "secureDirectoryReadRelative", "secureDirectoryMoveUnreadableSource",
				"secureDirectoryListUnreadable", "transferToUnreadableSource", "transferToReadOnlyTarget",
				"transferToDatagram", "transferFromUnreadableSource", "transferFromDatagram",
				"transferFromSuppliedExistingWithoutOverwrite", "transferToSuppliedExistingWithoutOverwrite",
				"transferFromIntoReplacementOfCreatedFile", "transferFromAfterFailedCreation",
				"fileOutputStreamThroughDanglingLink", "redirectToReadOnly", "redirectAppendToReadOnly",
				"redirectFromUnreadable", "processBuilderRedirectOutputReadOnly",
				"processBuilderRedirectInputUnreadable");
	}

	/**
	 * Lists the calls the policy forbids under AspectJ: all of the above, and a
	 * write through a file channel the test class opened. Instrumentation does not
	 * check a write through an open file channel; it checks the opening.
	 *
	 * @return their names
	 */
	static Stream<String> refusedAspectJScenarios() {
		return Stream.concat(refusedScenarios(), Stream.of("writeToSuppliedExistingWithoutOverwrite"));
	}

	/**
	 * Lists the image-output calls the policy allows.
	 *
	 * @return their names
	 */
	static Stream<String> allowedImageScenarios() {
		return Stream.of("createImageOutputStreamNew", "fileImageOutputStreamNew");
	}

	/**
	 * Lists the image-output calls the policy forbids.
	 *
	 * @return their names
	 */
	static Stream<String> refusedImageScenarios() {
		return Stream.of("createImageOutputStreamExistingNoRead", "fileImageOutputStreamExistingNoRead");
	}

	/**
	 * Opens a file for reading, as the test class, so the subject only moves its
	 * bytes.
	 *
	 * @param file the file
	 * @return the channel
	 * @throws IOException if opening fails
	 */
	private static FileChannel openForReading(Path file) throws IOException {
		return FileChannel.open(file, StandardOpenOption.READ);
	}

	/**
	 * Opens a file for writing, creating it if it is missing, as the test class.
	 *
	 * @param file the file
	 * @return the channel
	 * @throws IOException if opening fails
	 */
	private static FileChannel openForWriting(Path file) throws IOException {
		return FileChannel.open(file, StandardOpenOption.CREATE, StandardOpenOption.WRITE);
	}

	/**
	 * Opens an existing file for writing, as the test class, which Ares does not
	 * check.
	 *
	 * @param file the file
	 * @return the channel
	 * @throws IOException if opening fails
	 */
	private static FileChannel openExistingForWriting(Path file) throws IOException {
		return FileChannel.open(file, StandardOpenOption.WRITE);
	}

	/**
	 * Makes a symbolic link to a file that does not exist, as the test class. Skips
	 * the test where the system does not let it make links, as Windows without
	 * developer mode does not.
	 *
	 * @param link   where the link goes
	 * @param target the missing file it points to
	 * @return the link
	 */
	private static Path danglingLink(Path link, Path target) {
		try {
			return Files.createSymbolicLink(link, target.toAbsolutePath());
		} catch (UnsupportedOperationException | IOException | SecurityException unsupported) {
			Assumptions.abort("This system does not let the test make a symbolic link: " + unsupported);
			return link;
		}
	}

	/**
	 * Gives a file the new content, so a copy of it would show in another file.
	 *
	 * @param file the file
	 * @return the file
	 */
	private static Path newContent(Path file) {
		try {
			Files.writeString(file, NEW_CONTENT, StandardCharsets.UTF_8);
		} catch (IOException e) {
			throw new IllegalStateException(e);
		}
		return file;
	}

	/**
	 * Opens a datagram channel connected to an address reserved for documentation,
	 * as the test class. Connecting a datagram channel sends nothing, and it does
	 * not block, so a missed refusal cannot hang the test.
	 *
	 * @return the channel
	 * @throws IOException if opening fails
	 */
	@SuppressWarnings("PMD.AvoidUsingHardCodedIP")
	private static DatagramChannel connectedDatagram() throws IOException {
		DatagramChannel channel = DatagramChannel.open();
		channel.connect(new InetSocketAddress("203.0.113.1", 9));
		channel.configureBlocking(false);
		return channel;
	}

	/**
	 * Moves a file's bytes with the subject's {@code transferTo}, both channels
	 * opened by the test class.
	 *
	 * @param source the file read from
	 * @param target the file written to, created if it is missing
	 * @throws IOException if moving fails
	 */
	private static void transferToFile(Path source, Path target) throws IOException {
		try (FileChannel in = openForReading(source); FileChannel out = openForWriting(target)) {
			FileOpeningRolesMain.transferTo(in, out);
		}
	}

	/**
	 * Moves a file's bytes with the subject's {@code transferFrom}, both channels
	 * opened by the test class.
	 *
	 * @param source the file read from
	 * @param target the file written to, created if it is missing
	 * @throws IOException if moving fails
	 */
	private static void transferFromFile(Path source, Path target) throws IOException {
		try (FileChannel in = openForReading(source); FileChannel out = openForWriting(target)) {
			FileOpeningRolesMain.transferFrom(out, in);
		}
	}

	/**
	 * Skips a secure-directory scenario where the JDK offers no secure directory
	 * stream, as JDK 17 on macOS does not. Linux and newer JDKs offer one.
	 *
	 * @param scenario the name of the call
	 */
	private static void assumeSecureDirectoryStreamsFor(String scenario) {
		if (!scenario.startsWith("secureDirectory")) {
			return;
		}
		try (DirectoryStream<Path> stream = Files.newDirectoryStream(FOLDER)) {
			Assumptions.assumeTrue(stream instanceof SecureDirectoryStream,
					"This JDK offers no secure directory stream on this system.");
		} catch (IOException e) {
			throw new IllegalStateException(e);
		}
	}

	/**
	 * Runs one call the policy allows and checks its effect.
	 *
	 * @param scenario the name of the call
	 */
	private static void runAllowed(String scenario) {
		assumeSecureDirectoryStreamsFor(scenario);
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
		case "randomAccessFileNewWithoutRead" -> assertWritten(
				() -> FileOpeningRolesMain.randomAccessFileWrite(WRITE_NO_READ.resolve("new.txt"), NEW_CONTENT),
				WRITE_NO_READ.resolve("new.txt"));
		case "providerSymbolicLink" -> {
			Path target = existing(READ_CREATE.resolve("target.txt")).toAbsolutePath();
			assertDoesNotThrow(() -> FileOpeningRolesMain.providerSymbolicLink(READ_CREATE.resolve("link"), target));
			assertTrue(Files.isSymbolicLink(READ_CREATE.resolve("link")));
		}
		case "providerHardLink" -> {
			Path existingFile = existing(WRITE_NO_READ.resolve("old.txt"));
			assertDoesNotThrow(
					() -> FileOpeningRolesMain.providerHardLink(WRITE_NO_READ.resolve("link.txt"), existingFile));
			assertTrue(Files.exists(WRITE_NO_READ.resolve("link.txt")));
		}
		case "providerDeleteIfExists" -> {
			Path file = existing(WRITE_NO_READ.resolve("old.txt"));
			assertTrue(assertDoesNotThrow(() -> FileOpeningRolesMain.providerDeleteIfExists(file)));
			assertFalse(Files.exists(file));
		}
		case "secureDirectoryReadAbsolute" -> {
			Path file = existing(READ_ONLY.resolve("in.txt")).toAbsolutePath();
			assertEquals('A', assertDoesNotThrow(() -> FileOpeningRolesMain.secureDirectoryRead(READ_ONLY, file)));
		}
		case "secureDirectoryMove" -> {
			Path source = existing(ALL.resolve("src.txt"));
			assertMoved(() -> FileOpeningRolesMain.secureDirectoryMove(ALL, source.toAbsolutePath(),
					ALL.resolve("dst.txt").toAbsolutePath()), source, ALL.resolve("dst.txt"));
		}
		case "secureDirectoryList" -> {
			existing(READ_ONLY.resolve("in.txt"));
			assertEquals(1, assertDoesNotThrow(
					() -> FileOpeningRolesMain.secureDirectoryList(ALL, READ_ONLY.toAbsolutePath())));
		}
		case "transferToFile" -> {
			Path source = existing(READ_ONLY.resolve("in.txt"));
			assertDoesNotThrow(() -> transferToFile(source, WRITE_NO_READ.resolve("new.txt")));
			assertEquals(OLD_CONTENT, content(WRITE_NO_READ.resolve("new.txt")));
		}
		case "transferFromFile" -> {
			Path source = existing(READ_ONLY.resolve("in.txt"));
			assertDoesNotThrow(() -> transferFromFile(source, WRITE_NO_READ.resolve("new.txt")));
			assertEquals(OLD_CONTENT, content(WRITE_NO_READ.resolve("new.txt")));
		}
		case "transferFromIntoOwnNewFile" -> {
			Path source = existing(READ_ONLY.resolve("in.txt"));
			assertDoesNotThrow(() -> {
				try (FileChannel in = openForReading(source)) {
					FileOpeningRolesMain.transferFromIntoNewFile(READ_CREATE.resolve("new.txt"), in);
				}
			});
			assertEquals(OLD_CONTENT, content(READ_CREATE.resolve("new.txt")));
		}
		case "fileOutputStreamNewWithReadCreate" -> assertWritten(
				() -> FileOpeningRolesMain.fileOutputStreamWrite(READ_CREATE.resolve("new.txt").toFile(), NEW_CONTENT),
				READ_CREATE.resolve("new.txt"));
		case "redirectToNew" -> assertDoesNotThrow(
				() -> FileOpeningRolesMain.redirectTo(WRITE_NO_READ.resolve("new.txt").toFile()));
		case "redirectAppendToExisting" -> assertDoesNotThrow(
				() -> FileOpeningRolesMain.redirectAppendTo(existing(WRITE_NO_READ.resolve("old.txt")).toFile()));
		case "redirectFromReadable" -> assertDoesNotThrow(
				() -> FileOpeningRolesMain.redirectFrom(existing(READ_ONLY.resolve("in.txt")).toFile()));
		case "processBuilderRedirectOutput" -> assertDoesNotThrow(() -> FileOpeningRolesMain
				.redirectOutput(new ProcessBuilder("echo", "hello"), WRITE_NO_READ.resolve("new.txt").toFile()));
		case "processBuilderRedirectInput" -> assertDoesNotThrow(() -> FileOpeningRolesMain
				.redirectInput(new ProcessBuilder(cat()), existing(READ_ONLY.resolve("in.txt")).toFile()));
		default -> throw new IllegalArgumentException(scenario);
		}
	}

	/**
	 * Runs one call the policy forbids and checks that nothing changed.
	 *
	 * @param scenario the name of the call
	 */
	private static void runRefused(String scenario) {
		assumeSecureDirectoryStreamsFor(scenario);
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
		case "providerSymbolicLinkReadOnly" -> {
			Path target = existing(READ_ONLY.resolve("target.txt")).toAbsolutePath();
			assertThrows(SecurityException.class,
					() -> FileOpeningRolesMain.providerSymbolicLink(READ_ONLY.resolve("link"), target));
			assertFalse(Files.exists(READ_ONLY.resolve("link"), LinkOption.NOFOLLOW_LINKS));
		}
		case "providerHardLinkReadOnly" -> {
			Path existingFile = existing(READ_ONLY.resolve("old.txt"));
			assertRefusedNotCreated(
					() -> FileOpeningRolesMain.providerHardLink(READ_ONLY.resolve("link.txt"), existingFile),
					READ_ONLY.resolve("link.txt"));
		}
		case "providerDeleteIfExistsReadOnly" -> assertRefusedUnchanged(
				() -> FileOpeningRolesMain.providerDeleteIfExists(existing(READ_ONLY.resolve("old.txt"))),
				READ_ONLY.resolve("old.txt"));
		case "secureDirectoryReadUnreadable" -> {
			Path file = existing(WRITE_NO_READ.resolve("secret.txt")).toAbsolutePath();
			assertThrows(SecurityException.class, () -> FileOpeningRolesMain.secureDirectoryRead(ALL, file));
		}
		case "secureDirectoryReadRelative" -> {
			existing(READ_ONLY.resolve("in.txt"));
			assertThrows(SecurityException.class,
					() -> FileOpeningRolesMain.secureDirectoryRead(READ_ONLY, Path.of("in.txt")));
		}
		case "secureDirectoryMoveUnreadableSource" -> {
			Path source = existing(WRITE_NO_READ.resolve("secret.txt"));
			assertRefusedKeepsBoth(
					() -> FileOpeningRolesMain.secureDirectoryMove(ALL, source.toAbsolutePath(),
							WRITE_NO_READ.resolve("moved.txt").toAbsolutePath()),
					source, WRITE_NO_READ.resolve("moved.txt"));
		}
		case "secureDirectoryListUnreadable" -> assertThrows(SecurityException.class,
				() -> FileOpeningRolesMain.secureDirectoryList(ALL, WRITE_NO_READ.toAbsolutePath()));
		case "transferToUnreadableSource" -> {
			Path source = existing(WRITE_NO_READ.resolve("secret.txt"));
			assertThrows(SecurityException.class, () -> transferToFile(source, ALL.resolve("new.txt")));
			assertEquals("", content(ALL.resolve("new.txt")));
		}
		case "transferToReadOnlyTarget" -> {
			Path source = existing(READ_ONLY.resolve("in.txt"));
			Path target = existing(READ_ONLY.resolve("old.txt"));
			assertRefusedUnchanged(() -> transferToFile(source, target), target);
		}
		case "transferToDatagram" -> {
			Path source = existing(READ_ONLY.resolve("in.txt"));
			assertThrows(SecurityException.class, () -> {
				try (FileChannel in = openForReading(source); DatagramChannel out = connectedDatagram()) {
					FileOpeningRolesMain.transferTo(in, out);
				}
			});
		}
		case "transferFromUnreadableSource" -> {
			Path source = existing(WRITE_NO_READ.resolve("secret.txt"));
			assertThrows(SecurityException.class, () -> transferFromFile(source, ALL.resolve("new.txt")));
			assertEquals("", content(ALL.resolve("new.txt")));
		}
		case "transferFromSuppliedExistingWithoutOverwrite" -> {
			Path source = newContent(READ_ONLY.resolve("in.txt"));
			Path target = existing(READ_CREATE.resolve("old.txt"));
			assertRefusedUnchanged(() -> {
				try (FileChannel in = openForReading(source); FileChannel out = openExistingForWriting(target)) {
					FileOpeningRolesMain.transferFrom(out, in);
				}
			}, target);
		}
		case "transferToSuppliedExistingWithoutOverwrite" -> {
			Path source = newContent(READ_ONLY.resolve("in.txt"));
			Path target = existing(READ_CREATE.resolve("old.txt"));
			assertRefusedUnchanged(() -> {
				try (FileChannel in = openForReading(source); FileChannel out = openExistingForWriting(target)) {
					FileOpeningRolesMain.transferTo(in, out);
				}
			}, target);
		}
		case "transferFromIntoReplacementOfCreatedFile" -> {
			Path target = READ_CREATE.resolve("new.txt");
			Path first = existing(READ_ONLY.resolve("in.txt"));
			assertDoesNotThrow(() -> {
				try (FileChannel in = openForReading(first)) {
					FileOpeningRolesMain.transferFromIntoNewFile(target, in);
				}
			});
			assertDoesNotThrow(() -> Files.delete(target));
			existing(target);
			Path source = newContent(READ_ONLY.resolve("in.txt"));
			assertRefusedUnchanged(() -> {
				try (FileChannel in = openForReading(source); FileChannel out = openExistingForWriting(target)) {
					FileOpeningRolesMain.transferFrom(out, in);
				}
			}, target);
		}
		case "transferFromAfterFailedCreation" -> {
			Path target = READ_CREATE.resolve("new.txt");
			assertFalse(assertDoesNotThrow(
					() -> FileOpeningRolesMain.printWriterWithCharset(target.toFile(), "not-a-charset")));
			existing(target);
			Path source = newContent(READ_ONLY.resolve("in.txt"));
			assertRefusedUnchanged(() -> {
				try (FileChannel in = openForReading(source); FileChannel out = openExistingForWriting(target)) {
					FileOpeningRolesMain.transferFrom(out, in);
				}
			}, target);
		}
		case "fileOutputStreamThroughDanglingLink" -> {
			Path outside = FOLDER.resolve("outside.txt");
			Path link = danglingLink(READ_CREATE.resolve("link.txt"), outside);
			assertThrows(SecurityException.class,
					() -> FileOpeningRolesMain.fileOutputStreamWrite(link.toFile(), NEW_CONTENT));
			assertFalse(Files.exists(outside), "the refused write still created the file the link points to");
		}
		case "writeToSuppliedExistingWithoutOverwrite" -> {
			Path target = existing(READ_CREATE.resolve("old.txt"));
			assertRefusedUnchanged(() -> {
				try (FileChannel out = openExistingForWriting(target)) {
					FileOpeningRolesMain.writeTo(out, NEW_CONTENT);
				}
			}, target);
		}
		case "transferFromDatagram" -> assertThrows(SecurityException.class, () -> {
			try (DatagramChannel in = connectedDatagram();
					FileChannel out = openForWriting(WRITE_NO_READ.resolve("new.txt"))) {
				FileOpeningRolesMain.transferFrom(out, in);
			}
		});
		case "redirectToReadOnly" -> assertThrows(SecurityException.class,
				() -> FileOpeningRolesMain.redirectTo(READ_ONLY.resolve("new.txt").toFile()));
		case "redirectAppendToReadOnly" -> assertThrows(SecurityException.class,
				() -> FileOpeningRolesMain.redirectAppendTo(existing(READ_ONLY.resolve("old.txt")).toFile()));
		case "redirectFromUnreadable" -> assertThrows(SecurityException.class,
				() -> FileOpeningRolesMain.redirectFrom(existing(WRITE_NO_READ.resolve("secret.txt")).toFile()));
		case "processBuilderRedirectOutputReadOnly" -> assertThrows(SecurityException.class, () -> FileOpeningRolesMain
				.redirectOutput(new ProcessBuilder("echo", "hello"), READ_ONLY.resolve("new.txt").toFile()));
		case "processBuilderRedirectInputUnreadable" -> assertThrows(SecurityException.class, () -> FileOpeningRolesMain
				.redirectInput(new ProcessBuilder(cat()), existing(WRITE_NO_READ.resolve("secret.txt")).toFile()));
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
	@MethodSource("allowedScenarios")
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
	@MethodSource("refusedAspectJScenarios")
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
	@MethodSource("allowedScenarios")
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
	@MethodSource("refusedScenarios")
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
	@MethodSource("allowedScenarios")
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
	@MethodSource("refusedAspectJScenarios")
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
	@MethodSource("allowedScenarios")
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
	@MethodSource("refusedScenarios")
	@Policy(value = WALAINSTRUMENTATION, withinPath = WITHIN_PATH)
	void refusedMavenWalaInstrumentation(String scenario) {
		runRefused(scenario);
	}

	/**
	 * Runs one image-output call and checks its effect: a new file in the folder
	 * without read is allowed, an existing one there is refused, since the stream
	 * could read it.
	 *
	 * @param scenario the name of the call
	 */
	private static void runImage(String scenario) {
		Path newFile = WRITE_NO_READ.resolve("new.img");
		Path oldFile = WRITE_NO_READ.resolve("old.txt");
		switch (scenario) {
		case "createImageOutputStreamNew" -> {
			assertDoesNotThrow(() -> ImageOutputMain.createImageOutputStream(newFile.toFile()));
			assertEquals("n", content(newFile));
		}
		case "fileImageOutputStreamNew" -> {
			assertDoesNotThrow(() -> ImageOutputMain.fileImageOutputStream(newFile.toFile()));
			assertEquals("n", content(newFile));
		}
		case "createImageOutputStreamExistingNoRead" -> assertRefusedUnchanged(
				() -> ImageOutputMain.createImageOutputStream(existing(oldFile).toFile()), oldFile);
		case "fileImageOutputStreamExistingNoRead" -> assertRefusedUnchanged(
				() -> ImageOutputMain.fileImageOutputStream(existing(oldFile).toFile()), oldFile);
		default -> throw new IllegalArgumentException(scenario);
		}
	}

	/**
	 * ArchunitAspectJ: each image-output call the policy allows works.
	 *
	 * @param scenario the call
	 */
	@Public
	@ParameterizedTest
	@MethodSource("allowedImageScenarios")
	@Policy(value = ARCHUNITASPECTJ, withinPath = IMAGE_WITHIN_PATH)
	void allowedImageMavenArchunitAspectJ(String scenario) {
		runImage(scenario);
	}

	/**
	 * ArchunitAspectJ: each image-output call the policy forbids is refused.
	 *
	 * @param scenario the call
	 */
	@Public
	@ParameterizedTest
	@MethodSource("refusedImageScenarios")
	@Policy(value = ARCHUNITASPECTJ, withinPath = IMAGE_WITHIN_PATH)
	void refusedImageMavenArchunitAspectJ(String scenario) {
		runImage(scenario);
	}

	/**
	 * ArchunitInstrumentation: each image-output call the policy allows works.
	 *
	 * @param scenario the call
	 */
	@Public
	@ParameterizedTest
	@MethodSource("allowedImageScenarios")
	@Policy(value = ARCHUNITINSTRUMENTATION, withinPath = IMAGE_WITHIN_PATH)
	void allowedImageMavenArchunitInstrumentation(String scenario) {
		runImage(scenario);
	}

	/**
	 * ArchunitInstrumentation: each image-output call the policy forbids is
	 * refused.
	 *
	 * @param scenario the call
	 */
	@Public
	@ParameterizedTest
	@MethodSource("refusedImageScenarios")
	@Policy(value = ARCHUNITINSTRUMENTATION, withinPath = IMAGE_WITHIN_PATH)
	void refusedImageMavenArchunitInstrumentation(String scenario) {
		runImage(scenario);
	}

	/**
	 * WalaAspectJ: each image-output call the policy allows works.
	 *
	 * @param scenario the call
	 */
	@Public
	@ParameterizedTest
	@MethodSource("allowedImageScenarios")
	@Policy(value = WALAASPECTJ, withinPath = IMAGE_WITHIN_PATH)
	void allowedImageMavenWalaAspectJ(String scenario) {
		runImage(scenario);
	}

	/**
	 * WalaAspectJ: each image-output call the policy forbids is refused.
	 *
	 * @param scenario the call
	 */
	@Public
	@ParameterizedTest
	@MethodSource("refusedImageScenarios")
	@Policy(value = WALAASPECTJ, withinPath = IMAGE_WITHIN_PATH)
	void refusedImageMavenWalaAspectJ(String scenario) {
		runImage(scenario);
	}

	/**
	 * WalaInstrumentation: each image-output call the policy allows works.
	 *
	 * @param scenario the call
	 */
	@Public
	@ParameterizedTest
	@MethodSource("allowedImageScenarios")
	@Policy(value = WALAINSTRUMENTATION, withinPath = IMAGE_WITHIN_PATH)
	void allowedImageMavenWalaInstrumentation(String scenario) {
		runImage(scenario);
	}

	/**
	 * WalaInstrumentation: each image-output call the policy forbids is refused.
	 *
	 * @param scenario the call
	 */
	@Public
	@ParameterizedTest
	@MethodSource("refusedImageScenarios")
	@Policy(value = WALAINSTRUMENTATION, withinPath = IMAGE_WITHIN_PATH)
	void refusedImageMavenWalaInstrumentation(String scenario) {
		runImage(scenario);
	}
}
