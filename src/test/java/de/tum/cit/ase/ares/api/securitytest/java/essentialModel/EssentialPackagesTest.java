package de.tum.cit.ase.ares.api.securitytest.java.essentialModel;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("EssentialPackages Tests")
public class EssentialPackagesTest {

    private List<String> javaPackages;
    private List<String> archUnitPackages;
    private List<String> walaPackages;
    private List<String> aspectJPackages;
    private List<String> instrumentationPackages;
    private List<String> aresPackages;
    private List<String> junitPackages;

    @BeforeEach
    void setUp() {
        javaPackages = List.of("java.lang", "java.util");
        archUnitPackages = List.of("com.tngtech.archunit");
        walaPackages = List.of("com.ibm.wala");
        aspectJPackages = List.of("org.aspectj");
        instrumentationPackages = List.of("java.lang.instrument");
        aresPackages = List.of("de.tum.cit.ase.ares");
        junitPackages = List.of("org.junit");
    }

    @Nested
    @DisplayName("Constructor Tests")
    class ConstructorTests {

        @Test
        @DisplayName("Should create EssentialPackages with valid parameters")
        void shouldCreateEssentialPackagesWithValidParameters() {
            // Act
            EssentialPackages essentialPackages = new EssentialPackages(
                    javaPackages, archUnitPackages, walaPackages, aspectJPackages,
                    instrumentationPackages, aresPackages, junitPackages
            );

            // Assert
            assertNotNull(essentialPackages);
            assertEquals(javaPackages, essentialPackages.essentialJavaPackages());
            assertEquals(archUnitPackages, essentialPackages.essentialArchUnitPackages());
            assertEquals(walaPackages, essentialPackages.essentialWalaPackages());
            assertEquals(aspectJPackages, essentialPackages.essentialAspectJPackages());
            assertEquals(instrumentationPackages, essentialPackages.essentialInstrumentationPackages());
            assertEquals(aresPackages, essentialPackages.essentialAresPackages());
            assertEquals(junitPackages, essentialPackages.essentialJUnitPackages());
        }

        @Test
        @DisplayName("Should throw NullPointerException when essentialJavaPackages is null")
        void shouldThrowNullPointerExceptionWhenEssentialJavaPackagesIsNull() {
            // Act & Assert
            assertThrows(NullPointerException.class, () -> new EssentialPackages(
                    null, archUnitPackages, walaPackages, aspectJPackages,
                    instrumentationPackages, aresPackages, junitPackages
            ));
        }

        @Test
        @DisplayName("Should throw NullPointerException when essentialArchUnitPackages is null")
        void shouldThrowNullPointerExceptionWhenEssentialArchUnitPackagesIsNull() {
            // Act & Assert
            assertThrows(NullPointerException.class, () -> new EssentialPackages(
                    javaPackages, null, walaPackages, aspectJPackages,
                    instrumentationPackages, aresPackages, junitPackages
            ));
        }

        @Test
        @DisplayName("Should throw NullPointerException when essentialWalaPackages is null")
        void shouldThrowNullPointerExceptionWhenEssentialWalaPackagesIsNull() {
            // Act & Assert
            assertThrows(NullPointerException.class, () -> new EssentialPackages(
                    javaPackages, archUnitPackages, null, aspectJPackages,
                    instrumentationPackages, aresPackages, junitPackages
            ));
        }

        @Test
        @DisplayName("Should throw NullPointerException when essentialAspectJPackages is null")
        void shouldThrowNullPointerExceptionWhenEssentialAspectJPackagesIsNull() {
            // Act & Assert
            assertThrows(NullPointerException.class, () -> new EssentialPackages(
                    javaPackages, archUnitPackages, walaPackages, null,
                    instrumentationPackages, aresPackages, junitPackages
            ));
        }

        @Test
        @DisplayName("Should throw NullPointerException when essentialInstrumentationPackages is null")
        void shouldThrowNullPointerExceptionWhenEssentialInstrumentationPackagesIsNull() {
            // Act & Assert
            assertThrows(NullPointerException.class, () -> new EssentialPackages(
                    javaPackages, archUnitPackages, walaPackages, aspectJPackages,
                    null, aresPackages, junitPackages
            ));
        }

        @Test
        @DisplayName("Should throw NullPointerException when essentialAresPackages is null")
        void shouldThrowNullPointerExceptionWhenEssentialAresPackagesIsNull() {
            // Act & Assert
            assertThrows(NullPointerException.class, () -> new EssentialPackages(
                    javaPackages, archUnitPackages, walaPackages, aspectJPackages,
                    instrumentationPackages, null, junitPackages
            ));
        }

        @Test
        @DisplayName("Should throw NullPointerException when essentialJUnitPackages is null")
        void shouldThrowNullPointerExceptionWhenEssentialJUnitPackagesIsNull() {
            // Act & Assert
            assertThrows(NullPointerException.class, () -> new EssentialPackages(
                    javaPackages, archUnitPackages, walaPackages, aspectJPackages,
                    instrumentationPackages, aresPackages, null
            ));
        }

        @Test
        @DisplayName("Should create EssentialPackages with empty lists")
        void shouldCreateEssentialPackagesWithEmptyLists() {
            // Arrange
            List<String> emptyList = List.of();

            // Act
            EssentialPackages essentialPackages = new EssentialPackages(
                    emptyList, emptyList, emptyList, emptyList,
                    emptyList, emptyList, emptyList
            );

            // Assert
            assertNotNull(essentialPackages);
            assertTrue(essentialPackages.essentialJavaPackages().isEmpty());
            assertTrue(essentialPackages.essentialArchUnitPackages().isEmpty());
            assertTrue(essentialPackages.essentialWalaPackages().isEmpty());
            assertTrue(essentialPackages.essentialAspectJPackages().isEmpty());
            assertTrue(essentialPackages.essentialInstrumentationPackages().isEmpty());
            assertTrue(essentialPackages.essentialAresPackages().isEmpty());
            assertTrue(essentialPackages.essentialJUnitPackages().isEmpty());
        }
    }

    @Nested
    @DisplayName("getEssentialPackages() Tests")
    class GetEssentialPackagesTests {

        @Test
        @DisplayName("Should return aggregated list of all essential packages")
        void shouldReturnAggregatedListOfAllEssentialPackages() {
            // Arrange
            EssentialPackages essentialPackages = new EssentialPackages(
                    javaPackages, archUnitPackages, walaPackages, aspectJPackages,
                    instrumentationPackages, aresPackages, junitPackages
            );

            // Act
            List<String> result = essentialPackages.getEssentialPackages();

            // Assert
            assertNotNull(result);
            assertEquals(8, result.size()); // 2 + 1 + 1 + 1 + 1 + 1 + 1 = 8 packages total
            assertTrue(result.containsAll(javaPackages));
            assertTrue(result.containsAll(archUnitPackages));
            assertTrue(result.containsAll(walaPackages));
            assertTrue(result.containsAll(aspectJPackages));
            assertTrue(result.containsAll(instrumentationPackages));
            assertTrue(result.containsAll(aresPackages));
            assertTrue(result.containsAll(junitPackages));
        }

        @Test
        @DisplayName("Should return empty list when all package lists are empty")
        void shouldReturnEmptyListWhenAllPackageListsAreEmpty() {
            // Arrange
            List<String> emptyList = List.of();
            EssentialPackages essentialPackages = new EssentialPackages(
                    emptyList, emptyList, emptyList, emptyList,
                    emptyList, emptyList, emptyList
            );

            // Act
            List<String> result = essentialPackages.getEssentialPackages();

            // Assert
            assertNotNull(result);
            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("Should maintain order of packages in aggregated list")
        void shouldMaintainOrderOfPackagesInAggregatedList() {
            // Arrange
            List<String> java = List.of("java1", "java2");
            List<String> archUnit = List.of("archunit1");
            List<String> wala = List.of("wala1");
            List<String> aspectJ = List.of("aspectj1");
            List<String> instrumentation = List.of("instr1");
            List<String> ares = List.of("ares1");
            List<String> junit = List.of("junit1");

            EssentialPackages essentialPackages = new EssentialPackages(
                    java, archUnit, wala, aspectJ, instrumentation, ares, junit
            );

            // Act
            List<String> result = essentialPackages.getEssentialPackages();

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
            EssentialPackages.Builder builder = EssentialPackages.builder();

            // Assert
            assertNotNull(builder);
        }

        @Test
        @DisplayName("Should build EssentialPackages with all required fields")
        void shouldBuildEssentialPackagesWithAllRequiredFields() {
            // Act
            EssentialPackages essentialPackages = EssentialPackages.builder()
                    .essentialJavaPackages(javaPackages)
                    .essentialArchUnitPackages(archUnitPackages)
                    .essentialWalaPackages(walaPackages)
                    .essentialAspectJPackages(aspectJPackages)
                    .essentialInstrumentationPackages(instrumentationPackages)
                    .essentialAresPackages(aresPackages)
                    .essentialJUnitPackages(junitPackages)
                    .build();

            // Assert
            assertNotNull(essentialPackages);
            assertEquals(javaPackages, essentialPackages.essentialJavaPackages());
            assertEquals(archUnitPackages, essentialPackages.essentialArchUnitPackages());
            assertEquals(walaPackages, essentialPackages.essentialWalaPackages());
            assertEquals(aspectJPackages, essentialPackages.essentialAspectJPackages());
            assertEquals(instrumentationPackages, essentialPackages.essentialInstrumentationPackages());
            assertEquals(aresPackages, essentialPackages.essentialAresPackages());
            assertEquals(junitPackages, essentialPackages.essentialJUnitPackages());
        }

        @Test
        @DisplayName("Should throw NullPointerException when building with null essentialJavaPackages")
        void shouldThrowNullPointerExceptionWhenBuildingWithNullEssentialJavaPackages() {
            // Act & Assert
            assertThrows(NullPointerException.class, () -> EssentialPackages.builder()
                    .essentialJavaPackages(null)
                    .essentialArchUnitPackages(archUnitPackages)
                    .essentialWalaPackages(walaPackages)
                    .essentialAspectJPackages(aspectJPackages)
                    .essentialInstrumentationPackages(instrumentationPackages)
                    .essentialAresPackages(aresPackages)
                    .essentialJUnitPackages(junitPackages)
                    .build());
        }

        @Test
        @DisplayName("Should throw NullPointerException when building with missing essentialJavaPackages")
        void shouldThrowNullPointerExceptionWhenBuildingWithMissingEssentialJavaPackages() {
            // Act & Assert
            assertThrows(NullPointerException.class, () -> EssentialPackages.builder()
                    .essentialArchUnitPackages(archUnitPackages)
                    .essentialWalaPackages(walaPackages)
                    .essentialAspectJPackages(aspectJPackages)
                    .essentialInstrumentationPackages(instrumentationPackages)
                    .essentialAresPackages(aresPackages)
                    .essentialJUnitPackages(junitPackages)
                    .build());
        }

        @Test
        @DisplayName("Should return same builder instance for method chaining")
        void shouldReturnSameBuilderInstanceForMethodChaining() {
            // Arrange
            EssentialPackages.Builder builder = EssentialPackages.builder();

            // Act & Assert
            assertSame(builder, builder.essentialJavaPackages(javaPackages));
            assertSame(builder, builder.essentialArchUnitPackages(archUnitPackages));
            assertSame(builder, builder.essentialWalaPackages(walaPackages));
            assertSame(builder, builder.essentialAspectJPackages(aspectJPackages));
            assertSame(builder, builder.essentialInstrumentationPackages(instrumentationPackages));
            assertSame(builder, builder.essentialAresPackages(aresPackages));
            assertSame(builder, builder.essentialJUnitPackages(junitPackages));
        }

        @Test
        @DisplayName("Should throw NullPointerException for each null parameter in builder methods")
        void shouldThrowNullPointerExceptionForEachNullParameterInBuilderMethods() {
            EssentialPackages.Builder builder = EssentialPackages.builder();

            assertThrows(NullPointerException.class, () -> builder.essentialJavaPackages(null));
            assertThrows(NullPointerException.class, () -> builder.essentialArchUnitPackages(null));
            assertThrows(NullPointerException.class, () -> builder.essentialWalaPackages(null));
            assertThrows(NullPointerException.class, () -> builder.essentialAspectJPackages(null));
            assertThrows(NullPointerException.class, () -> builder.essentialInstrumentationPackages(null));
            assertThrows(NullPointerException.class, () -> builder.essentialAresPackages(null));
            assertThrows(NullPointerException.class, () -> builder.essentialJUnitPackages(null));
        }
    }
}
