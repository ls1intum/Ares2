package de.tum.cit.ase.ares.api.architecture.java.packageimport.fixtures;

/**
 * Fixture depending on {@code javax.swing} and on a third-party library, which
 * stay forbidden even when every {@code java.*} package is permitted.
 */
public class UsesJavaxAndThirdParty {

	/**
	 * Creates a Swing label.
	 *
	 * @return the label
	 */
	public Object label() {
		return new javax.swing.JLabel("label");
	}

	/**
	 * Creates a Jackson object mapper.
	 *
	 * @return the mapper
	 */
	public Object mapper() {
		return new com.fasterxml.jackson.databind.ObjectMapper();
	}
}
