package de.tum.cit.ase.ares.api.architecture.java.archunit;

//<editor-fold desc="Imports">

import java.io.IOException;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaClasses;

import de.tum.cit.ase.ares.api.architecture.java.JavaArchitectureTestCase;
import de.tum.cit.ase.ares.api.architecture.java.JavaArchitectureTestCaseSupported;
import de.tum.cit.ase.ares.api.localization.Messages;
import de.tum.cit.ase.ares.api.policy.policySubComponents.ClassPermission;
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

	public JavaArchunitTestCase(@Nonnull JavaArchitectureTestCaseSupported javaArchitectureTestCaseSupported,
			@Nonnull Set<PackagePermission> allowedPackages, @Nonnull JavaClasses javaClasses,
			@Nonnull Set<ClassPermission> allowedClasses) {
		super(javaArchitectureTestCaseSupported, allowedPackages, javaClasses, null, null, allowedClasses);
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
					+ "return !path.contains(\"/de/tum/cit/ase/ares/api/\");\n" + "})\n" + ".importPackages()";
		}
		String packagesAsString = packages.stream().map(p -> "\"" + p + "\"").collect(Collectors.joining(", "));
		return "new ClassFileImporter()\n" + ".withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)\n"
				+ ".withImportOption(location -> {\n" + "String path = location.toString().replace(\"\\\\\", \"/\");\n"
				+ "return !path.contains(\"/de/tum/cit/ase/ares/api/\");\n" + "})\n" + ".importPackages("
				+ packagesAsString + ")";
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
	/**
	 * Cache of per-({@link JavaArchitectureTestCaseSupported}, {@code JavaClasses})
	 * outcomes. Each entry is either {@code Optional.empty()} for a clean rule or
	 * {@code Optional.of(AssertionError)} captured from the first evaluation.
	 * Subsequent invocations replay the cached outcome instead of re-scanning.
	 * Keyed on category name plus {@code System.identityHashCode} of the
	 * {@code JavaClasses}; {@code JavaClasses} is rebuilt at most once per JVM fork
	 * by {@code ArchitectureMode.getJavaClasses(classPath)}, so the identity is
	 * stable for the fork's lifetime.
	 */
	/**
	 * Cache of per-({@link JavaArchitectureTestCaseSupported}, {@code JavaClasses})
	 * outcomes. Each entry is either {@code Optional.empty()} (no violation) or
	 * {@code Optional.of(SecurityException)} pre-parsed from the first evaluation.
	 * Storing the already-parsed exception lets cached re-throws skip
	 * {@link JavaArchitectureTestCase#parseErrorMessage(AssertionError)} entirely.
	 * Keyed on category name plus {@code System.identityHashCode} of the
	 * {@code JavaClasses}; that instance is rebuilt at most once per JVM fork.
	 */
	// The cached value is a FutureTask rather than the plain outcome so that the
	// expensive
	// ArchUnit check does not run inside computeIfAbsent: computeIfAbsent only
	// allocates the task
	// and returns immediately, releasing the map's bin lock, while the actual
	// evaluation happens
	// in task.run() outside the map. A concurrent caller for the same key sees the
	// same task, so
	// the check still runs at most once and the second caller simply blocks on
	// task.get().
	private static final java.util.concurrent.ConcurrentHashMap<String, java.util.concurrent.FutureTask<java.util.Optional<SecurityException>>> RULE_OUTCOME_CACHE = new java.util.concurrent.ConcurrentHashMap<>();
	private static final java.util.concurrent.ConcurrentHashMap<String, java.util.concurrent.FutureTask<java.util.Optional<SecurityException>>> PACKAGE_OUTCOME_CACHE = new java.util.concurrent.ConcurrentHashMap<>();

	/**
	 * Per-instance identity tokens for the analysed {@link JavaClasses}. Replaces
	 * {@code System.identityHashCode}, whose 32-bit value could collide between two
	 * distinct instances within one JVM and mis-serve a cached security verdict. An
	 * {@link java.util.IdentityHashMap} keyed on the actual instance gives a
	 * guaranteed-unique token per distinct {@code JavaClasses}.
	 */
	private static final java.util.Map<JavaClasses, String> JAVA_CLASSES_TOKENS = new java.util.IdentityHashMap<>();
	private static final java.util.concurrent.atomic.AtomicLong JAVA_CLASSES_TOKEN_SEQUENCE = new java.util.concurrent.atomic.AtomicLong();

	private static String javaClassesToken(JavaClasses javaClasses) {
		synchronized (JAVA_CLASSES_TOKENS) {
			return JAVA_CLASSES_TOKENS.computeIfAbsent(javaClasses,
					key -> "jc" + JAVA_CLASSES_TOKEN_SEQUENCE.incrementAndGet());
		}
	}

	@Override
	public void executeArchitectureTestCase(@Nonnull String architectureMode, @Nonnull String aopMode) {
		JavaArchitectureTestCaseSupported supported = (JavaArchitectureTestCaseSupported) this.architectureTestCaseSupported;
		// The allow-listed classes change which violations are exempt, so they MUST be
		// part of the cache key; otherwise a run with exemptions could be served a
		// no-exemption outcome (or vice versa).
		String allowedClassesSignature = getAllowedClasses().stream().map(ClassPermission::className).sorted()
				.collect(java.util.stream.Collectors.joining(","));
		java.util.concurrent.FutureTask<java.util.Optional<SecurityException>> task;
		if (supported == JavaArchitectureTestCaseSupported.PACKAGE_IMPORT) {
			String allowedSignature = allowedPackages.stream().map(p -> p.importTheFollowingPackage()).sorted()
					.collect(java.util.stream.Collectors.joining(","));
			String pkgKey = allowedSignature + "#" + allowedClassesSignature + "@" + javaClassesToken(javaClasses);
			task = PACKAGE_OUTCOME_CACHE.computeIfAbsent(pkgKey,
					unusedKey -> new java.util.concurrent.FutureTask<>(() -> runRuleAndCapture(supported)));
		} else {
			String key = supported.name() + "#" + allowedClassesSignature + "@" + javaClassesToken(javaClasses);
			task = RULE_OUTCOME_CACHE.computeIfAbsent(key,
					unusedKey -> new java.util.concurrent.FutureTask<>(() -> runRuleAndCapture(supported)));
		}
		// run() evaluates the rule only for the first caller of this key; for any later
		// caller the
		// task is already run, so run() is a no-op and awaitOutcome just returns the
		// cached result.
		task.run();
		java.util.Optional<SecurityException> cached = awaitOutcome(task);
		if (cached.isPresent()) {
			throw cached.get();
		}
	}

	private static java.util.Optional<SecurityException> awaitOutcome(
			java.util.concurrent.FutureTask<java.util.Optional<SecurityException>> task) {
		try {
			return task.get();
		} catch (InterruptedException interrupted) {
			Thread.currentThread().interrupt();
			throw new IllegalStateException("Interrupted while awaiting the architecture rule outcome", interrupted);
		} catch (java.util.concurrent.ExecutionException executionFailure) {
			Throwable cause = executionFailure.getCause();
			if (cause instanceof RuntimeException runtimeCause) {
				throw runtimeCause;
			}
			if (cause instanceof Error errorCause) {
				throw errorCause;
			}
			throw new IllegalStateException("The architecture rule evaluation failed", executionFailure);
		}
	}

	private java.util.Optional<SecurityException> runRuleAndCapture(JavaArchitectureTestCaseSupported supported) {
		try {
			// Route through the allow-aware rule so allow-listed (essential/test) classes
			// are exempt; an empty allow-list reproduces the original behaviour.
			JavaArchunitTestCaseCollection.allowAwareRuleFor(supported, getAllowedClasses(), allowedPackages)
					.check(javaClasses);
			return java.util.Optional.empty();
		} catch (AssertionError ae) {
			try {
				JavaArchitectureTestCase.parseErrorMessage(ae);
				return java.util.Optional.empty();
			} catch (SecurityException parsed) {
				return java.util.Optional.of(parsed);
			}
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
		@Nonnull
		private Set<ClassPermission> allowedClasses = Set.of();

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
			this.javaArchitectureTestCaseSupported = Objects.requireNonNull(javaArchitectureTestCaseSupported,
					"javaArchitecturalTestCaseSupported must not be null");
			return this;
		}

		/**
		 * Sets the Java classes to be analysed by this architecture test case.
		 *
		 * @param javaClasses Collection of Java classes for analysis
		 * @return This builder instance for method chaining
		 * @author Sarp Sahinalp
		 * @since 2.0.0
		 */
		@Nonnull
		public JavaArchunitTestCase.Builder javaClasses(@Nonnull JavaClasses javaClasses) {
			this.javaClasses = Objects.requireNonNull(javaClasses, "javaClasses must not be null");
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
			this.allowedPackages = Objects.requireNonNull(allowedPackages, "allowedPackages must not be null");
			return this;
		}

		/**
		 * Sets the allowed (exempt) class permissions.
		 *
		 * @param allowedClasses Set of class permissions exempt from the rules
		 * @return This builder instance for method chaining
		 * @author Markus Paulsen
		 * @since 2.0.0
		 */
		@Nonnull
		public JavaArchunitTestCase.Builder allowedClasses(@Nonnull Set<ClassPermission> allowedClasses) {
			this.allowedClasses = Objects.requireNonNull(allowedClasses, "allowedClasses must not be null");
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
					Objects.requireNonNull(javaArchitectureTestCaseSupported,
							"javaArchitecturalTestCaseSupported must not be null"),
					Objects.requireNonNull(allowedPackages, "allowedPackages must not be null"),
					Objects.requireNonNull(javaClasses, "javaClasses must not be null"), allowedClasses);
		}
	}
	// </editor-fold>
}
