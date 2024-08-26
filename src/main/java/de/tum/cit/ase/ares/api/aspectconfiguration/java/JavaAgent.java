package de.tum.cit.ase.ares.api.aspectconfiguration.java;

import java.lang.instrument.Instrumentation;
import java.util.List;
import java.util.Map;

import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.matcher.ElementMatchers;

public class JavaAgent {

    public static void premain(String agentArgs, Instrumentation inst) {
        installAgentBuilder(inst, JavaPointcuts.methodsWhichCanReadFiles, JavaBindings::createReadPathBinding);
        installAgentBuilder(inst, JavaPointcuts.methodsWhichCanWriteFiles, JavaBindings::createWritePathBinding);
        installAgentBuilder(inst, JavaPointcuts.methodsWhichCanExecuteFiles, JavaBindings::createExecutePathBinding);
    }

    private static void installAgentBuilder(
            Instrumentation inst,
            Map<String, List<String>> methodsMap,
            AgentBuilder.Transformer transformer
    ) {
        new AgentBuilder
                .Default()
                .ignore(ElementMatchers.none())
                .with(AgentBuilder.RedefinitionStrategy.RETRANSFORMATION)
                .type(JavaPointcuts.getClassesMatcher(methodsMap))
                .transform(transformer)
                .installOn(inst);
    }
}
