package de.tum.cit.ase.ares.api.aspectconfiguration.java;

import de.tum.cit.ase.ares.api.aspectconfiguration.java.aspectj.JavaAspectJConfiguration;
import de.tum.cit.ase.ares.api.aspectconfiguration.java.aspectj.JavaAspectJConfigurationSettings;
import de.tum.cit.ase.ares.api.aspectconfiguration.java.instrumentation.JavaInstrumentationConfiguration;
import de.tum.cit.ase.ares.api.aspectconfiguration.java.instrumentation.JavaInstrumentationConfigurationSettings;
import de.tum.cit.ase.ares.api.util.FileTools;

import java.nio.file.Path;
import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * Enum representing the different modes of Aspect-Oriented Programming (AOP)
 * available for Java in Ares.
 *
 * <p>This enum provides two modes:</p>
 * <ul>
 *   <li>{@link #INSTRUMENTATION} - A mode using Java Instrumentation for weaving aspects into the code.</li>
 *   <li>{@link #ASPECTJ} - A mode using AspectJ for weaving aspects into the code.</li>
 * </ul>
 */
public enum JavaAOPMode {

    /**
     * Instrumentation mode.
     *
     * <p>In this mode, Java Instrumentation is used to weave aspects into the code at load-time.</p>
     */
    INSTRUMENTATION,

    /**
     * AspectJ mode.
     *
     * <p>In this mode, AspectJ is used to weave aspects into the code at compile-time.</p>
     */
    ASPECTJ;

    public List<Path> filesToCopy() {
        return (switch (this) {
            case ASPECTJ -> Stream.of(
                    new String[]{"templates", "java", "aspectj", "adviceandpointcut", "JavaAspectJFileSystemAdviceDefinitions.aj"},
                    new String[]{"templates", "java", "aspectj", "adviceandpointcut", "JavaAspectJFileSystemPointcutDefinitions.aj"}
            );
            case INSTRUMENTATION -> Stream.of(
                    new String[]{"templates", "java", "instrumentation", "advice", "JavaInstrumentationAdviceToolbox.java"},
                    new String[]{"templates", "java", "instrumentation", "advice", "JavaInstrumentationReadPathAdvice.java"},
                    new String[]{"templates", "java", "instrumentation", "advice", "JavaInstrumentationOverwritePathAdvice.java"},
                    new String[]{"templates", "java", "instrumentation", "advice", "JavaInstrumentationExecutePathAdvice.java"},
                    new String[]{"templates", "java", "instrumentation", "pointcut", "JavaInstrumentationPointcutDefinitions.java"},
                    new String[]{"templates", "java", "instrumentation", "pointcut", "JavaInstrumentationBindingDefinitions.java"},
                    new String[]{"templates", "java", "instrumentation", "JavaInstrumentationAgent.java"}
            );
        }).map(FileTools::resolveOnResources).toList();
    }

    public List<String[]> fileValues(String packageName) {
        return switch (this) {
            case ASPECTJ -> Stream.of(
                            IntStream.range(0, 42).mapToObj(i -> packageName).toArray(String[]::new),
                            IntStream.range(0, 38).mapToObj(i -> packageName).toArray(String[]::new))
                    .toList();
            case INSTRUMENTATION -> Stream.of(
                            new String[]{packageName, packageName, packageName, packageName, packageName, packageName, packageName, packageName, packageName},
                            new String[]{packageName},
                            new String[]{packageName},
                            new String[]{packageName},
                            new String[]{packageName},
                            new String[]{packageName, packageName, packageName, packageName, packageName},
                            new String[]{packageName, packageName, packageName}
                    )
                    .toList();
        };
    }

    public List<Path> targetsToCopyTo(Path projectPath, String packageName) {
        return (switch (this) {
            case ASPECTJ -> Stream.of(
                    new String[]{"aspectconfiguration", "java", "aspectj", "adviceandpointcut", "JavaAspectJFileSystemAdviceDefinitions.aj"},
                    new String[]{"aspectconfiguration", "java", "aspectj", "adviceandpointcut", "JavaAspectJFileSystemPointcutDefinitions.aj"}
            );
            case INSTRUMENTATION -> Stream.of(
                    new String[]{"aspectconfiguration", "java", "instrumentation", "advice", "JavaInstrumentationAdviceToolbox.java"},
                    new String[]{"aspectconfiguration", "java", "instrumentation", "advice", "JavaInstrumentationReadPathAdvice.java"},
                    new String[]{"aspectconfiguration", "java", "instrumentation", "advice", "JavaInstrumentationOverwritePathAdvice.java"},
                    new String[]{"aspectconfiguration", "java", "instrumentation", "advice", "JavaInstrumentationExecutePathAdvice.java"},
                    new String[]{"aspectconfiguration", "java", "instrumentation", "pointcut", "JavaInstrumentationPointcutDefinitions.java"},
                    new String[]{"aspectconfiguration", "java", "instrumentation", "pointcut", "JavaInstrumentationBindingDefinitions.java"},
                    new String[]{"aspectconfiguration", "java", "instrumentation", "JavaInstrumentationAgent.java"}
            );
        }).map(pathParticles -> FileTools.resolveOnTests(projectPath, packageName, pathParticles)).toList();
    }

    public Path threePartedFileHeader() {
        return FileTools.resolveOnResources(switch (this) {
            case ASPECTJ ->
                    new String[]{"templates", "java", "aspectj", "JavaAspectJConfigurationCollectionHeader.txt"};
            case INSTRUMENTATION ->
                    new String[]{"templates", "java", "instrumentation", "JavaInstrumentationConfigurationCollectionHeader.txt"};
        });
    }

    public String threePartedFileBody(List<?> configuration) {
        return switch (this) {
            case ASPECTJ ->
                    JavaAspectJConfiguration.createAspectConfigurationFileFullContent((List<JavaAspectJConfiguration>) configuration);
            case INSTRUMENTATION ->
                    JavaInstrumentationConfiguration.createAspectConfigurationFileFullContent((List<JavaInstrumentationConfiguration>) configuration);
        };
    }

    public Path threePartedFileFooter() {
        return FileTools.resolveOnResources(switch (this) {
            case ASPECTJ ->
                    new String[]{"templates", "java", "aspectj", "JavaAspectJConfigurationCollectionFooter.txt"};
            case INSTRUMENTATION ->
                    new String[]{"templates", "java", "instrumentation", "JavaInstrumentationConfigurationCollectionFooter.txt"};
        });
    }

    public String[] fileValue(String packageName) {
        return switch (this) {
            case ASPECTJ -> new String[]{packageName};
            case INSTRUMENTATION -> new String[]{packageName};
        };
    }

    public Path targetToCopyTo(Path projectPath, String packageName) {
        return FileTools.resolveOnTests(projectPath, packageName, switch (this) {
            case ASPECTJ -> new String[]{"aspectconfiguration", "java", "aspectj", "JavaAspectJConfigurationSettings.java"};
            case INSTRUMENTATION -> new String[]{"aspectconfiguration", "java", "instrumentation", "JavaInstrumentationConfigurationSettings.java"};
        });
    }


    public void reset() {
        switch (this) {
            case ASPECTJ -> JavaAspectJConfigurationSettings.reset();
            case INSTRUMENTATION -> JavaInstrumentationConfigurationSettings.reset();
        }
    }
}