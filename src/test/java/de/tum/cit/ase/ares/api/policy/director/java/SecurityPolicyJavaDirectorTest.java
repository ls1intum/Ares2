package de.tum.cit.ase.ares.api.policy.director.java;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import de.tum.cit.ase.ares.api.aop.AOPMode;
import de.tum.cit.ase.ares.api.architecture.ArchitectureMode;
import de.tum.cit.ase.ares.api.buildtoolconfiguration.BuildMode;
import de.tum.cit.ase.ares.api.policy.SecurityPolicy;
import de.tum.cit.ase.ares.api.policy.policySubComponents.ProgrammingLanguageConfiguration;
import de.tum.cit.ase.ares.api.policy.policySubComponents.ResourceAccesses;
import de.tum.cit.ase.ares.api.policy.policySubComponents.SupervisedCode;
import de.tum.cit.ase.ares.api.securitytest.TestCaseAbstractFactoryAndBuilder;
import de.tum.cit.ase.ares.api.securitytest.java.JavaTestCaseFactoryAndBuilder;
import de.tum.cit.ase.ares.api.securitytest.java.creator.JavaCreator;
import de.tum.cit.ase.ares.api.securitytest.java.essentialModel.EssentialClasses;
import de.tum.cit.ase.ares.api.securitytest.java.essentialModel.EssentialPackages;
import de.tum.cit.ase.ares.api.securitytest.java.essentialModel.yaml.EssentialDataYAMLReader;
import de.tum.cit.ase.ares.api.securitytest.java.executer.JavaExecuter;
import de.tum.cit.ase.ares.api.securitytest.java.projectScanner.JavaProjectScanner;
import de.tum.cit.ase.ares.api.securitytest.java.writer.JavaWriter;

@DisplayName("SecurityPolicyJavaDirector Tests")
public class SecurityPolicyJavaDirectorTest {

	@Mock
	private JavaCreator mockCreator;
	@Mock
	private JavaWriter mockWriter;
	@Mock
	private JavaExecuter mockExecuter;
	@Mock
	private EssentialDataYAMLReader mockEssentialDataReader;
	@Mock
	private JavaProjectScanner mockProjectScanner;
	@Mock
	private SecurityPolicy mockSecurityPolicy;

	@TempDir
	Path tempDir;

	private Path essentialPackagesPath;
	private Path essentialClassesPath;
	private Path projectPath;

	// Real SupervisedCode instance for testing
	private SupervisedCode supervisedCodeWithValidConfig;

	@BeforeEach
	void setUp() throws Exception {
		MockitoAnnotations.openMocks(this);
		essentialPackagesPath = tempDir.resolve("essential-packages.yaml");
		essentialClassesPath = tempDir.resolve("essential-classes.yaml");
		projectPath = tempDir.resolve("project");

		Files.createFile(essentialPackagesPath);
		Files.createFile(essentialClassesPath);
		Files.createDirectories(projectPath);
		Files.writeString(projectPath.resolve("pom.xml"), "<project/>");
		Files.writeString(projectPath.resolve("build.gradle"), "plugins { id 'java' }");

		// Setup mock for EssentialDataYAMLReader to return valid mock objects
		setupEssentialDataMocks();

		// Setup mock for JavaProjectScanner to return valid values
		setupProjectScannerMocks();

		// Create real SupervisedCode instances for testing
		setupSupervisedCodeInstances();
	}

	private void setupSupervisedCodeInstances() {
		// Create a SupervisedCode instance with valid configuration
		ResourceAccesses validResourceAccesses = ResourceAccesses.builder().regardingFileSystemInteractions(List.of())
				.regardingNetworkConnections(List.of()).regardingCommandExecutions(List.of())
				.regardingThreadCreations(List.of()).regardingPackageImports(List.of()).build();

		supervisedCodeWithValidConfig = new SupervisedCode(
				ProgrammingLanguageConfiguration.JAVA_USING_MAVEN_ARCHUNIT_AND_ASPECTJ, "de.example.test", "MainClass",
				List.of("TestClass1", "TestClass2"), validResourceAccesses);

		// Note: We cannot create a SupervisedCode with null config directly
		// because the Record constructor validates parameters.
		// We'll handle the null case differently in the tests.
	}

	private SupervisedCode createSupervisedCodeWithConfig(ProgrammingLanguageConfiguration config) {
		ResourceAccesses resourceAccesses = ResourceAccesses.builder().regardingFileSystemInteractions(List.of())
				.regardingNetworkConnections(List.of()).regardingCommandExecutions(List.of())
				.regardingThreadCreations(List.of()).regardingPackageImports(List.of()).build();

		return new SupervisedCode(config, "de.example.test", "MainClass", List.of("TestClass1", "TestClass2"),
				resourceAccesses);
	}

	private void setupEssentialDataMocks() {
		// Create mock EssentialPackages
		EssentialPackages mockEssentialPackages = EssentialPackages.builder()
				.essentialJavaPackages(List.of("java.lang", "java.util"))
				.essentialArchunitPackages(List.of("com.tngtech.archunit"))
				.essentialWalaPackages(List.of("com.ibm.wala")).essentialAspectJPackages(List.of("org.aspectj"))
				.essentialInstrumentationPackages(List.of("java.lang.instrument"))
				.essentialAresPackages(List.of("de.tum.cit.ase.ares")).essentialJUnitPackages(List.of("org.junit"))
				.build();

		// Create mock EssentialClasses
		EssentialClasses mockEssentialClasses = EssentialClasses.builder()
				.essentialJavaClasses(List.of("java.lang.Object", "java.lang.String"))
				.essentialArchunitClasses(List.of("com.tngtech.archunit.core.domain.JavaClass"))
				.essentialWalaClasses(List.of("com.ibm.wala.classLoader.IClass"))
				.essentialAspectJClasses(List.of("org.aspectj.lang.ProceedingJoinPoint"))
				.essentialInstrumentationClasses(List.of("java.lang.instrument.Instrumentation"))
				.essentialAresClasses(List.of("de.tum.cit.ase.ares.api.Ares"))
				.essentialJUnitClasses(List.of("org.junit.jupiter.api.Test")).build();

		// Setup mocks to return these objects
		when(mockEssentialDataReader.readEssentialPackagesFrom(any(Path.class))).thenReturn(mockEssentialPackages);
		when(mockEssentialDataReader.readEssentialClassesFrom(any(Path.class))).thenReturn(mockEssentialClasses);
	}

	private void setupProjectScannerMocks() {
		// Setup JavaProjectScanner to return valid values to prevent null pointer
		// exceptions
		when(mockProjectScanner.scanForTestClasses()).thenReturn(new String[] { "TestClass1", "TestClass2" });
		when(mockProjectScanner.scanForPackageName()).thenReturn("com.example.package");
		when(mockProjectScanner.scanForMainClassInPackage()).thenReturn("MainClass");
	}

	@Nested
	@DisplayName("Constructor Tests")
	class ConstructorTests {

		@Test
		@DisplayName("Should create instance with valid parameters")
		void shouldCreateInstanceWithValidParameters() {
			// Act
			SecurityPolicyJavaDirector director = new SecurityPolicyJavaDirector(mockCreator, mockWriter, mockExecuter,
					mockEssentialDataReader, mockProjectScanner, essentialPackagesPath, essentialClassesPath);

			// Assert
			assertNotNull(director);
		}
	}

	@Nested
	@DisplayName("Builder Tests")
	class BuilderTests {

		@Test
		@DisplayName("Should build instance using builder pattern")
		void shouldBuildInstanceUsingBuilderPattern() {
			// Act
			SecurityPolicyJavaDirector director = SecurityPolicyJavaDirector.javaBuilder().creator(mockCreator)
					.writer(mockWriter).executer(mockExecuter).essentialDataReader(mockEssentialDataReader)
					.javaScanner(mockProjectScanner).essentialPackagesPath(essentialPackagesPath)
					.essentialClassesPath(essentialClassesPath).build();

			// Assert
			assertNotNull(director);
		}

		@Test
		@DisplayName("Should return non-null builder")
		void shouldReturnNonNullBuilder() {
			// Act
			SecurityPolicyJavaDirector.Builder builder = SecurityPolicyJavaDirector.javaBuilder();

			// Assert
			assertNotNull(builder);
		}

		@Test
		@DisplayName("Should throw exception when building with null creator")
		void shouldThrowExceptionWhenBuildingWithNullCreator() {
			// Act & Assert
			assertThrows(NullPointerException.class, () -> SecurityPolicyJavaDirector.javaBuilder().writer(mockWriter)
					.executer(mockExecuter).essentialDataReader(mockEssentialDataReader).javaScanner(mockProjectScanner)
					.essentialPackagesPath(essentialPackagesPath).essentialClassesPath(essentialClassesPath).build());
		}

		@Test
		@DisplayName("Should throw exception when building with null writer")
		void shouldThrowExceptionWhenBuildingWithNullWriter() {
			// Act & Assert
			assertThrows(NullPointerException.class, () -> SecurityPolicyJavaDirector.javaBuilder().creator(mockCreator)
					.executer(mockExecuter).essentialDataReader(mockEssentialDataReader).javaScanner(mockProjectScanner)
					.essentialPackagesPath(essentialPackagesPath).essentialClassesPath(essentialClassesPath).build());
		}

		@Test
		@DisplayName("Should throw exception when building without setting all required fields")
		void shouldThrowExceptionWhenBuildingWithoutAllRequiredFields() {
			// Act & Assert
			assertThrows(NullPointerException.class, () -> SecurityPolicyJavaDirector.javaBuilder().build());
		}

		@ParameterizedTest
		@ValueSource(strings = { "creator", "writer", "executer", "essentialDataReader", "javaScanner",
				"essentialPackagesPath", "essentialClassesPath" })
		void rejectsEveryIndividuallyMissingBuilderDependency(String missing) {
			SecurityPolicyJavaDirector.Builder builder = SecurityPolicyJavaDirector.javaBuilder();
			if (!"creator".equals(missing)) {
				builder.creator(mockCreator);
			}
			if (!"writer".equals(missing)) {
				builder.writer(mockWriter);
			}
			if (!"executer".equals(missing)) {
				builder.executer(mockExecuter);
			}
			if (!"essentialDataReader".equals(missing)) {
				builder.essentialDataReader(mockEssentialDataReader);
			}
			if (!"javaScanner".equals(missing)) {
				builder.javaScanner(mockProjectScanner);
			}
			if (!"essentialPackagesPath".equals(missing)) {
				builder.essentialPackagesPath(essentialPackagesPath);
			}
			if (!"essentialClassesPath".equals(missing)) {
				builder.essentialClassesPath(essentialClassesPath);
			}
			NullPointerException failure = assertThrows(NullPointerException.class, builder::build);
			assertTrue(failure.getMessage().contains(missing));
		}

		@Test
		void rejectsEveryNullConstructorDependency() {
			assertAll(() -> assertThrows(NullPointerException.class,
					() -> new SecurityPolicyJavaDirector(null, mockWriter, mockExecuter, mockEssentialDataReader,
							mockProjectScanner, essentialPackagesPath, essentialClassesPath)),
					() -> assertThrows(NullPointerException.class,
							() -> new SecurityPolicyJavaDirector(mockCreator, null, mockExecuter,
									mockEssentialDataReader, mockProjectScanner, essentialPackagesPath,
									essentialClassesPath)),
					() -> assertThrows(NullPointerException.class,
							() -> new SecurityPolicyJavaDirector(mockCreator, mockWriter, null, mockEssentialDataReader,
									mockProjectScanner, essentialPackagesPath, essentialClassesPath)),
					() -> assertThrows(NullPointerException.class,
							() -> new SecurityPolicyJavaDirector(mockCreator, mockWriter, mockExecuter, null,
									mockProjectScanner, essentialPackagesPath, essentialClassesPath)),
					() -> assertThrows(NullPointerException.class,
							() -> new SecurityPolicyJavaDirector(mockCreator, mockWriter, mockExecuter,
									mockEssentialDataReader, null, essentialPackagesPath, essentialClassesPath)),
					() -> assertThrows(NullPointerException.class,
							() -> new SecurityPolicyJavaDirector(mockCreator, mockWriter, mockExecuter,
									mockEssentialDataReader, mockProjectScanner, null, essentialClassesPath)),
					() -> assertThrows(NullPointerException.class,
							() -> new SecurityPolicyJavaDirector(mockCreator, mockWriter, mockExecuter,
									mockEssentialDataReader, mockProjectScanner, essentialPackagesPath, null)));
		}

		@Test
		@DisplayName("Should allow chaining builder methods")
		void shouldAllowChainingBuilderMethods() {
			// Act
			SecurityPolicyJavaDirector.Builder builder = SecurityPolicyJavaDirector.javaBuilder().creator(mockCreator)
					.writer(mockWriter).executer(mockExecuter).essentialDataReader(mockEssentialDataReader)
					.javaScanner(mockProjectScanner).essentialPackagesPath(essentialPackagesPath)
					.essentialClassesPath(essentialClassesPath);

			// Assert
			assertNotNull(builder);
			SecurityPolicyJavaDirector director = builder.build();
			assertNotNull(director);
		}
	}

	@Nested
	@DisplayName("CreateTestCases Tests")
	class CreateTestCasesTests {

		private SecurityPolicyJavaDirector director;

		@BeforeEach
		void setUp() {
			director = new SecurityPolicyJavaDirector(mockCreator, mockWriter, mockExecuter, mockEssentialDataReader,
					mockProjectScanner, essentialPackagesPath, essentialClassesPath);
		}

		@Test
		@DisplayName("Should fall back to the default configuration when the security policy is null")
		void shouldFallBackToDefaultConfigurationWhenCreatingTestCasesWithNullSecurityPolicy() throws Exception {
			Path maven = Files.createDirectory(tempDir.resolve("default-maven-project"));
			Files.writeString(maven.resolve("pom.xml"), "<project/>");
			TestCaseAbstractFactoryAndBuilder result = director.createTestCases(null, maven);
			assertEquals(BuildMode.MAVEN, result.buildMode());
			assertEquals(ArchitectureMode.ARCHUNIT, result.architectureMode());
			assertEquals(AOPMode.ASPECTJ, result.aopMode());
		}

		@ParameterizedTest
		@EnumSource(ProgrammingLanguageConfiguration.class)
		void selectsEveryExplicitModeExactly(ProgrammingLanguageConfiguration configuration) {
			SecurityPolicy policy = SecurityPolicy.builder()
					.regardingTheSupervisedCode(createSupervisedCodeWithConfig(configuration)).build();
			TestCaseAbstractFactoryAndBuilder result = director.createTestCases(policy, projectPath);
			String name = configuration.name();
			assertEquals(name.contains("MAVEN") ? BuildMode.MAVEN : BuildMode.GRADLE, result.buildMode());
			assertEquals(name.contains("ARCHUNIT") ? ArchitectureMode.ARCHUNIT : ArchitectureMode.WALA,
					result.architectureMode());
			assertEquals(name.contains("ASPECTJ") ? AOPMode.ASPECTJ : AOPMode.INSTRUMENTATION, result.aopMode());
		}

		@Test
		void recognisesBothNoPolicyBuildToolsAndRejectsInvalidDiscovery() throws Exception {
			Path maven = Files.createDirectory(tempDir.resolve("maven-project"));
			Files.writeString(maven.resolve("pom.xml"), "<project/>");
			Path gradle = Files.createDirectory(tempDir.resolve("gradle-project"));
			Files.writeString(gradle.resolve("build.gradle.kts"), "plugins { java }");
			TestCaseAbstractFactoryAndBuilder mavenFactory = director.createTestCases(null, maven);
			TestCaseAbstractFactoryAndBuilder gradleFactory = director.createTestCases(null, gradle);
			assertEquals(BuildMode.MAVEN, mavenFactory.buildMode());
			assertEquals(BuildMode.GRADLE, gradleFactory.buildMode());
			assertEquals(ArchitectureMode.ARCHUNIT, gradleFactory.architectureMode());
			assertEquals(AOPMode.ASPECTJ, gradleFactory.aopMode());

			Path ambiguous = Files.createDirectory(tempDir.resolve("ambiguous-project"));
			Files.writeString(ambiguous.resolve("pom.xml"), "<project/>");
			Files.writeString(ambiguous.resolve("build.gradle"), "plugins { id 'java' }");
			Path unsupported = Files.createDirectory(tempDir.resolve("unsupported-project"));
			assertThrows(IllegalStateException.class, () -> director.createTestCases(null, ambiguous));
			assertThrows(IllegalStateException.class, () -> director.createTestCases(null, unsupported));
		}

		@Test
		@DisplayName("Should create test cases for MAVEN_ARCHUNIT_ASPECTJ configuration")
		void shouldCreateTestCasesForMavenArchunitAspectJConfiguration() {
			// Arrange
			SecurityPolicy securityPolicy = SecurityPolicy.builder()
					.regardingTheSupervisedCode(supervisedCodeWithValidConfig).build();

			// Act
			TestCaseAbstractFactoryAndBuilder result = director.createTestCases(securityPolicy, projectPath);

			// Assert
			assertNotNull(result);
			assertInstanceOf(JavaTestCaseFactoryAndBuilder.class, result);
		}

		@Test
		@DisplayName("Should create test cases for MAVEN_ARCHUNIT_INSTRUMENTATION configuration")
		void shouldCreateTestCasesForMavenArchunitInstrumentationConfiguration() {
			// Arrange
			SupervisedCode supervisedCode = createSupervisedCodeWithConfig(
					ProgrammingLanguageConfiguration.JAVA_USING_MAVEN_ARCHUNIT_AND_INSTRUMENTATION);
			SecurityPolicy securityPolicy = SecurityPolicy.builder().regardingTheSupervisedCode(supervisedCode).build();

			// Act
			TestCaseAbstractFactoryAndBuilder result = director.createTestCases(securityPolicy, projectPath);

			// Assert
			assertNotNull(result);
			assertInstanceOf(JavaTestCaseFactoryAndBuilder.class, result);
		}

		@Test
		@DisplayName("Should create test cases for MAVEN_WALA_ASPECTJ configuration")
		void shouldCreateTestCasesForMavenWalaAspectJConfiguration() {
			// Arrange
			SupervisedCode supervisedCode = createSupervisedCodeWithConfig(
					ProgrammingLanguageConfiguration.JAVA_USING_MAVEN_WALA_AND_ASPECTJ);
			SecurityPolicy securityPolicy = SecurityPolicy.builder().regardingTheSupervisedCode(supervisedCode).build();

			// Act
			TestCaseAbstractFactoryAndBuilder result = director.createTestCases(securityPolicy, projectPath);

			// Assert
			assertNotNull(result);
			assertInstanceOf(JavaTestCaseFactoryAndBuilder.class, result);
		}

		@Test
		@DisplayName("Should create test cases for MAVEN_WALA_INSTRUMENTATION configuration")
		void shouldCreateTestCasesForMavenWalaInstrumentationConfiguration() {
			// Arrange
			SupervisedCode supervisedCode = createSupervisedCodeWithConfig(
					ProgrammingLanguageConfiguration.JAVA_USING_MAVEN_WALA_AND_INSTRUMENTATION);
			SecurityPolicy securityPolicy = SecurityPolicy.builder().regardingTheSupervisedCode(supervisedCode).build();

			// Act
			TestCaseAbstractFactoryAndBuilder result = director.createTestCases(securityPolicy, projectPath);

			// Assert
			assertNotNull(result);
			assertInstanceOf(JavaTestCaseFactoryAndBuilder.class, result);
		}

		@Test
		@DisplayName("Should create test cases for GRADLE_ARCHUNIT_ASPECTJ configuration")
		void shouldCreateTestCasesForGradleArchunitAspectJConfiguration() {
			// Arrange
			SupervisedCode supervisedCode = createSupervisedCodeWithConfig(
					ProgrammingLanguageConfiguration.JAVA_USING_GRADLE_ARCHUNIT_AND_ASPECTJ);
			SecurityPolicy securityPolicy = SecurityPolicy.builder().regardingTheSupervisedCode(supervisedCode).build();

			// Act
			TestCaseAbstractFactoryAndBuilder result = director.createTestCases(securityPolicy, projectPath);

			// Assert
			assertNotNull(result);
			assertInstanceOf(JavaTestCaseFactoryAndBuilder.class, result);
		}

		@Test
		@DisplayName("Should create test cases for GRADLE_ARCHUNIT_INSTRUMENTATION configuration")
		void shouldCreateTestCasesForGradleArchunitInstrumentationConfiguration() {
			// Arrange
			SupervisedCode supervisedCode = createSupervisedCodeWithConfig(
					ProgrammingLanguageConfiguration.JAVA_USING_GRADLE_ARCHUNIT_AND_INSTRUMENTATION);
			SecurityPolicy securityPolicy = SecurityPolicy.builder().regardingTheSupervisedCode(supervisedCode).build();

			// Act
			TestCaseAbstractFactoryAndBuilder result = director.createTestCases(securityPolicy, projectPath);

			// Assert
			assertNotNull(result);
			assertInstanceOf(JavaTestCaseFactoryAndBuilder.class, result);
		}

		@Test
		@DisplayName("Should create test cases for GRADLE_WALA_ASPECTJ configuration")
		void shouldCreateTestCasesForGradleWalaAspectJConfiguration() {
			// Arrange
			SupervisedCode supervisedCode = createSupervisedCodeWithConfig(
					ProgrammingLanguageConfiguration.JAVA_USING_GRADLE_WALA_AND_ASPECTJ);
			SecurityPolicy securityPolicy = SecurityPolicy.builder().regardingTheSupervisedCode(supervisedCode).build();

			// Act
			TestCaseAbstractFactoryAndBuilder result = director.createTestCases(securityPolicy, projectPath);

			// Assert
			assertNotNull(result);
			assertInstanceOf(JavaTestCaseFactoryAndBuilder.class, result);
		}

		@Test
		@DisplayName("Should create test cases for GRADLE_WALA_INSTRUMENTATION configuration")
		void shouldCreateTestCasesForGradleWalaInstrumentationConfiguration() {
			// Arrange
			SupervisedCode supervisedCode = createSupervisedCodeWithConfig(
					ProgrammingLanguageConfiguration.JAVA_USING_GRADLE_WALA_AND_INSTRUMENTATION);
			SecurityPolicy securityPolicy = SecurityPolicy.builder().regardingTheSupervisedCode(supervisedCode).build();

			// Act
			TestCaseAbstractFactoryAndBuilder result = director.createTestCases(securityPolicy, projectPath);

			// Assert
			assertNotNull(result);
			assertInstanceOf(JavaTestCaseFactoryAndBuilder.class, result);
		}

		@Test
		@DisplayName("Should handle null project folder path")
		void shouldHandleNullProjectFolderPath() {
			// Arrange
			SupervisedCode supervisedCode = createSupervisedCodeWithConfig(
					ProgrammingLanguageConfiguration.JAVA_USING_MAVEN_ARCHUNIT_AND_ASPECTJ);
			SecurityPolicy securityPolicy = SecurityPolicy.builder().regardingTheSupervisedCode(supervisedCode).build();

			// Act
			TestCaseAbstractFactoryAndBuilder result = director.createTestCases(securityPolicy, null);

			// Assert
			assertNotNull(result);
			assertInstanceOf(JavaTestCaseFactoryAndBuilder.class, result);
		}

		@Test
		@DisplayName("Should throw exception when supervised code is null")
		void shouldThrowExceptionWhenSupervisedCodeIsNull() {
			// Arrange
			SecurityPolicy securityPolicy = mock(SecurityPolicy.class);
			when(securityPolicy.regardingTheSupervisedCode()).thenReturn(null);

			// Act & Assert
			assertThrows(NullPointerException.class, () -> director.createTestCases(securityPolicy, projectPath));
		}

		@Test
		@DisplayName("Should throw exception when programming language configuration is null")
		void shouldThrowExceptionWhenProgrammingLanguageConfigurationIsNull() {
			// Arrange
			// Da SupervisedCode ein Record ist, können wir nicht das Verhalten mit Mockito
			// steuern
			// Stattdessen müssen wir eine spezielle Testsituation simulieren

			// Erstelle ein Security Policy mit einem gemockten SupervisedCode (verwendet
			// ein reales Objekt mit Spy)
			SupervisedCode supervisedCode = createSupervisedCodeWithConfig(
					ProgrammingLanguageConfiguration.JAVA_USING_MAVEN_ARCHUNIT_AND_ASPECTJ);
			SupervisedCode spyCode = spy(supervisedCode);

			// Simuliere, dass die Programmiersprachen-Konfiguration null ist
			doReturn(null).when(spyCode).theFollowingProgrammingLanguageConfigurationIsUsed();

			SecurityPolicy securityPolicy = SecurityPolicy.builder().regardingTheSupervisedCode(spyCode).build();

			// Act & Assert
			assertThrows(NullPointerException.class, () -> director.createTestCases(securityPolicy, projectPath));
		}
	}

	@Nested
	@DisplayName("Default Constants Tests")
	class DefaultConstantsTests {

		@Test
		@DisplayName("Should have non-null default essential packages path")
		void shouldHaveNonNullDefaultEssentialPackagesPath() {
			assertTrue(Files.isReadable(SecurityPolicyJavaDirector.DEFAULT_ESSENTIAL_PACKAGES_PATH));
			assertFalse(new EssentialDataYAMLReader()
					.readEssentialPackagesFrom(SecurityPolicyJavaDirector.DEFAULT_ESSENTIAL_PACKAGES_PATH)
					.getEssentialPackages().isEmpty());
		}

		@Test
		@DisplayName("Should have non-null default essential classes path")
		void shouldHaveNonNullDefaultEssentialClassesPath() {
			assertTrue(Files.isReadable(SecurityPolicyJavaDirector.DEFAULT_ESSENTIAL_CLASSES_PATH));
			assertFalse(new EssentialDataYAMLReader()
					.readEssentialClassesFrom(SecurityPolicyJavaDirector.DEFAULT_ESSENTIAL_CLASSES_PATH)
					.getEssentialClasses().isEmpty());
		}
	}
}
