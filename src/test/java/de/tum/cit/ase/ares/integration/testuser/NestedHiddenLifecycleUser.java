package de.tum.cit.ase.ares.integration.testuser;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Nested;

import de.tum.cit.ase.ares.api.Deadline;
import de.tum.cit.ase.ares.api.MirrorOutput;
import de.tum.cit.ase.ares.api.Policy;
import de.tum.cit.ase.ares.api.jupiter.HiddenTest;
import de.tum.cit.ase.ares.api.jupiter.Public;
import de.tum.cit.ase.ares.api.jupiter.PublicTest;

/**
 * A public outer class whose only hidden test sits two {@code @Nested} levels
 * down. The outer class setup and teardown are shared with that hidden test, so
 * their output must stay hidden.
 */
@Public
@Policy(activated = false)
public class NestedHiddenLifecycleUser {

	/** Emits class setup text shared with the nested hidden test. */
	@BeforeAll
	static void beforeAll() {
		System.out.print("SECRET_NESTED_BEFORE_ALL");
	}

	/** Emits class teardown text shared with the nested hidden test. */
	@AfterAll
	static void afterAll() {
		System.err.print("SECRET_NESTED_AFTER_ALL");
	}

	/** Keeps the public method and its output observable. */
	@PublicTest
	@MirrorOutput(MirrorOutput.MirrorOutputPolicy.ENABLED)
	void publicBody() {
		System.out.print("VISIBLE_NESTED_PUBLIC_BODY");
	}

	/** The first nesting level, with no test of its own. */
	@Nested
	class Inner {

		/** The second nesting level, holding the hidden test. */
		@Nested
		class Deeper {

			/** Keeps the hidden method's failure opaque. */
			@HiddenTest
			@Deadline("2000-01-01 00:00")
			void hiddenBody() {
				System.err.print("SECRET_NESTED_BODY");
				throw new AssertionError("SECRET_NESTED_FAILURE");
			}
		}
	}
}
