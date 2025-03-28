package de.tum.cit.ase.ares.api.securitytest.java.executer;

import de.tum.cit.ase.ares.api.aop.java.JavaAOPMode;
import de.tum.cit.ase.ares.api.aop.java.JavaAOPTestCase;
import de.tum.cit.ase.ares.api.architecture.java.JavaArchitectureMode;
import de.tum.cit.ase.ares.api.architecture.java.JavaArchitectureTestCase;
import de.tum.cit.ase.ares.api.buildtoolconfiguration.java.JavaBuildMode;

import javax.annotation.Nonnull;
import java.util.List;

/**
 * Interface for executing security test cases across different programming languages and frameworks.
 *
 * <p>Description: This interface defines a contract for executing security test cases for
 * different programming languages and frameworks, orchestrating the test execution process.
 *
 * <p>Design Rationale: Implements the Strategy design pattern to allow for different
 * implementation strategies for executing security test cases based on target language or framework.
 *
 * @since 2.0.0
 * @author Markus Paulsen
 * @version 2.0.0
 */
public interface Executer {

    /**
     * Executes security test cases.
     *
     * @since 2.0.0
     * @author Markus Paulsen
     * @param javaBuildMode the Java build mode to use; must not be null
     * @param javaArchitectureMode the Java architecture mode to use; must not be null
     * @param javaAOPMode the Java AOP mode to use; must not be null
     * @param essentialPackages the list of essential packages; must not be null
     * @param essentialClasses the list of essential classes; must not be null
     * @param testClasses the list of test classes; must not be null
     * @param packageName the name of the package containing the main class; must not be null
     * @param mainClassInPackageName the name of the main class; must not be null
     * @param javaArchitectureTestCases the list of architecture test cases; must not be null
     * @param javaAOPTestCases the list of AOP test cases; must not be null
     */
    void executeSecurityTestCases(
            @Nonnull JavaBuildMode javaBuildMode,
            @Nonnull JavaArchitectureMode javaArchitectureMode,
            @Nonnull JavaAOPMode javaAOPMode,
            @Nonnull List<String> essentialPackages,
            @Nonnull List<String> essentialClasses,
            @Nonnull List<String> testClasses,
            @Nonnull String packageName,
            @Nonnull String mainClassInPackageName,
            @Nonnull List<JavaArchitectureTestCase> javaArchitectureTestCases,
            @Nonnull List<JavaAOPTestCase> javaAOPTestCases
    );

}
