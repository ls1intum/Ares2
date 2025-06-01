package de.tum.cit.ase.ares.api.securitytest;

import de.tum.cit.ase.ares.api.aop.AOPMode;
import de.tum.cit.ase.ares.api.aop.AOPTestCase;
import de.tum.cit.ase.ares.api.architecture.ArchitectureMode;
import de.tum.cit.ase.ares.api.architecture.ArchitectureTestCase;
import de.tum.cit.ase.ares.api.buildtoolconfiguration.BuildMode;
import de.tum.cit.ase.ares.api.policy.SecurityPolicy;
import de.tum.cit.ase.ares.api.policy.policySubComponents.ResourceAccesses;
import de.tum.cit.ase.ares.api.policy.policySubComponents.SupervisedCode;
import de.tum.cit.ase.ares.api.securitytest.java.creator.Creator;
import de.tum.cit.ase.ares.api.securitytest.java.essentialModel.EssentialClasses;
import de.tum.cit.ase.ares.api.securitytest.java.essentialModel.EssentialDataReader;
import de.tum.cit.ase.ares.api.securitytest.java.essentialModel.EssentialPackages;
import de.tum.cit.ase.ares.api.securitytest.java.executer.Executer;
import de.tum.cit.ase.ares.api.securitytest.java.projectScanner.ProjectScanner;
import de.tum.cit.ase.ares.api.securitytest.java.writer.Writer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@DisplayName("TestCaseAbstractFactoryAndBuilder Tests")
public class TestCaseAbstractFactoryAndBuilderTest {

    @Mock private Creator mockCreator;
    @Mock private Writer mockWriter;
    @Mock private Executer mockExecuter;
    @Mock private EssentialDataReader mockEssentialDataReader;
    @Mock private ProjectScanner mockProjectScanner;
    @Mock private SecurityPolicy mockSecurityPolicy;
    @Mock private SupervisedCode mockSupervisedCode;
    @Mock private ResourceAccesses mockResourceAccesses;
    @Mock private EssentialPackages mockEssentialPackages;
    @Mock private EssentialClasses mockEssentialClasses;

    private Path essentialPackagesPath;
    private Path essentialClassesPath;
    private Path projectPath;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        essentialPackagesPath = Paths.get("test/essential/packages.yaml");
        essentialClassesPath = Paths.get("test/essential/classes.yaml");
        projectPath = Paths.get("test/project");

        // Setup default mock behaviors
        when(mockEssentialDataReader.readEssentialPackagesFrom(essentialPackagesPath))
                .thenReturn(mockEssentialPackages);
        when(mockEssentialDataReader.readEssentialClassesFrom(essentialClassesPath))
                .thenReturn(mockEssentialClasses);
        when(mockEssentialPackages.getEssentialPackages())
                .thenReturn(List.of("java.lang", "java.util"));
        when(mockEssentialClasses.getEssentialClasses())
                .thenReturn(List.of("java.lang.Object", "java.lang.String"));

        when(mockProjectScanner.scanForBuildMode()).thenReturn(BuildMode.MAVEN);
        when(mockProjectScanner.scanForTestClasses()).thenReturn(new String[]{"TestClass1", "TestClass2"});
        when(mockProjectScanner.scanForPackageName()).thenReturn("com.example");
        when(mockProjectScanner.scanForMainClassInPackage()).thenReturn("Main");
    }

    // Concrete implementation for testing the abstract class
    private static class TestableFactoryAndBuilder extends TestCaseAbstractFactoryAndBuilder {
        private final List<Path> testCasePaths;
        private boolean executed = false;

        public TestableFactoryAndBuilder(Creator creator, Writer writer, Executer executer,
                                       EssentialDataReader essentialDataReader, ProjectScanner projectScanner,
                                       Path essentialPackagesPath, Path essentialClassesPath,
                                       BuildMode buildMode, ArchitectureMode architectureMode, AOPMode aopMode,
                                       SecurityPolicy securityPolicy, Path projectPath) {
            super(creator, writer, executer, essentialDataReader, projectScanner,
                  essentialPackagesPath, essentialClassesPath, buildMode, architectureMode, aopMode,
                  securityPolicy, projectPath);
            this.testCasePaths = new ArrayList<>();
        }

        @Override
        public List<Path> writeTestCases(Path testFolderPath) {
            testCasePaths.add(Paths.get("test1.java"));
            testCasePaths.add(Paths.get("test2.java"));
            return testCasePaths;
        }

        @Override
        public void executeTestCases() {
            executed = true;
        }

        public boolean wasExecuted() {
            return executed;
        }

        public List<Path> getTestCasePaths() {
            return testCasePaths;
        }
    }

    @Nested
    @DisplayName("Constructor Tests")
    class ConstructorTests {

        @Test
        @DisplayName("Should initialize with all required parameters")
        void shouldInitializeWithAllRequiredParameters() {
            // Act
            TestableFactoryAndBuilder factory = new TestableFactoryAndBuilder(
                    mockCreator, mockWriter, mockExecuter, mockEssentialDataReader, mockProjectScanner,
                    essentialPackagesPath, essentialClassesPath,
                    BuildMode.MAVEN, ArchitectureMode.ARCHUNIT, AOPMode.ASPECTJ,
                    mockSecurityPolicy, projectPath
            );

            // Assert
            assertNotNull(factory);
            verify(mockEssentialDataReader).readEssentialPackagesFrom(essentialPackagesPath);
            verify(mockEssentialDataReader).readEssentialClassesFrom(essentialClassesPath);
            verify(mockCreator).createTestCases(
                    eq(BuildMode.MAVEN),
                    eq(ArchitectureMode.ARCHUNIT),
                    eq(AOPMode.ASPECTJ),
                    anyList(),
                    anyList(),
                    anyList(),
                    anyString(),
                    anyString(),
                    anyList(),
                    anyList(),
                    any(ResourceAccesses.class),
                    eq(projectPath)
            );
        }

        @Test
        @DisplayName("Should use defaults when modes are null")
        void shouldUseDefaultsWhenModesAreNull() {
            // Act
            TestableFactoryAndBuilder factory = new TestableFactoryAndBuilder(
                    mockCreator, mockWriter, mockExecuter, mockEssentialDataReader, mockProjectScanner,
                    essentialPackagesPath, essentialClassesPath,
                    null, null, null, null, projectPath
            );

            // Assert
            assertNotNull(factory);
            verify(mockProjectScanner).scanForBuildMode();
            verify(mockCreator).createTestCases(
                    eq(BuildMode.MAVEN), // from mock
                    eq(ArchitectureMode.WALA), // default
                    eq(AOPMode.INSTRUMENTATION), // default
                    anyList(),
                    anyList(),
                    anyList(),
                    anyString(),
                    anyString(),
                    anyList(),
                    anyList(),
                    any(ResourceAccesses.class),
                    eq(projectPath)
            );
        }

        @Test
        @DisplayName("Should use security policy when provided")
        void shouldUseSecurityPolicyWhenProvided() {
            // Arrange
            when(mockSecurityPolicy.regardingTheSupervisedCode()).thenReturn(mockSupervisedCode);
            when(mockSupervisedCode.theFollowingClassesAreTestClasses()).thenReturn(new String[]{"PolicyTest"});
            when(mockSupervisedCode.theSupervisedCodeUsesTheFollowingPackage()).thenReturn("com.policy");
            when(mockSupervisedCode.theMainClassInsideThisPackageIs()).thenReturn("PolicyMain");
            when(mockSupervisedCode.theFollowingResourceAccessesArePermitted()).thenReturn(mockResourceAccesses);

            // Act
            TestableFactoryAndBuilder factory = new TestableFactoryAndBuilder(
                    mockCreator, mockWriter, mockExecuter, mockEssentialDataReader, mockProjectScanner,
                    essentialPackagesPath, essentialClassesPath,
                    BuildMode.GRADLE, ArchitectureMode.ARCHUNIT, AOPMode.ASPECTJ,
                    mockSecurityPolicy, projectPath
            );

            // Assert
            assertNotNull(factory);
            verify(mockSecurityPolicy).regardingTheSupervisedCode();
            verify(mockSupervisedCode).theFollowingClassesAreTestClasses();
            verify(mockSupervisedCode).theSupervisedCodeUsesTheFollowingPackage();
            verify(mockSupervisedCode).theMainClassInsideThisPackageIs();
            verify(mockSupervisedCode).theFollowingResourceAccessesArePermitted();
        }

        @Test
        @DisplayName("Should throw NullPointerException for null creator")
        void shouldThrowNullPointerExceptionForNullCreator() {
            // Act & Assert
            assertThrows(NullPointerException.class, () -> new TestableFactoryAndBuilder(
                    null, mockWriter, mockExecuter, mockEssentialDataReader, mockProjectScanner,
                    essentialPackagesPath, essentialClassesPath,
                    BuildMode.MAVEN, ArchitectureMode.ARCHUNIT, AOPMode.ASPECTJ,
                    mockSecurityPolicy, projectPath
            ));
        }

        @Test
        @DisplayName("Should throw NullPointerException for null writer")
        void shouldThrowNullPointerExceptionForNullWriter() {
            // Act & Assert
            assertThrows(NullPointerException.class, () -> new TestableFactoryAndBuilder(
                    mockCreator, null, mockExecuter, mockEssentialDataReader, mockProjectScanner,
                    essentialPackagesPath, essentialClassesPath,
                    BuildMode.MAVEN, ArchitectureMode.ARCHUNIT, AOPMode.ASPECTJ,
                    mockSecurityPolicy, projectPath
            ));
        }

        @Test
        @DisplayName("Should throw NullPointerException for null executer")
        void shouldThrowNullPointerExceptionForNullExecuter() {
            // Act & Assert
            assertThrows(NullPointerException.class, () -> new TestableFactoryAndBuilder(
                    mockCreator, mockWriter, null, mockEssentialDataReader, mockProjectScanner,
                    essentialPackagesPath, essentialClassesPath,
                    BuildMode.MAVEN, ArchitectureMode.ARCHUNIT, AOPMode.ASPECTJ,
                    mockSecurityPolicy, projectPath
            ));
        }

        @Test
        @DisplayName("Should throw NullPointerException for null essential data reader")
        void shouldThrowNullPointerExceptionForNullEssentialDataReader() {
            // Act & Assert
            assertThrows(NullPointerException.class, () -> new TestableFactoryAndBuilder(
                    mockCreator, mockWriter, mockExecuter, null, mockProjectScanner,
                    essentialPackagesPath, essentialClassesPath,
                    BuildMode.MAVEN, ArchitectureMode.ARCHUNIT, AOPMode.ASPECTJ,
                    mockSecurityPolicy, projectPath
            ));
        }

        @Test
        @DisplayName("Should throw NullPointerException for null project scanner")
        void shouldThrowNullPointerExceptionForNullProjectScanner() {
            // Act & Assert
            assertThrows(NullPointerException.class, () -> new TestableFactoryAndBuilder(
                    mockCreator, mockWriter, mockExecuter, mockEssentialDataReader, null,
                    essentialPackagesPath, essentialClassesPath,
                    BuildMode.MAVEN, ArchitectureMode.ARCHUNIT, AOPMode.ASPECTJ,
                    mockSecurityPolicy, projectPath
            ));
        }

        @Test
        @DisplayName("Should throw NullPointerException for null essential packages path")
        void shouldThrowNullPointerExceptionForNullEssentialPackagesPath() {
            // Act & Assert
            assertThrows(NullPointerException.class, () -> new TestableFactoryAndBuilder(
                    mockCreator, mockWriter, mockExecuter, mockEssentialDataReader, mockProjectScanner,
                    null, essentialClassesPath,
                    BuildMode.MAVEN, ArchitectureMode.ARCHUNIT, AOPMode.ASPECTJ,
                    mockSecurityPolicy, projectPath
            ));
        }

        @Test
        @DisplayName("Should throw NullPointerException for null essential classes path")
        void shouldThrowNullPointerExceptionForNullEssentialClassesPath() {
            // Act & Assert
            assertThrows(NullPointerException.class, () -> new TestableFactoryAndBuilder(
                    mockCreator, mockWriter, mockExecuter, mockEssentialDataReader, mockProjectScanner,
                    essentialPackagesPath, null,
                    BuildMode.MAVEN, ArchitectureMode.ARCHUNIT, AOPMode.ASPECTJ,
                    mockSecurityPolicy, projectPath
            ));
        }
    }

    @Nested
    @DisplayName("Abstract Method Tests")
    class AbstractMethodTests {

        private TestableFactoryAndBuilder factory;

        @BeforeEach
        void setUp() {
            factory = new TestableFactoryAndBuilder(
                    mockCreator, mockWriter, mockExecuter, mockEssentialDataReader, mockProjectScanner,
                    essentialPackagesPath, essentialClassesPath,
                    BuildMode.MAVEN, ArchitectureMode.ARCHUNIT, AOPMode.ASPECTJ,
                    mockSecurityPolicy, projectPath
            );
        }

        @Test
        @DisplayName("Should write test cases successfully")
        void shouldWriteTestCasesSuccessfully() {
            // Arrange
            Path testFolderPath = Paths.get("test/output");

            // Act
            List<Path> result = factory.writeTestCases(testFolderPath);

            // Assert
            assertNotNull(result);
            assertEquals(2, result.size());
            assertEquals(Paths.get("test1.java"), result.get(0));
            assertEquals(Paths.get("test2.java"), result.get(1));
        }

        @Test
        @DisplayName("Should write test cases with null folder path")
        void shouldWriteTestCasesWithNullFolderPath() {
            // Act
            List<Path> result = factory.writeTestCases(null);

            // Assert
            assertNotNull(result);
            assertEquals(2, result.size());
        }

        @Test
        @DisplayName("Should execute test cases successfully")
        void shouldExecuteTestCasesSuccessfully() {
            // Arrange
            assertFalse(factory.wasExecuted());

            // Act
            factory.executeTestCases();

            // Assert
            assertTrue(factory.wasExecuted());
        }
    }

    @Nested
    @DisplayName("Integration Tests")
    class IntegrationTests {

        @Test
        @DisplayName("Should handle complete workflow")
        void shouldHandleCompleteWorkflow() {
            // Arrange
            TestableFactoryAndBuilder factory = new TestableFactoryAndBuilder(
                    mockCreator, mockWriter, mockExecuter, mockEssentialDataReader, mockProjectScanner,
                    essentialPackagesPath, essentialClassesPath,
                    BuildMode.MAVEN, ArchitectureMode.ARCHUNIT, AOPMode.ASPECTJ,
                    mockSecurityPolicy, projectPath
            );

            // Act
            List<Path> writtenPaths = factory.writeTestCases(Paths.get("output"));
            factory.executeTestCases();

            // Assert
            assertNotNull(writtenPaths);
            assertFalse(writtenPaths.isEmpty());
            assertTrue(factory.wasExecuted());
        }

        @Test
        @DisplayName("Should work with minimal configuration")
        void shouldWorkWithMinimalConfiguration() {
            // Act
            TestableFactoryAndBuilder factory = new TestableFactoryAndBuilder(
                    mockCreator, mockWriter, mockExecuter, mockEssentialDataReader, mockProjectScanner,
                    essentialPackagesPath, essentialClassesPath,
                    null, null, null, null, null
            );

            // Assert
            assertNotNull(factory);
            verify(mockProjectScanner).scanForBuildMode();
        }

        @Test
        @DisplayName("Should handle mixed null and non-null parameters")
        void shouldHandleMixedNullAndNonNullParameters() {
            // Act
            TestableFactoryAndBuilder factory = new TestableFactoryAndBuilder(
                    mockCreator, mockWriter, mockExecuter, mockEssentialDataReader, mockProjectScanner,
                    essentialPackagesPath, essentialClassesPath,
                    BuildMode.GRADLE, null, AOPMode.ASPECTJ, null, projectPath
            );

            // Assert
            assertNotNull(factory);
            verify(mockCreator).createTestCases(
                    eq(BuildMode.GRADLE),
                    eq(ArchitectureMode.WALA), // default
                    eq(AOPMode.ASPECTJ),
                    anyList(),
                    anyList(),
                    anyList(),
                    anyString(),
                    anyString(),
                    anyList(),
                    anyList(),
                    any(ResourceAccesses.class),
                    eq(projectPath)
            );
        }
    }
}
