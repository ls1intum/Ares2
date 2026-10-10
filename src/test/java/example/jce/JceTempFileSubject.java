package example.jce;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/** Student-like temporary files created through the real runtime boundary. */
public final class JceTempFileSubject {

	/** Prevents instances of the fixture. */
	private JceTempFileSubject() {
	}

	/**
	 * Creates a temporary file with {@code java.io.File}, in the given directory
	 * or, for {@code null}, in the JDK's default one.
	 */
	public static File createWithFile(File directory) throws IOException {
		return directory == null ? File.createTempFile("ares", ".tmp") : File.createTempFile("ares", ".tmp", directory);
	}

	/**
	 * Creates a temporary file with {@code java.io.File}, passing {@code null} as
	 * the directory, which also means the JDK's default one.
	 */
	public static File createWithFileAndNullDirectory() throws IOException {
		return File.createTempFile("ares", ".tmp", null);
	}

	/**
	 * Creates a temporary file with {@code java.nio.file.Files}, in the given
	 * directory or, for {@code null}, in the JDK's default one.
	 */
	public static Path createWithFiles(Path directory) throws IOException {
		return directory == null ? Files.createTempFile("ares", ".tmp")
				: Files.createTempFile(directory, "ares", ".tmp");
	}
}
