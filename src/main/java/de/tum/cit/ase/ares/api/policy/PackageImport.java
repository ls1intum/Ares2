package de.tum.cit.ase.ares.api.policy;

/**
 * This class represents the package imports part of the security policy.
 *
 * @author Markus Paulsen
 * @version 2.0.0
 * @since 2.0.0
 */
public record PackageImport(
        /**
         * The package that the students are allowed to import.
         */
        String iAllowTheStudentsToImportTheFollowingPackage
) {
}
