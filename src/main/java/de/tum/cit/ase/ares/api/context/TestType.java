package de.tum.cit.ase.ares.api.context;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

import de.tum.cit.ase.ares.api.jupiter.*;

/**
 * Type of an Ares test case
 *
 * @see Hidden
 * @see Public
 * @see HiddenTest
 * @see PublicTest
 * @author Christian Femers
 * @since 0.2.0
 * @version 1.1.0
 */
@API(status = Status.MAINTAINED)
public enum TestType {
	PUBLIC,
	HIDDEN
}
