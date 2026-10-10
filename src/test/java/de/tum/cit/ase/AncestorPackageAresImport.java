package de.tum.cit.ase;

import de.tum.cit.ase.ares.api.policy.policySubComponents.PackagePermission;

/**
 * Student code in {@code de.tum.cit.ase} that imports from Ares' own trusted
 * namespace below its package, which its own package must not permit.
 */
public final class AncestorPackageAresImport {

	/**
	 * Not instantiated; the fixture is only analysed.
	 */
	private AncestorPackageAresImport() {
	}

	/**
	 * Uses a class from Ares' own API.
	 *
	 * @return a permission built by Ares' own API
	 */
	public static PackagePermission aresApi() {
		return new PackagePermission("java.util");
	}
}
