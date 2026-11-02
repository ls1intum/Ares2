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

	/**
	 * Returns the supported build-descriptor filenames.
	 *
	 * @return the descriptor filenames for this build mode
	 */
	public String[] fileName() {
		return switch (this) {
		case MAVEN -> new String[] { "pom.xml" };
		case GRADLE -> new String[] { "build.gradle", "build.gradle.kts" };
		};
	}

	// <editor-fold desc="Other methods">
	/**
	 * Returns the production bytecode directory.
	 *
	 * @return the build-tool-relative production directory
	 */
	public String getBuildDirectory() {
		return switch (this) {
		case MAVEN -> "target/classes";
		case GRADLE -> "build/classes/java/main";
		};
	}

	/**
	 * Returns the test bytecode directory.
	 *
	 * @return the build-tool-relative test directory
	 */
	public String getTestBuildDirectory() {
		return switch (this) {
		case MAVEN -> "target/test-classes";
		case GRADLE -> "build/classes/java/test";
		};
	}

	/**
	 * Turns a withinPath into the folder of compiled classes it names, below this
	 * build's production or test output. Without a withinPath the package name is
	 * used instead.
	 *
	 * @param projectPath the withinPath of the policy, such as
	 *                    "classes/java/main/org/example"; may be null or empty
	 * @param packageName the supervised package, such as "de.tum.cit.ase"
	 * @return the folder the static analysis reads
	 */
	public String getClasspath(Path projectPath, String packageName) {
		if (projectPath == null || projectPath.toString().isEmpty()) {
			if (packageName != null && !packageName.isEmpty()) {
				String packagePath = packageName.replace(".", "/");
				return reportClasspath(Paths.get(getBuildDirectory(), packagePath));
			}
			return reportClasspath(Path.of(getBuildDirectory()));
		}

		String projectPathStr = withForwardSlashes(projectPath);

		if (projectPathStr.startsWith("classes/java/main/")) {
			String extractedPackagePath = projectPathStr.substring("classes/java/main/".length());
			return reportClasspath(Paths.get(getBuildDirectory(), extractedPackagePath));
		} else if (projectPathStr.startsWith("classes/")) {
			String extractedPackagePath = projectPathStr.substring("classes/".length());
			return reportClasspath(Paths.get(getBuildDirectory(), extractedPackagePath));
		} else if (projectPathStr.startsWith("test-classes/java/test/")) {
			String extractedPackagePath = projectPathStr.substring("test-classes/java/test/".length());
			return reportClasspath(Paths.get(getTestBuildDirectory(), extractedPackagePath));
		} else if (projectPathStr.startsWith("test-classes/")) {
			String extractedPackagePath = projectPathStr.substring("test-classes/".length());
			return reportClasspath(Paths.get(getTestBuildDirectory(), extractedPackagePath));
		}

		return reportClasspath(Paths.get(getBuildDirectory(), projectPathStr));
	}

	/**
	 * Writes a withinPath with "/" between its names, so the bytecode prefixes
	 * match on Windows too. There toString() joins names with "\", no prefix
	 * matched, and the analysis was pointed at a folder that does not exist.
	 *
	 * @param withinPath the instructor's bytecode-relative scope
	 * @return the same path with "/" as its separator
	 */
	static String withForwardSlashes(Path withinPath) {
		return withinPath.toString().replace(withinPath.getFileSystem().getSeparator(), "/");
	}

	private String reportClasspath(Path path) {
		String resolved = path.toString();
		LOG.info("Resolved Ares analysis/import path for {}: {}", this, resolved);
		return resolved;
	}
	// </editor-fold>
}
