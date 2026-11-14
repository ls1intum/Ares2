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
 * parameters. Any student class whose origin cannot be read refuses the
 * manager.
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
		Set<String> studentRoots = studentRoots(supervisedCode, loader, className);
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
	 * Returns the places the student code is loaded from. With a main class, that
	 * is the main class's place: both build tools compile all student code into one
	 * output folder. Without one, it is the place of every class of the supervised
	 * package and its subpackages that is not a listed test class or nested in one.
	 *
	 * @param supervisedCode the policy part naming the package and main class.
	 * @param loader         the loader of the test classes.
	 * @param managerName    the manager's name, for the refusal.
	 * @return the roots, as normalised text; empty when no student class exists.
	 * @throws SecurityException when a student class's origin cannot be read.
	 */
	private static Set<String> studentRoots(SupervisedCode supervisedCode, ClassLoader loader, String managerName) {
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
		for (String candidate : classesBelowPackage(packageName, loader, managerName)) {
			Class<?> type = ConfigurationUtils.loadByCanonicalName(candidate, loader)
					.orElseThrow(() -> unknownOrigin(managerName, candidate));
			if (!isListedOrNestedInListed(type, supervisedCode.theFollowingClassesAreTestClasses())) {
				roots.add(rootOf(type).orElseThrow(() -> unknownOrigin(managerName, candidate)));
			}
		}
		return roots;
	}

	/**
	 * Lists every class in a package and its subpackages, nested classes included,
	 * from every folder the loader serves the package from.
	 *
	 * @param packageName the package.
	 * @param loader      the loader.
	 * @param managerName the manager's name, for the refusal.
	 * @return the binary class names.
	 * @throws SecurityException when the package comes from somewhere other than a
	 *                           folder, or a folder cannot be read.
	 */
	private static List<String> classesBelowPackage(String packageName, ClassLoader loader, String managerName) {
		List<String> names = new ArrayList<>();
		try {
			Enumeration<URL> directories = loader.getResources(packageName.replace('.', '/'));
			while (directories.hasMoreElements()) {
				URL directory = directories.nextElement();
				if (!"file".equals(directory.getProtocol())) {
					throw unknownOrigin(managerName, directory.toString());
				}
				Path root = Path.of(directory.toURI());
				try (Stream<Path> files = Files.walk(root)) {
					files.filter(file -> isClassFile(file)).map(file -> binaryName(packageName, root.relativize(file)))
							.forEach(names::add);
				}
			}
		} catch (IOException | URISyntaxException | IllegalArgumentException unreadable) {
			throw unknownOrigin(managerName, packageName);
		}
		return names;
	}

	/**
	 * Returns whether a path is a class file, other than a module descriptor.
	 *
	 * @param file the path.
	 * @return whether it names a class.
	 */
	private static boolean isClassFile(Path file) {
		String name = String.valueOf(file.getFileName());
		return name.endsWith(".class") && !"module-info.class".equals(name) && Files.isRegularFile(file);
	}

	/**
	 * Returns the binary name of a class file found below a package's folder.
	 *
	 * @param packageName the package.
	 * @param relative    the class file's path relative to the package's folder.
	 * @return the binary name, such as {@code org.example.sub.Outer$Inner}.
	 */
	private static String binaryName(String packageName, Path relative) {
		String path = relative.toString().replace('\\', '/');
		return packageName + "." + path.substring(0, path.length() - ".class".length()).replace('/', '.');
	}

	/**
	 * The refusal for a student class, or a package location, whose origin cannot
	 * be read.
	 *
	 * @param managerName the manager's name.
	 * @param what        the class or location.
	 * @return the exception to throw.
	 */
	private static SecurityException unknownOrigin(String managerName, String what) {
		return new SecurityException(
				Messages.localized("policy.behavior.io.manager.custom.unknown.origin", managerName, what));
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
