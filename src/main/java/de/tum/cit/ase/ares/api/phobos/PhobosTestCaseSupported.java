package de.tum.cit.ase.ares.api.phobos;

import java.util.List;

public interface PhobosTestCaseSupported {

	/**
	 * Retrieves the list of Phobos test cases for isolating exercise evaluation
	 * environment.
	 *
	 * @since 2.0.1
	 * @return a list of Phobos test cases
	 */

	List<PhobosTestCaseSupported> getPhobosTestCases();
}
