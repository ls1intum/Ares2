package de.tum.cit.ase.ares.api.policy;

/**
 * This class represents the thread creation part of the security policy.
 */
public record ThreadCreation(
        /**
         * The class from which the students are allowed to create one thread.
         */
        String iAllowTheStudentsToCreateOneThreadFromTheFollowingClass
) {
}
