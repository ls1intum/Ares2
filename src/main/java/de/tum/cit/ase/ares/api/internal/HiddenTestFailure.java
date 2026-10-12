package de.tum.cit.ase.ares.api.internal;

/**
 * Marks a failed hidden test without a student-visible exception description.
 */
final class HiddenTestFailure extends AssertionError {

	/** Serialization identifier for the failure marker. */
	private static final long serialVersionUID = 1L;

	/** Creates a failed result with no message, cause, or stack. */
	HiddenTestFailure() {
		setStackTrace(new StackTraceElement[0]);
	}

	/** Leaves report renderers no text to place in a failure block. */
	@Override
	public String toString() {
		return ""; //$NON-NLS-1$
	}
}
