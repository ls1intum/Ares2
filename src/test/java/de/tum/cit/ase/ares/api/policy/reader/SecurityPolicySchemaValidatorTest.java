package de.tum.cit.ase.ares.api.policy.reader;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
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
import de.tum.cit.ase.ares.api.localization.AdviceActionWording;
import de.tum.cit.ase.ares.api.localization.Messages;
import de.tum.cit.ase.ares.api.policy.policySubComponents.ProgrammingLanguageConfiguration;

/**
 * Checks each rule of the policy schema validator on its own. Every rejection
 * case breaks exactly one rule of an otherwise valid policy and expects, in
 * English and in German, the catalogue message that names the field by its
 * short path, quotes the rejected value and says what to write.
 */
class SecurityPolicySchemaValidatorTest {

	/** The policy field that holds the supervised code settings. */
	private static final String SUPERVISED = "/regardingTheSupervisedCode";

	/** The policy field that holds the permitted resource accesses. */
	private static final String ACCESSES = SUPERVISED + "/theFollowingResourceAccessesArePermitted";

	/** The factory that builds the replacement values of the cases. */
	private static final JsonNodeFactory NODES = JsonNodeFactory.instance;

	/** The two languages every rejection is checked in. */
	private static final List<Locale> LOCALES = List.of(Locale.ENGLISH, Locale.GERMAN);

	/** Stands in an expected argument for the localised name of the top level. */
	private static final String ROOT = "<root>";

	/** Stands in an expected argument for the replacement value written as JSON. */
	private static final String VALUE = "<value>";

	/** The fields of a file permission, as the unknown-field message lists them. */
	private static final String FILE_FIELDS = "createAllFiles, deleteAllFiles, executeAllFiles, "
			+ "onThisPathAndAllPathsBelow, overwriteAllFiles, readAllFiles";

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
	 * Validates a tree that must be refused and returns the refusal's message.
	 *
	 * @param tree the policy tree to validate.
	 * @return the message of the refusal, without Jackson's location.
	 */
	private static String rejection(@Nullable JsonNode tree) {
		MismatchedInputException thrown = assertThrows(MismatchedInputException.class,
				() -> SecurityPolicySchemaValidator.validate(tree));
		assertInstanceOf(SecurityPolicySchemaValidator.PolicySchemaViolation.class, thrown);
		return thrown.getOriginalMessage();
	}

	/**
	 * Checks, in English and German, that the validator refuses the tree with the
	 * catalogue message of the key, and that the message uses short paths only.
	 *
	 * @param tree      the policy tree to validate.
	 * @param value     the rejected value as JSON, put in for {@link #VALUE}.
	 * @param key       the expected {@code policy.schema.*} key.
	 * @param arguments the expected arguments; {@link #ROOT} and {@link #VALUE} are
	 *                  replaced.
	 */
	private static void assertRejected(@Nullable JsonNode tree, String value, String key, Object... arguments) {
		for (Locale locale : LOCALES) {
			String actual = AdviceActionWording.inLocale(locale, () -> rejection(tree));
			String expected = AdviceActionWording
					.inLocale(locale,
							() -> Messages.localized(key,
									Arrays.stream(arguments)
											.map(argument -> ROOT.equals(argument)
													? Messages.localized("policy.schema.root")
													: VALUE.equals(argument) ? value : argument)
											.toArray()));
			assertEquals(expected, actual, locale.toString());
			assertFalse(actual.startsWith("!"), actual);
			assertFalse(actual.contains("$."), actual);
			assertFalse(actual.contains("regardingTheSupervisedCode."), actual);
			assertFalse(actual.contains("theFollowingResourceAccessesArePermitted."), actual);
			assertFalse(actual.contains("theFollowingTestBehaviorIsConfigured."), actual);
		}
	}

	/**
	 * Returns the expected message key and arguments for a version Ares does not
	 * support: "must be 1" while one version exists, a range once there are more.
	 *
	 * @param version the rejected version as written.
	 * @return the key followed by its arguments.
	 */
	private static Object[] unsupportedVersion(String version) {
		if (AresConstants.MINIMUM_POLICY_VERSION == AresConstants.MAXIMUM_POLICY_VERSION) {
			return new Object[] { "policy.schema.version.single", version,
					String.valueOf(AresConstants.MAXIMUM_POLICY_VERSION) };
		}
		return new Object[] { "policy.schema.version.range", version,
				String.valueOf(AresConstants.MINIMUM_POLICY_VERSION),
				String.valueOf(AresConstants.MAXIMUM_POLICY_VERSION) };
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
		assertRejected(NODES.textNode("a bare scalar"), "\"a bare scalar\"", "policy.schema.object", ROOT, VALUE);
	}

	/**
	 * Checks that a missing root is refused, not left to fail with a null pointer.
	 */
	@Test
	void rejectsAMissingRoot() {
		assertRejected(null, "null", "policy.schema.object", ROOT, VALUE);
	}

	/**
	 * Checks that an unknown field in the test behaviour object is refused with the
	 * hint that the object takes no fields yet.
	 */
	@Test
	void rejectsAnUnknownFieldInTheTestBehavior() {
		ObjectNode tree = validTree();
		ObjectNode behavior = NODES.objectNode();
		behavior.set("extra", NODES.textNode("x"));
		((ObjectNode) tree.at(SUPERVISED)).set("theFollowingTestBehaviorIsConfigured", behavior);
		assertRejected(tree, "", "policy.schema.unknown.field.none", "theFollowingTestBehaviorIsConfigured", "extra");
	}

	/** Checks that two unknown fields are named together, in order. */
	@Test
	void rejectsSeveralUnknownFieldsTogether() {
		ObjectNode tree = validTree();
		ObjectNode file = (ObjectNode) tree.at(ACCESSES + "/regardingFileSystemInteractions/0");
		file.set("readAllFile", NODES.booleanNode(true));
		file.set("deleteAllFile", NODES.booleanNode(true));
		assertRejected(tree, "", "policy.schema.unknown.fields", "regardingFileSystemInteractions[0]",
				"deleteAllFile, readAllFile", FILE_FIELDS);
	}

	/** Checks that two missing fields are named together, in order. */
	@Test
	void rejectsSeveralMissingFieldsTogether() {
		ObjectNode tree = validTree();
		ObjectNode network = (ObjectNode) tree.at(ACCESSES + "/regardingNetworkConnections/0");
		network.remove("sendData");
		network.remove("onThePort");
		assertRejected(tree, "", "policy.schema.missing.fields", "regardingNetworkConnections[0]",
				"onThePort, sendData");
	}

	/**
	 * Checks the English wording a typo in a file rule produces: the entry, the
	 * unknown field and every known field, so the right spelling is in sight.
	 */
	@Test
	void namesTheKnownFieldsOfAMisspelledFileRuleInEnglish() {
		ObjectNode tree = validTree();
		((ObjectNode) tree.at(ACCESSES + "/regardingFileSystemInteractions/0")).set("readAllFile",
				NODES.booleanNode(true));
		assertEquals(
				"regardingFileSystemInteractions[0] contains the unknown field readAllFile. Correct or remove "
						+ "it; the known fields are " + FILE_FIELDS + ".",
				AdviceActionWording.inLocale(Locale.ENGLISH, () -> rejection(tree)));
	}

	/** Checks the German wording of the same typo. */
	@Test
	void namesTheKnownFieldsOfAMisspelledFileRuleInGerman() {
		ObjectNode tree = validTree();
		((ObjectNode) tree.at(ACCESSES + "/regardingFileSystemInteractions/0")).set("readAllFile",
				NODES.booleanNode(true));
		assertEquals(
				"regardingFileSystemInteractions[0] enthält das unbekannte Feld readAllFile. Korrigieren oder "
						+ "entfernen Sie es; die bekannten Felder sind " + FILE_FIELDS + ".",
				AdviceActionWording.inLocale(Locale.GERMAN, () -> rejection(tree)));
	}

	/**
	 * Checks the English wording of a quoted flag: the YAML path, not an internal
	 * label, the value as written, and the fix.
	 */
	@Test
	void quotesAQuotedFlagWithItsPathInEnglish() {
		JsonNode tree = changed(ACCESSES + "/regardingFileSystemInteractions/0", "readAllFiles", NODES.textNode("yes"));
		assertEquals("regardingFileSystemInteractions[0].readAllFiles is \"yes\", but must be true or false, written "
				+ "without quotes.", AdviceActionWording.inLocale(Locale.ENGLISH, () -> rejection(tree)));
	}

	/**
	 * Checks that while Ares supports one policy version, the message names that
	 * version and how to set it, rather than a range from it to itself.
	 */
	@Test
	void namesTheOnlySupportedVersionWithoutADegenerateRange() {
		org.junit.jupiter.api.Assumptions.assumeTrue(
				AresConstants.MINIMUM_POLICY_VERSION == AresConstants.MAXIMUM_POLICY_VERSION,
				"Ares supports more than one policy version");
		JsonNode tree = changed("", "thisPolicyFileCompliesToThePolicyVersion", NODES.numberNode(7));
		String only = String.valueOf(AresConstants.MAXIMUM_POLICY_VERSION);
		String message = AdviceActionWording.inLocale(Locale.ENGLISH, () -> rejection(tree));
		assertEquals("thisPolicyFileCompliesToThePolicyVersion is 7, but must be " + only
				+ ". Set thisPolicyFileCompliesToThePolicyVersion: " + only + ".", message);
		assertFalse(message.contains("between"), message);
		assertFalse(message.contains("inclusive"), message);
	}

	/** Checks that an unknown configuration is quoted and the known ones listed. */
	@Test
	void listsTheKnownConfigurationsForAnUnknownOne() {
		JsonNode tree = changed(SUPERVISED, "theFollowingProgrammingLanguageConfigurationIsUsed",
				NODES.textNode("JAVA_USING_GRADLE_ARCHUNIT_AND_SPRINGAOP"));
		String message = AdviceActionWording.inLocale(Locale.ENGLISH, () -> rejection(tree));
		assertTrue(message.contains("\"JAVA_USING_GRADLE_ARCHUNIT_AND_SPRINGAOP\""), message);
		for (ProgrammingLanguageConfiguration configuration : ProgrammingLanguageConfiguration.values()) {
			assertTrue(message.contains(configuration.name()), message);
		}
	}

	/** Checks that a very long rejected value is cut short in the message. */
	@Test
	void cutsAVeryLongRejectedValueShort() {
		String longName = "1" + "a".repeat(200);
		JsonNode tree = changed(SUPERVISED, "theSupervisedCodeUsesTheFollowingPackage", NODES.textNode(longName));
		String quoted = NODES.textNode(longName).toString().substring(0, 100) + "...";
		assertRejected(tree, quoted, "policy.schema.package", "theSupervisedCodeUsesTheFollowingPackage", VALUE);
	}

	/**
	 * Checks that each broken rule is refused with the message of that rule.
	 *
	 * @param caseName  what is broken, used as the test name.
	 * @param tree      the policy tree with the defect.
	 * @param value     the rejected value as JSON, for {@link #VALUE}.
	 * @param key       the expected message key.
	 * @param arguments the expected arguments of that message.
	 */
	@ParameterizedTest(name = "{0}")
	@MethodSource("brokenRules")
	void rejectsEachBrokenRuleWithItsOwnMessage(String caseName, JsonNode tree, String value, String key,
			Object[] arguments) {
		assertRejected(tree, value, key, arguments);
	}

	/**
	 * Builds one case: a valid policy with one field changed.
	 *
	 * @param caseName      what is broken.
	 * @param parentPointer where the broken field sits.
	 * @param field         the broken field.
	 * @param replacement   the broken value, or null to remove the field.
	 * @param value         the rejected value as JSON, for {@link #VALUE}.
	 * @param keyAndArgs    the expected key followed by its arguments.
	 * @return the case.
	 */
	private static Arguments broken(String caseName, String parentPointer, String field, @Nullable JsonNode replacement,
			String value, Object... keyAndArgs) {
		return Arguments.of(caseName, changed(parentPointer, field, replacement), value, keyAndArgs[0],
				Arrays.copyOfRange(keyAndArgs, 1, keyAndArgs.length));
	}

	/**
	 * Lists the broken rules, each with one defect and the message it must give.
	 *
	 * @return the cases for the parameterised test.
	 */
	static Stream<Arguments> brokenRules() {
		String version = "thisPolicyFileCompliesToThePolicyVersion";
		String language = "theFollowingProgrammingLanguageConfigurationIsUsed";
		String packageField = "theSupervisedCodeUsesTheFollowingPackage";
		String mainClass = "theMainClassInsideThisPackageIs";
		String testClasses = "theFollowingClassesAreTestClasses";
		String resources = "theFollowingResourceAccessesArePermitted";
		String knownConfigurations = Arrays.stream(ProgrammingLanguageConfiguration.values()).map(Enum::name)
				.collect(Collectors.joining(", "));
		JsonNode text = NODES.textNode("x");
		JsonNode five = NODES.numberNode(5);
		JsonNode invalidName = NODES.textNode("1bad");
		JsonNode invalidCommand = NODES.textNode("echo ");
		String files = ACCESSES + "/regardingFileSystemInteractions/0";
		String network = ACCESSES + "/regardingNetworkConnections/0";
		String command = ACCESSES + "/regardingCommandExecutions/0";
		String file0 = "regardingFileSystemInteractions[0]";
		String network0 = "regardingNetworkConnections[0]";
		String thread0 = "regardingThreadCreations[0]";
		String import0 = "regardingPackageImports[0]";
		String command0 = "regardingCommandExecutions[0]";
		String arguments = command0 + ".withTheseArguments";
		return Stream.of(
				broken("root unknown field", "", "extra", text, "", "policy.schema.unknown.field", ROOT, "extra",
						"regardingTheSupervisedCode, " + version),
				broken("root missing field", "", version, null, "", "policy.schema.missing.field", ROOT, version),
				broken("root null field", "", "regardingTheSupervisedCode", NODES.nullNode(), "", "policy.schema.null",
						"regardingTheSupervisedCode"),
				broken("version decimal", "", version, NODES.numberNode(1.5d), "1.5", "policy.schema.integer", version,
						VALUE),
				broken("version beyond int", "", version, NODES.numberNode(3000000000L), "",
						unsupportedVersion("3000000000")),
				broken("version above range", "", version, NODES.numberNode(AresConstants.MAXIMUM_POLICY_VERSION + 1),
						"", unsupportedVersion(String.valueOf(AresConstants.MAXIMUM_POLICY_VERSION + 1))),
				broken("version below range", "", version, NODES.numberNode(AresConstants.MINIMUM_POLICY_VERSION - 1),
						"", unsupportedVersion(String.valueOf(AresConstants.MINIMUM_POLICY_VERSION - 1))),
				broken("supervised code not an object", "", "regardingTheSupervisedCode", text, "\"x\"",
						"policy.schema.object", "regardingTheSupervisedCode", VALUE),
				broken("supervised code unknown field", SUPERVISED, "extra", text, "", "policy.schema.unknown.field",
						"regardingTheSupervisedCode", "extra",
						String.join(", ", testClasses, language, resources, "theFollowingTestBehaviorIsConfigured",
								mainClass, packageField)),
				broken("configuration not text", SUPERVISED, language, five, "5", "policy.schema.text", language,
						VALUE),
				broken("configuration unknown", SUPERVISED, language, NODES.textNode("NO_SUCH_CONFIGURATION"),
						"\"NO_SUCH_CONFIGURATION\"", "policy.schema.configuration", language, VALUE,
						knownConfigurations),
				broken("package not text", SUPERVISED, packageField, five, "5", "policy.schema.text.optional",
						packageField, VALUE),
				broken("main class blank", SUPERVISED, mainClass, NODES.textNode("  "), "\"  \"",
						"policy.schema.text.optional", mainClass, VALUE),
				broken("package invalid", SUPERVISED, packageField, invalidName, "\"1bad\"", "policy.schema.package",
						packageField, VALUE),
				broken("main class invalid", SUPERVISED, mainClass, NODES.textNode("Main Class"), "\"Main Class\"",
						"policy.schema.main.class", mainClass, VALUE),
				broken("test classes not an array", SUPERVISED, testClasses, NODES.textNode("com.example.MainTest"),
						"\"com.example.MainTest\"", "policy.schema.text.list", testClasses, VALUE,
						"com.example.MainTest"),
				broken("test class not text", SUPERVISED, testClasses, array(five), "5", "policy.schema.text",
						testClasses + "[0]", VALUE),
				broken("test class blank", SUPERVISED, testClasses, array(NODES.textNode("  ")), "\"  \"",
						"policy.schema.text", testClasses + "[0]", VALUE),
				broken("test class invalid", SUPERVISED, testClasses, array(invalidName), "\"1bad\"",
						"policy.schema.test.class", testClasses + "[0]", VALUE),
				broken("resources not an object", SUPERVISED, resources, text, "\"x\"", "policy.schema.object",
						resources, VALUE),
				broken("file list not an array", ACCESSES, "regardingFileSystemInteractions", text, "\"x\"",
						"policy.schema.list", "regardingFileSystemInteractions", VALUE),
				broken("file entry not an object", ACCESSES, "regardingFileSystemInteractions", array(text), "\"x\"",
						"policy.schema.object", file0, VALUE),
				broken("network entry missing port", network, "onThePort", null, "", "policy.schema.missing.field",
						network0, "onThePort"),
				broken("file path not text", files, "onThisPathAndAllPathsBelow", five, "5", "policy.schema.text",
						file0 + ".onThisPathAndAllPathsBelow", VALUE),
				broken("file path blank", files, "onThisPathAndAllPathsBelow", NODES.textNode("  "), "\"  \"",
						"policy.schema.text", file0 + ".onThisPathAndAllPathsBelow", VALUE),
				broken("file path only a Unicode space", files, "onThisPathAndAllPathsBelow", NODES.textNode(" "),
						NODES.textNode(" ").toString(), "policy.schema.text", file0 + ".onThisPathAndAllPathsBelow",
						VALUE),
				broken("file path walks up", files, "onThisPathAndAllPathsBelow", NODES.textNode("../x"), "\"../x\"",
						"policy.schema.file.path", file0 + ".onThisPathAndAllPathsBelow", VALUE),
				broken("file flag quoted", files, "createAllFiles", NODES.textNode("true"), "\"true\"",
						"policy.schema.boolean", file0 + ".createAllFiles", VALUE),
				broken("host not text", network, "onTheHost", five, "5", "policy.schema.text", network0 + ".onTheHost",
						VALUE),
				broken("host invalid", network, "onTheHost", NODES.textNode("bad host"), "\"bad host\"",
						"policy.schema.host", network0 + ".onTheHost", VALUE),
				broken("port not integral", network, "onThePort", NODES.textNode("80"), "\"80\"",
						"policy.schema.integer", network0 + ".onThePort", VALUE),
				broken("thread class not text", ACCESSES + "/regardingThreadCreations/0", "ofThisClass", five, "5",
						"policy.schema.text", thread0 + ".ofThisClass", VALUE),
				broken("thread class invalid", ACCESSES + "/regardingThreadCreations/0", "ofThisClass", invalidName,
						"\"1bad\"", "policy.schema.thread.class", thread0 + ".ofThisClass", VALUE),
				broken("import not text", ACCESSES + "/regardingPackageImports/0", "importTheFollowingPackage", five,
						"5", "policy.schema.text", import0 + ".importTheFollowingPackage", VALUE),
				broken("import invalid", ACCESSES + "/regardingPackageImports/0", "importTheFollowingPackage",
						invalidName, "\"1bad\"", "policy.schema.package.import", import0 + ".importTheFollowingPackage",
						VALUE),
				broken("exactMatchOnly quoted", ACCESSES + "/regardingPackageImports/0", "exactMatchOnly",
						NODES.textNode("true"), "\"true\"", "policy.schema.boolean.optional",
						import0 + ".exactMatchOnly", VALUE),
				broken("timeout not integral", ACCESSES + "/regardingTimeouts/0", "timeout", NODES.textNode("30"),
						"\"30\"", "policy.schema.integer", "regardingTimeouts[0].timeout", VALUE),
				broken("commands not an array", ACCESSES, "regardingCommandExecutions", NODES.textNode("java"),
						"\"java\"", "policy.schema.list", "regardingCommandExecutions", VALUE),
				broken("bare command invalid", ACCESSES, "regardingCommandExecutions", array(invalidCommand),
						"\"echo \"", "policy.schema.command", command0, VALUE),
				broken("command entry neither text nor object", ACCESSES, "regardingCommandExecutions", array(five),
						"5", "policy.schema.object", command0, VALUE),
				broken("command name not text", command, "executeTheCommand", five, "5", "policy.schema.text",
						command0 + ".executeTheCommand", VALUE),
				broken("command name invalid", command, "executeTheCommand", invalidCommand, "\"echo \"",
						"policy.schema.command", command0 + ".executeTheCommand", VALUE),
				broken("arguments not an array", command, "withTheseArguments", NODES.textNode("--version"),
						"\"--version\"", "policy.schema.text.list", arguments, VALUE, "--version"),
				broken("argument not text", command, "withTheseArguments", array(five), "5", "policy.schema.text.any",
						arguments + "[0]", VALUE),
				broken("argument with control character", command, "withTheseArguments",
						array(NODES.textNode("\u0001")), NODES.textNode("\u0001").toString(),
						"policy.schema.command.argument", arguments + "[0]", VALUE));
	}
}
