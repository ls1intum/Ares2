package de.tum.cit.ase.ares.api.securitytest.java.writer;

import de.tum.cit.ase.ares.api.aop.java.JavaAOPMode;
import de.tum.cit.ase.ares.api.aop.java.JavaAOPTestCase;
import de.tum.cit.ase.ares.api.architecture.java.JavaArchitectureMode;
import de.tum.cit.ase.ares.api.architecture.java.JavaArchitectureTestCase;
import de.tum.cit.ase.ares.api.buildtoolconfiguration.java.JavaBuildMode;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.nio.file.Path;
import java.util.List;

/**
 * Interface for writing security test cases across different programming languages and frameworks.
 *
 * <p>Description: This interface defines a contract for creating and writing security test files
 * for different programming languages and frameworks, encapsulating the test generation process.
 *
 * <p>Design Rationale: Implements the Strategy design pattern to allow for different
 * implementation strategies for writing security test cases based on target language or framework.
 *
 * @since 2.0.0
 * @author Markus Paulsen
 * @version 2.0.0
 */
public interface Writer {

    /**
     * Writes security test cases to files.
     *
     * @param essentialClasses the list of essential classes; must not be null
     * @param testClasses the list of test classes; must not be null
     * @param javaArchitectureTestCases the list of architecture test cases; must not be null
     * @param javaAOPTestCases the list of AOP test cases; must not be null
     * @param projectDirectory the directory of the project; may be null
     * @return a list of paths to the created files
     */
    @Nonnull
    List<Path> writeSecurityTestCases(
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
            @Nullable Path projectDirectory
    );

}
