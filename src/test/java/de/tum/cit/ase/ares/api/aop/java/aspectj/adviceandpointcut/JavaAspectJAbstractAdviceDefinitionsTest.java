package de.tum.cit.ase.ares.api.aop.java.aspectj.adviceandpointcut;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.net.SocketAddress;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.DatagramChannel;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import javax.net.SocketFactory;
import javax.net.ssl.SSLSocketFactory;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.reflect.ConstructorSignature;
import org.aspectj.lang.reflect.MethodSignature;
import org.junit.jupiter.api.Test;

/**
 * Tests for the diagnostic normalisation that closes the reporting gap between
 * the AspectJ and the Instrumentation backend.
 * <p>
 * AspectJ weaves {@code call(...)} join points and therefore names the static
 * receiver type at the call site, while Instrumentation advises the
 * implementation whose bytecode runs. The same blocked operation was reported
 * under two different class names, so an expectation authored against one
 * backend could never match the other. {@code describeDeniedCall} appends the
 * resolved runtime declaration when it differs.
 * <p>
 * This is best-effort normalisation for ordinary public virtual calls on
 * platform classes, so the fallback behaviour is asserted just as carefully as
 * the resolution itself.
 */
class JavaAspectJAbstractAdviceDefinitionsTest {

	private static JoinPoint methodJoinPoint(Object target, String declaringTypeName, String name,
			Class<?>[] parameterTypes) {
		MethodSignature signature = mock(MethodSignature.class);
		when(signature.getDeclaringTypeName()).thenReturn(declaringTypeName);
		when(signature.getName()).thenReturn(name);
		when(signature.getParameterTypes()).thenReturn(parameterTypes);
		JoinPoint joinPoint = mock(JoinPoint.class);
		when(joinPoint.getSignature()).thenReturn(signature);
		when(joinPoint.getTarget()).thenReturn(target);
		return joinPoint;
	}

	private static String staticSignature(String declaringTypeName, String name, Class<?>[] parameterTypes) {
		StringBuilder rendered = new StringBuilder(declaringTypeName).append('.').append(name).append('(');
		for (int i = 0; i < parameterTypes.length; i++) {
			if (i > 0) {
				rendered.append(',');
			}
			rendered.append(parameterTypes[i].getName());
		}
		return rendered.append(')').toString();
	}

	private static String describe(Object target, String declaringTypeName, String name, Class<?>... parameterTypes) {
		return JavaAspectJAbstractAdviceDefinitions.describeDeniedCall(
				methodJoinPoint(target, declaringTypeName, name, parameterTypes),
				staticSignature(declaringTypeName, name, parameterTypes));
	}

	/**
	 * The concrete factory overrides the abstract method, so the resolved
	 * declaration is the implementation Instrumentation would have reported.
	 */
	@Test
	void describeDeniedCall_reportsOverridingImplementationForSocketFactory() {
		String described = describe(SocketFactory.getDefault(), "javax.net.SocketFactory", "createSocket", String.class,
				int.class);

		assertTrue(described.startsWith("javax.net.SocketFactory.createSocket(java.lang.String,int)"),
				"the guaranteed static signature must stay first: " + described);
		assertTrue(described.contains("javax.net.DefaultSocketFactory.createSocket"),
				"the resolved implementation must be appended: " + described);
	}

	@Test
	void describeDeniedCall_reportsOverridingImplementationForSslSocketFactory() {
		String described = describe(SSLSocketFactory.getDefault(), "javax.net.ssl.SSLSocketFactory", "createSocket",
				String.class, int.class);

		assertTrue(described.contains("sun.security.ssl.SSLSocketFactoryImpl.createSocket"), described);
	}

	@Test
	void describeDeniedCall_reportsImplementationForDatagramChannel() throws Exception {
		try (DatagramChannel channel = DatagramChannel.open()) {
			String described = describe(channel, "java.nio.channels.DatagramChannel", "connect", SocketAddress.class);

			assertTrue(described.contains("sun.nio.ch.DatagramChannelImpl.connect"), described);
		}
	}

	/**
	 * The runtime class here is {@code UnixAsynchronousSocketChannelImpl}, but the
	 * method is declared one level up. Reading the runtime class directly would
	 * report the wrong name, so this pins the declaring-class resolution.
	 */
	@Test
	void describeDeniedCall_reportsDeclaringClassRatherThanRuntimeClass() throws Exception {
		try (AsynchronousSocketChannel channel = AsynchronousSocketChannel.open()) {
			String described = describe(channel, "java.nio.channels.AsynchronousSocketChannel", "connect",
					SocketAddress.class);

			assertTrue(described.contains("sun.nio.ch.AsynchronousSocketChannelImpl.connect"), described);
			assertFalse(described.contains("UnixAsynchronousSocketChannelImpl"),
					"the runtime class does not declare the method and must not be reported: " + described);
		}
	}

	@Test
	void describeDeniedCall_reportsImplementationForScheduledExecutorService() {
		ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
		try {
			String described = describe(executor, "java.util.concurrent.ScheduledExecutorService", "schedule",
					Runnable.class, long.class, TimeUnit.class);

			assertTrue(described.contains("Executors$DelegatedScheduledExecutorService.schedule"), described);
		} finally {
			executor.shutdownNow();
		}
	}

	/**
	 * An inherited method must resolve to the class that declares it, not to the
	 * concrete pipeline subclass the runtime happens to use.
	 */
	@Test
	void describeDeniedCall_reportsInheritedDeclaringClassForStream() {
		String described = describe(Stream.of("element"), "java.util.stream.Stream", "parallel");

		assertTrue(described.contains("java.util.stream.AbstractPipeline.parallel"), described);
		assertFalse(described.contains("ReferencePipeline"),
				"the declaring class, not the runtime class, must be reported: " + described);
	}

	/**
	 * A static call has no receiver, so there is no dispatch to resolve and the
	 * static signature must be returned untouched.
	 */
	@Test
	void describeDeniedCall_fallsBackForNullTarget() {
		String expected = staticSignature("java.nio.file.Files", "readString",
				new Class<?>[] { java.nio.file.Path.class });

		String described = describe(null, "java.nio.file.Files", "readString", java.nio.file.Path.class);

		assertEquals(expected, described);
	}

	/**
	 * Constructor join points already name the concrete type, so there is nothing
	 * to normalise.
	 */
	@Test
	void describeDeniedCall_fallsBackForConstructorSignature() {
		ConstructorSignature signature = mock(ConstructorSignature.class);
		JoinPoint joinPoint = mock(JoinPoint.class);
		when(joinPoint.getSignature()).thenReturn(signature);

		String described = JavaAspectJAbstractAdviceDefinitions.describeDeniedCall(joinPoint,
				"java.lang.ProcessBuilder.<init>(java.lang.String[])");

		assertEquals("java.lang.ProcessBuilder.<init>(java.lang.String[])", described);
	}

	/**
	 * Resolving members of an application-loaded class can trigger that class's own
	 * loading, which would run while the advice guard suppresses nested
	 * interception. Such targets must never be reflected over.
	 */
	@Test
	void describeDeniedCall_fallsBackForApplicationLoadedTarget() {
		ApplicationLoadedTarget target = new ApplicationLoadedTarget();
		assertFalse(
				target.getClass().getClassLoader() == null
						|| target.getClass().getClassLoader() == ClassLoader.getPlatformClassLoader(),
				"this test is meaningless unless the target really is application-loaded");
		String expected = staticSignature(ApplicationLoadedTarget.class.getName(), "run", new Class<?>[0]);

		String described = describe(target, ApplicationLoadedTarget.class.getName(), "run");

		assertEquals(expected, described);
	}

	/**
	 * A signature that does not resolve against the runtime type must degrade to
	 * the static signature rather than turning a denial into a different failure.
	 */
	@Test
	void describeDeniedCall_fallsBackWhenMethodCannotBeResolved() {
		String expected = staticSignature("java.util.stream.Stream", "noSuchMethod", new Class<?>[0]);

		String described = describe(Stream.of("element"), "java.util.stream.Stream", "noSuchMethod");

		assertEquals(expected, described);
	}

	/**
	 * When the static declaration already is the declaring class, nothing is
	 * appended, so unchanged messages stay byte-identical.
	 */
	@Test
	void describeDeniedCall_addsNothingWhenDeclarationsAgree() {
		String expected = staticSignature("java.lang.StringBuilder", "append", new Class<?>[] { String.class });

		String described = describe(new StringBuilder(), "java.lang.StringBuilder", "append", String.class);

		assertEquals(expected, described);
	}

	/**
	 * The diagnostic form must never be substituted for the static signature: the
	 * enforcement ignore maps are keyed on the static form (for example
	 * {@code java.io.File.delete}), so a resolved subclass name would silently
	 * change which parameters and fields get inspected.
	 */
	@Test
	void describeDeniedCall_preservesTheEnforcementKeyPrefix() {
		String staticForm = staticSignature("java.io.File", "delete", new Class<?>[0]);

		String described = describe(new java.io.File("ares-diagnostic-probe"), "java.io.File", "delete");

		assertTrue(described.startsWith(staticForm),
				"the enforcement key must remain recoverable from the message: " + described);
	}

	private static final class ApplicationLoadedTarget {

		@SuppressWarnings("unused")
		public void run() {
			// Never invoked; only its declaring class loader matters.
		}
	}
}
