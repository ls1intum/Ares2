package de.tum.cit.ase.ares.api.aop.java.instrumentation;

import java.lang.instrument.Instrumentation;
import java.util.List;
import java.util.Map;

import de.tum.cit.ase.ares.api.aop.java.JavaAOPTestCaseSettings;
import de.tum.cit.ase.ares.api.aop.java.instrumentation.advice.IgnoreValues;
import de.tum.cit.ase.ares.api.aop.java.instrumentation.advice.JavaInstrumentationAdviceCommandSystemToolbox;
import de.tum.cit.ase.ares.api.aop.java.instrumentation.advice.JavaInstrumentationAdviceFileSystemToolbox;
import de.tum.cit.ase.ares.api.aop.java.instrumentation.advice.JavaInstrumentationAdviceThreadSystemToolbox;
import de.tum.cit.ase.ares.api.aop.java.instrumentation.pointcut.JavaInstrumentationBindingDefinitions;
import de.tum.cit.ase.ares.api.aop.java.instrumentation.pointcut.JavaInstrumentationPointcutDefinitions;
import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.dynamic.ClassFileLocator;
import net.bytebuddy.dynamic.loading.ClassInjector;
import net.bytebuddy.dynamic.loading.ClassInjector.UsingUnsafe.Factory;
import net.bytebuddy.matcher.ElementMatchers;

/**
 * This class is the entry point for the Java instrumentation agent.
 * It installs the agent builder for the different types of file operations.
 */
public class JavaInstrumentationAgent {

    /**
     * This method is called before the application's main method is called.
     * It installs the agent builder for the different types of file operations.
     *
     * @param agentArgs The agent arguments.
     * @param inst      The instrumentation instance.
     */
    public static void premain(String agentArgs, Instrumentation inst) {
        Factory unsafeFactory = Factory.resolve(inst);

        putToolboxOnBootClassLoader(unsafeFactory);

        installAgentBuilder(inst, unsafeFactory, JavaInstrumentationPointcutDefinitions.methodsWhichCanReadFiles, JavaInstrumentationBindingDefinitions::createReadPathMethodBinding);
        installAgentBuilder(inst, unsafeFactory, JavaInstrumentationPointcutDefinitions.methodsWhichCanOverwriteFiles, JavaInstrumentationBindingDefinitions::createOverwritePathMethodBinding);
        installAgentBuilder(inst, unsafeFactory, JavaInstrumentationPointcutDefinitions.methodsWhichCanExecuteFiles, JavaInstrumentationBindingDefinitions::createExecutePathMethodBinding);
        installAgentBuilder(inst, unsafeFactory, JavaInstrumentationPointcutDefinitions.methodsWhichCanDeleteFiles, JavaInstrumentationBindingDefinitions::createDeletePathMethodBinding);
        installAgentBuilder(inst, unsafeFactory, JavaInstrumentationPointcutDefinitions.methodsWhichCanCreateThreads, JavaInstrumentationBindingDefinitions::createCreateThreadMethodBinding);
        installAgentBuilder(inst, unsafeFactory, JavaInstrumentationPointcutDefinitions.methodsWhichCanExecuteCommands, JavaInstrumentationBindingDefinitions::createExecuteCommandMethodBinding);

        installAgentBuilder(inst, unsafeFactory, JavaInstrumentationPointcutDefinitions.methodsWhichCanReadFiles, JavaInstrumentationBindingDefinitions::createReadPathConstructorBinding);
        installAgentBuilder(inst, unsafeFactory, JavaInstrumentationPointcutDefinitions.methodsWhichCanOverwriteFiles, JavaInstrumentationBindingDefinitions::createOverwritePathConstructorBinding);
        installAgentBuilder(inst, unsafeFactory, JavaInstrumentationPointcutDefinitions.methodsWhichCanExecuteFiles, JavaInstrumentationBindingDefinitions::createExecutePathConstructorBinding);
        installAgentBuilder(inst, unsafeFactory, JavaInstrumentationPointcutDefinitions.methodsWhichCanDeleteFiles, JavaInstrumentationBindingDefinitions::createDeletePathConstructorBinding);
        installAgentBuilder(inst, unsafeFactory, JavaInstrumentationPointcutDefinitions.methodsWhichCanCreateThreads, JavaInstrumentationBindingDefinitions::createCreateThreadConstructorBinding);
        installAgentBuilder(inst, unsafeFactory, JavaInstrumentationPointcutDefinitions.methodsWhichCanExecuteCommands, JavaInstrumentationBindingDefinitions::createExecuteCommandConstructorBinding);
    }

    /**
     * This method is called when the agent is attached to a running JVM.
     * It installs the agent builder for the different types of file operations.
     *
     * @param agentArgs The agent arguments.
     * @param inst      The instrumentation instance.
     */
    public static void agentmain(String agentArgs, Instrumentation inst) {
        premain(agentArgs, inst);
    }

    private static void putToolboxOnBootClassLoader(
            Factory unsafeFactory
    ) {
        try {
            unsafeFactory
                    .make(null, null)
                    .injectRaw(Map.ofEntries(
                            Map.entry(
                                    JavaInstrumentationAdviceFileSystemToolbox.class.getName(),
                                    ClassFileLocator.ForClassLoader.read(JavaInstrumentationAdviceFileSystemToolbox.class)
                            ),
                            Map.entry(
                                    JavaInstrumentationAdviceThreadSystemToolbox.class.getName(),
                                    ClassFileLocator.ForClassLoader.read(JavaInstrumentationAdviceThreadSystemToolbox.class)
                            ),
                            Map.entry(JavaInstrumentationAdviceCommandSystemToolbox.class.getName(),
                                    ClassFileLocator.ForClassLoader.read(JavaInstrumentationAdviceCommandSystemToolbox.class)
                            ),
                            Map.entry(
                                    IgnoreValues.class.getName(),
                                    ClassFileLocator.ForClassLoader.read(IgnoreValues.class)
                            ),
                            Map.entry(
                                    IgnoreValues.Type.class.getName(),
                                    ClassFileLocator.ForClassLoader.read(IgnoreValues.Type.class)
                            ),
                            Map.entry(
                                    JavaAOPTestCaseSettings.class.getName(),
                                    ClassFileLocator.ForClassLoader.read(JavaAOPTestCaseSettings.class)
                            )

                    ));
        } catch (Exception e) {
            throw new SecurityException(JavaInstrumentationAdviceFileSystemToolbox.localize("security.instrumentation.agent.installation.error", "Putting the Toolbox on the BootClassLoader failed", e));
        }
    }

    /**
     * This method installs an agent builder for the provided methods map.
     * It applies a pointcut to match the methods and installs the transformer for bytecode modification.
     *
     * @param inst       The instrumentation instance used to instrument bytecode.
     * @param methodsMap A map containing method signatures as keys with lists of method names or parameters as values.
     *                   These define the pointcuts for instrumentation.
     * @param transformer The transformer responsible for applying the bytecode modifications at runtime.
     * @throws SecurityException If the installation of the agent builder fails.
     */
    private static void installAgentBuilder(
            Instrumentation inst,
            ClassInjector.UsingUnsafe.Factory unsafeFactory,
            Map<String, List<String>> methodsMap,
            AgentBuilder.Transformer transformer
    ) {
        try {
            new AgentBuilder
                    .Default()
                    .ignore(ElementMatchers.nameStartsWith("net.bytebuddy."))
                    .with(AgentBuilder.TypeStrategy.Default.REBASE)
                    .with(AgentBuilder.RedefinitionStrategy.RETRANSFORMATION)
                    .with(new AgentBuilder.InjectionStrategy.UsingUnsafe.OfFactory(unsafeFactory))
                    .disableClassFormatChanges()
                    .type(JavaInstrumentationPointcutDefinitions.getClassesMatcher(methodsMap))
                    .transform(transformer)
                    .installOn(inst);
        } catch (Exception e) {
            throw new SecurityException(JavaInstrumentationAdviceFileSystemToolbox.localize("security.instrumentation.agent.installation.error", String.join(", ", methodsMap.keySet())), e);
        }
    }
}
