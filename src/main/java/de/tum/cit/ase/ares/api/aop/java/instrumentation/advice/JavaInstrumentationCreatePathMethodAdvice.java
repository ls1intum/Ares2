package de.tum.cit.ase.ares.api.aop.java.instrumentation.advice;

import java.lang.reflect.Field;
import java.lang.reflect.InaccessibleObjectException;

import net.bytebuddy.asm.Advice;

/**
 * Advice that guards methods capable of creating files or directories.
 * <p>
 * Whenever an intercepted method is entered the advice extracts all instance
 * fields (if an instance is present) and forwards them together with the
 * original parameters to
 * {@link JavaInstrumentationAdviceFileSystemToolbox#checkFileSystemInteraction(String, String, String, String, Object[], Object[], Object)}
 * using the {@code create} action. The toolbox then validates whether the
 * pending creation is permitted by the active security policy.
 * </p>
 */
public final class JavaInstrumentationCreatePathMethodAdvice {

	/**
	 * Intercepts the start of a method that can create files.
	 *
	 * @param declaringTypeName the fully qualified name of the declaring type
	 * @param methodName        the simple method name
	 * @param methodSignature   the JVM method descriptor
	 * @param instance          the receiver instance, {@code null} for static
	 *                          methods
	 * @param parameters        the original method arguments
	 */
	@Advice.OnMethodEnter
	public static void onEnter(@Advice.Origin("#t") String declaringTypeName, @Advice.Origin("#m") String methodName,
			@Advice.Origin("#s") String methodSignature, @Advice.This(optional = true) Object instance,
			@Advice.AllArguments Object... parameters) {
		Field[] fields = instance != null ? instance.getClass().getDeclaredFields() : new Field[0];
		Object[] attributes = new Object[fields.length];
		if (instance != null) {
			for (int i = 0; i < fields.length; i++) {
				try {
					fields[i].setAccessible(true);
					attributes[i] = fields[i].get(instance);
				} catch (InaccessibleObjectException | IllegalAccessException | SecurityException e) {
					continue;
				} catch (IllegalArgumentException e) {
					throw new SecurityException(JavaInstrumentationAdviceAbstractToolbox.localize(
							"security.instrumentation.illegal.argument.exception", fields[i].getName(),
							fields[i].getDeclaringClass().getName(), instance.getClass().getName()), e);
				} catch (NullPointerException e) {
					throw new SecurityException(JavaInstrumentationAdviceAbstractToolbox.localize(
							"security.instrumentation.null.pointer.exception", fields[i].getName(),
							instance.getClass().getName()), e);
				} catch (ExceptionInInitializerError e) {
					throw new SecurityException(JavaInstrumentationAdviceAbstractToolbox.localize(
							"security.instrumentation.exception.in-initializer.error", fields[i].getName(),
							instance.getClass().getName()), e);
				}
			}
		}

		JavaInstrumentationAdviceFileSystemToolbox.checkFileSystemInteraction("create", declaringTypeName, methodName,
				methodSignature, attributes, parameters, instance);
	}
}
