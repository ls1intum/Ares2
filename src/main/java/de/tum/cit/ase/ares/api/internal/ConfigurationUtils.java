package de.tum.cit.ase.ares.api.internal;

import java.lang.reflect.Method;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.platform.commons.annotation.Testable;
import org.junit.platform.commons.support.AnnotationSupport;
import org.junit.platform.commons.support.HierarchyTraversalMode;
import org.junit.platform.commons.support.ReflectionSupport;

import de.tum.cit.ase.ares.api.MirrorOutput;
import de.tum.cit.ase.ares.api.MirrorOutput.MirrorOutputPolicy;
import de.tum.cit.ase.ares.api.Policy;
import de.tum.cit.ase.ares.api.PrivilegedExceptionsOnly;
import de.tum.cit.ase.ares.api.context.TestContext;
import de.tum.cit.ase.ares.api.context.TestContextUtils;
import de.tum.cit.ase.ares.api.context.TestType;
import de.tum.cit.ase.ares.api.jupiter.JupiterContext;
import de.tum.cit.ase.ares.api.jupiter.JupiterSecurityExtension;
import de.tum.cit.ase.ares.api.localization.Messages;
import de.tum.cit.ase.ares.api.policy.SecurityPolicy;
import de.tum.cit.ase.ares.api.policy.policySubComponents.SupervisedCode;
import de.tum.cit.ase.ares.api.policy.policySubComponents.TestBehaviorConfiguration;
import de.tum.cit.ase.ares.api.policy.policySubComponents.VisibilityAndScheduleConfiguration;
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
		Optional<Policy> policy = TestContextUtils.findAnnotationIn(context, Policy.class);
		if (context instanceof JupiterContext jupiter && policy.isPresent()) {
			ExtensionContext classContext = classContextOf(jupiter.getExtensionContext());
			Optional<PolicyBehavior> earlier = earlierReading(classContext, policy.get());
			if (earlier.isPresent()) {
				return earlier.get();
			}
			return policy.flatMap(ConfigurationUtils::activePolicyPath).map(path -> cached(classContext, path))
					.orElse(PolicyBehavior.NONE);
		}
		return policy.flatMap(ConfigurationUtils::activePolicyPath).map(ConfigurationUtils::read)
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
	 * Returns the active policy's hidden-test schedule and visibility, having
	 * checked that every entry names a real test class or method.
	 *
	 * @param context the test context.
	 * @return the schedule and visibility; empty when not configured.
	 * @throws IllegalArgumentException when an entry matches nothing.
	 */
	public static Optional<VisibilityAndScheduleConfiguration> findPolicyVisibilityAndSchedule(TestContext context) {
		VisibilityAndScheduleConfiguration visibility = testBehavior(context)
				.regardingTheVisibilityAndScheduleOfTheTestCases();
		if (visibility == null) {
			return Optional.empty();
		}
		ClassLoader loader = context.testClass().map(Class::getClassLoader)
				.orElseGet(() -> Thread.currentThread().getContextClassLoader());
		for (String entry : visibility.theFollowingTestsAreHidden()) {
			requireMatch("theFollowingTestsAreHidden", entry, loader);
		}
		for (String entry : visibility.theFollowingTestsArePublic()) {
			requireMatch("theFollowingTestsArePublic", entry, loader);
		}
		return Optional.of(visibility);
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
	 * Resolves whether standard output should be mirrored for a test. A hidden test
	 * never echoes.
	 *
	 * @param context the current test context
	 * @return whether mirroring is enabled
	 */
	public static boolean shouldMirrorOutput(TestContext context) {
		boolean hidden = context.findTestType().orElse(null) == TestType.HIDDEN;
		return !hidden && TestContextUtils.findAnnotationIn(context, MirrorOutput.class).map(MirrorOutput::value)
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
	 * Requires a visibility entry to name a test: a method entry a JUnit test
	 * method the class declares or inherits, a class entry a class that has one,
	 * itself, in a supertype or in a {@code @Nested} class. An entry naming
	 * anything else would never apply, so the intended test would silently keep its
	 * other visibility.
	 *
	 * @param field  the list the entry is in, for the message.
	 * @param entry  the entry.
	 * @param loader the loader of the test classes.
	 * @throws IllegalArgumentException when the entry matches nothing, or no test.
	 */
	private static void requireMatch(String field, String entry, ClassLoader loader) {
		int hash = entry.indexOf('#');
		String className = hash < 0 ? entry : entry.substring(0, hash);
		Optional<Class<?>> testClass = loadByCanonicalName(className, loader);
		boolean matches = testClass.isPresent()
				&& (hash < 0 || declaresMethod(testClass.get(), entry.substring(hash + 1)));
		if (!matches) {
			throw new IllegalArgumentException(
					Messages.localized("policy.behavior.hidden.tests.unmatched", field, entry));
		}
		boolean namesATest = hash < 0 ? holdsTest(testClass.get(), new HashSet<>())
				: hasTestMethod(testClass.get(), entry.substring(hash + 1));
		if (!namesATest) {
			throw new IllegalArgumentException(
					Messages.localized("policy.behavior.hidden.tests.not.a.test", field, entry));
		}
	}

	/**
	 * Whether a class declares or inherits a JUnit test method of that name.
	 *
	 * @param type       the class.
	 * @param methodName the method's name.
	 * @return whether such a test exists.
	 */
	private static boolean hasTestMethod(Class<?> type, String methodName) {
		return typeHierarchy(type).flatMap(current -> Stream.of(current.getDeclaredMethods()))
				.filter(method -> method.getName().equals(methodName)).anyMatch(ConfigurationUtils::isTestMethod);
	}

	/**
	 * Whether a class, its supertypes or its {@code @Nested} classes have a JUnit
	 * test method.
	 *
	 * @param type    the class.
	 * @param visited the classes already looked at.
	 * @return whether such a test exists.
	 */
	private static boolean holdsTest(Class<?> type, Set<Class<?>> visited) {
		if (!visited.add(type)) {
			return false;
		}
		boolean own = typeHierarchy(type).flatMap(current -> Stream.of(current.getDeclaredMethods()))
				.anyMatch(ConfigurationUtils::isTestMethod);
		return own || Stream.of(type.getDeclaredClasses())
				.filter(nested -> AnnotationSupport.isAnnotated(nested, Nested.class))
				.anyMatch(nested -> holdsTest(nested, visited));
	}

	/**
	 * Whether JUnit treats a method as a test: it carries {@code @Testable}, which
	 * {@code @Test}, {@code @TestFactory}, {@code @TestTemplate} and the
	 * annotations built on them carry.
	 *
	 * @param method the method.
	 * @return whether it is a test method.
	 */
	private static boolean isTestMethod(Method method) {
		return AnnotationSupport.isAnnotated(method, Testable.class);
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

	/**
	 * Returns whether a class or one of its supertypes declares a method.
	 *
	 * @param type       the class.
	 * @param methodName the method's name.
	 * @return whether it is declared.
	 */
	private static boolean declaresMethod(Class<?> type, String methodName) {
		return typeHierarchy(type).flatMap(current -> Stream.of(current.getDeclaredMethods())).map(Method::getName)
				.anyMatch(methodName::equals);
	}

	/**
	 * Returns a class and all its supertypes, interfaces included.
	 *
	 * @param type the class.
	 * @return the types, each once.
	 */
	private static Stream<Class<?>> typeHierarchy(Class<?> type) {
		Set<Class<?>> found = new LinkedHashSet<>();
		Deque<Class<?>> pending = new ArrayDeque<>(List.of(type));
		while (!pending.isEmpty()) {
			Class<?> current = pending.pop();
			if (found.add(current)) {
				Optional.ofNullable(current.getSuperclass()).ifPresent(pending::add);
				pending.addAll(List.of(current.getInterfaces()));
			}
		}
		return found.stream();
	}
}
