package de.tum.cit.ase.ares.api.aop.java.aspectj.adviceandpointcut;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Stream;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import de.tum.cit.ase.ares.api.aop.java.JavaAOPTestCaseSettings;

/**
 * The AspectJ advice tells which threads go unchecked once a test has ended,
 * gives the reason for a refusal then, and stops its walk for Ares' own utility
 * reads at the first student frame.
 */
class JavaAspectJSupervisionEndTest {

	/** The package standing in for student code. */
	private static final String STUDENT_PACKAGE = "org.example";

	/** Leaves nothing armed for the next test class. */
	@AfterEach
	void resetSettings() {
		JavaAOPTestCaseSettings.reset();
	}

	/**
	 * Tells on a new thread whether that thread goes unchecked.
	 *
	 * @return {@code true} if the new thread is unchecked
	 * @throws InterruptedException if waiting for the thread is interrupted
	 */
	private static boolean isUncheckedOnAnotherThread() throws InterruptedException {
		AtomicBoolean unchecked = new AtomicBoolean();
		Thread other = new Thread(
				() -> unchecked.set(JavaAspectJAbstractAdviceDefinitions.isUncheckedOutsideASupervisedTest()));
		other.start();
		other.join();
		return unchecked.get();
	}

	/**
	 * After a test, only the thread that ended it goes unchecked, and a refusal
	 * names the ended test as its reason.
	 */
	@Test
	void onlyTheEndingThreadGoesUncheckedAfterTheTest() throws InterruptedException {
		JavaAOPTestCaseSettings.reset();
		assertFalse(JavaAspectJAbstractAdviceDefinitions.isUncheckedOutsideASupervisedTest());

		JavaAOPTestCaseSettings.endSupervision();

		assertTrue(JavaAspectJAbstractAdviceDefinitions.isUncheckedOutsideASupervisedTest());
		assertFalse(isUncheckedOnAnotherThread());
		assertEquals(JavaAspectJAbstractAdviceDefinitions.localize("security.advice.denial.reason.test.ended"),
				JavaAspectJAbstractAdviceDefinitions.buildDenialReason("file", "read", true));
	}

	/** An unguarded phase leaves every thread unchecked. */
	@Test
	void suspendedSupervisionLeavesEveryThreadUnchecked() throws InterruptedException {
		JavaAOPTestCaseSettings.endSupervision();
		JavaAOPTestCaseSettings.suspendSupervision();

		assertTrue(isUncheckedOnAnotherThread());
	}

	/**
	 * A utility read counts as Ares' own only if Ares called the utility and no
	 * student frame comes before it; the walk ends at that student frame.
	 */
	@Test
	void utilityReadCountsOnlyWithoutStudentCodeBeforeIt() {
		assertTrue(JavaAspectJAbstractAdviceDefinitions.isTrustedUtilityReadForAres(
				List.of("de.tum.cit.ase.ares.api.util.ProjectSourcesFinder", "de.tum.cit.ase.ares.api.ast.Probe")
						.iterator(),
				STUDENT_PACKAGE));
		assertFalse(JavaAspectJAbstractAdviceDefinitions
				.isTrustedUtilityReadForAres(Stream.iterate("org.example.Hog", name -> {
					throw new IllegalStateException("read past the first student frame");
				}).iterator(), STUDENT_PACKAGE));
	}
}
