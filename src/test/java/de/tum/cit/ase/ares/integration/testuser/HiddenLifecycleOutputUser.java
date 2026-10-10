package de.tum.cit.ase.ares.integration.testuser;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;

import de.tum.cit.ase.ares.api.Deadline;
import de.tum.cit.ase.ares.api.MirrorOutput;
import de.tum.cit.ase.ares.api.Policy;
import de.tum.cit.ase.ares.api.WithIOManager;
import de.tum.cit.ase.ares.api.io.AresIOContext;
import de.tum.cit.ase.ares.api.io.IOManager;
import de.tum.cit.ase.ares.api.jupiter.Hidden;
import de.tum.cit.ase.ares.api.jupiter.HiddenTest;

/** Emits distinctive text from every hidden Jupiter lifecycle phase. */
@Hidden
@Deadline("2000-01-01 00:00")
@MirrorOutput(MirrorOutput.MirrorOutputPolicy.ENABLED)
@WithIOManager(HiddenLifecycleOutputUser.NoisyIOManager.class)
@Policy(activated = false)
public class HiddenLifecycleOutputUser {

	/** Emits output while a hidden test instance is constructed. */
	public HiddenLifecycleOutputUser() {
		System.out.print("SECRET_HIDDEN_CONSTRUCTOR");
	}

	/** Emits output before the class's tests. */
	@BeforeAll
	static void beforeAll() {
		System.out.print("SECRET_HIDDEN_BEFORE_ALL");
	}

	/** Emits output before the hidden test body. */
	@BeforeEach
	void beforeEach() {
		System.out.print("SECRET_HIDDEN_BEFORE_EACH");
	}

	/** Emits output after the hidden test body. */
	@AfterEach
	void afterEach() {
		System.err.print("SECRET_HIDDEN_AFTER_EACH");
	}

	/** Emits output after the class's tests. */
	@AfterAll
	static void afterAll() {
		System.err.print("SECRET_HIDDEN_AFTER_ALL");
	}

	/** Emits output from a hidden test body. */
	@HiddenTest
	void hiddenBody() {
		System.out.print("SECRET_HIDDEN_BODY");
		throw new AssertionError("SECRET_HIDDEN_FAILURE");
	}

	/** Emits output when Jupiter calls a custom I/O manager. */
	public static final class NoisyIOManager implements IOManager<Void> {

		/** Creates a manager and emits a distinctive marker. */
		public NoisyIOManager() {
			System.out.print("SECRET_HIDDEN_MANAGER_CONSTRUCTOR");
		}

		/** Emits output when capture starts. */
		@Override
		public void beforeTestExecution(AresIOContext context) {
			System.out.print("SECRET_HIDDEN_MANAGER_BEFORE");
		}

		/** Emits output when capture ends. */
		@Override
		public void afterTestExecution(AresIOContext context) {
			System.err.print("SECRET_HIDDEN_MANAGER_AFTER");
		}

		/** Provides no controller to the test method. */
		@Override
		public Void getControllerInstance(AresIOContext context) {
			return null;
		}

		/** Provides no controller class. */
		@Override
		public Class<Void> getControllerClass() {
			return null;
		}
	}
}
