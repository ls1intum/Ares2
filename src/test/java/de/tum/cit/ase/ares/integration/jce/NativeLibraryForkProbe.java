package de.tum.cit.ase.ares.integration.jce;

import de.tum.cit.ase.ares.api.aop.java.instrumentation.JavaInstrumentationAgent;

import example.nativelibrary.NativeLibraryFirstUser;

/**
 * Trusted child launcher that arms a real policy as {@link JceForkProbe} does,
 * then lets the student use an image or the preferences for the first time. It
 * is kept apart from {@link JceForkProbe} because that class is copied alone
 * into the Precompile consumer build.
 */
public final class NativeLibraryForkProbe {

	/** Prevents instances of the launcher. */
	private NativeLibraryForkProbe() {
	}

	/**
	 * Runs one first library use, named by the first argument ({@code image} or
	 * {@code preferences}), and leaves ordering markers in its log.
	 */
	public static void main(String[] arguments) {
		JceForkProbe.armPolicy(arguments);
		checkFirstLibraryUse(arguments[0]);
		JavaInstrumentationAgent.throwIfTransformationFailed();
		System.out.println("JCE_PROBE_PASSED");
	}

	/**
	 * Lets the student use an image or the preferences for the first time, then
	 * asks for the same library from launcher code beneath student code, which must
	 * be refused.
	 */
	private static void checkFirstLibraryUse(String operation) {
		String library = "image".equals(operation) ? "awt" : "prefs";
		System.out.println("NATIVE_LIBRARY_FIRST_USE");
		System.out.println("NATIVE_LIBRARY_OUTCOME " + firstUseOutcome(operation));
		try {
			NativeLibraryFirstUser.run(() -> System.loadLibrary(library));
		} catch (SecurityException denied) {
			System.out.println("NATIVE_LIBRARY_LAUNCHER_LOAD_DENIED " + denied.getMessage());
			return;
		}
		throw new AssertionError("The launcher's own load of " + library + " was not refused");
	}

	/**
	 * Runs the student's first use and describes on one line how it ended, naming
	 * every cause of a failure.
	 */
	private static String firstUseOutcome(String operation) {
		try {
			return "OK " + ("image".equals(operation) ? NativeLibraryFirstUser.readPixel()
					: NativeLibraryFirstUser.openPreferences());
		} catch (RuntimeException | Error failure) {
			StringBuilder description = new StringBuilder("FAILED");
			for (Throwable cause = failure; cause != null; cause = cause.getCause()) {
				description.append(" | ").append(cause);
			}
			return description.toString().replace('\n', ' ');
		}
	}
}
