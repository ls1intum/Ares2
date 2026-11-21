package de.tum.cit.ase.ares.integration.testuser;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.MethodOrderer.MethodName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.TestMethodOrder;

import de.tum.cit.ase.ares.api.Policy;
import de.tum.cit.ase.ares.api.io.AresIOContext;
import de.tum.cit.ase.ares.api.io.IOManager;
import de.tum.cit.ase.ares.api.io.IOTester;
import de.tum.cit.ase.ares.api.jupiter.PublicTest;
import de.tum.cit.ase.ares.api.localization.UseLocale;
import de.tum.cit.ase.ares.integration.testuser.subject.InputOutputPenguin;

/**
 * Tests whose console manager comes from the policy's
 * {@code theInputAndOutputAreManagedBy}: the IO tester, none at all, or a
 * custom class. Each nested class runs the cases under one of the four
 * combinations of static analysis and runtime checks.
 */
@UseLocale("en")
@SuppressWarnings("static-method")
public class IOManagerPolicyUser {

	/** The assertions every mode combination shares. */
	@TestMethodOrder(MethodName.class)
	abstract static class Cases {

		/**
		 * Prints a short line and checks that the IO tester recorded it.
		 *
		 * @param tester the IO tester of this test.
		 */
		static void recordsOutput(IOTester tester) {
			System.out.println("hi");
			assertThat(tester.out().getLinesAsString()).containsExactly("hi");
		}
	}

	/** ArchUnit with AspectJ. */
	@Nested
	class MavenArchunitAspectJ extends Cases {

		/**
		 * The IO tester the policy chooses records output for an assertion.
		 *
		 * @param tester the IO tester of this test.
		 */
		@PublicTest
		@Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/aspectj/PolicyIOManagerIOTester.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/testuser/subject/helloWorld")
		void ioTesterRecordsOutput(IOTester tester) {
			recordsOutput(tester);
		}

		/** The IO tester the policy chooses enforces the policy's output limit. */
		@PublicTest
		@Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/aspectj/PolicyIOManagerIOTester.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/testuser/subject/helloWorld")
		void ioTesterEnforcesTheLimit() {
			InputOutputPenguin.writeTwoLines();
		}

		/** With no manager, output is neither recorded nor limited. */
		@PublicTest
		@Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/aspectj/PolicyIOManagerNone.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/testuser/subject/helloWorld")
		void noneLetsOutputThrough() {
			InputOutputPenguin.writeTwoLines();
		}

		/**
		 * With no manager, no IO tester exists to hand to a test.
		 *
		 * @param tester never resolved.
		 */
		@PublicTest
		@Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/aspectj/PolicyIOManagerNone.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/testuser/subject/helloWorld")
		void noneProvidesNoTester(IOTester tester) {
			recordsOutput(tester);
		}

		/** A custom manager loaded from where the student code is, is refused. */
		@PublicTest
		@Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/aspectj/PolicyIOManagerCustom.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/testuser/subject/helloWorld")
		void customFromTheStudentOutputIsRefused() {
			System.out.println("hi");
		}
	}

	/** ArchUnit with instrumentation. */
	@Nested
	class MavenArchunitInstrumentation extends Cases {

		/**
		 * The IO tester the policy chooses records output for an assertion.
		 *
		 * @param tester the IO tester of this test.
		 */
		@PublicTest
		@Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/instrumentation/PolicyIOManagerIOTester.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/testuser/subject/helloWorld")
		void ioTesterRecordsOutput(IOTester tester) {
			recordsOutput(tester);
		}

		/** The IO tester the policy chooses enforces the policy's output limit. */
		@PublicTest
		@Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/instrumentation/PolicyIOManagerIOTester.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/testuser/subject/helloWorld")
		void ioTesterEnforcesTheLimit() {
			InputOutputPenguin.writeTwoLines();
		}

		/** With no manager, output is neither recorded nor limited. */
		@PublicTest
		@Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/instrumentation/PolicyIOManagerNone.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/testuser/subject/helloWorld")
		void noneLetsOutputThrough() {
			InputOutputPenguin.writeTwoLines();
		}

		/**
		 * With no manager, no IO tester exists to hand to a test.
		 *
		 * @param tester never resolved.
		 */
		@PublicTest
		@Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/instrumentation/PolicyIOManagerNone.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/testuser/subject/helloWorld")
		void noneProvidesNoTester(IOTester tester) {
			recordsOutput(tester);
		}

		/** A custom manager loaded from where the student code is, is refused. */
		@PublicTest
		@Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/instrumentation/PolicyIOManagerCustom.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/testuser/subject/helloWorld")
		void customFromTheStudentOutputIsRefused() {
			System.out.println("hi");
		}
	}

	/** WALA with AspectJ. */
	@Nested
	class MavenWalaAspectJ extends Cases {

		/**
		 * The IO tester the policy chooses records output for an assertion.
		 *
		 * @param tester the IO tester of this test.
		 */
		@PublicTest
		@Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/aspectj/PolicyIOManagerIOTester.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/testuser/subject/helloWorld")
		void ioTesterRecordsOutput(IOTester tester) {
			recordsOutput(tester);
		}

		/** The IO tester the policy chooses enforces the policy's output limit. */
		@PublicTest
		@Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/aspectj/PolicyIOManagerIOTester.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/testuser/subject/helloWorld")
		void ioTesterEnforcesTheLimit() {
			InputOutputPenguin.writeTwoLines();
		}

		/** With no manager, output is neither recorded nor limited. */
		@PublicTest
		@Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/aspectj/PolicyIOManagerNone.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/testuser/subject/helloWorld")
		void noneLetsOutputThrough() {
			InputOutputPenguin.writeTwoLines();
		}

		/**
		 * With no manager, no IO tester exists to hand to a test.
		 *
		 * @param tester never resolved.
		 */
		@PublicTest
		@Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/aspectj/PolicyIOManagerNone.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/testuser/subject/helloWorld")
		void noneProvidesNoTester(IOTester tester) {
			recordsOutput(tester);
		}

		/** A custom manager loaded from where the student code is, is refused. */
		@PublicTest
		@Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/aspectj/PolicyIOManagerCustom.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/testuser/subject/helloWorld")
		void customFromTheStudentOutputIsRefused() {
			System.out.println("hi");
		}
	}

	/** WALA with instrumentation. */
	@Nested
	class MavenWalaInstrumentation extends Cases {

		/**
		 * The IO tester the policy chooses records output for an assertion.
		 *
		 * @param tester the IO tester of this test.
		 */
		@PublicTest
		@Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/instrumentation/PolicyIOManagerIOTester.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/testuser/subject/helloWorld")
		void ioTesterRecordsOutput(IOTester tester) {
			recordsOutput(tester);
		}

		/** The IO tester the policy chooses enforces the policy's output limit. */
		@PublicTest
		@Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/instrumentation/PolicyIOManagerIOTester.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/testuser/subject/helloWorld")
		void ioTesterEnforcesTheLimit() {
			InputOutputPenguin.writeTwoLines();
		}

		/** With no manager, output is neither recorded nor limited. */
		@PublicTest
		@Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/instrumentation/PolicyIOManagerNone.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/testuser/subject/helloWorld")
		void noneLetsOutputThrough() {
			InputOutputPenguin.writeTwoLines();
		}

		/**
		 * With no manager, no IO tester exists to hand to a test.
		 *
		 * @param tester never resolved.
		 */
		@PublicTest
		@Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/instrumentation/PolicyIOManagerNone.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/testuser/subject/helloWorld")
		void noneProvidesNoTester(IOTester tester) {
			recordsOutput(tester);
		}

		/** A custom manager loaded from where the student code is, is refused. */
		@PublicTest
		@Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/instrumentation/PolicyIOManagerCustom.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/testuser/subject/helloWorld")
		void customFromTheStudentOutputIsRefused() {
			System.out.println("hi");
		}
	}

	/**
	 * A manager the custom policy names. It lives in the test output, which here is
	 * also where the student code is, so Ares refuses it.
	 */
	public static class RecordingManager implements IOManager<Void> {

		/** Does nothing before a test. */
		@Override
		public void beforeTestExecution(AresIOContext context) {
			// The refusal happens before any test runs.
		}

		/** Does nothing after a test. */
		@Override
		public void afterTestExecution(AresIOContext context) {
			// The refusal happens before any test runs.
		}

		/** Provides no controller. */
		@Override
		public Void getControllerInstance(AresIOContext context) {
			return null;
		}

		/** Provides no controller type. */
		@Override
		public Class<Void> getControllerClass() {
			return null;
		}
	}
}
