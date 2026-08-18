package de.tum.cit.ase.ares.api.architecture.java.archunit;

//<editor-fold desc="Imports">
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
//</editor-fold>

/**
 * The classes a generated architecture test analyses, read when that test runs
 * rather than when it was written.
 * <p>
 * A generated test used to carry the answer instead of the question: the
 * package list was taken from whatever was compiled at generation time and
 * written into the file as a literal. In the precompile flow nothing is
 * compiled at that point, so the list was empty, the emitted import had no
 * arguments, and every rule then checked an empty set of classes and passed.
 * The suite was green and enforced nothing.
 * <p>
 * Asking at runtime fixes that, and it is also the only point at which the
 * answer is true: the compiled output is what will actually execute, and it
 * cannot have changed afterwards the way a generation-time snapshot can.
 * <p>
 * <b>Why this class is self-contained.</b> Ares copies its {@code api} subtree
 * into the supervised project as sources, and a generated test compiles against
 * those copies rather than against the Ares jar. Only what the copy manifests
 * list is available, which is why this reads the output root and the reserved
 * prefixes itself instead of calling {@code JavaProjectScanner} or
 * {@code ProjectSourcesFinder}, and why its diagnostics are written out here
 * rather than taken from the message bundle.
 *
 * @since 2.0.0
 * @author Markus Paulsen
 */
public final class JavaArchunitSupervisedClasses {

	// <editor-fold desc="Attributes">
	/**
	 * The build tools' conventional production output directories, in the order
	 * they are tried. Read rather than taken from the descriptor, so a build that
	 * writes elsewhere is not followed there.
	 */
	private static final List<String> PRODUCTION_OUTPUT_ROOTS = List.of("target/classes", "build/classes/java/main");

	/**
	 * Path fragments belonging to the framework rather than to the supervised
	 * project: Ares itself, and the copy of its API that generation writes into the
	 * project. Neither can be supervised, and demanding that a scope cover them
	 * would demand the impossible.
	 */
	private static final List<String> FRAMEWORK_PATHS = List.of("/de/tum/cit/ase/ares/api/", "/ares/api/");

	/**
	 * The inventory, kept per scope and mode so that a suite of rules imports the
	 * output once rather than once per rule.
	 */
	private static final Map<String, JavaClasses> CACHE = new ConcurrentHashMap<>();
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
	 * a boundary once something confirms it. Every executable class in the output
	 * must lie within it; a class outside it would otherwise run unsupervised while
	 * the suite reported success.
	 *
	 * @param supervisedPackage the derived scope
	 * @return the classes to analyse; never empty
	 * @throws SecurityException if nothing is compiled, or a class lies outside the
	 *                           scope
	 */
	public static JavaClasses validated(String supervisedPackage) {
		return CACHE.computeIfAbsent("validated|" + supervisedPackage,
				key -> importProduction(supervisedPackage, true));
	}

	/**
	 * The supervised classes for a scope an instructor pinned in a policy.
	 * <p>
	 * Only the scope's own subtree is imported and no whole-project check is made:
	 * a pinned policy may deliberately supervise part of the output, and narrowing
	 * it is then a decision rather than a mistake.
	 *
	 * @param supervisedPackage the pinned scope
	 * @return the classes to analyse
	 * @throws SecurityException if nothing is compiled
	 */
	public static JavaClasses pinned(String supervisedPackage) {
		return CACHE.computeIfAbsent("pinned|" + supervisedPackage, key -> importProduction(supervisedPackage, false));
	}
	// </editor-fold>

	// <editor-fold desc="Import and validation">
	private static JavaClasses importProduction(String supervisedPackage, boolean validateCoverage) {
		if (supervisedPackage == null || supervisedPackage.isBlank()) {
			throw new SecurityException("Ares Security Error (Reason: Ares-Code; Stage: Execution): "
					+ "The supervised scope is empty, so there is nothing for the architecture rules to be "
					+ "checked against. Declare theSupervisedCodeUsesTheFollowingPackage in a security policy.");
		}
		Path outputRoot = productionOutputRoot();
		JavaClasses imported = new ClassFileImporter().withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
				.withImportOption(location -> !isFrameworkOwned(location.toString()))
				.withImportOption(
						location -> validateCoverage || isWithinScopePath(location.toString(), supervisedPackage))
				.importPath(outputRoot);
		List<JavaClass> supervisable = supervisableClassesIn(imported);
		if (supervisable.isEmpty()) {
			// Empty at generation is ordinary; empty here is not. This runs when the
			// output is final, so nothing to analyse means the rules would pass against
			// nothing, which is what this class exists to stop.
			throw new SecurityException("Ares Security Error (Reason: Ares-Code; Stage: Execution): "
					+ "No supervisable production class was found under " + outputRoot + " for the supervised "
					+ "scope \"" + supervisedPackage + "\", so the architecture rules would be checked against "
					+ "nothing. Ares refuses to report success over an empty analysis.");
		}
		if (validateCoverage) {
			requireScopeToCover(supervisable, supervisedPackage, outputRoot);
		}
		return imported;
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
			if (!isWithinScope(declared, supervisedPackage)) {
				throw new SecurityException("Ares Security Error (Reason: Student-Code; Stage: Execution): "
						+ "The supervised scope derived from the project is " + supervisedPackage
						+ ", but the compiled class " + javaClass.getName() + " under " + outputRoot
						+ " declares the package " + declared + ", which lies outside it. The derivation counts "
						+ "declarations and can therefore be steered by whoever adds files, so Ares refuses to "
						+ "enforce over part of the output. Declare "
						+ "theSupervisedCodeUsesTheFollowingPackage in a security policy to state the scope "
						+ "authoritatively.");
			}
		}
	}

	/**
	 * The classes any scope could cover: executable code, excluding the descriptors
	 * that cannot run.
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
	 * The production output root, or a refusal naming both places that were tried.
	 */
	private static Path productionOutputRoot() {
		for (String candidate : PRODUCTION_OUTPUT_ROOTS) {
			Path root = Path.of(candidate);
			if (Files.isDirectory(root)) {
				return root.toAbsolutePath();
			}
		}
		throw new SecurityException("Ares Security Error (Reason: Ares-Code; Stage: Execution): "
				+ "No compiled production output was found under " + String.join(" or ", PRODUCTION_OUTPUT_ROOTS)
				+ ", so the architecture rules have nothing to be checked against. Ares refuses to report "
				+ "success over an empty analysis.");
	}

	/**
	 * Whether the declared package is the scope or lies below it, compared on a
	 * segment boundary so that a scope of {@code de.tum.cit.aet} does not swallow
	 * the unrelated {@code de.tum.cit.aetevil}.
	 */
	private static boolean isWithinScope(String declared, String scope) {
		return declared.equals(scope) || declared.startsWith(scope + ".");
	}

	private static boolean isWithinScopePath(String location, String scope) {
		return location.replace("\\", "/").contains("/" + scope.replace('.', '/') + "/");
	}

	private static boolean isFrameworkOwned(String location) {
		String path = location.replace("\\", "/");
		return FRAMEWORK_PATHS.stream().anyMatch(path::contains);
	}

	private static boolean isCompilationMetadata(JavaClass javaClass) {
		String name = javaClass.getName();
		String simpleName = name.substring(name.lastIndexOf('.') + 1);
		return "package-info".equals(simpleName) || "module-info".equals(simpleName);
	}
	// </editor-fold>
}
