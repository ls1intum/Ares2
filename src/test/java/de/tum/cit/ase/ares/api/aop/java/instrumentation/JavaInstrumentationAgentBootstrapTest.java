package de.tum.cit.ase.ares.api.aop.java.instrumentation;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.junit.jupiter.api.Test;

import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.dynamic.loading.ClassInjector;
import net.bytebuddy.dynamic.loading.ClassInjector.UsingUnsafe.Factory;

class JavaInstrumentationAgentBootstrapTest {

	@Test
	void nullBootstrapInjectorFailsClosed() throws Exception {
		Factory factory = mock(Factory.class);
		when(factory.make(isNull(), isNull())).thenReturn((ClassInjector) null);
		Method method = JavaInstrumentationAgent.class.getDeclaredMethod("putToolboxOnBootClassLoader", Factory.class);
		method.setAccessible(true);
		InvocationTargetException wrapper = assertThrows(InvocationTargetException.class,
				() -> method.invoke(null, factory));
		assertInstanceOf(SecurityException.class, wrapper.getCause());
	}

	@Test
	void reportedTransformationFailureIsConsumedAfterItIsSurfaced() throws Exception {
		java.lang.reflect.Field listenerField = JavaInstrumentationAgent.class
				.getDeclaredField("TRANSFORMATION_FAILURE_LISTENER");
		listenerField.setAccessible(true);
		AgentBuilder.Listener listener = (AgentBuilder.Listener) listenerField.get(null);
		listener.onError("example.Broken", getClass().getClassLoader(), null, false,
				new IllegalStateException("deliberate test failure"));

		assertThrows(SecurityException.class, JavaInstrumentationAgent::throwIfTransformationFailed);
		assertDoesNotThrow(JavaInstrumentationAgent::throwIfTransformationFailed);
	}
}
