package de.tum.cit.ase.ares.api.aspectconfiguration.java;

/**
 * Supported aspect configurations in Java programming language.
 *
 * @author Markus Paulsen
 * @version 2.0.0
 * @since 2.0.0
 */
public enum JavaSupportedAspectConfiguration {
    /**
     * Aspect configuration for the file system interaction.
     */
    FILESYSTEMINTERACTION,
    /**
     * Aspect configuration for the network connection.
     */
    NETWORKCONNECTION,
    /**
     * Aspect configuration for the command execution.
     */
    COMMAND_EXECUTION,
    /**
     * Aspect configuration for the thread creation.
     */
    THREAD_CREATION,
    /**
     * Aspect configuration for the package import.
     */
    PACKAGE_IMPORT
}