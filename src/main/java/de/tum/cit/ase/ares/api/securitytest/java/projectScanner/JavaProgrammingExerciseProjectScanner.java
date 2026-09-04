package de.tum.cit.ase.ares.api.securitytest.java.projectScanner;

import java.util.Objects;

import javax.annotation.Nonnull;

import de.tum.cit.ase.ares.api.buildtoolconfiguration.BuildToolConfiguration;

public class JavaProgrammingExerciseProjectScanner extends JavaProjectScanner {
	public JavaProgrammingExerciseProjectScanner() {
		super();
	}

	public JavaProgrammingExerciseProjectScanner(BuildToolConfiguration buildConfiguration) {
		super(buildConfiguration);
	}

	/**
	 * Rebinds this TUM-specific scanner to the discovered build configuration,
	 * preserving its concrete type. Overrides
	 * {@link JavaProjectScanner#withBuildConfiguration(BuildToolConfiguration)} so
	 * a framework-default {@code JavaProgrammingExerciseProjectScanner} is rebuilt
	 * as one of its own kind rather than as a plain {@link JavaProjectScanner}. A
	 * further subclass is returned unchanged.
	 *
	 * @since 2.1.0
	 * @author Markus Paulsen
	 * @param buildConfiguration the discovered build configuration; must not be
	 *                           null.
	 * @return the scanner bound to the configuration, or this instance when it is a
	 *         further subclass.
	 */
	@Override
	@Nonnull
	public JavaProgrammingExerciseProjectScanner withBuildConfiguration(
			@Nonnull BuildToolConfiguration buildConfiguration) {
		Objects.requireNonNull(buildConfiguration, "buildConfiguration must not be null");
		return getClass() == JavaProgrammingExerciseProjectScanner.class
				? new JavaProgrammingExerciseProjectScanner(buildConfiguration)
				: this;
	}

	// <editor-fold desc="TUM-specific scan defaults">

	/**
	 * TUM-specific default package, used by the inherited
	 * {@link JavaProjectScanner#scanForPackageName()} when the project itself
	 * declares no package. Overriding the {@code protected} default (rather than
	 * re-implementing {@code scanForPackageName}) lets the parent's polymorphic
	 * fallback pick this up.
	 * <p>
	 * The root package the Artemis exercise templates at TUM are generated with,
	 * deliberately not Ares' own {@code de.tum.cit.ase}: that identifies this
	 * library, this is a guess about the supervised project, and the two looking
	 * alike is what makes them easy to conflate.
	 *
	 * @since 2.0.0
	 * @author Markus Paulsen
	 * @return the TUM default package name
	 */
	@Override
	@Nonnull
	protected String getDefaultPackage() {
		return "de.tum.cit.aet";
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
