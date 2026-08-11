package com.foo;

import example.student.AspectJSecurityProbe;
import example.student.InstrumentationSecurityProbe;

/**
 * Fixture for I-105/TD-051's package-boundary case: a class genuinely inside an
 * allow-listed package ({@code com.foo}). Both {@link #checkAspectJ} and
 * {@link #checkInstrumentation} must report no violation. The allow-listed
 * package itself comes from the {@code allowedListedPackages} setting (set by
 * the test before calling these), not a method parameter.
 */
public final class AllowedPackageMember {

	private AllowedPackageMember() {
	}

	public static String checkAspectJ() throws Exception {
		return AspectJSecurityProbe.checkCallstackCriteria("com", new String[0]);
	}

	public static String checkInstrumentation() throws Exception {
		return InstrumentationSecurityProbe.checkCallstackCriteria("com", new String[0]);
	}
}
