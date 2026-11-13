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

import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import de.tum.cit.ase.ares.api.aop.AOPMode;
import de.tum.cit.ase.ares.api.architecture.ArchitectureMode;
import de.tum.cit.ase.ares.api.buildtoolconfiguration.BuildMode;
import de.tum.cit.ase.ares.api.policy.policySubComponents.OutputConfiguration;
import de.tum.cit.ase.ares.api.policy.policySubComponents.StrictTimeoutConfiguration;
import de.tum.cit.ase.ares.api.policy.policySubComponents.TestBehaviorConfiguration;

/**
 * Checks that generating a precompile exercise refuses the strict timeout and
 * output categories, since no generated code applies them, and writes nothing.
 */
class JavaWriterTestBehaviorRefusalTest {

	/** The exercise project the writer is confined to. */
	@TempDir
	Path tempDir;

	/**
	 * Each category, alone or together, is refused in both languages before any
	 * file is written or any architecture or AOP output is prepared.
	 *
	 * @param configuration the refused configuration.
	 * @param named         the category names the refusal must contain.
	 * @throws IOException if the project cannot be set up or listed
	 */
	@ParameterizedTest
	@MethodSource("refused")
	void theCategoryIsRefusedBeforeAnythingIsWritten(TestBehaviorConfiguration configuration, List<String> named)
			throws IOException {
		Files.writeString(tempDir.resolve("pom.xml"), "<project/>");
		ArchitectureMode architectureMode = mock(ArchitectureMode.class);
		AOPMode aopMode = mock(AOPMode.class);
		Locale previous = Locale.getDefault(Locale.Category.DISPLAY);
		try {
			for (Locale locale : List.of(Locale.ENGLISH, Locale.GERMAN)) {
				Locale.setDefault(Locale.Category.DISPLAY, locale);
				assertThatThrownBy(() -> new JavaWriter(tempDir).writeTestCases(BuildMode.MAVEN, architectureMode,
						aopMode, List.of(), List.of(), List.of(), "com.example", "Main", List.of(), List.of(),
						List.of(), configuration, tempDir.resolve("src/test/java")))
								.isInstanceOf(SecurityException.class)
								.hasMessageContaining(
										locale == Locale.GERMAN ? "Precompile-Übung" : "precompile exercise")
								.satisfies(failure -> assertThat(failure.getMessage()).contains(named));
			}
		} finally {
			Locale.setDefault(Locale.Category.DISPLAY, previous);
		}
		verifyNoInteractions(architectureMode, aopMode);
		try (Stream<Path> files = Files.walk(tempDir)) {
			assertThat(files.filter(Files::isRegularFile)).containsExactly(tempDir.resolve("pom.xml"));
		}
	}

	/**
	 * The refused configurations and the category names each refusal names.
	 *
	 * @return the cases.
	 */
	static Stream<org.junit.jupiter.params.provider.Arguments> refused() {
		StrictTimeoutConfiguration timeout = StrictTimeoutConfiguration.builder().theTimeoutIs("2s").build();
		OutputConfiguration output = OutputConfiguration.builder().theOutputIsMirrored(true).build();
		return Stream.of(
				org.junit.jupiter.params.provider.Arguments.of(
						TestBehaviorConfiguration.builder().regardingTheStrictTimeoutForTheTestCases(timeout).build(),
						List.of("regardingTheStrictTimeoutForTheTestCases")),
				org.junit.jupiter.params.provider.Arguments.of(
						TestBehaviorConfiguration.builder().regardingTheOutputOfTheTestCases(output).build(),
						List.of("regardingTheOutputOfTheTestCases")),
				org.junit.jupiter.params.provider.Arguments.of(
						TestBehaviorConfiguration.builder().regardingTheStrictTimeoutForTheTestCases(timeout)
								.regardingTheOutputOfTheTestCases(output).build(),
						List.of("regardingTheStrictTimeoutForTheTestCases", "regardingTheOutputOfTheTestCases")));
	}
}
