package de.tum.cit.ase.ares.integration.aop.forbidden.subject.baseline.runtime;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileAttribute;

/**
 * Student code trying what the secure baseline still denies without a policy
 * entry: reading a random-seed device or the timezone file itself, creating a
 * temporary file in a named directory, and planting a file from a callback the
 * JDK runs while it creates a temporary file.
 */
public final class BaselineStillDeniedMain {

	/**
	 * Prevents instantiation of this subject.
	 */
	private BaselineStillDeniedMain() {
		throw new SecurityException(
				"Ares Security Error (Reason: Ares-Code; Stage: Test): BaselineStillDeniedMain is a utility class and should not be instantiated.");
	}

	/**
	 * Reads one byte straight from {@code /dev/urandom}.
	 *
	 * @return the byte read
	 * @throws IOException if the device cannot be read
	 */
	public static int readEntropyDeviceDirectly() throws IOException {
		try (InputStream device = new FileInputStream("/dev/urandom")) {
			return device.read();
		}
	}

	/**
	 * Reads one byte straight from {@code /etc/localtime}.
	 *
	 * @return the byte read
	 * @throws IOException if the file cannot be read
	 */
	public static int readSystemTimezoneFileDirectly() throws IOException {
		try (InputStream timezone = new FileInputStream("/etc/localtime")) {
			return timezone.read();
		}
	}

	/**
	 * Creates a temporary file in a named directory with
	 * {@link File#createTempFile(String, String, File)}.
	 *
	 * @param directory the directory to create it in
	 * @return the created file
	 * @throws IOException if the file cannot be created
	 */
	public static File createTempFileIn(File directory) throws IOException {
		return File.createTempFile("ares-baseline-", ".tmp", directory);
	}

	/**
	 * Creates a temporary file in a named directory with
	 * {@link Files#createTempFile(Path, String, String, FileAttribute...)}.
	 *
	 * @param directory the directory to create it in
	 * @return the created file
	 * @throws IOException if the file cannot be created
	 */
	public static Path createTempFileWithFilesIn(Path directory) throws IOException {
		return Files.createTempFile(directory, "ares-baseline-", ".tmp");
	}

	/**
	 * Creates a temporary directory with
	 * {@link Files#createTempDirectory(String, FileAttribute...)}.
	 *
	 * @return the created directory
	 * @throws IOException if it cannot be created
	 */
	public static Path createTempDirectory() throws IOException {
		return Files.createTempDirectory("ares-baseline-");
	}

	/**
	 * Creates a temporary file without a directory, passing a file attribute whose
	 * name the JDK reads while it creates the file; reading the name plants another
	 * file at the given place.
	 *
	 * @param plantedFile where the callback tries to create a file
	 * @return the created temporary file
	 * @throws IOException if a file cannot be created
	 */
	public static Path createTempFileWithPlantingAttribute(Path plantedFile) throws IOException {
		return Files.createTempFile("ares-baseline-", ".tmp", new PlantingAttribute(plantedFile));
	}

	/**
	 * A file attribute that creates a file as soon as the JDK asks for its name. A
	 * plain class, not a record: the instrumentation agent cannot yet transform the
	 * bootstrap call a record uses for its generated methods.
	 */
	private static final class PlantingAttribute implements FileAttribute<Object> {

		/**
		 * Where the file is created.
		 */
		private final Path plantedFile;

		/**
		 * Creates the attribute.
		 *
		 * @param plantedFile where the file is created
		 */
		private PlantingAttribute(Path plantedFile) {
			this.plantedFile = plantedFile;
		}

		/**
		 * Creates the planted file, then reports a name no file system supports.
		 *
		 * @return the attribute name
		 */
		@Override
		public String name() {
			try {
				Files.createFile(plantedFile);
			} catch (IOException ignored) {
				return "ares:planted";
			}
			return "ares:planted";
		}

		/**
		 * Returns no value.
		 *
		 * @return {@code null}
		 */
		@Override
		public Object value() {
			return null;
		}
	}
}
