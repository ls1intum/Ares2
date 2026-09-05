package de.tum.cit.ase.ares.api.internal;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.Optional;
import java.util.Properties;

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
import de.tum.cit.ase.ares.api.localization.Messages;
import de.tum.cit.ase.ares.api.policy.SecurityPolicy;
import de.tum.cit.ase.ares.api.policy.policySubComponents.PrivilegedExceptionsConfiguration;
import de.tum.cit.ase.ares.api.policy.policySubComponents.SupervisedCode;
import de.tum.cit.ase.ares.api.policy.policySubComponents.TestBehaviorConfiguration;
import de.tum.cit.ase.ares.api.policy.reader.SecurityPolicyReader;

/**
 * Resolvers for the non-policy test annotations retained by Ares, and their
 * policy fallbacks.
 */
@API(status = Status.INTERNAL)
public final class ConfigurationUtils {

	/** Properties key naming whether the generated resource enables the feature. */
	private static final String PRIVILEGED_EXCEPTIONS_ENABLED_KEY = "regardingPrivilegedExceptions.onlyPrivilegedExceptionsAreReported";
	/**
	 * Properties key naming the generated resource's configured failure message.
	 */
	private static final String PRIVILEGED_EXCEPTIONS_MESSAGE_KEY = "regardingPrivilegedExceptions.theFailureMessageIs";

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
	 * Resolves the effective non-privileged failure message for a test.
	 * <p>
	 * Checks, in order: the nearest {@code @PrivilegedExceptionsOnly} annotation; a
	 * dynamic re-read of the policy YAML named by {@code @Policy}, for a
	 * postcompile deployment where that file is still resolvable; a generated,
	 * project-level resource, for a precompile deployment where nothing dynamically
	 * resolves a policy any more. Once a dynamic policy applies, its verdict is
	 * final and never falls through to the generated resource, even when the
	 * verdict is "disabled" - falling through there would let a stale resource
	 * silently override an explicit disable.
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
		return resolveFromGeneratedResource();
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

	/**
	 * Resolves the policy-level default from the generated, project-level resource
	 * a precompile deployment writes; this is a fast, harmless miss in postcompile,
	 * where nothing ever writes it.
	 * <p>
	 * A present-but-malformed resource fails closed with a
	 * {@link SecurityException} rather than being silently treated as absent -
	 * "absent" means "disabled" for this feature, which is the direction that leaks
	 * a hidden test's real failure detail, not the safe one.
	 *
	 * @return the configured message, if the generated resource effectively enables
	 *         the feature
	 */
	private static Optional<String> resolveFromGeneratedResource() {
		Properties properties = new Properties();
		try (InputStream resource = ConfigurationUtils.class
				.getResourceAsStream("/" + TestBehaviorConfiguration.GENERATED_RESOURCE_PATH)) {
			if (resource == null) {
				return Optional.empty();
			}
			properties.load(resource);
		} catch (IOException malformed) {
			throw new SecurityException(Messages.localized("security.policy.behavior.resource.malformed",
					TestBehaviorConfiguration.GENERATED_RESOURCE_PATH), malformed);
		}
		String enabled = properties.getProperty(PRIVILEGED_EXCEPTIONS_ENABLED_KEY);
		if (enabled == null) {
			return Optional.empty();
		}
		if (!"true".equalsIgnoreCase(enabled) && !"false".equalsIgnoreCase(enabled)) {
			throw new SecurityException(Messages.localized("security.policy.behavior.resource.malformed",
					TestBehaviorConfiguration.GENERATED_RESOURCE_PATH));
		}
		if (!Boolean.parseBoolean(enabled)) {
			return Optional.empty();
		}
		String message = properties.getProperty(PRIVILEGED_EXCEPTIONS_MESSAGE_KEY);
		return Optional
				.of(message == null || message.isBlank() ? PrivilegedExceptionsConfiguration.DEFAULT_FAILURE_MESSAGE
						: message);
	}
}
