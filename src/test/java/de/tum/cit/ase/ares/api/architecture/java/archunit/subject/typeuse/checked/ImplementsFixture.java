package de.tum.cit.ase.ares.api.architecture.java.archunit.subject.typeuse.checked;

import org.example.subject.typeuse.foreign.ForeignTypeUse;

/**
 * Carries the foreign annotation on an implemented interface.
 */
public class ImplementsFixture implements @ForeignTypeUse Runnable {

	/**
	 * Does nothing.
	 */
	@Override
	public void run() {
	}
}
