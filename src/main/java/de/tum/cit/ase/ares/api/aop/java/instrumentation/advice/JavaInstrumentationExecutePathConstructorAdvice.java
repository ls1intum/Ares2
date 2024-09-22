package de.tum.cit.ase.ares.api.aop.java.instrumentation.advice;

import net.bytebuddy.asm.Advice;

public class JavaInstrumentationExecutePathConstructorAdvice {

    @Advice.OnMethodEnter
    public static void onEnter(
            @Advice.Origin("#t") String declaringTypeName,
            @Advice.AllArguments Object... parameters
    ) {
        JavaInstrumentationAdviceToolbox.checkFileSystemInteraction(
                "execute",
                declaringTypeName,
                "<init>",
                "",
                new Object[0],
                parameters
        );
    }
}
