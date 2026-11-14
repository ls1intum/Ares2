package de.tum.cit.ase.ares.integration.testuser;

import de.tum.cit.ase.ares.api.Deadline;
import de.tum.cit.ase.ares.api.Policy;
import de.tum.cit.ase.ares.api.jupiter.HiddenTest;

/** Fails during construction before a hidden method's future deadline check. */
@Deadline("2999-01-01 00:00 UTC")
@Policy(activated = false)
public class FutureHiddenConstructorUser {

	/** Whether the hidden method body ran. */
	public static boolean bodyRan;

	/** Emits private text before the constructor fails. */
	public FutureHiddenConstructorUser() {
		System.err.print("SECRET_FUTURE_CONSTRUCTOR");
		throw new IllegalStateException("SECRET_FUTURE_CONSTRUCTOR_FAILURE");
	}

	/** Marks execution if the scheduler reaches the body. */
	@HiddenTest
	void hiddenBody() {
		bodyRan = true;
	}
}
