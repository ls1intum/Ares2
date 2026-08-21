package de.tum.cit.ase.ares.api.architecture.java.archunit;

//<editor-fold desc="Imports">
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.tngtech.archunit.base.DescribedPredicate;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;

import de.tum.cit.ase.ares.api.policy.policySubComponents.PackagePermission;
//</editor-fold>

/**
 * The classes a generated architecture test analyses, read when that test runs
 * rather than when it was written.
 * <p>
 * A generated test used to carry the answer instead of the question: the
 * package list came from whatever was compiled at generation time. In the
 * precompile flow that is nothing, so the emitted import had no arguments,
 * every rule checked an empty set and passed, and the suite was green while
 * enforcing nothing. Runtime is also the only point at which the answer is
 * true, since the compiled output is what will execute and cannot change
 * afterwards the way a snapshot can.
 * <p>
 * <b>Why this class is self-contained.</b> Ares copies its {@code api} subtree
 * into the supervised project as sources, and a generated test compiles against
 * those copies, not the Ares jar. Only what the copy manifests list is
 * available, hence the output root resolved here rather than through
 * {@code ProjectSourcesFinder}, and diagnostics spelled out rather than taken
 * from the message bundle.
 *
 * @since 2.0.0
 * @author Markus Paulsen
 */
public final class JavaArchunitSupervisedClasses {

	// <editor-fold desc="Attributes">
	/**
	 * The inventory, kept per scope and mode so that a suite of rules imports the
	 * output once rather than once per rule.
	 */
	private static final Map<String, JavaClasses> CACHE = new ConcurrentHashMap<>();

	/**
	 * The namespaces supervised code may not declare and may not be granted.
	 * <p>
	 * Ares' own prefix is written in two pieces on purpose. Copying this class into
	 * the supervised project rewrites the framework's package token wherever it
	 * appears, string literals included, so spelling it out would silently turn
	 * this entry into the copied API's prefix and stop naming Ares itself.
	 */
	private static final List<String> RESERVED_PREFIXES = List.of("java.", "javax.", "sun.", "jdk.", "com.sun.",
			"de.tum." + "cit.ase.ares.api.", "net.bytebuddy.", "org.aspectj.", "com.ibm.wala.",
			"com.tngtech.archunit.");

	/**
	 * The prefix of the API copied beside this class, taken from where this class
	 * itself ended up rather than from a literal, which the copy would rewrite.
	 */
	private static final String COPIED_API_PREFIX = copiedApiPrefix();
	// </editor-fold>

	// <editor-fold desc="Constructor">
	private JavaArchunitSupervisedClasses() {
		throw new IllegalStateException(
				"JavaArchunitSupervisedClasses is a utility class and should not be instantiated");
	}
	// </editor-fold>

	// <editor-fold desc="Entry points">
	/**
	 * The supervised classes for a scope Ares derived from the project, checked
	 * against the whole compiled output before they are returned.
	 * <p>
	 * A derived scope is a reading of files the submitter can add to, so it is only
	 * a boundary once something confirms it: every executable class in the output
	 * must lie within it, or it would run unsupervised while the suite reported
	 * success. An output holding nothing executable is accepted with a warning,
	 * since there is then no class that could escape.
	 *
	 * @param supervisedPackage the derived scope
	 * @return the classes to analyse, empty only when nothing is compiled
	 * @throws SecurityException if the output cannot be identified or a class lies
	 *                           outside the scope
	 */
	public static JavaClasses validated(String supervisedPackage) {
		return CACHE.computeIfAbsent("validated|" + supervisedPackage,
				key -> importProduction(supervisedPackage, true));
	}

	/**
	 * The supervised classes for a scope an instructor pinned in a policy.
	 * <p>
	 * No whole-project check is made: a pinned policy may deliberately supervise
	 * part of the output, which is a decision rather than a mistake. Narrowing to
	 * <em>nothing</em> is not, so a scope matching no compiled class while classes
	 * exist is refused.
	 *
	 * @param supervisedPackage the pinned scope
	 * @return the classes to analyse
	 * @throws SecurityException if the output cannot be identified or the scope
	 *                           matches none of the compiled classes
	 */
	public static JavaClasses pinned(String supervisedPackage) {
		return CACHE.computeIfAbsent("pinned|" + supervisedPackage, key -> importProduction(supervisedPackage, false));
	}

	/**
	 * The packages the supervised code may import from itself, read from the
	 * compiled output rather than from the scope.
	 * <p>
	 * The scope is a prefix, so permitting it permits everything below it, and a
	 * permission written into the generated file outlives the coverage check beside
	 * it. The packages the output declares cannot be broader than that output, so
	 * they are what the generated rules ask for. A reserved one is skipped: it is
	 * refused during validation, so reaching one here would mean the inventory and
	 * the check disagreed, and a permission must never name a trusted namespace
	 * whatever else went wrong.
	 * <p>
	 * A package that <em>contains</em> a trusted namespace is refused outright.
	 * Validation cannot catch that one, because it asks whether a package lies
	 * inside a reserved prefix and {@code de.tum.cit} lies inside none, while a
	 * scope broad enough to hold it covers every compiled class by construction.
	 * Permitting it would grant the supervised code every import from the
	 * framework's own namespace below it, which is the framework supervising it.
	 *
	 * @param supervisedPackage the supervised scope
	 * @param declaredByPolicy  the permissions the policy itself granted, which
	 *                          stay authoritative
	 * @return the permissions the generated rule should apply
	 */
	public static Set<PackagePermission> allowedPackages(String supervisedPackage,
			Set<PackagePermission> declaredByPolicy) {
		Set<PackagePermission> permissions = new LinkedHashSet<>(declaredByPolicy);
		for (JavaClass javaClass : validated(supervisedPackage)) {
			String declared = javaClass.getPackageName();
			if (declared.isBlank() || reservedPrefixOf(declared) != null) {
				continue;
			}
			String reserved = ancestorOfReservedPrefix(declared);
			if (reserved != null) {
				throw new SecurityException("Ares Security Error (Reason: Student-Code; Stage: Execution): "
						+ "The compiled class " + javaClass.getName() + " declares the package " + declared
						+ ", which lies above the trusted namespace " + reserved
						+ ". A permission is matched as a prefix, so permitting that package would grant the "
						+ "supervised code every import from that namespace. Declare "
						+ "theSupervisedCodeUsesTheFollowingPackage in a security policy, and what may be "
						+ "imported in theFollowingResourceAccessesArePermitted.");
			}
			permissions.add(new PackagePermission(declared));
		}
		return permissions;
	}
	// </editor-fold>

	// <editor-fold desc="Import and validation">
	/**
	 * Imports the compiled production output whole, with nothing filtered out:
	 * filtering first would decide what is worth checking by package name, which is
	 * exactly what whoever adds files controls, and an excluded class would never
	 * reach the check below.
	 * <p>
	 * No compiled class at all is nothing to supervise rather than a wrong scope,
	 * which is the state of an exercise whose supervised package is still empty, so
	 * it warns instead of refusing. A validated scope then returns the whole
	 * inventory, since the check has just established that all of it lies within
	 * the scope; a pinned one is narrowed here rather than at import, because
	 * importing whole is what keeps that check from being decided by the very
	 * package names it inspects.
	 */
	private static JavaClasses importProduction(String supervisedPackage, boolean validateCoverage) {
		if (supervisedPackage == null || supervisedPackage.isBlank()) {
			throw new SecurityException("Ares Security Error (Reason: Ares-Code; Stage: Execution): "
					+ "The supervised scope is empty, so there is nothing for the architecture rules to be "
					+ "checked against. Declare theSupervisedCodeUsesTheFollowingPackage in a security policy.");
		}
		Path outputRoot = productionOutputRoot();
		if (!Files.isDirectory(outputRoot)) {
			warnNothingToSupervise(outputRoot, supervisedPackage);
			return new ClassFileImporter().importPackages();
		}
		JavaClasses imported = new ClassFileImporter().importPath(outputRoot);
		List<JavaClass> supervisable = supervisableClassesIn(imported);
		if (supervisable.isEmpty()) {
			warnNothingToSupervise(outputRoot, supervisedPackage);
			return imported;
		}
		if (validateCoverage) {
			requireScopeToCover(supervisable, supervisedPackage, outputRoot);
			return imported;
		}
		JavaClasses narrowed = imported
				.that(new DescribedPredicate<JavaClass>("declared within the supervised scope " + supervisedPackage) {
					@Override
					public boolean test(JavaClass javaClass) {
						return isWithinScope(javaClass.getPackageName(), supervisedPackage);
					}
				});
		if (!narrowed.iterator().hasNext()) {
			throw new SecurityException("Ares Security Error (Reason: Ares-Code; Stage: Execution): "
					+ "The pinned supervised scope \"" + supervisedPackage + "\" matches no compiled production "
					+ "class under " + outputRoot + ", although " + supervisable.size() + " were found there, so "
					+ "the architecture rules would be checked against nothing. Correct "
					+ "theSupervisedCodeUsesTheFollowingPackage in the security policy.");
		}
		return narrowed;
	}

	/**
	 * Reports that there is no compiled production class to analyse, which leaves
	 * the architecture rules vacuous rather than mis-scoped.
	 * <p>
	 * Written to {@link System.Logger} rather than to Ares' message bundle, for the
	 * same reason the diagnostics above are spelled out here: only what the copy
	 * manifest lists is available in a supervised project.
	 */
	private static void warnNothingToSupervise(Path outputRoot, String supervisedPackage) {
		System.getLogger(JavaArchunitSupervisedClasses.class.getName()).log(System.Logger.Level.WARNING,
				"No compiled production class was found under {0}, so the architecture rules for the supervised "
						+ "scope \"{1}\" have nothing to analyse and cannot fail. This is expected while the "
						+ "supervised package is still empty; if it is not empty, the production output was "
						+ "written somewhere this check does not read.",
				outputRoot, supervisedPackage);
	}

	/**
	 * Refuses a derived scope the compiled output contradicts, naming the class
	 * that escapes it so the reader is told what to fix rather than that something
	 * is wrong.
	 */
	private static void requireScopeToCover(List<JavaClass> supervisable, String supervisedPackage, Path outputRoot) {
		for (JavaClass javaClass : supervisable) {
			String declared = javaClass.getPackageName();
			if (declared.isBlank()) {
				throw new SecurityException("Ares Security Error (Reason: Student-Code; Stage: Execution): "
						+ "The compiled class " + javaClass.getName() + " under " + outputRoot
						+ " declares no package; a class in the default package lies outside every supervised "
						+ "scope. Declare a package for it, or pin theSupervisedCodeUsesTheFollowingPackage in "
						+ "a security policy.");
			}
			String reserved = reservedPrefixOf(declared);
			if (reserved != null) {
				throw new SecurityException("Ares Security Error (Reason: Student-Code; Stage: Execution): "
						+ "The compiled class " + javaClass.getName() + " under " + outputRoot
						+ " declares the reserved package " + declared + ", which lies inside the trusted "
						+ "namespace " + reserved + ". Supervised code may not be named into a trusted "
						+ "namespace, because the analysers and the runtime both treat those frames as the "
						+ "framework's own.");
			}
			if (!isWithinScope(declared, supervisedPackage)) {
				throw new SecurityException("Ares Security Error (Reason: Student-Code; Stage: Execution): "
						+ "The supervised scope derived from the project is " + supervisedPackage
						+ ", but the compiled class " + javaClass.getName() + " under " + outputRoot
						+ " declares the package " + declared + ", which lies outside it. The derivation counts "
						+ "declarations and can therefore be steered by whoever adds files, so Ares refuses to "
						+ "enforce over part of the output. Declare theSupervisedCodeUsesTheFollowingPackage in "
						+ "a security policy to state the scope authoritatively.");
			}
		}
	}

	/**
	 * The classes any scope could cover: executable code, excluding the descriptors
	 * that cannot run. Nothing else is removed, because what is left is what the
	 * scope is checked against.
	 */
	private static List<JavaClass> supervisableClassesIn(JavaClasses imported) {
		List<JavaClass> supervisable = new ArrayList<>();
		for (JavaClass javaClass : imported) {
			if (javaClass.isTopLevelClass() && !isCompilationMetadata(javaClass)) {
				supervisable.add(javaClass);
			}
		}
		return supervisable;
	}
	// </editor-fold>

	// <editor-fold desc="Helpers">
	/**
	 * The production output root, decided by which build descriptor the project has
	 * rather than by which output directory happens to exist.
	 * <p>
	 * Trying {@code target/classes} and then {@code build/classes/java/main} in
	 * turn looks equivalent and is not: a Gradle project that once built with Maven
	 * still has a {@code target/classes}, and the rules would then be checked
	 * against a stale tree that need not resemble what will run. The descriptor
	 * says which build tool owns the project, so it decides. A project carrying
	 * both descriptors is refused rather than guessed at.
	 *
	 * @return the production output root
	 */
	private static Path productionOutputRoot() {
		boolean maven = Files.isRegularFile(Path.of("pom.xml"));
		boolean gradle = Files.isRegularFile(Path.of("build.gradle"))
				|| Files.isRegularFile(Path.of("build.gradle.kts"));
		if (maven && gradle) {
			throw new SecurityException("Ares Security Error (Reason: Ares-Code; Stage: Execution): "
					+ "The project has both a Maven and a Gradle descriptor, so which compiled output the "
					+ "architecture rules should be checked against is ambiguous. Ares refuses to guess.");
		}
		if (!maven && !gradle) {
			throw new SecurityException("Ares Security Error (Reason: Ares-Code; Stage: Execution): "
					+ "The project has neither a Maven nor a Gradle descriptor, so the compiled output the "
					+ "architecture rules should be checked against cannot be identified.");
		}
		return Path.of(maven ? "target/classes" : "build/classes/java/main").toAbsolutePath();
	}

	/**
	 * Whether the declared package is the scope or lies below it, compared on a
	 * segment boundary so that a scope of {@code de.tum.cit.aet} does not swallow
	 * the unrelated {@code de.tum.cit.aetevil}.
	 */
	private static boolean isWithinScope(String declared, String scope) {
		return declared.equals(scope) || declared.startsWith(scope + ".");
	}

	/**
	 * The trusted namespace the package lies inside, or null if it lies inside
	 * none. The copied API counts as one: in a supervised project it is the
	 * framework, whatever it is called there.
	 */
	private static String reservedPrefixOf(String packageName) {
		if (packageName == null || packageName.isBlank()) {
			return null;
		}
		String normalized = packageName.endsWith(".") ? packageName : packageName + ".";
		if (!COPIED_API_PREFIX.isEmpty() && normalized.startsWith(COPIED_API_PREFIX)) {
			return COPIED_API_PREFIX;
		}
		for (String reserved : RESERVED_PREFIXES) {
			if (normalized.startsWith(reserved)) {
				return reserved;
			}
		}
		return null;
	}

	/**
	 * The trusted namespace lying <em>below</em> the package, or null if none does.
	 * <p>
	 * The complement of {@link #reservedPrefixOf(String)}, and the question a
	 * permission has to ask. That one refuses a package inside a trusted namespace,
	 * which is how supervised code would be trusted by name. This one refuses a
	 * package that contains one, because permitting a package permits everything
	 * below it. A package equal to a reserved prefix lies inside it rather than
	 * above it, and is left to the other method so that one package cannot be
	 * refused twice for two different reasons.
	 */
	private static String ancestorOfReservedPrefix(String packageName) {
		if (packageName == null || packageName.isBlank()) {
			return null;
		}
		// Reserved prefixes carry a trailing dot and package names do not, so both
		// ends are normalised before comparing. Without it "com" would not be seen as
		// the ancestor of "com.sun." that it is.
		String normalized = packageName.endsWith(".") ? packageName : packageName + ".";
		if (!COPIED_API_PREFIX.isEmpty() && COPIED_API_PREFIX.startsWith(normalized)
				&& !COPIED_API_PREFIX.equals(normalized)) {
			return COPIED_API_PREFIX;
		}
		for (String reserved : RESERVED_PREFIXES) {
			if (reserved.startsWith(normalized) && !reserved.equals(normalized)) {
				return reserved;
			}
		}
		return null;
	}

	private static String copiedApiPrefix() {
		String self = JavaArchunitSupervisedClasses.class.getPackageName();
		int marker = self.indexOf(".ares.api.");
		return marker < 0 ? "" : self.substring(0, marker) + ".ares.api.";
	}

	private static boolean isCompilationMetadata(JavaClass javaClass) {
		String name = javaClass.getName();
		String simpleName = name.substring(name.lastIndexOf('.') + 1);
		return "package-info".equals(simpleName) || "module-info".equals(simpleName);
	}
	// </editor-fold>
}
