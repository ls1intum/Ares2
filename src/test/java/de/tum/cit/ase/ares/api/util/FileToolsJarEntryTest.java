package de.tum.cit.ase.ares.api.util;

import static de.tum.cit.ase.ares.api.localization.Messages.localized;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/** Checks how a file inside a JAR file is read when the entry is not there. */
class FileToolsJarEntryTest {

	/**
	 * An entry the JAR file does not contain is refused with the Ares error
	 * template, naming the entry and the JAR file.
	 *
	 * @param directory a folder for the JAR file
	 * @throws IOException if the JAR file cannot be written
	 */
	@Test
	void refusesAnEntryTheJarDoesNotContain(@TempDir Path directory) throws IOException {
		Path jar = directory.resolve("resources.jar");
		try (OutputStream file = Files.newOutputStream(jar); JarOutputStream out = new JarOutputStream(file)) {
			out.putNextEntry(new JarEntry("present.txt"));
			out.closeEntry();
		}
		SecurityException refused = assertThrows(SecurityException.class,
				() -> FileTools.readFile(jar.resolve("absent.txt")));
		assertEquals(localized("security.file-tools.jar.entry.missing", "absent.txt", jar), refused.getMessage());
	}
}
