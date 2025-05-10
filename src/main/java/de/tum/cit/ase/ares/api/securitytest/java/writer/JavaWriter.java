package de.tum.cit.ase.ares.api.securitytest.java.writer;

import de.tum.cit.ase.ares.api.aop.java.JavaAOPMode;
import de.tum.cit.ase.ares.api.aop.java.JavaAOPTestCase;
import de.tum.cit.ase.ares.api.architecture.java.JavaArchitectureMode;
import de.tum.cit.ase.ares.api.architecture.java.JavaArchitectureTestCase;
import de.tum.cit.ase.ares.api.buildtoolconfiguration.java.JavaBuildMode;
import de.tum.cit.ase.ares.api.phobos.Phobos;
import de.tum.cit.ase.ares.api.util.FileTools;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

/**
 * Writes security test cases in Java programming language.
 *
 * <p>Description: This class is responsible for creating and writing security test files
 * for Java projects, including both architecture test files and AOP test files.
 *
 * <p>Design Rationale: The JavaWriter implements the Writer interface following the
 * Strategy design pattern to allow for different implementation strategies for writing
 * security test cases for different programming languages and frameworks.
 *
 * @since 2.0.0
 * @author Markus Paulsen
 * @version 2.0.0
 */
public class JavaWriter implements Writer {

    //<editor-fold desc="Helper methods">

    /**
     * Creates Java architecture test files.
     *
     * @since 2.0.0
     * @author Markus Paulsen
     * @param javaArchitectureMode the Java architecture mode to use; must not be null
     * @param essentialPackages the list of essential packages; must not be null
     * @param packageName the name of the package containing the main class; must not be null
     * @param mainClassInPackageName the name of the main class; must not be null
     * @param javaArchitectureTestCases the list of architecture test cases; must not be null
     * @param testFolderPath the directory of the project; may be null
     * @return a list of paths to the created files
     */
    @Nonnull
    private List<Path> createJavaArchitectureFiles(
            @Nonnull JavaArchitectureMode javaArchitectureMode,
            @Nonnull List<String> essentialPackages,
            @Nonnull String packageName,
            @Nonnull String mainClassInPackageName,
            @Nonnull List<JavaArchitectureTestCase> javaArchitectureTestCases,
            @Nullable Path testFolderPath
    ) {
        return Stream.concat(
                FileTools.copyJavaPhobosFiles(
                        javaArchitectureMode.filesToCopy(),
                        javaArchitectureMode.targetsToCopyTo(testFolderPath, packageName),
                        javaArchitectureMode.fileValues(packageName)
                ).stream(),
                Stream.of(FileTools.createThreePartedJavaPhobosFile(
                        javaArchitectureMode.threePartedFileHeader(),
                        javaArchitectureMode.threePartedFileBody(javaArchitectureTestCases),
                        javaArchitectureMode.threePartedFileFooter(),
                        javaArchitectureMode.targetToCopyTo(testFolderPath, packageName),
                        javaArchitectureMode.fileValue(packageName)
                ))
        ).collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
    }

    /**
     * Creates Java AOP test files.
     *
     * @since 2.0.0
     * @author Markus Paulsen
     * @param javaAOPMode the Java AOP mode to use; must not be null
     * @param essentialClasses the list of essential classes; must not be null
     * @param testClasses the list of test classes; must not be null
     * @param packageName the name of the package containing the main class; must not be null
     * @param mainClassInPackageName the name of the main class; must not be null
     * @param javaAOPTestCases the list of AOP test cases; must not be null
     * @param testFolderPath the directory of the project; may be null
     * @return a list of paths to the created files
     */
    @Nonnull
    private List<Path> createJavaAOPFiles(
            @Nonnull JavaAOPMode javaAOPMode,
            @Nonnull List<String> essentialClasses,
            @Nonnull List<String> testClasses,
            @Nonnull String packageName,
            @Nonnull String mainClassInPackageName,
            @Nonnull List<JavaAOPTestCase> javaAOPTestCases,
            @Nullable Path testFolderPath
    ) {
        @Nonnull ArrayList<String> allowedClasses = Stream.concat(
                essentialClasses.stream(),
                testClasses.stream()
        ).collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
        return Stream.concat(
                FileTools.copyJavaPhobosFiles(
                        javaAOPMode.filesToCopy(),
                        javaAOPMode.targetsToCopyTo(testFolderPath, packageName),
                        javaAOPMode.fileValues(packageName, mainClassInPackageName)
                ).stream(),
                Stream.of(FileTools.createThreePartedJavaPhobosFile(
                        javaAOPMode.threePartedFileHeader(),
                        javaAOPMode.threePartedFileBody(javaAOPMode.toString(), packageName, allowedClasses, javaAOPTestCases),
                        javaAOPMode.threePartedFileFooter(),
                        javaAOPMode.targetToCopyTo(testFolderPath, packageName),
                        javaAOPMode.fileValue(packageName)
                ))
        ).collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
    }

    @Nonnull
    private List<Path> createPhobosFiles(
            @Nonnull String packageName,
            @Nonnull List<JavaAOPTestCase> javaAOPTestCases,
            @Nullable Path testFolderPath
    ) {
        return Stream.concat(
                FileTools.copyJavaPhobosFiles(
                        Phobos.filesToCopy(),
                        Phobos.targetsToCopyTo(testFolderPath, packageName),
                        Phobos.fileValues(packageName)
                ).stream(),
                Stream.of(FileTools.createThreePartedJavaPhobosFile(
                        Phobos.threePartedFileHeader(),
                        Phobos.threePartedFileBody(javaAOPTestCases, testFolderPath),
                        Phobos.threePartedFileFooter(),
                        Phobos.targetToCopyTo(testFolderPath, packageName),
                        Phobos.fileValue(packageName)
                ))
        ).collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
    }

    //</editor-fold>

    //<editor-fold desc="Write security test cases methods">

    /**
     * Writes security test cases to files.
     *
     * @since 2.0.0
     * @author Markus Paulsen
     * @param javaBuildMode the Java build mode to use; must not be null
     * @param javaArchitectureMode the Java architecture mode to use; must not be null
     * @param javaAOPMode the Java AOP mode to use; must not be null
     * @param essentialPackages the list of essential packages; must not be null
     * @param essentialClasses the list of essential classes; must not be null
     * @param testClasses the list of test classes; must not be null
     * @param packageName the name of the package containing the main class; must not be null
     * @param mainClassInPackageName the name of the main class; must not be null
     * @param javaArchitectureTestCases the list of architecture test cases; must not be null
     * @param javaAOPTestCases the list of AOP test cases; must not be null
     * @param testFolderPath the directory of the project; may be null
     * @return a list of paths to the created files
     */
    @Nonnull
    public List<Path> writeSecurityTestCases(
            @Nonnull JavaBuildMode javaBuildMode,
            @Nonnull JavaArchitectureMode javaArchitectureMode,
            @Nonnull JavaAOPMode javaAOPMode,
            @Nonnull List<String> essentialPackages,
            @Nonnull List<String> essentialClasses,
            @Nonnull List<String> testClasses,
            @Nonnull String packageName,
            @Nonnull String mainClassInPackageName,
            @Nonnull List<JavaArchitectureTestCase> javaArchitectureTestCases,
            @Nonnull List<JavaAOPTestCase> javaAOPTestCases,
            @Nullable Path testFolderPath
    ) {
        return Stream.of(
                        createJavaArchitectureFiles(
                                javaArchitectureMode,
                                essentialPackages,
                                packageName,
                                mainClassInPackageName,
                                javaArchitectureTestCases,
                                testFolderPath
                        ).stream(),
                        createJavaAOPFiles(
                                javaAOPMode,
                                essentialClasses,
                                testClasses,
                                packageName,
                                mainClassInPackageName,
                                javaAOPTestCases,
                                testFolderPath
                        ).stream(),
                        createPhobosFiles(
                                packageName,
                                javaAOPTestCases,
                                testFolderPath
                        ).stream()
                )
                .flatMap(s -> s)
                .collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
    }
    //</editor-fold>
}
