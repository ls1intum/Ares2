package de.tum.cit.ase.ares.api.securitytest.java.creator;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import de.tum.cit.ase.ares.api.aop.AOPMode;
import de.tum.cit.ase.ares.api.aop.AOPTestCase;
import de.tum.cit.ase.ares.api.architecture.ArchitectureMode;
import de.tum.cit.ase.ares.api.architecture.ArchitectureTestCase;
import de.tum.cit.ase.ares.api.buildtoolconfiguration.BuildMode;
import de.tum.cit.ase.ares.api.phobos.PhobosTestCase;
import de.tum.cit.ase.ares.api.policy.policySubComponents.ResourceAccesses;

/**
 * The {@link Creator} contract Ares 2.1.2 published, still satisfiable.
 * <p>
 * Which of the two overloads is abstract decides who keeps working. Adding the
 * scope-aware one as the abstract method and demoting the released one to a
 * bridge served callers and nobody else: a class written against the released
 * signature stopped being a complete implementation, so it no longer compiled,
 * and one already compiled lacked the new descriptor and could be reached
 * through {@code AbstractMethodError} from Ares' own call site.
 * <p>
 * This test is the contract. {@code LegacyCreator} implements only what 2.1.2
 * asked for, so the fact that this file compiles is half the assertion, and the
 * other half is that Ares' own call, which always passes the scope, arrives
 * there rather than failing.
 */
class ReleasedCreatorContractTest {

	@Test
	@DisplayName("An implementation of the released signature alone is still complete")
	void anImplementationOfTheReleasedSignatureIsStillComplete() {
		LegacyCreator legacy = new LegacyCreator();

		// The overload Ares itself calls. Reaching the released method means the
		// default supplied the missing parameter by dropping it, which is what such
		// an implementation did before that parameter existed.
		assertDoesNotThrow(() -> callAsAresDoes(legacy),
				"a class written against the released signature must remain usable");

		assertEquals(1, legacy.calls, "the call must arrive at the released method rather than vanish");
	}

	private static void callAsAresDoes(Creator creator) {
		creator.createTestCases(BuildMode.MAVEN, ArchitectureMode.ARCHUNIT, AOPMode.ASPECTJ, List.of(), List.of(),
				List.of(), "de.tum.cit.aet", "Main", new ArrayList<>(), new ArrayList<>(), new ArrayList<>(),
				ResourceAccesses.createRestrictive(), Path.of("."), true);
	}

	/**
	 * A {@link Creator} as it could have been written against 2.1.2, implementing
	 * the released overload and nothing else.
	 */
	@SuppressWarnings({ "deprecation", "removal" })
	private static final class LegacyCreator implements Creator {

		private int calls;

		@Override
		public void createTestCases(@Nonnull BuildMode buildMode, @Nonnull ArchitectureMode architectureMode,
				@Nonnull AOPMode aopMode, @Nonnull List<String> essentialPackages,
				@Nonnull List<String> essentialClasses, @Nonnull List<String> testClasses, @Nonnull String packageName,
				@Nonnull String mainClassInPackageName, @Nonnull List<ArchitectureTestCase> architectureTestCases,
				@Nonnull List<AOPTestCase> aopTestCases, @Nonnull List<PhobosTestCase> phobosTestCases,
				@Nonnull ResourceAccesses resourceAccesses, @Nonnull Path projectPath) {
			calls++;
		}
	}
}
