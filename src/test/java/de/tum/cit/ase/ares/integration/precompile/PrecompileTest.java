package de.tum.cit.ase.ares.integration.precompile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import de.tum.cit.ase.ares.api.policy.SecurityPolicyReaderAndDirector;

public class PrecompileTest {

	/**
	 * Exercises the precompile flow ({@code writeTestCases}) which generates the
	 * security-test scaffold (architecture/AOP helper classes, localisation
	 * resources and the Phobos files).
	 * <p>
	 * The scaffold is written into a JUnit-managed temporary directory rather than
	 * the current working directory, so the repository is never polluted. The write
	 * target is nested several levels deep on purpose: the localisation step places
	 * its {@code resources} folder as a sibling two levels up, and the Phobos step
	 * climbs three levels up, so a deep target keeps every generated artefact
	 * inside {@code tempDir}, which {@link TempDir} removes afterwards.
	 */
	@Test
	void testPrecompileJavaMavenArchunitInstrumentation(@TempDir Path tempDir) throws IOException {
		Path projectFolderPath = Files.createDirectory(tempDir.resolve("project"));
		Files.writeString(projectFolderPath.resolve("pom.xml"), "<project/>");
		Files.createDirectories(projectFolderPath.resolve("src/main/java"));
		Files.createDirectories(projectFolderPath.resolve("src/test/java"));
		Files.createDirectories(projectFolderPath.resolve("target/classes"));
		Path writeTarget = projectFolderPath.resolve("src/test/java");
		SecurityPolicyReaderAndDirector.builder().projectFolderPath(projectFolderPath).build().createTestCases()
				.writeTestCases(writeTarget);
	}
}
