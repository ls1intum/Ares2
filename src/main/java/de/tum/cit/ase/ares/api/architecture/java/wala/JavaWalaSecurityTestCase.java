package de.tum.cit.ase.ares.api.architecture.java.wala;

import com.ibm.wala.ipa.callgraph.CallGraph;
import com.tngtech.archunit.core.domain.JavaClasses;
import de.tum.cit.ase.ares.api.architecture.java.JavaArchitecturalTestCaseSupported;
import de.tum.cit.ase.ares.api.policy.SecurityPolicy;

import java.util.Set;
import java.util.stream.Collectors;
import static de.tum.cit.ase.ares.api.architecture.java.JavaArchitectureTestCase.parseErrorMessage;
import static de.tum.cit.ase.ares.api.localization.Messages.localized;

/**
 * Security test case for the Java programming language using WALA.
 */
public class JavaWalaSecurityTestCase {

    /**
     * Selects the supported architecture test case in the Java programming language.
     */
    private final JavaArchitecturalTestCaseSupported javaArchitectureTestCaseSupported;

    /**
     * List of allowed packages to be imported.
     */
    private final Set<String> allowedPackages;

    public JavaWalaSecurityTestCase(Builder builder) {
        this.javaArchitectureTestCaseSupported = builder.javaArchitectureTestCaseSupported;
        this.allowedPackages = builder.allowedPackages;
    }

    @SuppressWarnings("unused")
    public String writeArchitectureTestCase() {
        // TODO: For further releases
        return "";
    }

    /**
     * Execute the architecture test case.
     * @param callGraph The call graph to check
     * @param javaClasses The Java classes to check
     */
    public void executeArchitectureTestCase(CallGraph callGraph, JavaClasses javaClasses) {
        try {
            switch (this.javaArchitectureTestCaseSupported) {
                case REFLECTION -> JavaWalaSecurityTestCaseCollection.noReflection(callGraph);
                case FILESYSTEM_INTERACTION -> JavaWalaSecurityTestCaseCollection.noFileSystemAccess(callGraph);
                case TERMINATE_JVM -> JavaWalaSecurityTestCaseCollection.noJVMTermination(callGraph);
                case NETWORK_CONNECTION -> JavaWalaSecurityTestCaseCollection.noNetworkAccess(callGraph);
                case COMMAND_EXECUTION -> JavaWalaSecurityTestCaseCollection.noCommandExecution(callGraph);
                case PACKAGE_IMPORT -> JavaWalaSecurityTestCaseCollection.restrictPackageImport(javaClasses, allowedPackages);
                case THREAD_CREATION -> JavaWalaSecurityTestCaseCollection.noThreadManipulation(callGraph);
                case SERIALIZATION -> JavaWalaSecurityTestCaseCollection.noSerialization(callGraph);
                case CLASS_LOADING -> JavaWalaSecurityTestCaseCollection.noClassLoading(callGraph);
                default -> throw new SecurityException(localized("security.common.unsupported.operation", this.javaArchitectureTestCaseSupported));
            }
        } catch (AssertionError e) {
            // check if long error message is enabled
            parseErrorMessage(e);
        }
    }

    // Static method to start the builder
    public static Builder builder() {
        return new Builder();
    }


    // Static Builder class
    public static class Builder {
        private JavaArchitecturalTestCaseSupported javaArchitectureTestCaseSupported;
        private Set<String> allowedPackages;

        public Builder javaArchitecturalTestCaseSupported(JavaArchitecturalTestCaseSupported javaArchitectureTestCaseSupported) {
            this.javaArchitectureTestCaseSupported = javaArchitectureTestCaseSupported;
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
