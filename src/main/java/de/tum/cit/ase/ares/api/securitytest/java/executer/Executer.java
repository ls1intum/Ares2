package de.tum.cit.ase.ares.api.securitytest.java.executer;

import de.tum.cit.ase.ares.api.aop.java.JavaAOPMode;
import de.tum.cit.ase.ares.api.aop.java.JavaAOPTestCase;
import de.tum.cit.ase.ares.api.architecture.java.JavaArchitectureMode;
import de.tum.cit.ase.ares.api.architecture.java.JavaArchitectureTestCase;

import javax.annotation.Nonnull;
import java.util.List;

/**
 * Interface for executing security test cases.
 */
public interface Executer {

    /**
     * Executes security test cases.
     */
    void executeSecurityTestCases(
            @Nonnull JavaArchitectureMode javaArchitectureMode,
            @Nonnull JavaAOPMode javaAOPMode,
            @Nonnull String packageName,
            @Nonnull String mainClassInPackageName,
            @Nonnull String[] testClasses,
            @Nonnull List<String> essentialClasses,
            @Nonnull List<JavaArchitectureTestCase> javaArchitectureTestCases,
            @Nonnull List<JavaAOPTestCase> javaAOPTestCases
    );

}
