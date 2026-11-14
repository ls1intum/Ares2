package de.tum.cit.ase.ares.integration.testuser;

import java.io.IOException;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import de.tum.cit.ase.ares.api.MirrorOutput;
import de.tum.cit.ase.ares.api.MirrorOutput.MirrorOutputPolicy;
import de.tum.cit.ase.ares.api.Policy;
import de.tum.cit.ase.ares.api.jupiter.Public;
import de.tum.cit.ase.ares.api.localization.UseLocale;
import de.tum.cit.ase.ares.integration.aop.forbidden.subject.fileSystem.read.oldMethods.files.FilesReadMain;

/**
 * Test classes whose constructor or one lifecycle method reads a file the
 * policy forbids, at MINIMIZED, so the report of a blocked action outside the
 * test method can be checked for every place it can happen; and two whose setup
 * fails or aborts while only privileged failures are reported.
 */
@SuppressWarnings({ "static-method", "PMD.TestClassWithoutTestCases" })
public final class FeedbackLevelLifecycleUsers {

	/** The MINIMIZED policy every class uses. */
	static final String POLICY = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/aspectj/PolicyFeedbackLevelMINIMIZED.yaml";

	/**
	 * A policy reporting only privileged failures, with the message "Policy
	 * message".
	 */
	static final String PRIVILEGED_ONLY_POLICY = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/aspectj/PolicyFeedbackLevelPrivilegedMinimized.yaml";

	/** Student code that does nothing forbidden. */
	static final String HARMLESS_WITHIN = "test-classes/de/tum/cit/ase/ares/integration/testuser/subject/helloWorld";

	/** The student code reading a file. */
	static final String WITHIN = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/oldMethods/files";

	/** Not to be created. */
	private FeedbackLevelLifecycleUsers() {
	}

	/**
	 * Reads the forbidden file.
	 *
	 * @throws IOException never, the read is blocked.
	 */
	static void readForbidden() throws IOException {
		FilesReadMain.accessFileSystemViaFilesRead();
	}

	/** Reads the forbidden file in its constructor. */
	@Public
	@UseLocale("en")
	@MirrorOutput(MirrorOutputPolicy.DISABLED)
	@Policy(value = POLICY, withinPath = WITHIN)
	public static final class ConstructorRead {

		/**
		 * Builds the test instance.
		 *
		 * @throws IOException never, the read is blocked.
		 */
		public ConstructorRead() throws IOException {
			readForbidden();
		}

		/** The test the constructor runs for. */
		@Test
		void runs() {
		}
	}

	/** Reads the forbidden file before all tests. */
	@Public
	@UseLocale("en")
	@MirrorOutput(MirrorOutputPolicy.DISABLED)
	@Policy(value = POLICY, withinPath = WITHIN)
	public static final class BeforeAllRead {

		/**
		 * Runs before all tests.
		 *
		 * @throws IOException never, the read is blocked.
		 */
		@BeforeAll
		static void setUpClass() throws IOException {
			readForbidden();
		}

		/** The test the setup runs for. */
		@Test
		void runs() {
		}
	}

	/** Reads the forbidden file before each test. */
	@Public
	@UseLocale("en")
	@MirrorOutput(MirrorOutputPolicy.DISABLED)
	@Policy(value = POLICY, withinPath = WITHIN)
	public static final class BeforeEachRead {

		/**
		 * Runs before each test.
		 *
		 * @throws IOException never, the read is blocked.
		 */
		@BeforeEach
		void setUp() throws IOException {
			readForbidden();
		}

		/** The test the setup runs for. */
		@Test
		void runs() {
		}
	}

	/** Reads the forbidden file after each test. */
	@Public
	@UseLocale("en")
	@MirrorOutput(MirrorOutputPolicy.DISABLED)
	@Policy(value = POLICY, withinPath = WITHIN)
	public static final class AfterEachRead {

		/**
		 * Runs after each test.
		 *
		 * @throws IOException never, the read is blocked.
		 */
		@AfterEach
		void tearDown() throws IOException {
			readForbidden();
		}

		/** The test the teardown runs for. */
		@Test
		void runs() {
		}
	}

	/** Reads the forbidden file after all tests. */
	@Public
	@UseLocale("en")
	@MirrorOutput(MirrorOutputPolicy.DISABLED)
	@Policy(value = POLICY, withinPath = WITHIN)
	public static final class AfterAllRead {

		/**
		 * Runs after all tests.
		 *
		 * @throws IOException never, the read is blocked.
		 */
		@AfterAll
		static void tearDownClass() throws IOException {
			readForbidden();
		}

		/** The test the teardown runs for. */
		@Test
		void runs() {
		}
	}

	/** Fails its setup with a text the policy's message replaces. */
	@Public
	@UseLocale("en")
	@MirrorOutput(MirrorOutputPolicy.DISABLED)
	@Policy(value = PRIVILEGED_ONLY_POLICY, withinPath = HARMLESS_WITHIN)
	public static final class PrivilegedOnlySetupFails {

		/** Fails before each test. */
		@BeforeEach
		void setUp() {
			throw new AssertionError("secret setup detail");
		}

		/** The test the setup runs for. */
		@Test
		void runs() {
		}
	}

	/** Stops its setup with an assumption, which skips the test. */
	@Public
	@UseLocale("en")
	@MirrorOutput(MirrorOutputPolicy.DISABLED)
	@Policy(value = PRIVILEGED_ONLY_POLICY, withinPath = HARMLESS_WITHIN)
	public static final class PrivilegedOnlySetupAborts {

		/** Aborts before each test. */
		@BeforeEach
		void setUp() {
			Assumptions.abort("not today");
		}

		/** The test the setup runs for. */
		@Test
		void runs() {
		}
	}
}
