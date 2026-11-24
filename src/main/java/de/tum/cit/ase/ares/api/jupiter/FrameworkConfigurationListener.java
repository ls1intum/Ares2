package de.tum.cit.ase.ares.api.jupiter;

import javax.annotation.Nonnull;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.junit.platform.launcher.LauncherSession;
import org.junit.platform.launcher.LauncherSessionListener;

import de.tum.cit.ase.ares.api.securitytest.FrameworkConfigurationGuard;

/**
 * Runs {@link FrameworkConfigurationGuard} when JUnit opens a test run, before
 * any test is found and before Ares logs anything. A settings file that Logback
 * or JUnit reads only later is therefore refused before it is used. The Ares
 * jar registers this class with JUnit.
 */
@API(status = Status.INTERNAL)
public final class FrameworkConfigurationListener implements LauncherSessionListener {

	/**
	 * Creates the listener JUnit calls once per test run.
	 */
	public FrameworkConfigurationListener() {
		super();
	}

	/**
	 * Refuses the test run if the compiled submission carries such a file.
	 *
	 * @param session the test run JUnit opens
	 * @throws SecurityException naming the file
	 */
	@Override
	public void launcherSessionOpened(@Nonnull LauncherSession session) {
		FrameworkConfigurationGuard.requireNone();
	}
}
