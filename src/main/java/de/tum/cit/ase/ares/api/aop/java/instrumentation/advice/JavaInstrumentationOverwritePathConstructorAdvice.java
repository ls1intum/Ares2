package de.tum.cit.ase.ares.api.aop.java.instrumentation.advice;

import net.bytebuddy.asm.Advice;

public class JavaInstrumentationOverwritePathConstructorAdvice {

    @Advice.OnMethodEnter
    public static void onEnter(
            @Advice.Origin("#t") String declaringTypeName,
            @Advice.AllArguments Object... parameters
    ) {
        JavaInstrumentationAdviceToolbox.checkFileSystemInteraction(
                "overwrite",
                declaringTypeName,
                "<init>",
                "",
                new Object[0],
                parameters
        );
    }
}
