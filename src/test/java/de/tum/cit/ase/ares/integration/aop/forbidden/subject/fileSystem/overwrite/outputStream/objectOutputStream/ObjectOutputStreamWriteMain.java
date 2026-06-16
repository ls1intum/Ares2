package de.tum.cit.ase.ares.integration.aop.forbidden.subject.fileSystem.overwrite.outputStream.objectOutputStream;

import java.io.FileOutputStream;
import java.io.IOException;

public class ObjectOutputStreamWriteMain {

	private static final String NOT_TRUSTED_DIR = "src/test/java/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/overwrite/nottrusteddir";
	private static final String NOT_TRUSTED_DAT = NOT_TRUSTED_DIR + "/nottrusted.dat";

	private ObjectOutputStreamWriteMain() {
		throw new SecurityException(
				"Ares Security Error (Reason: Ares-Code; Stage: Test): Main is a utility class and should not be instantiated.");
	}

	/**
	 * Access the file system through the file-output sink used by object streams.
	 */
	public static void accessFileSystemViaObjectOutputStream() throws IOException {
		try (FileOutputStream outputStream = new FileOutputStream(NOT_TRUSTED_DAT)) {
			outputStream.write(100);
		}
	}

	public static void accessFileSystemViaObjectOutputStreamWithData() throws IOException {
		try (FileOutputStream outputStream = new FileOutputStream(NOT_TRUSTED_DAT)) {
			outputStream.write("Hello, world!".getBytes());
		}
	}

	public static void accessFileSystemViaObjectOutputStreamWithDataAndOffset() throws IOException {
		try (FileOutputStream outputStream = new FileOutputStream(NOT_TRUSTED_DAT)) {
			byte[] data = "Hello, world!".getBytes();
			outputStream.write(data, 0, data.length);
		}
	}

	public static void accessFileSystemViaObjectOutputStreamWithObject() throws IOException {
		try (FileOutputStream outputStream = new FileOutputStream(NOT_TRUSTED_DAT)) {
			outputStream.write("John Doe,30".getBytes());
		}
	}

	public static void accessFileSystemViaObjectOutputStreamWithPrimitives() throws IOException {
		try (FileOutputStream outputStream = new FileOutputStream(NOT_TRUSTED_DAT)) {
			outputStream.write("42,3.14,true,Hello ObjectOutputStream".getBytes());
		}
	}
}
