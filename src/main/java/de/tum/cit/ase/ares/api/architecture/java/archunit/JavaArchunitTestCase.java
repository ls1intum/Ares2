package de.tum.cit.ase.ares.api.architecture.java.archunit;

//<editor-fold desc="Imports">

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.google.common.base.Preconditions;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaClasses;

import de.tum.cit.ase.ares.api.architecture.java.JavaArchitectureTestCase;
import de.tum.cit.ase.ares.api.architecture.java.JavaArchitectureTestCaseSupported;
import de.tum.cit.ase.ares.api.localization.Messages;
import de.tum.cit.ase.ares.api.policy.policySubComponents.PackagePermission;
import de.tum.cit.ase.ares.api.util.FileTools;
//</editor-fold>

/**
 * Architecture test case for the Java programming language using Archunit and
 * concrete product of the abstract factory design pattern.
 *
 * @author Sarp Sahinalp
 * @version 2.0.0
 * @see <a href=
 *      "https://refactoring.guru/design-patterns/abstract-factory">Abstract
 *      Factory Design Pattern</a>
 * @since 2.0.0
 */
public class JavaArchunitTestCase extends JavaArchitectureTestCase {

	// <editor-fold desc="Constructors">

	public JavaArchunitTestCase(@Nonnull JavaArchitectureTestCaseSupported javaArchitectureTestCaseSupported,
			@Nonnull Set<PackagePermission> allowedPackages, @Nonnull JavaClasses javaClasses) {
		super(javaArchitectureTestCaseSupported, allowedPackages, javaClasses, null);
	}

	// </editor-fold>

	// <editor-fold desc="Write security test case methods">

	/**
	 * Formats the Set<PackagePermission> structure as a Java-literal
	 * Set.of(PackagePermission(...), ...).
	 */
	private String allowedPackagesAsCode() {
		if (allowedPackages.isEmpty()) {
			return "Set.of()";
		}
		String inner = allowedPackages.stream().map(pp -> String.format("new %s(\"%s\")",
				PackagePermission.class.getSimpleName(), pp.importTheFollowingPackage()))
				.collect(Collectors.joining(", "));
		return "Set.of(" + inner + ")";
	}

	/**
	 * Formats the JavaClasses structure as a Java-literal
	 * ClassFileImporter.importPackages(...) String.
	 */
	private String javaClassesAsCode() {
		Set<String> packages = javaClasses.stream().map(JavaClass::getPackageName)
				.collect(Collectors.toCollection(HashSet::new));

		if (packages.isEmpty()) {
			return "new ClassFileImporter()\n" + ".withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)\n"
					+ ".withImportOption(location -> {\n"
					+ "String path = location.toString().replace(\"\\\\\", \"/\");\n"
					+ "return !path.contains(\"/ares/api/\");\n" + "})\n" + ".importPackages()";
		}
		String packagesAsString = packages.stream().map(p -> "\"" + p + "\"").collect(Collectors.joining(", "));
		return "new ClassFileImporter()\n" + ".withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)\n"
				+ ".withImportOption(location -> {\n" + "String path = location.toString().replace(\"\\\\\", \"/\");\n"
				+ "return !path.contains(\"/ares/api/\");\n" + "})\n" + ".importPackages(" + packagesAsString + ")";
	}

	/**
	 * Returns the content of the architecture test case file in the Java
	 * programming language.
	 */
	@Override
	@Nonnull
	public String writeArchitectureTestCase(@Nonnull String architectureMode, @Nonnull String aopMode) {
		try {
			String testWithPlaceholders = FileTools
					.readRuleFile(FileTools.readFile(FileTools.resolveFileOnSourceDirectory("templates", "architecture",
							"java", "archunit", "rules",
							((JavaArchitectureTestCaseSupported) this.architectureTestCaseSupported).name() + ".txt")))
					.stream().reduce("", (acc, line) -> acc + line + "\n");
			return testWithPlaceholders.replace("${allowedPackages}", allowedPackagesAsCode()).replace("${javaClasses}",
					javaClassesAsCode());
		} catch (AssertionError | IOException e) {
			throw new SecurityException(Messages.localized("architecture.illegal.statement", e.getMessage()));
		}
	}
	// </editor-fold>

	// <editor-fold desc="Execute security test case methods">

	/**
	 * Executes the architecture test case.
	 */
	@Override
	public void executeArchitectureTestCase(@Nonnull String architectureMode, @Nonnull String aopMode) {
		try {
			switch ((JavaArchitectureTestCaseSupported) this.architectureTestCaseSupported) {
			case FILESYSTEM_INTERACTION -> JavaArchunitTestCaseCollection.NO_CLASS_MUST_ACCESS_FILE_SYSTEM
					.check(javaClasses);
			case NETWORK_CONNECTION -> JavaArchunitTestCaseCollection.NO_CLASS_MUST_ACCESS_NETWORK.check(javaClasses);
			case COMMAND_EXECUTION -> JavaArchunitTestCaseCollection.NO_CLASS_MUST_EXECUTE_COMMANDS.check(javaClasses);
			case THREAD_CREATION -> JavaArchunitTestCaseCollection.NO_CLASS_MUST_CREATE_THREADS.check(javaClasses);
			case PACKAGE_IMPORT -> JavaArchunitTestCaseCollection.noClassMustImportForbiddenPackages(allowedPackages)
					.check(javaClasses);
			case REFLECTION -> JavaArchunitTestCaseCollection.NO_CLASS_MUST_USE_REFLECTION.check(javaClasses);
			case TERMINATE_JVM -> JavaArchunitTestCaseCollection.NO_CLASS_MUST_TERMINATE_JVM.check(javaClasses);
			case SERIALIZATION -> JavaArchunitTestCaseCollection.NO_CLASS_MUST_SERIALIZE.check(javaClasses);
			case CLASS_LOADING -> JavaArchunitTestCaseCollection.NO_CLASS_MUST_USE_CLASSLOADERS.check(javaClasses);
			case NATIVE_CODE -> JavaArchunitTestCaseCollection.NO_CLASS_MUST_ACCESS_NATIVE_CODE.check(javaClasses);
			case AGENT_ATTACH -> JavaArchunitTestCaseCollection.NO_CLASS_MUST_ATTACH_AGENTS.check(javaClasses);
			case ENVIRONMENT_ACCESS -> JavaArchunitTestCaseCollection.NO_CLASS_MUST_ACCESS_ENVIRONMENT.check(javaClasses);
			case MODULE_SYSTEM -> JavaArchunitTestCaseCollection.NO_CLASS_MUST_ACCESS_MODULE_SYSTEM.check(javaClasses);
			default -> throw new SecurityException(
					Messages.localized("security.common.unsupported.operation", this.architectureTestCaseSupported));
			}
		} catch (AssertionError e) {
			JavaArchitectureTestCase.parseErrorMessage(e);
		}
	}
	// </editor-fold>

	// <editor-fold desc="Builder">

	/**
	 * Creates a new builder instance for constructing JavaArchitectureTestCase
	 * objects.
	 *
	 * @return A new Builder instance
	 * @author Sarp Sahinalp
	 * @since 2.0.0
	 */
	@Nonnull
	public static JavaArchunitTestCase.Builder archunitBuilder() {
		return new JavaArchunitTestCase.Builder();
	}

	/**
	 * Builder for the Java architecture test case.
	 */
	public static class Builder {
		@Nullable
		private JavaArchitectureTestCaseSupported javaArchitectureTestCaseSupported;
		@Nullable
		private JavaClasses javaClasses;
		@Nullable
		private Set<PackagePermission> allowedPackages;

		/**
		 * Sets the architecture test case type supported by this instance.
		 *
		 * @param javaArchitectureTestCaseSupported The type of architecture test case
		 *                                          to support
		 * @return This builder instance for method chaining
		 * @throws SecurityException if the parameter is null
		 * @author Sarp Sahinalp
		 * @since 2.0.0
		 */
		@Nonnull
		public JavaArchunitTestCase.Builder javaArchitectureTestCaseSupported(
				@Nonnull JavaArchitectureTestCaseSupported javaArchitectureTestCaseSupported) {
			this.javaArchitectureTestCaseSupported = Preconditions.checkNotNull(javaArchitectureTestCaseSupported,
					"javaArchitecturalTestCaseSupported must not be null");
			return this;
		}

		/**
		 * Sets the Java classes to be analyzed by this architecture test case.
		 *
		 * @param javaClasses Collection of Java classes for analysis
		 * @return This builder instance for method chaining
		 * @author Sarp Sahinalp
		 * @since 2.0.0
		 */
		@Nonnull
		public JavaArchunitTestCase.Builder javaClasses(@Nonnull JavaClasses javaClasses) {
			this.javaClasses = Preconditions.checkNotNull(javaClasses, "javaClasses must not be null");
			return this;
		}

		/**
		 * Sets the allowed package permissions.
		 *
		 * @param allowedPackages Set of package permissions that should be allowed
		 * @return This builder instance for method chaining
		 * @author Sarp Sahinalp
		 * @since 2.0.0
		 */
		@Nonnull
		public JavaArchunitTestCase.Builder allowedPackages(@Nonnull Set<PackagePermission> allowedPackages) {
			this.allowedPackages = Preconditions.checkNotNull(allowedPackages, "allowedPackages must not be null");
			return this;
		}

		/**
		 * Builds and returns a new JavaArchitectureTestCase instance with the
		 * configured properties.
		 *
		 * @return A new JavaArchitectureTestCase instance
		 * @throws SecurityException if required parameters are missing
		 * @author Sarp Sahinalp
		 * @since 2.0.0
		 */
		@Nonnull
		public JavaArchunitTestCase build() {
			return new JavaArchunitTestCase(
					Preconditions.checkNotNull(javaArchitectureTestCaseSupported,
							"javaArchitecturalTestCaseSupported must not be null"),
					Preconditions.checkNotNull(allowedPackages, "allowedPackages must not be null"),
					Preconditions.checkNotNull(javaClasses, "javaClasses must not be null"));
		}
	}
	// </editor-fold>
}
