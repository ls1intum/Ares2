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
 * {@code regardingHiddenTests}, each under its own policy, with and without the
 * annotations that still win locally.
 */
@UseLocale("en")
@TestMethodOrder(MethodName.class)
@SuppressWarnings("static-method")
public class HiddenTestsPolicyUser {

	/** Where the policies of this class live. */
	private static final String POLICIES = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/aspectj/";

	/** The benign subject the policies analyse. */
	private static final String SUBJECT = "test-classes/de/tum/cit/ase/ares/integration/testuser/subject/helloWorld";

	/** A hidden test without its own deadline, after the policy's deadline. */
	@HiddenTest
	@Policy(value = POLICIES + "PolicyHiddenTestsPastUser.yaml", withinPath = SUBJECT)
	void policyPastDeadlineRuns() {
		// Runs, because the policy's deadline has passed.
	}

	/** A hidden test without its own deadline, before the policy's deadline. */
	@HiddenTest
	@Policy(value = POLICIES + "PolicyHiddenTestsFutureUser.yaml", withinPath = SUBJECT)
	void policyFutureDeadlineBlocks() {
		// Never runs before the policy's deadline.
	}

	/** A passed policy deadline, extended by an annotation into the future. */
	@HiddenTest
	@ExtendedDeadline("365000d")
	@Policy(value = POLICIES + "PolicyHiddenTestsPastUser.yaml", withinPath = SUBJECT)
	void annotationExtensionAddsToThePolicy() {
		// Never runs before the extended deadline.
	}

	/** A passed method deadline wins over a future policy deadline. */
	@HiddenTest
	@Deadline("2000-01-01 00:00 UTC")
	@Policy(value = POLICIES + "PolicyHiddenTestsFutureUser.yaml", withinPath = SUBJECT)
	void methodDeadlineWinsOverThePolicy() {
		// Runs, because the method's own deadline has passed.
	}

	/** A future deadline, but before the policy's always-run-before date. */
	@HiddenTest
	@Policy(value = POLICIES + "PolicyHiddenTestsActiveUser.yaml", withinPath = SUBJECT)
	void policyAlwaysRunBeforeRuns() {
		// Runs, because the always-run-before date lies ahead.
	}

	/** A public test is not held back by the policy's deadline. */
	@PublicTest
	@Policy(value = POLICIES + "PolicyHiddenTestsFutureUser.yaml", withinPath = SUBJECT)
	void publicTestIgnoresThePolicyDeadline() {
		// Runs: public tests have no deadline.
	}

	/** A public test in a class the policy lists stays public. */
	@PublicTest
	@Policy(value = POLICIES + "PolicyHiddenTestsListedUser.yaml", withinPath = SUBJECT)
	void annotationWinsOverTheList() {
		// Runs: the annotation wins over the policy's list.
	}

	/** A public test under a policy whose list names nothing that exists. */
	@PublicTest
	@Policy(value = POLICIES + "PolicyHiddenTestsUnmatchedUser.yaml", withinPath = SUBJECT)
	void unmatchedEntryFailsAPublicTest() {
		// Never runs: the policy fails every test.
	}

	/** A public test under a policy that hides unlisted tests stays public. */
	@PublicTest
	@Policy(value = POLICIES + "PolicyHiddenTestsUnlistedHiddenUser.yaml", withinPath = SUBJECT)
	void annotationWinsWhenUnlistedTestsAreHidden() {
		// Runs: the annotation wins over the policy's setting for unlisted tests.
	}

	/** A public test under a policy whose public list names nothing that exists. */
	@PublicTest
	@Policy(value = POLICIES + "PolicyHiddenTestsUnmatchedPublicUser.yaml", withinPath = SUBJECT)
	void unmatchedPublicEntryFailsAPublicTest() {
		// Never runs: the policy fails every test.
	}

	/**
	 * A public test under a policy that does not say whether unlisted tests are
	 * hidden.
	 */
	@PublicTest
	@Policy(value = POLICIES + "PolicyHiddenTestsWithoutSwitchUser.yaml", withinPath = SUBJECT)
	void aPolicyWithoutTheUnlistedSwitchFailsATest() {
		// Never runs: the policy cannot be read.
	}
}
