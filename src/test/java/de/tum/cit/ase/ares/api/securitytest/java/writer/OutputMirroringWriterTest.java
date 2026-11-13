package de.tum.cit.ase.ares.api.securitytest.java.writer;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.function.UnaryOperator;

import org.junit.jupiter.api.Test;

import de.tum.cit.ase.ares.api.buildtoolconfiguration.BuildToolConfiguration;
import de.tum.cit.ase.ares.api.policy.policySubComponents.InputOutputManagerKind;
import de.tum.cit.ase.ares.api.policy.policySubComponents.OutputConfiguration;
import de.tum.cit.ase.ares.api.policy.policySubComponents.TestBehaviorConfiguration;

/**
 * Checks what the output-mirroring writer generates for a precompile exercise,
 * and that it removes exactly that again.
 */
class OutputMirroringWriterTest extends GeneratedHookWriterProject {

	/** Without the setting, nothing is generated or registered. */
	@Test
	void writesNothingWithoutTheSetting() {
		GeneratedHookFiles.Contribution generated = writer(null).write(TestBehaviorConfiguration.builder().build(),
				PACKAGE, testFolder);

		assertThat(generated.written()).isEmpty();
		assertThat(generated.jupiterHooks()).isEmpty();
		assertThat(source(OutputMirroringSources.JUPITER_HOOK)).doesNotExist();
	}

	/**
	 * With the setting, the extension and its sentinel are written and the
	 * extension registered.
	 *
	 * @throws IOException if a written file cannot be read
	 */
	@Test
	void writesTheExtensionAndItsSentinel() throws IOException {
		GeneratedHookFiles.Contribution generated = writer(null).write(configured(), PACKAGE, testFolder);

		assertThat(generated.jupiterHooks()).containsExactly(OutputMirroringSources.JUPITER_HOOK);
		assertThat(source(OutputMirroringSources.JUPITER_HOOK)).content()
				.contains("implements BeforeEachCallback, AfterEachCallback")
				.contains("com.example.ares.api.localization.Messages.localized(\"output_tester.output_maxExceeded\"")
				.contains("REGARDING_THE_OUTPUT_THE_MAXIMUM_NUMBER_OF_OUTPUT_CHARACTERS");
		assertThat(source(OutputMirroringSources.JUPITER_SENTINEL)).exists();
	}

	/**
	 * Generating twice writes the same files.
	 *
	 * @throws IOException if a written file cannot be read
	 */
	@Test
	void aSecondRunWritesTheSameFiles() throws IOException {
		writer(null).write(configured(), PACKAGE, testFolder);
		String hook = Files.readString(source(OutputMirroringSources.JUPITER_HOOK));

		writer(null).write(configured(), PACKAGE, testFolder);

		assertThat(Files.readString(source(OutputMirroringSources.JUPITER_HOOK))).isEqualTo(hook);
	}

	/**
	 * Removing the setting removes the extension and its sentinel, source and
	 * compiled.
	 *
	 * @throws IOException if a file cannot be written
	 */
	@Test
	void removingTheSettingRemovesTheHook() throws IOException {
		BuildToolConfiguration layout = mavenLayout();
		writer(layout).write(configured(), PACKAGE, testFolder);
		Path compiled = projectRoot
				.resolve("target/test-classes/de/tum/cit/ase/ares/generated/GeneratedOutputMirroring.class");
		Files.createDirectories(compiled.getParent());
		Files.write(compiled, new byte[] { (byte) 0xCA, (byte) 0xFE });

		writer(layout).write(TestBehaviorConfiguration.builder().build(), PACKAGE, testFolder);

		assertThat(source(OutputMirroringSources.JUPITER_HOOK)).doesNotExist();
		assertThat(source(OutputMirroringSources.JUPITER_SENTINEL)).doesNotExist();
		assertThat(compiled).doesNotExist();
	}

	/** With NONE, no output hook is written, and an earlier one is removed. */
	@Test
	void noneWritesNoOutputHook() {
		writer(null).write(configured(), PACKAGE, testFolder);
		GeneratedHookFiles.Contribution generated = writer(null).write(
				TestBehaviorConfiguration.builder()
						.regardingTheOutputOfTheTestCases(OutputConfiguration.builder()
								.theInputAndOutputAreManagedBy(InputOutputManagerKind.NONE).build())
						.build(),
				PACKAGE, testFolder);
		assertThat(generated.written()).isEmpty();
		assertThat(testFolder.resolve("de/tum/cit/ase/ares/generated/" + OutputMirroringSources.JUPITER_HOOK + ".java"))
				.doesNotExist();
	}

	/**
	 * CUSTOM stops the generator in both languages, since precompile has no
	 * IOManager.
	 */
	@Test
	void customStopsTheGenerator() {
		java.util.Locale previous = java.util.Locale.getDefault();
		try {
			for (java.util.Locale locale : List.of(java.util.Locale.ENGLISH, java.util.Locale.GERMAN)) {
				java.util.Locale.setDefault(locale);
				org.assertj.core.api.Assertions.assertThatThrownBy(() -> writer(null).write(
						TestBehaviorConfiguration.builder()
								.regardingTheOutputOfTheTestCases(OutputConfiguration.builder()
										.theInputAndOutputAreManagedBy(InputOutputManagerKind.CUSTOM)
										.theCustomInputAndOutputManagerIs("org.example.Manager").build())
								.build(),
						PACKAGE, testFolder)).isInstanceOf(SecurityException.class).hasMessageContaining("CUSTOM");
			}
		} finally {
			java.util.Locale.setDefault(previous);
		}
	}

	/**
	 * The writer for this test's exercise, confining nothing.
	 *
	 * @param layout the build layout, or null.
	 * @return the writer
	 */
	private OutputMirroringWriter writer(BuildToolConfiguration layout) {
		return new OutputMirroringWriter(new GeneratedHookFiles(projectRoot, layout, UnaryOperator.identity()));
	}

	/**
	 * A configuration with the category set.
	 *
	 * @return the configuration
	 */
	private static TestBehaviorConfiguration configured() {
		return TestBehaviorConfiguration.builder().regardingTheOutputOfTheTestCases(
				OutputConfiguration.builder().theMaximumNumberOfOutputCharactersIs(10L).build()).build();
	}
}
