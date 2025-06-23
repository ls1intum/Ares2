package de.tum.cit.ase.ares.api.aop.java.instrumentation.advice;

import net.bytebuddy.asm.Advice;

/**
 * This class provides advice for the execution of constructors creating threads.
 * It is responsible for verifying whether the constructor execution is allowed based on the thread system
 * security policies defined within the application.
 * <p>
 * If an execution attempt violates these policies, a SecurityException is thrown, preventing
 * unauthorized thread creation. The class interacts with the JavaInstrumentationAdviceFileSystemToolbox to
 * perform these security checks.
 */
public class JavaInstrumentationExecuteCommandConstructorAdvice {
    /**
     * This method is called when a constructor that can execute commands is entered.
     * It performs security checks to determine whether the constructor execution is allowed according
     * to command system security policies. If the constructor execution is not permitted, a SecurityException
     * is thrown, blocking the execution.
     * <p>
     * The checkCommandSystemInteraction method from JavaInstrumentationAdviceCommandSystemToolbox is called to
     * perform these checks, ensuring that the constructor's parameters
     * adhere to the security restrictions.
     *
     * @param declaringTypeName The name of the class that declares the constructor.
     * @param parameters        The parameters passed to the constructor being executed.
     */
    @Advice.OnMethodEnter
    public static void onEnter(
            @Advice.Origin("#t") String declaringTypeName,
            @Advice.AllArguments Object... parameters
    ) {

        JavaInstrumentationAdviceCommandSystemToolbox.checkCommandSystemInteraction(
                "execute",
                declaringTypeName,
                "<init>",
                "",
                new Object[0],
                parameters
        );
    }
}
