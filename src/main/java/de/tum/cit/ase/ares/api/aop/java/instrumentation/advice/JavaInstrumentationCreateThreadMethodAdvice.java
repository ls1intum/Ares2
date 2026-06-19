package de.tum.cit.ase.ares.api.aop.java.instrumentation.advice;

import java.lang.reflect.Field;
import java.lang.reflect.InaccessibleObjectException;

import net.bytebuddy.asm.Advice;

/**
 * This class provides advice for the execution of methods creating threads. It
 * is responsible for verifying whether the method execution is allowed based on
 * the thread system security policies defined within the application.
 * <p>
 * If an execution attempt violates these policies, a SecurityException is
 * thrown, preventing unauthorized thread creation. The class interacts with the
 * JavaInstrumentationAdviceThreadSystemToolbox to perform these security
 * checks.
 */
public final class JavaInstrumentationCreateThreadMethodAdvice {
	/**
	 * This method is called when a method creating threads is entered. It performs
	 * security checks to determine whether the method execution is allowed
	 * according to thread system security policies. If the method execution is not
	 * permitted, a SecurityException is thrown, blocking the execution.
	 * <p>
	 * The checkThreadSystemInteraction method from
	 * JavaInstrumentationAdviceThreadSystemToolbox is called to perform these
	 * checks, ensuring that both the method's parameters and the instance fields
	 * adhere to the security restrictions.
	 *
	 * @param declaringTypeName The name of the class that declares the method.
	 * @param methodName        The name of the method being executed.
	 * @param methodSignature   The signature of the method being executed.
	 * @param instance          The instance of the class that declares the method
	 *                          (if applicable).
	 * @param parameters        The parameters passed to the method being executed.
	 * @throws SecurityException If the method execution is not allowed based on
	 *                           security policies.
	 */
	@Advice.OnMethodEnter
	public static void onEnter(@Advice.Origin("#t") String declaringTypeName, @Advice.Origin("#m") String methodName,
			@Advice.Origin("#s") String methodSignature, @Advice.This(optional = true) Object instance,
			@Advice.AllArguments Object... parameters) {
		// <editor-fold desc="Attributes">
		final Field[] fields = instance != null ? instance.getClass().getDeclaredFields() : new Field[0];
		final Object[] attributes = new Object[fields.length];
		if (instance != null) {
			for (int i = 0; i < fields.length; i++) {
				try {
					fields[i].setAccessible(true);
					attributes[i] = fields[i].get(instance);
				} catch (InaccessibleObjectException | IllegalAccessException | SecurityException
						| IllegalArgumentException | NullPointerException | ExceptionInInitializerError e) {
					// Skip an unreadable field rather than aborting the whole interaction: a
					// JDK-internal field (e.g. reached via Ares's own timeout executor) that throws
					// on read must not turn a JDK-side reflection limit into an Ares-Code
					// SecurityException. The check still runs over the parameters and the readable
					// fields. Uniform across all four subsystems and both engines.
					continue;
				}
			}
		}
		// </editor-fold>

		// <editor-fold desc="Check">
		JavaInstrumentationAdviceThreadSystemToolbox.checkThreadSystemInteraction("create", declaringTypeName,
				methodName, methodSignature, attributes, parameters, instance);
		// </editor-fold>
	}
}
