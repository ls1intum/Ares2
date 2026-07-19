package de.tum.cit.ase.ares.integration.architecture.forbidden;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import de.tum.cit.ase.ares.api.Policy;
import de.tum.cit.ase.ares.api.jupiter.PublicTest;
import de.tum.cit.ase.ares.integration.aop.forbidden.subject.fileSystem.delete.fileDelete.FileDeleteMain;
import de.tum.cit.ase.ares.integration.aop.forbidden.subject.fileSystem.delete.filesDelete.FilesDeleteMain;
import de.tum.cit.ase.ares.integration.aop.forbidden.subject.fileSystem.delete.thirdPartyPackage.DeleteThirdPartyPackageMain;
import de.tum.cit.ase.ares.integration.testuser.subject.architectureTests.thirdpartypackage.ThirdPartyPackagePenguin;

class FileSystemAccessDeleteTest extends SystemAccessTest {

	private static final String FILE_DELETE_PATH = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/delete/fileDelete";
	private static final String FILES_DELETE_PATH = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/delete/filesDelete";
	private static final String THIRD_PARTY_PACKAGE_PATH = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/delete/thirdPartyPackage";

	private static final Path NOT_TRUSTED_DELETE_PATH = Paths.get("src", "test", "java", "de", "tum", "cit", "ase",
			"ares", "integration", "aop", "forbidden", "subject", "fileSystem", "delete", "nottrusteddir",
			"nottrusted.txt");

	// <editor-fold desc="accessFileSystemViaFileDelete()">
	@Test
	void test_accessFileSystemViaFileDeleteMavenArchunitAspectJ_test() {
		executeTestAndExpectSecurityException(FileSystemAccessDeleteTest.class,
				"test_accessFileSystemViaFileDeleteMavenArchunitAspectJ");
	}

	@Disabled(SUBJECT_PROBE_REASON)
	@PublicTest
	@Policy(value = ARCHUNIT_ASPECTJ_POLICY, withinPath = FILE_DELETE_PATH)
	void test_accessFileSystemViaFileDeleteMavenArchunitAspectJ() {
		assertAresSecurityExceptionRead(FileDeleteMain::accessFileSystemViaFileDelete, FileDeleteMain.class);
	}

	@Test
	void test_accessFileSystemViaFileDeleteMavenArchunitInstrumentation_test() {
		executeTestAndExpectSecurityException(FileSystemAccessDeleteTest.class,
				"test_accessFileSystemViaFileDeleteMavenArchunitInstrumentation");
	}

	@Disabled(SUBJECT_PROBE_REASON)
	@PublicTest
	@Policy(value = ARCHUNIT_INSTRUMENTATION_POLICY, withinPath = FILE_DELETE_PATH)
	void test_accessFileSystemViaFileDeleteMavenArchunitInstrumentation() {
		assertAresSecurityExceptionDelete(FileDeleteMain::accessFileSystemViaFileDelete, FileDeleteMain.class);
	}

	@Test
	void test_accessFileSystemViaFileDeleteMavenWalaAspectJ_test() {
		executeTestAndExpectSecurityException(FileSystemAccessDeleteTest.class,
				"test_accessFileSystemViaFileDeleteMavenWalaAspectJ");
	}

	@Disabled(SUBJECT_PROBE_REASON)
	@PublicTest
	@Policy(value = WALA_ASPECTJ_POLICY, withinPath = FILE_DELETE_PATH)
	void test_accessFileSystemViaFileDeleteMavenWalaAspectJ() {
		assertAresSecurityExceptionDelete(FileDeleteMain::accessFileSystemViaFileDelete, FileDeleteMain.class,
				NOT_TRUSTED_DELETE_PATH);
	}

	@Test
	void test_accessFileSystemViaFileDeleteMavenWalaInstrumentation_test() {
		executeTestAndExpectSecurityException(FileSystemAccessDeleteTest.class,
				"test_accessFileSystemViaFileDeleteMavenWalaInstrumentation");
	}

	@Disabled(SUBJECT_PROBE_REASON)
	@PublicTest
	@Policy(value = WALA_INSTRUMENTATION_POLICY, withinPath = FILE_DELETE_PATH)
	void test_accessFileSystemViaFileDeleteMavenWalaInstrumentation() {
		assertAresSecurityExceptionDelete(FileDeleteMain::accessFileSystemViaFileDelete, FileDeleteMain.class,
				NOT_TRUSTED_DELETE_PATH);
	}
	// </editor-fold>

	// <editor-fold desc="accessFileSystemViaFileDeleteOnExit()">
	@Test
	void test_accessFileSystemViaFileDeleteOnExitMavenArchunitAspectJ_test() {
		executeTestAndExpectSecurityException(FileSystemAccessDeleteTest.class,
				"test_accessFileSystemViaFileDeleteOnExitMavenArchunitAspectJ");
	}

	@Disabled(SUBJECT_PROBE_REASON)
	@PublicTest
	@Policy(value = ARCHUNIT_ASPECTJ_POLICY, withinPath = FILE_DELETE_PATH)
	void test_accessFileSystemViaFileDeleteOnExitMavenArchunitAspectJ() {
		// Read, as File.new has the parameter
		assertAresSecurityExceptionRead(FileDeleteMain::accessFileSystemViaFileDeleteOnExit, FileDeleteMain.class);
	}

	@Test
	void test_accessFileSystemViaFileDeleteOnExitMavenArchunitInstrumentation_test() {
		executeTestAndExpectSecurityException(FileSystemAccessDeleteTest.class,
				"test_accessFileSystemViaFileDeleteOnExitMavenArchunitInstrumentation");
	}

	@Disabled(SUBJECT_PROBE_REASON)
	@PublicTest
	@Policy(value = ARCHUNIT_INSTRUMENTATION_POLICY, withinPath = FILE_DELETE_PATH)
	void test_accessFileSystemViaFileDeleteOnExitMavenArchunitInstrumentation() {
		assertAresSecurityExceptionDelete(FileDeleteMain::accessFileSystemViaFileDeleteOnExit, FileDeleteMain.class);
	}

	@Test
	void test_accessFileSystemViaFileDeleteOnExitMavenWalaAspectJ_test() {
		executeTestAndExpectSecurityException(FileSystemAccessDeleteTest.class,
				"test_accessFileSystemViaFileDeleteOnExitMavenWalaAspectJ");
	}

	@Disabled(SUBJECT_PROBE_REASON)
	@PublicTest
	@Policy(value = WALA_ASPECTJ_POLICY, withinPath = FILE_DELETE_PATH)
	void test_accessFileSystemViaFileDeleteOnExitMavenWalaAspectJ() {
		assertAresSecurityExceptionDelete(FileDeleteMain::accessFileSystemViaFileDeleteOnExit, FileDeleteMain.class,
				NOT_TRUSTED_DELETE_PATH);
	}

	@Test
	void test_accessFileSystemViaFileDeleteOnExitMavenWalaInstrumentation_test() {
		executeTestAndExpectSecurityException(FileSystemAccessDeleteTest.class,
				"test_accessFileSystemViaFileDeleteOnExitMavenWalaInstrumentation");
	}

	@Disabled(SUBJECT_PROBE_REASON)
	@PublicTest
	@Policy(value = WALA_INSTRUMENTATION_POLICY, withinPath = FILE_DELETE_PATH)
	void test_accessFileSystemViaFileDeleteOnExitMavenWalaInstrumentation() {
		assertAresSecurityExceptionDelete(FileDeleteMain::accessFileSystemViaFileDeleteOnExit, FileDeleteMain.class,
				NOT_TRUSTED_DELETE_PATH);
	}
	// </editor-fold>

	// <editor-fold desc="accessFileSystemViaFilesDelete()">
	@Test
	void test_accessFileSystemViaFilesDeleteMavenArchunitAspectJ_test() {
		executeTestAndExpectSecurityException(FileSystemAccessDeleteTest.class,
				"test_accessFileSystemViaFilesDeleteMavenArchunitAspectJ");
	}

	@Disabled(SUBJECT_PROBE_REASON)
	@PublicTest
	@Policy(value = ARCHUNIT_ASPECTJ_POLICY, withinPath = FILES_DELETE_PATH)
	void test_accessFileSystemViaFilesDeleteMavenArchunitAspectJ() {
		assertAresSecurityExceptionDelete(FilesDeleteMain::accessFileSystemViaFilesDelete, FilesDeleteMain.class);
	}

	@Test
	void test_accessFileSystemViaFilesDeleteMavenArchunitInstrumentation_test() {
		executeTestAndExpectSecurityException(FileSystemAccessDeleteTest.class,
				"test_accessFileSystemViaFilesDeleteMavenArchunitInstrumentation");
	}

	@Disabled(SUBJECT_PROBE_REASON)
	@PublicTest
	@Policy(value = ARCHUNIT_INSTRUMENTATION_POLICY, withinPath = FILES_DELETE_PATH)
	void test_accessFileSystemViaFilesDeleteMavenArchunitInstrumentation() {
		assertAresSecurityExceptionDelete(FilesDeleteMain::accessFileSystemViaFilesDelete, FilesDeleteMain.class);
	}

	@Test
	void test_accessFileSystemViaFilesDeleteMavenWalaAspectJ_test() {
		executeTestAndExpectSecurityException(FileSystemAccessDeleteTest.class,
				"test_accessFileSystemViaFilesDeleteMavenWalaAspectJ");
	}

	@Disabled(SUBJECT_PROBE_REASON)
	@PublicTest
	@Policy(value = WALA_ASPECTJ_POLICY, withinPath = FILES_DELETE_PATH)
	void test_accessFileSystemViaFilesDeleteMavenWalaAspectJ() {
		assertAresSecurityExceptionDelete(FilesDeleteMain::accessFileSystemViaFilesDelete, FilesDeleteMain.class,
				NOT_TRUSTED_DELETE_PATH);
	}

	@Test
	void test_accessFileSystemViaFilesDeleteMavenWalaInstrumentation_test() {
		executeTestAndExpectSecurityException(FileSystemAccessDeleteTest.class,
				"test_accessFileSystemViaFilesDeleteMavenWalaInstrumentation");
	}

	@Disabled(SUBJECT_PROBE_REASON)
	@PublicTest
	@Policy(value = WALA_INSTRUMENTATION_POLICY, withinPath = FILES_DELETE_PATH)
	void test_accessFileSystemViaFilesDeleteMavenWalaInstrumentation() {
		assertAresSecurityExceptionDelete(FilesDeleteMain::accessFileSystemViaFilesDelete, FilesDeleteMain.class,
				NOT_TRUSTED_DELETE_PATH);
	}
	// </editor-fold>

	// <editor-fold desc="accessFileSystemViaFilesDeleteIfExists()">
	@Test
	void test_accessFileSystemViaFilesDeleteIfExistsMavenArchunitAspectJ_test() {
		executeTestAndExpectSecurityException(FileSystemAccessDeleteTest.class,
				"test_accessFileSystemViaFilesDeleteIfExistsMavenArchunitAspectJ");
	}

	@Disabled(SUBJECT_PROBE_REASON)
	@PublicTest
	@Policy(value = ARCHUNIT_ASPECTJ_POLICY, withinPath = FILES_DELETE_PATH)
	void test_accessFileSystemViaFilesDeleteIfExistsMavenArchunitAspectJ() {
		assertAresSecurityExceptionDelete(FilesDeleteMain::accessFileSystemViaFilesDeleteIfExists,
				FilesDeleteMain.class);
	}

	@Test
	void test_accessFileSystemViaFilesDeleteIfExistsMavenArchunitInstrumentation_test() {
		executeTestAndExpectSecurityException(FileSystemAccessDeleteTest.class,
				"test_accessFileSystemViaFilesDeleteIfExistsMavenArchunitInstrumentation");
	}

	@Disabled(SUBJECT_PROBE_REASON)
	@PublicTest
	@Policy(value = ARCHUNIT_INSTRUMENTATION_POLICY, withinPath = FILES_DELETE_PATH)
	void test_accessFileSystemViaFilesDeleteIfExistsMavenArchunitInstrumentation() {
		assertAresSecurityExceptionDelete(FilesDeleteMain::accessFileSystemViaFilesDeleteIfExists,
				FilesDeleteMain.class);
	}

	@Test
	void test_accessFileSystemViaFilesDeleteIfExistsMavenWalaAspectJ_test() {
		executeTestAndExpectSecurityException(FileSystemAccessDeleteTest.class,
				"test_accessFileSystemViaFilesDeleteIfExistsMavenWalaAspectJ");
	}

	@Disabled(SUBJECT_PROBE_REASON)
	@PublicTest
	@Policy(value = WALA_ASPECTJ_POLICY, withinPath = FILES_DELETE_PATH)
	void test_accessFileSystemViaFilesDeleteIfExistsMavenWalaAspectJ() {
		assertAresSecurityExceptionDelete(FilesDeleteMain::accessFileSystemViaFilesDeleteIfExists,
				FilesDeleteMain.class, NOT_TRUSTED_DELETE_PATH);
	}

	@Test
	void test_accessFileSystemViaFilesDeleteIfExistsMavenWalaInstrumentation_test() {
		executeTestAndExpectSecurityException(FileSystemAccessDeleteTest.class,
				"test_accessFileSystemViaFilesDeleteIfExistsMavenWalaInstrumentation");
	}

	@Disabled(SUBJECT_PROBE_REASON)
	@PublicTest
	@Policy(value = WALA_INSTRUMENTATION_POLICY, withinPath = FILES_DELETE_PATH)
	void test_accessFileSystemViaFilesDeleteIfExistsMavenWalaInstrumentation() {
		assertAresSecurityExceptionDelete(FilesDeleteMain::accessFileSystemViaFilesDeleteIfExists,
				FilesDeleteMain.class, NOT_TRUSTED_DELETE_PATH);
	}
	// </editor-fold>

	// <editor-fold desc="accessFileSystemViaThirdPartyPackage">

	@Test
	void test_accessFileSystemViaThirdPartyPackageMavenArchunitAspectJ_test() {
		executeTestAndExpectSecurityException(FileSystemAccessDeleteTest.class,
				"test_accessFileSystemViaThirdPartyPackageMavenArchunitAspectJ");
	}

	@Disabled(SUBJECT_PROBE_REASON)
	@PublicTest
	@Policy(value = ARCHUNIT_ASPECTJ_POLICY, withinPath = THIRD_PARTY_PACKAGE_PATH)
	void test_accessFileSystemViaThirdPartyPackageMavenArchunitAspectJ() {
		assertAresSecurityExceptionDelete(DeleteThirdPartyPackageMain::accessFileSystemViaThirdPartyPackage,
				ThirdPartyPackagePenguin.class);
	}

	@Test
	void test_accessFileSystemViaThirdPartyPackageMavenArchunitInstrumentation_test() {
		executeTestAndExpectSecurityException(FileSystemAccessDeleteTest.class,
				"test_accessFileSystemViaThirdPartyPackageMavenArchunitInstrumentation");
	}

	@Disabled(SUBJECT_PROBE_REASON)
	@PublicTest
	@Policy(value = ARCHUNIT_INSTRUMENTATION_POLICY, withinPath = THIRD_PARTY_PACKAGE_PATH)
	void test_accessFileSystemViaThirdPartyPackageMavenArchunitInstrumentation() {
		assertAresSecurityExceptionDelete(DeleteThirdPartyPackageMain::accessFileSystemViaThirdPartyPackage,
				ThirdPartyPackagePenguin.class);
	}

	@Test
	void test_accessFileSystemViaThirdPartyPackageMavenWalaAspectJ_test() {
		executeTestAndExpectSecurityException(FileSystemAccessDeleteTest.class,
				"test_accessFileSystemViaThirdPartyPackageMavenWalaAspectJ");
	}

	@Disabled(SUBJECT_PROBE_REASON)
	@PublicTest
	@Policy(value = WALA_ASPECTJ_POLICY, withinPath = THIRD_PARTY_PACKAGE_PATH)
	void test_accessFileSystemViaThirdPartyPackageMavenWalaAspectJ() {
		assertAresSecurityExceptionDelete(DeleteThirdPartyPackageMain::accessFileSystemViaThirdPartyPackage,
				ThirdPartyPackagePenguin.class, NOT_TRUSTED_DELETE_PATH);
	}

	@Test
	void test_accessFileSystemViaThirdPartyPackageMavenWalaInstrumentation_test() {
		executeTestAndExpectSecurityException(FileSystemAccessDeleteTest.class,
				"test_accessFileSystemViaThirdPartyPackageMavenWalaInstrumentation");
	}

	@Disabled(SUBJECT_PROBE_REASON)
	@PublicTest
	@Policy(value = WALA_INSTRUMENTATION_POLICY, withinPath = THIRD_PARTY_PACKAGE_PATH)
	void test_accessFileSystemViaThirdPartyPackageMavenWalaInstrumentation() {
		assertAresSecurityExceptionDelete(DeleteThirdPartyPackageMain::accessFileSystemViaThirdPartyPackage,
				ThirdPartyPackagePenguin.class, NOT_TRUSTED_DELETE_PATH);
	}
	// </editor-fold>
}
