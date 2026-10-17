package de.tum.cit.ase.ares.api.architecture.java.archunit.subject.typeuse.checked;

import org.example.subject.typeuse.foreign.ForeignTypeUse;

/**
 * Carries the foreign annotation on a receiver parameter.
 */
public class ReceiverFixture {

	/**
	 * Declares an annotated receiver.
	 *
	 * @return the hash code
	 */
	public int self(@ForeignTypeUse ReceiverFixture this) {
		return hashCode();
	}
}
