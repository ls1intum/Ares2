package de.tum.cit.ase.ares.api.architecture.java.archunit.subject.typeuse.checked;

import java.util.List;
import java.util.function.IntSupplier;

/**
 * Uses generics, arrays, a lambda and a bridge method, and no foreign type
 * annotation.
 */
public class CleanFixture implements Comparable<CleanFixture> {

	/**
	 * A generic field.
	 */
	List<String[]> items = List.of();

	/**
	 * Compares by hash code, which makes the compiler add a bridge method.
	 *
	 * @param other the other fixture
	 * @return the comparison result
	 */
	@Override
	public int compareTo(CleanFixture other) {
		return Integer.compare(hashCode(), other.hashCode());
	}

	/**
	 * Returns a lambda.
	 *
	 * @return the supplier
	 */
	public IntSupplier supplier() {
		return () -> items.size();
	}
}
