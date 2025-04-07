package de.tum.cit.ase.ares.api.securitytest;

import com.google.common.base.Preconditions;
import de.tum.cit.ase.ares.api.policy.SecurityPolicy;
import de.tum.cit.ase.ares.api.securitytest.java.creator.Creator;
import de.tum.cit.ase.ares.api.securitytest.java.essentialModel.EssentialDataReader;
import de.tum.cit.ase.ares.api.securitytest.java.executer.Executer;
import de.tum.cit.ase.ares.api.securitytest.java.projectScanner.ProjectScanner;
import de.tum.cit.ase.ares.api.securitytest.java.writer.Writer;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.nio.file.Path;
import java.util.List;

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
public abstract class SecurityTestCaseAbstractFactoryAndBuilder {

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

    //<editor-fold desc="File Based Configuration">

    /**
     * Path to the essential packages' configuration.
     */
    @Nonnull
    protected final Path essentialPackagesPath;

    /**
     * These packages are essential for the execution of the security test cases and are therefore not subject to the security policy.
     */
    @Nonnull
    protected final List<String> essentialPackages;

    /**
     * Path to the essential classes' configuration.
     */
    @Nonnull
    protected final Path essentialClassesPath;

    /**
     * These classes are essential for the execution of the security test cases and are therefore not subject to the security policy.
     */
    @Nonnull
    protected final List<String> essentialClasses;
    //</editor-fold>

    /**
     * The effective project path where test cases will be generated.
     */
    @Nullable
    protected final Path projectPath;

    //</editor-fold>

    //<editor-fold desc="Constructor">

    /**
     * Constructs a new JavaSecurityTestCaseFactoryAndBuilder with the provided configuration.
     * <p>
     * This constructor initialises the factory and builder by setting the build mode, architecture mode,
     * AOP mode, essential configurations, and security policy. If the testPath is null, a default path is used.
     * </p>
     *
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
    public SecurityTestCaseAbstractFactoryAndBuilder(
            @Nonnull Creator creator, @Nonnull Writer writer, @Nonnull Executer executer,
            @Nonnull EssentialDataReader essentialDataReader, @Nonnull ProjectScanner projectScanner,
            @Nonnull Path essentialPackagesPath, @Nonnull Path essentialClassesPath,
            @Nullable SecurityPolicy securityPolicy, @Nullable Path projectPath
    ) {

        //<editor-fold desc="Tools">
        this.creator = Preconditions.checkNotNull(creator);
        this.writer = Preconditions.checkNotNull(writer);
        this.executer = Preconditions.checkNotNull(executer);
        this.essentialDataReader = Preconditions.checkNotNull(essentialDataReader);
        this.projectScanner = Preconditions.checkNotNull(projectScanner);
        //</editor-fold>

        //<editor-fold desc="File Based Configuration">
        this.essentialPackagesPath = Preconditions.checkNotNull(essentialPackagesPath, "essentialPackagesPath must not be null");
        this.essentialPackages = Preconditions.checkNotNull(essentialDataReader, "essentialPackagesReader must not be null")
                .readEssentialPackagesFrom(this.essentialPackagesPath)
                .getEssentialPackages();
        this.essentialClassesPath = Preconditions.checkNotNull(essentialClassesPath, "essentialClassesPath must not be null");
        this.essentialClasses = Preconditions.checkNotNull(essentialDataReader, "essentialClassesReader must not be null")
                .readEssentialClassesFrom(this.essentialClassesPath)
                .getEssentialClasses();
        //</editor-fold>

        this.projectPath = projectPath;
    }
    //</editor-fold>

    //<editor-fold desc="Abstract Methods">

    /**
     * Writes the generated security test cases to files.
     *
     * @since 2.0.0
     * @author Markus Paulsen
     * @param projectDirectory the target directory where Ares 2 saves test case files. It may be null.
     * @return a non-null list of Path objects representing the generated test case files.
     */
    @Nonnull
    public abstract List<Path> writeSecurityTestCases(@Nullable Path projectDirectory);

    /**
     * Executes the generated security test cases.
     *
     * @since 2.0.0
     * @author Markus Paulsen
     */
    public abstract void executeSecurityTestCases();
    //</editor-fold>

}