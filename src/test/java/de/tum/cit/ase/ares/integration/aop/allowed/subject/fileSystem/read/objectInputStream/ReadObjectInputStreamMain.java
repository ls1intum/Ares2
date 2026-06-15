package de.tum.cit.ase.ares.integration.aop.allowed.subject.fileSystem.read.objectInputStream;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InvalidClassException;
import java.io.ObjectInputStream;
import java.io.OptionalDataException;
import java.io.StreamCorruptedException;

public class ReadObjectInputStreamMain {

	private ReadObjectInputStreamMain() {
		throw new SecurityException(
				"Ares Security Error (Reason: Ares-Code; Stage: Test): Main is a utility class and should not be instantiated.");
	}

	/**
	 * Access the file system using {@link ObjectInputStream} for reading.
	 *
	 * @return The content of the trusted file as string
	 */
	public static String accessFileSystemViaObjectInputStream() throws IOException, ClassNotFoundException {
		try (ObjectInputStream ois = new ObjectInputStream(
				new FileInputStream("src/test/java/de/tum/cit/ase/ares/integration/aop/allowed/subject/trusted.txt"))) {
			// Try to read an object; even when it fails due to format issues, the
			// file-system access through ObjectInputStream is still exercised.
			try {
				Object obj = ois.readObject();
				return obj.toString();
			} catch (ClassNotFoundException | InvalidClassException | StreamCorruptedException
					| OptionalDataException e) {
				// Expected for non-serialized files: the file-system access was still
				// attempted, which is what this scenario verifies.
			}
		}

		// Fallback: read as a regular text file if object deserialization fails.
		try (FileInputStream fis = new FileInputStream(
				"src/test/java/de/tum/cit/ase/ares/integration/aop/allowed/subject/trusted.txt")) {
			byte[] data = new byte[fis.available()];
			fis.read(data);
			return new String(data);
		}
	}
}
