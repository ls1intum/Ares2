package de.tum.cit.ase.ares.api.internal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;

import org.junit.jupiter.api.Test;

import de.tum.cit.ase.ares.api.Policy;
import de.tum.cit.ase.ares.api.PrivilegedExceptionsOnly;
import de.tum.cit.ase.ares.api.context.TestContext;

class ConfigurationUtilsTest {

	private static final String POLICY_ENABLED = "src/test/resources/de/tum/cit/ase/ares/api/internal/configurationUtils/PolicyPrivilegedExceptionsEnabled.yaml";
	private static final String POLICY_DISABLED = "src/test/resources/de/tum/cit/ase/ares/api/internal/configurationUtils/PolicyPrivilegedExceptionsDisabled.yaml";
	private static final String POLICY_WITHOUT_BEHAVIOR = "src/test/resources/de/tum/cit/ase/ares/api/internal/configurationUtils/PolicyWithoutTestBehavior.yaml";

	static class PlainFixture {
		void test() {
		}
	}

	@PrivilegedExceptionsOnly("Annotation message")
	static class AnnotationOnlyFixture {
		void test() {
		}
	}

	@Policy(value = POLICY_ENABLED)
	static class PolicyEnabledFixture {
		void test() {
		}
	}

	@Policy(value = POLICY_DISABLED)
	static class PolicyDisabledFixture {
		void test() {
		}
	}

	@Policy(value = POLICY_WITHOUT_BEHAVIOR)
	static class PolicyWithoutBehaviorFixture {
		void test() {
		}
	}

	@Policy(value = POLICY_ENABLED, activated = false)
	static class PolicyInactiveFixture {
		void test() {
		}
	}

	@PrivilegedExceptionsOnly("Annotation message")
	@Policy(value = POLICY_ENABLED)
	static class AnnotationAndPolicyEnabledFixture {
		void test() {
		}
	}

	@PrivilegedExceptionsOnly("Annotation message")
	@Policy(value = POLICY_DISABLED)
	static class AnnotationAndPolicyDisabledFixture {
		void test() {
		}
	}

	@PrivilegedExceptionsOnly("Class message")
	static class ClassAndMethodAnnotationFixture {
		@PrivilegedExceptionsOnly("Method message")
		void methodWithOwnAnnotation() {
		}

		void methodWithoutOwnAnnotation() {
		}
	}

	@PrivilegedExceptionsOnly("Enclosing message")
	static class EnclosingClassFixture {
		static class Inner {
			void test() {
			}
		}
	}

	@Test
	void neitherAnnotationNorPolicyReturnsEmpty() throws Exception {
		Optional<String> message = ConfigurationUtils.getNonprivilegedFailureMessage(context(PlainFixture.class));

		assertFalse(message.isPresent());
	}

	@Test
	void annotationOnlyReturnsAnnotationMessage() throws Exception {
		Optional<String> message = ConfigurationUtils
				.getNonprivilegedFailureMessage(context(AnnotationOnlyFixture.class));

		assertTrue(message.isPresent());
		assertEquals("Annotation message", message.get());
	}

	@Test
	void policyEnabledOnlyReturnsPolicyMessage() throws Exception {
		Optional<String> message = ConfigurationUtils
				.getNonprivilegedFailureMessage(context(PolicyEnabledFixture.class));

		assertTrue(message.isPresent());
		assertEquals("Policy message", message.get());
	}

	@Test
	void policyDisabledOnlyReturnsEmpty() throws Exception {
		Optional<String> message = ConfigurationUtils
				.getNonprivilegedFailureMessage(context(PolicyDisabledFixture.class));

		assertFalse(message.isPresent());
	}

	@Test
	void policyWithoutBehaviorWrapperReturnsEmpty() throws Exception {
		Optional<String> message = ConfigurationUtils
				.getNonprivilegedFailureMessage(context(PolicyWithoutBehaviorFixture.class));

		assertFalse(message.isPresent());
	}

	@Test
	void inactivePolicyNeverActivatesTheBehaviouralDefault() throws Exception {
		Optional<String> message = ConfigurationUtils
				.getNonprivilegedFailureMessage(context(PolicyInactiveFixture.class));

		assertFalse(message.isPresent());
	}

	@Test
	void annotationWinsOverPolicyEnabled() throws Exception {
		Optional<String> message = ConfigurationUtils
				.getNonprivilegedFailureMessage(context(AnnotationAndPolicyEnabledFixture.class));

		assertTrue(message.isPresent());
		assertEquals("Annotation message", message.get());
	}

	@Test
	void annotationStillWinsOverPolicyDisabled() throws Exception {
		Optional<String> message = ConfigurationUtils
				.getNonprivilegedFailureMessage(context(AnnotationAndPolicyDisabledFixture.class));

		assertTrue(message.isPresent());
		assertEquals("Annotation message", message.get());
	}

	@Test
	void methodAnnotationOverridesClassLevel() throws Exception {
		Optional<String> message = ConfigurationUtils.getNonprivilegedFailureMessage(
				context(ClassAndMethodAnnotationFixture.class, "methodWithOwnAnnotation"));

		assertTrue(message.isPresent());
		assertEquals("Method message", message.get());
	}

	@Test
	void classLevelAnnotationAppliesWhenMethodHasNone() throws Exception {
		Optional<String> message = ConfigurationUtils.getNonprivilegedFailureMessage(
				context(ClassAndMethodAnnotationFixture.class, "methodWithoutOwnAnnotation"));

		assertTrue(message.isPresent());
		assertEquals("Class message", message.get());
	}

	@Test
	void enclosingClassAnnotationAppliesToInnerClassTest() throws Exception {
		Optional<String> message = ConfigurationUtils
				.getNonprivilegedFailureMessage(context(EnclosingClassFixture.Inner.class));

		assertTrue(message.isPresent());
		assertEquals("Enclosing message", message.get());
	}

	private static TestContext context(Class<?> type) throws Exception {
		return context(type, "test");
	}

	private static TestContext context(Class<?> type, String methodName) throws Exception {
		return TestContextFixtures.of(type, methodName);
	}
}
