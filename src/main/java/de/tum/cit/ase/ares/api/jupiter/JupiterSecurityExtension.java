package de.tum.cit.ase.ares.api.jupiter;

import static de.tum.cit.ase.ares.api.aop.java.instrumentation.advice.JavaInstrumentationAdviceFileSystemToolbox.localize;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.Optional;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.junit.jupiter.api.extension.*;
import org.junit.platform.commons.function.Try;

import de.tum.cit.ase.ares.api.Policy;
import de.tum.cit.ase.ares.api.aop.java.instrumentation.JavaInstrumentationAgent;
import de.tum.cit.ase.ares.api.context.TestContextUtils;
import de.tum.cit.ase.ares.api.policy.SecurityPolicyReaderAndDirector;

@API(status = Status.INTERNAL)
public class JupiterSecurityExtension
		implements UnifiedInvocationInterceptor, BeforeTestExecutionCallback, AfterTestExecutionCallback {
	private static final ExtensionContext.Namespace NAMESPACE = ExtensionContext.Namespace
			.create(JupiterSecurityExtension.class);
	private static final String POLICY_PREPARED_KEY = "policy-prepared";
	/**
	 * Store key for the guard state of class-level callbacks. It differs from the
	 * method-level key because a store lookup also reads the parent's entries, and
	 * a method must never see a state that belongs to the class.
	 */
	private static final String CLASS_POLICY_PREPARED_KEY = "class-policy-prepared";

	private enum LifecycleState {
		PREPARING,
		PREPARED,
		CLOSED
	}

	// <editor-fold desc="Lifecycle Callbacks">

	/**
	 * Arms the guard before a setup method of a test method runs and leaves it
	 * armed for the test, so student code reached from {@code @BeforeEach} is
	 * restricted. A failure or abort closes it again, because the callback that
	 * normally does so is then skipped.
	 */
	@Override
	public void interceptBeforeEachMethod(Invocation<Void> invocation,
			ReflectiveInvocationContext<Method> invocationContext, ExtensionContext extensionContext) throws Throwable {
		proceedArmedUntilTestEnds(invocation, extensionContext);
	}

	/**
	 * Arms the guard while the test class constructor runs and closes it
	 * afterwards, so a student constructor reached from a field initialiser or the
	 * constructor is restricted. JUnit hands this callback the class context even
	 * for a test instance created per method, so the guard is closed again and
	 * armed anew for the test. A policy on the class is needed for this to take
	 * effect, because a policy on a test method is not known yet.
	 */
	@Override
	public <T> T interceptTestClassConstructor(Invocation<T> invocation,
			ReflectiveInvocationContext<java.lang.reflect.Constructor<T>> invocationContext,
			ExtensionContext extensionContext) throws Throwable {
		return interceptGenericInvocation(invocation, extensionContext, Optional.of(invocationContext));
	}

	/**
	 * Arms the guard while a {@code @BeforeAll} method runs and closes it
	 * afterwards. This only happens when Ares is registered for the class, for
	 * example by a class-level {@code @Public}, and the class carries a
	 * {@code @Policy}.
	 */
	@Override
	public void interceptBeforeAllMethod(Invocation<Void> invocation,
			ReflectiveInvocationContext<Method> invocationContext, ExtensionContext extensionContext) throws Throwable {
		interceptGenericInvocation(invocation, extensionContext, Optional.of(invocationContext));
	}

	/**
	 * Arms the guard again while an {@code @AfterEach} method runs, because the
	 * test has already closed it, and closes it afterwards.
	 */
	@Override
	public void interceptAfterEachMethod(Invocation<Void> invocation,
			ReflectiveInvocationContext<Method> invocationContext, ExtensionContext extensionContext) throws Throwable {
		interceptGenericInvocation(invocation, extensionContext, Optional.of(invocationContext));
	}

	/**
	 * Arms the guard while an {@code @AfterAll} method runs and closes it
	 * afterwards.
	 */
	@Override
	public void interceptAfterAllMethod(Invocation<Void> invocation,
			ReflectiveInvocationContext<Method> invocationContext, ExtensionContext extensionContext) throws Throwable {
		interceptGenericInvocation(invocation, extensionContext, Optional.of(invocationContext));
	}

	/**
	 * Arms the guard, runs the invocation and leaves the guard armed for the test
	 * that follows. It closes the guard only when the invocation throws, because
	 * the callback that usually closes it is not reached then.
	 */
	private void proceedArmedUntilTestEnds(Invocation<Void> invocation, ExtensionContext extensionContext)
			throws Throwable {
		prepareSecurityOnce(extensionContext);
		try {
			invocation.proceed();
		} catch (Throwable failure) {
			try {
				closeSecurityOnce(extensionContext);
			} catch (Throwable closeFailure) {
				failure.addSuppressed(closeFailure);
			}
			throw failure;
		}
	}

	/**
	 * Sets up the security policy before each test method execution.
	 * <p>
	 * This callback is used as a reliable alternative to
	 * {@link InvocationInterceptor#interceptTestTemplateMethod} and
	 * {@link InvocationInterceptor#interceptTestMethod}, which are not invoked when
	 * tests are executed via {@code EngineTestKit} (e.g., in meta-tests). Lifecycle
	 * callbacks like {@link BeforeTestExecutionCallback} are reliably called in all
	 * execution contexts.
	 */
	@Override
	public void beforeTestExecution(ExtensionContext extensionContext) throws Exception {
		prepareSecurityOnce(extensionContext);
	}

	private void prepareSecurityOnce(ExtensionContext extensionContext) {
		ExtensionContext.Store store = extensionContext.getStore(NAMESPACE);
		String stateKey = stateKey(extensionContext);
		synchronized (store) {
			LifecycleState state = store.get(stateKey, LifecycleState.class);
			if (state == LifecycleState.PREPARED || state == LifecycleState.PREPARING) {
				return;
			}
			store.put(stateKey, LifecycleState.PREPARING);
		}
		try {
			resetSettingsInStandardClassLoader();
			resetSettingsInBootstrapClassLoader();
			prepareSecurity(extensionContext);
			store.put(stateKey, LifecycleState.PREPARED);
		} catch (RuntimeException | Error failure) {
			store.remove(stateKey);
			try {
				resetSettingsInStandardClassLoader();
				resetSettingsInBootstrapClassLoader();
			} catch (RuntimeException | Error resetFailure) {
				failure.addSuppressed(resetFailure);
			}
			throw failure;
		}
	}

	void prepareSecurity(ExtensionContext extensionContext) {
		JupiterContext testContext = JupiterContext.of(extensionContext);
		Optional<Policy> policyOpt = TestContextUtils.findAnnotationIn(testContext, Policy.class);
		boolean hasPolicyAnnotation = policyOpt.isPresent();
		boolean isAresActivated = policyOpt.map(Policy::activated).orElse(true);
		boolean isTestMethodPresent = testContext.testMethod().isPresent();

		if (isAresActivated && (hasPolicyAnnotation || isTestMethodPresent)) {
			Path policyPath = policyOpt.filter(p -> !p.value().isBlank())
					.map(JupiterSecurityExtension::testAndGetPolicyValue).orElse(null);
			Path withinPath = policyOpt.filter(p -> !p.withinPath().isBlank())
					.map(JupiterSecurityExtension::testAndGetPolicyWithinPath).orElse(Path.of(""));
			SecurityPolicyReaderAndDirector.builder().securityPolicyFilePath(policyPath)
					.projectFolderPath(Path.of("").toAbsolutePath()).withinPath(withinPath).build().createTestCases()
					.executeTestCases();
		}
	}

	/**
	 * Resets the security settings after each test method execution.
	 */
	@Override
	public void afterTestExecution(ExtensionContext extensionContext) throws Exception {
		closeSecurityOnce(extensionContext);
	}

	private static void closeSecurityOnce(ExtensionContext extensionContext) {
		ExtensionContext.Store store = extensionContext.getStore(NAMESPACE);
		String stateKey = stateKey(extensionContext);
		synchronized (store) {
			LifecycleState state = store.get(stateKey, LifecycleState.class);
			if (state == null || state == LifecycleState.CLOSED) {
				return;
			}
			store.put(stateKey, LifecycleState.CLOSED);
		}
		try {
			JavaInstrumentationAgent.throwIfTransformationFailed();
		} finally {
			resetSettingsInStandardClassLoader();
			resetSettingsInBootstrapClassLoader();
			store.remove(stateKey);
		}
	}

	/**
	 * Picks the store key that matches the level of the given context.
	 */
	private static String stateKey(ExtensionContext extensionContext) {
		return extensionContext.getTestMethod().isPresent() ? POLICY_PREPARED_KEY : CLASS_POLICY_PREPARED_KEY;
	}

	// </editor-fold>

	@Override
	public <T> T interceptGenericInvocation(Invocation<T> invocation, ExtensionContext extensionContext,
			Optional<ReflectiveInvocationContext<?>> invocationContext) throws Throwable {
		prepareSecurityOnce(extensionContext);
		T result = null;
		Throwable failure = null;
		try {
			result = invocation.proceed();
		} catch (Throwable t) {
			failure = t;
		} finally {
			try {
				closeSecurityOnce(extensionContext);
			} catch (Throwable teardownFailure) {
				if (failure == null) {
					throw teardownFailure;
				}
				failure.addSuppressed(teardownFailure);
			}
		}
		if (failure != null) {
			throw failure;
		}
		return result;
	}

	public static void resetSettings(Class<?> javaTestCaseSettingsClass) {
		try {
			Method resetMethod = javaTestCaseSettingsClass.getDeclaredMethod("reset");
			resetMethod.setAccessible(true);
			resetMethod.invoke(null);
			resetMethod.setAccessible(false);
		} catch (NoSuchMethodException e) {
			throw new SecurityException(localize("security.settings.reset.method.not.found"), e);
		} catch (IllegalAccessException e) {
			throw new SecurityException(localize("security.settings.reset.access.denied"), e);
		} catch (InvocationTargetException e) {
			throw new SecurityException(localize("security.settings.error.within.method"), e);
		}
	}

	/**
	 * Resets the settings in the standard (context) ClassLoader.
	 */
	public static void resetSettingsInStandardClassLoader() {
		String className = "de.tum.cit.ase.ares.api.aop.java.JavaAOPTestCaseSettings";
		Try.call(() -> Class.forName(className, true, Thread.currentThread().getContextClassLoader()))
				.ifSuccess(JupiterSecurityExtension::resetSettings);
	}

	/**
	 * Attempts to reset the settings in the Bootstrap ClassLoader. This method
	 * silently fails if the class is not yet loaded in the Bootstrap ClassLoader,
	 * which happens before any instrumented code runs.
	 */
	public static void resetSettingsInBootstrapClassLoader() {
		try {
			// Use null as ClassLoader to load from Bootstrap ClassLoader
			// Use false to avoid class initialization
			Class<?> settingsClass = Class.forName("de.tum.cit.ase.ares.api.aop.java.JavaAOPTestCaseSettings", false,
					null);
			resetSettingsInClass(settingsClass);
		} catch (ClassNotFoundException e) {
			// Class not yet loaded in the bootstrap class loader: there is nothing to
			// reset, which is the only benign reason this can fail.
		}
	}

	static void resetSettingsInClass(Class<?> settingsClass) {
		try {
			resetSettingsWithMethod(settingsClass.getDeclaredMethod("reset"));
		} catch (NoSuchMethodException e) {
			// The class is present but its reset failed. Fail closed rather than let the
			// next test inherit stale security settings, matching resetSettings for the
			// standard class loader.
			throw new SecurityException(localize("security.settings.reset.method.not.found"), e);
		}
	}

	static void resetSettingsWithMethod(Method resetMethod) {
		try {
			resetMethod.invoke(null);
		} catch (IllegalAccessException e) {
			throw new SecurityException(localize("security.settings.reset.access.denied"), e);
		} catch (InvocationTargetException e) {
			throw new SecurityException(localize("security.settings.error.within.method"), e);
		}
	}

	public static Path testAndGetPolicyValue(Policy policyAnnotation) {
		String policyValue = policyAnnotation.value();
		if (policyValue.isBlank()) {
			throw new SecurityException(localize("security.policy.reader.path.blank"));
		}
		try {
			Path policyPath = Path.of(policyValue);
			if (!policyPath.toFile().exists()) {
				throw new SecurityException(localize("security.policy.reader.path.not.exists", policyPath));
			}
			return policyPath;
		} catch (InvalidPathException e) {
			throw new SecurityException(localize("security.policy.reader.path.invalid", policyValue));
		}
	}

	public static Path testAndGetPolicyWithinPath(Policy policyAnnotation) {
		String policyValue = policyAnnotation.withinPath();
		try {
			Path policyWithinPath = Path.of(policyValue);
			if (!policyWithinPath.startsWith("classes") && !policyWithinPath.startsWith("test-classes")) {
				throw new SecurityException(localize("security.policy.within.path.wrong.bytecode.path", policyValue));
			}
			return policyWithinPath;
		} catch (InvalidPathException e) {
			throw new SecurityException(localize("security.policy.within.path.invalid", policyValue));
		}
	}
}
