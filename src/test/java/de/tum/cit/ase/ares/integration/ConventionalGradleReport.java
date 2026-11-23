package de.tum.cit.ase.ares.integration;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.List;

/**
 * The JVM arguments {@code GradleSourceRoots.gradle} would hand the tests of a
 * Gradle project with the conventional folders, for test fixtures whose
 * hand-written builds stand for such a project but cannot apply the snippet.
 */
public final class ConventionalGradleReport {

	/** Not instantiable: it only offers static methods. */
	private ConventionalGradleReport() {
	}

	/**
	 * The report: {@code src/main/java} into {@code build/classes/java/main} and
	 * {@code src/test/java} into {@code build/classes/java/test}.
	 *
	 * @param project the project directory
	 * @return the JVM arguments, each {@code -Dname=value}
	 */
	public static List<String> arguments(Path project) {
		Path root = project.toAbsolutePath().normalize();
		return List.of("-Dares.gradle.sourceRoots.version=1", "-Dares.gradle.projectDir=" + encode(root),
				"-Dares.gradle.testSet=test", "-Dares.gradle.testSourceRoots=" + encode(root.resolve("src/test/java")),
				"-Dares.gradle.testOutputRoot=" + encode(root.resolve("build/classes/java/test")),
				"-Dares.gradle.mainSourceRoots=" + encode(root.resolve("src/main/java")),
				"-Dares.gradle.mainOutputRoot=" + encode(root.resolve("build/classes/java/main")));
	}

	/** A path as the snippet writes it, percent-encoded. */
	private static String encode(Path path) {
		return URLEncoder.encode(path.toString(), StandardCharsets.UTF_8);
	}
}
