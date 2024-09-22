package de.tum.cit.ase.ares.api.securitytest.java;

//<editor-fold desc="Imports">

import com.google.common.collect.Streams;
import com.tngtech.archunit.ArchConfiguration;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import de.tum.cit.ase.ares.api.aop.java.JavaSecurityTestCaseSupported;
import de.tum.cit.ase.ares.api.architecture.java.archunit.JavaArchUnitSecurityTestCase;
import de.tum.cit.ase.ares.api.architecture.java.archunit.JavaArchUnitTestCaseSupported;
import de.tum.cit.ase.ares.api.architecture.java.archunit.postcompile.JavaArchitectureTestCaseCollection;
import de.tum.cit.ase.ares.api.aop.java.JavaSecurityTestCase;
import de.tum.cit.ase.ares.api.policy.SecurityPolicy;
import de.tum.cit.ase.ares.api.policy.SecurityPolicy.ResourceAccesses;
import de.tum.cit.ase.ares.api.securitytest.SecurityTestCaseAbstractFactoryAndBuilder;
import de.tum.cit.ase.ares.api.aop.java.JavaAOPMode;
import de.tum.cit.ase.ares.api.architecture.java.JavaArchitectureMode;
import de.tum.cit.ase.ares.api.buildtoolconfiguration.java.JavaBuildMode;
import de.tum.cit.ase.ares.api.util.FileTools;
import de.tum.cit.ase.ares.api.util.ProjectSourcesFinder;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.Supplier;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static com.google.common.collect.Iterables.isEmpty;
//</editor-fold>

/**
 * Factory and builder class for creating and running Java security test cases.
 * <p>
 * This class is responsible for generating the necessary security test cases based on the
 * provided build tool, AOP mode, and security policy. It also facilitates the writing of
 * these test cases to files and their execution.
 * </p>
 */
public class JavaSecurityTestCaseFactoryAndBuilder implements SecurityTestCaseAbstractFactoryAndBuilder {

    //<editor-fold desc="Attributes">
    /**
     * The build tool used in the project (e.g., Maven or Gradle).
     */
    @Nonnull
    private final JavaBuildMode javaBuildMode;

    /**
     * The architecture mode used in the project (e.g., ArchUnit).
     */
    @Nonnull
    private final JavaArchitectureMode javaArchitectureMode;

    /**
     * The Aspect-Oriented Programming (AOP) mode used in the project (e.g., AspectJ or Instrumentation).
     */
    @Nonnull
    private final JavaAOPMode javaAOPMode;

    /**
     * List of architecture test cases to be generated based on the security policy.
     */
    @Nonnull
    private final List<JavaArchUnitSecurityTestCase> javaArchUnitTestCases = new ArrayList<>();

    /**
     * List of Instrumentation configurations to be generated based on the security policy.
     */
    @Nonnull
    private final List<JavaSecurityTestCase> javaSecurityTestCases = new ArrayList<>();

    /**
     * The package name where the main classes reside.
     */
    @Nonnull
    private final String packageName;

    /**
     * The main class inside the package.
     */
    @Nonnull
    private final String mainClassInPackageName;

    /**
     * The test classes that will be considered in security tests.
     */
    @Nonnull
    private final String[] testClasses;

    /**
     * The functional classes that define behavior and pointcuts.
     */
    @Nonnull
    private final String[] functionClasses;

    /**
     * The resource accesses permitted as defined in the security policy.
     */
    @Nonnull
    private final ResourceAccesses resourceAccesses;

    /**
     * The path within the project where the test cases will be applied.
     */
    private final Path projectPath;
    //</editor-fold>

    //<editor-fold desc="Constructor">

    /**
     * Constructs a new {@link JavaSecurityTestCaseFactoryAndBuilder} with the specified build tool, AOP mode,
     * security policy, and within path.
     *
     * @param javaBuildMode  the build tool used in the project.
     * @param javaAOPMode    the AOP mode used in the project.
     * @param securityPolicy the security policy to be enforced.
     * @param projectPath    the path within the project where the test cases will be applied.
     */
    public JavaSecurityTestCaseFactoryAndBuilder(
            @Nullable JavaBuildMode javaBuildMode,
            @Nullable JavaArchitectureMode javaArchitectureMode,
            @Nullable JavaAOPMode javaAOPMode,
            @Nonnull SecurityPolicy securityPolicy,
            @Nonnull Path projectPath
    ) {
        if (securityPolicy.regardingTheSupervisedCode().theProgrammingLanguageUsesTheFollowingPackage() == null) {
            throw new IllegalArgumentException("Ares Security Error (Reason: Ares-Code; Stage: Execution): The package name cannot be null.");
        } else if (securityPolicy.regardingTheSupervisedCode().theMainClassInsideThisPackageIs() == null) {
            throw new IllegalArgumentException("Ares Security Error (Reason: Ares-Code; Stage: Execution): The main class inside the package cannot be null.");
        }

        if (javaBuildMode == null) {
            throw new SecurityException("Ares Security Error (Reason: Ares-Code; Stage: Creation): The build mode cannot be null.");
        }
        this.javaBuildMode = javaBuildMode;
        if (javaArchitectureMode == null) {
            throw new SecurityException("Ares Security Error (Reason: Ares-Code; Stage: Creation): The architecture mode cannot be null.");
        }
        this.javaArchitectureMode = javaArchitectureMode;
        if (javaAOPMode == null) {
            throw new SecurityException("Ares Security Error (Reason: Ares-Code; Stage: Creation): The AOP mode cannot be null.");
        }
        this.javaAOPMode = javaAOPMode;
        if (securityPolicy.regardingTheSupervisedCode().theProgrammingLanguageUsesTheFollowingPackage() == null) {
            throw new SecurityException("Ares Security Error (Reason: Ares-Code; Stage: Creation): The x cannot be null.");
        }
        this.packageName = securityPolicy.regardingTheSupervisedCode().theProgrammingLanguageUsesTheFollowingPackage();
        if (securityPolicy.regardingTheSupervisedCode().theMainClassInsideThisPackageIs() == null) {
            throw new SecurityException("Ares Security Error (Reason: Ares-Code; Stage: Creation): The x cannot be null.");
        }
        this.mainClassInPackageName = securityPolicy.regardingTheSupervisedCode().theMainClassInsideThisPackageIs();
        if (securityPolicy.regardingTheSupervisedCode().theFollowingResourceAccessesArePermitted() == null) {
            throw new SecurityException("Ares Security Error (Reason: Ares-Code; Stage: Creation): The x cannot be null.");
        }
        this.resourceAccesses = securityPolicy.regardingTheSupervisedCode().theFollowingResourceAccessesArePermitted();
        if (securityPolicy.regardingTheSupervisedCode().theFollowingClassesAreTestClasses() == null) {
            throw new SecurityException("Ares Security Error (Reason: Ares-Code; Stage: Creation): The x cannot be null.");
        }
        this.testClasses = securityPolicy.regardingTheSupervisedCode().theFollowingClassesAreTestClasses();
        // TODO Markus: projectPath is configured wrongly, since for AOP and Architecture tests different paths are used (for Architectural path to bytecode, for AOP path to source code)
        this.projectPath = projectPath;

        this.functionClasses = new String[]{
                packageName + ".api.aop.java.aspectj.adviceandpointcut.JavaAspectJFileSystemAdviceDefinitions",
                packageName + ".api.aop.java.aspectj.adviceandpointcut.JavaAspectJFileSystemPointcutDefinitions",
                packageName + ".api.aop.java.JavaSecurityTestCaseSettings",
                packageName + ".api.aop.java.instrumentation.advice.JavaInstrumentationAdviceToolbox",
                packageName + ".api.aop.java.instrumentation.advice.JavaInstrumentationDeletePathMethodAdvice",
                packageName + ".api.aop.java.instrumentation.advice.JavaInstrumentationExecutePathMethodAdvice",
                packageName + ".api.aop.java.instrumentation.advice.JavaInstrumentationOverwritePathMethodAdvice",
                packageName + ".api.aop.java.instrumentation.advice.JavaInstrumentationReadPathMethodAdvice",
                packageName + ".api.aop.java.instrumentation.advice.JavaInstrumentationDeletePathConstructorAdvice",
                packageName + ".api.aop.java.instrumentation.advice.JavaInstrumentationExecutePathConstructorAdvice",
                packageName + ".api.aop.java.instrumentation.advice.JavaInstrumentationOverwritePathConstructorAdvice",
                packageName + ".api.aop.java.instrumentation.advice.JavaInstrumentationReadPathConstructorAdvice",
                packageName + ".api.aop.java.pointcut.instrumentation.JavaInstrumentationPointcutDefinitions",
                packageName + ".api.aop.java.pointcut.instrumentation.JavaInstrumentationBindingDefinitions",
                packageName + ".api.aop.java.instrumentation.JavaInstrumentationAgent"
        };
        this.createSecurityTestCases();
    }
    //</editor-fold>

    //<editor-fold desc="Create security test cases methods">

    /**
     * Parses the security policy to determine which test cases and configurations need to be created.
     * <p>
     * This method checks the security policy for specific permissions and based on the results,
     * generates the appropriate architecture test cases or AspectJ/Instrumentation configurations.
     * The `methods` array contains suppliers for resource access lists, such as file system interactions and network connections.
     * </p>
     */
    private void createSecurityTestCases() {
        //<editor-fold desc="Delete old rules code">
        //javaAOPMode.reset();
        //</editor-fold>

        //<editor-fold desc="Create fixed rules code">
        Set<SecurityPolicy.PackagePermission> allowedPackages = new HashSet<>(resourceAccesses.regardingPackageImports());
        // Add default imports needed for the execution
        allowedPackages.addAll(Set.of(
                new SecurityPolicy.PackagePermission("java.lang"),
                new SecurityPolicy.PackagePermission("org.java.aspectj"),
                new SecurityPolicy.PackagePermission("org.aspectj"),
                new SecurityPolicy.PackagePermission("de.tum.cit.ase.ares.api.aop.java.aspectj.adviceandpointcut")));
        javaArchUnitTestCases.add(
                new JavaArchUnitSecurityTestCase(
                        JavaArchUnitTestCaseSupported.PACKAGE_IMPORT,
                        allowedPackages
                )
        );
        //</editor-fold>

        //<editor-fold desc="Create variable rules code">
        @Nonnull Supplier<List<?>>[] methods = new Supplier[]{
                (Supplier<List<?>>) resourceAccesses::regardingFileSystemInteractions,
                (Supplier<List<?>>) resourceAccesses::regardingNetworkConnections,
                (Supplier<List<?>>) resourceAccesses::regardingCommandExecutions,
//                (Supplier<List<?>>) resourceAccesses::regardingThreadCreations,
        };
        IntStream
                .range(0, methods.length)
                .forEach(i -> {
                    //<editor-fold desc="Load supported checks code">
                    // TODO Markus: What if JavaArchUnitTestCaseSupported, JavaAspectJSecurityTestCaseSupported and JavaSecurityTestCaseSupported are not in the same order or smaller than methods?
                    JavaArchUnitTestCaseSupported javaArchitectureTestCasesSupportedValue = JavaArchUnitTestCaseSupported.values()[i];
                    JavaSecurityTestCaseSupported javaSecurityTestCaseSupportedValue = JavaSecurityTestCaseSupported.values()[i];
                    //</editor-fold>

                    if (isEmpty(methods[i].get())) {
                        //<editor-fold desc="Architecture test case code">
                        javaArchUnitTestCases.add(new JavaArchUnitSecurityTestCase(javaArchitectureTestCasesSupportedValue));
                        //</editor-fold>
                    } else {
                        //<editor-fold desc="AOP code">
                        javaSecurityTestCases.add(new JavaSecurityTestCase(javaSecurityTestCaseSupportedValue, resourceAccesses));
                        //</editor-fold>
                    }
                });
        //</editor-fold>
    }
    //</editor-fold>

    //<editor-fold desc="Write security test cases methods">

    /**
     * Writes the generated test cases and configurations to files in the specified path.
     * <p>
     * This method copies the relevant files based on the AOP mode, creates the necessary
     * architecture test case files, and combines them with the configuration files.
     * </p>
     *
     * @param projectDirectory the path where the test cases and configurations will be written.
     * @return a list of paths representing the files that were created.
     */
    @Override
    @Nonnull
    public List<Path> writeSecurityTestCases(@Nonnull Path projectDirectory) {
        //<editor-fold desc="Create architecture test case files code">
        @Nonnull ArrayList<Path> javaCopiedArchitectureFiles = new ArrayList<>(FileTools.copyJavaFiles(
                javaArchitectureMode.filesToCopy(),
                javaArchitectureMode.targetsToCopyTo(projectPath, packageName),
                javaArchitectureMode.fileValues(packageName)
        ));
        @Nonnull Path javaArchitectureTestCaseCollectionFile = FileTools.createThreePartedJavaFile(
                javaArchitectureMode.threePartedFileHeader(),
                javaArchitectureMode.threePartedFileBody(javaArchUnitTestCases),
                javaArchitectureMode.threePartedFileFooter(),
                javaArchitectureMode.targetToCopyTo(projectPath, packageName),
                javaArchitectureMode.fileValue(packageName)
        );
        javaCopiedArchitectureFiles.add(javaArchitectureTestCaseCollectionFile);
        //</editor-fold>

        //<editor-fold desc="Create aspect configuration files code">
        @Nonnull ArrayList<Path> javaCopiedAspectFiles = new ArrayList<>(FileTools.copyJavaFiles(
                javaAOPMode.filesToCopy(),
                javaAOPMode.targetsToCopyTo(projectPath, packageName),
                javaAOPMode.fileValues(packageName, mainClassInPackageName)
        ));
        @Nonnull Path javaAspectConfigurationCollectionFile = FileTools.createThreePartedJavaFile(
                javaAOPMode.threePartedFileHeader(),
                javaAOPMode.threePartedFileBody(
                        switch (javaAOPMode) {
                            case ASPECTJ -> "AspectJ";
                            case INSTRUMENTATION -> "Instrumentation";
                        }, packageName
                        , Stream.concat(
                                Arrays.stream(testClasses),
                                Arrays.stream(functionClasses)
                        ).toList(),
                        this.javaSecurityTestCases
                ),
                javaAOPMode.threePartedFileFooter(),
                javaAOPMode.targetToCopyTo(projectPath, packageName),
                javaAOPMode.fileValue(packageName)
        );
        javaCopiedAspectFiles.add(javaAspectConfigurationCollectionFile);
        //</editor-fold>
        //<editor-fold desc="Combine files code">
        return new ArrayList<>(Streams.concat(javaCopiedArchitectureFiles.stream(), javaCopiedAspectFiles.stream()).toList());
        //</editor-fold>
    }
    //</editor-fold>

    //<editor-fold desc="Execute security test cases methods">

    /**
     * Executes the generated security test cases.
     * <p>
     * This method imports the classes from the specified path and runs the security test cases
     * defined in the architecture test cases and aspect configurations. It checks for certain
     * architecture violations and applies aspect-oriented configurations if needed.
     * </p>
     */
    @Override
    public void executeSecurityTestCases() {
        //<editor-fold desc="Load classes code">
        @Nonnull JavaClasses classes = new ClassFileImporter().importPath(Paths.get(ProjectSourcesFinder.isGradleProject() ? "build" : "target", projectPath.toString()).toString());
        //</editor-fold>#

        //<editor-fold desc="Set ArchUnit properties for optimization">
        ArchConfiguration.get().setProperty("archRule.failOnEmptyShould", "false");
        ArchConfiguration.get().setResolveMissingDependenciesFromClassPath(false);
        //</editor-fold>

        //<editor-fold desc="Enforce fixed rules code">
        JavaArchitectureTestCaseCollection.NO_CLASSES_SHOULD_USE_REFLECTION.check(classes);
        JavaArchitectureTestCaseCollection.NO_CLASSES_SHOULD_TERMINATE_JVM.check(classes);
        //</editor-fold>

        //<editor-fold desc="Enforce variable rules code">
        JavaSecurityTestCase.setJavaAdviceSettingValue("aopMode", javaAOPMode.toString(), javaAOPMode.toString());
        JavaSecurityTestCase.setJavaAdviceSettingValue("restrictedPackage", packageName, javaAOPMode.toString());
        JavaSecurityTestCase.setJavaAdviceSettingValue(
                "allowedListedClasses",
                Stream.concat(
                        Arrays.stream(testClasses),
                        ("de.tum.cit.ase.ares").equals(packageName) ? Arrays.stream(functionClasses): Stream.of("de.tum.cit.ase.ares")
                ).toArray(String[]::new),
                javaAOPMode.toString());
        javaArchUnitTestCases.forEach(archTest -> archTest.executeArchitectureTestCase(classes));
        javaSecurityTestCases.forEach(javaSecurityTestCase -> javaSecurityTestCase.executeAOPSecurityTestCase(javaAOPMode.toString()));
        //</editor-fold>
    }
    //</editor-fold>
}