package de.tum.cit.ase.ares.api.securitytest.java.essentialModel.yaml;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import de.tum.cit.ase.ares.api.securitytest.java.essentialModel.EssentialClasses;
import de.tum.cit.ase.ares.api.securitytest.java.essentialModel.EssentialPackages;

@DisplayName("EssentialDataYAMLReader Tests")
public class EssentialDataYAMLReaderTest {

	private EssentialDataYAMLReader reader;

	@BeforeEach
	void setUp() {
		reader = new EssentialDataYAMLReader();
	}

	@Nested
	@DisplayName("readEssentialPackagesFrom() Tests")
	class ReadEssentialPackagesFromTests {

		@Test
		@DisplayName("Should read essential packages from valid YAML file")
		void shouldReadEssentialPackagesFromValidYAMLFile() {
			// Arrange
			Path packagesPath = Paths.get(
					"src/main/resources/de/tum/cit/ase/ares/api/configuration/essentialFiles/java/EssentialPackages.yaml");

			// Act
			EssentialPackages result = reader.readEssentialPackagesFrom(packagesPath);

			// Assert
			assertNotNull(result);
			assertNotNull(result.essentialJavaPackages());
			assertNotNull(result.essentialArchunitPackages());
			assertNotNull(result.essentialWalaPackages());
			assertNotNull(result.essentialAspectJPackages());
			assertNotNull(result.essentialInstrumentationPackages());
			assertNotNull(result.essentialAresPackages());
			assertNotNull(result.essentialJUnitPackages());

			// Check that the JDK baseline is java.lang and java.io, not the whole of java
			assertEquals(List.of("java.lang", "java.io"), result.essentialJavaPackages());

			// Check that AspectJ packages contain expected packages
			assertTrue(result.essentialAspectJPackages().contains("org.aspectj"));

			// Check that instrumentation packages contain expected package
			assertTrue(result.essentialInstrumentationPackages()
					.contains("de.tum.cit.ase.ares.api.aop.java.aspectj.adviceandpointcut"));
		}

		@Test
		@DisplayName("Should throw NullPointerException when path is null")
		void shouldThrowNullPointerExceptionWhenPathIsNull() {
			// Act & Assert using reflection to bypass @Nonnull compile-time analysis
			InvocationTargetException ex = assertThrows(InvocationTargetException.class,
					() -> EssentialDataYAMLReader.class.getMethod("readEssentialPackagesFrom", Path.class)
							.invoke(reader, new Object[] { null }));
			assertTrue(ex.getCause() instanceof NullPointerException);
		}

		@Test
		@DisplayName("Should throw SecurityException when file does not exist")
		void shouldThrowSecurityExceptionWhenFileDoesNotExist() {
			// Arrange
			Path nonExistentPath = Paths.get("non/existent/file.yaml");

			// Act & Assert
			assertThrows(SecurityException.class, () -> reader.readEssentialPackagesFrom(nonExistentPath));
		}

		@Test
		@DisplayName("Should throw SecurityException when YAML is malformed")
		void shouldThrowSecurityExceptionWhenYAMLIsMalformed(@TempDir Path tempDir) throws IOException {
			// Arrange
			Path malformedFile = tempDir.resolve("malformed.yaml");
			Files.writeString(malformedFile, """
					essentialJavaPackages:
					  - java
					essentialArchunitPackages: [
					# This is malformed YAML - missing closing bracket
					""");

			// Act & Assert
			assertThrows(SecurityException.class, () -> reader.readEssentialPackagesFrom(malformedFile));
		}

		@Test
		@DisplayName("Should throw SecurityException when YAML has wrong structure")
		void shouldThrowSecurityExceptionWhenYAMLHasWrongStructure(@TempDir Path tempDir) throws IOException {
			// Arrange
			Path wrongStructureFile = tempDir.resolve("wrong_structure.yaml");
			Files.writeString(wrongStructureFile, """
					wrongField: value
					anotherWrongField:
					  - item1
					  - item2
					""");

			// Act & Assert
			assertThrows(SecurityException.class, () -> reader.readEssentialPackagesFrom(wrongStructureFile));
		}
	}

	@Nested
	@DisplayName("readEssentialClassesFrom() Tests")
	class ReadEssentialClassesFromTests {

		@Test
		@DisplayName("Should read essential classes from valid YAML file")
		void shouldReadEssentialClassesFromValidYAMLFile() {
			// Arrange
			Path classesPath = Paths.get(
					"src/main/resources/de/tum/cit/ase/ares/api/configuration/essentialFiles/java/EssentialClasses.yaml");

			// Act
			EssentialClasses result = reader.readEssentialClassesFrom(classesPath);

			// Assert
			assertNotNull(result);
			assertNotNull(result.essentialJavaClasses());
			assertNotNull(result.essentialArchunitClasses());
			assertNotNull(result.essentialWalaClasses());
			assertNotNull(result.essentialAspectJClasses());
			assertNotNull(result.essentialInstrumentationClasses());
			assertNotNull(result.essentialAresClasses());
			assertNotNull(result.essentialJUnitClasses());

			// Check specific classes
			assertTrue(
					result.essentialArchunitClasses().contains("de.tum.cit.ase.ares.api.architecture.java.archunit"));
			assertTrue(result.essentialWalaClasses().contains("de.tum.cit.ase.ares.api.architecture.java.wala"));
			assertTrue(result.essentialAspectJClasses().contains("de.tum.cit.ase.ares.api.aop.java.aspectj"));
			assertTrue(result.essentialInstrumentationClasses()
					.contains("de.tum.cit.ase.ares.api.aop.java.instrumentation"));
			assertTrue(result.essentialJUnitClasses().contains("de.tum.cit.ase.ares.api.jupiter"));

			// Check some Ares classes
			assertTrue(result.essentialAresClasses().contains("de.tum.cit.ase.ares.api.internal"));
			assertTrue(result.essentialAresClasses().contains("de.tum.cit.ase.ares.api.util.FileTools"));
		}

		@Test
		@DisplayName("Should throw NullPointerException when path is null")
		void shouldThrowNullPointerExceptionWhenPathIsNull() {
			// Act & Assert using reflection to bypass @Nonnull compile-time analysis
			InvocationTargetException ex = assertThrows(InvocationTargetException.class,
					() -> EssentialDataYAMLReader.class.getMethod("readEssentialClassesFrom", Path.class).invoke(reader,
							new Object[] { null }));
			assertTrue(ex.getCause() instanceof NullPointerException);
		}

		@Test
		@DisplayName("Should throw SecurityException when file does not exist")
		void shouldThrowSecurityExceptionWhenFileDoesNotExist() {
			// Arrange
			Path nonExistentPath = Paths.get("non/existent/classes.yaml");

			// Act & Assert
			assertThrows(SecurityException.class, () -> reader.readEssentialClassesFrom(nonExistentPath));
		}

		@Test
		@DisplayName("Should throw SecurityException when YAML is malformed")
		void shouldThrowSecurityExceptionWhenYAMLIsMalformed(@TempDir Path tempDir) throws IOException {
			// Arrange
			Path malformedFile = tempDir.resolve("malformed_classes.yaml");
			Files.writeString(malformedFile, """
					essentialJavaClasses: []
					essentialArchunitClasses: [
					# This is malformed YAML - missing closing bracket
					""");

			// Act & Assert
			assertThrows(SecurityException.class, () -> reader.readEssentialClassesFrom(malformedFile));
		}

		@Test
		@DisplayName("Should throw SecurityException when YAML has wrong structure")
		void shouldThrowSecurityExceptionWhenYAMLHasWrongStructure(@TempDir Path tempDir) throws IOException {
			// Arrange
			Path wrongStructureFile = tempDir.resolve("wrong_structure_classes.yaml");
			Files.writeString(wrongStructureFile, """
					wrongField: value
					anotherWrongField:
					  - item1
					  - item2
					""");

			// Act & Assert
			assertThrows(SecurityException.class, () -> reader.readEssentialClassesFrom(wrongStructureFile));
		}
	}

	@Nested
	@DisplayName("Integration Tests")
	class IntegrationTests {

		@Test
		@DisplayName("Should read and process both packages and classes successfully")
		void shouldReadAndProcessBothPackagesAndClassesSuccessfully() {
			// Arrange
			Path packagesPath = Paths.get(
					"src/main/resources/de/tum/cit/ase/ares/api/configuration/essentialFiles/java/EssentialPackages.yaml");
			Path classesPath = Paths.get(
					"src/main/resources/de/tum/cit/ase/ares/api/configuration/essentialFiles/java/EssentialClasses.yaml");

			// Act
			EssentialPackages packages = reader.readEssentialPackagesFrom(packagesPath);
			EssentialClasses classes = reader.readEssentialClassesFrom(classesPath);

			// Assert
			assertNotNull(packages);
			assertNotNull(classes);

			// Verify that both provide aggregated lists
			assertNotNull(packages.getEssentialPackages());
			assertNotNull(classes.getEssentialClasses());

			assertFalse(packages.getEssentialPackages().isEmpty());
			assertFalse(classes.getEssentialClasses().isEmpty());
		}

		@Test
		@DisplayName("Should handle empty YAML files gracefully")
		void shouldHandleEmptyYAMLFilesGracefully(@TempDir Path tempDir) throws IOException {
			// Arrange
			Path emptyPackagesFile = tempDir.resolve("empty_packages.yaml");
			Files.writeString(emptyPackagesFile, """
					essentialJavaPackages: []
					essentialArchunitPackages: []
					essentialWalaPackages: []
					essentialAspectJPackages: []
					essentialInstrumentationPackages: []
					essentialAresPackages: []
					essentialJUnitPackages: []
					""");

			Path emptyClassesFile = tempDir.resolve("empty_classes.yaml");
			Files.writeString(emptyClassesFile, """
					essentialJavaClasses: []
					essentialArchunitClasses: []
					essentialWalaClasses: []
					essentialAspectJClasses: []
					essentialInstrumentationClasses: []
					essentialAresClasses: []
					essentialJUnitClasses: []
					""");

			// Act
			EssentialPackages packages = reader.readEssentialPackagesFrom(emptyPackagesFile);
			EssentialClasses classes = reader.readEssentialClassesFrom(emptyClassesFile);

			// Assert
			assertNotNull(packages);
			assertNotNull(classes);

			assertTrue(packages.getEssentialPackages().isEmpty());
			assertTrue(classes.getEssentialClasses().isEmpty());
		}
	}

	@Nested
	@DisplayName("Error Handling Tests")
	class ErrorHandlingTests {

		@Test
		@DisplayName("Should handle file with different encoding")
		void shouldHandleFileWithDifferentEncoding(@TempDir Path tempDir) throws IOException {
			// Arrange
			Path utf8File = tempDir.resolve("utf8_packages.yaml");
			Files.writeString(utf8File, """
					essentialJavaPackages:
					  - "java.util"
					  - "java.io"
					essentialArchunitPackages: []
					essentialWalaPackages: []
					essentialAspectJPackages: []
					essentialInstrumentationPackages: []
					essentialAresPackages: []
					essentialJUnitPackages: []
					""");

			// Act
			EssentialPackages result = reader.readEssentialPackagesFrom(utf8File);

			// Assert
			assertNotNull(result);
			assertEquals(2, result.essentialJavaPackages().size());
			assertTrue(result.essentialJavaPackages().contains("java.util"));
			assertTrue(result.essentialJavaPackages().contains("java.io"));
		}

		@Test
		@DisplayName("Should handle large YAML files")
		void shouldHandleLargeYAMLFiles(@TempDir Path tempDir) throws IOException {
			// Arrange
			Path largeFile = tempDir.resolve("large_packages.yaml");
			StringBuilder content = new StringBuilder();
			content.append("essentialJavaPackages:\n");
			for (int i = 0; i < 1000; i++) {
				content.append("  - \"java.package").append(i).append("\"\n");
			}
			content.append("essentialArchunitPackages: []\n");
			content.append("essentialWalaPackages: []\n");
			content.append("essentialAspectJPackages: []\n");
			content.append("essentialInstrumentationPackages: []\n");
			content.append("essentialAresPackages: []\n");
			content.append("essentialJUnitPackages: []\n");

			Files.writeString(largeFile, content.toString());

			// Act
			EssentialPackages result = reader.readEssentialPackagesFrom(largeFile);

			// Assert
			assertNotNull(result);
			assertEquals(1000, result.essentialJavaPackages().size());
			assertTrue(result.essentialJavaPackages().contains("java.package0"));
			assertTrue(result.essentialJavaPackages().contains("java.package999"));
		}
	}

	/**
	 * An essential file rewritten between two reads is read afresh, every caller
	 * gets lists of its own, and dropping the kept lists changes no result.
	 *
	 * @throws Exception if the file cannot be written or the cache not reached
	 */
	@Test
	@DisplayName("Should read a rewritten essential file afresh and hand out copies")
	void shouldReadARewrittenEssentialFileAfresh(@TempDir Path tempDir) throws Exception {
		Path packagesFile = tempDir.resolve("EssentialPackages.yaml");
		Files.writeString(packagesFile, essentialPackages("first.pkg"));
		EssentialPackages first = reader.readEssentialPackagesFrom(packagesFile);
		first.essentialJavaPackages().add("added.by.caller");
		EssentialPackages unchanged = reader.readEssentialPackagesFrom(packagesFile);
		Files.writeString(packagesFile, essentialPackages("second.pkg"));
		EssentialPackages rewritten = reader.readEssentialPackagesFrom(packagesFile);

		assertEquals(List.of("first.pkg"), unchanged.essentialJavaPackages());
		assertEquals(List.of("second.pkg"), rewritten.essentialJavaPackages());

		java.lang.reflect.Field cache = EssentialDataYAMLReader.class.getDeclaredField("READ_ESSENTIALS");
		cache.setAccessible(true);
		((java.util.Map<?, ?>) cache.get(null)).clear();
		assertEquals(rewritten, reader.readEssentialPackagesFrom(packagesFile));
	}

	/**
	 * Builds an essential packages file whose Java list holds one package.
	 *
	 * @param javaPackage the one Java package
	 * @return the file's text
	 */
	private static String essentialPackages(String javaPackage) {
		return """
				essentialJavaPackages: ["%s"]
				essentialArchunitPackages: []
				essentialWalaPackages: []
				essentialAspectJPackages: []
				essentialInstrumentationPackages: []
				essentialAresPackages: []
				essentialJUnitPackages: []
				""".formatted(javaPackage);
	}
}
