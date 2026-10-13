package de.tum.cit.ase.ares.testutilities;

import java.lang.reflect.Field;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.PosixFilePermissions;

/**
 * A program run in its own JVM by {@code TrustedStartupFreezeTest}. It does
 * what student code could do first thing: change a JVM property, then use what
 * that property steers, and prints what it observed.
 */
public final class StartupFreezeProbe {

	/** The settings class whose two copies hold the frozen temp directory. */
	private static final String SETTINGS_CLASS_NAME = "de.tum.cit.ase.ares.api.aop.java.JavaAOPTestCaseSettings";

	/**
	 * Prevents instantiation of this program.
	 */
	private StartupFreezeProbe() {
		throw new UnsupportedOperationException("StartupFreezeProbe is a program, not an object");
	}

	/**
	 * Runs one probe, after pointing a property at the directory. {@code temp}
	 * changes {@code java.io.tmpdir} and prints where a new temp file lands.
	 * {@code settings} changes it and prints the frozen temp directory of the
	 * application and the bootstrap settings copy. {@code javaHome} changes
	 * {@code java.home} and prints the Java home the file-system aspect trusts and
	 * the changed property.
	 *
	 * @param args the probe name and the directory
	 * @throws Exception if the probe cannot run
	 */
	public static void main(String[] args) throws Exception {
		switch (args[0]) {
		case "temp" -> {
			System.setProperty("java.io.tmpdir", args[1]);
			Path created = Files.createTempFile("ares-freeze-probe-", ".tmp", ownerOnly());
			try {
				System.out.println("RESULT=" + created.getParent().toRealPath());
			} finally {
				Files.deleteIfExists(created);
			}
		}
		case "settings" -> {
			System.setProperty("java.io.tmpdir", args[1]);
			System.out.println("RESULT=" + frozenTempDirectory(ClassLoader.getSystemClassLoader()) + "|"
					+ frozenTempDirectory(null));
		}
		default -> {
			System.setProperty("java.home", args[1]);
			Class<?> aspect = Class.forName(
					"de.tum.cit.ase.ares.api.aop.java.aspectj.adviceandpointcut.JavaAspectJFileSystemAdviceDefinitions");
			Field trustedJavaHome = aspect.getDeclaredField("TRUSTED_JAVA_HOME");
			trustedJavaHome.setAccessible(true);
			System.out.println("RESULT=" + trustedJavaHome.get(null) + "|" + System.getProperty("java.home"));
		}
		}
	}

	/**
	 * Owner-only permissions for the probe's temp file, where the file system has
	 * POSIX permissions; none otherwise.
	 *
	 * @return the file attributes to create the temp file with
	 */
	private static FileAttribute<?>[] ownerOnly() {
		if (!FileSystems.getDefault().supportedFileAttributeViews().contains("posix")) {
			return new FileAttribute<?>[0];
		}
		return new FileAttribute<?>[] {
				PosixFilePermissions.asFileAttribute(PosixFilePermissions.fromString("rw-------")) };
	}

	/**
	 * The frozen temp directory one copy of the settings holds.
	 *
	 * @param loader the loader of the copy, {@code null} for the bootstrap copy
	 * @return its value, or {@code absent} when that copy does not exist
	 * @throws ReflectiveOperationException if the field cannot be read
	 */
	private static Object frozenTempDirectory(ClassLoader loader) throws ReflectiveOperationException {
		Class<?> settings;
		try {
			settings = Class.forName(SETTINGS_CLASS_NAME, true, loader);
		} catch (ClassNotFoundException absent) {
			return "absent";
		}
		Field frozen = settings.getDeclaredField("frozenDefaultTempDirectory");
		frozen.setAccessible(true);
		return frozen.get(null);
	}
}
