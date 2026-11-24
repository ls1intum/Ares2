package de.tum.cit.ase.ares.integration.aop.forbidden.subject.staticNarrowing.setOut;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

/**
 * Supervised code that replaces the standard output of the whole JVM.
 */
public final class SetOutMain {

	/**
	 * Hidden constructor of a utility class.
	 */
	private SetOutMain() {
		throw new UnsupportedOperationException("Utility class");
	}

	/**
	 * Replaces {@code System.out} with a stream into memory.
	 */
	public static void replaceOut() {
		System.setOut(new PrintStream(new ByteArrayOutputStream()));
	}
}
