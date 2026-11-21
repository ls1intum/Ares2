package de.tum.cit.ase.ares.api.ast;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Writes Java source files for the AST tests.
 */
public final class AstSourceFiles {

	private AstSourceFiles() {
	}

	/**
	 * Writes the source as UTF-8 below the root and creates missing folders.
	 *
	 * @param root         the folder the file goes into
	 * @param relativePath the path of the file below the root
	 * @param source       the text to write
	 * @return the written file
	 * @throws IOException if the file cannot be written
	 */
	public static Path write(Path root, String relativePath, String source) throws IOException {
		Path file = root.resolve(relativePath);
		Files.createDirectories(file.getParent());
		return Files.writeString(file, source, StandardCharsets.UTF_8);
	}
}
