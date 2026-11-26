package de.tum.cit.ase.ares.api.aop.java.instrumentation.advice;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Proxy;
import java.util.List;

import org.junit.jupiter.api.Test;

/**
 * Checks which call stacks count as the JDK building the pipe of a selector,
 * which on Windows binds and connects a loopback socket of its own. The stacks
 * are built by hand, newest frame first, so these tests run on every system; a
 * frame "of the JVM" claims a class of the JDK's base module, any other frame
 * claims this test class.
 */
class JavaInstrumentationPipeSetUpTest {

	/** The student class at the bottom of every stack. */
	private static final String STUDENT = "de.tum.cit.ase.ares.integration.student.Student";

	/** Ares' network check, the newest frame of every stack. */
	private static final String ARES_CHECK = "de.tum.cit.ase.ares.api.aop.java.instrumentation.advice.JavaInstrumentationAdviceNetworkSystemToolbox";

	/**
	 * Builds a hand-made stack frame that answers only its class name, method name
	 * and declaring class, which is all the pipe rule reads.
	 *
	 * @param className      the frame's class, by its binary name
	 * @param methodName     the frame's method
	 * @param declaringClass the class whose loader and module the frame reports
	 * @return the frame
	 */
	private static StackWalker.StackFrame frame(String className, String methodName, Class<?> declaringClass) {
		return (StackWalker.StackFrame) Proxy.newProxyInstance(StackWalker.StackFrame.class.getClassLoader(),
				new Class<?>[] { StackWalker.StackFrame.class },
				(proxy, method, arguments) -> switch (method.getName()) {
				case "getClassName" -> className;
				case "getMethodName" -> methodName;
				case "getDeclaringClass" -> declaringClass;
				default -> throw new UnsupportedOperationException(method.getName());
				});
	}

	/**
	 * Builds a frame of a class in the JDK's base module.
	 *
	 * @param className  the class
	 * @param methodName the method
	 * @return the frame
	 */
	private static StackWalker.StackFrame jvm(String className, String methodName) {
		return frame(className, methodName, String.class);
	}

	/**
	 * Builds a frame of a class some other class loader loaded.
	 *
	 * @param className  the class
	 * @param methodName the method
	 * @return the frame
	 */
	private static StackWalker.StackFrame other(String className, String methodName) {
		return frame(className, methodName, JavaInstrumentationPipeSetUpTest.class);
	}

	/**
	 * Reads a hand-made stack as the pipe set-up.
	 *
	 * @param frames the stack, newest first
	 * @return what the toolbox says
	 */
	private static boolean isPipeSetUp(StackWalker.StackFrame... frames) {
		return JavaInstrumentationAdviceAbstractToolbox.isJdkPipeSetUp(List.of(frames).iterator());
	}

	/** The bind the Windows CI refused, reached from {@code Selector.open()}. */
	@Test
	void bindOfTheSelectorPipeIsThePipeSetUp() {
		assertTrue(isPipeSetUp(jvm(ARES_CHECK, "checkNetworkSystemInteraction"),
				jvm("sun.nio.ch.ServerSocketChannelImpl", "bind"),
				jvm("sun.nio.ch.PipeImpl$Initializer$LoopbackConnector", "run"),
				jvm("sun.nio.ch.PipeImpl$Initializer", "run"), jvm("sun.nio.ch.PipeImpl", "<init>"),
				jvm("sun.nio.ch.WEPollSelectorImpl", "<init>"), jvm("java.nio.channels.Selector", "open"),
				other(STUDENT, "listen")));
	}

	/** The loopback connect of the same pipe. */
	@Test
	void connectOfTheSelectorPipeIsThePipeSetUp() {
		assertTrue(isPipeSetUp(jvm(ARES_CHECK, "checkNetworkSystemInteraction"),
				jvm("sun.nio.ch.SocketChannelImpl", "connect"), jvm("java.nio.channels.SocketChannel", "open"),
				jvm("sun.nio.ch.PipeImpl$Initializer$LoopbackConnector", "run"), other(STUDENT, "listen")));
	}

	/** Student code between the check and the pipe keeps the check. */
	@Test
	void studentCodeBeforeThePipeIsNoPipeSetUp() {
		assertFalse(isPipeSetUp(jvm(ARES_CHECK, "checkNetworkSystemInteraction"),
				jvm("sun.nio.ch.ServerSocketChannelImpl", "bind"), other(STUDENT, "bindServer"),
				jvm("sun.nio.ch.PipeImpl", "<init>")));
	}

	/** A bind without the pipe on the stack keeps the check. */
	@Test
	void bindWithoutThePipeIsNoPipeSetUp() {
		assertFalse(isPipeSetUp(jvm(ARES_CHECK, "checkNetworkSystemInteraction"),
				jvm("sun.nio.ch.ServerSocketChannelImpl", "bind"), other(STUDENT, "bindServer")));
	}

	/**
	 * A class that only takes the pipe's name, outside the JDK, keeps the check.
	 */
	@Test
	void pipeNameOutsideTheJdkIsNoPipeSetUp() {
		assertFalse(isPipeSetUp(jvm(ARES_CHECK, "checkNetworkSystemInteraction"),
				jvm("sun.nio.ch.ServerSocketChannelImpl", "bind"), other("sun.nio.ch.PipeImpl", "<init>")));
	}

	/** Ares' frames below a JDK frame are not skipped, so they end the walk. */
	@Test
	void aresFrameBetweenJdkFramesIsNoPipeSetUp() {
		assertFalse(isPipeSetUp(jvm("sun.nio.ch.ServerSocketChannelImpl", "bind"),
				other("de.tum.cit.ase.ares.api.Helper", "bind"), jvm("sun.nio.ch.PipeImpl", "<init>")));
	}
}
