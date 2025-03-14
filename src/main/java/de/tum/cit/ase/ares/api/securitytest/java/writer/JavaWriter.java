package de.tum.cit.ase.ares.api.securitytest.java.writer;

import de.tum.cit.ase.ares.api.aop.java.JavaAOPMode;
import de.tum.cit.ase.ares.api.aop.java.JavaAOPTestCase;
import de.tum.cit.ase.ares.api.architecture.java.JavaArchitectureMode;
import de.tum.cit.ase.ares.api.architecture.java.JavaArchitectureTestCase;
import de.tum.cit.ase.ares.api.util.FileTools;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

public class JavaWriter implements Writer {

    //<editor-fold desc="Helper methods">
    @Nonnull
    private List<Path> createJavaArchitectureFiles(
            @Nonnull JavaArchitectureMode javaArchitectureMode,
            @Nullable Path projectDirectory,
            @Nonnull String packageName,
            @Nonnull List<JavaArchitectureTestCase> javaArchitectureTestCases
    ) {
        return Stream.concat(
                FileTools.copyJavaFiles(
                        javaArchitectureMode.filesToCopy(),
                        javaArchitectureMode.targetsToCopyTo(projectDirectory, packageName),
                        javaArchitectureMode.fileValues(packageName)
                ).stream(),
                Stream.of(FileTools.createThreePartedJavaFile(
                        javaArchitectureMode.threePartedFileHeader(),
                        javaArchitectureMode.threePartedFileBody(javaArchitectureTestCases),
                        javaArchitectureMode.threePartedFileFooter(),
                        javaArchitectureMode.targetToCopyTo(projectDirectory, packageName),
                        javaArchitectureMode.fileValue(packageName)
                ))
        ).collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
    }

    @Nonnull
    private List<Path> createJavaAOPFiles(
            @Nonnull JavaAOPMode javaAOPMode,
            @Nullable Path projectDirectory,
            @Nonnull String packageName,
            @Nonnull String mainClassInPackageName,
            @Nonnull String[] testClasses,
            @Nonnull List<String> essentialClasses,
            @Nonnull List<JavaAOPTestCase> javaAOPTestCases
    ) {
        ArrayList<String> allowedClasses = Stream.concat(
                Arrays.stream(testClasses),
                essentialClasses.stream()
        ).collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
        return Stream.concat(
                FileTools.copyJavaFiles(
                        javaAOPMode.filesToCopy(),
                        javaAOPMode.targetsToCopyTo(projectDirectory, packageName),
                        javaAOPMode.fileValues(packageName, mainClassInPackageName)
                ).stream(),
                Stream.of(FileTools.createThreePartedJavaFile(
                        javaAOPMode.threePartedFileHeader(),
                        javaAOPMode.threePartedFileBody(javaAOPMode.toString(), packageName, allowedClasses, javaAOPTestCases),
                        javaAOPMode.threePartedFileFooter(),
                        javaAOPMode.targetToCopyTo(projectDirectory, packageName),
                        javaAOPMode.fileValue(packageName)
                ))
        ).collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
    }
    //</editor-fold>

    //<editor-fold desc="Write security test cases methods">
    @Override
    @Nonnull
    public List<Path> writeSecurityTestCases(
            @Nullable Path projectDirectory,
            @Nonnull JavaArchitectureMode javaArchitectureMode,
            @Nonnull JavaAOPMode javaAOPMode,
            @Nonnull String packageName,
            @Nonnull String mainClassInPackageName,
            @Nonnull String[] testClasses,
            @Nonnull List<String> essentialClasses,
            @Nonnull List<JavaArchitectureTestCase> javaArchitectureTestCases,
            @Nonnull List<JavaAOPTestCase> javaAOPTestCases
    ) {
        return Stream.concat(
                createJavaArchitectureFiles(
                        javaArchitectureMode,
                        projectDirectory,
                        packageName,
                        javaArchitectureTestCases
                ).stream(),
                createJavaAOPFiles(
                        javaAOPMode,
                        projectDirectory,
                        packageName,
                        mainClassInPackageName,
                        testClasses,
                        essentialClasses,
                        javaAOPTestCases
                ).stream()
        ).collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
    }
    //</editor-fold>
}
