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
// Replaces the former inert @WhitelistPath("target/**")/@BlacklistPath with the equivalent
// active @Policy, scanned against the benign HelloWorld subject.
@Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/aspectj/PolicyPrivilegedExceptionUser.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/testuser/subject/helloWorld")
@SuppressWarnings("static-method")
public class PrivilegedExceptionUser {

	@PublicTest
	void nonprivilegedExceptionExtern() {
		PrivilegedExceptionPenguin.throwNullPointerException();
	}

	@PublicTest
	void nonprivilegedExceptionIntern() {
		throw new NullPointerException("xy");
	}

	@PublicTest
	void nonprivilegedExceptionTry() {
		PrivilegedExceptionPenguin.throwPrivilegedNullPointerException();
	}

	@PublicTest
	void privilegedExceptionFail() {
		TestUtils.privilegedThrow(() -> {
			fail("xyz");
		});
	}

	@PublicTest
	void privilegedExceptionNormal() throws Exception {
		TestUtils.privilegedThrow(() -> {
			throw new NullPointerException("xyz");
		});
	}

	@PublicTest
	void privilegedTimeout() throws InterruptedException {
		Thread.sleep(1000);
	}
}
