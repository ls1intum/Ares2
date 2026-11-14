package de.tum.cit.ase.ares.integration.testuser;

import de.tum.cit.ase.ares.api.Deadline;
import de.tum.cit.ase.ares.api.Policy;
import de.tum.cit.ase.ares.api.WithIOManager;
import de.tum.cit.ase.ares.api.io.AresIOContext;
import de.tum.cit.ase.ares.api.io.IOManager;
import de.tum.cit.ase.ares.api.jupiter.Hidden;
import de.tum.cit.ase.ares.api.jupiter.HiddenTest;

/** A hidden fixture whose I/O callback fails before its test method starts. */
@Hidden
@Deadline("2000-01-01 00:00")
@Policy(activated = false)
@WithIOManager(HiddenCallbackFailureUser.ThrowingManager.class)
public class HiddenCallbackFailureUser {

	/** The body is never reached after the callback fails. */
	@HiddenTest
	void hidden() {
	}

	/** An I/O manager that fails during its setup callback. */
	public static final class ThrowingManager implements IOManager<Void> {

		/** Fails before the hidden method can start. */
		@Override
		public void beforeTestExecution(AresIOContext context) {
			throw new AssertionError("SECRET_CALLBACK_FAILURE");
		}

		/** Has no state to release. */
		@Override
		public void afterTestExecution(AresIOContext context) {
		}

		/** Provides no controller. */
		@Override
		public Void getControllerInstance(AresIOContext context) {
			return null;
		}

		/** Provides no controller type. */
		@Override
		public Class<Void> getControllerClass() {
			return null;
		}
	}
}
