package de.tum.cit.ase.ares.api.securitytest.java.writer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import de.tum.cit.ase.ares.api.aop.AOPMode;
import de.tum.cit.ase.ares.api.architecture.ArchitectureMode;
import de.tum.cit.ase.ares.api.buildtoolconfiguration.BuildMode;
import de.tum.cit.ase.ares.api.policy.policySubComponents.TestBehaviorConfiguration;
import de.tum.cit.ase.ares.api.policy.policySubComponents.VisibilityAndScheduleConfiguration;

/**
 * Checks that generating a precompile exercise refuses the visibility and
 * schedule category, since no generated code applies it, and writes nothing.
 */
class JavaWriterVisibilityRefusalTest {

	/** The exercise project the writer is confined to. */
	@TempDir
	Path tempDir;

	/**
	 * The category is refused in both languages before any file is written or any
	 * architecture or AOP output is prepared.
	 *
	 * @throws IOException if the project cannot be set up or listed
	 */
	@Test
	void theVisibilityCategoryIsRefusedBeforeAnythingIsWritten() throws IOException {
		Files.writeString(tempDir.resolve("pom.xml"), "<project/>");
		ArchitectureMode architectureMode = mock(ArchitectureMode.class);
		AOPMode aopMode = mock(AOPMode.class);
		TestBehaviorConfiguration configuration = TestBehaviorConfiguration.builder()
				.regardingTheVisibilityAndScheduleOfTheTestCases(VisibilityAndScheduleConfiguration.builder()
						.theDeadlineIs("2000-01-01 00:00 UTC").testsNotListedHereAreHidden(false).build())
				.build();
		Locale previous = Locale.getDefault();
		try {
			for (Locale locale : List.of(Locale.ENGLISH, Locale.GERMAN)) {
				Locale.setDefault(locale);
				assertThatThrownBy(() -> new JavaWriter(tempDir).writeTestCases(BuildMode.MAVEN, architectureMode,
						aopMode, List.of(), List.of(), List.of(), "com.example", "Main", List.of(), List.of(),
						List.of(), configuration, tempDir.resolve("src/test/java")))
								.isInstanceOf(SecurityException.class)
								.hasMessageContaining("regardingTheVisibilityAndScheduleOfTheTestCases")
								.hasMessageContaining(
										locale == Locale.GERMAN ? "Precompile-Übung" : "precompile exercise");
			}
		} finally {
			Locale.setDefault(previous);
		}
		verifyNoInteractions(architectureMode, aopMode);
		try (Stream<Path> files = Files.walk(tempDir)) {
			assertThat(files.filter(Files::isRegularFile)).containsExactly(tempDir.resolve("pom.xml"));
		}
	}
}
