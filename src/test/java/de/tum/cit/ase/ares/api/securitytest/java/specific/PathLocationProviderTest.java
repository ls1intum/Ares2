package de.tum.cit.ase.ares.api.securitytest.java.specific;

import com.tngtech.archunit.core.importer.Location;
import com.tngtech.archunit.junit.LocationProvider;
import de.tum.cit.ase.ares.api.securitytest.java.StudentCompiledClassesPath;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;

import java.nio.file.Paths;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive unit tests for {@link PathLocationProvider}.
 * 
 * <p>This test suite validates the functionality of the PathLocationProvider class,
 * ensuring proper location detection from annotated test classes and appropriate
 * error handling for missing annotations.</p>
 * 
 * @author Test Implementation
 * @version 2.0.0
 * @since 2.0.0
 */
@DisplayName("PathLocationProvider Tests")
public class PathLocationProviderTest {

    private PathLocationProvider provider;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        provider = new PathLocationProvider();
    }

    @Nested
    @DisplayName("Interface Implementation Tests")
    class InterfaceImplementationTests {

        @Test
        @DisplayName("Should implement LocationProvider interface")
        void testImplementsLocationProviderInterface() {
            // Given & When & Then
            assertTrue(provider instanceof LocationProvider);
        }
    }

    @Nested
    @DisplayName("Successful Location Retrieval Tests")
    class SuccessfulLocationRetrievalTests {

        @StudentCompiledClassesPath("target/classes")
        static class AnnotatedTestClass {
        }

        @StudentCompiledClassesPath("/custom/path/to/classes")
        static class CustomPathTestClass {
        }

        @StudentCompiledClassesPath("build/classes/java/main")
        static class GradleStyleTestClass {
        }

        @Test
        @DisplayName("Should retrieve location from annotated test class")
        void testGetLocation_WithAnnotation() {
            // Given & When
            Set<Location> result = provider.get(AnnotatedTestClass.class);

            // Then
            assertNotNull(result);
            assertEquals(1, result.size());
            Location location = result.iterator().next();
            assertTrue(location.asURI().toString().contains("target/classes"));
        }

        @Test
        @DisplayName("Should handle custom path correctly")
        void testGetLocation_CustomPath() {
            // Given & When
            Set<Location> result = provider.get(CustomPathTestClass.class);

            // Then
            assertNotNull(result);
            assertEquals(1, result.size());
            Location location = result.iterator().next();
            assertTrue(location.asURI().toString().contains("custom/path/to/classes"));
        }

        @Test
        @DisplayName("Should handle Gradle-style paths")
        void testGetLocation_GradlePath() {
            // Given & When
            Set<Location> result = provider.get(GradleStyleTestClass.class);

            // Then
            assertNotNull(result);
            assertEquals(1, result.size());
            Location location = result.iterator().next();
            assertTrue(location.asURI().toString().contains("build/classes/java/main"));
        }
    }

    @Nested
    @DisplayName("Error Handling Tests")
    class ErrorHandlingTests {

        static class NonAnnotatedTestClass {
        }

        @Test
        @DisplayName("Should throw SecurityException for non-annotated class")
        void testGetLocation_NoAnnotation() {
            // Given & When & Then
            SecurityException exception = assertThrows(SecurityException.class, 
                () -> provider.get(NonAnnotatedTestClass.class));
            
            assertTrue(exception.getMessage().contains("Ares Security Error"));
            assertTrue(exception.getMessage().contains("PathLocationProvider"));
            assertTrue(exception.getMessage().contains("StudentCompiledClassesPath"));
        }

        @Test
        @DisplayName("Should throw NullPointerException for null test class")
        @SuppressWarnings("null")
        void testGetLocation_NullTestClass() {
            // Given & When & Then
            assertThrows(NullPointerException.class, 
                () -> provider.get(null));
        }

        @Test
        @DisplayName("Should provide specific error message format")
        void testGetLocation_ErrorMessageFormat() {
            // Given & When
            SecurityException exception = assertThrows(SecurityException.class, 
                () -> provider.get(NonAnnotatedTestClass.class));

            // Then
            String expectedMessage = String.format(
                "Ares Security Error (Reason: Ares-Code; Stage: Creation): %s can only be used on classes annotated with @%s",
                "PathLocationProvider", "StudentCompiledClassesPath"
            );
            assertEquals(expectedMessage, exception.getMessage());
        }
    }

    @Nested
    @DisplayName("Return Value Tests")
    class ReturnValueTests {

        @StudentCompiledClassesPath("test/path")
        static class TestClass {
        }

        @Test
        @DisplayName("Should return non-null set")
        void testGetLocation_NonNullReturn() {
            // Given & When
            Set<Location> result = provider.get(TestClass.class);

            // Then
            assertNotNull(result);
        }

        @Test
        @DisplayName("Should return singleton set")
        void testGetLocation_SingletonSet() {
            // Given & When
            Set<Location> result = provider.get(TestClass.class);

            // Then
            assertEquals(1, result.size());
        }

        @Test
        @DisplayName("Should return immutable set")
        void testGetLocation_ImmutableSet() {
            // Given & When
            Set<Location> result = provider.get(TestClass.class);

            // Then
            assertThrows(UnsupportedOperationException.class, 
                () -> result.clear());
        }
    }

    @Nested
    @DisplayName("Annotation Processing Tests")
    class AnnotationProcessingTests {

        @StudentCompiledClassesPath("")
        static class EmptyPathTestClass {
        }

        @StudentCompiledClassesPath("relative/path")
        static class RelativePathTestClass {
        }

        @StudentCompiledClassesPath("./current/directory")
        static class CurrentDirectoryTestClass {
        }

        @Test
        @DisplayName("Should handle empty path annotation")
        void testGetLocation_EmptyPath() {
            // Given & When
            Set<Location> result = provider.get(EmptyPathTestClass.class);

            // Then
            assertNotNull(result);
            assertEquals(1, result.size());
            // Empty path should result in current directory
        }

        @Test
        @DisplayName("Should handle relative paths")
        void testGetLocation_RelativePath() {
            // Given & When
            Set<Location> result = provider.get(RelativePathTestClass.class);

            // Then
            assertNotNull(result);
            assertEquals(1, result.size());
            Location location = result.iterator().next();
            assertTrue(location.asURI().toString().contains("relative/path"));
        }

        @Test
        @DisplayName("Should handle current directory notation")
        void testGetLocation_CurrentDirectory() {
            // Given & When
            Set<Location> result = provider.get(CurrentDirectoryTestClass.class);

            // Then
            assertNotNull(result);
            assertEquals(1, result.size());
            Location location = result.iterator().next();
            assertTrue(location.asURI().toString().contains("current/directory"));
        }
    }

    @Nested
    @DisplayName("Security Tests")
    class SecurityTests {

        @Test
        @DisplayName("Should enforce annotation presence as security measure")
        void testSecurity_AnnotationRequired() {
            // Given
            class UnauthorizedClass {
            }

            // When & Then
            SecurityException exception = assertThrows(SecurityException.class, 
                () -> provider.get(UnauthorizedClass.class));
            
            assertTrue(exception.getMessage().startsWith("Ares Security Error"));
        }

        @Test
        @DisplayName("Should validate test class is not null")
        @SuppressWarnings("null")
        void testSecurity_NonNullValidation() {
            // Given & When & Then
            assertThrows(NullPointerException.class, 
                () -> provider.get(null));
        }
    }

    @Nested
    @DisplayName("Edge Case Tests")
    class EdgeCaseTests {

        @StudentCompiledClassesPath("very/long/path/with/many/segments/that/could/potentially/cause/issues")
        static class LongPathTestClass {
        }

        @StudentCompiledClassesPath("/absolute/unix/path")
        static class UnixAbsolutePathTestClass {
        }

        @Test
        @DisplayName("Should handle long paths")
        void testGetLocation_LongPath() {
            // Given & When
            Set<Location> result = provider.get(LongPathTestClass.class);

            // Then
            assertNotNull(result);
            assertEquals(1, result.size());
        }

        @Test
        @DisplayName("Should handle absolute Unix paths")
        void testGetLocation_AbsoluteUnixPath() {
            // Given & When
            Set<Location> result = provider.get(UnixAbsolutePathTestClass.class);

            // Then
            assertNotNull(result);
            assertEquals(1, result.size());
            Location location = result.iterator().next();
            assertTrue(location.asURI().toString().contains("absolute/unix/path"));
        }
    }
}
