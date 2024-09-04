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
public class JavaSecurityTestCaseSettings {

    /**
     * Private constructor to prevent instantiation of this utility class.
     * <p>
     * The constructor throws an {@link IllegalStateException} to enforce the utility class pattern.
     * </p>
     */
    private JavaSecurityTestCaseSettings() {
        throw new IllegalStateException("Utility class");
    }

    private static String aomMode = null;

    /**
     * The package that is restricted from certain operations.
     */
    private static String restrictedPackage = null;

    /**
     * List of classes that are allowed to be instrumented.
     */
    private static String[] allowedListedClasses = {
            "de.tum.cit.ase.ares.api.aop.java.JavaSecurityTestCaseSettings",
            "de.tum.cit.ase.ares.api.aop.instrumentation.adviceandpointcut.JavaInstrumentationAdviceToolbox",
            "de.tum.cit.ase.ares.api.aop.instrumentation.adviceandpointcut.JavaInstrumentationReadPathAdvice",
            "de.tum.cit.ase.ares.api.aop.instrumentation.adviceandpointcut.JavaWritePathAdvice",
            "de.tum.cit.ase.ares.api.aop.instrumentation.adviceandpointcut.JavaInstrumentationExecutePathAdvice",
            "de.tum.cit.ase.ares.api.aop.java.pointcut.instrumentation.JavaInstrumentationPointcutDefinitions",
            "de.tum.cit.ase.ares.api.aop.java.pointcut.instrumentation.JavaInstrumentationBindingDefinitions",
            "de.tum.cit.ase.ares.api.aop.java.instrumentation.JavaInstrumentationAgent"
    };

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
     * Hosts that are allowed to be connected to.
     */
    private static String[] hostsAllowedToBeConnectedTo = null;

    /**
     * Ports that are allowed to be connected to.
     */
    private static int[] portsAllowedToBeConnecedTo = null;

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
     * Number of threads that are allowed to be created.
     */
    private static int[] threadNumberAllowedToBeCreated = null;

    /**
     * Classes of threads that are allowed to be created.
     */
    private static String[] threadClassAllowedToBeCreated = null;

    private static void reset() {
        JavaSecurityTestCaseSettings.aomMode = null;
        JavaSecurityTestCaseSettings.restrictedPackage = null;
        JavaSecurityTestCaseSettings.allowedListedClasses = new String[]{
                "de.tum.cit.ase.ares.api.aop.java.instrumentation.advice.JavaInstrumentationAdviceToolbox",
                "de.tum.cit.ase.ares.api.aop.java.instrumentation.advice.JavaInstrumentationReadPathAdvice",
                "de.tum.cit.ase.ares.api.aop.java.instrumentation.advice.JavaInstrumentationOverwritePathAdvice",
                "de.tum.cit.ase.ares.api.aop.java.instrumentation.advice.JavaInstrumentationExecutePathAdvice",
                "de.tum.cit.ase.ares.api.aop.java.instrumentation.pointcut.JavaInstrumentationPointcutDefinitions",
                "de.tum.cit.ase.ares.api.aop.java.instrumentation.pointcut.JavaInstrumentationBindingDefinitions",
                "de.tum.cit.ase.ares.api.aop.java.instrumentation.JavaInstrumentationAgent",
                "de.tum.cit.ase.ares.api.aop.java.JavaSecurityTestCaseSettings"
        };
        JavaSecurityTestCaseSettings.pathsAllowedToBeRead = null;
        JavaSecurityTestCaseSettings.pathsAllowedToBeOverwritten = null;
        JavaSecurityTestCaseSettings.pathsAllowedToBeExecuted = null;
        JavaSecurityTestCaseSettings.hostsAllowedToBeConnectedTo = null;
        JavaSecurityTestCaseSettings.portsAllowedToBeConnecedTo = null;
        JavaSecurityTestCaseSettings.hostsAllowedToBeSentTo = null;
        JavaSecurityTestCaseSettings.portsAllowedToBeSentTo = null;
        JavaSecurityTestCaseSettings.hostsAllowedToBeReceivedFrom = null;
        JavaSecurityTestCaseSettings.portsAllowedToBeReceivedFrom = null;
        JavaSecurityTestCaseSettings.commandsAllowedToBeExecuted = null;
        JavaSecurityTestCaseSettings.argumentsAllowedToBePassed = null;
        JavaSecurityTestCaseSettings.threadNumberAllowedToBeCreated = null;
        JavaSecurityTestCaseSettings.threadClassAllowedToBeCreated = null;
    }
}