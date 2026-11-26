package de.tum.cit.ase.ares.integration.aop.forbidden.subject.fileSystem.inspect;

import java.io.File;
import java.io.IOException;
import java.nio.file.AccessMode;
import java.nio.file.FileVisitOption;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributeView;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.spi.FileSystemProvider;
import java.util.List;
import java.util.stream.Stream;

import javax.imageio.ImageIO;
import javax.swing.filechooser.FileSystemView;

/**
 * Student code that asks about a file without opening it: whether it exists,
 * how large it is, who owns it and similar questions.
 */
public final class InspectMain {

	/**
	 * Where the fixtures of these tests live.
	 */
	public static final String BASE = "src/test/java/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/inspect";

	/**
	 * A file inside the folder the policy permits reading.
	 */
	public static final String READABLE_FILE = BASE + "/readable/file.txt";

	/**
	 * A file inside a folder no rule of the policy names.
	 */
	public static final String FORBIDDEN_FILE = BASE + "/forbidden/file.txt";

	/**
	 * Prevents instantiation of this utility class.
	 */
	private InspectMain() {
		throw new SecurityException(
				"Ares Security Error (Reason: Ares-Code; Stage: Test): Main is a utility class and should not be instantiated.");
	}

	/**
	 * Returns the provider that serves the given path.
	 *
	 * @param path the path
	 * @return its file system provider
	 */
	private static FileSystemProvider provider(Path path) {
		return path.getFileSystem().provider();
	}

	/**
	 * Asks {@code Files.exists}.
	 *
	 * @param path the path to ask about
	 * @return the answer
	 */
	public static boolean filesExists(String path) {
		return Files.exists(Path.of(path));
	}

	/**
	 * Asks {@code Files.notExists}.
	 *
	 * @param path the path to ask about
	 * @return the answer
	 */
	public static boolean filesNotExists(String path) {
		return Files.notExists(Path.of(path));
	}

	/**
	 * Asks {@code Files.isDirectory}.
	 *
	 * @param path the path to ask about
	 * @return the answer
	 */
	public static boolean filesIsDirectory(String path) {
		return Files.isDirectory(Path.of(path));
	}

	/**
	 * Asks {@code Files.isRegularFile}.
	 *
	 * @param path the path to ask about
	 * @return the answer
	 */
	public static boolean filesIsRegularFile(String path) {
		return Files.isRegularFile(Path.of(path));
	}

	/**
	 * Asks {@code Files.isSymbolicLink}.
	 *
	 * @param path the path to ask about
	 * @return the answer
	 */
	public static boolean filesIsSymbolicLink(String path) {
		return Files.isSymbolicLink(Path.of(path));
	}

	/**
	 * Asks {@code Files.isReadable}.
	 *
	 * @param path the path to ask about
	 * @return the answer
	 */
	public static boolean filesIsReadable(String path) {
		return Files.isReadable(Path.of(path));
	}

	/**
	 * Asks {@code Files.isWritable}.
	 *
	 * @param path the path to ask about
	 * @return the answer
	 */
	public static boolean filesIsWritable(String path) {
		return Files.isWritable(Path.of(path));
	}

	/**
	 * Asks {@code Files.isExecutable}.
	 *
	 * @param path the path to ask about
	 * @return the answer
	 */
	public static boolean filesIsExecutable(String path) {
		return Files.isExecutable(Path.of(path));
	}

	/**
	 * Asks {@code Files.isHidden}.
	 *
	 * @param path the path to ask about
	 * @return the answer
	 * @throws IOException if the file cannot be asked about
	 */
	public static boolean filesIsHidden(String path) throws IOException {
		return Files.isHidden(Path.of(path));
	}

	/**
	 * Asks {@code Files.isSameFile} with the given path first.
	 *
	 * @param path  the path asked about first
	 * @param other the path asked about second
	 * @return the answer
	 * @throws IOException if the files cannot be asked about
	 */
	public static boolean filesIsSameFile(String path, String other) throws IOException {
		return Files.isSameFile(Path.of(path), Path.of(other));
	}

	/**
	 * Asks {@code Files.size}.
	 *
	 * @param path the path to ask about
	 * @return the answer
	 * @throws IOException if the file cannot be asked about
	 */
	public static long filesSize(String path) throws IOException {
		return Files.size(Path.of(path));
	}

	/**
	 * Asks {@code Files.getLastModifiedTime}.
	 *
	 * @param path the path to ask about
	 * @return the answer
	 * @throws IOException if the file cannot be asked about
	 */
	public static Object filesGetLastModifiedTime(String path) throws IOException {
		return Files.getLastModifiedTime(Path.of(path));
	}

	/**
	 * Asks {@code Files.getOwner}.
	 *
	 * @param path the path to ask about
	 * @return the answer
	 * @throws IOException if the file cannot be asked about
	 */
	public static Object filesGetOwner(String path) throws IOException {
		return Files.getOwner(Path.of(path));
	}

	/**
	 * Asks {@code Files.getPosixFilePermissions}.
	 *
	 * @param path the path to ask about
	 * @return the answer
	 * @throws IOException if the file cannot be asked about
	 */
	public static Object filesGetPosixFilePermissions(String path) throws IOException {
		return Files.getPosixFilePermissions(Path.of(path));
	}

	/**
	 * Asks {@code Files.readAttributes} for a class of attributes.
	 *
	 * @param path the path to ask about
	 * @return the answer
	 * @throws IOException if the file cannot be asked about
	 */
	public static Object filesReadAttributesByClass(String path) throws IOException {
		return Files.readAttributes(Path.of(path), BasicFileAttributes.class);
	}

	/**
	 * Asks {@code Files.readAttributes} for attributes named in a String.
	 *
	 * @param path the path to ask about
	 * @return the answer
	 * @throws IOException if the file cannot be asked about
	 */
	public static Object filesReadAttributesByName(String path) throws IOException {
		return Files.readAttributes(Path.of(path), "basic:size");
	}

	/**
	 * Asks {@code Files.getAttribute}.
	 *
	 * @param path the path to ask about
	 * @return the answer
	 * @throws IOException if the file cannot be asked about
	 */
	public static Object filesGetAttribute(String path) throws IOException {
		return Files.getAttribute(Path.of(path), "basic:size");
	}

	/**
	 * Asks {@code Files.getFileAttributeView}.
	 *
	 * @param path the path to ask about
	 * @return the answer
	 */
	public static Object filesGetFileAttributeView(String path) {
		return Files.getFileAttributeView(Path.of(path), BasicFileAttributeView.class);
	}

	/**
	 * Asks {@code Files.getFileStore}.
	 *
	 * @param path the path to ask about
	 * @return the answer
	 * @throws IOException if the file cannot be asked about
	 */
	public static Object filesGetFileStore(String path) throws IOException {
		return Files.getFileStore(Path.of(path));
	}

	/**
	 * Asks {@code Files.readSymbolicLink}.
	 *
	 * @param path the path to ask about
	 * @return the answer
	 * @throws IOException if the path is not a link
	 */
	public static Object filesReadSymbolicLink(String path) throws IOException {
		return Files.readSymbolicLink(Path.of(path));
	}

	/**
	 * Asks the provider's {@code checkAccess}.
	 *
	 * @param path the path to ask about
	 * @throws IOException if the file is missing or not readable
	 */
	public static void providerCheckAccess(String path) throws IOException {
		Path asPath = Path.of(path);
		provider(asPath).checkAccess(asPath, AccessMode.READ);
	}

	/**
	 * Asks the provider's {@code readAttributes} for a class of attributes.
	 *
	 * @param path the path to ask about
	 * @return the answer
	 * @throws IOException if the file cannot be asked about
	 */
	public static Object providerReadAttributesByClass(String path) throws IOException {
		Path asPath = Path.of(path);
		return provider(asPath).readAttributes(asPath, BasicFileAttributes.class);
	}

	/**
	 * Asks the provider's {@code readAttributes} for attributes named in a String.
	 *
	 * @param path the path to ask about
	 * @return the answer
	 * @throws IOException if the file cannot be asked about
	 */
	public static Object providerReadAttributesByName(String path) throws IOException {
		Path asPath = Path.of(path);
		return provider(asPath).readAttributes(asPath, "basic:size");
	}

	/**
	 * Asks the provider's {@code getFileAttributeView}.
	 *
	 * @param path the path to ask about
	 * @return the answer
	 */
	public static Object providerGetFileAttributeView(String path) {
		Path asPath = Path.of(path);
		return provider(asPath).getFileAttributeView(asPath, BasicFileAttributeView.class);
	}

	/**
	 * Asks the provider's {@code getFileStore}.
	 *
	 * @param path the path to ask about
	 * @return the answer
	 * @throws IOException if the file cannot be asked about
	 */
	public static Object providerGetFileStore(String path) throws IOException {
		Path asPath = Path.of(path);
		return provider(asPath).getFileStore(asPath);
	}

	/**
	 * Asks the provider's {@code isHidden}.
	 *
	 * @param path the path to ask about
	 * @return the answer
	 * @throws IOException if the file cannot be asked about
	 */
	public static boolean providerIsHidden(String path) throws IOException {
		Path asPath = Path.of(path);
		return provider(asPath).isHidden(asPath);
	}

	/**
	 * Asks the provider's {@code isSameFile} with the given path first.
	 *
	 * @param path  the path asked about first
	 * @param other the path asked about second
	 * @return the answer
	 * @throws IOException if the files cannot be asked about
	 */
	public static boolean providerIsSameFile(String path, String other) throws IOException {
		Path asPath = Path.of(path);
		return provider(asPath).isSameFile(asPath, Path.of(other));
	}

	/**
	 * Asks the provider's {@code readSymbolicLink}.
	 *
	 * @param path the path to ask about
	 * @return the answer
	 * @throws IOException if the path is not a link
	 */
	public static Object providerReadSymbolicLink(String path) throws IOException {
		Path asPath = Path.of(path);
		return provider(asPath).readSymbolicLink(asPath);
	}

	/**
	 * Asks {@code File.exists}.
	 *
	 * @param path the path to ask about
	 * @return the answer
	 */
	public static boolean fileExists(String path) {
		return new File(path).exists();
	}

	/**
	 * Asks {@code File.isFile}.
	 *
	 * @param path the path to ask about
	 * @return the answer
	 */
	public static boolean fileIsFile(String path) {
		return new File(path).isFile();
	}

	/**
	 * Asks {@code File.isDirectory}.
	 *
	 * @param path the path to ask about
	 * @return the answer
	 */
	public static boolean fileIsDirectory(String path) {
		return new File(path).isDirectory();
	}

	/**
	 * Asks {@code File.isHidden}.
	 *
	 * @param path the path to ask about
	 * @return the answer
	 */
	public static boolean fileIsHidden(String path) {
		return new File(path).isHidden();
	}

	/**
	 * Asks {@code File.length}.
	 *
	 * @param path the path to ask about
	 * @return the answer
	 */
	public static long fileLength(String path) {
		return new File(path).length();
	}

	/**
	 * Asks {@code File.lastModified}.
	 *
	 * @param path the path to ask about
	 * @return the answer
	 */
	public static long fileLastModified(String path) {
		return new File(path).lastModified();
	}

	/**
	 * Asks {@code File.canRead}.
	 *
	 * @param path the path to ask about
	 * @return the answer
	 */
	public static boolean fileCanRead(String path) {
		return new File(path).canRead();
	}

	/**
	 * Asks {@code File.canWrite}.
	 *
	 * @param path the path to ask about
	 * @return the answer
	 */
	public static boolean fileCanWrite(String path) {
		return new File(path).canWrite();
	}

	/**
	 * Asks {@code File.canExecute}.
	 *
	 * @param path the path to ask about
	 * @return the answer
	 */
	public static boolean fileCanExecute(String path) {
		return new File(path).canExecute();
	}

	/**
	 * Asks {@code File.getFreeSpace}.
	 *
	 * @param path the path to ask about
	 * @return the answer
	 */
	public static long fileGetFreeSpace(String path) {
		return new File(path).getFreeSpace();
	}

	/**
	 * Asks {@code File.getTotalSpace}.
	 *
	 * @param path the path to ask about
	 * @return the answer
	 */
	public static long fileGetTotalSpace(String path) {
		return new File(path).getTotalSpace();
	}

	/**
	 * Asks {@code File.getUsableSpace}.
	 *
	 * @param path the path to ask about
	 * @return the answer
	 */
	public static long fileGetUsableSpace(String path) {
		return new File(path).getUsableSpace();
	}

	/**
	 * Asks the file system view's {@code isHiddenFile}.
	 *
	 * @param path the path to ask about
	 * @return the answer
	 */
	public static boolean viewIsHiddenFile(String path) {
		return FileSystemView.getFileSystemView().isHiddenFile(new File(path));
	}

	/**
	 * Asks the file system view's {@code isTraversable}.
	 *
	 * @param path the path to ask about
	 * @return the answer
	 */
	public static Boolean viewIsTraversable(String path) {
		return FileSystemView.getFileSystemView().isTraversable(new File(path));
	}

	/**
	 * Asks the file system view's {@code isFileSystem}.
	 *
	 * @param path the path to ask about
	 * @return the answer
	 */
	public static boolean viewIsFileSystem(String path) {
		return FileSystemView.getFileSystemView().isFileSystem(new File(path));
	}

	/**
	 * Asks the file system view's {@code isLink}.
	 *
	 * @param path the path to ask about
	 * @return the answer
	 */
	public static boolean viewIsLink(String path) {
		return FileSystemView.getFileSystemView().isLink(new File(path));
	}

	/**
	 * Asks the file system view's {@code getParentDirectory}.
	 *
	 * @param path the path to ask about
	 * @return the answer
	 */
	public static Object viewGetParentDirectory(String path) {
		return FileSystemView.getFileSystemView().getParentDirectory(new File(path));
	}

	/**
	 * Asks the file system view's {@code isRoot}.
	 *
	 * @param path the path to ask about
	 * @return the answer
	 */
	public static boolean viewIsRoot(String path) {
		return FileSystemView.getFileSystemView().isRoot(new File(path));
	}

	/**
	 * Asks the file system view's {@code isFileSystemRoot}.
	 *
	 * @param path the path to ask about
	 * @return the answer
	 */
	public static boolean viewIsFileSystemRoot(String path) {
		return FileSystemView.getFileSystemView().isFileSystemRoot(new File(path));
	}

	/**
	 * Asks the file system view's {@code isDrive}.
	 *
	 * @param path the path to ask about
	 * @return the answer
	 */
	public static boolean viewIsDrive(String path) {
		return FileSystemView.getFileSystemView().isDrive(new File(path));
	}

	/**
	 * Asks the file system view's {@code isFloppyDrive}.
	 *
	 * @param path the path to ask about
	 * @return the answer
	 */
	public static boolean viewIsFloppyDrive(String path) {
		return FileSystemView.getFileSystemView().isFloppyDrive(new File(path));
	}

	/**
	 * Asks the file system view's {@code isComputerNode}.
	 *
	 * @param path the path to ask about
	 * @return the answer
	 */
	public static boolean viewIsComputerNode(String path) {
		return FileSystemView.getFileSystemView().isComputerNode(new File(path));
	}

	/**
	 * Asks the file system view's {@code getSystemDisplayName}.
	 *
	 * @param path the path to ask about
	 * @return the answer
	 */
	public static String viewGetSystemDisplayName(String path) {
		return FileSystemView.getFileSystemView().getSystemDisplayName(new File(path));
	}

	/**
	 * Asks the file system view's {@code getSystemTypeDescription}.
	 *
	 * @param path the path to ask about
	 * @return the answer
	 */
	public static String viewGetSystemTypeDescription(String path) {
		return FileSystemView.getFileSystemView().getSystemTypeDescription(new File(path));
	}

	/**
	 * Asks the file system view's {@code getSystemIcon}.
	 *
	 * @param path the path to ask about
	 * @return the answer
	 */
	public static Object viewGetSystemIcon(String path) {
		return FileSystemView.getFileSystemView().getSystemIcon(new File(path));
	}

	/**
	 * Asks the file system view's {@code getSystemIcon} for a given size.
	 *
	 * @param path the path to ask about
	 * @return the answer
	 */
	public static Object viewGetSystemIconOfSize(String path) {
		return FileSystemView.getFileSystemView().getSystemIcon(new File(path), 16, 16);
	}

	/**
	 * Asks the file system view's {@code getLinkLocation}.
	 *
	 * @param path the path to ask about
	 * @return the answer
	 * @throws java.io.FileNotFoundException if the path is not a link
	 */
	public static Object viewGetLinkLocation(String path) throws java.io.FileNotFoundException {
		return FileSystemView.getFileSystemView().getLinkLocation(new File(path));
	}

	/**
	 * Asks {@code Files.probeContentType}, which reads the start of the file.
	 *
	 * @param path the path to ask about
	 * @return the answer
	 * @throws IOException if the file cannot be read
	 */
	public static String filesProbeContentType(String path) throws IOException {
		return Files.probeContentType(Path.of(path));
	}

	/**
	 * Asks {@code File.exists} through a method reference that the JDK calls.
	 *
	 * @param path the path to ask about
	 * @return the answer
	 */
	public static boolean fileExistsThroughStream(String path) {
		return Stream.of(new File(path)).anyMatch(File::exists);
	}

	/**
	 * Asks {@code File.exists} on a student subclass of {@link File}.
	 *
	 * @param path the path to ask about
	 * @return the answer
	 */
	public static boolean studentFileExists(String path) {
		return new StudentFile(path).exists();
	}

	/**
	 * Walks the folder and asks {@code File.exists} about another path from the
	 * walk's own callback.
	 *
	 * @param folder the folder to walk
	 * @param other  the path the callback asks about
	 * @throws IOException if the folder cannot be walked
	 */
	public static void walkAndAskFromCallback(String folder, String other) throws IOException {
		Files.walkFileTree(Path.of(folder), new SimpleFileVisitor<>() {
			@Override
			public FileVisitResult visitFile(Path file, BasicFileAttributes attributes) {
				new File(other).exists();
				return FileVisitResult.CONTINUE;
			}
		});
	}

	/**
	 * Walks the folder and follows the links in it.
	 *
	 * @param folder the folder to walk
	 * @return the names of the entries the walk saw
	 * @throws IOException if the folder cannot be walked
	 */
	public static List<String> walkFollowingLinks(String folder) throws IOException {
		try (Stream<Path> paths = Files.walk(Path.of(folder), FileVisitOption.FOLLOW_LINKS)) {
			return paths.map(path -> path.getFileName().toString()).toList();
		}
	}

	/**
	 * Finds the regular files below the folder.
	 *
	 * @param folder the folder to search
	 * @return how many regular files were found
	 * @throws IOException if the folder cannot be searched
	 */
	public static long findRegularFiles(String folder) throws IOException {
		try (Stream<Path> paths = Files.find(Path.of(folder), Integer.MAX_VALUE,
				(path, attributes) -> attributes.isRegularFile())) {
			return paths.count();
		}
	}

	/**
	 * Reads an image.
	 *
	 * @param path the image file
	 * @return whether an image was read
	 * @throws IOException if the image cannot be read
	 */
	public static boolean readImage(String path) throws IOException {
		return ImageIO.read(new File(path)) != null;
	}

	/**
	 * Creates a folder and every missing folder above it.
	 *
	 * @param folder the folder to create
	 * @return whether the folder exists afterwards
	 * @throws IOException if the folders cannot be created
	 */
	public static boolean createDirectories(String folder) throws IOException {
		return Files.isDirectory(Files.createDirectories(Path.of(folder)));
	}

	/**
	 * Uses a student class for the first time, so it is loaded now.
	 *
	 * @return the value the class gives back
	 */
	public static int useHelperForTheFirstTime() {
		return InspectHelper.answer();
	}

	/**
	 * Asks whether the file is a link without following it.
	 *
	 * @param path the path to ask about
	 * @return the answer
	 */
	public static boolean filesExistsWithoutFollowingLinks(String path) {
		return Files.exists(Path.of(path), LinkOption.NOFOLLOW_LINKS);
	}
}
