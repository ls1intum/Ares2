package de.tum.cit.ase.ares.api.jupiter;

import static de.tum.cit.ase.ares.api.internal.ReportingUtils.doProceedAndPostProcess;
import static de.tum.cit.ase.ares.api.internal.ReportingUtils.redactHiddenLifecycleFailure;
import static de.tum.cit.ase.ares.api.internal.TestGuardUtils.checkForHidden;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Optional;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.junit.jupiter.api.extension.*;

import de.tum.cit.ase.ares.api.Deadline;
import de.tum.cit.ase.ares.api.context.TestType;

/**
 * This class' main purpose is to guard the {@link HiddenTest}s execution and
 * evaluate the {@link Deadline}.
 *
 * @author Christian Femers
 */
@API(status = Status.INTERNAL)
public final class JupiterTestGuard implements UnifiedInvocationInterceptor, BeforeEachCallback, AfterEachCallback {

	/** Store namespace for the hidden stream capture. */
	private static final ExtensionContext.Namespace CAPTURE_NAMESPACE = ExtensionContext.Namespace
			.create(JupiterTestGuard.class);
	/** Store key for the capture installed before test callbacks. */
	private static final String CAPTURE_KEY = "hiddenOutputCapture"; //$NON-NLS-1$

	/** Starts capture before I/O managers and other test callbacks run. */
	@Override
	public void beforeEach(ExtensionContext context) {
		if (isHidden(context)) {
			context.getStore(CAPTURE_NAMESPACE).put(CAPTURE_KEY, HiddenOutputCapture.start());
		}
	}

	/** Restores the streams after I/O managers and other callbacks complete. */
	@Override
	public void afterEach(ExtensionContext context) {
		HiddenOutputCapture capture = context.getStore(CAPTURE_NAMESPACE).remove(CAPTURE_KEY,
				HiddenOutputCapture.class);
		if (capture != null) {
			capture.close();
		}
	}

	/** Hides a hidden test constructor's output and failure details. */
	@Override
	public <T> T interceptTestClassConstructor(Invocation<T> invocation,
			ReflectiveInvocationContext<Constructor<T>> invocationContext, ExtensionContext extensionContext)
			throws Throwable {
		return proceedWithLifecycleCapture(invocation, extensionContext);
	}

	/** Hides class setup output when the class includes hidden tests. */
	@Override
	public void interceptBeforeAllMethod(Invocation<Void> invocation,
			ReflectiveInvocationContext<Method> invocationContext, ExtensionContext extensionContext) throws Throwable {
		proceedWithLifecycleCapture(invocation, extensionContext);
	}

	/** Hides setup output for a hidden test. */
	@Override
	public void interceptBeforeEachMethod(Invocation<Void> invocation,
			ReflectiveInvocationContext<Method> invocationContext, ExtensionContext extensionContext) throws Throwable {
		proceedWithLifecycleCapture(invocation, extensionContext);
	}

	/** Hides teardown output for a hidden test. */
	@Override
	public void interceptAfterEachMethod(Invocation<Void> invocation,
			ReflectiveInvocationContext<Method> invocationContext, ExtensionContext extensionContext) throws Throwable {
		proceedWithLifecycleCapture(invocation, extensionContext);
	}

	/** Hides class teardown output when the class includes hidden tests. */
	@Override
	public void interceptAfterAllMethod(Invocation<Void> invocation,
			ReflectiveInvocationContext<Method> invocationContext, ExtensionContext extensionContext) throws Throwable {
		proceedWithLifecycleCapture(invocation, extensionContext);
	}

	/** Checks the deadline and reports a supervised test result. */
	@Override
	public <T> T interceptGenericInvocation(Invocation<T> invocation, ExtensionContext extensionContext,
			Optional<ReflectiveInvocationContext<?>> invocationContext) throws Throwable {
		JupiterContext jupiterContext = JupiterContext.of(extensionContext);
		checkForHidden(jupiterContext);
		return doProceedAndPostProcess(invocation, jupiterContext);
	}

	/**
	 * Runs a lifecycle phase with output capture if it belongs to a hidden test.
	 */
	private static <T> T proceedWithLifecycleCapture(Invocation<T> invocation, ExtensionContext context)
			throws Throwable {
		boolean hidden = isHidden(context);
		if (!hidden && !containsHiddenTest(context)) {
			return invocation.proceed();
		}
		HiddenOutputCapture capture = HiddenOutputCapture.start();
		try {
			if (hidden) {
				return doProceedAndPostProcess(invocation, JupiterContext.of(context));
			}
			try {
				return invocation.proceed();
			} catch (Throwable failure) {
				throw redactHiddenLifecycleFailure(failure);
			}
		} finally {
			capture.close();
		}
	}

	/** Tells whether the current Jupiter context belongs to a hidden test. */
	private static boolean isHidden(ExtensionContext context) {
		return JupiterContext.of(context).findTestType().orElse(null) == TestType.HIDDEN;
	}

	/** Finds a hidden method in a mixed test class for class lifecycle methods. */
	private static boolean containsHiddenTest(ExtensionContext context) {
		return context.getTestClass().map(JupiterTestGuard::hasHiddenMethodInHierarchy).orElse(false);
	}

	/** Finds a hidden method declared on this class or a parent test class. */
	private static boolean hasHiddenMethodInHierarchy(Class<?> type) {
		for (Class<?> current = type; current != null; current = current.getSuperclass()) {
			if (Arrays.stream(current.getDeclaredMethods())
					.anyMatch(method -> method.isAnnotationPresent(HiddenTest.class)
							|| method.isAnnotationPresent(Hidden.class))) {
				return true;
			}
		}
		return false;
	}
}
