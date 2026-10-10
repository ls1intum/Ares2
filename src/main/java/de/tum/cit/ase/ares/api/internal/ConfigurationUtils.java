package de.tum.cit.ase.ares.api.internal;

import java.nio.file.Path;
import java.util.Optional;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

import de.tum.cit.ase.ares.api.MirrorOutput;
import de.tum.cit.ase.ares.api.Policy;
import de.tum.cit.ase.ares.api.PrivilegedExceptionsOnly;
import de.tum.cit.ase.ares.api.context.TestContext;
import de.tum.cit.ase.ares.api.context.TestContextUtils;
import de.tum.cit.ase.ares.api.context.TestType;
import de.tum.cit.ase.ares.api.jupiter.JupiterSecurityExtension;
import de.tum.cit.ase.ares.api.policy.SecurityPolicy;
import de.tum.cit.ase.ares.api.policy.policySubComponents.OutputMirroringConfiguration;
import de.tum.cit.ase.ares.api.policy.reader.SecurityPolicyReader;

/** Resolvers for the non-policy test annotations retained by Ares. */
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
		return resolveOutputMirroring(context).mirrored();
	}

	/**
	 * Resolves the maximum number of standard-output characters for a test.
	 *
	 * @param context the current test context
	 * @return the configured limit
	 */
	public static long getMaxStandardOutput(TestContext context) {
		return resolveOutputMirroring(context).maximumCharacterCount();
	}

	/**
	 * Resolves both I/O settings from the same annotation or policy read.
	 *
	 * @param context the current test
	 * @return its resolved output settings
	 */
	public static ResolvedOutputMirroring resolveOutputMirroring(TestContext context) {
		Optional<MirrorOutput> annotation = TestContextUtils.findAnnotationIn(context, MirrorOutput.class);
		boolean hidden = context.findTestType().orElse(null) == TestType.HIDDEN;
		if (annotation.isPresent()) {
			return new ResolvedOutputMirroring(!hidden && annotation.get().value().isEnabled(),
					annotation.get().maxCharCount());
		}
		Optional<OutputMirroringConfiguration> policy = findPolicyOutputMirroring(context);
		return new ResolvedOutputMirroring(!hidden && policy.map(OutputMirroringConfiguration::mirrored).orElse(false),
				policy.map(OutputMirroringConfiguration::maximumCharacterCount)
						.orElse(MirrorOutput.DEFAULT_MAX_STD_OUT));
	}

	/**
	 * The two output settings resolved from one policy version.
	 *
	 * @param mirrored              whether output reaches the original console
	 * @param maximumCharacterCount the output limit for each stream
	 */
	public record ResolvedOutputMirroring(boolean mirrored, long maximumCharacterCount) {
	}

	/**
	 * Resolves the optional failure message for non-privileged exceptions.
	 *
	 * @param context the current test context
	 * @return the configured message, if present
	 */
	public static Optional<String> getNonprivilegedFailureMessage(TestContext context) {
		return TestContextUtils.findAnnotationIn(context, PrivilegedExceptionsOnly.class)
				.map(PrivilegedExceptionsOnly::value);
	}

	/**
	 * The output-mirroring default of the policy dynamically active for this test,
	 * read straight from its file. Empty when no active {@code @Policy} applies or
	 * the policy configures no {@code regardingOutputMirroring}.
	 *
	 * @param context the current test context
	 * @return the policy's output-mirroring category, if any
	 */
	public static Optional<OutputMirroringConfiguration> findPolicyOutputMirroring(TestContext context) {
		Optional<Path> policyPath = activeDynamicPolicyPath(context);
		if (policyPath.isEmpty()) {
			return Optional.empty();
		}
		SecurityPolicy securityPolicy = SecurityPolicyReader.selectSecurityPolicyReader(policyPath.get())
				.readSecurityPolicyFrom(policyPath.get());
		return Optional.ofNullable(securityPolicy.regardingTheSupervisedCode()
				.theFollowingTestBehaviorIsConfiguredOrEmpty().regardingOutputMirroring());
	}

	/**
	 * Resolves the file path of the policy YAML dynamically active for this test,
	 * exactly as {@code JupiterSecurityExtension} already does at real test-run
	 * time - skipping {@code SecurityPolicyDirector}, since nothing here needs
	 * test-case creation.
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
}
