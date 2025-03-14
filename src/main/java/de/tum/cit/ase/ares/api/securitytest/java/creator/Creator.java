package de.tum.cit.ase.ares.api.securitytest.java.creator;

import de.tum.cit.ase.ares.api.aop.java.JavaAOPMode;
import de.tum.cit.ase.ares.api.aop.java.JavaAOPTestCase;
import de.tum.cit.ase.ares.api.architecture.java.JavaArchitectureMode;
import de.tum.cit.ase.ares.api.architecture.java.JavaArchitectureTestCase;
import de.tum.cit.ase.ares.api.buildtoolconfiguration.java.JavaBuildMode;
import de.tum.cit.ase.ares.api.policy.policySubComponents.ResourceAccesses;

import javax.annotation.Nonnull;
import java.nio.file.Path;
import java.util.List;

/**
 * Interface for creating security test cases.
 */
public interface Creator {

    /**
     * Creates security test cases.
     */
    void createSecurityTestCases(
            JavaBuildMode javaBuildMode,
            JavaArchitectureMode javaArchitectureMode,
            JavaAOPMode javaAOPMode,
            List<String> essentialPackages,
            List<String> essentialClasses,
            String[] testClasses,
            String packageName,
            String mainClassInPackageName,
            ResourceAccesses resourceAccesses,
            Path projectPath,
            @Nonnull List<JavaArchitectureTestCase> javaArchitectureTestCases,
            @Nonnull List<JavaAOPTestCase> javaAOPTestCases
    );
}
