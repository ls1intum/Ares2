package de.tum.cit.ase.ares.api.policy;

import de.tum.cit.ase.ares.api.policy.director.SecurityPolicyDirector;
import de.tum.cit.ase.ares.api.policy.reader.SecurityPolicyReader;
import de.tum.cit.ase.ares.api.securitytest.TestCaseAbstractFactoryAndBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@DisplayName("SecurityPolicyReaderAndDirector Tests")
public class SecurityPolicyReaderAndDirectorTest {

    @Mock private SecurityPolicyReader mockSecurityPolicyReader;
    @Mock private SecurityPolicyDirector mockSecurityPolicyDirector;
    @Mock private SecurityPolicy mockSecurityPolicy;
    @Mock private TestCaseAbstractFactoryAndBuilder mockFactoryAndBuilder;

    @TempDir
    Path tempDir;

    private Path securityPolicyFilePath;
    private Path projectFolderPath;

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        securityPolicyFilePath = tempDir.resolve("security-policy.yaml");
        projectFolderPath = tempDir.resolve("project");
        Files.createFile(securityPolicyFilePath);
        Files.createDirectories(projectFolderPath);
    }

    @Nested
    @DisplayName("Constructor Tests")
    class ConstructorTests {

        @Test
        @DisplayName("Should create instance with valid parameters")
        void shouldCreateInstanceWithValidParameters() {
            // Act
            SecurityPolicyReaderAndDirector instance = new SecurityPolicyReaderAndDirector(
                    securityPolicyFilePath, projectFolderPath);

            // Assert
            assertNotNull(instance);
        }

        @Test
        @DisplayName("Should create instance with null parameters")
        void shouldCreateInstanceWithNullParameters() {
            // Act
            SecurityPolicyReaderAndDirector instance = new SecurityPolicyReaderAndDirector(null, null);

            // Assert
            assertNotNull(instance);
        }
    }

    @Nested
    @DisplayName("Builder Tests")
    class BuilderTests {

        @Test
        @DisplayName("Should build instance using builder pattern")
        void shouldBuildInstanceUsingBuilderPattern() {
            // Act
            SecurityPolicyReaderAndDirector instance = SecurityPolicyReaderAndDirector.builder()
                    .securityPolicyFilePath(securityPolicyFilePath)
                    .projectFolderPath(projectFolderPath)
                    .build();

            // Assert
            assertNotNull(instance);
        }

        @Test
        @DisplayName("Should build instance with null values")
        void shouldBuildInstanceWithNullValues() {
            // Act
            SecurityPolicyReaderAndDirector instance = SecurityPolicyReaderAndDirector.builder()
                    .securityPolicyFilePath(null)
                    .projectFolderPath(null)
                    .build();

            // Assert
            assertNotNull(instance);
        }

        @Test
        @DisplayName("Builder should return non-null builder")
        void builderShouldReturnNonNullBuilder() {
            // Act
            SecurityPolicyReaderAndDirector.Builder builder = SecurityPolicyReaderAndDirector.builder();

            // Assert
            assertNotNull(builder);
        }
    }

    @Nested
    @DisplayName("CreateTestCases Tests")
    class CreateTestCasesTests {

        @Test
        @DisplayName("Should create test cases when security policy file exists")
        void shouldCreateTestCasesWhenSecurityPolicyFileExists() {
            try (MockedStatic<SecurityPolicyReader> readerMock = mockStatic(SecurityPolicyReader.class);
                 MockedStatic<SecurityPolicyDirector> directorMock = mockStatic(SecurityPolicyDirector.class)) {

                // Arrange
                readerMock.when(() -> SecurityPolicyReader.selectSecurityPolicyReader(securityPolicyFilePath))
                        .thenReturn(mockSecurityPolicyReader);
                when(mockSecurityPolicyReader.readSecurityPolicyFrom(securityPolicyFilePath))
                        .thenReturn(mockSecurityPolicy);
                directorMock.when(() -> SecurityPolicyDirector.selectSecurityPolicyDirector(mockSecurityPolicy))
                        .thenReturn(mockSecurityPolicyDirector);
                when(mockSecurityPolicyDirector.createTestCases(mockSecurityPolicy, projectFolderPath))
                        .thenReturn(mockFactoryAndBuilder);

                SecurityPolicyReaderAndDirector instance = new SecurityPolicyReaderAndDirector(
                        securityPolicyFilePath, projectFolderPath);

                // Act
                SecurityPolicyReaderAndDirector result = instance.createTestCases();

                // Assert
                assertNotNull(result);
                assertSame(instance, result);
                verify(mockSecurityPolicyReader).readSecurityPolicyFrom(securityPolicyFilePath);
                verify(mockSecurityPolicyDirector).createTestCases(mockSecurityPolicy, projectFolderPath);
            }
        }

        @Test
        @DisplayName("Should handle null security policy file path")
        void shouldHandleNullSecurityPolicyFilePath() {
            // Arrange
            SecurityPolicyReaderAndDirector instance = new SecurityPolicyReaderAndDirector(null, projectFolderPath);

            // Act
            SecurityPolicyReaderAndDirector result = instance.createTestCases();

            // Assert
            assertNotNull(result);
            assertSame(instance, result);
        }

        @Test
        @DisplayName("Should handle null project folder path")
        void shouldHandleNullProjectFolderPath() {
            try (MockedStatic<SecurityPolicyReader> readerMock = mockStatic(SecurityPolicyReader.class);
                 MockedStatic<SecurityPolicyDirector> directorMock = mockStatic(SecurityPolicyDirector.class)) {

                // Arrange
                readerMock.when(() -> SecurityPolicyReader.selectSecurityPolicyReader(securityPolicyFilePath))
                        .thenReturn(mockSecurityPolicyReader);
                when(mockSecurityPolicyReader.readSecurityPolicyFrom(securityPolicyFilePath))
                        .thenReturn(mockSecurityPolicy);
                directorMock.when(() -> SecurityPolicyDirector.selectSecurityPolicyDirector(mockSecurityPolicy))
                        .thenReturn(mockSecurityPolicyDirector);
                when(mockSecurityPolicyDirector.createTestCases(mockSecurityPolicy, null))
                        .thenReturn(mockFactoryAndBuilder);

                SecurityPolicyReaderAndDirector instance = new SecurityPolicyReaderAndDirector(
                        securityPolicyFilePath, null);

                // Act
                SecurityPolicyReaderAndDirector result = instance.createTestCases();

                // Assert
                assertNotNull(result);
                assertSame(instance, result);
                verify(mockSecurityPolicyDirector).createTestCases(mockSecurityPolicy, null);
            }
        }
    }

    @Nested
    @DisplayName("WriteTestCases Tests")
    class WriteTestCasesTests {

        @Test
        @DisplayName("Should write test cases successfully")
        void shouldWriteTestCasesSuccessfully() {
            try (MockedStatic<SecurityPolicyReader> readerMock = mockStatic(SecurityPolicyReader.class);
                 MockedStatic<SecurityPolicyDirector> directorMock = mockStatic(SecurityPolicyDirector.class)) {

                // Arrange
                List<Path> expectedPaths = Arrays.asList(tempDir.resolve("test1.java"), tempDir.resolve("test2.java"));
                
                readerMock.when(() -> SecurityPolicyReader.selectSecurityPolicyReader(securityPolicyFilePath))
                        .thenReturn(mockSecurityPolicyReader);
                when(mockSecurityPolicyReader.readSecurityPolicyFrom(securityPolicyFilePath))
                        .thenReturn(mockSecurityPolicy);
                directorMock.when(() -> SecurityPolicyDirector.selectSecurityPolicyDirector(mockSecurityPolicy))
                        .thenReturn(mockSecurityPolicyDirector);
                when(mockSecurityPolicyDirector.createTestCases(mockSecurityPolicy, projectFolderPath))
                        .thenReturn(mockFactoryAndBuilder);
                when(mockFactoryAndBuilder.writeTestCases(any(Path.class))).thenReturn(expectedPaths);

                SecurityPolicyReaderAndDirector instance = new SecurityPolicyReaderAndDirector(
                        securityPolicyFilePath, projectFolderPath);
                instance.createTestCases();

                Path testFolderPath = tempDir.resolve("test");

                // Act
                List<Path> result = instance.writeTestCases(testFolderPath);

                // Assert
                assertNotNull(result);
                assertEquals(expectedPaths, result);
                verify(mockFactoryAndBuilder).writeTestCases(testFolderPath);
            }
        }

        @Test
        @DisplayName("Should throw exception when factory not initialized")
        void shouldThrowExceptionWhenFactoryNotInitialized() {
            // Arrange
            SecurityPolicyReaderAndDirector instance = new SecurityPolicyReaderAndDirector(
                    securityPolicyFilePath, projectFolderPath);
            Path testFolderPath = tempDir.resolve("test");

            // Act & Assert
            assertThrows(NullPointerException.class, () -> instance.writeTestCases(testFolderPath));
        }
    }

    @Nested
    @DisplayName("WriteTestCasesAndContinue Tests")
    class WriteTestCasesAndContinueTests {

        @Test
        @DisplayName("Should write test cases and continue")
        void shouldWriteTestCasesAndContinue() {
            try (MockedStatic<SecurityPolicyReader> readerMock = mockStatic(SecurityPolicyReader.class);
                 MockedStatic<SecurityPolicyDirector> directorMock = mockStatic(SecurityPolicyDirector.class)) {

                // Arrange
                List<Path> expectedPaths = Arrays.asList(tempDir.resolve("test1.java"));
                
                readerMock.when(() -> SecurityPolicyReader.selectSecurityPolicyReader(securityPolicyFilePath))
                        .thenReturn(mockSecurityPolicyReader);
                when(mockSecurityPolicyReader.readSecurityPolicyFrom(securityPolicyFilePath))
                        .thenReturn(mockSecurityPolicy);
                directorMock.when(() -> SecurityPolicyDirector.selectSecurityPolicyDirector(mockSecurityPolicy))
                        .thenReturn(mockSecurityPolicyDirector);
                when(mockSecurityPolicyDirector.createTestCases(mockSecurityPolicy, projectFolderPath))
                        .thenReturn(mockFactoryAndBuilder);
                when(mockFactoryAndBuilder.writeTestCases(any(Path.class))).thenReturn(expectedPaths);

                SecurityPolicyReaderAndDirector instance = new SecurityPolicyReaderAndDirector(
                        securityPolicyFilePath, projectFolderPath);
                instance.createTestCases();

                Path testFolderPath = tempDir.resolve("test");

                // Act
                SecurityPolicyReaderAndDirector result = instance.writeTestCasesAndContinue(testFolderPath);

                // Assert
                assertNotNull(result);
                assertSame(instance, result);
                verify(mockFactoryAndBuilder).writeTestCases(testFolderPath);
            }
        }

        @Test
        @DisplayName("Should throw exception when factory not initialized")
        void shouldThrowExceptionWhenFactoryNotInitializedForContinue() {
            // Arrange
            SecurityPolicyReaderAndDirector instance = new SecurityPolicyReaderAndDirector(
                    securityPolicyFilePath, projectFolderPath);
            Path testFolderPath = tempDir.resolve("test");

            // Act & Assert
            assertThrows(NullPointerException.class, () -> instance.writeTestCasesAndContinue(testFolderPath));
        }
    }

    @Nested
    @DisplayName("ExecuteTestCases Tests")
    class ExecuteTestCasesTests {

        @Test
        @DisplayName("Should execute test cases successfully")
        void shouldExecuteTestCasesSuccessfully() {
            try (MockedStatic<SecurityPolicyReader> readerMock = mockStatic(SecurityPolicyReader.class);
                 MockedStatic<SecurityPolicyDirector> directorMock = mockStatic(SecurityPolicyDirector.class)) {

                // Arrange
                readerMock.when(() -> SecurityPolicyReader.selectSecurityPolicyReader(securityPolicyFilePath))
                        .thenReturn(mockSecurityPolicyReader);
                when(mockSecurityPolicyReader.readSecurityPolicyFrom(securityPolicyFilePath))
                        .thenReturn(mockSecurityPolicy);
                directorMock.when(() -> SecurityPolicyDirector.selectSecurityPolicyDirector(mockSecurityPolicy))
                        .thenReturn(mockSecurityPolicyDirector);
                when(mockSecurityPolicyDirector.createTestCases(mockSecurityPolicy, projectFolderPath))
                        .thenReturn(mockFactoryAndBuilder);

                SecurityPolicyReaderAndDirector instance = new SecurityPolicyReaderAndDirector(
                        securityPolicyFilePath, projectFolderPath);
                instance.createTestCases();

                // Act
                SecurityPolicyReaderAndDirector result = instance.executeTestCases();

                // Assert
                assertNotNull(result);
                assertSame(instance, result);
                verify(mockFactoryAndBuilder).executeTestCases();
            }
        }

        @Test
        @DisplayName("Should throw exception when factory not initialized")
        void shouldThrowExceptionWhenFactoryNotInitializedForExecute() {
            // Arrange
            SecurityPolicyReaderAndDirector instance = new SecurityPolicyReaderAndDirector(
                    securityPolicyFilePath, projectFolderPath);

            // Act & Assert
            assertThrows(NullPointerException.class, () -> instance.executeTestCases());
        }
    }
}
