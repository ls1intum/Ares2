package de.tum.cit.ase.ares.api.aspectconfiguration.javainstrumentation.pointcut;

import de.tum.cit.ase.ares.api.aspectconfiguration.javainstrumentation.advice.JavaInstrumentationAdviceToolbox;
import de.tum.cit.ase.ares.api.aspectconfiguration.javainstrumentation.advice.JavaInstrumentationExecutePathAdvice;
import de.tum.cit.ase.ares.api.aspectconfiguration.javainstrumentation.advice.JavaInstrumentationReadPathAdvice;
import de.tum.cit.ase.ares.api.aspectconfiguration.javainstrumentation.advice.JavaInstrumentationOverwritePathAdvice;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.ClassFileLocator;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.dynamic.loading.ClassInjector;
import net.bytebuddy.utility.JavaModule;

import java.security.ProtectionDomain;
import java.util.List;
import java.util.Map;


public class JavaInstrumentationBindingDefinitions {

    private JavaInstrumentationBindingDefinitions() {
        throw new IllegalStateException("Utility class");
    }

    //<editor-fold desc="Tools">
    private static DynamicType.Builder<?> createBinding(
            DynamicType.Builder<?> builder, TypeDescription typeDescription, ClassLoader classLoader,
            Map<String, List<String>> pointcuts, Class<?> advice
    ) {
        loadToolbox(classLoader);
        return builder.visit(Advice.to(advice).on(JavaInstrumentationPointcutDefinitions.getMethodsMatcher(typeDescription, pointcuts)));
    }

    private static void loadToolbox(ClassLoader classLoader) {
        new ClassInjector.UsingUnsafe(classLoader)
                .inject(Map.of(
                        new TypeDescription.ForLoadedType(JavaInstrumentationAdviceToolbox.class),
                        ClassFileLocator.ForClassLoader.read(JavaInstrumentationAdviceToolbox.class)
                ));
    }
    //</editor-fold>

    //<editor-fold desc="Read Path">
    static DynamicType.Builder<?> createReadPathBinding(
            DynamicType.Builder<?> builder, TypeDescription typeDescription,
            ClassLoader classLoader, JavaModule ignoredJavaModule,
            ProtectionDomain ignoredProtectionDomain
    ) {
        return createBinding(
                builder, typeDescription, classLoader,
                JavaInstrumentationPointcutDefinitions.methodsWhichCanReadFiles, JavaInstrumentationReadPathAdvice.class
        );
    }
    //</editor-fold>

    //<editor-fold desc="Overwrite Path">
    static DynamicType.Builder<?> createOverwritePathBinding(
            DynamicType.Builder<?> builder, TypeDescription typeDescription,
            ClassLoader classLoader, JavaModule ignoredJavaModule,
            ProtectionDomain ignoredProtectionDomain
    ) {
        return createBinding(
                builder, typeDescription, classLoader,
                JavaInstrumentationPointcutDefinitions.methodsWhichCanOverwriteFiles, JavaInstrumentationOverwritePathAdvice.class
        );
    }
    //</editor-fold>

    //<editor-fold desc="Execute Path">
    static DynamicType.Builder<?> createExecutePathBinding(
            DynamicType.Builder<?> builder, TypeDescription typeDescription,
            ClassLoader classLoader, JavaModule ignoredJavaModule,
            ProtectionDomain ignoredProtectionDomain
    ) {
        return createBinding(
                builder, typeDescription, classLoader,
                JavaInstrumentationPointcutDefinitions.methodsWhichCanExecuteFiles, JavaInstrumentationExecutePathAdvice.class
        );
    }
    //</editor-fold>
}

