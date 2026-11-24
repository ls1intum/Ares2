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
	 * Lock object for synchronising access to all security-related settings.
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
	 * Returns the lock object used for synchronising access to settings.
	 * <p>
	 * External code that needs to perform atomic check-and-modify operations on
	 * settings should synchronise on this lock.
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
	 * The default temp directory, resolved to its real location, that trusted Ares
	 * code fixed when the JVM started. It holds for the whole JVM, so
	 * {@link #reset()} leaves it alone; {@code null} means nothing fixed it, and a
	 * temp file without a named directory is then refused.
	 */
	private static volatile String frozenDefaultTempDirectory = null;

	/**
	 * The thread that ended the last supervised test, or {@code null} while a test
	 * runs supervised. Only this thread, which runs JUnit and Ares, goes unchecked
	 * after a test has ended; any other thread the test left running, such as a
	 * shutdown hook, keeps being checked against no allowance at all.
	 */
	private static volatile Thread supervisionEndedOnThread = null;

	/**
	 * Whether no thread is checked at all, because Ares does not guard what runs
	 * now: a test whose policy is deactivated, or a class phase without a policy.
	 */
	private static volatile boolean supervisionSuspended = false;

	/**
	 * Ends a supervised test. Every allowance goes, but the package, the test
	 * classes and the modes stay, so code the test left running is refused from now
	 * on. The calling thread goes unchecked until the next test is armed. The
	 * package is never cleared here, since a moment without it would check nothing.
	 */
	public static void endSupervision() {
		synchronized (SETTINGS_LOCK) {
			clearAllowances();
			JavaAOPTestCaseSettings.supervisionEndedOnThread = Thread.currentThread();
			JavaAOPTestCaseSettings.supervisionSuspended = false;
		}
	}

	/**
	 * Lifts every check until {@link #endSupervision()} runs, for a phase Ares does
	 * not guard.
	 */
	public static void suspendSupervision() {
		supervisionSuspended = true;
	}

	/**
	 * Resets every setting to its default, so nothing is checked at all, except the
	 * frozen temp directory, which holds for the whole JVM. Writers synchronise on
	 * {@link #SETTINGS_LOCK}, so a reset never interleaves field by field with a
	 * write of {@code JavaAOPTestCase}; readers do not, so it is not atomic for
	 * them.
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
			clearAllowances();
			JavaAOPTestCaseSettings.supervisionEndedOnThread = null;
			JavaAOPTestCaseSettings.supervisionSuspended = false;
		}
	}

	/**
	 * Removes every allowance of the policy, for files, network, commands and
	 * threads. The caller holds {@link #SETTINGS_LOCK}.
	 */
	private static void clearAllowances() {
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
