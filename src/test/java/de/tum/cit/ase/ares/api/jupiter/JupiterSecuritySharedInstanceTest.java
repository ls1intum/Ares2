package de.tum.cit.ase.ares.api.jupiter;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.lang.reflect.Constructor;
import java.util.Locale;
import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.InvocationInterceptor.Invocation;
import org.junit.jupiter.api.extension.ReflectiveInvocationContext;

import de.tum.cit.ase.ares.api.Policy;
import de.tum.cit.ase.ares.api.aop.java.instrumentation.advice.AresSecurityViolation;
import de.tum.cit.ase.ares.testutilities.MockStores;

/**
 * Checks that a blocked action raised while Ares arms its checks for a shared
 * test instance reaches JUnit at the policy's feedback level, like one raised
 * for a per-test instance, and that the constructor then does not run.
 */
class JupiterSecuritySharedInstanceTest {

	/** The policy at MINIMIZED the fixture class names. */
	private static final String MINIMIZED_POLICY = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/aspectj/PolicyFeedbackLevelMINIMIZED.yaml";

	/** The MINIMIZED text, in English. */
	private static final String MINIMIZED = "Ares Security Error (Reason: Student-Code; Stage: Execution): An operation was blocked by Ares.";

	/** The locale before the test. */
	private final Locale locale = Locale.getDefault();

	/** Restores the locale. */
	@AfterEach
	void restoreLocale() {
		Locale.setDefault(locale);
	}

	/**
	 * A blocked action while arming for a shared instance shows the MINIMIZED text,
	 * and the constructor does not run.
	 *
	 * @throws Throwable if the interception fails otherwise
	 */
	@Test
	@SuppressWarnings("unchecked")
	void armingAFailedSharedInstanceFollowsTheFeedbackLevel() throws Throwable {
		Locale.setDefault(Locale.ENGLISH);
		SecurityException violation = violation();
		JupiterSecurityExtension extension = new JupiterSecurityExtension() {
			@Override
			void prepareSecurity(ExtensionContext context) {
				throw violation;
			}
		};
		Invocation<Object> invocation = mock(Invocation.class);
		ReflectiveInvocationContext<Constructor<Object>> invocationContext = mock(ReflectiveInvocationContext.class);

		assertThatThrownBy(
				() -> extension.interceptTestClassConstructor(invocation, invocationContext, sharedInstanceContext()))
						.isInstanceOf(SecurityException.class).hasMessage(MINIMIZED);
		verify(invocation, never()).proceed();
	}

	/**
	 * Creates a blocked action the way Ares's checks do.
	 *
	 * @return the violation.
	 * @throws ReflectiveOperationException if it cannot be created
	 */
	private static SecurityException violation() throws ReflectiveOperationException {
		Constructor<AresSecurityViolation> constructor = AresSecurityViolation.class
				.getDeclaredConstructor(String.class, String.class, String.class, String.class);
		constructor.setAccessible(true);
		return constructor.newInstance("file", "read", "secret.txt",
				"Ares Security Error (Reason: Student-Code; Stage: Execution): Fixture tried to illegally read secret.txt");
	}

	/**
	 * The class context JUnit gives a shared instance's constructor: no test
	 * method, and a working store.
	 *
	 * @return the context.
	 */
	private static ExtensionContext sharedInstanceContext() {
		ExtensionContext context = mock(ExtensionContext.class);
		ExtensionContext.Store store = MockStores.workingStore();
		when(context.getStore(any())).thenReturn(store);
		when(context.getTestClass()).thenReturn(Optional.of(SharedFixture.class));
		when(context.getRequiredTestClass()).thenAnswer(call -> SharedFixture.class);
		when(context.getTestMethod()).thenReturn(Optional.empty());
		when(context.getElement()).thenReturn(Optional.of(SharedFixture.class));
		when(context.getParent()).thenReturn(Optional.empty());
		when(context.getConfigurationParameter(any(), any())).thenReturn(Optional.empty());
		return context;
	}

	/** A test class with a class-level policy at MINIMIZED. */
	@Policy(value = MINIMIZED_POLICY, withinPath = "test-classes/de/tum/cit/ase/ares/integration/testuser/subject")
	static final class SharedFixture {
	}
}
