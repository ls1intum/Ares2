package de.tum.cit.ase.ares.testutilities;

/**
 * Holds an exception named like Ares's blocked-action exception but declared
 * outside Ares's own package, as a student could write one.
 */
public final class LookalikeViolation {

	/** Not to be created. */
	private LookalikeViolation() {
	}

	/** A student exception that only borrows the name. */
	public static final class AresSecurityViolation extends SecurityException {

		/** Serialisation version. */
		private static final long serialVersionUID = 1L;

		/**
		 * Creates one.
		 *
		 * @param message the message.
		 */
		public AresSecurityViolation(String message) {
			super(message);
		}
	}
}
