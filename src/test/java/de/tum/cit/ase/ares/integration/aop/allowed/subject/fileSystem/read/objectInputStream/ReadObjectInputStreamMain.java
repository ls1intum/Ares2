package de.tum.cit.ase.ares.integration.aop.allowed.subject.fileSystem.read.objectInputStream;

import java.io.FileInputStream;
import java.io.IOException;

public class ReadObjectInputStreamMain {

	private ReadObjectInputStreamMain() {
		throw new SecurityException(
				"Ares Security Error (Reason: Ares-Code; Stage: Test): Main is a utility class and should not be instantiated.");
	}

	/**
	 * Access the file system through the stream used by object-input scenarios.
	 * 
	 * @return The content of the trusted file as string
	 */
	public static String accessFileSystemViaObjectInputStream() throws IOException {
		try (FileInputStream fis = new FileInputStream(
				"src/test/java/de/tum/cit/ase/ares/integration/aop/allowed/subject/trusted.txt")) {
			byte[] data = new byte[fis.available()];
			fis.read(data);
			return new String(data);
		}
	}
}
