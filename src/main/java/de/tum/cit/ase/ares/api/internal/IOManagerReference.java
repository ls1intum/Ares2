package de.tum.cit.ase.ares.api.internal;

import java.io.IOException;
import java.lang.reflect.Modifier;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.CodeSource;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

import de.tum.cit.ase.ares.api.io.IOManager;
import de.tum.cit.ase.ares.api.localization.Messages;
import de.tum.cit.ase.ares.api.policy.policySubComponents.SupervisedCode;

/**
 * Checks the I/O manager a policy names before Ares ever creates it. Only the
 * instructor's own test classes qualify: the class must be listed as a test
 * class or nested in one, must not come from where the student code comes from,
 * and must be a concrete {@link IOManager} with a public constructor without
 * parameters.
 */
@API(status = Status.INTERNAL)
public final class IOManagerReference {

	/** Not instantiable. */
	private IOManagerReference() {
	}

	/**
	 * Loads and checks the I/O manager a policy names, failing closed on any doubt.
	 *
	 * @param className      the manager's canonical class name.
	 * @param supervisedCode the policy part naming the test classes and package.
	 * @param loader         the loader of the test classes.
	 * @return the checked manager class.
	 * @throws SecurityException naming the reason the class is refused.
	 */
	@SuppressWarnings("unchecked")
	public static Class<? extends IOManager<?>> resolve(String className, SupervisedCode supervisedCode,
			ClassLoader loader) {
		Class<?> manager = ConfigurationUtils.loadByCanonicalName(className, loader)
				.orElseThrow(() -> new SecurityException(
						Messages.localized("policy.behavior.io.manager.custom.not.loadable", className)));
		if (!isListedOrNestedInListed(manager, supervisedCode.theFollowingClassesAreTestClasses())) {
			throw new SecurityException(
					Messages.localized("policy.behavior.io.manager.custom.not.test.class", className));
		}
		Optional<String> managerRoot = rootOf(manager);
		Set<String> studentRoots = studentRoots(supervisedCode, loader);
		if (managerRoot.isEmpty() || studentRoots.isEmpty()) {
			throw new SecurityException(
					Messages.localized("policy.behavior.io.manager.custom.no.reference", className));
		}
		if (studentRoots.contains(managerRoot.get())) {
			throw new SecurityException(
					Messages.localized("policy.behavior.io.manager.custom.student.origin", className));
		}
		if (!IOManager.class.isAssignableFrom(manager) || manager.isInterface()
				|| Modifier.isAbstract(manager.getModifiers()) || !hasPublicNoArgumentConstructor(manager)) {
			throw new SecurityException(Messages.localized("policy.behavior.io.manager.custom.shape", className));
		}
		return (Class<? extends IOManager<?>>) manager;
	}

	/**
	 * Returns whether a class or one of its enclosing classes is a listed test
	 * class.
	 *
	 * @param type        the class.
	 * @param testClasses the listed test classes.
	 * @return whether it qualifies.
	 */
	private static boolean isListedOrNestedInListed(Class<?> type, List<String> testClasses) {
		for (Class<?> current = type; current != null; current = current.getEnclosingClass()) {
			if (current.getCanonicalName() != null && testClasses.contains(current.getCanonicalName())) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Returns the places the student code is loaded from: the main class's, or
	 * every class of the supervised package that is not a test class.
	 *
	 * @param supervisedCode the policy part naming the package and main class.
	 * @param loader         the loader of the test classes.
	 * @return the roots, as normalised text; empty when none can be found.
	 */
	private static Set<String> studentRoots(SupervisedCode supervisedCode, ClassLoader loader) {
		String packageName = supervisedCode.theSupervisedCodeUsesTheFollowingPackage();
		if (packageName == null || packageName.isBlank()) {
			return Set.of();
		}
		String mainClass = supervisedCode.theMainClassInsideThisPackageIs();
		if (mainClass != null && !mainClass.isBlank()) {
			return ConfigurationUtils.loadByCanonicalName(packageName + "." + mainClass, loader)
					.flatMap(IOManagerReference::rootOf).map(Set::of).orElse(Set.of());
		}
		Set<String> roots = new HashSet<>();
		for (String candidate : classesInPackage(packageName, loader)) {
			if (!supervisedCode.theFollowingClassesAreTestClasses().contains(candidate)) {
				ConfigurationUtils.loadByCanonicalName(candidate, loader).flatMap(IOManagerReference::rootOf)
						.ifPresent(roots::add);
			}
		}
		return roots;
	}

	/**
	 * Lists the top-level classes directly in a package, from every directory the
	 * loader serves it from.
	 *
	 * @param packageName the package.
	 * @param loader      the loader.
	 * @return the class names.
	 */
	private static List<String> classesInPackage(String packageName, ClassLoader loader) {
		List<String> names = new ArrayList<>();
		try {
			Enumeration<URL> directories = loader.getResources(packageName.replace('.', '/'));
			while (directories.hasMoreElements()) {
				URL directory = directories.nextElement();
				if (!"file".equals(directory.getProtocol())) {
					continue;
				}
				try (Stream<Path> files = Files.list(Path.of(directory.toURI()))) {
					files.map(Path::getFileName).filter(java.util.Objects::nonNull).map(Path::toString)
							.filter(name -> name.endsWith(".class") && !name.contains("$"))
							.map(name -> packageName + "." + name.substring(0, name.length() - ".class".length()))
							.forEach(names::add);
				}
			}
		} catch (IOException | URISyntaxException unreadable) {
			return List.of();
		}
		return names;
	}

	/**
	 * Returns where a class was loaded from, normalised for comparison.
	 *
	 * @param type the class.
	 * @return the location; empty when unknown.
	 */
	private static Optional<String> rootOf(Class<?> type) {
		CodeSource source = type.getProtectionDomain().getCodeSource();
		if (source == null || source.getLocation() == null) {
			return Optional.empty();
		}
		try {
			URI location = source.getLocation().toURI();
			return Optional
					.of("file".equals(location.getScheme()) ? Path.of(location).toAbsolutePath().normalize().toString()
							: location.normalize().toString());
		} catch (URISyntaxException | IllegalArgumentException unreadable) {
			return Optional.empty();
		}
	}

	/**
	 * Returns whether a class has a public constructor without parameters.
	 *
	 * @param type the class.
	 * @return whether it has one.
	 */
	private static boolean hasPublicNoArgumentConstructor(Class<?> type) {
		try {
			return Modifier.isPublic(type.getConstructor().getModifiers()) && Modifier.isPublic(type.getModifiers());
		} catch (NoSuchMethodException none) {
			return false;
		}
	}
}
