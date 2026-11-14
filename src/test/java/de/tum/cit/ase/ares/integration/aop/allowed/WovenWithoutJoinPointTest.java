package de.tum.cit.ase.ares.integration.aop.allowed;

import static org.junit.jupiter.api.Assertions.assertEquals;

import de.tum.cit.ase.ares.api.Policy;
import de.tum.cit.ase.ares.api.jupiter.PublicTest;
import de.tum.cit.ase.ares.integration.aop.allowed.subject.weaving.WovenWithoutJoinPoint;

/**
 * Checks that a correctly woven exercise whose code calls nothing the Ares
 * aspects intercept is not refused as unwoven, in all four mode combinations.
 */
class WovenWithoutJoinPointTest {

	/** The compiled subject the policies supervise. */
	private static final String WITHIN_PATH = "test-classes/de/tum/cit/ase/ares/integration/aop/allowed/subject/weaving";

	/** The policy for archunit with aspectj. */
	private static final String ARCHUNIT_ASPECTJ_POLICY = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/aspectj/PolicyOnePathAllowedRead.yaml";

	/** The policy for archunit with instrumentation. */
	private static final String ARCHUNIT_INSTRUMENTATION_POLICY = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/instrumentation/PolicyOnePathAllowedRead.yaml";

	/** The policy for wala with aspectj. */
	private static final String WALA_ASPECTJ_POLICY = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/aspectj/PolicyOnePathAllowedRead.yaml";

	/** The policy for wala with instrumentation. */
	private static final String WALA_INSTRUMENTATION_POLICY = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/wala/instrumentation/PolicyOnePathAllowedRead.yaml";

	/**
	 * Checks that the woven class without advice runs under archunit with aspectj.
	 */
	@PublicTest
	@Policy(value = ARCHUNIT_ASPECTJ_POLICY, withinPath = WITHIN_PATH)
	void test_aWovenClassWithoutAdviceRunsMavenArchunitAspectJ() {
		assertEquals(5, WovenWithoutJoinPoint.add(2, 3));
	}

	/**
	 * Checks that the woven class without advice runs under archunit with
	 * instrumentation.
	 */
	@PublicTest
	@Policy(value = ARCHUNIT_INSTRUMENTATION_POLICY, withinPath = WITHIN_PATH)
	void test_aWovenClassWithoutAdviceRunsMavenArchunitInstrumentation() {
		assertEquals(5, WovenWithoutJoinPoint.add(2, 3));
	}

	/** Checks that the woven class without advice runs under wala with aspectj. */
	@PublicTest
	@Policy(value = WALA_ASPECTJ_POLICY, withinPath = WITHIN_PATH)
	void test_aWovenClassWithoutAdviceRunsMavenWalaAspectJ() {
		assertEquals(5, WovenWithoutJoinPoint.add(2, 3));
	}

	/**
	 * Checks that the woven class without advice runs under wala with
	 * instrumentation.
	 */
	@PublicTest
	@Policy(value = WALA_INSTRUMENTATION_POLICY, withinPath = WITHIN_PATH)
	void test_aWovenClassWithoutAdviceRunsMavenWalaInstrumentation() {
		assertEquals(5, WovenWithoutJoinPoint.add(2, 3));
	}
}
