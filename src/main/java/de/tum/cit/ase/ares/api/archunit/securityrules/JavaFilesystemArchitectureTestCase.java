package de.tum.cit.ase.ares.api.archunit.securityrules;

import de.tum.cit.ase.ares.api.util.ProjectSourcesFinder;

import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;

import static de.tum.cit.ase.ares.api.archunit.ArchitectureRulesStorage.TEST_FILE_NAME;

public class JavaFilesystemArchitectureTestCase implements ArchitectureTestCase {

    private final String pathToTestDir;

    private final boolean gradleProject = ProjectSourcesFinder.isGradleProject();

    public JavaFilesystemArchitectureTestCase(String pathToTestDir){
        this.pathToTestDir = pathToTestDir;
    }

    @Override
    public void runArchitectureTestCase(Object securityPolicy) throws IOException, InterruptedException {
        // Run the test cases
        String testContent = createArchitectureTestFileContent(new Object());
        // now write the testContent to a temporary file and run the test
        Path testFile = Path.of(this.pathToTestDir + "/" + TEST_FILE_NAME);
        try {
            testFile = Files.createFile(Path.of(this.pathToTestDir + "/" + TEST_FILE_NAME));
        } catch (FileAlreadyExistsException e) {
            // Ignore, should not be the case
        }

        try {
            Files.writeString(testFile, testContent);
        } catch (IOException e) {
            // TODO dedicated exception
            throw new RuntimeException(e);
        }
        // run the test
        ProcessBuilder compileProcessBuilder;
        if (gradleProject) {
            compileProcessBuilder = new ProcessBuilder("./gradlew", "test", "--tests", TEST_FILE_NAME);
        } else {
            compileProcessBuilder = new ProcessBuilder("mvn", "test", "-Dtest=%s".formatted(TEST_FILE_NAME));
        }
        compileProcessBuilder.inheritIO();
        Process compileProcess = compileProcessBuilder.start();
        int exitCode = compileProcess.waitFor();

        if (exitCode != 0) {
            throw new AssertionError("Build failed with exit code " + exitCode);
        }

        Files.delete(testFile);
    }

    // Given Security
    @Override
    public String createArchitectureTestFileContent(Object securityPolicy) {
        // TODO retrieve test directory path from the passed in repo in the assignment build
        return String.format("""
                package %s;

                import com.tngtech.archunit.base.DescribedPredicate;
                import com.tngtech.archunit.core.domain.*;
                import com.tngtech.archunit.core.importer.ImportOption;
                import com.tngtech.archunit.junit.AnalyzeClasses;
                import com.tngtech.archunit.junit.ArchTest;
                import com.tngtech.archunit.lang.ArchRule;
                import com.tngtech.archunit.lang.syntax.ArchRuleDefinition;
                import de.tum.cit.ase.ares.api.archunit.PathLocationProvider;
                import de.tum.cit.ase.ares.api.archunit.StudentPath;
                import de.tum.cit.ase.ares.api.archunit.conditions.TransitivelyAccessesMethodsCondition;
                import de.tum.cit.ase.ares.api.archunit.securityrules.JavaFilesystemArchitectureTestCase;
                               \s
                import java.util.*;
                               \s
                /**
                 * This class executes the security rules on the architecture.
                 */
                @StudentPath("%s")
                @AnalyzeClasses(locations = PathLocationProvider.class, importOptions = ImportOption.DoNotIncludeTests.class)
                class FileSystemSecurityRulesTest {
                               \s
                    private static final Set<String> bannedMethods = JavaFilesystemArchitectureTestCase.getForbiddenMethods();
                   \s
                    /**
                     * The packages that should not be accessed by the student submission.
                     */
                    private static final List<String> bannedPackages = List.of(
                            "java.nio.file",
                            "java.util.prefs",
                            "sun.print",
                            "sun.security",
                            "java.util.jar",
                            "sun.awt.X11",
                            "javax.imageio.stream",
                            "javax.sound.midi",
                            "javax.swing.filechooser",
                            "java.awt.desktop");
                               \s
                    /**
                     * This method checks if any class in the given package accesses the file system.
                     */
                    @ArchTest
                    public static final ArchRule noClassesShouldAccessFileSystem = ArchRuleDefinition.noClasses()
                            .should(new TransitivelyAccessesMethodsCondition(new DescribedPredicate<>("accesses file system") {
                                @Override
                                public boolean test(JavaAccess<?> javaAccess) {
                                    if (bannedPackages.contains(javaAccess.getTargetOwner().getPackageName())) {
                                        return true;
                                    }
                                    return bannedMethods.contains(javaAccess.getTarget().getFullName());
                                }
                            }));
                }""", "de.tum.cit.ase" , gradleProject ? "build/classes" : "target/classes");
    }

    /**
     * Used by the generated ArchUnit tests
     */
    public static Set<String> getForbiddenMethods() {
        return forbiddenMethods;
    }
}
