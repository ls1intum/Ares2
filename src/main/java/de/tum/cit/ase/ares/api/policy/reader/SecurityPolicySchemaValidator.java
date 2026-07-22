package de.tum.cit.ase.ares.api.policy.reader;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.regex.Pattern;

import javax.annotation.Nonnull;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;

import de.tum.cit.ase.ares.api.policy.PolicyValueValidator;
import de.tum.cit.ase.ares.api.policy.SecurityPolicy;

/**
 * Validates the parsed policy tree before Jackson binds it to records.
 * <p>
 * Description: Checks the shape of the tree, not the syntax of the file it came
 * from: the required and permitted fields, the type of each value, and the
 * format of each value against the patterns in {@link PolicyValueValidator}.
 * Failures are reported as a JSON path, so a policy author is pointed at the
 * field rather than at the parser.
 * <p>
 * Design Rationale: This class is deliberately not tied to one file format. Its
 * input is a Jackson {@link JsonNode}, which is the same tree whether the file
 * was YAML, JSON or anything else Jackson can parse, and everything
 * format-specific lives in the reader that produces that tree, currently the
 * mapper configured by {@code SecurityPolicyYAMLReader}. A second format would
 * therefore reuse this class unchanged, which is why the validation is not
 * expressed as an interface with one implementation per format: there is
 * nothing here that varies by format.
 * <p>
 * It is public only so that the format-specific readers in the sub-packages can
 * reach it. Treat it as internal to the policy reader.
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
			"theFollowingResourceAccessesArePermitted");
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

	private SecurityPolicySchemaValidator() {
		throw new UnsupportedOperationException("SecurityPolicySchemaValidator is a utility class");
	}

	public static void validate(@Nonnull JsonNode root) throws MismatchedInputException {
		requireObject(root, "$", ROOT_FIELDS, ROOT_FIELDS);
		requireIntegral(root, "thisPolicyFileCompliesToThePolicyVersion", "$");
		JsonNode policyVersion = root.get("thisPolicyFileCompliesToThePolicyVersion");
		if (!policyVersion.canConvertToInt()) {
			fail("$.thisPolicyFileCompliesToThePolicyVersion must fit into an int");
		}
		int declaredPolicyVersion = policyVersion.intValue();
		if (declaredPolicyVersion < SecurityPolicy.MINIMUM_POLICY_VERSION
				|| declaredPolicyVersion > SecurityPolicy.MAXIMUM_POLICY_VERSION) {
			fail("$.thisPolicyFileCompliesToThePolicyVersion must be between " + SecurityPolicy.MINIMUM_POLICY_VERSION
					+ " and " + SecurityPolicy.MAXIMUM_POLICY_VERSION + " (inclusive), but was "
					+ declaredPolicyVersion);
		}
		JsonNode supervisedCode = root.get("regardingTheSupervisedCode");
		requireObject(supervisedCode, "$.regardingTheSupervisedCode", SUPERVISED_CODE_FIELDS,
				Set.of("theFollowingProgrammingLanguageConfigurationIsUsed", "theFollowingClassesAreTestClasses",
						"theFollowingResourceAccessesArePermitted"));
		requireText(supervisedCode, "theFollowingProgrammingLanguageConfigurationIsUsed",
				"$.regardingTheSupervisedCode");
		requirePattern(supervisedCode, "theFollowingProgrammingLanguageConfigurationIsUsed",
				"$.regardingTheSupervisedCode", PolicyValueValidator.PROGRAMMING_LANGUAGE_CONFIGURATION_PATTERN);
		requireOptionalText(supervisedCode, "theSupervisedCodeUsesTheFollowingPackage", "$.regardingTheSupervisedCode");
		requireOptionalText(supervisedCode, "theMainClassInsideThisPackageIs", "$.regardingTheSupervisedCode");
		requireOptionalPattern(supervisedCode, "theSupervisedCodeUsesTheFollowingPackage",
				"$.regardingTheSupervisedCode", PolicyValueValidator.JAVA_PACKAGE_PATTERN);
		requireOptionalPattern(supervisedCode, "theMainClassInsideThisPackageIs", "$.regardingTheSupervisedCode",
				PolicyValueValidator.JAVA_CLASS_NAME_PATTERN);
		requireTextArray(supervisedCode.get("theFollowingClassesAreTestClasses"),
				"$.regardingTheSupervisedCode.theFollowingClassesAreTestClasses",
				PolicyValueValidator.JAVA_CLASS_PATH_PATTERN);

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
			requirePattern(permission, "ofThisClass", "thread permission", PolicyValueValidator.THREAD_CLASS_PATTERN);
		}
		validateObjectArray(resources.get("regardingPackageImports"), "regardingPackageImports", PACKAGE_FIELDS,
				PACKAGE_FIELDS);
		for (JsonNode permission : resources.get("regardingPackageImports")) {
			requireText(permission, "importTheFollowingPackage", "package permission");
			if (!PolicyValueValidator.matchesPackageImport(permission.get("importTheFollowingPackage").textValue())) {
				fail("package permission.importTheFollowingPackage must match "
						+ PolicyValueValidator.JAVA_PACKAGE_PATTERN.pattern() + " or *");
			}
		}
		validateObjectArray(resources.get("regardingTimeouts"), "regardingTimeouts", TIMEOUT_FIELDS, TIMEOUT_FIELDS);
		for (JsonNode permission : resources.get("regardingTimeouts")) {
			requireIntegral(permission, "timeout", "timeout permission");
		}
	}

	private static void validateCommandArray(JsonNode commands) throws MismatchedInputException {
		if (commands == null || !commands.isArray()) {
			fail("regardingCommandExecutions must be an array");
		}
		for (JsonNode command : commands) {
			if (command.isTextual()) {
				if (command.textValue().isBlank()) {
					fail("A bare command permission must not be blank");
				}
				if (!PolicyValueValidator.matches(command.textValue(), PolicyValueValidator.COMMAND_PATTERN)) {
					fail("A bare command permission must not contain control characters");
				}
				continue;
			}
			requireObject(command, "regardingCommandExecutions entry", COMMAND_FIELDS, COMMAND_FIELDS);
			requireText(command, "executeTheCommand", "regardingCommandExecutions entry");
			requirePattern(command, "executeTheCommand", "regardingCommandExecutions entry",
					PolicyValueValidator.COMMAND_PATTERN);
			requireTextArray(command.get("withTheseArguments"), "regardingCommandExecutions entry.withTheseArguments");
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
		requireTextArray(node, path, null);
	}

	private static void requireTextArray(JsonNode node, String path, Pattern pattern) throws MismatchedInputException {
		if (node == null || !node.isArray()) {
			fail(path + " must be an array of strings");
		}
		for (JsonNode element : node) {
			if (!element.isTextual() || element.textValue().isBlank()) {
				fail(path + " must contain only non-blank strings");
			}
			if (pattern != null && !PolicyValueValidator.matches(element.textValue(), pattern)) {
				fail(path + " entries must match " + pattern.pattern());
			}
		}
	}

	private static void requirePattern(JsonNode parent, String field, String path, Pattern pattern)
			throws MismatchedInputException {
		if (!PolicyValueValidator.matches(parent.get(field).textValue(), pattern)) {
			fail(path + "." + field + " must match " + pattern.pattern());
		}
	}

	private static void requireOptionalPattern(JsonNode parent, String field, String path, Pattern pattern)
			throws MismatchedInputException {
		JsonNode node = parent.get(field);
		if (node != null && !node.isNull() && !PolicyValueValidator.matches(node.textValue(), pattern)) {
			fail(path + "." + field + " must match " + pattern.pattern() + " or be null");
		}
	}

	private static void fail(String message) throws MismatchedInputException {
		throw MismatchedInputException.from((JsonParser) null, SecurityPolicy.class, message);
	}
}
