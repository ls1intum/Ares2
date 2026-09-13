package de.tum.cit.ase.ares.api.internal;

import java.lang.reflect.Field;
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
	 * postcompile deployment where that file is still resolvable; the generated,
	 * compiled settings class {@code JavaWriter} writes at precompile time, for a
	 * deployment where nothing dynamically resolves a policy any more. Once a
	 * dynamic policy applies, its verdict is final and never falls through to the
	 * generated settings class, even when the verdict is "disabled" - falling
	 * through there would let a stale class silently override an explicit disable.
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
		return resolveFromGeneratedSettingsClass();
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
	 * Resolves the policy-level default from the generated, compiled settings class
	 * a precompile deployment writes; this is a fast, harmless miss in postcompile,
	 * where nothing ever writes it. Absence of the class means "nothing configured"
	 * and resolves to empty; a present class missing the expected field - version
	 * skew between the Ares jar active now and the one that generated the class -
	 * fails closed with a {@link SecurityException} rather than being silently
	 * treated as absent, since "absent" is the direction that leaks a hidden test's
	 * real failure detail, not the safe one. Any other reflective failure (the
	 * field existing with an unexpected type, or being inaccessible) is not this
	 * specific, understood failure mode and is left to propagate rather than folded
	 * into the same fail-closed path.
	 *
	 * @return the configured message, if the generated settings class effectively
	 *         enables the feature
	 */
	private static Optional<String> resolveFromGeneratedSettingsClass() {
		Class<?> settingsClass;
		try {
			// The thread context classloader, not the implicit caller classloader: Ares
			// itself may be loaded by a different classloader than the one that loaded a
			// consumer exercise's own generated classes.
			settingsClass = Class.forName(TestBehaviorConfiguration.GENERATED_CLASS_NAME, true,
					Thread.currentThread().getContextClassLoader());
		} catch (ClassNotFoundException notConfigured) {
			return Optional.empty();
		}
		boolean enabled;
		String message;
		try {
			Field enabledField = settingsClass
					.getField(TestBehaviorConfiguration.PRIVILEGED_EXCEPTIONS_ENABLED_FIELD_NAME);
			Field messageField = settingsClass
					.getField(TestBehaviorConfiguration.PRIVILEGED_EXCEPTIONS_MESSAGE_FIELD_NAME);
			enabled = (boolean) enabledField.get(null);
			message = (String) messageField.get(null);
		} catch (NoSuchFieldException versionSkew) {
			throw new SecurityException(Messages.localized("security.policy.behavior.settings.class.malformed",
					TestBehaviorConfiguration.GENERATED_CLASS_NAME), versionSkew);
		} catch (IllegalAccessException impossible) {
			throw new IllegalStateException("Unable to read generated test-behaviour settings class: "
					+ TestBehaviorConfiguration.GENERATED_CLASS_NAME, impossible);
		}
		if (!enabled) {
			return Optional.empty();
		}
		return Optional
				.of(message == null || message.isBlank() ? PrivilegedExceptionsConfiguration.DEFAULT_FAILURE_MESSAGE
						: message);
	}
}
