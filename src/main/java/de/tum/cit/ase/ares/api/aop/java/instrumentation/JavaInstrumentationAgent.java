package de.tum.cit.ase.ares.api.aop.java.instrumentation;

import java.lang.instrument.Instrumentation;
import java.util.List;
import java.util.Map;

import de.tum.cit.ase.ares.api.aop.java.instrumentation.pointcut.JavaInstrumentationBindingDefinitions;
import de.tum.cit.ase.ares.api.aop.java.instrumentation.pointcut.JavaInstrumentationPointcutDefinitions;
import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.matcher.ElementMatchers;

public class JavaInstrumentationAgent {

    public static void premain(String agentArgs, Instrumentation inst) {
        installAgentBuilder(inst, JavaInstrumentationPointcutDefinitions.methodsWhichCanReadFiles, JavaInstrumentationBindingDefinitions::createReadPathBinding);
        installAgentBuilder(inst, JavaInstrumentationPointcutDefinitions.methodsWhichCanOverwriteFiles, JavaInstrumentationBindingDefinitions::createOverwritePathBinding);
        installAgentBuilder(inst, JavaInstrumentationPointcutDefinitions.methodsWhichCanExecuteFiles, JavaInstrumentationBindingDefinitions::createExecutePathBinding);
        installAgentBuilder(inst, JavaInstrumentationPointcutDefinitions.methodsWhichCanDeleteFiles, JavaInstrumentationBindingDefinitions::createDeletePathBinding);
    }

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
            throw new SecurityException("Ares Security Error (Reason: Ares-Code; Stage: Creation): Failed to install agent builder on " + String.join(", ", methodsMap.keySet()) + ".", e);

        }
    }
}
