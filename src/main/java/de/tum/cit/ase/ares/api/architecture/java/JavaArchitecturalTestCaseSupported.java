package de.tum.cit.ase.ares.api.architecture.java;

/**
 * Supported architecture test cases in Java programming language.
 *
 * @author Markus Paulsen
 * @version 2.0.0
 * @since 2.0.0
 */
public enum JavaArchitecturalTestCaseSupported {
    /**
     * Architecture test case for the file system interaction.
     */
    FILESYSTEM_INTERACTION,
    /**
     * Architecture test case for the network connection.
     */
    NETWORK_CONNECTION,
    /**
     * Architecture test case for the command execution.
     */
    COMMAND_EXECUTION,
    /**
     * Architecture test case for the thread creation.
     */
    THREAD_CREATION,
    /**
     * Architecture test case for the package import.
     */
    PACKAGE_IMPORT,
    /**
     * Architecture test case for the premature jvm termination.
     */
    TERMINATE_JVM,
    /**
     * Architecture test case for the reflection.
     */
    REFLECTION,
    /**
     * Architecture test case for the serialization.
     */
    SERIALIZATION,
    /**
     * Architecture test case for the class loading.
     */
    CLASS_LOADING,

}
