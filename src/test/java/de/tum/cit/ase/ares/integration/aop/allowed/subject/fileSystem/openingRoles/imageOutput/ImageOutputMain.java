package de.tum.cit.ase.ares.integration.aop.allowed.subject.fileSystem.openingRoles.imageOutput;

import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.imageio.stream.FileImageOutputStream;
import javax.imageio.stream.ImageOutputStream;

/**
 * Supervised code that opens a file as an image output stream, which can read
 * the file as well as write it.
 */
public final class ImageOutputMain {

	/**
	 * Hidden constructor of a utility class.
	 */
	private ImageOutputMain() {
		throw new UnsupportedOperationException("Utility class");
	}

	/**
	 * Writes one byte through {@code ImageIO.createImageOutputStream(File)}.
	 *
	 * @param file the file to write
	 * @throws IOException if writing fails
	 */
	public static void createImageOutputStream(File file) throws IOException {
		try (ImageOutputStream output = ImageIO.createImageOutputStream(file)) {
			output.write('n');
		}
	}

	/**
	 * Writes one byte through {@code new FileImageOutputStream(File)}.
	 *
	 * @param file the file to write
	 * @throws IOException if writing fails
	 */
	public static void fileImageOutputStream(File file) throws IOException {
		try (FileImageOutputStream output = new FileImageOutputStream(file)) {
			output.write('n');
		}
	}
}
