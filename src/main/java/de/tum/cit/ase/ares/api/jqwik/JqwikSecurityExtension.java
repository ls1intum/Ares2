package de.tum.cit.ase.ares.api.jqwik;

import static de.tum.cit.ase.ares.api.jupiter.JupiterSecurityExtension.resetSettingsInBootstrapClassLoader;
import static de.tum.cit.ase.ares.api.jupiter.JupiterSecurityExtension.resetSettingsInStandardClassLoader;
import static de.tum.cit.ase.ares.api.jupiter.JupiterSecurityExtension.testAndGetPolicyValue;
import static de.tum.cit.ase.ares.api.jupiter.JupiterSecurityExtension.testAndGetPolicyWithinPath;

import java.nio.file.Path;
import java.util.Optional;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

import net.jqwik.api.lifecycle.*;

import de.tum.cit.ase.ares.api.Policy;
import de.tum.cit.ase.ares.api.aop.java.instrumentation.JavaInstrumentationAgent;
import de.tum.cit.ase.ares.api.context.TestContextUtils;
import de.tum.cit.ase.ares.api.policy.SecurityPolicyReaderAndDirector;

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

		Optional<Policy> policyOpt = TestContextUtils.findAnnotationIn(testContext, Policy.class);
		boolean isAresActivated = policyOpt.map(Policy::activated).orElse(true);

		// ALWAYS reset settings first to ensure clean state for every test.
		resetSettingsInStandardClassLoader();
		resetSettingsInBootstrapClassLoader();

		// Determine security enforcement:
		// - @Policy(activated=true) → enforce with custom policy from file
		// - no @Policy on property → enforce with default policy (null path)
		// - @Policy(activated=false) → Ares deactivated (no security checks)
		if (isAresActivated) {
			try {
				Path policyPath = policyOpt.filter(p -> !p.value().isBlank()).map(p -> testAndGetPolicyValue(p))
						.orElse(null);
				Path withinPath = policyOpt.filter(p -> !p.withinPath().isBlank())
						.map(p -> testAndGetPolicyWithinPath(p)).orElse(Path.of(""));
				SecurityPolicyReaderAndDirector.builder().securityPolicyFilePath(policyPath)
						.projectFolderPath(Path.of("").toAbsolutePath()).withinPath(withinPath).build()
						.createTestCases().executeTestCases();
			} catch (RuntimeException | Error failure) {
				try {
					resetSettingsInStandardClassLoader();
					resetSettingsInBootstrapClassLoader();
				} catch (RuntimeException | Error resetFailure) {
					failure.addSuppressed(resetFailure);
				}
				throw failure;
			}
		}
		PropertyExecutionResult result;
		SecurityException transformationFailure = null;
		Exception resetFailure = null;
		try {
			result = property.execute();
		} finally {
			try {
				JavaInstrumentationAgent.throwIfTransformationFailed();
			} catch (SecurityException e) {
				transformationFailure = e;
			}
			try {
				// ALWAYS reset settings AFTER the test to ensure clean state.
				resetSettingsInStandardClassLoader();
				resetSettingsInBootstrapClassLoader();
			} catch (Exception e) {
				resetFailure = e;
			}
		}
		return mergeLifecycleFailures(result, transformationFailure, resetFailure);
	}

	static PropertyExecutionResult mergeLifecycleFailures(PropertyExecutionResult result,
			SecurityException transformationFailure, Exception resetFailure) {
		if (transformationFailure != null) {
			if (resetFailure != null) {
				transformationFailure.addSuppressed(resetFailure);
			}
			result.throwable().ifPresent(transformationFailure::addSuppressed);
			return result.mapToFailed(transformationFailure);
		}
		if (resetFailure != null) {
			Optional<Throwable> propExecError = result.throwable();
			if (propExecError.isPresent()) {
				propExecError.get().addSuppressed(resetFailure);
			} else {
				return result.mapToFailed(resetFailure);
			}
		}
		return result;
	}
}
