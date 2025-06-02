package de.tum.cit.ase.ares.api.policy.director.java;

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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.doReturn;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@DisplayName("SecurityPolicyJavaDirector Tests")
public class SecurityPolicyJavaDirectorTest {

    @Mock private JavaCreator mockCreator;
    @Mock private JavaWriter mockWriter;
    @Mock private JavaExecuter mockExecuter;
    @Mock private EssentialDataYAMLReader mockEssentialDataReader;
    @Mock private JavaProjectScanner mockProjectScanner;
    @Mock private SecurityPolicy mockSecurityPolicy;

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
        
        // Setup mock for EssentialDataYAMLReader to return valid mock objects
        setupEssentialDataMocks();
        
        // Setup mock for JavaProjectScanner to return valid values
        setupProjectScannerMocks();
        
        // Create real SupervisedCode instances for testing
        setupSupervisedCodeInstances();
    }
    
    private void setupSupervisedCodeInstances() {
        // Create a SupervisedCode instance with valid configuration
        ResourceAccesses validResourceAccesses = ResourceAccesses.builder()
                .regardingFileSystemInteractions(List.of())
                .regardingNetworkConnections(List.of())
                .regardingCommandExecutions(List.of())
                .regardingThreadCreations(List.of())
                .regardingPackageImports(List.of())
                .build();
                
        supervisedCodeWithValidConfig = new SupervisedCode(
                ProgrammingLanguageConfiguration.JAVA_USING_MAVEN_ARCHUNIT_AND_ASPECTJ,
                "de.example.test",
                "MainClass",
                new String[]{"TestClass1", "TestClass2"},
                validResourceAccesses
        );
        
        // Note: We cannot create a SupervisedCode with null config directly
        // because the Record constructor validates parameters. 
        // We'll handle the null case differently in the tests.
    }
    
    private SupervisedCode createSupervisedCodeWithConfig(ProgrammingLanguageConfiguration config) {
        ResourceAccesses resourceAccesses = ResourceAccesses.builder()
                .regardingFileSystemInteractions(List.of())
                .regardingNetworkConnections(List.of())
                .regardingCommandExecutions(List.of())
                .regardingThreadCreations(List.of())
                .regardingPackageImports(List.of())
                .build();
                
        return new SupervisedCode(
                config,
                "de.example.test",
                "MainClass",
                new String[]{"TestClass1", "TestClass2"},
                resourceAccesses
        );
    }
    
    private void setupEssentialDataMocks() {
        // Create mock EssentialPackages
        EssentialPackages mockEssentialPackages = EssentialPackages.builder()
                .essentialJavaPackages(List.of("java.lang", "java.util"))
                .essentialArchUnitPackages(List.of("com.tngtech.archunit"))
                .essentialWalaPackages(List.of("com.ibm.wala"))
                .essentialAspectJPackages(List.of("org.aspectj"))
                .essentialInstrumentationPackages(List.of("java.lang.instrument"))
                .essentialAresPackages(List.of("de.tum.cit.ase.ares"))
                .essentialJUnitPackages(List.of("org.junit"))
                .build();
                
        // Create mock EssentialClasses
        EssentialClasses mockEssentialClasses = EssentialClasses.builder()
                .essentialJavaClasses(List.of("java.lang.Object", "java.lang.String"))
                .essentialArchUnitClasses(List.of("com.tngtech.archunit.core.domain.JavaClass"))
                .essentialWalaClasses(List.of("com.ibm.wala.classLoader.IClass"))
                .essentialAspectJClasses(List.of("org.aspectj.lang.ProceedingJoinPoint"))
                .essentialInstrumentationClasses(List.of("java.lang.instrument.Instrumentation"))
                .essentialAresClasses(List.of("de.tum.cit.ase.ares.api.Ares"))
                .essentialJUnitClasses(List.of("org.junit.jupiter.api.Test"))
                .build();
        
        // Setup mocks to return these objects
        when(mockEssentialDataReader.readEssentialPackagesFrom(any(Path.class)))
                .thenReturn(mockEssentialPackages);
        when(mockEssentialDataReader.readEssentialClassesFrom(any(Path.class)))
                .thenReturn(mockEssentialClasses);
    }
    
    private void setupProjectScannerMocks() {
        // Setup JavaProjectScanner to return valid values to prevent null pointer exceptions
        when(mockProjectScanner.scanForTestClasses())
                .thenReturn(new String[]{"TestClass1", "TestClass2"});
        when(mockProjectScanner.scanForPackageName())
                .thenReturn("com.example.package");
        when(mockProjectScanner.scanForMainClassInPackage())
                .thenReturn("MainClass");
    }

    @Nested
    @DisplayName("Constructor Tests")
    class ConstructorTests {

        @Test
        @DisplayName("Should create instance with valid parameters")
        void shouldCreateInstanceWithValidParameters() {
            // Act
            SecurityPolicyJavaDirector director = new SecurityPolicyJavaDirector(
                    mockCreator, mockWriter, mockExecuter, mockEssentialDataReader, 
                    mockProjectScanner, essentialPackagesPath, essentialClassesPath);

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
            SecurityPolicyJavaDirector director = SecurityPolicyJavaDirector.javaBuilder()
                    .creator(mockCreator)
                    .writer(mockWriter)
                    .executer(mockExecuter)
                    .essentialDataReader(mockEssentialDataReader)
                    .javaScanner(mockProjectScanner)
                    .essentialPackagesPath(essentialPackagesPath)
                    .essentialClassesPath(essentialClassesPath)
                    .build();

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
            assertThrows(NullPointerException.class, () -> 
                    SecurityPolicyJavaDirector.javaBuilder()
                            .writer(mockWriter)
                            .executer(mockExecuter)
                            .essentialDataReader(mockEssentialDataReader)
                            .javaScanner(mockProjectScanner)
                            .essentialPackagesPath(essentialPackagesPath)
                            .essentialClassesPath(essentialClassesPath)
                            .build());
        }

        @Test
        @DisplayName("Should throw exception when building with null writer")
        void shouldThrowExceptionWhenBuildingWithNullWriter() {
            // Act & Assert
            assertThrows(NullPointerException.class, () -> 
                    SecurityPolicyJavaDirector.javaBuilder()
                            .creator(mockCreator)
                            .executer(mockExecuter)
                            .essentialDataReader(mockEssentialDataReader)
                            .javaScanner(mockProjectScanner)
                            .essentialPackagesPath(essentialPackagesPath)
                            .essentialClassesPath(essentialClassesPath)
                            .build());
        }

        @Test
        @DisplayName("Should throw exception when building without setting all required fields")
        void shouldThrowExceptionWhenBuildingWithoutAllRequiredFields() {
            // Act & Assert
            assertThrows(NullPointerException.class, () -> 
                    SecurityPolicyJavaDirector.javaBuilder().build());
        }

        @Test
        @DisplayName("Should allow chaining builder methods")
        void shouldAllowChainingBuilderMethods() {
            // Act
            SecurityPolicyJavaDirector.Builder builder = SecurityPolicyJavaDirector.javaBuilder()
                    .creator(mockCreator)
                    .writer(mockWriter)
                    .executer(mockExecuter)
                    .essentialDataReader(mockEssentialDataReader)
                    .javaScanner(mockProjectScanner)
                    .essentialPackagesPath(essentialPackagesPath)
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
            director = new SecurityPolicyJavaDirector(
                    mockCreator, mockWriter, mockExecuter, mockEssentialDataReader,
                    mockProjectScanner, essentialPackagesPath, essentialClassesPath);
        }

        @Test
        @DisplayName("Should throw exception when creating test cases with null security policy and project path")
        void shouldThrowExceptionWhenCreatingTestCasesWithNullSecurityPolicyAndProjectPath() {
            // Act & Assert
            assertThrows(NullPointerException.class, () -> 
                    director.createTestCases(null, null));
        }

        @Test
        @DisplayName("Should create test cases for MAVEN_ARCHUNIT_ASPECTJ configuration")
        void shouldCreateTestCasesForMavenArchUnitAspectJConfiguration() {
            // Arrange
            SecurityPolicy securityPolicy = SecurityPolicy.builder()
                    .regardingTheSupervisedCode(supervisedCodeWithValidConfig)
                    .build();

            // Act
            TestCaseAbstractFactoryAndBuilder result = director.createTestCases(securityPolicy, projectPath);

            // Assert
            assertNotNull(result);
            assertInstanceOf(JavaTestCaseFactoryAndBuilder.class, result);
        }

        @Test
        @DisplayName("Should create test cases for MAVEN_ARCHUNIT_INSTRUMENTATION configuration")
        void shouldCreateTestCasesForMavenArchUnitInstrumentationConfiguration() {
            // Arrange
            SupervisedCode supervisedCode = createSupervisedCodeWithConfig(
                    ProgrammingLanguageConfiguration.JAVA_USING_MAVEN_ARCHUNIT_AND_INSTRUMENTATION);
            SecurityPolicy securityPolicy = SecurityPolicy.builder()
                    .regardingTheSupervisedCode(supervisedCode)
                    .build();

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
            SecurityPolicy securityPolicy = SecurityPolicy.builder()
                    .regardingTheSupervisedCode(supervisedCode)
                    .build();

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
            SecurityPolicy securityPolicy = SecurityPolicy.builder()
                    .regardingTheSupervisedCode(supervisedCode)
                    .build();

            // Act
            TestCaseAbstractFactoryAndBuilder result = director.createTestCases(securityPolicy, projectPath);

            // Assert
            assertNotNull(result);
            assertInstanceOf(JavaTestCaseFactoryAndBuilder.class, result);
        }

        @Test
        @DisplayName("Should create test cases for GRADLE_ARCHUNIT_ASPECTJ configuration")
        void shouldCreateTestCasesForGradleArchUnitAspectJConfiguration() {
            // Arrange
            SupervisedCode supervisedCode = createSupervisedCodeWithConfig(
                    ProgrammingLanguageConfiguration.JAVA_USING_GRADLE_ARCHUNIT_AND_ASPECTJ);
            SecurityPolicy securityPolicy = SecurityPolicy.builder()
                    .regardingTheSupervisedCode(supervisedCode)
                    .build();

            // Act
            TestCaseAbstractFactoryAndBuilder result = director.createTestCases(securityPolicy, projectPath);

            // Assert
            assertNotNull(result);
            assertInstanceOf(JavaTestCaseFactoryAndBuilder.class, result);
        }

        @Test
        @DisplayName("Should create test cases for GRADLE_ARCHUNIT_INSTRUMENTATION configuration")
        void shouldCreateTestCasesForGradleArchUnitInstrumentationConfiguration() {
            // Arrange
            SupervisedCode supervisedCode = createSupervisedCodeWithConfig(
                    ProgrammingLanguageConfiguration.JAVA_USING_GRADLE_ARCHUNIT_AND_INSTRUMENTATION);
            SecurityPolicy securityPolicy = SecurityPolicy.builder()
                    .regardingTheSupervisedCode(supervisedCode)
                    .build();

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
            SecurityPolicy securityPolicy = SecurityPolicy.builder()
                    .regardingTheSupervisedCode(supervisedCode)
                    .build();

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
            SecurityPolicy securityPolicy = SecurityPolicy.builder()
                    .regardingTheSupervisedCode(supervisedCode)
                    .build();

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
            SecurityPolicy securityPolicy = SecurityPolicy.builder()
                    .regardingTheSupervisedCode(supervisedCode)
                    .build();

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
            assertThrows(NullPointerException.class, () -> 
                    director.createTestCases(securityPolicy, projectPath));
        }

        @Test
        @DisplayName("Should throw exception when programming language configuration is null")
        void shouldThrowExceptionWhenProgrammingLanguageConfigurationIsNull() {
            // Arrange
            // Da SupervisedCode ein Record ist, können wir nicht das Verhalten mit Mockito steuern
            // Stattdessen müssen wir eine spezielle Testsituation simulieren
            
            // Erstelle ein Security Policy mit einem gemockten SupervisedCode (verwendet ein reales Objekt mit Spy)
            SupervisedCode supervisedCode = createSupervisedCodeWithConfig(
                    ProgrammingLanguageConfiguration.JAVA_USING_MAVEN_ARCHUNIT_AND_ASPECTJ);
            SupervisedCode spyCode = spy(supervisedCode);
            
            // Simuliere, dass die Programmiersprachen-Konfiguration null ist
            doReturn(null).when(spyCode).theFollowingProgrammingLanguageConfigurationIsUsed();
            
            SecurityPolicy securityPolicy = SecurityPolicy.builder()
                    .regardingTheSupervisedCode(spyCode)
                    .build();

            // Act & Assert
            assertThrows(NullPointerException.class, () -> 
                    director.createTestCases(securityPolicy, projectPath));
        }
    }

    @Nested
    @DisplayName("Default Constants Tests")
    class DefaultConstantsTests {

        @Test
        @DisplayName("Should have non-null default essential packages path")
        void shouldHaveNonNullDefaultEssentialPackagesPath() {
            // Assert
            assertNotNull(SecurityPolicyJavaDirector.DEFAULT_ESSENTIAL_PACKAGES_PATH);
        }

        @Test
        @DisplayName("Should have non-null default essential classes path")
        void shouldHaveNonNullDefaultEssentialClassesPath() {
            // Assert
            assertNotNull(SecurityPolicyJavaDirector.DEFAULT_ESSENTIAL_CLASSES_PATH);
        }
    }
}
