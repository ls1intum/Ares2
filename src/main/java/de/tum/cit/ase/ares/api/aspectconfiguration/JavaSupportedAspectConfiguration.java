package de.tum.cit.ase.ares.api.aspectconfiguration;

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
    COMMANDEXECUTION,
    /**
     * Aspect configuration for the thread creation.
     */
    THREADCREATION,
    /**
     * Aspect configuration for the package import.
     */
    PACKAGEIMPORT
}