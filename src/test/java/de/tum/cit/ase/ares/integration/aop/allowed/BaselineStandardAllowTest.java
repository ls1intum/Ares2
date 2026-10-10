package de.tum.cit.ase.ares.integration.aop.allowed;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.junit.jupiter.api.AfterAll;

import de.tum.cit.ase.ares.api.Policy;
import de.tum.cit.ase.ares.api.jupiter.PublicTest;
import de.tum.cit.ase.ares.integration.aop.allowed.subject.baseline.BaselineStandardAllowMain;
import de.tum.cit.ase.ares.integration.aop.allowed.subject.baselineCertificates.TrustedCertificatesMain;

/**
 * Runs student code that uses the secure baseline's standard-allowed operations
 * under a policy granting no file access, in all four mode combinations,
 * through the real static analysis and the real runtime checks.
 */
class BaselineStandardAllowTest {

	/**
	 * Folder of the student code the static analysis inspects.
	 */
	private static final String WITHIN_PATH = "test-classes/de/tum/cit/ase/ares/integration/aop/allowed/subject/baseline";

	/**
	 * Folder of the student code loading the trusted certificates.
	 */
	private static final String CERTIFICATES_WITHIN_PATH = "test-classes/de/tum/cit/ase/ares/integration/aop/allowed/subject/baselineCertificates";

	/**
	 * Policy without file access for MavenArchunitAspectJ.
	 */
	private static final String POLICY_ARCHUNITASPECTJ = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/aspectj/PolicyBaselineStandardAllow.yaml";

	/**
	 * Policy with one network connection and no file access for
	 * MavenArchunitAspectJ.
	 */
	private static final String CERTIFICATES_POLICY_ARCHUNITASPECTJ = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/aspectj/PolicyBaselineTrustedCertificates.yaml";

	/**
	 * Policy without file access for MavenArchunitInstrumentation.
	 */
	private static final String POLICY_ARCHUNITINSTRUMENTATION = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/instrumentation/PolicyBaselineStandardAllow.yaml";

	/**
	 * Policy with one network connection and no file access for
	 * MavenArchunitInstrumentation.
	 */
	private static final String CERTIFICATES_POLICY_ARCHUNITINSTRUMENTATION = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/instrumentation/PolicyBaselineTrustedCertificates.yaml";

	/**
	 * Policy without file access for MavenWalaAspectJ.
	 */
	private static final String POLICY_WALAASPECTJ = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/aspectj/PolicyBaselineStandardAllow.yaml";

	/**
	 * Policy with one network connection and no file access for MavenWalaAspectJ.
	 */
	private static final String CERTIFICATES_POLICY_WALAASPECTJ = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/aspectj/PolicyBaselineTrustedCertificates.yaml";

	/**
	 * Policy without file access for MavenWalaInstrumentation.
	 */
	private static final String POLICY_WALAINSTRUMENTATION = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/instrumentation/PolicyBaselineStandardAllow.yaml";

	/**
	 * Policy with one network connection and no file access for
	 * MavenWalaInstrumentation.
	 */
	private static final String CERTIFICATES_POLICY_WALAINSTRUMENTATION = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/instrumentation/PolicyBaselineTrustedCertificates.yaml";

	/**
	 * Temporary files the tests created, deleted once all tests are done, outside
	 * any policy.
	 */
	private static final Queue<Path> CREATED_FILES = new ConcurrentLinkedQueue<>();

	// <editor-fold desc="createTempFileWithFileWithoutDirectory">
	/**
	 * {@code File.createTempFile} without a directory works without a policy entry
	 * (item 1.15).
	 */
	@PublicTest
	@Policy(value = POLICY_ARCHUNITASPECTJ, withinPath = WITHIN_PATH)
	void test_createTempFileWithFileWithoutDirectoryMavenArchunitAspectJ() {
		assertDoesNotThrow(() -> remember(BaselineStandardAllowMain.createTempFileWithFile().toPath()));
	}

	/**
	 * {@code File.createTempFile} without a directory works without a policy entry
	 * (item 1.15).
	 */
	@PublicTest
	@Policy(value = POLICY_ARCHUNITINSTRUMENTATION, withinPath = WITHIN_PATH)
	void test_createTempFileWithFileWithoutDirectoryMavenArchunitInstrumentation() {
		assertDoesNotThrow(() -> remember(BaselineStandardAllowMain.createTempFileWithFile().toPath()));
	}

	/**
	 * {@code File.createTempFile} without a directory works without a policy entry
	 * (item 1.15).
	 */
	@PublicTest
	@Policy(value = POLICY_WALAASPECTJ, withinPath = WITHIN_PATH)
	void test_createTempFileWithFileWithoutDirectoryMavenWalaAspectJ() {
		assertDoesNotThrow(() -> remember(BaselineStandardAllowMain.createTempFileWithFile().toPath()));
	}

	/**
	 * {@code File.createTempFile} without a directory works without a policy entry
	 * (item 1.15).
	 */
	@PublicTest
	@Policy(value = POLICY_WALAINSTRUMENTATION, withinPath = WITHIN_PATH)
	void test_createTempFileWithFileWithoutDirectoryMavenWalaInstrumentation() {
		assertDoesNotThrow(() -> remember(BaselineStandardAllowMain.createTempFileWithFile().toPath()));
	}
	// </editor-fold>

	// <editor-fold desc="createTempFileWithFilesWithoutDirectory">
	/**
	 * {@code Files.createTempFile} without a directory works without a policy entry
	 * (item 1.15).
	 */
	@PublicTest
	@Policy(value = POLICY_ARCHUNITASPECTJ, withinPath = WITHIN_PATH)
	void test_createTempFileWithFilesWithoutDirectoryMavenArchunitAspectJ() {
		assertDoesNotThrow(() -> remember(BaselineStandardAllowMain.createTempFileWithFiles()));
	}

	/**
	 * {@code Files.createTempFile} without a directory works without a policy entry
	 * (item 1.15).
	 */
	@PublicTest
	@Policy(value = POLICY_ARCHUNITINSTRUMENTATION, withinPath = WITHIN_PATH)
	void test_createTempFileWithFilesWithoutDirectoryMavenArchunitInstrumentation() {
		assertDoesNotThrow(() -> remember(BaselineStandardAllowMain.createTempFileWithFiles()));
	}

	/**
	 * {@code Files.createTempFile} without a directory works without a policy entry
	 * (item 1.15).
	 */
	@PublicTest
	@Policy(value = POLICY_WALAASPECTJ, withinPath = WITHIN_PATH)
	void test_createTempFileWithFilesWithoutDirectoryMavenWalaAspectJ() {
		assertDoesNotThrow(() -> remember(BaselineStandardAllowMain.createTempFileWithFiles()));
	}

	/**
	 * {@code Files.createTempFile} without a directory works without a policy entry
	 * (item 1.15).
	 */
	@PublicTest
	@Policy(value = POLICY_WALAINSTRUMENTATION, withinPath = WITHIN_PATH)
	void test_createTempFileWithFilesWithoutDirectoryMavenWalaInstrumentation() {
		assertDoesNotThrow(() -> remember(BaselineStandardAllowMain.createTempFileWithFiles()));
	}
	// </editor-fold>

	// <editor-fold desc="drawSecureRandomNumbers">
	/**
	 * {@code SecureRandom} works without a policy entry (item 1.11).
	 */
	@PublicTest
	@Policy(value = POLICY_ARCHUNITASPECTJ, withinPath = WITHIN_PATH)
	void test_drawSecureRandomNumbersMavenArchunitAspectJ() {
		assertDoesNotThrow(() -> BaselineStandardAllowMain.drawSecureRandomNumbers());
	}

	/**
	 * {@code SecureRandom} works without a policy entry (item 1.11).
	 */
	@PublicTest
	@Policy(value = POLICY_ARCHUNITINSTRUMENTATION, withinPath = WITHIN_PATH)
	void test_drawSecureRandomNumbersMavenArchunitInstrumentation() {
		assertDoesNotThrow(() -> BaselineStandardAllowMain.drawSecureRandomNumbers());
	}

	/**
	 * {@code SecureRandom} works without a policy entry (item 1.11).
	 */
	@PublicTest
	@Policy(value = POLICY_WALAASPECTJ, withinPath = WITHIN_PATH)
	void test_drawSecureRandomNumbersMavenWalaAspectJ() {
		assertDoesNotThrow(() -> BaselineStandardAllowMain.drawSecureRandomNumbers());
	}

	/**
	 * {@code SecureRandom} works without a policy entry (item 1.11).
	 */
	@PublicTest
	@Policy(value = POLICY_WALAINSTRUMENTATION, withinPath = WITHIN_PATH)
	void test_drawSecureRandomNumbersMavenWalaInstrumentation() {
		assertDoesNotThrow(() -> BaselineStandardAllowMain.drawSecureRandomNumbers());
	}
	// </editor-fold>

	// <editor-fold desc="createRandomUuid">
	/**
	 * {@code UUID.randomUUID()} works without a policy entry (item 1.11).
	 */
	@PublicTest
	@Policy(value = POLICY_ARCHUNITASPECTJ, withinPath = WITHIN_PATH)
	void test_createRandomUuidMavenArchunitAspectJ() {
		assertDoesNotThrow(() -> BaselineStandardAllowMain.createRandomUuid());
	}

	/**
	 * {@code UUID.randomUUID()} works without a policy entry (item 1.11).
	 */
	@PublicTest
	@Policy(value = POLICY_ARCHUNITINSTRUMENTATION, withinPath = WITHIN_PATH)
	void test_createRandomUuidMavenArchunitInstrumentation() {
		assertDoesNotThrow(() -> BaselineStandardAllowMain.createRandomUuid());
	}

	/**
	 * {@code UUID.randomUUID()} works without a policy entry (item 1.11).
	 */
	@PublicTest
	@Policy(value = POLICY_WALAASPECTJ, withinPath = WITHIN_PATH)
	void test_createRandomUuidMavenWalaAspectJ() {
		assertDoesNotThrow(() -> BaselineStandardAllowMain.createRandomUuid());
	}

	/**
	 * {@code UUID.randomUUID()} works without a policy entry (item 1.11).
	 */
	@PublicTest
	@Policy(value = POLICY_WALAINSTRUMENTATION, withinPath = WITHIN_PATH)
	void test_createRandomUuidMavenWalaInstrumentation() {
		assertDoesNotThrow(() -> BaselineStandardAllowMain.createRandomUuid());
	}
	// </editor-fold>

	// <editor-fold desc="readSystemTimezone">
	/**
	 * The system timezone works without a policy entry (item 1.12).
	 */
	@PublicTest
	@Policy(value = POLICY_ARCHUNITASPECTJ, withinPath = WITHIN_PATH)
	void test_readSystemTimezoneMavenArchunitAspectJ() {
		assertDoesNotThrow(() -> BaselineStandardAllowMain.readSystemTimezone());
	}

	/**
	 * The system timezone works without a policy entry (item 1.12).
	 */
	@PublicTest
	@Policy(value = POLICY_ARCHUNITINSTRUMENTATION, withinPath = WITHIN_PATH)
	void test_readSystemTimezoneMavenArchunitInstrumentation() {
		assertDoesNotThrow(() -> BaselineStandardAllowMain.readSystemTimezone());
	}

	/**
	 * The system timezone works without a policy entry (item 1.12).
	 */
	@PublicTest
	@Policy(value = POLICY_WALAASPECTJ, withinPath = WITHIN_PATH)
	void test_readSystemTimezoneMavenWalaAspectJ() {
		assertDoesNotThrow(() -> BaselineStandardAllowMain.readSystemTimezone());
	}

	/**
	 * The system timezone works without a policy entry (item 1.12).
	 */
	@PublicTest
	@Policy(value = POLICY_WALAINSTRUMENTATION, withinPath = WITHIN_PATH)
	void test_readSystemTimezoneMavenWalaInstrumentation() {
		assertDoesNotThrow(() -> BaselineStandardAllowMain.readSystemTimezone());
	}
	// </editor-fold>

	// <editor-fold desc="loadTrustedCertificates">
	/**
	 * The JDK's trusted certificates load without a file entry; encrypted
	 * connections need a network entry anyway (item 1.13).
	 */
	@PublicTest
	@Policy(value = CERTIFICATES_POLICY_ARCHUNITASPECTJ, withinPath = CERTIFICATES_WITHIN_PATH)
	void test_loadTrustedCertificatesMavenArchunitAspectJ() {
		assertDoesNotThrow(() -> TrustedCertificatesMain.loadTrustedCertificates());
	}

	/**
	 * The JDK's trusted certificates load without a file entry; encrypted
	 * connections need a network entry anyway (item 1.13).
	 */
	@PublicTest
	@Policy(value = CERTIFICATES_POLICY_ARCHUNITINSTRUMENTATION, withinPath = CERTIFICATES_WITHIN_PATH)
	void test_loadTrustedCertificatesMavenArchunitInstrumentation() {
		assertDoesNotThrow(() -> TrustedCertificatesMain.loadTrustedCertificates());
	}

	/**
	 * The JDK's trusted certificates load without a file entry; encrypted
	 * connections need a network entry anyway (item 1.13).
	 */
	@PublicTest
	@Policy(value = CERTIFICATES_POLICY_WALAASPECTJ, withinPath = CERTIFICATES_WITHIN_PATH)
	void test_loadTrustedCertificatesMavenWalaAspectJ() {
		assertDoesNotThrow(() -> TrustedCertificatesMain.loadTrustedCertificates());
	}

	/**
	 * The JDK's trusted certificates load without a file entry; encrypted
	 * connections need a network entry anyway (item 1.13).
	 */
	@PublicTest
	@Policy(value = CERTIFICATES_POLICY_WALAINSTRUMENTATION, withinPath = CERTIFICATES_WITHIN_PATH)
	void test_loadTrustedCertificatesMavenWalaInstrumentation() {
		assertDoesNotThrow(() -> TrustedCertificatesMain.loadTrustedCertificates());
	}
	// </editor-fold>

	// <editor-fold desc="formatWithLocaleAndCharsetData">
	/**
	 * Locale and charset data work without a policy entry (item 1.14).
	 */
	@PublicTest
	@Policy(value = POLICY_ARCHUNITASPECTJ, withinPath = WITHIN_PATH)
	void test_formatWithLocaleAndCharsetDataMavenArchunitAspectJ() {
		assertDoesNotThrow(() -> BaselineStandardAllowMain.formatWithLocaleAndCharsetData());
	}

	/**
	 * Locale and charset data work without a policy entry (item 1.14).
	 */
	@PublicTest
	@Policy(value = POLICY_ARCHUNITINSTRUMENTATION, withinPath = WITHIN_PATH)
	void test_formatWithLocaleAndCharsetDataMavenArchunitInstrumentation() {
		assertDoesNotThrow(() -> BaselineStandardAllowMain.formatWithLocaleAndCharsetData());
	}

	/**
	 * Locale and charset data work without a policy entry (item 1.14).
	 */
	@PublicTest
	@Policy(value = POLICY_WALAASPECTJ, withinPath = WITHIN_PATH)
	void test_formatWithLocaleAndCharsetDataMavenWalaAspectJ() {
		assertDoesNotThrow(() -> BaselineStandardAllowMain.formatWithLocaleAndCharsetData());
	}

	/**
	 * Locale and charset data work without a policy entry (item 1.14).
	 */
	@PublicTest
	@Policy(value = POLICY_WALAINSTRUMENTATION, withinPath = WITHIN_PATH)
	void test_formatWithLocaleAndCharsetDataMavenWalaInstrumentation() {
		assertDoesNotThrow(() -> BaselineStandardAllowMain.formatWithLocaleAndCharsetData());
	}
	// </editor-fold>

	/**
	 * Deletes the temporary files the tests created.
	 *
	 * @throws IOException if one cannot be deleted
	 */
	@AfterAll
	static void deleteCreatedFiles() throws IOException {
		for (Path created : CREATED_FILES) {
			Files.deleteIfExists(created);
		}
	}

	/**
	 * Remembers a created temporary file for deletion.
	 *
	 * @param created the file
	 * @return the same file
	 */
	private static Path remember(Path created) {
		CREATED_FILES.add(created);
		return created;
	}
}
