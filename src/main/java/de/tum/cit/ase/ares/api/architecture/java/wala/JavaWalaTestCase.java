package de.tum.cit.ase.ares.api.architecture.java.wala;

//<editor-fold desc="Imports">

import com.google.common.base.Preconditions;
import com.ibm.wala.ipa.callgraph.CallGraph;
import com.tngtech.archunit.core.domain.JavaClasses;
import de.tum.cit.ase.ares.api.architecture.java.JavaArchitectureTestCase;
import de.tum.cit.ase.ares.api.architecture.java.JavaArchitectureTestCaseSupported;
import de.tum.cit.ase.ares.api.localization.Messages;
import de.tum.cit.ase.ares.api.policy.policySubComponents.PackagePermission;
import de.tum.cit.ase.ares.api.util.FileTools;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Set;
//</editor-fold>

/**
 * Architecture test case for the Java programming language using WALA and concrete product of the abstract factory design pattern.
 *
 * @author Sarp Sahinalp
 * @version 2.0.0
 * @see <a href="https://refactoring.guru/design-patterns/abstract-factory">Abstract Factory Design Pattern</a>
 * @since 2.0.0
 */
public class JavaWalaTestCase extends JavaArchitectureTestCase {

    //<editor-fold desc="Constructors">

    public JavaWalaTestCase(
            @Nonnull JavaArchitectureTestCaseSupported javaArchitectureTestCaseSupported,
            @Nonnull Set<PackagePermission> allowedPackages,
            @Nonnull JavaClasses javaClasses,
            @Nonnull CallGraph callGraph
    ) {
        super(javaArchitectureTestCaseSupported, allowedPackages, javaClasses, callGraph);
    }


    //</editor-fold>

    //<editor-fold desc="Write security test case methods">

    /**
     * Returns the content of the architecture test case file in the Java programming language.
     */
    @Override
    @Nonnull
    public String writeArchitectureTestCase(@Nonnull String architectureMode, @Nonnull String aopMode) {
        try {
            return FileTools.readRuleFile(
                    Paths.get("de", "tum", "cit", "ase", "ares", "api",
                            "templates", "architecture", "java", "wala", "rules", ((JavaArchitectureTestCaseSupported) this.architectureTestCaseSupported).name() + ".txt")
            ).stream().reduce("", (acc, line) -> acc + line + "\n");
        } catch (AssertionError | IOException e) {
            throw new SecurityException("Ares Security Error (Reason: Student-Code; Stage: Execution): Illegal Statement found: " + e.getMessage());
        }
    }
    //</editor-fold>

    //<editor-fold desc="Execute security test case methods">

    /**
     * Executes the architecture test case.
     */
    @Override
    public void executeArchitectureTestCase(@Nonnull String architectureMode, @Nonnull String aopMode) {
        try {
            switch ((JavaArchitectureTestCaseSupported) this.architectureTestCaseSupported) {
                case FILESYSTEM_INTERACTION -> JavaWalaTestCaseCollection
                        .NO_CLASS_MUST_ACCESS_FILE_SYSTEM
                        .check(callGraph);
                case NETWORK_CONNECTION -> JavaWalaTestCaseCollection
                        .NO_CLASS_MUST_ACCESS_NETWORK
                        .check(callGraph);
                case THREAD_CREATION -> JavaWalaTestCaseCollection
                        .NO_CLASS_MUST_CREATE_THREADS
                        .check(callGraph);
                case COMMAND_EXECUTION -> JavaWalaTestCaseCollection
                        .NO_CLASS_MUST_EXECUTE_COMMANDS
                        .check(callGraph);
                case PACKAGE_IMPORT -> JavaWalaTestCaseCollection
                        .noClassMustImportForbiddenPackages(javaClasses, allowedPackages);
                case REFLECTION -> JavaWalaTestCaseCollection
                        .NO_CLASS_MUST_USE_REFLECTION
                        .check(callGraph);
                case TERMINATE_JVM -> JavaWalaTestCaseCollection
                        .NO_CLASS_MUST_TERMINATE_JVM
                        .check(callGraph);
                case SERIALIZATION -> JavaWalaTestCaseCollection
                        .NO_CLASS_MUST_SERIALIZE
                        .check(callGraph);
                case CLASS_LOADING -> JavaWalaTestCaseCollection
                        .NO_CLASS_MUST_USE_CLASSLOADERS
                        .check(callGraph);
                default ->
                        throw new SecurityException(Messages.localized("security.common.unsupported.operation", this.architectureTestCaseSupported));
            }
        } catch (AssertionError e) {
            JavaArchitectureTestCase.parseErrorMessage(e);
        }
    }
    //</editor-fold>

    //<editor-fold desc="Builder">

    /**
     * Creates a new builder instance for constructing JavaArchitectureTestCase objects.
     *
     * @since 2.0.0
     * @author Sarp Sahinalp
     * @return A new Builder instance
     */
    @Nonnull
    public static JavaWalaTestCase.Builder walaBuilder() {
        return new JavaWalaTestCase.Builder();
    }

    /**
     * Builder for the Java architecture test case.
     */
    public static class Builder {
        @Nullable
        private JavaArchitectureTestCaseSupported javaArchitectureTestCaseSupported;
        @Nullable
        private JavaClasses javaClasses;
        @Nullable
        private CallGraph callGraph;
        @Nullable
        private Set<PackagePermission> allowedPackages;

        /**
         * Sets the architecture test case type supported by this instance.
         *
         * @since 2.0.0
         * @author Sarp Sahinalp
         * @param javaArchitectureTestCaseSupported The type of architecture test case to support
         * @return This builder instance for method chaining
         * @throws SecurityException if the parameter is null
         */
        @Nonnull
        public JavaWalaTestCase.Builder javaArchitectureTestCaseSupported(@Nonnull JavaArchitectureTestCaseSupported javaArchitectureTestCaseSupported) {
            this.javaArchitectureTestCaseSupported = Preconditions.checkNotNull(javaArchitectureTestCaseSupported, "javaArchitecturalTestCaseSupported must not be null");
            return this;
        }

        /**
         * Sets the Java classes to be analyzed by this architecture test case.
         *
         * @since 2.0.0
         * @author Sarp Sahinalp
         * @param javaClasses Collection of Java classes for analysis
         * @return This builder instance for method chaining
         */
        @Nonnull
        public JavaWalaTestCase.Builder javaClasses(@Nonnull JavaClasses javaClasses) {
            this.javaClasses = Preconditions.checkNotNull(javaClasses, "javaClasses must not be null");
            return this;
        }

        /**
         * Sets the call graph to be used for analysis.
         * Required for WALA mode but not for ARCHUNIT mode.
         *
         * @since 2.0.0
         * @author Sarp Sahinalp
         * @param callGraph Call graph representing method relationships
         * @return This builder instance for method chaining
         */
        @Nonnull
        public JavaWalaTestCase.Builder callGraph(@Nullable CallGraph callGraph) {
            this.callGraph = callGraph;
            return this;
        }

        /**
         * Sets the allowed package permissions.
         *
         * @since 2.0.0
         * @author Sarp Sahinalp
         * @param allowedPackages Set of package permissions that should be allowed
         * @return This builder instance for method chaining
         */
        @Nonnull
        public JavaWalaTestCase.Builder allowedPackages(@Nonnull Set<PackagePermission> allowedPackages) {
            this.allowedPackages = Preconditions.checkNotNull(allowedPackages, "allowedPackages must not be null");
            return this;
        }

        /**
         * Builds and returns a new JavaArchitectureTestCase instance with the configured properties.
         *
         * @since 2.0.0
         * @author Sarp Sahinalp
         * @return A new JavaArchitectureTestCase instance
         * @throws SecurityException if required parameters are missing
         */
        @Nonnull
        public JavaWalaTestCase build() {
            return new JavaWalaTestCase(
                    Preconditions.checkNotNull(javaArchitectureTestCaseSupported, "javaArchitecturalTestCaseSupported must not be null"),
                    Preconditions.checkNotNull(allowedPackages, "allowedPackages must not be null"),
                    Preconditions.checkNotNull(javaClasses, "javaClasses must not be null"),
                    Preconditions.checkNotNull(callGraph, "callGraph must not be null")
            );
        }
    }
    //</editor-fold>
}
