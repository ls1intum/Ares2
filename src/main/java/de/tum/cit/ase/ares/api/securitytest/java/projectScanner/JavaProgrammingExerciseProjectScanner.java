package de.tum.cit.ase.ares.api.securitytest.java.projectScanner;

import javax.annotation.Nonnull;

import de.tum.cit.ase.ares.api.buildtoolconfiguration.BuildToolConfiguration;

public class JavaProgrammingExerciseProjectScanner extends JavaProjectScanner {
	public JavaProgrammingExerciseProjectScanner() {
		super();
	}

	public JavaProgrammingExerciseProjectScanner(BuildToolConfiguration buildConfiguration) {
		super(buildConfiguration);
	}

	// <editor-fold desc="TUM-specific scan defaults">

	/**
	 * TUM-specific default package, used by the inherited
	 * {@link JavaProjectScanner#scanForPackageName()} when the project itself
	 * declares no package. Overriding the {@code protected} default (rather than
	 * re-implementing {@code scanForPackageName}) lets the parent's polymorphic
	 * fallback pick this up.
	 *
	 * @since 2.0.0
	 * @author Markus Paulsen
	 * @return the TUM default package name
	 */
	@Override
	@Nonnull
	protected String getDefaultPackage() {
		return "de.tum.cit.ase";
	}

	/**
	 * TUM-specific default main class, used by the inherited
	 * {@link JavaProjectScanner#scanForMainClassInPackage()} when no main class is
	 * detected.
	 *
	 * @since 2.0.0
	 * @author Markus Paulsen
	 * @return the TUM default main class name
	 */
	@Override
	@Nonnull
	protected String getDefaultMainClass() {
		return "Main";
	}
	// </editor-fold>
}
