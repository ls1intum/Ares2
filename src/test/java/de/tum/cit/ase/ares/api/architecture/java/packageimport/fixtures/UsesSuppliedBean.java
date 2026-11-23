package de.tum.cit.ase.ares.api.architecture.java.packageimport.fixtures;

import java.lang.management.RuntimeMXBean;

/**
 * Fixture reading the class path from a management bean it is handed, without
 * calling {@code ManagementFactory} itself.
 */
public class UsesSuppliedBean {

	/**
	 * Reads the class path from the given bean.
	 *
	 * @param bean a runtime bean obtained elsewhere
	 * @return the class path of the running JVM
	 */
	public String classPathOf(RuntimeMXBean bean) {
		return bean.getClassPath();
	}
}
