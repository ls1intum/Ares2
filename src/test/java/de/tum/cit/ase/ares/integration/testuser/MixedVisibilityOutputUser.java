package de.tum.cit.ase.ares.integration.testuser;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;

import de.tum.cit.ase.ares.api.Deadline;
import de.tum.cit.ase.ares.api.MirrorOutput;
import de.tum.cit.ase.ares.api.Policy;
import de.tum.cit.ase.ares.api.jupiter.HiddenTest;
import de.tum.cit.ase.ares.api.jupiter.Public;
import de.tum.cit.ase.ares.api.jupiter.PublicTest;

/** Checks class lifecycle output shared by public and hidden methods. */
@Public
@Policy(activated = false)
public class MixedVisibilityOutputUser {

	/** Emits class setup text that belongs to a mixed class. */
	@BeforeAll
	static void beforeAll() {
		System.out.print("SECRET_HIDDEN_MIXED_BEFORE_ALL");
	}

	/** Emits class teardown text that belongs to a mixed class. */
	@AfterAll
	static void afterAll() {
		System.err.print("SECRET_HIDDEN_MIXED_AFTER_ALL");
	}

	/** Keeps the public method and its output observable. */
	@PublicTest
	@MirrorOutput(MirrorOutput.MirrorOutputPolicy.ENABLED)
	void publicBody() {
		System.out.print("VISIBLE_PUBLIC_MIXED_BODY");
	}

	/** Keeps the hidden method's failure opaque. */
	@HiddenTest
	@Deadline("2000-01-01 00:00")
	void hiddenBody() {
		System.err.print("SECRET_HIDDEN_MIXED_BODY");
		throw new AssertionError("SECRET_HIDDEN_MIXED_FAILURE");
	}
}
