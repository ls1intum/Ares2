package de.tum.cit.ase.ares.api.aop.java;

import de.tum.cit.ase.ares.api.aop.AOPTestCaseSupported;
import java.util.List;

/**
 * Enum of supported AOP test cases for Java.
 *
 * <p>Description: This enum defines the different types of aspect configurations supported for dynamic code analysis in Java applications.
 *
 * <p>Design Rationale: Organising AOP test cases as an enum centralises aspect configuration management and supports consistency in dynamic analysis.
 *
 * @since 2.0.0
 * @author Markus Paulsen
 * @version 2.0.0
 */
public enum JavaAOPTestCaseSupported implements AOPTestCaseSupported {

    /**
     * Aspect configuration for managing file system interactions.
     */
    FILESYSTEM_INTERACTION,

    /**
     * Aspect configuration for managing network connections.
     */
    NETWORK_CONNECTION,

    /**
     * Aspect configuration for managing command executions.
     */
    COMMAND_EXECUTION,

    /**
     * Aspect configuration for managing thread creation.
     */
    THREAD_CREATION;

    /**
     * Retrieves the dynamic AOP test cases.
     *
     * @since 2.0.0
     * @author Markus Paulsen
     * @return a list of dynamic AOP test cases.
     */
    public List<AOPTestCaseSupported> getDynamic() {
        return List.of(
                JavaAOPTestCaseSupported.FILESYSTEM_INTERACTION,
                JavaAOPTestCaseSupported.NETWORK_CONNECTION,
                JavaAOPTestCaseSupported.COMMAND_EXECUTION,
                JavaAOPTestCaseSupported.THREAD_CREATION
        );
    }
}