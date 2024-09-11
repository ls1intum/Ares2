package de.tum.cit.ase.ares.api.aop.java.instrumentation.pointcut;

import de.tum.cit.ase.ares.api.aop.java.JavaSecurityTestCaseSettings;
import de.tum.cit.ase.ares.api.aop.java.instrumentation.advice.JavaInstrumentationAdviceToolbox;
import de.tum.cit.ase.ares.api.aop.java.instrumentation.advice.JavaInstrumentationDeletePathAdvice;
import de.tum.cit.ase.ares.api.aop.java.instrumentation.advice.JavaInstrumentationExecutePathAdvice;
import de.tum.cit.ase.ares.api.aop.java.instrumentation.advice.JavaInstrumentationReadPathAdvice;
import de.tum.cit.ase.ares.api.aop.java.instrumentation.advice.JavaInstrumentationOverwritePathAdvice;
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

    //<editor-fold desc="Constructor">
    private JavaInstrumentationBindingDefinitions() {
        throw new UnsupportedOperationException("Ares Security Error (Reason: Ares-Code; Stage: Creation): JavaInstrumentationBindingDefinitions is a utility class and should not be instantiated.");
    }
    //</editor-fold>

    //<editor-fold desc="Tools">
    private static DynamicType.Builder<?> createBinding(
            DynamicType.Builder<?> builder, TypeDescription typeDescription, ClassLoader classLoader,
            Map<String, List<String>> pointcuts, Class<?> advice
    ) {
        try{
            loadToolbox(classLoader);
            return builder.visit(Advice.to(advice).on(JavaInstrumentationPointcutDefinitions.getMethodsMatcher(typeDescription, pointcuts)));
        }
        catch (Exception e) {
            throw new SecurityException("Ares Security Error (Reason: Ares-Code; Stage: Creation): Could not create a binding in general.", e);
        }

    }

    private static void loadToolbox(ClassLoader classLoader) {
        try {
            new ClassInjector
                    .UsingUnsafe(classLoader)
                    .inject(Map.of(
                            new TypeDescription.ForLoadedType(JavaInstrumentationAdviceToolbox.class),
                            ClassFileLocator.ForClassLoader.read(JavaInstrumentationAdviceToolbox.class),
                            new TypeDescription.ForLoadedType(JavaSecurityTestCaseSettings.class),
                            ClassFileLocator.ForClassLoader.read(JavaSecurityTestCaseSettings.class)
                    ));
        }
        catch (Exception e) {
            throw new SecurityException("Ares Security Error (Reason: Ares-Code; Stage: Creation): Could not add the related classes to the bootstrap loader.", e);
        }

    }
    //</editor-fold>

    //<editor-fold desc="Read Path">
    public static DynamicType.Builder<?> createReadPathBinding(
            DynamicType.Builder<?> builder, TypeDescription typeDescription,
            ClassLoader classLoader, JavaModule ignoredJavaModule,
            ProtectionDomain ignoredProtectionDomain
    ) {
        try {
            return createBinding(
                    builder, typeDescription, classLoader,
                    JavaInstrumentationPointcutDefinitions.methodsWhichCanReadFiles, JavaInstrumentationReadPathAdvice.class
            );
        } catch (Exception e) {
            throw new SecurityException("Ares Security Error (Reason: Ares-Code; Stage: Creation): Could not create binding for read path.", e);
        }
    }
    //</editor-fold>

    //<editor-fold desc="Overwrite Path">
    public static DynamicType.Builder<?> createOverwritePathBinding(
            DynamicType.Builder<?> builder, TypeDescription typeDescription,
            ClassLoader classLoader, JavaModule ignoredJavaModule,
            ProtectionDomain ignoredProtectionDomain
    ) {
        try {
            return createBinding(
                    builder, typeDescription, classLoader,
                    JavaInstrumentationPointcutDefinitions.methodsWhichCanOverwriteFiles, JavaInstrumentationOverwritePathAdvice.class
            );
        } catch (Exception e) {
            throw new SecurityException("Ares Security Error (Reason: Ares-Code; Stage: Creation): Could not create binding for overwrite path.", e);
        }

    }
    //</editor-fold>

    //<editor-fold desc="Execute Path">
    public static DynamicType.Builder<?> createExecutePathBinding(
            DynamicType.Builder<?> builder, TypeDescription typeDescription,
            ClassLoader classLoader, JavaModule ignoredJavaModule,
            ProtectionDomain ignoredProtectionDomain
    ) {
        try {
            return createBinding(
                    builder, typeDescription, classLoader,
                    JavaInstrumentationPointcutDefinitions.methodsWhichCanExecuteFiles, JavaInstrumentationExecutePathAdvice.class
            );
        } catch (Exception e) {
            throw new SecurityException("Ares Security Error (Reason: Ares-Code; Stage: Creation): Could not create binding for execute path.", e);
        }
    }
    //</editor-fold>

    //<editor-fold desc="Delete Path">
    public static DynamicType.Builder<?> createDeletePathBinding(
            DynamicType.Builder<?> builder, TypeDescription typeDescription,
            ClassLoader classLoader, JavaModule ignoredJavaModule,
            ProtectionDomain ignoredProtectionDomain
    ) {
        try {
            return createBinding(
                    builder, typeDescription, classLoader,
                    JavaInstrumentationPointcutDefinitions.methodsWhichCanDeleteFiles, JavaInstrumentationDeletePathAdvice.class
            );
        } catch (Exception e) {
            throw new SecurityException("Ares Security Error (Reason: Ares-Code; Stage: Creation): Could not create binding for delete path.", e);
        }
    }
    //</editor-fold>
}

