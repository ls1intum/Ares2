package de.tum.cit.ase.ares.api.securitytest.java.executer;

import java.util.List;
import java.util.stream.Stream;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import de.tum.cit.ase.ares.api.aop.AOPMode;
import de.tum.cit.ase.ares.api.aop.java.JavaAOPTestCase;
import de.tum.cit.ase.ares.api.aop.java.instrumentation.JavaInstrumentationAgent;
import de.tum.cit.ase.ares.api.architecture.ArchitectureMode;
import de.tum.cit.ase.ares.api.architecture.java.JavaArchitectureTestCase;
import de.tum.cit.ase.ares.api.buildtoolconfiguration.BuildMode;

/**
 * Implementation for executing Java security test cases.
 * <p>
 * Description: This class executes security test cases for Java programming
 * language, setting up necessary test configurations and executing architecture
 * and AOP test cases.
 * <p>
 * Design Rationale: Implements the Executer interface for the Java programming
 * language, following the Strategy design pattern.
 *
 * @since 2.0.0
 * @author Markus Paulsen
 * @version 2.0.0
 */
public class JavaExecuter implements Executer {

	// <editor-fold desc="Helper methods">

	/**
	 * Sets the value of a Java advice setting.
	 *
	 * @since 2.0.0
	 * @author Markus Paulsen
	 * @param key              the key of the setting to set; must not be null
	 * @param value            the value to set for the setting; may be null
	 * @param architectureMode the Java architecture mode; must not be null
	 * @param aopMode          the Java AOP mode; must not be null
	 */
	private void setJavaAdviceSettingValue(@Nonnull String key, @Nullable Object value,
			@Nonnull ArchitectureMode architectureMode, @Nonnull AOPMode aopMode) {
		JavaAOPTestCase.setJavaAdviceSettingValue(key, value, architectureMode.toString(), aopMode.toString());
	}
	// </editor-fold>

	// <editor-fold desc="Execute test cases methods">

	/**
	 * Executes the generated security test cases: the architecture checks run and
	 * the allowances are published first, and the package that switches on the
	 * call-stack checks comes last, so nothing observes a half-armed policy. Only
	 * then does the thread that ended the last test lose its exemption. Nothing
	 * here runs supervised code.
	 *
	 * @since 2.0.0
	 * @author Markus Paulsen
	 * @param buildMode                 build mode
	 * @param architectureMode          architecture mode
	 * @param aopMode                   AOP mode
	 * @param essentialPackages         essential packages
	 * @param essentialClasses          essential classes
	 * @param testClasses               test classes
	 * @param packageName               main class's package
	 * @param mainClassInPackageName    main class's name
	 * @param javaArchitectureTestCases architecture test cases
	 * @param javaAOPTestCases          AOP test cases
	 */
	@Override
	public void executeTestCases(@Nonnull BuildMode buildMode, @Nonnull ArchitectureMode architectureMode,
			@Nonnull AOPMode aopMode, @Nonnull List<String> essentialPackages, @Nonnull List<String> essentialClasses,
			@Nonnull List<String> testClasses, @Nonnull String packageName, @Nonnull String mainClassInPackageName,
			@Nonnull List<JavaArchitectureTestCase> javaArchitectureTestCases,
			@Nonnull List<JavaAOPTestCase> javaAOPTestCases) {
		@Nonnull
		String buildModeString = buildMode.toString();
		@Nonnull
		String architectureModeString = architectureMode.toString();
		@Nonnull
		String aopModeString = aopMode.toString();
		setJavaAdviceSettingValue("allowedListedPackages", essentialPackages.toArray(String[]::new), architectureMode,
				aopMode);
		setJavaAdviceSettingValue("allowedListedClasses",
				Stream.concat(essentialClasses.stream(), testClasses.stream()).distinct().toArray(String[]::new),
				architectureMode, aopMode);
		setJavaAdviceSettingValue("buildMode", buildModeString, architectureMode, aopMode);
		setJavaAdviceSettingValue("architectureMode", architectureModeString, architectureMode, aopMode);
		setJavaAdviceSettingValue("aopMode", aopModeString, architectureMode, aopMode);
		setJavaAdviceSettingValue("mainClass", mainClassInPackageName, architectureMode, aopMode);
		javaArchitectureTestCases.forEach(javaArchitectureTestCase -> javaArchitectureTestCase
				.executeArchitectureTestCase(architectureModeString, aopModeString));
		javaAOPTestCases
				.forEach(javaTestCase -> javaTestCase.executeAOPTestCase(architectureModeString, aopModeString));
		if (aopMode == AOPMode.INSTRUMENTATION) {
			JavaInstrumentationAgent.activate();
		}
		setJavaAdviceSettingValue("restrictedPackage", packageName, architectureMode, aopMode);
		setJavaAdviceSettingValue("supervisionEndedOnThread", null, architectureMode, aopMode);
		if (aopMode == AOPMode.INSTRUMENTATION) {
			JavaInstrumentationAgent.registerThreadMonitorRestrictedPackage(packageName);
		}
	}
	// </editor-fold>
}
