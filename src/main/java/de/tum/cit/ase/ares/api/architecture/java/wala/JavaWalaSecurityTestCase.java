package de.tum.cit.ase.ares.api.architecture.java.wala;

import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ipa.callgraph.CallGraph;
import com.tngtech.archunit.core.domain.JavaClasses;
import de.tum.cit.ase.ares.api.architecture.java.JavaArchitecturalTestCaseSupported;
import de.tum.cit.ase.ares.api.architecture.java.archunit.JavaArchitectureTestCaseCollection;
import de.tum.cit.ase.ares.api.policy.SecurityPolicy;

import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static de.tum.cit.ase.ares.api.architecture.java.JavaArchitectureTestCase.parseErrorMessage;

/**
 * A utility class to check security rules in a call graph.
 */
public class JavaWalaSecurityTestCase {

    /**
     * Selects the supported architecture test case in the Java programming language.
     */
    private final JavaArchitecturalTestCaseSupported javaArchitectureTestCaseSupported;

    /**
     * Flag to determine if the error message should be long or parsed for less details.
     */
    private final boolean longError;

    /**
     * List of allowed packages to be imported.
     */
    private Set<String> allowedPackages;

    public JavaWalaSecurityTestCase(Builder builder) {
        this.javaArchitectureTestCaseSupported = builder.javaArchitectureTestCaseSupported;
        this.longError = builder.longError;
        this.allowedPackages = builder.allowedPackages;
    }

    @SuppressWarnings("unused")
    public String writeArchitectureTestCase() {
        // TODO: For further releases
        return "";
    }

    public void executeArchitectureTestCase(CallGraph callGraph, JavaClasses javaClasses) {
        try {
            switch (this.javaArchitectureTestCaseSupported) {
                case REFLECTION -> JavaWalaSecurityTestCaseCollection.noReflection(callGraph);
                case FILESYSTEM_INTERACTION -> JavaWalaSecurityTestCaseCollection.noFileSystemAccess(callGraph);
                case TERMINATE_JVM -> JavaWalaSecurityTestCaseCollection.noJVMTermination(callGraph);
                case NETWORK_CONNECTION -> JavaWalaSecurityTestCaseCollection.noNetworkAccess(callGraph);
                case COMMAND_EXECUTION -> JavaWalaSecurityTestCaseCollection.noCommandExecution(callGraph);
                case PACKAGE_IMPORT -> JavaArchitectureTestCaseCollection
                        .noClassesShouldImportForbiddenPackages(allowedPackages)
                        .check(javaClasses);
                case THREAD_CREATION -> JavaWalaSecurityTestCaseCollection.noThreadCreation(callGraph);
                default -> throw new UnsupportedOperationException("Not implemented yet %s".formatted(this.javaArchitectureTestCaseSupported));
            }
        } catch (AssertionError e) {
            // check if long error message is enabled
            parseErrorMessage(e, longError);
        }
    }

    // Static method to start the builder
    public static Builder builder() {
        return new Builder();
    }


    // Static Builder class
    public static class Builder {
        private JavaArchitecturalTestCaseSupported javaArchitectureTestCaseSupported;
        private Predicate<CGNode> targetNodeFilter;
        private boolean longError = false;
        private Set<String> allowedPackages;

        public Builder javaArchitecturalTestCaseSupported(JavaArchitecturalTestCaseSupported javaArchitectureTestCaseSupported) {
            if (javaArchitectureTestCaseSupported == null) {
                throw new IllegalArgumentException("javaArchitectureTestCaseSupported must not be null");
            }
            this.javaArchitectureTestCaseSupported = javaArchitectureTestCaseSupported;
            return this;
        }

        public Builder longError(boolean longError) {
            this.longError = longError;
            return this;
        }

        public Builder allowedPackages(Set<SecurityPolicy.PackagePermission> packages) {
            if (packages != null) {
                this.allowedPackages = packages.stream()
                        .map(SecurityPolicy.PackagePermission::importTheFollowingPackage)
                        .collect(Collectors.toSet());
            }
            return this;
        }

        public JavaWalaSecurityTestCase build() {
            return new JavaWalaSecurityTestCase(this);
        }
    }
}
