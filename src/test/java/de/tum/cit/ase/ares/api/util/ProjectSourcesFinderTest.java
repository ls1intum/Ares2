package de.tum.cit.ase.ares.api.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import de.tum.cit.ase.ares.api.buildtoolconfiguration.BuildMode;

class ProjectSourcesFinderTest {
	@TempDir
	Path temporaryDirectory;

	@Test
	void discoversMavenCustomRoots() throws IOException {
		Files.createDirectories(temporaryDirectory.resolve("assignment"));
		Files.createDirectories(temporaryDirectory.resolve("checks"));
		Files.writeString(temporaryDirectory.resolve("pom.xml"), """
				<project><build><sourceDirectory>${project.basedir}/assignment</sourceDirectory>
				<testSourceDirectory>${project.basedir}/checks</testSourceDirectory></build></project>
				""");
		var configuration = ProjectSourcesFinder.discover(temporaryDirectory);
		assertEquals(BuildMode.MAVEN, configuration.buildMode());
		assertEquals(temporaryDirectory.resolve("assignment").toRealPath(),
				configuration.productionSourceRoots().get(0));
		assertEquals(temporaryDirectory.resolve("checks").toRealPath(), configuration.testSourceRoots().get(0));
	}

	@Test
	void discoversGroovyAndKotlinGradleRoots() throws IOException {
		for (String descriptor : new String[] { "build.gradle", "build.gradle.kts" }) {
			Path root = Files.createDirectory(temporaryDirectory.resolve(descriptor.replace('.', '-')));
			Files.createDirectories(root.resolve("assignment"));
			Files.createDirectories(root.resolve("checks"));
			Files.writeString(root.resolve("gradle.properties"), "assignmentPath=assignment\n");
			Files.writeString(root.resolve(descriptor), """
					sourceSets {
					  main { java { srcDir(assignmentPath) } }
					  test { java { srcDir 'checks' } }
					}
					""");
			var configuration = ProjectSourcesFinder.discover(root);
			assertEquals(BuildMode.GRADLE, configuration.buildMode());
			assertEquals(root.resolve("assignment").toRealPath(), configuration.productionSourceRoots().get(0));
			assertEquals(root.resolve("checks").toRealPath(), configuration.testSourceRoots().get(0));
		}
	}

	/**
	 * Regression: 'srcDir' is a prefix of 'srcDirs', so an alternation that tried
	 * the singular form first consumed it and captured the leftover "s = [", which
	 * resolved to nothing. Every srcDirs declaration was dropped without a trace,
	 * and a project declaring its main sources that way looked like a project with
	 * no production sources at all.
	 */
	@Test
	void discoversGradleRootsDeclaredWithSrcDirsList() throws IOException {
		Files.createDirectories(temporaryDirectory.resolve("assignment/src"));
		Files.createDirectories(temporaryDirectory.resolve("test"));
		Files.writeString(temporaryDirectory.resolve("build.gradle"), """
				def assignmentSrcDir = "assignment/src"
				sourceSets {
				  test { java { srcDir 'test' } }
				  main { java { srcDirs = [assignmentSrcDir] } }
				}
				""");
		var configuration = ProjectSourcesFinder.discover(temporaryDirectory);
		assertEquals(temporaryDirectory.resolve("assignment/src").toRealPath(),
				configuration.productionSourceRoots().get(0));
		assertEquals(temporaryDirectory.resolve("test").toRealPath(), configuration.testSourceRoots().get(0));
	}

	@Test
	void discoversGradleRootsFromLiteralListsAndAppendedAssignments() throws IOException {
		Files.createDirectories(temporaryDirectory.resolve("first"));
		Files.createDirectories(temporaryDirectory.resolve("second"));
		Files.createDirectories(temporaryDirectory.resolve("appended"));
		Files.writeString(temporaryDirectory.resolve("build.gradle"), """
				sourceSets {
				  main { java { srcDirs = ['first', 'second'] } }
				  test { java { srcDirs += ['appended'] } }
				}
				""");
		var configuration = ProjectSourcesFinder.discover(temporaryDirectory);
		assertEquals(2, configuration.productionSourceRoots().size());
		assertEquals(temporaryDirectory.resolve("first").toRealPath(), configuration.productionSourceRoots().get(0));
		assertEquals(temporaryDirectory.resolve("second").toRealPath(), configuration.productionSourceRoots().get(1));
		assertEquals(temporaryDirectory.resolve("appended").toRealPath(), configuration.testSourceRoots().get(0));
	}

	@Test
	void rejectsAmbiguousMissingAndEscapingProjects() throws IOException {
		Files.writeString(temporaryDirectory.resolve("pom.xml"), "<project/>");
		Files.writeString(temporaryDirectory.resolve("build.gradle"), "plugins { id 'java' }");
		assertThrows(IllegalStateException.class, () -> ProjectSourcesFinder.discover(temporaryDirectory));
		assertEquals(BuildMode.GRADLE, ProjectSourcesFinder.discover(temporaryDirectory, BuildMode.GRADLE).buildMode());
		Files.delete(temporaryDirectory.resolve("pom.xml"));
		Path escape = temporaryDirectory.resolveSibling(temporaryDirectory.getFileName() + "-escape");
		Files.createDirectory(escape);
		Files.writeString(temporaryDirectory.resolve("build.gradle"),
				"sourceSets { main { java { srcDir '../" + escape.getFileName() + "' } } }");
		assertThrows(SecurityException.class,
				() -> ProjectSourcesFinder.discover(temporaryDirectory, BuildMode.GRADLE));
	}
}
