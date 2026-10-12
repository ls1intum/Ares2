package de.tum.cit.ase.ares.integration.testuser;

import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.MethodOrderer.MethodName;
import org.junit.jupiter.api.TestMethodOrder;

import de.tum.cit.ase.ares.api.MirrorOutput;
import de.tum.cit.ase.ares.api.MirrorOutput.MirrorOutputPolicy;
import de.tum.cit.ase.ares.api.Policy;
import de.tum.cit.ase.ares.api.StrictTimeout;
import de.tum.cit.ase.ares.api.jupiter.PublicTest;
import de.tum.cit.ase.ares.api.localization.UseLocale;

/**
 * Tests whose time limit comes from the policy's
 * {@code regardingStrictTimeouts} rather than from {@code @StrictTimeout},
 * under a policy that allows no thread creation.
 */
@UseLocale("en")
@MirrorOutput(MirrorOutputPolicy.DISABLED)
@TestMethodOrder(MethodName.class)
@Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/aspectj/PolicyStrictTimeoutUser.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/testuser/subject/helloWorld")
@SuppressWarnings("static-method")
public class StrictTimeoutPolicyUser {

	/** An endless loop, stopped by the policy's 200 ms. */
	@PublicTest
	void policyStopsAnEndlessLoop() {
		while (!Thread.currentThread().isInterrupted()) {
			Thread.onSpinWait();
		}
	}

	/**
	 * A fast test, inside the policy's limit.
	 *
	 * @throws InterruptedException if interrupted
	 */
	@PublicTest
	void policyLetsAFastTestPass() throws InterruptedException {
		Thread.sleep(20);
	}

	/**
	 * A test slower than the policy's limit, allowed by a longer annotation.
	 *
	 * @throws InterruptedException if interrupted
	 */
	@PublicTest
	@StrictTimeout(value = 2, unit = TimeUnit.SECONDS)
	void annotationLetsASlowTestPass() throws InterruptedException {
		Thread.sleep(500);
	}
}
