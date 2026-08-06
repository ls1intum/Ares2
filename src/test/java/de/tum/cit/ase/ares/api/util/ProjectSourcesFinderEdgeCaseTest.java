package de.tum.cit.ase.ares.api.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import de.tum.cit.ase.ares.api.buildtoolconfiguration.BuildMode;

/**
 * Exercises the rejection, fallback and legacy paths of
 * {@link ProjectSourcesFinder}.
 * <p>
 * Discovery decides which sources Ares supervises, so a path that is never
 * taken under test is a way for the supervised scope to be wrong without anyone
 * noticing. {@link ProjectSourcesFinderTest} covers the layouts that resolve;
 * this covers what happens when they do not.
 */
@DisplayName("ProjectSourcesFinder edge cases")
class ProjectSourcesFinderEdgeCaseTest {

	@TempDir
	Path temporaryDirectory;

	private String originalPomPath;
	private String originalGradlePath;

	@BeforeEach
	void rememberConfiguredPaths() {
		originalPomPath = ProjectSourcesFinder.getPomXmlPath();
		originalGradlePath = ProjectSourcesFinder.getBuildGradlePath();
	}

	@AfterEach
	void restoreConfiguredPaths() {
		ProjectSourcesFinder.setPomXmlPath(originalPomPath);
		ProjectSourcesFinder.setBuildGradlePath(originalGradlePath);
	}

	// <editor-fold desc="Construction">

	@Test
	@DisplayName("Refuses instantiation, as a utility class")
	void refusesInstantiation() throws NoSuchMethodException {
		Constructor<ProjectSourcesFinder> constructor = ProjectSourcesFinder.class.getDeclaredConstructor();
		constructor.setAccessible(true);
		InvocationTargetException failure = assertThrows(InvocationTargetException.class, constructor::newInstance);
		assertTrue(failure.getCause() instanceof SecurityException,
				"expected a SecurityException, got " + failure.getCause());
	}

	// </editor-fold>

	// <editor-fold desc="Rejections">

	@Test
	@DisplayName("Rejects a project root that is not a directory")
	void rejectsProjectRootThatIsNotADirectory() throws IOException {
		Path file = Files.writeString(temporaryDirectory.resolve("not-a-directory"), "");
		assertThrows(IllegalArgumentException.class, () -> ProjectSourcesFinder.discover(file));
	}

	@Test
	@DisplayName("Rejects a selected build tool whose descriptor is absent")
	void rejectsSelectedModeWithoutItsDescriptor() throws IOException {
		Files.writeString(temporaryDirectory.resolve("build.gradle"), "plugins { id 'java' }");
		assertThrows(IllegalStateException.class,
				() -> ProjectSourcesFinder.discover(temporaryDirectory, BuildMode.MAVEN));

		Path mavenOnly = Files.createDirectory(temporaryDirectory.resolve("maven-only"));
		Files.writeString(mavenOnly.resolve("pom.xml"), "<project/>");
		assertThrows(IllegalStateException.class, () -> ProjectSourcesFinder.discover(mavenOnly, BuildMode.GRADLE));
	}

	@Test
	@DisplayName("Rejects a project with no build descriptor at all")
	void rejectsProjectWithoutAnyDescriptor() {
		assertThrows(IllegalStateException.class, () -> ProjectSourcesFinder.discover(temporaryDirectory));
	}

	@Test
	@DisplayName("Rejects a source root that is not a directory")
	void rejectsSourceRootThatIsNotADirectory() throws IOException {
		Files.writeString(temporaryDirectory.resolve("assignment"), "");
		Files.writeString(temporaryDirectory.resolve("build.gradle"),
				"sourceSets { main { java { srcDir 'assignment' } } }");
		assertThrows(IllegalStateException.class, () -> ProjectSourcesFinder.discover(temporaryDirectory));
	}

	@Test
	@DisplayName("Rejects a malformed Maven descriptor")
	void rejectsMalformedMavenDescriptor() throws IOException {
		Files.writeString(temporaryDirectory.resolve("pom.xml"), "<project><build>");
		assertThrows(IllegalStateException.class, () -> ProjectSourcesFinder.discover(temporaryDirectory));
	}

	@Test
	@DisplayName("Rejects an unreadable Gradle descriptor")
	void rejectsUnreadableGradleDescriptor() throws IOException {
		assumePosix();
		Path descriptor = Files.writeString(temporaryDirectory.resolve("build.gradle"), "sourceSets { }");
		Files.setPosixFilePermissions(descriptor, PosixFilePermissions.fromString("---------"));
		try {
			assumeUnreadable(descriptor);
			assertThrows(IllegalStateException.class, () -> ProjectSourcesFinder.discover(temporaryDirectory));
		} finally {
			Files.setPosixFilePermissions(descriptor, PosixFilePermissions.fromString("rw-r--r--"));
		}
	}

	@Test
	@DisplayName("Rejects unreadable Gradle properties")
	void rejectsUnreadableGradleProperties() throws IOException {
		assumePosix();
		Files.createDirectories(temporaryDirectory.resolve("assignment"));
		Files.writeString(temporaryDirectory.resolve("build.gradle"),
				"sourceSets { main { java { srcDir assignmentPath } } }");
		Path properties = Files.writeString(temporaryDirectory.resolve("gradle.properties"),
				"assignmentPath=assignment\n");
		Files.setPosixFilePermissions(properties, PosixFilePermissions.fromString("---------"));
		try {
			assumeUnreadable(properties);
			assertThrows(IllegalStateException.class, () -> ProjectSourcesFinder.discover(temporaryDirectory));
		} finally {
			Files.setPosixFilePermissions(properties, PosixFilePermissions.fromString("rw-r--r--"));
		}
	}

	// </editor-fold>

	// <editor-fold desc="Fallbacks and parsing detail">

	@Test
	@DisplayName("Falls back to the conventional roots when the descriptor names none")
	void fallsBackToConventionalRoots() throws IOException {
		Files.createDirectories(temporaryDirectory.resolve("src/main/java"));
		Files.createDirectories(temporaryDirectory.resolve("src/test/java"));
		Files.writeString(temporaryDirectory.resolve("build.gradle"), "plugins { id 'java' }");

		var configuration = ProjectSourcesFinder.discover(temporaryDirectory);

		assertEquals(temporaryDirectory.resolve("src/main/java").toRealPath(),
				configuration.productionSourceRoots().get(0));
		assertEquals(temporaryDirectory.resolve("src/test/java").toRealPath(), configuration.testSourceRoots().get(0));
	}

	@Test
	@DisplayName("Ignores a blank Maven source directory and takes the conventional root")
	void ignoresBlankMavenSourceDirectory() throws IOException {
		Files.createDirectories(temporaryDirectory.resolve("src/main/java"));
		Files.createDirectories(temporaryDirectory.resolve("src/test/java"));
		Files.writeString(temporaryDirectory.resolve("pom.xml"), """
				<project><build><sourceDirectory>   </sourceDirectory>
				<testSourceDirectory></testSourceDirectory></build></project>
				""");

		var configuration = ProjectSourcesFinder.discover(temporaryDirectory);

		assertEquals(temporaryDirectory.resolve("src/main/java").toRealPath(),
				configuration.productionSourceRoots().get(0));
	}

	@Test
	@DisplayName("Reads a Kotlin source set opened with getByName, and strips line comments")
	void readsKotlinSourceSetAndStripsComments() throws IOException {
		Files.createDirectories(temporaryDirectory.resolve("assignment"));
		Files.createDirectories(temporaryDirectory.resolve("checks"));
		Files.writeString(temporaryDirectory.resolve("build.gradle.kts"), """
				sourceSets {
				  getByName("main") {
				    java { srcDir("assignment") } // srcDir 'ignored-because-commented-out'
				  }
				  getByName("test") {
				    java { srcDir("checks") }
				  }
				}
				""");

		var configuration = ProjectSourcesFinder.discover(temporaryDirectory);

		assertEquals(temporaryDirectory.resolve("assignment").toRealPath(),
				configuration.productionSourceRoots().get(0));
		assertEquals(1, configuration.productionSourceRoots().size(),
				"the commented-out srcDir must not contribute a root");
		assertEquals(temporaryDirectory.resolve("checks").toRealPath(), configuration.testSourceRoots().get(0));
	}

	@Test
	@DisplayName("Unwraps a files(...) entry")
	void unwrapsFilesEntry() throws IOException {
		Files.createDirectories(temporaryDirectory.resolve("wrapped"));
		Files.writeString(temporaryDirectory.resolve("build.gradle"), """
				sourceSets {
				  main { java { srcDirs = [files('wrapped')] } }
				}
				""");

		var configuration = ProjectSourcesFinder.discover(temporaryDirectory);

		assertEquals(temporaryDirectory.resolve("wrapped").toRealPath(), configuration.productionSourceRoots().get(0));
	}

	@Test
	@DisplayName("Accepts both quotation styles in one list")
	void acceptsBothQuotationStyles() throws IOException {
		Files.createDirectories(temporaryDirectory.resolve("single"));
		Files.createDirectories(temporaryDirectory.resolve("double"));
		Files.writeString(temporaryDirectory.resolve("build.gradle"), """
				sourceSets {
				  main { java { srcDirs = ['single', "double"] } }
				}
				""");

		var configuration = ProjectSourcesFinder.discover(temporaryDirectory);

		assertEquals(2, configuration.productionSourceRoots().size());
		assertEquals(temporaryDirectory.resolve("double").toRealPath(), configuration.productionSourceRoots().get(0));
		assertEquals(temporaryDirectory.resolve("single").toRealPath(), configuration.productionSourceRoots().get(1));
	}

	@Test
	@DisplayName("Skips comments and malformed entries in gradle.properties, and drops unresolvable tokens")
	void skipsCommentsAndMalformedGradleProperties() throws IOException {
		Files.createDirectories(temporaryDirectory.resolve("assignment"));
		Files.createDirectories(temporaryDirectory.resolve("src/main/java"));
		Files.writeString(temporaryDirectory.resolve("gradle.properties"), """
				# assignmentPath=commented-out
				a-line-without-a-separator
				=leading-separator-only
				assignmentPath=assignment
				""");
		Files.writeString(temporaryDirectory.resolve("build.gradle"), """
				sourceSets {
				  main { java { srcDirs = [assignmentPath, unknownProperty] } }
				}
				""");

		var configuration = ProjectSourcesFinder.discover(temporaryDirectory);

		assertEquals(1, configuration.productionSourceRoots().size(),
				"the unresolvable token must be dropped rather than guessed");
		assertEquals(temporaryDirectory.resolve("assignment").toRealPath(),
				configuration.productionSourceRoots().get(0));
	}

	@Test
	@DisplayName("Accepts a relative Maven source directory")
	void acceptsRelativeMavenSourceDirectory() throws IOException {
		Files.createDirectories(temporaryDirectory.resolve("assignment"));
		Files.writeString(temporaryDirectory.resolve("pom.xml"),
				"<project><build><sourceDirectory>assignment</sourceDirectory></build></project>");

		var configuration = ProjectSourcesFinder.discover(temporaryDirectory);

		assertEquals(temporaryDirectory.resolve("assignment").toRealPath(),
				configuration.productionSourceRoots().get(0));
	}

	@Test
	@DisplayName("Leaves the roots empty when neither the descriptor nor the convention offers one")
	void leavesRootsEmptyWithoutDescriptorOrConvention() throws IOException {
		Files.writeString(temporaryDirectory.resolve("pom.xml"), "<project/>");

		var configuration = ProjectSourcesFinder.discover(temporaryDirectory);

		assertTrue(configuration.productionSourceRoots().isEmpty());
		assertTrue(configuration.testSourceRoots().isEmpty());
	}

	@Test
	@DisplayName("Treats a mismatched pair of quotation marks as unresolvable rather than as a literal")
	void treatsMismatchedQuotationAsUnresolvable() throws IOException {
		Files.createDirectories(temporaryDirectory.resolve("src/main/java"));
		Files.writeString(temporaryDirectory.resolve("build.gradle"), """
				sourceSets {
				  main { java { srcDirs = ["mismatched', 'other"] } }
				}
				""");

		var configuration = ProjectSourcesFinder.discover(temporaryDirectory);

		assertEquals(temporaryDirectory.resolve("src/main/java").toRealPath(),
				configuration.productionSourceRoots().get(0),
				"neither half-quoted token resolves, so the conventional root applies");
	}

	@Test
	@DisplayName("Reports no legacy source path when the project has neither descriptor")
	void reportsNoLegacySourcePathWithoutAnyDescriptor() {
		ProjectSourcesFinder.setPomXmlPath(temporaryDirectory.resolve("absent-pom.xml").toString());
		ProjectSourcesFinder.setBuildGradlePath(temporaryDirectory.resolve("absent-build.gradle").toString());

		assertEquals(Optional.empty(), ProjectSourcesFinder.findProjectSourcesPath());
	}

	@Test
	@DisplayName("Accepts an absolute source root inside the project")
	void acceptsAbsoluteSourceRootInsideTheProject() throws IOException {
		Path absolute = Files.createDirectories(temporaryDirectory.resolve("assignment"));
		Files.writeString(temporaryDirectory.resolve("build.gradle"),
				"sourceSets { main { java { srcDir '" + absolute + "' } } }");

		var configuration = ProjectSourcesFinder.discover(temporaryDirectory);

		assertEquals(absolute.toRealPath(), configuration.productionSourceRoots().get(0));
	}

	// </editor-fold>

	// <editor-fold desc="Legacy descriptor accessors">

	@Test
	@DisplayName("Reports no Maven project when the configured descriptor path is null or absent")
	void reportsNoMavenProjectWithoutADescriptor() {
		ProjectSourcesFinder.setPomXmlPath(null);
		assertFalse(ProjectSourcesFinder.isMavenProject());

		ProjectSourcesFinder.setPomXmlPath(temporaryDirectory.resolve("absent-pom.xml").toString());
		assertFalse(ProjectSourcesFinder.isMavenProject());
	}

	@Test
	@DisplayName("Reports no Gradle project when the configured descriptor path is null, and accepts the Kotlin one")
	void reportsGradleProjectFromEitherDescriptor() throws IOException {
		ProjectSourcesFinder.setBuildGradlePath(null);
		assertFalse(ProjectSourcesFinder.isGradleProject());

		Path groovy = temporaryDirectory.resolve("build.gradle");
		ProjectSourcesFinder.setBuildGradlePath(groovy.toString());
		assertFalse(ProjectSourcesFinder.isGradleProject());

		Files.writeString(temporaryDirectory.resolve("build.gradle.kts"), "plugins { id(\"java\") }");
		assertTrue(ProjectSourcesFinder.isGradleProject(), "the Kotlin descriptor must be recognised too");
	}

	@Test
	@DisplayName("Reads the legacy Gradle source directory, and only that property")
	void readsLegacyGradleSourceDirectory() throws IOException {
		Path descriptor = temporaryDirectory.resolve("build.gradle");
		Files.writeString(descriptor, """
				def someOtherProperty = "not-the-one"
				def assignmentSrcDir = "assignment/src"
				""");
		ProjectSourcesFinder.setBuildGradlePath(descriptor.toString());
		ProjectSourcesFinder.setPomXmlPath(temporaryDirectory.resolve("absent-pom.xml").toString());

		assertEquals(Optional.of(Path.of("assignment/src")), ProjectSourcesFinder.findProjectSourcesPath());
	}

	@Test
	@DisplayName("Reports no legacy Gradle source directory when the descriptor names none or cannot be read")
	void reportsNoLegacyGradleSourceDirectory() throws IOException {
		Path descriptor = Files.writeString(temporaryDirectory.resolve("build.gradle"), "plugins { id 'java' }\n");
		ProjectSourcesFinder.setBuildGradlePath(descriptor.toString());
		ProjectSourcesFinder.setPomXmlPath(temporaryDirectory.resolve("absent-pom.xml").toString());
		assertEquals(Optional.empty(), ProjectSourcesFinder.findProjectSourcesPath());

		assumePosix();
		Files.setPosixFilePermissions(descriptor, PosixFilePermissions.fromString("---------"));
		try {
			assumeUnreadable(descriptor);
			assertEquals(Optional.empty(), ProjectSourcesFinder.findProjectSourcesPath(),
					"an unreadable descriptor is reported, not thrown");
		} finally {
			Files.setPosixFilePermissions(descriptor, PosixFilePermissions.fromString("rw-r--r--"));
		}
	}

	@Test
	@DisplayName("Reads the legacy Maven source directory and substitutes both base-directory prefixes")
	void readsLegacyMavenSourceDirectory() throws IOException {
		Path descriptor = temporaryDirectory.resolve("pom.xml");
		ProjectSourcesFinder.setPomXmlPath(descriptor.toString());

		Files.writeString(descriptor,
				"<project><build><sourceDirectory>${project.basedir}/assignment</sourceDirectory></build></project>");
		assertEquals(Optional.of(Path.of("assignment")), ProjectSourcesFinder.findProjectSourcesPath());

		Files.writeString(descriptor,
				"<project><build><sourceDirectory>${basedir}/assignment</sourceDirectory></build></project>");
		assertEquals(Optional.of(Path.of("assignment")), ProjectSourcesFinder.findProjectSourcesPath());

		Files.writeString(descriptor,
				"<project><build><sourceDirectory>plain/assignment</sourceDirectory></build></project>");
		assertEquals(Optional.of(Path.of("plain/assignment")), ProjectSourcesFinder.findProjectSourcesPath());
	}

	@Test
	@DisplayName("Reports an unusable legacy Maven descriptor rather than failing")
	void reportsUnusableLegacyMavenDescriptor() throws IOException {
		Path descriptor = temporaryDirectory.resolve("pom.xml");
		ProjectSourcesFinder.setPomXmlPath(descriptor.toString());

		Files.writeString(descriptor, "<project><build><sourceDirectory>  </sourceDirectory></build></project>");
		assertEquals(Optional.of(Path.of(conventionalProductionSource())),
				ProjectSourcesFinder.findProjectSourcesPath(),
				"a blank source directory falls through to the conventional root");

		Files.writeString(descriptor, "<project><build>");
		assertEquals(Optional.empty(), ProjectSourcesFinder.findProjectSourcesPath(),
				"a malformed descriptor is reported, not thrown");
	}

	// </editor-fold>

	private static String conventionalProductionSource() {
		return "src/main/java";
	}

	private static void assumePosix() {
		assumeTrue(FileSystems.getDefault().supportedFileAttributeViews().contains("posix"),
				"needs POSIX permissions to make a file unreadable");
	}

	/**
	 * Permission bits do not bind a superuser, so clearing them is not by itself a
	 * denial. Without this check such a run would report a failure of the code
	 * under test where the fixture is what could not be established.
	 */
	private static void assumeUnreadable(Path path) {
		assumeTrue(!Files.isReadable(path), () -> "cannot make " + path + " unreadable in this environment");
	}
}
