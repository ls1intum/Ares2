package de.tum.cit.ase.ares.api.jupiter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectClass;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.InvocationInterceptor.Invocation;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
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
	@Execution(ExecutionMode.CONCURRENT)
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

	@Test
	void engineTestKitPreparesOrdinaryRepeatedParameterisedAndParallelInvocationsExactlyOnce() {
		ENGINE_PREPARATIONS.set(0);
		EngineTestKit.engine("junit-jupiter").selectors(selectClass(EngineFixture.class))
				.configurationParameter("junit.jupiter.execution.parallel.enabled", "true")
				.configurationParameter("junit.jupiter.execution.parallel.mode.default", "concurrent").execute()
				.testEvents().assertStatistics(statistics -> statistics.started(7).succeeded(7).failed(0));
		assertEquals(7, ENGINE_PREPARATIONS.get());
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

	private static final List<String> OBSERVATIONS = new CopyOnWriteArrayList<>();

	public static class ArmingSecurityExtension extends JupiterSecurityExtension {
		@Override
		void prepareSecurity(ExtensionContext context) {
			if (context.getTestMethod().isPresent()) {
				ENGINE_PREPARATIONS.incrementAndGet();
			}
			setRestrictedPackage("armed");
		}
	}

	private static void observe(String phase) {
		try {
			OBSERVATIONS.add(phase + "=" + (restrictedPackageField().get(null) != null));
		} catch (ReflectiveOperationException exception) {
			throw new AssertionError(exception);
		}
	}

	@ExtendWith(ArmingSecurityExtension.class)
	static class SetupPhasesFixture {
		SetupPhasesFixture() {
			observe("constructor");
		}

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

	@ExtendWith(ArmingSecurityExtension.class)
	static class AbortedSetupFixture {
		@BeforeEach
		void beforeEach() {
			org.junit.jupiter.api.Assumptions.assumeTrue(false);
		}

		@Test
		void test() {
			observe("test");
		}
	}

	private static void runFixture(Class<?> fixture) throws ReflectiveOperationException {
		OBSERVATIONS.clear();
		ENGINE_PREPARATIONS.set(0);
		restrictedPackageField().set(null, null);
		EngineTestKit.engine("junit-jupiter").selectors(selectClass(fixture)).execute();
	}

	@Test
	void setupAndTeardownMethodsRunWithTheGuardArmed() throws ReflectiveOperationException {
		runFixture(SetupPhasesFixture.class);

		assertEquals(
				List.of("constructor=true", "beforeAll=true", "beforeEach=true", "test=true", "afterEach=true",
						"afterAll=true"),
				List.copyOf(OBSERVATIONS).stream()
						.sorted(java.util.Comparator.comparingInt(
								o -> List.of("constructor", "beforeAll", "beforeEach", "test", "afterEach", "afterAll")
										.indexOf(o.substring(0, o.indexOf('=')))))
						.toList());
		assertNull(restrictedPackageField().get(null), "the guard must be closed once the class has finished");
	}

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

	@Test
	void setupMethodThatThrowsDoesNotLeaveTheGuardArmed() throws ReflectiveOperationException {
		runFixture(FailingSetupFixture.class);

		assertEquals(List.of("beforeEach=true"), OBSERVATIONS);
		assertNull(restrictedPackageField().get(null), "a failed setup method must still close the guard");
	}

	@Test
	void setupMethodThatAbortsDoesNotLeaveTheGuardArmed() throws ReflectiveOperationException {
		runFixture(AbortedSetupFixture.class);

		assertEquals(List.of(), OBSERVATIONS);
		assertNull(restrictedPackageField().get(null), "an aborted setup method must still close the guard");
	}

	@Test
	void armingBeforeEachDoesNotPrepareAgainWhenTheTestMethodStarts() throws ReflectiveOperationException {
		runFixture(PerClassFixture.class);

		assertEquals(2, ENGINE_PREPARATIONS.get(), "exactly one preparation per test method, shared with beforeEach");
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
