package com.foobar;

import example.student.AspectJSecurityProbe;
import example.student.InstrumentationSecurityProbe;

/**
 * Fixture for I-105/TD-051's package-boundary case: a class in an unrelated
 * sibling package ({@code com.foobar}) that merely shares a string prefix with
 * the allow-listed package ({@code com.foo}). Before the fix, an unbounded
 * {@code startsWith} comparison wrongly treated this as allowed too; both
 * {@link #checkAspectJ} and {@link #checkInstrumentation} must now report a
 * violation.
 */
public final class UnrelatedPackageMember {

	private UnrelatedPackageMember() {
	}

	public static String checkAspectJ() throws Exception {
		return AspectJSecurityProbe.checkCallstackCriteria("com", new String[0]);
	}

	public static String checkInstrumentation() throws Exception {
		return InstrumentationSecurityProbe.checkCallstackCriteria("com", new String[0]);
	}
}
