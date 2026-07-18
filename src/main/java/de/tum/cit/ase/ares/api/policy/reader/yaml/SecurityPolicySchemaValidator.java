package de.tum.cit.ase.ares.api.policy.reader.yaml;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import javax.annotation.Nonnull;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;

import de.tum.cit.ase.ares.api.policy.SecurityPolicy;

/** Validates the security-policy YAML tree before Jackson record binding. */
final class SecurityPolicySchemaValidator {

	private static final Set<String> ROOT_FIELDS = Set.of("regardingTheSupervisedCode");
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

	static void validate(@Nonnull JsonNode root) throws MismatchedInputException {
		requireObject(root, "$", ROOT_FIELDS, ROOT_FIELDS);
		JsonNode supervisedCode = root.get("regardingTheSupervisedCode");
		requireObject(supervisedCode, "$.regardingTheSupervisedCode", SUPERVISED_CODE_FIELDS,
				Set.of("theFollowingProgrammingLanguageConfigurationIsUsed", "theFollowingClassesAreTestClasses",
						"theFollowingResourceAccessesArePermitted"));
		requireText(supervisedCode, "theFollowingProgrammingLanguageConfigurationIsUsed",
				"$.regardingTheSupervisedCode");
		requireOptionalText(supervisedCode, "theSupervisedCodeUsesTheFollowingPackage", "$.regardingTheSupervisedCode");
		requireOptionalText(supervisedCode, "theMainClassInsideThisPackageIs", "$.regardingTheSupervisedCode");
		requireTextArray(supervisedCode.get("theFollowingClassesAreTestClasses"),
				"$.regardingTheSupervisedCode.theFollowingClassesAreTestClasses");

		JsonNode resources = supervisedCode.get("theFollowingResourceAccessesArePermitted");
		requireObject(resources, "$.regardingTheSupervisedCode.theFollowingResourceAccessesArePermitted",
				RESOURCE_ACCESS_FIELDS, RESOURCE_ACCESS_FIELDS);
		validateObjectArray(resources.get("regardingFileSystemInteractions"), "regardingFileSystemInteractions",
				FILE_FIELDS, FILE_FIELDS);
		for (JsonNode permission : resources.get("regardingFileSystemInteractions")) {
			requireText(permission, "onThisPathAndAllPathsBelow", "file permission");
			requireBooleans(permission, FILE_FIELDS, Set.of("onThisPathAndAllPathsBelow"), "file permission");
		}
		validateObjectArray(resources.get("regardingNetworkConnections"), "regardingNetworkConnections", NETWORK_FIELDS,
				NETWORK_FIELDS);
		for (JsonNode permission : resources.get("regardingNetworkConnections")) {
			requireText(permission, "onTheHost", "network permission");
			requireIntegral(permission, "onThePort", "network permission");
			requireBooleans(permission, NETWORK_FIELDS, Set.of("onTheHost", "onThePort"), "network permission");
		}
		validateCommandArray(resources.get("regardingCommandExecutions"));
		validateObjectArray(resources.get("regardingThreadCreations"), "regardingThreadCreations", THREAD_FIELDS,
				THREAD_FIELDS);
		for (JsonNode permission : resources.get("regardingThreadCreations")) {
			requireIntegral(permission, "createTheFollowingNumberOfThreads", "thread permission");
			requireText(permission, "ofThisClass", "thread permission");
		}
		validateObjectArray(resources.get("regardingPackageImports"), "regardingPackageImports", PACKAGE_FIELDS,
				PACKAGE_FIELDS);
		for (JsonNode permission : resources.get("regardingPackageImports")) {
			requireText(permission, "importTheFollowingPackage", "package permission");
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
				continue;
			}
			requireObject(command, "command permission", COMMAND_FIELDS, COMMAND_FIELDS);
			requireText(command, "executeTheCommand", "command permission");
			requireTextArray(command.get("withTheseArguments"), "command permission.withTheseArguments");
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

	private static void fail(String message) throws MismatchedInputException {
		throw MismatchedInputException.from((JsonParser) null, SecurityPolicy.class, message);
	}
}
