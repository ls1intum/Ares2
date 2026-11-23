package de.tum.cit.ase.ares.api.aop.java.instrumentation.advice;

import java.lang.reflect.Field;
import java.lang.reflect.InaccessibleObjectException;

import net.bytebuddy.asm.Advice;

/**
 * This class provides advice for the execution of methods executing files. It
 * is responsible for verifying whether the method execution is allowed based on
 * the file system security policies defined within the application.
 * <p>
 * If an execution attempt violates these policies, a SecurityException is
 * thrown, preventing unauthorised file executions. The class interacts with the
 * JavaInstrumentationAdviceFileSystemToolbox to perform these security checks.
 */
public final class JavaInstrumentationExecutePathMethodAdvice {
	private JavaInstrumentationExecutePathMethodAdvice() {
		throw new SecurityException(JavaInstrumentationAdviceAbstractToolbox.localize(
				"security.instrumentation.utility.initialization", "JavaInstrumentationExecutePathMethodAdvice"));
	}

	/**
	 * Runs before a method that executes a file and throws if the policy forbids
	 * it. A public {@code loadLibrary} call may be left to the check one step
	 * deeper.
	 *
	 * @param declaringTypeName the class
	 * @param methodName        the method
	 * @param methodSignature   for messages
	 * @param methodDescriptor  JVM descriptor
	 * @param instance          receiver, or {@code null}
	 * @param parameters        arguments
	 * @throws SecurityException if forbidden
	 */
	@Advice.OnMethodEnter
	public static void onEnter(@Advice.Origin("#t") String declaringTypeName, @Advice.Origin("#m") String methodName,
			@Advice.Origin("#s") String methodSignature, @Advice.Origin("#d") String methodDescriptor,
			@Advice.This(optional = true) Object instance, @Advice.AllArguments Object... parameters) {
		// <editor-fold desc="Attributes">
		final Field[] fields = instance != null ? instance.getClass().getDeclaredFields() : new Field[0];
		final Object[] attributes = new Object[fields.length];
		if (instance != null) {
			for (int i = 0; i < fields.length; i++) {
				if (java.lang.reflect.Modifier.isStatic(fields[i].getModifiers())) {
					continue;
				}
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
		JavaInstrumentationAdviceFileSystemToolbox.checkExecuteInteraction(declaringTypeName, methodName,
				methodSignature, methodDescriptor, attributes, parameters, instance);
		// </editor-fold>
	}
}
