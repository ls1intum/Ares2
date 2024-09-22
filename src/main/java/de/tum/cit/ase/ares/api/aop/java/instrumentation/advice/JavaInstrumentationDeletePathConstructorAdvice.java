package de.tum.cit.ase.ares.api.aop.java.instrumentation.advice;

import net.bytebuddy.asm.Advice;

import java.util.Arrays;

public class JavaInstrumentationDeletePathConstructorAdvice {

    @Advice.OnMethodEnter
    public static void onEnter(
            @Advice.Origin("#t") String declaringTypeName,
            @Advice.AllArguments Object... parameters
    ) {
        JavaInstrumentationAdviceToolbox.checkFileSystemInteraction(
                "delete",
                declaringTypeName,
                "<init>",
                "",
                new Object[0],
                parameters
        );
    }
}
