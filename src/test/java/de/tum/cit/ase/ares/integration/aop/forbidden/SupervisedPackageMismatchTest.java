package de.tum.cit.ase.ares.integration.aop.forbidden;

import static org.junit.platform.engine.discovery.DiscoverySelectors.selectClass;
import static org.junit.platform.testkit.engine.EventConditions.event;
import static org.junit.platform.testkit.engine.EventConditions.finishedWithFailure;
import static org.junit.platform.testkit.engine.EventConditions.test;
import static org.junit.platform.testkit.engine.TestExecutionResultConditions.instanceOf;
import static org.junit.platform.testkit.engine.TestExecutionResultConditions.message;

import org.junit.jupiter.api.Test;
import org.junit.platform.testkit.engine.EngineTestKit;

import de.tum.cit.ase.ares.api.Policy;
import de.tum.cit.ase.ares.api.jupiter.PublicTest;

/**
 * A policy whose supervised package does not hold the classes its withinPath
 * points to would check those classes before the tests but never while they
 * run, so Ares refuses it. The same withinPath with the right package passes in
 * {@code SupervisionLifecycleTest}.
 */
class SupervisedPackageMismatchTest {

	/** The folder of the student classes, outside the policy's package. */
	private static final String WITHIN_PATH = "test-classes/de/tum/cit/ase/ares/integration/aop/allowed/subject/lifecycle";
	/** The folder that holds the policies of all four mode combinations. */
	private static final String POLICIES = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/";
	/** The policy file name, the same in every mode folder. */
	private static final String POLICY = "/PolicyForeignSupervisedPackage.yaml";

	/** Has one test, which must fail before it runs. */
	abstract static class MismatchFixture {

		/** Runs nothing; the policy is refused before it. */
		@PublicTest
		void refusedBeforeItRuns() {
			Math.sqrt(2);
		}
	}

	/** The mismatch under ArchUnit and AspectJ. */
	@Policy(value = POLICIES + "archunit/aspectj" + POLICY, withinPath = WITHIN_PATH)
	static class ArchunitAspectJ extends MismatchFixture {
	}

	/** The mismatch under ArchUnit and instrumentation. */
	@Policy(value = POLICIES + "archunit/instrumentation" + POLICY, withinPath = WITHIN_PATH)
	static class ArchunitInstrumentation extends MismatchFixture {
	}

	/** The mismatch under WALA and AspectJ. */
	@Policy(value = POLICIES + "wala/aspectj" + POLICY, withinPath = WITHIN_PATH)
	static class WalaAspectJ extends MismatchFixture {
	}

	/** The mismatch under WALA and instrumentation. */
	@Policy(value = POLICIES + "wala/instrumentation" + POLICY, withinPath = WITHIN_PATH)
	static class WalaInstrumentation extends MismatchFixture {
	}

	/**
	 * Runs a fixture and checks that its test failed with a refusal naming a
	 * student class outside the package.
	 *
	 * @param fixture the fixture to run
	 */
	private static void assertRefused(Class<?> fixture) {
		EngineTestKit.engine("junit-jupiter").selectors(selectClass(fixture)).execute().testEvents().assertThatEvents()
				.haveExactly(1, event(test(), finishedWithFailure(instanceOf(SecurityException.class),
						message(text -> text.contains("aop.allowed.subject.lifecycle.")))));
	}

	/** Refused under ArchUnit and AspectJ. */
	@Test
	void foreignSupervisedPackageIsRefusedMavenArchunitAspectJ() {
		assertRefused(ArchunitAspectJ.class);
	}

	/** Refused under ArchUnit and instrumentation. */
	@Test
	void foreignSupervisedPackageIsRefusedMavenArchunitInstrumentation() {
		assertRefused(ArchunitInstrumentation.class);
	}

	/** Refused under WALA and AspectJ. */
	@Test
	void foreignSupervisedPackageIsRefusedMavenWalaAspectJ() {
		assertRefused(WalaAspectJ.class);
	}

	/** Refused under WALA and instrumentation. */
	@Test
	void foreignSupervisedPackageIsRefusedMavenWalaInstrumentation() {
		assertRefused(WalaInstrumentation.class);
	}
}
