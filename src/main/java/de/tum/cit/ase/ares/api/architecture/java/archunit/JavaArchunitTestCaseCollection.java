package de.tum.cit.ase.ares.api.architecture.java.archunit;

//<editor-fold desc="Imports">
import java.nio.file.Path;
import java.util.Set;
import java.util.stream.Collectors;

import com.tngtech.archunit.base.DescribedPredicate;
import com.tngtech.archunit.core.domain.JavaAccess;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.lang.syntax.ArchRuleDefinition;

import de.tum.cit.ase.ares.api.architecture.java.FileHandlerConstants;
import de.tum.cit.ase.ares.api.architecture.java.JavaArchitectureTestCase;
import de.tum.cit.ase.ares.api.architecture.java.JavaArchitectureTestCaseSupported;
import de.tum.cit.ase.ares.api.localization.Messages;
import de.tum.cit.ase.ares.api.policy.policySubComponents.ClassPermission;
import de.tum.cit.ase.ares.api.policy.policySubComponents.PackagePermission;
import de.tum.cit.ase.ares.api.util.FileTools;
//</editor-fold>

/**
 * Collection of security test cases that analyse Java applications using
 * Archunit framework. This class provides static methods to verify that
 * analysed code does not: - Use reflection - Access file system - Access
 * network - Terminate JVM - Execute system commands - Create threads
 */
public final class JavaArchunitTestCaseCollection {

	// <editor-fold desc="Constructor">
	private JavaArchunitTestCaseCollection() {
		throw new SecurityException(Messages.localized("security.general.utility.initialization",
				JavaArchunitTestCaseCollection.class.getSimpleName()));
	}
	// </editor-fold>

	// <editor-fold desc="Tool methods">

	/**
	 * Converts Java-style array notation in method signatures to the JNI-style
	 * format that ArchUnit uses in {@code JavaAccess.getTarget().getFullName()}.
	 * <p>
	 * For example: {@code "java.lang.String[]"} becomes
	 * {@code "[Ljava.lang.String;"} and {@code "byte[]"} becomes {@code "[B"}.
	 *
	 * @param signature the method signature with Java-style array notation
	 * @return the signature with JNI-style array notation
	 */
	private static String convertArrayNotation(String signature) {
		// Handle primitive arrays first (before the general object array regex)
		String result = signature;
		result = result.replace("byte[]", "[B");
		result = result.replace("short[]", "[S");
		result = result.replace("int[]", "[I");
		result = result.replace("long[]", "[J");
		result = result.replace("float[]", "[F");
		result = result.replace("double[]", "[D");
		result = result.replace("boolean[]", "[Z");
		result = result.replace("char[]", "[C");
		// Handle object arrays: qualified.Type[] -> [Lqualified.Type;
		result = result.replaceAll("([a-zA-Z_$][a-zA-Z0-9_.$]*)\\[\\]", "[L$1;");
		return result;
	}

	private static ArchRule createNoClassShouldHaveMethodRule(String ruleName, Path methodsFilePath) {
		return createNoClassShouldHaveMethodRule(ruleName, methodsFilePath, Set.of());
	}

	/**
	 * Builds a "no class should call the forbidden methods" rule, exempting the
	 * allow-listed classes via {@code .that(...)}. With an empty allow-list the
	 * predicate matches every class, so the rule behaves exactly as the unfiltered
	 * variant (used by the static constants / generated-template path).
	 */
	private static ArchRule createNoClassShouldHaveMethodRule(String ruleName, Path methodsFilePath,
			Set<ClassPermission> allowedClasses) {
		return ArchRuleDefinition.noClasses().that(isNotAllowedClass(allowedClasses))
				.should(new TransitivelyAccessesMethodsCondition(new DescribedPredicate<>(ruleName) {
					private Set<String> forbiddenMethods;

					@Override
					public boolean test(JavaAccess<?> javaAccess) {
						if (forbiddenMethods == null) {
							forbiddenMethods = FileTools.readMethodsFile(FileTools.readFile(methodsFilePath)).stream()
									.map(JavaArchunitTestCaseCollection::convertArrayNotation)
									.collect(Collectors.toSet());
						}
						return forbiddenMethods.stream().filter(method -> !method.isEmpty())
								.anyMatch(method -> javaAccess.getTarget().getFullName().startsWith(method));
					}
				})).as(ruleName);
	}

	/**
	 * Predicate selecting classes that are NOT on the allow-list, so a rule built
	 * with {@code .that(...)} only applies to non-exempt classes. Boundary-aware
	 * nested-class matching is delegated to
	 * {@link JavaArchitectureTestCase#isAllowedClass}.
	 */
	private static DescribedPredicate<JavaClass> isNotAllowedClass(Set<ClassPermission> allowedClasses) {
		return new DescribedPredicate<>("not an allow-listed class") {
			@Override
			public boolean test(JavaClass javaClass) {
				return !JavaArchitectureTestCase.isAllowedClass(javaClass.getFullName(), allowedClasses);
			}
		};
	}

	/**
	 * Returns the architecture rule for the given category, exempting the
	 * allow-listed classes. Used by the runtime execution path; the static rule
	 * constants above remain unfiltered for the generated-template path.
	 */
	public static ArchRule allowAwareRuleFor(JavaArchitectureTestCaseSupported supported,
			Set<ClassPermission> allowedClasses, Set<PackagePermission> allowedPackages) {
		return switch (supported) {
		case FILESYSTEM_INTERACTION -> createNoClassShouldHaveMethodRule(
				Messages.localized("security.architecture.file.system.access"),
				FileHandlerConstants.ARCHUNIT_FILESYSTEM_METHODS, allowedClasses);
		case NETWORK_CONNECTION -> createNoClassShouldHaveMethodRule(
				Messages.localized("security.architecture.network.access"),
				FileHandlerConstants.ARCHUNIT_NETWORK_METHODS, allowedClasses);
		case THREAD_CREATION -> createNoClassShouldHaveMethodRule(
				Messages.localized("security.architecture.manipulate.threads"),
				FileHandlerConstants.ARCHUNIT_THREAD_MANIPULATION_METHODS, allowedClasses);
		case COMMAND_EXECUTION -> createNoClassShouldHaveMethodRule(
				Messages.localized("security.architecture.execute.command"),
				FileHandlerConstants.ARCHUNIT_COMMAND_EXECUTION_METHODS, allowedClasses);
		case PACKAGE_IMPORT -> noClassMustImportForbiddenPackages(allowedPackages, allowedClasses);
		case REFLECTION -> createNoClassShouldHaveMethodRule(
				Messages.localized("security.architecture.reflection.uses"),
				FileHandlerConstants.ARCHUNIT_REFLECTION_METHODS, allowedClasses);
		case TERMINATE_JVM -> createNoClassShouldHaveMethodRule(
				Messages.localized("security.architecture.terminate.jvm"),
				FileHandlerConstants.ARCHUNIT_JVM_TERMINATION_METHODS, allowedClasses);
		case SERIALIZATION -> createNoClassShouldHaveMethodRule(Messages.localized("security.architecture.serialize"),
				FileHandlerConstants.ARCHUNIT_SERIALIZATION_METHODS, allowedClasses);
		case CLASS_LOADING -> createNoClassShouldHaveMethodRule(
				Messages.localized("security.architecture.class.loading"),
				FileHandlerConstants.ARCHUNIT_CLASSLOADER_METHODS, allowedClasses);
		case NATIVE_CODE -> createNoClassShouldHaveMethodRule(
				Messages.localized("security.architecture.native.code.access"),
				FileHandlerConstants.ARCHUNIT_NATIVE_CODE_METHODS, allowedClasses);
		case AGENT_ATTACH -> createNoClassShouldHaveMethodRule(Messages.localized("security.architecture.agent.attach"),
				FileHandlerConstants.ARCHUNIT_AGENT_ATTACH_METHODS, allowedClasses);
		case ENVIRONMENT_ACCESS -> createNoClassShouldHaveMethodRule(
				Messages.localized("security.architecture.environment.access"),
				FileHandlerConstants.ARCHUNIT_ENVIRONMENT_ACCESS_METHODS, allowedClasses);
		case MODULE_SYSTEM -> createNoClassShouldHaveMethodRule(
				Messages.localized("security.architecture.module.system.access"),
				FileHandlerConstants.ARCHUNIT_MODULE_SYSTEM_METHODS, allowedClasses);
		case JNDI_INJECTION -> createNoClassShouldHaveMethodRule(
				Messages.localized("security.architecture.jndi.injection"),
				FileHandlerConstants.ARCHUNIT_JNDI_INJECTION_METHODS, allowedClasses);
		};
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
		return noClassMustImportForbiddenPackages(allowedPackages, Set.of());
	}

	/**
	 * Package-import rule that also exempts the allow-listed classes (an empty
	 * allow-list reproduces the original behaviour).
	 * <p>
	 * The allow-list is a union: a dependency is permitted as soon as <em>any</em>
	 * entry matches. So if the same package is present both as an exact-match entry
	 * and as a subpackage-inclusive one, the subpackage-inclusive entry wins
	 * because it is the more permissive of the two. That is intentional: the JDK
	 * baseline is exact-matched, and a policy author who additionally grants a JDK
	 * package as a subtree is making an explicit, visible choice to widen it.
	 */
	public static ArchRule noClassMustImportForbiddenPackages(Set<PackagePermission> allowedPackages,
			Set<ClassPermission> allowedClasses) {
		return ArchRuleDefinition.noClasses().that(isNotAllowedClass(allowedClasses)).should()
				.dependOnClassesThat(new DescribedPredicate<>("imports a forbidden package package") {
					@Override
					public boolean test(JavaClass javaClass) {
						String packageName = javaClass.getPackageName();
						return allowedPackages.stream().noneMatch(allowedPackage -> {
							String allowed = allowedPackage.importTheFollowingPackage();
							// Trailing-dot tolerant, boundary-aware match: a bare startsWith would
							// let allowed "com.foo" also cover the unrelated package "com.foobar".
							String normalizedAllowed = allowed.endsWith(".")
									? allowed.substring(0, allowed.length() - 1)
									: allowed;
							if (packageName.equals(normalizedAllowed)) {
								return true;
							}
							// An exact-match permission must not extend to subpackages. Allowing
							// "java.lang" as a subtree would silently re-admit java.lang.reflect,
							// java.lang.invoke, java.lang.instrument, java.lang.management,
							// java.lang.foreign and java.lang.module, which are governed by the
							// separate reflection, agent-attach, environment, native-code and
							// module-system rules.
							return !allowedPackage.exactMatchOnly() && packageName.startsWith(normalizedAllowed + ".");
						});
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

	// <editor-fold desc="Native code related rule">
	/**
	 * This method checks if any class in the given package accesses native code.
	 */
	public static final ArchRule NO_CLASS_MUST_ACCESS_NATIVE_CODE = createNoClassShouldHaveMethodRule(
			Messages.localized("security.architecture.native.code.access"),
			FileHandlerConstants.ARCHUNIT_NATIVE_CODE_METHODS);
	// </editor-fold>

	// <editor-fold desc="Agent attach related rule">
	/**
	 * This method checks if any class in the given package attaches agents.
	 */
	public static final ArchRule NO_CLASS_MUST_ATTACH_AGENTS = createNoClassShouldHaveMethodRule(
			Messages.localized("security.architecture.agent.attach"),
			FileHandlerConstants.ARCHUNIT_AGENT_ATTACH_METHODS);
	// </editor-fold>

	// <editor-fold desc="Environment access related rule">
	/**
	 * This method checks if any class in the given package accesses the
	 * environment.
	 */
	public static final ArchRule NO_CLASS_MUST_ACCESS_ENVIRONMENT = createNoClassShouldHaveMethodRule(
			Messages.localized("security.architecture.environment.access"),
			FileHandlerConstants.ARCHUNIT_ENVIRONMENT_ACCESS_METHODS);
	// </editor-fold>

	// <editor-fold desc="Module system related rule">
	/**
	 * This method checks if any class in the given package accesses the module
	 * system.
	 */
	public static final ArchRule NO_CLASS_MUST_ACCESS_MODULE_SYSTEM = createNoClassShouldHaveMethodRule(
			Messages.localized("security.architecture.module.system.access"),
			FileHandlerConstants.ARCHUNIT_MODULE_SYSTEM_METHODS);
	// </editor-fold>

	// <editor-fold desc="JNDI injection related rule">
	/**
	 * This method checks if any class in the given package performs JNDI lookups.
	 */
	public static final ArchRule NO_CLASS_MUST_PERFORM_JNDI_LOOKUPS = createNoClassShouldHaveMethodRule(
			Messages.localized("security.architecture.jndi.injection"),
			FileHandlerConstants.ARCHUNIT_JNDI_INJECTION_METHODS);
	// </editor-fold>
	// </editor-fold>
}
