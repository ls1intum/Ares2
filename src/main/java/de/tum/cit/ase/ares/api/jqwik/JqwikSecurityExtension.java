package de.tum.cit.ase.ares.api.jqwik;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Optional;

import de.tum.cit.ase.ares.api.Policy;
import de.tum.cit.ase.ares.api.policy.SecurityPolicyReaderAndDirector;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

import net.jqwik.api.lifecycle.*;

import de.tum.cit.ase.ares.api.internal.ConfigurationUtils;

import static de.tum.cit.ase.ares.api.internal.TestGuardUtils.hasAnnotation;
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
				Policy policy = policyAnnotation.get();
				Path policyPath = Path.of(policy.value());
				if (!policyPath.toFile().exists()) {
					throw new SecurityException("Policy file does not exist at: " + policyPath);
				}
				new SecurityPolicyReaderAndDirector(policyPath).runSecurityTestCases();
			}

		}
		var configuration = ConfigurationUtils.generateConfiguration(testContext);
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
