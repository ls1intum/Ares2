package de.tum.cit.ase.ares.api.architecture.java.wala;

//<editor-fold desc="Imports">

import com.ibm.wala.ipa.callgraph.CallGraph;
import com.tngtech.archunit.core.domain.JavaClasses;
import de.tum.cit.ase.ares.api.architecture.java.JavaArchitectureTestCaseSupported;
import de.tum.cit.ase.ares.api.policy.SecurityPolicy;
import de.tum.cit.ase.ares.api.policy.policySubComponents.PackagePermission;

import javax.annotation.Nonnull;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import static de.tum.cit.ase.ares.api.architecture.java.JavaArchitectureTestCase.parseErrorMessage;
import static de.tum.cit.ase.ares.api.localization.Messages.localized;
//</editor-fold>

/**
 * Security test case for the Java programming language using WALA.
 *
 * @author Sarp Sahinalp
 * @version 2.0.0
 * @see <a href="https://refactoring.guru/design-patterns/abstract-factory">Abstract Factory Design Pattern</a>
 * @since 2.0.0
 */
public class JavaWalaSecurityTestCase {

    //<editor-fold desc="Attributes">
    /**
     * Selects the supported architecture test case in the Java programming language.
     */
    @Nonnull
    private final JavaArchitectureTestCaseSupported javaArchitectureTestCaseSupported;

    /**
     * List of allowed packages to be imported.
     */
    @Nonnull
    private final Set<String> allowedPackages;
    //</editor-fold>

    //<editor-fold desc="Constructors">

    /**
     * Constructor for JavaWalaSecurityTestCase.
     *
     * @param builder Selects the supported architecture test case in the Java programming language
     */
    public JavaWalaSecurityTestCase(@Nonnull Builder builder) {
        this.javaArchitectureTestCaseSupported = builder.javaArchitectureTestCaseSupported;
        this.allowedPackages = builder.allowedPackages;
    }
    //</editor-fold>

    //<editor-fold desc="Tool methods">
    /**
     * Returns the content of the architecture test case file in the Java programming language.
     */
    @SuppressWarnings("unused")
    @Nonnull
    public String writeArchitectureTestCase() {
        // TODO: For further releases
        return "";
    }
    //</editor-fold>

    //<editor-fold desc="Execute security test case methods">

    /**
     * Execute the architecture test case.
     * @param callGraph The call graph to check
     * @param javaClasses The Java classes to check
     */
    public void executeArchitectureTestCase(CallGraph callGraph, JavaClasses javaClasses) {
        try {
            switch (this.javaArchitectureTestCaseSupported) {
                case FILESYSTEM_INTERACTION ->
                        JavaWalaSecurityTestCaseCollection
                            .noFileSystemAccess(callGraph);
                case NETWORK_CONNECTION ->
                        JavaWalaSecurityTestCaseCollection
                            .noNetworkAccess(callGraph);
                case THREAD_CREATION ->
                        JavaWalaSecurityTestCaseCollection
                            .noThreadManipulation(callGraph);
                case COMMAND_EXECUTION ->
                        JavaWalaSecurityTestCaseCollection
                            .noCommandExecution(callGraph);
                case PACKAGE_IMPORT ->
                        JavaWalaSecurityTestCaseCollection
                            .restrictPackageImport(javaClasses, allowedPackages);
                case REFLECTION ->
                        JavaWalaSecurityTestCaseCollection
                            .noReflection(callGraph);
                case TERMINATE_JVM ->
                        JavaWalaSecurityTestCaseCollection
                            .noJVMTermination(callGraph);
                case SERIALIZATION ->
                        JavaWalaSecurityTestCaseCollection
                            .noSerialization(callGraph);
                case CLASS_LOADING ->
                        JavaWalaSecurityTestCaseCollection
                            .noClassLoading(callGraph);
                default -> throw new SecurityException(localized("security.common.unsupported.operation", this.javaArchitectureTestCaseSupported));
            }
        } catch (AssertionError e) {
            parseErrorMessage(e);
        }
    }
    //</editor-fold>

    //<editor-fold desc="Builder">
    // Static method to start the builder
    public static Builder builder() {
        return new Builder();
    }


    // Static Builder class
    public static class Builder {
        private JavaArchitectureTestCaseSupported javaArchitectureTestCaseSupported;
        private Set<String> allowedPackages = new HashSet<>();

        public Builder javaArchitecturalTestCaseSupported(JavaArchitectureTestCaseSupported javaArchitectureTestCaseSupported) {
            this.javaArchitectureTestCaseSupported = javaArchitectureTestCaseSupported;
            return this;
        }

        public Builder allowedPackages(Set<PackagePermission> packages) {
            if (packages != null) {
                this.allowedPackages = packages.stream()
                        .map(PackagePermission::importTheFollowingPackage)
                        .collect(Collectors.toSet());
            }
            return this;
        }

        public JavaWalaSecurityTestCase build() {
            return new JavaWalaSecurityTestCase(this);
        }
    }
    //</editor-fold>
}
