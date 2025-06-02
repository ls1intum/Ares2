package de.tum.cit.ase.ares.api.architecture.java;

import de.tum.cit.ase.ares.api.architecture.ArchitectureTestCaseSupported;

import javax.annotation.Nonnull;
import java.util.List;

/**
 * Enum of supported architecture test cases for Java.
 *
 * <p>Description: This enum defines the different types of architecture test cases available for static and dynamic code analysis in Java applications.
 *
 * <p>Design Rationale: Using an enum simplifies management of test cases and facilitates clear separation between static and dynamic analysis categories.
 *
 * @since 2.0.0
 * @author Markus Paulsen
 * @version 2.0.0
 */
public enum JavaArchitectureTestCaseSupported implements ArchitectureTestCaseSupported {

    /**
     * Test case for file system interaction.
     */
    FILESYSTEM_INTERACTION,

    /**
     * Test case for network connection.
     */
    NETWORK_CONNECTION,

    /**
     * Test case for command execution.
     */
    COMMAND_EXECUTION,

    /**
     * Test case for thread creation.
     */
    THREAD_CREATION,

    /**
     * Test case for package import.
     */
    PACKAGE_IMPORT,

    /**
     * Test case for premature JVM termination.
     */
    TERMINATE_JVM,

    /**
     * Test case for reflection usage.
     */
    REFLECTION,

    /**
     * Test case for serialization.
     */
    SERIALIZATION,

    /**
     * Test case for class loading.
     */
    CLASS_LOADING;

    /**
     * Retrieves the static architecture test cases.
     *
     * @since 2.0.0
     * @author Markus Paulsen
     * @return a list of static architecture test cases.
     */
    @Nonnull
    public List<ArchitectureTestCaseSupported> getStatic() {
        return List.of(
                JavaArchitectureTestCaseSupported.PACKAGE_IMPORT,
                JavaArchitectureTestCaseSupported.TERMINATE_JVM,
                JavaArchitectureTestCaseSupported.REFLECTION,
                JavaArchitectureTestCaseSupported.SERIALIZATION,
                JavaArchitectureTestCaseSupported.CLASS_LOADING
        );
    }

    /**
     * Retrieves the dynamic architecture test cases.
     *
     * @since 2.0.0
     * @author Markus Paulsen
     * @return a list of dynamic architecture test cases.
     */
    @Nonnull
    public List<ArchitectureTestCaseSupported> getDynamic() {
        return List.of(
                JavaArchitectureTestCaseSupported.FILESYSTEM_INTERACTION,
                JavaArchitectureTestCaseSupported.NETWORK_CONNECTION,
                JavaArchitectureTestCaseSupported.COMMAND_EXECUTION,
                JavaArchitectureTestCaseSupported.THREAD_CREATION
        );
    }
}