package de.tum.cit.ase.ares.api.aspectconfiguration.java;

import de.tum.cit.ase.ares.api.aspectconfiguration.java.advice.JavaAdviceToolbox;
import de.tum.cit.ase.ares.api.aspectconfiguration.java.advice.JavaReadPathAdvice;
import de.tum.cit.ase.ares.api.aspectconfiguration.java.advice.JavaWritePathAdvice;
import de.tum.cit.ase.ares.api.aspectconfiguration.java.advice.JavaExecutePathAdvice;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.ClassFileLocator;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.dynamic.loading.ClassInjector;
import net.bytebuddy.utility.JavaModule;

import java.security.ProtectionDomain;
import java.util.List;
import java.util.Map;


public class JavaBindings {

    private JavaBindings() {
        throw new IllegalStateException("Utility class");
    }

    //<editor-fold desc="Tools">
    private static DynamicType.Builder<?> createBinding(
            DynamicType.Builder<?> builder, TypeDescription typeDescription, ClassLoader classLoader,
            Map<String, List<String>> pointcuts, Class<?> advice
    ) {
        loadToolbox(classLoader);
        return builder.visit(Advice.to(advice).on(JavaPointcuts.getMethodsMatcher(typeDescription, pointcuts)));
    }

    private static void loadToolbox(ClassLoader classLoader) {
        new ClassInjector.UsingUnsafe(classLoader)
                .inject(Map.of(
                        new TypeDescription.ForLoadedType(JavaAdviceToolbox.class),
                        ClassFileLocator.ForClassLoader.read(JavaAdviceToolbox.class)
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
                JavaPointcuts.methodsWhichCanReadFiles, JavaReadPathAdvice.class
        );
    }
    //</editor-fold>

    //<editor-fold desc="Write Path">
    static DynamicType.Builder<?> createWritePathBinding(
            DynamicType.Builder<?> builder, TypeDescription typeDescription,
            ClassLoader classLoader, JavaModule ignoredJavaModule,
            ProtectionDomain ignoredProtectionDomain
    ) {
        return createBinding(
                builder, typeDescription, classLoader,
                JavaPointcuts.methodsWhichCanWriteFiles, JavaWritePathAdvice.class
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
                JavaPointcuts.methodsWhichCanExecuteFiles, JavaExecutePathAdvice.class
        );
    }
    //</editor-fold>
}

