package de.tum.cit.ase.ares.api.securitytest.java.creator;

import java.nio.file.Path;
import java.util.List;

import javax.annotation.Nonnull;

import de.tum.cit.ase.ares.api.aop.AOPMode;
import de.tum.cit.ase.ares.api.aop.AOPTestCase;
import de.tum.cit.ase.ares.api.architecture.ArchitectureMode;
import de.tum.cit.ase.ares.api.architecture.ArchitectureTestCase;
import de.tum.cit.ase.ares.api.buildtoolconfiguration.BuildMode;
import de.tum.cit.ase.ares.api.phobos.PhobosTestCase;
import de.tum.cit.ase.ares.api.policy.policySubComponents.ResourceAccesses;

/**
 * Creates security test cases based on security policies.
 * <p>
 * Description: This interface defines the contract for creating security test
 * cases for different programming languages and frameworks.
 * <p>
 * Design Rationale: The Creator interface follows the Strategy design pattern
 * to allow for different implementation strategies for creating security test
 * cases for different programming languages and frameworks.
 *
 * @since 2.0.0
 * @author Markus Paulsen
 * @version 2.0.0
 */
public interface Creator {

	/**
	 * Creates the security test cases based on the security policy.
	 *
	 * @since 2.0.0
	 * @author Markus Paulsen
	 * @param essentialClasses      the list of essential classes; must not be null
	 * @param testClasses           the list of test classes; must not be null
	 * @param architectureTestCases the list to populate with architecture test
	 *                              cases; must not be null
	 * @param aopTestCases          the list to populate with AOP test cases; must
	 *                              not be null
	 * @param resourceAccesses      the resource accesses permitted by the security
	 *                              policy; must not be null
	 * @param projectPath           the path to the project; must not be null
	 */
	/**
	 * The signature released in 2.1.2, and still the one an implementation has to
	 * provide.
	 * <p>
	 * Which of the two overloads is abstract decides who keeps working. Adding the
	 * scope-aware one as the abstract method and demoting this to a bridge served
	 * callers and nobody else: an implementation written against this signature was
	 * then no longer a complete implementation, so it failed to compile, and an
	 * already compiled one lacked the new descriptor and could be reached through
	 * {@code AbstractMethodError}. Leaving this abstract keeps the released
	 * contract exactly what it was.
	 *
	 * @param buildMode              the build tool
	 * @param architectureMode       the architecture analyser
	 * @param aopMode                the enforcement backend
	 * @param essentialPackages      the packages Ares itself needs
	 * @param essentialClasses       the classes Ares itself needs
	 * @param testClasses            the test classes
	 * @param packageName            the supervised scope
	 * @param mainClassInPackageName the main class
	 * @param architectureTestCases  the architecture test cases to fill
	 * @param aopTestCases           the AOP test cases to fill
	 * @param phobosTestCases        the Phobos test cases to fill
	 * @param resourceAccesses       the permitted resource accesses
	 * @param projectPath            the project root
	 * @deprecated implement the overload that states whether the supervised scope
	 *             was derived; this one cannot express it
	 */
	@Deprecated(forRemoval = true)
	void createTestCases(
			// TODO Markus: Remove Java from Abstract Class
			@Nonnull BuildMode buildMode, @Nonnull ArchitectureMode architectureMode, @Nonnull AOPMode aopMode,
			@Nonnull List<String> essentialPackages, @Nonnull List<String> essentialClasses,
			@Nonnull List<String> testClasses, @Nonnull String packageName, @Nonnull String mainClassInPackageName,
			@Nonnull List<ArchitectureTestCase> architectureTestCases, @Nonnull List<AOPTestCase> aopTestCases,
			@Nonnull List<PhobosTestCase> phobosTestCases, @Nonnull ResourceAccesses resourceAccesses,
			@Nonnull Path projectPath);

	/**
	 * The same, told whether the supervised scope was derived from the project or
	 * pinned by a policy.
	 * <p>
	 * A {@code default} rather than the abstract method, so that an implementation
	 * predating the parameter stays a complete implementation. What it does then is
	 * the only honest thing available: it drops the parameter and calls the
	 * released overload, which is exactly the behaviour such an implementation had
	 * before the parameter existed.
	 * <p>
	 * An implementation that means to act on the scope overrides this, as
	 * {@code JavaCreator} does, and implements the released overload as a one-line
	 * delegation to it. Ares itself always calls this one.
	 *
	 * @param buildMode                 the build tool
	 * @param architectureMode          the architecture analyser
	 * @param aopMode                   the enforcement backend
	 * @param essentialPackages         the packages Ares itself needs
	 * @param essentialClasses          the classes Ares itself needs
	 * @param testClasses               the test classes
	 * @param packageName               the supervised scope
	 * @param mainClassInPackageName    the main class
	 * @param architectureTestCases     the architecture test cases to fill
	 * @param aopTestCases              the AOP test cases to fill
	 * @param phobosTestCases           the Phobos test cases to fill
	 * @param resourceAccesses          the permitted resource accesses
	 * @param projectPath               the project root
	 * @param supervisedScopeWasDerived whether Ares derived the supervised scope
	 *                                  rather than reading it from a policy
	 */
	@SuppressWarnings("removal")
	default void createTestCases(@Nonnull BuildMode buildMode, @Nonnull ArchitectureMode architectureMode,
			@Nonnull AOPMode aopMode, @Nonnull List<String> essentialPackages, @Nonnull List<String> essentialClasses,
			@Nonnull List<String> testClasses, @Nonnull String packageName, @Nonnull String mainClassInPackageName,
			@Nonnull List<ArchitectureTestCase> architectureTestCases, @Nonnull List<AOPTestCase> aopTestCases,
			@Nonnull List<PhobosTestCase> phobosTestCases, @Nonnull ResourceAccesses resourceAccesses,
			@Nonnull Path projectPath, boolean supervisedScopeWasDerived) {
		createTestCases(buildMode, architectureMode, aopMode, essentialPackages, essentialClasses, testClasses,
				packageName, mainClassInPackageName, architectureTestCases, aopTestCases, phobosTestCases,
				resourceAccesses, projectPath);
	}
}
