package de.tum.cit.ase.ares.api.architecture.java.archunit.subject.typeuse.checked;

import java.util.Collections;
import java.util.List;

import org.example.subject.typeuse.foreign.ForeignTypeUse;

/**
 * Carries the foreign annotation on an explicit type argument of a call.
 */
public class CallTypeArgumentFixture {

	/**
	 * Calls with an annotated type argument.
	 *
	 * @return an empty list
	 */
	public List<String> none() {
		return Collections.<@ForeignTypeUse String>emptyList();
	}
}
