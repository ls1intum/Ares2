package de.tum.cit.ase.ares.api.policy.reader.yaml;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermission;
import java.time.Duration;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;

import de.tum.cit.ase.ares.api.MirrorOutput;
import de.tum.cit.ase.ares.api.policy.SecurityPolicy;
import de.tum.cit.ase.ares.api.policy.policySubComponents.InputOutputManagerKind;
import de.tum.cit.ase.ares.api.policy.policySubComponents.ProgrammingLanguageConfiguration;
import de.tum.cit.ase.ares.api.policy.policySubComponents.TestBehaviorConfiguration;

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

	/** Reading and validating {@code theFollowingTestBehaviorIsConfigured}. */
	@Nested
	@DisplayName("theFollowingTestBehaviorIsConfigured Tests")
	class TestBehaviorConfigurationTests {

		/**
		 * A policy without {@code theFollowingTestBehaviorIsConfigured} has no
		 * behaviour configuration.
		 *
		 * @param tempDir the folder for the policy file.
		 * @throws IOException if the policy file cannot be written
		 */
		@Test
		@DisplayName("Should parse a policy without the behavioural wrapper exactly as before")
		void wrapperAbsentParsesAsBefore(@TempDir Path tempDir) throws IOException {
			assertNull(
					read(tempDir, minimalPolicy()).regardingTheSupervisedCode().theFollowingTestBehaviorIsConfigured());
		}

		/**
		 * An empty wrapper, as 2.2.1 accepted it, sets neither category.
		 *
		 * @param tempDir the folder for the policy file.
		 * @throws IOException if the policy file cannot be written
		 */
		@Test
		@DisplayName("Should parse the wrapper with no category, as released in 2.2.1")
		void emptyWrapperParses(@TempDir Path tempDir) throws IOException {
			TestBehaviorConfiguration behavior = behavior(tempDir, "{}");
			assertNull(behavior.regardingTheStrictTimeoutForTheTestCases());
			assertNull(behavior.regardingTheOutputOfTheTestCases());
		}

		/**
		 * Both categories are read with every field.
		 *
		 * @param tempDir the folder for the policy file.
		 * @throws IOException if the policy file cannot be written
		 */
		@Test
		@DisplayName("Should parse its categories with every field")
		void ownCategoriesParse(@TempDir Path tempDir) throws IOException {
			TestBehaviorConfiguration behavior = behavior(tempDir, """

					    regardingTheStrictTimeoutForTheTestCases:
					      theTimeoutIs: "2s"
					      theTerminationGraceIs: "500ms"
					    regardingTheOutputOfTheTestCases:
					      theOutputIsMirrored: true
					      theMaximumNumberOfOutputCharactersIs: 1000
					      theInputAndOutputAreManagedBy: IO_TESTER
					""");
			assertEquals(Duration.ofSeconds(2), behavior.regardingTheStrictTimeoutForTheTestCases().timeout());
			assertEquals(Optional.of(Duration.ofMillis(500)),
					behavior.regardingTheStrictTimeoutForTheTestCases().terminationGrace());
			assertTrue(behavior.regardingTheOutputOfTheTestCases().mirrored());
			assertEquals(1000, behavior.regardingTheOutputOfTheTestCases().maximumCharacterCount());
			assertEquals(InputOutputManagerKind.IO_TESTER, behavior.regardingTheOutputOfTheTestCases().managerKind());
		}

		/**
		 * An empty output category gives the defaults: no mirroring, the IO tester's
		 * limit and the IO tester.
		 *
		 * @param tempDir the folder for the policy file.
		 * @throws IOException if the policy file cannot be written
		 */
		@Test
		@DisplayName("Should default every optional field to today's behaviour")
		void emptyOptionalCategoriesDefault(@TempDir Path tempDir) throws IOException {
			TestBehaviorConfiguration behavior = behavior(tempDir, """

					    regardingTheOutputOfTheTestCases: {}
					""");
			assertFalse(behavior.regardingTheOutputOfTheTestCases().mirrored());
			assertEquals(MirrorOutput.DEFAULT_MAX_STD_OUT,
					behavior.regardingTheOutputOfTheTestCases().maximumCharacterCount());
			assertEquals(InputOutputManagerKind.IO_TESTER, behavior.regardingTheOutputOfTheTestCases().managerKind());
		}

		/**
		 * Every malformed section is refused when the policy is read, including the
		 * categories this branch does not handle.
		 *
		 * @param description what is wrong with the section.
		 * @param section     the section's YAML.
		 * @param tempDir     the folder for the policy file.
		 * @throws IOException if the policy file cannot be written
		 */
		@ParameterizedTest(name = "[{index}] {0}")
		@DisplayName("Should reject every malformed test-behaviour section")
		@CsvSource(delimiter = '|', value = { "wrapper not an object | []", "wrapper explicit null | null",
				"unknown category | { regardingSomethingElse: {} }",
				"category explicit null | { regardingTheStrictTimeoutForTheTestCases: null }",
				"field explicit null | { regardingTheStrictTimeoutForTheTestCases: { theTimeoutIs: '1s', theTerminationGraceIs: null } }",
				"strict timeout missing timeout | { regardingTheStrictTimeoutForTheTestCases: { theTerminationGraceIs: '1s' } }",
				"strict timeout not a duration | { regardingTheStrictTimeoutForTheTestCases: { theTimeoutIs: 'soon' } }",
				"strict timeout a number | { regardingTheStrictTimeoutForTheTestCases: { theTimeoutIs: 2 } }",
				"strict timeout zero | { regardingTheStrictTimeoutForTheTestCases: { theTimeoutIs: '0s' } }",
				"strict timeout units out of order | { regardingTheStrictTimeoutForTheTestCases: { theTimeoutIs: '1s 1m' } }",
				"strict timeout repeated unit | { regardingTheStrictTimeoutForTheTestCases: { theTimeoutIs: '1s 2s' } }",
				"grace above one day | { regardingTheStrictTimeoutForTheTestCases: { theTimeoutIs: '1s', theTerminationGraceIs: '2d' } }",
				"mirroring not a boolean | { regardingTheOutputOfTheTestCases: { theOutputIsMirrored: 'yes' } }",
				"limit not a number | { regardingTheOutputOfTheTestCases: { theMaximumNumberOfOutputCharactersIs: 'many' } }",
				"limit zero | { regardingTheOutputOfTheTestCases: { theMaximumNumberOfOutputCharactersIs: 0 } }",
				"unknown manager | { regardingTheOutputOfTheTestCases: { theInputAndOutputAreManagedBy: OTHER } }",
				"custom without class | { regardingTheOutputOfTheTestCases: { theInputAndOutputAreManagedBy: CUSTOM } }",
				"class without custom | { regardingTheOutputOfTheTestCases: { theCustomInputAndOutputManagerIs: 'org.example.M' } }",
				"none with mirroring | { regardingTheOutputOfTheTestCases: { theInputAndOutputAreManagedBy: NONE, theOutputIsMirrored: false } }",
				"failure feedback not handled here | { regardingTheFailureFeedbackForTheTestCases: { onlyPrivilegedExceptionsAreReported: true } }",
				"visibility and schedule not handled here | { regardingTheVisibilityAndScheduleOfTheTestCases: { theDeadlineIs: '2000-01-01 00:00 UTC', testsNotListedHereAreHidden: false } }" })
		void malformedSectionsAreRejected(String description, String section, @TempDir Path tempDir)
				throws IOException {
			Path policyFile = tempDir.resolve("malformed.yaml");
			Files.writeString(policyFile,
					minimalPolicy().stripTrailing() + "\n  theFollowingTestBehaviorIsConfigured: " + section + "\n");
			assertThrows(RuntimeException.class, () -> reader.readSecurityPolicyFrom(policyFile), description);
		}

		/**
		 * Category names that were never released are refused.
		 *
		 * @param category the old category name.
		 * @param body     the category's YAML.
		 * @param tempDir  the folder for the policy file.
		 * @throws IOException if the policy file cannot be written
		 */
		@ParameterizedTest(name = "[{index}] {0}")
		@DisplayName("Should reject the unreleased keys the separate pull requests used")
		@CsvSource(delimiter = '|', value = { "regardingStrictTimeouts | { theTimeoutIs: '1s' }",
				"regardingOutputMirroring | { theOutputIsMirrored: true }" })
		void oldCategoryKeysAreRejected(String category, String body, @TempDir Path tempDir) throws IOException {
			Path policyFile = tempDir.resolve("old.yaml");
			Files.writeString(policyFile, minimalPolicy().stripTrailing()
					+ "\n  theFollowingTestBehaviorIsConfigured: { " + category + ": " + body + " }\n");
			assertThrows(RuntimeException.class, () -> reader.readSecurityPolicyFrom(policyFile), category);
		}

		/**
		 * Field names that were never released are refused inside the new categories.
		 *
		 * @param field   the old field name.
		 * @param section the section's YAML.
		 * @param tempDir the folder for the policy file.
		 * @throws IOException if the policy file cannot be written
		 */
		@ParameterizedTest(name = "[{index}] {0}")
		@DisplayName("Should reject the unreleased field names inside the new categories")
		@CsvSource(delimiter = '|', value = {
				"theTimeUnitIs | { regardingTheStrictTimeoutForTheTestCases: { theTimeoutIs: '1s', theTimeUnitIs: SECONDS } }",
				"theTerminationGraceUnitIs | { regardingTheStrictTimeoutForTheTestCases: { theTimeoutIs: '1s', theTerminationGraceUnitIs: SECONDS } }",
				"theMaximumCharacterCountIs | { regardingTheOutputOfTheTestCases: { theMaximumCharacterCountIs: 5 } }" })
		void oldFieldNamesAreRejected(String field, String section, @TempDir Path tempDir) throws IOException {
			Path policyFile = tempDir.resolve("old-field.yaml");
			Files.writeString(policyFile,
					minimalPolicy().stripTrailing() + "\n  theFollowingTestBehaviorIsConfigured: " + section + "\n");
			assertThrows(RuntimeException.class, () -> reader.readSecurityPolicyFrom(policyFile), field);
		}

		/** Reads a policy text from a temporary file. */
		private SecurityPolicy read(Path tempDir, String text) throws IOException {
			Path policyFile = tempDir.resolve("policy.yaml");
			Files.writeString(policyFile, text);
			return reader.readSecurityPolicyFrom(policyFile);
		}

		/** Reads the test behaviour of the minimal policy with the given section. */
		private TestBehaviorConfiguration behavior(Path tempDir, String section) throws IOException {
			return read(tempDir,
					minimalPolicy().stripTrailing() + "\n  theFollowingTestBehaviorIsConfigured: " + section + "\n")
							.regardingTheSupervisedCode().theFollowingTestBehaviorIsConfigured();
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
