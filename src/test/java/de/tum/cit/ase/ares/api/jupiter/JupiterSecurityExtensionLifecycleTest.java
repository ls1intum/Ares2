package de.tum.cit.ase.ares.api.jupiter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectClass;
import static org.junit.platform.testkit.engine.EventConditions.container;
import static org.junit.platform.testkit.engine.EventConditions.event;
import static org.junit.platform.testkit.engine.EventConditions.finishedWithFailure;
import static org.junit.platform.testkit.engine.EventConditions.test;
import static org.junit.platform.testkit.engine.TestExecutionResultConditions.instanceOf;
import static org.junit.platform.testkit.engine.TestExecutionResultConditions.message;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.InvocationInterceptor;
import org.junit.jupiter.api.extension.InvocationInterceptor.Invocation;
import org.junit.jupiter.api.extension.ReflectiveInvocationContext;
import org.junit.jupiter.api.extension.TestInstanceFactory;
import org.junit.jupiter.api.extension.TestInstanceFactoryContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.platform.testkit.engine.EngineExecutionResults;
import org.junit.platform.testkit.engine.EngineTestKit;

class JupiterSecurityExtensionLifecycleTest {
	private static final AtomicInteger ENGINE_PREPARATIONS = new AtomicInteger();

	public static class CountingSecurityExtension extends JupiterSecurityExtension {
		@Override
		void prepareSecurity(ExtensionContext context) {
			if (context.getTestMethod().isPresent()) {
				ENGINE_PREPARATIONS.incrementAndGet();
			}
		}
	}

	@ExtendWith(CountingSecurityExtension.class)
	static class EngineFixture {
		@Test
		void ordinary() {
		}

		@org.junit.jupiter.api.RepeatedTest(3)
		void repeated() {
		}

		@ParameterizedTest
		@ValueSource(ints = { 1, 2, 3 })
		void parameterised(int value) {
			assertEquals(value, value);
		}
	}

	@Test
	void callbackAndInterceptorPrepareExactlyOncePerInvocation() throws Throwable {
		AtomicInteger preparations = new AtomicInteger();
		JupiterSecurityExtension extension = new JupiterSecurityExtension() {
			@Override
			void prepareSecurity(ExtensionContext context) {
				preparations.incrementAndGet();
			}
		};
		ExtensionContext first = contextWithStore();
		extension.beforeTestExecution(first);
		Invocation<Void> invocation = () -> null;
		extension.interceptGenericInvocation(invocation, first, Optional.empty());
		extension.afterTestExecution(first);
		assertEquals(1, preparations.get());

		ExtensionContext repeatedInvocation = contextWithStore();
		extension.beforeTestExecution(repeatedInvocation);
		extension.interceptGenericInvocation(invocation, repeatedInvocation, Optional.empty());
		assertEquals(2, preparations.get());
	}

	/**
	 * Every invocation of an ordinary, a repeated and a parameterised test prepares
	 * once for its constructor and once for its test.
	 */
	@Test
	void engineTestKitPreparesOrdinaryRepeatedAndParameterisedInvocationsExactlyOnce() {
		ENGINE_PREPARATIONS.set(0);
		EngineTestKit.engine("junit-jupiter").selectors(selectClass(EngineFixture.class)).execute().testEvents()
				.assertStatistics(statistics -> statistics.started(7).succeeded(7).failed(0));
		assertEquals(14, ENGINE_PREPARATIONS.get(),
				"each of the 7 invocations prepares once for its constructor and once for its test");
	}

	@Test
	void failedPreparationResetsPartiallyPublishedSecuritySettings() throws ReflectiveOperationException {
		JupiterSecurityExtension extension = new JupiterSecurityExtension() {
			@Override
			void prepareSecurity(ExtensionContext context) {
				setRestrictedPackage("stale.package");
				throw new IllegalStateException("deliberate preparation failure");
			}
		};

		assertThrows(IllegalStateException.class, () -> extension.beforeTestExecution(contextWithStore()));
		assertNull(restrictedPackageField().get(null));
	}

	/** JUnit setting that lets tests run in several threads at once. */
	private static final String PARALLEL_EXECUTION_ENABLED = "junit.jupiter.execution.parallel.enabled";

	/** Marker standing for the policy another test left armed. */
	private static final String OTHER_TEST_POLICY = "armed by another test";

	/** Whether the guard was armed, recorded by each phase of a fixture. */
	private static final List<String> OBSERVATIONS = new CopyOnWriteArrayList<>();

	/** Arms a marker setting on prepare, so a fixture can see the guard. */
	public static class ArmingSecurityExtension extends JupiterSecurityExtension {
		@Override
		void prepareSecurity(ExtensionContext context) {
			if (context.getTestMethod().isPresent()) {
				ENGINE_PREPARATIONS.incrementAndGet();
			}
			setRestrictedPackage("armed");
		}
	}

	/** Records whether the guard is armed during the named phase. */
	private static void observe(String phase) {
		try {
			OBSERVATIONS.add(phase + "=" + (restrictedPackageField().get(null) != null));
		} catch (ReflectiveOperationException exception) {
			throw new AssertionError(exception);
		}
	}

	/**
	 * Cannot arm the guard for a test method, as when the policy analysis fails.
	 */
	public static class FailingPreparationExtension extends JupiterSecurityExtension {
		@Override
		void prepareSecurity(ExtensionContext context) {
			if (context.getTestMethod().isPresent()) {
				throw new SecurityException("deliberate preparation failure");
			}
		}
	}

	/** Has a disabled test, which JUnit skips after it has built the instance. */
	@ExtendWith(FailingPreparationExtension.class)
	static class DisabledTestFixture {
		/** Must be skipped, not failed by the failing preparation. */
		@Disabled
		@Test
		void test() {
			observe("test");
		}
	}

	/** Has an enabled test, whose start must report the failing preparation. */
	@ExtendWith(FailingPreparationExtension.class)
	static class EnabledTestFixture {
		/** Must fail, because its guard cannot be armed. */
		@Test
		void test() {
			observe("test");
		}
	}

	/** Observes the guard in every phase of one test per method. */
	@ExtendWith(ArmingSecurityExtension.class)
	static class SetupPhasesFixture {
		/** Observes the constructor phase. */
		SetupPhasesFixture() {
			observe("constructor");
		}

		/** Observes the guard in the other phases. */
		@BeforeAll
		static void beforeAll() {
			observe("beforeAll");
		}

		@BeforeEach
		void beforeEach() {
			observe("beforeEach");
		}

		@Test
		void test() {
			observe("test");
		}

		@AfterEach
		void afterEach() {
			observe("afterEach");
		}

		@AfterAll
		static void afterAll() {
			observe("afterAll");
		}
	}

	/** Observes the constructor, the setup method and the test of one test. */
	@ExtendWith(ArmingSecurityExtension.class)
	static class ConstructorSetupAndTestFixture {
		/** Observes the constructor phase. */
		ConstructorSetupAndTestFixture() {
			observe("constructor");
		}

		/** Observes the setup method. */
		@BeforeEach
		void beforeEach() {
			observe("beforeEach");
		}

		/** Observes the test. */
		@Test
		void test() {
			observe("test");
		}
	}

	/** Builds test instances itself, so JUnit never calls their constructor. */
	public static class ObservingInstanceFactory implements TestInstanceFactory {
		/** Records that it ran, then builds the instance. */
		@Override
		public Object createTestInstance(TestInstanceFactoryContext factoryContext, ExtensionContext extensionContext) {
			observe("factory");
			return new FactoryBuiltFixture();
		}
	}

	/** Has its instance built by a factory instead of by its constructor. */
	@ExtendWith({ ArmingSecurityExtension.class, ObservingInstanceFactory.class })
	static class FactoryBuiltFixture {
		/** Observes the test. */
		@Test
		void test() {
			observe("test");
		}
	}

	/** Observes the guard when one instance serves two tests. */
	@ExtendWith(ArmingSecurityExtension.class)
	@TestInstance(TestInstance.Lifecycle.PER_CLASS)
	static class PerClassFixture {
		PerClassFixture() {
			observe("constructor");
		}

		@BeforeAll
		void beforeAll() {
			observe("beforeAll");
		}

		@BeforeEach
		void beforeEach() {
			observe("beforeEach");
		}

		@Test
		void first() {
			observe("first");
		}

		@Test
		void second() {
			observe("second");
		}
	}

	/** Throws from the setup method to see whether the guard is closed. */
	@ExtendWith(ArmingSecurityExtension.class)
	static class FailingSetupFixture {
		@BeforeEach
		void beforeEach() {
			observe("beforeEach");
			throw new IllegalStateException("deliberate setup failure");
		}

		@Test
		void test() {
			observe("test");
		}
	}

	/** Aborts from the setup method to see whether the guard is closed. */
	@ExtendWith(ArmingSecurityExtension.class)
	static class AbortedSetupFixture {
		@BeforeEach
		void beforeEach() {
			Assumptions.assumeTrue(false);
		}

		@Test
		void test() {
			observe("test");
		}
	}

	/**
	 * Wraps the setup methods of a fixture from outside and fails them once they
	 * have run.
	 */
	public static class FailingOuterInterceptor implements InvocationInterceptor {
		@Override
		public void interceptBeforeEachMethod(Invocation<Void> invocation,
				ReflectiveInvocationContext<Method> invocationContext, ExtensionContext extensionContext)
				throws Throwable {
			invocation.proceed();
			throw new IllegalStateException("deliberate failure after the setup method returned");
		}
	}

	/**
	 * Fails the setup method from an interceptor that sits outside Ares, after Ares
	 * has returned.
	 */
	@ExtendWith({ FailingOuterInterceptor.class, ArmingSecurityExtension.class })
	static class OuterInterceptorFailureFixture {
		/** Observes the setup method. */
		@BeforeEach
		void beforeEach() {
			observe("beforeEach");
		}

		/** Must not run, because the setup method failed. */
		@Test
		void test() {
			observe("test");
		}
	}

	/** Declares a setup method that a subclass inherits. */
	static class InheritedSetup {
		/** Observes the inherited setup method. */
		@BeforeEach
		void inheritedSetup() {
			observe("inheritedSetup");
		}
	}

	/** Has an inherited and an own setup method. */
	@ExtendWith(ArmingSecurityExtension.class)
	static class MultipleSetupFixture extends InheritedSetup {
		/** Observes the own setup method. */
		@BeforeEach
		void ownSetup() {
			observe("ownSetup");
		}

		/** Observes the test. */
		@Test
		void test() {
			observe("test");
		}
	}

	/** Has a nested class with its own setup method and test. */
	@ExtendWith(ArmingSecurityExtension.class)
	static class NestedFixture {
		/** The nested class whose phases are observed. */
		@Nested
		class Inner {
			/** Observes the setup method of the nested class. */
			@BeforeEach
			void innerBeforeEach() {
				observe("innerBeforeEach");
			}

			/** Observes the test of the nested class. */
			@Test
			void innerTest() {
				observe("innerTest");
			}
		}
	}

	/** Runs one fixture alone with a closed guard and empty records. */
	private static EngineExecutionResults runFixture(Class<?> fixture) throws ReflectiveOperationException {
		return runFixture(fixture, Map.of(), null);
	}

	/**
	 * Runs one fixture alone as {@link #runFixture(Class)} does, with JUnit
	 * settings and the given setting left armed beforehand.
	 */
	private static EngineExecutionResults runFixture(Class<?> fixture, Map<String, String> configurationParameters,
			String armedBefore) throws ReflectiveOperationException {
		OBSERVATIONS.clear();
		ENGINE_PREPARATIONS.set(0);
		restrictedPackageField().set(null, armedBefore);
		return EngineTestKit.engine("junit-jupiter").selectors(selectClass(fixture))
				.configurationParameters(configurationParameters).execute();
	}

	/** Every phase sees an armed guard, and it is closed at the end. */
	@Test
	void setupAndTeardownMethodsRunWithTheGuardArmed() throws ReflectiveOperationException {
		runFixture(SetupPhasesFixture.class);

		assertEquals(List.of("beforeAll=true", "constructor=true", "beforeEach=true", "test=true", "afterEach=true",
				"afterAll=true"), OBSERVATIONS);
		assertNull(restrictedPackageField().get(null), "the guard must be closed once the class has finished");
	}

	/** A shared instance must not leave later tests unguarded. */
	@Test
	void everyTestOfAPerClassInstanceStillRunsWithTheGuardArmed() throws ReflectiveOperationException {
		runFixture(PerClassFixture.class);

		assertTrue(OBSERVATIONS.contains("constructor=true"), OBSERVATIONS.toString());
		assertTrue(OBSERVATIONS.contains("beforeAll=true"), OBSERVATIONS.toString());
		assertEquals(2, OBSERVATIONS.stream().filter("beforeEach=true"::equals).count(), OBSERVATIONS.toString());
		assertTrue(OBSERVATIONS.contains("first=true"), OBSERVATIONS.toString());
		assertTrue(OBSERVATIONS.contains("second=true"), OBSERVATIONS.toString());
		assertNull(restrictedPackageField().get(null));
	}

	/** A failing setup method must close the guard. */
	@Test
	void setupMethodThatThrowsDoesNotLeaveTheGuardArmed() throws ReflectiveOperationException {
		runFixture(FailingSetupFixture.class).testEvents()
				.assertStatistics(statistics -> statistics.started(1).failed(1));

		assertEquals(List.of("beforeEach=true"), OBSERVATIONS);
		assertNull(restrictedPackageField().get(null), "a failed setup method must still close the guard");
	}

	/** An aborted setup method must close the guard. */
	@Test
	void setupMethodThatAbortsDoesNotLeaveTheGuardArmed() throws ReflectiveOperationException {
		runFixture(AbortedSetupFixture.class).testEvents()
				.assertStatistics(statistics -> statistics.started(1).aborted(1));

		assertEquals(List.of(), OBSERVATIONS);
		assertEquals(2, ENGINE_PREPARATIONS.get(), "the constructor and the setup method must both have been armed");
		assertNull(restrictedPackageField().get(null), "an aborted setup method must still close the guard");
	}

	/** The test method reuses the preparation done for its setup method. */
	@Test
	void armingBeforeEachDoesNotPrepareAgainWhenTheTestMethodStarts() throws ReflectiveOperationException {
		runFixture(PerClassFixture.class);

		assertEquals(2, ENGINE_PREPARATIONS.get(), "exactly one preparation per test method, shared with beforeEach");
	}

	/**
	 * A test that JUnit skips is not failed by a policy that cannot be applied
	 * while its instance is built.
	 */
	@Test
	void skippedTestIsNotFailedByAFailingPreparation() throws ReflectiveOperationException {
		runFixture(DisabledTestFixture.class).testEvents()
				.assertStatistics(statistics -> statistics.skipped(1).failed(0));

		assertEquals(List.of(), OBSERVATIONS);
	}

	/**
	 * The failing preparation still fails a test that runs, and the test body does
	 * not run.
	 */
	@Test
	void failingPreparationStillFailsATestThatRuns() throws ReflectiveOperationException {
		runFixture(EnabledTestFixture.class).testEvents()
				.assertStatistics(statistics -> statistics.started(1).failed(1));

		assertEquals(List.of(), OBSERVATIONS);
		assertNull(restrictedPackageField().get(null));
	}

	/**
	 * An interceptor outside this extension that fails a setup method must not
	 * leave the guard armed.
	 */
	@Test
	void outerInterceptorFailingTheSetupMethodDoesNotLeaveTheGuardArmed() throws ReflectiveOperationException {
		runFixture(OuterInterceptorFailureFixture.class).testEvents()
				.assertStatistics(statistics -> statistics.started(1).failed(1));

		assertEquals(List.of("beforeEach=true"), OBSERVATIONS);
		assertNull(restrictedPackageField().get(null), "the guard must be closed after the test has failed");
	}

	/**
	 * Several setup methods, inherited ones included, all run armed and share one
	 * preparation.
	 */
	@Test
	void inheritedAndOwnSetupMethodsRunArmedWithOnePreparation() throws ReflectiveOperationException {
		runFixture(MultipleSetupFixture.class);

		assertEquals(List.of("inheritedSetup=true", "ownSetup=true", "test=true"), OBSERVATIONS);
		assertEquals(2, ENGINE_PREPARATIONS.get(),
				"one for the constructor, one shared by the setup methods and the test");
		assertNull(restrictedPackageField().get(null));
	}

	/**
	 * A nested class runs its setup method and test armed, and closes the guard.
	 */
	@Test
	void nestedClassRunsArmedAndClosesTheGuard() throws ReflectiveOperationException {
		runFixture(NestedFixture.class).testEvents().assertStatistics(statistics -> statistics.succeeded(1));

		assertEquals(List.of("innerBeforeEach=true", "innerTest=true"), OBSERVATIONS);
		assertNull(restrictedPackageField().get(null));
	}

	/**
	 * Enabled parallel execution, spelled in any case JUnit accepts, fails the test
	 * before its constructor, setup method or body runs.
	 */
	@ParameterizedTest
	@ValueSource(strings = { "true", "TRUE" })
	void enabledParallelExecutionIsRefusedBeforeTheConstructorRuns(String enabled) throws ReflectiveOperationException {
		assertRefusedBeforeAnyPhaseRuns(ConstructorSetupAndTestFixture.class, enabled);
	}

	/**
	 * Enabled parallel execution fails the test before a test instance factory
	 * builds its instance.
	 */
	@Test
	void enabledParallelExecutionIsRefusedBeforeATestInstanceFactoryRuns() throws ReflectiveOperationException {
		assertRefusedBeforeAnyPhaseRuns(FactoryBuiltFixture.class, "true");
	}

	/**
	 * Runs the fixture with parallel execution set as given, while another test's
	 * setting is armed, and checks that its one test failed with the refusal, that
	 * no phase ran and that nothing was reset or armed.
	 */
	private static void assertRefusedBeforeAnyPhaseRuns(Class<?> fixture, String enabled)
			throws ReflectiveOperationException {
		try {
			runFixture(fixture, Map.of(PARALLEL_EXECUTION_ENABLED, enabled), OTHER_TEST_POLICY).testEvents()
					.assertThatEvents()
					.haveExactly(1, event(test(), finishedWithFailure(instanceOf(SecurityException.class),
							message(text -> text.contains(PARALLEL_EXECUTION_ENABLED)))));

			assertEquals(List.of(), OBSERVATIONS);
			assertEquals(0, ENGINE_PREPARATIONS.get(), "the guard must never have been armed");
			assertEquals(OTHER_TEST_POLICY, restrictedPackageField().get(null),
					"the setting of another test must be neither reset nor replaced");
		} finally {
			restrictedPackageField().set(null, null);
		}
	}

	/**
	 * Enabled parallel execution fails the class before its class setup and
	 * teardown methods run.
	 */
	@Test
	void enabledParallelExecutionIsRefusedBeforeTheClassSetupMethodRuns() throws ReflectiveOperationException {
		try {
			runFixture(SetupPhasesFixture.class, Map.of(PARALLEL_EXECUTION_ENABLED, "true"), OTHER_TEST_POLICY)
					.containerEvents().assertThatEvents().haveExactly(1, event(container(SetupPhasesFixture.class),
							finishedWithFailure(instanceOf(SecurityException.class))));

			assertEquals(List.of(), OBSERVATIONS);
			assertEquals(0, ENGINE_PREPARATIONS.get(), "the guard must never have been armed");
			assertEquals(OTHER_TEST_POLICY, restrictedPackageField().get(null),
					"the setting of another test must be neither reset nor replaced");
		} finally {
			restrictedPackageField().set(null, null);
		}
	}

	/**
	 * Parallel execution set to false, or to a value JUnit reads as false, leaves
	 * every phase armed as when the setting is absent.
	 */
	@ParameterizedTest
	@ValueSource(strings = { "false", "yes" })
	void disabledParallelExecutionLeavesEveryPhaseArmed(String enabled) throws ReflectiveOperationException {
		runFixture(SetupPhasesFixture.class, Map.of(PARALLEL_EXECUTION_ENABLED, enabled), null).testEvents()
				.assertStatistics(statistics -> statistics.started(1).succeeded(1));

		assertEquals(List.of("beforeAll=true", "constructor=true", "beforeEach=true", "test=true", "afterEach=true",
				"afterAll=true"), OBSERVATIONS);
		assertNull(restrictedPackageField().get(null));
	}

	private static void setRestrictedPackage(String value) {
		try {
			restrictedPackageField().set(null, value);
		} catch (ReflectiveOperationException exception) {
			throw new AssertionError(exception);
		}
	}

	private static Field restrictedPackageField() throws ReflectiveOperationException {
		Class<?> settings = Class.forName("de.tum.cit.ase.ares.api.aop.java.JavaAOPTestCaseSettings");
		Field field = settings.getDeclaredField("restrictedPackage");
		field.setAccessible(true);
		return field;
	}

	private ExtensionContext contextWithStore() {
		ExtensionContext context = mock(ExtensionContext.class);
		Map<Object, Object> values = new HashMap<>();
		ExtensionContext.Store store = new ExtensionContext.Store() {
			@Override
			public Object get(Object key) {
				return values.get(key);
			}

			@Override
			public <V> V get(Object key, Class<V> requiredType) {
				return requiredType.cast(values.get(key));
			}

			@Override
			public <K, V> Object getOrComputeIfAbsent(K key, Function<? super K, ? extends V> creator) {
				return values.computeIfAbsent(key, ignored -> creator.apply(key));
			}

			@Override
			public <K, V> Object computeIfAbsent(K key, Function<? super K, ? extends V> creator) {
				return getOrComputeIfAbsent(key, creator);
			}

			@Override
			public <K, V> V getOrComputeIfAbsent(K key, Function<? super K, ? extends V> creator,
					Class<V> requiredType) {
				return requiredType.cast(getOrComputeIfAbsent(key, creator));
			}

			@Override
			public <K, V> V computeIfAbsent(K key, Function<? super K, ? extends V> creator, Class<V> requiredType) {
				return getOrComputeIfAbsent(key, creator, requiredType);
			}

			@Override
			public void put(Object key, Object value) {
				values.put(key, value);
			}

			@Override
			public Object remove(Object key) {
				return values.remove(key);
			}

			@Override
			public <V> V remove(Object key, Class<V> requiredType) {
				return requiredType.cast(values.remove(key));
			}
		};
		when(context.getStore(org.mockito.ArgumentMatchers.any())).thenReturn(store);
		return context;
	}
}
