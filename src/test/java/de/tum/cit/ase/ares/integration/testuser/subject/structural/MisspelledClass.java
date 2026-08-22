package de.tum.cit.ase.ares.integration.testuser.subject.structural;

public class MisspelledClass {
	// empty
}

/**
 * A second, package-private top-level type declared alongside
 * {@link MisspelledClass} in the same file — Java permits more than one
 * top-level type per source file. Used to prove that {@code ClassNameScanner}
 * discovers every top-level type a file declares, not just the one whose name
 * matches the filename, and that {@code checkParameters} accepts a canonical
 * parameter type name ({@code java.lang.String}) exactly as it accepts the
 * simple form.
 */
class AdditionalTopLevelType {

	@SuppressWarnings("unused")
	public AdditionalTopLevelType(String someText) {
		// nothing
	}

	@SuppressWarnings("unused")
	public void acceptCanonicalParameter(String someText) {
		// nothing
	}
}
