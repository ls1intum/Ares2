package de.tum.cit.ase.ares.api.phobos.java;

import java.util.List;

import de.tum.cit.ase.ares.api.phobos.PhobosTestCaseSupported;

public enum JavaPhobosTestCaseSupported implements PhobosTestCaseSupported {

	/**
	 * Aspect configuration for managing file system interactions.
	 */
	FILESYSTEM_INTERACTION,

	/**
	 * Aspect configuration for managing network connections.
	 */
	NETWORK_CONNECTION,

	/**
	 * Aspect configuration for managing thread creation.
	 */
	TIMEOUT;

	@Override
	public List<PhobosTestCaseSupported> getPhobosTestCases() {
		return List.of(JavaPhobosTestCaseSupported.FILESYSTEM_INTERACTION,
				JavaPhobosTestCaseSupported.NETWORK_CONNECTION, JavaPhobosTestCaseSupported.TIMEOUT);
	}
}
