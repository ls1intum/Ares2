package de.tum.cit.ase.ares.api.securitytest.java;

//<editor-fold desc="Imports">

import com.google.common.base.MoreObjects;
import de.tum.cit.ase.ares.api.architecture.java.JavaArchitectureTestCase;
import de.tum.cit.ase.ares.api.architecture.java.JavaArchitectureMode;
import de.tum.cit.ase.ares.api.aop.java.JavaAOPTestCase;
import de.tum.cit.ase.ares.api.aop.java.JavaAOPMode;
import de.tum.cit.ase.ares.api.buildtoolconfiguration.java.JavaBuildMode;
import de.tum.cit.ase.ares.api.policy.SecurityPolicy;
import de.tum.cit.ase.ares.api.policy.policySubComponents.ResourceAccesses;
import de.tum.cit.ase.ares.api.policy.policySubComponents.SupervisedCode;
import de.tum.cit.ase.ares.api.securitytest.SecurityTestCaseAbstractFactoryAndBuilder;
import de.tum.cit.ase.ares.api.securitytest.java.creator.Creator;
import de.tum.cit.ase.ares.api.securitytest.java.essentialModel.EssentialDataReader;
import de.tum.cit.ase.ares.api.securitytest.java.executer.Executer;
import de.tum.cit.ase.ares.api.securitytest.java.projectScanner.ProjectScanner;
import de.tum.cit.ase.ares.api.securitytest.java.writer.Writer;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
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
public class JavaSecurityTestCaseFactoryAndBuilder extends SecurityTestCaseAbstractFactoryAndBuilder {

    //<editor-fold desc="Attributes">

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
    //</editor-fold>

    //<editor-fold desc="Tested-Domain">

    /**
     * These classes are part of the unrestricted test code and are therefore not subject to the security policy.
     */
    @Nonnull
    private final List<String> testClasses;

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
     * @param essentialDataReader
     *          the reader for essential configuration; must not be null.
     * @param essentialPackagesPath
     *          the path to the essential packages configuration; must not be null.
     * @param essentialClassesPath
     *          the path to the essential classes configuration; must not be null.
     * @param projectPath
     *          the project path where test cases will be generated; if null, a default is used.
     * @param securityPolicy
     *          the security policy to enforce; may be null.
     */
    public JavaSecurityTestCaseFactoryAndBuilder(
            @Nonnull Creator creator, @Nonnull Writer writer, @Nonnull Executer executer, @Nonnull EssentialDataReader essentialDataReader, @Nonnull ProjectScanner projectScanner,
            @Nonnull Path essentialPackagesPath, @Nonnull Path essentialClassesPath,
            @Nullable JavaBuildMode javaBuildMode, @Nullable JavaArchitectureMode javaArchitectureMode, @Nullable JavaAOPMode javaAOPMode,
            @Nullable SecurityPolicy securityPolicy, @Nullable Path projectPath
            ) {

        super(
                creator, writer, executer,
                essentialDataReader, projectScanner,
                essentialPackagesPath, essentialClassesPath,
                securityPolicy, projectPath
        );

        //<editor-fold desc="Modes and Project Paths">
        this.javaBuildMode = MoreObjects.firstNonNull(javaBuildMode, projectScanner.scanForBuildMode());
        this.javaArchitectureMode = MoreObjects.firstNonNull(javaArchitectureMode, JavaArchitectureMode.WALA);
        this.javaAOPMode = MoreObjects.firstNonNull(javaAOPMode, JavaAOPMode.INSTRUMENTATION);
        //</editor-fold>

        //<editor-fold desc="Policy Based Configuration">
        final SupervisedCode supervisedCode = Optional.ofNullable(securityPolicy)
                .map(SecurityPolicy::regardingTheSupervisedCode)
                .orElse(null);
        this.testClasses = new ArrayList<>(Arrays.asList(Optional.ofNullable(supervisedCode)
                .map(SupervisedCode::theFollowingClassesAreTestClasses)
                .orElseGet(projectScanner::scanForTestClasses)));
        this.packageName = Optional.ofNullable(supervisedCode)
                .map(SupervisedCode::theSupervisedCodeUsesTheFollowingPackage)
                .orElseGet(projectScanner::scanForPackageName);
        this.mainClassInPackageName = Optional.ofNullable(supervisedCode)
                .map(SupervisedCode::theMainClassInsideThisPackageIs)
                .orElseGet(projectScanner::scanForMainClassInPackage);
        this.resourceAccesses = Optional.ofNullable(supervisedCode)
                .map(SupervisedCode::theFollowingResourceAccessesArePermitted)
                .orElseGet(ResourceAccesses::createRestrictive);
        //</editor-fold>

        //<editor-fold desc="Test Case Creation">
        this.creator.createSecurityTestCases(
                this.javaBuildMode,
                this.javaArchitectureMode,
                this.javaAOPMode,
                this.essentialPackages,
                this.essentialClasses,
                this.testClasses,
                this.packageName,
                this.mainClassInPackageName,
                this.javaArchitectureTestCases,
                this.javaAOPTestCases,
                this.resourceAccesses,
                this.projectPath
        );
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
        return writer.writeSecurityTestCases(
                javaBuildMode,
                javaArchitectureMode,
                javaAOPMode,
                essentialPackages,
                essentialClasses,
                testClasses,
                packageName,
                mainClassInPackageName,
                javaArchitectureTestCases,
                javaAOPTestCases,
                projectDirectory
        );
    }
    //</editor-fold>

    //<editor-fold desc="Execute security test cases methods">

    /**
     * Executes the generated security test cases.
     * <p>
     * This method sets up the necessary test configurations and then sequentially executes the architecture
     * and AOP test cases.
     * </p>
     */
    @Override
    public void executeSecurityTestCases() {
        executer.executeSecurityTestCases(
                javaBuildMode,
                javaArchitectureMode,
                javaAOPMode,
                essentialPackages,
                essentialClasses,
                testClasses,
                packageName,
                mainClassInPackageName,
                javaArchitectureTestCases,
                javaAOPTestCases
        );
    }
    //</editor-fold>
}