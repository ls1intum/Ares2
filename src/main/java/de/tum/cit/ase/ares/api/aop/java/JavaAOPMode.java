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
                    new String[]{"templates", "aop", "java", "aspectj", "adviceandpointcut", "JavaAspectJFileSystemAdviceDefinitions.aj"},
                    new String[]{"templates", "aop", "java", "aspectj", "adviceandpointcut", "JavaAspectJFileSystemPointcutDefinitions.aj"}
            );
            case INSTRUMENTATION -> Stream.of(
                    new String[]{"templates", "aop", "java", "instrumentation", "advice", "JavaInstrumentationAdviceToolbox.java"},
                    new String[]{"templates", "aop", "java", "instrumentation", "advice", "JavaInstrumentationDeletePathAdvice.java"},
                    new String[]{"templates", "aop", "java", "instrumentation", "advice", "JavaInstrumentationExecutePathAdvice.java"},
                    new String[]{"templates", "aop", "java", "instrumentation", "advice", "JavaInstrumentationOverwritePathAdvice.java"},
                    new String[]{"templates", "aop", "java", "instrumentation", "advice", "JavaInstrumentationReadPathAdvice.java"},
                    new String[]{"templates", "aop", "java", "instrumentation", "pointcut", "JavaInstrumentationBindingDefinitions.java"},
                    new String[]{"templates", "aop", "java", "instrumentation", "pointcut", "JavaInstrumentationPointcutDefinitions.java"},
                    new String[]{"templates", "aop", "java", "instrumentation", "JavaInstrumentationAgent.java"},
                    new String[]{"templates", "META-INF", "MANIFEST.MF"}
            );
        }).map(FileTools::resolveOnResources).toList();
    }

    public List<String[]> fileValues(String packageName, String mainClassInPackageName) {
        return switch (this) {
            case ASPECTJ -> Stream.of(
                            FileTools.generatePackageNameArray(packageName, 28),
                            FileTools.generatePackageNameArray(packageName, 1))
                    .toList();
            case INSTRUMENTATION -> Stream.of(
                            FileTools.generatePackageNameArray(packageName, 2),
                            FileTools.generatePackageNameArray(packageName, 1),
                            FileTools.generatePackageNameArray(packageName, 1),
                            FileTools.generatePackageNameArray(packageName, 1),
                            FileTools.generatePackageNameArray(packageName, 1),
                            FileTools.generatePackageNameArray(packageName, 8),
                            FileTools.generatePackageNameArray(packageName, 1),
                            FileTools.generatePackageNameArray(packageName, 3),
                            new String[]{packageName, packageName, mainClassInPackageName}
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
                    new String[]{"aop", "java", "instrumentation", "advice", "JavaInstrumentationDeletePathAdvice.java"},
                    new String[]{"aop", "java", "instrumentation", "advice", "JavaInstrumentationExecutePathAdvice.java"},
                    new String[]{"aop", "java", "instrumentation", "advice", "JavaInstrumentationOverwritePathAdvice.java"},
                    new String[]{"aop", "java", "instrumentation", "advice", "JavaInstrumentationReadPathAdvice.java"},
                    new String[]{"aop", "java", "instrumentation", "pointcut", "JavaInstrumentationBindingDefinitions.java"},
                    new String[]{"aop", "java", "instrumentation", "pointcut", "JavaInstrumentationPointcutDefinitions.java"},
                    new String[]{"aop", "java", "instrumentation", "JavaInstrumentationAgent.java"},
                    new String[]{"META-INF", "MANIFEST.MF"}
            );
        }).map(pathParticles -> FileTools.resolveOnTests(projectPath, packageName, pathParticles)).toList();
    }

    public Path threePartedFileHeader() {
        return FileTools.resolveOnResources("templates", "aop", "java", "JavaSecurityTestCaseSettingsHeader.txt");
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
        return FileTools.resolveOnResources("templates", "aop", "java", "JavaSecurityTestCaseSettingsFooter.txt");
    }

    public String[] fileValue(String packageName) {
        return FileTools.generatePackageNameArray(packageName, 1);
    }

    public Path targetToCopyTo(Path projectPath, String packageName) {
        return FileTools.resolveOnTests(projectPath, packageName, "aop", "java", "JavaSecurityTestCaseSettings.java");
    }


    public void reset() {
        try {
            ClassLoader customClassLoader = Thread.currentThread().getContextClassLoader();
            Class<?> settingsClass = Class.forName("de.tum.cit.ase.ares.api.aop.java.JavaSecurityTestCaseSettings", true, customClassLoader);
            Method method = settingsClass.getDeclaredMethod("reset");
            method.setAccessible(true);
            method.invoke(null);
            method.setAccessible(false);

        } catch (ClassNotFoundException e) {
            throw new SecurityException("Ares Security Error (Reason: Ares-Code; Stage: Creation): The class for the specific security test case settings could not be found. Ensure the class name is correct and the class is available at runtime.", e);

        } catch (NoSuchMethodException e) {
            throw new SecurityException("Ares Security Error (Reason: Ares-Code; Stage: Creation): The 'reset' method could not be found in the specified class. Ensure the method exists and is correctly named.", e);

        } catch (IllegalAccessException e) {
            throw new SecurityException("Ares Security Error (Reason: Ares-Code; Stage: Creation): Access to the 'reset' method was denied. Ensure the method is public and accessible.", e);

        } catch (InvocationTargetException e) {
            throw new SecurityException("Ares Security Error (Reason: Ares-Code; Stage: Creation): An error occurred while invoking the 'reset' method. This could be due to an underlying issue within the method implementation.", e);
        }
    }
}