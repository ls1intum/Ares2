package de.tum.cit.ase.ares.api.aop.java;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Test suite for JavaAOPTestCaseSettings utility class.
 * <p>
 * Description: This class contains unit tests that verify the
 * non-instantiability of the JavaAOPTestCaseSettings utility class, the private
 * static structure of all configuration fields, and the correct resetting of
 * those fields.
 * <p>
 * Design Rationale: Enforcing the utility class pattern, verifying that all
 * configuration fields are private and static, and ensuring complete reset
 * functionality prevents improper usage and maintains configuration integrity.
 *
 * @since 2.0.0
 * @author Markus Paulsen
 * @version 2.0.0
 */
public class JavaAOPTestCaseSettingsTest {

	/**
	 * Verifies the utility class structure and constructor behavior.
	 * <p>
	 * Description: Confirms that JavaAOPTestCaseSettings has no public
	 * constructors, exactly one private constructor, and that invoking it via
	 * reflection throws a SecurityException with the appropriate message.
	 *
	 * @since 2.0.0
	 * @author Markus Paulsen
	 */
	@Test
	public void testClassStructureAndConstructor() {
		Class<JavaAOPTestCaseSettings> clazz = JavaAOPTestCaseSettings.class;
		Constructor<?>[] publicConstructors = clazz.getConstructors();
		Constructor<?>[] declaredConstructors = clazz.getDeclaredConstructors();

		Assertions.assertAll("class structure",
				() -> Assertions.assertEquals(0, publicConstructors.length, "There must be no public constructors"),
				() -> Assertions.assertEquals(1, declaredConstructors.length,
						"There must be exactly one declared constructor"),
				() -> Assertions.assertTrue(Modifier.isPrivate(declaredConstructors[0].getModifiers()),
						"The declared constructor must be private"));

		Constructor<?> constructor = declaredConstructors[0];
		constructor.setAccessible(true);
		InvocationTargetException thrown = Assertions.assertThrows(InvocationTargetException.class,
				constructor::newInstance, "Invoking private constructor must throw SecurityException");
		constructor.setAccessible(false);
		String msg = thrown.getTargetException().getMessage();
		Assertions.assertNotNull(msg, "Exception message should not be null");
		Assertions.assertTrue(msg.contains("Ares Security Error") || msg.contains("Ares Sicherheitsfehler"));
	}

	/**
	 * Verifies that all configuration fields are private and static.
	 * <p>
	 * Description: Uses reflection to inspect each declared field in
	 * JavaAOPTestCaseSettings and asserts that it is declared private and static.
	 *
	 * @since 2.0.0
	 * @author Markus Paulsen
	 */
	@Test
	public void testAllFieldsArePrivateStatic() {
		Field[] fields = JavaAOPTestCaseSettings.class.getDeclaredFields();
		Assertions.assertAll("field modifiers", () -> {
			for (Field f : fields) {
				Assertions.assertTrue(Modifier.isPrivate(f.getModifiers()), f.getName() + " must be private");
				Assertions.assertTrue(Modifier.isStatic(f.getModifiers()), f.getName() + " must be static");
			}
		});
	}

	/**
	 * Validates that the reset method clears all static configuration fields.
	 * <p>
	 * Description: Sets each static field to a non-null sample value via
	 * reflection, invokes the private reset method, and asserts that each field is
	 * reset to null.
	 *
	 * @since 2.0.0
	 * @author Markus Paulsen
	 */
	@Test
	public void testResetResetsAllFields() throws Exception {
		Class<JavaAOPTestCaseSettings> clazz = JavaAOPTestCaseSettings.class;
		Map<String, Object> samples = Map.ofEntries(Map.entry("buildMode", "Maven"),
				Map.entry("architectureMode", "Archunit"), Map.entry("aopMode", "Aspectj"),
				Map.entry("restrictedPackage", "de.tum.cit.ase"), Map.entry("mainClass", "Main"),
				Map.entry("allowedListedPackages", new String[] { "pkg1" }),
				Map.entry("allowedListedClasses", new String[] { "Cls1" }),
				Map.entry("pathsAllowedToBeRead", new String[] { "/read" }),
				Map.entry("pathsAllowedToBeOverwritten", new String[] { "/overwrite" }),
				Map.entry("pathsAllowedToBeExecuted", new String[] { "/exec" }),
				Map.entry("pathsAllowedToBeDeleted", new String[] { "/delete" }),
				Map.entry("hostsAllowedToBeConnectedTo", new String[] { "host" }),
				Map.entry("portsAllowedToBeConnectedTo", new int[] { 8080 }),
				Map.entry("hostsAllowedToBeSentTo", new String[] { "sendHost" }),
				Map.entry("portsAllowedToBeSentTo", new int[] { 443 }),
				Map.entry("hostsAllowedToBeReceivedFrom", new String[] { "recvHost" }),
				Map.entry("portsAllowedToBeReceivedFrom", new int[] { 22 }),
				Map.entry("commandsAllowedToBeExecuted", new String[] { "cmd" }),
				Map.entry("argumentsAllowedToBePassed", new String[][] { { "arg1" } }),
				Map.entry("threadClassAllowedToBeCreated", new String[] { "ThreadCls" }),
				Map.entry("threadNumberAllowedToBeCreated", new int[] { 5 }));

		// set and verify each field
		for (Map.Entry<String, Object> entry : samples.entrySet()) {
			Field field = clazz.getDeclaredField(entry.getKey());
			setAndTestField(field, entry.getValue());
		}

		// invoke reset()
		Method resetMethod = clazz.getDeclaredMethod("reset");
		resetMethod.setAccessible(true);
		resetMethod.invoke(null);
		resetMethod.setAccessible(false);

		// verify reset
		for (String name : samples.keySet()) {
			Field field = clazz.getDeclaredField(name);
			getAndTestField(field, name);
		}
	}

	/**
	 * Utility to set a private static field to a sample value and assert it was set
	 * correctly.
	 * <p>
	 * Description: Uses reflection to modify and verify private static fields, then
	 * restores their accessibility.
	 *
	 * @param field       target static field
	 * @param sampleValue value to set for testing
	 * @throws IllegalAccessException if reflection fails
	 * @since 2.0.0
	 * @author Markus Paulsen
	 */
	private void setAndTestField(Field field, Object sampleValue) throws IllegalAccessException {
		field.setAccessible(true);
		field.set(null, sampleValue);
		Object actual = field.get(null);
		if (field.getType().isArray()) {
			Class<?> comp = field.getType().getComponentType();
			if (comp.isPrimitive() && comp == int.class) {
				Assertions.assertArrayEquals((int[]) sampleValue, (int[]) actual,
						field.getName() + " array not set correctly");
			} else {
				Assertions.assertArrayEquals((Object[]) sampleValue, (Object[]) actual,
						field.getName() + " array not set correctly");
			}
		} else {
			Assertions.assertEquals(sampleValue, actual, field.getName() + " not set correctly");
		}
		field.setAccessible(false);
	}

	/**
	 * Utility to assert that a private static field is reset to null.
	 * <p>
	 * Description: Uses reflection to verify that private static fields are null
	 * after reset, then restores their accessibility.
	 *
	 * @param field target static field
	 * @param name  name of the field for assertion message
	 * @throws IllegalAccessException if reflection fails
	 * @since 2.0.0
	 * @author Markus Paulsen
	 */
	private void getAndTestField(Field field, String name) throws IllegalAccessException {
		field.setAccessible(true);
		Assertions.assertNull(field.get(null), name + " must be reset to null");
		field.setAccessible(false);
	}

	/**
	 * Stress test for I-032 (scoped): concurrent
	 * {@link JavaAOPTestCaseSettings#reset()} and
	 * {@link JavaAOPTestCase#setJavaAdviceSettingValue} calls must not throw and
	 * must not leave the settings lock itself in a broken state usable by later
	 * tests.
	 * <p>
	 * Description: This does <strong>not</strong> prove the absence of every
	 * possible race — concurrency is inherently hard to assert deterministically,
	 * and this fix is deliberately scoped to synchronising {@code reset()} and each
	 * individual field write against each other (not full multi-field read
	 * atomicity, which is a larger, deferred follow-up — see
	 * {@link JavaAOPTestCaseSettings#reset()}'s Javadoc). What this test does
	 * verify: many threads concurrently calling {@code reset()} and
	 * {@code setJavaAdviceSettingValue} on the same field, repeatedly, never throw
	 * (e.g. a reflection failure from the lock-resolution indirection added by this
	 * fix). The test deliberately makes no claim about which concurrent writer
	 * wins.
	 *
	 * @since 2.0.0
	 * @author Markus Paulsen
	 */
	@Test
	void concurrentResetAndSettingWritesDoNotThrowOrCorruptState() throws Exception {
		int writerThreads = 4;
		int resetterThreads = 4;
		int iterationsPerThread = 200;
		AtomicBoolean failed = new AtomicBoolean(false);
		AtomicReference<Throwable> firstFailure = new AtomicReference<>();

		ExecutorService executor = Executors.newFixedThreadPool(writerThreads + resetterThreads);
		try {
			List<Future<?>> futures = new ArrayList<>();
			for (int t = 0; t < writerThreads; t++) {
				int threadId = t;
				futures.add(executor.submit(() -> {
					for (int i = 0; i < iterationsPerThread; i++) {
						try {
							JavaAOPTestCase.setJavaAdviceSettingValue("restrictedPackage",
									"stress-writer-" + threadId + "-" + i, "ARCH", "INSTRUMENTATION");
						} catch (Throwable failure) {
							failed.set(true);
							firstFailure.compareAndSet(null, failure);
						}
					}
				}));
			}
			for (int t = 0; t < resetterThreads; t++) {
				futures.add(executor.submit(() -> {
					for (int i = 0; i < iterationsPerThread; i++) {
						try {
							JavaAOPTestCaseSettings.reset();
						} catch (Throwable failure) {
							failed.set(true);
							firstFailure.compareAndSet(null, failure);
						}
					}
				}));
			}
			for (Future<?> future : futures) {
				future.get(30, TimeUnit.SECONDS);
			}
		} finally {
			executor.shutdownNow();
			JavaAOPTestCaseSettings.reset();
		}

		Assertions.assertFalse(failed.get(),
				() -> "Concurrent reset()/setJavaAdviceSettingValue calls must not throw, but: " + firstFailure.get());
	}
}
