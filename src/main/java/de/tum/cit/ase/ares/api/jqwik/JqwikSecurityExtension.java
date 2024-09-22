package de.tum.cit.ase.ares.api.jqwik;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.Path;
import java.util.Optional;
import java.util.stream.Stream;

import de.tum.cit.ase.ares.api.Policy;
import de.tum.cit.ase.ares.api.jupiter.JupiterSecurityExtension;
import de.tum.cit.ase.ares.api.policy.SecurityPolicyReaderAndDirector;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

import net.jqwik.api.lifecycle.*;

import static de.tum.cit.ase.ares.api.internal.TestGuardUtils.hasAnnotation;
import static de.tum.cit.ase.ares.api.jupiter.JupiterSecurityExtension.resetSettings;
import static org.junit.platform.commons.support.AnnotationSupport.findAnnotation;
//REMOVED: Import of ArtemisSecurityManager

/**
 * <p>
 * <i>Adaption for jqwik.</i>
 *
 * @author Christian Femers
 */
@API(status = Status.INTERNAL)
public final class JqwikSecurityExtension implements AroundPropertyHook {

	@Override
	public int aroundPropertyProximity() {
		return 30;
	}

	@Override
	public PropertyExecutionResult aroundProperty(PropertyLifecycleContext context, PropertyExecutor property)
			throws Throwable {
		var testContext = JqwikContext.of(context);
		/**
		 * Check if the test method has the {@link Policy} annotation. If it does, read
		 * the policy file and run the security test cases.
		 */
		if (hasAnnotation(testContext, Policy.class)) {
			Optional<Policy> policyAnnotation = findAnnotation(testContext.testMethod(), Policy.class);
			if (policyAnnotation.isPresent()) {
				Path policyPath = JupiterSecurityExtension.testAndGetPolicyValue(policyAnnotation.get());
				if (!policyAnnotation.get().withinPath().isBlank()) {
					Path withinPath = JupiterSecurityExtension.testAndGetPolicyWithinPath(policyAnnotation.get());
					new SecurityPolicyReaderAndDirector(policyPath, withinPath).executeSecurityTestCases();
				} else {
					new SecurityPolicyReaderAndDirector(policyPath, Path.of("classes")).executeSecurityTestCases();
				}
			}
		} else {
			try {
				Class<?> javaSecurityTestCaseSettingsClass = Class.forName("de.tum.cit.ase.ares.api.aop.java.JavaSecurityTestCaseSettings");
				resetSettings(javaSecurityTestCaseSettingsClass);
				javaSecurityTestCaseSettingsClass = Class.forName("de.tum.cit.ase.ares.api.aop.java.JavaSecurityTestCaseSettings", true, null);
				resetSettings(javaSecurityTestCaseSettingsClass);
			} catch (ClassNotFoundException e) {
				throw new SecurityException("Security configuration error: The class for the specific security test case settings could not be found. Ensure the class name is correct and the class is available at runtime.", e);
			}
		}
//REMOVED: Installing of ArtemisSecurityManager
		PropertyExecutionResult result;
		Throwable error = null;
		try {
			/*
			 * Note that the only Throwable not caught and collected is OutOfMemoryError
			 */
			result = property.execute();
		} finally {
			try {
				//REMOVED: InInstallation of ArtemisSecurityManager
			} catch (Exception e) {
				error = e;
			}
		}
		// Fix for issue #1, add as suppressed exception
		if (error != null) {
			Optional<Throwable> propExecError = result.throwable();
			if (propExecError.isPresent())
				propExecError.get().addSuppressed(error);
			else
				result = result.mapToFailed(error);
		}
		return result;
	}
}
