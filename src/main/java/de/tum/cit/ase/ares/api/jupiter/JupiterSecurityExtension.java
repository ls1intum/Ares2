package de.tum.cit.ase.ares.api.jupiter;

import static de.tum.cit.ase.ares.api.aop.java.instrumentation.advice.JavaInstrumentationAdviceFileSystemToolbox.localize;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.Optional;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.junit.jupiter.api.ClassTemplate;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.*;
import org.junit.platform.commons.function.Try;
import org.junit.platform.commons.support.AnnotationSupport;

import de.tum.cit.ase.ares.api.Policy;
import de.tum.cit.ase.ares.api.aop.java.instrumentation.JavaInstrumentationAgent;
import de.tum.cit.ase.ares.api.context.TestContextUtils;
import de.tum.cit.ase.ares.api.policy.SecurityPolicyReaderAndDirector;

/**
 * Arms the security guard for a test method, the code around it, and what JUnit
 * builds or closes around a template, factory or parameterised class. Class
 * phases, the static initialiser too, are guarded only if the class carries
 * {@code @Policy}. Between tests, code a test left running is allowed nothing;
 * only the thread that ended it goes unchecked. It refuses parallel execution.
 */
@API(status = Status.INTERNAL)
public class JupiterSecurityExtension
		implements UnifiedInvocationInterceptor, TestInstantiationAwareExtension, TestInstancePreConstructCallback,
		BeforeTestExecutionCallback, AfterTestExecutionCallback, AfterEachCallback, ExecutionCondition {
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
	/**
	 * Store key prefix for the guard state of a container, armed while JUnit builds
	 * or closes what it runs. The container's unique ID completes it, as a store
	 * lookup also reads the entries of enclosing containers.
	 */
	private static final String TEMPLATE_POLICY_PREPARED_KEY = "template-policy-prepared:";
	/** Store key for the resource that closes the guard of a container. */
	private static final String TEMPLATE_CLOSER_KEY = "template-guard-closer";
	/**
	 * Store key for the innermost armed container, read from below, so ending a
	 * test inside it arms the container again.
	 */
	private static final String ARMED_CONTAINER_KEY = "armed-container";
	/**
	 * Store key prefix for the guard state while a class with a policy is
	 * initialised; the class context's unique ID completes it.
	 */
	private static final String CLASS_INITIALISATION_KEY = "class-initialisation:";
	/** The settings class whose two copies hold the armed policy. */
	private static final String SETTINGS_CLASS_NAME = "de.tum.cit.ase.ares.api.aop.java.JavaAOPTestCaseSettings";
	/** JUnit setting that lets tests run in several threads at once. */
	private static final String PARALLEL_EXECUTION_ENABLED_KEY = "junit.jupiter.execution.parallel.enabled";

	/**
	 * Holds the innermost armed container in a store. The store closes what it
	 * holds that can be closed, and a context can, so the context is not stored
	 * itself.
	 *
	 * @param context the armed container
	 */
	private record ArmedContainer(ExtensionContext context) {
	}

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
	 * Refuses enabled parallel execution before a test instance is built, whether
	 * by its constructor or by a test instance factory.
	 */
	@Override
	public void preConstructTestInstance(TestInstanceFactoryContext factoryContext, ExtensionContext extensionContext) {
		refuseParallelExecution(extensionContext);
	}

	/**
	 * Arms the guard for a container before JUnit runs anything in it, since an
	 * argument source, a factory's stream or a closed argument can run student
	 * code. The guard closes when the container ends. A class with a policy is then
	 * initialised under it. It never disables anything.
	 */
	@Override
	public ConditionEvaluationResult evaluateExecutionCondition(ExtensionContext extensionContext) {
		if (isContainer(extensionContext)) {
			ExtensionContext.Store store = extensionContext.getStore(NAMESPACE);
			prepareSecurityOnce(extensionContext, containerKey(extensionContext));
			store.put(ARMED_CONTAINER_KEY, new ArmedContainer(extensionContext));
			store.put(TEMPLATE_CLOSER_KEY,
					(AutoCloseable) () -> closeSecurityOnce(extensionContext, containerKey(extensionContext)));
		}
		initialiseTestClassUnderItsPolicy(extensionContext);
		return ConditionEvaluationResult.enabled("Ares guards what JUnit runs in this container");
	}

	/**
	 * Whether a context is a container in which JUnit runs student code between its
	 * tests: a template test or a parameterised class, not one of their
	 * invocations, or a test factory.
	 */
	private static boolean isContainer(ExtensionContext extensionContext) {
		Optional<Method> testMethod = extensionContext.getTestMethod();
		if (testMethod.isPresent()) {
			return AnnotationSupport.isAnnotated(testMethod.get(), TestFactory.class)
					|| AnnotationSupport.isAnnotated(testMethod.get(), TestTemplate.class)
							&& extensionContext.getParent().flatMap(ExtensionContext::getTestMethod).isEmpty();
		}
		return extensionContext.getTestClass()
				.filter(testClass -> AnnotationSupport.isAnnotated(testClass, ClassTemplate.class)).isPresent();
	}

	/**
	 * Names the state key of a container, unique to it.
	 */
	private static String containerKey(ExtensionContext extensionContext) {
		return TEMPLATE_POLICY_PREPARED_KEY + extensionContext.getUniqueId();
	}

	/**
	 * Initialises the test class of a class context under the class's policy, so
	 * its static initialiser runs under it, whatever JUnit or another extension
	 * would touch the class first. A class without a policy is left alone, as its
	 * class phases are not guarded.
	 */
	private void initialiseTestClassUnderItsPolicy(ExtensionContext extensionContext) {
		Optional<Class<?>> testClass = extensionContext.getTestClass();
		if (extensionContext.getTestMethod().isPresent() || testClass.isEmpty() || !hasActivePolicy(extensionContext)) {
			return;
		}
		boolean armedAsContainer = isContainer(extensionContext);
		String stateKey = CLASS_INITIALISATION_KEY + extensionContext.getUniqueId();
		if (!armedAsContainer) {
			prepareSecurityOnce(extensionContext, stateKey);
		}
		try {
			Class.forName(testClass.get().getName(), true, testClass.get().getClassLoader());
		} catch (ClassNotFoundException e) {
			throw new IllegalStateException(e);
		} finally {
			if (!armedAsContainer) {
				closeSecurityOnce(extensionContext, stateKey);
			}
		}
	}

	/**
	 * Whether an activated {@code @Policy} applies to the given context.
	 */
	private static boolean hasActivePolicy(ExtensionContext extensionContext) {
		return TestContextUtils.findAnnotationIn(JupiterContext.of(extensionContext), Policy.class)
				.filter(Policy::activated).isPresent();
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
	 * If the guard cannot be armed, the constructor does not run and the test
	 * fails, even one JUnit would skip, as the constructor may call student code. A
	 * failed installation of the instrumentation is reported first. A shared
	 * instance is built under the class context, so the guard is armed only for a
	 * class with a policy.
	 */
	@Override
	public <T> T interceptTestClassConstructor(Invocation<T> invocation,
			ReflectiveInvocationContext<java.lang.reflect.Constructor<T>> invocationContext,
			ExtensionContext extensionContext) throws Throwable {
		if (extensionContext.getTestMethod().isPresent()) {
			try {
				prepareSecurityOnce(extensionContext);
			} catch (RuntimeException armingFailure) {
				JavaInstrumentationAgent.throwIfActivationFailed();
				throw armingFailure;
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
	 * Arms the guard once for the given context, under the store key of its level.
	 */
	private void prepareSecurityOnce(ExtensionContext extensionContext) {
		prepareSecurityOnce(extensionContext, stateKey(extensionContext));
	}

	/**
	 * Reads the policy and arms the guard once for the given context and key. It
	 * first ends the previous test, so code that test left running stays refused
	 * while this one is prepared. A second call does nothing, and a failure leaves
	 * the test ended and an enclosing container armed again. Enabled parallel
	 * execution is refused before anything else.
	 */
	private void prepareSecurityOnce(ExtensionContext extensionContext, String stateKey) {
		refuseParallelExecution(extensionContext);
		ExtensionContext.Store store = extensionContext.getStore(NAMESPACE);
		synchronized (store) {
			LifecycleState state = store.get(stateKey, LifecycleState.class);
			if (state == LifecycleState.PREPARED || state == LifecycleState.PREPARING) {
				return;
			}
			store.put(stateKey, LifecycleState.PREPARING);
		}
		try {
			endSupervisionInBothClassLoaders();
			prepareSecurity(extensionContext);
			store.put(stateKey, LifecycleState.PREPARED);
		} catch (RuntimeException | Error failure) {
			store.remove(stateKey);
			try {
				endSupervisionInBothClassLoaders();
				rearmEnclosingContainer(extensionContext, stateKey);
			} catch (RuntimeException | Error resetFailure) {
				failure.addSuppressed(resetFailure);
			}
			throw failure;
		}
	}

	/**
	 * Arms the policy that applies to the given context. Where Ares guards nothing,
	 * for a deactivated policy or a class phase without one, it suspends every
	 * check until the phase ends, as such a phase is documented to run unguarded.
	 */
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
		} else {
			invokeInBothClassLoaders("suspendSupervision");
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
	 * Closes the guard once for the given context, under the store key of its
	 * level.
	 */
	private void closeSecurityOnce(ExtensionContext extensionContext) {
		closeSecurityOnce(extensionContext, stateKey(extensionContext));
	}

	/**
	 * Ends the test once for the given context and key: its allowances go, so code
	 * it left running is refused. Inside an armed container, the container's guard
	 * is then armed again for what JUnit runs next. It also reports a failed class
	 * transformation of the instrumentation agent.
	 */
	private void closeSecurityOnce(ExtensionContext extensionContext, String stateKey) {
		ExtensionContext.Store store = extensionContext.getStore(NAMESPACE);
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
			endSupervisionInBothClassLoaders();
			store.remove(stateKey);
			rearmEnclosingContainer(extensionContext, stateKey);
		}
	}

	/**
	 * Arms the innermost armed container around a context again, after a test in it
	 * or the container's own phase has ended. A container that itself ends looks
	 * for the one around it.
	 */
	private void rearmEnclosingContainer(ExtensionContext extensionContext, String closedKey) {
		Optional<ExtensionContext> searchFrom = closedKey.equals(containerKey(extensionContext))
				? extensionContext.getParent()
				: Optional.of(extensionContext);
		Optional<ExtensionContext> container = searchFrom
				.map(context -> context.getStore(NAMESPACE).get(ARMED_CONTAINER_KEY, ArmedContainer.class))
				.map(ArmedContainer::context);
		if (container.isPresent() && container.get().getStore(NAMESPACE).get(containerKey(container.get()),
				LifecycleState.class) == LifecycleState.PREPARED) {
			prepareSecurity(container.get());
		}
	}

	/**
	 * Fails closed when JUnit parallel execution is enabled, read as JUnit reads
	 * it. The policy is one setting for the whole JVM, so a test that ends could
	 * lift it while another test still runs student code.
	 */
	private static void refuseParallelExecution(ExtensionContext extensionContext) {
		if (extensionContext.getConfigurationParameter(PARALLEL_EXECUTION_ENABLED_KEY, Boolean::parseBoolean)
				.orElse(false)) {
			throw new SecurityException(localize("security.policy.parallel.execution.refused"));
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

	/**
	 * Ends the current test in both copies of the settings, see
	 * {@code JavaAOPTestCaseSettings.endSupervision}.
	 */
	static void endSupervisionInBothClassLoaders() {
		invokeInBothClassLoaders("endSupervision");
	}

	/**
	 * Runs a static method of the settings class, by name, in the copy of the
	 * standard class loader and in that of the boot class loader. A copy that is
	 * not loaded has nothing to change.
	 */
	private static void invokeInBothClassLoaders(String methodName) {
		Try.call(() -> Class.forName(SETTINGS_CLASS_NAME, true, Thread.currentThread().getContextClassLoader()))
				.ifSuccess(settingsClass -> invokeSettingsMethod(settingsClass, methodName));
		Try.call(() -> Class.forName(SETTINGS_CLASS_NAME, false, null))
				.ifSuccess(settingsClass -> invokeSettingsMethod(settingsClass, methodName));
	}

	/**
	 * Runs a static method of one copy of the settings class. A missing method
	 * fails closed, so no test inherits the settings of the one before.
	 */
	private static void invokeSettingsMethod(Class<?> settingsClass, String methodName) {
		try {
			resetSettingsWithMethod(settingsClass.getDeclaredMethod(methodName));
		} catch (NoSuchMethodException e) {
			throw new SecurityException(localize("security.settings.reset.method.not.found"), e);
		}
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
