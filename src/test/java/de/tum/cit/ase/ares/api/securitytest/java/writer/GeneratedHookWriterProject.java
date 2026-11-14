package de.tum.cit.ase.ares.api.securitytest.java.writer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.io.TempDir;

import de.tum.cit.ase.ares.api.buildtoolconfiguration.BuildMode;
import de.tum.cit.ase.ares.api.buildtoolconfiguration.BuildToolConfiguration;

/**
 * The exercise every generated-hook writer test writes into: a temporary
 * project with a test source root and a plain Gradle build, or a Maven layout
 * on request.
 */
abstract class GeneratedHookWriterProject {

	/** The exercise package the copied Ares classes live in. */
	static final String PACKAGE = "com.example";

	/** The exercise root of each test. */
	@TempDir
	Path projectRoot;

	/** The exercise's test source root. */
	Path testFolder;

	/**
	 * Creates the exercise folders and a plain Gradle build.
	 *
	 * @throws IOException if the folders cannot be created
	 */
	@BeforeEach
	void createProject() throws IOException {
		testFolder = Files.createDirectories(projectRoot.resolve("src/test/java"));
		Files.writeString(projectRoot.resolve("build.gradle"), "plugins { id 'java' }");
	}

	/**
	 * Turns the project into a Maven one with its output folders.
	 *
	 * @return the Maven layout.
	 * @throws IOException if a folder or the build file cannot be written
	 */
	BuildToolConfiguration mavenLayout() throws IOException {
		for (String directory : List.of("src/main/java", "target/classes", "target/test-classes")) {
			Files.createDirectories(projectRoot.resolve(directory));
		}
		Files.writeString(projectRoot.resolve("pom.xml"), "<project/>");
		return new BuildToolConfiguration(BuildMode.MAVEN, projectRoot, List.of(projectRoot.resolve("src/main/java")),
				List.of(testFolder), projectRoot.resolve("target/classes"), projectRoot.resolve("target/test-classes"));
	}

	/**
	 * Where a generated class's source belongs.
	 *
	 * @param simpleName the class's simple name.
	 * @return the source file.
	 */
	Path source(String simpleName) {
		return testFolder.resolve("de/tum/cit/ase/ares/generated").resolve(simpleName + ".java");
	}
}
