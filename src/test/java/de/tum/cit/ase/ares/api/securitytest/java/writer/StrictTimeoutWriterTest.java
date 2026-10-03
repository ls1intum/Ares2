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
import de.tum.cit.ase.ares.api.policy.policySubComponents.StrictTimeoutsConfiguration;
import de.tum.cit.ase.ares.api.policy.policySubComponents.TestBehaviorConfiguration;

/**
 * Checks what the strict-timeout writer generates for a precompile exercise,
 * and that it removes exactly that again.
 */
class StrictTimeoutWriterTest {

	/** The exercise package the copied Ares classes live in. */
	private static final String PACKAGE = "com.example";

	/** The exercise root of each test. */
	@TempDir
	Path projectRoot;

	/** The exercise's test source root. */
	private Path testFolder;

	/**
	 * Creates the exercise folders and a Gradle build without jqwik.
	 *
	 * @throws IOException if the folders cannot be created
	 */
	@BeforeEach
	void setUp() throws IOException {
		testFolder = Files.createDirectories(projectRoot.resolve("src/test/java"));
		Files.writeString(projectRoot.resolve("build.gradle"), "plugins { id 'java' }");
	}

	/** Without the setting, nothing is generated or registered. */
	@Test
	void writesNothingWithoutTheSetting() {
		GeneratedHookFiles.Contribution generated = writer(null).write(TestBehaviorConfiguration.builder().build(),
				PACKAGE, testFolder);

		assertThat(generated.written()).isEmpty();
		assertThat(generated.jupiterHooks()).isEmpty();
		assertThat(source(StrictTimeoutSources.JUPITER_HOOK)).doesNotExist();
	}

	/**
	 * With the setting, the JUnit interceptor and its sentinel are written and
	 * registered; without jqwik, no jqwik file is written.
	 *
	 * @throws IOException if a written file cannot be read
	 */
	@Test
	void writesTheJupiterInterceptor() throws IOException {
		GeneratedHookFiles.Contribution generated = writer(null).write(configured(), PACKAGE, testFolder);

		assertThat(generated.jupiterHooks()).containsExactly(StrictTimeoutSources.JUPITER_HOOK);
		assertThat(generated.jqwikHooks()).isEmpty();
		assertThat(source(StrictTimeoutSources.JUPITER_HOOK)).content()
				.contains("public final class GeneratedStrictTimeout implements InvocationInterceptor")
				.contains("public static <T> T executeWithTimeout(")
				.contains("com.example.ares.api.localization.Messages.localized(\"timeout.failure_message\"").contains(
						"de.tum.cit.ase.ares.generated.GeneratedTestBehaviorSettings.REGARDING_STRICT_TIMEOUTS_THE_TIMEOUT_IS");
		assertThat(source(StrictTimeoutSources.JUPITER_SENTINEL)).content().contains("JQWIK_HOOK_GENERATED = false;");
		assertThat(source(StrictTimeoutSources.JQWIK_HOOK)).doesNotExist();
	}

	/**
	 * A build naming jqwik also gets the jqwik hook, compiled against jqwik's API
	 * only, and its sentinel.
	 *
	 * @throws IOException if a file cannot be written or read
	 */
	@Test
	void writesTheJqwikHookWhenTheBuildUsesJqwik() throws IOException {
		Files.writeString(projectRoot.resolve("build.gradle"), "dependencies { testImplementation 'net.jqwik:jqwik' }");

		GeneratedHookFiles.Contribution generated = writer(null).write(configured(), PACKAGE, testFolder);

		assertThat(generated.jqwikHooks()).containsExactly(StrictTimeoutSources.JQWIK_HOOK);
		assertThat(source(StrictTimeoutSources.JQWIK_HOOK)).content().contains("implements AroundTryHook")
				.contains("PropagationMode.ALL_DESCENDANTS").doesNotContain("import net.jqwik.engine");
		assertThat(source(StrictTimeoutSources.JQWIK_SENTINEL)).exists();
		assertThat(source(StrictTimeoutSources.JUPITER_SENTINEL)).content().contains("JQWIK_HOOK_GENERATED = true;");
	}

	/**
	 * Generating twice writes the same files.
	 *
	 * @throws IOException if a written file cannot be read
	 */
	@Test
	void aSecondRunWritesTheSameFiles() throws IOException {
		writer(null).write(configured(), PACKAGE, testFolder);
		String hook = Files.readString(source(StrictTimeoutSources.JUPITER_HOOK));

		writer(null).write(configured(), PACKAGE, testFolder);

		assertThat(Files.readString(source(StrictTimeoutSources.JUPITER_HOOK))).isEqualTo(hook);
	}

	/**
	 * Removing the setting removes the hooks, source and compiled.
	 *
	 * @throws IOException if a file cannot be written
	 */
	@Test
	void removingTheSettingRemovesTheHooks() throws IOException {
		for (String directory : List.of("src/main/java", "target/classes", "target/test-classes")) {
			Files.createDirectories(projectRoot.resolve(directory));
		}
		Files.writeString(projectRoot.resolve("pom.xml"), "<project/>");
		BuildToolConfiguration layout = new BuildToolConfiguration(BuildMode.MAVEN, projectRoot,
				List.of(projectRoot.resolve("src/main/java")), List.of(testFolder),
				projectRoot.resolve("target/classes"), projectRoot.resolve("target/test-classes"));
		writer(layout).write(configured(), PACKAGE, testFolder);
		Path compiled = projectRoot
				.resolve("target/test-classes/de/tum/cit/ase/ares/generated/GeneratedStrictTimeout.class");
		Files.createDirectories(compiled.getParent());
		Files.write(compiled, new byte[] { (byte) 0xCA, (byte) 0xFE });

		writer(layout).write(TestBehaviorConfiguration.builder().build(), PACKAGE, testFolder);

		assertThat(source(StrictTimeoutSources.JUPITER_HOOK)).doesNotExist();
		assertThat(source(StrictTimeoutSources.JUPITER_SENTINEL)).doesNotExist();
		assertThat(compiled).doesNotExist();
	}

	/**
	 * The writer for this test's exercise, confining nothing.
	 *
	 * @param layout the build layout, or null.
	 * @return the writer
	 */
	private StrictTimeoutWriter writer(BuildToolConfiguration layout) {
		return new StrictTimeoutWriter(new GeneratedHookFiles(projectRoot, layout, UnaryOperator.identity()));
	}

	/**
	 * A configuration with a strict timeout.
	 *
	 * @return the configuration
	 */
	private static TestBehaviorConfiguration configured() {
		return TestBehaviorConfiguration.builder()
				.regardingStrictTimeouts(StrictTimeoutsConfiguration.builder().theTimeoutIs(2).build()).build();
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
