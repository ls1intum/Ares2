package de.tum.cit.ase.ares.api.architecture.java.archunit.subject.typeuse.checked;

import org.example.subject.typeuse.foreign.ForeignTypeUse;

/**
 * Carries the foreign annotation on a caught exception type.
 */
public class CatchParameterFixture {

	/**
	 * Catches an annotated exception type.
	 *
	 * @param text a number as text
	 * @return the number, or zero
	 */
	public int parse(String text) {
		try {
			return Integer.parseInt(text);
		} catch (@ForeignTypeUse NumberFormatException exception) {
			return 0;
		}
	}
}
