package de.tum.cit.ase.ares.api.aop.java.instrumentation.advice;

import java.lang.reflect.Field;
import java.lang.reflect.InaccessibleObjectException;

import static net.bytebuddy.asm.Advice.*;

public class JavaInstrumentationDeletePathAdvice {
    @OnMethodEnter
    public static void onEnter(
            @Origin("#t") String declaringTypeName,
            @Origin("#m") String methodName,
            @Origin("#s") String methodSignature,
            @This(optional = true) Object instance,
            @AllArguments Object... parameters
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
                    throw new SecurityException("Ares Security Error (Reason: Ares-Code; Stage: Execution): Unable to make field '" + fields[i].getName() + "' in class '"
                            + instance.getClass().getName() + "' accessible due to JVM security restrictions.", e);
                } catch (IllegalAccessException e) {
                    throw new SecurityException("Ares Security Error (Reason: Ares-Code; Stage: Execution): Access denied to field '" + fields[i].getName()
                            + "' in class '" + instance.getClass().getName() + "'. Field access is not permitted.", e);
                } catch (IllegalArgumentException e) {
                    throw new SecurityException("Ares Security Error (Reason: Ares-Code; Stage: Execution): Field '" + fields[i].getName() + "' in class '"
                            + fields[i].getDeclaringClass().getName() + "' cannot be accessed because the provided instance is of type '"
                            + instance.getClass().getName() + "', which is not the declaring class or interface.", e);
                } catch (NullPointerException e) {
                    throw new SecurityException("Ares Security Error (Reason: Ares-Code; Stage: Execution): The field '" + fields[i].getName()
                            + "' in class '" + instance.getClass().getName() + "' is unexpectedly null. This may indicate a corrupt or improperly initialized object.", e);
                } catch (ExceptionInInitializerError e) {
                    throw new SecurityException("Ares Security Error (Reason: Ares-Code; Stage: Execution): Initialization of the field '" + fields[i].getName()
                            + "' in class '" + instance.getClass().getName() + "' failed due to an error during static initialization or field setup.", e);
                }
            }
        }
        //</editor-fold>

        //<editor-fold desc="Check">
        JavaInstrumentationAdviceToolbox.checkFileSystemInteraction(
                "delete",
                declaringTypeName,
                methodName,
                methodSignature,
                attributes,
                parameters
        );
        //</editor-fold>
    }
}