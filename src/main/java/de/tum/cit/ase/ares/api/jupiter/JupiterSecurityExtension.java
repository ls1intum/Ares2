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

	private enum LifecycleState {
		PREPARING,
		PREPARED,
		CLOSED
	}

	// <editor-fold desc="Lifecycle Callbacks">

	@Override
	public void interceptBeforeEachMethod(Invocation<Void> invocation,
			ReflectiveInvocationContext<Method> invocationContext, ExtensionContext extensionContext) throws Throwable {
		invocation.proceed();
	}

	@Override
	public <T> T interceptTestClassConstructor(Invocation<T> invocation,
			ReflectiveInvocationContext<java.lang.reflect.Constructor<T>> invocationContext,
			ExtensionContext extensionContext) throws Throwable {
		return invocation.proceed();
	}

	@Override
	public void interceptBeforeAllMethod(Invocation<Void> invocation,
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
		synchronized (store) {
			LifecycleState state = store.get(POLICY_PREPARED_KEY, LifecycleState.class);
			if (state == LifecycleState.PREPARED || state == LifecycleState.PREPARING) {
				return;
			}
			store.put(POLICY_PREPARED_KEY, LifecycleState.PREPARING);
		}
		try {
			resetSettingsInStandardClassLoader();
			resetSettingsInBootstrapClassLoader();
			prepareSecurity(extensionContext);
			store.put(POLICY_PREPARED_KEY, LifecycleState.PREPARED);
		} catch (RuntimeException | Error failure) {
			store.remove(POLICY_PREPARED_KEY);
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
		synchronized (store) {
			LifecycleState state = store.get(POLICY_PREPARED_KEY, LifecycleState.class);
			if (state == null || state == LifecycleState.CLOSED) {
				return;
			}
			store.put(POLICY_PREPARED_KEY, LifecycleState.CLOSED);
		}
		try {
			JavaInstrumentationAgent.throwIfTransformationFailed();
		} finally {
			resetSettingsInStandardClassLoader();
			resetSettingsInBootstrapClassLoader();
			store.remove(POLICY_PREPARED_KEY);
		}
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
