package de.tum.cit.ase.ares.api.securitytest.java.creator;

import de.tum.cit.ase.ares.api.aop.AOPTestCase;
import de.tum.cit.ase.ares.api.aop.AOPMode;
import de.tum.cit.ase.ares.api.architecture.ArchitectureTestCase;
import de.tum.cit.ase.ares.api.architecture.ArchitectureMode;
import de.tum.cit.ase.ares.api.buildtoolconfiguration.BuildMode;
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
     * @param architectureTestCases the list to populate with architecture test cases; must not be null
     * @param aopTestCases the list to populate with AOP test cases; must not be null
     * @param resourceAccesses the resource accesses permitted by the security policy; must not be null
     * @param projectPath the path to the project; must not be null
     */
    void createTestCases(
            // TODO Markus: Remove Java from Abstract Class
            @Nonnull BuildMode buildMode,
            @Nonnull ArchitectureMode architectureMode,
            @Nonnull AOPMode aopMode,
            @Nonnull List<String> essentialPackages,
            @Nonnull List<String> essentialClasses,
            @Nonnull List<String> testClasses,
            @Nonnull String packageName,
            @Nonnull String mainClassInPackageName,
            @Nonnull List<ArchitectureTestCase> architectureTestCases,
            @Nonnull List<AOPTestCase> aopTestCases,
            @Nonnull ResourceAccesses resourceAccesses,
            @Nonnull Path projectPath
    );
}
