package de.tum.cit.ase.ares.api.aop.java.instrumentation.advice;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.Map;

import org.junit.jupiter.api.Test;

/**
 * Checks which call stacks count as the JDK starting its own image clean-up
 * thread, {@code sun.java2d.Disposer}, which the first image stream triggers.
 * The stacks are built by hand, newest frame first, so the tests do not depend
 * on whether this JVM started that thread already. A JDK frame reports a class
 * of the JDK's base module, any other frame this test class.
 */
class JavaInstrumentationImageDisposerSetUpTest {

	/** The student class at the bottom of every stack. */
	private static final String STUDENT = "de.tum.cit.ase.ares.integration.student.Student";

	/** Ares' thread check, the newest frame of every stack. */
	private static final String ARES_CHECK = "de.tum.cit.ase.ares.api.aop.java.instrumentation.advice.JavaInstrumentationAdviceThreadSystemToolbox";

	/** The JDK class that starts the clean-up thread. */
	private static final String DISPOSER = "sun.java2d.Disposer";

	/**
	 * Builds a hand-made stack frame that answers only its class name and declaring
	 * class, which is all the rule reads; any other question fails the test.
	 *
	 * @param className      the frame's class, by its binary name
	 * @param declaringClass the class whose loader and module the frame reports
	 * @return the frame
	 */
	private static StackWalker.StackFrame frameOf(String className, Class<?> declaringClass) {
		Map<String, Object> answers = Map.of("getClassName", className, "getDeclaringClass", declaringClass);
		return (StackWalker.StackFrame) Proxy.newProxyInstance(StackWalker.StackFrame.class.getClassLoader(),
				new Class<?>[] { StackWalker.StackFrame.class }, (proxy, method, arguments) -> {
					if (!answers.containsKey(method.getName())) {
						throw new UnsupportedOperationException(method.getName());
					}
					return answers.get(method.getName());
				});
	}

	/**
	 * Builds a frame that reports a class of the JDK's base module.
	 *
	 * @param className the class
	 * @return the frame
	 */
	private static StackWalker.StackFrame jdk(String className) {
		return frameOf(className, Object.class);
	}

	/**
	 * Builds a frame that reports a class the application class loader loaded.
	 *
	 * @param className the class
	 * @return the frame
	 */
	private static StackWalker.StackFrame application(String className) {
		return frameOf(className, JavaInstrumentationImageDisposerSetUpTest.class);
	}

	/**
	 * Reads a hand-made stack as the image clean-up set-up.
	 *
	 * @param stack the stack, newest first
	 * @return what the toolbox says
	 */
	private static boolean readsAsDisposerSetUp(StackWalker.StackFrame... stack) {
		return JavaInstrumentationAdviceThreadSystemToolbox.isJdkImageDisposerSetUp(Arrays.asList(stack).iterator());
	}

	/** The thread start the first image stream of a student triggers. */
	@Test
	void startFromTheDisposerIsTheSetUp() {
		assertTrue(readsAsDisposerSetUp(jdk(ARES_CHECK), jdk("java.lang.Thread"), jdk(DISPOSER),
				jdk("java.security.AccessController"), jdk(DISPOSER), jdk("javax.imageio.stream.FileImageOutputStream"),
				application(STUDENT)));
	}

	/** Student code between the check and the Disposer keeps the check. */
	@Test
	void studentCodeBeforeTheDisposerIsNoSetUp() {
		assertFalse(
				readsAsDisposerSetUp(jdk(ARES_CHECK), jdk("java.lang.Thread"), application(STUDENT), jdk(DISPOSER)));
	}

	/** A thread start without the Disposer on the stack keeps the check. */
	@Test
	void startWithoutTheDisposerIsNoSetUp() {
		assertFalse(readsAsDisposerSetUp(jdk(ARES_CHECK), jdk("java.lang.Thread"), application(STUDENT)));
	}

	/**
	 * A class outside the JDK that only takes the Disposer's name keeps the check.
	 */
	@Test
	void disposerNameOutsideTheJdkIsNoSetUp() {
		assertFalse(readsAsDisposerSetUp(jdk(ARES_CHECK), jdk("java.lang.Thread"), application(DISPOSER)));
	}

	/** Ares' frames below a JDK frame are not skipped, so they end the walk. */
	@Test
	void aresFrameBetweenJdkFramesIsNoSetUp() {
		assertFalse(readsAsDisposerSetUp(jdk("java.lang.Thread"), application("de.tum.cit.ase.ares.api.Helper"),
				jdk(DISPOSER)));
	}
}
