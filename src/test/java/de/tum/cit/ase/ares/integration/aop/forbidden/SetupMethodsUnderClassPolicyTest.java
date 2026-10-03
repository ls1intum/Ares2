package de.tum.cit.ase.ares.integration.aop.forbidden;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import de.tum.cit.ase.ares.api.Policy;
import de.tum.cit.ase.ares.api.jupiter.Public;
import de.tum.cit.ase.ares.integration.aop.forbidden.subject.fileSystem.read.byteStream.FileinputStreamMain;

/**
 * Student code that runs from a field initialiser or a {@code @BeforeAll}
 * method must be blocked when the class carries the {@code @Policy}. A
 * method-level policy is not known that early, so only a class-level policy can
 * restrict them. {@code @Public} on the class registers Ares for the whole
 * class, which {@code @BeforeAll} needs.
 */
@Public
@Policy(value = SystemAccessTest.ARCHUNIT_ASPECTJ_POLICY_ONE_PATH_ALLOWED_READ, withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/byteStream")
class SetupMethodsUnderClassPolicyTest extends SystemAccessTest {
	private static Throwable beforeAllFailure;

	private final Throwable fieldInitialiserFailure = runStudentCode();

	@BeforeAll
	static void runStudentCodeFromBeforeAll() {
		beforeAllFailure = runStudentCode();
	}

	private static Throwable runStudentCode() {
		try {
			FileinputStreamMain.accessFileSystemViaFileInputStream();
			return null;
		} catch (Throwable failure) {
			return failure;
		}
	}

	private static Executable rethrow(Throwable failure) {
		return () -> {
			if (failure != null) {
				throw failure;
			}
		};
	}

	@Test
	void test_studentCodeInFieldInitialiserIsBlocked() {
		assertAresSecurityExceptionRead(rethrow(fieldInitialiserFailure), FileinputStreamMain.class);
	}

	@Test
	void test_studentCodeInBeforeAllIsBlocked() {
		assertAresSecurityExceptionRead(rethrow(beforeAllFailure), FileinputStreamMain.class);
	}
}
