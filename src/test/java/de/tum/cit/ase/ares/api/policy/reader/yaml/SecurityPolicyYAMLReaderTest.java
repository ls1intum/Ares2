package de.tum.cit.ase.ares.api.policy.reader.yaml;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.spy;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermission;
import java.util.EnumSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;

import de.tum.cit.ase.ares.api.AresConstants;
import de.tum.cit.ase.ares.api.localization.AdviceActionWording;
import de.tum.cit.ase.ares.api.localization.Messages;
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

		/**
		 * Checked-in policies that are broken on purpose, so that a test can show the
		 * refusal Ares gives for them.
		 */
		private static final Set<String> DELIBERATELY_BROKEN_POLICIES = Set
				.of("PolicyFailureMessageShapeMisspelledField.yaml");

		@Test
		void shouldReadEveryCheckedInSecurityPolicy() throws IOException {
			Path examplePolicy = Path.of("src/main/resources/ExampleConfiguration.yaml");
			Path testPolicies = Path.of("src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies");
			try (Stream<Path> files = Stream.concat(Stream.of(examplePolicy), Files.walk(testPolicies))) {
				files.filter(Files::isRegularFile).filter(path -> path.getFileName().toString().endsWith(".yaml"))
						.filter(path -> !DELIBERATELY_BROKEN_POLICIES.contains(path.getFileName().toString()))
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

		/**
		 * Checks that when the file parses and validates but turning it into a policy
		 * object yields {@code null}, the reader throws a {@link SecurityException}
		 * rather than returning {@code null}.
		 */
		@Test
		@DisplayName("Should wrap a null binding result as a SecurityException")
		void wrapsNullBindingResultAsSecurityException(@TempDir Path tempDir) throws Exception {
			Path policyFile = tempDir.resolve("policy.yaml");
			Files.writeString(policyFile, minimalPolicy());
			YAMLMapper spyMapper = spy(new YAMLMapper());
			SecurityPolicyYAMLReader spyReader = new SecurityPolicyYAMLReader(spyMapper, tempDir);
			doReturn(null).when(spyMapper).treeToValue(any(JsonNode.class), eq(SecurityPolicy.class));

			assertThrows(SecurityException.class, () -> spyReader.readSecurityPolicyFrom(policyFile));
		}

		/**
		 * Checks that an {@link UnsupportedOperationException} thrown while turning the
		 * file into a policy object reaches the caller as a {@link SecurityException}.
		 */
		@Test
		@DisplayName("Should wrap an UnsupportedOperationException as a SecurityException")
		void wrapsUnsupportedOperationAsSecurityException(@TempDir Path tempDir) throws Exception {
			Path policyFile = tempDir.resolve("policy.yaml");
			Files.writeString(policyFile, minimalPolicy());
			YAMLMapper spyMapper = spy(new YAMLMapper());
			SecurityPolicyYAMLReader spyReader = new SecurityPolicyYAMLReader(spyMapper, tempDir);
			doThrow(new UnsupportedOperationException("boom")).when(spyMapper).treeToValue(any(JsonNode.class),
					eq(SecurityPolicy.class));

			assertThrows(SecurityException.class, () -> spyReader.readSecurityPolicyFrom(policyFile));
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
		@DisplayName("Should parse the wrapper present but empty")
		void wrapperPresentButEmptyParses(@TempDir Path tempDir) throws IOException {
			Path policyFile = tempDir.resolve("empty-behavior.yaml");
			Files.writeString(policyFile,
					minimalPolicy().stripTrailing() + "\n  theFollowingTestBehaviorIsConfigured: {}\n");

			SecurityPolicy policy = reader.readSecurityPolicyFrom(policyFile);

			assertNotNull(policy.regardingTheSupervisedCode().theFollowingTestBehaviorIsConfigured());
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
		@DisplayName("Should reject an explicit null wrapper, distinct from the wrapper being absent")
		void explicitNullWrapperIsRejected(@TempDir Path tempDir) throws IOException {
			Path policyFile = tempDir.resolve("privileged-null-wrapper.yaml");
			Files.writeString(policyFile,
					minimalPolicy().stripTrailing() + "\n  theFollowingTestBehaviorIsConfigured: null\n");

			assertThrows(SecurityException.class, () -> reader.readSecurityPolicyFrom(policyFile));
		}
	}

	/**
	 * Checks that every error raised while reading or validating the policy names
	 * the file and the concrete cause in its first line, with stage Creation, one
	 * prefix only, short field paths, and the original exception kept as cause.
	 */
	@Nested
	@DisplayName("First line names the cause")
	class FirstLineCauseTests {

		/** The English start of a reader error. */
		private static final String ENGLISH_PREFIX = "Ares Security Error (Reason: Ares-Code; Stage: Creation): ";

		/** The German start of a reader error. */
		private static final String GERMAN_PREFIX = "Ares Sicherheitsfehler (Grund: Ares-Code; Phase: Erstellung): ";

		/** Any Ares prefix, used to count them in one message. */
		private static final Pattern ANY_PREFIX = Pattern.compile("Ares (Security Error|Sicherheitsfehler) \\(");

		/**
		 * Reads the policy, which must be refused, in the given language.
		 *
		 * @param policy the policy file.
		 * @param locale the language.
		 * @return the refusal.
		 */
		private SecurityException refusal(Path policy, Locale locale) {
			SecurityException[] refused = new SecurityException[1];
			AdviceActionWording.inLocale(locale, () -> {
				refused[0] = assertThrows(SecurityException.class, () -> reader.readSecurityPolicyFrom(policy));
				return refused[0].getMessage();
			});
			return refused[0];
		}

		/**
		 * Checks what every reader error shares: the prefix with stage Creation in the
		 * language, a single prefix, no {@code $.} paths and a kept cause.
		 *
		 * @param refused the refusal.
		 * @param locale  the language it was raised in.
		 */
		private void assertShape(SecurityException refused, Locale locale) {
			String message = refused.getMessage();
			assertTrue(message.startsWith(Locale.GERMAN.equals(locale) ? GERMAN_PREFIX : ENGLISH_PREFIX), message);
			Matcher prefixes = ANY_PREFIX.matcher(message);
			int count = 0;
			while (prefixes.find()) {
				count++;
			}
			assertEquals(1, count, message);
			assertFalse(message.contains("$."), message);
			assertFalse(message.contains("regardingTheSupervisedCode."), message);
			assertNotNull(refused.getCause(), message);
		}

		/**
		 * Checks, in English and German, that the refusal of the policy reads exactly
		 * as the reader key with the file and the given cause.
		 *
		 * @param policy the policy file.
		 * @param key    the reader's {@code security.policy.*} key.
		 * @param cause  computes the expected cause in the current language.
		 */
		private void assertFirstLine(Path policy, String key, Supplier<String> cause) {
			for (Locale locale : List.of(Locale.ENGLISH, Locale.GERMAN)) {
				SecurityException refused = refusal(policy, locale);
				String expected = AdviceActionWording.inLocale(locale,
						() -> Messages.localized(key, policy.toString(), cause.get()));
				assertEquals(expected, refused.getMessage(), locale.toString());
				assertShape(refused, locale);
			}
		}

		/**
		 * Writes a policy file.
		 *
		 * @param folder  where to write it.
		 * @param content the YAML text.
		 * @return the file.
		 * @throws IOException if it cannot be written.
		 */
		private Path policy(Path folder, String content) throws IOException {
			return Files.writeString(folder.resolve("policy.yaml"), content);
		}

		@Test
		void anUnknownFieldIsNamedWithTheKnownFields(@TempDir Path tempDir) throws IOException {
			Path policy = policy(tempDir,
					minimalPolicy().replace("regardingFileSystemInteractions:", "regardingFileSystemInteraction:"));
			assertFirstLine(policy, "security.policy.data.bind.failed",
					() -> Messages.localized("policy.schema.unknown.field", "theFollowingResourceAccessesArePermitted",
							"regardingFileSystemInteraction",
							"regardingCommandExecutions, regardingFileSystemInteractions, "
									+ "regardingNetworkConnections, regardingPackageImports, "
									+ "regardingThreadCreations, regardingTimeouts"));
		}

		@Test
		void aWrongTypeQuotesTheValueUnderItsShortPath(@TempDir Path tempDir) throws IOException {
			Path policy = policy(tempDir, minimalPolicy().replace("regardingFileSystemInteractions: []", """
					regardingFileSystemInteractions:
					      - onThisPathAndAllPathsBelow: /tmp/data
					        readAllFiles: "yes"
					        overwriteAllFiles: false
					        createAllFiles: false
					        executeAllFiles: false
					        deleteAllFiles: false"""));
			assertFirstLine(policy, "security.policy.data.bind.failed", () -> Messages
					.localized("policy.schema.boolean", "regardingFileSystemInteractions[0].readAllFiles", "\"yes\""));
		}

		@Test
		void aMissingRequiredFieldNamesTheFieldAndItsObject(@TempDir Path tempDir) throws IOException {
			Path policy = policy(tempDir, minimalPolicy().replace("  theFollowingClassesAreTestClasses: []\n", ""));
			assertFirstLine(policy, "security.policy.data.bind.failed",
					() -> Messages.localized("policy.schema.missing.field", "regardingTheSupervisedCode",
							"theFollowingClassesAreTestClasses"));
		}

		@Test
		void aNegativeThreadCountLosesJacksonsFramingAndTheInnerPrefix(@TempDir Path tempDir) throws IOException {
			Path policy = policy(tempDir, minimalPolicy().replace("regardingThreadCreations: []", """
					regardingThreadCreations:
					      - createTheFollowingNumberOfThreads: -1
					        ofThisClass: java.lang.Thread"""));
			assertFirstLine(policy, "security.policy.data.bind.failed", () -> {
				String inner = Messages.localized("policy.permission.thread.count.negative", -1);
				return inner.substring(inner.indexOf("): ") + 3);
			});
			String message = refusal(policy, Locale.ENGLISH).getMessage();
			assertFalse(message.contains("Cannot construct instance"), message);
			assertFalse(message.contains("problem:"), message);
		}

		@Test
		void anUnknownLanguageConfigurationListsTheKnownOnes(@TempDir Path tempDir) throws IOException {
			Path policy = policy(tempDir, minimalPolicy().replace("JAVA_USING_MAVEN_ARCHUNIT_AND_ASPECTJ",
					"JAVA_USING_GRADLE_ARCHUNIT_AND_SPRINGAOP"));
			String known = Stream.of(ProgrammingLanguageConfiguration.values()).map(Enum::name)
					.collect(Collectors.joining(", "));
			assertFirstLine(policy, "security.policy.data.bind.failed",
					() -> Messages.localized("policy.schema.configuration",
							"theFollowingProgrammingLanguageConfigurationIsUsed",
							"\"JAVA_USING_GRADLE_ARCHUNIT_AND_SPRINGAOP\"", known));
		}

		@Test
		void aWrongVersionNamesTheSupportedOne(@TempDir Path tempDir) throws IOException {
			org.junit.jupiter.api.Assumptions.assumeTrue(
					AresConstants.MINIMUM_POLICY_VERSION == AresConstants.MAXIMUM_POLICY_VERSION,
					"Ares supports more than one policy version");
			Path policy = policy(tempDir, minimalPolicy().replace("thisPolicyFileCompliesToThePolicyVersion: 1",
					"thisPolicyFileCompliesToThePolicyVersion: 7"));
			assertFirstLine(policy, "security.policy.data.bind.failed",
					() -> Messages.localized("policy.schema.version.single", "7",
							String.valueOf(AresConstants.MAXIMUM_POLICY_VERSION)));
		}

		@Test
		void malformedYamlNamesTheParserErrorAndItsLine(@TempDir Path tempDir) throws IOException {
			Path policy = policy(tempDir, "regardingTheSupervisedCode: [unterminated\n");
			for (Locale locale : List.of(Locale.ENGLISH, Locale.GERMAN)) {
				SecurityException refused = refusal(policy, locale);
				String start = AdviceActionWording.inLocale(locale,
						() -> Messages.localized("security.policy.read.failed", policy.toString(), ""));
				String message = refused.getMessage();
				assertTrue(message.startsWith(start), message);
				String cause = message.substring(start.length());
				assertTrue(cause.matches("(?s)\\w+Exception: .+"), cause);
				assertTrue(cause.contains("line"), cause);
				assertShape(refused, locale);
			}
		}

		@Test
		void aMissingFileNamesTheExceptionAndThePath(@TempDir Path tempDir) {
			Path absent = tempDir.resolve("guaranteed-absent-policy.yaml");
			assertFalse(Files.exists(absent));
			assertFirstLine(absent, "security.policy.io.exception.cause",
					() -> "NoSuchFileException: " + absent.normalize());
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
