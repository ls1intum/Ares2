package de.tum.cit.ase.ares.api.internal;

import java.nio.file.InvalidPathException;
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
import de.tum.cit.ase.ares.api.Policy;
import de.tum.cit.ase.ares.api.PrivilegedExceptionsOnly;
import de.tum.cit.ase.ares.api.context.TestContext;
import de.tum.cit.ase.ares.api.context.TestContextUtils;
import de.tum.cit.ase.ares.api.io.IOManager;
import de.tum.cit.ase.ares.api.jupiter.JupiterContext;
import de.tum.cit.ase.ares.api.jupiter.JupiterSecurityExtension;
import de.tum.cit.ase.ares.api.policy.SecurityPolicy;
import de.tum.cit.ase.ares.api.policy.policySubComponents.OutputConfiguration;
import de.tum.cit.ase.ares.api.policy.policySubComponents.StrictTimeoutConfiguration;
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
				.forEach(path -> reading(classContext, path, loaderOf(testClass)));
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
	 * Returns the custom I/O manager the active policy names, already checked.
	 *
	 * @param context the test context.
	 * @return the manager class; empty unless the policy names a custom one.
	 */
	public static Optional<Class<? extends IOManager<?>>> findPolicyCustomIOManager(TestContext context) {
		return policyBehavior(context).customIOManager();
	}

	/**
	 * Returns what the active policy configures, read once per file and class.
	 *
	 * @param context the test context.
	 * @return the configuration; empty when no policy is active.
	 */
	private static PolicyBehavior policyBehavior(TestContext context) {
		Optional<Policy> policy = TestContextUtils.findAnnotationIn(context, Policy.class);
		ClassLoader loader = loaderOf(context.testClass());
		if (context instanceof JupiterContext jupiter && policy.isPresent()) {
			ExtensionContext classContext = classContextOf(jupiter.getExtensionContext());
			Optional<PolicyBehavior> earlier = earlierReading(classContext, policy.get());
			if (earlier.isPresent()) {
				return earlier.get();
			}
			return policy.flatMap(ConfigurationUtils::activePolicyPath).map(path -> cached(classContext, path, loader))
					.orElse(PolicyBehavior.NONE);
		}
		return policy.flatMap(ConfigurationUtils::activePolicyPath).map(path -> read(path, loader))
				.orElse(PolicyBehavior.NONE);
	}

	/**
	 * Returns what an active policy's file configured when the class already read
	 * it, without checking that the file still exists, so a test that moves its
	 * policy afterwards keeps the reading it started with.
	 *
	 * @param classContext the class context whose store holds the cache.
	 * @param policy       the annotation.
	 * @return the earlier reading; empty when there is none or it failed.
	 */
	private static Optional<PolicyBehavior> earlierReading(ExtensionContext classContext, Policy policy) {
		if (!policy.activated() || policy.value().isBlank()) {
			return Optional.empty();
		}
		try {
			String key = Path.of(policy.value()).toAbsolutePath().normalize().toString();
			return Optional.ofNullable(classContext.getStore(NAMESPACE).get(key, Reading.class)).map(Reading::behavior);
		} catch (InvalidPathException invalid) {
			return Optional.empty();
		}
	}

	/**
	 * Returns the active policy's strict timeout.
	 *
	 * @param context the test context.
	 * @return the strict timeout; empty when not configured.
	 */
	public static Optional<StrictTimeoutConfiguration> findPolicyStrictTimeout(TestContext context) {
		return Optional.ofNullable(testBehavior(context).regardingTheStrictTimeoutForTheTestCases());
	}

	/**
	 * Returns the active policy's output settings.
	 *
	 * @param context the test context.
	 * @return the output settings; empty when not configured.
	 */
	public static Optional<OutputConfiguration> findPolicyOutput(TestContext context) {
		return Optional.ofNullable(testBehavior(context).regardingTheOutputOfTheTestCases());
	}

	/**
	 * Returns whether a test's output is echoed.
	 *
	 * @param context the test context.
	 * @return whether output is echoed.
	 */
	public static boolean shouldMirrorOutput(TestContext context) {
		return resolveOutputMirroring(context).mirrored();
	}

	/**
	 * Returns how much output a test may write per stream.
	 *
	 * @param context the test context.
	 * @return the output limit.
	 */
	public static long getMaxStandardOutput(TestContext context) {
		return resolveOutputMirroring(context).maximumCharacterCount();
	}

	/**
	 * Resolves mirroring and the limit together: the nearest {@code @MirrorOutput}
	 * replaces the whole category, else the policy, else no echo and the default
	 * limit.
	 *
	 * @param context the test context.
	 * @return the resolved settings.
	 */
	public static ResolvedOutputMirroring resolveOutputMirroring(TestContext context) {
		Optional<MirrorOutput> annotation = TestContextUtils.findAnnotationIn(context, MirrorOutput.class);
		if (annotation.isPresent()) {
			return new ResolvedOutputMirroring(annotation.get().value().isEnabled(), annotation.get().maxCharCount());
		}
		Optional<OutputConfiguration> policy = findPolicyOutput(context);
		return new ResolvedOutputMirroring(policy.map(OutputConfiguration::mirrored).orElse(false),
				policy.map(OutputConfiguration::maximumCharacterCount).orElse(MirrorOutput.DEFAULT_MAX_STD_OUT));
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
	 * Mirroring and the output limit, resolved together.
	 *
	 * @param mirrored              whether output is echoed.
	 * @param maximumCharacterCount the output limit per stream.
	 */
	public record ResolvedOutputMirroring(boolean mirrored, long maximumCharacterCount) {
	}

	/**
	 * Returns the cached configuration of a policy file, reading it on first use.
	 *
	 * @param classContext the class context whose store holds the cache.
	 * @param policyPath   the policy file.
	 * @param loader       the loader of the test classes.
	 * @return the configuration.
	 */
	private static PolicyBehavior cached(ExtensionContext classContext, Path policyPath, ClassLoader loader) {
		Reading reading = reading(classContext, policyPath, loader);
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
	 * @param loader       the loader of the test classes.
	 * @return the reading.
	 */
	private static Reading reading(ExtensionContext classContext, Path policyPath, ClassLoader loader) {
		String key = policyPath.toAbsolutePath().normalize().toString();
		return classContext.getStore(NAMESPACE).getOrComputeIfAbsent(key, ignored -> {
			try {
				return new Reading(read(policyPath, loader), null);
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
	 * Reads a policy file's test behaviour through the reader, never the director,
	 * and checks a custom I/O manager it names.
	 *
	 * @param policyPath the policy file.
	 * @param loader     the loader of the test classes.
	 * @return the configuration; empty when the file configures none.
	 */
	private static PolicyBehavior read(Path policyPath, ClassLoader loader) {
		SecurityPolicy securityPolicy = SecurityPolicyReader.selectSecurityPolicyReader(policyPath)
				.readSecurityPolicyFrom(policyPath);
		SupervisedCode supervisedCode = securityPolicy.regardingTheSupervisedCode();
		TestBehaviorConfiguration behavior = supervisedCode.theFollowingTestBehaviorIsConfiguredOrEmpty();
		Optional<Class<? extends IOManager<?>>> customIOManager = Optional
				.ofNullable(behavior.regardingTheOutputOfTheTestCases())
				.flatMap(OutputConfiguration::customManagerClassName)
				.map(name -> IOManagerReference.resolve(name, supervisedCode, loader));
		return new PolicyBehavior(behavior, customIOManager);
	}

	/**
	 * Returns the loader of the test classes.
	 *
	 * @param testClass the test class, if known.
	 * @return its loader, else the thread's.
	 */
	private static ClassLoader loaderOf(Optional<Class<?>> testClass) {
		return testClass.map(Class::getClassLoader).orElseGet(() -> Thread.currentThread().getContextClassLoader());
	}

	/**
	 * What one policy file configures for tests, as kept in the cache.
	 *
	 * @param behavior        the test behaviour.
	 * @param customIOManager the checked custom I/O manager, if one is named.
	 */
	private record PolicyBehavior(TestBehaviorConfiguration behavior,
			Optional<Class<? extends IOManager<?>>> customIOManager) {

		/** What no active policy configures. */
		private static final PolicyBehavior NONE = new PolicyBehavior(new TestBehaviorConfiguration(),
				Optional.empty());
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

	/**
	 * Loads a class by its canonical name, trying each nesting reading.
	 *
	 * @param canonicalName the canonical name.
	 * @param loader        the loader.
	 * @return the class; empty when none matches.
	 */
	static Optional<Class<?>> loadByCanonicalName(String canonicalName, ClassLoader loader) {
		String binaryName = canonicalName;
		while (true) {
			try {
				return Optional.of(Class.forName(binaryName, false, loader));
			} catch (ClassNotFoundException | LinkageError notThisReading) {
				int lastDot = binaryName.lastIndexOf('.');
				if (lastDot < 0) {
					return Optional.empty();
				}
				binaryName = binaryName.substring(0, lastDot) + '$' + binaryName.substring(lastDot + 1);
			}
		}
	}
}
