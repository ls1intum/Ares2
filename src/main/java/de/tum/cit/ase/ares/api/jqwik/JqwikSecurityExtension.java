package de.tum.cit.ase.ares.api.jqwik;

import static de.tum.cit.ase.ares.api.internal.TestGuardUtils.hasAnnotation;
import static de.tum.cit.ase.ares.api.jupiter.JupiterSecurityExtension.resetSettingsInBootstrapClassLoader;
import static de.tum.cit.ase.ares.api.jupiter.JupiterSecurityExtension.resetSettingsInStandardClassLoader;
import static de.tum.cit.ase.ares.api.jupiter.JupiterSecurityExtension.testAndGetPolicyValue;
import static de.tum.cit.ase.ares.api.jupiter.JupiterSecurityExtension.testAndGetPolicyWithinPath;
import static org.junit.platform.commons.support.AnnotationSupport.findAnnotation;

import java.nio.file.Path;
import java.util.Optional;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

import net.jqwik.api.lifecycle.*;

import de.tum.cit.ase.ares.api.Policy;
import de.tum.cit.ase.ares.api.policy.SecurityPolicyReaderAndDirector;
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
	public PropertyExecutionResult aroundProperty(PropertyLifecycleContext context, PropertyExecutor property) {
		var testContext = JqwikContext.of(context);

		Optional<Policy> policyOpt = findAnnotation(testContext.testMethod(), Policy.class);
		boolean hasPolicyAnnotation = hasAnnotation(testContext, Policy.class);
		boolean isAresActivated = policyOpt.map(Policy::activated).orElse(true);

		// ALWAYS reset settings first to ensure clean state for every test.
		resetSettingsInStandardClassLoader();
		resetSettingsInBootstrapClassLoader();

		// Determine security enforcement:
		// - @Policy(activated=true) or no @Policy → Ares active (security checks enabled)
		// - @Policy(activated=false) → Ares deactivated (no security checks)
		// - @Policy(value="file.yaml") → custom policy from file
		if (!(hasPolicyAnnotation && !isAresActivated)) {
			Path policyPath = policyOpt.filter(p -> !p.value().isBlank())
					.map(p -> testAndGetPolicyValue(p)).orElse(null);
			Path withinPath = policyOpt.filter(p -> !p.withinPath().isBlank())
					.map(p -> testAndGetPolicyWithinPath(p)).orElse(Path.of(""));
			SecurityPolicyReaderAndDirector.builder().securityPolicyFilePath(policyPath)
					.projectFolderPath(withinPath).build().createTestCases().executeTestCases();
		}
		// REMOVED: Installing of ArtemisSecurityManager
		PropertyExecutionResult result;
		Throwable error = null;
		try {
			result = property.execute();
		} finally {
			try {
				// REMOVED: Uninstallation of ArtemisSecurityManager
				// ALWAYS reset settings AFTER the test to ensure clean state.
				resetSettingsInStandardClassLoader();
				resetSettingsInBootstrapClassLoader();
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
