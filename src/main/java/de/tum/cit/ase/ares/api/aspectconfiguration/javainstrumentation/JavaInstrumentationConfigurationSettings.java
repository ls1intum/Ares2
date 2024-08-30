package de.tum.cit.ase.ares.api.aspectconfiguration.javainstrumentation;

/**
 * Configuration settings for Java instrumentation aspect configurations.
 * <p>
 * This class holds the static configuration settings used by Java instrumentation aspects,
 * such as allowed file paths, network hosts and ports, command executions, and thread creations.
 * These settings are used to control and manage various security-related behaviors in the application.
 * </p>
 * <p>
 * As a utility class, it is not intended to be instantiated.
 * </p>
 *
 * @version 2.0.0
 * @since 2.0.0
 */
public class JavaInstrumentationConfigurationSettings {

    /**
     * Private constructor to prevent instantiation of this utility class.
     * <p>
     * The constructor throws an {@link IllegalStateException} to enforce the utility class pattern.
     * </p>
     */
    private JavaInstrumentationConfigurationSettings() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * The package that is restricted from certain operations.
     */
    private static String restrictedPackage = "";

    /**
     * List of classes that are allowed to be instrumented.
     */
    private static String[] allowedListedClasses = {
            "de.tum.cit.ase.ares.api.aspectconfiguration.javainstrumentation.JavaInstrumentationConfigurationSettings",
            "de.tum.cit.ase.ares.api.aspectconfiguration.javainstrumentation.adviceAndPointcut.JavaInstrumentationAdviceToolbox",
            "de.tum.cit.ase.ares.api.aspectconfiguration.javainstrumentation.adviceAndPointcut.JavaInstrumentationReadPathAdvice",
            "de.tum.cit.ase.ares.api.aspectconfiguration.javainstrumentation.adviceAndPointcut.JavaWritePathAdvice",
            "de.tum.cit.ase.ares.api.aspectconfiguration.javainstrumentation.adviceAndPointcut.JavaInstrumentationExecutePathAdvice",
            "de.tum.cit.ase.ares.api.aspectconfiguration.javainstrumentation.pointcut.JavaInstrumentationPointcutDefinitions",
            "de.tum.cit.ase.ares.api.aspectconfiguration.javainstrumentation.pointcut.JavaInstrumentationBindingDefinitions",
            "de.tum.cit.ase.ares.api.aspectconfiguration.javainstrumentation.JavaInstrumentationAgent"
    };

    /**
     * Paths that are allowed to be read.
     */
    private static String[] pathsAllowedToBeRead = {};

    /**
     * Paths that are allowed to be overwritten.
     */
    private static String[] pathsAllowedToBeOverwritten = {};

    /**
     * Paths that are allowed to be executed.
     */
    private static String[] pathsAllowedToBeExecuted = {};

    /**
     * Hosts that are allowed to be connected to.
     */
    private static String[] hostsAllowedToBeConnectedTo = {};

    /**
     * Ports that are allowed to be connected to.
     */
    private static int[] portsAllowedToBeConnecedTo = {};

    /**
     * Hosts that are allowed to send data to.
     */
    private static String[] hostsAllowedToBeSentTo = {};

    /**
     * Ports that are allowed to send data to.
     */
    private static int[] portsAllowedToBeSentTo = {};

    /**
     * Hosts that are allowed to receive data from.
     */
    private static String[] hostsAllowedToBeReceivedFrom = {};

    /**
     * Ports that are allowed to receive data from.
     */
    private static int[] portsAllowedToBeReceivedFrom = {};

    /**
     * Commands that are allowed to be executed.
     */
    private static String[] commandsAllowedToBeExecuted = {};

    /**
     * Arguments that are allowed to be passed to the commands.
     */
    private static String[][] argumentsAllowedToBePassed = {};

    /**
     * Number of threads that are allowed to be created.
     */
    private static int[] threadNumberAllowedToBeCreated = {};

    /**
     * Classes of threads that are allowed to be created.
     */
    private static String[] threadClassAllowedToBeCreated = {};
}