package %s.api.aop.java.instrumentation.advice;

import net.bytebuddy.asm.Advice;

public class JavaInstrumentationDeletePathConstructorAdvice {

    @Advice.OnMethodEnter
    public static void onEnter(
            @Advice.Origin("#t") String declaringTypeName,
            @Advice.AllArguments Object... parameters
    ) {
        JavaInstrumentationAdviceFileSystemToolbox.checkFileSystemInteraction(
                "delete",
                declaringTypeName,
                "<init>",
                "",
                new Object[0],
                parameters
        );
    }
}
