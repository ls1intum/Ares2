package de.tum.cit.ase.ares.api.jupiter;

import static de.tum.cit.ase.ares.api.aop.java.instrumentation.advice.JavaInstrumentationAdviceFileSystemToolbox.localize;
import static de.tum.cit.ase.ares.api.internal.TestGuardUtils.hasAnnotation;
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
import de.tum.cit.ase.ares.api.policy.SecurityPolicyReaderAndDirector;
//REMOVED: Import of ArtemisSecurityManager

@API(status = Status.INTERNAL)
public final class JupiterSecurityExtension implements UnifiedInvocationInterceptor {

	@Override
	public <T> T interceptGenericInvocation(Invocation<T> invocation, ExtensionContext extensionContext,
			Optional<ReflectiveInvocationContext<?>> invocationContext) throws Throwable {
		JupiterContext testContext = JupiterContext.of(extensionContext);

		Optional<Policy> policyOpt = findAnnotation(testContext.testMethod(), Policy.class);
		boolean hasPolicyAnnotation = hasAnnotation(testContext, Policy.class);
		boolean isPolicyActivated = policyOpt.map(Policy::activated).orElse(false);

		// ALWAYS reset settings first to ensure clean state for every test.
		// This prevents settings from a previous @Policy test from affecting the
		// current test.
		// We have to reset both the settings classes in the runtime and the bootstrap
		// class loader to be able to run multiple tests in the same JVM instance.
		// Use 'false' for initialization to match how the instrumentation advice loads
		// the class.
		String className = "de.tum.cit.ase.ares.api.aop.java.JavaAOPTestCaseSettings";
		Try.call(() -> Class.forName(className, true, Thread.currentThread().getContextClassLoader()))
				.ifSuccess(JupiterSecurityExtension::resetSettings);
		// Reset Bootstrap ClassLoader settings (may silently fail if class not yet
		// loaded there)
		resetSettingsInBootstrapClassLoader();

		// THEN activate policy if annotation is present and activated
		if (hasPolicyAnnotation && isPolicyActivated) {
			policyOpt.ifPresent(policy -> SecurityPolicyReaderAndDirector.builder()
					.securityPolicyFilePath(
							!policy.value().isBlank() ? JupiterSecurityExtension.testAndGetPolicyValue(policy) : null)
					.projectFolderPath(
							!policy.withinPath().isBlank() ? JupiterSecurityExtension.testAndGetPolicyWithinPath(policy)
									: Path.of(""))
					.build().createTestCases().executeTestCases());
		}
		// REMOVED: Installing of ArtemisSecurityManager
		Throwable failure = null;
		try {
			return invocation.proceed();
		} catch (Throwable t) {
			failure = t;
		} finally {
			try {
				// REMOVED: Uninstallation of ArtemisSecurityManager
				// ALWAYS reset settings AFTER the test to ensure clean state for subsequent
				// tests.
				// This is critical because tests without @Policy annotation do not trigger this
				// extension,
				// so settings must be cleared after @Policy tests to prevent leakage.
				if (hasPolicyAnnotation && isPolicyActivated) {
					Try.call(() -> Class.forName(className, true, Thread.currentThread().getContextClassLoader()))
							.ifSuccess(JupiterSecurityExtension::resetSettings);
					resetSettingsInBootstrapClassLoader();
				}
			} catch (Exception e) {
				if (failure == null)
					failure = e;
				else
					failure.addSuppressed(e);
			}
		}
		throw failure;
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
			Method resetMethod = settingsClass.getDeclaredMethod("reset");
			resetMethod.setAccessible(true);
			resetMethod.invoke(null);
			resetMethod.setAccessible(false);
		} catch (ClassNotFoundException e) {
			// Class not yet loaded in Bootstrap ClassLoader - OK if no instrumentation yet
		} catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
			// Reset failed - log silently
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
