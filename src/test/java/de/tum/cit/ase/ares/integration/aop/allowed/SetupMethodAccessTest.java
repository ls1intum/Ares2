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
class SetupMethodAccessTest {
	private static final String WITHIN_PATH = "test-classes/de/tum/cit/ase/ares/integration/aop/allowed/subject/fileSystem/read/fileInputStream";
	private static final String POLICIES = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/";

	private Throwable setupFailure;

	@BeforeEach
	void runStudentCodeFromSetupMethod() {
		try {
			ReadFileInputStreamMain.accessFileSystemViaFileInputStream();
		} catch (Throwable failure) {
			setupFailure = failure;
		}
	}

	@PublicTest
	@Policy(value = POLICIES + "archunit/aspectj/PolicyOnePathAllowedRead.yaml", withinPath = WITHIN_PATH)
	void test_allowedReadInBeforeEachStillWorksMavenArchunitAspectJ() {
		assertNull(setupFailure);
	}

	@PublicTest
	@Policy(value = POLICIES + "archunit/instrumentation/PolicyOnePathAllowedRead.yaml", withinPath = WITHIN_PATH)
	void test_allowedReadInBeforeEachStillWorksMavenArchunitInstrumentation() {
		assertNull(setupFailure);
	}

	@PublicTest
	@Policy(value = POLICIES + "wala/aspectj/PolicyOnePathAllowedRead.yaml", withinPath = WITHIN_PATH)
	void test_allowedReadInBeforeEachStillWorksMavenWalaAspectJ() {
		assertNull(setupFailure);
	}

	@PublicTest
	@Policy(value = POLICIES + "wala/instrumentation/PolicyOnePathAllowedRead.yaml", withinPath = WITHIN_PATH)
	void test_allowedReadInBeforeEachStillWorksMavenWalaInstrumentation() {
		assertNull(setupFailure);
	}
}
