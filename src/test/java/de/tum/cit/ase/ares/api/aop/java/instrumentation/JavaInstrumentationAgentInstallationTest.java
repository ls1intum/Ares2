package de.tum.cit.ase.ares.api.aop.java.instrumentation;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.Instrumentation;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import net.bytebuddy.ByteBuddy;
import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.dynamic.loading.ClassInjector.UsingUnsafe.Factory;

/**
 * Checks how the agent installs its pointcuts: through a single class file
 * transformer, failing closed when that cannot be installed, and keeping a
 * failing pointcut from costing a class the other pointcuts.
 */
class JavaInstrumentationAgentInstallationTest {

	/**
	 * Name of the class the isolation test offers to the agent, outside every
	 * package the agent ignores.
	 */
	private static final String SAMPLE_CLASS = "example.isolation.Sample";

	/**
	 * A transformer that leaves every class unchanged, enough to register a
	 * pointcut without rewriting anything.
	 */
	private static final AgentBuilder.Transformer UNCHANGED = (builder, typeDescription, classLoader, module,
			protectionDomain) -> builder;

	/**
	 * Every pointcut ends up in one transformer, registered once with the JVM.
	 *
	 * @throws Exception If the private installation method cannot be reached.
	 */
	@Test
	void allPointcutsShareOneClassFileTransformer() throws Exception {
		Instrumentation instrumentation = retransformingInstrumentation(true);

		install(instrumentation, List.of(pointcut("java.io.File", "exists", UNCHANGED),
				pointcut("java.net.Socket", "connect", UNCHANGED), pointcut("java.lang.Thread", "start", UNCHANGED)));

		verify(instrumentation, times(1)).addTransformer(any(ClassFileTransformer.class), eq(true));
	}

	/**
	 * A failed installation fails closed and names the classes it meant to watch.
	 *
	 * @throws Exception If the private installation method cannot be reached.
	 */
	@Test
	void failedInstallationNamesEveryWatchedClass() throws Exception {
		Instrumentation instrumentation = retransformingInstrumentation(false);
		List<Object> pointcuts = List.of(pointcut("java.io.File", "exists", UNCHANGED),
				pointcut("java.net.Socket", "connect", UNCHANGED));

		InvocationTargetException wrapper = assertThrows(InvocationTargetException.class,
				() -> install(instrumentation, pointcuts));

		SecurityException failure = assertInstanceOf(SecurityException.class, wrapper.getCause());
		assertTrue(failure.getMessage().contains("java.io.File"));
		assertTrue(failure.getMessage().contains("java.net.Socket"));
	}

	/**
	 * A pointcut whose transformer fails on a class does not stop the next pointcut
	 * from transforming that class, and the failure is still reported.
	 *
	 * @throws Exception If the class cannot be offered to the installed
	 *                   transformer.
	 */
	@Test
	void failingPointcutLeavesOtherPointcutsInPlace() throws Exception {
		Instrumentation instrumentation = retransformingInstrumentation(true);
		AtomicBoolean secondApplied = new AtomicBoolean();
		AgentBuilder.Transformer failing = (builder, typeDescription, classLoader, module, protectionDomain) -> {
			throw new IllegalStateException("deliberate test failure");
		};
		AgentBuilder.Transformer recording = (builder, typeDescription, classLoader, module, protectionDomain) -> {
			secondApplied.set(true);
			return builder;
		};
		install(instrumentation,
				List.of(pointcut(SAMPLE_CLASS, "toString", failing), pointcut(SAMPLE_CLASS, "toString", recording)));
		ArgumentCaptor<ClassFileTransformer> installed = ArgumentCaptor.forClass(ClassFileTransformer.class);
		verify(instrumentation).addTransformer(installed.capture(), eq(true));

		installed.getValue().transform(getClass().getClassLoader(), SAMPLE_CLASS.replace('.', '/'), null, null,
				new ByteBuddy().subclass(Object.class).name(SAMPLE_CLASS).make().getBytes());

		assertTrue(secondApplied.get());
		assertThrows(SecurityException.class, JavaInstrumentationAgent::throwIfTransformationFailed);
		assertDoesNotThrow(JavaInstrumentationAgent::throwIfTransformationFailed);
	}

	/**
	 * Creates an instrumentation stand-in that reports no loaded classes.
	 *
	 * @param retransformationSupported Whether it claims to support retransforming
	 *                                  classes.
	 * @return The stand-in.
	 */
	private static Instrumentation retransformingInstrumentation(boolean retransformationSupported) {
		Instrumentation instrumentation = mock(Instrumentation.class);
		when(instrumentation.isRetransformClassesSupported()).thenReturn(retransformationSupported);
		when(instrumentation.getAllLoadedClasses()).thenReturn(new Class<?>[0]);
		return instrumentation;
	}

	/**
	 * Creates a pointcut watching one method of one class.
	 *
	 * @param className   The watched class.
	 * @param methodName  The watched method.
	 * @param transformer The transformer applied to a matching class.
	 * @return The pointcut, as the agent's private record.
	 * @throws ReflectiveOperationException If the record cannot be constructed.
	 */
	private static Object pointcut(String className, String methodName, AgentBuilder.Transformer transformer)
			throws ReflectiveOperationException {
		Constructor<?> constructor = Class.forName(JavaInstrumentationAgent.class.getName() + "$Pointcut")
				.getDeclaredConstructor(Map.class, AgentBuilder.Transformer.class);
		constructor.setAccessible(true);
		return constructor.newInstance(Map.of(className, List.of(methodName)), transformer);
	}

	/**
	 * Calls the agent's private installation method.
	 *
	 * @param instrumentation The instrumentation to install on.
	 * @param pointcuts       The pointcuts to install.
	 * @throws ReflectiveOperationException If the method cannot be called, or wraps
	 *                                      the exception it threw.
	 */
	private static void install(Instrumentation instrumentation, List<Object> pointcuts)
			throws ReflectiveOperationException {
		Method method = JavaInstrumentationAgent.class.getDeclaredMethod("installAgentBuilder", Instrumentation.class,
				Factory.class, List.class);
		method.setAccessible(true);
		method.invoke(null, instrumentation, mock(Factory.class), pointcuts);
	}
}
