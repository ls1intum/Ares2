package de.tum.cit.ase.ares.api.internal;

import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

import de.tum.cit.ase.ares.api.MirrorOutput;
import de.tum.cit.ase.ares.api.MirrorOutput.MirrorOutputPolicy;
import de.tum.cit.ase.ares.api.Policy;
import de.tum.cit.ase.ares.api.PrivilegedExceptionsOnly;
import de.tum.cit.ase.ares.api.context.TestContext;
import de.tum.cit.ase.ares.api.context.TestContextUtils;
import de.tum.cit.ase.ares.api.context.TestType;
import de.tum.cit.ase.ares.api.jupiter.JupiterSecurityExtension;
import de.tum.cit.ase.ares.api.localization.Messages;
import de.tum.cit.ase.ares.api.policy.SecurityPolicy;
import de.tum.cit.ase.ares.api.policy.policySubComponents.HiddenTestsConfiguration;
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
		if (context.findTestType().orElse(null) == TestType.HIDDEN) {
			return false;
		}
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
	 * The hidden-test schedule and test visibility of the policy dynamically active
	 * for this test, read straight from its file, with every entry of both lists
	 * checked against the loaded classes. Empty when no active {@code @Policy}
	 * applies or the policy configures no {@code regardingHiddenTests}.
	 *
	 * @param context the current test context
	 * @return the policy's hidden-test category, if any
	 * @throws IllegalArgumentException naming the first entry that matches no class
	 *                                  or method
	 */
	public static Optional<HiddenTestsConfiguration> findPolicyHiddenTests(TestContext context) {
		Optional<Path> policyPath = activeDynamicPolicyPath(context);
		if (policyPath.isEmpty()) {
			return Optional.empty();
		}
		SecurityPolicy securityPolicy = SecurityPolicyReader.selectSecurityPolicyReader(policyPath.get())
				.readSecurityPolicyFrom(policyPath.get());
		HiddenTestsConfiguration hiddenTests = securityPolicy.regardingTheSupervisedCode()
				.theFollowingTestBehaviorIsConfiguredOrEmpty().regardingHiddenTests();
		if (hiddenTests == null) {
			return Optional.empty();
		}
		ClassLoader loader = context.testClass().map(Class::getClassLoader)
				.orElseGet(() -> Thread.currentThread().getContextClassLoader());
		for (String entry : hiddenTests.theFollowingTestsAreHidden()) {
			requireMatch("theFollowingTestsAreHidden", entry, loader);
		}
		for (String entry : hiddenTests.theFollowingTestsArePublic()) {
			requireMatch("theFollowingTestsArePublic", entry, loader);
		}
		return Optional.of(hiddenTests);
	}

	/**
	 * Fails unless an entry names a loadable class and, if it names a method, one
	 * that class or one of its supertypes declares.
	 *
	 * @param field  the list the entry is in.
	 * @param entry  the entry, {@code pkg.Class} or {@code pkg.Class#method}.
	 * @param loader the loader of the test classes.
	 * @throws IllegalArgumentException naming the list and the entry
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
	}

	/**
	 * Loads a class by its canonical name, so {@code pkg.Outer.Inner} finds the
	 * nested class {@code pkg.Outer$Inner}.
	 *
	 * @param canonicalName the name with dots only.
	 * @param loader        the loader to use.
	 * @return the class, or empty when no reading of the name loads one
	 */
	private static Optional<Class<?>> loadByCanonicalName(String canonicalName, ClassLoader loader) {
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
	 * Whether a class, one of its superclasses or one of the interfaces any of them
	 * implements declares a method of that name, the classes JUnit looks in for
	 * test methods.
	 *
	 * @param type       the class.
	 * @param methodName the method's name.
	 * @return true when one does
	 */
	private static boolean declaresMethod(Class<?> type, String methodName) {
		return typeHierarchy(type).flatMap(current -> Stream.of(current.getDeclaredMethods()))
				.anyMatch(method -> method.getName().equals(methodName));
	}

	/**
	 * A class followed by its superclasses and every interface they implement,
	 * directly or through other interfaces.
	 *
	 * @param type the class.
	 * @return the class and all its supertypes, each once
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
