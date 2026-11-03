package de.tum.cit.ase.ares.api.policy.reader;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.regex.Pattern;

import javax.annotation.Nonnull;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;

import de.tum.cit.ase.ares.api.AresConstants;
import de.tum.cit.ase.ares.api.policy.PolicyValueValidator;
import de.tum.cit.ase.ares.api.policy.SecurityPolicy;
import de.tum.cit.ase.ares.api.policy.policySubComponents.LanguageNameRules;
import de.tum.cit.ase.ares.api.policy.policySubComponents.ProgrammingLanguageConfiguration;

/**
 * Validates the parsed policy tree before Jackson binds it to records.
 * <p>
 * Description: Checks the fields and the type and format of each value.
 * Language-specific names are checked with the {@link LanguageNameRules} of the
 * declared {@link ProgrammingLanguageConfiguration}.
 * <p>
 * Design Rationale: Taking a {@link JsonNode} keeps it independent of the file
 * format. Public only for the readers in the sub-packages.
 *
 * @since 2.0.0
 * @author Markus Paulsen
 */
public final class SecurityPolicySchemaValidator {

	private static final Set<String> ROOT_FIELDS = Set.of("thisPolicyFileCompliesToThePolicyVersion",
			"regardingTheSupervisedCode");
	private static final Set<String> SUPERVISED_CODE_FIELDS = Set.of(
			"theFollowingProgrammingLanguageConfigurationIsUsed", "theSupervisedCodeUsesTheFollowingPackage",
			"theMainClassInsideThisPackageIs", "theFollowingClassesAreTestClasses",
			"theFollowingResourceAccessesArePermitted", "theFollowingTestBehaviorIsConfigured");
	private static final Set<String> RESOURCE_ACCESS_FIELDS = Set.of("regardingFileSystemInteractions",
			"regardingNetworkConnections", "regardingCommandExecutions", "regardingThreadCreations",
			"regardingPackageImports", "regardingTimeouts");
	private static final Set<String> FILE_FIELDS = Set.of("onThisPathAndAllPathsBelow", "readAllFiles",
			"overwriteAllFiles", "createAllFiles", "executeAllFiles", "deleteAllFiles");
	private static final Set<String> NETWORK_FIELDS = Set.of("onTheHost", "onThePort", "openConnections", "sendData",
			"receiveData");
	private static final Set<String> COMMAND_FIELDS = Set.of("executeTheCommand", "withTheseArguments");
	private static final Set<String> THREAD_FIELDS = Set.of("createTheFollowingNumberOfThreads", "ofThisClass");
	private static final Set<String> PACKAGE_FIELDS = Set.of("importTheFollowingPackage");
	private static final Set<String> TIMEOUT_FIELDS = Set.of("timeout");
	private static final Set<String> TEST_BEHAVIOR_FIELDS = Set.of();

	private SecurityPolicySchemaValidator() {
		throw new UnsupportedOperationException("SecurityPolicySchemaValidator is a utility class");
	}

	/**
	 * Validates a parsed policy tree.
	 * <p>
	 * The language configuration is identified first, so every language-specific
	 * name is checked with that language's rules, and an unknown configuration is
	 * rejected here rather than later during Jackson's enum binding.
	 *
	 * @param root the root of the parsed policy tree.
	 * @throws MismatchedInputException if the tree breaks the schema.
	 */
	public static void validate(@Nonnull JsonNode root) throws MismatchedInputException {
		requireObject(root, "$", ROOT_FIELDS, ROOT_FIELDS);
		requireIntegral(root, "thisPolicyFileCompliesToThePolicyVersion", "$");
		JsonNode policyVersion = root.get("thisPolicyFileCompliesToThePolicyVersion");
		if (!policyVersion.canConvertToInt()) {
			fail("$.thisPolicyFileCompliesToThePolicyVersion must fit into an int");
		}
		int declaredPolicyVersion = policyVersion.intValue();
		if (declaredPolicyVersion < AresConstants.MINIMUM_POLICY_VERSION
				|| declaredPolicyVersion > AresConstants.MAXIMUM_POLICY_VERSION) {
			fail("$.thisPolicyFileCompliesToThePolicyVersion must be between " + AresConstants.MINIMUM_POLICY_VERSION
					+ " and " + AresConstants.MAXIMUM_POLICY_VERSION + " (inclusive), but was "
					+ declaredPolicyVersion);
		}
		JsonNode supervisedCode = root.get("regardingTheSupervisedCode");
		requireObject(supervisedCode, "$.regardingTheSupervisedCode", SUPERVISED_CODE_FIELDS,
				Set.of("theFollowingProgrammingLanguageConfigurationIsUsed", "theFollowingClassesAreTestClasses",
						"theFollowingResourceAccessesArePermitted"));
		requireText(supervisedCode, "theFollowingProgrammingLanguageConfigurationIsUsed",
				"$.regardingTheSupervisedCode");
		LanguageNameRules nameRules = languageRulesFor(
				supervisedCode.get("theFollowingProgrammingLanguageConfigurationIsUsed").textValue());
		requireOptionalText(supervisedCode, "theSupervisedCodeUsesTheFollowingPackage", "$.regardingTheSupervisedCode");
		requireOptionalText(supervisedCode, "theMainClassInsideThisPackageIs", "$.regardingTheSupervisedCode");
		JsonNode packageNode = supervisedCode.get("theSupervisedCodeUsesTheFollowingPackage");
		if (packageNode != null && !packageNode.isNull() && !nameRules.matchesPackage(packageNode.textValue())) {
			fail("$.regardingTheSupervisedCode.theSupervisedCodeUsesTheFollowingPackage"
					+ " must be a valid package name or null");
		}
		JsonNode mainClassNode = supervisedCode.get("theMainClassInsideThisPackageIs");
		if (mainClassNode != null && !mainClassNode.isNull() && !nameRules.matchesTypeName(mainClassNode.textValue())) {
			fail("$.regardingTheSupervisedCode.theMainClassInsideThisPackageIs must be a valid class name or null");
		}
		requireTextArray(supervisedCode.get("theFollowingClassesAreTestClasses"),
				"$.regardingTheSupervisedCode.theFollowingClassesAreTestClasses");
		for (JsonNode testClass : supervisedCode.get("theFollowingClassesAreTestClasses")) {
			if (!nameRules.matchesClassPath(testClass.textValue())) {
				fail("$.regardingTheSupervisedCode.theFollowingClassesAreTestClasses entries"
						+ " must be valid class names");
			}
		}

		JsonNode resources = supervisedCode.get("theFollowingResourceAccessesArePermitted");
		requireObject(resources, "$.regardingTheSupervisedCode.theFollowingResourceAccessesArePermitted",
				RESOURCE_ACCESS_FIELDS, RESOURCE_ACCESS_FIELDS);
		validateObjectArray(resources.get("regardingFileSystemInteractions"), "regardingFileSystemInteractions",
				FILE_FIELDS, FILE_FIELDS);
		for (JsonNode permission : resources.get("regardingFileSystemInteractions")) {
			requireText(permission, "onThisPathAndAllPathsBelow", "file permission");
			requirePattern(permission, "onThisPathAndAllPathsBelow", "file permission",
					PolicyValueValidator.FILE_PATH_PATTERN);
			requireBooleans(permission, FILE_FIELDS, Set.of("onThisPathAndAllPathsBelow"), "file permission");
		}
		validateObjectArray(resources.get("regardingNetworkConnections"), "regardingNetworkConnections", NETWORK_FIELDS,
				NETWORK_FIELDS);
		for (JsonNode permission : resources.get("regardingNetworkConnections")) {
			requireText(permission, "onTheHost", "network permission");
			requirePattern(permission, "onTheHost", "network permission", PolicyValueValidator.HOST_PATTERN);
			requireIntegral(permission, "onThePort", "network permission");
			requireBooleans(permission, NETWORK_FIELDS, Set.of("onTheHost", "onThePort"), "network permission");
		}
		validateCommandArray(resources.get("regardingCommandExecutions"));
		validateObjectArray(resources.get("regardingThreadCreations"), "regardingThreadCreations", THREAD_FIELDS,
				THREAD_FIELDS);
		for (JsonNode permission : resources.get("regardingThreadCreations")) {
			requireIntegral(permission, "createTheFollowingNumberOfThreads", "thread permission");
			requireText(permission, "ofThisClass", "thread permission");
			if (!nameRules.matchesThreadConstruct(permission.get("ofThisClass").textValue())) {
				fail("thread permission.ofThisClass must be a valid thread construct");
			}
		}
		validateObjectArray(resources.get("regardingPackageImports"), "regardingPackageImports", PACKAGE_FIELDS,
				PACKAGE_FIELDS);
		for (JsonNode permission : resources.get("regardingPackageImports")) {
			requireText(permission, "importTheFollowingPackage", "package permission");
			if (!nameRules.matchesPackageImport(permission.get("importTheFollowingPackage").textValue())) {
				fail("package permission.importTheFollowingPackage must be a valid package name or *");
			}
		}
		validateObjectArray(resources.get("regardingTimeouts"), "regardingTimeouts", TIMEOUT_FIELDS, TIMEOUT_FIELDS);
		for (JsonNode permission : resources.get("regardingTimeouts")) {
			requireIntegral(permission, "timeout", "timeout permission");
		}

		JsonNode testBehavior = supervisedCode.get("theFollowingTestBehaviorIsConfigured");
		if (testBehavior != null) {
			requireObject(testBehavior, "$.regardingTheSupervisedCode.theFollowingTestBehaviorIsConfigured",
					TEST_BEHAVIOR_FIELDS, Set.of());
		}
	}

	/**
	 * Returns the name rules of the language configuration with the given name.
	 *
	 * @param configurationName the configuration name written in the policy.
	 * @return that configuration's name rules.
	 * @throws MismatchedInputException if no configuration has that name.
	 */
	private static LanguageNameRules languageRulesFor(String configurationName) throws MismatchedInputException {
		for (ProgrammingLanguageConfiguration configuration : ProgrammingLanguageConfiguration.values()) {
			if (configuration.name().equals(configurationName)) {
				return configuration.nameRules();
			}
		}
		throw MismatchedInputException.from((JsonParser) null, SecurityPolicy.class,
				"$.regardingTheSupervisedCode.theFollowingProgrammingLanguageConfigurationIsUsed"
						+ " must be a known configuration");
	}

	/**
	 * Validates the command permissions.
	 * <p>
	 * An entry may be a bare string, meaning that command with no arguments. The
	 * form is deprecated on {@code CommandPermission} but part of policy format
	 * version 1, so it is accepted and held to the same command pattern. A written
	 * null is rejected by {@link #requireObject}. Arguments may be empty strings,
	 * which the argument pattern accepts.
	 *
	 * @param commands the command permissions array.
	 */
	private static void validateCommandArray(JsonNode commands) throws MismatchedInputException {
		if (commands == null || !commands.isArray()) {
			fail("regardingCommandExecutions must be an array");
		}
		for (JsonNode command : commands) {
			if (command.isTextual()) {
				if (!PolicyValueValidator.matches(command.textValue(), PolicyValueValidator.COMMAND_PATTERN)) {
					fail("regardingCommandExecutions entry must match "
							+ PolicyValueValidator.COMMAND_PATTERN.pattern());
				}
				continue;
			}
			requireObject(command, "regardingCommandExecutions entry", COMMAND_FIELDS, COMMAND_FIELDS);
			requireText(command, "executeTheCommand", "regardingCommandExecutions entry");
			requirePattern(command, "executeTheCommand", "regardingCommandExecutions entry",
					PolicyValueValidator.COMMAND_PATTERN);
			requireTextArrayAllowingBlankEntries(command.get("withTheseArguments"),
					"regardingCommandExecutions entry.withTheseArguments");
			for (JsonNode argument : command.get("withTheseArguments")) {
				if (!PolicyValueValidator.matches(argument.textValue(),
						PolicyValueValidator.COMMAND_ARGUMENT_PATTERN)) {
					fail("regardingCommandExecutions entry.withTheseArguments must not contain control characters");
				}
			}
		}
	}

	private static void validateObjectArray(JsonNode node, String path, Set<String> allowedFields,
			Set<String> requiredFields) throws MismatchedInputException {
		if (node == null || !node.isArray()) {
			fail(path + " must be an array");
		}
		for (JsonNode element : node) {
			requireObject(element, path + " entry", allowedFields, requiredFields);
		}
	}

	private static void requireObject(JsonNode node, String path, Set<String> allowedFields, Set<String> requiredFields)
			throws MismatchedInputException {
		if (node == null || !node.isObject()) {
			fail(path + " must be a non-null object");
		}
		Set<String> actualFields = new HashSet<>();
		Iterator<String> fieldNames = node.fieldNames();
		fieldNames.forEachRemaining(actualFields::add);
		Set<String> unknownFields = new HashSet<>(actualFields);
		unknownFields.removeAll(allowedFields);
		if (!unknownFields.isEmpty()) {
			fail(path + " contains unknown fields " + unknownFields);
		}
		Set<String> missingFields = new HashSet<>(requiredFields);
		missingFields.removeAll(actualFields);
		if (!missingFields.isEmpty()) {
			fail(path + " is missing required fields " + missingFields);
		}
		for (String requiredField : requiredFields) {
			if (node.get(requiredField).isNull()) {
				fail(path + "." + requiredField + " must not be null");
			}
		}
	}

	private static void requireOptionalText(JsonNode parent, String field, String path)
			throws MismatchedInputException {
		JsonNode node = parent.get(field);
		if (node != null && !node.isNull() && (!node.isTextual() || node.textValue().isBlank())) {
			fail(path + "." + field + " must be a non-blank string or null");
		}
	}

	private static void requireText(JsonNode parent, String field, String path) throws MismatchedInputException {
		JsonNode node = parent.get(field);
		if (node == null || !node.isTextual() || node.textValue().isBlank()) {
			fail(path + "." + field + " must be a non-blank string");
		}
	}

	private static void requireIntegral(JsonNode parent, String field, String path) throws MismatchedInputException {
		JsonNode node = parent.get(field);
		if (node == null || !node.isIntegralNumber()) {
			fail(path + "." + field + " must be an integer");
		}
	}

	private static void requireBooleans(JsonNode parent, Set<String> fields, Set<String> excluded, String path)
			throws MismatchedInputException {
		for (String field : fields) {
			if (!excluded.contains(field) && !parent.get(field).isBoolean()) {
				fail(path + "." + field + " must be a boolean");
			}
		}
	}

	private static void requireTextArray(JsonNode node, String path) throws MismatchedInputException {
		if (node == null || !node.isArray()) {
			fail(path + " must be an array of strings");
		}
		for (JsonNode element : node) {
			if (!element.isTextual() || element.textValue().isBlank()) {
				fail(path + " must contain only non-blank strings");
			}
		}
	}

	/**
	 * Requires an array whose entries are all strings, empty or blank ones
	 * included.
	 *
	 * @param node the node to check.
	 * @param path the field path used in the failure message.
	 * @throws MismatchedInputException if the node is not such an array.
	 */
	private static void requireTextArrayAllowingBlankEntries(JsonNode node, String path)
			throws MismatchedInputException {
		if (node == null || !node.isArray()) {
			fail(path + " must be an array of strings");
		}
		for (JsonNode element : node) {
			if (!element.isTextual()) {
				fail(path + " must contain only strings");
			}
		}
	}

	/**
	 * Requires a field's text to fully match a validation pattern.
	 *
	 * @param parent  the object holding the field.
	 * @param field   the field name.
	 * @param path    the path used in the failure message.
	 * @param pattern the required pattern.
	 * @throws MismatchedInputException if the text does not match.
	 */
	private static void requirePattern(JsonNode parent, String field, String path, Pattern pattern)
			throws MismatchedInputException {
		if (!PolicyValueValidator.matches(parent.get(field).textValue(), pattern)) {
			fail(path + "." + field + " must match " + pattern.pattern());
		}
	}

	private static void fail(String message) throws MismatchedInputException {
		throw MismatchedInputException.from((JsonParser) null, SecurityPolicy.class, message);
	}
}
