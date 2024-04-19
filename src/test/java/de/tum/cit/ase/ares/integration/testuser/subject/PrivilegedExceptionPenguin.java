package de.tum.cit.ase.ares.integration.testuser.subject;

import de.tum.cit.ase.ares.api.TestUtils;

public final class PrivilegedExceptionPenguin {

	private PrivilegedExceptionPenguin() {
	}

	public static void throwNullPointerException() {
		throw new NullPointerException("xyz");
	}

	public static void throwPrivilegedNullPointerException() {
		TestUtils.privilegedThrow((Runnable) () -> {
			throw new NullPointerException("xyz");
		});
	}
}
