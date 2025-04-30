package de.tum.cit.ase.ares.api.aop.java.instrumentation;

import java.io.File;
import java.io.IOException;
import java.lang.instrument.Instrumentation;
import java.lang.instrument.UnmodifiableClassException;
import java.lang.management.ManagementFactory;
import java.nio.file.Files;
import java.util.List;
import java.util.Map;

import de.tum.cit.ase.ares.api.aop.java.JavaAOPTestCaseSettings;
import de.tum.cit.ase.ares.api.aop.java.instrumentation.advice.JavaInstrumentationAdviceFileSystemToolbox;
import de.tum.cit.ase.ares.api.aop.java.instrumentation.advice.JavaInstrumentationAdviceThreadSystemToolbox;
import de.tum.cit.ase.ares.api.aop.java.instrumentation.advice.JavaInstrumentationDeletePathConstructorAdvice;
import de.tum.cit.ase.ares.api.aop.java.instrumentation.advice.JavaInstrumentationDeletePathMethodAdvice;
import de.tum.cit.ase.ares.api.aop.java.instrumentation.pointcut.JavaInstrumentationBindingDefinitions;
import de.tum.cit.ase.ares.api.aop.java.instrumentation.pointcut.JavaInstrumentationPointcutDefinitions;
import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.ClassFileLocator;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.dynamic.loading.ClassInjector;
import net.bytebuddy.matcher.ElementMatchers;
import net.bytebuddy.utility.JavaModule;

import static de.tum.cit.ase.ares.api.aop.java.instrumentation.advice.JavaInstrumentationAdviceFileSystemToolbox.localize;
import static java.util.Map.entry;

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
        File tempDir;
        try {
            tempDir = Files.createTempDirectory("bb-bootstrap").toFile();
        } catch (IOException e) {
            throw new SecurityException(e);
        }

        ClassInjector.UsingInstrumentation.of(
                tempDir,
                ClassInjector.UsingInstrumentation.Target.BOOTSTRAP,
                inst
        ).inject(Map.ofEntries(
                // your existing helpers
                entry(
                        new TypeDescription.ForLoadedType(JavaInstrumentationAdviceFileSystemToolbox.class),
                        ClassFileLocator.ForClassLoader.read(JavaInstrumentationAdviceFileSystemToolbox.class)
                ),
                entry(
                        new TypeDescription.ForLoadedType(JavaInstrumentationAdviceThreadSystemToolbox.class),
                        ClassFileLocator.ForClassLoader.read(JavaInstrumentationAdviceThreadSystemToolbox.class)
                ),
                entry(
                        new TypeDescription.ForLoadedType(JavaAOPTestCaseSettings.class),
                        ClassFileLocator.ForClassLoader.read(JavaAOPTestCaseSettings.class)
                ),
                entry(
                        new TypeDescription.ForLoadedType(JavaInstrumentationBindingDefinitions.class),
                        ClassFileLocator.ForClassLoader.read(JavaInstrumentationBindingDefinitions.class)
                ),
                entry(
                        new TypeDescription.ForLoadedType(JavaInstrumentationDeletePathMethodAdvice.class),
                        ClassFileLocator.ForClassLoader.read(JavaInstrumentationDeletePathMethodAdvice.class)
                ),
                entry(
                        new TypeDescription.ForLoadedType(JavaInstrumentationDeletePathConstructorAdvice.class),
                        ClassFileLocator.ForClassLoader.read(JavaInstrumentationDeletePathConstructorAdvice.class)
                )
        ));


        installAgentBuilder(
                inst,
                JavaInstrumentationPointcutDefinitions.methodsWhichCanDeleteFiles,
                JavaInstrumentationBindingDefinitions::createDeletePathMethodBinding
        );
        installAgentBuilder(
                inst,
                JavaInstrumentationPointcutDefinitions.methodsWhichCanDeleteFiles,
                JavaInstrumentationBindingDefinitions::createDeletePathConstructorBinding
        );

        try {
            Class<?> winProv = Class.forName(
                    "sun.nio.fs.WindowsFileSystemProvider",
                    false,   // don’t init again
                    null     // bootstrap loader
            );
            inst.retransformClasses(winProv);
            System.out.println("triggered retransform for " + winProv);

            
        } catch (Exception e) {
            e.printStackTrace();
        }
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
                    .with(new AgentBuilder.Listener.Adapter() {
                        @Override
                        public void onTransformation(
                                TypeDescription td,
                                ClassLoader cl,
                                JavaModule module,
                                boolean loaded,
                                DynamicType type
                        ) {
                            System.out.println("Transforming " + td.getName() + ", builder plan:");
                            List deleteMethod = td.getDeclaredMethods().stream().filter(x -> x.getName().contains("delete")).toList();
                            System.out.println(deleteMethod);
                            System.out.println(type);
                        }
                    })
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
