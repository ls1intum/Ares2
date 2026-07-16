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

	@Test
	void testImplementationFingerprintChangesWhenEitherArchitectureEngineChanges() throws IOException {
		Path aresCodeSource = Files.createDirectory(temporaryDirectory.resolve("ares"));
		Path walaCodeSource = temporaryDirectory.resolve("wala.jar");
		Path archUnitCodeSource = temporaryDirectory.resolve("archunit.jar");
		Files.writeString(walaCodeSource, "first WALA implementation");
		Files.writeString(archUnitCodeSource, "first ArchUnit implementation");
		String original = JavaWalaTestCase.implementationFingerprint(aresCodeSource, walaCodeSource,
				archUnitCodeSource);

		Files.writeString(walaCodeSource, "second WALA implementation");
		String changedWala = JavaWalaTestCase.implementationFingerprint(aresCodeSource, walaCodeSource,
				archUnitCodeSource);
		Files.writeString(archUnitCodeSource, "second ArchUnit implementation");
		String changedArchUnit = JavaWalaTestCase.implementationFingerprint(aresCodeSource, walaCodeSource,
				archUnitCodeSource);

		Assertions.assertNotEquals(original, changedWala);
		Assertions.assertNotEquals(changedWala, changedArchUnit);
	}
}
