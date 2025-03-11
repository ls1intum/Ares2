package de.tum.cit.ase.ares.api.securitytest.java.scanner;

import de.tum.cit.ase.ares.api.buildtoolconfiguration.java.JavaBuildMode;
import de.tum.cit.ase.ares.api.util.ProjectSourcesFinder;

import javax.annotation.Nonnull;
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

/**
 * Utility class for scanning Java source files.
 *
 * <p>Description: This class scans Java source files to extract metadata such as test classes, package names and main classes. It is designed to facilitate automated configuration and test setup within Java projects.</p>
 *
 * <p>Design Rationale: The class utilises stream processing and regular expression matching to efficiently analyse source files, ensuring flexibility in recognising diverse code patterns.</p>
 *
 * @since 2.0.0
 * @author Markus Paulsen
 * @version 2.0.0
 */
public class JavaProgrammingExerciseScanner implements JavaScanner {

    /**
     * Default package name used if none is found.
     */
    private static final String DEFAULT_PACKAGE = "de.tum.cit.aet";

    /**
     * Default main class name used if no main class is detected.
     */
    private static final String DEFAULT_MAIN_CLASS = "Main";

    /**
     * Regex pattern to match public class declarations.
     */
    private static final Pattern CLASS_PATTERN = Pattern.compile("public\\s+class\\s+([A-Za-z0-9_]+)");

    /**
     * Regex pattern to match package declarations.
     */
    private static final Pattern PACKAGE_PATTERN = Pattern.compile("package\\s+([a-zA-Z_][a-zA-Z0-9_]*(\\.[a-zA-Z_][a-zA-Z0-9_]*)*)");

    /**
     * Regex pattern to detect the main method in source files.
     */
    private static final Pattern MAIN_METHOD_PATTERN = Pattern.compile("public\\s+static\\s+void\\s+main\\s*\\(\\s*String\\s*\\[");

    /**
     * Regex pattern to identify test annotations.
     */
    private static final Pattern TEST_ANNOTATION_PATTERN = Pattern.compile("@Test\\b");

    /**
     * Determines the build mode of the Java project.
     *
     * @since 2.0.0
     * @author Markus Paulsen
     * @return the JavaBuildMode for the project
     */
    @Nonnull
    public JavaBuildMode scanForBuildMode() {
        return ProjectSourcesFinder.isGradleProject() ? JavaBuildMode.GRADLE : JavaBuildMode.MAVEN;
    }

    /**
     * Scans the project for test classes.
     *
     * @since 2.0.0
     * @author Markus Paulsen
     * @return an array of fully qualified class names identified as test classes
     */
    @Nonnull
    public String[] scanForTestClasses() {
        return scanJavaFiles(this::extractTestClass).filter(Objects::nonNull).toArray(String[]::new);
    }

    /**
     * Determines the most commonly used package name in the project.
     *
     * @since 2.0.0
     * @author Markus Paulsen
     * @return the most frequent package name or a default if none is found
     */
    @Nonnull
    public String scanForPackageName() {
        Map<String, Long> packageCounts = scanJavaFiles(this::extractPackageName).filter(Objects::nonNull).collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));

        return packageCounts.entrySet().stream().max(Map.Entry.comparingByValue()).map(Map.Entry::getKey).orElse(DEFAULT_PACKAGE);
    }

    /**
     * Identifies the main class within the project.
     *
     * @since 2.0.0
     * @author Markus Paulsen
     * @return the name of the class containing the main method or a default value if none is found
     */
    @Nonnull
    public String scanForMainClassInPackage() {
        List<String> mainClasses = scanJavaFiles(this::extractMainClass).filter(Objects::nonNull).toList();

        // Prioritise classes named "Main" or "Application"
        return mainClasses.stream().filter(name -> "Main".equals(name) || "Application".equals(name)).findFirst().orElse(!mainClasses.isEmpty() ? mainClasses.getFirst() : DEFAULT_MAIN_CLASS);
    }

    /**
     * Scans for the test directory within the project structure.
     *
     * @since 2.0.0
     * @author Markus Paulsen
     * @return the path to the test directory as a string, or a default path if not found
     */
    @Nonnull
    public Path scanForTestPath() {
        Optional<Path> projectRoot = ProjectSourcesFinder.findProjectSourcesPath();
        if (projectRoot.isPresent()) {
            JavaBuildMode buildMode = scanForBuildMode();
            Path testPath;
            if (buildMode == JavaBuildMode.GRADLE) {// Check for custom test directory in Gradle projects
                Path buildGradle = projectRoot.get().resolve("build.gradle");
                if (Files.exists(buildGradle)) {
                    try {
                        String content = Files.readString(buildGradle);
                        if (content.contains("srcDir 'test'")) {
                            testPath = projectRoot.get().resolve("test");
                            if (Files.exists(testPath)) {
                                return testPath;
                            }
                        }
                    } catch (IOException e) {
                        // Fall back to default if file reading fails
                    }
                }
                // Default Gradle test path
            }
            testPath = projectRoot.get().resolve("src/test/java");

            if (Files.exists(testPath)) {
                return testPath;
            }
        }

        return Path.of("src/test/java"); // Return a default path if no specific path is found
    }

    /**
     * Scans Java files and processes them using the provided extractor function.
     *
     * @param extractor function to extract data from file content
     * @param <T> the type of the extracted data
     * @since 2.0.0
     * @author Markus Paulsen
     * @return a stream of extracted values
     */
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

    /**
     * Retrieves all Java file paths from the project sources.
     *
     * @since 2.0.0
     * @author Markus Paulsen
     * @return a list of paths to Java files
     */
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
     * Checks if the given file is a Java source file.
     *
     * @param path the path of the file to check
     * @param attributes the file's basic attributes
     * @since 2.0.0
     * @author Markus Paulsen
     * @return true if the file is a regular Java file, false otherwise
     */
    private boolean fileIsJavaFile(Path path, BasicFileAttributes attributes) {
        return attributes.isRegularFile() && path.toString().endsWith(".java");
    }

    /**
     * Extracts the class name from the given file content.
     *
     * @param content the content of a Java file
     * @since 2.0.0
     * @author Markus Paulsen
     * @return the class name if found, otherwise null
     */
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
     * @return the fully qualified name of the test class if the file is a test class, otherwise null
     */
    private String extractTestClass(String content) {
        boolean isTest = TEST_ANNOTATION_PATTERN.matcher(content).find() || content.contains("extends TestCase");

        if (isTest) {
            String packageName = extractPackageName(content);
            String className = extractClassName(content);
            if (packageName != null && className != null) {
                return packageName + "." + className;
            }
        }
        return null;
    }
}