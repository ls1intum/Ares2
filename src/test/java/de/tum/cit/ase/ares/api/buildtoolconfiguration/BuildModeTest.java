package de.tum.cit.ase.ares.api.buildtoolconfiguration;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.file.Path;

import org.junit.jupiter.api.Test;

class BuildModeTest {
	@Test
	void exposesSupportedBuildFilesAndOutputRoots() {
		assertArrayEquals(new String[] { "pom.xml" }, BuildMode.MAVEN.fileName());
		assertArrayEquals(new String[] { "build.gradle", "build.gradle.kts" }, BuildMode.GRADLE.fileName());
		assertEquals("target/classes", BuildMode.MAVEN.getBuildDirectory());
		assertEquals("target/test-classes", BuildMode.MAVEN.getTestBuildDirectory());
		assertEquals("build/classes/java/main", BuildMode.GRADLE.getBuildDirectory());
		assertEquals("build/classes/java/test", BuildMode.GRADLE.getTestBuildDirectory());
	}

	@Test
	void mapsEverySupportedWithinPathPrefix() {
		assertEquals(Path.of("target/classes/a/b").toString(),
				BuildMode.MAVEN.getClasspath(Path.of("classes/a/b"), "ignored"));
		assertEquals(Path.of("build/classes/java/main/a/b").toString(),
				BuildMode.GRADLE.getClasspath(Path.of("classes/java/main/a/b"), "ignored"));
		assertEquals(Path.of("target/test-classes/a/b").toString(),
				BuildMode.MAVEN.getClasspath(Path.of("test-classes/a/b"), "ignored"));
		assertEquals(Path.of("build/classes/java/test/a/b").toString(),
				BuildMode.GRADLE.getClasspath(Path.of("test-classes/java/test/a/b"), "ignored"));
		assertEquals(Path.of("target/classes/de/example").toString(), BuildMode.MAVEN.getClasspath(null, "de.example"));
		assertEquals(Path.of("build/classes/java/main").toString(), BuildMode.GRADLE.getClasspath(Path.of(""), null));
	}

	@Test
	void documentsTrustedTraversalMappingWithoutClaimingConfinement() {
		assertEquals(Path.of("target/classes/../outside").toString(),
				BuildMode.MAVEN.getClasspath(Path.of("classes/../outside"), "ignored"));
		assertEquals(Path.of("build/classes/java/test/../outside").toString(),
				BuildMode.GRADLE.getClasspath(Path.of("test-classes/../outside"), "ignored"));
		assertEquals(Path.of("target/classes/a/b").toString(),
				BuildMode.MAVEN.getClasspath(Path.of("classes//a///b"), "ignored"));
	}
}
