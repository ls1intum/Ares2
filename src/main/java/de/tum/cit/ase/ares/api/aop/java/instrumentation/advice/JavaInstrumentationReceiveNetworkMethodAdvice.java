package de.tum.cit.ase.ares.api.aop.java.instrumentation.advice;

import java.lang.reflect.Field;
import java.lang.reflect.InaccessibleObjectException;

import net.bytebuddy.asm.Advice;

public final class JavaInstrumentationReceiveNetworkMethodAdvice {

	@Advice.OnMethodEnter
	public static void onEnter(@Advice.Origin("#t") String declaringTypeName, @Advice.Origin("#m") String methodName,
			@Advice.Origin("#s") String methodSignature, @Advice.This(optional = true) Object instance,
			@Advice.AllArguments Object... parameters) {
		final Field[] fields = instance != null ? instance.getClass().getDeclaredFields() : new Field[0];
		final Object[] attributes = new Object[fields.length];
		if (instance != null) {
			for (int i = 0; i < fields.length; i++) {
				try {
					fields[i].setAccessible(true);
					attributes[i] = fields[i].get(instance);
				} catch (InaccessibleObjectException e) {
					throw new SecurityException(JavaInstrumentationAdviceNetworkSystemToolbox.localize(
							"security.instrumentation.inaccessible.object.exception", fields[i].getName(),
							instance.getClass().getName()), e);
				} catch (IllegalAccessException e) {
					throw new SecurityException(JavaInstrumentationAdviceNetworkSystemToolbox.localize(
							"security.instrumentation.illegal.access.exception", fields[i].getName(),
							instance.getClass().getName()), e);
				}
			}
		}
		JavaInstrumentationAdviceNetworkSystemToolbox.checkNetworkSystemInteraction("receive", declaringTypeName,
				methodName, methodSignature, attributes, parameters, instance);
	}
}

