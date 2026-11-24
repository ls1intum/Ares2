package de.tum.cit.ase.ares.api.securitytest.java.creator;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import de.tum.cit.ase.ares.api.buildtoolconfiguration.BuildToolConfiguration;
import de.tum.cit.ase.ares.api.localization.Messages;
import de.tum.cit.ase.ares.api.securitytest.ReservedPackageGuard;

/**
 * Checks that the classes Ares analyses statically, those below the withinPath
 * of the policy, and the classes it checks while they run, those of the
 * supervised package, are the same student classes. Where the two differ, one
 * of the two layers would check nothing, so Ares refuses before arming.
 */
final class SupervisedScopeCheck {

	/** The ending of a compiled class file. */
	private static final String CLASS_SUFFIX = ".class";

	/** Prevents creating instances of this utility class. */
	private SupervisedScopeCheck() {
		throw new SecurityException(Messages.localized("security.general.utility.initialization",
				SupervisedScopeCheck.class.getSimpleName()));
	}

	/**
	 * Refuses a scope that holds a class outside the supervised package, as after a
	 * misspelt package, and a production class of the package that lies outside the
	 * scope, as after a withinPath naming a sub-folder or the test output. Exempted
	 * classes, descriptors and Ares' own reserved packages are left out.
	 *
	 * @param scopeClassNames      the classes imported from the scope
	 * @param scope                the folder the scope resolved to
	 * @param productionOutputRoot the whole compiled production output
	 * @param supervisedPackage    the package the runtime checks supervise
	 * @param exemptedClassNames   the exempted classes, by exact name
	 * @throws SecurityException if the scope and the package do not match
	 */
	static void requireScopeToMatchPackage(@Nonnull Collection<String> scopeClassNames, @Nonnull Path scope,
			@Nonnull Path productionOutputRoot, @Nonnull String supervisedPackage,
			@Nonnull Collection<String> exemptedClassNames) {
		if (supervisedPackage.isBlank()) {
			return;
		}
		Set<String> exempted = Set.copyOf(exemptedClassNames);
		scopeClassNames.stream().filter(name -> isChecked(name, exempted) && !isInPackage(name, supervisedPackage))
				.sorted().findFirst().ifPresent(className -> {
					throw new SecurityException(Messages.localized("security.policy.scope.class.outside.package",
							className, supervisedPackage));
				});
		firstPackageClassOutsideScope(scope, productionOutputRoot, supervisedPackage, exempted).ifPresent(className -> {
			throw new SecurityException(Messages.localized("security.policy.scope.package.class.outside", className,
					supervisedPackage, scope));
		});
	}

	/**
	 * Finds a production class of the supervised package whose file lies outside
	 * the scope. Both folders are compared at their real location.
	 *
	 * @param scope                the folder the scope resolved to
	 * @param productionOutputRoot the whole compiled production output
	 * @param supervisedPackage    the supervised package
	 * @param exempted             the exempted classes
	 * @return the first such class, if any
	 * @throws SecurityException if the production output cannot be read
	 */
	@Nonnull
	private static Optional<String> firstPackageClassOutsideScope(@Nonnull Path scope,
			@Nonnull Path productionOutputRoot, @Nonnull String supervisedPackage, @Nonnull Set<String> exempted) {
		if (!Files.isDirectory(productionOutputRoot)) {
			return Optional.empty();
		}
		Path normalisedScope = BuildToolConfiguration.canonicalise(scope);
		Path normalisedRoot = BuildToolConfiguration.canonicalise(productionOutputRoot);
		try (Stream<Path> files = Files.walk(normalisedRoot)) {
			return files.filter(file -> file.toString().endsWith(CLASS_SUFFIX))
					.filter(file -> !file.startsWith(normalisedScope)).map(file -> binaryNameOf(normalisedRoot, file))
					.filter(name -> isChecked(name, exempted) && isInPackage(name, supervisedPackage)).sorted()
					.findFirst();
		} catch (IOException | UncheckedIOException unreadable) {
			throw new SecurityException(
					Messages.localized("security.scope.output.unreadable", normalisedRoot.toString()), unreadable);
		}
	}

	/**
	 * Whether a class takes part in the comparison: not exempted, not a package or
	 * module descriptor and not in a package reserved for Ares and its libraries.
	 *
	 * @param className the binary class name
	 * @param exempted  the exempted classes
	 * @return {@code true} if the class is compared
	 */
	private static boolean isChecked(@Nonnull String className, @Nonnull Set<String> exempted) {
		String simpleName = className.substring(className.lastIndexOf('.') + 1);
		return !exempted.contains(className) && !"package-info".equals(simpleName) && !"module-info".equals(simpleName)
				&& ReservedPackageGuard.reservedPrefixOf(packageOf(className)) == null;
	}

	/**
	 * Whether a class lies in the given package or below it, at a dot.
	 *
	 * @param className   the binary class name
	 * @param packageName the package
	 * @return {@code true} if it does
	 */
	private static boolean isInPackage(@Nonnull String className, @Nonnull String packageName) {
		return className.startsWith(packageName + ".");
	}

	/**
	 * The package of a binary class name.
	 *
	 * @param className the binary class name
	 * @return its package, or {@code null} for the default package
	 */
	@Nullable
	private static String packageOf(@Nonnull String className) {
		int lastDot = className.lastIndexOf('.');
		return lastDot < 0 ? null : className.substring(0, lastDot);
	}

	/**
	 * Turns a class file's path below an output root into its binary class name.
	 *
	 * @param root the output root
	 * @param file a class file below it
	 * @return the binary class name
	 */
	@Nonnull
	private static String binaryNameOf(@Nonnull Path root, @Nonnull Path file) {
		String relative = root.relativize(file).toString().replace('\\', '/');
		return relative.substring(0, relative.length() - CLASS_SUFFIX.length()).replace('/', '.');
	}
}
