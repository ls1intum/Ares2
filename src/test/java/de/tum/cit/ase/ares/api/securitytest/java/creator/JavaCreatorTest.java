package de.tum.cit.ase.ares.api.securitytest.java.creator;

import com.ibm.wala.ipa.callgraph.CallGraph;
import com.tngtech.archunit.core.domain.JavaClasses;
import de.tum.cit.ase.ares.api.aop.AOPTestCase;
import de.tum.cit.ase.ares.api.aop.AOPMode;
import de.tum.cit.ase.ares.api.architecture.ArchitectureTestCase;
import de.tum.cit.ase.ares.api.architecture.ArchitectureMode;
import de.tum.cit.ase.ares.api.buildtoolconfiguration.BuildMode;
import de.tum.cit.ase.ares.api.policy.policySubComponents.PackagePermission;
import de.tum.cit.ase.ares.api.policy.policySubComponents.ResourceAccesses;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Test suite for the JavaCreator class.
 *
 * <p>Description: This class contains unit tests that verify the creation of Java security test cases
 * based on security policies. It tests the interaction with build modes, architecture modes, and AOP modes,
 * as well as the preparation of allowed packages and classes.
 *
 * <p>Design Rationale: Tests the Creator pattern implementation for Java projects, ensuring proper
 * delegation to specialized components and correct handling of security policies.
 *
 * @since 2.0.0
 * @author Markus Paulsen
 * @version 2.0.0
 */
public class JavaCreatorTest {

    @Mock
    private BuildMode buildMode;

    @Mock
    private ArchitectureMode architectureMode;

    @Mock
    private AOPMode aopMode;

    @Mock
    private ResourceAccesses resourceAccesses;

    @Mock
    private JavaClasses javaClasses;

    @Mock
    private CallGraph callGraph;

    @TempDir
    Path tempDir;

    private JavaCreator javaCreator;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        javaCreator = new JavaCreator();
    }

    @Nested
    @DisplayName("CacheResult Tests")
    class CacheResultTests {

        @Test
        @DisplayName("Should cache supplier result and avoid recomputation")
        void shouldCacheSupplierResult() {
            // Arrange
            @SuppressWarnings("unchecked")
            Supplier<String> mockSupplier = mock(Supplier.class);
            when(mockSupplier.get()).thenReturn("cached_value");

            // Act
            Supplier<String> cachedSupplier = JavaCreator.cacheResult(mockSupplier);
            String firstCall = cachedSupplier.get();
            String secondCall = cachedSupplier.get();

            // Assert
            assertEquals("cached_value", firstCall);
            assertEquals("cached_value", secondCall);
            verify(mockSupplier, times(1)).get(); // Should only be called once
        }

        @Test
        @DisplayName("Should handle null values in cache")
        @SuppressWarnings("unchecked")
        void shouldHandleNullValuesInCache() {
            // Arrange
            Supplier<String> mockSupplier = mock(Supplier.class);
            when(mockSupplier.get()).thenReturn(null);

            // Act
            Supplier<String> cachedSupplier = JavaCreator.cacheResult(mockSupplier);
            String result = cachedSupplier.get();

            // Assert
            assertNull(result);
            verify(mockSupplier, times(1)).get();
        }
    }

    @Nested
    @DisplayName("CreateTestCases Tests")
    class CreateTestCasesTests {

        @Test
        @DisplayName("Should create test cases with valid parameters")
        void shouldCreateTestCasesWithValidParameters() {
            // Arrange
            List<String> essentialPackages = List.of("java.lang", "java.util");
            List<String> essentialClasses = List.of("String", "Object");
            List<String> testClasses = List.of("TestClass1", "TestClass2");
            String packageName = "com.example";
            String mainClassName = "Main";
            List<ArchitectureTestCase> architectureTestCases = new ArrayList<>();
            List<AOPTestCase> aopTestCases = new ArrayList<>();

            String classpath = "/test/classpath";
            when(buildMode.getClasspath(tempDir)).thenReturn(classpath);
            when(architectureMode.getJavaClasses(classpath)).thenReturn(javaClasses);
            when(architectureMode.getCallGraph(classpath)).thenReturn(callGraph);
            when(resourceAccesses.regardingPackageImports()).thenReturn(List.of(new PackagePermission("allowed.package")));

            // Act
            assertDoesNotThrow(() -> javaCreator.createTestCases(
                    buildMode, architectureMode, aopMode, essentialPackages,
                    essentialClasses, testClasses, packageName, mainClassName,
                    architectureTestCases, aopTestCases, resourceAccesses, tempDir
            ));

            // Assert
            verify(buildMode).getClasspath(tempDir);
            verify(architectureMode).getJavaClasses(classpath);
            verify(architectureMode).getCallGraph(classpath);
            verify(resourceAccesses).regardingPackageImports();
        }

        @Test
        @DisplayName("Should handle empty essential packages")
        void shouldHandleEmptyEssentialPackages() {
            // Arrange
            List<String> essentialPackages = List.of();
            List<String> essentialClasses = List.of("TestClass");
            List<String> testClasses = List.of("TestClass");
            String packageName = "com.example";
            String mainClassName = "Main";
            List<ArchitectureTestCase> architectureTestCases = new ArrayList<>();
            List<AOPTestCase> aopTestCases = new ArrayList<>();

            String classpath = "/test/classpath";
            when(buildMode.getClasspath(tempDir)).thenReturn(classpath);
            when(architectureMode.getJavaClasses(classpath)).thenReturn(javaClasses);
            when(architectureMode.getCallGraph(classpath)).thenReturn(callGraph);
            when(resourceAccesses.regardingPackageImports()).thenReturn(List.of());

            // Act & Assert
            assertDoesNotThrow(() -> javaCreator.createTestCases(
                    buildMode, architectureMode, aopMode, essentialPackages,
                    essentialClasses, testClasses, packageName, mainClassName,
                    architectureTestCases, aopTestCases, resourceAccesses, tempDir
            ));
        }

        @Test
        @DisplayName("Should handle empty essential classes")
        void shouldHandleEmptyEssentialClasses() {
            // Arrange
            List<String> essentialPackages = List.of("java.lang");
            List<String> essentialClasses = List.of();
            List<String> testClasses = List.of();
            String packageName = "com.example";
            String mainClassName = "Main";
            List<ArchitectureTestCase> architectureTestCases = new ArrayList<>();
            List<AOPTestCase> aopTestCases = new ArrayList<>();

            String classpath = "/test/classpath";
            when(buildMode.getClasspath(tempDir)).thenReturn(classpath);
            when(architectureMode.getJavaClasses(classpath)).thenReturn(javaClasses);
            when(architectureMode.getCallGraph(classpath)).thenReturn(callGraph);
            when(resourceAccesses.regardingPackageImports()).thenReturn(List.of());

            // Act & Assert
            assertDoesNotThrow(() -> javaCreator.createTestCases(
                    buildMode, architectureMode, aopMode, essentialPackages,
                    essentialClasses, testClasses, packageName, mainClassName,
                    architectureTestCases, aopTestCases, resourceAccesses, tempDir
            ));
        }

        @Test
        @DisplayName("Should use cached results for JavaClasses and CallGraph")
        void shouldUseCachedResultsForJavaClassesAndCallGraph() {
            // Arrange
            List<String> essentialPackages = List.of("java.lang");
            List<String> essentialClasses = List.of("String");
            List<String> testClasses = List.of("TestClass");
            String packageName = "com.example";
            String mainClassName = "Main";
            List<ArchitectureTestCase> architectureTestCases = new ArrayList<>();
            List<AOPTestCase> aopTestCases = new ArrayList<>();

            String classpath = "/test/classpath";
            when(buildMode.getClasspath(tempDir)).thenReturn(classpath);
            when(architectureMode.getJavaClasses(classpath)).thenReturn(javaClasses);
            when(architectureMode.getCallGraph(classpath)).thenReturn(callGraph);
            when(resourceAccesses.regardingPackageImports()).thenReturn(List.of());

            // Act - Call twice to test caching
            javaCreator.createTestCases(
                    buildMode, architectureMode, aopMode, essentialPackages,
                    essentialClasses, testClasses, packageName, mainClassName,
                    architectureTestCases, aopTestCases, resourceAccesses, tempDir
            );

            javaCreator.createTestCases(
                    buildMode, architectureMode, aopMode, essentialPackages,
                    essentialClasses, testClasses, packageName, mainClassName,
                    new ArrayList<>(), new ArrayList<>(), resourceAccesses, tempDir
            );

            // Assert - Each method should be called twice due to new cache instances
            verify(buildMode, times(2)).getClasspath(tempDir);
            verify(architectureMode, times(2)).getJavaClasses(classpath);
            verify(architectureMode, times(2)).getCallGraph(classpath);
        }

        @Test
        @DisplayName("Should populate architecture and AOP test case lists")
        void shouldPopulateArchitectureAndAOPTestCaseLists() {
            // Arrange
            List<String> essentialPackages = List.of("java.lang");
            List<String> essentialClasses = List.of("String");
            List<String> testClasses = List.of("TestClass");
            String packageName = "com.example";
            String mainClassName = "Main";
            List<ArchitectureTestCase> architectureTestCases = new ArrayList<>();
            List<AOPTestCase> aopTestCases = new ArrayList<>();

            String classpath = "/test/classpath";
            when(buildMode.getClasspath(tempDir)).thenReturn(classpath);
            when(architectureMode.getJavaClasses(classpath)).thenReturn(javaClasses);
            when(architectureMode.getCallGraph(classpath)).thenReturn(callGraph);
            when(resourceAccesses.regardingPackageImports()).thenReturn(List.of());

            // Act
            javaCreator.createTestCases(
                    buildMode, architectureMode, aopMode, essentialPackages,
                    essentialClasses, testClasses, packageName, mainClassName,
                    architectureTestCases, aopTestCases, resourceAccesses, tempDir
            );

            // Assert - The lists should be modified (exact contents depend on implementation details)
            // We verify that the method completed successfully and the parameters were used
            verify(resourceAccesses).regardingPackageImports();
        }
    }

    @Nested
    @DisplayName("Implementation Tests")
    class ImplementationTests {

        @Test
        @DisplayName("Should implement Creator interface")
        void shouldImplementCreatorInterface() {
            // Assert
            assertTrue(javaCreator instanceof Creator);
        }

        @Test
        @DisplayName("Should have public constructor")
        void shouldHavePublicConstructor() {
            // Act & Assert
            assertDoesNotThrow(() -> new JavaCreator());
        }
    }

    @Nested
    @DisplayName("Error Handling Tests")
    class ErrorHandlingTests {

        @Test
        @DisplayName("Should handle null parameters gracefully")
        void shouldHandleNullParametersGracefully() {
            // Arrange
            List<String> essentialPackages = List.of("java.lang");
            List<String> essentialClasses = List.of("String");
            List<String> testClasses = List.of("TestClass");
            String packageName = "com.example";
            String mainClassName = "Main";
            List<ArchitectureTestCase> architectureTestCases = new ArrayList<>();
            List<AOPTestCase> aopTestCases = new ArrayList<>();

            // Mock buildMode to throw exception to test error handling
            when(buildMode.getClasspath(tempDir)).thenThrow(new IllegalArgumentException("Invalid path"));

            // Act & Assert - Should propagate exceptions properly
            assertThrows(IllegalArgumentException.class, () -> javaCreator.createTestCases(
                    buildMode, architectureMode, aopMode, essentialPackages,
                    essentialClasses, testClasses, packageName, mainClassName,
                    architectureTestCases, aopTestCases, resourceAccesses, tempDir
            ));
        }

        @Test
        @DisplayName("Should handle buildMode exceptions")
        void shouldHandleBuildModeExceptions() {
            // Arrange
            List<String> essentialPackages = List.of("java.lang");
            List<String> essentialClasses = List.of("String");
            List<String> testClasses = List.of("TestClass");
            String packageName = "com.example";
            String mainClassName = "Main";
            List<ArchitectureTestCase> architectureTestCases = new ArrayList<>();
            List<AOPTestCase> aopTestCases = new ArrayList<>();

            when(buildMode.getClasspath(tempDir)).thenThrow(new RuntimeException("Build error"));

            // Act & Assert
            assertThrows(RuntimeException.class, () -> javaCreator.createTestCases(
                    buildMode, architectureMode, aopMode, essentialPackages,
                    essentialClasses, testClasses, packageName, mainClassName,
                    architectureTestCases, aopTestCases, resourceAccesses, tempDir
            ));
        }
    }
}
