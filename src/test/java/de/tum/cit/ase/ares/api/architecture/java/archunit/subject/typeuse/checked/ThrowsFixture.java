package de.tum.cit.ase.ares.api.architecture.java.archunit.subject.typeuse.checked;

import org.example.subject.typeuse.foreign.ForeignTypeUse;

/**
 * Carries the foreign annotation on a thrown exception type.
 */
public class ThrowsFixture {

	/**
	 * Declares an annotated thrown exception.
	 *
	 * @throws Exception never
	 */
	public void fail() throws @ForeignTypeUse Exception {
	}
}
