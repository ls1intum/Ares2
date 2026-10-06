package de.tum.cit.ase.ares.integration.aop.forbidden.subject.preferences;

import java.util.prefs.Preferences;

/**
 * Real {@code java.util.prefs.Preferences} calls used to verify that Ares
 * intercepts the Preferences API through its pointcut bindings, not only
 * through the advice logic. Each call is a store entry point that Ares denies
 * before the method body runs, so no backing store is actually touched under a
 * policy that does not permit all paths.
 */
public final class PreferencesMain {

	private PreferencesMain() {
		throw new SecurityException(
				"Ares Security Error (Reason: Ares-Code; Stage: Test): Main is a utility class and should not be instantiated.");
	}

	public static Preferences accessFileSystemViaPreferencesUserRoot() {
		return Preferences.userRoot();
	}

	public static Preferences accessFileSystemViaPreferencesUserNodeForPackage() {
		return Preferences.userNodeForPackage(PreferencesMain.class);
	}
}
