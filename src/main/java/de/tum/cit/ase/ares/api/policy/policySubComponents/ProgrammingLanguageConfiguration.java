package de.tum.cit.ase.ares.api.policy.policySubComponents;

/**
 * Enumerates the supported programming language configurations.
 * <p>
 * Description: Each constant fixes the three choices Ares needs before it can
 * supervise a submission, and its name spells them out in order:
 * {@code JAVA_USING_<build tool>_<architecture analyser>_AND_<enforcement
 * mechanism>}.
 * <ul>
 * <li>The build tool, Maven or Gradle, determines how the supervised code is
 * compiled and where its classpath comes from.</li>
 * <li>The architecture analyser, ArchUnit or WALA, determines how the code is
 * examined statically before it runs. ArchUnit inspects declared structure;
 * WALA builds a call graph and so reaches calls that structure alone does not
 * reveal.</li>
 * <li>The enforcement mechanism, AspectJ or instrumentation, determines how the
 * policy is enforced while the code runs. AspectJ weaves advice at compile
 * time; instrumentation attaches a Java agent at run time.</li>
 * </ul>
 * All eight combinations are supported, and a policy names exactly one of them.
 * <p>
 * Design Rationale: Naming the whole combination in a single enumeration keeps
 * the three choices from being set independently, which would allow
 * combinations that were never built or tested. The names are also the literal
 * values a policy file carries, so the set of legal values is visible in one
 * place.
 *
 * @since 2.0.0
 * @author Markus Paulsen
 */
public enum ProgrammingLanguageConfiguration {
	/** Maven, static analysis by ArchUnit, enforcement by AspectJ. */
	JAVA_USING_MAVEN_ARCHUNIT_AND_ASPECTJ,
	/** Maven, static analysis by ArchUnit, enforcement by instrumentation. */
	JAVA_USING_MAVEN_ARCHUNIT_AND_INSTRUMENTATION,
	/** Maven, static analysis by WALA, enforcement by AspectJ. */
	JAVA_USING_MAVEN_WALA_AND_ASPECTJ,
	/** Maven, static analysis by WALA, enforcement by instrumentation. */
	JAVA_USING_MAVEN_WALA_AND_INSTRUMENTATION,
	/** Gradle, static analysis by ArchUnit, enforcement by AspectJ. */
	JAVA_USING_GRADLE_ARCHUNIT_AND_ASPECTJ,
	/** Gradle, static analysis by ArchUnit, enforcement by instrumentation. */
	JAVA_USING_GRADLE_ARCHUNIT_AND_INSTRUMENTATION,
	/** Gradle, static analysis by WALA, enforcement by AspectJ. */
	JAVA_USING_GRADLE_WALA_AND_ASPECTJ,
	/** Gradle, static analysis by WALA, enforcement by instrumentation. */
	JAVA_USING_GRADLE_WALA_AND_INSTRUMENTATION
}
