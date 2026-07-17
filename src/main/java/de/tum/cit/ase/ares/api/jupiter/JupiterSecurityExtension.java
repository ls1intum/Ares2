package de.tum.cit.ase.ares.api.jupiter;

import static de.tum.cit.ase.ares.api.aop.java.instrumentation.advice.JavaInstrumentationAdviceFileSystemToolbox.localize;
import static org.junit.platform.commons.support.AnnotationSupport.findAnnotation;

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
import de.tum.cit.ase.ares.api.policy.SecurityPolicyReaderAndDirector;

@API(status = Status.INTERNAL)
public final class JupiterSecurityExtension
		implements UnifiedInvocationInterceptor, BeforeTestExecutionCallback, AfterTestExecutionCallback {
	private static final ExtensionContext.Namespace NAMESPACE = ExtensionContext.Namespace
			.create(JupiterSecurityExtension.class);
	private static final String POLICY_PREPARED_KEY = "policy-prepared";

	// <editor-fold desc="Lifecycle Callbacks">

	@Override
	public void interceptBeforeEachMethod(Invocation<Void> invocation,
			ReflectiveInvocationContext<Method> invocationContext, ExtensionContext extensionContext) throws Throwable {
		invocation.proceed();
	}

	@Override
	public void interceptAfterEachMethod(Invocation<Void> invocation,
			ReflectiveInvocationContext<Method> invocationContext, ExtensionContext extensionContext) throws Throwable {
		try {
			invocation.proceed();
		} finally {
			resetSettingsInStandardClassLoader();
			resetSettingsInBootstrapClassLoader();
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
		resetSettingsInStandardClassLoader();
		resetSettingsInBootstrapClassLoader();
		JupiterContext testContext = JupiterContext.of(extensionContext);
		Optional<Policy> methodPolicy = findAnnotation(testContext.testMethod(), Policy.class);
		Optional<Policy> classPolicy = findAnnotation(testContext.testClass(), Policy.class);
		Optional<Policy> policyOpt = methodPolicy.or(() -> classPolicy);
		boolean hasPolicyAnnotation = policyOpt.isPresent();
		boolean isAresActivated = policyOpt.map(Policy::activated).orElse(true);
		boolean isTestMethodPresent = testContext.testMethod().isPresent();

		if (isAresActivated && (hasPolicyAnnotation || isTestMethodPresent)) {
			Path policyPath = policyOpt.filter(p -> !p.value().isBlank())
					.map(JupiterSecurityExtension::testAndGetPolicyValue).orElse(null);
			Path withinPath = policyOpt.filter(p -> !p.withinPath().isBlank())
					.map(JupiterSecurityExtension::testAndGetPolicyWithinPath).orElse(Path.of(""));
			SecurityPolicyReaderAndDirector.builder().securityPolicyFilePath(policyPath).projectFolderPath(withinPath)
					.build().createTestCases().executeTestCases();
		}
		extensionContext.getStore(NAMESPACE).put(POLICY_PREPARED_KEY, Boolean.TRUE);
	}

	/**
	 * Resets the security settings after each test method execution.
	 */
	@Override
	public void afterTestExecution(ExtensionContext extensionContext) throws Exception {
		try {
			JavaInstrumentationAgent.throwIfTransformationFailed();
		} finally {
			extensionContext.getStore(NAMESPACE).remove(POLICY_PREPARED_KEY);
			resetSettingsInStandardClassLoader();
			resetSettingsInBootstrapClassLoader();
		}
	}

	// </editor-fold>

	@Override
	public <T> T interceptGenericInvocation(Invocation<T> invocation, ExtensionContext extensionContext,
			Optional<ReflectiveInvocationContext<?>> invocationContext) throws Throwable {
		JupiterContext testContext = JupiterContext.of(extensionContext);

		Optional<Policy> methodPolicy = findAnnotation(testContext.testMethod(), Policy.class);
		Optional<Policy> classPolicy = findAnnotation(testContext.testClass(), Policy.class);
		Optional<Policy> policyOpt = methodPolicy.or(() -> classPolicy);
		boolean hasPolicyAnnotation = policyOpt.isPresent();
		boolean isAresActivated = policyOpt.map(Policy::activated).orElse(true);

		boolean policyAlreadyPrepared = Boolean.TRUE
				.equals(extensionContext.getStore(NAMESPACE).get(POLICY_PREPARED_KEY, Boolean.class));

		// Determine security enforcement:
		// - @Policy(activated=true) → enforce with custom policy from file
		// - no @Policy on @Test method → enforce with default policy (null path)
		// - @Policy(activated=false) → Ares deactivated (no security checks)
		// Skip enforcement during constructor interception (no test method present
		// yet).
		boolean isTestMethodPresent = testContext.testMethod().isPresent();
		if (!policyAlreadyPrepared) {
			// Invocation interception is the fallback for execution environments that do
			// not invoke BeforeTestExecutionCallback. Normal Jupiter execution has already
			// prepared the policy and must not build the same WALA graph twice.
			resetSettingsInStandardClassLoader();
			resetSettingsInBootstrapClassLoader();
		}
		if (!policyAlreadyPrepared && isAresActivated && (hasPolicyAnnotation || isTestMethodPresent)) {
			Path policyPath = policyOpt.filter(p -> !p.value().isBlank())
					.map(JupiterSecurityExtension::testAndGetPolicyValue).orElse(null);
			Path withinPath = policyOpt.filter(p -> !p.withinPath().isBlank())
					.map(JupiterSecurityExtension::testAndGetPolicyWithinPath).orElse(Path.of(""));
			SecurityPolicyReaderAndDirector.builder().securityPolicyFilePath(policyPath).projectFolderPath(withinPath)
					.build().createTestCases().executeTestCases();
		}
		T result = null;
		Throwable failure = null;
		try {
			result = invocation.proceed();
		} catch (Throwable t) {
			failure = t;
		} finally {
			Throwable transformationFailure = null;
			try {
				JavaInstrumentationAgent.throwIfTransformationFailed();
			} catch (SecurityException e) {
				transformationFailure = e;
			}
			try {
				// ALWAYS reset settings AFTER the test to ensure clean state for subsequent
				// tests, since security is now enforced by default.
				resetSettingsInStandardClassLoader();
				resetSettingsInBootstrapClassLoader();
			} catch (Exception e) {
				if (failure == null && transformationFailure == null) {
					// The test itself passed, so invocation.proceed() left a pending return on
					// this method. Assigning to failure would let that return run and swallow
					// this teardown failure, leaving the next test with un-reset security
					// settings (fail-open). Throwing from the finally overrides the pending
					// return and propagates the teardown failure instead.
					throw e;
				}
				if (transformationFailure != null) {
					transformationFailure.addSuppressed(e);
				} else {
					failure.addSuppressed(e);
				}
			}
			if (transformationFailure != null) {
				if (failure != null) {
					transformationFailure.addSuppressed(failure);
				}
				throw transformationFailure;
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
