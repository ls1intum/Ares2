package %s.ares.api.aop.java.instrumentation.advice;

import net.bytebuddy.asm.Advice;

import java.lang.reflect.Field;
import java.lang.reflect.InaccessibleObjectException;

import static %s.ares.api.aop.java.instrumentation.advice.JavaInstrumentationAdviceFileSystemToolbox.localize;
import static net.bytebuddy.asm.Advice.OnMethodEnter;

/**
 * This class provides advice for the execution of methods reading files.
 * It is responsible for verifying whether the method execution is allowed based on the file system
 * security policies defined within the application.
 * <p>
 * If an execution attempt violates these policies, a SecurityException is thrown, preventing
 * unauthorized file readings. The class interacts with the JavaInstrumentationAdviceFileSystemToolbox to
 * perform these security checks.
 */
public class JavaInstrumentationReadPathMethodAdvice {
    /**
     * This method is called when a method reading files is entered.
     * It performs security checks to determine whether the method execution is allowed according
     * to file system security policies. If the method execution is not permitted, a SecurityException
     * is thrown, blocking the execution.
     * <p>
     * The checkFileSystemInteraction method from JavaInstrumentationAdviceFileSystemToolbox is called to
     * perform these checks, ensuring that both the method's parameters and the instance fields
     * adhere to the security restrictions.
     *
     * @param declaringTypeName The name of the class that declares the method.
     * @param methodName        The name of the method being executed.
     * @param methodSignature   The signature of the method being executed.
     * @param instance          The instance of the class that declares the method (if applicable).
     * @param parameters        The parameters passed to the method being executed.
     * @throws SecurityException If the method execution is not allowed based on security policies.
     */
    @OnMethodEnter
    public static void onEnter(
            @Advice.Origin("#t") String declaringTypeName,
            @Advice.Origin("#m") String methodName,
            @Advice.Origin("#s") String methodSignature,
            @Advice.This(optional = true) Object instance,
            @Advice.AllArguments Object... parameters
    ) {

        //<editor-fold desc="Attributes">
        final Field[] fields = instance != null ? instance.getClass().getDeclaredFields() : new Field[0];
        final Object[] attributes = new Object[fields.length];
        if (instance != null) {
            for (int i = 0; i < fields.length; i++) {
                try {
                    fields[i].setAccessible(true);
                    attributes[i] = fields[i].get(instance);
                } catch (InaccessibleObjectException e) {
                    throw new SecurityException(localize("security.instrumentation.inaccessible.object.exception", fields[i].getName(), instance.getClass().getName()), e);
                } catch (IllegalAccessException e) {
                    throw new SecurityException(localize("security.instrumentation.illegal.access.exception", fields[i].getName(), instance.getClass().getName()), e);
                } catch (IllegalArgumentException e) {
                    throw new SecurityException(localize("security.instrumentation.illegal.argument.exception", fields[i].getName(), fields[i].getDeclaringClass().getName(), instance.getClass().getName()), e);
                } catch (NullPointerException e) {
                    throw new SecurityException(localize("security.instrumentation.null.pointer.exception", fields[i].getName(), instance.getClass().getName()), e);
                } catch (ExceptionInInitializerError e) {
                    throw new SecurityException(localize("security.instrumentation.exception.in-initializer.error", fields[i].getName(), instance.getClass().getName()), e);
                }
            }
        }
        //</editor-fold>

        //<editor-fold desc="Check">
        JavaInstrumentationAdviceFileSystemToolbox.checkFileSystemInteraction(
                "read",
                declaringTypeName,
                methodName,
                methodSignature,
                attributes,
                parameters
        );
        //</editor-fold>
    }
}