package de.tum.cit.ase.ares.api.aop.java.instrumentation;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.junit.jupiter.api.Test;

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
}
