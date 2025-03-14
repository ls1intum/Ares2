package de.tum.cit.ase.ares.api.securitytest.java.executer;

import de.tum.cit.ase.ares.api.aop.java.JavaAOPMode;
import de.tum.cit.ase.ares.api.aop.java.JavaAOPTestCase;
import de.tum.cit.ase.ares.api.architecture.java.JavaArchitectureMode;
import de.tum.cit.ase.ares.api.architecture.java.JavaArchitectureTestCase;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public class JavaExecuter implements Executer {

    //<editor-fold desc="Helper methods">
    /**
     * Sets the value of a Java advice setting.
     * <p>
     * This method sets the value of a Java advice setting based on the provided key and value.
     * </p>
     *
     * @param key
     *          the key of the setting to set; must not be null.
     * @param value
     *          the value to set for the setting; may be null.
     */
    private void setJavaAdviceSettingValue(String key, Object value, JavaArchitectureMode javaArchitectureMode, JavaAOPMode javaAOPMode) {
        JavaAOPTestCase.setJavaAdviceSettingValue(key, value, javaArchitectureMode.toString(), javaAOPMode.toString());
    }
    //</editor-fold>

    //<editor-fold desc="Execute test cases methods">
    /**
     * Executes the generated security test cases.
     * <p>
     * This method sets up the necessary test configurations and then sequentially executes the architecture
     * and AOP test cases.
     * </p>
     */
    @Override
    public void executeSecurityTestCases(
            @Nonnull JavaArchitectureMode javaArchitectureMode,
            @Nonnull JavaAOPMode javaAOPMode,
            @Nonnull String packageName,
            @Nonnull String mainClassInPackageName,
            @Nonnull String[] testClasses,
            @Nonnull List<String> essentialClasses,
            @Nonnull List<JavaArchitectureTestCase> javaArchitectureTestCases,
            @Nonnull List<JavaAOPTestCase> javaAOPTestCases
    ) {
        String javaArchitectureModeString = javaArchitectureMode.toString();
        String javaAOPModeString = javaAOPMode.toString();
        Map.ofEntries(
                Map.entry("architectureMode", javaArchitectureModeString),
                Map.entry("aopMode", javaAOPModeString),
                Map.entry("restrictedPackage", packageName),
                Map.entry("allowedListedClasses", Stream.concat(Arrays.stream(testClasses), essentialClasses.stream()).toArray(String[]::new))
        ).forEach((key, value) -> setJavaAdviceSettingValue(key, value, javaArchitectureMode, javaAOPMode));
        javaArchitectureTestCases.forEach(javaArchitectureTestCase -> javaArchitectureTestCase.executeArchitectureTestCase(javaArchitectureModeString, javaAOPModeString));
        javaAOPTestCases.forEach(javaSecurityTestCase -> javaSecurityTestCase.executeAOPSecurityTestCase(javaArchitectureModeString, javaAOPModeString));
    }
    //</editor-fold>

}
