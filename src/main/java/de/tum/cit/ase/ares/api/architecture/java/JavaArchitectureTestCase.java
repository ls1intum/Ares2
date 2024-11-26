package de.tum.cit.ase.ares.api.architecture.java;

import com.ibm.wala.ipa.callgraph.CallGraph;
import com.tngtech.archunit.core.domain.JavaClasses;
import de.tum.cit.ase.ares.api.architecture.ArchitectureSecurityTestCase;
import de.tum.cit.ase.ares.api.architecture.java.archunit.JavaArchUnitSecurityTestCase;
import de.tum.cit.ase.ares.api.architecture.java.wala.JavaWalaSecurityTestCase;
import de.tum.cit.ase.ares.api.policy.SecurityPolicy;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Set;

import static de.tum.cit.ase.ares.api.localization.Messages.localized;

public class JavaArchitectureTestCase implements ArchitectureSecurityTestCase {

    //<editor-fold desc="Attributes">
    @Nonnull
    private final JavaArchitecturalTestCaseSupported javaArchitectureTestCaseSupported;

    @Nonnull
    private final JavaClasses javaClasses;
    @Nullable
    private final CallGraph callGraph;
    @Nullable
    private final Set<SecurityPolicy.PackagePermission> allowedPackages;
    //</editor-fold>

    //<editor-fold desc="Constructor">
    public JavaArchitectureTestCase(@Nonnull JavaArchitecturalTestCaseSupported javaArchitectureTestCaseSupported, @Nonnull JavaClasses javaClasses, @Nullable CallGraph callGraph, @Nullable Set<SecurityPolicy.PackagePermission> allowedPackages) {
        this.javaArchitectureTestCaseSupported = javaArchitectureTestCaseSupported;
        this.javaClasses = javaClasses;
        this.callGraph = callGraph;
        this.allowedPackages = allowedPackages;
    }
    //</editor-fold>

    //<editor-fold desc="Write architecture test case methods">
    @Nonnull
    @Override
    public String writeArchitectureTestCase(@Nonnull String architectureMode) {
        return "";
    }
    //</editor-fold>

    //<editor-fold desc="Execute architecture test case methods">
    @Override
    public void executeArchitectureTestCase(@Nonnull JavaArchitectureMode architectureMode) {
        // TODO: Add some checks for the modes and the needs e.g. (CallGraph not null for WALA)
        switch (architectureMode) {
            case WALA -> JavaWalaSecurityTestCase.builder()
                    .javaArchitecturalTestCaseSupported(javaArchitectureTestCaseSupported)
                    .allowedPackages(allowedPackages)
                    .build()
                    .executeArchitectureTestCase(callGraph, javaClasses);
            case ARCHUNIT -> JavaArchUnitSecurityTestCase.builder()
                    .javaArchitecturalTestCaseSupported(javaArchitectureTestCaseSupported)
                    .allowedPackages(allowedPackages)
                    .build()
                    .executeArchitectureTestCase(javaClasses);
        }
    }
    //</editor-fold>

    /**
     * Parses the error message of an assertion error.
     */
    public static void parseErrorMessage(AssertionError e) {
        String[] split;
        if (e.getMessage() == null || e.getMessage().split("\n").length < 2) {
            throw new SecurityException(localized("security.archunit.illegal.execution", e.getMessage()));
        }
        split = e.getMessage().split("\n");
        throw new SecurityException(localized("security.archunit.violation.error", split[0].replaceAll(".*?'(.*?)'.*\r*", "$1"), split[1]));
    }

    // TODO: Outsource this to a new class
    /**
     * Starts the builder for the Java architecture test case.
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Builder for the Java architecture test case.
     */
    //<editor-fold desc="Builder">
    public static class Builder {
        private JavaArchitecturalTestCaseSupported javaArchitectureTestCaseSupported;
        private JavaClasses javaClasses;
        private CallGraph callGraph;
        private Set<SecurityPolicy.PackagePermission> allowedPackages;

        public Builder javaArchitecturalTestCaseSupported(JavaArchitecturalTestCaseSupported javaArchitectureTestCaseSupported) {
            if (javaArchitectureTestCaseSupported == null) {
                throw new SecurityException(localized("security.common.not.null", "javaArchitectureTestCaseSupported"));
            }
            this.javaArchitectureTestCaseSupported = javaArchitectureTestCaseSupported;
            return this;
        }

        public Builder javaClasses(JavaClasses javaClasses) {
            this.javaClasses = javaClasses;
            return this;
        }

        public Builder callGraph(CallGraph callGraph) {
            this.callGraph = callGraph;
            return this;
        }

        public Builder allowedPackages(Set<SecurityPolicy.PackagePermission> allowedPackages) {
            this.allowedPackages = allowedPackages;
            return this;
        }

        public JavaArchitectureTestCase build() {
            if (javaArchitectureTestCaseSupported == null) {
                throw new SecurityException(localized("security.common.not.null", "javaArchitectureTestCaseSupported"));
            }
            return new JavaArchitectureTestCase(javaArchitectureTestCaseSupported, javaClasses, callGraph, allowedPackages);
        }
    }
    //</editor-fold>

}
