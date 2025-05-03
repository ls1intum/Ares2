package de.tum.cit.ase.ares.api.aop.java.instrumentation.pointcut;

import de.tum.cit.ase.ares.api.aop.java.JavaAOPTestCaseSettings;
import de.tum.cit.ase.ares.api.aop.java.instrumentation.advice.JavaInstrumentationAdviceThreadSystemToolbox;
import de.tum.cit.ase.ares.api.aop.java.instrumentation.advice.JavaInstrumentationAdviceFileSystemToolbox;
import de.tum.cit.ase.ares.api.aop.java.instrumentation.advice.JavaInstrumentationCreateThreadConstructorAdvice;
import de.tum.cit.ase.ares.api.aop.java.instrumentation.advice.JavaInstrumentationCreateThreadMethodAdvice;
import de.tum.cit.ase.ares.api.aop.java.instrumentation.advice.JavaInstrumentationDeletePathConstructorAdvice;
import de.tum.cit.ase.ares.api.aop.java.instrumentation.advice.JavaInstrumentationDeletePathMethodAdvice;
import de.tum.cit.ase.ares.api.aop.java.instrumentation.advice.JavaInstrumentationExecutePathConstructorAdvice;
import de.tum.cit.ase.ares.api.aop.java.instrumentation.advice.JavaInstrumentationExecutePathMethodAdvice;
import de.tum.cit.ase.ares.api.aop.java.instrumentation.advice.JavaInstrumentationOverwritePathConstructorAdvice;
import de.tum.cit.ase.ares.api.aop.java.instrumentation.advice.JavaInstrumentationOverwritePathMethodAdvice;
import de.tum.cit.ase.ares.api.aop.java.instrumentation.advice.JavaInstrumentationReadPathConstructorAdvice;
import de.tum.cit.ase.ares.api.aop.java.instrumentation.advice.JavaInstrumentationReadPathMethodAdvice;
import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.ClassFileLocator;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.dynamic.loading.ClassInjector;
import net.bytebuddy.matcher.ElementMatcher;
import net.bytebuddy.utility.JavaModule;

import java.security.ProtectionDomain;
import java.util.List;
import java.util.Map;

import static de.tum.cit.ase.ares.api.aop.java.instrumentation.advice.JavaInstrumentationAdviceFileSystemToolbox.localize;

/**
 * This class provides the definitions for the bindings of the Java instrumentation.
 * It is responsible for creating the bindings for different pointcuts, which represent file system
 * operations such as reading, overwriting, executing, or deleting files. These bindings enable
 * security-related advice to be applied to monitor and control file operations at runtime.
 * The advice ensures that file system interactions adhere to the established security policies,
 * preventing unauthorized or malicious file operations.
 */
public class JavaInstrumentationBindingDefinitions {

    //<editor-fold desc="Constructor">
    /**
     * This class is a utility class and should not be instantiated.
     */
    private JavaInstrumentationBindingDefinitions() {
        throw new SecurityException(localize("security.general.utility.initialization"));
    }
    //</editor-fold>

    //<editor-fold desc="Tools">
    /**
     * This method creates a binding for the given type description, class loader, pointcuts, and advice.
     * The binding connects the bytecode modification process to the specific methods defined by the pointcuts
     * and applies the provided advice. This ensures that security policies are enforced on methods interacting
     * with the file system, preventing unauthorized actions such as file manipulation or access.
     *
     * @param builder         The builder used to create the binding.
     * @param typeDescription The description of the class whose methods are being instrumented.
     * @param classLoader     The class loader responsible for loading the class.
     * @param pointcuts       The pointcuts that specify which methods should be instrumented.
     * @param advice          The advice to be applied to the methods matched by the pointcuts.
     * @return The builder with the binding applied.
     * @throws SecurityException If the binding could not be created, preventing the enforcement of security policies.
     */
    private static DynamicType.Builder<?> createMethodBinding(
            DynamicType.Builder<?> builder, TypeDescription typeDescription, ClassLoader classLoader, JavaModule javaModule, ProtectionDomain protectionDomain,
            Map<String, List<String>> pointcuts, Class<?> advice
    ) {
        try {
            //loadToolbox(classLoader);
            //return builder.visit(Advice.to(advice).on(JavaInstrumentationPointcutDefinitions.getMethodsMatcher(typeDescription, pointcuts)));

            String adviceClassName = advice.getName();
            ElementMatcher<? super MethodDescription> matcher =
                    JavaInstrumentationPointcutDefinitions.getMethodsMatcher(typeDescription, pointcuts);

            AgentBuilder.Transformer.ForAdvice transformer = new AgentBuilder.Transformer.ForAdvice()
                    .include(advice.getClassLoader())
                    .advice(matcher, adviceClassName);

            // Invoke the transformer rather than builder.visit(...)
            return transformer.transform(builder, typeDescription, classLoader, javaModule, protectionDomain);
        } catch (Exception e) {
            throw new SecurityException(localize("security.instrumentation.binding.error"), e);
        }
    }

    private static DynamicType.Builder<?> createConstructorBinding(
            DynamicType.Builder<?> builder, TypeDescription typeDescription, ClassLoader classLoader, JavaModule javaModule, ProtectionDomain protectionDomain,
            Map<String, List<String>> pointcuts, Class<?> advice
    ) {
        try {
            //loadToolbox(classLoader);
            //return builder.visit(Advice.to(advice).on(JavaInstrumentationPointcutDefinitions.getConstructorsMatcher(typeDescription, pointcuts)));

            String adviceClassName = advice.getName();
            ElementMatcher<? super MethodDescription> matcher =
                    JavaInstrumentationPointcutDefinitions.getConstructorsMatcher(typeDescription, pointcuts);

            AgentBuilder.Transformer.ForAdvice transformer = new AgentBuilder.Transformer.ForAdvice()
                    .include(advice.getClassLoader())
                    .advice(matcher, adviceClassName);

            return transformer.transform(builder, typeDescription, classLoader, javaModule, protectionDomain);

        } catch (Exception e) {
            throw new SecurityException(localize("security.instrumentation.binding.error"), e);
        }
    }

    /**
     * This method loads the toolbox classes into the bootstrap loader using the provided class loader.
     * It ensures that the required classes for the instrumentation (such as the advice and test case settings)
     * are available in the runtime environment. This process is essential for enforcing the necessary
     * security checks during file system operations, ensuring that all file interactions comply with
     * the security policies defined in the advice.
     *
     * @param classLoader The class loader responsible for loading the toolbox classes.
     * @throws SecurityException If the toolbox classes could not be loaded into the bootstrap loader,
     *                           potentially affecting the integrity of the security checks.
     */
    private static void loadToolbox(ClassLoader classLoader) {
        try {
            //new
            ClassInjector
                    .UsingUnsafe
                    .ofBootLoader()
                    //.UsingUnsafe(classLoader)
                    .inject(Map.of(
                            new TypeDescription.ForLoadedType(JavaInstrumentationAdviceFileSystemToolbox.class),
                            ClassFileLocator.ForClassLoader.read(JavaInstrumentationAdviceFileSystemToolbox.class),
                            new TypeDescription.ForLoadedType(JavaInstrumentationAdviceThreadSystemToolbox.class),
                            ClassFileLocator.ForClassLoader.read(JavaInstrumentationAdviceThreadSystemToolbox.class),
                            new TypeDescription.ForLoadedType(JavaAOPTestCaseSettings.class),
                            ClassFileLocator.ForClassLoader.read(JavaAOPTestCaseSettings.class)
                    ));
        }
        catch (Exception e) {
            throw new SecurityException("Ares Security Error (Reason: Ares-Code; Stage: Creation): Could not add the related classes to the bootstrap loader.", e);
        }

    }
    //</editor-fold>

    //<editor-fold desc="Read Path">
    /**
     * This method creates a binding for the read path pointcut. It applies the instrumentation advice
     * for file read operations defined in the corresponding pointcuts, ensuring that security-related
     * advice is applied when methods that read files are invoked. This helps to enforce security policies
     * related to file read operations, preventing unauthorized access to files.
     *
     * @param builder                 The builder used to create the binding.
     * @param typeDescription         The description of the class whose methods are being instrumented.
     * @param classLoader             The class loader responsible for loading the class.
     * @param javaModule       The Java module being ignored (for compatibility reasons).
     * @param protectionDomain The protection domain being ignored (for compatibility reasons).
     * @return The builder with the binding applied for file read operations.
     * @throws SecurityException If the binding could not be created for the read path, preventing the security advice from being applied.
     */
    public static DynamicType.Builder<?> createReadPathMethodBinding(
            DynamicType.Builder<?> builder, TypeDescription typeDescription,
            ClassLoader classLoader, JavaModule javaModule,
            ProtectionDomain protectionDomain
    ) {
        try {
            return createMethodBinding(
                    builder, typeDescription, classLoader, javaModule, protectionDomain,
                    JavaInstrumentationPointcutDefinitions.methodsWhichCanReadFiles, JavaInstrumentationReadPathMethodAdvice.class
            );
        } catch (Exception e) {
            throw new SecurityException(localize("security.instrumentation.read.method.binding.error"), e);
        }
    }

    public static DynamicType.Builder<?> createReadPathConstructorBinding(
            DynamicType.Builder<?> builder, TypeDescription typeDescription,
            ClassLoader classLoader, JavaModule javaModule,
            ProtectionDomain protectionDomain
    ) {
        try {
            return createConstructorBinding(
                    builder, typeDescription, classLoader, javaModule, protectionDomain,
                    JavaInstrumentationPointcutDefinitions.methodsWhichCanReadFiles, JavaInstrumentationReadPathConstructorAdvice.class
            );
        } catch (Exception e) {
            throw new SecurityException(localize("security.instrumentation.read.constructor.binding.error"), e);
        }
    }
    //</editor-fold>

    //<editor-fold desc="Overwrite Path">
    /**
     * This method creates a binding for the overwrite path pointcut. It applies the instrumentation advice
     * for file overwrite operations defined in the corresponding pointcuts, ensuring that security-related
     * advice is applied when methods that overwrite files are invoked. This ensures that unauthorized or
     * potentially harmful file overwriting actions are detected and prevented.
     *
     * @param builder                 The builder used to create the binding.
     * @param typeDescription         The description of the class whose methods are being instrumented.
     * @param classLoader             The class loader responsible for loading the class.
     * @param javaModule       The Java module being ignored (for compatibility reasons).
     * @param protectionDomain The protection domain being ignored (for compatibility reasons).
     * @return The builder with the binding applied for file overwrite operations.
     * @throws SecurityException If the binding could not be created for the overwrite path, preventing the application of security advice for file overwriting operations.
     */
    public static DynamicType.Builder<?> createOverwritePathMethodBinding(
            DynamicType.Builder<?> builder, TypeDescription typeDescription,
            ClassLoader classLoader, JavaModule javaModule,
            ProtectionDomain protectionDomain
    ) {
        try {
            return createMethodBinding(
                    builder, typeDescription, classLoader, javaModule, protectionDomain,
                    JavaInstrumentationPointcutDefinitions.methodsWhichCanOverwriteFiles, JavaInstrumentationOverwritePathMethodAdvice.class
            );
        } catch (Exception e) {
            throw new SecurityException(localize("security.instrumentation.overwrite.method.binding.error"), e);
        }

    }

    public static DynamicType.Builder<?> createOverwritePathConstructorBinding(
            DynamicType.Builder<?> builder, TypeDescription typeDescription,
            ClassLoader classLoader, JavaModule javaModule,
            ProtectionDomain protectionDomain
    ) {
        try {
            return createConstructorBinding(
                    builder, typeDescription, classLoader, javaModule, protectionDomain,
                    JavaInstrumentationPointcutDefinitions.methodsWhichCanOverwriteFiles, JavaInstrumentationOverwritePathConstructorAdvice.class
            );
        } catch (Exception e) {
            throw new SecurityException(localize("security.instrumentation.overwrite.constructor.binding.error"), e);
        }

    }
    //</editor-fold>

    //<editor-fold desc="Execute Path">
    /**
     * This method creates a binding for the execute path pointcut. It applies the instrumentation advice
     * for file execution operations defined in the corresponding pointcuts, ensuring that security-related
     * advice is applied when methods that execute files are invoked. This helps to prevent unauthorized
     * file executions that could compromise the system's integrity.
     *
     * @param builder                 The builder used to create the binding.
     * @param typeDescription         The description of the class whose methods are being instrumented.
     * @param classLoader             The class loader responsible for loading the class.
     * @param javaModule       The Java module being ignored (for compatibility reasons).
     * @param protectionDomain The protection domain being ignored (for compatibility reasons).
     * @return The builder with the binding applied for file execution operations.
     * @throws SecurityException If the binding could not be created for the execute path, preventing the enforcement of security policies for file execution.
     */
    public static DynamicType.Builder<?> createExecutePathMethodBinding(
            DynamicType.Builder<?> builder, TypeDescription typeDescription,
            ClassLoader classLoader, JavaModule javaModule,
            ProtectionDomain protectionDomain
    ) {
        try {
            return createMethodBinding(
                    builder, typeDescription, classLoader, javaModule, protectionDomain,
                    JavaInstrumentationPointcutDefinitions.methodsWhichCanExecuteFiles, JavaInstrumentationExecutePathMethodAdvice.class
            );
        } catch (Exception e) {
            throw new SecurityException(localize("security.instrumentation.execute.method.binding.error"), e);
        }
    }

    public static DynamicType.Builder<?> createExecutePathConstructorBinding(
            DynamicType.Builder<?> builder, TypeDescription typeDescription,
            ClassLoader classLoader, JavaModule javaModule,
            ProtectionDomain protectionDomain
    ) {
        try {
            return createConstructorBinding(
                    builder, typeDescription, classLoader, javaModule, protectionDomain,
                    JavaInstrumentationPointcutDefinitions.methodsWhichCanExecuteFiles, JavaInstrumentationExecutePathConstructorAdvice.class
            );
        } catch (Exception e) {
            throw new SecurityException(localize("security.instrumentation.execute.constructor.binding.error"), e);
        }
    }
    //</editor-fold>

    //<editor-fold desc="Delete Path">
    /**
     * This method creates a binding for the delete path pointcut. It applies the instrumentation advice
     * for file deletion operations defined in the corresponding pointcuts, ensuring that security-related
     * advice is applied when methods that delete files are invoked. This safeguards against unauthorized
     * or harmful file deletion operations.
     *
     * @param builder                 The builder used to create the binding.
     * @param typeDescription         The description of the class whose methods are being instrumented.
     * @param classLoader             The class loader responsible for loading the class.
     * @param javaModule       The Java module being ignored (for compatibility reasons).
     * @param protectionDomain The protection domain being ignored (for compatibility reasons).
     * @return The builder with the binding applied for file deletion operations.
     * @throws SecurityException If the binding could not be created for the delete path, preventing the enforcement of security policies for file deletion operations.
     */
    public static DynamicType.Builder<?> createDeletePathMethodBinding(
            DynamicType.Builder<?> builder, TypeDescription typeDescription,
            ClassLoader classLoader, JavaModule javaModule,
            ProtectionDomain protectionDomain
    ) {
        try {
            return createMethodBinding(
                    builder, typeDescription, classLoader, javaModule, protectionDomain,
                    JavaInstrumentationPointcutDefinitions.methodsWhichCanDeleteFiles, JavaInstrumentationDeletePathMethodAdvice.class
            );
        } catch (Exception e) {
            throw new SecurityException(localize("security.instrumentation.delete.method.binding.error"), e);
        }
    }

    public static DynamicType.Builder<?> createDeletePathConstructorBinding(
            DynamicType.Builder<?> builder, TypeDescription typeDescription,
            ClassLoader classLoader, JavaModule javaModule,
            ProtectionDomain protectionDomain
    ) {
        try {
            return createConstructorBinding(
                    builder, typeDescription, classLoader, javaModule, protectionDomain,
                    JavaInstrumentationPointcutDefinitions.methodsWhichCanDeleteFiles, JavaInstrumentationDeletePathConstructorAdvice.class
            );
        } catch (Exception e) {
            throw new SecurityException(localize("security.instrumentation.delete.constructor.binding.error"), e);
        }
    }
    //</editor-fold>

    //<editor-fold desc="Create Thread">
    /**
     * This method creates a binding for the create thread pointcut. It applies the instrumentation advice
     * for file deletion operations defined in the corresponding pointcuts, ensuring that security-related
     * advice is applied when methods that create threads are invoked. This safeguards against unauthorized
     * or harmful file deletion operations.
     *
     * @param builder                 The builder used to create the binding.
     * @param typeDescription         The description of the class whose methods are being instrumented.
     * @param classLoader             The class loader responsible for loading the class.
     * @param javaModule       The Java module being ignored (for compatibility reasons).
     * @param protectionDomain The protection domain being ignored (for compatibility reasons).
     * @return The builder with the binding applied for file deletion operations.
     * @throws SecurityException If the binding could not be created for the create thread, preventing the enforcement of security policies for file deletion operations.
     */
    public static DynamicType.Builder<?> createCreateThreadMethodBinding(
            DynamicType.Builder<?> builder, TypeDescription typeDescription,
            ClassLoader classLoader, JavaModule javaModule,
            ProtectionDomain protectionDomain
    ) {
        try {
            return createMethodBinding(
                    builder, typeDescription, classLoader, javaModule, protectionDomain,
                    JavaInstrumentationPointcutDefinitions.methodsWhichCanCreateThreads, JavaInstrumentationCreateThreadMethodAdvice.class
            );
        } catch (Exception e) {
            throw new SecurityException(localize("security.instrumentation.create.thread.method.binding.error"), e);
        }
    }

    public static DynamicType.Builder<?> createCreateThreadConstructorBinding(
            DynamicType.Builder<?> builder, TypeDescription typeDescription,
            ClassLoader classLoader, JavaModule javaModule,
            ProtectionDomain protectionDomain
    ) {
        try {
            return createConstructorBinding(
                    builder, typeDescription, classLoader, javaModule, protectionDomain,
                    JavaInstrumentationPointcutDefinitions.methodsWhichCanCreateThreads, JavaInstrumentationCreateThreadConstructorAdvice.class
            );
        } catch (Exception e) {
            throw new SecurityException(localize("security.instrumentation.create.thread.constructor.binding.error"), e);
        }
    }
    //</editor-fold>
}

