package de.tum.cit.ase.ares.api.aop.java.instrumentation.advice;

import net.bytebuddy.asm.Advice;

public final class JavaInstrumentationSendNetworkConstructorAdvice {

	@Advice.OnMethodEnter
	public static void onEnter(@Advice.Origin("#t") String declaringTypeName,
			@Advice.AllArguments Object... parameters) {
		JavaInstrumentationAdviceNetworkSystemToolbox.checkNetworkSystemInteraction("send", declaringTypeName,
				"<init>", "", new Object[0], parameters, null);
	}
}

