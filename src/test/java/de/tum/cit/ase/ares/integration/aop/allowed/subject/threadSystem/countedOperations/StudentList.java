package de.tum.cit.ase.ares.integration.aop.allowed.subject.threadSystem.countedOperations;

import java.util.ArrayList;

/**
 * A list class the student wrote, used as plain data to be sorted.
 */
public final class StudentList extends ArrayList<Integer> {

	/** The serial version, as for every list. */
	private static final long serialVersionUID = 1L;

	/**
	 * Builds a list of the given number of zeros.
	 *
	 * @param size how many elements it holds
	 */
	public StudentList(int size) {
		for (int i = 0; i < size; i++) {
			add(0);
		}
	}
}
