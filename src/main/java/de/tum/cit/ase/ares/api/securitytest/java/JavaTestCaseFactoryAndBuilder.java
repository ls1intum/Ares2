package de.tum.cit.ase.ares.api.securitytest.java;

//<editor-fold desc="Imports">

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;
import de.tum.cit.ase.ares.api.aop.java.JavaAOPTestCase;
import de.tum.cit.ase.ares.api.architecture.ArchitectureMode;
import de.tum.cit.ase.ares.api.aop.AOPMode;
import de.tum.cit.ase.ares.api.architecture.java.JavaArchitectureTestCase;
import de.tum.cit.ase.ares.api.buildtoolconfiguration.BuildMode;
import de.tum.cit.ase.ares.api.policy.SecurityPolicy;
import de.tum.cit.ase.ares.api.policy.policySubComponents.ResourceAccesses;
import de.tum.cit.ase.ares.api.policy.policySubComponents.SupervisedCode;
import de.tum.cit.ase.ares.api.securitytest.TestCaseAbstractFactoryAndBuilder;
import de.tum.cit.ase.ares.api.securitytest.java.creator.Creator;
import de.tum.cit.ase.ares.api.securitytest.java.creator.JavaCreator;
import de.tum.cit.ase.ares.api.securitytest.java.essentialModel.EssentialDataReader;
import de.tum.cit.ase.ares.api.securitytest.java.executer.Executer;
import de.tum.cit.ase.ares.api.securitytest.java.executer.JavaExecuter;
import de.tum.cit.ase.ares.api.securitytest.java.projectScanner.JavaProjectScanner;
import de.tum.cit.ase.ares.api.securitytest.java.projectScanner.ProjectScanner;
import de.tum.cit.ase.ares.api.securitytest.java.writer.JavaWriter;
import de.tum.cit.ase.ares.api.securitytest.java.writer.Writer;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
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
public class JavaTestCaseFactoryAndBuilder extends TestCaseAbstractFactoryAndBuilder {

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
    public JavaTestCaseFactoryAndBuilder(
            @Nonnull JavaCreator creator, @Nonnull JavaWriter writer, @Nonnull JavaExecuter executer,
            @Nonnull EssentialDataReader essentialDataReader, @Nonnull JavaProjectScanner projectScanner,
            @Nonnull Path essentialPackagesPath, @Nonnull Path essentialClassesPath,
            @Nullable BuildMode buildMode, @Nullable ArchitectureMode architectureMode, @Nullable AOPMode aopMode,
            @Nullable SecurityPolicy securityPolicy, @Nullable Path projectPath
    ) {
        super(creator, writer, executer, essentialDataReader, projectScanner, essentialPackagesPath, essentialClassesPath,
                buildMode, architectureMode, aopMode, securityPolicy, projectPath);
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
     * @param testFolderPath
     *          the directory where test case files will be written; may be null.
     * @return a non-null list of {@link Path} objects representing the generated files.
     */
    @Override
    @Nonnull
    public List<Path> writeTestCases(@Nullable Path testFolderPath) {
        return writer.writeTestCases(
                buildMode,
                architectureMode,
                aopMode,
                essentialPackages,
                essentialClasses,
                testClasses,
                packageName,
                mainClassInPackageName,
                this.architectureTestCases.stream().map(architectureTestCase -> (JavaArchitectureTestCase) architectureTestCase).toList(),
                this.aopTestCases.stream().map(aopTestCase -> (JavaAOPTestCase) aopTestCase).toList(),
                testFolderPath
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
    public void executeTestCases() {
        executer.executeTestCases(
                buildMode,
                architectureMode,
                aopMode,
                essentialPackages,
                essentialClasses,
                testClasses,
                packageName,
                mainClassInPackageName,
                this.architectureTestCases.stream().map(architectureTestCase -> (JavaArchitectureTestCase) architectureTestCase).toList(),
                this.aopTestCases.stream().map(aopTestCase -> (JavaAOPTestCase) aopTestCase).toList()
        );
    }
    //</editor-fold>

    //<editor-fold desc="Builder">
    @Nonnull
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        @Nullable private JavaCreator creator;
        @Nullable private JavaWriter writer;
        @Nullable private JavaExecuter executer;
        @Nullable private EssentialDataReader essentialDataReader;
        @Nullable private JavaProjectScanner projectScanner;
        @Nullable private Path essentialPackagesPath;
        @Nullable private Path essentialClassesPath;
        @Nullable private BuildMode buildMode;
        @Nullable private ArchitectureMode architectureMode;
        @Nullable private AOPMode aopMode;
        @Nullable private SecurityPolicy securityPolicy;
        @Nullable private Path projectPath;

        public Builder creator(@Nonnull JavaCreator creator) {
            this.creator = Objects.requireNonNull(creator, "creator must not be null");
            return this;
        }
        public Builder writer(@Nonnull JavaWriter writer) {
            this.writer = Objects.requireNonNull(writer, "writer must not be null");
            return this;
        }
        public Builder executer(@Nonnull JavaExecuter executer) {
            this.executer = Objects.requireNonNull(executer, "executer must not be null");
            return this;
        }
        public Builder essentialDataReader(@Nonnull EssentialDataReader essentialDataReader) {
            this.essentialDataReader = Objects.requireNonNull(essentialDataReader, "essentialDataReader must not be null");
            return this;
        }
        public Builder projectScanner(@Nonnull JavaProjectScanner projectScanner) {
            this.projectScanner = Objects.requireNonNull(projectScanner, "projectScanner must not be null");
            return this;
        }
        public Builder essentialPackagesPath(@Nonnull Path essentialPackagesPath) {
            this.essentialPackagesPath = Objects.requireNonNull(essentialPackagesPath, "essentialPackagesPath must not be null");
            return this;
        }
        public Builder essentialClassesPath(@Nonnull Path essentialClassesPath) {
            this.essentialClassesPath = Objects.requireNonNull(essentialClassesPath, "essentialClassesPath must not be null");
            return this;
        }
        public Builder buildMode(@Nullable BuildMode buildMode) {
            this.buildMode = buildMode;
            return this;
        }
        public Builder architectureMode(@Nullable ArchitectureMode architectureMode) {
            this.architectureMode = architectureMode;
            return this;
        }
        public Builder aopMode(@Nullable AOPMode aopMode) {
            this.aopMode = aopMode;
            return this;
        }
        public Builder securityPolicy(@Nullable SecurityPolicy securityPolicy) {
            this.securityPolicy = securityPolicy;
            return this;
        }
        public Builder projectPath(@Nullable Path projectPath) {
            this.projectPath = projectPath;
            return this;
        }

        @Nonnull
        public JavaTestCaseFactoryAndBuilder build() {
            return new JavaTestCaseFactoryAndBuilder(
                    java.util.Objects.requireNonNull(creator, "creator must not be null"),
                    java.util.Objects.requireNonNull(writer, "writer must not be null"),
                    java.util.Objects.requireNonNull(executer, "executer must not be null"),
                    java.util.Objects.requireNonNull(essentialDataReader, "essentialDataReader must not be null"),
                    java.util.Objects.requireNonNull(projectScanner, "projectScanner must not be null"),
                    java.util.Objects.requireNonNull(essentialPackagesPath, "essentialPackagesPath must not be null"),
                    java.util.Objects.requireNonNull(essentialClassesPath, "essentialClassesPath must not be null"),
                    buildMode,
                    architectureMode,
                    aopMode,
                    securityPolicy,
                    projectPath
            );
        }
    }
    //</editor-fold>
}