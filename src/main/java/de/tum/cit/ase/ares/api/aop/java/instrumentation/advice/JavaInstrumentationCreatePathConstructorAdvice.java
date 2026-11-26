package de.tum.cit.ase.ares.api.aop.java.instrumentation.advice;

import net.bytebuddy.asm.Advice;

/**
 * This class provides advice for the execution of constructors creating files.
 * It is responsible for verifying whether the constructor execution is allowed
 * based on the file system security policies defined within the application.
 * <p>
 * If an execution attempt violates these policies, a SecurityException is
 * thrown, preventing unauthorised file creations. The class interacts with the
 * JavaInstrumentationAdviceFileSystemToolbox to perform these security checks.
 */
public final class JavaInstrumentationCreatePathConstructorAdvice {
	private JavaInstrumentationCreatePathConstructorAdvice() {
		throw new SecurityException(JavaInstrumentationAdviceAbstractToolbox.localize(
				"security.instrumentation.utility.initialization", "JavaInstrumentationCreatePathConstructorAdvice"));
	}

	/**
	 * This method is called when a constructor creating files is entered. It
	 * performs security checks to determine whether the constructor execution is
	 * allowed according to file system security policies. If the constructor
	 * execution is not permitted, a SecurityException is thrown, blocking the
	 * execution.
	 * <p>
	 * The checkFileSystemInteraction method from
	 * JavaInstrumentationAdviceFileSystemToolbox is called to perform these checks,
	 * ensuring that the constructor's parameters adhere to the security
	 * restrictions.
	 *
	 * @param declaringTypeName The name of the class that declares the constructor.
	 * @param parameters        The parameters passed to the constructor being
	 *                          executed.
	 */
	@Advice.OnMethodEnter
	public static void onEnter(@Advice.Origin("#t") String declaringTypeName,
			@Advice.AllArguments Object... parameters) {
		JavaInstrumentationAdviceFileSystemToolbox.checkFileSystemInteraction("create", declaringTypeName, "<init>", "",
				new Object[0], parameters, null);
	}

	/**
	 * Called when the constructor has finished. Remembers the new object if Ares
	 * allowed the call to create the file it has open. Byte Buddy runs no exit
	 * advice for a constructor that throws, so a failed one remembers nothing.
	 *
	 * @param instance the new object
	 */
	@Advice.OnMethodExit
	public static void onExit(@Advice.This Object instance) {
		JavaInstrumentationAdviceFileSystemToolbox.rememberCreatedHandle(instance, false);
	}
}
