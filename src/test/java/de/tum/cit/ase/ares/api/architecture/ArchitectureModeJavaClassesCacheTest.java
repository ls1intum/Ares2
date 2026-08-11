package de.tum.cit.ase.ares.api.architecture;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.tngtech.archunit.core.domain.JavaClasses;

class ArchitectureModeJavaClassesCacheTest {

	@TempDir
	Path temporaryDirectory;

	@Test
	void reusesOnlyUnchangedArchitectureInputs() throws Exception {
		Path classFile = temporaryDirectory.resolve("Fixture.class");
		copyClassFile(ArchitectureMode.class, classFile);

		JavaClasses firstImport = ArchitectureMode.ARCHUNIT.getJavaClasses(temporaryDirectory.toString());
		JavaClasses cachedImport = ArchitectureMode.ARCHUNIT.getJavaClasses(temporaryDirectory.toString());
		assertThat(cachedImport).isSameAs(firstImport);

		copyClassFile(ArchitectureTestCase.class, classFile);
		JavaClasses refreshedImport = ArchitectureMode.ARCHUNIT.getJavaClasses(temporaryDirectory.toString());
		assertThat(refreshedImport).isNotSameAs(firstImport);
		assertThat(ArchitectureMode.ARCHUNIT.getJavaClasses(temporaryDirectory.toString())).isSameAs(refreshedImport);
	}

	private static void copyClassFile(Class<?> sourceClass, Path target) throws IOException {
		String resourceName = "/" + sourceClass.getName().replace('.', '/') + ".class";
		try (InputStream classBytes = sourceClass.getResourceAsStream(resourceName)) {
			assertThat(classBytes).as("compiled fixture %s", resourceName).isNotNull();
			Files.copy(classBytes, target, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
		}
	}
}
