package de.tum.cit.ase.ares.api.policy.policySubComponents;

import javax.annotation.Nonnull;

/**
 * Enumerates the supported programming language configurations.
 * <p>
 * Description: Each name gives the build tool (Maven or Gradle), the analyser
 * that checks the code before it runs (ArchUnit or WALA) and how it is checked
 * while it runs (AspectJ or instrumentation). A policy file names one.
 * <p>
 * Design Rationale: Naming the whole combination at once stops the three
 * choices being set apart.
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
	JAVA_USING_GRADLE_WALA_AND_INSTRUMENTATION;

	/**
	 * Returns the name-validation rules for the language this configuration
	 * selects.
	 * <p>
	 * This is the one place, together with the language-named components it
	 * returns, where a configuration is mapped to a language's name rules. A
	 * further language adds its own case here; the switch has no default, so a new
	 * enum value forces that decision at compile time.
	 *
	 * @since 2.1.0
	 * @author Markus Paulsen
	 * @return the non-null name-validation rules for this configuration's language.
	 */
	@Nonnull
	public LanguageNameRules nameRules() {
		return switch (this) {
		case JAVA_USING_MAVEN_ARCHUNIT_AND_ASPECTJ, JAVA_USING_MAVEN_ARCHUNIT_AND_INSTRUMENTATION, JAVA_USING_MAVEN_WALA_AND_ASPECTJ, JAVA_USING_MAVEN_WALA_AND_INSTRUMENTATION, JAVA_USING_GRADLE_ARCHUNIT_AND_ASPECTJ, JAVA_USING_GRADLE_ARCHUNIT_AND_INSTRUMENTATION, JAVA_USING_GRADLE_WALA_AND_ASPECTJ, JAVA_USING_GRADLE_WALA_AND_INSTRUMENTATION -> JavaNameRules.INSTANCE;
		};
	}
}
