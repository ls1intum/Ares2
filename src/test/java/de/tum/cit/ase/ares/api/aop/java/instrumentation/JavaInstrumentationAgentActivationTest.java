package de.tum.cit.ase.ares.api.aop.java.instrumentation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.Instrumentation;
import java.lang.instrument.UnmodifiableClassException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import net.bytebuddy.dynamic.loading.ClassInjector.UsingUnsafe.Factory;

import de.tum.cit.ase.ares.api.aop.java.instrumentation.advice.JavaInstrumentationLibraryLoadWeaveRecord;
import de.tum.cit.ase.ares.api.jupiter.JupiterSecurityExtension;

/**
 * Checks the deferred activation of the agent: the transformers are installed
 * once, on demand, never twice, and a failure keeps every later test from
 * running unguarded. It also checks how a compiled-in policy registers its
 * student package. Each test swaps in a stand-in for the JVM's instrumentation
 * and restores the agent's state and the JVM's weave record afterwards.
 */
class JavaInstrumentationAgentActivationTest {

	/**
	 * Names of the agent's static fields that one activation changes, saved before
	 * and restored after every test that swaps in a stand-in.
	 */
	private static final List<String> STATE_FIELDS = List.of("instrumentation", "classInjectorFactory", "activated",
			"activationFailure");

	/**
	 * Names of the fields of the weave record held by the JVM's own loader. Each
	 * installation attempt, the stand-in's too, seals that real record, and a
	 * sealed record ignores every later installation, so they are restored too.
	 */
	private static final List<String> WEAVE_RECORD_FIELDS = List.of("open", "marked", "sealed");

	/**
	 * Values of {@link #STATE_FIELDS} before the test.
	 */
	private final List<Object> savedState = new ArrayList<>();

	/**
	 * Values of {@link #WEAVE_RECORD_FIELDS} before the test.
	 */
	private final List<Object> savedWeaveRecord = new ArrayList<>();

	/**
	 * The packages registered for call-site rewriting before the test. The set only
	 * grows in the real agent, so a test that registers one is undone.
	 */
	private final List<String> savedCallSitePackages = new ArrayList<>();

	/**
	 * Saves the agent's activation state and the JVM's weave record.
	 *
	 * @throws ReflectiveOperationException If a field cannot be read.
	 */
	@BeforeEach
	void saveAgentState() throws ReflectiveOperationException {
		for (String name : STATE_FIELDS) {
			savedState.add(field(name).get(null));
		}
		Object weaveRecord = bootWeaveRecord();
		for (String name : WEAVE_RECORD_FIELDS) {
			savedWeaveRecord.add(weaveRecordField(name).get(weaveRecord));
		}
		savedCallSitePackages.addAll(callSitePackages());
	}

	/**
	 * Restores the agent's activation state and the JVM's weave record and drops
	 * the failures the stand-in provoked, so no later test inherits them.
	 *
	 * @throws ReflectiveOperationException If a field cannot be written.
	 */
	@AfterEach
	void restoreAgentState() throws ReflectiveOperationException {
		for (int i = 0; i < STATE_FIELDS.size(); i++) {
			field(STATE_FIELDS.get(i)).set(null, savedState.get(i));
		}
		Object weaveRecord = bootWeaveRecord();
		for (int i = 0; i < WEAVE_RECORD_FIELDS.size(); i++) {
			weaveRecordField(WEAVE_RECORD_FIELDS.get(i)).set(weaveRecord, savedWeaveRecord.get(i));
		}
		((AtomicReference<?>) field("PERMANENT_FAILURE").get(null)).set(null);
		((AtomicReference<?>) field("TRANSFORMATION_FAILURE").get(null)).set(null);
		Set<String> packages = callSitePackages();
		packages.clear();
		packages.addAll(savedCallSitePackages);
	}

	/**
	 * A policy compiled in by Precompile has exactly its student package, and no
	 * other, added to the packages whose calls are rewritten, as Postcompile's
	 * runner does.
	 *
	 * @throws ReflectiveOperationException If the agent or the settings cannot be
	 *                                      reached.
	 */
	@Test
	void compiledInPolicyRegistersItsStudentPackage() throws ReflectiveOperationException {
		deactivate(standIn(true));
		String studentPackage = uniquePackage();
		Set<String> expected = new HashSet<>(callSitePackages());
		expected.add(studentPackage);
		try {
			compileIn("INSTRUMENTATION", studentPackage);
			registerCompiledRestrictedPackage().invoke(null);
			assertEquals(expected, Set.copyOf(callSitePackages()));
		} finally {
			JupiterSecurityExtension.resetSettingsInBootstrapClassLoader();
		}
	}

	/**
	 * For a compiled-in mode with a blank, whitespace-only or missing package name,
	 * the start-up step throws a SecurityException and registers nothing.
	 *
	 * @throws ReflectiveOperationException If the agent or the settings cannot be
	 *                                      reached.
	 */
	@Test
	void compiledInPolicyWithoutAPackageFailsStartUp() throws ReflectiveOperationException {
		deactivate(standIn(true));
		Set<String> before = Set.copyOf(callSitePackages());
		try {
			for (String studentPackage : new String[] { "", "  ", null }) {
				compileIn("INSTRUMENTATION", studentPackage);
				InvocationTargetException failure = assertThrows(InvocationTargetException.class,
						() -> registerCompiledRestrictedPackage().invoke(null));
				assertTrue(failure.getCause() instanceof SecurityException, String.valueOf(failure.getCause()));
				assertEquals(before, Set.copyOf(callSitePackages()));
			}
		} finally {
			JupiterSecurityExtension.resetSettingsInBootstrapClassLoader();
		}
	}

	/**
	 * Settings without a mode, or with AspectJ, register nothing. Start-up never
	 * reaches these cases, because then no instrumentation policy counts as
	 * compiled in; they guard the method on its own.
	 *
	 * @throws ReflectiveOperationException If the agent or the settings cannot be
	 *                                      reached.
	 */
	@Test
	void settingsWithoutAnInstrumentationModeRegisterNothing() throws ReflectiveOperationException {
		deactivate(standIn(true));
		Set<String> before = Set.copyOf(callSitePackages());
		try {
			for (String mode : new String[] { null, "ASPECTJ" }) {
				compileIn(mode, uniquePackage());
				registerCompiledRestrictedPackage().invoke(null);
				assertEquals(before, Set.copyOf(callSitePackages()));
			}
		} finally {
			JupiterSecurityExtension.resetSettingsInBootstrapClassLoader();
		}
	}

	/**
	 * The first activation registers the pointcut transformer and the thread
	 * call-site transformer; a second one registers nothing.
	 *
	 * @throws ReflectiveOperationException If the agent state cannot be set.
	 */
	@Test
	void activationInstallsEachTransformerExactlyOnce() throws ReflectiveOperationException {
		Instrumentation instrumentation = standIn(true);
		deactivate(instrumentation);

		JavaInstrumentationAgent.activate();
		JavaInstrumentationAgent.activate();

		verify(instrumentation, times(2)).addTransformer(any(ClassFileTransformer.class), anyBoolean());
	}

	/**
	 * Threads that activate at the same time still install the transformers only
	 * once, and every one of them returns only after they are installed.
	 *
	 * @throws Exception If the agent state cannot be set or a thread fails.
	 */
	@Test
	void concurrentActivationInstallsOnce() throws Exception {
		Instrumentation instrumentation = standIn(true);
		deactivate(instrumentation);
		int threads = 8;
		CountDownLatch start = new CountDownLatch(1);
		ExecutorService executor = Executors.newFixedThreadPool(threads);
		try {
			List<Future<Boolean>> results = new ArrayList<>();
			Callable<Boolean> activation = () -> {
				start.await();
				JavaInstrumentationAgent.activate();
				return (Boolean) field("activated").get(null);
			};
			for (int i = 0; i < threads; i++) {
				results.add(executor.submit(activation));
			}
			start.countDown();
			for (Future<Boolean> result : results) {
				assertTrue(result.get());
			}
		} finally {
			executor.shutdownNow();
		}
		verify(instrumentation, times(2)).addTransformer(any(ClassFileTransformer.class), anyBoolean());
	}

	/**
	 * A failed installation fails closed and withdraws its transformer, and every
	 * later attempt fails closed too, without trying to install a second time.
	 *
	 * @throws ReflectiveOperationException If the agent state cannot be set.
	 */
	@Test
	void failedActivationFailsClosedEveryTime() throws ReflectiveOperationException {
		Instrumentation instrumentation = standIn(false);
		deactivate(instrumentation);

		SecurityException first = assertThrows(SecurityException.class, JavaInstrumentationAgent::activate);
		SecurityException second = assertThrows(SecurityException.class, JavaInstrumentationAgent::activate);

		assertSame(first, second.getCause());
		assertFalse((Boolean) field("activated").get(null));
		verify(instrumentation, times(1)).addTransformer(any(ClassFileTransformer.class), anyBoolean());
		verify(instrumentation, times(1)).removeTransformer(any(ClassFileTransformer.class));
	}

	/**
	 * Activation without an attached agent fails closed instead of leaving the
	 * policy unenforced.
	 *
	 * @throws ReflectiveOperationException If the agent state cannot be set.
	 */
	@Test
	void activationWithoutAgentFailsClosed() throws ReflectiveOperationException {
		deactivate(null);

		assertThrows(SecurityException.class, JavaInstrumentationAgent::activate);
		assertFalse((Boolean) field("activated").get(null));
	}

	/**
	 * Start-up installs the transformers at once only if the settings already name
	 * an AOP mode, as a policy compiled in by Precompile does.
	 *
	 * @throws ReflectiveOperationException If the settings cannot be reached.
	 */
	@Test
	void onlyCompiledInPolicyInstallsAtStartUp() throws ReflectiveOperationException {
		Method decision = JavaInstrumentationAgent.class.getDeclaredMethod("isPolicyCompiledIn");
		decision.setAccessible(true);
		Field aopMode = Class.forName("de.tum.cit.ase.ares.api.aop.java.JavaAOPTestCaseSettings", true, null)
				.getDeclaredField("aopMode");
		aopMode.setAccessible(true);
		try {
			JupiterSecurityExtension.resetSettingsInBootstrapClassLoader();
			assertFalse((Boolean) decision.invoke(null));
			aopMode.set(null, "INSTRUMENTATION");
			assertTrue((Boolean) decision.invoke(null));
		} finally {
			JupiterSecurityExtension.resetSettingsInBootstrapClassLoader();
		}
	}

	/**
	 * A class the JVM refuses to retransform during activation fails the
	 * activation, and every later one, instead of being reported once and then left
	 * unguarded.
	 *
	 * @throws Exception If the agent state cannot be set.
	 */
	@Test
	void refusedRetransformationFailsActivationEveryTime() throws Exception {
		Instrumentation instrumentation = standIn(true);
		when(instrumentation.getAllLoadedClasses()).thenReturn(new Class<?>[] { java.io.File.class });
		when(instrumentation.isModifiableClass(any())).thenReturn(true);
		doThrow(new UnmodifiableClassException("deliberate test failure")).when(instrumentation)
				.retransformClasses(any());
		deactivate(instrumentation);

		SecurityException first = assertThrows(SecurityException.class, JavaInstrumentationAgent::activate);
		SecurityException second = assertThrows(SecurityException.class, JavaInstrumentationAgent::activate);

		assertSame(first, second.getCause());
		assertTrue(first.getMessage().contains("java.io.File"));
		assertFalse((Boolean) field("activated").get(null));
	}

	/**
	 * A failure during activation still fails it when another test consumes the
	 * per-test failure report while the installation is running.
	 *
	 * @throws Exception If the agent state cannot be set.
	 */
	@Test
	void failureConsumedByAnotherTestStillFailsActivation() throws Exception {
		Instrumentation instrumentation = standIn(true);
		when(instrumentation.getAllLoadedClasses()).thenReturn(new Class<?>[] { java.io.File.class });
		when(instrumentation.isModifiableClass(any())).thenReturn(true);
		doThrow(new UnmodifiableClassException("deliberate test failure")).when(instrumentation)
				.retransformClasses(any());
		AtomicBoolean consumed = new AtomicBoolean();
		doAnswer(invocation -> {
			try {
				JavaInstrumentationAgent.throwIfTransformationFailed();
			} catch (SecurityException consumedByAnotherTest) {
				consumed.set(true);
			}
			return null;
		}).when(instrumentation).addTransformer(any(ClassFileTransformer.class), anyBoolean());
		deactivate(instrumentation);

		assertThrows(SecurityException.class, JavaInstrumentationAgent::activate);
		assertTrue(consumed.get(), "the per-test report must have been consumed during installation");
		assertFalse((Boolean) field("activated").get(null));
	}

	/**
	 * A transformation failure recorded after a successful activation, for example
	 * by a class-loading thread that raced the installation, fails every later
	 * activation, not only the next test's report.
	 *
	 * @throws ReflectiveOperationException If the agent state cannot be set.
	 */
	@Test
	void transformationFailureAfterActivationFailsEveryLaterActivation() throws ReflectiveOperationException {
		Instrumentation instrumentation = standIn(true);
		deactivate(instrumentation);
		JavaInstrumentationAgent.activate();
		Class<?> failureType = Class.forName(JavaInstrumentationAgent.class.getName() + "$TransformationFailure");
		Constructor<?> failureConstructor = failureType.getDeclaredConstructor(String.class, Throwable.class);
		failureConstructor.setAccessible(true);
		Method record = JavaInstrumentationAgent.class.getDeclaredMethod("recordFailure", failureType);
		record.setAccessible(true);

		record.invoke(null,
				failureConstructor.newInstance("example.Late", new IllegalStateException("deliberate test failure")));

		assertThrows(SecurityException.class, JavaInstrumentationAgent::activate);
		assertThrows(SecurityException.class, JavaInstrumentationAgent::activate);
		assertThrows(SecurityException.class, JavaInstrumentationAgent::throwIfActivationFailed);
	}

	/**
	 * Puts the agent back into its state before activation, with the given
	 * instrumentation.
	 *
	 * @param instrumentation The instrumentation the agent should use, or null for
	 *                        no agent.
	 * @throws ReflectiveOperationException If a field cannot be written.
	 */
	private static void deactivate(Instrumentation instrumentation) throws ReflectiveOperationException {
		field("instrumentation").set(null, instrumentation);
		field("classInjectorFactory").set(null, instrumentation == null ? null : mock(Factory.class));
		field("activated").set(null, false);
		field("activationFailure").set(null, null);
		((AtomicReference<?>) field("PERMANENT_FAILURE").get(null)).set(null);
	}

	/**
	 * Creates an instrumentation stand-in that reports no loaded classes.
	 *
	 * @param retransformationSupported Whether it claims to support retransforming
	 *                                  classes; without it installing fails.
	 * @return The stand-in.
	 */
	private static Instrumentation standIn(boolean retransformationSupported) {
		Instrumentation instrumentation = mock(Instrumentation.class);
		when(instrumentation.isRetransformClassesSupported()).thenReturn(retransformationSupported);
		when(instrumentation.getAllLoadedClasses()).thenReturn(new Class<?>[0]);
		return instrumentation;
	}

	/**
	 * Opens one static field of the agent.
	 *
	 * @param name The field name.
	 * @return The accessible field.
	 * @throws NoSuchFieldException If the agent has no such field.
	 */
	private static Field field(String name) throws NoSuchFieldException {
		Field field = JavaInstrumentationAgent.class.getDeclaredField(name);
		field.setAccessible(true);
		return field;
	}

	/**
	 * The agent's live set of packages whose calls are rewritten.
	 *
	 * @return The set itself, not a copy.
	 * @throws ReflectiveOperationException If the field cannot be read.
	 */
	@SuppressWarnings("unchecked")
	private static Set<String> callSitePackages() throws ReflectiveOperationException {
		return (Set<String>) field("INSTRUMENTED_CALL_SITE_PACKAGES").get(null);
	}

	/**
	 * A package name no other test or class uses.
	 *
	 * @return The name.
	 */
	private static String uniquePackage() {
		return "example.x." + System.nanoTime();
	}

	/**
	 * Writes a mode and a student package into the settings copy on the boot class
	 * loader, as a policy compiled in by Precompile has them.
	 *
	 * @param mode           The AOP mode, or null.
	 * @param studentPackage The student package, or null.
	 * @throws ReflectiveOperationException If the settings cannot be reached.
	 */
	private static void compileIn(String mode, String studentPackage) throws ReflectiveOperationException {
		Class<?> settings = Class.forName("de.tum.cit.ase.ares.api.aop.java.JavaAOPTestCaseSettings", true, null);
		Field aopMode = settings.getDeclaredField("aopMode");
		aopMode.setAccessible(true);
		aopMode.set(null, mode);
		Field restrictedPackage = settings.getDeclaredField("restrictedPackage");
		restrictedPackage.setAccessible(true);
		restrictedPackage.set(null, studentPackage);
	}

	/**
	 * The agent's start-up step that registers a compiled-in student package.
	 *
	 * @return The method, made accessible.
	 * @throws NoSuchMethodException If the method is missing.
	 */
	private static Method registerCompiledRestrictedPackage() throws NoSuchMethodException {
		Method method = JavaInstrumentationAgent.class.getDeclaredMethod("registerCompiledRestrictedPackage");
		method.setAccessible(true);
		return method;
	}

	/**
	 * Returns the weave record held by the JVM's own loader, the one activation
	 * opens and seals.
	 *
	 * @return The record.
	 * @throws ReflectiveOperationException If the record cannot be reached.
	 */
	private static Object bootWeaveRecord() throws ReflectiveOperationException {
		return weaveRecordField("INSTALLATION").get(null);
	}

	/**
	 * Opens one field of the copy of the weave record class held by the JVM's own
	 * loader.
	 *
	 * @param name The field name.
	 * @return The accessible field.
	 * @throws ReflectiveOperationException If that copy or field cannot be found.
	 */
	private static Field weaveRecordField(String name) throws ReflectiveOperationException {
		Field field = Class.forName(JavaInstrumentationLibraryLoadWeaveRecord.class.getName(), true, null)
				.getDeclaredField(name);
		field.setAccessible(true);
		return field;
	}
}
