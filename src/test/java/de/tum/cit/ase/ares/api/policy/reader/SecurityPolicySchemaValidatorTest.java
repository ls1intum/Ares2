package de.tum.cit.ase.ares.api.policy.reader;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.List;
import java.util.stream.Stream;

import javax.annotation.Nullable;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;

import de.tum.cit.ase.ares.api.AresConstants;

/**
 * Checks each rule of the policy schema validator on its own. Every case breaks
 * exactly one rule of an otherwise valid policy and expects the message that
 * names the field to fix. Messages are compared literally, as they are not
 * translated.
 */
class SecurityPolicySchemaValidatorTest {

	/** The policy field that holds the supervised code settings. */
	private static final String SUPERVISED = "/regardingTheSupervisedCode";

	/** The policy field that holds the permitted resource accesses. */
	private static final String ACCESSES = SUPERVISED + "/theFollowingResourceAccessesArePermitted";

	/** The factory that builds the replacement values of the cases. */
	private static final JsonNodeFactory NODES = JsonNodeFactory.instance;

	/** Marks an expected message that is compared only up to its pattern. */
	private static final String PATTERN_PREFIX_END = "must match ";

	/**
	 * Parses a fully populated, valid policy into a fresh tree.
	 *
	 * @return the policy tree, which the caller may change freely.
	 */
	private static ObjectNode validTree() {
		String yaml = """
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
		try {
			return (ObjectNode) new YAMLMapper().readTree(yaml);
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}

	/**
	 * Builds a valid policy and changes one field of it.
	 *
	 * @param parentPointer where the field sits, as a JSON pointer.
	 * @param field         the name of the field to change.
	 * @param replacement   the new value, or null to remove the field.
	 * @return the changed policy tree.
	 */
	private static JsonNode changed(String parentPointer, String field, @Nullable JsonNode replacement) {
		ObjectNode tree = validTree();
		ObjectNode parent = (ObjectNode) tree.at(parentPointer);
		if (replacement == null) {
			parent.remove(field);
		} else {
			parent.set(field, replacement);
		}
		return tree;
	}

	/**
	 * Builds an array node from the given entries.
	 *
	 * @param entries the entries of the array.
	 * @return the array node.
	 */
	private static ArrayNode array(JsonNode... entries) {
		return NODES.arrayNode().addAll(List.of(entries));
	}

	/**
	 * Checks that the validator refuses the tree with the expected message. A
	 * message that ends at the pattern marker is compared only up to it.
	 *
	 * @param tree            the policy tree to validate.
	 * @param expectedMessage the message the refusal must carry.
	 */
	private static void assertRejected(JsonNode tree, String expectedMessage) {
		MismatchedInputException thrown = assertThrows(MismatchedInputException.class,
				() -> SecurityPolicySchemaValidator.validate(tree));
		if (expectedMessage.endsWith(PATTERN_PREFIX_END)) {
			assertTrue(thrown.getOriginalMessage().startsWith(expectedMessage), thrown.getOriginalMessage());
		} else {
			assertEquals(expectedMessage, thrown.getOriginalMessage());
		}
	}

	/** Checks that a fully populated policy is accepted. */
	@Test
	void acceptsTheFullyPopulatedPolicy() {
		assertDoesNotThrow(() -> SecurityPolicySchemaValidator.validate(validTree()));
	}

	/** Checks that a command written as a bare name is accepted. */
	@Test
	void acceptsTheBareStringCommandForm() {
		JsonNode tree = changed(ACCESSES, "regardingCommandExecutions", array(NODES.textNode("java")));
		assertDoesNotThrow(() -> SecurityPolicySchemaValidator.validate(tree));
	}

	/** Checks that empty and blank command arguments are accepted. */
	@Test
	void acceptsEmptyAndBlankCommandArguments() {
		ObjectNode tree = validTree();
		((ObjectNode) tree.at(ACCESSES + "/regardingCommandExecutions/0")).set("withTheseArguments",
				array(NODES.textNode(""), NODES.textNode("  ")));
		assertDoesNotThrow(() -> SecurityPolicySchemaValidator.validate(tree));
	}

	/** Checks that the package and the main class may be left out or be null. */
	@Test
	void acceptsAnOmittedOrNullPackageAndMainClass() {
		ObjectNode omitted = validTree();
		ObjectNode omittedCode = (ObjectNode) omitted.at(SUPERVISED);
		omittedCode.remove("theSupervisedCodeUsesTheFollowingPackage");
		omittedCode.remove("theMainClassInsideThisPackageIs");
		assertDoesNotThrow(() -> SecurityPolicySchemaValidator.validate(omitted));
		ObjectNode nulled = validTree();
		ObjectNode nulledCode = (ObjectNode) nulled.at(SUPERVISED);
		nulledCode.set("theSupervisedCodeUsesTheFollowingPackage", NODES.nullNode());
		nulledCode.set("theMainClassInsideThisPackageIs", NODES.nullNode());
		assertDoesNotThrow(() -> SecurityPolicySchemaValidator.validate(nulled));
	}

	/** Checks that an empty test behaviour object is accepted. */
	@Test
	void acceptsAnEmptyTestBehaviorObject() {
		JsonNode tree = changed(SUPERVISED, "theFollowingTestBehaviorIsConfigured", NODES.objectNode());
		assertDoesNotThrow(() -> SecurityPolicySchemaValidator.validate(tree));
	}

	/** Checks that a root that is not an object is refused. */
	@Test
	void rejectsARootThatIsNotAnObject() {
		assertRejected(NODES.textNode("a bare scalar"), "$ must be a non-null object");
	}

	/** Checks that an unknown field in the test behaviour object is refused. */
	@Test
	void rejectsAnUnknownFieldInTheTestBehavior() {
		ObjectNode tree = validTree();
		ObjectNode behavior = NODES.objectNode();
		behavior.set("extra", NODES.textNode("x"));
		((ObjectNode) tree.at(SUPERVISED)).set("theFollowingTestBehaviorIsConfigured", behavior);
		assertRejected(tree,
				"$.regardingTheSupervisedCode.theFollowingTestBehaviorIsConfigured contains unknown fields [extra]");
	}

	/**
	 * Checks that each broken rule is refused with the message of that rule.
	 *
	 * @param caseName        what is broken, used as the test name.
	 * @param parentPointer   where the broken field sits.
	 * @param field           the broken field.
	 * @param replacement     the broken value, or null to remove the field.
	 * @param expectedMessage the message the refusal must carry.
	 */
	@ParameterizedTest(name = "{0}")
	@MethodSource("brokenRules")
	void rejectsEachBrokenRuleWithItsOwnMessage(String caseName, String parentPointer, String field,
			@Nullable JsonNode replacement, String expectedMessage) {
		assertRejected(changed(parentPointer, field, replacement), expectedMessage);
	}

	/**
	 * Lists the broken rules, each with one defect and the message it must give.
	 *
	 * @return the cases for the parameterised test.
	 */
	static Stream<Arguments> brokenRules() {
		String version = "thisPolicyFileCompliesToThePolicyVersion";
		String language = "theFollowingProgrammingLanguageConfigurationIsUsed";
		String supervised = "$.regardingTheSupervisedCode";
		String testClasses = "theFollowingClassesAreTestClasses";
		String versionRange = "$.thisPolicyFileCompliesToThePolicyVersion must be between "
				+ AresConstants.MINIMUM_POLICY_VERSION + " and " + AresConstants.MAXIMUM_POLICY_VERSION
				+ " (inclusive), but was ";
		JsonNode text = NODES.textNode("x");
		JsonNode five = NODES.numberNode(5);
		JsonNode invalidName = NODES.textNode("1bad");
		JsonNode invalidCommand = NODES.textNode("echo ");
		String files = ACCESSES + "/regardingFileSystemInteractions/0";
		String network = ACCESSES + "/regardingNetworkConnections/0";
		String commands = ACCESSES + "/regardingCommandExecutions";
		String command = commands + "/0";
		String entry = "regardingCommandExecutions entry";
		return Stream.of(Arguments.of("root unknown field", "", "extra", text, "$ contains unknown fields [extra]"),
				Arguments.of("root missing field", "", version, null, "$ is missing required fields [" + version + "]"),
				Arguments.of("root null field", "", "regardingTheSupervisedCode", NODES.nullNode(),
						"$.regardingTheSupervisedCode must not be null"),
				Arguments.of("version decimal", "", version, NODES.numberNode(1.5d),
						"$." + version + " must be an integer"),
				Arguments.of("version beyond int", "", version, NODES.numberNode(3000000000L),
						"$." + version + " must fit into an int"),
				Arguments.of("version above range", "", version,
						NODES.numberNode(AresConstants.MAXIMUM_POLICY_VERSION + 1),
						versionRange + (AresConstants.MAXIMUM_POLICY_VERSION + 1)),
				Arguments.of("version below range", "", version,
						NODES.numberNode(AresConstants.MINIMUM_POLICY_VERSION - 1),
						versionRange + (AresConstants.MINIMUM_POLICY_VERSION - 1)),
				Arguments.of("supervised code not an object", "", "regardingTheSupervisedCode", text,
						supervised + " must be a non-null object"),
				Arguments.of("supervised code unknown field", SUPERVISED, "extra", text,
						supervised + " contains unknown fields [extra]"),
				Arguments.of("configuration not text", SUPERVISED, language, five,
						supervised + "." + language + " must be a non-blank string"),
				Arguments.of("configuration unknown", SUPERVISED, language, NODES.textNode("NO_SUCH_CONFIGURATION"),
						supervised + "." + language + " must be a known configuration"),
				Arguments.of("package not text", SUPERVISED, "theSupervisedCodeUsesTheFollowingPackage", five,
						supervised + ".theSupervisedCodeUsesTheFollowingPackage must be a non-blank string or null"),
				Arguments.of("main class blank", SUPERVISED, "theMainClassInsideThisPackageIs", NODES.textNode("  "),
						supervised + ".theMainClassInsideThisPackageIs must be a non-blank string or null"),
				Arguments.of("package invalid", SUPERVISED, "theSupervisedCodeUsesTheFollowingPackage", invalidName,
						supervised + ".theSupervisedCodeUsesTheFollowingPackage must be a valid package name or null"),
				Arguments.of("main class invalid", SUPERVISED, "theMainClassInsideThisPackageIs",
						NODES.textNode("Main Class"),
						supervised + ".theMainClassInsideThisPackageIs must be a valid class name or null"),
				Arguments.of("test classes not an array", SUPERVISED, testClasses,
						NODES.textNode("com.example.MainTest"),
						supervised + "." + testClasses + " must be an array of strings"),
				Arguments.of("test class not text", SUPERVISED, testClasses, array(five),
						supervised + "." + testClasses + " must contain only non-blank strings"),
				Arguments.of("test class invalid", SUPERVISED, testClasses, array(invalidName),
						supervised + "." + testClasses + " entries must be valid class names"),
				Arguments.of("resources not an object", SUPERVISED, "theFollowingResourceAccessesArePermitted", text,
						supervised + ".theFollowingResourceAccessesArePermitted must be a non-null object"),
				Arguments.of("file list not an array", ACCESSES, "regardingFileSystemInteractions", text,
						"regardingFileSystemInteractions must be an array"),
				Arguments.of("file entry not an object", ACCESSES, "regardingFileSystemInteractions", array(text),
						"regardingFileSystemInteractions entry must be a non-null object"),
				Arguments.of("network entry missing port", network, "onThePort", null,
						"regardingNetworkConnections entry is missing required fields [onThePort]"),
				Arguments.of("file path not text", files, "onThisPathAndAllPathsBelow", five,
						"file permission.onThisPathAndAllPathsBelow must be a non-blank string"),
				Arguments.of("file path walks up", files, "onThisPathAndAllPathsBelow", NODES.textNode("../x"),
						"file permission.onThisPathAndAllPathsBelow must match "),
				Arguments.of("file flag quoted", files, "createAllFiles", NODES.textNode("true"),
						"file permission.createAllFiles must be a boolean"),
				Arguments.of("host not text", network, "onTheHost", five,
						"network permission.onTheHost must be a non-blank string"),
				Arguments.of("host invalid", network, "onTheHost", NODES.textNode("bad host"),
						"network permission.onTheHost must match "),
				Arguments.of("port not integral", network, "onThePort", NODES.textNode("80"),
						"network permission.onThePort must be an integer"),
				Arguments.of("thread class not text", ACCESSES + "/regardingThreadCreations/0", "ofThisClass", five,
						"thread permission.ofThisClass must be a non-blank string"),
				Arguments.of("thread class invalid", ACCESSES + "/regardingThreadCreations/0", "ofThisClass",
						invalidName, "thread permission.ofThisClass must be a valid thread construct"),
				Arguments.of("import not text", ACCESSES + "/regardingPackageImports/0", "importTheFollowingPackage",
						five, "package permission.importTheFollowingPackage must be a non-blank string"),
				Arguments.of("import invalid", ACCESSES + "/regardingPackageImports/0", "importTheFollowingPackage",
						invalidName, "package permission.importTheFollowingPackage must be a valid package name or *"),
				Arguments.of("exactMatchOnly quoted", ACCESSES + "/regardingPackageImports/0", "exactMatchOnly",
						NODES.textNode("true"), "package permission.exactMatchOnly must be a boolean when present"),
				Arguments.of("timeout not integral", ACCESSES + "/regardingTimeouts/0", "timeout", NODES.textNode("30"),
						"timeout permission.timeout must be an integer"),
				Arguments.of("commands not an array", ACCESSES, "regardingCommandExecutions", NODES.textNode("java"),
						"regardingCommandExecutions must be an array"),
				Arguments.of("bare command invalid", ACCESSES, "regardingCommandExecutions", array(invalidCommand),
						entry + " must match "),
				Arguments.of("command entry neither text nor object", ACCESSES, "regardingCommandExecutions",
						array(five), entry + " must be a non-null object"),
				Arguments.of("command name not text", command, "executeTheCommand", five,
						entry + ".executeTheCommand must be a non-blank string"),
				Arguments.of("command name invalid", command, "executeTheCommand", invalidCommand,
						entry + ".executeTheCommand must match "),
				Arguments.of("arguments not an array", command, "withTheseArguments", NODES.textNode("--version"),
						entry + ".withTheseArguments must be an array of strings"),
				Arguments.of("argument not text", command, "withTheseArguments", array(five),
						entry + ".withTheseArguments must contain only strings"),
				Arguments.of("argument with control character", command, "withTheseArguments",
						array(NODES.textNode("\u0001")),
						entry + ".withTheseArguments must not contain control characters"));
	}
}
