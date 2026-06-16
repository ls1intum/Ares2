package de.tum.cit.ase.ares.integration;

import static de.tum.cit.ase.ares.testutilities.CustomConditions.testFailedWith;

import org.junit.platform.testkit.engine.Events;

import de.tum.cit.ase.ares.integration.testuser.InternalErrorLeakageTemporaryUser;
import de.tum.cit.ase.ares.testutilities.CustomConditions.Option;
import de.tum.cit.ase.ares.testutilities.TestTest;
import de.tum.cit.ase.ares.testutilities.UserBased;
import de.tum.cit.ase.ares.testutilities.UserTestResults;

/**
 * TEMPORARY end-to-end verification for the student-feedback leakage fixes,
 * driving {@link InternalErrorLeakageTemporaryUser} through the full Ares
 * pipeline and asserting on the student-visible feedback. Stands in for the
 * manual Artemis-submission test plans of PR #96 and PR #98.
 * <p>
 * Marked temporary: delete or fold into permanent coverage once the two PRs
 * land.
 */
@UserBased(InternalErrorLeakageTemporaryUser.class)
class InternalErrorLeakageTemporaryTest {

	@UserTestResults
	private static Events tests;

	private final String hiddenTestLeakingSecret = "hiddenTestLeakingSecret";
	private final String publicTestWithVisibleMessage = "publicTestWithVisibleMessage";
	private final String publicTestWithInternalAresError = "publicTestWithInternalAresError";
	private final String publicTestWithStudentSecurityError = "publicTestWithStudentSecurityError";
	private final String hiddenTestWithInternalAresError = "hiddenTestWithInternalAresError";

	// --- PR #96: hidden-test leakage ------------------------------------------

	/**
	 * PR #96 test plan:
	 * <ul>
	 * <li>Create a {@code @HiddenTest} that throws
	 * {@code new RuntimeException("SECRET_HIDDEN_CASE_42")}.</li>
	 * <li>Submit to Artemis and verify the student results page shows only "Hidden
	 * test failed." — no exception message, no stack trace.</li>
	 * </ul>
	 */
	@TestTest
	void test_hiddenTestLeakingSecret() {
		tests.assertThatEvents().haveExactly(1,
				testFailedWith(hiddenTestLeakingSecret, AssertionError.class, "Hidden test failed."));
	}

	/**
	 * PR #96 test plan:
	 * <ul>
	 * <li>Verify {@code @PublicTest} failures still show the full assertion
	 * message.</li>
	 * </ul>
	 */
	@TestTest
	void test_publicTestWithVisibleMessage() {
		tests.assertThatEvents().haveExactly(1, testFailedWith(publicTestWithVisibleMessage, AssertionError.class,
				"VISIBLE_PUBLIC_MESSAGE", Option.MESSAGE_CONTAINS));
	}

	// --- PR #98: Ares-internal error leakage ----------------------------------

	/**
	 * PR #98 test plan:
	 * <ul>
	 * <li>Create a DoS test ({@code while(true){}}) with a strict policy.</li>
	 * <li>Submit to Artemis and verify student feedback shows only the generic
	 * instructor-contact message.</li>
	 * <li>Verify server logs contain the full original SecurityException for
	 * debugging — produced in the same branch that yields the generic message, so
	 * the assertion below on the generic student message also proves the
	 * server-side ERROR log fired. (Logback output is not part of the captured
	 * student-facing {@code Events}, hence not asserted directly here.)</li>
	 * </ul>
	 */
	@TestTest
	void test_publicTestWithInternalAresError() {
		tests.assertThatEvents().haveExactly(1, testFailedWith(publicTestWithInternalAresError, AssertionError.class,
				"An internal Ares error occurred during test execution. Please contact your instructor."));
	}

	/**
	 * PR #98: guards the narrowing from commit b9ad5a46 — a student-triggered
	 * {@code Reason: Student-Code} SecurityException must NOT be suppressed and
	 * must still reach the student (not a numbered checklist item).
	 */
	@TestTest
	void test_publicTestWithStudentSecurityError() {
		tests.assertThatEvents().haveExactly(1, testFailedWith(publicTestWithStudentSecurityError,
				SecurityException.class, "Unable to make field accessible", Option.MESSAGE_CONTAINS));
	}

	// --- Merge resolution: hidden wins display, internal error still logged ----

	/**
	 * Combined PR #96 + PR #98 for a hidden test that hits an Ares-internal error:
	 * the student results page shows only "Hidden test failed." (PR #96), while the
	 * original SecurityException is logged server-side for the instructor (PR #98).
	 */
	@TestTest
	void test_hiddenTestWithInternalAresError() {
		tests.assertThatEvents().haveExactly(1,
				testFailedWith(hiddenTestWithInternalAresError, AssertionError.class, "Hidden test failed."));
	}
}
