package de.tum.cit.ase.ares.api.internal;

import java.nio.file.Path;
import java.util.Optional;

import javax.annotation.Nonnull;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

import de.tum.cit.ase.ares.api.MirrorOutput;
import de.tum.cit.ase.ares.api.MirrorOutput.MirrorOutputPolicy;
import de.tum.cit.ase.ares.api.Policy;
import de.tum.cit.ase.ares.api.PrivilegedExceptionsOnly;
import de.tum.cit.ase.ares.api.context.TestContext;
import de.tum.cit.ase.ares.api.context.TestContextUtils;
import de.tum.cit.ase.ares.api.jupiter.JupiterSecurityExtension;
import de.tum.cit.ase.ares.api.policy.SecurityPolicy;
import de.tum.cit.ase.ares.api.policy.policySubComponents.PrivilegedExceptionsConfiguration;
import de.tum.cit.ase.ares.api.policy.policySubComponents.SupervisedCode;
import de.tum.cit.ase.ares.api.policy.reader.SecurityPolicyReader;

/**
 * Resolvers for the non-policy test annotations retained by Ares, and their
 * policy fallbacks.
 */
@API(status = Status.INTERNAL)
public final class ConfigurationUtils {

	private ConfigurationUtils() {
	}

	/**
	 * Resolves whether standard output should be mirrored for a test.
	 *
	 * @param context the current test context
	 * @return whether mirroring is enabled
	 */
	public static boolean shouldMirrorOutput(TestContext context) {
		return TestContextUtils.findAnnotationIn(context, MirrorOutput.class).map(MirrorOutput::value)
				.map(MirrorOutputPolicy::isEnabled).orElse(false);
	}

	/**
	 * Resolves the maximum number of standard-output characters for a test.
	 *
	 * @param context the current test context
	 * @return the configured limit
	 */
	public static long getMaxStandardOutput(TestContext context) {
		return TestContextUtils.findAnnotationIn(context, MirrorOutput.class).map(MirrorOutput::maxCharCount)
				.orElse(MirrorOutput.DEFAULT_MAX_STD_OUT);
	}

	/**
	 * Resolves the message a failed test shows instead of its real error, if any.
	 * Checks, in order: the nearest {@code @PrivilegedExceptionsOnly}, then the
	 * policy file named by an active {@code @Policy}. A precompile exercise reports
	 * failures through its own generated hooks instead, never through this method.
	 *
	 * @param context the current test context
	 * @return the configured message, if privileged-exceptions-only reporting is
	 *         effectively enabled
	 */
	public static Optional<String> getNonprivilegedFailureMessage(TestContext context) {
		Optional<String> fromAnnotation = TestContextUtils.findAnnotationIn(context, PrivilegedExceptionsOnly.class)
				.map(PrivilegedExceptionsOnly::value);
		if (fromAnnotation.isPresent()) {
			return fromAnnotation;
		}
		Optional<Path> dynamicPolicyPath = activeDynamicPolicyPath(context);
		if (dynamicPolicyPath.isPresent()) {
			SecurityPolicy securityPolicy = SecurityPolicyReader.selectSecurityPolicyReader(dynamicPolicyPath.get())
					.readSecurityPolicyFrom(dynamicPolicyPath.get());
			return privilegedExceptionsMessageFrom(securityPolicy);
		}
		return Optional.empty();
	}

	/**
	 * Resolves the file path of the policy YAML dynamically active for this test,
	 * exactly as {@code JupiterSecurityExtension}/{@code JqwikSecurityExtension}
	 * already do at real test-run time - skipping {@code SecurityPolicyDirector},
	 * since nothing here needs test-case creation.
	 *
	 * @param context the current test context
	 * @return the active policy's path, or empty when no policy dynamically applies
	 */
	private static Optional<Path> activeDynamicPolicyPath(TestContext context) {
		Optional<Policy> policyAnnotation = TestContextUtils.findAnnotationIn(context, Policy.class);
		if (policyAnnotation.isEmpty() || !policyAnnotation.get().activated()
				|| policyAnnotation.get().value().isBlank()) {
			return Optional.empty();
		}
		return Optional.of(JupiterSecurityExtension.testAndGetPolicyValue(policyAnnotation.get()));
	}

	/**
	 * Extracts the effective privileged-exceptions message from a resolved policy.
	 *
	 * @param securityPolicy the policy to read; must not be null.
	 * @return the configured message, if the policy enables the feature.
	 */
	@Nonnull
	private static Optional<String> privilegedExceptionsMessageFrom(SecurityPolicy securityPolicy) {
		SupervisedCode supervisedCode = securityPolicy.regardingTheSupervisedCode();
		PrivilegedExceptionsConfiguration configuration = supervisedCode.theFollowingTestBehaviorIsConfiguredOrEmpty()
				.regardingPrivilegedExceptions();
		if (configuration == null || !configuration.onlyPrivilegedExceptionsAreReported()) {
			return Optional.empty();
		}
		return Optional.of(configuration.theFailureMessageIs());
	}
}
