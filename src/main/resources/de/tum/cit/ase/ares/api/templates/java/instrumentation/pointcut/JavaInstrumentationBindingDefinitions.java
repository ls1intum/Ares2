package %s.aspectconfiguration.java.instrumentation.pointcut;

import %s.aspectconfiguration.java.instrumentation.advice.JavaInstrumentationAdviceToolbox;
import %s.aspectconfiguration.java.instrumentation.advice.JavaInstrumentationExecutePathAdvice;
import %s.aspectconfiguration.java.instrumentation.advice.JavaInstrumentationOverwritePathAdvice;
import %s.aspectconfiguration.java.instrumentation.advice.JavaInstrumentationReadPathAdvice;
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
    public static DynamicType.Builder<?> createReadPathBinding(
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
    public static DynamicType.Builder<?> createOverwritePathBinding(
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
    public static DynamicType.Builder<?> createExecutePathBinding(
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

