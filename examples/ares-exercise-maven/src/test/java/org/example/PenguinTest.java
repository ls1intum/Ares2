package org.example;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.tum.cit.ase.ares.api.Policy;
import de.tum.cit.ase.ares.api.jupiter.PublicTest;

/**
 * The trusted test class, named in {@code theFollowingClassesAreTestClasses} of
 * the policy, and therefore exempt from enforcement itself.
 * <p>
 * A correct run of this class is <b>green</b> and contains an asserted
 * rejection. A failed build is not the expected outcome.
 * </p>
 */
@Policy(value = "src/test/resources/SecurityPolicy.yaml", withinPath = "classes/org/example")
public class PenguinTest {

	@PublicTest
	void name() {
		assertEquals("Julian", new Penguin("Julian").getName());
	}

	/** Positive control: the one file the policy permits must stay readable. */
	@PublicTest
	void readsThePermittedFile() throws Exception {
		assertEquals("penguins are allowed", new Penguin("Julian").readPermittedFile());
	}

	/**
	 * Negative control: everything else in the same domain must be rejected. If
	 * this assertion ever stops holding, enforcement is not active, and the
	 * positive control above is what proves the policy is not simply denying
	 * everything.
	 */
	@PublicTest
	void rejectsTheForbiddenFile() {
		SecurityException violation = assertThrows(SecurityException.class,
				() -> new Penguin("Julian").readForbiddenFile());
		// Assert on the file name, not on the prose. Ares localises its violation
		// messages, so "blocked by Ares" appears as "von Ares blockiert" on a German
		// JVM and an assertion on the English wording fails on a correctly blocked
		// read. The path and the offending method name are locale-stable.
		assertTrue(violation.getMessage().contains("secret.txt"),
				"expected an Ares file-system violation naming secret.txt, was: " + violation.getMessage());
	}
}
