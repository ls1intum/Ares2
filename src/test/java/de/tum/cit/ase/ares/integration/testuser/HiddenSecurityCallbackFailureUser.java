package de.tum.cit.ase.ares.integration.testuser;

import de.tum.cit.ase.ares.api.Deadline;
import de.tum.cit.ase.ares.api.Policy;
import de.tum.cit.ase.ares.api.jupiter.Hidden;
import de.tum.cit.ase.ares.api.jupiter.HiddenTest;

/** A hidden fixture whose policy callback fails before its method starts. */
@Hidden
@Deadline("2000-01-01 00:00")
@Policy("missing-hidden-policy.yaml")
public class HiddenSecurityCallbackFailureUser {

	/** The body is never reached after policy setup fails. */
	@HiddenTest
	void hidden() {
	}
}
