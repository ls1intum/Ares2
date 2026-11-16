package de.tum.cit.ase.ares.api.aop.java.instrumentation;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import org.junit.jupiter.api.Test;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.lang.ArchRule;

import de.tum.cit.ase.ares.api.aop.java.JavaAOPTestCaseSettings;

/**
 * Checks that the package holding the classes the instrumentation agent puts on
 * the boot class loader, and the settings class, do not need AspectJ. That
 * loader sees AspectJ only when it is on the boot class path, so a class Ares's
 * own build wove with an aspect stops the agent from starting.
 */
class JavaInstrumentationBootClassLoaderAspectJTest {

	/**
	 * Package holding the classes the agent loads on the boot class loader.
	 */
	private static final String BOOT_LOADED_PACKAGE = "de.tum.cit.ase.ares.api.aop.java.instrumentation.advice";

	/** Packages of AspectJ, which the boot class loader does not see by default. */
	private static final String ASPECTJ_PACKAGES = "org.aspectj..";

	/** Rule that no checked class refers to any AspectJ class. */
	private static final ArchRule NO_ASPECTJ_DEPENDENCY = noClasses().should().dependOnClassesThat()
			.resideInAPackage(ASPECTJ_PACKAGES)
			.because("it holds code loaded on the boot class loader, which sees AspectJ only on the boot class path");

	/**
	 * Checks every production class of the package holding the boot-loaded classes,
	 * the call sites included, so a call site woven by mistake fails here.
	 */
	@Test
	void bootLoadedAdviceClassesDoNotDependOnAspectJ() {
		JavaClasses adviceClasses = new ClassFileImporter()
				.withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS).importPackages(BOOT_LOADED_PACKAGE);
		NO_ASPECTJ_DEPENDENCY.check(adviceClasses);
	}

	/**
	 * Checks the settings class, the one boot-loaded class that lives outside the
	 * advice package.
	 */
	@Test
	void bootLoadedSettingsClassDoesNotDependOnAspectJ() {
		NO_ASPECTJ_DEPENDENCY.check(new ClassFileImporter().importClasses(JavaAOPTestCaseSettings.class));
	}
}
