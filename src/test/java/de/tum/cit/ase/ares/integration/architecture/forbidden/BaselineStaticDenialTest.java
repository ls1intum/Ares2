package de.tum.cit.ase.ares.integration.architecture.forbidden;

import java.io.IOException;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import de.tum.cit.ase.ares.api.Policy;
import de.tum.cit.ase.ares.api.jupiter.PublicTest;
import de.tum.cit.ase.ares.integration.aop.forbidden.subject.baseline.staticCleaner.CleanerCreateMain;
import de.tum.cit.ase.ares.integration.aop.forbidden.subject.baseline.staticFileWithDirectory.FileCreateTempFileWithDirectoryMain;
import de.tum.cit.ase.ares.integration.aop.forbidden.subject.baseline.staticFilesWithDirectory.FilesCreateTempFileWithDirectoryMain;
import de.tum.cit.ase.ares.integration.aop.forbidden.subject.baseline.staticManagementClassPath.ManagementClassPathMain;
import de.tum.cit.ase.ares.integration.aop.forbidden.subject.baseline.staticTempDirectory.FilesCreateTempDirectoryMain;

/**
 * Checks that the static analysis denies what the secure baseline does not
 * cover: temp files in a named directory or a temp directory when a policy
 * grants no file access, reading the class path through java.lang.management,
 * and creating a java.lang.ref.Cleaner, which starts a thread. Each case runs
 * as a disabled inner test, as the other architecture tests do. Under AspectJ
 * the runtime check allows all of them, so there only the static analysis can
 * make these inner tests fail.
 */
class BaselineStaticDenialTest extends SystemAccessTest {

	/**
	 * Folder of the student code calling {@code File.createTempFile} with a
	 * directory.
	 */
	private static final String FILE_WITH_DIRECTORY_PATH = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/baseline/staticFileWithDirectory";

	/**
	 * Folder of the student code calling {@code Files.createTempFile} with a
	 * directory.
	 */
	private static final String FILES_WITH_DIRECTORY_PATH = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/baseline/staticFilesWithDirectory";

	/**
	 * Folder of the student code calling {@code Files.createTempDirectory}.
	 */
	private static final String TEMP_DIRECTORY_PATH = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/baseline/staticTempDirectory";

	/**
	 * Folder of the student code creating a {@code java.lang.ref.Cleaner}.
	 */
	private static final String CLEANER_PATH = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/baseline/staticCleaner";

	/**
	 * Folder of the student code reading the class path through
	 * {@code java.lang.management}.
	 */
	private static final String MANAGEMENT_CLASS_PATH_PATH = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/baseline/staticManagementClassPath";

	/**
	 * Policy without file access for MavenArchunitAspectJ.
	 */
	private static final String BASELINE_POLICY_ARCHUNITASPECTJ = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/aspectj/PolicyBaselineStandardAllow.yaml";

	/**
	 * Policy without file access for MavenArchunitInstrumentation.
	 */
	private static final String BASELINE_POLICY_ARCHUNITINSTRUMENTATION = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/instrumentation/PolicyBaselineStandardAllow.yaml";

	/**
	 * Policy without file access for MavenWalaAspectJ.
	 */
	private static final String BASELINE_POLICY_WALAASPECTJ = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/aspectj/PolicyBaselineStandardAllow.yaml";

	/**
	 * Policy without file access for MavenWalaInstrumentation.
	 */
	private static final String BASELINE_POLICY_WALAINSTRUMENTATION = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/instrumentation/PolicyBaselineStandardAllow.yaml";

	// <editor-fold desc="createTempFileWithFileInDirectory">
	/**
	 * {@code File.createTempFile} with a named directory is denied by the static
	 * analysis in MavenArchunitAspectJ.
	 */
	@Test
	void test_createTempFileWithFileInDirectoryMavenArchunitAspectJ_test() {
		executeTestAndExpectSecurityException(BaselineStaticDenialTest.class,
				"test_createTempFileWithFileInDirectoryMavenArchunitAspectJ");
	}

	/**
	 * {@code File.createTempFile} with a named directory is denied by the static
	 * analysis in MavenArchunitInstrumentation.
	 */
	@Test
	void test_createTempFileWithFileInDirectoryMavenArchunitInstrumentation_test() {
		executeTestAndExpectSecurityException(BaselineStaticDenialTest.class,
				"test_createTempFileWithFileInDirectoryMavenArchunitInstrumentation");
	}

	/**
	 * {@code File.createTempFile} with a named directory is denied by the static
	 * analysis in MavenWalaAspectJ.
	 */
	@Test
	void test_createTempFileWithFileInDirectoryMavenWalaAspectJ_test() {
		executeTestAndExpectSecurityException(BaselineStaticDenialTest.class,
				"test_createTempFileWithFileInDirectoryMavenWalaAspectJ");
	}

	/**
	 * {@code File.createTempFile} with a named directory is denied by the static
	 * analysis in MavenWalaInstrumentation.
	 */
	@Test
	void test_createTempFileWithFileInDirectoryMavenWalaInstrumentation_test() {
		executeTestAndExpectSecurityException(BaselineStaticDenialTest.class,
				"test_createTempFileWithFileInDirectoryMavenWalaInstrumentation");
	}
	// </editor-fold>

	// <editor-fold desc="createTempFileWithFilesInDirectory">
	/**
	 * {@code Files.createTempFile} with a named directory is denied by the static
	 * analysis in MavenArchunitAspectJ.
	 */
	@Test
	void test_createTempFileWithFilesInDirectoryMavenArchunitAspectJ_test() {
		executeTestAndExpectSecurityException(BaselineStaticDenialTest.class,
				"test_createTempFileWithFilesInDirectoryMavenArchunitAspectJ");
	}

	/**
	 * {@code Files.createTempFile} with a named directory is denied by the static
	 * analysis in MavenArchunitInstrumentation.
	 */
	@Test
	void test_createTempFileWithFilesInDirectoryMavenArchunitInstrumentation_test() {
		executeTestAndExpectSecurityException(BaselineStaticDenialTest.class,
				"test_createTempFileWithFilesInDirectoryMavenArchunitInstrumentation");
	}

	/**
	 * {@code Files.createTempFile} with a named directory is denied by the static
	 * analysis in MavenWalaAspectJ.
	 */
	@Test
	void test_createTempFileWithFilesInDirectoryMavenWalaAspectJ_test() {
		executeTestAndExpectSecurityException(BaselineStaticDenialTest.class,
				"test_createTempFileWithFilesInDirectoryMavenWalaAspectJ");
	}

	/**
	 * {@code Files.createTempFile} with a named directory is denied by the static
	 * analysis in MavenWalaInstrumentation.
	 */
	@Test
	void test_createTempFileWithFilesInDirectoryMavenWalaInstrumentation_test() {
		executeTestAndExpectSecurityException(BaselineStaticDenialTest.class,
				"test_createTempFileWithFilesInDirectoryMavenWalaInstrumentation");
	}
	// </editor-fold>

	// <editor-fold desc="createTempDirectory">
	/**
	 * {@code Files.createTempDirectory} is denied by the static analysis in
	 * MavenArchunitAspectJ.
	 */
	@Test
	void test_createTempDirectoryMavenArchunitAspectJ_test() {
		executeTestAndExpectSecurityException(BaselineStaticDenialTest.class,
				"test_createTempDirectoryMavenArchunitAspectJ");
	}

	/**
	 * {@code Files.createTempDirectory} is denied by the static analysis in
	 * MavenArchunitInstrumentation.
	 */
	@Test
	void test_createTempDirectoryMavenArchunitInstrumentation_test() {
		executeTestAndExpectSecurityException(BaselineStaticDenialTest.class,
				"test_createTempDirectoryMavenArchunitInstrumentation");
	}

	/**
	 * {@code Files.createTempDirectory} is denied by the static analysis in
	 * MavenWalaAspectJ.
	 */
	@Test
	void test_createTempDirectoryMavenWalaAspectJ_test() {
		executeTestAndExpectSecurityException(BaselineStaticDenialTest.class,
				"test_createTempDirectoryMavenWalaAspectJ");
	}

	/**
	 * {@code Files.createTempDirectory} is denied by the static analysis in
	 * MavenWalaInstrumentation.
	 */
	@Test
	void test_createTempDirectoryMavenWalaInstrumentation_test() {
		executeTestAndExpectSecurityException(BaselineStaticDenialTest.class,
				"test_createTempDirectoryMavenWalaInstrumentation");
	}
	// </editor-fold>

	// <editor-fold desc="managementClassPath">
	/**
	 * Reading the class path through {@code java.lang.management} is denied by the
	 * static analysis in MavenArchunitAspectJ.
	 */
	@Test
	void test_managementClassPathMavenArchunitAspectJ_test() {
		executeTestAndExpectSecurityException(BaselineStaticDenialTest.class,
				"test_managementClassPathMavenArchunitAspectJ", "environment.access",
				"java.lang.management.ManagementFactory");
	}

	/**
	 * Reading the class path through {@code java.lang.management} is denied by the
	 * static analysis in MavenArchunitInstrumentation.
	 */
	@Test
	void test_managementClassPathMavenArchunitInstrumentation_test() {
		executeTestAndExpectSecurityException(BaselineStaticDenialTest.class,
				"test_managementClassPathMavenArchunitInstrumentation", "environment.access",
				"java.lang.management.ManagementFactory");
	}

	/**
	 * Reading the class path through {@code java.lang.management} is denied by the
	 * static analysis in MavenWalaAspectJ.
	 */
	@Test
	void test_managementClassPathMavenWalaAspectJ_test() {
		executeTestAndExpectSecurityException(BaselineStaticDenialTest.class,
				"test_managementClassPathMavenWalaAspectJ", "environment.access",
				"java.lang.management.ManagementFactory");
	}

	/**
	 * Reading the class path through {@code java.lang.management} is denied by the
	 * static analysis in MavenWalaInstrumentation.
	 */
	@Test
	void test_managementClassPathMavenWalaInstrumentation_test() {
		executeTestAndExpectSecurityException(BaselineStaticDenialTest.class,
				"test_managementClassPathMavenWalaInstrumentation", "environment.access",
				"java.lang.management.ManagementFactory");
	}
	// </editor-fold>

	// <editor-fold desc="cleanerCreate">
	/**
	 * Creating a {@code java.lang.ref.Cleaner}, which starts a thread, is denied by
	 * the static analysis in MavenArchunitAspectJ.
	 */
	@Test
	void test_cleanerCreateMavenArchunitAspectJ_test() {
		executeTestAndExpectSecurityException(BaselineStaticDenialTest.class, "test_cleanerCreateMavenArchunitAspectJ",
				"manipulate.threads", "java.lang.ref.Cleaner.create");
	}

	/**
	 * Creating a {@code java.lang.ref.Cleaner}, which starts a thread, is denied by
	 * the static analysis in MavenArchunitInstrumentation.
	 */
	@Test
	void test_cleanerCreateMavenArchunitInstrumentation_test() {
		executeTestAndExpectSecurityException(BaselineStaticDenialTest.class,
				"test_cleanerCreateMavenArchunitInstrumentation", "manipulate.threads", "java.lang.ref.Cleaner.create");
	}

	/**
	 * Creating a {@code java.lang.ref.Cleaner}, which starts a thread, is denied by
	 * the static analysis in MavenWalaAspectJ. WALA names the student method rather
	 * than the JDK call in this message.
	 */
	@Test
	void test_cleanerCreateMavenWalaAspectJ_test() {
		executeTestAndExpectSecurityException(BaselineStaticDenialTest.class, "test_cleanerCreateMavenWalaAspectJ",
				"manipulate.threads", "CleanerCreateMain.create()");
	}

	/**
	 * Creating a {@code java.lang.ref.Cleaner}, which starts a thread, is denied by
	 * the static analysis in MavenWalaInstrumentation. WALA names the student
	 * method rather than the JDK call in this message.
	 */
	@Test
	void test_cleanerCreateMavenWalaInstrumentation_test() {
		executeTestAndExpectSecurityException(BaselineStaticDenialTest.class,
				"test_cleanerCreateMavenWalaInstrumentation", "manipulate.threads", "CleanerCreateMain.create()");
	}
	// </editor-fold>

	// ===================== DISABLED TESTS BELOW =====================

	/**
	 * Calls {@code File.createTempFile} with a named directory under a policy
	 * without file access in MavenArchunitAspectJ.
	 */
	@Disabled(SUBJECT_PROBE_REASON)
	@PublicTest
	@Policy(value = BASELINE_POLICY_ARCHUNITASPECTJ, withinPath = FILE_WITH_DIRECTORY_PATH)
	void test_createTempFileWithFileInDirectoryMavenArchunitAspectJ() throws IOException {
		FileCreateTempFileWithDirectoryMain.createTempFile();
	}

	/**
	 * Calls {@code File.createTempFile} with a named directory under a policy
	 * without file access in MavenArchunitInstrumentation.
	 */
	@Disabled(SUBJECT_PROBE_REASON)
	@PublicTest
	@Policy(value = BASELINE_POLICY_ARCHUNITINSTRUMENTATION, withinPath = FILE_WITH_DIRECTORY_PATH)
	void test_createTempFileWithFileInDirectoryMavenArchunitInstrumentation() throws IOException {
		FileCreateTempFileWithDirectoryMain.createTempFile();
	}

	/**
	 * Calls {@code File.createTempFile} with a named directory under a policy
	 * without file access in MavenWalaAspectJ.
	 */
	@Disabled(SUBJECT_PROBE_REASON)
	@PublicTest
	@Policy(value = BASELINE_POLICY_WALAASPECTJ, withinPath = FILE_WITH_DIRECTORY_PATH)
	void test_createTempFileWithFileInDirectoryMavenWalaAspectJ() throws IOException {
		FileCreateTempFileWithDirectoryMain.createTempFile();
	}

	/**
	 * Calls {@code File.createTempFile} with a named directory under a policy
	 * without file access in MavenWalaInstrumentation.
	 */
	@Disabled(SUBJECT_PROBE_REASON)
	@PublicTest
	@Policy(value = BASELINE_POLICY_WALAINSTRUMENTATION, withinPath = FILE_WITH_DIRECTORY_PATH)
	void test_createTempFileWithFileInDirectoryMavenWalaInstrumentation() throws IOException {
		FileCreateTempFileWithDirectoryMain.createTempFile();
	}

	/**
	 * Calls {@code Files.createTempFile} with a named directory under a policy
	 * without file access in MavenArchunitAspectJ.
	 */
	@Disabled(SUBJECT_PROBE_REASON)
	@PublicTest
	@Policy(value = BASELINE_POLICY_ARCHUNITASPECTJ, withinPath = FILES_WITH_DIRECTORY_PATH)
	void test_createTempFileWithFilesInDirectoryMavenArchunitAspectJ() throws IOException {
		FilesCreateTempFileWithDirectoryMain.createTempFile();
	}

	/**
	 * Calls {@code Files.createTempFile} with a named directory under a policy
	 * without file access in MavenArchunitInstrumentation.
	 */
	@Disabled(SUBJECT_PROBE_REASON)
	@PublicTest
	@Policy(value = BASELINE_POLICY_ARCHUNITINSTRUMENTATION, withinPath = FILES_WITH_DIRECTORY_PATH)
	void test_createTempFileWithFilesInDirectoryMavenArchunitInstrumentation() throws IOException {
		FilesCreateTempFileWithDirectoryMain.createTempFile();
	}

	/**
	 * Calls {@code Files.createTempFile} with a named directory under a policy
	 * without file access in MavenWalaAspectJ.
	 */
	@Disabled(SUBJECT_PROBE_REASON)
	@PublicTest
	@Policy(value = BASELINE_POLICY_WALAASPECTJ, withinPath = FILES_WITH_DIRECTORY_PATH)
	void test_createTempFileWithFilesInDirectoryMavenWalaAspectJ() throws IOException {
		FilesCreateTempFileWithDirectoryMain.createTempFile();
	}

	/**
	 * Calls {@code Files.createTempFile} with a named directory under a policy
	 * without file access in MavenWalaInstrumentation.
	 */
	@Disabled(SUBJECT_PROBE_REASON)
	@PublicTest
	@Policy(value = BASELINE_POLICY_WALAINSTRUMENTATION, withinPath = FILES_WITH_DIRECTORY_PATH)
	void test_createTempFileWithFilesInDirectoryMavenWalaInstrumentation() throws IOException {
		FilesCreateTempFileWithDirectoryMain.createTempFile();
	}

	/**
	 * Calls {@code Files.createTempDirectory} under a policy without file access in
	 * MavenArchunitAspectJ.
	 */
	@Disabled(SUBJECT_PROBE_REASON)
	@PublicTest
	@Policy(value = BASELINE_POLICY_ARCHUNITASPECTJ, withinPath = TEMP_DIRECTORY_PATH)
	void test_createTempDirectoryMavenArchunitAspectJ() throws IOException {
		FilesCreateTempDirectoryMain.createTempDirectory();
	}

	/**
	 * Calls {@code Files.createTempDirectory} under a policy without file access in
	 * MavenArchunitInstrumentation.
	 */
	@Disabled(SUBJECT_PROBE_REASON)
	@PublicTest
	@Policy(value = BASELINE_POLICY_ARCHUNITINSTRUMENTATION, withinPath = TEMP_DIRECTORY_PATH)
	void test_createTempDirectoryMavenArchunitInstrumentation() throws IOException {
		FilesCreateTempDirectoryMain.createTempDirectory();
	}

	/**
	 * Calls {@code Files.createTempDirectory} under a policy without file access in
	 * MavenWalaAspectJ.
	 */
	@Disabled(SUBJECT_PROBE_REASON)
	@PublicTest
	@Policy(value = BASELINE_POLICY_WALAASPECTJ, withinPath = TEMP_DIRECTORY_PATH)
	void test_createTempDirectoryMavenWalaAspectJ() throws IOException {
		FilesCreateTempDirectoryMain.createTempDirectory();
	}

	/**
	 * Calls {@code Files.createTempDirectory} under a policy without file access in
	 * MavenWalaInstrumentation.
	 */
	@Disabled(SUBJECT_PROBE_REASON)
	@PublicTest
	@Policy(value = BASELINE_POLICY_WALAINSTRUMENTATION, withinPath = TEMP_DIRECTORY_PATH)
	void test_createTempDirectoryMavenWalaInstrumentation() throws IOException {
		FilesCreateTempDirectoryMain.createTempDirectory();
	}

	/**
	 * Reads the class path through {@code java.lang.management} under a policy
	 * without environment access in MavenArchunitAspectJ.
	 */
	@Disabled(SUBJECT_PROBE_REASON)
	@PublicTest
	@Policy(value = BASELINE_POLICY_ARCHUNITASPECTJ, withinPath = MANAGEMENT_CLASS_PATH_PATH)
	void test_managementClassPathMavenArchunitAspectJ() {
		ManagementClassPathMain.classPath();
	}

	/**
	 * Reads the class path through {@code java.lang.management} under a policy
	 * without environment access in MavenArchunitInstrumentation.
	 */
	@Disabled(SUBJECT_PROBE_REASON)
	@PublicTest
	@Policy(value = BASELINE_POLICY_ARCHUNITINSTRUMENTATION, withinPath = MANAGEMENT_CLASS_PATH_PATH)
	void test_managementClassPathMavenArchunitInstrumentation() {
		ManagementClassPathMain.classPath();
	}

	/**
	 * Reads the class path through {@code java.lang.management} under a policy
	 * without environment access in MavenWalaAspectJ.
	 */
	@Disabled(SUBJECT_PROBE_REASON)
	@PublicTest
	@Policy(value = BASELINE_POLICY_WALAASPECTJ, withinPath = MANAGEMENT_CLASS_PATH_PATH)
	void test_managementClassPathMavenWalaAspectJ() {
		ManagementClassPathMain.classPath();
	}

	/**
	 * Reads the class path through {@code java.lang.management} under a policy
	 * without environment access in MavenWalaInstrumentation.
	 */
	@Disabled(SUBJECT_PROBE_REASON)
	@PublicTest
	@Policy(value = BASELINE_POLICY_WALAINSTRUMENTATION, withinPath = MANAGEMENT_CLASS_PATH_PATH)
	void test_managementClassPathMavenWalaInstrumentation() {
		ManagementClassPathMain.classPath();
	}

	/**
	 * Creates a {@code java.lang.ref.Cleaner} under a policy without thread
	 * creation in MavenArchunitAspectJ.
	 */
	@Disabled(SUBJECT_PROBE_REASON)
	@PublicTest
	@Policy(value = BASELINE_POLICY_ARCHUNITASPECTJ, withinPath = CLEANER_PATH)
	void test_cleanerCreateMavenArchunitAspectJ() {
		CleanerCreateMain.create();
	}

	/**
	 * Creates a {@code java.lang.ref.Cleaner} under a policy without thread
	 * creation in MavenArchunitInstrumentation.
	 */
	@Disabled(SUBJECT_PROBE_REASON)
	@PublicTest
	@Policy(value = BASELINE_POLICY_ARCHUNITINSTRUMENTATION, withinPath = CLEANER_PATH)
	void test_cleanerCreateMavenArchunitInstrumentation() {
		CleanerCreateMain.create();
	}

	/**
	 * Creates a {@code java.lang.ref.Cleaner} under a policy without thread
	 * creation in MavenWalaAspectJ.
	 */
	@Disabled(SUBJECT_PROBE_REASON)
	@PublicTest
	@Policy(value = BASELINE_POLICY_WALAASPECTJ, withinPath = CLEANER_PATH)
	void test_cleanerCreateMavenWalaAspectJ() {
		CleanerCreateMain.create();
	}

	/**
	 * Creates a {@code java.lang.ref.Cleaner} under a policy without thread
	 * creation in MavenWalaInstrumentation.
	 */
	@Disabled(SUBJECT_PROBE_REASON)
	@PublicTest
	@Policy(value = BASELINE_POLICY_WALAINSTRUMENTATION, withinPath = CLEANER_PATH)
	void test_cleanerCreateMavenWalaInstrumentation() {
		CleanerCreateMain.create();
	}
}
