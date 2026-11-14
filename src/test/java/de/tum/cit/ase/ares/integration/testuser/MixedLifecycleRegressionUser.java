package de.tum.cit.ase.ares.integration.testuser;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInfo;

import de.tum.cit.ase.ares.api.Deadline;
import de.tum.cit.ase.ares.api.Policy;
import de.tum.cit.ase.ares.api.io.IOTester;
import de.tum.cit.ase.ares.api.jupiter.HiddenTest;
import de.tum.cit.ase.ares.api.jupiter.Public;
import de.tum.cit.ase.ares.api.jupiter.PublicTest;

/**
 * Exercises public setup failures beside hidden tests and recorded setup
 * output.
 */
@Public
@Policy(activated = false)
public class MixedLifecycleRegressionUser {

	/** Emits output shared by the class's public and hidden tests. */
	@BeforeAll
	static void beforeAll() {
		System.out.print("SECRET_SHARED_SETUP");
	}

	/** Writes grading input and fails only the named public test. */
	@BeforeEach
	void setUp(TestInfo testInfo) {
		System.out.print("ready");
		System.out.flush();
		if (testInfo.getTestMethod().orElseThrow().getName().equals("publicSetupFailure")) {
			throw new AssertionError("VISIBLE_PUBLIC_SETUP_FAILURE");
		}
	}

	/** Its setup failure must keep its public diagnostic. */
	@PublicTest
	void publicSetupFailure() {
	}

	/** Checks that setup output remains available to the I/O tester. */
	@HiddenTest
	@Deadline("2000-01-01 00:00")
	void hiddenChecksSetupOutput(IOTester io) {
		assertThat(io.out().getOutputAsString()).contains("ready");
	}
}
