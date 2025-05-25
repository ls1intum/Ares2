package de.tum.cit.ase.ares.api.architecture;

import com.ibm.wala.ipa.callgraph.CallGraph;
import com.opencsv.exceptions.CsvException;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import de.tum.cit.ase.ares.api.aop.java.javaAOPModeData.JavaCSVFileLoader;
import de.tum.cit.ase.ares.api.architecture.java.JavaArchitectureTestCase;
import de.tum.cit.ase.ares.api.architecture.java.JavaArchitectureTestCaseSupported;
import de.tum.cit.ase.ares.api.architecture.java.archunit.JavaArchUnitTestCase;
import de.tum.cit.ase.ares.api.architecture.java.wala.CustomCallgraphBuilder;
import de.tum.cit.ase.ares.api.architecture.java.wala.JavaWalaTestCase;
import de.tum.cit.ase.ares.api.util.FileTools;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Enum representing the architecture modes for Java security test cases.
 *
 * <p>Description: Provides different modes for architecture test case generation and execution in Java.
 * The modes determine how files and settings are copied and resolved based on the underlying architecture analysis tool.</p>
 *
 * <p>Design Rationale: Using an enum to represent architecture modes centralises configuration and enables future extensions
 * (e.g. supporting WALA) while ensuring that file handling and resource resolution are consistently applied.</p>
 *
 * @since 2.0.0
 * @author Markus Paulsen
 * @version 2.0.0
 */
public enum ArchitectureMode {

    /**
     * ArchUnit mode for analysing Java code with TNGs ArchUnit.
     */
    ARCHUNIT,

    /**
     * WALA mode for analysing Java code with IBMs WALA.
     */
    WALA;

    //<editor-fold desc="Load configuration">
    public List<List<String>> getCopyConfigurationEntries() {
        try {
            return (new JavaCSVFileLoader()).loadCopyData(this);
        } catch (IOException | CsvException e) {
            throw new RuntimeException(e);
        }
    }

    public List<List<String>> getEditConfigurationEntries() {
        try {
            return (new JavaCSVFileLoader()).loadEditData(this);
        } catch (IOException | CsvException e) {
            throw new RuntimeException(e);
        }
    }
    //</editor-fold>

    //<editor-fold desc="Multi-file methods">

    /**
     * Retrieves the list of resource file paths to copy for the current architecture mode.
     *
     * @since 2.0.0
     * @author Markus Paulsen
     * @return a list of paths representing the resource files to copy.
     */
    @Nonnull
    public List<Path> filesToCopy() {
        return getCopyConfigurationEntries().stream()
                .map(entry -> entry.getFirst().split("/"))
                .map(FileTools::resolveOnPackage)
                .toList();
    }

    /**
     * Retrieves the file value arrays based on the provided package name.
     *
     * @since 2.0.0
     * @author Markus Paulsen
     * @param packageName the base package name.
     * @return a list of string arrays representing file values.
     */
    @Nonnull
    public List<String[]> formatValues(@Nonnull String packageName, @Nonnull String mainClassInPackageName) {
        return getCopyConfigurationEntries().stream()
                .map(entry -> entry.get(1))
                .map(Integer::parseInt)
                .map(entry -> switch (entry){
                    case 0 -> new String[]{packageName, packageName, mainClassInPackageName};
                    default -> FileTools.generatePackageNameArray(packageName, entry);
                })
                .toList();
    }

    /**
     * Determines the target paths for copying resource files based on the project path and package name.
     *
     * @since 2.0.0
     * @author Markus Paulsen
     * @param projectPath the project path.
     * @param packageName the base package name.
     * @return a list of paths representing the target locations.
     */
    @Nonnull
    public List<Path> targetsToCopyTo(@Nonnull Path projectPath, @Nonnull String packageName) {
        return getCopyConfigurationEntries().stream()
                .map(entry -> entry.get(2).split("/"))
                .map(FileTools::resolveOnPackage)
                .map(projectPath::resolve)
                .toList();
    }
    //</editor-fold>

    //<editor-fold desc="Single-file methods">

    /**
     * Retrieves the path to the file header template for the three-parted file.
     *
     * @since 2.0.0
     * @author Markus Paulsen
     * @return the path to the file header template.
     */
    @Nonnull
    public Path threePartedFileHeader() {
        return getEditConfigurationEntries().stream()
                .map(entry -> entry.getFirst().split("/"))
                .map(FileTools::resolveOnPackage)
                .toList().getFirst();
    }

    /**
     * Generates the body content for the three-parted file by concatenating the architecture test case definitions.
     *
     * @since 2.0.0
     * @author Markus Paulsen
     * @param testCases the list of test cases.
     * @return a string representing the body content.
     */
    @SuppressWarnings("unchecked")
    @Nonnull
    public String threePartedFileBody(List<?> testCases) {
        return switch (this) {
            case ARCHUNIT -> String.join("\n",
                    convertToJavaArchUnitTestCases((List<JavaArchitectureTestCase>) testCases).stream()
                            .map(javaArchUnitTestCase -> javaArchUnitTestCase.writeArchitectureTestCase("ARCHUNIT", ""))
                            .toList()

            );
            case WALA -> String.join("\n",
                    convertToJavaWalaTestCases((List<JavaArchitectureTestCase>) testCases).stream()
                            .map(javaWalaTestCase -> javaWalaTestCase.writeArchitectureTestCase("WALA", ""))
                            .toList()
            );
        };
    }

    /**
     * Retrieves the path to the file footer template for the three-parted file.
     *
     * @since 2.0.0
     * @author Markus Paulsen
     * @return the path to the file footer template.
     */
    @Nonnull
    public Path threePartedFileFooter() {
        return getEditConfigurationEntries().stream()
                .map(entry -> entry.get(1).split("/"))
                .map(FileTools::resolveOnPackage)
                .toList().getFirst();
    }

    /**
     * Generates the file value array based on the provided package name.
     *
     * @since 2.0.0
     * @author Markus Paulsen
     * @param packageName the base package name.
     * @return an array of strings representing the file value.
     */
    @Nonnull
    public String[] formatValues(@Nonnull String packageName) {
        return switch (this) {
            case ARCHUNIT -> FileTools.generatePackageNameArray(packageName, 2);
            case WALA -> FileTools.generatePackageNameArray(packageName, 2);
        };
    }

    /**
     * Determines the target path for copying the main architecture test case file based on the project path and package name.
     *
     * @since 2.0.0
     * @author Markus Paulsen
     * @param projectPath the project path.
     * @param packageName the base package name.
     * @return the target path for the main file.
     */
    @Nonnull
    public Path targetToCopyTo(@Nonnull Path projectPath, @Nonnull String packageName) {
        return getEditConfigurationEntries().stream()
                .map(entry -> entry.get(2).split("/"))
                .map(FileTools::resolveOnPackage)
                .toList().getFirst();
    }
    //</editor-fold>

    //<editor-fold desc="Reset methods">
    // (No reset methods defined for JavaArchitectureMode)
    //</editor-fold>

    //<editor-fold desc="Other methods">

    /**
     * Imports and analyzes Java classes from the specified class path using ArchUnit's ClassFileImporter.
     * This method enables static code analysis by creating a collection of Java class metadata.
     *
     * @since 2.0.0
     * @author Markus Paulsen
     * @param classPath the file system path containing Java classes to import
     * @return a JavaClasses object containing all imported classes for analysis
     */
    @Nonnull
    public JavaClasses getJavaClasses(String classPath) {
        return new ClassFileImporter().importPath(classPath);
    }

    /**
     * Builds a call graph based on the current architecture mode. Returns null for ARCHUNIT mode
     * as it employs non-call-graph analysis, while in WALA mode it constructs a proper call graph
     * representing method caller-callee relationships.
     *
     * @since 2.0.0
     * @author Markus Paulsen
     * @param classPath the file system path containing Java classes to analyze
     * @return a CallGraph object for WALA mode, or null for ARCHUNIT mode
     */
    @Nullable
    public CallGraph getCallGraph(String classPath) {
        return switch (this) {
            case ARCHUNIT -> null;
            case WALA -> new CustomCallgraphBuilder().buildCallGraph(classPath);
        };
    }
    //</editor-fold>

    //<editor-fold desc="Static methods">
    private static JavaArchUnitTestCase convertToJavaArchUnitTestCase(JavaArchitectureTestCase testCase) {
        return JavaArchUnitTestCase.archunitBuilder()
                .javaArchitectureTestCaseSupported((JavaArchitectureTestCaseSupported) testCase.getArchitectureTestCaseSupported())
                .allowedPackages(testCase.getAllowedPackages())
                .javaClasses(testCase.getJavaClasses())
                .build();
    }

    private static JavaWalaTestCase convertToJavaWalaTestCases(JavaArchitectureTestCase testCase) {
        return JavaWalaTestCase.walaBuilder()
                .javaArchitectureTestCaseSupported((JavaArchitectureTestCaseSupported) testCase.getArchitectureTestCaseSupported())
                .allowedPackages(testCase.getAllowedPackages())
                .callGraph(testCase.getCallGraph())
                .javaClasses(testCase.getJavaClasses())
                .build();
    }

    private static List<JavaArchUnitTestCase> convertToJavaArchUnitTestCases(List<JavaArchitectureTestCase> testCases) {
        return new ArrayList<>(testCases.stream().map(ArchitectureMode::convertToJavaArchUnitTestCase).toList());
    }

    private static List<JavaWalaTestCase> convertToJavaWalaTestCases(List<JavaArchitectureTestCase> testCases) {
        return new ArrayList<>(testCases.stream().map(ArchitectureMode::convertToJavaWalaTestCases).toList());
    }
    //</editor-fold>
}