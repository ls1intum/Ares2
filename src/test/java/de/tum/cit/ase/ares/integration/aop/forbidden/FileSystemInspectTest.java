package de.tum.cit.ase.ares.integration.aop.forbidden;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import javax.imageio.ImageIO;
import javax.swing.filechooser.FileSystemView;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.function.Executable;
import org.junit.jupiter.api.io.TempDir;

import de.tum.cit.ase.ares.api.Policy;
import de.tum.cit.ase.ares.api.jupiter.PublicTest;
import de.tum.cit.ase.ares.integration.aop.forbidden.subject.fileSystem.inspect.InspectMain;

/**
 * Checks that asking about a file without opening it (whether it exists, its
 * size, its owner and similar) obeys the policy: allowed below a path that any
 * file rule names, refused elsewhere, in all four mode combinations.
 */
class FileSystemInspectTest extends SystemAccessTest {

	/**
	 * The compiled subjects the policies supervise.
	 */
	private static final String WITHIN_PATH = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/inspect";

	/**
	 * Where the policies of these tests live.
	 */
	private static final String POLICIES = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/";

	/**
	 * Permits one folder per file action, ArchUnit and AspectJ.
	 */
	private static final String ARCHUNIT_ASPECTJ_POLICY = POLICIES + "archunit/aspectj/PolicyInspect.yaml";

	/**
	 * Permits one folder per file action, ArchUnit and instrumentation.
	 */
	private static final String ARCHUNIT_INSTRUMENTATION_POLICY = POLICIES
			+ "archunit/instrumentation/PolicyInspect.yaml";

	/**
	 * Permits one folder per file action, WALA and AspectJ.
	 */
	private static final String WALA_ASPECTJ_POLICY = POLICIES + "wala/aspectj/PolicyInspect.yaml";

	/**
	 * Permits one folder per file action, WALA and instrumentation.
	 */
	private static final String WALA_INSTRUMENTATION_POLICY = POLICIES + "wala/instrumentation/PolicyInspect.yaml";

	/**
	 * Names the readable folder but grants nothing on it, ArchUnit and AspectJ.
	 */
	private static final String ARCHUNIT_ASPECTJ_ALL_FALSE_POLICY = POLICIES
			+ "archunit/aspectj/PolicyInspectAllFalse.yaml";

	/**
	 * Names the readable folder but grants nothing on it, ArchUnit and
	 * instrumentation.
	 */
	private static final String ARCHUNIT_INSTRUMENTATION_ALL_FALSE_POLICY = POLICIES
			+ "archunit/instrumentation/PolicyInspectAllFalse.yaml";

	/**
	 * Names the readable folder but grants nothing on it, WALA and AspectJ.
	 */
	private static final String WALA_ASPECTJ_ALL_FALSE_POLICY = POLICIES + "wala/aspectj/PolicyInspectAllFalse.yaml";

	/**
	 * Names the readable folder but grants nothing on it, WALA and instrumentation.
	 */
	private static final String WALA_INSTRUMENTATION_ALL_FALSE_POLICY = POLICIES
			+ "wala/instrumentation/PolicyInspectAllFalse.yaml";

	/**
	 * The forbidden file, as a refusal names it.
	 */
	private static final Path FORBIDDEN = Paths.get(InspectMain.FORBIDDEN_FILE);

	/**
	 * The folders the policy names, one per file action.
	 */
	private static final List<String> PERMITTED_FOLDERS = List.of("readable", "overwritable", "creatable", "executable",
			"deletable");

	/**
	 * The folder the create-directories test makes below the creatable folder.
	 */
	private static final Path CREATED_FOLDERS = Paths.get(InspectMain.BASE, "creatable", "made");

	/**
	 * The links a test made, removed after each test.
	 */
	private final List<Path> createdLinks = new ArrayList<>();

	/**
	 * One question about a single path.
	 */
	@FunctionalInterface
	private interface Inspection {

		/**
		 * Asks the question.
		 *
		 * @param path the path asked about
		 * @throws Throwable whatever the question throws
		 */
		void inspect(String path) throws Throwable;
	}

	/**
	 * Lists every single-path question the subject can ask, by name.
	 *
	 * @return the questions in a fixed order
	 */
	private static Map<String, Inspection> inspections() {
		Map<String, Inspection> inspections = new LinkedHashMap<>();
		inspections.put("Files.exists", InspectMain::filesExists);
		inspections.put("Files.notExists", InspectMain::filesNotExists);
		inspections.put("Files.isDirectory", InspectMain::filesIsDirectory);
		inspections.put("Files.isRegularFile", InspectMain::filesIsRegularFile);
		inspections.put("Files.isSymbolicLink", InspectMain::filesIsSymbolicLink);
		inspections.put("Files.isReadable", InspectMain::filesIsReadable);
		inspections.put("Files.isWritable", InspectMain::filesIsWritable);
		inspections.put("Files.isExecutable", InspectMain::filesIsExecutable);
		inspections.put("Files.isHidden", InspectMain::filesIsHidden);
		inspections.put("Files.size", InspectMain::filesSize);
		inspections.put("Files.getLastModifiedTime", InspectMain::filesGetLastModifiedTime);
		inspections.put("Files.getOwner", InspectMain::filesGetOwner);
		inspections.put("Files.getPosixFilePermissions", InspectMain::filesGetPosixFilePermissions);
		inspections.put("Files.readAttributes(Class)", InspectMain::filesReadAttributesByClass);
		inspections.put("Files.readAttributes(String)", InspectMain::filesReadAttributesByName);
		inspections.put("Files.getAttribute", InspectMain::filesGetAttribute);
		inspections.put("Files.getFileAttributeView", InspectMain::filesGetFileAttributeView);
		inspections.put("Files.getFileStore", InspectMain::filesGetFileStore);
		inspections.put("Files.readSymbolicLink", InspectMain::filesReadSymbolicLink);
		inspections.put("FileSystemProvider.checkAccess", InspectMain::providerCheckAccess);
		inspections.put("FileSystemProvider.readAttributes(Class)", InspectMain::providerReadAttributesByClass);
		inspections.put("FileSystemProvider.readAttributes(String)", InspectMain::providerReadAttributesByName);
		inspections.put("FileSystemProvider.getFileAttributeView", InspectMain::providerGetFileAttributeView);
		inspections.put("FileSystemProvider.getFileStore", InspectMain::providerGetFileStore);
		inspections.put("FileSystemProvider.isHidden", InspectMain::providerIsHidden);
		inspections.put("FileSystemProvider.readSymbolicLink", InspectMain::providerReadSymbolicLink);
		inspections.put("File.exists", InspectMain::fileExists);
		inspections.put("File.isFile", InspectMain::fileIsFile);
		inspections.put("File.isDirectory", InspectMain::fileIsDirectory);
		inspections.put("File.isHidden", InspectMain::fileIsHidden);
		inspections.put("File.length", InspectMain::fileLength);
		inspections.put("File.lastModified", InspectMain::fileLastModified);
		inspections.put("File.canRead", InspectMain::fileCanRead);
		inspections.put("File.canWrite", InspectMain::fileCanWrite);
		inspections.put("File.canExecute", InspectMain::fileCanExecute);
		inspections.put("File.getFreeSpace", InspectMain::fileGetFreeSpace);
		inspections.put("File.getTotalSpace", InspectMain::fileGetTotalSpace);
		inspections.put("File.getUsableSpace", InspectMain::fileGetUsableSpace);
		inspections.put("FileSystemView.isHiddenFile", InspectMain::viewIsHiddenFile);
		inspections.put("FileSystemView.isTraversable", InspectMain::viewIsTraversable);
		inspections.put("FileSystemView.isFileSystem", InspectMain::viewIsFileSystem);
		inspections.put("FileSystemView.isLink", InspectMain::viewIsLink);
		inspections.put("FileSystemView.getParentDirectory", InspectMain::viewGetParentDirectory);
		inspections.put("FileSystemView.isRoot", InspectMain::viewIsRoot);
		inspections.put("FileSystemView.isFileSystemRoot", InspectMain::viewIsFileSystemRoot);
		inspections.put("FileSystemView.isDrive", InspectMain::viewIsDrive);
		inspections.put("FileSystemView.isFloppyDrive", InspectMain::viewIsFloppyDrive);
		inspections.put("FileSystemView.isComputerNode", InspectMain::viewIsComputerNode);
		inspections.put("FileSystemView.getSystemDisplayName", InspectMain::viewGetSystemDisplayName);
		inspections.put("FileSystemView.getSystemTypeDescription", InspectMain::viewGetSystemTypeDescription);
		inspections.put("FileSystemView.getSystemIcon", InspectMain::viewGetSystemIcon);
		inspections.put("FileSystemView.getSystemIcon(int, int)", InspectMain::viewGetSystemIconOfSize);
		inspections.put("FileSystemView.getLinkLocation", InspectMain::viewGetLinkLocation);
		return inspections;
	}

	/**
	 * Sets up the JDK's file system view, image reading and file icon once, before
	 * any policy is armed. Their first use starts graphics threads, which a policy
	 * without thread creations rightly refuses. Afterwards the JDK keeps the icon
	 * and its image disposer thread, so the tests start no graphics thread. That
	 * start-up is not what these tests check.
	 *
	 * @throws IOException if the image cannot be read
	 */
	@BeforeAll
	static void setUpTheGraphicsSystem() throws IOException {
		FileSystemView.getFileSystemView().getSystemIcon(new File(InspectMain.READABLE_FILE));
		ImageIO.read(Paths.get(InspectMain.BASE, "readable", "image.png").toFile());
	}

	/**
	 * Removes the links and folders a test made, so every run starts from the
	 * committed fixtures alone.
	 *
	 * @throws IOException if one cannot be removed
	 */
	@AfterEach
	void removeWhatTheTestMade() throws IOException {
		for (Path link : createdLinks) {
			Files.deleteIfExists(link);
		}
		createdLinks.clear();
		if (Files.exists(CREATED_FOLDERS)) {
			try (Stream<Path> tree = Files.walk(CREATED_FOLDERS)) {
				for (Path path : tree.sorted(Comparator.reverseOrder()).toList()) {
					Files.delete(path);
				}
			}
		}
	}

	// <editor-fold desc="Helpers">

	/**
	 * Fails if Ares refuses the call. Any other exception, such as a file that is
	 * not a link, is the answer to the question and not a refusal.
	 *
	 * @param name       what the call asks, for the failure message
	 * @param executable the call
	 */
	private static void assertNotRefused(String name, Executable executable) {
		try {
			executable.execute();
		} catch (Throwable thrown) {
			for (Throwable cause = thrown; cause != null; cause = cause.getCause()) {
				if (cause instanceof SecurityException) {
					Assertions.fail(name + " was refused although the policy permits it", thrown);
				}
			}
		}
	}

	/**
	 * Expects the call to be refused as an inspection of the given path.
	 *
	 * @param name         what the call asks, for the failure message
	 * @param executable   the call
	 * @param expectedPath the path the refusal must name
	 */
	private void assertInspectionRefused(String name, Executable executable, Path expectedPath) {
		SecurityException exception = Assertions.assertThrows(SecurityException.class, executable,
				() -> name + " was not refused");
		assertGeneralErrorMessageWithPath(expectedPath, exception.getMessage(), "illegally inspect",
				"illegal zu untersuchen", InspectMain.class);
	}

	/**
	 * Every question about a file in a permitted folder is answered, whichever file
	 * action the folder is permitted for.
	 */
	private void assertPermittedPathsAnswered() {
		List<Executable> checks = new ArrayList<>();
		inspections().forEach((name, inspection) -> checks
				.add(() -> assertNotRefused(name, () -> inspection.inspect(InspectMain.READABLE_FILE))));
		for (String folder : PERMITTED_FOLDERS) {
			String file = InspectMain.BASE + "/" + folder + "/file.txt";
			checks.add(() -> assertNotRefused("File.exists in " + folder, () -> InspectMain.fileExists(file)));
			checks.add(() -> assertNotRefused("Files.size in " + folder, () -> InspectMain.filesSize(file)));
		}
		checks.add(() -> assertNotRefused("Files.exists of a missing file in a permitted folder",
				() -> InspectMain.filesExists(InspectMain.BASE + "/readable/missing.txt")));
		checks.add(() -> assertNotRefused("Files.isSameFile within permitted folders", () -> InspectMain
				.filesIsSameFile(InspectMain.READABLE_FILE, InspectMain.BASE + "/deletable/file.txt")));
		checks.add(() -> assertNotRefused("FileSystemProvider.isSameFile within permitted folders", () -> InspectMain
				.providerIsSameFile(InspectMain.READABLE_FILE, InspectMain.BASE + "/deletable/file.txt")));
		Assertions.assertAll(checks.stream());
	}

	/**
	 * Every question about a file outside the permitted folders is refused and
	 * names that file, including a missing one, either side of isSameFile and a
	 * question asked from a callback of a permitted walk.
	 */
	private void assertForbiddenPathsRefused() {
		List<Executable> checks = new ArrayList<>();
		inspections().forEach((name, inspection) -> checks.add(
				() -> assertInspectionRefused(name, () -> inspection.inspect(InspectMain.FORBIDDEN_FILE), FORBIDDEN)));
		Path missing = Paths.get(InspectMain.BASE, "forbidden", "missing.txt");
		checks.add(() -> assertInspectionRefused("Files.exists of a missing file",
				() -> InspectMain.filesExists(missing.toString()), missing));
		checks.add(() -> assertInspectionRefused("Files.isSameFile, forbidden first",
				() -> InspectMain.filesIsSameFile(InspectMain.FORBIDDEN_FILE, InspectMain.READABLE_FILE), FORBIDDEN));
		checks.add(() -> assertInspectionRefused("Files.isSameFile, forbidden second",
				() -> InspectMain.filesIsSameFile(InspectMain.READABLE_FILE, InspectMain.FORBIDDEN_FILE), FORBIDDEN));
		checks.add(() -> assertInspectionRefused("FileSystemProvider.isSameFile, forbidden first",
				() -> InspectMain.providerIsSameFile(InspectMain.FORBIDDEN_FILE, InspectMain.READABLE_FILE),
				FORBIDDEN));
		checks.add(() -> assertInspectionRefused("FileSystemProvider.isSameFile, forbidden second",
				() -> InspectMain.providerIsSameFile(InspectMain.READABLE_FILE, InspectMain.FORBIDDEN_FILE),
				FORBIDDEN));
		checks.add(() -> assertInspectionRefused("File.exists from a walk's callback",
				() -> InspectMain.walkAndAskFromCallback(InspectMain.BASE + "/readable", InspectMain.FORBIDDEN_FILE),
				FORBIDDEN));
		checks.add(() -> Assertions.assertThrows(SecurityException.class,
				() -> InspectMain.studentFileExists(InspectMain.FORBIDDEN_FILE),
				"File.exists on a student subclass of File was not refused"));
		Assertions.assertAll(checks.stream());
	}

	/**
	 * A rule that names the folder but grants no action on it permits no question
	 * about it either.
	 */
	private void assertRuleWithoutActionsRefuses() {
		Path readable = Paths.get(InspectMain.READABLE_FILE);
		Assertions.assertAll(
				() -> assertInspectionRefused("File.exists", () -> InspectMain.fileExists(InspectMain.READABLE_FILE),
						readable),
				() -> assertInspectionRefused("Files.exists", () -> InspectMain.filesExists(InspectMain.READABLE_FILE),
						readable));
	}

	/**
	 * Work the policy permits keeps working although the JDK asks about files on
	 * the way: creating nested folders, loading a student class, walking and
	 * searching a folder, reading an image and probing a content type.
	 */
	private void assertPermittedWorkStillWorks() {
		Assertions.assertAll(
				() -> Assertions.assertTrue(InspectMain.createDirectories(CREATED_FOLDERS.resolve("a/b").toString()),
						"Files.createDirectories inside the creatable folder failed"),
				() -> Assertions.assertEquals(42, InspectMain.useHelperForTheFirstTime(),
						"Loading a student class failed"),
				() -> Assertions.assertTrue(
						InspectMain.walkFollowingLinks(InspectMain.BASE + "/readable").contains("file.txt"),
						"Files.walk of the readable folder failed"),
				() -> Assertions.assertTrue(InspectMain.findRegularFiles(InspectMain.BASE + "/readable") >= 2,
						"Files.find in the readable folder failed"),
				() -> Assertions.assertTrue(InspectMain.readImage(InspectMain.BASE + "/readable/image.png"),
						"ImageIO.read of a readable image failed"),
				() -> assertNotRefused("Files.probeContentType of a readable file",
						() -> InspectMain.filesProbeContentType(InspectMain.READABLE_FILE)));
	}

	/**
	 * Probing the content type reads the file, so it is refused as a read outside
	 * the readable folder.
	 */
	private void assertProbeContentTypeRefusedAsRead() {
		SecurityException exception = Assertions.assertThrows(SecurityException.class,
				() -> InspectMain.filesProbeContentType(InspectMain.FORBIDDEN_FILE));
		assertGeneralErrorMessageWithPath(FORBIDDEN, exception.getMessage(), "illegally read", "illegal zu lesen",
				InspectMain.class);
	}

	/**
	 * Makes a link, or skips the test where the system does not allow it.
	 *
	 * @param link   where the link goes
	 * @param target what the link points to
	 */
	private void makeLink(Path link, Path target) {
		try {
			Files.deleteIfExists(link);
			Files.createSymbolicLink(link, target);
			createdLinks.add(link);
		} catch (IOException | UnsupportedOperationException e) {
			Assumptions.abort("Symbolic links cannot be created here: " + e);
		}
	}

	/**
	 * Links are followed before the check: a link in a permitted folder that points
	 * outside is refused, so is a dangling link, while a link outside that points
	 * into a permitted folder is answered.
	 */
	private void assertLinksAreFollowed() {
		Path readable = Paths.get(InspectMain.BASE, "readable");
		Path outward = readable.resolve("outward-link");
		Path dangling = readable.resolve("dangling-link");
		Path inward = Paths.get(InspectMain.BASE, "forbidden", "inward-link");
		makeLink(outward, FORBIDDEN.toAbsolutePath());
		makeLink(dangling, readable.resolve("does-not-exist").toAbsolutePath());
		makeLink(inward, Paths.get(InspectMain.READABLE_FILE).toAbsolutePath());
		Assertions.assertAll(
				() -> Assertions.assertThrows(SecurityException.class, () -> InspectMain.fileExists(outward.toString()),
						"A link pointing outside was not refused"),
				() -> Assertions.assertThrows(SecurityException.class,
						() -> InspectMain.filesExistsWithoutFollowingLinks(outward.toString()),
						"A link pointing outside was not refused without following links"),
				() -> Assertions.assertThrows(SecurityException.class,
						() -> InspectMain.fileExists(dangling.toString()), "A dangling link was not refused"),
				() -> assertNotRefused("A link pointing inside", () -> InspectMain.fileExists(inward.toString())));
	}

	// </editor-fold>

	// <editor-fold desc="A permitted path is answered">

	/**
	 * Questions about permitted paths are answered with ArchUnit and AspectJ.
	 */
	@PublicTest
	@Policy(value = ARCHUNIT_ASPECTJ_POLICY, withinPath = WITHIN_PATH)
	void test_inspectPermittedPathMavenArchunitAspectJ() {
		assertPermittedPathsAnswered();
	}

	/**
	 * Questions about permitted paths are answered with ArchUnit and
	 * instrumentation.
	 */
	@PublicTest
	@Policy(value = ARCHUNIT_INSTRUMENTATION_POLICY, withinPath = WITHIN_PATH)
	void test_inspectPermittedPathMavenArchunitInstrumentation() {
		assertPermittedPathsAnswered();
	}

	/**
	 * Questions about permitted paths are answered with WALA and AspectJ.
	 */
	@PublicTest
	@Policy(value = WALA_ASPECTJ_POLICY, withinPath = WITHIN_PATH)
	void test_inspectPermittedPathMavenWalaAspectJ() {
		assertPermittedPathsAnswered();
	}

	/**
	 * Questions about permitted paths are answered with WALA and instrumentation.
	 */
	@PublicTest
	@Policy(value = WALA_INSTRUMENTATION_POLICY, withinPath = WITHIN_PATH)
	void test_inspectPermittedPathMavenWalaInstrumentation() {
		assertPermittedPathsAnswered();
	}

	// </editor-fold>

	// <editor-fold desc="A forbidden path is refused">

	/**
	 * Questions about forbidden paths are refused with ArchUnit and AspectJ.
	 */
	@PublicTest
	@Policy(value = ARCHUNIT_ASPECTJ_POLICY, withinPath = WITHIN_PATH)
	void test_inspectForbiddenPathMavenArchunitAspectJ() {
		assertForbiddenPathsRefused();
	}

	/**
	 * Questions about forbidden paths are refused with ArchUnit and
	 * instrumentation.
	 */
	@PublicTest
	@Policy(value = ARCHUNIT_INSTRUMENTATION_POLICY, withinPath = WITHIN_PATH)
	void test_inspectForbiddenPathMavenArchunitInstrumentation() {
		assertForbiddenPathsRefused();
	}

	/**
	 * Questions about forbidden paths are refused with WALA and AspectJ.
	 */
	@PublicTest
	@Policy(value = WALA_ASPECTJ_POLICY, withinPath = WITHIN_PATH)
	void test_inspectForbiddenPathMavenWalaAspectJ() {
		assertForbiddenPathsRefused();
	}

	/**
	 * Questions about forbidden paths are refused with WALA and instrumentation.
	 */
	@PublicTest
	@Policy(value = WALA_INSTRUMENTATION_POLICY, withinPath = WITHIN_PATH)
	void test_inspectForbiddenPathMavenWalaInstrumentation() {
		assertForbiddenPathsRefused();
	}

	// </editor-fold>

	// <editor-fold desc="A rule without actions permits no question">

	/**
	 * A rule without actions permits no question, with ArchUnit and AspectJ.
	 */
	@PublicTest
	@Policy(value = ARCHUNIT_ASPECTJ_ALL_FALSE_POLICY, withinPath = WITHIN_PATH)
	void test_inspectRuleWithoutActionsMavenArchunitAspectJ() {
		assertRuleWithoutActionsRefuses();
	}

	/**
	 * A rule without actions permits no question, with ArchUnit and
	 * instrumentation.
	 */
	@PublicTest
	@Policy(value = ARCHUNIT_INSTRUMENTATION_ALL_FALSE_POLICY, withinPath = WITHIN_PATH)
	void test_inspectRuleWithoutActionsMavenArchunitInstrumentation() {
		assertRuleWithoutActionsRefuses();
	}

	/**
	 * A rule without actions permits no question, with WALA and AspectJ.
	 */
	@PublicTest
	@Policy(value = WALA_ASPECTJ_ALL_FALSE_POLICY, withinPath = WITHIN_PATH)
	void test_inspectRuleWithoutActionsMavenWalaAspectJ() {
		assertRuleWithoutActionsRefuses();
	}

	/**
	 * A rule without actions permits no question, with WALA and instrumentation.
	 */
	@PublicTest
	@Policy(value = WALA_INSTRUMENTATION_ALL_FALSE_POLICY, withinPath = WITHIN_PATH)
	void test_inspectRuleWithoutActionsMavenWalaInstrumentation() {
		assertRuleWithoutActionsRefuses();
	}

	// </editor-fold>

	// <editor-fold desc="Permitted work keeps working">

	/**
	 * Permitted work keeps working with ArchUnit and AspectJ.
	 *
	 * @param temporaryFolder a folder JUnit makes and removes around the test
	 */
	@PublicTest
	@Policy(value = ARCHUNIT_ASPECTJ_POLICY, withinPath = WITHIN_PATH)
	void test_inspectPermittedWorkMavenArchunitAspectJ(@TempDir Path temporaryFolder) {
		Assertions.assertTrue(Files.isDirectory(temporaryFolder), "JUnit did not make the temporary folder");
		assertPermittedWorkStillWorks();
		assertProbeContentTypeRefusedAsRead();
	}

	/**
	 * Permitted work keeps working with ArchUnit and instrumentation.
	 *
	 * @param temporaryFolder a folder JUnit makes and removes around the test
	 */
	@PublicTest
	@Policy(value = ARCHUNIT_INSTRUMENTATION_POLICY, withinPath = WITHIN_PATH)
	void test_inspectPermittedWorkMavenArchunitInstrumentation(@TempDir Path temporaryFolder) {
		Assertions.assertTrue(Files.isDirectory(temporaryFolder), "JUnit did not make the temporary folder");
		assertPermittedWorkStillWorks();
		assertProbeContentTypeRefusedAsRead();
	}

	/**
	 * Permitted work keeps working with WALA and AspectJ.
	 *
	 * @param temporaryFolder a folder JUnit makes and removes around the test
	 */
	@PublicTest
	@Policy(value = WALA_ASPECTJ_POLICY, withinPath = WITHIN_PATH)
	void test_inspectPermittedWorkMavenWalaAspectJ(@TempDir Path temporaryFolder) {
		Assertions.assertTrue(Files.isDirectory(temporaryFolder), "JUnit did not make the temporary folder");
		assertPermittedWorkStillWorks();
		assertProbeContentTypeRefusedAsRead();
	}

	/**
	 * Permitted work keeps working with WALA and instrumentation.
	 *
	 * @param temporaryFolder a folder JUnit makes and removes around the test
	 */
	@PublicTest
	@Policy(value = WALA_INSTRUMENTATION_POLICY, withinPath = WITHIN_PATH)
	void test_inspectPermittedWorkMavenWalaInstrumentation(@TempDir Path temporaryFolder) {
		Assertions.assertTrue(Files.isDirectory(temporaryFolder), "JUnit did not make the temporary folder");
		assertPermittedWorkStillWorks();
		assertProbeContentTypeRefusedAsRead();
	}

	// </editor-fold>

	// <editor-fold desc="Links are followed">

	/**
	 * Links are followed before the check with ArchUnit and AspectJ.
	 */
	@PublicTest
	@Policy(value = ARCHUNIT_ASPECTJ_POLICY, withinPath = WITHIN_PATH)
	void test_inspectLinksMavenArchunitAspectJ() {
		assertLinksAreFollowed();
	}

	/**
	 * Links are followed before the check with ArchUnit and instrumentation.
	 */
	@PublicTest
	@Policy(value = ARCHUNIT_INSTRUMENTATION_POLICY, withinPath = WITHIN_PATH)
	void test_inspectLinksMavenArchunitInstrumentation() {
		assertLinksAreFollowed();
	}

	/**
	 * Links are followed before the check with WALA and AspectJ.
	 */
	@PublicTest
	@Policy(value = WALA_ASPECTJ_POLICY, withinPath = WITHIN_PATH)
	void test_inspectLinksMavenWalaAspectJ() {
		assertLinksAreFollowed();
	}

	/**
	 * Links are followed before the check with WALA and instrumentation.
	 */
	@PublicTest
	@Policy(value = WALA_INSTRUMENTATION_POLICY, withinPath = WITHIN_PATH)
	void test_inspectLinksMavenWalaInstrumentation() {
		assertLinksAreFollowed();
	}

	// </editor-fold>

	// <editor-fold desc="Instrumentation checks the questions the JDK asks">

	/**
	 * Instrumentation also checks a question the JDK asks on the student's behalf:
	 * through a method reference, and while a walk follows a link out of the
	 * permitted folder. The JDK's walk skips an entry it may not ask about, so the
	 * link is missing from the walk rather than ending it.
	 */
	private void assertQuestionsAskedByTheJdkRefused() {
		Path outward = Paths.get(InspectMain.BASE, "readable", "outward-link");
		makeLink(outward, FORBIDDEN.toAbsolutePath());
		Assertions.assertAll(
				() -> assertInspectionRefused("File::exists through a stream",
						() -> InspectMain.fileExistsThroughStream(InspectMain.FORBIDDEN_FILE), FORBIDDEN),
				() -> Assertions.assertFalse(
						InspectMain.walkFollowingLinks(InspectMain.BASE + "/readable").contains("outward-link"),
						"A walk followed a link out of the readable folder"));
	}

	/**
	 * Questions the JDK asks are checked with ArchUnit and instrumentation.
	 */
	@PublicTest
	@Policy(value = ARCHUNIT_INSTRUMENTATION_POLICY, withinPath = WITHIN_PATH)
	void test_inspectAskedByTheJdkMavenArchunitInstrumentation() {
		assertQuestionsAskedByTheJdkRefused();
	}

	/**
	 * Questions the JDK asks are checked with WALA and instrumentation.
	 */
	@PublicTest
	@Policy(value = WALA_INSTRUMENTATION_POLICY, withinPath = WITHIN_PATH)
	void test_inspectAskedByTheJdkMavenWalaInstrumentation() {
		assertQuestionsAskedByTheJdkRefused();
	}

	// </editor-fold>
}
