package de.tum.cit.ase.ares.api.securitytest;

import java.util.List;

import javax.annotation.Nullable;

import de.tum.cit.ase.ares.api.architecture.java.wala.WalaPathClassification;
import de.tum.cit.ase.ares.api.localization.Messages;

/**
 * Rejects supervised (student) code that declares a package under one of Ares's
 * trusted infrastructure prefixes.
 * <p>
 * The static analysers and the runtime AOP both treat those prefixes as trusted
 * (not student code): ArchUnit excludes {@code de.tum.cit.ase.ares.api} from
 * import, WALA classifies the {@link WalaPathClassification#INFRA_PREFIXES} as
 * infrastructure, and the runtime advice ignores stack frames in those
 * packages. A submission that declares such a package would therefore be
 * trusted by name and bypass every check. This guard fails closed before any
 * analysis or execution rather than letting that happen.
 * <p>
 * The reserved set is exactly {@link WalaPathClassification#INFRA_PREFIXES}, so
 * the guard can never drift from the classifier that grants the trust.
 *
 * @since 2.0.0
 * @author Markus Paulsen
 */
public final class ReservedPackageGuard {

	private ReservedPackageGuard() {
		throw new IllegalStateException(
				Messages.localized("security.general.utility.initialization", "ReservedPackageGuard"));
	}

	/**
	 * The package prefixes (each ending in a dot) that student code must not
	 * declare. Identical to the classifier's infrastructure prefixes.
	 */
	private static final List<String> RESERVED_PREFIXES = WalaPathClassification.INFRA_PREFIXES;

	/**
	 * Returns the reserved prefix that the given package falls under, or
	 * {@code null} if the package is not reserved. A {@code null} or blank package
	 * is never reserved (the default package cannot collide with a trusted prefix).
	 *
	 * @since 2.0.0
	 * @author Markus Paulsen
	 * @param packageName the dotted package name to test
	 * @return the matched reserved prefix, or {@code null}
	 */
	@Nullable
	public static String reservedPrefixOf(@Nullable String packageName) {
		if (packageName == null || packageName.isBlank()) {
			return null;
		}
		// Append a dot so a package equal to a prefix without its trailing dot (e.g.
		// "jdk") still matches "jdk.", while a shorter parent (e.g. the pinned
		// "de.tum.cit.ase.ares") does not match the longer "de.tum.cit.ase.ares.api.".
		String normalized = packageName.endsWith(".") ? packageName : packageName + ".";
		for (String reserved : RESERVED_PREFIXES) {
			if (normalized.startsWith(reserved)) {
				return reserved;
			}
		}
		return null;
	}

	/**
	 * Throws a {@link SecurityException} if the given package is reserved.
	 *
	 * @since 2.0.0
	 * @author Markus Paulsen
	 * @param packageName the dotted package name to validate
	 */
	public static void validatePackage(@Nullable String packageName) {
		String reserved = reservedPrefixOf(packageName);
		if (reserved != null) {
			throw new SecurityException(Messages.localized("security.policy.reserved.package", packageName, reserved));
		}
	}

	/**
	 * Validates the declaring package of each fully qualified class name, throwing
	 * a {@link SecurityException} on the first one that is reserved.
	 *
	 * @since 2.0.0
	 * @author Markus Paulsen
	 * @param fullyQualifiedClassNames the binary class names to validate
	 */
	public static void validateClassNames(@Nullable Iterable<String> fullyQualifiedClassNames) {
		if (fullyQualifiedClassNames == null) {
			return;
		}
		for (String className : fullyQualifiedClassNames) {
			if (className == null) {
				continue;
			}
			int lastDot = className.lastIndexOf('.');
			String packageName = lastDot > 0 ? className.substring(0, lastDot) : "";
			validatePackage(packageName);
		}
	}
}
