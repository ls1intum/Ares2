package de.tum.cit.ase.ares.integration.testuser;

import org.junit.jupiter.api.MethodOrderer.MethodName;
import org.junit.jupiter.api.TestMethodOrder;

import de.tum.cit.ase.ares.api.Deadline;
import de.tum.cit.ase.ares.api.ExtendedDeadline;
import de.tum.cit.ase.ares.api.Policy;
import de.tum.cit.ase.ares.api.jupiter.HiddenTest;
import de.tum.cit.ase.ares.api.jupiter.PublicTest;
import de.tum.cit.ase.ares.api.localization.UseLocale;

/**
 * Tests whose hidden-test schedule comes from the policy's
 * {@code regardingTheVisibilityAndScheduleOfTheTestCases}, each under its own
 * policy, with and without the annotations that still win locally.
 */
@UseLocale("en")
@TestMethodOrder(MethodName.class)
@SuppressWarnings("static-method")
public class HiddenTestsPolicyUser {

	/** Where the policies of this class live. */
	private static final String POLICIES = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/aspectj/";

	/**
	 * Where the deliberately unreadable policies live, outside the folder every
	 * checked-in policy is read from.
	 */
	private static final String INVALID_POLICIES = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/invalidsecuritypolicies/";

	/** The benign subject the policies analyse. */
	private static final String SUBJECT = "test-classes/de/tum/cit/ase/ares/integration/testuser/subject/helloWorld";

	/**
	 * A hidden test without its own deadline, after the policy's deadline. Runs,
	 * because the policy's deadline has passed.
	 */
	@HiddenTest
	@Policy(value = POLICIES + "PolicyHiddenTestsPastUser.yaml", withinPath = SUBJECT)
	void policyPastDeadlineRuns() {
	}

	/**
	 * A hidden test without its own deadline, before the policy's deadline. Never
	 * runs before the policy's deadline.
	 */
	@HiddenTest
	@Policy(value = POLICIES + "PolicyHiddenTestsFutureUser.yaml", withinPath = SUBJECT)
	void policyFutureDeadlineBlocks() {
	}

	/**
	 * A passed policy deadline, extended by an annotation into the future. Never
	 * runs before the extended deadline.
	 */
	@HiddenTest
	@ExtendedDeadline("365000d")
	@Policy(value = POLICIES + "PolicyHiddenTestsPastUser.yaml", withinPath = SUBJECT)
	void annotationExtensionAddsToThePolicy() {
	}

	/**
	 * A passed method deadline wins over a future policy deadline. Runs, because
	 * the method's own deadline has passed.
	 */
	@HiddenTest
	@Deadline("2000-01-01 00:00 UTC")
	@Policy(value = POLICIES + "PolicyHiddenTestsFutureUser.yaml", withinPath = SUBJECT)
	void methodDeadlineWinsOverThePolicy() {
	}

	/**
	 * A future deadline, but before the policy's always-run-before date. Runs,
	 * because the always-run-before date lies ahead.
	 */
	@HiddenTest
	@Policy(value = POLICIES + "PolicyHiddenTestsActiveUser.yaml", withinPath = SUBJECT)
	void policyAlwaysRunBeforeRuns() {
	}

	/**
	 * A public test is not held back by the policy's deadline. Runs: public tests
	 * have no deadline.
	 */
	@PublicTest
	@Policy(value = POLICIES + "PolicyHiddenTestsFutureUser.yaml", withinPath = SUBJECT)
	void publicTestIgnoresThePolicyDeadline() {
	}

	/**
	 * A public test in a class the policy lists stays public. Runs: the annotation
	 * wins over the policy's list.
	 */
	@PublicTest
	@Policy(value = POLICIES + "PolicyHiddenTestsListedUser.yaml", withinPath = SUBJECT)
	void annotationWinsOverTheList() {
	}

	/**
	 * A public test under a policy whose list names nothing that exists. Never
	 * runs: the policy fails every test.
	 */
	@PublicTest
	@Policy(value = POLICIES + "PolicyHiddenTestsUnmatchedUser.yaml", withinPath = SUBJECT)
	void unmatchedEntryFailsAPublicTest() {
	}

	/**
	 * A public test under a policy that hides unlisted tests stays public. Runs:
	 * the annotation wins over the policy's setting for unlisted tests.
	 */
	@PublicTest
	@Policy(value = POLICIES + "PolicyHiddenTestsUnlistedHiddenUser.yaml", withinPath = SUBJECT)
	void annotationWinsWhenUnlistedTestsAreHidden() {
	}

	/**
	 * A public test under a policy whose public list names nothing that exists.
	 * Never runs: the policy fails every test.
	 */
	@PublicTest
	@Policy(value = POLICIES + "PolicyHiddenTestsUnmatchedPublicUser.yaml", withinPath = SUBJECT)
	void unmatchedPublicEntryFailsAPublicTest() {
	}

	/**
	 * A public test under a policy that does not say whether unlisted tests are
	 * hidden. Never runs: the policy cannot be read.
	 */
	@PublicTest
	@Policy(value = INVALID_POLICIES + "PolicyHiddenTestsWithoutSwitchUser.yaml", withinPath = SUBJECT)
	void aPolicyWithoutTheUnlistedSwitchFailsATest() {
	}
}
