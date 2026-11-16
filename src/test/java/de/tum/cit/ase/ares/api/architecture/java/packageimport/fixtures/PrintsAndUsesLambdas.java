package de.tum.cit.ase.ares.api.architecture.java.packageimport.fixtures;

/**
 * Fixture writing ordinary code a student cannot avoid: printing, a lambda, a
 * record, an enum, a switch on a string and string concatenation. ArchUnit
 * reports its JDK dependencies, and those of its nested classes, only in
 * {@code java.lang} and {@code java.io}.
 */
public class PrintsAndUsesLambdas {

	/** A record, compiled with generated accessor and object methods. */
	record Point(int x, int y) {
	}

	/** An enum, compiled with generated values and valueOf methods. */
	enum Colour {
		RED,
		GREEN
	}

	/**
	 * Prints through a lambda.
	 *
	 * @param word the word to print
	 * @return a number derived from the word
	 */
	public int print(String word) {
		Runnable printer = () -> System.out.println("Word: " + word + new Point(1, 2));
		printer.run();
		return switch (word) {
		case "a" -> Colour.RED.ordinal();
		default -> Colour.valueOf("GREEN").ordinal();
		};
	}
}
