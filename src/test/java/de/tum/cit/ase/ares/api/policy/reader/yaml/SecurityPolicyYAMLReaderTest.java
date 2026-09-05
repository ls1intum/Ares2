package de.tum.cit.ase.ares.api.policy.reader.yaml;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermission;
import java.util.EnumSet;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;

import de.tum.cit.ase.ares.api.policy.SecurityPolicy;
import de.tum.cit.ase.ares.api.policy.policySubComponents.ProgrammingLanguageConfiguration;

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
			assertThrows(NullPointerException.class, () -> new SecurityPolicyYAMLReader(null));
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
			SecurityPolicyYAMLReader testReader = SecurityPolicyYAMLReader.yamlBuilder().yamlMapper(yamlMapper).build();

			// Assert
			assertNotNull(testReader);
		}

		@Test
		@DisplayName("Should throw NullPointerException when building without YAMLMapper")
		void shouldThrowNullPointerExceptionWhenBuildingWithoutYAMLMapper() {
			// Act & Assert
			assertThrows(NullPointerException.class, () -> SecurityPolicyYAMLReader.yamlBuilder().build());
		}

		@Test
		@DisplayName("Should throw NullPointerException when setting null YAMLMapper")
		void shouldThrowNullPointerExceptionWhenSettingNullYAMLMapper() {
			// Act & Assert
			assertThrows(NullPointerException.class, () -> SecurityPolicyYAMLReader.yamlBuilder().yamlMapper(null));
		}

		@Test
		@DisplayName("Should allow method chaining")
		void shouldAllowMethodChaining() {
			// Act
			SecurityPolicyYAMLReader testReader = SecurityPolicyYAMLReader.yamlBuilder().yamlMapper(yamlMapper).build();

			// Assert
			assertNotNull(testReader);
		}
	}

	@Nested
	@DisplayName("readSecurityPolicyFrom() Tests")
	class ReadSecurityPolicyFromTests {

		@Test
		void shouldReadEveryCheckedInSecurityPolicy() throws IOException {
			Path examplePolicy = Path.of("src/main/resources/ExampleConfiguration.yaml");
			Path testPolicies = Path.of("src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies");
			try (Stream<Path> files = Stream.concat(Stream.of(examplePolicy), Files.walk(testPolicies))) {
				files.filter(Files::isRegularFile).filter(path -> path.getFileName().toString().endsWith(".yaml"))
						.forEach(path -> assertDoesNotThrow(() -> reader.readSecurityPolicyFrom(path), path::toString));
			}
		}

		@Test
		@DisplayName("Should throw NullPointerException when path is null")
		void shouldThrowNullPointerExceptionWhenPathIsNull() {
			// Act & Assert
			assertThrows(NullPointerException.class, () -> reader.readSecurityPolicyFrom(null));
		}

		@Test
		@DisplayName("Should throw SecurityException when file does not exist")
		void shouldThrowSecurityExceptionWhenFileDoesNotExist(@TempDir Path tempDir) {
			Path nonExistentPath = tempDir.resolve("guaranteed-absent-policy.yaml");
			assertFalse(Files.exists(nonExistentPath));
			assertThrows(SecurityException.class, () -> reader.readSecurityPolicyFrom(nonExistentPath));
		}

		@Test
		@DisplayName("Should read valid security policy from YAML file")
		void shouldReadValidSecurityPolicyFromYAMLFile(@TempDir Path tempDir) throws IOException {
			// Arrange
			Path policyFile = tempDir.resolve("security-policy.yaml");
			String yamlContent = """
					thisPolicyFileCompliesToThePolicyVersion: 1
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
					    regardingTimeouts: []
					""";
			Files.writeString(policyFile, yamlContent);

			// Act
			SecurityPolicy policy = reader.readSecurityPolicyFrom(policyFile);

			// Assert
			assertNotNull(policy);
			assertNotNull(policy.regardingTheSupervisedCode());
			assertEquals(ProgrammingLanguageConfiguration.JAVA_USING_MAVEN_ARCHUNIT_AND_ASPECTJ,
					policy.regardingTheSupervisedCode().theFollowingProgrammingLanguageConfigurationIsUsed());
			assertEquals("com.example", policy.regardingTheSupervisedCode().theSupervisedCodeUsesTheFollowingPackage());
			assertEquals("Main", policy.regardingTheSupervisedCode().theMainClassInsideThisPackageIs());
			assertEquals(List.of("TestClass1", "TestClass2"),
					policy.regardingTheSupervisedCode().theFollowingClassesAreTestClasses());
		}

		@Test
		@DisplayName("Should read minimal security policy from YAML file")
		void shouldReadMinimalSecurityPolicyFromYAMLFile(@TempDir Path tempDir) throws IOException {
			// Arrange
			Path policyFile = tempDir.resolve("minimal-policy.yaml");
			String yamlContent = """
					thisPolicyFileCompliesToThePolicyVersion: 1
					regardingTheSupervisedCode:
					  theFollowingProgrammingLanguageConfigurationIsUsed: JAVA_USING_MAVEN_ARCHUNIT_AND_ASPECTJ
					  theFollowingClassesAreTestClasses: []
					  theFollowingResourceAccessesArePermitted:
					    regardingFileSystemInteractions: []
					    regardingNetworkConnections: []
					    regardingCommandExecutions: []
					    regardingThreadCreations: []
					    regardingPackageImports: []
					    regardingTimeouts: []
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
			String malformedYaml = "regardingTheSupervisedCode: [unterminated\n";
			Files.writeString(malformedFile, malformedYaml);

			// Act & Assert
			assertThrows(SecurityException.class, () -> reader.readSecurityPolicyFrom(malformedFile));
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
			assertThrows(SecurityException.class, () -> reader.readSecurityPolicyFrom(invalidFile));
		}

		@Test
		@DisplayName("Should throw SecurityException when file is not readable")
		void shouldThrowSecurityExceptionWhenFileIsNotReadable(@TempDir Path tempDir) throws IOException {
			Path unreadableFile = tempDir.resolve("unreadable.yaml");
			Files.writeString(unreadableFile, minimalPolicy());
			try {
				Files.setPosixFilePermissions(unreadableFile, EnumSet.noneOf(PosixFilePermission.class));
				org.junit.jupiter.api.Assumptions.assumeFalse(Files.isReadable(unreadableFile),
						"The platform or current user cannot enforce an unreadable fixture");
				SecurityException failure = assertThrows(SecurityException.class,
						() -> reader.readSecurityPolicyFrom(unreadableFile));
				assertNotNull(failure.getCause());
			} catch (UnsupportedOperationException exception) {
				org.junit.jupiter.api.Assumptions.abort("POSIX permissions are unavailable");
			} finally {
				try {
					Files.setPosixFilePermissions(unreadableFile,
							EnumSet.of(PosixFilePermission.OWNER_READ, PosixFilePermission.OWNER_WRITE));
				} catch (UnsupportedOperationException ignored) {
					unreadableFile.toFile().setReadable(true);
				}
			}
		}

		@Test
		void literalNullRootUsesTheDocumentedSecurityException(@TempDir Path tempDir) throws IOException {
			Path nullPolicy = tempDir.resolve("null.yaml");
			Files.writeString(nullPolicy, "null\n");
			SecurityException failure = assertThrows(SecurityException.class,
					() -> reader.readSecurityPolicyFrom(nullPolicy));
			assertTrue(failure.getMessage().contains(nullPolicy.toString()));
			assertNotNull(failure.getCause());
		}

		@Test
		@DisplayName("Should handle different programming language configurations")
		void shouldHandleDifferentProgrammingLanguageConfigurations(@TempDir Path tempDir) throws IOException {
			for (ProgrammingLanguageConfiguration config : ProgrammingLanguageConfiguration.values()) {
				// Arrange
				Path policyFile = tempDir.resolve("policy-" + config.name() + ".yaml");
				String yamlContent = String.format("""
						thisPolicyFileCompliesToThePolicyVersion: 1
						regardingTheSupervisedCode:
						  theFollowingProgrammingLanguageConfigurationIsUsed: %s
						  theFollowingClassesAreTestClasses: []
						  theFollowingResourceAccessesArePermitted:
						    regardingFileSystemInteractions: []
						    regardingNetworkConnections: []
						    regardingCommandExecutions: []
						    regardingThreadCreations: []
						    regardingPackageImports: []
						    regardingTimeouts: []
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
					thisPolicyFileCompliesToThePolicyVersion: 1
					regardingTheSupervisedCode:
					  theFollowingProgrammingLanguageConfigurationIsUsed: JAVA_USING_MAVEN_ARCHUNIT_AND_ASPECTJ
					  theFollowingClassesAreTestClasses: []
					  theFollowingResourceAccessesArePermitted:
					    regardingFileSystemInteractions: []
					    regardingNetworkConnections: []
					    regardingCommandExecutions: []
					    regardingThreadCreations: []
					    regardingPackageImports: []
					    regardingTimeouts: []
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
					thisPolicyFileCompliesToThePolicyVersion: 1
					regardingTheSupervisedCode:
					  # Missing required fields
					  theSupervisedCodeUsesTheFollowingPackage: "com.example"
					""";
			Files.writeString(incompleteFile, incompleteYaml);

			// Act & Assert
			SecurityException exception = assertThrows(SecurityException.class,
					() -> reader.readSecurityPolicyFrom(incompleteFile));
			assertNotNull(exception.getMessage());
		}

		@Test
		@DisplayName("Should handle empty file gracefully")
		void shouldHandleEmptyFileGracefully(@TempDir Path tempDir) throws IOException {
			// Arrange
			Path emptyFile = tempDir.resolve("empty.yaml");
			Files.writeString(emptyFile, "");

			// Act & Assert
			assertThrows(SecurityException.class, () -> reader.readSecurityPolicyFrom(emptyFile));
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
			assertThrows(SecurityException.class, () -> reader.readSecurityPolicyFrom(commentsOnlyFile));
		}
	}

	@Nested
	@DisplayName("theFollowingTestBehaviorIsConfigured Tests")
	class TestBehaviorConfigurationTests {

		@Test
		@DisplayName("Should parse a policy without the behavioural wrapper exactly as before")
		void wrapperAbsentParsesAsBefore(@TempDir Path tempDir) throws IOException {
			Path policyFile = tempDir.resolve("no-behavior.yaml");
			Files.writeString(policyFile, minimalPolicy());

			SecurityPolicy policy = reader.readSecurityPolicyFrom(policyFile);

			assertNull(policy.regardingTheSupervisedCode().theFollowingTestBehaviorIsConfigured());
		}

		@Test
		@DisplayName("Should parse the wrapper present but with no category configured")
		void wrapperPresentWithoutPrivilegedExceptionsCategory(@TempDir Path tempDir) throws IOException {
			Path policyFile = tempDir.resolve("empty-behavior.yaml");
			Files.writeString(policyFile,
					minimalPolicy().stripTrailing() + "\n  theFollowingTestBehaviorIsConfigured: {}\n");

			SecurityPolicy policy = reader.readSecurityPolicyFrom(policyFile);

			assertNotNull(policy.regardingTheSupervisedCode().theFollowingTestBehaviorIsConfigured());
			assertNull(policy.regardingTheSupervisedCode().theFollowingTestBehaviorIsConfigured()
					.regardingPrivilegedExceptions());
		}

		@Test
		@DisplayName("Should parse a fully configured privileged-exceptions category set to true")
		void validConfigurationTrue(@TempDir Path tempDir) throws IOException {
			Path policyFile = tempDir.resolve("privileged-true.yaml");
			Files.writeString(policyFile, minimalPolicy().stripTrailing() + """

					  theFollowingTestBehaviorIsConfigured:
					    regardingPrivilegedExceptions:
					      onlyPrivilegedExceptionsAreReported: true
					      theFailureMessageIs: "Custom message"
					""");

			SecurityPolicy policy = reader.readSecurityPolicyFrom(policyFile);

			var privilegedExceptions = policy.regardingTheSupervisedCode().theFollowingTestBehaviorIsConfigured()
					.regardingPrivilegedExceptions();
			assertTrue(privilegedExceptions.onlyPrivilegedExceptionsAreReported());
			assertEquals("Custom message", privilegedExceptions.theFailureMessageIs());
		}

		@Test
		@DisplayName("Should parse a fully configured privileged-exceptions category set to false")
		void validConfigurationFalse(@TempDir Path tempDir) throws IOException {
			Path policyFile = tempDir.resolve("privileged-false.yaml");
			Files.writeString(policyFile, minimalPolicy().stripTrailing() + """

					  theFollowingTestBehaviorIsConfigured:
					    regardingPrivilegedExceptions:
					      onlyPrivilegedExceptionsAreReported: false
					      theFailureMessageIs: "Custom message"
					""");

			SecurityPolicy policy = reader.readSecurityPolicyFrom(policyFile);

			assertFalse(policy.regardingTheSupervisedCode().theFollowingTestBehaviorIsConfigured()
					.regardingPrivilegedExceptions().onlyPrivilegedExceptionsAreReported());
		}

		@Test
		@DisplayName("Should default the omitted failure message")
		void omittedFailureMessageDefaults(@TempDir Path tempDir) throws IOException {
			Path policyFile = tempDir.resolve("privileged-default-message.yaml");
			Files.writeString(policyFile, minimalPolicy().stripTrailing() + """

					  theFollowingTestBehaviorIsConfigured:
					    regardingPrivilegedExceptions:
					      onlyPrivilegedExceptionsAreReported: true
					""");

			SecurityPolicy policy = reader.readSecurityPolicyFrom(policyFile);

			assertEquals("Test failed.", policy.regardingTheSupervisedCode().theFollowingTestBehaviorIsConfigured()
					.regardingPrivilegedExceptions().theFailureMessageIs());
		}

		@Test
		@DisplayName("Should default a whitespace-only failure message, not reject it")
		void blankFailureMessageDefaultsInsteadOfBeingRejected(@TempDir Path tempDir) throws IOException {
			Path policyFile = tempDir.resolve("privileged-blank-message.yaml");
			Files.writeString(policyFile, minimalPolicy().stripTrailing() + """

					  theFollowingTestBehaviorIsConfigured:
					    regardingPrivilegedExceptions:
					      onlyPrivilegedExceptionsAreReported: true
					      theFailureMessageIs: "   "
					""");

			SecurityPolicy policy = reader.readSecurityPolicyFrom(policyFile);

			assertEquals("Test failed.", policy.regardingTheSupervisedCode().theFollowingTestBehaviorIsConfigured()
					.regardingPrivilegedExceptions().theFailureMessageIs());
		}

		@Test
		@DisplayName("Should reject a missing onlyPrivilegedExceptionsAreReported field")
		void missingRequiredFieldIsRejected(@TempDir Path tempDir) throws IOException {
			Path policyFile = tempDir.resolve("privileged-missing-required.yaml");
			Files.writeString(policyFile, minimalPolicy().stripTrailing() + """

					  theFollowingTestBehaviorIsConfigured:
					    regardingPrivilegedExceptions:
					      theFailureMessageIs: "Custom message"
					""");

			assertThrows(SecurityException.class, () -> reader.readSecurityPolicyFrom(policyFile));
		}

		@Test
		@DisplayName("Should reject a non-boolean onlyPrivilegedExceptionsAreReported")
		void nonBooleanOnlyPrivilegedExceptionsAreReportedIsRejected(@TempDir Path tempDir) throws IOException {
			Path policyFile = tempDir.resolve("privileged-non-boolean.yaml");
			Files.writeString(policyFile, minimalPolicy().stripTrailing() + """

					  theFollowingTestBehaviorIsConfigured:
					    regardingPrivilegedExceptions:
					      onlyPrivilegedExceptionsAreReported: "yes"
					""");

			assertThrows(SecurityException.class, () -> reader.readSecurityPolicyFrom(policyFile));
		}

		@Test
		@DisplayName("Should reject an unknown field inside regardingPrivilegedExceptions")
		void unknownFieldInsideCategoryIsRejected(@TempDir Path tempDir) throws IOException {
			Path policyFile = tempDir.resolve("privileged-unknown-field.yaml");
			Files.writeString(policyFile, minimalPolicy().stripTrailing() + """

					  theFollowingTestBehaviorIsConfigured:
					    regardingPrivilegedExceptions:
					      onlyPrivilegedExceptionsAreReported: true
					      unknownField: "value"
					""");

			assertThrows(SecurityException.class, () -> reader.readSecurityPolicyFrom(policyFile));
		}

		@Test
		@DisplayName("Should reject an unknown behavioural category name")
		void unknownBehaviouralCategoryIsRejected(@TempDir Path tempDir) throws IOException {
			Path policyFile = tempDir.resolve("privileged-unknown-category.yaml");
			Files.writeString(policyFile, minimalPolicy().stripTrailing() + """

					  theFollowingTestBehaviorIsConfigured:
					    regardingSomeUnknownFeature:
					      enabled: true
					""");

			assertThrows(SecurityException.class, () -> reader.readSecurityPolicyFrom(policyFile));
		}

		@Test
		@DisplayName("Should reject a malformed (non-object) behavioural wrapper")
		void malformedWrapperTypeIsRejected(@TempDir Path tempDir) throws IOException {
			Path policyFile = tempDir.resolve("privileged-malformed-wrapper.yaml");
			Files.writeString(policyFile,
					minimalPolicy().stripTrailing() + "\n  theFollowingTestBehaviorIsConfigured: \"not-an-object\"\n");

			assertThrows(SecurityException.class, () -> reader.readSecurityPolicyFrom(policyFile));
		}

		@Test
		@DisplayName("Should reject a malformed (non-object) privileged-exceptions category")
		void malformedCategoryTypeIsRejected(@TempDir Path tempDir) throws IOException {
			Path policyFile = tempDir.resolve("privileged-malformed-category.yaml");
			Files.writeString(policyFile, minimalPolicy().stripTrailing() + """

					  theFollowingTestBehaviorIsConfigured:
					    regardingPrivilegedExceptions: "not-an-object"
					""");

			assertThrows(SecurityException.class, () -> reader.readSecurityPolicyFrom(policyFile));
		}
	}

	private static String minimalPolicy() {
		return """
				thisPolicyFileCompliesToThePolicyVersion: 1
				regardingTheSupervisedCode:
				  theFollowingProgrammingLanguageConfigurationIsUsed: JAVA_USING_MAVEN_ARCHUNIT_AND_ASPECTJ
				  theFollowingClassesAreTestClasses: []
				  theFollowingResourceAccessesArePermitted:
				    regardingFileSystemInteractions: []
				    regardingNetworkConnections: []
				    regardingCommandExecutions: []
				    regardingThreadCreations: []
				    regardingPackageImports: []
				    regardingTimeouts: []
				""";
	}
}
