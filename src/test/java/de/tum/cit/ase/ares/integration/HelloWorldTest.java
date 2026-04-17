package de.tum.cit.ase.ares.integration;

import static de.tum.cit.ase.ares.testutilities.CustomConditions.finishedSuccessfully;

import org.junit.platform.testkit.engine.Events;

import de.tum.cit.ase.ares.integration.testuser.HelloWorldUser;
import de.tum.cit.ase.ares.testutilities.TestTest;
import de.tum.cit.ase.ares.testutilities.UserBased;
import de.tum.cit.ase.ares.testutilities.UserTestResults;

/**
 * Verifies that simple hello-world tests pass under AspectJ @Policy modes and
 * with deactivated policy. Instrumentation modes are excluded because
 * {@code @UserBased} does not load the Java agent into the nested test JVM.
 */
@UserBased(HelloWorldUser.class)
class HelloWorldTest {

	@UserTestResults
	private static Events tests;

	@TestTest
	void test_helloWorld_mavenArchunitAspectJ() {
		tests.assertThatEvents().haveExactly(1, finishedSuccessfully("helloWorld_mavenArchunitAspectJ"));
	}

	@TestTest
	void test_helloWorld_mavenWalaAspectJ() {
		tests.assertThatEvents().haveExactly(1, finishedSuccessfully("helloWorld_mavenWalaAspectJ"));
	}

	@TestTest
	void test_helloWorld_policyDeactivated() {
		tests.assertThatEvents().haveExactly(1, finishedSuccessfully("helloWorld_policyDeactivated"));
	}

	@TestTest
	void test_helloWorld_noPolicy() {
		tests.assertThatEvents().haveExactly(1, finishedSuccessfully("helloWorld_noPolicy"));
	}
}
