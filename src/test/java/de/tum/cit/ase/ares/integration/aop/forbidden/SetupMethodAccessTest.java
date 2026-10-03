package de.tum.cit.ase.ares.integration.aop.forbidden;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.function.Executable;

import de.tum.cit.ase.ares.api.Policy;
import de.tum.cit.ase.ares.api.jupiter.PublicTest;
import de.tum.cit.ase.ares.integration.aop.forbidden.subject.fileSystem.read.byteStream.FileinputStreamMain;

/**
 * Student code that runs from a {@code @BeforeEach} method must be blocked by
 * the same policy as student code that runs in the test method. The student
 * action runs in the setup method, so a test only passes if the guard was
 * already armed when that method started.
 */
class SetupMethodAccessTest extends SystemAccessTest {
	private static final String WITHIN_PATH = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/byteStream";

	private Throwable setupFailure;

	@BeforeEach
	void runStudentCodeFromSetupMethod() {
		try {
			FileinputStreamMain.accessFileSystemViaFileInputStream();
		} catch (Throwable failure) {
			setupFailure = failure;
		}
	}

	private Executable theSetupFailure() {
		return () -> {
			if (setupFailure != null) {
				throw setupFailure;
			}
		};
	}

	@PublicTest
	@Policy(value = ARCHUNIT_ASPECTJ_POLICY_ONE_PATH_ALLOWED_READ, withinPath = WITHIN_PATH)
	void test_studentCodeInBeforeEachIsBlockedMavenArchunitAspectJ() {
		assertAresSecurityExceptionRead(theSetupFailure(), FileinputStreamMain.class);
	}

	@PublicTest
	@Policy(value = ARCHUNIT_INSTRUMENTATION_POLICY_ONE_PATH_ALLOWED_READ, withinPath = WITHIN_PATH)
	void test_studentCodeInBeforeEachIsBlockedMavenArchunitInstrumentation() {
		assertAresSecurityExceptionRead(theSetupFailure(), FileinputStreamMain.class);
	}

	@PublicTest
	@Policy(value = WALA_ASPECTJ_POLICY_ONE_PATH_ALLOWED_READ, withinPath = WITHIN_PATH)
	void test_studentCodeInBeforeEachIsBlockedMavenWalaAspectJ() {
		assertAresSecurityExceptionRead(theSetupFailure(), FileinputStreamMain.class);
	}

	@PublicTest
	@Policy(value = WALA_INSTRUMENTATION_POLICY_ONE_PATH_ALLOWED_READ, withinPath = WITHIN_PATH)
	void test_studentCodeInBeforeEachIsBlockedMavenWalaInstrumentation() {
		assertAresSecurityExceptionRead(theSetupFailure(), FileinputStreamMain.class);
	}
}
