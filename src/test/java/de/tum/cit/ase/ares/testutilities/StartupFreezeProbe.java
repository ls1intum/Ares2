package de.tum.cit.ase.ares.testutilities;

import java.io.File;
import java.lang.reflect.Field;

/**
 * A program run in its own JVM by {@code TrustedStartupFreezeTest}. It does
 * what student code could do first thing: change a JVM property, then use what
 * that property steers, and prints what it observed.
 */
public final class StartupFreezeProbe {

	/**
	 * Prevents instantiation of this program.
	 */
	private StartupFreezeProbe() {
		throw new UnsupportedOperationException("StartupFreezeProbe is a program, not an object");
	}

	/**
	 * Runs one probe. {@code temp <dir>} points {@code java.io.tmpdir} at the
	 * directory and prints where a new temp file lands. {@code javaHome <dir>}
	 * points {@code java.home} at the directory and prints the Java home the
	 * file-system aspect trusts.
	 *
	 * @param args the probe name and the directory
	 * @throws Exception if the probe cannot run
	 */
	public static void main(String[] args) throws Exception {
		if ("temp".equals(args[0])) {
			System.setProperty("java.io.tmpdir", args[1]);
			File created = File.createTempFile("ares-freeze-probe-", ".tmp");
			System.out.println("RESULT=" + created.getParentFile().getCanonicalPath());
			created.delete();
		} else {
			System.setProperty("java.home", args[1]);
			Class<?> aspect = Class.forName(
					"de.tum.cit.ase.ares.api.aop.java.aspectj.adviceandpointcut.JavaAspectJFileSystemAdviceDefinitions");
			Field trustedJavaHome = aspect.getDeclaredField("TRUSTED_JAVA_HOME");
			trustedJavaHome.setAccessible(true);
			System.out.println("RESULT=" + trustedJavaHome.get(null));
		}
	}
}
