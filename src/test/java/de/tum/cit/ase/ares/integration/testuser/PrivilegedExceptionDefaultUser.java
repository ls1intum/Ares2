package de.tum.cit.ase.ares.integration.testuser;

import static org.junit.Assert.fail;

import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.MethodOrderer.MethodName;
import org.junit.jupiter.api.TestMethodOrder;

import de.tum.cit.ase.ares.api.*;
import de.tum.cit.ase.ares.api.MirrorOutput.MirrorOutputPolicy;
import de.tum.cit.ase.ares.api.jupiter.PublicTest;
import de.tum.cit.ase.ares.api.localization.UseLocale;
import de.tum.cit.ase.ares.integration.testuser.subject.PrivilegedExceptionPenguin;

/**
 * Carries no {@code @PrivilegedExceptionsOnly} at all, so every test below
 * relies only on the policy's own default. This proves the policy-only path end
 * to end through the real reporting and timeout handling.
 */
@UseLocale("en")
@MirrorOutput(MirrorOutputPolicy.DISABLED)
@StrictTimeout(value = 300, unit = TimeUnit.MILLISECONDS)
@TestMethodOrder(MethodName.class)
@Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/aspectj/PolicyPrivilegedExceptionDefaultUser.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/testuser/subject/helloWorld")
@SuppressWarnings("static-method")
public class PrivilegedExceptionDefaultUser {

	/** Fails with an ordinary assertion. */
	@PublicTest
	void nonprivilegedFailure() {
		fail("xyz");
	}

	/** Fails with a privileged assertion. */
	@PublicTest
	void privilegedAssertion() {
		TestUtils.privilegedThrow(() -> {
			fail("xyz");
		});
	}

	/** Throws a privileged exception. */
	@PublicTest
	void privilegedException() {
		PrivilegedExceptionPenguin.throwPrivilegedNullPointerException();
	}

	/** Sleeps past the class's timeout. */
	@PublicTest
	void policyDefaultTimeout() throws InterruptedException {
		Thread.sleep(1000);
	}
}
