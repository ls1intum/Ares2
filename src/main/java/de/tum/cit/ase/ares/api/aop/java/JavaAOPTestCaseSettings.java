package de.tum.cit.ase.ares.api.aop.java;

import de.tum.cit.ase.ares.api.localization.Messages;

/**
 * Configuration settings for Java instrumentation aspect configurations.
 * <p>
 * This class holds the static configuration settings used by Java
 * instrumentation aspects, such as allowed file paths, network hosts and ports,
 * command executions, and thread creations. These settings are used to control
 * and manage various security-related behaviors in the application.
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
	 * Lock object for synchronizing access to all security-related settings.
	 * <p>
	 * This ensures thread-safe access to settings during concurrent test execution,
	 * preventing race conditions that could lead to security bypasses.
	 * </p>
	 */
	private static final Object SETTINGS_LOCK = new Object();

	/**
	 * Private constructor to prevent instantiation of this utility class.
	 * <p>
	 * The constructor throws an {@link SecurityException} to enforce the utility
	 * class pattern.
	 * </p>
	 */
	private JavaAOPTestCaseSettings() {
		throw new SecurityException(Messages.localized("security.general.utility.initialization", "JavaAOPTestCaseSettings"));
	}

	/**
	 * Returns the lock object used for synchronizing access to settings.
	 * <p>
	 * External code that needs to perform atomic check-and-modify operations on
	 * settings should synchronize on this lock.
	 * </p>
	 *
	 * @return the settings lock object
	 */
	public static Object getSettingsLock() {
		return SETTINGS_LOCK;
	}

	/**
	 * The mode of the build.
	 */
	private static volatile String buildMode = null;

	/**
	 * The mode of the architecture tests.
	 */
	private static volatile String architectureMode = null;

	/**
	 * The mode of the aspect-oriented programming (AOP) configuration.
	 */
	private static volatile String aopMode = null;

	/**
	 * The package that is restricted from certain operations.
	 */
	private static volatile String restrictedPackage = null;

	/**
	 * The main class.
	 */
	private static volatile String mainClass = null;

	/**
	 * List of packages that are allowed to be instrumented.
	 */
	private static volatile String[] allowedListedPackages = null;

	/**
	 * List of classes that are allowed to be instrumented.
	 */
	private static volatile String[] allowedListedClasses = null;

	/**
	 * Paths that are allowed to be read.
	 */
	private static volatile String[] pathsAllowedToBeRead = null;

	/**
	 * Paths that are allowed to be overwritten.
	 */
	private static volatile String[] pathsAllowedToBeOverwritten = null;

	/**
	 * Paths that are allowed to be created.
	 */
	private static volatile String[] pathsAllowedToBeCreated = null;

	/**
	 * Paths that are allowed to be executed.
	 */
	private static volatile String[] pathsAllowedToBeExecuted = null;

	/**
	 * Paths that are allowed to be executed.
	 */
	private static volatile String[] pathsAllowedToBeDeleted = null;

	/**
	 * Hosts that are allowed to be connected to.
	 */
	private static volatile String[] hostsAllowedToBeConnectedTo = null;

	/**
	 * Ports that are allowed to be connected to.
	 */
	private static volatile int[] portsAllowedToBeConnectedTo = null;

	/**
	 * Hosts that are allowed to send data to.
	 */
	private static volatile String[] hostsAllowedToBeSentTo = null;

	/**
	 * Ports that are allowed to send data to.
	 */
	private static volatile int[] portsAllowedToBeSentTo = null;

	/**
	 * Hosts that are allowed to receive data from.
	 */
	private static volatile String[] hostsAllowedToBeReceivedFrom = null;

	/**
	 * Ports that are allowed to receive data from.
	 */
	private static volatile int[] portsAllowedToBeReceivedFrom = null;

	/**
	 * Commands that are allowed to be executed.
	 */
	private static volatile String[] commandsAllowedToBeExecuted = null;

	/**
	 * Arguments that are allowed to be passed to the commands.
	 */
	private static volatile String[][] argumentsAllowedToBePassed = null;

	/**
	 * Classes of threads that are allowed to be created.
	 */
	private static volatile String[] threadClassAllowedToBeCreated = null;

	/**
	 * Number of threads that are allowed to be created.
	 */
	private static volatile int[] threadNumberAllowedToBeCreated = null;

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
		JavaAOPTestCaseSettings.pathsAllowedToBeCreated = null;
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
