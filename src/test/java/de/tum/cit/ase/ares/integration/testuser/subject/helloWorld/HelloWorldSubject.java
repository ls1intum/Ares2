package de.tum.cit.ase.ares.integration.testuser.subject.helloWorld;

/**
 * A deliberately benign subject class used by HelloWorldUser tests to verify
 * that Ares can run architecture analysis without detecting any security
 * violations when the subject code is clean.
 */
public class HelloWorldSubject {

	public String greet() {
		return "Hello, World!";
	}
}
