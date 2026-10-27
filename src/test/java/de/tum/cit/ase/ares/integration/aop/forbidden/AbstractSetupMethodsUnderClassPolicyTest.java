package de.tum.cit.ase.ares.integration.aop.forbidden;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.function.Executable;

import de.tum.cit.ase.ares.integration.aop.forbidden.subject.fileSystem.read.byteStream.FileinputStreamMain;

/**
 * Shared part of the tests that run student code from a field initialiser and
 * from {@code @BeforeAll}. A subclass carries the class-level {@code @Policy}
 * of one mode combination and {@code @Public}, which registers Ares for the
 * whole class, as {@code @BeforeAll} needs. A policy on a test method is not
 * known that early, so only a class-level policy can restrict these two.
 */
abstract class AbstractSetupMethodsUnderClassPolicyTest extends SystemAccessTest {
	/** The package of the student class that reads the forbidden file. */
	static final String WITHIN_PATH = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/read/byteStream";

	/** What the student code threw from the {@code @BeforeAll} method, or null. */
	private static Throwable beforeAllFailure;

	/** What the student code threw from this field initialiser, or null. */
	private final Throwable fieldInitialiserFailure = runStudentCode();

	/** Runs student code once, before any test of the class. */
	@BeforeAll
	static void runStudentCodeFromBeforeAll() {
		beforeAllFailure = runStudentCode();
	}

	/** Runs the student read and returns what it threw, or null if it succeeded. */
	private static Throwable runStudentCode() {
		try {
			FileinputStreamMain.accessFileSystemViaFileInputStream();
			return null;
		} catch (Throwable failure) {
			return failure;
		}
	}

	/** Rethrows the given failure, so a test can assert on it. */
	private static Executable rethrow(Throwable failure) {
		return () -> {
			if (failure != null) {
				throw failure;
			}
		};
	}

	/** Asserts that the field initialiser was denied by Ares. */
	void assertFieldInitialiserWasBlocked() {
		assertAresSecurityExceptionRead(rethrow(fieldInitialiserFailure), FileinputStreamMain.class);
	}

	/** Asserts that the {@code @BeforeAll} method was denied by Ares. */
	void assertBeforeAllWasBlocked() {
		assertAresSecurityExceptionRead(rethrow(beforeAllFailure), FileinputStreamMain.class);
	}
}
