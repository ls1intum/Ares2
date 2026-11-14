package de.tum.cit.ase.ares.api.jupiter;

import static de.tum.cit.ase.ares.api.internal.ReportingUtils.doProceedAndPostProcess;
import static de.tum.cit.ase.ares.api.internal.TestGuardUtils.checkForHidden;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Optional;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.junit.jupiter.api.extension.*;

import de.tum.cit.ase.ares.api.Deadline;
import de.tum.cit.ase.ares.api.internal.ReportingUtils;

/**
 * This class' main purpose is to guard the {@link HiddenTest}s execution and
 * evaluate the {@link Deadline}.
 *
 * @author Christian Femers
 */
@API(status = Status.INTERNAL)
public final class JupiterTestGuard implements UnifiedInvocationInterceptor {
	@Override
	public <T> T interceptTestClassConstructor(Invocation<T> invocation,
			ReflectiveInvocationContext<Constructor<T>> invocationContext, ExtensionContext extensionContext)
			throws Throwable {
		return invocation.proceed();
	}

	@Override
	public void interceptBeforeAllMethod(Invocation<Void> invocation,
			ReflectiveInvocationContext<Method> invocationContext, ExtensionContext extensionContext) throws Throwable {
		invocation.proceed();
	}

	@Override
	public void interceptBeforeEachMethod(Invocation<Void> invocation,
			ReflectiveInvocationContext<Method> invocationContext, ExtensionContext extensionContext) throws Throwable {
		invocation.proceed();
	}

	@Override
	public void interceptAfterEachMethod(Invocation<Void> invocation,
			ReflectiveInvocationContext<Method> invocationContext, ExtensionContext extensionContext) throws Throwable {
		invocation.proceed();
	}

	@Override
	public void interceptAfterAllMethod(Invocation<Void> invocation,
			ReflectiveInvocationContext<Method> invocationContext, ExtensionContext extensionContext) throws Throwable {
		invocation.proceed();
	}

	@Override
	public <T> T interceptGenericInvocation(Invocation<T> invocation, ExtensionContext extensionContext,
			Optional<ReflectiveInvocationContext<?>> invocationContext) throws Throwable {
		JupiterContext jupiterContext = JupiterContext.of(extensionContext);
		checkForHidden(jupiterContext);
		return doProceedAndPostProcess(invocation, jupiterContext);
	}

	/**
	 * Reports a failure of a test's constructor or lifecycle method as the policy's
	 * failure feedback allows, before Jupiter receives it: a blocked action at the
	 * feedback level, and, when only privileged failures are reported, any other
	 * failure except an aborted test as the policy's message.
	 *
	 * @param context the invocation's context.
	 * @param failure the failure.
	 * @return the failure to throw instead.
	 */
	static Throwable invocationFailure(ExtensionContext context, Throwable failure) {
		return ReportingUtils.processLifecycleFailure(failure, JupiterContext.of(context));
	}

	/**
	 * Applies the policy's feedback level to a blocked action that fails a JUnit
	 * callback, before Jupiter receives it.
	 *
	 * @param context the callback's context.
	 * @param failure the failure.
	 * @return the failure to throw instead.
	 */
	static RuntimeException callbackFailure(ExtensionContext context, Throwable failure) {
		Throwable safe = ReportingUtils.processBlockedAction(failure, JupiterContext.of(context));
		if (safe instanceof Error error) {
			throw error;
		}
		return (RuntimeException) safe;
	}
}
