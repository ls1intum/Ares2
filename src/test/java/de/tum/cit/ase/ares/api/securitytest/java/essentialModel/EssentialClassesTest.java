package de.tum.cit.ase.ares.api.securitytest.java.essentialModel;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("EssentialClasses Tests")
public class EssentialClassesTest {

    private List<String> javaClasses;
    private List<String> archUnitClasses;
    private List<String> walaClasses;
    private List<String> aspectJClasses;
    private List<String> instrumentationClasses;
    private List<String> aresClasses;
    private List<String> junitClasses;

    @BeforeEach
    void setUp() {
        javaClasses = List.of("java.lang.String", "java.util.List");
        archUnitClasses = List.of("com.tngtech.archunit.core.domain.JavaClass");
        walaClasses = List.of("com.ibm.wala.ipa.callgraph.CallGraph");
        aspectJClasses = List.of("org.aspectj.lang.ProceedingJoinPoint");
        instrumentationClasses = List.of("java.lang.instrument.Instrumentation");
        aresClasses = List.of("de.tum.cit.ase.ares.api.jupiter.Public");
        junitClasses = List.of("org.junit.jupiter.api.Test");
    }

    @Nested
    @DisplayName("Constructor Tests")
    class ConstructorTests {

        @Test
        @DisplayName("Should create EssentialClasses with valid parameters")
        void shouldCreateEssentialClassesWithValidParameters() {
            // Act
            EssentialClasses essentialClasses = new EssentialClasses(
                    javaClasses, archUnitClasses, walaClasses, aspectJClasses,
                    instrumentationClasses, aresClasses, junitClasses
            );

            // Assert
            assertNotNull(essentialClasses);
            assertEquals(javaClasses, essentialClasses.essentialJavaClasses());
            assertEquals(archUnitClasses, essentialClasses.essentialArchUnitClasses());
            assertEquals(walaClasses, essentialClasses.essentialWalaClasses());
            assertEquals(aspectJClasses, essentialClasses.essentialAspectJClasses());
            assertEquals(instrumentationClasses, essentialClasses.essentialInstrumentationClasses());
            assertEquals(aresClasses, essentialClasses.essentialAresClasses());
            assertEquals(junitClasses, essentialClasses.essentialJUnitClasses());
        }

        @Test
        @DisplayName("Should throw NullPointerException when essentialJavaClasses is null")
        void shouldThrowNullPointerExceptionWhenEssentialJavaClassesIsNull() {
            // Act & Assert
            assertThrows(NullPointerException.class, () -> new EssentialClasses(
                    null, archUnitClasses, walaClasses, aspectJClasses,
                    instrumentationClasses, aresClasses, junitClasses
            ));
        }

        @Test
        @DisplayName("Should throw NullPointerException when essentialArchUnitClasses is null")
        void shouldThrowNullPointerExceptionWhenEssentialArchUnitClassesIsNull() {
            // Act & Assert
            assertThrows(NullPointerException.class, () -> new EssentialClasses(
                    javaClasses, null, walaClasses, aspectJClasses,
                    instrumentationClasses, aresClasses, junitClasses
            ));
        }

        @Test
        @DisplayName("Should throw NullPointerException when essentialWalaClasses is null")
        void shouldThrowNullPointerExceptionWhenEssentialWalaClassesIsNull() {
            // Act & Assert
            assertThrows(NullPointerException.class, () -> new EssentialClasses(
                    javaClasses, archUnitClasses, null, aspectJClasses,
                    instrumentationClasses, aresClasses, junitClasses
            ));
        }

        @Test
        @DisplayName("Should throw NullPointerException when essentialAspectJClasses is null")
        void shouldThrowNullPointerExceptionWhenEssentialAspectJClassesIsNull() {
            // Act & Assert
            assertThrows(NullPointerException.class, () -> new EssentialClasses(
                    javaClasses, archUnitClasses, walaClasses, null,
                    instrumentationClasses, aresClasses, junitClasses
            ));
        }

        @Test
        @DisplayName("Should throw NullPointerException when essentialInstrumentationClasses is null")
        void shouldThrowNullPointerExceptionWhenEssentialInstrumentationClassesIsNull() {
            // Act & Assert
            assertThrows(NullPointerException.class, () -> new EssentialClasses(
                    javaClasses, archUnitClasses, walaClasses, aspectJClasses,
                    null, aresClasses, junitClasses
            ));
        }

        @Test
        @DisplayName("Should throw NullPointerException when essentialAresClasses is null")
        void shouldThrowNullPointerExceptionWhenEssentialAresClassesIsNull() {
            // Act & Assert
            assertThrows(NullPointerException.class, () -> new EssentialClasses(
                    javaClasses, archUnitClasses, walaClasses, aspectJClasses,
                    instrumentationClasses, null, junitClasses
            ));
        }

        @Test
        @DisplayName("Should throw NullPointerException when essentialJUnitClasses is null")
        void shouldThrowNullPointerExceptionWhenEssentialJUnitClassesIsNull() {
            // Act & Assert
            assertThrows(NullPointerException.class, () -> new EssentialClasses(
                    javaClasses, archUnitClasses, walaClasses, aspectJClasses,
                    instrumentationClasses, aresClasses, null
            ));
        }

        @Test
        @DisplayName("Should create EssentialClasses with empty lists")
        void shouldCreateEssentialClassesWithEmptyLists() {
            // Arrange
            List<String> emptyList = List.of();

            // Act
            EssentialClasses essentialClasses = new EssentialClasses(
                    emptyList, emptyList, emptyList, emptyList,
                    emptyList, emptyList, emptyList
            );

            // Assert
            assertNotNull(essentialClasses);
            assertTrue(essentialClasses.essentialJavaClasses().isEmpty());
            assertTrue(essentialClasses.essentialArchUnitClasses().isEmpty());
            assertTrue(essentialClasses.essentialWalaClasses().isEmpty());
            assertTrue(essentialClasses.essentialAspectJClasses().isEmpty());
            assertTrue(essentialClasses.essentialInstrumentationClasses().isEmpty());
            assertTrue(essentialClasses.essentialAresClasses().isEmpty());
            assertTrue(essentialClasses.essentialJUnitClasses().isEmpty());
        }
    }

    @Nested
    @DisplayName("getEssentialClasses() Tests")
    class GetEssentialClassesTests {

        @Test
        @DisplayName("Should return aggregated list of all essential classes")
        void shouldReturnAggregatedListOfAllEssentialClasses() {
            // Arrange
            EssentialClasses essentialClasses = new EssentialClasses(
                    javaClasses, archUnitClasses, walaClasses, aspectJClasses,
                    instrumentationClasses, aresClasses, junitClasses
            );

            // Act
            List<String> result = essentialClasses.getEssentialClasses();

            // Assert
            assertNotNull(result);
            assertEquals(8, result.size()); // 2 + 1 + 1 + 1 + 1 + 1 + 1 = 8 classes total
            assertTrue(result.containsAll(javaClasses));
            assertTrue(result.containsAll(archUnitClasses));
            assertTrue(result.containsAll(walaClasses));
            assertTrue(result.containsAll(aspectJClasses));
            assertTrue(result.containsAll(instrumentationClasses));
            assertTrue(result.containsAll(aresClasses));
            assertTrue(result.containsAll(junitClasses));
        }

        @Test
        @DisplayName("Should return empty list when all class lists are empty")
        void shouldReturnEmptyListWhenAllClassListsAreEmpty() {
            // Arrange
            List<String> emptyList = List.of();
            EssentialClasses essentialClasses = new EssentialClasses(
                    emptyList, emptyList, emptyList, emptyList,
                    emptyList, emptyList, emptyList
            );

            // Act
            List<String> result = essentialClasses.getEssentialClasses();

            // Assert
            assertNotNull(result);
            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("Should maintain order of classes in aggregated list")
        void shouldMaintainOrderOfClassesInAggregatedList() {
            // Arrange
            List<String> java = List.of("java1", "java2");
            List<String> archUnit = List.of("archunit1");
            List<String> wala = List.of("wala1");
            List<String> aspectJ = List.of("aspectj1");
            List<String> instrumentation = List.of("instr1");
            List<String> ares = List.of("ares1");
            List<String> junit = List.of("junit1");

            EssentialClasses essentialClasses = new EssentialClasses(
                    java, archUnit, wala, aspectJ, instrumentation, ares, junit
            );

            // Act
            List<String> result = essentialClasses.getEssentialClasses();

            // Assert
            assertNotNull(result);
            assertEquals(8, result.size());
            assertEquals("java1", result.get(0));
            assertEquals("java2", result.get(1));
            assertEquals("archunit1", result.get(2));
            assertEquals("wala1", result.get(3));
            assertEquals("aspectj1", result.get(4));
            assertEquals("instr1", result.get(5));
            assertEquals("ares1", result.get(6));
            assertEquals("junit1", result.get(7));
        }
    }

    @Nested
    @DisplayName("Builder Tests")
    class BuilderTests {

        @Test
        @DisplayName("Should create builder instance")
        void shouldCreateBuilderInstance() {
            // Act
            EssentialClasses.Builder builder = EssentialClasses.builder();

            // Assert
            assertNotNull(builder);
        }

        @Test
        @DisplayName("Should build EssentialClasses with all required fields")
        void shouldBuildEssentialClassesWithAllRequiredFields() {
            // Act
            EssentialClasses essentialClasses = EssentialClasses.builder()
                    .essentialJavaClasses(javaClasses)
                    .essentialArchUnitClasses(archUnitClasses)
                    .essentialWalaClasses(walaClasses)
                    .essentialAspectJClasses(aspectJClasses)
                    .essentialInstrumentationClasses(instrumentationClasses)
                    .essentialAresClasses(aresClasses)
                    .essentialJUnitClasses(junitClasses)
                    .build();

            // Assert
            assertNotNull(essentialClasses);
            assertEquals(javaClasses, essentialClasses.essentialJavaClasses());
            assertEquals(archUnitClasses, essentialClasses.essentialArchUnitClasses());
            assertEquals(walaClasses, essentialClasses.essentialWalaClasses());
            assertEquals(aspectJClasses, essentialClasses.essentialAspectJClasses());
            assertEquals(instrumentationClasses, essentialClasses.essentialInstrumentationClasses());
            assertEquals(aresClasses, essentialClasses.essentialAresClasses());
            assertEquals(junitClasses, essentialClasses.essentialJUnitClasses());
        }

        @Test
        @DisplayName("Should throw NullPointerException when building with null essentialJavaClasses")
        void shouldThrowNullPointerExceptionWhenBuildingWithNullEssentialJavaClasses() {
            // Act & Assert
            assertThrows(NullPointerException.class, () -> EssentialClasses.builder()
                    .essentialJavaClasses(null)
                    .essentialArchUnitClasses(archUnitClasses)
                    .essentialWalaClasses(walaClasses)
                    .essentialAspectJClasses(aspectJClasses)
                    .essentialInstrumentationClasses(instrumentationClasses)
                    .essentialAresClasses(aresClasses)
                    .essentialJUnitClasses(junitClasses)
                    .build());
        }

        @Test
        @DisplayName("Should throw NullPointerException when building with missing essentialJavaClasses")
        void shouldThrowNullPointerExceptionWhenBuildingWithMissingEssentialJavaClasses() {
            // Act & Assert
            assertThrows(NullPointerException.class, () -> EssentialClasses.builder()
                    .essentialArchUnitClasses(archUnitClasses)
                    .essentialWalaClasses(walaClasses)
                    .essentialAspectJClasses(aspectJClasses)
                    .essentialInstrumentationClasses(instrumentationClasses)
                    .essentialAresClasses(aresClasses)
                    .essentialJUnitClasses(junitClasses)
                    .build());
        }

        @Test
        @DisplayName("Should return same builder instance for method chaining")
        void shouldReturnSameBuilderInstanceForMethodChaining() {
            // Arrange
            EssentialClasses.Builder builder = EssentialClasses.builder();

            // Act & Assert
            assertSame(builder, builder.essentialJavaClasses(javaClasses));
            assertSame(builder, builder.essentialArchUnitClasses(archUnitClasses));
            assertSame(builder, builder.essentialWalaClasses(walaClasses));
            assertSame(builder, builder.essentialAspectJClasses(aspectJClasses));
            assertSame(builder, builder.essentialInstrumentationClasses(instrumentationClasses));
            assertSame(builder, builder.essentialAresClasses(aresClasses));
            assertSame(builder, builder.essentialJUnitClasses(junitClasses));
        }

        @Test
        @DisplayName("Should throw NullPointerException for each null parameter in builder methods")
        void shouldThrowNullPointerExceptionForEachNullParameterInBuilderMethods() {
            EssentialClasses.Builder builder = EssentialClasses.builder();

            assertThrows(NullPointerException.class, () -> builder.essentialJavaClasses(null));
            assertThrows(NullPointerException.class, () -> builder.essentialArchUnitClasses(null));
            assertThrows(NullPointerException.class, () -> builder.essentialWalaClasses(null));
            assertThrows(NullPointerException.class, () -> builder.essentialAspectJClasses(null));
            assertThrows(NullPointerException.class, () -> builder.essentialInstrumentationClasses(null));
            assertThrows(NullPointerException.class, () -> builder.essentialAresClasses(null));
            assertThrows(NullPointerException.class, () -> builder.essentialJUnitClasses(null));
        }
    }
}
