package %s.api.aop.java.instrumentation.advice;

import net.bytebuddy.asm.Advice;

public class JavaInstrumentationReadPathConstructorAdvice {

    @Advice.OnMethodEnter
    public static void onEnter(
            @Advice.Origin("#t") String declaringTypeName,
            @Advice.AllArguments Object... parameters
    ) {
        JavaInstrumentationAdviceToolbox.checkFileSystemInteraction(
                "read",
                declaringTypeName,
                "<init>",
                "",
                new Object[0],
                parameters
        );
    }
}
