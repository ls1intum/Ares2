package de.tum.cit.ase.ares.api.internal;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.platform.commons.support.AnnotationSupport;
import org.junit.platform.commons.support.HierarchyTraversalMode;
import org.junit.platform.commons.support.ReflectionSupport;

import de.tum.cit.ase.ares.api.MirrorOutput;
import de.tum.cit.ase.ares.api.MirrorOutput.MirrorOutputPolicy;
import de.tum.cit.ase.ares.api.Policy;
import de.tum.cit.ase.ares.api.context.TestContext;
import de.tum.cit.ase.ares.api.context.TestContextUtils;
import de.tum.cit.ase.ares.api.jupiter.JupiterContext;
import de.tum.cit.ase.ares.api.jupiter.JupiterSecurityExtension;
import de.tum.cit.ase.ares.api.policy.SecurityPolicy;
import de.tum.cit.ase.ares.api.policy.policySubComponents.FailureFeedbackConfiguration;
import de.tum.cit.ase.ares.api.policy.policySubComponents.SecurityViolationFeedbackLevel;
import de.tum.cit.ase.ares.api.policy.policySubComponents.SupervisedCode;
import de.tum.cit.ase.ares.api.policy.policySubComponents.TestBehaviorConfiguration;
import de.tum.cit.ase.ares.api.policy.reader.SecurityPolicyReader;

/**
 * Answers what a test's behavioural settings are. Annotations come first where
 * a feature has one; the active policy file is the default. The policy is read
 * once per file and kept for the test class, before Ares supervises any test
 * code, so a file changed while tests run changes nothing.
 */
@API(status = Status.INTERNAL)
public final class ConfigurationUtils {

	/** The store namespace holding the read test-behaviour configurations. */
	private static final ExtensionContext.Namespace NAMESPACE = ExtensionContext.Namespace
			.create(ConfigurationUtils.class);

	/** Not instantiable. */
	private ConfigurationUtils() {
	}

	/**
	 * Reads every policy file a test class can use (its own {@code @Policy} and
	 * every method-level one) into the class's store, unless already there. Ares
	 * calls this from its earliest callback, before any test code runs.
	 *
	 * @param context the context of the class or of one of its tests.
	 */
	public static void prepareTestBehavior(TestContext context) {
		if (!(context instanceof JupiterContext jupiter)) {
			return;
		}
		ExtensionContext classContext = classContextOf(jupiter.getExtensionContext());
		Optional<Class<?>> testClass = classContext.getTestClass();
		Stream.concat(testClass.stream().flatMap(type -> annotationsOf(type).stream()), testClass.stream()
				.flatMap(type -> ReflectionSupport.findMethods(type, method -> true, HierarchyTraversalMode.TOP_DOWN)
						.stream())
				.flatMap(method -> AnnotationSupport.findAnnotation(method, Policy.class).stream()))
				.map(ConfigurationUtils::activePolicyPathOrNone).flatMap(Optional::stream)
				.forEach(path -> reading(classContext, path));
	}

	/**
	 * Returns the file of an active policy annotation, or nothing when it is
	 * deactivated, names no file or names one that does not exist. Used only for
	 * reading ahead: the test that uses the annotation reports the problem itself.
	 *
	 * @param policy the annotation.
	 * @return the file.
	 */
	private static Optional<Path> activePolicyPathOrNone(Policy policy) {
		try {
			return activePolicyPath(policy);
		} catch (RuntimeException unusable) {
			return Optional.empty();
		}
	}

	/**
	 * Returns the test behaviour of the active policy, empty when no policy is
	 * active. Read once per file and test class.
	 *
	 * @param context the test context.
	 * @return the test behaviour; never null.
	 */
	public static TestBehaviorConfiguration testBehavior(TestContext context) {
		return policyBehavior(context).behavior();
	}

	/**
	 * Returns what the active policy configures, read once per file and class.
	 *
	 * @param context the test context.
	 * @return the configuration; empty when no policy is active.
	 */
	private static PolicyBehavior policyBehavior(TestContext context) {
		Optional<Path> policyPath = TestContextUtils.findAnnotationIn(context, Policy.class)
				.flatMap(ConfigurationUtils::activePolicyPath);
		if (policyPath.isEmpty()) {
			return PolicyBehavior.NONE;
		}
		if (context instanceof JupiterContext jupiter) {
			return cached(classContextOf(jupiter.getExtensionContext()), policyPath.get());
		}
		return read(policyPath.get());
	}

	/**
	 * Returns the active policy's failure feedback.
	 *
	 * @param context the test context.
	 * @return the failure feedback; empty when not configured.
	 */
	public static Optional<FailureFeedbackConfiguration> findPolicyFailureFeedback(TestContext context) {
		return Optional.ofNullable(testBehavior(context).regardingTheFailureFeedbackForTheTestCases());
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
	 * Returns the message a non-privileged public failure shows, when the policy
	 * turns privileged-only reporting on. Only the policy decides this.
	 *
	 * @param context the test context.
	 * @return the message; empty when privileged-only reporting is off.
	 */
	public static Optional<String> getNonprivilegedFailureMessage(TestContext context) {
		return findPolicyFailureFeedback(context).filter(FailureFeedbackConfiguration::privilegedOnly)
				.map(FailureFeedbackConfiguration::theFailureMessageForOtherExceptionsIs);
	}

	/**
	 * Returns how much a blocked action reveals, {@code FULL} without a policy
	 * setting.
	 *
	 * @param context the test context.
	 * @return the feedback level.
	 */
	public static SecurityViolationFeedbackLevel getSecurityViolationFeedbackLevel(TestContext context) {
		return findPolicyFailureFeedback(context).map(FailureFeedbackConfiguration::feedbackLevel)
				.orElse(SecurityViolationFeedbackLevel.FULL);
	}

	/**
	 * Returns the cached configuration of a policy file, reading it on first use.
	 *
	 * @param classContext the class context whose store holds the cache.
	 * @param policyPath   the policy file.
	 * @return the configuration.
	 */
	private static PolicyBehavior cached(ExtensionContext classContext, Path policyPath) {
		Reading reading = reading(classContext, policyPath);
		if (reading.failure() != null) {
			throw reading.failure();
		}
		return reading.behavior();
	}

	/**
	 * Returns the cached reading of a policy file, reading it on first use. A file
	 * that cannot be read is kept as its failure, so only the tests using that file
	 * fail, each with the same error.
	 *
	 * @param classContext the class context whose store holds the cache.
	 * @param policyPath   the policy file.
	 * @return the reading.
	 */
	private static Reading reading(ExtensionContext classContext, Path policyPath) {
		String key = policyPath.toAbsolutePath().normalize().toString();
		return classContext.getStore(NAMESPACE).getOrComputeIfAbsent(key, ignored -> {
			try {
				return new Reading(read(policyPath), null);
			} catch (RuntimeException unreadable) {
				return new Reading(null, unreadable);
			}
		}, Reading.class);
	}

	/**
	 * One policy file's reading as kept in the cache: what it configures, or why it
	 * could not be read.
	 *
	 * @param behavior what the file configures; null when it could not be read.
	 * @param failure  why it could not be read; null when it was read.
	 */
	private record Reading(PolicyBehavior behavior, RuntimeException failure) {
	}

	/**
	 * Reads a policy file's test behaviour through the reader, never the director.
	 *
	 * @param policyPath the policy file.
	 * @return the configuration; empty when the file configures none.
	 */
	private static PolicyBehavior read(Path policyPath) {
		SecurityPolicy securityPolicy = SecurityPolicyReader.selectSecurityPolicyReader(policyPath)
				.readSecurityPolicyFrom(policyPath);
		SupervisedCode supervisedCode = securityPolicy.regardingTheSupervisedCode();
		return new PolicyBehavior(supervisedCode.theFollowingTestBehaviorIsConfiguredOrEmpty());
	}

	/**
	 * What one policy file configures for tests, as kept in the cache.
	 *
	 * @param behavior the test behaviour.
	 */
	private record PolicyBehavior(TestBehaviorConfiguration behavior) {

		/** What no active policy configures. */
		private static final PolicyBehavior NONE = new PolicyBehavior(new TestBehaviorConfiguration());
	}

	/**
	 * Returns the class context a context belongs to, itself when it is one.
	 *
	 * @param context a class or method context.
	 * @return the nearest context with a test class and no test method.
	 */
	private static ExtensionContext classContextOf(ExtensionContext context) {
		ExtensionContext current = context;
		while (current.getTestMethod().isPresent() && current.getParent().isPresent()) {
			current = current.getParent().get();
		}
		return current;
	}

	/**
	 * Returns the {@code @Policy} of a class, if any.
	 *
	 * @param type the class.
	 * @return the annotation, at most one.
	 */
	private static List<Policy> annotationsOf(Class<?> type) {
		return AnnotationSupport.findAnnotation(type, Policy.class).stream().toList();
	}

	/**
	 * Returns the file of an active policy annotation, empty when it is deactivated
	 * or names no file.
	 *
	 * @param policy the annotation.
	 * @return the file.
	 */
	private static Optional<Path> activePolicyPath(Policy policy) {
		if (!policy.activated() || policy.value().isBlank()) {
			return Optional.empty();
		}
		return Optional.of(JupiterSecurityExtension.testAndGetPolicyValue(policy));
	}
}
