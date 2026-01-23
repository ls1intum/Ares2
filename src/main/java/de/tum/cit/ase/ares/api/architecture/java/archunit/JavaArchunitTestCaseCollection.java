package de.tum.cit.ase.ares.api.architecture.java.archunit;

//<editor-fold desc="Imports">
import java.nio.file.Path;
import java.util.Set;

import com.tngtech.archunit.base.DescribedPredicate;
import com.tngtech.archunit.core.domain.JavaAccess;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.lang.syntax.ArchRuleDefinition;

import de.tum.cit.ase.ares.api.architecture.java.FileHandlerConstants;
import de.tum.cit.ase.ares.api.localization.Messages;
import de.tum.cit.ase.ares.api.policy.policySubComponents.PackagePermission;
import de.tum.cit.ase.ares.api.util.FileTools;
//</editor-fold>

/**
 * Collection of security test cases that analyze Java applications using
 * Archunit framework. This class provides static methods to verify that
 * analyzed code does not: - Use reflection - Access file system - Access
 * network - Terminate JVM - Execute system commands - Create threads
 */
public class JavaArchunitTestCaseCollection {

	// <editor-fold desc="Constructor">
	private JavaArchunitTestCaseCollection() {
		throw new SecurityException(Messages.localized("security.general.utility.initialization",
				JavaArchunitTestCaseCollection.class.getSimpleName()));
	}
	// </editor-fold>

	// <editor-fold desc="Tool methods">

	private static ArchRule createNoClassShouldHaveMethodRule(String ruleName, Path methodsFilePath) {
		return ArchRuleDefinition.noClasses()
				.should(new TransitivelyAccessesMethodsCondition(new DescribedPredicate<>(ruleName) {
					private Set<String> forbiddenMethods;

					@Override
					public boolean test(JavaAccess<?> javaAccess) {
						if (forbiddenMethods == null) {
							forbiddenMethods = FileTools.readMethodsFile(FileTools.readFile(methodsFilePath));
						}
						return forbiddenMethods.stream().filter(method -> !method.isEmpty())
								.anyMatch(method -> javaAccess.getTarget().getFullName().startsWith(method));
					}
				})).as(ruleName);
	}
	// </editor-fold>

	// <editor-fold desc="Dynamic rules">
	// <editor-fold desc="File System related rule">
	/**
	 * This method checks if any class in the given package accesses the file
	 * system.
	 */
	public static final ArchRule NO_CLASS_MUST_ACCESS_FILE_SYSTEM = createNoClassShouldHaveMethodRule(
			Messages.localized("security.architecture.file.system.access"),
			FileHandlerConstants.ARCHUNIT_FILESYSTEM_METHODS);
	// </editor-fold>

	// <editor-fold desc="Network Connections related rule">
	/**
	 * This method checks if any class in the given package accesses the network.
	 */
	public static final ArchRule NO_CLASS_MUST_ACCESS_NETWORK = createNoClassShouldHaveMethodRule(
			Messages.localized("security.architecture.network.access"), FileHandlerConstants.ARCHUNIT_NETWORK_METHODS);
	// </editor-fold>

	// <editor-fold desc="Thread Creation related rule">
	/**
	 * This method checks if any class in the given package creates threads.
	 */
	public static final ArchRule NO_CLASS_MUST_CREATE_THREADS = createNoClassShouldHaveMethodRule(
			Messages.localized("security.architecture.manipulate.threads"),
			FileHandlerConstants.ARCHUNIT_THREAD_MANIPULATION_METHODS);
	// </editor-fold>

	// <editor-fold desc="Command Execution related rule">
	/**
	 * This method checks if any class in the given package executes commands.
	 */
	public static final ArchRule NO_CLASS_MUST_EXECUTE_COMMANDS = createNoClassShouldHaveMethodRule(
			Messages.localized("security.architecture.execute.command"),
			FileHandlerConstants.ARCHUNIT_COMMAND_EXECUTION_METHODS);
	// </editor-fold>
	// </editor-fold>

	// <editor-fold desc="Semi-dynamic rules">
	// <editor-fold desc="Package Import related rule">
	/**
	 * This method checks if any class in the given package imports forbidden
	 * packages.
	 */
	public static ArchRule noClassMustImportForbiddenPackages(Set<PackagePermission> allowedPackages) {
		return ArchRuleDefinition.noClasses().should()
				.dependOnClassesThat(new DescribedPredicate<>("imports a forbidden package package") {
					@Override
					public boolean test(JavaClass javaClass) {
						return allowedPackages.stream().noneMatch(allowedPackage -> javaClass.getPackageName()
								.startsWith(allowedPackage.importTheFollowingPackage()));
					}
				}).as(Messages.localized("security.architecture.package.import"));
	}
	// </editor-fold>
	// </editor-fold>

	// <editor-fold desc="Static rules">
	// <editor-fold desc="Reflection related rule">
	/**
	 * This method checks if any class in the given package uses reflection.
	 */
	public static final ArchRule NO_CLASS_MUST_USE_REFLECTION = createNoClassShouldHaveMethodRule(
			Messages.localized("security.architecture.reflection.uses"),
			FileHandlerConstants.ARCHUNIT_REFLECTION_METHODS);
	// </editor-fold>

	// <editor-fold desc="Termination related rule">
	/**
	 * This method checks if any class in the given package uses the command line.
	 */
	public static final ArchRule NO_CLASS_MUST_TERMINATE_JVM = createNoClassShouldHaveMethodRule(
			Messages.localized("security.architecture.terminate.jvm"),
			FileHandlerConstants.ARCHUNIT_JVM_TERMINATION_METHODS);
	// </editor-fold>

	// <editor-fold desc="Serialization related rule">
	/**
	 * This method checks if any class in the given package uses serialization.
	 */
	public static final ArchRule NO_CLASS_MUST_SERIALIZE = createNoClassShouldHaveMethodRule(
			Messages.localized("security.architecture.serialize"), FileHandlerConstants.ARCHUNIT_SERIALIZATION_METHODS);
	// </editor-fold>

	// <editor-fold desc="Class Loading related rule">
	/**
	 * This method checks if any class in the given package uses class loaders.
	 */
	public static final ArchRule NO_CLASS_MUST_USE_CLASSLOADERS = createNoClassShouldHaveMethodRule(
			Messages.localized("security.architecture.class.loading"),
			FileHandlerConstants.ARCHUNIT_CLASSLOADER_METHODS);
	// </editor-fold>
	// </editor-fold>
}
