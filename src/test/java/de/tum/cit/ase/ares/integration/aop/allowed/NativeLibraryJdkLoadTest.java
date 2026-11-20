package de.tum.cit.ase.ares.integration.aop.allowed;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;

import de.tum.cit.ase.ares.api.Policy;
import de.tum.cit.ase.ares.api.jupiter.PublicTest;
import de.tum.cit.ase.ares.integration.aop.allowed.subject.nativeLibrary.JdkLibraryUser;

/**
 * Lets student code make the JDK load its own {@code jaas} library, which the
 * JDK asks for on every use, so the check runs whatever loaded it before. The
 * same test then asks for that library from test code beneath the student code,
 * which must be refused by the same check.
 */
class NativeLibraryJdkLoadTest {

	/**
	 * Folder of the student code the static analysis inspects.
	 */
	private static final String WITHIN_PATH = "test-classes/de/tum/cit/ase/ares/integration/aop/allowed/subject/nativeLibrary";

	/**
	 * Policy with one unrelated create path for MavenArchunitInstrumentation.
	 */
	private static final String POLICY_ARCHUNITINSTRUMENTATION = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/instrumentation/PolicyNativeLibraryLoad.yaml";

	/**
	 * Policy with one unrelated create path for MavenWalaInstrumentation.
	 */
	private static final String POLICY_WALAINSTRUMENTATION = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/instrumentation/PolicyNativeLibraryLoad.yaml";

	/**
	 * The JDK method whose check decides every library load in instrumentation
	 * mode, as the refusal names it.
	 */
	private static final String DECIDING_METHOD = "java.lang.Runtime.loadLibrary0(java.lang.Class,java.lang.String)";

	/**
	 * The JDK loads {@code jaas} for the student under ArchUnit and
	 * instrumentation.
	 */
	@PublicTest
	@Policy(value = POLICY_ARCHUNITINSTRUMENTATION, withinPath = WITHIN_PATH)
	void test_jdkLoadsItsOwnLibraryForStudentMavenArchunitInstrumentation() {
		assertJdkLoadAllowedAndTestLoadRefused();
	}

	/**
	 * The JDK loads {@code jaas} for the student under WALA and instrumentation.
	 */
	@PublicTest
	@Policy(value = POLICY_WALAINSTRUMENTATION, withinPath = WITHIN_PATH)
	void test_jdkLoadsItsOwnLibraryForStudentMavenWalaInstrumentation() {
		assertJdkLoadAllowedAndTestLoadRefused();
	}

	/**
	 * Requires two student uses to succeed and the test's own request for the same
	 * library, made beneath student code, to be refused by the deciding method's
	 * check.
	 */
	private static void assertJdkLoadAllowedAndTestLoadRefused() {
		assertDoesNotThrow(JdkLibraryUser::readUserId);
		assertDoesNotThrow(JdkLibraryUser::readUserId);
		SecurityException refused = assertThrows(SecurityException.class,
				() -> JdkLibraryUser.run(() -> System.loadLibrary("jaas")));
		String expected = Path.of("jaas").toAbsolutePath() + " via " + DECIDING_METHOD;
		assertTrue(refused.getMessage().contains(expected), refused::getMessage);
	}
}
