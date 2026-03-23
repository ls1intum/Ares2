package de.tum.cit.ase.ares.api.securitytest.java.projectScanner;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.google.common.base.Preconditions;

import de.tum.cit.ase.ares.api.buildtoolconfiguration.BuildMode;
import de.tum.cit.ase.ares.api.util.ProjectSourcesFinder;

/**
 * Utility class for scanning Java source files.
 * <p>
 * Description: This class scans Java source files to extract metadata such as
 * test classes, package names and main classes. It is designed to facilitate
 * automated configuration and test setup within Java projects.
 * </p>
 * <p>
 * Design Rationale: The class utilises stream processing and regular expression
 * matching to efficiently analyse source files, ensuring flexibility in
 * recognising diverse code patterns.
 * </p>
 *
 * @since 2.0.0
 * @author Markus Paulsen
 * @version 2.0.0
 */
public class JavaProjectScanner implements ProjectScanner {

	// <editor-fold desc="Fixed Regex Patterns (defined by Java)">
	/**
	 * Regex pattern to match public class declarations in Java files.
	 */
	@Nonnull
	private static final Pattern CLASS_PATTERN = Preconditions.checkNotNull(
			Pattern.compile(
					"\\bpublic\\s+(?:final\\s+|abstract\\s+|strictfp\\s+)*class\\s+([A-Za-z_$][A-Za-z0-9_$]*)\\b"),
			"CLASS_PATTERN must not be null");

	/**
	 * Regex pattern to match package declarations in Java files.
	 */
	@Nonnull
	private static final Pattern PACKAGE_PATTERN = Preconditions.checkNotNull(
			Pattern.compile("\\bpackage\\s+([A-Za-z_$][A-Za-z0-9_$]*(?:\\.[A-Za-z_$][A-Za-z0-9_$]*)*)\\s*;"),
			"PACKAGE_PATTERN must not be null");

	/**
	 * Regex pattern to detect the main method in Java files.
	 */
	@Nonnull
	private static final Pattern MAIN_METHOD_PATTERN = Preconditions.checkNotNull(Pattern.compile(
			"\\bpublic\\s+static\\s+void\\s+main\\s*\\(\\s*String\\s*(?:\\[\\s*]|\\.\\.\\.)\\s*[A-Za-z_$][A-Za-z0-9_$]*\\s*\\)"),
			"MAIN_METHOD_PATTERN must not be null");

	/**
	 * Regex pattern to identify test annotations in Java files.
	 */
	@Nonnull
	private static final Pattern TEST_ANNOTATION_PATTERN = Preconditions
			.checkNotNull(Pattern.compile("@(?:Test|Property)\\b"), "TEST_ANNOTATION_PATTERN must not be null");
	// </editor-fold>

	// <editor-fold desc="Variable Regex Patterns (defined by project)">

	/**
	 * Default package name used if none is found.
	 */
	@Nonnull
	private String getDefaultPackage() {
		return "";
	}

	/**
	 * Default main class name used if no main class is detected.
	 */
	@Nonnull
	private String getDefaultMainClass() {
		return "Main";
	}

	/**
	 * Regex pattern to identify test annotations.
	 */
	@Nonnull
	private static Pattern getTestAnnotationPattern() {
		return TEST_ANNOTATION_PATTERN;
	}
	// </editor-fold>

	// <editor-fold desc="Helper methods">

	/**
	 * Retrieves all Java file paths from the project sources.
	 *
	 * @since 2.0.0
	 * @author Markus Paulsen
	 * @return a list of paths to Java files
	 */
	@Nonnull
	private List<Path> getJavaFiles() {
		return ProjectSourcesFinder.findProjectSourcesPath().map(sourcePath -> {
			try (Stream<Path> stream = Files.find(sourcePath, Integer.MAX_VALUE, this::fileIsJavaFile)) {
				return stream.collect(Collectors.toList());
			} catch (IOException e) {
				return Collections.<Path>emptyList();
			}
		}).orElse(Collections.emptyList());
	}

	/**
	 * Retrieves all Java file paths from the test sources directory.
	 *
	 * @since 2.0.0
	 * @author Markus Paulsen
	 * @return a list of paths to Java test files
	 */
	@Nonnull
	private List<Path> getTestJavaFiles() {
		Path testPath = findTestSourcePath();
		if (testPath != null && Files.exists(testPath)) {
			try (Stream<Path> stream = Files.find(testPath, Integer.MAX_VALUE, this::fileIsJavaFile)) {
				return stream.collect(Collectors.toList());
			} catch (IOException e) {
				return Collections.emptyList();
			}
		}
		return Collections.emptyList();
	}

	/**
	 * Finds the test source directory by inspecting the build configuration file
	 * at the project root.
	 *
	 * @since 2.0.0
	 * @author Markus Paulsen
	 * @return the path to the test source directory, or null if not found
	 */
	@Nullable
	private Path findTestSourcePath() {
		if (ProjectSourcesFinder.isGradleProject()) {
			try {
				String content = Files.readString(Path.of(ProjectSourcesFinder.getBuildGradlePath()));
				if (content.contains("srcDir 'test'")) {
					Path testPath = Path.of("test");
					if (Files.exists(testPath)) {
						return testPath;
					}
				}
			} catch (IOException e) {
				// fall through to default
			}
			Path defaultPath = Path.of("src/test/java");
			if (Files.exists(defaultPath)) {
				return defaultPath;
			}
		}
		if (ProjectSourcesFinder.isMavenProject()) {
			Path defaultPath = Path.of("src/test/java");
			if (Files.exists(defaultPath)) {
				return defaultPath;
			}
		}
		return null;
	}

	/**
	 * Checks if the given file is a Java source file.
	 *
	 * @param path       the path of the file to check
	 * @param attributes the file's basic attributes
	 * @since 2.0.0
	 * @author Markus Paulsen
	 * @return true if the file is a regular Java file, false otherwise
	 */
	private boolean fileIsJavaFile(@Nonnull Path path, @Nonnull BasicFileAttributes attributes) {
		return Preconditions.checkNotNull(attributes, "attributes must not be null").isRegularFile()
				&& Preconditions.checkNotNull(path, "path must not be null").toString().endsWith(".java");
	}

	/**
	 * Scans Java files and processes them using the provided extractor function.
	 *
	 * @param extractor function to extract data from file content
	 * @param <T>       the type of the extracted data
	 * @since 2.0.0
	 * @author Markus Paulsen
	 * @return a stream of extracted values
	 */
	@Nonnull
	private <T> Stream<T> scanJavaFiles(Function<String, T> extractor) {
		return getJavaFiles().stream().flatMap(file -> {
			try {
				String content = Files.readString(file);
				T result = extractor.apply(content);
				return result != null ? Stream.of(result) : Stream.empty();
			} catch (IOException e) {
				return Stream.empty();
			}
		});
	}

	// <editor-fold desc="Extract methods">

	/**
	 * Extracts the class name from the given file content.
	 *
	 * @param content the content of a Java file
	 * @since 2.0.0
	 * @author Markus Paulsen
	 * @return the class name if found, otherwise null
	 */
	@Nullable
	private String extractClassName(String content) {
		Matcher classMatcher = CLASS_PATTERN.matcher(content);
		return classMatcher.find() ? classMatcher.group(1) : null;
	}

	/**
	 * Extracts the package name from the given file content.
	 *
	 * @param content the content of a Java file
	 * @since 2.0.0
	 * @author Markus Paulsen
	 * @return the package name if found, otherwise null
	 */
	@Nullable
	private String extractPackageName(String content) {
		Matcher packageMatcher = PACKAGE_PATTERN.matcher(content);
		return packageMatcher.find() ? packageMatcher.group(1) : null;
	}

	/**
	 * Extracts the main class from the file content if a main method is present.
	 *
	 * @param content the content of a Java file
	 * @since 2.0.0
	 * @author Markus Paulsen
	 * @return the class name containing the main method, or null if not present
	 */
	@Nullable
	private String extractMainClass(String content) {
		if (MAIN_METHOD_PATTERN.matcher(content).find()) {
			return extractClassName(content);
		}
		return null;
	}

	/**
	 * Extracts test class information from the file content.
	 *
	 * @param content the content of a Java file
	 * @since 2.0.0
	 * @author Markus Paulsen
	 * @return the fully qualified name of the test class if the file is a test
	 *         class, otherwise null
	 */
	@Nullable
	private String extractTestClass(String content) {
		boolean isTest = getTestAnnotationPattern().matcher(content).find() || content.contains("extends TestCase");

		if (isTest) {
			String packageName = extractPackageName(content);
			String className = extractClassName(content);
			if (packageName != null && className != null) {
				return packageName + "." + className;
			}
		}
		return null;
	}
	// </editor-fold>
	// </editor-fold>

	// <editor-fold desc="Scan methods">

	/**
	 * Determines the build mode of the Java project.
	 *
	 * @since 2.0.0
	 * @author Markus Paulsen
	 * @return the JavaBuildMode for the project
	 */
	@Override
	@Nonnull
	public BuildMode scanForBuildMode() {
		return ProjectSourcesFinder.isGradleProject() ? BuildMode.GRADLE : BuildMode.MAVEN;
	}

	/**
	 * Scans the project for test classes.
	 *
	 * @since 2.0.0
	 * @author Markus Paulsen
	 * @return an array of fully qualified class names identified as test classes
	 */
	@Override
	@Nonnull
	public String[] scanForTestClasses() {
		return getTestJavaFiles().stream().flatMap(file -> {
			try {
				String content = Files.readString(file);
				String result = extractTestClass(content);
				return result != null ? Stream.of(result) : Stream.empty();
			} catch (IOException e) {
				return Stream.empty();
			}
		}).toArray(String[]::new);
	}

	/**
	 * Determines the most commonly used package name in the project.
	 *
	 * @since 2.0.0
	 * @author Markus Paulsen
	 * @return the most frequent package name or a default if none is found
	 */
	@Override
	@Nonnull
	public String scanForPackageName() {
		Map<String, Long> packageCounts = scanJavaFiles(this::extractPackageName).filter(Objects::nonNull)
				.collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));
		return packageCounts.entrySet().stream().max(Map.Entry.comparingByValue()).map(Map.Entry::getKey)
				.orElse(getDefaultPackage());
	}

	/**
	 * Identifies the main class within the project.
	 *
	 * @since 2.0.0
	 * @author Markus Paulsen
	 * @return the name of the class containing the main method or a default value
	 *         if none is found
	 */
	@Override
	@Nonnull
	public String scanForMainClassInPackage() {
		List<String> mainClasses = scanJavaFiles(this::extractMainClass).filter(Objects::nonNull).toList();
		return mainClasses.stream().filter(name -> "Main".equals(name) || "Application".equals(name)).findFirst()
				.orElse(!mainClasses.isEmpty() ? mainClasses.get(0) : getDefaultMainClass());
	}

	/**
	 * Scans for the test directory within the project structure.
	 *
	 * @since 2.0.0
	 * @author Markus Paulsen
	 * @return the path to the test directory as a string, or a default path if not
	 *         found
	 */
	@Override
	@Nonnull
	public Path scanForTestPath() {
		Path testPath = findTestSourcePath();
		return testPath != null ? testPath : Path.of("src/test/java");
	}
	// </editor-fold>
}
