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

/**
 * Arms the security guard for a test method and for the constructor,
 * {@code @BeforeEach}, {@code @AfterEach}, {@code @BeforeAll} and
 * {@code @AfterAll} code around it. The class phases are guarded only if the
 * class itself carries {@code @Policy}. A per-method test instance is built
 * under the policy of its test method.
 */
@API(status = Status.INTERNAL)
public class JupiterSecurityExtension implements UnifiedInvocationInterceptor, TestInstantiationAwareExtension,
		BeforeTestExecutionCallback, AfterTestExecutionCallback, AfterEachCallback {
	/** Namespace of the guard state in the extension store. */
	private static final ExtensionContext.Namespace NAMESPACE = ExtensionContext.Namespace
			.create(JupiterSecurityExtension.class);
	/** Store key for the guard state of a test method. */
	private static final String POLICY_PREPARED_KEY = "policy-prepared";
	/**
	 * Store key for the guard state of callbacks that run without a test method. It
	 * differs from the method key because a store lookup also reads the parent's
	 * entries, and a method must never see a state of the class.
	 */
	private static final String CLASS_POLICY_PREPARED_KEY = "class-policy-prepared";

	private enum LifecycleState {
		PREPARING,
		PREPARED,
		CLOSED
	}

	// <editor-fold desc="Lifecycle Callbacks">

	/**
	 * Makes JUnit hand the constructor interception the context of the test method,
	 * so the policy on that method is known when a per-method test instance is
	 * created. A shared instance is still built under the class context.
	 */
	@Override
	public ExtensionContextScope getTestInstantiationExtensionContextScope(ExtensionContext rootContext) {
		return ExtensionContextScope.TEST_METHOD;
	}

	/**
	 * Arms the guard before a setup method runs and leaves it armed for the test. A
	 * failure or abort closes it again, because the callback that normally does so
	 * is then skipped.
	 */
	@Override
	public void interceptBeforeEachMethod(Invocation<Void> invocation,
			ReflectiveInvocationContext<Method> invocationContext, ExtensionContext extensionContext) throws Throwable {
		proceedArmedUntilTestEnds(invocation, extensionContext);
	}

	/**
	 * Arms the guard while the constructor of a test class runs and closes it
	 * afterwards. For a per-method instance the policy is that of the test method.
	 * JUnit builds the instance before it decides to skip the test, so a failure to
	 * arm the guard here is left for the test itself. A shared instance is built
	 * under the class context, so the guard is armed only for a class with a
	 * policy.
	 */
	@Override
	public <T> T interceptTestClassConstructor(Invocation<T> invocation,
			ReflectiveInvocationContext<java.lang.reflect.Constructor<T>> invocationContext,
			ExtensionContext extensionContext) throws Throwable {
		if (extensionContext.getTestMethod().isPresent()) {
			try {
				prepareSecurityOnce(extensionContext);
			} catch (RuntimeException armingFailure) {
				return invocation.proceed();
			}
		}
		return interceptGenericInvocation(invocation, extensionContext, Optional.of(invocationContext));
	}

	/**
	 * Arms the guard, runs the invocation and leaves the guard armed for the test
	 * that follows. It closes the guard only when the invocation throws, because
	 * the callback that usually closes it is not reached then.
	 */
	private <T> T proceedArmedUntilTestEnds(Invocation<T> invocation, ExtensionContext extensionContext)
			throws Throwable {
		prepareSecurityOnce(extensionContext);
		try {
			return invocation.proceed();
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

	/**
	 * Reads the policy and arms the guard once for the given context. A second call
	 * for the same context does nothing, and a failure leaves the guard reset.
	 */
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

	/**
	 * Closes the guard if nothing else did, for example when an interceptor outside
	 * this extension failed a setup method after it had returned. JUnit calls this
	 * for every test, even when {@link #afterTestExecution} was skipped.
	 */
	@Override
	public void afterEach(ExtensionContext extensionContext) throws Exception {
		closeSecurityOnce(extensionContext);
	}

	/**
	 * Resets the guard once for the given context. It also reports a failed class
	 * transformation of the instrumentation agent.
	 */
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
