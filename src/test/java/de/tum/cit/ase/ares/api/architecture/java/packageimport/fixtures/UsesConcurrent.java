package de.tum.cit.ase.ares.api.architecture.java.packageimport.fixtures;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Fixture depending on a subpackage of {@code java.util}, used to verify that a
 * policy grant keeps its subpackage-inclusive meaning while an exact grant does
 * not.
 */
public class UsesConcurrent {

	private final AtomicInteger counter = new AtomicInteger();

	public int next() {
		return counter.incrementAndGet();
	}
}
