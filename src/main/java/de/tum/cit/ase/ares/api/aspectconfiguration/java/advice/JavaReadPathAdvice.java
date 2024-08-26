package de.tum.cit.ase.ares.api.aspectconfiguration.java.advice;

import java.lang.reflect.Field;

import static net.bytebuddy.asm.Advice.*;

public class JavaReadPathAdvice {
    @OnMethodEnter
    public static void onEnter(
            @Origin("#t") String declaringTypeName,
            @Origin("#m") String methodName,
            @Origin("#s") String methodSignature,
            @This(optional = true) Object instance,
            @AllArguments Object... parameters
    ) {
        //<editor-fold desc="Settings">
        final String restrictedPackage = "de.tum.cit.ase";

        final String[] allowedClasses = {
                "de.tum.cit.ase.ares.api.aspectconfiguration.java.JavaAgent",
                "de.tum.cit.ase.ares.api.aspectconfiguration.java.JavaBindings",
                "de.tum.cit.ase.ares.api.aspectconfiguration.java.advice.JavaToolbox"
        };

        final String[] allowedPaths = {};
        //</editor-fold>

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
                "read",
                declaringTypeName, methodName, methodSignature, attributes, parameters,
                restrictedPackage, allowedClasses, allowedPaths
        );
        //</editor-fold>
    }
}