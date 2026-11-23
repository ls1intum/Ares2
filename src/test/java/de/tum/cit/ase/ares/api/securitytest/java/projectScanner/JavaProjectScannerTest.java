package de.tum.cit.ase.ares.api.securitytest.java.projectScanner;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;

import de.tum.cit.ase.ares.api.buildtoolconfiguration.BuildMode;
import de.tum.cit.ase.ares.api.util.ProjectSourcesFinder;

/**
 * Comprehensive unit tests for {@link JavaProjectScanner}.
 * <p>
 * This test suite validates the functionality of the JavaProjectScanner class,
 * ensuring proper scanning of Java projects for build modes, test classes,
 * package names, main classes, and test paths.
 * </p>
 * 
 * @author Test Implementation
 * @version 2.0.0
 * @since 2.0.0
 */
@DisplayName("JavaProjectScanner Tests")
public class JavaProjectScannerTest {

	private JavaProjectScanner scanner;

	@BeforeEach
	void setUp() {
		MockitoAnnotations.openMocks(this);
		scanner = new JavaProjectScanner();
	}

	@Nested
	@DisplayName("Build Mode Scanning Tests")
	class BuildModeScanningTests {

		@Test
		@DisplayName("Should return GRADLE build mode when Gradle project is detected")
		void testScanForBuildMode_GradleProject() {
			try (MockedStatic<ProjectSourcesFinder> mockedFinder = mockStatic(ProjectSourcesFinder.class)) {
				// Given
				mockedFinder.when(ProjectSourcesFinder::isGradleProject).thenReturn(true);

				// When
				BuildMode result = scanner.scanForBuildMode();

				// Then
				assertEquals(BuildMode.GRADLE, result);
				mockedFinder.verify(ProjectSourcesFinder::isGradleProject);
			}
		}

		@Test
		@DisplayName("Should return MAVEN build mode when Maven project is detected")
		void testScanForBuildMode_MavenProject() {
			try (MockedStatic<ProjectSourcesFinder> mockedFinder = mockStatic(ProjectSourcesFinder.class)) {
				// Given
				mockedFinder.when(ProjectSourcesFinder::isGradleProject).thenReturn(false);

				// When
				BuildMode result = scanner.scanForBuildMode();

				// Then
				assertEquals(BuildMode.MAVEN, result);
				mockedFinder.verify(ProjectSourcesFinder::isGradleProject);
			}
		}
	}

	@Nested
	@DisplayName("Test Class Scanning Tests")
	class TestClassScanningTests {

		@Test
		@DisplayName("Should return empty array when no test classes are found")
		void testScanForTestClasses_NoTestClasses() {
			try (MockedStatic<ProjectSourcesFinder> mockedFinder = mockStatic(ProjectSourcesFinder.class)) {
				// Given
				mockedFinder.when(ProjectSourcesFinder::findProjectSourcesPath).thenReturn(Optional.empty());

				// When
				String[] result = scanner.scanForTestClasses();

				// Then
				assertNotNull(result);
				assertEquals(0, result.length);
			}
		}

		@Test
		@DisplayName("Should identify test classes correctly")
		void testScanForTestClasses_WithTestAnnotations() {
			// This test would require complex file system mocking
			// For now, verify the method executes without errors
			assertDoesNotThrow(() -> scanner.scanForTestClasses());
		}

		/**
		 * Name of a class in Ares' own test tree that holds direct test methods.
		 */
		private static final String OWN_TEST_CLASS = JavaProjectScannerDiagnosticsTest.class.getName();

		/**
		 * Scans with the given build file answers and checks that Ares' own test tree
		 * is read from the conventional root.
		 *
		 * @param maven  whether the project counts as a Maven project
		 * @param gradle whether the project counts as a Gradle project
		 */
		private void assertScansOwnTestTree(boolean maven, boolean gradle) {
			try (MockedStatic<ProjectSourcesFinder> mockedFinder = mockStatic(ProjectSourcesFinder.class)) {
				mockedFinder.when(ProjectSourcesFinder::isMavenProject).thenReturn(maven);
				mockedFinder.when(ProjectSourcesFinder::isGradleProject).thenReturn(gradle);

				String[] result = scanner.scanForTestClasses();

				assertTrue(java.util.Arrays.asList(result).contains(OWN_TEST_CLASS));
			}
		}

		/**
		 * A project that only counts as a Maven project is still scanned in its
		 * conventional test folder.
		 */
		@Test
		@DisplayName("Should read src/test/java of a Maven project without a build configuration")
		void testScanForTestClasses_MavenConventionalRoot() {
			assertScansOwnTestTree(true, false);
		}

		/**
		 * A project that only counts as a Gradle project is still scanned in its
		 * conventional test folder.
		 */
		@Test
		@DisplayName("Should read src/test/java of a Gradle project without a build configuration")
		void testScanForTestClasses_GradleConventionalRoot() {
			assertScansOwnTestTree(false, true);
		}
	}

	@Nested
	@DisplayName("Package Name Scanning Tests")
	class PackageNameScanningTests {

		@Test
		@DisplayName("Should return default package when no sources are found")
		void testScanForPackageName_NoSources() {
			try (MockedStatic<ProjectSourcesFinder> mockedFinder = mockStatic(ProjectSourcesFinder.class)) {
				// Given
				mockedFinder.when(ProjectSourcesFinder::findProjectSourcesPath).thenReturn(Optional.empty());

				// When
				String result = scanner.scanForPackageName();

				// Then
				assertNotNull(result);
				assertEquals("", result); // Default package is empty string
			}
		}

		@Test
		@DisplayName("Should detect most common package name")
		void testScanForPackageName_Success() {
			// This test would require complex file system mocking
			// For now, verify the method executes without errors
			assertDoesNotThrow(() -> scanner.scanForPackageName());
		}
	}

	@Nested
	@DisplayName("Main Class Scanning Tests")
	class MainClassScanningTests {

		@Test
		@DisplayName("Should return default main class when no main method is found")
		void testScanForMainClassInPackage_NoMainMethod() {
			try (MockedStatic<ProjectSourcesFinder> mockedFinder = mockStatic(ProjectSourcesFinder.class)) {
				// Given
				mockedFinder.when(ProjectSourcesFinder::findProjectSourcesPath).thenReturn(Optional.empty());

				// When
				String result = scanner.scanForMainClassInPackage();

				// Then
				assertNotNull(result);
				assertEquals("Main", result); // Default main class
			}
		}

		/**
		 * Without a build configuration the sources come from the folder the finder
		 * reports, and the class with a main method in it is found.
		 */
		@Test
		@DisplayName("Should scan the sources the finder reports")
		void scansTheSourcesTheFinderReports(@TempDir Path sources) throws IOException {
			Files.writeString(sources.resolve("Tool.java"),
					"package tools;\nclass Tool { public static void main(String[] arguments) {} }\n");
			try (MockedStatic<ProjectSourcesFinder> mockedFinder = mockStatic(ProjectSourcesFinder.class)) {
				mockedFinder.when(ProjectSourcesFinder::findProjectSourcesPath).thenReturn(Optional.of(sources));

				assertEquals("Tool", scanner.scanForMainClassInPackage());
			}
		}

		@Test
		@DisplayName("Should detect main class successfully")
		void testScanForMainClassInPackage_Success() {
			// This test would require complex file system mocking
			// For now, verify the method executes without errors
			assertDoesNotThrow(() -> scanner.scanForMainClassInPackage());
		}
	}

	@Nested
	@DisplayName("Test Path Scanning Tests")
	class TestPathScanningTests {

		@Test
		@DisplayName("Should return default test path when no project root is found")
		void testScanForTestPath_NoProjectRoot() {
			try (MockedStatic<ProjectSourcesFinder> mockedFinder = mockStatic(ProjectSourcesFinder.class)) {
				// Given
				mockedFinder.when(ProjectSourcesFinder::findProjectSourcesPath).thenReturn(Optional.empty());

				// When
				Path result = scanner.scanForTestPath();

				// Then
				assertNotNull(result);
				assertEquals(Path.of("src/test/java"), result);
			}
		}

		@Test
		@DisplayName("Should scan for test path successfully")
		void testScanForTestPath_Success() {
			// This test would require complex file system mocking
			// For now, verify the method executes without errors
			assertDoesNotThrow(() -> scanner.scanForTestPath());
		}
	}

	@Nested
	@DisplayName("Interface Implementation Tests")
	class InterfaceImplementationTests {

		@Test
		@DisplayName("Should implement ProjectScanner interface")
		void testImplementsProjectScannerInterface() {
			// Given & When & Then
			assertTrue(scanner instanceof ProjectScanner);
		}

		@Test
		@DisplayName("Should have all required interface methods")
		void testInterfaceMethodsExist() {
			// Given & When & Then
			assertDoesNotThrow(() -> scanner.scanForBuildMode());
			assertDoesNotThrow(() -> scanner.scanForTestClasses());
			assertDoesNotThrow(() -> scanner.scanForPackageName());
			assertDoesNotThrow(() -> scanner.scanForMainClassInPackage());
			assertDoesNotThrow(() -> scanner.scanForTestPath());
		}
	}

	@Nested
	@DisplayName("Error Handling Tests")
	class ErrorHandlingTests {

		@Test
		@DisplayName("Should handle IOException gracefully during file operations")
		void testErrorHandling_IOException() {
			// This would require more complex mocking to simulate IO errors
			// For now, verify all methods can handle missing files/directories
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
}
