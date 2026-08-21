package de.tum.cit.ase.ares.api.aop.java.aspectj.adviceandpointcut;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.lang.reflect.Method;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;

import de.tum.cit.ase.ares.api.StrictTimeout;
import de.tum.cit.ase.ares.api.context.TestContext;
import de.tum.cit.ase.ares.api.internal.TimeoutUtils;

/**
 * Regression test for the AspectJ backend's copy of the {@code @StrictTimeout}
 * exemption. The instrumentation backend carries the same logic in its own
 * class, and the two are kept in step by hand, so each needs its own test:
 * without this one a regression in the aspect would pass every other check,
 * since the self-tests reach this classifier only through the
 * restricted-package branch.
 */
class JavaAspectJThreadSystemAdviceDefinitionsTest {

	@Test
	void aStudentStackIsNotExemptedAsTimeoutMachineryWhenTheRestrictedPackageMatchesNothing() throws Throwable {
		// The @StrictTimeout worker runs the student's body inside
		// TimeoutUtils.rethrowThrowableSafe, so TimeoutUtils sits on the student's own
		// stack, below the student's frames. While the restricted package matched, the
		// student frame was always found first and this never showed. With a package
		// that matches nothing, accepting any TimeoutUtils frame exempted every thread
		// operation under a @StrictTimeout: enforcement switched itself off silently,
		// which is exactly what a mis-scoped supervised package produces.
		Method isThreadCreationFromAresTimeout = JavaAspectJThreadSystemAdviceDefinitions.class
				.getDeclaredMethod("isThreadCreationFromAresTimeout", String.class);
		isThreadCreationFromAresTimeout.setAccessible(true);

		Boolean exempted = TimeoutUtils.performTimeoutExecution(
				() -> (Boolean) isThreadCreationFromAresTimeout.invoke(null, "de.tum.cit.matches.nothing"),
				strictTimeoutContext());

		assertFalse(exempted, "a thread operation from the student's own body must never count as Ares' own");
	}

	private static TestContext strictTimeoutContext() throws NoSuchMethodException {
		Method target = JavaAspectJThreadSystemAdviceDefinitionsTest.class.getDeclaredMethod("strictTimeoutTarget");
		TestContext context = mock(TestContext.class);
		when(context.testMethod()).thenReturn(Optional.of(target));
		when(context.testClass()).thenReturn(Optional.of(JavaAspectJThreadSystemAdviceDefinitionsTest.class));
		return context;
	}

	@StrictTimeout(value = 30, unit = TimeUnit.SECONDS)
	private static void strictTimeoutTarget() {
		// Supplies the annotation the mocked test context reports, so that
		// performTimeoutExecution really goes through its worker rather than running
		// the supplier inline.
	}
}
