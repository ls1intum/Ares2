package de.tum.cit.ase.ares.api.aop.java;

import de.tum.cit.ase.ares.api.util.FileTools;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
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
                    new String[]{"aop", "java", "aspectj", "adviceandpointcut", "JavaAspectJFileSystemAdviceDefinitions.aj"},
                    new String[]{"aop", "java", "aspectj", "adviceandpointcut", "JavaAspectJFileSystemPointcutDefinitions.aj"}
            );
            case INSTRUMENTATION -> Stream.of(
                    new String[]{"aop", "java", "instrumentation", "advice", "JavaInstrumentationAdviceToolbox.java"},
                    new String[]{"aop", "java", "instrumentation", "advice", "JavaInstrumentationReadPathAdvice.java"},
                    new String[]{"aop", "java", "instrumentation", "advice", "JavaInstrumentationOverwritePathAdvice.java"},
                    new String[]{"aop", "java", "instrumentation", "advice", "JavaInstrumentationExecutePathAdvice.java"},
                    new String[]{"aop", "java", "instrumentation", "pointcut", "JavaInstrumentationPointcutDefinitions.java"},
                    new String[]{"aop", "java", "instrumentation", "pointcut", "JavaInstrumentationBindingDefinitions.java"},
                    new String[]{"aop", "java", "instrumentation", "JavaInstrumentationAgent.java"}
            );
        }).map(pathParticles -> FileTools.resolveOnTests(projectPath, packageName, pathParticles)).toList();
    }

    public Path threePartedFileHeader() {
        return FileTools.resolveOnResources("templates", "java", "aop", "JavaSecurityTestCaseSettingsHeader.txt");
    }

    public String threePartedFileBody(
            String aomMode,
            String restrictedPackage,
            List<String> allowedListedClasses,
            List<JavaSecurityTestCase> javaSecurityTestCases
    ) {
        return JavaSecurityTestCase.writeAOPSecurityTestCaseFile(aomMode, restrictedPackage, allowedListedClasses, javaSecurityTestCases);
    }

    public Path threePartedFileFooter() {
        return FileTools.resolveOnResources("templates", "java", "aop", "JavaSecurityTestCaseSettingsFooter.txt");
    }

    public String[] fileValue(String packageName) {
        return new String[]{packageName};
    }

    public Path targetToCopyTo(Path projectPath, String packageName) {
        return FileTools.resolveOnTests(projectPath, packageName, "aop", "java", "JavaSecurityTestCaseSettings.java");
    }


    public void reset() {
        try {
            Class<?> settingsClass = Class.forName("de.tum.cit.ase.ares.api.aop.java.JavaSecurityTestCaseSettings", true, null);
            Method method = settingsClass.getDeclaredMethod("reset");
            method.invoke(null);

        } catch (ClassNotFoundException e) {
            throw new SecurityException("Security configuration error: The class for the specific security test case settings could not be found. Ensure the class name is correct and the class is available at runtime.", e);

        } catch (NoSuchMethodException e) {
            throw new SecurityException("Security configuration error: The 'reset' method could not be found in the specified class. Ensure the method exists and is correctly named.", e);

        } catch (IllegalAccessException e) {
            throw new SecurityException("Security configuration error: Access to the 'reset' method was denied. Ensure the method is public and accessible.", e);

        } catch (InvocationTargetException e) {
            throw new SecurityException("Security configuration error: An error occurred while invoking the 'reset' method. This could be due to an underlying issue within the method implementation.", e);
        }
    }
}