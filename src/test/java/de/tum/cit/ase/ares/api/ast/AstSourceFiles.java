package de.tum.cit.ase.ares.api.ast;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Writes Java source files for the AST tests.
 */
public final class AstSourceFiles {

	/**
	 * Stops anyone from creating this helper, which has only static methods.
	 */
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

	/**
	 * Writes {@code pom.xml} below the root with the source folder as its source
	 * directory. The folder is XML-escaped, so a name with an ampersand works.
	 *
	 * @param root            the folder the pom goes into
	 * @param sourceDirectory the folder to name in the pom
	 * @return the written pom
	 * @throws IOException if the file cannot be written
	 */
	public static Path writePomWithSourceDirectory(Path root, Path sourceDirectory) throws IOException {
		String escaped = sourceDirectory.toString().replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
				.replace("\"", "&quot;").replace("'", "&apos;");
		return write(root, "pom.xml",
				"<project><build><sourceDirectory>" + escaped + "</sourceDirectory></build></project>");
	}
}
