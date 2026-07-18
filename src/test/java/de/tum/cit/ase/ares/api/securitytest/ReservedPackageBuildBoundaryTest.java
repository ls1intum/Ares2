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

	private boolean matches(String pattern, String classFile) {
		return classFile.startsWith(pattern.substring(0, pattern.length() - 2));
	}
}
