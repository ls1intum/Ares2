package de.tum.cit.ase.ares.api.architecture.java.wala;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class JavaWalaTestCaseCacheTest {
	@TempDir
	Path temporaryDirectory;

	@Test
	void testImplementationFingerprintChangesWhenExistingClassIsOverwritten() throws IOException {
		Path apiRoot = temporaryDirectory.resolve(Path.of("de", "tum", "cit", "ase", "ares", "api"));
		Files.createDirectories(apiRoot);
		Path classFile = apiRoot.resolve("Rule.class");
		Files.writeString(classFile, "first implementation");
		String firstFingerprint = JavaWalaTestCase.implementationFingerprint(temporaryDirectory);

		Files.writeString(classFile, "second implementation");
		String secondFingerprint = JavaWalaTestCase.implementationFingerprint(temporaryDirectory);

		Assertions.assertNotEquals(firstFingerprint, secondFingerprint);
	}
}
