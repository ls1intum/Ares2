package de.tum.cit.ase.ares.api.aop.java.instrumentation;

import java.lang.instrument.Instrumentation;
import java.util.List;
import java.util.Map;

import de.tum.cit.ase.ares.api.aop.java.instrumentation.pointcut.JavaInstrumentationBindingDefinitions;
import de.tum.cit.ase.ares.api.aop.java.instrumentation.pointcut.JavaInstrumentationPointcutDefinitions;
import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.matcher.ElementMatchers;

import static de.tum.cit.ase.ares.api.aop.java.instrumentation.advice.JavaInstrumentationAdviceFileSystemToolbox.localize;

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
        installAgentBuilder(inst, JavaInstrumentationPointcutDefinitions.methodsWhichCanReadFiles, JavaInstrumentationBindingDefinitions::createReadPathMethodBinding);
        installAgentBuilder(inst, JavaInstrumentationPointcutDefinitions.methodsWhichCanOverwriteFiles, JavaInstrumentationBindingDefinitions::createOverwritePathMethodBinding);
        installAgentBuilder(inst, JavaInstrumentationPointcutDefinitions.methodsWhichCanExecuteFiles, JavaInstrumentationBindingDefinitions::createExecutePathMethodBinding);
        installAgentBuilder(inst, JavaInstrumentationPointcutDefinitions.methodsWhichCanDeleteFiles, JavaInstrumentationBindingDefinitions::createDeletePathMethodBinding);
        installAgentBuilder(inst, JavaInstrumentationPointcutDefinitions.methodsWhichCanCreateThreads, JavaInstrumentationBindingDefinitions::createCreateThreadMethodBinding);

        installAgentBuilder(inst, JavaInstrumentationPointcutDefinitions.methodsWhichCanReadFiles, JavaInstrumentationBindingDefinitions::createReadPathConstructorBinding);
        installAgentBuilder(inst, JavaInstrumentationPointcutDefinitions.methodsWhichCanOverwriteFiles, JavaInstrumentationBindingDefinitions::createOverwritePathConstructorBinding);
        installAgentBuilder(inst, JavaInstrumentationPointcutDefinitions.methodsWhichCanExecuteFiles, JavaInstrumentationBindingDefinitions::createExecutePathConstructorBinding);
        installAgentBuilder(inst, JavaInstrumentationPointcutDefinitions.methodsWhichCanDeleteFiles, JavaInstrumentationBindingDefinitions::createDeletePathConstructorBinding);
        installAgentBuilder(inst, JavaInstrumentationPointcutDefinitions.methodsWhichCanCreateThreads, JavaInstrumentationBindingDefinitions::createCreateThreadConstructorBinding);
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
            Map<String, List<String>> methodsMap,
            AgentBuilder.Transformer transformer
    ) {
        try {
            new AgentBuilder
                    .Default()
                    .ignore(ElementMatchers.none())
                    .with(AgentBuilder.RedefinitionStrategy.RETRANSFORMATION)
                    .type(JavaInstrumentationPointcutDefinitions.getClassesMatcher(methodsMap))
                    .transform(transformer)
                    .installOn(inst);
        } catch (Exception e) {
            throw new SecurityException(localize("security.instrumentation.agent.installation.error", String.join(", ", methodsMap.keySet())), e);

        }
    }
}
