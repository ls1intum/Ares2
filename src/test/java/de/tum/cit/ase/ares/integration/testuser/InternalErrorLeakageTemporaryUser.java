package de.tum.cit.ase.ares.integration.testuser;

import org.junit.jupiter.api.MethodOrderer.MethodName;
import org.junit.jupiter.api.TestMethodOrder;

import de.tum.cit.ase.ares.api.Deadline;
import de.tum.cit.ase.ares.api.MirrorOutput;
import de.tum.cit.ase.ares.api.Policy;
import de.tum.cit.ase.ares.api.StrictTimeout;
import de.tum.cit.ase.ares.api.jupiter.HiddenTest;
import de.tum.cit.ase.ares.api.jupiter.PublicTest;
import de.tum.cit.ase.ares.api.localization.UseLocale;

/**
 * TEMPORARY end-to-end "student" test cases for the student-feedback leakage
 * fixes of PR #96 (hidden-test leakage) and PR #98 (Ares-internal error
 * leakage). The driver
 * {@link de.tum.cit.ase.ares.integration.InternalErrorLeakageTemporaryTest}
 * runs them and asserts what a student would actually see.
 * <p>
 * The class runs under a real, activated {@code @Policy} (Maven + ArchUnit +
 * AspectJ) so the demonstration covers the full enforced configuration: the
 * security policy is read, the architecture analysis runs, and the
 * {@code JupiterTestGuard} reporting path post-processes the thrown exceptions
 * exactly as it would for a real submission. Both fixes live in
 * {@code ReportingUtils.processThrowable} (the exception reporting layer).
 * <p>
 * Marked temporary: delete or fold into permanent coverage once the two PRs
 * land.
 */
@UseLocale("en")
@MirrorOutput(MirrorOutput.MirrorOutputPolicy.DISABLED)
@StrictTimeout(5)
@Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/aspectj/PolicyInternalErrorLeakage.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/testuser/subject/helloWorld")
@TestMethodOrder(MethodName.class)
@SuppressWarnings("static-method")
public class InternalErrorLeakageTemporaryUser {

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
	 * PR #96: a hidden test that leaks a secret through its failure. The past
	 * deadline lets the body run so the thrown exception passes through the hidden
	 * suppression in {@code ReportingUtils.processThrowable}.
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
	 * Hidden test that hits an Ares-internal error: the student must still see only
	 * the hidden message.
	 */
	@HiddenTest
	@Deadline("2000-01-01 00:00")
	void hiddenTestWithInternalAresError() {
		throw new SecurityException(INTERNAL_ARES_ERROR_MESSAGE);
	}
}
