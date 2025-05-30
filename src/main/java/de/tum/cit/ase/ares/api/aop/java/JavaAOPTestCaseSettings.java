package de.tum.cit.ase.ares.api.aop.java;

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
public class JavaAOPTestCaseSettings {

    /**
     * Private constructor to prevent instantiation of this utility class.
     * <p>
     * The constructor throws an {@link SecurityException} to enforce the utility class pattern.
     * </p>
     */
    private JavaAOPTestCaseSettings() {
        throw new SecurityException("Ares Security Error (Reason: Ares-Code; Stage: Creation): JavaAOPTestCaseSettings is a utility class and should not be instantiated.");
    }

    /**
     * The mode of the build.
     */
    private static String buildMode = null;

    /**
     * The mode of the architecture tests.
     */
    private static String architectureMode = null;

    /**
     * The mode of the aspect-oriented programming (AOP) configuration.
     */
    private static String aopMode = null;

    /**
     * The package that is restricted from certain operations.
     */
    private static String restrictedPackage = null;

    /**
     * The main class.
     */
    private static String mainClass = null;

    /**
     * List of packages that are allowed to be instrumented.
     */
    private static String[] allowedListedPackages = null;

    /**
     * List of classes that are allowed to be instrumented.
     */
    private static String[] allowedListedClasses = null;

    /**
     * Paths that are allowed to be read.
     */
    private static String[] pathsAllowedToBeRead = null;

    /**
     * Paths that are allowed to be overwritten.
     */
    private static String[] pathsAllowedToBeOverwritten = null;

    /**
     * Paths that are allowed to be executed.
     */
    private static String[] pathsAllowedToBeExecuted = null;

    /**
     * Paths that are allowed to be executed.
     */
    private static String[] pathsAllowedToBeDeleted = null;

    /**
     * Hosts that are allowed to be connected to.
     */
    private static String[] hostsAllowedToBeConnectedTo = null;

    /**
     * Ports that are allowed to be connected to.
     */
    private static int[] portsAllowedToBeConnectedTo = null;

    /**
     * Hosts that are allowed to send data to.
     */
    private static String[] hostsAllowedToBeSentTo = null;

    /**
     * Ports that are allowed to send data to.
     */
    private static int[] portsAllowedToBeSentTo = null;

    /**
     * Hosts that are allowed to receive data from.
     */
    private static String[] hostsAllowedToBeReceivedFrom = null;

    /**
     * Ports that are allowed to receive data from.
     */
    private static int[] portsAllowedToBeReceivedFrom = null;

    /**
     * Commands that are allowed to be executed.
     */
    private static String[] commandsAllowedToBeExecuted = null;

    /**
     * Arguments that are allowed to be passed to the commands.
     */
    private static String[][] argumentsAllowedToBePassed = null;

    /**
     * Classes of threads that are allowed to be created.
     */
    private static String[] threadClassAllowedToBeCreated = null;

    /**
     * Number of threads that are allowed to be created.
     */
    private static int[] threadNumberAllowedToBeCreated = null;

    /**
     * Resets the configuration settings to their default values.
     */
    private static void reset() {
        JavaAOPTestCaseSettings.buildMode = null;
        JavaAOPTestCaseSettings.architectureMode = null;
        JavaAOPTestCaseSettings.aopMode = null;
        JavaAOPTestCaseSettings.restrictedPackage = null;
        JavaAOPTestCaseSettings.mainClass = null;
        JavaAOPTestCaseSettings.allowedListedPackages = null;
        JavaAOPTestCaseSettings.allowedListedClasses = null;
        JavaAOPTestCaseSettings.pathsAllowedToBeRead = null;
        JavaAOPTestCaseSettings.pathsAllowedToBeOverwritten = null;
        JavaAOPTestCaseSettings.pathsAllowedToBeExecuted = null;
        JavaAOPTestCaseSettings.pathsAllowedToBeDeleted = null;
        JavaAOPTestCaseSettings.hostsAllowedToBeConnectedTo = null;
        JavaAOPTestCaseSettings.portsAllowedToBeConnectedTo = null;
        JavaAOPTestCaseSettings.hostsAllowedToBeSentTo = null;
        JavaAOPTestCaseSettings.portsAllowedToBeSentTo = null;
        JavaAOPTestCaseSettings.hostsAllowedToBeReceivedFrom = null;
        JavaAOPTestCaseSettings.portsAllowedToBeReceivedFrom = null;
        JavaAOPTestCaseSettings.commandsAllowedToBeExecuted = null;
        JavaAOPTestCaseSettings.argumentsAllowedToBePassed = null;
        JavaAOPTestCaseSettings.threadClassAllowedToBeCreated = null;
        JavaAOPTestCaseSettings.threadNumberAllowedToBeCreated = null;
    }
}