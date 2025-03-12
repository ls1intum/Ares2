package de.tum.cit.ase.ares.api.securitytest.java;

//<editor-fold desc="Imports">

import de.tum.cit.ase.ares.api.architecture.java.JavaArchitectureTestCase;
import de.tum.cit.ase.ares.api.architecture.java.JavaArchitectureMode;
import de.tum.cit.ase.ares.api.aop.java.JavaAOPTestCase;
import de.tum.cit.ase.ares.api.aop.java.JavaAOPMode;
import de.tum.cit.ase.ares.api.buildtoolconfiguration.java.JavaBuildMode;
import de.tum.cit.ase.ares.api.policy.SecurityPolicy;
import de.tum.cit.ase.ares.api.policy.policySubComponents.ResourceAccesses;
import de.tum.cit.ase.ares.api.policy.policySubComponents.SupervisedCode;
import de.tum.cit.ase.ares.api.securitytest.SecurityTestCaseAbstractFactoryAndBuilder;
import de.tum.cit.ase.ares.api.securitytest.java.creator.JavaCreator;
import de.tum.cit.ase.ares.api.securitytest.java.essentialModel.EssentialDataReader;
import de.tum.cit.ase.ares.api.securitytest.java.scanner.JavaScanner;
import de.tum.cit.ase.ares.api.util.FileTools;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;
//</editor-fold>

/**
 * Factory and builder class for creating and executing Java security test cases.
 * <p>
 * This class generates the necessary security test cases based on the provided build tool, architecture mode,
 * AOP mode, and security policy. It writes the generated test cases to files and executes them.
 * <p>
 * Following the Abstract Factory and Builder design patterns, this class delegates the creation
 * of test cases to specialised helper classes while ensuring clear configuration and robust error handling.
 * </p>
 *
 * <p>
 * <strong>Design Patterns:</strong> Abstract Factory, Builder, and Strategy (for test case execution).
 * </p>
 *
 * @author Markus Paulsen
 * @version 3.0.0
 * @since 2.0.0
 */
@SuppressWarnings("FieldCanBeLocal")
public class JavaSecurityTestCaseFactoryAndBuilder implements SecurityTestCaseAbstractFactoryAndBuilder {

    //<editor-fold desc="Attributes">

    //<editor-fold desc="Constants">
    /**
     * The package name where the main classes reside.
     */
    @Nonnull
    private static final String ARES_PACKAGE = "de.tum.cit.ase.ares";
    //</editor-fold>

    //<editor-fold desc="Modes and Project Paths">

    /**
     * The build mode used in the project (e.g., Maven or Gradle).
     */
    @Nonnull
    private final JavaBuildMode javaBuildMode;

    /**
     * The architecture mode used in the project (e.g., ArchUnit or Wala).
     */
    @Nonnull
    private final JavaArchitectureMode javaArchitectureMode;

    /**
     * The Aspect-Oriented Programming (AOP) mode used in the project (e.g., AspectJ or Instrumentation).
     */
    @Nonnull
    private final JavaAOPMode javaAOPMode;

    /**
     * The effective project path where test cases will be generated.
     */
    @Nonnull
    private final Path testPath;
    //</editor-fold>

    //<editor-fold desc="File Based Configuration">

    /**
     * These packages are essential for the execution of the security test cases and are therefore not subject to the security policy.
     */
    @Nonnull
    private final List<String> essentialPackages;

    /**
     * These classes are essential for the execution of the security test cases and are therefore not subject to the security policy.
     */
    @Nonnull
    private final List<String> essentialClasses;
    //</editor-fold>

    //<editor-fold desc="Tested-Domain">

    /**
     * These classes are part of the unrestricted test code and are therefore not subject to the security policy.
     */
    @Nonnull
    private final String[] testClasses;

    /**
     * This package is part of the restricted student code and are therefore subject to the security policy.
     */
    @Nonnull
    private final String packageName;

    /**
     * This main class is part of the restricted student code and are therefore subject to the security policy.
     */
    @Nonnull
    private final String mainClassInPackageName;

    /**
     * The resource accesses permitted as defined by the security policy.
     */
    @Nonnull
    private final ResourceAccesses resourceAccesses;
    //</editor-fold>

    //<editor-fold desc="Generated Test Cases">
    /**
     * List of architecture test cases generated based on the security policy.
     */
    @Nonnull
    private final List<JavaArchitectureTestCase> javaArchitectureTestCases = new ArrayList<>();

    /**
     * List of AOP test cases generated based on the security policy.
     */
    @Nonnull
    private final List<JavaAOPTestCase> javaAOPTestCases = new ArrayList<>();
    //</editor-fold>

    //</editor-fold>

    //<editor-fold desc="Constructor">

    /**
     * Constructs a new JavaSecurityTestCaseFactoryAndBuilder with the provided configuration.
     * <p>
     * This constructor initialises the factory and builder by setting the build mode, architecture mode,
     * AOP mode, essential configurations, and security policy. If the testPath is null, a default path is used.
     * </p>
     *
     * @param javaBuildMode
     *          the build tool used in the project; must not be null.
     * @param javaArchitectureMode
     *          the architecture mode used in the project; must not be null.
     * @param javaAOPMode
     *          the AOP mode used in the project; must not be null.
     * @param essentialReader
     *          the reader for essential configuration; must not be null.
     * @param essentialPackagesPath
     *          the path to the essential packages configuration; must not be null.
     * @param essentialClassesPath
     *          the path to the essential classes configuration; must not be null.
     * @param testPath
     *          the project path where test cases will be generated; if null, a default is used.
     * @param securityPolicy
     *          the security policy to enforce; may be null.
     */
    public JavaSecurityTestCaseFactoryAndBuilder(@Nonnull EssentialDataReader essentialReader, @Nonnull Path essentialPackagesPath, @Nonnull Path essentialClassesPath, @Nonnull JavaScanner javaScanner, @Nullable JavaBuildMode javaBuildMode, @Nullable JavaArchitectureMode javaArchitectureMode, @Nullable JavaAOPMode javaAOPMode, @Nullable Path testPath, @Nullable SecurityPolicy securityPolicy) {

        //<editor-fold desc="Modes and Project Paths">
        this.javaBuildMode = javaBuildMode == null ? javaScanner.scanForBuildMode() : javaBuildMode;
        this.javaArchitectureMode = javaArchitectureMode == null ? JavaArchitectureMode.WALA : javaArchitectureMode;
        this.javaAOPMode = javaAOPMode == null ? JavaAOPMode.INSTRUMENTATION : javaAOPMode;
        this.testPath = testPath == null ? javaScanner.scanForTestPath() : testPath;
        //</editor-fold>

        //<editor-fold desc="File Based Configuration">
        this.essentialPackages = (Objects.requireNonNull(essentialReader, "essentialPackagesReader must not be null")).readEssentialPackagesFrom(Objects.requireNonNull(essentialPackagesPath, "essentialPackagesPath must not be null")).getEssentialPackages();
        this.essentialClasses = (Objects.requireNonNull(essentialReader, "essentialClassesReader must not be null")).readEssentialClassesFrom(Objects.requireNonNull(essentialClassesPath, "essentialClassesPath must not be null")).getEssentialClasses();
        //</editor-fold>

        //<editor-fold desc="Policy Based Configuration">
        SupervisedCode supervisedCode = securityPolicy == null ? null : securityPolicy.regardingTheSupervisedCode();
        this.testClasses = supervisedCode == null ? javaScanner.scanForTestClasses() : supervisedCode.theFollowingClassesAreTestClasses();
        this.packageName = supervisedCode == null || supervisedCode.theSupervisedCodeUsesTheFollowingPackage() == null ? javaScanner.scanForPackageName() : supervisedCode.theSupervisedCodeUsesTheFollowingPackage();
        this.mainClassInPackageName = supervisedCode == null || supervisedCode.theMainClassInsideThisPackageIs() == null ? javaScanner.scanForMainClassInPackage() : supervisedCode.theMainClassInsideThisPackageIs();
        this.resourceAccesses = supervisedCode == null ? ResourceAccesses.createRestrictive() : supervisedCode.theFollowingResourceAccessesArePermitted();
        //</editor-fold>

        //<editor-fold desc="Test Case Creation">
        (new JavaCreator(this.javaBuildMode, this.javaArchitectureMode, this.javaAOPMode, this.essentialPackages, this.essentialClasses, this.testClasses, this.packageName, this.mainClassInPackageName, this.resourceAccesses, this.testPath)).createSecurityTestCases();
        //</editor-fold>
    }
    //</editor-fold>

    //<editor-fold desc="Write security test cases methods">

    /**
     * Writes the generated security test cases to files.
     * <p>
     * This method creates files for both architecture and AOP test cases by utilising specialised file tools.
     * It returns a list of file paths to the generated files.
     * </p>
     *
     * @param projectDirectory
     *          the directory where test case files will be written; may be null.
     * @return a non-null list of {@link Path} objects representing the generated files.
     */
    @Override
    @Nonnull
    public List<Path> writeSecurityTestCases(@Nullable Path projectDirectory) {
        //<editor-fold desc="Create architecture test case files code">
        @Nonnull ArrayList<Path> javaCopiedArchitectureFiles = new ArrayList<>(FileTools.copyJavaFiles(javaArchitectureMode.filesToCopy(), javaArchitectureMode.targetsToCopyTo(testPath, packageName), javaArchitectureMode.fileValues(packageName)));
        @Nonnull Path javaArchitectureTestCaseCollectionFile = FileTools.createThreePartedJavaFile(javaArchitectureMode.threePartedFileHeader(), javaArchitectureMode.threePartedFileBody(javaArchitectureTestCases), javaArchitectureMode.threePartedFileFooter(), javaArchitectureMode.targetToCopyTo(testPath, packageName), javaArchitectureMode.fileValue(packageName));
        javaCopiedArchitectureFiles.add(javaArchitectureTestCaseCollectionFile);
        //</editor-fold>
        //<editor-fold desc="Create aspect configuration files code">
        @Nonnull ArrayList<Path> javaCopiedAspectFiles = new ArrayList<>(FileTools.copyJavaFiles(javaAOPMode.filesToCopy(), javaAOPMode.targetsToCopyTo(testPath, packageName), javaAOPMode.fileValues(packageName, mainClassInPackageName)));
        @Nonnull ArrayList<String> allowedListedClasses = new ArrayList<>(Stream.concat(Arrays.stream(testClasses), essentialClasses.stream()).toList());
        @Nonnull Path javaAspectConfigurationCollectionFile = FileTools.createThreePartedJavaFile(javaAOPMode.threePartedFileHeader(), javaAOPMode.threePartedFileBody(javaAOPMode.toString(), packageName, allowedListedClasses, javaAOPTestCases), javaAOPMode.threePartedFileFooter(), javaAOPMode.targetToCopyTo(testPath, packageName), javaAOPMode.fileValue(packageName));
        javaCopiedAspectFiles.add(javaAspectConfigurationCollectionFile);
        //</editor-fold>
        //<editor-fold desc="Combine files code">
        return new ArrayList<>(Stream.concat(javaCopiedArchitectureFiles.stream(), javaCopiedAspectFiles.stream()).toList());
        //</editor-fold>
    }
    //</editor-fold>

    //<editor-fold desc="Execute security test cases methods">

    /**
     * Sets the value of a Java advice setting.
     * <p>
     * This method sets the value of a Java advice setting based on the provided key and value.
     * </p>
     *
     * @param key
     *          the key of the setting to set; must not be null.
     * @param value
     *          the value to set for the setting; may be null.
     */
    private void setJavaAdviceSettingValue(String key, Object value) {
        JavaAOPTestCase.setJavaAdviceSettingValue(key, value, javaArchitectureMode.toString(), javaAOPMode.toString());
    }

    /**
     * Executes the generated security test cases.
     * <p>
     * This method sets up the necessary test configurations and then sequentially executes the architecture
     * and AOP test cases.
     * </p>
     */
    @Override
    public void executeSecurityTestCases() {
        //<editor-fold desc="Generate Values">
        String javaArchitectureModeString = javaArchitectureMode.toString();
        String javaAOPModeString = javaAOPMode.toString();
        String[] allowdListedClassesString = Stream.concat(Arrays.stream(testClasses), ARES_PACKAGE.equals(packageName) ? essentialClasses.stream() : Stream.of(ARES_PACKAGE)).toArray(String[]::new);
        //</editor-fold>
        //<editor-fold desc="Set Values">
        Map.of("architectureMode", javaArchitectureModeString, "aopMode", javaAOPModeString, "restrictedPackage", packageName, "allowedListedClasses", allowdListedClassesString).forEach(this::setJavaAdviceSettingValue);
        //</editor-fold>
        //<editor-fold desc="Execute architecture test cases">
        javaArchitectureTestCases.forEach(javaArchitectureTestCase -> javaArchitectureTestCase.executeArchitectureTestCase(javaArchitectureModeString, javaAOPModeString));
        //</editor-fold>
        //<editor-fold desc="Execute AOP test cases">
        javaAOPTestCases.forEach(javaSecurityTestCase -> javaSecurityTestCase.executeAOPSecurityTestCase(javaArchitectureModeString, javaAOPModeString));
        //</editor-fold>
    }
    //</editor-fold>
}