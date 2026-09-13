package de.tum.cit.ase.ares.api.securitytest.java.writer;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.file.Path;
import java.util.List;

import javax.annotation.Nonnull;

import org.junit.jupiter.api.Test;

import de.tum.cit.ase.ares.api.aop.AOPMode;
import de.tum.cit.ase.ares.api.aop.java.JavaAOPTestCase;
import de.tum.cit.ase.ares.api.architecture.ArchitectureMode;
import de.tum.cit.ase.ares.api.architecture.java.JavaArchitectureTestCase;
import de.tum.cit.ase.ares.api.buildtoolconfiguration.BuildMode;
import de.tum.cit.ase.ares.api.phobos.JavaPhobosTestCase;
import de.tum.cit.ase.ares.api.policy.policySubComponents.TestBehaviorConfiguration;

class WriterTest {

	/**
	 * Implements only the pre-release abstract method, the shape any implementation
	 * built before {@code testBehaviorConfiguration} existed would have.
	 */
	private static final class LegacyWriter implements Writer {

		private Path lastTestFolderPath;

		@Nonnull
		@Override
		public List<Path> writeTestCases(@Nonnull BuildMode buildMode, @Nonnull ArchitectureMode architectureMode,
				@Nonnull AOPMode aopMode, @Nonnull List<String> essentialPackages,
				@Nonnull List<String> essentialClasses, @Nonnull List<String> testClasses, @Nonnull String packageName,
				@Nonnull String mainClassInPackageName,
				@Nonnull List<JavaArchitectureTestCase> javaArchitectureTestCases,
				@Nonnull List<JavaAOPTestCase> javaAOPTestCases, @Nonnull List<JavaPhobosTestCase> phobosTestCases,
				@Nonnull Path testFolderPath) {
			this.lastTestFolderPath = testFolderPath;
			return List.of(testFolderPath);
		}
	}

	@Test
	void configurationAwareOverloadDefaultsToThePreReleaseMethodWhenNotOverridden() {
		LegacyWriter legacyWriter = new LegacyWriter();
		Path testFolderPath = Path.of("some/test/folder");

		List<Path> result = legacyWriter.writeTestCases(BuildMode.MAVEN, ArchitectureMode.ARCHUNIT, AOPMode.ASPECTJ,
				List.of(), List.of(), List.of(), "com.example", "Main", List.of(), List.of(), List.of(),
				TestBehaviorConfiguration.builder().build(), testFolderPath);

		assertEquals(List.of(testFolderPath), result);
		assertEquals(testFolderPath, legacyWriter.lastTestFolderPath);
	}
}
