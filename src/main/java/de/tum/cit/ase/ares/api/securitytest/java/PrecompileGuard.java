package de.tum.cit.ase.ares.api.securitytest.java;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import javax.annotation.Nonnull;

import org.junit.jupiter.api.Test;
import org.junit.platform.launcher.LauncherSession;
import org.junit.platform.launcher.LauncherSessionListener;

import com.tngtech.archunit.core.domain.JavaClasses;

import de.tum.cit.ase.ares.api.aop.java.JavaAOPTestCaseSettings;
import de.tum.cit.ase.ares.api.architecture.java.archunit.JavaArchunitSupervisedClasses;
import de.tum.cit.ase.ares.api.localization.Messages;

/**
 * Stops a Precompile exercise before its first test when Ares would not enforce
 * the policy: the runtime checks are missing, or a generated architecture check
 * fails. Precompile copies this class into the exercise and registers it with
 * JUnit, which calls it once when a test run starts. Postcompile never uses it.
 * <p>
 * Each literal {@code de.tum.cit.ase} below is rewritten by that copy step to
 * the exercise package.
 */
public final class PrecompileGuard implements LauncherSessionListener {

	/** The package the generated checks supervise, as the copy step rewrites it. */
	private static final String SUPERVISED_PACKAGE = "de.tum.cit.ase";

	/** The generated ArchUnit test class, present when ArchUnit was chosen. */
	private static final String ARCHUNIT_CHECKS = "de.tum.cit.ase.ares.api.architecture.java.archunit.JavaArchunitTestCase";

	/** The generated WALA test class, present when WALA was chosen. */
	private static final String WALA_CHECKS = "de.tum.cit.ase.ares.api.architecture.java.wala.JavaWalaTestCase";

	/** The copied agent, present when instrumentation was chosen. */
	private static final String AGENT = "de.tum.cit.ase.ares.api.aop.java.instrumentation.JavaInstrumentationAgent";

	/** The copied weaving check, present when AspectJ was chosen. */
	private static final String WEAVING_CHECK = "de.tum.cit.ase.ares.api.aop.java.aspectj.JavaAspectJWeavingCheck";

	/** Creates the guard; JUnit calls this when it reads the registration. */
	public PrecompileGuard() {
		super();
	}

	/**
	 * Runs every check of {@link #verify()} when JUnit opens a test run, so a
	 * failure stops the run before any test, and with it any student code, starts.
	 *
	 * @param session the test run JUnit opens
	 * @throws SecurityException if Ares would not enforce the policy
	 */
	@Override
	public void launcherSessionOpened(@Nonnull LauncherSession session) {
		verify();
	}

	/**
	 * Checks that the runtime checks the policy chose are in place, then runs each
	 * generated architecture check once.
	 *
	 * @throws SecurityException naming the first check that failed
	 */
	public static void verify() {
		requireRuntimeEnforcement(generatedAopMode());
		runArchitectureChecks(architectureChecks());
	}

	/**
	 * Reads the runtime checks the generated settings name: INSTRUMENTATION or
	 * ASPECTJ.
	 *
	 * @return the name the generated settings hold
	 * @throws SecurityException if the settings cannot be read
	 */
	@Nonnull
	static String generatedAopMode() {
		try {
			Field aopMode = JavaAOPTestCaseSettings.class.getDeclaredField("aopMode");
			aopMode.setAccessible(true);
			Object value = aopMode.get(null);
			if (value instanceof String mode) {
				return mode;
			}
		} catch (ReflectiveOperationException | RuntimeException unreadable) {
			throw refusal("security.precompile.guard.settings.unreadable", unreadable,
					JavaAOPTestCaseSettings.class.getName());
		}
		throw refusal("security.precompile.guard.settings.unreadable", null, JavaAOPTestCaseSettings.class.getName());
	}

	/**
	 * Refuses unless the runtime checks are in place: for instrumentation the agent
	 * must be attached, for AspectJ every supervised class must carry the aspects.
	 *
	 * @param aopMode the runtime checks the generated settings name
	 * @throws SecurityException if they are not in place
	 */
	static void requireRuntimeEnforcement(@Nonnull String aopMode) {
		switch (aopMode) {
		case "INSTRUMENTATION" -> invokeStatic(AGENT, "activate", new Class<?>[0]);
		case "ASPECTJ" -> requireWoven(JavaArchunitSupervisedClasses.pinned(SUPERVISED_PACKAGE));
		default -> throw refusal("security.precompile.guard.settings.unreadable", null,
				JavaAOPTestCaseSettings.class.getName());
		}
	}

	/**
	 * Refuses when a supervised class was compiled without the generated aspects.
	 *
	 * @param supervisedClasses the compiled supervised classes
	 * @throws SecurityException naming the first class without them
	 */
	private static void requireWoven(@Nonnull JavaClasses supervisedClasses) {
		Object unwoven = invokeStatic(WEAVING_CHECK, "unwovenClassNames",
				new Class<?>[] { JavaClasses.class, java.util.Collection.class }, supervisedClasses, Set.of());
		if (unwoven instanceof List<?> names && !names.isEmpty()) {
			throw refusal("security.precompile.guard.aspectj.not.woven", null, names.size(), names.get(0));
		}
	}

	/**
	 * Returns the generated architecture test class. Exactly one of the two is
	 * generated, so finding none or both means the exercise is incomplete.
	 *
	 * @return the generated test class
	 * @throws SecurityException if not exactly one is present
	 */
	@Nonnull
	static Class<?> architectureChecks() {
		List<Class<?>> present = new ArrayList<>();
		for (String name : List.of(ARCHUNIT_CHECKS, WALA_CHECKS)) {
			try {
				present.add(Class.forName(name));
			} catch (ClassNotFoundException absent) {
				continue;
			}
		}
		if (present.size() != 1) {
			throw refusal("security.precompile.guard.incomplete", null, ARCHUNIT_CHECKS + ", " + WALA_CHECKS);
		}
		return present.get(0);
	}

	/**
	 * Runs each test method of the generated class, in name order and each on a new
	 * instance, and stops at the first one that fails.
	 *
	 * @param checks the generated test class
	 * @throws SecurityException naming the failed check and its message
	 */
	static void runArchitectureChecks(@Nonnull Class<?> checks) {
		List<Method> tests = new ArrayList<>();
		for (Method method : checks.getDeclaredMethods()) {
			if (method.isAnnotationPresent(Test.class)) {
				tests.add(method);
			}
		}
		tests.sort(Comparator.comparing(Method::getName));
		for (Method test : tests) {
			try {
				test.invoke(checks.getDeclaredConstructor().newInstance());
			} catch (InvocationTargetException failed) {
				throw refusal("security.precompile.guard.check.failed", failed.getCause(), test.getName(),
						String.valueOf(failed.getCause().getMessage()));
			} catch (ReflectiveOperationException | RuntimeException unusable) {
				throw refusal("security.precompile.guard.incomplete", unusable, checks.getName());
			}
		}
	}

	/**
	 * Calls a public static method of a copied class by name, since which classes
	 * are copied depends on the policy. A failure inside it is rethrown as it is
	 * when it is a security error.
	 *
	 * @param className  the copied class
	 * @param methodName the method
	 * @param types      its parameter types
	 * @param arguments  its arguments
	 * @return what the method returned
	 * @throws SecurityException if the method refused or cannot be called
	 */
	private static Object invokeStatic(@Nonnull String className, @Nonnull String methodName, @Nonnull Class<?>[] types,
			Object... arguments) {
		try {
			return Class.forName(className).getMethod(methodName, types).invoke(null, arguments);
		} catch (InvocationTargetException failed) {
			if (failed.getCause() instanceof SecurityException refused) {
				throw refused;
			}
			throw refusal("security.precompile.guard.incomplete", failed.getCause(), className);
		} catch (ReflectiveOperationException | RuntimeException | LinkageError unusable) {
			throw refusal("security.precompile.guard.incomplete", unusable, className);
		}
	}

	/**
	 * Builds the refusal for a message key.
	 *
	 * @param key       the message key
	 * @param cause     what went wrong underneath, or null
	 * @param arguments the values the message names
	 * @return the refusal to throw
	 */
	@Nonnull
	private static SecurityException refusal(@Nonnull String key, Throwable cause, Object... arguments) {
		return new SecurityException(Messages.localized(key, arguments), cause);
	}
}
