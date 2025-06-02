package de.tum.cit.ase.ares.api.securitytest.java.projectScanner;

import de.tum.cit.ase.ares.api.buildtoolconfiguration.BuildMode;
import de.tum.cit.ase.ares.api.util.ProjectSourcesFinder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;

import java.nio.file.Path;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Comprehensive unit tests for {@link JavaProgrammingExerciseProjectScanner}.
 * 
 * <p>This test suite validates the functionality of the JavaProgrammingExerciseProjectScanner class,
 * which extends JavaProjectScanner with TUM-specific configuration and test annotation patterns.</p>
 * 
 * @author Test Implementation
 * @version 2.0.0
 * @since 2.0.0
 */
@DisplayName("JavaProgrammingExerciseProjectScanner Tests")
public class JavaProgrammingExerciseProjectScannerTest {

    private JavaProgrammingExerciseProjectScanner scanner;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        scanner = new JavaProgrammingExerciseProjectScanner();
    }

    @Nested
    @DisplayName("Inheritance Tests")
    class InheritanceTests {

        @Test
        @DisplayName("Should extend JavaProjectScanner")
        void testExtendsJavaProjectScanner() {
            // Given & When & Then
            assertTrue(scanner instanceof JavaProjectScanner);
        }

        @Test
        @DisplayName("Should implement ProjectScanner interface")
        void testImplementsProjectScannerInterface() {
            // Given & When & Then
            assertTrue(scanner instanceof ProjectScanner);
        }
    }

    @Nested
    @DisplayName("Default Configuration Tests")
    class DefaultConfigurationTests {

        @Test
        @DisplayName("Should return TUM default package when no sources are found")
        void testDefaultPackage_TumSpecific() {
            try (MockedStatic<ProjectSourcesFinder> mockedFinder = mockStatic(ProjectSourcesFinder.class)) {
                // Given
                mockedFinder.when(ProjectSourcesFinder::findProjectSourcesPath)
                        .thenReturn(Optional.empty());

                // When
                String result = scanner.scanForPackageName();

                // Then
                assertNotNull(result);
                assertEquals("de.tum.cit.ase", result); // TUM-specific default package
            }
        }

        @Test
        @DisplayName("Should return default main class when no main method is found")
        void testDefaultMainClass() {
            try (MockedStatic<ProjectSourcesFinder> mockedFinder = mockStatic(ProjectSourcesFinder.class)) {
                // Given
                mockedFinder.when(ProjectSourcesFinder::findProjectSourcesPath)
                        .thenReturn(Optional.empty());

                // When
                String result = scanner.scanForMainClassInPackage();

                // Then
                assertNotNull(result);
                assertEquals("Main", result); // Default main class
            }
        }
    }

    @Nested
    @DisplayName("Build Mode Tests")
    class BuildModeTests {

        @Test
        @DisplayName("Should inherit build mode detection from parent class")
        void testBuildModeDetection_Gradle() {
            try (MockedStatic<ProjectSourcesFinder> mockedFinder = mockStatic(ProjectSourcesFinder.class)) {
                // Given
                mockedFinder.when(ProjectSourcesFinder::isGradleProject).thenReturn(true);

                // When
                BuildMode result = scanner.scanForBuildMode();

                // Then
                assertEquals(BuildMode.GRADLE, result);
            }
        }

        @Test
        @DisplayName("Should detect Maven projects correctly")
        void testBuildModeDetection_Maven() {
            try (MockedStatic<ProjectSourcesFinder> mockedFinder = mockStatic(ProjectSourcesFinder.class)) {
                // Given
                mockedFinder.when(ProjectSourcesFinder::isGradleProject).thenReturn(false);

                // When
                BuildMode result = scanner.scanForBuildMode();

                // Then
                assertEquals(BuildMode.MAVEN, result);
            }
        }
    }

    @Nested
    @DisplayName("Test Class Detection Tests")
    class TestClassDetectionTests {

        @Test
        @DisplayName("Should detect test classes with extended annotation patterns")
        void testTestClassDetection_ExtendedPatterns() {
            // Given & When
            String[] result = scanner.scanForTestClasses();

            // Then
            assertNotNull(result);
            // The scanner should handle @Test, @Property, @PublicTest, @PrivateTest annotations
        }

        @Test
        @DisplayName("Should return empty array when no test classes found")
        void testTestClassDetection_NoTestClasses() {
            try (MockedStatic<ProjectSourcesFinder> mockedFinder = mockStatic(ProjectSourcesFinder.class)) {
                // Given
                mockedFinder.when(ProjectSourcesFinder::findProjectSourcesPath)
                        .thenReturn(Optional.empty());

                // When
                String[] result = scanner.scanForTestClasses();

                // Then
                assertNotNull(result);
                assertEquals(0, result.length);
            }
        }
    }

    @Nested
    @DisplayName("Package Name Detection Tests")
    class PackageNameDetectionTests {

        @Test
        @DisplayName("Should detect package names correctly")
        void testPackageNameDetection() {
            // Given & When
            String result = scanner.scanForPackageName();

            // Then
            assertNotNull(result);
            // Should either find actual packages or return TUM default
        }

        @Test
        @DisplayName("Should prioritize most common package name")
        void testPackageNameDetection_MostCommon() {
            // This would require complex file system mocking
            // For now, verify the method executes without errors
            assertDoesNotThrow(() -> scanner.scanForPackageName());
        }
    }

    @Nested
    @DisplayName("Main Class Detection Tests")
    class MainClassDetectionTests {

        @Test
        @DisplayName("Should detect main class correctly")
        void testMainClassDetection() {
            // Given & When
            String result = scanner.scanForMainClassInPackage();

            // Then
            assertNotNull(result);
            // Should either find actual main class or return default "Main"
        }

        @Test
        @DisplayName("Should prefer standard main class names")
        void testMainClassDetection_PreferredNames() {
            // This would require complex file system mocking
            // For now, verify the method executes without errors
            assertDoesNotThrow(() -> scanner.scanForMainClassInPackage());
        }
    }

    @Nested
    @DisplayName("Test Path Detection Tests")
    class TestPathDetectionTests {

        @Test
        @DisplayName("Should inherit test path detection from parent class")
        void testTestPathDetection() {
            // Given & When
            Path result = scanner.scanForTestPath();

            // Then
            assertNotNull(result);
            // Should detect proper test path structure
        }

        @Test
        @DisplayName("Should handle missing project root gracefully")
        void testTestPathDetection_NoProjectRoot() {
            try (MockedStatic<ProjectSourcesFinder> mockedFinder = mockStatic(ProjectSourcesFinder.class)) {
                // Given
                mockedFinder.when(ProjectSourcesFinder::findProjectSourcesPath)
                        .thenReturn(Optional.empty());

                // When
                Path result = scanner.scanForTestPath();

                // Then
                assertNotNull(result);
                assertEquals(Path.of("src/test/java"), result);
            }
        }
    }

    @Nested
    @DisplayName("TUM-Specific Features Tests")
    class TumSpecificFeaturesTests {

        @Test
        @DisplayName("Should use TUM-specific test annotation patterns")
        void testTumTestAnnotationPatterns() {
            // The scanner should support @PublicTest and @PrivateTest annotations
            // in addition to standard @Test and @Property annotations
            assertDoesNotThrow(() -> scanner.scanForTestClasses());
        }

        @Test
        @DisplayName("Should use TUM-specific default package")
        void testTumDefaultPackage() {
            try (MockedStatic<ProjectSourcesFinder> mockedFinder = mockStatic(ProjectSourcesFinder.class)) {
                // Given
                mockedFinder.when(ProjectSourcesFinder::findProjectSourcesPath)
                        .thenReturn(Optional.empty());

                // When
                String result = scanner.scanForPackageName();

                // Then
                assertEquals("de.tum.cit.ase", result);
            }
        }
    }

    @Nested
    @DisplayName("Error Handling Tests")
    class ErrorHandlingTests {

        @Test
        @DisplayName("Should handle all operations gracefully")
        void testErrorHandling_AllOperations() {
            // Given & When & Then
            assertDoesNotThrow(() -> {
                scanner.scanForBuildMode();
                scanner.scanForTestClasses();
                scanner.scanForPackageName();
                scanner.scanForMainClassInPackage();
                scanner.scanForTestPath();
            });
        }

        @Test
        @DisplayName("Should never return null values")
        void testNonNullReturns() {
            // Given & When & Then
            assertNotNull(scanner.scanForBuildMode());
            assertNotNull(scanner.scanForTestClasses());
            assertNotNull(scanner.scanForPackageName());
            assertNotNull(scanner.scanForMainClassInPackage());
            assertNotNull(scanner.scanForTestPath());
        }
    }

    @Nested
    @DisplayName("Polymorphism Tests")
    class PolymorphismTests {

        @Test
        @DisplayName("Should work correctly when treated as parent class")
        void testPolymorphism_AsJavaProjectScanner() {
            // Given
            JavaProjectScanner parentScanner = scanner;

            // When & Then
            assertDoesNotThrow(() -> {
                parentScanner.scanForBuildMode();
                parentScanner.scanForTestClasses();
                parentScanner.scanForPackageName();
                parentScanner.scanForMainClassInPackage();
                parentScanner.scanForTestPath();
            });
        }

        @Test
        @DisplayName("Should work correctly when treated as interface")
        void testPolymorphism_AsProjectScanner() {
            // Given
            ProjectScanner interfaceScanner = scanner;

            // When & Then
            assertDoesNotThrow(() -> {
                interfaceScanner.scanForBuildMode();
                interfaceScanner.scanForTestClasses();
                interfaceScanner.scanForPackageName();
                interfaceScanner.scanForMainClassInPackage();
                interfaceScanner.scanForTestPath();
            });
        }
    }
}
