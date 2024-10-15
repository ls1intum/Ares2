package de.tum.cit.ase.ares.api.aop.java;

import de.tum.cit.ase.ares.api.util.FileTools;

import javax.annotation.Nonnull;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

import static de.tum.cit.ase.ares.api.aop.java.instrumentation.advice.JavaInstrumentationAdviceToolbox.localize;

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

    //<editor-fold desc="Multi-file methods">
    /**
     * Retrieves a list of file paths to copy for the specific AOP mode.
     *
     * @return a list of file paths for the selected AOP mode (AspectJ or Instrumentation).
     */
    @Nonnull
    public List<Path> filesToCopy() {
        return (switch (this) {
            case ASPECTJ -> Stream.of(
                    new String[]{"templates", "aop", "java", "aspectj", "adviceandpointcut", "JavaAspectJFileSystemAdviceDefinitions.aj"},
                    new String[]{"templates", "aop", "java", "aspectj", "adviceandpointcut", "JavaAspectJFileSystemPointcutDefinitions.aj"}
            );
            case INSTRUMENTATION -> Stream.of(
                    new String[]{"templates", "aop", "java", "instrumentation", "advice", "JavaInstrumentationAdviceToolbox.java"},
                    new String[]{"templates", "aop", "java", "instrumentation", "advice", "JavaInstrumentationDeletePathMethodAdvice.java"},
                    new String[]{"templates", "aop", "java", "instrumentation", "advice", "JavaInstrumentationExecutePathMethodAdvice.java"},
                    new String[]{"templates", "aop", "java", "instrumentation", "advice", "JavaInstrumentationOverwritePathMethodAdvice.java"},
                    new String[]{"templates", "aop", "java", "instrumentation", "advice", "JavaInstrumentationReadPathMethodAdvice.java"},
                    new String[]{"templates", "aop", "java", "instrumentation", "advice", "JavaInstrumentationDeletePathConstructorAdvice.java"},
                    new String[]{"templates", "aop", "java", "instrumentation", "advice", "JavaInstrumentationExecutePathConstructorAdvice.java"},
                    new String[]{"templates", "aop", "java", "instrumentation", "advice", "JavaInstrumentationOverwritePathConstructorAdvice.java"},
                    new String[]{"templates", "aop", "java", "instrumentation", "advice", "JavaInstrumentationReadPathConstructorAdvice.java"},
                    new String[]{"templates", "aop", "java", "instrumentation", "pointcut", "JavaInstrumentationBindingDefinitions.java"},
                    new String[]{"templates", "aop", "java", "instrumentation", "pointcut", "JavaInstrumentationPointcutDefinitions.java"},
                    new String[]{"templates", "aop", "java", "instrumentation", "JavaInstrumentationAgent.java"},
                    new String[]{"templates", "META-INF", "MANIFEST.MF"}
            );
        }).map(FileTools::resolveOnResources).toList();
    }

    /**
     * Retrieves the values for file templates to copy based on the AOP mode.
     *
     * @param packageName            the base package name for the Java files.
     * @param mainClassInPackageName the main class inside the package.
     * @return a list of arrays representing the file values to be copied.
     */
    @Nonnull
    public List<String[]> fileValues(@Nonnull String packageName, @Nonnull String mainClassInPackageName) {
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
                            FileTools.generatePackageNameArray(packageName, 1),
                            FileTools.generatePackageNameArray(packageName, 1),
                            FileTools.generatePackageNameArray(packageName, 1),
                            FileTools.generatePackageNameArray(packageName, 1),
                            FileTools.generatePackageNameArray(packageName, 11),
                            FileTools.generatePackageNameArray(packageName, 1),
                            FileTools.generatePackageNameArray(packageName, 3),
                            new String[]{packageName, packageName, mainClassInPackageName}
                    )
                    .toList();
        };
    }

    /**
     * Determines the target locations where the files should be copied for the selected AOP mode.
     *
     * @param projectPath the path to the project where files should be copied.
     * @param packageName the base package name.
     * @return a list of paths representing the copy targets.
     */
    @Nonnull
    public List<Path> targetsToCopyTo(@Nonnull Path projectPath, @Nonnull String packageName) {
        return (switch (this) {
            case ASPECTJ -> Stream.of(
                    new String[]{"aop", "java", "aspectj", "adviceandpointcut", "JavaAspectJFileSystemAdviceDefinitions.aj"},
                    new String[]{"aop", "java", "aspectj", "adviceandpointcut", "JavaAspectJFileSystemPointcutDefinitions.aj"}
            );
            case INSTRUMENTATION -> Stream.of(
                    new String[]{"api", "aop", "java", "instrumentation", "advice", "JavaInstrumentationAdviceToolbox.java"},
                    new String[]{"api", "aop", "java", "instrumentation", "advice", "JavaInstrumentationDeletePathMethodAdvice.java"},
                    new String[]{"api", "aop", "java", "instrumentation", "advice", "JavaInstrumentationExecutePathMethodAdvice.java"},
                    new String[]{"api", "aop", "java", "instrumentation", "advice", "JavaInstrumentationOverwritePathMethodAdvice.java"},
                    new String[]{"api", "aop", "java", "instrumentation", "advice", "JavaInstrumentationReadPathMethodAdvice.java"},
                    new String[]{"api", "aop", "java", "instrumentation", "advice", "JavaInstrumentationDeletePathConstructorAdvice.java"},
                    new String[]{"api", "aop", "java", "instrumentation", "advice", "JavaInstrumentationExecutePathConstructorAdvice.java"},
                    new String[]{"api", "aop", "java", "instrumentation", "advice", "JavaInstrumentationOverwritePathConstructorAdvice.java"},
                    new String[]{"api", "aop", "java", "instrumentation", "advice", "JavaInstrumentationReadPathConstructorAdvice.java"},
                    new String[]{"api", "aop", "java", "instrumentation", "pointcut", "JavaInstrumentationBindingDefinitions.java"},
                    new String[]{"api", "aop", "java", "instrumentation", "pointcut", "JavaInstrumentationPointcutDefinitions.java"},
                    new String[]{"api", "aop", "java", "instrumentation", "JavaInstrumentationAgent.java"},
                    new String[]{"META-INF", "MANIFEST.MF"}
            );
        }).map(pathParticles -> FileTools.resolveOnTests(projectPath, packageName, pathParticles)).toList();
    }
    //</editor-fold>

    //<editor-fold desc="Single-file methods">
    /**
     * Retrieves the file header for the three-parted security test case file.
     *
     * @return the path to the file header template.
     */
    @Nonnull
    public Path threePartedFileHeader() {
        return FileTools.resolveOnResources("templates", "aop", "java", "JavaSecurityTestCaseSettingsHeader.txt");
    }

    /**
     * Generates the body for the three-parted security test case file based on the provided mode and classes.
     *
     * @param aopMode               the AOP mode (AspectJ or Instrumentation).
     * @param restrictedPackage     the package being restricted by the security test cases.
     * @param allowedListedClasses  the list of allowed classes in the restricted package.
     * @param javaSecurityTestCases the list of security test cases.
     * @return the body of the three-parted security test case file.
     */
    @Nonnull
    public String threePartedFileBody(
            @Nonnull String aopMode,
            @Nonnull String restrictedPackage,
            @Nonnull List<String> allowedListedClasses,
            @Nonnull List<JavaSecurityTestCase> javaSecurityTestCases
    ) {
        return JavaSecurityTestCase.writeAOPSecurityTestCaseFile(aopMode, restrictedPackage, allowedListedClasses, javaSecurityTestCases);
    }

    /**
     * Retrieves the file footer for the three-parted security test case file.
     *
     * @return the path to the file footer template.
     */
    @Nonnull
    public Path threePartedFileFooter() {
        return FileTools.resolveOnResources("templates", "aop", "java", "JavaSecurityTestCaseSettingsFooter.txt");
    }

    /**
     * Generates the file value array based on the package name.
     *
     * @param packageName the base package name.
     * @return an array representing the file value.
     */
    @Nonnull
    public String[] fileValue(@Nonnull String packageName) {
        return FileTools.generatePackageNameArray(packageName, 1);
    }

    /**
     * Determines the target path for the main configuration file.
     *
     * @param projectPath the path to the project.
     * @param packageName the base package name.
     * @return the target path for the main configuration file.
     */
    @Nonnull
    public Path targetToCopyTo(@Nonnull Path projectPath, @Nonnull String packageName) {
        return FileTools.resolveOnTests(projectPath, packageName, "api", "aop", "java", "JavaSecurityTestCaseSettings.java");
    }
    //</editor-fold>

    //<editor-fold desc="Reset methods">
    /**
     * Resets the security test case settings.
     *
     * <p>This method invokes the reset method in the settings class using reflection.</p>
     */
    public void reset() {
        try {
            ClassLoader customClassLoader = Thread.currentThread().getContextClassLoader();
            Class<?> settingsClass = Class.forName("de.tum.cit.ase.ares.api.aop.java.JavaSecurityTestCaseSettings", true, customClassLoader);
            Method method = settingsClass.getDeclaredMethod("reset");
            method.setAccessible(true);
            method.invoke(null);
            method.setAccessible(false);

        } catch (ClassNotFoundException e) {
            throw new SecurityException(localize("security.creation.reset.class.not.found.exception"), e);

        } catch (NoSuchMethodException e) {
            throw new SecurityException(localize("security.creation.reset.no.method.exception"), e);

        } catch (IllegalAccessException e) {
            throw new SecurityException(localize("security.creation.reset.illegal.access.exception"), e);

        } catch (InvocationTargetException e) {
            throw new SecurityException(localize("security.creation.reset.invocation.target.exception"), e);
        }
    }
    //</editor-fold>
}