package de.tum.cit.ase.ares.api.securitytest.java.writer;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.function.UnaryOperator;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import de.tum.cit.ase.ares.api.buildtoolconfiguration.BuildMode;
import de.tum.cit.ase.ares.api.buildtoolconfiguration.BuildToolConfiguration;
import de.tum.cit.ase.ares.api.policy.policySubComponents.OutputMirroringConfiguration;
import de.tum.cit.ase.ares.api.policy.policySubComponents.TestBehaviorConfiguration;

/**
 * Checks what the output-mirroring writer generates for a precompile exercise,
 * and that it removes exactly that again.
 */
class OutputMirroringWriterTest {

	/** The exercise package the copied Ares classes live in. */
	private static final String PACKAGE = "com.example";

	/** The exercise root of each test. */
	@TempDir
	Path projectRoot;

	/** The exercise's test source root. */
	private Path testFolder;

	/**
	 * Creates the exercise's test source folder.
	 *
	 * @throws IOException if it cannot be created
	 */
	@BeforeEach
	void setUp() throws IOException {
		testFolder = Files.createDirectories(projectRoot.resolve("src/test/java"));
	}

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
		assertThat(generated.jqwikHooks()).isEmpty();
		assertThat(source(OutputMirroringSources.JUPITER_HOOK)).content()
				.contains("implements BeforeEachCallback, AfterEachCallback")
				.contains("com.example.ares.api.localization.Messages.localized(\"output_tester.output_maxExceeded\"")
				.contains("REGARDING_OUTPUT_MIRRORING_THE_MAXIMUM_CHARACTER_COUNT_IS");
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
		for (String directory : List.of("src/main/java", "target/classes", "target/test-classes")) {
			Files.createDirectories(projectRoot.resolve(directory));
		}
		Files.writeString(projectRoot.resolve("pom.xml"), "<project/>");
		BuildToolConfiguration layout = new BuildToolConfiguration(BuildMode.MAVEN, projectRoot,
				List.of(projectRoot.resolve("src/main/java")), List.of(testFolder),
				projectRoot.resolve("target/classes"), projectRoot.resolve("target/test-classes"));
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
		return TestBehaviorConfiguration.builder().regardingOutputMirroring(
				OutputMirroringConfiguration.builder().theMaximumCharacterCountIs(10L).build()).build();
	}

	/**
	 * Where a generated class's source belongs.
	 *
	 * @param simpleName the class's simple name.
	 * @return the source path
	 */
	private Path source(String simpleName) {
		return testFolder.resolve("de/tum/cit/ase/ares/generated").resolve(simpleName + ".java");
	}
}
