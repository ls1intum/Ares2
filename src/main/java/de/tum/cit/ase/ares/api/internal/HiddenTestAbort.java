package de.tum.cit.ase.ares.api.internal;

import org.opentest4j.TestAbortedException;

/** Marks an aborted hidden test without a student-visible reason. */
final class HiddenTestAbort extends TestAbortedException {

	/** Serialization identifier for the abort marker. */
	private static final long serialVersionUID = 1L;

	/** Creates an aborted result with no message, cause, or stack. */
	HiddenTestAbort() {
		setStackTrace(new StackTraceElement[0]);
	}

	/** Leaves report renderers no text to place in an abort block. */
	@Override
	public String toString() {
		return ""; //$NON-NLS-1$
	}
}
