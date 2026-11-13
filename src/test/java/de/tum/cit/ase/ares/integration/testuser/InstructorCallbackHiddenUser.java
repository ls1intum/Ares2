package de.tum.cit.ase.ares.integration.testuser;

import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.ExtensionContext;

import de.tum.cit.ase.ares.api.Deadline;
import de.tum.cit.ase.ares.api.Policy;
import de.tum.cit.ase.ares.api.jupiter.Hidden;
import de.tum.cit.ase.ares.api.jupiter.HiddenTest;

/**
 * A hidden test whose instructor-written JUnit extension fails before the test
 * starts. Ares does not wrap other extensions' callbacks, so that failure keeps
 * its text even for a hidden test; this fixture pins that documented limit.
 */
@Hidden
@Deadline("2000-01-01 00:00")
@Policy(activated = false)
@ExtendWith(InstructorCallbackHiddenUser.FailingInstructorExtension.class)
public class InstructorCallbackHiddenUser {

	/** The body is never reached after the instructor's callback fails. */
	@HiddenTest
	void hidden() {
	}

	/** An instructor's extension that fails before every test. */
	public static final class FailingInstructorExtension implements BeforeEachCallback {

		/** Fails with a text the instructor chose. */
		@Override
		public void beforeEach(ExtensionContext context) {
			throw new AssertionError("INSTRUCTOR_CALLBACK_TEXT");
		}
	}
}
