package de.tum.cit.ase.ares.api.architecture.java.packageimport.fixtures;

import java.lang.management.ManagementFactory;

/**
 * Fixture reading the class path through {@code java.lang.management}, which
 * lies below the baseline {@code java.lang}. The import rule allows it, so the
 * environment rule has to refuse it.
 */
public class UsesManagement {

	/**
	 * Reads the class path without {@code System.getProperty}.
	 *
	 * @return the class path of the running JVM
	 */
	public String classPath() {
		return ManagementFactory.getRuntimeMXBean().getClassPath();
	}
}
