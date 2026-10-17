package de.tum.cit.ase.ares.api.architecture.java.archunit.subject.typeuse.checked;

import java.util.function.IntSupplier;

import org.example.subject.typeuse.foreign.ForeignTypeUse;

/**
 * Carries the foreign annotation inside a lambda body.
 */
public class LambdaFixture {

	/**
	 * Returns a lambda with an annotated local variable.
	 *
	 * @return the supplier
	 */
	public IntSupplier supplier() {
		return () -> {
			@ForeignTypeUse
			String value = "lambda";
			return value.length();
		};
	}
}
