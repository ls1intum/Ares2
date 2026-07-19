package de.tum.cit.ase.ares.api.policy.reader.yaml;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;

import de.tum.cit.ase.ares.api.policy.SecurityPolicy;
import de.tum.cit.ase.ares.api.policy.policySubComponents.NetworkPermission;
import de.tum.cit.ase.ares.api.policy.policySubComponents.ProgrammingLanguageConfiguration;
import de.tum.cit.ase.ares.api.policy.reader.SecurityPolicyReader;

class SecurityPolicyStrictSchemaTest {
	@TempDir
	Path tempDirectory;

	@Test
	void bindsOneFullyPopulatedPolicy() throws IOException {
		SecurityPolicy policy = read(validYaml());
		var resources = policy.regardingTheSupervisedCode().theFollowingResourceAccessesArePermitted();
		assertEquals(1, resources.regardingFileSystemInteractions().size());
		assertEquals(1, resources.regardingNetworkConnections().size());
		assertEquals(1, resources.regardingCommandExecutions().size());
		assertEquals(1, resources.regardingThreadCreations().size());
		assertEquals(1, resources.regardingPackageImports().size());
		assertEquals(1, resources.regardingTimeouts().size());
		assertEquals(0, resources.regardingNetworkConnections().get(0).onThePort());
	}

	static Stream<Arguments> invalidScalarCases() {
		return Stream.of(Arguments.of("onThePort: 0", "onThePort: null"),
				Arguments.of("onThePort: 0", "onThePort: \"443\""),
				Arguments.of("openConnections: true", "openConnections: yes"),
				Arguments.of("openConnections: true", "openConnections: 1"),
				Arguments.of("openConnections: true", "openConnections: \"true\""),
				Arguments.of("timeout: 30", "timeout: null"), Arguments.of("timeout: 30", "timeout: \"30\""),
				Arguments.of("createAllFiles: true", "createAllFiles: null"));
	}

	@ParameterizedTest
	@MethodSource("invalidScalarCases")
	void rejectsNullAndCoercedRequiredScalars(String original, String replacement) {
		SecurityException failure = assertThrows(SecurityException.class,
				() -> read(validYaml().replace(original, replacement)));
		assertTrue(failure.getCause() != null);
	}

	@ParameterizedTest
	@MethodSource("invalidPorts")
	void validatesPortBoundaries(int port, boolean valid) {
		if (valid) {
			assertEquals(port, new NetworkPermission("localhost", port, true, false, false).onThePort());
		} else {
			assertThrows(IllegalArgumentException.class,
					() -> new NetworkPermission("localhost", port, true, false, false));
		}
	}

	static Stream<Arguments> invalidPorts() {
		return Stream.of(Arguments.of(-2, false), Arguments.of(-1, false), Arguments.of(0, true),
				Arguments.of(443, true), Arguments.of(65535, true), Arguments.of(65536, false));
	}

	@Test
	void rejectsDuplicateKeysTrailingDocumentsUnknownCommandsAndNullRoot() {
		for (String duplicate : List.of(validYaml() + "\nregardingTheSupervisedCode: null\n",
				validYaml().replace("theMainClassInsideThisPackageIs: Main",
						"theMainClassInsideThisPackageIs: First\n  theMainClassInsideThisPackageIs: Main"),
				validYaml().replace("readAllFiles: true", "readAllFiles: false\n        readAllFiles: true"),
				validYaml().replace("onThePort: 0", "onThePort: 443\n        onThePort: 0"), validYaml().replace(
						"executeTheCommand: java", "executeTheCommand: javac\n        executeTheCommand: java"))) {
			assertThrows(SecurityException.class, () -> read(duplicate));
		}
		assertThrows(SecurityException.class, () -> read(validYaml() + "\n---\n" + validYaml()));
		assertThrows(SecurityException.class,
				() -> read(validYaml().replace("withTheseArguments", "withThoseArguments")));
		assertThrows(SecurityException.class, () -> read("null\n"));
	}

	static Stream<Arguments> invalidSchemaShapes() {
		return Stream.of(Arguments.of("onThePort: 0", "missingPort: 0", "onThePort"),
				Arguments.of("regardingNetworkConnections:", "regardingNetworkConnections: wrong\n    ignored:",
						"regardingNetworkConnections"),
				Arguments.of("regardingPackageImports:\n      - importTheFollowingPackage: java.util",
						"regardingPackageImports: [null]", "regardingPackageImports"),
				Arguments.of("theFollowingClassesAreTestClasses: [com.example.MainTest]",
						"theFollowingClassesAreTestClasses: [null]", "theFollowingClassesAreTestClasses"),
				Arguments.of("ofThisClass: java.lang.Thread", "ofThisClass: ' '", "ofThisClass"),
				Arguments.of("withTheseArguments: [--version]", "withTheseArguments: [1]", "withTheseArguments"));
	}

	@ParameterizedTest
	@MethodSource("invalidSchemaShapes")
	void rejectsMissingWrongContainerNullElementAndBlankValues(String original, String replacement,
			String rejectedField) {
		SecurityException failure = assertThrows(SecurityException.class,
				() -> read(validYaml().replace(original, replacement)));
		assertTrue(failure.getMessage().contains("policy") || failure.getCause().getMessage().contains(rejectedField));
	}

	static Stream<String> everyRequiredField() {
		return Stream.of("/thisPolicyFileCompliesToThePolicyVersion", "/regardingTheSupervisedCode",
				"/regardingTheSupervisedCode/theFollowingProgrammingLanguageConfigurationIsUsed",
				"/regardingTheSupervisedCode/theFollowingClassesAreTestClasses",
				"/regardingTheSupervisedCode/theFollowingResourceAccessesArePermitted",
				"/regardingTheSupervisedCode/theFollowingResourceAccessesArePermitted/regardingFileSystemInteractions",
				"/regardingTheSupervisedCode/theFollowingResourceAccessesArePermitted/regardingNetworkConnections",
				"/regardingTheSupervisedCode/theFollowingResourceAccessesArePermitted/regardingCommandExecutions",
				"/regardingTheSupervisedCode/theFollowingResourceAccessesArePermitted/regardingThreadCreations",
				"/regardingTheSupervisedCode/theFollowingResourceAccessesArePermitted/regardingPackageImports",
				"/regardingTheSupervisedCode/theFollowingResourceAccessesArePermitted/regardingTimeouts",
				"/regardingTheSupervisedCode/theFollowingResourceAccessesArePermitted/regardingFileSystemInteractions/0/onThisPathAndAllPathsBelow",
				"/regardingTheSupervisedCode/theFollowingResourceAccessesArePermitted/regardingFileSystemInteractions/0/readAllFiles",
				"/regardingTheSupervisedCode/theFollowingResourceAccessesArePermitted/regardingFileSystemInteractions/0/overwriteAllFiles",
				"/regardingTheSupervisedCode/theFollowingResourceAccessesArePermitted/regardingFileSystemInteractions/0/createAllFiles",
				"/regardingTheSupervisedCode/theFollowingResourceAccessesArePermitted/regardingFileSystemInteractions/0/executeAllFiles",
				"/regardingTheSupervisedCode/theFollowingResourceAccessesArePermitted/regardingFileSystemInteractions/0/deleteAllFiles",
				"/regardingTheSupervisedCode/theFollowingResourceAccessesArePermitted/regardingNetworkConnections/0/onTheHost",
				"/regardingTheSupervisedCode/theFollowingResourceAccessesArePermitted/regardingNetworkConnections/0/onThePort",
				"/regardingTheSupervisedCode/theFollowingResourceAccessesArePermitted/regardingNetworkConnections/0/openConnections",
				"/regardingTheSupervisedCode/theFollowingResourceAccessesArePermitted/regardingNetworkConnections/0/sendData",
				"/regardingTheSupervisedCode/theFollowingResourceAccessesArePermitted/regardingNetworkConnections/0/receiveData",
				"/regardingTheSupervisedCode/theFollowingResourceAccessesArePermitted/regardingCommandExecutions/0/executeTheCommand",
				"/regardingTheSupervisedCode/theFollowingResourceAccessesArePermitted/regardingCommandExecutions/0/withTheseArguments",
				"/regardingTheSupervisedCode/theFollowingResourceAccessesArePermitted/regardingThreadCreations/0/createTheFollowingNumberOfThreads",
				"/regardingTheSupervisedCode/theFollowingResourceAccessesArePermitted/regardingThreadCreations/0/ofThisClass",
				"/regardingTheSupervisedCode/theFollowingResourceAccessesArePermitted/regardingPackageImports/0/importTheFollowingPackage",
				"/regardingTheSupervisedCode/theFollowingResourceAccessesArePermitted/regardingTimeouts/0/timeout");
	}

	@ParameterizedTest
	@MethodSource("everyRequiredField")
	void rejectsOmissionAndExplicitNullForEveryRequiredField(String pointer) throws IOException {
		for (boolean explicitNull : List.of(false, true)) {
			ObjectNode root = (ObjectNode) new YAMLMapper().readTree(validYaml());
			int separator = pointer.lastIndexOf('/');
			String parentPointer = separator == 0 ? "" : pointer.substring(0, separator);
			String field = pointer.substring(separator + 1);
			ObjectNode parent = (ObjectNode) root.at(parentPointer);
			if (explicitNull) {
				parent.putNull(field);
			} else {
				parent.remove(field);
			}
			assertRejectedField(new YAMLMapper().writeValueAsString(root), field);
		}
	}

	static Stream<String> everyStrictScalarField() {
		return everyRequiredField()
				.filter(pointer -> !pointer.endsWith("regardingTheSupervisedCode")
						&& !pointer.endsWith("theFollowingResourceAccessesArePermitted")
						&& !pointer.contains("regardingFileSystemInteractions") || pointer.matches(".*/0/.*"))
				.filter(pointer -> !pointer.endsWith("theFollowingClassesAreTestClasses"))
				.filter(pointer -> !pointer.matches(
						".*/regarding(FileSystemInteractions|NetworkConnections|CommandExecutions|ThreadCreations|PackageImports|Timeouts)$"))
				.filter(pointer -> !pointer.endsWith("withTheseArguments"));
	}

	@ParameterizedTest
	@MethodSource("everyStrictScalarField")
	void rejectsWrongTypeForEveryRequiredScalar(String pointer) throws IOException {
		ObjectNode root = (ObjectNode) new YAMLMapper().readTree(validYaml());
		int separator = pointer.lastIndexOf('/');
		String field = pointer.substring(separator + 1);
		ObjectNode parent = (ObjectNode) root.at(pointer.substring(0, separator));
		JsonNode current = parent.get(field);
		if (current.isBoolean() || current.isIntegralNumber()) {
			parent.put(field, current.asText());
		} else {
			parent.put(field, 1);
		}
		assertRejectedField(new YAMLMapper().writeValueAsString(root), field);
	}

	@Test
	void rejectsWrongContainersAndNullElementsForEveryCollection() throws IOException {
		List<String> collections = List.of("theFollowingClassesAreTestClasses", "regardingFileSystemInteractions",
				"regardingNetworkConnections", "regardingCommandExecutions", "regardingThreadCreations",
				"regardingPackageImports", "regardingTimeouts", "withTheseArguments");
		for (String field : collections) {
			String pointer = pointerFor(field);
			ObjectNode wrongContainer = (ObjectNode) new YAMLMapper().readTree(validYaml());
			int separator = pointer.lastIndexOf('/');
			((ObjectNode) wrongContainer.at(pointer.substring(0, separator))).put(field, "not-an-array");
			assertRejectedField(new YAMLMapper().writeValueAsString(wrongContainer), field);

			ObjectNode nullElement = (ObjectNode) new YAMLMapper().readTree(validYaml());
			((com.fasterxml.jackson.databind.node.ArrayNode) nullElement.at(pointer)).removeAll().addNull();
			assertRejectedField(new YAMLMapper().writeValueAsString(nullElement), field);
		}
	}

	private static String pointerFor(String field) {
		String resources = "/regardingTheSupervisedCode/theFollowingResourceAccessesArePermitted/";
		return switch (field) {
		case "theFollowingClassesAreTestClasses" -> "/regardingTheSupervisedCode/" + field;
		case "withTheseArguments" -> resources + "regardingCommandExecutions/0/" + field;
		default -> resources + field;
		};
	}

	private void assertRejectedField(String yaml, String field) throws IOException {
		SecurityException failure = assertThrows(SecurityException.class, () -> read(yaml));
		StringBuilder messages = new StringBuilder();
		for (Throwable cause = failure; cause != null; cause = cause.getCause()) {
			messages.append(cause.getMessage()).append(' ');
		}
		assertTrue(messages.toString().contains(field), () -> "Expected rejected field " + field + " in " + messages);
	}

	@Test
	void suppliedMapperControlsBinding() throws IOException {
		YAMLMapper mapper = new YAMLMapper();
		SimpleModule module = new SimpleModule();
		module.addDeserializer(ProgrammingLanguageConfiguration.class,
				new JsonDeserializer<ProgrammingLanguageConfiguration>() {
					@Override
					public ProgrammingLanguageConfiguration deserialize(JsonParser parser,
							DeserializationContext context) throws IOException {
						assertEquals("JAVA_USING_MAVEN_ARCHUNIT_AND_ASPECTJ", parser.getText());
						return ProgrammingLanguageConfiguration.JAVA_USING_GRADLE_WALA_AND_INSTRUMENTATION;
					}
				});
		mapper.registerModule(module);
		Path file = write(validYaml());
		SecurityPolicy policy = new SecurityPolicyYAMLReader(mapper, tempDirectory).readSecurityPolicyFrom(file);
		assertEquals(ProgrammingLanguageConfiguration.JAVA_USING_GRADLE_WALA_AND_INSTRUMENTATION,
				policy.regardingTheSupervisedCode().theFollowingProgrammingLanguageConfigurationIsUsed());
	}

	@Test
	void expandsProjectRootAsScalarDataAfterParsing() throws IOException {
		Path unusualRoot = tempDirectory.resolve("root: #quote '\" \\ ä\nline\u0001");
		Files.createDirectories(unusualRoot);
		Path file = write(validYaml().replace("/tmp/data", "${PROJECT_ROOT}/line\\nname"));
		SecurityPolicy policy = new SecurityPolicyYAMLReader(new YAMLMapper(), unusualRoot)
				.readSecurityPolicyFrom(file);
		String path = policy.regardingTheSupervisedCode().theFollowingResourceAccessesArePermitted()
				.regardingFileSystemInteractions().get(0).onThisPathAndAllPathsBelow();
		assertTrue(path.startsWith(unusualRoot.toString()));
		assertTrue(path.endsWith("line\\nname"));
	}

	@Test
	void rejectsUnknownPathPlaceholders() throws IOException {
		Path file = write(validYaml().replace("/tmp/data", "${PROJECT_ROOT}/${PROJECT_ROOT}/${UNKNOWN}"));
		assertThrows(SecurityException.class,
				() -> new SecurityPolicyYAMLReader(new YAMLMapper(), tempDirectory).readSecurityPolicyFrom(file));
	}

	static Stream<Arguments> invalidPolicyValues() {
		return Stream.of(
				Arguments.of("thisPolicyFileCompliesToThePolicyVersion: 1",
						"thisPolicyFileCompliesToThePolicyVersion: 2"),
				Arguments.of("JAVA_USING_MAVEN_ARCHUNIT_AND_ASPECTJ", "JAVA_MAVEN_ARCHUNIT_ASPECTJ"),
				Arguments.of("theSupervisedCodeUsesTheFollowingPackage: com.example",
						"theSupervisedCodeUsesTheFollowingPackage: com.class"),
				Arguments.of("theMainClassInsideThisPackageIs: Main",
						"theMainClassInsideThisPackageIs: com.example.Main"),
				Arguments.of("theFollowingClassesAreTestClasses: [com.example.MainTest]",
						"theFollowingClassesAreTestClasses: [com.example.]"),
				Arguments.of("onThisPathAndAllPathsBelow: /tmp/data", "onThisPathAndAllPathsBelow: '${UNKNOWN}/data'"),
				Arguments.of("onTheHost: localhost", "onTheHost: 256.1.1.1"),
				Arguments.of("ofThisClass: java.lang.Thread", "ofThisClass: java.lang."),
				Arguments.of("importTheFollowingPackage: java.util", "importTheFollowingPackage: java.*"));
	}

	@ParameterizedTest
	@MethodSource("invalidPolicyValues")
	void rejectsValuesThatDoNotMatchTheirPolicyRegex(String original, String replacement) {
		assertThrows(SecurityException.class, () -> read(validYaml().replace(original, replacement)));
	}

	@Test
	void selectsEveryCaseCombinationOfYamlExtensions() {
		for (String extension : List.of("yaml", "yml")) {
			for (int mask = 0; mask < 1 << extension.length(); mask++) {
				StringBuilder mixed = new StringBuilder();
				for (int index = 0; index < extension.length(); index++) {
					char character = extension.charAt(index);
					mixed.append((mask & 1 << index) == 0 ? character : Character.toUpperCase(character));
				}
				assertInstanceOf(SecurityPolicyYAMLReader.class,
						SecurityPolicyReader.selectSecurityPolicyReader(Path.of("policy." + mixed)));
			}
		}
		assertThrows(NullPointerException.class, () -> SecurityPolicyReader.selectSecurityPolicyReader(null));
		assertThrows(IllegalArgumentException.class,
				() -> SecurityPolicyReader.selectSecurityPolicyReader(Path.of("policy")));
		assertThrows(IllegalArgumentException.class,
				() -> SecurityPolicyReader.selectSecurityPolicyReader(Path.of("policy.json")));
	}

	private SecurityPolicy read(String yaml) throws IOException {
		return new SecurityPolicyYAMLReader(new YAMLMapper(), tempDirectory).readSecurityPolicyFrom(write(yaml));
	}

	private Path write(String yaml) throws IOException {
		Path file = tempDirectory.resolve("policy-" + Integer.toUnsignedString(yaml.hashCode()) + ".yaml");
		Files.writeString(file, yaml);
		return file;
	}

	private static String validYaml() {
		return """
				thisPolicyFileCompliesToThePolicyVersion: 1
				regardingTheSupervisedCode:
				  theFollowingProgrammingLanguageConfigurationIsUsed: JAVA_USING_MAVEN_ARCHUNIT_AND_ASPECTJ
				  theSupervisedCodeUsesTheFollowingPackage: com.example
				  theMainClassInsideThisPackageIs: Main
				  theFollowingClassesAreTestClasses: [com.example.MainTest]
				  theFollowingResourceAccessesArePermitted:
				    regardingFileSystemInteractions:
				      - onThisPathAndAllPathsBelow: /tmp/data
				        readAllFiles: true
				        overwriteAllFiles: false
				        createAllFiles: true
				        executeAllFiles: false
				        deleteAllFiles: false
				    regardingNetworkConnections:
				      - onTheHost: localhost
				        onThePort: 0
				        openConnections: true
				        sendData: false
				        receiveData: false
				    regardingCommandExecutions:
				      - executeTheCommand: java
				        withTheseArguments: [--version]
				    regardingThreadCreations:
				      - createTheFollowingNumberOfThreads: 1
				        ofThisClass: java.lang.Thread
				    regardingPackageImports:
				      - importTheFollowingPackage: java.util
				    regardingTimeouts:
				      - timeout: 30
				""";
	}
}
