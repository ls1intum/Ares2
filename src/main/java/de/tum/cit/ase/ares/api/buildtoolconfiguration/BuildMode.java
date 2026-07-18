package de.tum.cit.ase.ares.api.buildtoolconfiguration;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Enum representing the Java build tools supported by Ares.
 * <p>
 * This enum provides two modes:
 * </p>
 * <ul>
 * <li>{@link #MAVEN} - A mode using Maven for running Ares.</li>
 * <li>{@link #GRADLE} - A mode using Gradle for running Ares.</li>
 * </ul>
 */
public enum BuildMode {

	/**
	 * Maven mode.
	 * <p>
	 * In this mode, Maven is used to run Ares.
	 * </p>
	 */
	MAVEN,

	/**
	 * Gradle mode.
	 * <p>
	 * In this mode, Gradle is used to run Ares.
	 * </p>
	 */
	GRADLE;

	private static final Logger LOG = LoggerFactory.getLogger(BuildMode.class);

	public String[] fileName() {
		return switch (this) {
		case MAVEN -> new String[] { "pom.xml" };
		case GRADLE -> new String[] { "build.gradle", "build.gradle.kts" };
		};
	}

	// <editor-fold desc="Other methods">
	public String getBuildDirectory() {
		return switch (this) {
		case MAVEN -> "target/classes";
		case GRADLE -> "build/classes/java/main";
		};
	}

	public String getTestBuildDirectory() {
		return switch (this) {
		case MAVEN -> "target/test-classes";
		case GRADLE -> "build/classes/java/test";
		};
	}

	/**
	 * Computes the classpath for class loading based on the project path and
	 * package name.
	 * <p>
	 * The projectPath (from @Policy withinPath) can have different formats:
	 * <ul>
	 * <li>null or empty - uses packageName to construct the classpath</li>
	 * <li>"classes/java/main/package/..." - Gradle-style bytecode path, converted
	 * to actual build path</li>
	 * <li>"classes/package/..." - Maven-style bytecode path, converted to actual
	 * build path</li>
	 * <li>"test-classes/..." - Test classes path (handled similarly)</li>
	 * </ul>
	 *
	 * @param projectPath the path specified in @Policy withinPath (can be null or
	 *                    empty)
	 * @param packageName the package name from the security policy (e.g.,
	 *                    "anonymous" or "de.tum.cit.ase")
	 * @return the actual filesystem classpath for class loading
	 */
	public String getClasspath(Path projectPath, String packageName) {
		// If projectPath is null or empty, use packageName to construct the classpath
		if (projectPath == null || projectPath.toString().isEmpty()) {
			if (packageName != null && !packageName.isEmpty()) {
				// Convert package name to path: "anonymous" -> "anonymous", "de.tum.cit" ->
				// "de/tum/cit"
				String packagePath = packageName.replace(".", "/");
				return reportClasspath(Paths.get(getBuildDirectory(), packagePath));
			}
			return reportClasspath(Path.of(getBuildDirectory()));
		}

		String projectPathStr = projectPath.toString();

		// Handle bytecode paths that start with "classes/" or "test-classes/"
		// These need to be converted to actual build tool paths
		if (projectPathStr.startsWith("classes/java/main/")) {
			// Gradle-style path: classes/java/main/package/subpackage
			// Extract package path and prepend actual build directory
			String extractedPackagePath = projectPathStr.substring("classes/java/main/".length());
			return reportClasspath(Paths.get(getBuildDirectory(), extractedPackagePath));
		} else if (projectPathStr.startsWith("classes/")) {
			// Maven-style path: classes/package/subpackage
			// Extract package path and prepend actual build directory
			String extractedPackagePath = projectPathStr.substring("classes/".length());
			return reportClasspath(Paths.get(getBuildDirectory(), extractedPackagePath));
		} else if (projectPathStr.startsWith("test-classes/java/test/")) {
			// Gradle test classes
			String extractedPackagePath = projectPathStr.substring("test-classes/java/test/".length());
			return reportClasspath(Paths.get(getTestBuildDirectory(), extractedPackagePath));
		} else if (projectPathStr.startsWith("test-classes/")) {
			// Maven test classes
			String extractedPackagePath = projectPathStr.substring("test-classes/".length());
			return reportClasspath(Paths.get(getTestBuildDirectory(), extractedPackagePath));
		}

		// Otherwise, assume it's a package path and combine with build directory
		return reportClasspath(Paths.get(getBuildDirectory(), projectPathStr));
	}

	private String reportClasspath(Path path) {
		String resolved = path.toString();
		LOG.info("Resolved Ares analysis/import path for {}: {}", this, resolved);
		return resolved;
	}
	// </editor-fold>
}
