package de.tum.cit.ase.ares.api.policy.policySubComponents;


/**
 * Enumerates the supported programming language configurations.
 *
 * <p>Description: Defines various Java project configurations, including build tools and aspect-oriented programming frameworks.
 *
 * <p>Design Rationale: This enumeration provides a standardised set of configurations for supervised code execution.
 *
 * @since 2.0.0
 * @author Markus Paulsen
 */
public enum ProgrammingLanguageConfiguration {
    JAVA_USING_MAVEN_ARCHUNIT_AND_ASPECTJ,
    JAVA_USING_MAVEN_ARCHUNIT_AND_INSTRUMENTATION,
    JAVA_USING_MAVEN_WALA_AND_ASPECTJ,
    JAVA_USING_MAVEN_WALA_AND_INSTRUMENTATION,
    JAVA_USING_GRADLE_ARCHUNIT_AND_ASPECTJ,
    JAVA_USING_GRADLE_ARCHUNIT_AND_INSTRUMENTATION,
    JAVA_USING_GRADLE_WALA_AND_ASPECTJ,
    JAVA_USING_GRADLE_WALA_AND_INSTRUMENTATION
}
