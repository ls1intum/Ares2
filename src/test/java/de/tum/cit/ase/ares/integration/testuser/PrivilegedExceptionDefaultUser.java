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

@UseLocale("en")
@MirrorOutput(MirrorOutputPolicy.DISABLED)
@StrictTimeout(value = 300, unit = TimeUnit.MILLISECONDS)
@TestMethodOrder(MethodName.class)
// Deliberately carries no @PrivilegedExceptionsOnly annotation at all: every test
// method below relies solely on the policy's own theFollowingTestBehaviorIsConfigured
// default, proving the policy-only path end to end through the real
// JupiterSecurityExtension -> ReportingUtils/TimeoutUtils pipeline.
@Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/aspectj/PolicyPrivilegedExceptionDefaultUser.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/testuser/subject/helloWorld")
@SuppressWarnings("static-method")
public class PrivilegedExceptionDefaultUser {

	@PublicTest
	void nonprivilegedFailure() {
		fail("xyz");
	}

	@PublicTest
	void privilegedAssertion() {
		TestUtils.privilegedThrow(() -> {
			fail("xyz");
		});
	}

	@PublicTest
	void privilegedException() {
		PrivilegedExceptionPenguin.throwPrivilegedNullPointerException();
	}

	@PublicTest
	void policyDefaultTimeout() throws InterruptedException {
		Thread.sleep(1000);
	}
}
