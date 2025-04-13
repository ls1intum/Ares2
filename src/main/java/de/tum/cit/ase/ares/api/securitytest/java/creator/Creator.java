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
 * Creates security test cases based on security policies.
 *
 * <p>Description: This interface defines the contract for creating security test cases
 * for different programming languages and frameworks.
 *
 * <p>Design Rationale: The Creator interface follows the Strategy design pattern to allow
 * for different implementation strategies for creating security test cases for different
 * programming languages and frameworks.
 *
 * @since 2.0.0
 * @author Markus Paulsen
 * @version 2.0.0
 */
public interface Creator {

    /**
     * Creates the security test cases based on the security policy.
     *
     * @since 2.0.0
     * @author Markus Paulsen
     * @param essentialClasses the list of essential classes; must not be null
     * @param testClasses the list of test classes; must not be null
     * @param javaArchitectureTestCases the list to populate with architecture test cases; must not be null
     * @param javaAOPTestCases the list to populate with AOP test cases; must not be null
     * @param resourceAccesses the resource accesses permitted by the security policy; must not be null
     * @param projectPath the path to the project; must not be null
     */
    void createSecurityTestCases(
            @Nonnull JavaBuildMode javaBuildMode,
            @Nonnull JavaArchitectureMode javaArchitectureMode,
            @Nonnull JavaAOPMode javaAOPMode,
            @Nonnull List<String> essentialPackages,
            @Nonnull List<String> essentialClasses,
            @Nonnull List<String> testClasses,
            @Nonnull String packageName,
            @Nonnull String mainClassInPackageName,
            @Nonnull List<JavaArchitectureTestCase> javaArchitectureTestCases,
            @Nonnull List<JavaAOPTestCase> javaAOPTestCases,
            @Nonnull ResourceAccesses resourceAccesses,
            @Nonnull Path projectPath
    );
}
