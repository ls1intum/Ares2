package de.tum.cit.ase.ares.api.securitytest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

import de.tum.cit.ase.ares.api.architecture.java.wala.WalaPathClassification;
import de.tum.cit.ase.ares.api.util.FileTools;

class ReservedPackageBuildBoundaryTest {
	private static final Path ROOT = FileTools.resolveFileOnSourceDirectory("configuration", "reservedPackages");

	@Test
	void versionedMavenAndGradleFixturesRejectEveryReservedPrefix() throws Exception {
		Path patternsFile = ROOT.resolve("ReservedPackagePrefixes.txt");
		List<String> lines = Files.readAllLines(patternsFile);
		assertTrue(lines.contains("# version=" + WalaPathClassification.RESERVED_PACKAGE_PREFIX_VERSION));
		assertTrue(
				lines.contains("# boundary-version=" + WalaPathClassification.RESERVED_PACKAGE_BUILD_BOUNDARY_VERSION));
		List<String> patterns = lines.stream().filter(line -> !line.isBlank() && !line.startsWith("#")).toList();
		List<String> expected = WalaPathClassification.RESERVED_PACKAGE_PREFIXES.stream()
				.map(prefix -> prefix.substring(0, prefix.length() - 1).replace('.', '/') + "/**").toList();
		assertEquals(expected, patterns);

		String maven = Files.readString(ROOT.resolve("MavenReservedPackages.xml"));
		String gradle = Files.readString(ROOT.resolve("GradleReservedPackages.gradle"));
		for (String pattern : patterns) {
			assertTrue(maven.contains(pattern), () -> "Maven fixture misses " + pattern);
			assertTrue(gradle.contains("'" + pattern + "'"), () -> "Gradle fixture misses " + pattern);
			String syntheticClass = pattern.replace("/**", "/Student.class");
			assertTrue(matches(pattern, syntheticClass), () -> "Fixture does not reject " + syntheticClass);
		}
		assertTrue(maven.contains("No bypass flag is supported"));
		assertTrue(gradle.contains("No bypass flag is supported"));
	}

	@Test
	void gradleFixtureGatesEveryTestTaskAndNotOnlyCheck() throws Exception {
		String gradle = Files.readString(ROOT.resolve("GradleReservedPackages.gradle"));
		String task = "verifyAresReservedPackagesV" + WalaPathClassification.RESERVED_PACKAGE_BUILD_BOUNDARY_VERSION;
		assertTrue(gradle.contains("tasks.register('" + task + "')"), () -> "Gradle fixture does not declare " + task);
		// The defect boundary version 2 exists to fix: check.dependsOn test, not the
		// reverse, so hanging the validation off `check` alone left `gradlew test`
		// - which is what a grading run invokes - completely ungated.
		assertTrue(gradle.contains("tasks.withType(Test).configureEach { dependsOn tasks.named('" + task + "') }"),
				"Gradle fixture must gate every Test task, or `gradlew test` skips the boundary");
		assertTrue(gradle.contains("tasks.named('check') { dependsOn tasks.named('" + task + "') }"),
				"Gradle fixture must still gate `check`, which covers `gradlew build`");
		assertTrue(gradle.contains("import org.gradle.api.tasks.testing.Test"),
				"the snippet is copied into foreign builds, so the Test type must be imported explicitly");
	}

	@Test
	void bothFixturesNameTheSameBoundaryVersion() throws Exception {
		String version = WalaPathClassification.RESERVED_PACKAGE_BUILD_BOUNDARY_VERSION;
		String maven = Files.readString(ROOT.resolve("MavenReservedPackages.xml"));
		String gradle = Files.readString(ROOT.resolve("GradleReservedPackages.gradle"));
		assertTrue(maven.contains("verify-ares-reserved-packages-v" + version));
		assertTrue(maven.contains("Ares reserved-package validation " + version + " rejected"));
		assertTrue(gradle.contains("def aresReservedPackageBoundaryVersion = '" + version + "'"));
	}

	/**
	 * Both scripts reject the files that would plug student code into the test run:
	 * every service file, and JUnit's and ArchUnit's settings at the root.
	 *
	 * @throws Exception if a script cannot be read
	 */
	@Test
	void bothFixturesRejectServiceAndFrameworkConfigurationFiles() throws Exception {
		String maven = Files.readString(ROOT.resolve("MavenReservedPackages.xml"));
		String gradle = Files.readString(ROOT.resolve("GradleReservedPackages.gradle"));
		for (String file : List.of("META-INF/services/**", "junit-platform.properties", "archunit.properties")) {
			assertTrue(maven.contains("<filename name=\"" + file + "\" casesensitive=\"false\"/>"),
					() -> "Maven fixture misses " + file);
		}
		assertTrue(gradle.contains("'META-INF/services/'"), "Gradle fixture misses the service files");
		assertTrue(gradle.contains("'junit-platform.properties', 'archunit.properties'"),
				"Gradle fixture misses the root configuration files");
		assertTrue(maven.contains("${ares.reserved.package.files}"), "Maven fixture must name the rejected files");
	}

	/**
	 * Both fixtures match reserved files in any letter case. Maven needs filename
	 * selectors for that: an include follows its fixed leading folders in one
	 * spelling only, so {@code meta-inf/} beside {@code META-INF/} went unchecked.
	 *
	 * @throws Exception if a script cannot be read
	 */
	@Test
	void bothFixturesMatchReservedFilesInAnyLetterCase() throws Exception {
		String maven = Files.readString(ROOT.resolve("MavenReservedPackages.xml"));
		String gradle = Files.readString(ROOT.resolve("GradleReservedPackages.gradle")).replace("\r\n", "\n");
		assertTrue(maven.contains("dir=\"${project.build.outputDirectory}\" defaultexcludes=\"no\">"),
				"Maven fixture must not skip folders such as CVS or .git");
		assertFalse(maven.contains("<include "), "an include misses differently cased sibling folders");
		assertEquals(maven.split("<filename ", -1).length - 1, maven.split("casesensitive=\"false\"/>", -1).length - 1,
				"every Maven filename selector must ignore letter case");
		assertTrue(gradle.contains(".collect { it.toLowerCase(Locale.ROOT) }"),
				"Gradle fixture must lower-case the reserved prefixes and root files");
		assertTrue(gradle.contains("def comparable = relative.toLowerCase(Locale.ROOT)"),
				"Gradle fixture must lower-case the path it compares");
	}

	/**
	 * The Gradle script scans the whole student output, resources included, since
	 * service and configuration files are resources rather than classes.
	 *
	 * @throws Exception if the script cannot be read
	 */
	@Test
	void gradleFixtureScansTheWholeMainOutput() throws Exception {
		String gradle = Files.readString(ROOT.resolve("GradleReservedPackages.gradle"));
		assertTrue(
				gradle.contains("def studentOutputDirs = sourceSets.main.output\n")
						|| gradle.contains("def studentOutputDirs = sourceSets.main.output\r\n"),
				"Gradle fixture must scan sourceSets.main.output, not only its class directories");
		assertTrue(!gradle.contains("sourceSets.main.output.classesDirs"),
				"scanning only the class directories misses student resources");
	}

	/**
	 * The examples carry copies of the shipped scripts; they must match, so CI
	 * tests the guard that ships.
	 *
	 * @throws Exception if a file cannot be read
	 */
	@Test
	void theExamplesCarryTheShippedScripts() throws Exception {
		String gradle = Files.readString(ROOT.resolve("GradleReservedPackages.gradle")).replace("\r\n", "\n");
		String exampleGradle = Files
				.readString(Path.of("examples/ares-exercise-gradle/gradle/AresReservedPackages.gradle"))
				.replace("\r\n", "\n");
		assertEquals(gradle, exampleGradle);
		String examplePom = Files.readString(Path.of("examples/ares-exercise-maven/pom.xml"));
		assertTrue(examplePom.contains(
				"verify-ares-reserved-packages-v" + WalaPathClassification.RESERVED_PACKAGE_BUILD_BOUNDARY_VERSION));
		String maven = Files.readString(ROOT.resolve("MavenReservedPackages.xml"));
		assertEquals(reservedFileset(maven), reservedFileset(examplePom));
	}

	/**
	 * Cuts the Maven fileset that lists the reserved files out of a script, with
	 * indentation and line endings removed, so two copies compare by content.
	 *
	 * @param script the Maven script or pom to read
	 * @return the fileset, one trimmed line after another
	 */
	private String reservedFileset(String script) {
		int start = script.indexOf("<fileset id=\"ares.reserved.files\"");
		int end = script.indexOf("</fileset>", start);
		assertTrue(start >= 0 && end > start, "no reserved-file fileset found");
		return script.substring(start, end).lines().map(String::strip).collect(Collectors.joining("\n"));
	}

	private boolean matches(String pattern, String classFile) {
		return classFile.startsWith(pattern.substring(0, pattern.length() - 2));
	}
}
