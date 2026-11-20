package de.tum.cit.ase.ares.api.aop.java.instrumentation.advice;

import net.bytebuddy.asm.Advice;

/**
 * The check put into the JDK method {@code Runtime.loadLibrary0}, which both
 * public {@code loadLibrary} methods call with the class the JDK says called
 * them. It lets the JDK load its own libraries and checks every other load
 * against the policy, as a file that is executed.
 */
public final class JavaInstrumentationLoadLibraryMethodAdvice {

	/**
	 * Refuses to create an instance; the class only holds the check.
	 */
	private JavaInstrumentationLoadLibraryMethodAdvice() {
		throw new SecurityException(JavaInstrumentationAdviceAbstractToolbox.localize(
				"security.instrumentation.utility.initialization", "JavaInstrumentationLoadLibraryMethodAdvice"));
	}

	/**
	 * Runs before the method body and throws if the load is not allowed.
	 *
	 * @param declaringTypeName the class that declares the method
	 * @param methodName        the method's name
	 * @param methodSignature   the parameter types, used only in messages
	 * @param methodDescriptor  the JVM descriptor, used to recognise the method
	 * @param fromClass         the class the JDK says asked for the library
	 * @param libraryName       the library name as the caller passed it
	 * @throws SecurityException if the policy forbids the load
	 */
	@Advice.OnMethodEnter
	public static void onEnter(@Advice.Origin("#t") String declaringTypeName, @Advice.Origin("#m") String methodName,
			@Advice.Origin("#s") String methodSignature, @Advice.Origin("#d") String methodDescriptor,
			@Advice.Argument(0) Class<?> fromClass, @Advice.Argument(1) String libraryName) {
		JavaInstrumentationAdviceFileSystemToolbox.checkLibraryLoad0(declaringTypeName, methodName, methodSignature,
				methodDescriptor, fromClass, libraryName);
	}
}
