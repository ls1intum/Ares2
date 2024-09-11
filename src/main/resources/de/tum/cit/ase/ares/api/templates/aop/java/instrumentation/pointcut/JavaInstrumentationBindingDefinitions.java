package %s.aop.java.instrumentation.pointcut;

import %s.aop.java.JavaSecurityTestCaseSettings;
import %s.aop.java.instrumentation.advice.JavaInstrumentationAdviceToolbox;
import %s.aop.java.instrumentation.advice.JavaInstrumentationExecutePathAdvice;
import %s.aop.java.instrumentation.advice.JavaInstrumentationReadPathAdvice;
import %s.aop.java.instrumentation.advice.JavaInstrumentationOverwritePathAdvice;
import %s.aop.java.instrumentation.advice.JavaInstrumentationDeletePathAdvice;
import %s.aop.java.instrumentation.pointcut.JavaInstrumentationPointcutDefinitions;
import de.tum.cit.ase.ares.api.aop.java.JavaSecurityTestCaseSettings;
import de.tum.cit.ase.ares.api.aop.java.instrumentation.advice.JavaInstrumentationAdviceToolbox;
import de.tum.cit.ase.ares.api.aop.java.instrumentation.advice.JavaInstrumentationDeletePathAdvice;
import de.tum.cit.ase.ares.api.aop.java.instrumentation.advice.JavaInstrumentationExecutePathAdvice;
import de.tum.cit.ase.ares.api.aop.java.instrumentation.advice.JavaInstrumentationOverwritePathAdvice;
import de.tum.cit.ase.ares.api.aop.java.instrumentation.advice.JavaInstrumentationReadPathAdvice;
import de.tum.cit.ase.ares.api.aop.java.instrumentation.pointcut.JavaInstrumentationPointcutDefinitions;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.ClassFileLocator;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.dynamic.loading.ClassInjector;
import net.bytebuddy.utility.JavaModule;

import java.security.ProtectionDomain;
import java.util.List;
import java.util.Map;


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
        throw new SecurityException("Ares Security Error (Reason: Ares-Code; Stage: Creation): JavaInstrumentationBindingDefinitions is a utility class and should not be instantiated.");
    }
    //</editor-fold>

    //<editor-fold desc="Tools">
    /**
     * This method creates a binding for the given type description, class loader, pointcuts, and advice.
     * The binding connects the bytecode modification process to the specific methods defined by the pointcuts
     * and applies the provided advice. This ensures that security policies are enforced on methods interacting
     * with the file system, preventing unauthorized actions such as file manipulation or access.
     *
     * @param builder The builder used to create the binding.
     * @param typeDescription The description of the class whose methods are being instrumented.
     * @param classLoader The class loader responsible for loading the class.
     * @param pointcuts The pointcuts that specify which methods should be instrumented.
     * @param advice The advice to be applied to the methods matched by the pointcuts.
     * @return The builder with the binding applied.
     * @throws SecurityException If the binding could not be created, preventing the enforcement of security policies.
     */
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
    /**
     * This method creates a binding for the read path pointcut. It applies the instrumentation advice
     * for file read operations defined in the corresponding pointcuts, ensuring that security-related
     * advice is applied when methods that read files are invoked. This helps to enforce security policies
     * related to file read operations, preventing unauthorized access to files.
     *
     * @param builder The builder used to create the binding.
     * @param typeDescription The description of the class whose methods are being instrumented.
     * @param classLoader The class loader responsible for loading the class.
     * @param ignoredJavaModule The Java module being ignored (for compatibility reasons).
     * @param ignoredProtectionDomain The protection domain being ignored (for compatibility reasons).
     * @return The builder with the binding applied for file read operations.
     * @throws SecurityException If the binding could not be created for the read path, preventing the security advice from being applied.
     */
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
    /**
     * This method creates a binding for the overwrite path pointcut. It applies the instrumentation advice
     * for file overwrite operations defined in the corresponding pointcuts, ensuring that security-related
     * advice is applied when methods that overwrite files are invoked. This ensures that unauthorized or
     * potentially harmful file overwriting actions are detected and prevented.
     *
     * @param builder The builder used to create the binding.
     * @param typeDescription The description of the class whose methods are being instrumented.
     * @param classLoader The class loader responsible for loading the class.
     * @param ignoredJavaModule The Java module being ignored (for compatibility reasons).
     * @param ignoredProtectionDomain The protection domain being ignored (for compatibility reasons).
     * @return The builder with the binding applied for file overwrite operations.
     * @throws SecurityException If the binding could not be created for the overwrite path, preventing the application of security advice for file overwriting operations.
     */
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
    /**
     * This method creates a binding for the execute path pointcut. It applies the instrumentation advice
     * for file execution operations defined in the corresponding pointcuts, ensuring that security-related
     * advice is applied when methods that execute files are invoked. This helps to prevent unauthorized
     * file executions that could compromise the system's integrity.
     *
     * @param builder The builder used to create the binding.
     * @param typeDescription The description of the class whose methods are being instrumented.
     * @param classLoader The class loader responsible for loading the class.
     * @param ignoredJavaModule The Java module being ignored (for compatibility reasons).
     * @param ignoredProtectionDomain The protection domain being ignored (for compatibility reasons).
     * @return The builder with the binding applied for file execution operations.
     * @throws SecurityException If the binding could not be created for the execute path, preventing the enforcement of security policies for file execution.
     */
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
    /**
     * This method creates a binding for the delete path pointcut. It applies the instrumentation advice
     * for file deletion operations defined in the corresponding pointcuts, ensuring that security-related
     * advice is applied when methods that delete files are invoked. This safeguards against unauthorized
     * or harmful file deletion operations.
     *
     * @param builder The builder used to create the binding.
     * @param typeDescription The description of the class whose methods are being instrumented.
     * @param classLoader The class loader responsible for loading the class.
     * @param ignoredJavaModule The Java module being ignored (for compatibility reasons).
     * @param ignoredProtectionDomain The protection domain being ignored (for compatibility reasons).
     * @return The builder with the binding applied for file deletion operations.
     * @throws SecurityException If the binding could not be created for the delete path, preventing the enforcement of security policies for file deletion operations.
     */
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