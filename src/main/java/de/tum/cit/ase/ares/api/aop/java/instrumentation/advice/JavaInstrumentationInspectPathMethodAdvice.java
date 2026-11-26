package de.tum.cit.ase.ares.api.aop.java.instrumentation.advice;

import net.bytebuddy.asm.Advice;

/**
 * Advice for methods that answer a question about a file without opening it,
 * such as whether it exists or how large it is. It hands each call to
 * JavaInstrumentationAdviceFileSystemToolbox, which refuses it when no file
 * rule of the policy names the path.
 */
public final class JavaInstrumentationInspectPathMethodAdvice {

	/**
	 * Prevents instantiation of this utility class.
	 */
	private JavaInstrumentationInspectPathMethodAdvice() {
		throw new SecurityException(JavaInstrumentationAdviceAbstractToolbox.localize(
				"security.instrumentation.utility.initialization", "JavaInstrumentationInspectPathMethodAdvice"));
	}

	/**
	 * Checks the call before the method runs. The receiver's fields are not
	 * collected: the receiver itself is checked, a {@code File} by its path.
	 *
	 * @param declaringTypeName the class that declares the method
	 * @param methodName        the method
	 * @param methodSignature   the method's parameter list
	 * @param instance          the receiver, or {@code null} for a static method
	 * @param parameters        the arguments
	 * @throws SecurityException if the policy does not permit the question
	 */
	@Advice.OnMethodEnter
	public static void onEnter(@Advice.Origin("#t") String declaringTypeName, @Advice.Origin("#m") String methodName,
			@Advice.Origin("#s") String methodSignature, @Advice.This(optional = true) Object instance,
			@Advice.AllArguments Object... parameters) {
		JavaInstrumentationAdviceFileSystemToolbox.checkFileSystemInteraction("inspect", declaringTypeName, methodName,
				methodSignature, new Object[0], parameters, instance);
	}
}
