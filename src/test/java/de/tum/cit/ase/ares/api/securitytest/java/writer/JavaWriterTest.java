package de.tum.cit.ase.ares.api.securitytest.java.writer;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.net.URI;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.MockedStatic;

import de.tum.cit.ase.ares.api.aop.AOPMode;
import de.tum.cit.ase.ares.api.aop.java.JavaAOPTestCase;
import de.tum.cit.ase.ares.api.aop.java.javaAOPTestCaseToolbox.JavaAOPTestCaseToolbox;
import de.tum.cit.ase.ares.api.architecture.ArchitectureMode;
import de.tum.cit.ase.ares.api.architecture.java.JavaArchitectureTestCase;
import de.tum.cit.ase.ares.api.buildtoolconfiguration.BuildMode;
import de.tum.cit.ase.ares.api.buildtoolconfiguration.BuildToolConfiguration;
import de.tum.cit.ase.ares.api.localization.AdviceActionWording;
import de.tum.cit.ase.ares.api.localization.Messages;
import de.tum.cit.ase.ares.api.phobos.JavaPhobosTestCase;
import de.tum.cit.ase.ares.api.phobos.Phobos;
import de.tum.cit.ase.ares.api.phobos.java.JavaPhobosTestCaseSupported;
import de.tum.cit.ase.ares.api.policy.SecurityPolicyReaderAndDirector;
import de.tum.cit.ase.ares.api.policy.policySubComponents.FilePermission;
import de.tum.cit.ase.ares.api.policy.policySubComponents.TestBehaviorConfiguration;
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
			assertNotNull(messages.getProperty("security.advice.denial.reason.target.not.allowed.file"));
		}
	}

	/**
	 * Writes the ArchUnit settings the generated checks rely on at the root of the
	 * test resources, which the build puts at the root of the test class path, the
	 * one place ArchUnit reads them from.
	 */
	@Test
	void writesTheArchunitSettingsWhereArchunitReadsThem() throws IOException {
		Path sourceRoot = Files.createDirectories(tempDir.resolve("src/test/java"));
		List<Path> returned = javaWriter.writeTestCases(BuildMode.MAVEN, ArchitectureMode.ARCHUNIT, AOPMode.ASPECTJ,
				List.of(), List.of(), List.of(), "exercise.security", "Main", List.of(), List.of(), List.of(),
				sourceRoot);
		Path settings = tempDir.resolve("src/test/resources/archunit.properties");
		java.util.Properties written = new java.util.Properties();
		try (var input = Files.newInputStream(settings)) {
			written.load(input);
		}
		assertEquals("false", written.getProperty("archRule.failOnEmptyShould"));
		assertEquals("false", written.getProperty("resolveMissingDependenciesFromClassPath"));
		assertTrue(returned.contains(settings.toRealPath()), returned.toString());
		assertFalse(Files.exists(tempDir.resolve("src/test/java/exercise/security/ares/api/archunit.properties")));
	}

	/**
	 * An instructor's own ArchUnit settings are kept line for line, their own value
	 * included, and only the settings the file does not name are added.
	 */
	@Test
	void keepsTheInstructorsArchunitSettingsAndAddsOnlyMissingOnes() throws IOException {
		Path sourceRoot = Files.createDirectories(tempDir.resolve("src/test/java"));
		Path settings = Files.createDirectories(tempDir.resolve("src/test/resources")).resolve("archunit.properties");
		List<String> instructorLines = List.of("# the instructor's own settings", "archRule.failOnEmptyShould=true",
				"freeze.store.default.path=frozen");
		Files.write(settings, instructorLines);
		for (int run = 0; run < 2; run++) {
			javaWriter.writeTestCases(BuildMode.MAVEN, ArchitectureMode.WALA, AOPMode.INSTRUMENTATION, List.of(),
					List.of(), List.of(), "exercise.security", "Main", List.of(), List.of(), List.of(), sourceRoot);
		}
		List<String> lines = Files.readAllLines(settings);
		assertEquals(instructorLines, lines.subList(0, instructorLines.size()));
		assertEquals(List.of("resolveMissingDependenciesFromClassPath=false"),
				lines.subList(instructorLines.size(), lines.size()));
	}

	/**
	 * Requires the returned paths to name every localisation bundle written below
	 * the resources folder, so the caller can list or clean them up.
	 */
	@Test
	void returnsTheCopiedLocalisationBundles() throws IOException {
		Path sourceRoot = Files.createDirectories(tempDir.resolve("src/test/java"));
		List<Path> returned = javaWriter.writeTestCases(BuildMode.MAVEN, ArchitectureMode.ARCHUNIT, AOPMode.ASPECTJ,
				List.of(), List.of(), List.of(), "exercise.security", "Main", List.of(), List.of(), List.of(),
				sourceRoot);
		List<Path> bundles;
		try (var walk = Files.walk(tempDir.resolve("src/test/resources/exercise/security"))) {
			bundles = walk.filter(Files::isRegularFile).toList();
		}
		assertFalse(bundles.isEmpty());
		Set<Path> returnedReal = new HashSet<>();
		for (Path path : returned) {
			if (path.toString().contains("localization")) {
				assertTrue(Files.exists(path), path.toString());
				returnedReal.add(path.toRealPath());
			}
		}
		for (Path bundle : bundles) {
			assertTrue(returnedReal.contains(bundle.toRealPath()), bundle.toString());
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

	/**
	 * A misspelled field in a policy without test behaviour stops precompile before
	 * anything is written. The refusal has stage Creation and names the unknown
	 * field and the known ones in its first line, in English and German.
	 */
	@Test
	void refusesAMisspelledPolicyFieldWithItsCauseAndWritesNoFiles() throws IOException {
		Path policy = Files.writeString(tempDir.resolve("SecurityPolicy.yaml"), """
				thisPolicyFileCompliesToThePolicyVersion: 1
				regardingTheSupervisedCode:
				  theFollowingProgrammingLanguageConfigurationIsUsed: JAVA_USING_MAVEN_ARCHUNIT_AND_ASPECTJ
				  theSupervisedCodeUsesTheFollowingPackage: com.example
				  theFollowingClassesAreTestClasses: []
				  theFollowingResourceAccessesArePermitted:
				    regardingFileSystemInteraction: []
				    regardingNetworkConnections: []
				    regardingCommandExecutions: []
				    regardingThreadCreations: []
				    regardingPackageImports: []
				    regardingTimeouts: []
				""");
		Path testFolder = Files.createDirectories(tempDir.resolve("src/test/java"));
		Set<Path> before = filesBelow(tempDir);
		for (Locale locale : List.of(Locale.ENGLISH, Locale.GERMAN)) {
			String message = AdviceActionWording.inLocale(locale,
					() -> assertThrows(SecurityException.class,
							() -> new SecurityPolicyReaderAndDirector(policy, tempDir).createTestCases()
									.writeTestCases(testFolder)).getMessage());
			String expected = AdviceActionWording.inLocale(locale,
					() -> Messages.localized("security.policy.data.bind.failed", policy.toString(),
							Messages.localized("policy.schema.unknown.field",
									"theFollowingResourceAccessesArePermitted", "regardingFileSystemInteraction",
									"regardingCommandExecutions, regardingFileSystemInteractions, "
											+ "regardingNetworkConnections, regardingPackageImports, "
											+ "regardingThreadCreations, regardingTimeouts")));
			assertEquals(expected, message, locale.toString());
			assertTrue(message.startsWith(
					Locale.GERMAN.equals(locale) ? "Ares Sicherheitsfehler (Grund: Ares-Code; Phase: Erstellung): "
							: "Ares Security Error (Reason: Ares-Code; Stage: Creation): "),
					message);
		}
		assertEquals(before, filesBelow(tempDir));
	}

	/**
	 * Lists every regular file below a folder.
	 *
	 * @param folder the folder.
	 * @return the files.
	 * @throws IOException if the folder cannot be walked.
	 */
	private static Set<Path> filesBelow(Path folder) throws IOException {
		try (Stream<Path> walk = Files.walk(folder)) {
			return walk.filter(Files::isRegularFile).collect(Collectors.toSet());
		}
	}

	/**
	 * Requires the Phobos configuration on disk to name each permitted path exactly
	 * as the policy does. A path holding {@code %n}, {@code %%}, {@code %s} or a
	 * lone {@code %} must reach the file unchanged, since a format string would
	 * turn the first into a line break that names a second path.
	 */
	@Test
	void writesPercentSignsIntoThePhobosConfigurationUnchanged() throws IOException {
		Path sourceRoot = Files.createDirectories(tempDir.resolve("src/test/java"));
		List<String> paths = List.of("/tmp/a%n/etc", "/tmp/b%%", "/tmp/c%s", "/tmp/d100%");
		List<JavaPhobosTestCase> phobos = List
				.of(JavaPhobosTestCase.builder()
						.javaPhobosTestCaseSupported(JavaPhobosTestCaseSupported.FILESYSTEM_INTERACTION)
						.resourceAccessSupplier(() -> paths.stream()
								.map(path -> new FilePermission(path, false, true, false, false, false)).toList())
						.build());
		javaWriter.writeTestCases(BuildMode.MAVEN, ArchitectureMode.ARCHUNIT, AOPMode.ASPECTJ, List.of(), List.of(),
				List.of(), "exercise.security", "Main", List.of(), List.of(), phobos, sourceRoot);
		String configuration = Files.readString(tempDir.resolve("SpecificExercise.cfg"));
		assertTrue(configuration.contains("[write]\n/tmp/a%n/etc\n/tmp/b%%\n/tmp/c%s\n/tmp/d100%\n"), configuration);
	}

	@Nested
	@DisplayName("writeTestCases() Tests")
	class WriteTestCasesTests {

		/**
		 * The English bundle below a resources folder, for the package
		 * exercise.security.
		 */
		private static final String LOCALISATION_BUNDLE = "resources/exercise/security/ares/api/localization/messages.properties";

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

		/**
		 * A path of another file system, here the Java runtime image, is refused as
		 * escaping the project, while an absolute path inside the project is accepted.
		 */
		@Test
		void refusesAPathOfAnotherFileSystem() throws IOException {
			Path inside = Files.createDirectories(tempDir.resolve("inside"));
			assertEquals(inside.toRealPath(), javaWriter.confineToProject(inside.toAbsolutePath()));
			Path runtimeImage = FileSystems.getFileSystem(URI.create("jrt:/")).getPath("/modules");
			SecurityException refused = assertThrows(SecurityException.class,
					() -> javaWriter.confineToProject(runtimeImage));
			assertEquals(Messages.localized("security.writer.target.outside.project", "/modules"),
					refused.getMessage());
		}

		/**
		 * A test folder three names below the file-system root gets its resources
		 * folder beside it, not inside it. The Phobos helper three folders up is then
		 * refused, after the files are written. It needs a project root two names deep,
		 * as a temporary folder under /tmp on Linux, and is skipped elsewhere.
		 */
		@Test
		void placesTheResourcesBesideATestFolderThreeNamesDeep() throws IOException {
			Path projectRoot = tempDir.toRealPath();
			assumeTrue(projectRoot.getNameCount() == 2, "needs a project root two names below the file-system root");
			Path testFolder = Files.createDirectories(projectRoot.resolve("src"));
			SecurityException refused = assertThrows(SecurityException.class, () -> writeRealFilesInto(testFolder));
			assertEquals(Messages.localized("security.writer.target.outside.project",
					testFolder.resolve("../../../PhobosCopyTool.sh")), refused.getMessage());
			assertTrue(Files.isRegularFile(projectRoot.resolve(LOCALISATION_BUNDLE)));
			assertFalse(Files.exists(testFolder.resolve("resources")));
		}

		/**
		 * A test folder with fewer than three names gets its resources folder inside
		 * it, as the writer then does not step up to the parent folder. The Phobos
		 * helper three folders up is then refused, after the bundles are written. It
		 * needs such a shallow project root and is skipped elsewhere.
		 */
		@Test
		void placesTheResourcesInsideATestFolderAtMostTwoNamesDeep() throws IOException {
			Path projectRoot = tempDir.toRealPath();
			assumeTrue(projectRoot.getNameCount() <= 2,
					"needs a project root at most two names below the file-system root");
			SecurityException refused = assertThrows(SecurityException.class, () -> writeRealFilesInto(projectRoot));
			assertEquals(Messages.localized("security.writer.target.outside.project",
					projectRoot.resolve("../../../PhobosCopyTool.sh")), refused.getMessage());
			assertTrue(Files.isRegularFile(projectRoot.resolve(LOCALISATION_BUNDLE)));
		}

		/**
		 * Writes the real ArchUnit and AspectJ files of the package
		 * {@code exercise.security} into the given test folder.
		 *
		 * @param testFolder the test folder to write into
		 * @return the written files
		 */
		private List<Path> writeRealFilesInto(Path testFolder) {
			return javaWriter.writeTestCases(BuildMode.MAVEN, ArchitectureMode.ARCHUNIT, AOPMode.ASPECTJ, List.of(),
					List.of(), List.of(), "exercise.security", "Main", List.of(), List.of(), List.of(), testFolder);
		}

		/**
		 * A Maven write into a project without a {@code pom.xml} is refused.
		 */
		@Test
		void rejectsASelectedBuildModeWithoutItsDescriptor() throws IOException {
			Files.delete(tempDir.resolve("pom.xml"));
			assertThrows(SecurityException.class,
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
						javaAOPTestCases, javaPhobosTestCases, emptyTestBehaviorConfiguration, testSourceRoot());

				// Assert
				assertNotNull(result);
				assertEquals(5, result.size()); // arch, AOP, Phobos, the guard registration and the ArchUnit settings

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
						emptyAOPTestCases, javaPhobosTestCases, emptyTestBehaviorConfiguration, testSourceRoot());

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
						javaPhobosTestCases, emptyTestBehaviorConfiguration, testSourceRoot());

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
							emptyTestBehaviorConfiguration, testSourceRoot());
				});
			}
		}

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
						javaAOPTestCases, javaPhobosTestCases, emptyTestBehaviorConfiguration, testSourceRoot());

				assertEquals(5, result.size());
				assertFalse(Files.exists(generatedSettingsClassPath()));
			}
		}

		/**
		 * The generated settings holder's constructor throws the catalogue message,
		 * escaped as a Java string literal.
		 */
		@Test
		@DisplayName("Should write a settings holder whose constructor refuses with the Ares error template")
		void writesASettingsHolderWhoseConstructorRefusesWithTheTemplate() throws IOException {
			TestBehaviorConfiguration configured = mock(TestBehaviorConfiguration.class);
			when(configured.literalFieldAssignments())
					.thenReturn(List.of("public static final int LIMIT = 1;" + System.lineSeparator()));
			try (MockedStatic<FileTools> mockedFileTools = mockStatic(FileTools.class);
					MockedStatic<Phobos> mockedPhobos = mockStatic(Phobos.class)) {
				stubArchitectureModeDefaults();
				stubAopModeDefaults();
				stubFileToolsDefaults(mockedFileTools);
				stubPhobosDefaults(mockedPhobos);

				javaWriter.writeTestCases(buildMode, architectureMode, aopMode, essentialPackages, essentialClasses,
						testClasses, packageName, mainClassInPackageName, javaArchitectureTestCases, javaAOPTestCases,
						javaPhobosTestCases, configured, testSourceRoot());

				String name = TestBehaviorConfiguration.GENERATED_CLASS_NAME;
				String expected = JavaAOPTestCaseToolbox.escapeJavaString(Messages.localized(
						"security.writer.settings.holder.instantiation", name.substring(name.lastIndexOf('.') + 1)));
				assertTrue(Files.readString(generatedSettingsClassPath())
						.contains("throw new SecurityException(\"" + expected + "\");"));
			}
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
						javaAOPTestCases, javaPhobosTestCases, testSourceRoot());

				assertEquals(5, result.size());
				assertFalse(Files.exists(generatedSettingsClassPath()));
			}
		}

		private Path generatedSettingsClassPath() {
			String fullyQualifiedName = TestBehaviorConfiguration.GENERATED_CLASS_NAME;
			int lastDot = fullyQualifiedName.lastIndexOf('.');
			String settingsPackagePath = fullyQualifiedName.substring(0, lastDot).replace('.', '/');
			String simpleClassName = fullyQualifiedName.substring(lastDot + 1);
			return testSourceRoot().resolve(settingsPackagePath).resolve(simpleClassName + ".java");
		}

		/**
		 * Returns the test source root of the made-up project, whose resources folder,
		 * where the guard registration goes, lies inside the project.
		 *
		 * @return the test source root
		 */
		private Path testSourceRoot() {
			return tempDir.resolve("src/test/java");
		}

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
	}

	/** Checks that the writer refuses a null project root. */
	@Test
	void constructorRejectsANullProjectRoot() {
		assertThrows(NullPointerException.class, () -> new JavaWriter((Path) null));
	}

	/** Checks that the writer refuses a project root that is a file. */
	@Test
	void constructorRejectsAProjectRootThatIsNotADirectory() throws IOException {
		Path file = Files.createFile(tempDir.resolve("not-a-directory.txt"));
		assertThrows(SecurityException.class, () -> new JavaWriter(file));
	}

	/**
	 * Checks that a writer bound to a Maven configuration refuses a Gradle write
	 * request with a SecurityException.
	 */
	@Test
	void writeTestCasesRejectsABuildModeThatMismatchesTheConfiguration() {
		BuildToolConfiguration configuration = new BuildToolConfiguration(BuildMode.MAVEN, tempDir, List.of(tempDir),
				List.of(tempDir), tempDir.resolve("classes"), tempDir.resolve("test-classes"));
		JavaWriter configured = new JavaWriter(configuration);
		SecurityException failure = assertThrows(SecurityException.class,
				() -> configured.writeTestCases(BuildMode.GRADLE, architectureMode, aopMode, essentialPackages,
						essentialClasses, testClasses, packageName, mainClassInPackageName, javaArchitectureTestCases,
						javaAOPTestCases, javaPhobosTestCases, tempDir));
		assertEquals(Messages.localized("security.writer.build.mode.mismatch"), failure.getMessage());
	}

	/**
	 * Checks that a matching build mode passes the mismatch check, so the request
	 * fails later, at the missing build descriptor, instead.
	 */
	@Test
	void writeTestCasesWithAMatchingBuildModePassesTheConfigurationGuard() throws IOException {
		Path emptyRoot = Files.createDirectories(tempDir.resolve("empty-root"));
		BuildToolConfiguration configuration = new BuildToolConfiguration(BuildMode.MAVEN, emptyRoot,
				List.of(emptyRoot), List.of(emptyRoot), emptyRoot.resolve("classes"),
				emptyRoot.resolve("test-classes"));
		JavaWriter configured = new JavaWriter(configuration);
		SecurityException failure = assertThrows(SecurityException.class,
				() -> configured.writeTestCases(BuildMode.MAVEN, architectureMode, aopMode, essentialPackages,
						essentialClasses, testClasses, packageName, mainClassInPackageName, javaArchitectureTestCases,
						javaAOPTestCases, javaPhobosTestCases, emptyRoot));
		assertEquals(
				Messages.localized("security.writer.build.descriptor.missing", BuildMode.MAVEN, emptyRoot.toRealPath()),
				failure.getMessage());
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
		mockedFileTools.when(() -> FileTools.createThreePartedFile(any(), any(), any(), any()))
				.thenReturn(tempDir.resolve("phobos-target.java"));
		mockedFileTools.when(() -> FileTools.copyFiles(any(), any())).thenReturn(List.of());
	}

	private void stubPhobosDefaults(MockedStatic<Phobos> mockedPhobos) {
		mockedPhobos.when(() -> Phobos.filesToCopy()).thenReturn(List.of());
		mockedPhobos.when(() -> Phobos.targetsToCopyTo(any())).thenReturn(List.of());
		mockedPhobos.when(() -> Phobos.threePartedFileHeader()).thenReturn(tempDir.resolve("phobos-header.java"));
		mockedPhobos.when(() -> Phobos.threePartedFileBody(any())).thenReturn("phobos-body");
		mockedPhobos.when(() -> Phobos.threePartedFileFooter()).thenReturn(tempDir.resolve("phobos-footer.java"));
		mockedPhobos.when(() -> Phobos.targetToCopyTo(any())).thenReturn(tempDir.resolve("phobos-target.java"));
	}
}
