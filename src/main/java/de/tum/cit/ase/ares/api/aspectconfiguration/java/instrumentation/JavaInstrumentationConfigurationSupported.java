package de.tum.cit.ase.ares.api.aspectconfiguration.java.instrumentation;

/**
 * Enum representing the supported aspect configurations in the Java programming language.
 * <p>
 * This enum defines the different types of aspect configurations that are supported for
 * instrumentation in Java. These configurations are used to control and manage different
 * types of interactions and behaviors within a Java application, such as file system interactions,
 * network connections, command executions, and thread creations.
 * </p>
 *
 * @version 2.0.0
 * @since 2.0.0
 */
public enum JavaInstrumentationConfigurationSupported {

    /**
     * Aspect configuration for managing file system interactions.
     * <p>
     * This configuration is used to control permissions and behaviors related to reading, writing,
     * and executing files within the file system.
     * </p>
     */
    FILESYSTEM_INTERACTION,

    /**
     * Aspect configuration for managing network connections.
     * <p>
     * This configuration is used to control permissions and behaviors related to opening connections,
     * sending data, and receiving data over the network.
     * </p>
     */
    NETWORK_CONNECTION,

    /**
     * Aspect configuration for managing command executions.
     * <p>
     * This configuration is used to control permissions and behaviors related to executing system commands
     * and managing the arguments passed to those commands.
     * </p>
     */
    COMMAND_EXECUTION,

    /**
     * Aspect configuration for managing thread creation.
     * <p>
     * This configuration is used to control permissions and behaviors related to creating and managing
     * threads within the application.
     * </p>
     */
    THREAD_CREATION
}