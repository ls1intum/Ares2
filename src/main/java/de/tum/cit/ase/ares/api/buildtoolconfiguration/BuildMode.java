package de.tum.cit.ase.ares.api.buildtoolconfiguration;

import java.nio.file.Path;
import java.util.regex.Pattern;

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
	 * A Windows drive such as {@code C:}, which no bytecode path can begin with.
	 */
	private static final Pattern DRIVE_PREFIX = Pattern.compile("[A-Za-z]:");

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
	 * Resolves a bytecode scope against this mode's conventional output roots.
	 *
	 * @param projectPath the optional bytecode-relative scope
	 * @param packageName the fallback package name
	 * @return the filesystem classpath for class loading
	 * @throws SecurityException if the scope, or the package path used without one,
	 *                           starts at a root or a drive
	 */
	public String getClasspath(Path projectPath, String packageName) {
		return reportClasspath(resolveClasspath(projectPath, packageName, Path.of(getBuildDirectory()),
				Path.of(getTestBuildDirectory())));
	}

	/**
	 * Maps either separator form to output roots using whole path components.
	 */
	static Path resolveClasspath(Path withinPath, String packageName, Path productionOutputRoot, Path testOutputRoot) {
		if (withinPath == null || withinPath.toString().isEmpty()) {
			return productionOutputRoot
					.resolve(relativeScope(packageName == null ? "" : packageName.replace('.', '/')));
		}
		Path configured = relativeScope(withinPath.toString());
		Path base = productionOutputRoot;
		Path prefix;
		if (configured.startsWith("classes/java/main")) {
			prefix = Path.of("classes/java/main");
		} else if (configured.startsWith("classes")) {
			prefix = Path.of("classes");
		} else if (configured.startsWith("test-classes/java/test")) {
			base = testOutputRoot;
			prefix = Path.of("test-classes/java/test");
		} else if (configured.startsWith("test-classes")) {
			base = testOutputRoot;
			prefix = Path.of("test-classes");
		} else {
			return base.resolve(configured);
		}
		return base.resolve(prefix.relativize(configured));
	}

	/**
	 * Reads a scope or package path with either separator and refuses one that
	 * starts at a root or a drive, on every host, since resolving it would replace
	 * the output root. The text checks run first, so such a path is refused before
	 * the host gets to parse it.
	 */
	private static Path relativeScope(String original) {
		String scope = original.replace('\\', '/');
		if (scope.startsWith("/") || DRIVE_PREFIX.matcher(scope).lookingAt() || Path.of(scope).getRoot() != null) {
			throw new SecurityException("Bytecode scope must be relative to the build output: " + original);
		}
		return Path.of(scope);
	}

	/** Logs and returns a classpath in the host filesystem's format. */
	private String reportClasspath(Path path) {
		String resolved = path.toString();
		LOG.info("Resolved Ares analysis/import path for {}: {}", this, resolved);
		return resolved;
	}
	// </editor-fold>
}
