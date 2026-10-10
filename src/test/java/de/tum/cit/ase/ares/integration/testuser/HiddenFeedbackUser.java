package de.tum.cit.ase.ares.integration.testuser;

import org.junit.jupiter.api.MethodOrderer.MethodName;
import org.junit.jupiter.api.TestMethodOrder;
import org.opentest4j.TestAbortedException;

import de.tum.cit.ase.ares.api.Deadline;
import de.tum.cit.ase.ares.api.MirrorOutput;
import de.tum.cit.ase.ares.api.Policy;
import de.tum.cit.ase.ares.api.StrictTimeout;
import de.tum.cit.ase.ares.api.jupiter.HiddenTest;
import de.tum.cit.ase.ares.api.jupiter.PublicTest;
import de.tum.cit.ase.ares.api.localization.UseLocale;

/**
 * Supervised fixtures for the permanent hidden-feedback contract test. Public
 * failures keep useful messages; hidden failures keep status without details.
 */
@UseLocale("en")
@MirrorOutput(MirrorOutput.MirrorOutputPolicy.DISABLED)
@StrictTimeout(5)
@Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/aspectj/PolicyInternalErrorLeakage.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/testuser/subject/helloWorld")
@TestMethodOrder(MethodName.class)
@SuppressWarnings("static-method")
public class HiddenFeedbackUser {

	/**
	 * The exact message an Ares-internal setup failure carries, mirroring the
	 * {@code security.advice.class.not.found.exception} template that fires when
	 * the framework cannot load its {@code JavaAOPTestCaseSettings} class during
	 * (e.g.) DoS test execution. We throw it directly because the real class-load
	 * failure is environmental and cannot be forced in-process where the class is
	 * present.
	 */
	private static final String INTERNAL_ARES_ERROR_MESSAGE = "Ares Security Error (Reason: Ares-Code; Stage: Execution): Could not find 'JavaAOPTestCaseSettings' class to access field 'x'";

	/**
	 * A student-triggered Ares-Code SecurityException that tests legitimately
	 * assert on.
	 */
	private static final String STUDENT_SECURITY_ERROR_MESSAGE = "Ares Security Error (Reason: Student-Code; Stage: Execution): Unable to make field accessible";

	// --- PR #96: hidden-test leakage ------------------------------------------

	/**
	 * A hidden failure after its deadline, carrying a distinctive secret.
	 */
	@HiddenTest
	@Deadline("2000-01-01 00:00")
	void hiddenTestLeakingSecret() {
		throw new RuntimeException("SECRET_HIDDEN_CASE_42");
	}

	/** PR #96: a public test failure whose full assertion message must survive. */
	@PublicTest
	void publicTestWithVisibleMessage() {
		throw new AssertionError("VISIBLE_PUBLIC_MESSAGE");
	}

	// --- PR #98: Ares-internal error leakage ----------------------------------

	/**
	 * PR #98: an Ares-internal setup failure surfacing in a public (DoS-style)
	 * test.
	 */
	@PublicTest
	void publicTestWithInternalAresError() {
		throw new SecurityException(INTERNAL_ARES_ERROR_MESSAGE);
	}

	/**
	 * PR #98: a student-triggered Ares-Code SecurityException that must NOT be
	 * suppressed.
	 */
	@PublicTest
	void publicTestWithStudentSecurityError() {
		throw new SecurityException(STUDENT_SECURITY_ERROR_MESSAGE);
	}

	// --- Merge resolution: hidden wins display, internal error still logged ----

	/**
	 * Hidden test with a framework-shaped error that must emit no details.
	 */
	@HiddenTest
	@Deadline("2000-01-01 00:00")
	void hiddenTestWithInternalAresError() {
		throw new SecurityException(INTERNAL_ARES_ERROR_MESSAGE);
	}

	/** A hidden abort keeps its status without its private reason. */
	@HiddenTest
	@Deadline("2000-01-01 00:00")
	void hiddenAbortWithSecret() {
		throw new TestAbortedException("SECRET_HIDDEN_ABORT");
	}

	/**
	 * A hidden timeout keeps failed status without its duration or worker stack.
	 */
	@HiddenTest
	@Deadline("2000-01-01 00:00")
	@StrictTimeout(1)
	void hiddenTimeoutWithSecret() throws InterruptedException {
		Thread.sleep(3000);
	}

	/** A future deadline prevents this hidden method from executing. */
	@HiddenTest
	@Deadline("2999-01-01 00:00")
	void futureHiddenMethod() {
		throw new AssertionError("SECRET_FUTURE_BODY_RAN");
	}
}
