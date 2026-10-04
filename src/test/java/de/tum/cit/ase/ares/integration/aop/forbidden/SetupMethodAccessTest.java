package de.tum.cit.ase.ares.integration.aop.forbidden;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.function.Executable;

import de.tum.cit.ase.ares.api.Policy;
import de.tum.cit.ase.ares.api.jupiter.PublicTest;
import de.tum.cit.ase.ares.integration.aop.forbidden.subject.fileSystem.read.byteStream.FileinputStreamMain;

/**
 * Student code that runs from a field initialiser or a {@code @BeforeEach}
 * method must be blocked by the same policy as student code that runs in the
 * test method. The student action runs in the setup method, so a test only
 * passes if the guard was already armed when that method started.
 */
class SetupMethodAccessTest extends SystemAccessTest {
	/** The package of the student class that reads the forbidden file. */
	private static final String WITHIN_PATH = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/byteStream";

	/**
	 * What the student code threw from the setup method, or null if it ran freely.
	 */
	private Throwable setupFailure;

	/**
	 * What the student code threw from this field initialiser, or null if it ran
	 * freely.
	 */
	private final Throwable fieldInitialiserFailure = captureStudentCode();

	/** Runs student code that reads a forbidden file, before every test. */
	@BeforeEach
	void runStudentCodeFromSetupMethod() {
		setupFailure = captureStudentCode();
	}

	/**
	 * Runs the student read and returns what it threw, or null if it ran freely.
	 */
	private static Throwable captureStudentCode() {
		try {
			FileinputStreamMain.accessFileSystemViaFileInputStream();
			return null;
		} catch (Throwable failure) {
			return failure;
		}
	}

	/** Rethrows what the given step caught, so a test can assert on it. */
	private static Executable rethrow(Throwable failure) {
		return () -> {
			if (failure != null) {
				throw failure;
			}
		};
	}

	/** Asserts that both the field initialiser and the setup method were denied. */
	private void assertBothWereBlocked() {
		assertAresSecurityExceptionRead(rethrow(setupFailure), FileinputStreamMain.class);
		assertAresSecurityExceptionRead(rethrow(fieldInitialiserFailure), FileinputStreamMain.class);
	}

	/** The setup read was denied under ArchUnit and AspectJ. */
	@PublicTest
	@Policy(value = ARCHUNIT_ASPECTJ_POLICY_ONE_PATH_ALLOWED_READ, withinPath = WITHIN_PATH)
	void test_studentCodeInBeforeEachIsBlockedMavenArchunitAspectJ() {
		assertBothWereBlocked();
	}

	/** The setup read was denied under ArchUnit and instrumentation. */
	@PublicTest
	@Policy(value = ARCHUNIT_INSTRUMENTATION_POLICY_ONE_PATH_ALLOWED_READ, withinPath = WITHIN_PATH)
	void test_studentCodeInBeforeEachIsBlockedMavenArchunitInstrumentation() {
		assertBothWereBlocked();
	}

	/** The setup read was denied under WALA and AspectJ. */
	@PublicTest
	@Policy(value = WALA_ASPECTJ_POLICY_ONE_PATH_ALLOWED_READ, withinPath = WITHIN_PATH)
	void test_studentCodeInBeforeEachIsBlockedMavenWalaAspectJ() {
		assertBothWereBlocked();
	}

	/** The setup read was denied under WALA and instrumentation. */
	@PublicTest
	@Policy(value = WALA_INSTRUMENTATION_POLICY_ONE_PATH_ALLOWED_READ, withinPath = WITHIN_PATH)
	void test_studentCodeInBeforeEachIsBlockedMavenWalaInstrumentation() {
		assertBothWereBlocked();
	}
}
