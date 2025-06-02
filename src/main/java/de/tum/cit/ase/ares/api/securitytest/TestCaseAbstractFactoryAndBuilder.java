package de.tum.cit.ase.ares.api.securitytest;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;
import de.tum.cit.ase.ares.api.aop.AOPMode;
import de.tum.cit.ase.ares.api.aop.AOPTestCase;
import de.tum.cit.ase.ares.api.architecture.ArchitectureMode;
import de.tum.cit.ase.ares.api.architecture.ArchitectureTestCase;
import de.tum.cit.ase.ares.api.buildtoolconfiguration.BuildMode;
import de.tum.cit.ase.ares.api.policy.SecurityPolicy;
import de.tum.cit.ase.ares.api.policy.policySubComponents.ResourceAccesses;
import de.tum.cit.ase.ares.api.policy.policySubComponents.SupervisedCode;
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

/**
 * Security test case factory and builder.
 *
 * <p>Description: Factory and builder interface for producing and executing security test cases in any programming language.
 * This interface combines elements of the Abstract Factory and Builder design patterns to generate, configure, and execute security test cases.
 * Implementations are responsible for creating security test case instances, writing them to files, and executing them according to a specified security policy.
 *
 * <p>Design Rationale: Using a combined factory and builder approach provides flexibility in test case creation and execution,
 * while promoting modular design and adherence to the Single Responsibility Principle.
 *
 * @since 2.0.0
 * @author Markus Paulsen
 * @version 2.0.0
 */
public abstract class TestCaseAbstractFactoryAndBuilder {

    //<editor-fold desc="Attributes">

    //<editor-fold desc="Tools">
    @Nonnull
    protected final Creator creator;

    @Nonnull
    protected final Writer writer;

    @Nonnull
    protected final Executer executer;

    @Nonnull
    protected final EssentialDataReader essentialDataReader;

    @Nonnull
    protected final ProjectScanner projectScanner;
    //</editor-fold>

    //<editor-fold desc="Modes and Project Paths">

    /**
     * The build mode used in the project (e.g., Maven or Gradle).
     */
    @Nonnull
    protected final BuildMode buildMode;

    /**
     * The architecture mode used in the project (e.g., ArchUnit or Wala).
     */
    @Nonnull
    protected final ArchitectureMode architectureMode;

    /**
     * The Aspect-Oriented Programming (AOP) mode used in the project (e.g., AspectJ or Instrumentation).
     */
    @Nonnull
    protected final AOPMode aopMode;

    /**
     * The effective project path where test cases will be generated.
     */
    @Nullable
    protected final Path projectPath;
    //</editor-fold>

    //<editor-fold desc="Essential Data">
    /**
     * Path to the essential packages' configuration.
     */
    @Nonnull
    protected final Path essentialPackagesPath;

    /**
     * Path to the essential classes' configuration.
     */
    @Nonnull
    protected final Path essentialClassesPath;

    /**
     * These packages are essential for the execution of the security test cases and are therefore not subject to the security policy.
     */
    @Nonnull
    protected final List<String> essentialPackages;

    /**
     * These classes are essential for the execution of the security test cases and are therefore not subject to the security policy.
     */
    @Nonnull
    protected final List<String> essentialClasses;
    //</editor-fold>

    //<editor-fold desc="Configuration">
    /**
     * These classes are part of the unrestricted test code and are therefore not subject to the security policy.
     */
    @Nonnull
    protected final List<String> testClasses;

    /**
     * The resource accesses permitted as defined by the security policy.
     */
    @Nonnull
    protected final ResourceAccesses resourceAccesses;

    /**
     * This package is part of the restricted student code and are therefore subject to the security policy.
     */
    @Nonnull
    protected final String packageName;

    /**
     * This main class is part of the restricted student code and are therefore subject to the security policy.
     */
    @Nonnull
    protected final String mainClassInPackageName;
    //</editor-fold>

    //<editor-fold desc="Test lists">
    /**
     * List of architecture test cases generated based on the security policy.
     */
    @Nonnull
    protected final List<ArchitectureTestCase> architectureTestCases = new ArrayList<>();

    /**
     * List of AOP test cases generated based on the security policy.
     */
    @Nonnull
    protected final List<AOPTestCase> aopTestCases = new ArrayList<>();
    //</editor-fold>

    //</editor-fold>

    //<editor-fold desc="Constructor">

    /**
     * Constructs a new JavaTestCaseFactoryAndBuilder with the provided configuration.
     * <p>
     * This constructor initialises the factory and builder by setting the build mode, architecture mode,
     * AOP mode, essential configurations, and security policy. If the testPath is null, a default path is used.
     * </p>
     *
     * @param buildMode
     *          the build tool used in the project; must not be null.
     * @param architectureMode
     *          the architecture mode used in the project; must not be null.
     * @param aopMode
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
    public TestCaseAbstractFactoryAndBuilder(
            @Nonnull Creator creator, @Nonnull Writer writer, @Nonnull Executer executer,
            @Nonnull EssentialDataReader essentialDataReader, @Nonnull ProjectScanner projectScanner,
            @Nonnull Path essentialPackagesPath, @Nonnull Path essentialClassesPath,
            @Nullable BuildMode buildMode, @Nullable ArchitectureMode architectureMode, @Nullable AOPMode aopMode,
            @Nullable SecurityPolicy securityPolicy, @Nullable Path projectPath
    ) {

        //<editor-fold desc="Tools">
        this.creator = Preconditions.checkNotNull(creator);
        this.writer = Preconditions.checkNotNull(writer);
        this.executer = Preconditions.checkNotNull(executer);
        this.essentialDataReader = Preconditions.checkNotNull(essentialDataReader);
        this.projectScanner = Preconditions.checkNotNull(projectScanner);
        //</editor-fold>

        //<editor-fold desc="Modes and Project Paths">
        this.buildMode = MoreObjects.firstNonNull(buildMode, projectScanner.scanForBuildMode());
        this.architectureMode = MoreObjects.firstNonNull(architectureMode, ArchitectureMode.WALA);
        this.aopMode = MoreObjects.firstNonNull(aopMode, AOPMode.INSTRUMENTATION);
        this.projectPath = projectPath;
        //</editor-fold>

        //<editor-fold desc="Essential Data">
        this.essentialPackagesPath = Preconditions.checkNotNull(essentialPackagesPath, "essentialPackagesPath must not be null");
        this.essentialClassesPath = Preconditions.checkNotNull(essentialClassesPath, "essentialClassesPath must not be null");
        this.essentialPackages = Preconditions.checkNotNull(essentialDataReader, "essentialPackagesReader must not be null").readEssentialPackagesFrom(this.essentialPackagesPath).getEssentialPackages();
        this.essentialClasses = Preconditions.checkNotNull(essentialDataReader, "essentialClassesReader must not be null").readEssentialClassesFrom(this.essentialClassesPath).getEssentialClasses();
        //</editor-fold>

        //<editor-fold desc="Configuration">
        final SupervisedCode supervisedCode = Optional.ofNullable(securityPolicy).map(SecurityPolicy::regardingTheSupervisedCode).orElse(null);
        final Optional<SupervisedCode> supervisedCodeOptional = Optional.ofNullable(supervisedCode);
        String[] testClassesArray = supervisedCodeOptional.map(SupervisedCode::theFollowingClassesAreTestClasses).orElseGet(projectScanner::scanForTestClasses);
        this.packageName = supervisedCodeOptional.map(SupervisedCode::theSupervisedCodeUsesTheFollowingPackage).orElseGet(projectScanner::scanForPackageName);
        this.mainClassInPackageName = supervisedCodeOptional.map(SupervisedCode::theMainClassInsideThisPackageIs).orElseGet(projectScanner::scanForMainClassInPackage);
        this.resourceAccesses = supervisedCodeOptional.map(SupervisedCode::theFollowingResourceAccessesArePermitted).orElseGet(ResourceAccesses::createRestrictive);
        this.testClasses = new ArrayList<>(Arrays.asList(testClassesArray));

        //</editor-fold>

        //<editor-fold desc="Test Case Creation">
        this.creator.createTestCases(
                this.buildMode,
                this.architectureMode,
                this.aopMode,
                this.essentialPackages,
                this.essentialClasses,
                this.testClasses,
                this.packageName,
                this.mainClassInPackageName,
                this.architectureTestCases,
                this.aopTestCases,
                this.resourceAccesses,
                this.projectPath
        );
        //</editor-fold>
    }
    //</editor-fold>
    //<editor-fold desc="Abstract Methods">

    /**
     * Writes the generated security test cases to files.
     *
     * @since 2.0.0
     * @author Markus Paulsen
     * @param testFolderPath the target directory where Ares 2 saves test case files. It may be null.
     * @return a non-null list of Path objects representing the generated test case files.
     */
    @Nonnull
    public abstract List<Path> writeTestCases(@Nullable Path testFolderPath);

    /**
     * Executes the generated security test cases.
     *
     * @since 2.0.0
     * @author Markus Paulsen
     */
    public abstract void executeTestCases();
    //</editor-fold>

}