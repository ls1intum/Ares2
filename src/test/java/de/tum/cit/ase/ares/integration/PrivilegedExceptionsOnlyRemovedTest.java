package de.tum.cit.ase.ares.integration;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import javax.tools.ToolProvider;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Checks that the removed {@code @PrivilegedExceptionsOnly} annotation is gone,
 * so a test still importing it fails to compile and names it.
 */
class PrivilegedExceptionsOnlyRemovedTest {

	/**
	 * Source importing the removed annotation fails to compile.
	 *
	 * @param tempDir where the source is written.
	 * @throws Exception if the source cannot be written
	 */
	@Test
	void removedAnnotationDoesNotCompile(@TempDir Path tempDir) throws Exception {
		Path source = tempDir.resolve("OldAnnotationImport.java");
		Files.writeString(source,
				"import de.tum.cit.ase.ares.api.PrivilegedExceptionsOnly; class OldAnnotationImport {} ");
		ByteArrayOutputStream diagnostics = new ByteArrayOutputStream();
		int exitCode = ToolProvider.getSystemJavaCompiler().run(null, null, diagnostics, "-classpath",
				System.getProperty("java.class.path"), source.toString());
		assertThat(exitCode).isNotZero();
		assertThat(diagnostics.toString(StandardCharsets.UTF_8)).contains("PrivilegedExceptionsOnly");
	}
}
