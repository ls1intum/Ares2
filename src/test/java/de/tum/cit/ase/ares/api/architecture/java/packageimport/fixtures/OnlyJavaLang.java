package de.tum.cit.ase.ares.api.architecture.java.packageimport.fixtures;

/**
 * Fixture whose only recorded dependency is {@code java.lang}, exercising the
 * constructs a student cannot avoid: string concatenation, pattern matching,
 * arrays and a thrown exception.
 */
public class OnlyJavaLang {

	private int counter;

	private String name = "anon";

	public String greet(String who) {
		return "Hello, " + who + "! #" + counter;
	}

	public int sum(int[] values) {
		int total = 0;
		for (int value : values) {
			total += value;
		}
		return total;
	}

	public boolean check(Object other) {
		if (other instanceof OnlyJavaLang sameType) {
			return sameType.counter == counter;
		}
		return false;
	}

	public void mayThrow() {
		if (counter < 0) {
			throw new IllegalStateException(name);
		}
	}
}
