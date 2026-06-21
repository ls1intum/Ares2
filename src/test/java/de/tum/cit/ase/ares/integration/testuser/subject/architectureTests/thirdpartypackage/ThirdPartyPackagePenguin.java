package de.tum.cit.ase.ares.integration.testuser.subject.architectureTests.thirdpartypackage;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.apache.commons.io.FileUtils;

/**
 * This class is used to emulate a third-party package that is not part of the
 * test user's codebase.
 */
public class ThirdPartyPackagePenguin {

	public static String readFile() throws IOException {
		return Files.readString(
				Path.of("src/test/java/de/tum/cit/ase/ares/integration/aop/forbidden/subject/nottrusted.txt"));
	}

	public static String readFile(Path pathToTrustedFile) throws IOException {
		return Files.readString(pathToTrustedFile);
	}

	public static Path overwriteFile(Path pathToUntrustedFile) throws IOException {
		byte[] content = "Hello, world!".getBytes();
		return Files.write(pathToUntrustedFile, content);
	}

	public static Process executeFile() throws IOException {
		return Runtime.getRuntime()
				.exec("src/test/java/de/tum/cit/ase/ares/integration/aop/forbidden/subject/nottrusted.txt");
	}

	public static Process executeFile(String filePath) throws IOException {
		return Runtime.getRuntime().exec(filePath);
	}

	public static void deleteFile() throws IOException {
		Files.delete(Path.of(
				"src/test/java/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/delete/nottrusteddir/nottrusted.txt"));
	}

	/**
	 * Deletes the not-trusted file through Apache Commons IO's
	 * {@link FileUtils#forceDelete(File)}. The actual delete happens inside the
	 * (non-woven) commons-io library, so this exercises the dedicated
	 * {@code FileUtils.forceDelete} interception rather than a JDK delete call.
	 */
	public static void deleteFileViaCommonsIo() throws IOException {
		FileUtils.forceDelete(new File(
				"src/test/java/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/delete/nottrusteddir/nottrusted.txt"));
	}
}
