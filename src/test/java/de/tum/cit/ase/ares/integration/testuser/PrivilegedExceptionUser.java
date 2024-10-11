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
@PrivilegedExceptionsOnly("ABC")
@MirrorOutput(MirrorOutputPolicy.DISABLED)
@StrictTimeout(value = 300, unit = TimeUnit.MILLISECONDS)
@TestMethodOrder(MethodName.class)
//@WhitelistPath(value = "target/**", type = PathType.GLOB)
//@BlacklistPath(value = "**Test*.{java,class}", type = PathType.GLOB)
@SuppressWarnings("static-method")
public class PrivilegedExceptionUser {

	@PublicTest
	void nonprivilegedExceptionExtern() {
		PrivilegedExceptionPenguin.throwNullPointerException();
	}

	@PrivilegedExceptionsOnly("ABC")
	@PublicTest
	void nonprivilegedExceptionIntern() {
		throw new NullPointerException("xy");
	}

	@PrivilegedExceptionsOnly("ABC")
	@PublicTest
	void nonprivilegedExceptionTry() {
		PrivilegedExceptionPenguin.throwPrivilegedNullPointerException();
	}

	@PrivilegedExceptionsOnly("ABC")
	@PublicTest
	void privilegedExceptionFail() {
		TestUtils.privilegedThrow(() -> {
			fail("xyz");
		});
	}

	@PrivilegedExceptionsOnly("ABC")
	@PublicTest
	void privilegedExceptionNormal() throws Exception {
		TestUtils.privilegedThrow(() -> {
			throw new NullPointerException("xyz");
		});
	}

	@PrivilegedExceptionsOnly("ABC")
	@PublicTest
	void privilegedTimeout() throws InterruptedException {
		Thread.sleep(1000);
	}
}
