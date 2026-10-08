package de.tum.cit.ase.ares.integration.aop.forbidden;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;

import de.tum.cit.ase.ares.api.Policy;
import de.tum.cit.ase.ares.api.jupiter.PublicTest;
import de.tum.cit.ase.ares.integration.aop.forbidden.subject.baseline.runtime.BaselineStillDeniedMain;

/**
 * Runs student code trying what the secure baseline still denies, under a
 * policy with one unrelated create path so the runtime checks decide, in all
 * four mode combinations.
 */
class BaselineStillDeniedTest extends SystemAccessTest {

	/**
	 * Folder of the student code the static analysis inspects.
	 */
	private static final String WITHIN_PATH = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/baseline/runtime";

	/**
	 * A random-seed device.
	 */
	private static final Path ENTROPY_DEVICE = Path.of("/dev/urandom");

	/**
	 * The system timezone file.
	 */
	private static final Path SYSTEM_TIMEZONE_FILE = Path.of("/etc/localtime");

	/**
	 * A directory no policy lists.
	 */
	private static final Path NOT_TRUSTED_DIRECTORY = Path
			.of("src/test/java/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/create/nottrusteddir");

	/**
	 * Policy with one unrelated create path for MavenArchunitAspectJ.
	 */
	private static final String POLICY_ARCHUNITASPECTJ = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/aspectj/PolicyOnePathAllowedCreate.yaml";

	/**
	 * Policy with one unrelated create path for MavenArchunitInstrumentation.
	 */
	private static final String POLICY_ARCHUNITINSTRUMENTATION = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/instrumentation/PolicyOnePathAllowedCreate.yaml";

	/**
	 * Policy with one unrelated create path for MavenWalaAspectJ.
	 */
	private static final String POLICY_WALAASPECTJ = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/aspectj/PolicyOnePathAllowedCreate.yaml";

	/**
	 * Policy with one unrelated create path for MavenWalaInstrumentation.
	 */
	private static final String POLICY_WALAINSTRUMENTATION = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/instrumentation/PolicyOnePathAllowedCreate.yaml";

	/**
	 * A subdirectory of the default temp directory, created before the tests.
	 */
	private static Path tempSubdirectory;

	/**
	 * A link in the default temp directory leading to the untrusted directory, or
	 * {@code null} where links are not available.
	 */
	private static Path tempLink;

	/**
	 * Creates the subdirectory and the link, outside any policy.
	 *
	 * @throws IOException if the subdirectory cannot be created
	 */
	@BeforeAll
	static void createTempDirectoryFixtures() throws IOException {
		Path tempDirectory = Path.of(System.getProperty("java.io.tmpdir"));
		tempSubdirectory = Files.createDirectories(tempDirectory.resolve("ares-baseline-subdirectory"));
		Path link = tempDirectory.resolve("ares-baseline-link");
		Files.deleteIfExists(link);
		try {
			tempLink = Files.createSymbolicLink(link, NOT_TRUSTED_DIRECTORY.toAbsolutePath());
		} catch (UnsupportedOperationException | IOException unavailable) {
			tempLink = null;
		}
	}

	/**
	 * Removes the subdirectory and the link.
	 *
	 * @throws IOException if one cannot be removed
	 */
	@AfterAll
	static void deleteTempDirectoryFixtures() throws IOException {
		if (tempLink != null) {
			Files.deleteIfExists(tempLink);
		}
		Files.deleteIfExists(tempSubdirectory.resolve("planted.txt"));
		Files.deleteIfExists(tempSubdirectory);
	}

	/**
	 * Skips a test on a system without the given file.
	 *
	 * @param file the file the test needs
	 */
	private static void assumeExists(Path file) {
		Assumptions.assumeTrue(Files.exists(file), () -> "requires " + file);
	}

	// <editor-fold desc="readEntropyDeviceDirectly">
	/**
	 * Reading {@code /dev/urandom} directly is denied: only the JDK's own seeding
	 * may read it.
	 */
	@PublicTest
	@Policy(value = POLICY_ARCHUNITASPECTJ, withinPath = WITHIN_PATH)
	void test_readEntropyDeviceDirectlyMavenArchunitAspectJ() {
		assumeExists(ENTROPY_DEVICE);
		assertAresSecurityExceptionRead(BaselineStillDeniedMain::readEntropyDeviceDirectly,
				BaselineStillDeniedMain.class, ENTROPY_DEVICE);
	}

	/**
	 * Reading {@code /dev/urandom} directly is denied: only the JDK's own seeding
	 * may read it.
	 */
	@PublicTest
	@Policy(value = POLICY_ARCHUNITINSTRUMENTATION, withinPath = WITHIN_PATH)
	void test_readEntropyDeviceDirectlyMavenArchunitInstrumentation() {
		assumeExists(ENTROPY_DEVICE);
		assertAresSecurityExceptionRead(BaselineStillDeniedMain::readEntropyDeviceDirectly,
				BaselineStillDeniedMain.class, ENTROPY_DEVICE);
	}

	/**
	 * Reading {@code /dev/urandom} directly is denied: only the JDK's own seeding
	 * may read it.
	 */
	@PublicTest
	@Policy(value = POLICY_WALAASPECTJ, withinPath = WITHIN_PATH)
	void test_readEntropyDeviceDirectlyMavenWalaAspectJ() {
		assumeExists(ENTROPY_DEVICE);
		assertAresSecurityExceptionRead(BaselineStillDeniedMain::readEntropyDeviceDirectly,
				BaselineStillDeniedMain.class, ENTROPY_DEVICE);
	}

	/**
	 * Reading {@code /dev/urandom} directly is denied: only the JDK's own seeding
	 * may read it.
	 */
	@PublicTest
	@Policy(value = POLICY_WALAINSTRUMENTATION, withinPath = WITHIN_PATH)
	void test_readEntropyDeviceDirectlyMavenWalaInstrumentation() {
		assumeExists(ENTROPY_DEVICE);
		assertAresSecurityExceptionRead(BaselineStillDeniedMain::readEntropyDeviceDirectly,
				BaselineStillDeniedMain.class, ENTROPY_DEVICE);
	}
	// </editor-fold>

	// <editor-fold desc="readSystemTimezoneFileDirectly">
	/**
	 * Reading {@code /etc/localtime} directly is denied; no exemption exists for
	 * it.
	 */
	@PublicTest
	@Policy(value = POLICY_ARCHUNITASPECTJ, withinPath = WITHIN_PATH)
	void test_readSystemTimezoneFileDirectlyMavenArchunitAspectJ() {
		assumeExists(SYSTEM_TIMEZONE_FILE);
		assertAresSecurityExceptionRead(BaselineStillDeniedMain::readSystemTimezoneFileDirectly,
				BaselineStillDeniedMain.class, SYSTEM_TIMEZONE_FILE);
	}

	/**
	 * Reading {@code /etc/localtime} directly is denied; no exemption exists for
	 * it.
	 */
	@PublicTest
	@Policy(value = POLICY_ARCHUNITINSTRUMENTATION, withinPath = WITHIN_PATH)
	void test_readSystemTimezoneFileDirectlyMavenArchunitInstrumentation() {
		assumeExists(SYSTEM_TIMEZONE_FILE);
		assertAresSecurityExceptionRead(BaselineStillDeniedMain::readSystemTimezoneFileDirectly,
				BaselineStillDeniedMain.class, SYSTEM_TIMEZONE_FILE);
	}

	/**
	 * Reading {@code /etc/localtime} directly is denied; no exemption exists for
	 * it.
	 */
	@PublicTest
	@Policy(value = POLICY_WALAASPECTJ, withinPath = WITHIN_PATH)
	void test_readSystemTimezoneFileDirectlyMavenWalaAspectJ() {
		assumeExists(SYSTEM_TIMEZONE_FILE);
		assertAresSecurityExceptionRead(BaselineStillDeniedMain::readSystemTimezoneFileDirectly,
				BaselineStillDeniedMain.class, SYSTEM_TIMEZONE_FILE);
	}

	/**
	 * Reading {@code /etc/localtime} directly is denied; no exemption exists for
	 * it.
	 */
	@PublicTest
	@Policy(value = POLICY_WALAINSTRUMENTATION, withinPath = WITHIN_PATH)
	void test_readSystemTimezoneFileDirectlyMavenWalaInstrumentation() {
		assumeExists(SYSTEM_TIMEZONE_FILE);
		assertAresSecurityExceptionRead(BaselineStillDeniedMain::readSystemTimezoneFileDirectly,
				BaselineStillDeniedMain.class, SYSTEM_TIMEZONE_FILE);
	}
	// </editor-fold>

	// <editor-fold desc="createTempFileInUntrustedDirectory">
	/**
	 * {@code File.createTempFile} in a directory the policy does not list is
	 * denied.
	 */
	@PublicTest
	@Policy(value = POLICY_ARCHUNITASPECTJ, withinPath = WITHIN_PATH)
	void test_createTempFileInUntrustedDirectoryMavenArchunitAspectJ() {
		assertAresSecurityExceptionCreate(
				() -> BaselineStillDeniedMain.createTempFileIn(NOT_TRUSTED_DIRECTORY.toFile()),
				BaselineStillDeniedMain.class);
	}

	/**
	 * {@code File.createTempFile} in a directory the policy does not list is
	 * denied.
	 */
	@PublicTest
	@Policy(value = POLICY_ARCHUNITINSTRUMENTATION, withinPath = WITHIN_PATH)
	void test_createTempFileInUntrustedDirectoryMavenArchunitInstrumentation() {
		assertAresSecurityExceptionCreate(
				() -> BaselineStillDeniedMain.createTempFileIn(NOT_TRUSTED_DIRECTORY.toFile()),
				BaselineStillDeniedMain.class);
	}

	/**
	 * {@code File.createTempFile} in a directory the policy does not list is
	 * denied.
	 */
	@PublicTest
	@Policy(value = POLICY_WALAASPECTJ, withinPath = WITHIN_PATH)
	void test_createTempFileInUntrustedDirectoryMavenWalaAspectJ() {
		assertAresSecurityExceptionCreate(
				() -> BaselineStillDeniedMain.createTempFileIn(NOT_TRUSTED_DIRECTORY.toFile()),
				BaselineStillDeniedMain.class);
	}

	/**
	 * {@code File.createTempFile} in a directory the policy does not list is
	 * denied.
	 */
	@PublicTest
	@Policy(value = POLICY_WALAINSTRUMENTATION, withinPath = WITHIN_PATH)
	void test_createTempFileInUntrustedDirectoryMavenWalaInstrumentation() {
		assertAresSecurityExceptionCreate(
				() -> BaselineStillDeniedMain.createTempFileIn(NOT_TRUSTED_DIRECTORY.toFile()),
				BaselineStillDeniedMain.class);
	}
	// </editor-fold>

	// <editor-fold desc="createTempFileWithFilesInUntrustedDirectory">
	/**
	 * {@code Files.createTempFile} in a directory the policy does not list is
	 * denied.
	 */
	@PublicTest
	@Policy(value = POLICY_ARCHUNITASPECTJ, withinPath = WITHIN_PATH)
	void test_createTempFileWithFilesInUntrustedDirectoryMavenArchunitAspectJ() {
		assertAresSecurityExceptionCreate(
				() -> BaselineStillDeniedMain.createTempFileWithFilesIn(NOT_TRUSTED_DIRECTORY),
				BaselineStillDeniedMain.class);
	}

	/**
	 * {@code Files.createTempFile} in a directory the policy does not list is
	 * denied.
	 */
	@PublicTest
	@Policy(value = POLICY_ARCHUNITINSTRUMENTATION, withinPath = WITHIN_PATH)
	void test_createTempFileWithFilesInUntrustedDirectoryMavenArchunitInstrumentation() {
		assertAresSecurityExceptionCreate(
				() -> BaselineStillDeniedMain.createTempFileWithFilesIn(NOT_TRUSTED_DIRECTORY),
				BaselineStillDeniedMain.class);
	}

	/**
	 * {@code Files.createTempFile} in a directory the policy does not list is
	 * denied.
	 */
	@PublicTest
	@Policy(value = POLICY_WALAASPECTJ, withinPath = WITHIN_PATH)
	void test_createTempFileWithFilesInUntrustedDirectoryMavenWalaAspectJ() {
		assertAresSecurityExceptionCreate(
				() -> BaselineStillDeniedMain.createTempFileWithFilesIn(NOT_TRUSTED_DIRECTORY),
				BaselineStillDeniedMain.class);
	}

	/**
	 * {@code Files.createTempFile} in a directory the policy does not list is
	 * denied.
	 */
	@PublicTest
	@Policy(value = POLICY_WALAINSTRUMENTATION, withinPath = WITHIN_PATH)
	void test_createTempFileWithFilesInUntrustedDirectoryMavenWalaInstrumentation() {
		assertAresSecurityExceptionCreate(
				() -> BaselineStillDeniedMain.createTempFileWithFilesIn(NOT_TRUSTED_DIRECTORY),
				BaselineStillDeniedMain.class);
	}
	// </editor-fold>

	// <editor-fold desc="createTempFileInSubdirectoryOfTempDirectory">
	/**
	 * A subdirectory of the default temp directory is not the default temp
	 * directory, so it is denied.
	 */
	@PublicTest
	@Policy(value = POLICY_ARCHUNITASPECTJ, withinPath = WITHIN_PATH)
	void test_createTempFileInSubdirectoryOfTempDirectoryMavenArchunitAspectJ() {
		assertAresSecurityExceptionCreate(() -> BaselineStillDeniedMain.createTempFileIn(tempSubdirectory.toFile()),
				BaselineStillDeniedMain.class, tempSubdirectory);
	}

	/**
	 * A subdirectory of the default temp directory is not the default temp
	 * directory, so it is denied.
	 */
	@PublicTest
	@Policy(value = POLICY_ARCHUNITINSTRUMENTATION, withinPath = WITHIN_PATH)
	void test_createTempFileInSubdirectoryOfTempDirectoryMavenArchunitInstrumentation() {
		assertAresSecurityExceptionCreate(() -> BaselineStillDeniedMain.createTempFileIn(tempSubdirectory.toFile()),
				BaselineStillDeniedMain.class, tempSubdirectory);
	}

	/**
	 * A subdirectory of the default temp directory is not the default temp
	 * directory, so it is denied.
	 */
	@PublicTest
	@Policy(value = POLICY_WALAASPECTJ, withinPath = WITHIN_PATH)
	void test_createTempFileInSubdirectoryOfTempDirectoryMavenWalaAspectJ() {
		assertAresSecurityExceptionCreate(() -> BaselineStillDeniedMain.createTempFileIn(tempSubdirectory.toFile()),
				BaselineStillDeniedMain.class, tempSubdirectory);
	}

	/**
	 * A subdirectory of the default temp directory is not the default temp
	 * directory, so it is denied.
	 */
	@PublicTest
	@Policy(value = POLICY_WALAINSTRUMENTATION, withinPath = WITHIN_PATH)
	void test_createTempFileInSubdirectoryOfTempDirectoryMavenWalaInstrumentation() {
		assertAresSecurityExceptionCreate(() -> BaselineStillDeniedMain.createTempFileIn(tempSubdirectory.toFile()),
				BaselineStillDeniedMain.class, tempSubdirectory);
	}
	// </editor-fold>

	// <editor-fold desc="createTempFileThroughLinkInTempDirectory">
	/**
	 * A link inside the default temp directory that leads elsewhere is judged by
	 * where it leads, so it is denied.
	 */
	@PublicTest
	@Policy(value = POLICY_ARCHUNITASPECTJ, withinPath = WITHIN_PATH)
	void test_createTempFileThroughLinkInTempDirectoryMavenArchunitAspectJ() {
		Assumptions.assumeTrue(tempLink != null, "symbolic links are not available here");
		assertAresSecurityExceptionCreate(() -> BaselineStillDeniedMain.createTempFileIn(tempLink.toFile()),
				BaselineStillDeniedMain.class, tempLink);
	}

	/**
	 * A link inside the default temp directory that leads elsewhere is judged by
	 * where it leads, so it is denied.
	 */
	@PublicTest
	@Policy(value = POLICY_ARCHUNITINSTRUMENTATION, withinPath = WITHIN_PATH)
	void test_createTempFileThroughLinkInTempDirectoryMavenArchunitInstrumentation() {
		Assumptions.assumeTrue(tempLink != null, "symbolic links are not available here");
		assertAresSecurityExceptionCreate(() -> BaselineStillDeniedMain.createTempFileIn(tempLink.toFile()),
				BaselineStillDeniedMain.class, tempLink);
	}

	/**
	 * A link inside the default temp directory that leads elsewhere is judged by
	 * where it leads, so it is denied.
	 */
	@PublicTest
	@Policy(value = POLICY_WALAASPECTJ, withinPath = WITHIN_PATH)
	void test_createTempFileThroughLinkInTempDirectoryMavenWalaAspectJ() {
		Assumptions.assumeTrue(tempLink != null, "symbolic links are not available here");
		assertAresSecurityExceptionCreate(() -> BaselineStillDeniedMain.createTempFileIn(tempLink.toFile()),
				BaselineStillDeniedMain.class, tempLink);
	}

	/**
	 * A link inside the default temp directory that leads elsewhere is judged by
	 * where it leads, so it is denied.
	 */
	@PublicTest
	@Policy(value = POLICY_WALAINSTRUMENTATION, withinPath = WITHIN_PATH)
	void test_createTempFileThroughLinkInTempDirectoryMavenWalaInstrumentation() {
		Assumptions.assumeTrue(tempLink != null, "symbolic links are not available here");
		assertAresSecurityExceptionCreate(() -> BaselineStillDeniedMain.createTempFileIn(tempLink.toFile()),
				BaselineStillDeniedMain.class, tempLink);
	}
	// </editor-fold>

	// <editor-fold desc="plantFileFromTempFileAttribute">
	/**
	 * A file attribute whose name the JDK reads while creating a temp file cannot
	 * use that moment to create a file elsewhere.
	 */
	@PublicTest
	@Policy(value = POLICY_ARCHUNITASPECTJ, withinPath = WITHIN_PATH)
	void test_plantFileFromTempFileAttributeMavenArchunitAspectJ() {
		assertAresSecurityExceptionCreate(
				() -> BaselineStillDeniedMain
						.createTempFileWithPlantingAttribute(tempSubdirectory.resolve("planted.txt")),
				BaselineStillDeniedMain.class, tempSubdirectory.resolve("planted.txt"));
	}

	/**
	 * A file attribute whose name the JDK reads while creating a temp file cannot
	 * use that moment to create a file elsewhere.
	 */
	@PublicTest
	@Policy(value = POLICY_ARCHUNITINSTRUMENTATION, withinPath = WITHIN_PATH)
	void test_plantFileFromTempFileAttributeMavenArchunitInstrumentation() {
		assertAresSecurityExceptionCreate(
				() -> BaselineStillDeniedMain
						.createTempFileWithPlantingAttribute(tempSubdirectory.resolve("planted.txt")),
				BaselineStillDeniedMain.class, tempSubdirectory.resolve("planted.txt"));
	}

	/**
	 * A file attribute whose name the JDK reads while creating a temp file cannot
	 * use that moment to create a file elsewhere.
	 */
	@PublicTest
	@Policy(value = POLICY_WALAASPECTJ, withinPath = WITHIN_PATH)
	void test_plantFileFromTempFileAttributeMavenWalaAspectJ() {
		assertAresSecurityExceptionCreate(
				() -> BaselineStillDeniedMain
						.createTempFileWithPlantingAttribute(tempSubdirectory.resolve("planted.txt")),
				BaselineStillDeniedMain.class, tempSubdirectory.resolve("planted.txt"));
	}

	/**
	 * A file attribute whose name the JDK reads while creating a temp file cannot
	 * use that moment to create a file elsewhere.
	 */
	@PublicTest
	@Policy(value = POLICY_WALAINSTRUMENTATION, withinPath = WITHIN_PATH)
	void test_plantFileFromTempFileAttributeMavenWalaInstrumentation() {
		assertAresSecurityExceptionCreate(
				() -> BaselineStillDeniedMain
						.createTempFileWithPlantingAttribute(tempSubdirectory.resolve("planted.txt")),
				BaselineStillDeniedMain.class, tempSubdirectory.resolve("planted.txt"));
	}
	// </editor-fold>

	// <editor-fold desc="createTempDirectory">
	/**
	 * A temporary directory is not standard-allowed and needs an entry, even
	 * without a named directory.
	 */
	@PublicTest
	@Policy(value = POLICY_ARCHUNITASPECTJ, withinPath = WITHIN_PATH)
	void test_createTempDirectoryMavenArchunitAspectJ() {
		assertThrowsAresSecurityException(BaselineStillDeniedMain::createTempDirectory);
	}

	/**
	 * A temporary directory is not standard-allowed and needs an entry, even
	 * without a named directory.
	 */
	@PublicTest
	@Policy(value = POLICY_ARCHUNITINSTRUMENTATION, withinPath = WITHIN_PATH)
	void test_createTempDirectoryMavenArchunitInstrumentation() {
		assertThrowsAresSecurityException(BaselineStillDeniedMain::createTempDirectory);
	}

	/**
	 * A temporary directory is not standard-allowed and needs an entry, even
	 * without a named directory.
	 */
	@PublicTest
	@Policy(value = POLICY_WALAASPECTJ, withinPath = WITHIN_PATH)
	void test_createTempDirectoryMavenWalaAspectJ() {
		assertThrowsAresSecurityException(BaselineStillDeniedMain::createTempDirectory);
	}

	/**
	 * A temporary directory is not standard-allowed and needs an entry, even
	 * without a named directory.
	 */
	@PublicTest
	@Policy(value = POLICY_WALAINSTRUMENTATION, withinPath = WITHIN_PATH)
	void test_createTempDirectoryMavenWalaInstrumentation() {
		assertThrowsAresSecurityException(BaselineStillDeniedMain::createTempDirectory);
	}
	// </editor-fold>
}
