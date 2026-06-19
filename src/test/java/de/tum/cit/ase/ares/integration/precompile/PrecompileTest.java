package de.tum.cit.ase.ares.integration.precompile;

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
	void testPrecompileJavaMavenArchunitInstrumentation(@TempDir Path tempDir) {
		Path writeTarget = tempDir.resolve("project").resolve("src").resolve("test");
		// Scope the precompile scan to a benign student-like subtree. An empty
		// projectFolderPath scans all of target/classes, which contains Ares's own
		// reserved de.tum.cit.ase.ares.api.internal classes and trips the
		// ReservedPackageGuard; a real submission never bundles Ares framework classes.
		Path projectFolderPath = Path.of("test-classes/de/tum/cit/ase/ares/integration/testuser/subject/helloWorld");
		SecurityPolicyReaderAndDirector.builder().securityPolicyFilePath(Path.of(""))
				.projectFolderPath(projectFolderPath).build().createTestCases().writeTestCases(writeTarget);
	}
}
