package %s.aspectconfiguration.java.instrumentation;

import %s.aspectconfiguration.java.instrumentation.pointcut.JavaInstrumentationBindingDefinitions;
import %s.aspectconfiguration.java.instrumentation.pointcut.JavaInstrumentationPointcutDefinitions;
import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.matcher.ElementMatchers;

import java.lang.instrument.Instrumentation;
import java.util.List;
import java.util.Map;

public class JavaInstrumentationAgent {

    public static void premain(String agentArgs, Instrumentation inst) {
        installAgentBuilder(inst, JavaInstrumentationPointcutDefinitions.methodsWhichCanReadFiles, JavaInstrumentationBindingDefinitions::createReadPathBinding);
        installAgentBuilder(inst, JavaInstrumentationPointcutDefinitions.methodsWhichCanOverwriteFiles, JavaInstrumentationBindingDefinitions::createOverwritePathBinding);
        installAgentBuilder(inst, JavaInstrumentationPointcutDefinitions.methodsWhichCanExecuteFiles, JavaInstrumentationBindingDefinitions::createExecutePathBinding);
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
                .type(JavaInstrumentationPointcutDefinitions.getClassesMatcher(methodsMap))
                .transform(transformer)
                .installOn(inst);
    }
}
