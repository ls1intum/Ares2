package de.tum.cit.ase.ares.api.buildtoolconfiguration;

import java.nio.file.Path;
import java.nio.file.Paths;

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

	public String[] threePartedFileHeader() {
		return switch (this) {
		case MAVEN -> new String[] { "templates", "java", "maven", "pomHeader.txt" };
		case GRADLE -> new String[] { "templates", "java", "gradle", "buildHeader.txt" };
		};
	}

	public String[] threePartedFileFooter() {
		return switch (this) {
		case MAVEN -> new String[] { "templates", "java", "maven", "pomFooter.txt" };
		case GRADLE -> new String[] { "templates", "java", "gradle", "buildFooter.txt" };
		};
	}

	public String[] fileName() {
		return switch (this) {
		case MAVEN -> new String[] { "pom.xml" };
		case GRADLE -> new String[] { "build.gradle" };
		};
	}

	// <editor-fold desc="Other methods">
	public String getBuildDirectory() {
		return switch (this) {
		case MAVEN -> "target/classes";
		case GRADLE -> "build/classes/java/main";
		};
	}

	/**
	 * Computes the classpath for class loading based on the project path and package name.
	 * <p>
	 * The projectPath (from @Policy withinPath) can have different formats:
	 * <ul>
	 *   <li>null or empty - uses packageName to construct the classpath</li>
	 *   <li>"classes/java/main/package/..." - Gradle-style bytecode path, converted to actual build path</li>
	 *   <li>"classes/package/..." - Maven-style bytecode path, converted to actual build path</li>
	 *   <li>"test-classes/..." - Test classes path (handled similarly)</li>
	 * </ul>
	 *
	 * @param projectPath the path specified in @Policy withinPath (can be null or empty)
	 * @param packageName the package name from the security policy (e.g., "anonymous" or "de.tum.cit.ase")
	 * @return the actual filesystem classpath for class loading
	 */
	public String getClasspath(Path projectPath, String packageName) {
		// If projectPath is null or empty, use packageName to construct the classpath
		if (projectPath == null || projectPath.toString().isEmpty()) {
			if (packageName != null && !packageName.isEmpty()) {
				// Convert package name to path: "anonymous" -> "anonymous", "de.tum.cit" -> "de/tum/cit"
				String packagePath = packageName.replace(".", "/");
				return Paths.get(getBuildDirectory(), packagePath).toString();
			}
			return getBuildDirectory();
		}
		
		String projectPathStr = projectPath.toString();
		
		// Handle bytecode paths that start with "classes/" or "test-classes/"
		// These need to be converted to actual build tool paths
		if (projectPathStr.startsWith("classes/java/main/")) {
			// Gradle-style path: classes/java/main/package/subpackage
			// Extract package path and prepend actual build directory
			String extractedPackagePath = projectPathStr.substring("classes/java/main/".length());
			return Paths.get(getBuildDirectory(), extractedPackagePath).toString();
		} else if (projectPathStr.startsWith("classes/")) {
			// Maven-style path: classes/package/subpackage  
			// Extract package path and prepend actual build directory
			String extractedPackagePath = projectPathStr.substring("classes/".length());
			return Paths.get(getBuildDirectory(), extractedPackagePath).toString();
		} else if (projectPathStr.startsWith("test-classes/java/test/")) {
			// Gradle test classes
			String extractedPackagePath = projectPathStr.substring("test-classes/java/test/".length());
			String testBuildDir = switch (this) {
				case MAVEN -> "target/test-classes";
				case GRADLE -> "build/classes/java/test";
			};
			return Paths.get(testBuildDir, extractedPackagePath).toString();
		} else if (projectPathStr.startsWith("test-classes/")) {
			// Maven test classes
			String extractedPackagePath = projectPathStr.substring("test-classes/".length());
			String testBuildDir = switch (this) {
				case MAVEN -> "target/test-classes";
				case GRADLE -> "build/classes/java/test";
			};
			return Paths.get(testBuildDir, extractedPackagePath).toString();
		}
		
		// Otherwise, assume it's a package path and combine with build directory
		return Paths.get(getBuildDirectory(), projectPathStr).toString();
	}
	// </editor-fold>
}
