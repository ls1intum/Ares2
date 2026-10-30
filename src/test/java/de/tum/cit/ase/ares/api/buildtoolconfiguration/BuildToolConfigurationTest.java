package de.tum.cit.ase.ares.api.buildtoolconfiguration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assumptions.abort;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;

/** Checks bytecode scopes for both build tools and both classpath APIs. */
class BuildToolConfigurationTest {

	/** The project whose output roots contain the resolved scopes. */
	@TempDir
	Path projectRoot;

	/**
	 * Both separator forms select the right output and strip the bytecode prefix.
	 */
	@ParameterizedTest
	@CsvSource({ "classes/java/main/org/example, false", "classes\\java\\main\\org\\example, false",
			"classes/java\\main/org\\example, false", "classes/org/example, false", "classes\\org\\example, false",
			"test-classes/java/test/org/example, true", "test-classes\\java\\test\\org\\example, true",
			"test-classes/org/example, true", "test-classes\\org\\example, true" })
	void resolvesBytecodePrefixes(String scope, boolean testClasses) {
		for (BuildMode mode : BuildMode.values()) {
			var configuration = configuration(mode);
			Path output = Path.of(testClasses ? mode.getTestBuildDirectory() : mode.getBuildDirectory());
			assertEquals(configuration.projectRoot().resolve(output).resolve("org/example").toString(),
					configuration.classpath(Path.of(scope), "ignored"));
			assertEquals(output.resolve("org/example").toString(), mode.getClasspath(Path.of(scope), "ignored"));
		}
	}

	/** A prefix without a package suffix selects the output root itself. */
	@ParameterizedTest
	@CsvSource({ "classes, false", "classes/java/main, false", "classes\\java\\main, false", "test-classes, true",
			"test-classes/java/test, true", "test-classes\\java\\test, true" })
	void resolvesWholeOutputRoots(String scope, boolean testClasses) {
		for (BuildMode mode : BuildMode.values()) {
			var configuration = configuration(mode);
			Path output = Path.of(testClasses ? mode.getTestBuildDirectory() : mode.getBuildDirectory());
			assertEquals(configuration.projectRoot().resolve(output).toString(),
					configuration.classpath(Path.of(scope), "ignored"));
			assertEquals(output.toString(), mode.getClasspath(Path.of(scope), "ignored"));
		}
	}

	/** Missing scopes use the package name, including a missing package name. */
	@ParameterizedTest
	@EnumSource(BuildMode.class)
	void retainsPackageFallback(BuildMode mode) {
		var configuration = configuration(mode);
		for (Path scope : new Path[] { null, Path.of("") }) {
			for (String packageName : new String[] { null, "", "org.example" }) {
				Path suffix = Path.of(packageName == null ? "" : packageName.replace('.', '/'));
				assertEquals(configuration.productionOutputRoot().resolve(suffix).toString(),
						configuration.classpath(scope, packageName));
				assertEquals(Path.of(mode.getBuildDirectory()).resolve(suffix).toString(),
						mode.getClasspath(scope, packageName));
			}
		}
	}

	/**
	 * A malformed package name that would become a rooted path is refused by both
	 * APIs on any host, instead of replacing the output root.
	 */
	@ParameterizedTest
	@ValueSource(strings = { ".hidden", ".." })
	void rejectsRootedPackageFallback(String packageName) {
		for (BuildMode mode : BuildMode.values()) {
			var configuration = configuration(mode);
			assertThrows(SecurityException.class, () -> configuration.classpath(null, packageName));
			assertThrows(SecurityException.class, () -> mode.getClasspath(null, packageName));
		}
	}

	/** Ordinary package paths and similar names are not mistaken for prefixes. */
	@ParameterizedTest
	@ValueSource(strings = { "org/example", "org\\example", "classes-extra/org/example",
			"test-classes-extra/org/example" })
	void retainsUnprefixedScopes(String scope) {
		var configuration = configuration(BuildMode.GRADLE);
		Path suffix = Path.of(scope.replace('\\', '/'));
		assertEquals(configuration.productionOutputRoot().resolve(suffix).toString(),
				configuration.classpath(Path.of(scope), "ignored"));
		assertEquals(Path.of(BuildMode.GRADLE.getBuildDirectory()).resolve(suffix).toString(),
				BuildMode.GRADLE.getClasspath(Path.of(scope), "ignored"));
	}

	/** Prefixes select discovered outputs rather than conventional output names. */
	@Test
	void usesCustomOutputRoots() {
		var configuration = new BuildToolConfiguration(BuildMode.GRADLE, projectRoot, List.of(), List.of(),
				Path.of("custom/main"), Path.of("custom/test"));
		assertEquals(configuration.productionOutputRoot().resolve("org/example").toString(),
				configuration.classpath(Path.of("classes\\java\\main\\org\\example"), "ignored"));
		assertEquals(configuration.testOutputRoot().resolve("org/example").toString(),
				configuration.classpath(Path.of("test-classes\\java\\test\\org\\example"), "ignored"));
	}

	/**
	 * A scope starting at a root or a drive is refused by both APIs on any host,
	 * including a backslash root that only normalisation turns into one.
	 */
	@ParameterizedTest
	@ValueSource(strings = { "/outside", "\\outside", "C:\\outside", "C:outside" })
	void rejectsRootedScopes(String scope) {
		for (BuildMode mode : BuildMode.values()) {
			var configuration = configuration(mode);
			assertThrows(SecurityException.class, () -> configuration.classpath(Path.of(scope), "ignored"));
			assertThrows(SecurityException.class, () -> mode.getClasspath(Path.of(scope), "ignored"));
		}
	}

	/** An absolute scope is refused by its form, even when it names the output. */
	@Test
	void rejectsAbsoluteScopeInsideProject() {
		var configuration = configuration(BuildMode.GRADLE);
		Path inside = configuration.productionOutputRoot().resolve("org/example");
		assertThrows(SecurityException.class, () -> configuration.classpath(inside, "ignored"));
		assertThrows(SecurityException.class, () -> BuildMode.GRADLE.getClasspath(inside, "ignored"));
	}

	/** One step beyond the project root is already refused. */
	@ParameterizedTest
	@CsvSource({ "GRADLE, classes/java/main/../../../../../outside",
			"GRADLE, classes\\java\\main\\..\\..\\..\\..\\..\\outside", "MAVEN, test-classes/../../../outside",
			"MAVEN, test-classes\\..\\..\\..\\outside" })
	void rejectsEscapingScopes(BuildMode mode, String scope) {
		var configuration = configuration(mode);
		assertThrows(SecurityException.class, () -> configuration.classpath(Path.of(scope), "ignored"));
	}

	/** Traversal that ends inside the project is normalised rather than refused. */
	@Test
	void normalisesContainedTraversal() {
		var configuration = configuration(BuildMode.GRADLE);
		assertEquals(configuration.projectRoot().resolve("inside").toString(),
				configuration.classpath(Path.of("classes\\java\\main\\..\\..\\..\\..\\inside"), "ignored"));
	}

	/** Constructor validation still rejects output roots outside the project. */
	@Test
	void rejectsEscapingOutputRoot() {
		assertThrows(SecurityException.class, () -> new BuildToolConfiguration(BuildMode.GRADLE, projectRoot, List.of(),
				List.of(), projectRoot.getParent(), Path.of("build/classes/java/test")));
	}

	/**
	 * Existing ancestors are resolved to their real paths for an analysis scope.
	 */
	@Test
	void canonicalisesExistingScope() throws IOException {
		var configuration = configuration(BuildMode.GRADLE);
		Path packageRoot = Files.createDirectories(configuration.productionOutputRoot().resolve("org/example"));
		assertEquals(packageRoot.toRealPath().toString(),
				configuration.classpath(Path.of("classes\\java\\main\\org\\example"), "ignored"));
	}

	/** A link inside the output must not redirect analysis outside the project. */
	@Test
	void rejectsScopeThroughEscapingSymbolicLink() throws IOException {
		Path nestedProject = Files.createDirectory(projectRoot.resolve("project"));
		Path outside = Files.createDirectory(projectRoot.resolve("outside"));
		var configuration = new BuildToolConfiguration(BuildMode.GRADLE, nestedProject, List.of(), List.of(),
				Path.of("build/classes/java/main"), Path.of("build/classes/java/test"));
		Path link = Files.createDirectories(configuration.productionOutputRoot()).resolve("linked");
		try {
			Files.createSymbolicLink(link, outside);
		} catch (IOException | UnsupportedOperationException exception) {
			abort("Creating symbolic links is unavailable: " + exception.getMessage());
		}
		assertThrows(SecurityException.class,
				() -> configuration.classpath(Path.of("classes\\java\\main\\linked\\missing"), "ignored"));
	}

	/** Builds a conventional layout under the temporary project. */
	private BuildToolConfiguration configuration(BuildMode mode) {
		return new BuildToolConfiguration(mode, projectRoot, List.of(), List.of(), Path.of(mode.getBuildDirectory()),
				Path.of(mode.getTestBuildDirectory()));
	}
}
