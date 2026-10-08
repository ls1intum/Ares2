package de.tum.cit.ase.ares.integration.aop.allowed;

import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.BeforeEach;

import de.tum.cit.ase.ares.api.Policy;
import de.tum.cit.ase.ares.api.jupiter.PublicTest;
import de.tum.cit.ase.ares.integration.aop.allowed.subject.fileSystem.read.fileInputStream.ReadFileInputStreamMain;

/**
 * Arming the guard before {@code @BeforeEach} must not block what the policy
 * permits: student code that reads the allowed file from the setup method still
 * succeeds.
 */
class SetupMethodAllowedAccessTest {
	/** The package of the student class that reads the allowed file. */
	private static final String WITHIN_PATH = "test-classes/de/tum/cit/ase/ares/integration/aop/allowed/subject/fileSystem/read/fileInputStream";
	/** The folder that holds the policies of all four mode combinations. */
	private static final String POLICIES = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/";

	/**
	 * What the student code threw from the setup method, or null if it succeeded.
	 */
	private Throwable setupFailure;

	/** Runs student code that reads the allowed file, before every test. */
	@BeforeEach
	void runStudentCodeFromSetupMethod() {
		try {
			ReadFileInputStreamMain.accessFileSystemViaFileInputStream();
		} catch (Throwable failure) {
			setupFailure = failure;
		}
	}

	/** The setup read succeeded under ArchUnit and AspectJ. */
	@PublicTest
	@Policy(value = POLICIES + "archunit/aspectj/PolicyOnePathAllowedRead.yaml", withinPath = WITHIN_PATH)
	void test_allowedReadInBeforeEachStillWorksMavenArchunitAspectJ() {
		assertNull(setupFailure);
	}

	/** The setup read succeeded under ArchUnit and instrumentation. */
	@PublicTest
	@Policy(value = POLICIES + "archunit/instrumentation/PolicyOnePathAllowedRead.yaml", withinPath = WITHIN_PATH)
	void test_allowedReadInBeforeEachStillWorksMavenArchunitInstrumentation() {
		assertNull(setupFailure);
	}

	/** The setup read succeeded under WALA and AspectJ. */
	@PublicTest
	@Policy(value = POLICIES + "wala/aspectj/PolicyOnePathAllowedRead.yaml", withinPath = WITHIN_PATH)
	void test_allowedReadInBeforeEachStillWorksMavenWalaAspectJ() {
		assertNull(setupFailure);
	}

	/** The setup read succeeded under WALA and instrumentation. */
	@PublicTest
	@Policy(value = POLICIES + "wala/instrumentation/PolicyOnePathAllowedRead.yaml", withinPath = WITHIN_PATH)
	void test_allowedReadInBeforeEachStillWorksMavenWalaInstrumentation() {
		assertNull(setupFailure);
	}
}
