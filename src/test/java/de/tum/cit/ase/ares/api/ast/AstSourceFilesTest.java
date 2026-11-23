package de.tum.cit.ase.ares.api.ast;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.file.Path;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import de.tum.cit.ase.ares.api.AresConfiguration;
import de.tum.cit.ase.ares.api.util.ProjectSourcesFinder;

/**
 * Checks {@link AstSourceFiles}, in particular that the generated pom keeps a
 * source folder name with an ampersand readable.
 */
class AstSourceFilesTest {

	/**
	 * A fresh folder for the files of one test.
	 */
	@TempDir
	Path directory;

	/**
	 * The pom path found before the test, put back afterwards.
	 */
	private String savedPomPath;

	/**
	 * The Gradle build path found before the test, put back afterwards.
	 */
	private String savedGradlePath;

	/**
	 * Remembers the build file paths.
	 */
	@BeforeEach
	void rememberSettings() {
		savedPomPath = ProjectSourcesFinder.getPomXmlPath();
		savedGradlePath = ProjectSourcesFinder.getBuildGradlePath();
	}

	/**
	 * Puts the old build file paths back.
	 */
	@AfterEach
	void restoreSettings() {
		AresConfiguration.setPomXmlPath(savedPomPath);
		AresConfiguration.setBuildGradlePath(savedGradlePath);
	}

	/**
	 * A source folder with an ampersand in its name is found again from the pom.
	 */
	@Test
	void pomKeepsAnAmpersandInTheSourceFolder() throws IOException {
		Path sources = directory.resolve("Research & Teaching");
		AstSourceFiles.write(sources, "A.java", "class A {\n}\n");
		Path pom = AstSourceFiles.writePomWithSourceDirectory(directory, sources);
		AresConfiguration.setPomXmlPath(pom.toString());
		AresConfiguration.setBuildGradlePath(directory.resolve("absent.gradle").toString());
		assertThat(ProjectSourcesFinder.findProjectSourcesPath()).contains(sources);
	}
}
