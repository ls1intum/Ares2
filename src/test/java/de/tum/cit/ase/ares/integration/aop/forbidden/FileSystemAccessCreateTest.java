package de.tum.cit.ase.ares.integration.aop.forbidden;

import java.nio.file.Path;

import de.tum.cit.ase.ares.api.Policy;
import de.tum.cit.ase.ares.api.jupiter.PublicTest;
import de.tum.cit.ase.ares.integration.aop.forbidden.subject.fileSystem.create.file.FileCreateDirectoryMain;
import de.tum.cit.ase.ares.integration.aop.forbidden.subject.fileSystem.create.fileSystemProvider.FileSystemProviderCreateDirectoryMain;
import de.tum.cit.ase.ares.integration.aop.forbidden.subject.fileSystem.create.files.FilesCreateDirectoryMain;

class FileSystemAccessCreateTest extends SystemAccessTest {

	private static final String FILE_CREATE_WITHIN_PATH = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/create/file";
	private static final String FILES_CREATE_WITHIN_PATH = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/create/files";
	private static final String FILE_SYSTEM_PROVIDER_CREATE_WITHIN_PATH = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/create/fileSystemProvider";

	private static final Path NOT_TRUSTED_BASE_DIR = Path.of(
			"src/test/java/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/create/nottrusteddir");
	private static final Path NOT_TRUSTED_FILE_MKDIR = NOT_TRUSTED_BASE_DIR.resolve("createdDir");
	private static final Path NOT_TRUSTED_FILE_MKDIRS = NOT_TRUSTED_BASE_DIR.resolve("nested/dir");
	private static final Path NOT_TRUSTED_FILES_CREATE_DIR = NOT_TRUSTED_BASE_DIR.resolve("filesCreateDir");
	private static final Path NOT_TRUSTED_FILES_CREATE_DIRS = NOT_TRUSTED_BASE_DIR.resolve("filesCreateDirs/nested");
	private static final Path NOT_TRUSTED_PROVIDER_CREATE_DIR = NOT_TRUSTED_BASE_DIR.resolve("providerDir");

	// <editor-fold desc="File.mkdir">
	@PublicTest
	@Policy(value = ARCHUNIT_ASPECTJ_POLICY_ONE_PATH_ALLOWED_OVERWRITE, withinPath = FILE_CREATE_WITHIN_PATH)
	void test_accessFileSystemViaFileMkdirMavenArchunitAspectJ() {
		assertAresSecurityExceptionCreate(FileCreateDirectoryMain::accessFileSystemViaFileMkdir,
				FileCreateDirectoryMain.class, NOT_TRUSTED_FILE_MKDIR);
	}

	@PublicTest
	@Policy(value = ARCHUNIT_INSTRUMENTATION_POLICY_ONE_PATH_ALLOWED_OVERWRITE, withinPath = FILE_CREATE_WITHIN_PATH)
	void test_accessFileSystemViaFileMkdirMavenArchunitInstrumentation() {
		assertAresSecurityExceptionCreate(FileCreateDirectoryMain::accessFileSystemViaFileMkdir,
				FileCreateDirectoryMain.class, NOT_TRUSTED_FILE_MKDIR);
	}

	@PublicTest
	@Policy(value = WALA_ASPECTJ_POLICY_ONE_PATH_ALLOWED_OVERWRITE, withinPath = FILE_CREATE_WITHIN_PATH)
	void test_accessFileSystemViaFileMkdirMavenWalaAspectJ() {
		assertAresSecurityExceptionCreate(FileCreateDirectoryMain::accessFileSystemViaFileMkdir,
				FileCreateDirectoryMain.class, NOT_TRUSTED_FILE_MKDIR);
	}

	@PublicTest
	@Policy(value = WALA_INSTRUMENTATION_POLICY_ONE_PATH_ALLOWED_OVERWRITE, withinPath = FILE_CREATE_WITHIN_PATH)
	void test_accessFileSystemViaFileMkdirMavenWalaInstrumentation() {
		assertAresSecurityExceptionCreate(FileCreateDirectoryMain::accessFileSystemViaFileMkdir,
				FileCreateDirectoryMain.class, NOT_TRUSTED_FILE_MKDIR);
	}
	// </editor-fold>

	// <editor-fold desc="File.mkdirs">
	@PublicTest
	@Policy(value = ARCHUNIT_ASPECTJ_POLICY_ONE_PATH_ALLOWED_OVERWRITE, withinPath = FILE_CREATE_WITHIN_PATH)
	void test_accessFileSystemViaFileMkdirsMavenArchunitAspectJ() {
		assertAresSecurityExceptionCreate(FileCreateDirectoryMain::accessFileSystemViaFileMkdirs,
				FileCreateDirectoryMain.class, NOT_TRUSTED_FILE_MKDIRS);
	}

	@PublicTest
	@Policy(value = ARCHUNIT_INSTRUMENTATION_POLICY_ONE_PATH_ALLOWED_OVERWRITE, withinPath = FILE_CREATE_WITHIN_PATH)
	void test_accessFileSystemViaFileMkdirsMavenArchunitInstrumentation() {
		assertAresSecurityExceptionCreate(FileCreateDirectoryMain::accessFileSystemViaFileMkdirs,
				FileCreateDirectoryMain.class, NOT_TRUSTED_FILE_MKDIRS);
	}

	@PublicTest
	@Policy(value = WALA_ASPECTJ_POLICY_ONE_PATH_ALLOWED_OVERWRITE, withinPath = FILE_CREATE_WITHIN_PATH)
	void test_accessFileSystemViaFileMkdirsMavenWalaAspectJ() {
		assertAresSecurityExceptionCreate(FileCreateDirectoryMain::accessFileSystemViaFileMkdirs,
				FileCreateDirectoryMain.class, NOT_TRUSTED_FILE_MKDIRS);
	}

	@PublicTest
	@Policy(value = WALA_INSTRUMENTATION_POLICY_ONE_PATH_ALLOWED_OVERWRITE, withinPath = FILE_CREATE_WITHIN_PATH)
	void test_accessFileSystemViaFileMkdirsMavenWalaInstrumentation() {
		assertAresSecurityExceptionCreate(FileCreateDirectoryMain::accessFileSystemViaFileMkdirs,
				FileCreateDirectoryMain.class, NOT_TRUSTED_FILE_MKDIRS);
	}
	// </editor-fold>

	// <editor-fold desc="Files.createDirectory">
	@PublicTest
	@Policy(value = ARCHUNIT_ASPECTJ_POLICY_ONE_PATH_ALLOWED_OVERWRITE, withinPath = FILES_CREATE_WITHIN_PATH)
	void test_accessFileSystemViaFilesCreateDirectoryMavenArchunitAspectJ() {
		assertAresSecurityExceptionCreate(FilesCreateDirectoryMain::accessFileSystemViaFilesCreateDirectory,
				FilesCreateDirectoryMain.class, NOT_TRUSTED_FILES_CREATE_DIR);
	}

	@PublicTest
	@Policy(value = ARCHUNIT_INSTRUMENTATION_POLICY_ONE_PATH_ALLOWED_OVERWRITE, withinPath = FILES_CREATE_WITHIN_PATH)
	void test_accessFileSystemViaFilesCreateDirectoryMavenArchunitInstrumentation() {
		assertAresSecurityExceptionCreate(FilesCreateDirectoryMain::accessFileSystemViaFilesCreateDirectory,
				FilesCreateDirectoryMain.class, NOT_TRUSTED_FILES_CREATE_DIR);
	}

	@PublicTest
	@Policy(value = WALA_ASPECTJ_POLICY_ONE_PATH_ALLOWED_OVERWRITE, withinPath = FILES_CREATE_WITHIN_PATH)
	void test_accessFileSystemViaFilesCreateDirectoryMavenWalaAspectJ() {
		assertAresSecurityExceptionCreate(FilesCreateDirectoryMain::accessFileSystemViaFilesCreateDirectory,
				FilesCreateDirectoryMain.class, NOT_TRUSTED_FILES_CREATE_DIR);
	}

	@PublicTest
	@Policy(value = WALA_INSTRUMENTATION_POLICY_ONE_PATH_ALLOWED_OVERWRITE, withinPath = FILES_CREATE_WITHIN_PATH)
	void test_accessFileSystemViaFilesCreateDirectoryMavenWalaInstrumentation() {
		assertAresSecurityExceptionCreate(FilesCreateDirectoryMain::accessFileSystemViaFilesCreateDirectory,
				FilesCreateDirectoryMain.class, NOT_TRUSTED_FILES_CREATE_DIR);
	}
	// </editor-fold>

	// <editor-fold desc="Files.createDirectories">
	@PublicTest
	@Policy(value = ARCHUNIT_ASPECTJ_POLICY_ONE_PATH_ALLOWED_OVERWRITE, withinPath = FILES_CREATE_WITHIN_PATH)
	void test_accessFileSystemViaFilesCreateDirectoriesMavenArchunitAspectJ() {
		assertAresSecurityExceptionCreate(FilesCreateDirectoryMain::accessFileSystemViaFilesCreateDirectories,
				FilesCreateDirectoryMain.class, NOT_TRUSTED_FILES_CREATE_DIRS);
	}

	@PublicTest
	@Policy(value = ARCHUNIT_INSTRUMENTATION_POLICY_ONE_PATH_ALLOWED_OVERWRITE, withinPath = FILES_CREATE_WITHIN_PATH)
	void test_accessFileSystemViaFilesCreateDirectoriesMavenArchunitInstrumentation() {
		assertAresSecurityExceptionCreate(FilesCreateDirectoryMain::accessFileSystemViaFilesCreateDirectories,
				FilesCreateDirectoryMain.class, NOT_TRUSTED_FILES_CREATE_DIRS);
	}

	@PublicTest
	@Policy(value = WALA_ASPECTJ_POLICY_ONE_PATH_ALLOWED_OVERWRITE, withinPath = FILES_CREATE_WITHIN_PATH)
	void test_accessFileSystemViaFilesCreateDirectoriesMavenWalaAspectJ() {
		assertAresSecurityExceptionCreate(FilesCreateDirectoryMain::accessFileSystemViaFilesCreateDirectories,
				FilesCreateDirectoryMain.class, NOT_TRUSTED_FILES_CREATE_DIRS);
	}

	@PublicTest
	@Policy(value = WALA_INSTRUMENTATION_POLICY_ONE_PATH_ALLOWED_OVERWRITE, withinPath = FILES_CREATE_WITHIN_PATH)
	void test_accessFileSystemViaFilesCreateDirectoriesMavenWalaInstrumentation() {
		assertAresSecurityExceptionCreate(FilesCreateDirectoryMain::accessFileSystemViaFilesCreateDirectories,
				FilesCreateDirectoryMain.class, NOT_TRUSTED_FILES_CREATE_DIRS);
	}
	// </editor-fold>

	// <editor-fold desc="Files.createTempDirectory">
	@PublicTest
	@Policy(value = ARCHUNIT_ASPECTJ_POLICY_ONE_PATH_ALLOWED_OVERWRITE, withinPath = FILES_CREATE_WITHIN_PATH)
	void test_accessFileSystemViaFilesCreateTempDirectoryMavenArchunitAspectJ() {
		assertAresSecurityExceptionCreate(FilesCreateDirectoryMain::accessFileSystemViaFilesCreateTempDirectory,
				FilesCreateDirectoryMain.class, NOT_TRUSTED_BASE_DIR);
	}

	@PublicTest
	@Policy(value = ARCHUNIT_INSTRUMENTATION_POLICY_ONE_PATH_ALLOWED_OVERWRITE, withinPath = FILES_CREATE_WITHIN_PATH)
	void test_accessFileSystemViaFilesCreateTempDirectoryMavenArchunitInstrumentation() {
		assertAresSecurityExceptionCreate(FilesCreateDirectoryMain::accessFileSystemViaFilesCreateTempDirectory,
				FilesCreateDirectoryMain.class, NOT_TRUSTED_BASE_DIR);
	}

	@PublicTest
	@Policy(value = WALA_ASPECTJ_POLICY_ONE_PATH_ALLOWED_OVERWRITE, withinPath = FILES_CREATE_WITHIN_PATH)
	void test_accessFileSystemViaFilesCreateTempDirectoryMavenWalaAspectJ() {
		assertAresSecurityExceptionCreate(FilesCreateDirectoryMain::accessFileSystemViaFilesCreateTempDirectory,
				FilesCreateDirectoryMain.class, NOT_TRUSTED_BASE_DIR);
	}

	@PublicTest
	@Policy(value = WALA_INSTRUMENTATION_POLICY_ONE_PATH_ALLOWED_OVERWRITE, withinPath = FILES_CREATE_WITHIN_PATH)
	void test_accessFileSystemViaFilesCreateTempDirectoryMavenWalaInstrumentation() {
		assertAresSecurityExceptionCreate(FilesCreateDirectoryMain::accessFileSystemViaFilesCreateTempDirectory,
				FilesCreateDirectoryMain.class, NOT_TRUSTED_BASE_DIR);
	}
	// </editor-fold>

	// <editor-fold desc="FileSystemProvider.createDirectory">
	@PublicTest
	@Policy(value = ARCHUNIT_ASPECTJ_POLICY_ONE_PATH_ALLOWED_OVERWRITE, withinPath = FILE_SYSTEM_PROVIDER_CREATE_WITHIN_PATH)
	void test_accessFileSystemViaFileSystemProviderCreateDirectoryMavenArchunitAspectJ() {
		assertAresSecurityExceptionCreate(
				FileSystemProviderCreateDirectoryMain::accessFileSystemViaFileSystemProviderCreateDirectory,
				FileSystemProviderCreateDirectoryMain.class, NOT_TRUSTED_PROVIDER_CREATE_DIR);
	}

	@PublicTest
	@Policy(value = ARCHUNIT_INSTRUMENTATION_POLICY_ONE_PATH_ALLOWED_OVERWRITE, withinPath = FILE_SYSTEM_PROVIDER_CREATE_WITHIN_PATH)
	void test_accessFileSystemViaFileSystemProviderCreateDirectoryMavenArchunitInstrumentation() {
		assertAresSecurityExceptionCreate(
				FileSystemProviderCreateDirectoryMain::accessFileSystemViaFileSystemProviderCreateDirectory,
				FileSystemProviderCreateDirectoryMain.class, NOT_TRUSTED_PROVIDER_CREATE_DIR);
	}

	@PublicTest
	@Policy(value = WALA_ASPECTJ_POLICY_ONE_PATH_ALLOWED_OVERWRITE, withinPath = FILE_SYSTEM_PROVIDER_CREATE_WITHIN_PATH)
	void test_accessFileSystemViaFileSystemProviderCreateDirectoryMavenWalaAspectJ() {
		assertAresSecurityExceptionCreate(
				FileSystemProviderCreateDirectoryMain::accessFileSystemViaFileSystemProviderCreateDirectory,
				FileSystemProviderCreateDirectoryMain.class, NOT_TRUSTED_PROVIDER_CREATE_DIR);
	}

	@PublicTest
	@Policy(value = WALA_INSTRUMENTATION_POLICY_ONE_PATH_ALLOWED_OVERWRITE, withinPath = FILE_SYSTEM_PROVIDER_CREATE_WITHIN_PATH)
	void test_accessFileSystemViaFileSystemProviderCreateDirectoryMavenWalaInstrumentation() {
		assertAresSecurityExceptionCreate(
				FileSystemProviderCreateDirectoryMain::accessFileSystemViaFileSystemProviderCreateDirectory,
				FileSystemProviderCreateDirectoryMain.class, NOT_TRUSTED_PROVIDER_CREATE_DIR);
	}
	// </editor-fold>
}
