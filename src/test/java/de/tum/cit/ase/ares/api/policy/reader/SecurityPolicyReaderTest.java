package de.tum.cit.ase.ares.api.policy.reader;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import de.tum.cit.ase.ares.api.policy.reader.yaml.SecurityPolicyYAMLReader;

class SecurityPolicyReaderTest {

	/**
	 * A null project root exercises the fallback that derives the effective root
	 * from the policy file's own location, rather than the caller-supplied one.
	 */
	@Test
	void selectSecurityPolicyReaderWithNullProjectRootDerivesRootFromPolicyLocation(@TempDir Path tempDir)
			throws IOException {
		Path policyFile = Files.createFile(tempDir.resolve("policy.yaml"));
		SecurityPolicyReader reader = SecurityPolicyReader.selectSecurityPolicyReader(policyFile, null);
		assertInstanceOf(SecurityPolicyYAMLReader.class, reader);
	}

	/**
	 * A path with no file name (a filesystem root) has no extension, so it maps to
	 * the empty extension and is rejected as an unsupported format.
	 */
	@Test
	void selectSecurityPolicyReaderRejectsPathWithoutFileName(@TempDir Path tempDir) {
		Path noFileName = Path.of("/");
		assertThrows(IllegalArgumentException.class,
				() -> SecurityPolicyReader.selectSecurityPolicyReader(noFileName, tempDir));
	}
}
