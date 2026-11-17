package de.tum.cit.ase.ares.api.aop.java.aspectj;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import javax.annotation.Nonnull;

import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaClasses;

import de.tum.cit.ase.ares.api.localization.Messages;

/**
 * Refuses an AspectJ run whose supervised classes were built without the Ares
 * aspects woven in, since nothing would check them while they run. It is to
 * AspectJ what the refusal of a missing agent is to instrumentation.
 */
public final class JavaAspectJWeavingCheck {

	/**
	 * The binary name of the marker the Ares aspects put on every class they are
	 * woven into. Compared by name, because the type itself is not public.
	 */
	@Nonnull
	public static final String MARKER = "de.tum.cit.ase.ares.api.aop.java.aspectj.adviceandpointcut.JavaAspectJWoven";

	/**
	 * Simple names of the class files that describe a package or a module. They
	 * hold no code that could run, so they are not checked.
	 */
	@Nonnull
	private static final Set<String> DESCRIPTOR_NAMES = Set.of("package-info", "module-info");

	/** Prevents creating instances of this utility class. */
	private JavaAspectJWeavingCheck() {
		throw new SecurityException(
				Messages.localized("security.general.utility.initialization", "JavaAspectJWeavingCheck"));
	}

	/**
	 * Refuses when a supervised class lacks the marker. Exempted classes, such as
	 * the test classes, are not checked, because enforcement skips them anyway.
	 *
	 * @param supervisedClasses  the classes read from the supervised class path
	 * @param exemptedClassNames the exact names of the exempted classes
	 * @throws SecurityException naming how many classes lack the marker and the
	 *                           first of them by name
	 */
	public static void requireWoven(@Nonnull JavaClasses supervisedClasses,
			@Nonnull Collection<String> exemptedClassNames) {
		List<String> unwoven = unwovenClassNames(supervisedClasses, exemptedClassNames);
		if (!unwoven.isEmpty()) {
			throw new SecurityException(
					Messages.localized("security.aspectj.not.woven", unwoven.size(), unwoven.get(0)));
		}
	}

	/**
	 * Lists, sorted, the supervised classes that are neither exempted nor a package
	 * or module descriptor and do not carry the marker.
	 *
	 * @param supervisedClasses  the classes read from the supervised class path
	 * @param exemptedClassNames the exact names of the exempted classes
	 * @return the names of the classes built without the Ares aspects
	 */
	@Nonnull
	static List<String> unwovenClassNames(@Nonnull JavaClasses supervisedClasses,
			@Nonnull Collection<String> exemptedClassNames) {
		return supervisedClasses.stream().filter(javaClass -> !DESCRIPTOR_NAMES.contains(javaClass.getSimpleName()))
				.filter(javaClass -> !exemptedClassNames.contains(javaClass.getName()))
				.filter(javaClass -> !javaClass.isAnnotatedWith(MARKER)).map(JavaClass::getName).sorted().toList();
	}
}
