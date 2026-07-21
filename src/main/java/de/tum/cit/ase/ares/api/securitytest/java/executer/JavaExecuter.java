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
	 * Executes the generated security test cases.
	 *
	 * @since 2.0.0
	 * @author Markus Paulsen
	 * @param buildMode                 the Java build mode to use; must not be null
	 * @param architectureMode          the Java architecture mode to use; must not
	 *                                  be null
	 * @param aopMode                   the Java AOP mode to use; must not be null
	 * @param essentialPackages         the list of essential packages; must not be
	 *                                  null
	 * @param essentialClasses          the list of essential classes; must not be
	 *                                  null
	 * @param testClasses               the list of test classes; must not be null
	 * @param packageName               the name of the package containing the main
	 *                                  class; must not be null
	 * @param mainClassInPackageName    the name of the main class; must not be null
	 * @param javaArchitectureTestCases the list of architecture test cases; must
	 *                                  not be null
	 * @param javaAOPTestCases          the list of AOP test cases; must not be null
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
		// Install allow-lists and descriptive settings before restrictedPackage. The
		// latter activates call-stack enforcement, so publishing it last prevents an
		// instrumented class-loading operation from observing a partially armed policy.
		setJavaAdviceSettingValue("allowedListedPackages", essentialPackages.toArray(String[]::new), architectureMode,
				aopMode);
		setJavaAdviceSettingValue("allowedListedClasses",
				Stream.concat(essentialClasses.stream(), testClasses.stream()).toArray(String[]::new), architectureMode,
				aopMode);
		setJavaAdviceSettingValue("buildMode", buildModeString, architectureMode, aopMode);
		setJavaAdviceSettingValue("architectureMode", architectureModeString, architectureMode, aopMode);
		setJavaAdviceSettingValue("aopMode", aopModeString, architectureMode, aopMode);
		setJavaAdviceSettingValue("mainClass", mainClassInPackageName, architectureMode, aopMode);
		// These calls prepare enforcement: architecture cases statically inspect the
		// bytecode and AOP cases publish the remaining domain allow-lists. Neither call
		// executes supervised code. Keep runtime interception disarmed until both have
		// completed, otherwise the analyser can block its own resource loading while
		// the
		// policy is still only partially installed.
		javaArchitectureTestCases.forEach(javaArchitectureTestCase -> javaArchitectureTestCase
				.executeArchitectureTestCase(architectureModeString, aopModeString));
		javaAOPTestCases
				.forEach(javaTestCase -> javaTestCase.executeAOPTestCase(architectureModeString, aopModeString));
		setJavaAdviceSettingValue("restrictedPackage", packageName, architectureMode, aopMode);
		if (aopMode == AOPMode.INSTRUMENTATION) {
			// Object's final monitor methods require application call-site rewriting.
			// Activate that transformer only after the complete policy is installed, so
			// retransformed classes can never observe a partially configured policy.
			JavaInstrumentationAgent.registerThreadMonitorRestrictedPackage(packageName);
		}
	}
	// </editor-fold>
}
