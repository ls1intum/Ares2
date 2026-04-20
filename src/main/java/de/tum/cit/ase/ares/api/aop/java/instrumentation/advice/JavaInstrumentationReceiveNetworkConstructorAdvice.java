package de.tum.cit.ase.ares.api.aop.java.instrumentation.advice;

import net.bytebuddy.asm.Advice;

/**
 * This class provides advice for the execution of constructors receiving
 * network data. It is responsible for verifying whether the constructor
 * execution is allowed based on the network system security policies defined
 * within the application.
 * <p>
 * If an execution attempt violates these policies, a SecurityException is
 * thrown, preventing unauthorized network receive operations. The class
 * interacts with the JavaInstrumentationAdviceNetworkSystemToolbox to perform
 * these security checks.
 */
public final class JavaInstrumentationReceiveNetworkConstructorAdvice {
	/**
	 * This method is called when a constructor receiving network data is
	 * entered. It performs security checks to determine whether the constructor
	 * execution is allowed according to network system security policies. If the
	 * constructor execution is not permitted, a SecurityException is thrown,
	 * blocking the execution.
	 * <p>
	 * The checkNetworkSystemInteraction method from
	 * JavaInstrumentationAdviceNetworkSystemToolbox is called to perform these
	 * checks, ensuring that the constructor's parameters adhere to the security
	 * restrictions.
	 *
	 * @param declaringTypeName The name of the class that declares the constructor.
	 * @param parameters        The parameters passed to the constructor being
	 *                          executed.
	 */
	@Advice.OnMethodEnter
	public static void onEnter(@Advice.Origin("#t") String declaringTypeName,
			@Advice.AllArguments Object... parameters) {
		JavaInstrumentationAdviceNetworkSystemToolbox.checkNetworkSystemInteraction("receive", declaringTypeName,
				"<init>", "", new Object[0], parameters, null);
	}
}

