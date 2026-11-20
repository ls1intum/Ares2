package de.tum.cit.ase.ares.api.securitytest.java.writer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;

import java.io.IOException;
import java.io.StringReader;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolver;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.platform.engine.discovery.DiscoverySelectors;
import org.junit.platform.testkit.engine.EngineExecutionResults;
import org.junit.platform.testkit.engine.EngineTestKit;

import de.tum.cit.ase.ares.api.buildtoolconfiguration.BuildMode;
import de.tum.cit.ase.ares.api.buildtoolconfiguration.BuildToolConfiguration;
import de.tum.cit.ase.ares.api.securitytest.ReservedPackageGuard;

/**
 * Checks the files every generated hook shares: one registration with JUnit for
 * all features, merged with the instructor's own entries, refused on conflict,
 * and removed again exactly.
 */
class GeneratedHookFilesTest {

	/** Fully qualified name of a first feature's JUnit hook. */
	private static final String FIRST_HOOK = "de.tum.cit.ase.ares.generated.FirstHook";

	/** Fully qualified name of a second feature's JUnit hook. */
	private static final String SECOND_HOOK = "de.tum.cit.ase.ares.generated.SecondHook";

	/** An extension the instructor registered before generation. */
	private static final String INSTRUCTOR_EXTENSION = "com.example.InstructorExtension";

	/** The exercise root of each test. */
	@TempDir
	Path projectRoot;

	/** The exercise's test resources root. */
	private Path resources;

	/**
	 * Creates the exercise's resources folder and a plain Gradle build.
	 *
	 * @throws IOException if the folders cannot be created
	 */
	@BeforeEach
	void setUp() throws IOException {
		resources = Files.createDirectories(projectRoot.resolve("src/test/resources"));
		Files.writeString(projectRoot.resolve("build.gradle"), "plugins { id 'java' }");
	}

	/**
	 * One feature's JUnit hook is registered, and auto-detection is switched on for
	 * it only.
	 *
	 * @throws IOException if a written file cannot be read
	 */
	@Test
	void registersAJupiterHook() throws IOException {
		files(null).register(resources, contribution(List.of("FirstHook")));

		assertThat(Files.readString(jupiterServices())).contains(FIRST_HOOK);
		assertThat(Files.readString(properties())).contains("junit.jupiter.extensions.autodetection.enabled=true")
				.contains("junit.jupiter.extensions.autodetection.include=" + FIRST_HOOK);
	}

	/**
	 * Two features share one include filter, since JUnit reads it as one setting.
	 *
	 * @throws IOException if a written file cannot be read
	 */
	@Test
	void twoFeaturesShareOneRegistration() throws IOException {
		GeneratedHookFiles.Contribution both = contribution(List.of("FirstHook"))
				.and(contribution(List.of("SecondHook")));

		files(null).register(resources, both);

		assertThat(Files.readString(properties())).contains("autodetection.include=" + FIRST_HOOK + "," + SECOND_HOOK);
		assertThat(Files.readString(jupiterServices())).contains(FIRST_HOOK).contains(SECOND_HOOK);
	}

	/**
	 * Dropping one feature leaves the other's hook registered.
	 *
	 * @throws IOException if a written file cannot be read
	 */
	@Test
	void droppingOneFeatureKeepsTheOther() throws IOException {
		files(null).register(resources, contribution(List.of("FirstHook", "SecondHook")));

		files(null).register(resources, contribution(List.of("SecondHook")));

		assertThat(Files.readString(properties())).contains("autodetection.include=" + SECOND_HOOK)
				.doesNotContain(FIRST_HOOK);
		assertThat(Files.readString(jupiterServices())).contains(SECOND_HOOK).doesNotContain(FIRST_HOOK);
	}

	/**
	 * Registering twice duplicates nothing.
	 *
	 * @throws IOException if a written file cannot be read
	 */
	@Test
	void aSecondRunDuplicatesNothing() throws IOException {
		files(null).register(resources, contribution(List.of("FirstHook")));
		String services = Files.readString(jupiterServices());
		String settings = Files.readString(properties());

		files(null).register(resources, contribution(List.of("FirstHook")));

		assertThat(Files.readString(jupiterServices())).isEqualTo(services);
		assertThat(Files.readString(properties())).isEqualTo(settings);
	}

	/**
	 * The instructor's own extension, registered with a trailing comment, stays
	 * registered and is included, so it keeps running.
	 *
	 * @throws IOException if a file cannot be written or read
	 */
	@Test
	void keepsAndIncludesTheInstructorsExtension() throws IOException {
		Files.createDirectories(jupiterServices().getParent());
		Files.writeString(jupiterServices(), INSTRUCTOR_EXTENSION + " # setup" + System.lineSeparator());

		files(null).register(resources, contribution(List.of("FirstHook")));

		assertThat(Files.readString(jupiterServices())).contains(INSTRUCTOR_EXTENSION).contains(FIRST_HOOK);
		assertThat(Files.readString(properties()))
				.contains("autodetection.include=" + FIRST_HOOK + "," + INSTRUCTOR_EXTENSION + System.lineSeparator())
				.doesNotContain("# setup");
	}

	/**
	 * The instructor's other JUnit settings stay, and auto-detection they already
	 * switched on is no conflict.
	 *
	 * @throws IOException if a file cannot be written or read
	 */
	@Test
	void keepsTheInstructorsOtherSettings() throws IOException {
		Files.writeString(properties(), "junit.jupiter.execution.parallel.enabled=false" + System.lineSeparator()
				+ "junit.jupiter.extensions.autodetection.enabled : TRUE" + System.lineSeparator());

		files(null).register(resources, contribution(List.of("FirstHook")));

		assertThat(Files.readString(properties())).contains("junit.jupiter.execution.parallel.enabled=false");
	}

	/**
	 * Auto-detection the instructor already switched on loaded every provider on
	 * the test class path, so the generated block adds no include filter that would
	 * narrow it.
	 *
	 * @throws IOException if a file cannot be written or read
	 */
	@Test
	void autodetectionTheInstructorSwitchedOnGetsNoIncludeFilter() throws IOException {
		Files.writeString(properties(),
				"junit.jupiter.extensions.autodetection.enabled : TRUE" + System.lineSeparator());

		files(null).register(resources, contribution(List.of("FirstHook")));

		assertThat(Files.readString(properties())).contains("junit.jupiter.extensions.autodetection.enabled=true")
				.doesNotContain("autodetection.include");
		assertThat(Files.readString(jupiterServices())).contains(FIRST_HOOK);
	}

	/**
	 * An extension a test dependency registers through its own service file keeps
	 * loading when the instructor had switched auto-detection on: a nested JUnit
	 * run with the generated settings resolves a parameter only that extension
	 * provides.
	 *
	 * @throws Exception if the nested run cannot be set up
	 */
	@Test
	void aDependencyExtensionKeepsLoadingWhenTheInstructorEnabledAutodetection() throws Exception {
		Files.writeString(properties(), "junit.jupiter.extensions.autodetection.enabled=true" + System.lineSeparator());
		files(null).register(resources, contribution(List.of("FirstHook")));

		runWithDependencyExtension().testEvents().assertStatistics(stats -> stats.succeeded(1).failed(0));
	}

	/**
	 * Without the instructor's switch, a dependency's extension did not load before
	 * generation and does not load after it: the include filter keeps it off.
	 *
	 * @throws Exception if the nested run cannot be set up
	 */
	@Test
	void aDependencyExtensionStaysOffWhenTheInstructorHadNotEnabledAutodetection() throws Exception {
		files(null).register(resources, contribution(List.of("FirstHook")));

		runWithDependencyExtension().testEvents().assertStatistics(stats -> stats.succeeded(0).failed(1));
	}

	/**
	 * A build file setting one of JUnit's auto-detection keys stops generation,
	 * naming the file and the key, since that setting overrides the generated one
	 * and cannot be read.
	 *
	 * @param buildFile the build file.
	 * @throws IOException if the build file cannot be written
	 */
	@ParameterizedTest
	@ValueSource(strings = { "pom.xml", "build.gradle", "build.gradle.kts" })
	void anAutodetectionSettingInABuildFileStopsGeneration(String buildFile) throws IOException {
		Files.writeString(projectRoot.resolve(buildFile),
				"systemProperty 'junit.jupiter.extensions.autodetection.include', 'com.example.*'");

		SecurityException failure = assertThrows(SecurityException.class,
				() -> files(null).register(resources, contribution(List.of("FirstHook"))));

		assertThat(failure.getMessage()).contains(buildFile).contains("junit.jupiter.extensions.autodetection.include");
		assertThat(jupiterServices()).doesNotExist();
		assertThat(properties()).doesNotExist();
	}

	/**
	 * The build-file refusal is localised: in German it still names the file and
	 * the key.
	 *
	 * @throws IOException if the build file cannot be written
	 */
	@Test
	void theBuildFileRefusalIsLocalisedInGerman() throws IOException {
		Files.writeString(projectRoot.resolve("pom.xml"),
				"<junit.jupiter.extensions.autodetection.enabled>true</junit.jupiter.extensions.autodetection.enabled>");
		Locale original = Locale.getDefault(Locale.Category.DISPLAY);
		try {
			Locale.setDefault(Locale.Category.DISPLAY, Locale.GERMAN);

			SecurityException failure = assertThrows(SecurityException.class,
					() -> files(null).register(resources, contribution(List.of("FirstHook"))));

			assertThat(failure.getMessage()).startsWith("Ares Sicherheitsfehler").contains("pom.xml")
					.contains("junit.jupiter.extensions.autodetection.enabled");
		} finally {
			Locale.setDefault(Locale.Category.DISPLAY, original);
		}
	}

	/**
	 * A build file setting is no obstacle while nothing is registered, since
	 * nothing generated depends on it then.
	 *
	 * @throws IOException if the build file cannot be written
	 */
	@Test
	void aBuildFileSettingDoesNotMatterWithoutHooks() throws IOException {
		Files.writeString(projectRoot.resolve("build.gradle"),
				"systemProperty 'junit.jupiter.extensions.autodetection.enabled', 'true'");

		assertThat(files(null).register(resources, contribution(List.of()))).isEmpty();
	}

	/**
	 * A setting that contradicts the registration stops generation, in any
	 * separator JUnit accepts, and names the file and the key.
	 *
	 * @param conflictingLine the instructor's conflicting setting.
	 * @throws IOException if the settings file cannot be written
	 */
	@ParameterizedTest
	@ValueSource(strings = { "junit.jupiter.extensions.autodetection.enabled=false",
			"junit.jupiter.extensions.autodetection.include=com.example.Other",
			"junit.jupiter.extensions.autodetection.exclude:com.example.Other",
			"junit.jupiter.extensions.autodetection.enabled false" })
	void refusesAConflictingSetting(String conflictingLine) throws IOException {
		Files.writeString(properties(), conflictingLine + System.lineSeparator());

		SecurityException failure = assertThrows(SecurityException.class,
				() -> files(null).register(resources, contribution(List.of("FirstHook"))));

		assertThat(failure.getMessage()).contains("junit-platform.properties")
				.contains(conflictingLine.split("[:= ]")[0]);
		assertThat(jupiterServices()).doesNotExist();
	}

	/**
	 * The refusal is localised: in German it still names the file and the key.
	 *
	 * @throws IOException if the settings file cannot be written
	 */
	@Test
	void theRefusalIsLocalisedInGerman() throws IOException {
		Files.writeString(properties(),
				"junit.jupiter.extensions.autodetection.enabled=false" + System.lineSeparator());
		Locale original = Locale.getDefault(Locale.Category.DISPLAY);
		try {
			Locale.setDefault(Locale.Category.DISPLAY, Locale.GERMAN);

			SecurityException failure = assertThrows(SecurityException.class,
					() -> files(null).register(resources, contribution(List.of("FirstHook"))));

			assertThat(failure.getMessage()).startsWith("Ares Sicherheitsfehler").contains("junit-platform.properties")
					.contains("junit.jupiter.extensions.autodetection.enabled");
		} finally {
			Locale.setDefault(Locale.Category.DISPLAY, original);
		}
	}

	/**
	 * Registering nothing removes the generated blocks and keeps the instructor's
	 * lines.
	 *
	 * @throws IOException if a file cannot be written or read
	 */
	@Test
	void registeringNothingRemovesOnlyWhatWasGenerated() throws IOException {
		Files.createDirectories(jupiterServices().getParent());
		Files.writeString(jupiterServices(), INSTRUCTOR_EXTENSION + System.lineSeparator());
		files(null).register(resources, contribution(List.of("FirstHook")));

		files(null).register(resources, GeneratedHookFiles.Contribution.NONE);

		assertThat(Files.readString(jupiterServices())).contains(INSTRUCTOR_EXTENSION).doesNotContain(FIRST_HOOK)
				.doesNotContain(GeneratedHookFiles.BLOCK_BEGIN);
		assertThat(properties()).doesNotExist();
	}

	/**
	 * Registering nothing also cleans the copies an earlier build left, in Maven's
	 * test output and in Gradle's test resources output.
	 *
	 * @param mode the build tool whose layout is used.
	 * @throws IOException if a file cannot be written or read
	 */
	@ParameterizedTest
	@ValueSource(strings = { "MAVEN", "GRADLE" })
	void registeringNothingCleansTheCopiedResources(String mode) throws IOException {
		BuildMode buildMode = BuildMode.valueOf(mode);
		BuildToolConfiguration layout = layout(buildMode);
		String copies = buildMode == BuildMode.MAVEN ? "target/test-classes" : "build/resources/test";
		Files.createDirectories(jupiterServices().getParent());
		Files.writeString(jupiterServices(), INSTRUCTOR_EXTENSION + System.lineSeparator());
		files(layout).register(resources, contribution(List.of("FirstHook")));
		Path copiedServices = projectRoot.resolve(copies).resolve(GeneratedHookFiles.JUPITER_SERVICE_FILE);
		Path copiedProperties = projectRoot.resolve(copies).resolve("junit-platform.properties");
		Files.createDirectories(copiedServices.getParent());
		Files.copy(jupiterServices(), copiedServices);
		Files.copy(properties(), copiedProperties);

		files(layout).register(resources, GeneratedHookFiles.Contribution.NONE);

		assertThat(Files.readString(copiedServices)).contains(INSTRUCTOR_EXTENSION).doesNotContain(FIRST_HOOK);
		assertThat(copiedProperties).doesNotExist();
	}

	/**
	 * Deleting a generated class removes its source and its compiled class.
	 *
	 * @param mode the build tool whose layout is used.
	 * @throws IOException if a file cannot be written
	 */
	@ParameterizedTest
	@ValueSource(strings = { "MAVEN", "GRADLE" })
	void deletingAGeneratedClassRemovesSourceAndCompiledClass(String mode) throws IOException {
		BuildToolConfiguration layout = layout(BuildMode.valueOf(mode));
		Path testFolder = Files.createDirectories(projectRoot.resolve("src/test/java"));
		Path source = files(layout).writeSource(testFolder, "FirstHook", "class FirstHook {}");
		Path compiled = layout.testOutputRoot().resolve("de/tum/cit/ase/ares/generated/FirstHook.class");
		Files.createDirectories(compiled.getParent());
		Files.write(compiled, new byte[] { (byte) 0xCA, (byte) 0xFE });

		files(layout).deleteGenerated(testFolder, "FirstHook");

		assertThat(source).doesNotExist();
		assertThat(compiled).doesNotExist();
	}

	/**
	 * The package the hooks are generated into is reserved, so a student class
	 * there is refused, while the hooks' own test output is not scanned.
	 *
	 * @throws IOException if the shipped prefix list cannot be read
	 */
	@Test
	void theGeneratedPackageIsReserved() throws IOException {
		assertThat(ReservedPackageGuard.reservedPrefixOf("de.tum.cit.ase.ares.generated.evil"))
				.isEqualTo("de.tum.cit.ase.ares.generated.");
		assertThrows(SecurityException.class, () -> ReservedPackageGuard
				.validateClassNames(List.of("de.tum.cit.ase.ares.generated.GeneratedStrictTimeout")));
		try (var list = GeneratedHookFilesTest.class.getResourceAsStream(
				"/de/tum/cit/ase/ares/api/configuration/reservedPackages/ReservedPackagePrefixes.txt")) {
			assertThat(new String(list.readAllBytes(), StandardCharsets.UTF_8).lines())
					.contains("de/tum/cit/ase/ares/generated/**");
		}
	}

	/**
	 * Runs {@link DependencyProbe} in a nested JUnit session configured with the
	 * generated settings file, as JUnit would read it, with a class path entry
	 * standing in for a dependency JAR that registers {@link DependencyExtension}
	 * through its own service file.
	 *
	 * @return the session's results
	 * @throws Exception if the run cannot be set up
	 */
	private EngineExecutionResults runWithDependencyExtension() throws Exception {
		Path dependency = Files.createDirectories(projectRoot.resolve("dependency"));
		Path services = dependency.resolve(GeneratedHookFiles.JUPITER_SERVICE_FILE);
		Files.createDirectories(services.getParent());
		Files.writeString(services, DependencyExtension.class.getName() + System.lineSeparator());
		ClassLoader original = Thread.currentThread().getContextClassLoader();
		try (URLClassLoader loader = new URLClassLoader(new URL[] { dependency.toUri().toURL() }, original)) {
			Thread.currentThread().setContextClassLoader(loader);
			return EngineTestKit.engine("junit-jupiter").configurationParameters(generatedSettings())
					.selectors(DiscoverySelectors.selectClass(DependencyProbe.class)).execute();
		} finally {
			Thread.currentThread().setContextClassLoader(original);
		}
	}

	/**
	 * The settings in the generated JUnit settings file, read the way JUnit reads
	 * the file.
	 *
	 * @return every key and its value
	 * @throws IOException if the file cannot be read
	 */
	private Map<String, String> generatedSettings() throws IOException {
		Properties settings = new Properties();
		settings.load(new StringReader(Files.readString(properties())));
		return settings.stringPropertyNames().stream().collect(Collectors.toMap(key -> key, settings::getProperty));
	}

	/**
	 * The shared file handling for this test's exercise, confining nothing.
	 *
	 * @param layout the build layout, or null.
	 * @return the file handling
	 */
	private GeneratedHookFiles files(BuildToolConfiguration layout) {
		return new GeneratedHookFiles(projectRoot, layout, UnaryOperator.identity());
	}

	/**
	 * A contribution with no written files.
	 *
	 * @param jupiterHooks the JUnit hooks' simple names.
	 * @return the contribution
	 */
	private static GeneratedHookFiles.Contribution contribution(List<String> jupiterHooks) {
		return new GeneratedHookFiles.Contribution(List.of(), jupiterHooks);
	}

	/**
	 * A build layout of this exercise, with its folders created.
	 *
	 * @param buildMode the build tool.
	 * @return the layout
	 * @throws IOException if a folder cannot be created
	 */
	private BuildToolConfiguration layout(BuildMode buildMode) throws IOException {
		boolean maven = buildMode == BuildMode.MAVEN;
		Path main = Files.createDirectories(projectRoot.resolve(maven ? "target/classes" : "build/classes/java/main"));
		Path test = Files
				.createDirectories(projectRoot.resolve(maven ? "target/test-classes" : "build/classes/java/test"));
		Path sources = Files.createDirectories(projectRoot.resolve("src/main/java"));
		Path testSources = Files.createDirectories(projectRoot.resolve("src/test/java"));
		Files.writeString(projectRoot.resolve("pom.xml"), "<project/>");
		return new BuildToolConfiguration(buildMode, projectRoot, List.of(sources), List.of(testSources), main, test);
	}

	/**
	 * The JUnit service file in the test resources.
	 *
	 * @return its path
	 */
	private Path jupiterServices() {
		return resources.resolve(GeneratedHookFiles.JUPITER_SERVICE_FILE);
	}

	/**
	 * JUnit's settings file in the test resources.
	 *
	 * @return its path
	 */
	private Path properties() {
		return resources.resolve(GeneratedHookFiles.PLATFORM_PROPERTIES);
	}

	/** What only {@link DependencyExtension} provides to a test. */
	public static final class DependencyMarker {
	}

	/**
	 * An extension a test dependency registers through its own service file,
	 * standing in for one inside a JAR.
	 */
	public static final class DependencyExtension implements ParameterResolver {

		/**
		 * Resolves only the marker.
		 *
		 * @param parameterContext the parameter.
		 * @param extensionContext the test.
		 * @return true for the marker
		 */
		@Override
		public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext) {
			return parameterContext.getParameter().getType() == DependencyMarker.class;
		}

		/**
		 * Provides a marker.
		 *
		 * @param parameterContext the parameter.
		 * @param extensionContext the test.
		 * @return a new marker
		 */
		@Override
		public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext) {
			return new DependencyMarker();
		}
	}

	/**
	 * A test that passes only when {@link DependencyExtension} was loaded. Run only
	 * through a nested session, never on its own.
	 */
	public static final class DependencyProbe {

		/**
		 * Needs the dependency's parameter.
		 *
		 * @param marker provided only by the dependency's extension.
		 */
		@Test
		void receivesTheDependencysParameter(DependencyMarker marker) {
			assertThat(marker).isNotNull();
		}
	}

	/**
	 * Checks that the generated hooks are refused for an exercise whose build file
	 * names a JUnit older than 5.12 or a version that is not a number, or cannot be
	 * read, under Maven and Gradle, and accepted at 5.12, above it, and when the
	 * version comes from elsewhere.
	 */
	@Nested
	class JUnitVersionCheck {

		/** The exercise's root folder. */
		@TempDir
		Path projectRoot;

		/**
		 * A Maven or Gradle build naming JUnit below 5.12 is refused, naming the
		 * version; at 5.12 and above it is accepted.
		 *
		 * @param buildFile the build file's name.
		 * @param text      the build file's text, with {@code %s} for the version.
		 * @throws IOException if the build file cannot be written
		 */
		@ParameterizedTest
		@CsvSource(delimiter = '|', value = {
				"pom.xml|<project><dependencies><dependency><groupId>org.junit.jupiter</groupId><artifactId>junit-jupiter</artifactId><version>%s</version></dependency></dependencies></project>",
				"pom.xml|<project><dependencyManagement><dependencies><dependency><groupId>org.junit</groupId><artifactId>junit-bom</artifactId><version>%s</version><type>pom</type></dependency></dependencies></dependencyManagement></project>",
				"build.gradle|dependencies { testImplementation 'org.junit.jupiter:junit-jupiter:%s' }",
				"build.gradle.kts|dependencies { testImplementation(platform(\"org.junit:junit-bom:%s\")) }" })
		void theVersionDecides(String buildFile, String text) throws IOException {
			write(buildFile, text.formatted("5.11.4"));
			assertThatThrownBy(() -> ExerciseJUnitVersion.requireIncludeFilterSupport(projectRoot))
					.isInstanceOf(SecurityException.class).hasMessageContaining("5.11.4");
			write(buildFile, text.formatted("5.12.0"));
			assertThatCode(() -> ExerciseJUnitVersion.requireIncludeFilterSupport(projectRoot))
					.doesNotThrowAnyException();
			write(buildFile, text.formatted("6.0.1"));
			assertThatCode(() -> ExerciseJUnitVersion.requireIncludeFilterSupport(projectRoot))
					.doesNotThrowAnyException();
		}

		/**
		 * A version taken from a property, a range from 5.12, a catalogue or a parent,
		 * and a project without a build file, are accepted.
		 *
		 * @param buildFile the build file's name.
		 * @param text      the build file's text.
		 * @throws IOException if a build file cannot be written
		 */
		@ParameterizedTest
		@CsvSource(delimiter = '|', value = {
				"pom.xml|<project><dependencies><dependency><artifactId>junit-jupiter</artifactId><version>${junit.version}</version></dependency></dependencies></project>",
				"pom.xml|<project><dependencies><dependency><artifactId>junit-jupiter</artifactId><version>[5.12,6)</version></dependency></dependencies></project>",
				"pom.xml|<project><dependencies><dependency><artifactId>junit-jupiter</artifactId><version>5.${junit.minor}.0</version></dependency></dependencies></project>",
				"pom.xml|<project><dependencies><dependency><artifactId>junit-jupiter</artifactId><scope>test</scope></dependency></dependencies></project>",
				"build.gradle|dependencies { testImplementation libs.junit.jupiter }",
				"build.gradle|dependencies { testImplementation \"org.junit.jupiter:junit-jupiter:$junitVersion\" }",
				"build.gradle.kts|dependencies { testImplementation(\"org.junit.jupiter:junit-jupiter:${junitVersion}\") }" })
		void aVersionFromElsewhereIsAccepted(String buildFile, String text) throws IOException {
			assertThatCode(() -> ExerciseJUnitVersion.requireIncludeFilterSupport(projectRoot))
					.doesNotThrowAnyException();
			write(buildFile, text);
			assertThatCode(() -> ExerciseJUnitVersion.requireIncludeFilterSupport(projectRoot))
					.doesNotThrowAnyException();
		}

		/**
		 * A Maven range is checked by its lowest version: one below 5.12 is refused,
		 * naming the range, also through a property; one from 5.12, or one with no
		 * lowest version, is accepted.
		 *
		 * @param range   the range.
		 * @param refused whether the build is refused.
		 * @throws IOException if the build file cannot be written
		 */
		@ParameterizedTest
		@CsvSource(delimiter = '|', value = { "[5.11.4]|true", "[5.11,6)|true", "(5.11.4,6)|true",
				"[5.12,6),[5.10,5.11)|true", "[5.12.0]|false", "[5.12,6)|false", "[5.12.0,)|false", "(,6)|false" })
		void aRangeIsCheckedByItsLowestVersion(String range, boolean refused) throws IOException {
			String direct = "<project><dependencies><dependency><artifactId>junit-jupiter</artifactId><version>%s</version></dependency></dependencies></project>";
			String property = "<project><properties><junit.version>%s</junit.version></properties><dependencies><dependency><artifactId>junit-jupiter</artifactId><version>${junit.version}</version></dependency></dependencies></project>";
			for (String text : new String[] { direct, property }) {
				write("pom.xml", text.formatted(range));
				if (refused) {
					assertThatThrownBy(() -> ExerciseJUnitVersion.requireIncludeFilterSupport(projectRoot))
							.isInstanceOf(SecurityException.class).hasMessageContaining(range);
				} else {
					assertThatCode(() -> ExerciseJUnitVersion.requireIncludeFilterSupport(projectRoot))
							.doesNotThrowAnyException();
				}
			}
		}

		/**
		 * A Maven range with no lowest version is checked by its highest one: when
		 * every version it allows is older than 5.12 it is refused, naming the range,
		 * also through a property and as one set of several; otherwise accepted.
		 *
		 * @param range   the range.
		 * @param refused whether the build is refused.
		 * @throws IOException if the build file cannot be written
		 */
		@ParameterizedTest
		@CsvSource(delimiter = '|', value = { "(,5.11.4]|true", "(,5.12)|true", "(,5.12.0)|true", "(,5.12-M1)|true",
				"[5.12,6),(,5.11]|true", "(,5.12]|false", "(,6)|false", "(,5.12.1)|false", "[5.12,6),(,7)|false" })
		void aRangeWithoutALowestVersionIsCheckedByItsHighest(String range, boolean refused) throws IOException {
			aRangeIsCheckedByItsLowestVersion(range, refused);
		}

		/**
		 * A Maven range whose only bound is not a number is refused as unreadable.
		 *
		 * @throws IOException if the build file cannot be written
		 */
		@Test
		void aRangeWithoutANumberAsItsHighestIsRefused() throws IOException {
			write("pom.xml",
					"<project><dependencies><dependency><artifactId>junit-jupiter</artifactId><version>(,latest]</version></dependency></dependencies></project>");
			assertThatThrownBy(() -> ExerciseJUnitVersion.requireIncludeFilterSupport(projectRoot))
					.isInstanceOf(SecurityException.class).hasMessageContaining("(,latest]");
		}

		/**
		 * A Maven range whose lowest version is not a number is refused as unreadable.
		 *
		 * @throws IOException if the build file cannot be written
		 */
		@Test
		void aRangeWithoutANumberIsRefused() throws IOException {
			write("pom.xml",
					"<project><dependencies><dependency><artifactId>junit-jupiter</artifactId><version>[latest,6)</version></dependency></dependencies></project>");
			assertThatThrownBy(() -> ExerciseJUnitVersion.requireIncludeFilterSupport(projectRoot))
					.isInstanceOf(SecurityException.class).hasMessageContaining("[latest,6)");
		}

		/**
		 * A version the exercise sets in its own pom properties, Gradle variables,
		 * {@code gradle.properties} or catalogue is looked up: below 5.12 it is refused
		 * in English and German, at 5.12 it is accepted.
		 *
		 * @param firstFile  the first file's path.
		 * @param firstText  its text, with {@code %s} for the version.
		 * @param secondFile the second file's path, or {@code -} for none.
		 * @param secondText its text, with {@code %s} for the version.
		 * @throws IOException if a file cannot be written
		 */
		@ParameterizedTest
		@CsvSource(delimiter = '|', value = {
				"pom.xml|<project><properties><junit.version>%s</junit.version></properties><dependencies><dependency><artifactId>junit-jupiter</artifactId><version>${junit.version}</version></dependency></dependencies></project>|-|-",
				"pom.xml|<project><properties><junit.version>${junit.base}</junit.version><junit.base>%s</junit.base></properties><dependencies><dependency><artifactId>junit-bom</artifactId><version>${junit.version}</version></dependency></dependencies></project>|-|-",
				"build.gradle|dependencies { testImplementation \"org.junit.jupiter:junit-jupiter:$junitVersion\" }|gradle.properties|junitVersion=%s",
				"build.gradle|ext.junitVersion = '%s'\\ndependencies { testImplementation \"org.junit.jupiter:junit-jupiter:${junitVersion}\" }|-|-",
				"build.gradle.kts|val junitVersion = \"%s\"\\ndependencies { testImplementation(\"org.junit.jupiter:junit-jupiter:$junitVersion\") }|-|-",
				"build.gradle|dependencies { testImplementation libs.junit.jupiter }|gradle/libs.versions.toml|[versions]\\njunit = \"%s\"\\n[libraries]\\njunit-jupiter = { module = \"org.junit.jupiter:junit-jupiter\", version.ref = \"junit\" }",
				"build.gradle.kts|dependencies { testImplementation(\"org.junit.jupiter:junit-jupiter:${libs.versions.junit.get()}\") }|gradle/libs.versions.toml|[versions]\\njunit = \"%s\"",
				"build.gradle|dependencies { testImplementation platform(libs.junit.bom) }|gradle/libs.versions.toml|[libraries]\\njunit-bom = { group = \"org.junit\", name = \"junit-bom\", version = \"%s\" }" })
		void aVersionTheExerciseSetsIsLookedUp(String firstFile, String firstText, String secondFile, String secondText)
				throws IOException {
			writeFiles(firstFile, firstText.formatted("5.11.4"), secondFile, secondText.formatted("5.11.4"));
			assertThatThrownBy(() -> ExerciseJUnitVersion.requireIncludeFilterSupport(projectRoot))
					.isInstanceOf(SecurityException.class).hasMessageStartingWith("Ares Security Error")
					.hasMessageContaining("5.11.4");
			inGerman(() -> assertThatThrownBy(() -> ExerciseJUnitVersion.requireIncludeFilterSupport(projectRoot))
					.hasMessageStartingWith("Ares Sicherheitsfehler").hasMessageContaining("5.11.4"));
			writeFiles(firstFile, firstText.formatted("5.12.0"), secondFile, secondText.formatted("5.12.0"));
			assertThatCode(() -> ExerciseJUnitVersion.requireIncludeFilterSupport(projectRoot))
					.doesNotThrowAnyException();
		}

		/**
		 * A catalogue's {@code [versions]} header with a trailing comment or spaces
		 * inside its brackets is still read, also beside a quoted value holding a hash,
		 * so a JUnit below 5.12 set there is refused and one from 5.12 accepted.
		 *
		 * @param header the table header as written.
		 * @throws IOException if a file cannot be written
		 */
		@ParameterizedTest
		@CsvSource(delimiter = '|', value = { "[versions] # pinned", "[ versions ]", "  [versions]   #" })
		void aVersionsTableWithACommentedHeaderIsRead(String header) throws IOException {
			String catalogue = header
					+ "\\nnote = \"see # below\"\\njunit = \"%s\"\\n[libraries]\\njunit-jupiter = { module = \"org.junit.jupiter:junit-jupiter\", version.ref = \"junit\" }";
			String build = "dependencies { testImplementation libs.junit.jupiter }";
			writeFiles("build.gradle", build, "gradle/libs.versions.toml", catalogue.formatted("5.11.4"));
			assertThatThrownBy(() -> ExerciseJUnitVersion.requireIncludeFilterSupport(projectRoot))
					.isInstanceOf(SecurityException.class).hasMessageContaining("5.11.4");
			writeFiles("build.gradle", build, "gradle/libs.versions.toml", catalogue.formatted("5.12.0"));
			assertThatCode(() -> ExerciseJUnitVersion.requireIncludeFilterSupport(projectRoot))
					.doesNotThrowAnyException();
		}

		/**
		 * A Gradle variable set twice is checked at every value, since a later
		 * assignment replaces an earlier one: an old JUnit in either place is refused,
		 * and two values from 5.12 are accepted.
		 *
		 * @param buildFile the build file's name.
		 * @param text      its text, with {@code %s} for the first and the second
		 *                  value.
		 * @param first     the first value.
		 * @param second    the second value.
		 * @param refused   whether the build is refused.
		 * @throws IOException if a file cannot be written
		 */
		@ParameterizedTest
		@CsvSource(delimiter = '|', value = {
				"build.gradle|ext.junitVersion = '%s'\\njunitVersion = '%s'\\ndependencies { testImplementation \"org.junit.jupiter:junit-jupiter:$junitVersion\" }|5.12.2|5.11.4|true",
				"build.gradle|ext.junitVersion = '%s'\\njunitVersion = '%s'\\ndependencies { testImplementation \"org.junit.jupiter:junit-jupiter:$junitVersion\" }|5.11.4|5.12.2|true",
				"build.gradle.kts|var junitVersion = \"%s\"\\njunitVersion = \"%s\"\\ndependencies { testImplementation(\"org.junit.jupiter:junit-jupiter:$junitVersion\") }|5.12.2|5.11.4|true",
				"build.gradle|ext.junitVersion = '%s'\\njunitVersion = '%s'\\ndependencies { testImplementation \"org.junit.jupiter:junit-jupiter:$junitVersion\" }|5.12.0|5.12.2|false" })
		void aVariableSetTwiceIsCheckedAtEveryValue(String buildFile, String text, String first, String second,
				boolean refused) throws IOException {
			writeFiles(buildFile, text.formatted(first, second), "-", "-");
			if (refused) {
				assertThatThrownBy(() -> ExerciseJUnitVersion.requireIncludeFilterSupport(projectRoot))
						.isInstanceOf(SecurityException.class).hasMessageContaining("5.11.4");
			} else {
				assertThatCode(() -> ExerciseJUnitVersion.requireIncludeFilterSupport(projectRoot))
						.doesNotThrowAnyException();
			}
		}

		/**
		 * A variable set to a number and to several copies of itself is refused as
		 * unreadable within seconds, instead of building its readings exponentially,
		 * while a variable set to a handful of values is still checked at each.
		 *
		 * @throws IOException if a file cannot be written
		 */
		@Test
		void aValueThatMultipliesItselfIsRefusedQuickly() throws IOException {
			writeFiles("build.gradle",
					"ext.v = '5.12.0'\\nv = \"${v}${v}${v}${v}\"\\ndependencies { testImplementation \"org.junit.jupiter:junit-jupiter:$v\" }",
					"-", "-");
			assertTimeoutPreemptively(Duration.ofSeconds(5),
					() -> assertThatThrownBy(() -> ExerciseJUnitVersion.requireIncludeFilterSupport(projectRoot))
							.isInstanceOf(SecurityException.class).hasMessageContaining("\"$v\""));
			writeFiles("build.gradle",
					"ext.v = '5.12.0'\\nv = '5.12.1'\\nv = '5.13.0'\\ndependencies { testImplementation \"org.junit.jupiter:junit-jupiter:$v\" }",
					"-", "-");
			assertThatCode(() -> ExerciseJUnitVersion.requireIncludeFilterSupport(projectRoot))
					.doesNotThrowAnyException();
		}

		/**
		 * A value set once to many copies of itself, or two values that repeat each
		 * other, is refused as unreadable within seconds, instead of growing longer
		 * with every lookup.
		 *
		 * @param firstFile the build file's path.
		 * @param firstText its text.
		 * @throws IOException if a file cannot be written
		 */
		@ParameterizedTest
		@CsvSource(delimiter = '|', value = {
				"pom.xml|<project><properties><v>${v}${v}${v}${v}${v}${v}${v}${v}${v}${v}${v}${v}${v}${v}${v}${v}${v}${v}${v}${v}${v}${v}${v}${v}${v}${v}${v}${v}${v}${v}${v}${v}${v}${v}${v}${v}${v}${v}${v}${v}${v}${v}${v}${v}${v}${v}${v}${v}${v}${v}${v}${v}${v}${v}${v}${v}${v}${v}${v}${v}${v}${v}${v}${v}</v></properties><dependencies><dependency><artifactId>junit-jupiter</artifactId><version>${v}</version></dependency></dependencies></project>",
				"pom.xml|<project><properties><a>${b}${b}${b}${b}${b}${b}${b}${b}</a><b>${a}${a}${a}${a}${a}${a}${a}${a}</b></properties><dependencies><dependency><artifactId>junit-jupiter</artifactId><version>${a}</version></dependency></dependencies></project>",
				"build.gradle|ext.a = \"${b}${b}${b}\"\\next.b = \"aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa\"\\ndependencies { testImplementation \"org.junit.jupiter:junit-jupiter:$a\" }" })
		void aValueThatGrowsWithEveryLookupIsRefusedQuickly(String firstFile, String firstText) throws IOException {
			writeFiles(firstFile, firstText, "-", "-");
			assertTimeoutPreemptively(Duration.ofSeconds(5),
					() -> assertThatThrownBy(() -> ExerciseJUnitVersion.requireIncludeFilterSupport(projectRoot))
							.isInstanceOf(SecurityException.class));
		}

		/**
		 * A value looked up through another one that the exercise sets is still checked
		 * at the number it ends in.
		 *
		 * @throws IOException if a file cannot be written
		 */
		@Test
		void aChainedValueIsStillChecked() throws IOException {
			String pom = "<project><properties><a>${b}</a><b>%s</b></properties><dependencies><dependency><artifactId>junit-jupiter</artifactId><version>${a}</version></dependency></dependencies></project>";
			writeFiles("pom.xml", pom.formatted("5.12.0"), "-", "-");
			assertThatCode(() -> ExerciseJUnitVersion.requireIncludeFilterSupport(projectRoot))
					.doesNotThrowAnyException();
			writeFiles("pom.xml", pom.formatted("5.11.4"), "-", "-");
			assertThatThrownBy(() -> ExerciseJUnitVersion.requireIncludeFilterSupport(projectRoot))
					.isInstanceOf(SecurityException.class).hasMessageContaining("5.11.4");
		}

		/**
		 * A reference the exercise's own files do not define, or a short loop, is
		 * accepted, since Ares cannot tell the version.
		 *
		 * @param firstFile  the first file's path.
		 * @param firstText  its text.
		 * @param secondFile the second file's path, or {@code -} for none.
		 * @param secondText its text.
		 * @throws IOException if a file cannot be written
		 */
		@ParameterizedTest
		@CsvSource(delimiter = '|', value = {
				"pom.xml|<project><properties><a>${b}</a><b>${a}</b></properties><dependencies><dependency><artifactId>junit-jupiter</artifactId><version>${a}</version></dependency></dependencies></project>|-|-",
				"pom.xml|<project><dependencies><dependency><artifactId>junit-jupiter</artifactId><version>${project.parent.version}</version></dependency></dependencies></project>|-|-",
				"build.gradle|ext.a = \"$b\"\\next.b = \"$a\"\\ndependencies { testImplementation \"org.junit.jupiter:junit-jupiter:$a\" }|-|-",
				"build.gradle|dependencies { testImplementation libs.junit.jupiter }|gradle/libs.versions.toml|[libraries]\\njunit-jupiter = { module = \"org.junit.jupiter:junit-jupiter\", version.ref = \"missing\" }" })
		void aReferenceAresCannotLookUpIsAccepted(String firstFile, String firstText, String secondFile,
				String secondText) throws IOException {
			writeFiles(firstFile, firstText, secondFile, secondText);
			assertThatCode(() -> ExerciseJUnitVersion.requireIncludeFilterSupport(projectRoot))
					.doesNotThrowAnyException();
		}

		/**
		 * A commented-out example naming a placeholder is ignored, while the active
		 * dependency still decides.
		 *
		 * @param buildFile the build file's name.
		 * @param text      the build file's text, with the active version as a slot.
		 * @throws IOException if the build file cannot be written
		 */
		@ParameterizedTest
		@CsvSource(delimiter = '|', value = {
				"pom.xml|<project><!-- <dependency><artifactId>junit-jupiter</artifactId><version>VERSION</version></dependency> --><dependencies><dependency><artifactId>junit-jupiter</artifactId><version>%s</version></dependency></dependencies></project>",
				"build.gradle|dependencies { testImplementation 'org.junit.jupiter:junit-jupiter:%s' } // testImplementation 'org.junit.jupiter:junit-jupiter:VERSION'",
				"build.gradle.kts|/* testImplementation(\"org.junit.jupiter:junit-jupiter:VERSION\") */ dependencies { testImplementation(\"org.junit.jupiter:junit-jupiter:%s\") }" })
		void aCommentedOutExampleIsIgnored(String buildFile, String text) throws IOException {
			write(buildFile, text.formatted("5.12.2"));
			assertThatCode(() -> ExerciseJUnitVersion.requireIncludeFilterSupport(projectRoot))
					.doesNotThrowAnyException();
			write(buildFile, text.formatted("5.10.0"));
			assertThatThrownBy(() -> ExerciseJUnitVersion.requireIncludeFilterSupport(projectRoot))
					.isInstanceOf(SecurityException.class).hasMessageContaining("5.10.0");
		}

		/**
		 * The slashes of a URL or a file pattern do not start a comment, so a version
		 * after them on the same line is still read.
		 *
		 * @throws IOException if the build file cannot be written
		 */
		@Test
		void slashesOutsideACommentHideNothing() throws IOException {
			write("build.gradle",
					"repositories { maven { url 'https://repo.example/m2' } } "
							+ "sourceSets { test { java { include '**/*.java' } } } "
							+ "dependencies { testImplementation 'org.junit.jupiter:junit-jupiter:5.10.0' } // */");
			assertThatThrownBy(() -> ExerciseJUnitVersion.requireIncludeFilterSupport(projectRoot))
					.isInstanceOf(SecurityException.class).hasMessageContaining("5.10.0");
		}

		/**
		 * A Maven or Gradle build naming a JUnit version that is not a number is
		 * refused, naming the build file and the value, in English and German.
		 *
		 * @param buildFile the build file's name.
		 * @param text      the build file's text.
		 * @param value     the value it names.
		 * @throws IOException if the build file cannot be written
		 */
		@ParameterizedTest
		@CsvSource(delimiter = '|', value = {
				"pom.xml|<project><dependencies><dependency><artifactId>junit-jupiter</artifactId><version>five</version></dependency></dependencies></project>|five",
				"pom.xml|<project><dependencies><dependency><artifactId>junit-bom</artifactId><version>99999999999.1</version></dependency></dependencies></project>|99999999999.1",
				"build.gradle|dependencies { testImplementation 'org.junit.jupiter:junit-jupiter:latest.release' }|latest.release",
				"build.gradle.kts|dependencies { testImplementation(platform(\"org.junit:junit-bom:5.x\")) }|5.x" })
		void aVersionThatIsNotANumberIsRefused(String buildFile, String text, String value) throws IOException {
			write(buildFile, text);
			assertThatThrownBy(() -> ExerciseJUnitVersion.requireIncludeFilterSupport(projectRoot))
					.isInstanceOf(SecurityException.class).hasMessageStartingWith("Ares Security Error")
					.hasMessageContaining(buildFile).hasMessageContaining("\"" + value + "\"");
			inGerman(() -> assertThatThrownBy(() -> ExerciseJUnitVersion.requireIncludeFilterSupport(projectRoot))
					.hasMessageStartingWith("Ares Sicherheitsfehler").hasMessageContaining(buildFile)
					.hasMessageContaining("\"" + value + "\""));
		}

		/**
		 * A Maven or Gradle build file that exists but is not a readable file is
		 * refused, naming it, in English and German.
		 *
		 * @param buildFile the build file's name.
		 * @throws IOException if the folder cannot be created
		 */
		@ParameterizedTest
		@CsvSource({ "pom.xml", "build.gradle", "build.gradle.kts" })
		void aBuildFileThatCannotBeReadIsRefused(String buildFile) throws IOException {
			Files.createDirectory(projectRoot.resolve(buildFile));
			assertThatThrownBy(() -> ExerciseJUnitVersion.requireIncludeFilterSupport(projectRoot))
					.isInstanceOf(SecurityException.class).hasMessageStartingWith("Ares Security Error")
					.hasMessageContaining(buildFile).hasMessageContaining("cannot be read");
			inGerman(() -> assertThatThrownBy(() -> ExerciseJUnitVersion.requireIncludeFilterSupport(projectRoot))
					.hasMessageStartingWith("Ares Sicherheitsfehler").hasMessageContaining(buildFile)
					.hasMessageContaining("nicht gelesen"));
		}

		/**
		 * The refusal is localised in German too.
		 *
		 * @throws IOException if the build file cannot be written
		 */
		@Test
		void theRefusalIsLocalisedInGerman() throws IOException {
			write("build.gradle", "dependencies { testImplementation 'org.junit.jupiter:junit-jupiter:5.10.2' }");
			inGerman(() -> assertThatThrownBy(() -> ExerciseJUnitVersion.requireIncludeFilterSupport(projectRoot))
					.hasMessageStartingWith("Ares Sicherheitsfehler").hasMessageContaining("5.10.2"));
		}

		/**
		 * Version numbers compare by major and minor, a qualifier does not count, and
		 * one too long to read is refused.
		 */
		@Test
		void versionsCompareByMajorAndMinor() {
			assertThat(ExerciseJUnitVersion.isTooOld("4.13")).isTrue();
			assertThat(ExerciseJUnitVersion.isTooOld("5.9.3")).isTrue();
			assertThat(ExerciseJUnitVersion.isTooOld("5.12")).isFalse();
			assertThat(ExerciseJUnitVersion.isTooOld("5.13.0")).isFalse();
			assertThat(ExerciseJUnitVersion.isTooOld("5.12.0-M1")).isFalse();
			assertThat(ExerciseJUnitVersion.isTooOld("5.11-RC1")).isTrue();
			assertThat(ExerciseJUnitVersion.isTooOld("99999999999999999999.1")).isTrue();
		}

		/**
		 * Runs a check with German as the display language, then restores the one
		 * before.
		 *
		 * @param check the check to run.
		 */
		private static void inGerman(Runnable check) {
			Locale original = Locale.getDefault(Locale.Category.DISPLAY);
			try {
				Locale.setDefault(Locale.Category.DISPLAY, Locale.GERMAN);
				check.run();
			} finally {
				Locale.setDefault(Locale.Category.DISPLAY, original);
			}
		}

		/**
		 * Empties the project folder, then writes up to two files, turning each written
		 * {@code \\n} into a line break.
		 *
		 * @param firstFile  the first file's path.
		 * @param firstText  its text.
		 * @param secondFile the second file's path, or {@code -} for none.
		 * @param secondText its text.
		 * @throws IOException if a file cannot be written or deleted
		 */
		private void writeFiles(String firstFile, String firstText, String secondFile, String secondText)
				throws IOException {
			try (Stream<Path> existing = Files.walk(projectRoot)) {
				for (Path path : existing.sorted(Comparator.reverseOrder()).toList()) {
					if (!path.equals(projectRoot)) {
						Files.delete(path);
					}
				}
			}
			writeOne(firstFile, firstText);
			if (!"-".equals(secondFile)) {
				writeOne(secondFile, secondText);
			}
		}

		/**
		 * A versionless dependency a parent manages passes the build file check, so
		 * every generated hook's source carries the check of the running engine that
		 * stops an old one, with no placeholder left.
		 *
		 * @throws IOException if the build file cannot be written
		 */
		@Test
		void aParentManagedVersionIsLeftToTheEngineCheck() throws IOException {
			write("pom.xml",
					"<project><parent><artifactId>course-parent</artifactId></parent><dependencies><dependency><artifactId>junit-jupiter</artifactId></dependency></dependencies></project>");
			assertThatCode(() -> ExerciseJUnitVersion.requireIncludeFilterSupport(projectRoot))
					.doesNotThrowAnyException();
			String messages = "com.example.ares.api.localization.Messages";
			for (String source : new String[] { StrictTimeoutSources.jupiterHook(messages, true),
					OutputMirroringSources.jupiterHook(messages, true),
					FailureReportingSources.jupiterHook(messages) }) {
				assertThat(source).contains("requireSupportedJUnitEngine(loadedJUnitEngineVersion());")
						.doesNotContain(ExerciseJUnitVersion.ENGINE_CHECK);
			}
		}

		/**
		 * Writes one file below the project folder, creating its folders.
		 *
		 * @param file the file's path.
		 * @param text its text, with {@code \\n} for a line break.
		 * @throws IOException if the file cannot be written
		 */
		private void writeOne(String file, String text) throws IOException {
			Path path = projectRoot.resolve(file);
			Files.createDirectories(path.getParent());
			Files.writeString(path, text.replace("\\n", "\n"));
		}

		/**
		 * Writes one build file and removes the others.
		 *
		 * @param buildFile the build file's name.
		 * @param text      its text.
		 * @throws IOException if a file cannot be written or deleted
		 */
		private void write(String buildFile, String text) throws IOException {
			for (String other : new String[] { "pom.xml", "build.gradle", "build.gradle.kts" }) {
				Path existing = projectRoot.resolve(other);
				if (!Files.isDirectory(existing)) {
					Files.deleteIfExists(existing);
				}
			}
			Files.writeString(projectRoot.resolve(buildFile), text);
		}
	}

	/**
	 * The exercise every generated-hook writer test writes into: a temporary
	 * project with a test source root and a plain Gradle build, or a Maven layout
	 * on request.
	 */
	abstract static class Project {

		/** The exercise package the copied Ares classes live in. */
		static final String PACKAGE = "com.example";

		/** The exercise root of each test. */
		@TempDir
		Path projectRoot;

		/** The exercise's test source root. */
		Path testFolder;

		/**
		 * Creates the exercise folders and a plain Gradle build.
		 *
		 * @throws IOException if the folders cannot be created
		 */
		@BeforeEach
		void createProject() throws IOException {
			testFolder = Files.createDirectories(projectRoot.resolve("src/test/java"));
			Files.writeString(projectRoot.resolve("build.gradle"), "plugins { id 'java' }");
		}

		/**
		 * Turns the project into a Maven one with its output folders.
		 *
		 * @return the Maven layout.
		 * @throws IOException if a folder or the build file cannot be written
		 */
		BuildToolConfiguration mavenLayout() throws IOException {
			for (String directory : List.of("src/main/java", "target/classes", "target/test-classes")) {
				Files.createDirectories(projectRoot.resolve(directory));
			}
			Files.writeString(projectRoot.resolve("pom.xml"), "<project/>");
			return new BuildToolConfiguration(BuildMode.MAVEN, projectRoot,
					List.of(projectRoot.resolve("src/main/java")), List.of(testFolder),
					projectRoot.resolve("target/classes"), projectRoot.resolve("target/test-classes"));
		}

		/**
		 * Where a generated class's source belongs.
		 *
		 * @param simpleName the class's simple name.
		 * @return the source file.
		 */
		Path source(String simpleName) {
			return testFolder.resolve("de/tum/cit/ase/ares/generated").resolve(simpleName + ".java");
		}
	}
}
