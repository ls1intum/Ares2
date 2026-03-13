package de.tum.cit.ase.ares.api.aop.java.instrumentation.advice;

import net.bytebuddy.asm.Advice;

public final class JavaInstrumentationReceiveNetworkConstructorAdvice {

	@Advice.OnMethodEnter
	public static void onEnter(@Advice.Origin("#t") String declaringTypeName,
			@Advice.AllArguments Object... parameters) {
		JavaInstrumentationAdviceNetworkSystemToolbox.checkNetworkSystemInteraction("receive", declaringTypeName,
				"<init>", "", new Object[0], parameters, null);
	}
}

