package de.tum.cit.ase.ares.integration.testuser;

import org.junit.jupiter.api.Test;

/** A plain JUnit test that Ares does not supervise, failing with a message. */
public class UnsupervisedFailureUser {

	/** Prints a line and fails with a message. */
	@Test
	void plainFailure() {
		System.out.println("PLAIN_OUTPUT");
		throw new AssertionError("PLAIN_FAILURE_TEXT");
	}
}
