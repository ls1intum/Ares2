package de.tum.cit.ase.ares.api.policy.reader.yaml;

import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import de.tum.cit.ase.ares.api.policy.SecurityPolicy;
import de.tum.cit.ase.ares.api.policy.policySubComponents.ProgrammingLanguageConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("SecurityPolicyYAMLReader Tests")
public class SecurityPolicyYAMLReaderTest {

    private SecurityPolicyYAMLReader reader;
    private YAMLMapper yamlMapper;

    @BeforeEach
    void setUp() {
        yamlMapper = new YAMLMapper();
        reader = new SecurityPolicyYAMLReader(yamlMapper);
    }

    @Nested
    @DisplayName("Constructor Tests")
    class ConstructorTests {

        @Test
        @DisplayName("Should create instance with valid YAMLMapper")
        void shouldCreateInstanceWithValidYAMLMapper() {
            // Act
            SecurityPolicyYAMLReader testReader = new SecurityPolicyYAMLReader(yamlMapper);

            // Assert
            assertNotNull(testReader);
        }

        @Test
        @DisplayName("Should throw NullPointerException when YAMLMapper is null")
        void shouldThrowNullPointerExceptionWhenYAMLMapperIsNull() {
            // Act & Assert
            assertThrows(NullPointerException.class, () -> 
                    new SecurityPolicyYAMLReader(null));
        }
    }

    @Nested
    @DisplayName("Builder Tests")
    class BuilderTests {

        @Test
        @DisplayName("Should create builder instance")
        void shouldCreateBuilderInstance() {
            // Act
            SecurityPolicyYAMLReader.Builder builder = SecurityPolicyYAMLReader.yamlBuilder();

            // Assert
            assertNotNull(builder);
        }

        @Test
        @DisplayName("Should build instance with valid YAMLMapper")
        void shouldBuildInstanceWithValidYAMLMapper() {
            // Act
            SecurityPolicyYAMLReader testReader = SecurityPolicyYAMLReader.yamlBuilder()
                    .yamlMapper(yamlMapper)
                    .build();

            // Assert
            assertNotNull(testReader);
        }

        @Test
        @DisplayName("Should throw NullPointerException when building without YAMLMapper")
        void shouldThrowNullPointerExceptionWhenBuildingWithoutYAMLMapper() {
            // Act & Assert
            assertThrows(NullPointerException.class, () -> 
                    SecurityPolicyYAMLReader.yamlBuilder().build());
        }

        @Test
        @DisplayName("Should throw NullPointerException when setting null YAMLMapper")
        void shouldThrowNullPointerExceptionWhenSettingNullYAMLMapper() {
            // Act & Assert
            assertThrows(NullPointerException.class, () -> 
                    SecurityPolicyYAMLReader.yamlBuilder().yamlMapper(null));
        }

        @Test
        @DisplayName("Should allow method chaining")
        void shouldAllowMethodChaining() {
            // Act
            SecurityPolicyYAMLReader testReader = SecurityPolicyYAMLReader.yamlBuilder()
                    .yamlMapper(yamlMapper)
                    .build();

            // Assert
            assertNotNull(testReader);
        }
    }

    @Nested
    @DisplayName("readSecurityPolicyFrom() Tests")
    class ReadSecurityPolicyFromTests {

        @Test
        @DisplayName("Should throw NullPointerException when path is null")
        void shouldThrowNullPointerExceptionWhenPathIsNull() {
            // Act & Assert
            assertThrows(NullPointerException.class, () -> 
                    reader.readSecurityPolicyFrom(null));
        }

        @Test
        @DisplayName("Should throw SecurityException when file does not exist")
        void shouldThrowSecurityExceptionWhenFileDoesNotExist() {
            // Arrange
            Path nonExistentPath = Paths.get("non/existent/policy.yaml");

            // Act & Assert
            assertThrows(IllegalArgumentException.class, () -> 
                    reader.readSecurityPolicyFrom(nonExistentPath));
        }

        @Test
        @DisplayName("Should read valid security policy from YAML file")
        void shouldReadValidSecurityPolicyFromYAMLFile(@TempDir Path tempDir) throws IOException {
            // Arrange
            Path policyFile = tempDir.resolve("security-policy.yaml");
            String yamlContent = """
                    regardingTheSupervisedCode:
                      theFollowingProgrammingLanguageConfigurationIsUsed: JAVA_USING_MAVEN_ARCHUNIT_AND_ASPECTJ
                      theSupervisedCodeUsesTheFollowingPackage: "com.example"
                      theMainClassInsideThisPackageIs: "Main"
                      theFollowingClassesAreTestClasses: ["TestClass1", "TestClass2"]
                      theFollowingResourceAccessesArePermitted:
                        regardingFileSystemInteractions: []
                        regardingNetworkConnections: []
                        regardingCommandExecutions: []
                        regardingThreadCreations: []
                        regardingPackageImports: []
                    """;
            Files.writeString(policyFile, yamlContent);

            // Act
            SecurityPolicy policy = reader.readSecurityPolicyFrom(policyFile);

            // Assert
            assertNotNull(policy);
            assertNotNull(policy.regardingTheSupervisedCode());
            assertEquals(ProgrammingLanguageConfiguration.JAVA_USING_MAVEN_ARCHUNIT_AND_ASPECTJ,
                    policy.regardingTheSupervisedCode().theFollowingProgrammingLanguageConfigurationIsUsed());
            assertEquals("com.example", 
                    policy.regardingTheSupervisedCode().theSupervisedCodeUsesTheFollowingPackage());
            assertEquals("Main", 
                    policy.regardingTheSupervisedCode().theMainClassInsideThisPackageIs());
            assertArrayEquals(new String[]{"TestClass1", "TestClass2"}, 
                    policy.regardingTheSupervisedCode().theFollowingClassesAreTestClasses());
        }

        @Test
        @DisplayName("Should read minimal security policy from YAML file")
        void shouldReadMinimalSecurityPolicyFromYAMLFile(@TempDir Path tempDir) throws IOException {
            // Arrange
            Path policyFile = tempDir.resolve("minimal-policy.yaml");
            String yamlContent = """
                    regardingTheSupervisedCode:
                      theFollowingProgrammingLanguageConfigurationIsUsed: JAVA_USING_MAVEN_ARCHUNIT_AND_ASPECTJ
                      theFollowingClassesAreTestClasses: []
                      theFollowingResourceAccessesArePermitted:
                        regardingFileSystemInteractions: []
                        regardingNetworkConnections: []
                        regardingCommandExecutions: []
                        regardingThreadCreations: []
                        regardingPackageImports: []
                    """;
            Files.writeString(policyFile, yamlContent);

            // Act
            SecurityPolicy policy = reader.readSecurityPolicyFrom(policyFile);

            // Assert
            assertNotNull(policy);
            assertNotNull(policy.regardingTheSupervisedCode());
            assertEquals(ProgrammingLanguageConfiguration.JAVA_USING_MAVEN_ARCHUNIT_AND_ASPECTJ,
                    policy.regardingTheSupervisedCode().theFollowingProgrammingLanguageConfigurationIsUsed());
        }

        @Test
        @DisplayName("Should throw SecurityException when YAML is malformed")
        void shouldThrowSecurityExceptionWhenYAMLIsMalformed(@TempDir Path tempDir) throws IOException {
            // Arrange
            Path malformedFile = tempDir.resolve("malformed.yaml");
            String malformedYaml = """
                    regardingTheSupervisedCode:
                      theFollowingProgrammingLanguageConfigurationIsUsed: JAVA_USING_MAVEN_ARCHUNIT_AND_ASPECTJ
                      # This is malformed YAML - missing value
                      theFollowingResourceAccessesArePermitted:
                    """;
            Files.writeString(malformedFile, malformedYaml);

            // Act & Assert
            assertThrows(SecurityException.class, () -> 
                    reader.readSecurityPolicyFrom(malformedFile));
        }

        @Test
        @DisplayName("Should throw SecurityException when YAML has invalid structure")
        void shouldThrowSecurityExceptionWhenYAMLHasInvalidStructure(@TempDir Path tempDir) throws IOException {
            // Arrange
            Path invalidFile = tempDir.resolve("invalid-structure.yaml");
            String invalidYaml = """
                    # This YAML structure doesn't match SecurityPolicy
                    wrongField: "value"
                    anotherWrongField: 123
                    """;
            Files.writeString(invalidFile, invalidYaml);

            // Act & Assert
            assertThrows(SecurityException.class, () -> 
                    reader.readSecurityPolicyFrom(invalidFile));
        }

        @Test
        @DisplayName("Should throw SecurityException when file is not readable")
        void shouldThrowSecurityExceptionWhenFileIsNotReadable(@TempDir Path tempDir) throws IOException {
            // Arrange
            Path unreadableFile = tempDir.resolve("unreadable.yaml");
            Files.writeString(unreadableFile, "test content");
            unreadableFile.toFile().setReadable(false);

            // Act & Assert
            assertThrows(IllegalArgumentException.class, () -> 
                    reader.readSecurityPolicyFrom(unreadableFile));

            // Clean up
            unreadableFile.toFile().setReadable(true);
        }

        @Test
        @DisplayName("Should handle different programming language configurations")
        void shouldHandleDifferentProgrammingLanguageConfigurations(@TempDir Path tempDir) throws IOException {
            for (ProgrammingLanguageConfiguration config : ProgrammingLanguageConfiguration.values()) {
                // Arrange
                Path policyFile = tempDir.resolve("policy-" + config.name() + ".yaml");
                String yamlContent = String.format("""
                        regardingTheSupervisedCode:
                          theFollowingProgrammingLanguageConfigurationIsUsed: %s
                          theFollowingClassesAreTestClasses: []
                          theFollowingResourceAccessesArePermitted:
                            regardingFileSystemInteractions: []
                            regardingNetworkConnections: []
                            regardingCommandExecutions: []
                            regardingThreadCreations: []
                            regardingPackageImports: []
                        """, config.name());
                Files.writeString(policyFile, yamlContent);

                // Act
                SecurityPolicy policy = reader.readSecurityPolicyFrom(policyFile);

                // Assert
                assertNotNull(policy);
                assertEquals(config, 
                        policy.regardingTheSupervisedCode().theFollowingProgrammingLanguageConfigurationIsUsed());
            }
        }
    }

    @Nested
    @DisplayName("Integration Tests")
    class IntegrationTests {

        @Test
        @DisplayName("Should work with SecurityPolicyReader factory method")
        void shouldWorkWithSecurityPolicyReaderFactoryMethod(@TempDir Path tempDir) throws IOException {
            // Arrange
            Path policyFile = tempDir.resolve("integration-test.yaml");
            String yamlContent = """
                    regardingTheSupervisedCode:
                      theFollowingProgrammingLanguageConfigurationIsUsed: JAVA_USING_MAVEN_ARCHUNIT_AND_ASPECTJ
                      theFollowingClassesAreTestClasses: []
                      theFollowingResourceAccessesArePermitted:
                        regardingFileSystemInteractions: []
                        regardingNetworkConnections: []
                        regardingCommandExecutions: []
                        regardingThreadCreations: []
                        regardingPackageImports: []
                    """;
            Files.writeString(policyFile, yamlContent);

            // Act
            SecurityPolicy policy = reader.readSecurityPolicyFrom(policyFile);

            // Assert
            assertNotNull(policy);
            assertNotNull(policy.regardingTheSupervisedCode());
        }
    }

    @Nested
    @DisplayName("Error Handling Tests")
    class ErrorHandlingTests {

        @Test
        @DisplayName("Should provide meaningful error message for missing required fields")
        void shouldProvideMeaningfulErrorMessageForMissingRequiredFields(@TempDir Path tempDir) throws IOException {
            // Arrange
            Path incompleteFile = tempDir.resolve("incomplete.yaml");
            String incompleteYaml = """
                    regardingTheSupervisedCode:
                      # Missing required fields
                      theSupervisedCodeUsesTheFollowingPackage: "com.example"
                    """;
            Files.writeString(incompleteFile, incompleteYaml);

            // Act & Assert
            SecurityException exception = assertThrows(SecurityException.class, () -> 
                    reader.readSecurityPolicyFrom(incompleteFile));
            assertNotNull(exception.getMessage());
        }

        @Test
        @DisplayName("Should handle empty file gracefully")
        void shouldHandleEmptyFileGracefully(@TempDir Path tempDir) throws IOException {
            // Arrange
            Path emptyFile = tempDir.resolve("empty.yaml");
            Files.writeString(emptyFile, "");

            // Act & Assert
            assertThrows(SecurityException.class, () -> 
                    reader.readSecurityPolicyFrom(emptyFile));
        }

        @Test
        @DisplayName("Should handle file with only comments")
        void shouldHandleFileWithOnlyComments(@TempDir Path tempDir) throws IOException {
            // Arrange
            Path commentsOnlyFile = tempDir.resolve("comments-only.yaml");
            String commentsOnlyYaml = """
                    # This file contains only comments
                    # No actual content
                    # Should be treated as empty
                    """;
            Files.writeString(commentsOnlyFile, commentsOnlyYaml);

            // Act & Assert
            assertThrows(SecurityException.class, () -> 
                    reader.readSecurityPolicyFrom(commentsOnlyFile));
        }
    }
}
