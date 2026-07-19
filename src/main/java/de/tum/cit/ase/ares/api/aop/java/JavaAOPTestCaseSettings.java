package de.tum.cit.ase.ares.api.aop.java;

import de.tum.cit.ase.ares.api.localization.Messages;

/**
 * Configuration settings for Java instrumentation aspect configurations.
 * <p>
 * This class holds the static configuration settings used by Java
 * instrumentation aspects, such as allowed file paths, network hosts and ports,
 * command executions, and thread creations. These settings are used to control
 * and manage various security-related behaviours in the application.
 * </p>
 * <p>
 * As a utility class, it is not intended to be instantiated.
 * </p>
 *
 * @version 2.0.0
 * @since 2.0.0
 */
// Every setting below is read and written by name through reflection, from
// JavaInstrumentationAdviceAbstractToolbox (getValueFromSettings ->
// resolveSettingsField -> getDeclaredField) and from the woven aspects. No compiler-visible
// reference to them exists, so PMD reports each one as an unused private field. They are the
// only channel through which a security policy reaches the advice: deleting one silently
// disarms the check it configures.
@SuppressWarnings("PMD.UnusedPrivateField")
public final class JavaAOPTestCaseSettings {

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
		throw new SecurityException(
				Messages.localized("security.general.utility.initialization", "JavaAOPTestCaseSettings"));
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
	 * <p>
	 * This method is called via reflection from
	 * {@link de.tum.cit.ase.ares.api.jupiter.JupiterSecurityExtension} when a test
	 * does not have a {@link de.tum.cit.ase.ares.api.Policy} annotation or when the
	 * policy is explicitly deactivated with {@code @Policy(activated = false)}.
	 * This ensures that AOP advices from previous tests do not affect subsequent
	 * tests running in the same JVM instance.
	 * </p>
	 * <p>
	 * Design Rationale (I-032, scoped): synchronizes on {@link #SETTINGS_LOCK} so
	 * this whole-set reset cannot interleave, field by field, with a concurrent
	 * {@link de.tum.cit.ase.ares.api.aop.java.JavaAOPTestCase#setJavaAdviceSettingValue}
	 * call (which synchronizes on the same lock — see that class). This closes the
	 * race between concurrent <em>writers</em> (two security-test-case setup/reset
	 * sequences interleaving on the same JVM). It deliberately does
	 * <strong>not</strong> attempt the full fix the audit describes (a single
	 * immutable policy object behind one atomically-swapped reference): that would
	 * also require every <em>reader</em> — the settings-reading prelude of all
	 * eight {@code check*InteractionImpl} methods across both AOP backends — to
	 * synchronize on the same lock for its whole multi-field read sequence, a much
	 * larger change this session cannot safely verify without running the full
	 * test/architecture matrix. See the audit write-up for the full reasoning; this
	 * is the "scope the first commit to the swap mechanism" fallback it explicitly
	 * permits.
	 * </p>
	 */
	public static void reset() {
		synchronized (SETTINGS_LOCK) {
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
}
