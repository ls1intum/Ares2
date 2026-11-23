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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.yaml.snakeyaml.error.Mark;
import org.yaml.snakeyaml.error.MarkedYAMLException;
import org.yaml.snakeyaml.scanner.ScannerException;

import com.fasterxml.jackson.core.JsonLocation;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.io.ContentReference;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.exc.ValueInstantiationException;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;

import de.tum.cit.ase.ares.api.AresConstants;
import de.tum.cit.ase.ares.api.localization.AdviceActionWording;
import de.tum.cit.ase.ares.api.localization.Messages;
import de.tum.cit.ase.ares.api.policy.SecurityPolicy;
import de.tum.cit.ase.ares.api.policy.policySubComponents.ProgrammingLanguageConfiguration;
import de.tum.cit.ase.ares.api.policy.policySubComponents.ResourceAccesses;
import de.tum.cit.ase.ares.api.policy.policySubComponents.ThreadPermission;

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

		/**
		 * Checks that a negative thread count is refused under its short path, with the
		 * value.
		 */
		@Test
		void aNegativeThreadCountNamesTheEntryAndTheValue(@TempDir Path tempDir) throws IOException {
			Path policy = policy(tempDir, minimalPolicy().replace("regardingThreadCreations: []", """
					regardingThreadCreations:
					      - createTheFollowingNumberOfThreads: -1
					        ofThisClass: java.lang.Thread"""));
			assertFirstLine(policy, "security.policy.data.bind.failed",
					() -> Messages.localized("policy.schema.thread.count",
							"regardingThreadCreations[0].createTheFollowingNumberOfThreads", "-1"));
		}

		/**
		 * Checks that a refusal raised while a record is built keeps one Ares prefix
		 * and loses Jackson's framing, with Jackson's failure and the original one kept
		 * as causes.
		 */
		@Test
		void aRecordRefusalLosesJacksonsFramingAndTheInnerPrefix(@TempDir Path tempDir) throws IOException {
			SimpleModule module = new SimpleModule();
			module.addDeserializer(ThreadPermission.class, new StdDeserializer<>(ThreadPermission.class) {
				@Override
				public ThreadPermission deserialize(JsonParser parser, DeserializationContext context)
						throws IOException {
					parser.skipChildren();
					return (ThreadPermission) context.handleInstantiationProblem(ThreadPermission.class, null,
							new IllegalArgumentException(
									Messages.localized("policy.permission.thread.count.negative", -1)));
				}
			});
			YAMLMapper mapper = new YAMLMapper();
			mapper.registerModule(module);
			reader = new SecurityPolicyYAMLReader(mapper);
			Path policy = policy(tempDir, minimalPolicy().replace("regardingThreadCreations: []", """
					regardingThreadCreations:
					      - createTheFollowingNumberOfThreads: 1
					        ofThisClass: java.lang.Thread"""));
			assertFirstLine(policy, "security.policy.data.bind.failed", () -> {
				String inner = Messages.localized("policy.permission.thread.count.negative", -1);
				return inner.substring(inner.indexOf("): ") + 3);
			});
			SecurityException refused = refusal(policy, Locale.ENGLISH);
			assertInstanceOf(ValueInstantiationException.class, refused.getCause());
			String framed = refused.getCause().getMessage();
			assertTrue(framed.contains("Cannot construct instance") && framed.contains("problem:"), framed);
			assertFalse(refused.getMessage().contains("Cannot construct instance"), refused.getMessage());
			assertFalse(refused.getMessage().contains("problem:"), refused.getMessage());
			assertInstanceOf(IllegalArgumentException.class, refused.getCause().getCause());
		}

		/**
		 * Checks, through the real reader, that numbers written in a way YAML reads as
		 * a decimal, a text or a too large integer are refused under their short path.
		 *
		 * @param written  the YAML spelling of the value.
		 * @param field    which of the three number fields it is put into.
		 * @param key      the expected schema key.
		 * @param rendered the value as the message quotes it.
		 * @param tempDir  where the policy is written.
		 * @throws IOException if the policy cannot be written.
		 */
		@ParameterizedTest(name = "{1}: {0}")
		@CsvSource(delimiter = '|', value = { "1.0|port|policy.schema.integer|1.0",
				"-0.0|port|policy.schema.integer|-0.0", "1e3|port|policy.schema.integer|1000.0",
				"\"443\"|port|policy.schema.integer|\"443\"",
				"18446744073709551617|port|policy.schema.port|18446744073709551617",
				"4294967296|threads|policy.schema.thread.count|4294967296", "-0|timeout|policy.schema.timeout|0",
				"18446744073709551617|timeout|policy.schema.timeout|18446744073709551617" })
		void aNumberOutsideItsRangeOrNotWholeIsRefusedUnderItsPath(String written, String field, String key,
				String rendered, @TempDir Path tempDir) throws IOException {
			Path policy = policy(tempDir, numbers("0", "0", "1").replace(numberLine(field, numberDefault(field)),
					numberLine(field, written)));
			assertFirstLine(policy, "security.policy.data.bind.failed",
					() -> Messages.localized(key, numberPath(field), rendered));
		}

		/**
		 * Checks that a port, a thread count and a timeout on the edge of their ranges
		 * are accepted and bound as written, -0 as 0.
		 */
		@Test
		void numbersOnTheBoundariesAreAcceptedAndBoundAsWritten(@TempDir Path tempDir) throws IOException {
			ResourceAccesses upper = reader
					.readSecurityPolicyFrom(policy(tempDir, numbers("65535", "2147483647", "9223372036854775807")))
					.regardingTheSupervisedCode().theFollowingResourceAccessesArePermitted();
			assertEquals(65535, upper.regardingNetworkConnections().get(0).onThePort());
			assertEquals(Integer.MAX_VALUE,
					upper.regardingThreadCreations().get(0).createTheFollowingNumberOfThreads());
			assertEquals(Long.MAX_VALUE, upper.regardingTimeouts().get(0).timeout());
			ResourceAccesses lower = reader.readSecurityPolicyFrom(policy(tempDir, numbers("-0", "-0", "1")))
					.regardingTheSupervisedCode().theFollowingResourceAccessesArePermitted();
			assertEquals(0, lower.regardingNetworkConnections().get(0).onThePort());
			assertEquals(0, lower.regardingThreadCreations().get(0).createTheFollowingNumberOfThreads());
			assertEquals(1L, lower.regardingTimeouts().get(0).timeout());
		}

		/**
		 * Checks that a refused path under a long project root is quoted with its end,
		 * where the part written after the placeholder sits.
		 */
		@Test
		void aLongProjectRootKeepsTheWrittenEndOfARefusedPath(@TempDir Path tempDir) throws IOException {
			Path projectRoot = tempDir.resolve("r".repeat(120));
			reader = new SecurityPolicyYAMLReader(yamlMapper, projectRoot);
			Path policy = policy(tempDir, minimalPolicy().replace("regardingFileSystemInteractions: []", """
					regardingFileSystemInteractions:
					      - onThisPathAndAllPathsBelow: ${PROJECT_ROOT}/../secret
					        readAllFiles: true
					        overwriteAllFiles: false
					        createAllFiles: false
					        executeAllFiles: false
					        deleteAllFiles: false"""));
			for (Locale locale : List.of(Locale.ENGLISH, Locale.GERMAN)) {
				SecurityException refused = refusal(policy, locale);
				String message = refused.getMessage();
				assertTrue(message.contains("..." + "r".repeat(20)), message);
				assertTrue(message.contains("r/../secret\","), message);
				assertShape(refused, locale);
			}
		}

		/**
		 * Checks that a file the parser cannot read is refused with the line and column
		 * the parser reported, worked out by hand for each file, and its own text.
		 *
		 * @param content the YAML text.
		 * @param line    the expected line, counted from 1.
		 * @param column  the expected column, counted from 1.
		 * @param tempDir where the policy is written.
		 * @throws IOException if the policy cannot be written.
		 */
		@ParameterizedTest(name = "line {1}, column {2}")
		@MethodSource("de.tum.cit.ase.ares.api.policy.reader.yaml.SecurityPolicyYAMLReaderTest#unreadableFiles")
		void anUnreadableFileNamesTheLineAndColumnTheParserReported(String content, int line, int column,
				@TempDir Path tempDir) throws IOException {
			Path policy = policy(tempDir, content);
			for (Locale locale : List.of(Locale.ENGLISH, Locale.GERMAN)) {
				SecurityException refused = refusal(policy, locale);
				String start = AdviceActionWording.inLocale(locale,
						() -> Messages.localized("security.policy.parse.failed.at", policy.toString(), line, column, "")
								.stripTrailing());
				assertTrue(refused.getMessage().startsWith(start), refused.getMessage());
				String parserText = refused.getMessage().substring(start.length()).strip();
				assertFalse(parserText.isEmpty(), refused.getMessage());
				assertFalse(parserText.matches("(?s)\\w+Exception: .*"), parserText);
				assertShape(refused, locale);
			}
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

		/**
		 * Checks that an unfinished list is refused with the line and column the parser
		 * reported and the parser's own text.
		 */
		@Test
		void malformedYamlNamesTheParserErrorAndItsLine(@TempDir Path tempDir) throws IOException {
			Path policy = policy(tempDir, "regardingTheSupervisedCode: [unterminated\n");
			for (Locale locale : List.of(Locale.ENGLISH, Locale.GERMAN)) {
				SecurityException refused = refusal(policy, locale);
				String start = AdviceActionWording.inLocale(locale, () -> Messages
						.localized("security.policy.parse.failed.at", policy.toString(), 2, 1, "").stripTrailing());
				String message = refused.getMessage();
				assertTrue(message.startsWith(start), message);
				assertTrue(message.contains("expected ',' or ']', but got <stream end>"), message);
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

	/**
	 * Checks how the reader finds where the parser stopped: SnakeYAML's own mark
	 * first, Jackson's location only as the fallback, and no position at all rather
	 * than a wrong one.
	 */
	@Nested
	@DisplayName("Parser problem position")
	class ParserProblemTests {

		/**
		 * Builds a SnakeYAML failure with its problem mark at the given place.
		 *
		 * @param line   the mark's line, counted from 0.
		 * @param column the mark's column, counted from 0.
		 * @param cause  the failure underneath, or null.
		 * @return the failure.
		 */
		private MarkedYAMLException markedFailure(int line, int column, Throwable cause) {
			Mark mark = new Mark("reader", 0, line, column, new int[] { ' ' }, 0);
			return new MarkedYAMLException("while scanning", mark, "found a tab", mark, cause) {
				/** Serialisation version, fixed so the class can be serialised safely. */
				private static final long serialVersionUID = 1L;
			};
		}

		/**
		 * Builds a Jackson parse failure at the given place.
		 *
		 * @param message the failure's text.
		 * @param line    the line, counted from 1, or -1 for unknown.
		 * @param column  the column, counted from 1, or -1 for unknown.
		 * @param cause   the failure underneath, or null.
		 * @return the failure.
		 */
		private JsonParseException jacksonFailure(String message, int line, int column, Throwable cause) {
			return new JsonParseException((JsonParser) null, message,
					new JsonLocation(ContentReference.unknown(), -1L, line, column), cause);
		}

		/**
		 * Checks that SnakeYAML's mark is used rather than Jackson's location, which
		 * points past a SnakeYAML failure.
		 */
		@Test
		void snakeYamlsMarkWinsOverJacksonsLocation() {
			MarkedYAMLException scanner = markedFailure(2, 0, null);
			assertEquals(new SecurityPolicyYAMLReader.ParserProblem(3, 1, scanner.getMessage().strip()),
					SecurityPolicyYAMLReader.parserProblemOf(jacksonFailure("wrapped", 10, 59, scanner)));
		}

		/**
		 * Checks that of two SnakeYAML failures in the chain, the innermost one gives
		 * the position.
		 */
		@Test
		void theInnermostMarkWins() {
			MarkedYAMLException outer = markedFailure(1, 1, markedFailure(4, 6, null));
			SecurityPolicyYAMLReader.ParserProblem problem = SecurityPolicyYAMLReader
					.parserProblemOf(jacksonFailure("wrapped", 10, 59, outer));
			assertEquals(5, problem.line());
			assertEquals(7, problem.column());
		}

		/**
		 * Checks that a SnakeYAML failure without a mark leaves the position to
		 * Jackson's location.
		 */
		@Test
		void aMissingMarkFallsBackToJacksonsLocation() {
			ScannerException scanner = new ScannerException("while scanning", null, "found a tab", null);
			assertEquals(new SecurityPolicyYAMLReader.ParserProblem(12, 21, "Duplicate field 'x'"),
					SecurityPolicyYAMLReader.parserProblemOf(jacksonFailure("Duplicate field 'x'", 12, 21, scanner)));
		}

		/**
		 * Checks that a location without a column gives no position rather than a half
		 * one.
		 */
		@Test
		void anUnknownColumnGivesNoPosition() {
			assertNull(SecurityPolicyYAMLReader.parserProblemOf(jacksonFailure("broken", 12, -1, null)));
		}

		/**
		 * Checks that a failure without any location gives no position.
		 */
		@Test
		void noLocationGivesNoPosition() {
			assertNull(SecurityPolicyYAMLReader
					.parserProblemOf(new JsonParseException((JsonParser) null, "broken", (JsonLocation) null)));
		}

		/**
		 * Checks that a cause chain that loops back on itself is walked once and then
		 * left.
		 */
		@Test
		void aCyclicCauseChainEnds() {
			IllegalStateException around = new IllegalStateException("around");
			JsonParseException failure = jacksonFailure("broken", 2, 3, around);
			around.initCause(failure);
			assertEquals(new SecurityPolicyYAMLReader.ParserProblem(2, 3, "broken"),
					SecurityPolicyYAMLReader.parserProblemOf(failure));
		}

		/**
		 * Checks that a blank parser text is replaced by the failure's class name, so
		 * the message never ends empty.
		 */
		@Test
		void aBlankParserTextFallsBackToTheClassName() {
			assertEquals(new SecurityPolicyYAMLReader.ParserProblem(1, 1, "JsonParseException"),
					SecurityPolicyYAMLReader.parserProblemOf(jacksonFailure("  ", 1, 1, null)));
		}

		/**
		 * Checks that without a trustworthy position the reader keeps its general
		 * reading failure.
		 */
		@Test
		void withoutAPositionTheReaderKeepsItsGeneralMessage(@TempDir Path tempDir) {
			Path policy = tempDir.resolve("policy.yaml");
			JsonParseException failure = jacksonFailure("broken", 12, -1, null);
			assertEquals(
					Messages.localized("security.policy.read.failed", policy.toString(),
							SecurityPolicyYAMLReader.causeOf(failure)),
					SecurityPolicyYAMLReader.parseFailureMessage(policy, failure));
		}
	}

	/**
	 * Lists files the parser cannot read, each with the line and column the parser
	 * reports for it: the culprit's own place, or the place just after it, which is
	 * why the message tells the reader to check the lines above as well.
	 *
	 * @return the text, the line and the column.
	 */
	static Stream<Arguments> unreadableFiles() {
		return Stream.of(
				Arguments.of("thisPolicyFileCompliesToThePolicyVersion: 1\nregardingTheSupervisedCode:\n"
						+ "\ttheFollowingClassesAreTestClasses: []\n", 3, 1),
				Arguments.of(
						"thisPolicyFileCompliesToThePolicyVersion: 1\nthisPolicyFileCompliesToThePolicyVersion: 1\n", 2,
						41),
				Arguments.of("a: 1\nb:2\nc: 3\n", 3, 1), Arguments.of("a:\n  - b: 1\n   c: 2\n", 3, 4),
				Arguments.of("a: \"x\nb: \"y\"\n", 2, 5), Arguments.of("? [a, b]\n: 1\n", 1, 4));
	}

	/**
	 * Builds a minimal policy with one network, one thread and one timeout entry.
	 *
	 * @param port        the port as written.
	 * @param threadCount the thread count as written.
	 * @param timeout     the timeout as written.
	 * @return the policy text.
	 */
	private static String numbers(String port, String threadCount, String timeout) {
		return minimalPolicy().replace("regardingNetworkConnections: []", """
				regardingNetworkConnections:
				      - onTheHost: localhost
				        %s
				        openConnections: true
				        sendData: false
				        receiveData: false""".formatted(numberLine("port", port)))
				.replace("regardingThreadCreations: []", """
						regardingThreadCreations:
						      - %s
						        ofThisClass: java.lang.Thread""".formatted(numberLine("threads", threadCount)))
				.replace("regardingTimeouts: []", """
						regardingTimeouts:
						      - %s""".formatted(numberLine("timeout", timeout)));
	}

	/**
	 * Writes one number field as a YAML line, without its indentation.
	 *
	 * @param field   port, threads or timeout.
	 * @param written the value as written.
	 * @return the line.
	 */
	private static String numberLine(String field, String written) {
		return switch (field) {
		case "port" -> "onThePort: " + written;
		case "threads" -> "createTheFollowingNumberOfThreads: " + written;
		default -> "timeout: " + written;
		};
	}

	/**
	 * Returns the value {@link #numbers} is called with for a field in the cases.
	 *
	 * @param field port, threads or timeout.
	 * @return its default.
	 */
	private static String numberDefault(String field) {
		return "timeout".equals(field) ? "1" : "0";
	}

	/**
	 * Returns the short path a refusal names for a number field.
	 *
	 * @param field port, threads or timeout.
	 * @return the short path.
	 */
	private static String numberPath(String field) {
		return switch (field) {
		case "port" -> "regardingNetworkConnections[0].onThePort";
		case "threads" -> "regardingThreadCreations[0].createTheFollowingNumberOfThreads";
		default -> "regardingTimeouts[0].timeout";
		};
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
