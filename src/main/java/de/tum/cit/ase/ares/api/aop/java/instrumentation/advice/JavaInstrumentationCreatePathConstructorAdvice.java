package de.tum.cit.ase.ares.api.aop.java.instrumentation.advice;

import net.bytebuddy.asm.Advice;

/**
 * Advice that guards constructors capable of creating files or directories.
 *
 * <p>The advice forwards constructor arguments to
 * {@link JavaInstrumentationAdviceFileSystemToolbox#checkFileSystemInteraction(String, String, String, String, Object[], Object[], Object)}
 * using the {@code create} action so the toolbox can enforce the creation policies.</p>
 */
public final class JavaInstrumentationCreatePathConstructorAdvice {

    /**
     * Intercepts the constructor execution before any bytecode of the constructor runs.
     *
     * @param declaringTypeName the fully qualified name of the declaring type
     * @param parameters        the original constructor arguments
     */
    @Advice.OnMethodEnter
    public static void onEnter(
            @Advice.Origin("#t") String declaringTypeName,
            @Advice.AllArguments Object... parameters
    ) {
        JavaInstrumentationAdviceFileSystemToolbox.checkFileSystemInteraction(
                "create",
                declaringTypeName,
                "<init>",
                "",
                new Object[0],
                parameters,
                null
        );
    }
}
