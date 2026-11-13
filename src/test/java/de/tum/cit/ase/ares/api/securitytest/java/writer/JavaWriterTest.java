package de.tum.cit.ase.ares.api.securitytest.java.writer;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.MockedStatic;

import de.tum.cit.ase.ares.api.aop.AOPMode;
import de.tum.cit.ase.ares.api.aop.java.JavaAOPTestCase;
import de.tum.cit.ase.ares.api.architecture.ArchitectureMode;
import de.tum.cit.ase.ares.api.architecture.java.JavaArchitectureTestCase;
import de.tum.cit.ase.ares.api.buildtoolconfiguration.BuildMode;
import de.tum.cit.ase.ares.api.buildtoolconfiguration.BuildToolConfiguration;
import de.tum.cit.ase.ares.api.phobos.JavaPhobosTestCase;
import de.tum.cit.ase.ares.api.phobos.Phobos;
import de.tum.cit.ase.ares.api.policy.policySubComponents.FailureFeedbackConfiguration;
import de.tum.cit.ase.ares.api.policy.policySubComponents.InputOutputManagerKind;
import de.tum.cit.ase.ares.api.policy.policySubComponents.OutputConfiguration;
import de.tum.cit.ase.ares.api.policy.policySubComponents.StrictTimeoutConfiguration;
import de.tum.cit.ase.ares.api.policy.policySubComponents.TestBehaviorConfiguration;
import de.tum.cit.ase.ares.api.policy.policySubComponents.VisibilityAndScheduleConfiguration;
import de.tum.cit.ase.ares.api.util.FileTools;

@DisplayName("JavaWriter Tests")
public class JavaWriterTest {

	private JavaWriter javaWriter;
	private BuildMode buildMode;
	private ArchitectureMode architectureMode;
	private AOPMode aopMode;
	private List<String> essentialPackages;
	private List<String> essentialClasses;
	private List<String> testClasses;
	private String packageName;
	private String mainClassInPackageName;
	private List<JavaArchitectureTestCase> javaArchitectureTestCases;
	private List<JavaAOPTestCase> javaAOPTestCases;
	private List<JavaPhobosTestCase> javaPhobosTestCases;
	/** A behaviour configuration with nothing configured. */
	private TestBehaviorConfiguration emptyTestBehaviorConfiguration;

	@TempDir
	Path tempDir;

	@BeforeEach
	void setUp() throws IOException {
		Files.writeString(tempDir.resolve("pom.xml"), "<project/>");
		Files.writeString(tempDir.resolve("build.gradle"), "plugins { id 'java' }");
		javaWriter = new JavaWriter(tempDir);
		buildMode = BuildMode.MAVEN;
		architectureMode = mock(ArchitectureMode.class);
		aopMode = mock(AOPMode.class);
		essentialPackages = List.of("java.lang", "java.util");
		essentialClasses = List.of("java.lang.String", "java.util.List");
		testClasses = List.of("TestClass1", "TestClass2");
		packageName = "com.example";
		mainClassInPackageName = "MainClass";
		javaArchitectureTestCases = List.of(mock(JavaArchitectureTestCase.class), mock(JavaArchitectureTestCase.class));
		javaAOPTestCases = List.of(mock(JavaAOPTestCase.class), mock(JavaAOPTestCase.class));
		javaPhobosTestCases = List.of(mock(JavaPhobosTestCase.class), mock(JavaPhobosTestCase.class));
		emptyTestBehaviorConfiguration = TestBehaviorConfiguration.builder().build();
	}

	/**
	 * Writes resource bundles where the generated namespace can load both locales.
	 */
	@Test
	void writesLocalisationIntoTheGeneratedNamespace() throws IOException {
		Path sourceRoot = Files.createDirectories(tempDir.resolve("src/test/java"));
		javaWriter.writeTestCases(BuildMode.MAVEN, ArchitectureMode.ARCHUNIT, AOPMode.ASPECTJ, List.of(), List.of(),
				List.of(), "exercise.security", "Main", List.of(), List.of(), List.of(), sourceRoot);
		Path bundles = tempDir.resolve("src/test/resources/exercise/security/ares/api/localization");
		for (String name : List.of("messages.properties", "messages_de.properties")) {
			var messages = new java.util.Properties();
			try (var input = Files.newInputStream(bundles.resolve(name))) {
				messages.load(input);
			}
			assertNotNull(messages.getProperty("security.advice.trusted.startup.missing"));
			assertNotNull(messages.getProperty("security.advice.startup.properties.unavailable"));
			assertNotNull(messages.getProperty("security.advice.temp.directory.holder.unreadable"));
			assertNotNull(messages.getProperty("security.advice.denial.reason.not.in.allowlist"));
		}
	}

	/**
	 * A package name that is not a Java package, such as an absolute path inside
	 * the project, is refused before anything is written, so it cannot steer the
	 * generated files out of their directories.
	 */
	@Test
	void refusesAPackageNameThatIsNotAJavaPackage() throws IOException {
		Path sourceRoot = Files.createDirectories(tempDir.resolve("src/test/java"));
		String escaping = tempDir.resolve("src/main/java").toString();
		assertThrows(IllegalArgumentException.class,
				() -> javaWriter.writeTestCases(BuildMode.MAVEN, ArchitectureMode.ARCHUNIT, AOPMode.ASPECTJ, List.of(),
						List.of(), List.of(), escaping, "Main", List.of(), List.of(), List.of(), sourceRoot));
		assertFalse(Files.exists(tempDir.resolve("src/test/resources")));
		assertFalse(Files.exists(tempDir.resolve("src/main")));
	}

	@Nested
	@DisplayName("writeTestCases() Tests")
	class WriteTestCasesTests {
		@Test
		void confinesRelativeAbsoluteTraversalSiblingAndSymlinkTargets() throws IOException {
			Path inside = Files.createDirectories(tempDir.resolve("inside"));
			assertEquals(inside.toRealPath(), javaWriter.confineToProject(Path.of("inside")));
			assertEquals(inside.toRealPath(), javaWriter.confineToProject(inside));
			assertThrows(SecurityException.class, () -> javaWriter.confineToProject(Path.of("../outside")));
			Path siblingPrefix = Files.createDirectory(tempDir.resolveSibling(tempDir.getFileName() + "-sibling"));
			assertThrows(SecurityException.class, () -> javaWriter.confineToProject(siblingPrefix));

			Path external = Files.createDirectory(tempDir.resolveSibling(tempDir.getFileName() + "-external"));
			Path link = tempDir.resolve("external-link");
			try {
				Files.createSymbolicLink(link, external);
				assertThrows(SecurityException.class, () -> javaWriter.confineToProject(link.resolve("new-file")));
			} catch (UnsupportedOperationException exception) {
				org.junit.jupiter.api.Assumptions.abort("Symbolic links are unsupported on this platform");
			}
		}

		@Test
		void rejectsASelectedBuildModeWithoutItsDescriptor() throws IOException {
			Files.delete(tempDir.resolve("pom.xml"));
			assertThrows(IllegalStateException.class,
					() -> javaWriter.writeTestCases(BuildMode.MAVEN, architectureMode, aopMode, essentialPackages,
							essentialClasses, testClasses, packageName, mainClassInPackageName,
							javaArchitectureTestCases, javaAOPTestCases, javaPhobosTestCases,
							emptyTestBehaviorConfiguration, tempDir));
		}

		@Test
		@DisplayName("Should implement Writer interface")
		void shouldImplementWriterInterface() {
			// Assert
			assertTrue(javaWriter instanceof Writer);
		}

		@Test
		@DisplayName("Should reject a null test folder before creating any files")
		void shouldRejectNullTestFolderBeforeCreatingFiles() {
			NullPointerException failure = assertThrows(NullPointerException.class,
					() -> javaWriter.writeTestCases(buildMode, architectureMode, aopMode, essentialPackages,
							essentialClasses, testClasses, packageName, mainClassInPackageName,
							javaArchitectureTestCases, javaAOPTestCases, javaPhobosTestCases,
							emptyTestBehaviorConfiguration, null));
			assertEquals("testFolderPath must not be null", failure.getMessage());
			verifyNoInteractions(architectureMode, aopMode);
		}

		@Test
		@DisplayName("Should write test cases and return file paths")
		void shouldWriteTestCasesAndReturnFilePaths() {
			try (MockedStatic<FileTools> mockedFileTools = mockStatic(FileTools.class);
					MockedStatic<Phobos> mockedPhobos = mockStatic(Phobos.class)) {
				// Arrange
				stubArchitectureModeDefaults();
				stubAopModeDefaults();
				stubFileToolsDefaults(mockedFileTools);
				stubPhobosDefaults(mockedPhobos);

				// Act
				List<Path> result = javaWriter.writeTestCases(buildMode, architectureMode, aopMode, essentialPackages,
						essentialClasses, testClasses, packageName, mainClassInPackageName, javaArchitectureTestCases,
						javaAOPTestCases, javaPhobosTestCases, emptyTestBehaviorConfiguration, tempDir);

				// Assert
				assertNotNull(result);
				assertEquals(3, result.size()); // One from arch, one from AOP, one from Phobos

				// Verify mocks were called correctly
				verify(architectureMode).fsFilesToCopy();
				verify(architectureMode).nonFSFilesToCopy();
				verify(architectureMode).fsTargetsToCopyTo(any());
				verify(architectureMode).nonFSTargetsToCopyTo(any());
				verify(architectureMode).placeholderValues();
				verify(architectureMode).fsFormatValues(packageName, mainClassInPackageName);
				verify(architectureMode).nonFSFormatValues(packageName, mainClassInPackageName);
				verify(architectureMode).threePartedFileHeader();
				verify(architectureMode).threePartedFileBody(javaArchitectureTestCases);
				verify(architectureMode).threePartedFileFooter();
				verify(architectureMode).targetToCopyTo(any());
				verify(architectureMode).formatValues(packageName);

				verify(aopMode).fsFilesToCopy();
				verify(aopMode).nonFSFilesToCopy();
				verify(aopMode).fsTargetsToCopyTo(any());
				verify(aopMode).nonFSTargetsToCopyTo(any());
				verify(aopMode).placeholderValues();
				verify(aopMode).fsFormatValues(packageName, mainClassInPackageName);
				verify(aopMode).nonFSFormatValues(packageName, mainClassInPackageName);
				verify(aopMode).threePartedFileHeader();
				verify(aopMode).threePartedFileFooter();
				verify(aopMode).targetToCopyTo(any());
				verify(aopMode).formatValues(packageName);
			}
		}

		// Removed null-path test; JavaWriter delegates to FileTools.resolveOnPath which
		// requires non-null Path

		@Test
		@DisplayName("Should handle empty lists")
		void shouldHandleEmptyLists() {
			try (MockedStatic<FileTools> mockedFileTools = mockStatic(FileTools.class);
					MockedStatic<Phobos> mockedPhobos = mockStatic(Phobos.class)) {
				// Arrange
				List<String> emptyPackages = List.of();
				List<String> emptyClasses = List.of();
				List<String> emptyTestClasses = List.of();
				List<JavaArchitectureTestCase> emptyArchTestCases = List.of();
				List<JavaAOPTestCase> emptyAOPTestCases = List.of();

				stubArchitectureModeDefaults();
				stubAopModeDefaults();
				stubFileToolsDefaults(mockedFileTools);
				stubPhobosDefaults(mockedPhobos);

				// Act
				List<Path> result = javaWriter.writeTestCases(buildMode, architectureMode, aopMode, emptyPackages,
						emptyClasses, emptyTestClasses, packageName, mainClassInPackageName, emptyArchTestCases,
						emptyAOPTestCases, javaPhobosTestCases, emptyTestBehaviorConfiguration, tempDir);

				// Assert
				assertNotNull(result);
				verify(architectureMode).threePartedFileBody(emptyArchTestCases);
				verify(aopMode).threePartedFileBody(eq("INSTRUMENTATION"), eq(packageName), any(), any(),
						eq(emptyAOPTestCases));
			}
		}

		@Test
		@DisplayName("Should keep essential packages apart and merge essential and test classes")
		void shouldMergeEssentialClassesAndTestClassesCorrectly() {
			try (MockedStatic<FileTools> mockedFileTools = mockStatic(FileTools.class);
					MockedStatic<Phobos> mockedPhobos = mockStatic(Phobos.class)) {
				stubArchitectureModeDefaults();
				stubAopModeDefaults();
				stubFileToolsDefaults(mockedFileTools);
				stubPhobosDefaults(mockedPhobos);

				// Act
				javaWriter.writeTestCases(buildMode, architectureMode, aopMode, essentialPackages, essentialClasses,
						testClasses, packageName, mainClassInPackageName, javaArchitectureTestCases, javaAOPTestCases,
						javaPhobosTestCases, emptyTestBehaviorConfiguration, tempDir);

				verify(aopMode).threePartedFileBody(eq("INSTRUMENTATION"), eq(packageName), eq(essentialPackages),
						argThat(list -> {
							List<String> allowedClasses = (List<String>) list;
							return allowedClasses.containsAll(essentialClasses)
									&& allowedClasses.containsAll(testClasses)
									&& allowedClasses.stream().noneMatch(essentialPackages::contains)
									&& allowedClasses.size() == essentialClasses.size() + testClasses.size();
						}), eq(javaAOPTestCases));
			}
		}

		@Test
		@DisplayName("Should handle different build modes")
		void shouldHandleDifferentBuildModes() {
			try (MockedStatic<FileTools> mockedFileTools = mockStatic(FileTools.class);
					MockedStatic<Phobos> mockedPhobos = mockStatic(Phobos.class)) {
				stubArchitectureModeDefaults();
				stubAopModeDefaults();
				stubFileToolsDefaults(mockedFileTools);
				stubPhobosDefaults(mockedPhobos);

				// Act & Assert - should work with different build modes
				assertDoesNotThrow(() -> {
					javaWriter.writeTestCases(BuildMode.GRADLE, architectureMode, aopMode, essentialPackages,
							essentialClasses, testClasses, packageName, mainClassInPackageName,
							javaArchitectureTestCases, javaAOPTestCases, javaPhobosTestCases,
							emptyTestBehaviorConfiguration, tempDir);
				});
			}
		}

		/** With nothing configured, no settings class is written. */
		@Test
		@DisplayName("Should write nothing extra when no behavioural category is configured")
		void shouldWriteNothingExtraWhenTestBehaviorEmpty() {
			try (MockedStatic<FileTools> mockedFileTools = mockStatic(FileTools.class);
					MockedStatic<Phobos> mockedPhobos = mockStatic(Phobos.class)) {
				stubArchitectureModeDefaults();
				stubAopModeDefaults();
				stubFileToolsDefaults(mockedFileTools);
				stubPhobosDefaults(mockedPhobos);

				List<Path> result = javaWriter.writeTestCases(buildMode, architectureMode, aopMode, essentialPackages,
						essentialClasses, testClasses, packageName, mainClassInPackageName, javaArchitectureTestCases,
						javaAOPTestCases, javaPhobosTestCases, emptyTestBehaviorConfiguration, tempDir);

				assertEquals(3, result.size());
				assertFalse(Files.exists(generatedSettingsClassPath()));
			}
		}

		/** The released signature still writes its outputs and no settings class. */
		@Test
		@DisplayName("Should write the settings class and the hidden-test hook when the category is configured")
		void shouldWriteTheHiddenTestsHookWhenConfigured() throws IOException {
			try (MockedStatic<FileTools> mockedFileTools = mockStatic(FileTools.class);
					MockedStatic<Phobos> mockedPhobos = mockStatic(Phobos.class)) {
				stubArchitectureModeDefaults();
				stubAopModeDefaults();
				stubFileToolsDefaults(mockedFileTools);
				stubPhobosDefaults(mockedPhobos);

				List<Path> result = writeWith(hiddenTestsConfigured());

				assertEquals(8, result.size(), "3 released outputs, the settings class, 2 sources, 2 registrations");
				assertTrue(Files.readString(generatedSettingsClassPath())
						.contains("REGARDING_THE_VISIBILITY_AND_SCHEDULE_THE_DEADLINE = 946684800000L;"));
				assertTrue(Files.exists(generatedSettingsClassPath().resolveSibling("GeneratedHiddenTests.java")));
			}
		}

		@Test
		@DisplayName("Should remove the hidden-test hook when regenerated without the category")
		void shouldRemoveTheHiddenTestsHookWhenRegeneratedWithout() {
			try (MockedStatic<FileTools> mockedFileTools = mockStatic(FileTools.class);
					MockedStatic<Phobos> mockedPhobos = mockStatic(Phobos.class)) {
				stubArchitectureModeDefaults();
				stubAopModeDefaults();
				stubFileToolsDefaults(mockedFileTools);
				stubPhobosDefaults(mockedPhobos);
				writeWith(hiddenTestsConfigured());

				List<Path> result = writeWith(emptyTestBehaviorConfiguration);

				assertEquals(3, result.size());
				assertFalse(Files.exists(generatedSettingsClassPath()));
				assertFalse(Files.exists(generatedSettingsClassPath().resolveSibling("GeneratedHiddenTests.java")));
			}
		}

		/**
		 * A refused setting stops the generator before it changes any earlier output,
		 * so a refused run leaves the previous hooks and their settings as they were.
		 *
		 * @param refused the configuration the generator refuses.
		 * @throws IOException if the settings class cannot be read
		 */
		@ParameterizedTest
		@MethodSource("refusedConfigurations")
		@DisplayName("Should change nothing when the generator refuses a setting")
		void shouldChangeNothingWhenASettingIsRefused(TestBehaviorConfiguration refused) throws IOException {
			try (MockedStatic<FileTools> mockedFileTools = mockStatic(FileTools.class);
					MockedStatic<Phobos> mockedPhobos = mockStatic(Phobos.class)) {
				stubArchitectureModeDefaults();
				stubAopModeDefaults();
				stubFileToolsDefaults(mockedFileTools);
				stubPhobosDefaults(mockedPhobos);
				writeWith(hiddenTestsConfigured());
				String before = Files.readString(generatedSettingsClassPath());
				clearInvocations(architectureMode, aopMode);

				assertThrows(SecurityException.class, () -> writeWith(refused));

				assertEquals(before, Files.readString(generatedSettingsClassPath()));
				assertTrue(Files.exists(generatedSettingsClassPath().resolveSibling("GeneratedHiddenTests.java")));
				verifyNoInteractions(architectureMode, aopMode);
			}
		}

		/**
		 * Settings the generator refuses: a hidden entry naming no test, and a custom
		 * I/O manager.
		 *
		 * @return the configurations.
		 */
		static Stream<TestBehaviorConfiguration> refusedConfigurations() {
			return Stream.of(
					TestBehaviorConfiguration.builder()
							.regardingTheVisibilityAndScheduleOfTheTestCases(VisibilityAndScheduleConfiguration
									.builder().theDeadlineIs("2000-01-01 00:00 UTC").testsNotListedHereAreHidden(false)
									.hiddenTests("com.example.Nothing#missing").build())
							.build(),
					TestBehaviorConfiguration.builder()
							.regardingTheOutputOfTheTestCases(OutputConfiguration.builder()
									.theInputAndOutputAreManagedBy(InputOutputManagerKind.CUSTOM)
									.theCustomInputAndOutputManagerIs("com.example.Manager").build())
							.build());
		}

		/**
		 * Runs the configuration-aware {@code writeTestCases} into {@code tempDir} with
		 * this class's fixed inputs.
		 *
		 * @param testBehaviorConfiguration the behaviour configuration to write.
		 * @return the written files.
		 */
		private List<Path> writeWith(TestBehaviorConfiguration testBehaviorConfiguration) {
			return javaWriter.writeTestCases(buildMode, architectureMode, aopMode, essentialPackages, essentialClasses,
					testClasses, packageName, mainClassInPackageName, javaArchitectureTestCases, javaAOPTestCases,
					javaPhobosTestCases, testBehaviorConfiguration, tempDir);
		}

		/**
		 * A configuration with a deadline at the start of 2000 and no hidden tests.
		 *
		 * @return the configuration.
		 */
		private TestBehaviorConfiguration hiddenTestsConfigured() {
			return TestBehaviorConfiguration
					.builder().regardingTheVisibilityAndScheduleOfTheTestCases(VisibilityAndScheduleConfiguration
							.builder().theDeadlineIs("2000-01-01 00:00 UTC").testsNotListedHereAreHidden(false).build())
					.build();
		}

		@Test
		@DisplayName("Should write the settings class and the strict-timeout hooks when a strict timeout is configured")
		void shouldWriteTheStrictTimeoutHooksWhenConfigured() throws IOException {
			try (MockedStatic<FileTools> mockedFileTools = mockStatic(FileTools.class);
					MockedStatic<Phobos> mockedPhobos = mockStatic(Phobos.class)) {
				stubArchitectureModeDefaults();
				stubAopModeDefaults();
				stubFileToolsDefaults(mockedFileTools);
				stubPhobosDefaults(mockedPhobos);

				List<Path> result = writeWith(strictTimeoutConfigured());

				assertEquals(8, result.size(), "3 released outputs, the settings class, 2 sources, 2 registrations");
				assertTrue(Files.readString(generatedSettingsClassPath())
						.contains("REGARDING_THE_STRICT_TIMEOUT_THE_TIMEOUT_NANOS = 2000000000L;"));
				assertTrue(Files.exists(generatedSettingsClassPath().resolveSibling("GeneratedStrictTimeout.java")));
			}
		}

		@Test
		@DisplayName("Should remove the strict-timeout hooks when regenerated without the setting")
		void shouldRemoveTheStrictTimeoutHooksWhenRegeneratedWithout() {
			try (MockedStatic<FileTools> mockedFileTools = mockStatic(FileTools.class);
					MockedStatic<Phobos> mockedPhobos = mockStatic(Phobos.class)) {
				stubArchitectureModeDefaults();
				stubAopModeDefaults();
				stubFileToolsDefaults(mockedFileTools);
				stubPhobosDefaults(mockedPhobos);
				writeWith(strictTimeoutConfigured());

				List<Path> result = writeWith(emptyTestBehaviorConfiguration);

				assertEquals(3, result.size());
				assertFalse(Files.exists(generatedSettingsClassPath()));
				assertFalse(Files.exists(generatedSettingsClassPath().resolveSibling("GeneratedStrictTimeout.java")));
			}
		}

		/**
		 * A configuration with a two-second strict timeout.
		 *
		 * @return the configuration.
		 */
		private TestBehaviorConfiguration strictTimeoutConfigured() {
			return TestBehaviorConfiguration.builder().regardingTheStrictTimeoutForTheTestCases(
					StrictTimeoutConfiguration.builder().theTimeoutIs("2s").build()).build();
		}

		@Test
		@DisplayName("Should write the settings class and the output-mirroring hook when the category is configured")
		void shouldWriteTheOutputMirroringHookWhenConfigured() throws IOException {
			try (MockedStatic<FileTools> mockedFileTools = mockStatic(FileTools.class);
					MockedStatic<Phobos> mockedPhobos = mockStatic(Phobos.class)) {
				stubArchitectureModeDefaults();
				stubAopModeDefaults();
				stubFileToolsDefaults(mockedFileTools);
				stubPhobosDefaults(mockedPhobos);

				List<Path> result = writeWith(outputMirroringConfigured());

				assertEquals(8, result.size(), "3 released outputs, the settings class, 2 sources, 2 registrations");
				assertTrue(Files.readString(generatedSettingsClassPath())
						.contains("REGARDING_THE_OUTPUT_THE_MAXIMUM_NUMBER_OF_OUTPUT_CHARACTERS = 10L;"));
				assertTrue(Files.exists(generatedSettingsClassPath().resolveSibling("GeneratedOutputMirroring.java")));
			}
		}

		@Test
		@DisplayName("Should remove the output-mirroring hook when regenerated without the category")
		void shouldRemoveTheOutputMirroringHookWhenRegeneratedWithout() {
			try (MockedStatic<FileTools> mockedFileTools = mockStatic(FileTools.class);
					MockedStatic<Phobos> mockedPhobos = mockStatic(Phobos.class)) {
				stubArchitectureModeDefaults();
				stubAopModeDefaults();
				stubFileToolsDefaults(mockedFileTools);
				stubPhobosDefaults(mockedPhobos);
				writeWith(outputMirroringConfigured());

				List<Path> result = writeWith(emptyTestBehaviorConfiguration);

				assertEquals(3, result.size());
				assertFalse(Files.exists(generatedSettingsClassPath()));
				assertFalse(Files.exists(generatedSettingsClassPath().resolveSibling("GeneratedOutputMirroring.java")));
			}
		}

		/**
		 * A configuration limiting output to 10 per stream.
		 *
		 * @return the configuration.
		 */
		private TestBehaviorConfiguration outputMirroringConfigured() {
			return TestBehaviorConfiguration.builder().regardingTheOutputOfTheTestCases(
					OutputConfiguration.builder().theMaximumNumberOfOutputCharactersIs(10L).build()).build();
		}

		@Test
		@DisplayName("Should still write the released outputs when called through the pre-release signature directly")
		void preReleaseSignatureStillWritesTheReleasedOutputs() {
			try (MockedStatic<FileTools> mockedFileTools = mockStatic(FileTools.class);
					MockedStatic<Phobos> mockedPhobos = mockStatic(Phobos.class)) {
				stubArchitectureModeDefaults();
				stubAopModeDefaults();
				stubFileToolsDefaults(mockedFileTools);
				stubPhobosDefaults(mockedPhobos);

				List<Path> result = javaWriter.writeTestCases(buildMode, architectureMode, aopMode, essentialPackages,
						essentialClasses, testClasses, packageName, mainClassInPackageName, javaArchitectureTestCases,
						javaAOPTestCases, javaPhobosTestCases, tempDir);

				assertEquals(3, result.size());
				assertFalse(Files.exists(generatedSettingsClassPath()));
			}
		}

		/**
		 * Where the generated settings class lands in {@code tempDir}.
		 *
		 * @return the path of the generated settings class.
		 */
		private Path generatedSettingsClassPath() {
			String fullyQualifiedName = TestBehaviorConfiguration.GENERATED_CLASS_NAME;
			int lastDot = fullyQualifiedName.lastIndexOf('.');
			String settingsPackagePath = fullyQualifiedName.substring(0, lastDot).replace('.', '/');
			String simpleClassName = fullyQualifiedName.substring(lastDot + 1);
			return tempDir.resolve(settingsPackagePath).resolve(simpleClassName + ".java");
		}

		/**
		 * A subclass overriding only the released overload is still called through the
		 * configuration-aware one.
		 */
		@Test
		@DisplayName("Should invoke a subclass override of the released overload before adding behaviour outputs")
		void configurationAwareOverloadInvokesASubclassOverrideOfTheReleasedOneFirst() {
			class LegacySubclassWriter extends JavaWriter {

				private boolean releasedOverloadInvoked;

				LegacySubclassWriter(Path projectRoot) {
					super(projectRoot);
				}

				@Override
				public List<Path> writeTestCases(BuildMode buildMode, ArchitectureMode architectureMode,
						AOPMode aopMode, List<String> essentialPackages, List<String> essentialClasses,
						List<String> testClasses, String packageName, String mainClassInPackageName,
						List<JavaArchitectureTestCase> javaArchitectureTestCases,
						List<JavaAOPTestCase> javaAOPTestCases, List<JavaPhobosTestCase> phobosTestCases,
						Path testFolderPath) {
					releasedOverloadInvoked = true;
					return List.of();
				}
			}

			LegacySubclassWriter legacySubclassWriter = new LegacySubclassWriter(tempDir);

			List<Path> result = legacySubclassWriter.writeTestCases(buildMode, architectureMode, aopMode,
					essentialPackages, essentialClasses, testClasses, packageName, mainClassInPackageName,
					javaArchitectureTestCases, javaAOPTestCases, javaPhobosTestCases, emptyTestBehaviorConfiguration,
					tempDir);

			assertTrue(legacySubclassWriter.releasedOverloadInvoked);
			assertEquals(List.of(), result);
		}

		/**
		 * A configured category is written as literal constants into the generated
		 * settings class.
		 */
		@Test
		@DisplayName("Should write the generated test-behaviour settings class when configured")
		void shouldWriteGeneratedSettingsClassWhenPrivilegedExceptionsConfigured() throws IOException {
			try (MockedStatic<FileTools> mockedFileTools = mockStatic(FileTools.class);
					MockedStatic<Phobos> mockedPhobos = mockStatic(Phobos.class)) {
				stubArchitectureModeDefaults();
				stubAopModeDefaults();
				stubFileToolsDefaults(mockedFileTools);
				stubPhobosDefaults(mockedPhobos);

				List<Path> result = writeWith(privilegedExceptionsConfigured());

				assertEquals(8, result.size(), "3 released outputs, the settings class and 4 failure-reporting files");
				assertTrue(result.stream().anyMatch(path -> path.endsWith("GeneratedFailureReporting.java")));
				Path written = generatedSettingsClassPath();
				assertTrue(Files.exists(written));
				String content = Files.readString(written);
				assertTrue(content.contains("public static final boolean "
						+ TestBehaviorConfiguration.FAILURE_FEEDBACK_PRIVILEGED_ONLY_FIELD_NAME + " = true;"));
				assertTrue(content.contains("public static final String "
						+ TestBehaviorConfiguration.FAILURE_FEEDBACK_MESSAGE_FIELD_NAME + " = \"Custom message\";"));
			}
		}

		/**
		 * Regenerating after the setting was removed deletes the class the earlier run
		 * wrote, so the removed setting does not stay in force.
		 */
		@Test
		@DisplayName("Should delete an earlier generated settings class when regenerated without configuration")
		void regeneratingWithoutConfigurationDeletesTheEarlierSettingsClass() {
			try (MockedStatic<FileTools> mockedFileTools = mockStatic(FileTools.class);
					MockedStatic<Phobos> mockedPhobos = mockStatic(Phobos.class)) {
				stubArchitectureModeDefaults();
				stubAopModeDefaults();
				stubFileToolsDefaults(mockedFileTools);
				stubPhobosDefaults(mockedPhobos);
				writeWith(privilegedExceptionsConfigured());
				assertTrue(Files.exists(generatedSettingsClassPath()));

				List<Path> result = writeWith(emptyTestBehaviorConfiguration);

				assertEquals(3, result.size());
				assertFalse(Files.exists(generatedSettingsClassPath()));
				assertFalse(
						Files.exists(generatedSettingsClassPath().resolveSibling("GeneratedFailureReporting.java")));
			}
		}

		/**
		 * With a known build layout, regenerating without configuration also deletes
		 * the compiled settings class an earlier build left in the test output.
		 */
		@Test
		@DisplayName("Should delete a stale compiled settings class from the test output when regenerated without configuration")
		void regeneratingWithoutConfigurationDeletesTheStaleCompiledSettingsClass() throws IOException {
			for (String directory : List.of("src/main/java", "src/test/java", "target/classes",
					"target/test-classes")) {
				Files.createDirectories(tempDir.resolve(directory));
			}
			BuildToolConfiguration mavenLayout = new BuildToolConfiguration(BuildMode.MAVEN, tempDir,
					List.of(tempDir.resolve("src/main/java")), List.of(tempDir.resolve("src/test/java")),
					tempDir.resolve("target/classes"), tempDir.resolve("target/test-classes"));
			Path staleClass = tempDir.resolve("target/test-classes")
					.resolve(TestBehaviorConfiguration.GENERATED_CLASS_NAME.replace('.', '/') + ".class");
			Files.createDirectories(staleClass.getParent());
			Files.write(staleClass, new byte[] { (byte) 0xCA, (byte) 0xFE });
			try (MockedStatic<FileTools> mockedFileTools = mockStatic(FileTools.class);
					MockedStatic<Phobos> mockedPhobos = mockStatic(Phobos.class)) {
				stubArchitectureModeDefaults();
				stubAopModeDefaults();
				stubFileToolsDefaults(mockedFileTools);
				stubPhobosDefaults(mockedPhobos);

				new JavaWriter(mavenLayout).writeTestCases(buildMode, architectureMode, aopMode, essentialPackages,
						essentialClasses, testClasses, packageName, mainClassInPackageName, javaArchitectureTestCases,
						javaAOPTestCases, javaPhobosTestCases, emptyTestBehaviorConfiguration,
						tempDir.resolve("src/test/java"));

				assertFalse(Files.exists(staleClass));
			}
		}

		/**
		 * A configuration enabling privileged-exceptions-only reporting with the
		 * message {@code Custom message}.
		 *
		 * @return the configuration.
		 */
		private TestBehaviorConfiguration privilegedExceptionsConfigured() {
			return TestBehaviorConfiguration.builder()
					.regardingTheFailureFeedbackForTheTestCases(
							FailureFeedbackConfiguration.builder().onlyPrivilegedExceptionsAreReported(true)
									.theFailureMessageForOtherExceptionsIs("Custom message").build())
					.build();
		}
	}

	private void stubArchitectureModeDefaults() {
		when(architectureMode.fsFilesToCopy()).thenReturn(List.of());
		when(architectureMode.nonFSFilesToCopy()).thenReturn(List.of());
		when(architectureMode.fsTargetsToCopyTo(any())).thenReturn(List.of());
		when(architectureMode.nonFSTargetsToCopyTo(any())).thenReturn(List.of());
		when(architectureMode.placeholderValues()).thenReturn(List.of());
		when(architectureMode.fsFormatValues(any(), any()))
				.thenReturn(List.<String[]>of(new String[] { "pkg", "pkg", "Main" }));
		when(architectureMode.nonFSFormatValues(any(), any()))
				.thenReturn(List.<String[]>of(new String[] { "pkg", "pkg", "Main" }));
		when(architectureMode.threePartedFileHeader()).thenReturn(tempDir.resolve("header.java"));
		when(architectureMode.threePartedFileBody(any())).thenReturn("body");
		when(architectureMode.threePartedFileFooter()).thenReturn(tempDir.resolve("footer.java"));
		when(architectureMode.targetToCopyTo(any())).thenReturn(tempDir.resolve("arch.java"));
		when(architectureMode.formatValues(any())).thenReturn(new String[] { "pkg", "pkg" });
	}

	private void stubAopModeDefaults() {
		when(aopMode.fsFilesToCopy()).thenReturn(List.of());
		when(aopMode.nonFSFilesToCopy()).thenReturn(List.of());
		when(aopMode.fsTargetsToCopyTo(any())).thenReturn(List.of());
		when(aopMode.nonFSTargetsToCopyTo(any())).thenReturn(List.of());
		when(aopMode.placeholderValues()).thenReturn(List.of());
		when(aopMode.fsFormatValues(any(), any())).thenReturn(List.<String[]>of(new String[] { "pkg", "pkg", "Main" }));
		when(aopMode.nonFSFormatValues(any(), any()))
				.thenReturn(List.<String[]>of(new String[] { "pkg", "pkg", "Main" }));
		when(aopMode.threePartedFileHeader()).thenReturn(tempDir.resolve("header.java"));
		when(aopMode.threePartedFileBody(any(), any(), any(), any(), any())).thenReturn("body");
		when(aopMode.threePartedFileFooter()).thenReturn(tempDir.resolve("footer.java"));
		when(aopMode.targetToCopyTo(any())).thenReturn(tempDir.resolve("aop.java"));
		when(aopMode.formatValues(any())).thenReturn(new String[] { "pkg" });
		when(aopMode.toString()).thenReturn("INSTRUMENTATION");
	}

	private void stubFileToolsDefaults(MockedStatic<FileTools> mockedFileTools) {
		mockedFileTools.when(() -> FileTools.copyAndFormatFSFiles(any(), any(), any())).thenReturn(List.of());
		mockedFileTools.when(() -> FileTools.copyAndFormatNonFSFiles(any(), any(), any(), any())).thenReturn(List.of());
		mockedFileTools.when(() -> FileTools.createThreePartedFormatStringFile(any(), any(), any(), any(), any()))
				.thenReturn(tempDir.resolve("test.java"));
		mockedFileTools.when(() -> FileTools.copyFiles(any(), any())).thenReturn(List.of());
	}

	private void stubPhobosDefaults(MockedStatic<Phobos> mockedPhobos) {
		mockedPhobos.when(() -> Phobos.filesToCopy()).thenReturn(List.of());
		mockedPhobos.when(() -> Phobos.targetsToCopyTo(any())).thenReturn(List.of());
		mockedPhobos.when(() -> Phobos.threePartedFileHeader()).thenReturn(tempDir.resolve("phobos-header.java"));
		mockedPhobos.when(() -> Phobos.threePartedFileBody(any())).thenReturn("phobos-body");
		mockedPhobos.when(() -> Phobos.threePartedFileFooter()).thenReturn(tempDir.resolve("phobos-footer.java"));
		mockedPhobos.when(() -> Phobos.targetToCopyTo(any())).thenReturn(tempDir.resolve("phobos-target.java"));
		mockedPhobos.when(() -> Phobos.fileValue(any())).thenReturn(new String[] { "value" });
	}
}
