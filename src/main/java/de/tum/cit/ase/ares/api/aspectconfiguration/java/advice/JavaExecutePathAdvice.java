package de.tum.cit.ase.ares.api.aspectconfiguration.java.advice;

import java.lang.reflect.Field;

import static net.bytebuddy.asm.Advice.*;

public class JavaExecutePathAdvice {
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
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        //</editor-fold>

        //<editor-fold desc="Check">
        JavaAdviceToolbox.checkFileSystemInteraction(
                "execute",
                declaringTypeName,
                methodName,
                methodSignature,
                attributes,
                parameters
        );
        //</editor-fold>
    }
}