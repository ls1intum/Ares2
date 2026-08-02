package de.tum.cit.ase.ares.api.securitytest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

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

	private boolean matches(String pattern, String classFile) {
		return classFile.startsWith(pattern.substring(0, pattern.length() - 2));
	}
}
