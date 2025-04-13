package de.tum.cit.ase.ares.api.securitytest.java.executer;

import de.tum.cit.ase.ares.api.aop.java.JavaAOPMode;
import de.tum.cit.ase.ares.api.aop.java.JavaAOPTestCase;
import de.tum.cit.ase.ares.api.architecture.java.JavaArchitectureMode;
import de.tum.cit.ase.ares.api.architecture.java.JavaArchitectureTestCase;
import de.tum.cit.ase.ares.api.buildtoolconfiguration.java.JavaBuildMode;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

/**
 * Implementation for executing Java security test cases.
 *
 * <p>Description: This class executes security test cases for Java programming language,
 * setting up necessary test configurations and executing architecture and AOP test cases.
 *
 * <p>Design Rationale: Implements the Executer interface for the Java programming language,
 * following the Strategy design pattern.
 *
 * @since 2.0.0
 * @author Markus Paulsen
 * @version 2.0.0
 */
public class JavaExecuter implements Executer {

    //<editor-fold desc="Helper methods">

    /**
     * Sets the value of a Java advice setting.
     *
     * @since 2.0.0
     * @author Markus Paulsen
     * @param key the key of the setting to set; must not be null
     * @param value the value to set for the setting; may be null
     * @param javaArchitectureMode the Java architecture mode; must not be null
     * @param javaAOPMode the Java AOP mode; must not be null
     */
    private void setJavaAdviceSettingValue(
            @Nonnull String key,
            @Nullable Object value,
            @Nonnull JavaArchitectureMode javaArchitectureMode,
            @Nonnull JavaAOPMode javaAOPMode
    ) {
        JavaAOPTestCase.setJavaAdviceSettingValue(key, value, javaArchitectureMode.toString(), javaAOPMode.toString());
    }
    //</editor-fold>

    //<editor-fold desc="Execute test cases methods">

    /**
     * Executes the generated security test cases.
     *
     * @since 2.0.0
     * @author Markus Paulsen
     * @param javaBuildMode the Java build mode to use; must not be null
     * @param javaArchitectureMode the Java architecture mode to use; must not be null
     * @param javaAOPMode the Java AOP mode to use; must not be null
     * @param essentialPackages the list of essential packages; must not be null
     * @param essentialClasses the list of essential classes; must not be null
     * @param testClasses the list of test classes; must not be null
     * @param packageName the name of the package containing the main class; must not be null
     * @param mainClassInPackageName the name of the main class; must not be null
     * @param javaArchitectureTestCases the list of architecture test cases; must not be null
     * @param javaAOPTestCases the list of AOP test cases; must not be null
     */
    @Override
    public void executeSecurityTestCases(
            @Nonnull JavaBuildMode javaBuildMode,
            @Nonnull JavaArchitectureMode javaArchitectureMode,
            @Nonnull JavaAOPMode javaAOPMode,
            @Nonnull List<String> essentialPackages,
            @Nonnull List<String> essentialClasses,
            @Nonnull List<String> testClasses,
            @Nonnull String packageName,
            @Nonnull String mainClassInPackageName,
            @Nonnull List<JavaArchitectureTestCase> javaArchitectureTestCases,
            @Nonnull List<JavaAOPTestCase> javaAOPTestCases
    ) {
        @Nonnull String javaBuildModeString = javaBuildMode.toString();
        @Nonnull String javaArchitectureModeString = javaArchitectureMode.toString();
        @Nonnull String javaAOPModeString = javaAOPMode.toString();
        Map.ofEntries(
                Map.entry("buildMode", javaBuildModeString),
                Map.entry("architectureMode", javaArchitectureModeString),
                Map.entry("aopMode", javaAOPModeString),
                Map.entry("allowedListedPackages", essentialPackages.toArray(String[]::new)),
                Map.entry("allowedListedClasses", Stream.concat(essentialClasses.stream(), testClasses.stream()).toArray(String[]::new)),
                Map.entry("restrictedPackage", packageName),
                Map.entry("mainClass", mainClassInPackageName)
        ).forEach((key, value) -> setJavaAdviceSettingValue(key, value, javaArchitectureMode, javaAOPMode));
        javaArchitectureTestCases.forEach(javaArchitectureTestCase -> javaArchitectureTestCase.executeArchitectureTestCase(javaArchitectureModeString, javaAOPModeString));
        javaAOPTestCases.forEach(javaSecurityTestCase -> javaSecurityTestCase.executeAOPSecurityTestCase(javaArchitectureModeString, javaAOPModeString));
    }
    //</editor-fold>

}
