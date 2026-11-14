package de.tum.cit.ase.ares.integration.architecture.forbidden;

import static org.junit.platform.engine.discovery.DiscoverySelectors.selectMethod;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.testkit.engine.EngineTestKit;
import org.junit.platform.testkit.engine.Events;

import de.tum.cit.ase.ares.api.Policy;
import de.tum.cit.ase.ares.api.jupiter.PublicTest;
import de.tum.cit.ase.ares.integration.architecture.forbidden.subject.typeannotation.TypeAnnotationCastMain;

/**
 * Checks, in all four mode combinations, that a type named only through a type
 * annotation counts as an import: student code with an annotated cast is
 * rejected without an allowance and passes with one. Each case runs as a
 * disabled inner test, as the other architecture tests do.
 */
class TypeAnnotationImportTest extends SystemAccessTest {

	/**
	 * Folder of the student code with the annotated cast.
	 */
	private static final String SUBJECT_PATH = "test-classes/de/tum/cit/ase/ares/integration/architecture/forbidden/subject/typeannotation";

	/**
	 * The package of the type annotation, which the rejection must name.
	 */
	private static final String FOREIGN_PACKAGE = "org.example.subject.typeuse.foreign";

	/**
	 * Policy without an allowance for the foreign package, for
	 * MavenArchunitAspectJ.
	 */
	private static final String FORBIDDEN_POLICY_ARCHUNITASPECTJ = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/aspectj/PolicyTypeAnnotationImportForbidden.yaml";

	/**
	 * Policy with an allowance for the foreign package, for MavenArchunitAspectJ.
	 */
	private static final String ALLOWED_POLICY_ARCHUNITASPECTJ = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/aspectj/PolicyTypeAnnotationImportAllowed.yaml";

	/**
	 * Policy without an allowance for the foreign package, for
	 * MavenArchunitInstrumentation.
	 */
	private static final String FORBIDDEN_POLICY_ARCHUNITINSTRUMENTATION = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/instrumentation/PolicyTypeAnnotationImportForbidden.yaml";

	/**
	 * Policy with an allowance for the foreign package, for
	 * MavenArchunitInstrumentation.
	 */
	private static final String ALLOWED_POLICY_ARCHUNITINSTRUMENTATION = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/instrumentation/PolicyTypeAnnotationImportAllowed.yaml";

	/**
	 * Policy without an allowance for the foreign package, for MavenWalaAspectJ.
	 */
	private static final String FORBIDDEN_POLICY_WALAASPECTJ = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/aspectj/PolicyTypeAnnotationImportForbidden.yaml";

	/**
	 * Policy with an allowance for the foreign package, for MavenWalaAspectJ.
	 */
	private static final String ALLOWED_POLICY_WALAASPECTJ = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/aspectj/PolicyTypeAnnotationImportAllowed.yaml";

	/**
	 * Policy without an allowance for the foreign package, for
	 * MavenWalaInstrumentation.
	 */
	private static final String FORBIDDEN_POLICY_WALAINSTRUMENTATION = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/instrumentation/PolicyTypeAnnotationImportForbidden.yaml";

	/**
	 * Policy with an allowance for the foreign package, for
	 * MavenWalaInstrumentation.
	 */
	private static final String ALLOWED_POLICY_WALAINSTRUMENTATION = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/instrumentation/PolicyTypeAnnotationImportAllowed.yaml";

	/**
	 * A cast annotated with a type from a forbidden package is rejected as an
	 * import in MavenArchunitAspectJ.
	 */
	@Test
	void test_typeAnnotationForbiddenMavenArchunitAspectJ_test() {
		executeTestAndExpectImportRejection("test_typeAnnotationForbiddenMavenArchunitAspectJ");
	}

	/**
	 * The same student code passes in MavenArchunitAspectJ once the policy allows
	 * the package.
	 */
	@Test
	void test_typeAnnotationAllowedMavenArchunitAspectJ_test() {
		executeTestAndExpectSuccess("test_typeAnnotationAllowedMavenArchunitAspectJ");
	}

	/**
	 * A cast annotated with a type from a forbidden package is rejected as an
	 * import in MavenArchunitInstrumentation.
	 */
	@Test
	void test_typeAnnotationForbiddenMavenArchunitInstrumentation_test() {
		executeTestAndExpectImportRejection("test_typeAnnotationForbiddenMavenArchunitInstrumentation");
	}

	/**
	 * The same student code passes in MavenArchunitInstrumentation once the policy
	 * allows the package.
	 */
	@Test
	void test_typeAnnotationAllowedMavenArchunitInstrumentation_test() {
		executeTestAndExpectSuccess("test_typeAnnotationAllowedMavenArchunitInstrumentation");
	}

	/**
	 * A cast annotated with a type from a forbidden package is rejected as an
	 * import in MavenWalaAspectJ.
	 */
	@Test
	void test_typeAnnotationForbiddenMavenWalaAspectJ_test() {
		executeTestAndExpectImportRejection("test_typeAnnotationForbiddenMavenWalaAspectJ");
	}

	/**
	 * The same student code passes in MavenWalaAspectJ once the policy allows the
	 * package.
	 */
	@Test
	void test_typeAnnotationAllowedMavenWalaAspectJ_test() {
		executeTestAndExpectSuccess("test_typeAnnotationAllowedMavenWalaAspectJ");
	}

	/**
	 * A cast annotated with a type from a forbidden package is rejected as an
	 * import in MavenWalaInstrumentation.
	 */
	@Test
	void test_typeAnnotationForbiddenMavenWalaInstrumentation_test() {
		executeTestAndExpectImportRejection("test_typeAnnotationForbiddenMavenWalaInstrumentation");
	}

	/**
	 * The same student code passes in MavenWalaInstrumentation once the policy
	 * allows the package.
	 */
	@Test
	void test_typeAnnotationAllowedMavenWalaInstrumentation_test() {
		executeTestAndExpectSuccess("test_typeAnnotationAllowedMavenWalaInstrumentation");
	}

	/**
	 * Runs one inner test and expects the import rejection naming the foreign
	 * package.
	 *
	 * @param methodName the inner test
	 */
	private void executeTestAndExpectImportRejection(String methodName) {
		executeTestAndExpectSecurityException(TypeAnnotationImportTest.class, methodName);
		Throwable failure = runInnerTest(methodName).failed().stream().findFirst()
				.flatMap(event -> event.getPayload(TestExecutionResult.class))
				.flatMap(TestExecutionResult::getThrowable).orElseThrow();
		boolean namesThePackage = false;
		for (Throwable cause = failure; cause != null && !namesThePackage; cause = cause.getCause()) {
			namesThePackage = cause.getMessage() != null && cause.getMessage().contains(FOREIGN_PACKAGE);
		}
		Assertions.assertTrue(namesThePackage, () -> "the rejection must name " + FOREIGN_PACKAGE + ": " + failure);
	}

	/**
	 * Runs one inner test and expects it to succeed.
	 *
	 * @param methodName the inner test
	 */
	private void executeTestAndExpectSuccess(String methodName) {
		Events events = runInnerTest(methodName);
		events.assertStatistics(stats -> stats.failed(0).aborted(0).succeeded(1));
	}

	/**
	 * Runs one inner test with the disabled condition switched off.
	 *
	 * @param methodName the inner test
	 * @return the test events
	 */
	private static Events runInnerTest(String methodName) {
		return EngineTestKit.engine("junit-jupiter")
				.configurationParameter("junit.jupiter.conditions.deactivate", "org.junit.*DisabledCondition")
				.selectors(selectMethod(TypeAnnotationImportTest.class, methodName)).execute().testEvents();
	}

	/**
	 * Runs the annotated cast under the forbidden policy in MavenArchunitAspectJ.
	 */
	@Disabled(SUBJECT_PROBE_REASON)
	@PublicTest
	@Policy(value = FORBIDDEN_POLICY_ARCHUNITASPECTJ, withinPath = SUBJECT_PATH)
	void test_typeAnnotationForbiddenMavenArchunitAspectJ() {
		TypeAnnotationCastMain.castToText("text");
	}

	/**
	 * Runs the annotated cast under the allowed policy in MavenArchunitAspectJ.
	 */
	@Disabled(SUBJECT_PROBE_REASON)
	@PublicTest
	@Policy(value = ALLOWED_POLICY_ARCHUNITASPECTJ, withinPath = SUBJECT_PATH)
	void test_typeAnnotationAllowedMavenArchunitAspectJ() {
		TypeAnnotationCastMain.castToText("text");
	}

	/**
	 * Runs the annotated cast under the forbidden policy in
	 * MavenArchunitInstrumentation.
	 */
	@Disabled(SUBJECT_PROBE_REASON)
	@PublicTest
	@Policy(value = FORBIDDEN_POLICY_ARCHUNITINSTRUMENTATION, withinPath = SUBJECT_PATH)
	void test_typeAnnotationForbiddenMavenArchunitInstrumentation() {
		TypeAnnotationCastMain.castToText("text");
	}

	/**
	 * Runs the annotated cast under the allowed policy in
	 * MavenArchunitInstrumentation.
	 */
	@Disabled(SUBJECT_PROBE_REASON)
	@PublicTest
	@Policy(value = ALLOWED_POLICY_ARCHUNITINSTRUMENTATION, withinPath = SUBJECT_PATH)
	void test_typeAnnotationAllowedMavenArchunitInstrumentation() {
		TypeAnnotationCastMain.castToText("text");
	}

	/**
	 * Runs the annotated cast under the forbidden policy in MavenWalaAspectJ.
	 */
	@Disabled(SUBJECT_PROBE_REASON)
	@PublicTest
	@Policy(value = FORBIDDEN_POLICY_WALAASPECTJ, withinPath = SUBJECT_PATH)
	void test_typeAnnotationForbiddenMavenWalaAspectJ() {
		TypeAnnotationCastMain.castToText("text");
	}

	/**
	 * Runs the annotated cast under the allowed policy in MavenWalaAspectJ.
	 */
	@Disabled(SUBJECT_PROBE_REASON)
	@PublicTest
	@Policy(value = ALLOWED_POLICY_WALAASPECTJ, withinPath = SUBJECT_PATH)
	void test_typeAnnotationAllowedMavenWalaAspectJ() {
		TypeAnnotationCastMain.castToText("text");
	}

	/**
	 * Runs the annotated cast under the forbidden policy in
	 * MavenWalaInstrumentation.
	 */
	@Disabled(SUBJECT_PROBE_REASON)
	@PublicTest
	@Policy(value = FORBIDDEN_POLICY_WALAINSTRUMENTATION, withinPath = SUBJECT_PATH)
	void test_typeAnnotationForbiddenMavenWalaInstrumentation() {
		TypeAnnotationCastMain.castToText("text");
	}

	/**
	 * Runs the annotated cast under the allowed policy in MavenWalaInstrumentation.
	 */
	@Disabled(SUBJECT_PROBE_REASON)
	@PublicTest
	@Policy(value = ALLOWED_POLICY_WALAINSTRUMENTATION, withinPath = SUBJECT_PATH)
	void test_typeAnnotationAllowedMavenWalaInstrumentation() {
		TypeAnnotationCastMain.castToText("text");
	}
}
