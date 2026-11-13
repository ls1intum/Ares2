package de.tum.cit.ase.ares.integration.testuser;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

import org.junit.jupiter.api.MethodOrderer.MethodName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.api.TestMethodOrder;

import de.tum.cit.ase.ares.api.Policy;
import de.tum.cit.ase.ares.api.jupiter.HiddenTest;
import de.tum.cit.ase.ares.api.jupiter.Public;
import de.tum.cit.ase.ares.api.jupiter.PublicTest;
import de.tum.cit.ase.ares.api.localization.UseLocale;

/**
 * Tests that overwrite their own policy file while the class runs, moving its
 * deadline into the past. Ares reads the test behaviour once, before the first
 * test, so the later tests keep the deadline it held then. Three variants cover
 * the ways a class can be supervised.
 */
@UseLocale("en")
@SuppressWarnings("static-method")
public class PolicyBehaviorCacheUser {

	/** The policy each variant starts from: a deadline in the future. */
	private static final Path ORIGINAL = Path.of(
			"src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/aspectj/PolicyTestBehaviorCacheFutureUser.yaml");

	/** A policy whose deadline has passed, for a method's own policy. */
	private static final String PAST = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/aspectj/PolicyTestBehaviorCachePastUser.yaml";

	/** The copy the variant with a public class reads. */
	private static final String PUBLIC_CLASS = "target/test-behavior-cache/PolicyPublicClass.yaml";

	/** The copy the variant with public methods reads. */
	private static final String PUBLIC_METHODS = "target/test-behavior-cache/PolicyPublicMethods.yaml";

	/** The copy the variant with one instance per class reads. */
	private static final String PER_CLASS = "target/test-behavior-cache/PolicyPerClass.yaml";

	/** The benign subject the policies analyse. */
	private static final String SUBJECT = "test-classes/de/tum/cit/ase/ares/integration/testuser/subject/helloWorld";

	/**
	 * The text of the policy with a passed deadline, read before Ares supervises
	 * anything.
	 */
	private static final String PAST_TEXT = readPast();

	static {
		copyOriginalTo(PUBLIC_CLASS);
		copyOriginalTo(PUBLIC_METHODS);
		copyOriginalTo(PER_CLASS);
	}

	/**
	 * Reads the policy with a passed deadline.
	 *
	 * @return its text.
	 */
	private static String readPast() {
		try {
			return Files.readString(Path.of(PAST), StandardCharsets.UTF_8);
		} catch (IOException unreadable) {
			throw new UncheckedIOException(unreadable);
		}
	}

	/**
	 * Puts a fresh copy of the original policy where a variant reads it.
	 *
	 * @param target the variant's copy.
	 */
	private static void copyOriginalTo(String target) {
		try {
			Files.createDirectories(Path.of(target).getParent());
			Files.copy(ORIGINAL, Path.of(target), StandardCopyOption.REPLACE_EXISTING);
		} catch (IOException unwritable) {
			throw new UncheckedIOException(unwritable);
		}
	}

	/**
	 * Overwrites a variant's policy file with one whose deadline has passed.
	 *
	 * @param target the variant's copy.
	 */
	static void moveTheDeadline(String target) {
		try {
			Files.writeString(Path.of(target), PAST_TEXT, StandardCharsets.UTF_8);
		} catch (IOException unwritable) {
			throw new UncheckedIOException(unwritable);
		}
	}

	/** A class marked public, with plain JUnit tests. */
	@Nested
	@Public
	@TestMethodOrder(MethodName.class)
	@Policy(value = PUBLIC_CLASS, withinPath = SUBJECT)
	class PublicClass {

		/** Moves the policy's deadline into the past after Ares has read it. */
		@Test
		void a_overwritesThePolicy() {
			moveTheDeadline(PUBLIC_CLASS);
		}

		/** Still held back by the deadline read before the overwrite. */
		@HiddenTest
		void b_hiddenTestKeepsTheDeadline() {
			moveTheDeadline(PUBLIC_CLASS);
		}

		/** Still runs under the policy read before the overwrite. */
		@Test
		void c_publicTestStillRuns() {
			moveTheDeadline(PUBLIC_CLASS);
		}
	}

	/** A class whose methods are each marked public or hidden. */
	@Nested
	@TestMethodOrder(MethodName.class)
	@Policy(value = PUBLIC_METHODS, withinPath = SUBJECT)
	class PublicMethods {

		/** Moves the policy's deadline into the past after Ares has read it. */
		@PublicTest
		void a_overwritesThePolicy() {
			moveTheDeadline(PUBLIC_METHODS);
		}

		/** Still held back by the deadline read before the overwrite. */
		@HiddenTest
		void b_hiddenTestKeepsTheDeadline() {
			moveTheDeadline(PUBLIC_METHODS);
		}

		/** Still runs under the policy read before the overwrite. */
		@PublicTest
		void c_publicTestStillRuns() {
			moveTheDeadline(PUBLIC_METHODS);
		}

		/** A method's own policy, with a passed deadline, gives it its own schedule. */
		@HiddenTest
		@Policy(value = PAST, withinPath = SUBJECT)
		void d_methodPolicyHasItsOwnDeadline() {
			moveTheDeadline(PUBLIC_METHODS);
		}
	}

	/** A class sharing one instance across its tests. */
	@Nested
	@TestInstance(Lifecycle.PER_CLASS)
	@TestMethodOrder(MethodName.class)
	@Policy(value = PER_CLASS, withinPath = SUBJECT)
	class PerClass {

		/** Moves the policy's deadline into the past after Ares has read it. */
		@PublicTest
		void a_overwritesThePolicy() {
			moveTheDeadline(PER_CLASS);
		}

		/** Still held back by the deadline read before the overwrite. */
		@HiddenTest
		void b_hiddenTestKeepsTheDeadline() {
			moveTheDeadline(PER_CLASS);
		}

		/** Still runs under the policy read before the overwrite. */
		@PublicTest
		void c_publicTestStillRuns() {
			moveTheDeadline(PER_CLASS);
		}
	}
}
