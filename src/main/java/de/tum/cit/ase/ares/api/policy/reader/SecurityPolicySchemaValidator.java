package de.tum.cit.ase.ares.api.policy.reader;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;

import de.tum.cit.ase.ares.api.AresConstants;
import de.tum.cit.ase.ares.api.localization.Messages;
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
 * format. Each message names the field by its short path, such as
 * {@code regardingFileSystemInteractions[0].readAllFiles}, quotes the rejected
 * value and says what to write instead.
 *
 * @since 2.0.0
 * @author Markus Paulsen
 */
public final class SecurityPolicySchemaValidator {

	/** The fields allowed at the top level of a policy. */
	private static final Set<String> ROOT_FIELDS = Set.of("thisPolicyFileCompliesToThePolicyVersion",
			"regardingTheSupervisedCode");
	/** The fields allowed under regardingTheSupervisedCode. */
	private static final Set<String> SUPERVISED_CODE_FIELDS = Set.of(
			"theFollowingProgrammingLanguageConfigurationIsUsed", "theSupervisedCodeUsesTheFollowingPackage",
			"theMainClassInsideThisPackageIs", "theFollowingClassesAreTestClasses",
			"theFollowingResourceAccessesArePermitted", "theFollowingTestBehaviorIsConfigured");
	/** The fields a policy must state under regardingTheSupervisedCode. */
	private static final Set<String> SUPERVISED_CODE_REQUIRED_FIELDS = Set.of(
			"theFollowingProgrammingLanguageConfigurationIsUsed", "theFollowingClassesAreTestClasses",
			"theFollowingResourceAccessesArePermitted");
	/** The fields allowed, and required, under the permitted resource accesses. */
	private static final Set<String> RESOURCE_ACCESS_FIELDS = Set.of("regardingFileSystemInteractions",
			"regardingNetworkConnections", "regardingCommandExecutions", "regardingThreadCreations",
			"regardingPackageImports", "regardingTimeouts");
	/** The fields of one file permission. */
	private static final Set<String> FILE_FIELDS = Set.of("onThisPathAndAllPathsBelow", "readAllFiles",
			"overwriteAllFiles", "createAllFiles", "executeAllFiles", "deleteAllFiles");
	/** The fields of one network permission. */
	private static final Set<String> NETWORK_FIELDS = Set.of("onTheHost", "onThePort", "openConnections", "sendData",
			"receiveData");
	/** The fields of one command permission. */
	private static final Set<String> COMMAND_FIELDS = Set.of("executeTheCommand", "withTheseArguments");
	/** The fields of one thread permission. */
	private static final Set<String> THREAD_FIELDS = Set.of("createTheFollowingNumberOfThreads", "ofThisClass");
	/** The fields of one package permission. */
	private static final Set<String> PACKAGE_FIELDS = Set.of("importTheFollowingPackage", "exactMatchOnly");
	/**
	 * The package permission fields a policy must state; exactMatchOnly may be left
	 * out.
	 */
	private static final Set<String> PACKAGE_REQUIRED_FIELDS = Set.of("importTheFollowingPackage");
	/** The fields of one timeout permission. */
	private static final Set<String> TIMEOUT_FIELDS = Set.of("timeout");
	/** The fields allowed under theFollowingTestBehaviorIsConfigured; none yet. */
	private static final Set<String> TEST_BEHAVIOR_FIELDS = Set.of();
	/** The required fields that hold a list, so a null there gets the [] hint. */
	private static final Set<String> LIST_FIELDS = Set.of("theFollowingClassesAreTestClasses",
			"regardingFileSystemInteractions", "regardingNetworkConnections", "regardingCommandExecutions",
			"regardingThreadCreations", "regardingPackageImports", "regardingTimeouts", "withTheseArguments");
	/** The path prefix of a field written directly inside a wrapper object. */
	private static final String TOP = "";
	/**
	 * The longest rejected value, in code points as written in JSON, quoted in
	 * full; a longer one is cut short.
	 */
	private static final int QUOTED_VALUE_LIMIT = 100;
	/** How many code points of a cut value are kept from its beginning. */
	private static final int QUOTED_HEAD = 40;
	/**
	 * How many code points of a cut value are kept from its end, so head, dots and
	 * tail together stay within the limit.
	 */
	private static final int QUOTED_TAIL = QUOTED_VALUE_LIMIT - QUOTED_HEAD - 3;
	/** The highest port; 0, the lowest, means every port. */
	private static final long MAXIMUM_PORT = 65_535L;

	/** Prevents instances of this utility. */
	private SecurityPolicySchemaValidator() {
		throw new SecurityException(
				Messages.localized("security.general.utility.initialization", "SecurityPolicySchemaValidator"));
	}

	/**
	 * Validates a parsed policy tree.
	 * <p>
	 * The language configuration is identified first, so every language-specific
	 * name is checked with that language's rules, and an unknown configuration is
	 * rejected here rather than later during Jackson's enum binding.
	 *
	 * @param root the root of the parsed policy tree.
	 * @throws MismatchedInputException if the tree breaks the schema; it is a
	 *                                  {@link PolicySchemaViolation}.
	 */
	public static void validate(@Nonnull JsonNode root) throws MismatchedInputException {
		requireObject(root, Messages.localized("policy.schema.root"), TOP, ROOT_FIELDS, ROOT_FIELDS);
		validateVersion(root.get("thisPolicyFileCompliesToThePolicyVersion"));
		JsonNode supervisedCode = root.get("regardingTheSupervisedCode");
		requireObject(supervisedCode, "regardingTheSupervisedCode", TOP, SUPERVISED_CODE_FIELDS,
				SUPERVISED_CODE_REQUIRED_FIELDS);
		LanguageNameRules nameRules = validateSupervisedCode(supervisedCode);
		JsonNode resources = supervisedCode.get("theFollowingResourceAccessesArePermitted");
		requireObject(resources, "theFollowingResourceAccessesArePermitted", TOP, RESOURCE_ACCESS_FIELDS,
				RESOURCE_ACCESS_FIELDS);
		validateFilePermissions(resources.get("regardingFileSystemInteractions"));
		validateNetworkPermissions(resources.get("regardingNetworkConnections"));
		validateCommandArray(resources.get("regardingCommandExecutions"));
		validateThreadPermissions(resources.get("regardingThreadCreations"), nameRules);
		validatePackagePermissions(resources.get("regardingPackageImports"), nameRules);
		validateObjectArray(resources.get("regardingTimeouts"), "regardingTimeouts", TIMEOUT_FIELDS, TIMEOUT_FIELDS);
		int index = 0;
		for (JsonNode permission : resources.get("regardingTimeouts")) {
			requireWholeNumberWithin(permission, "timeout", entry("regardingTimeouts", index++), 1L, Long.MAX_VALUE,
					"policy.schema.timeout");
		}
		JsonNode testBehavior = supervisedCode.get("theFollowingTestBehaviorIsConfigured");
		if (testBehavior != null) {
			requireObject(testBehavior, "theFollowingTestBehaviorIsConfigured", TOP, TEST_BEHAVIOR_FIELDS, Set.of());
		}
	}

	/**
	 * Requires the policy version to be a whole number that Ares supports.
	 * <p>
	 * With a single supported version the message says "must be 1", not a range
	 * from 1 to 1.
	 *
	 * @param version the version node, already known to be present and not null.
	 * @throws MismatchedInputException if the version is not supported.
	 */
	private static void validateVersion(JsonNode version) throws MismatchedInputException {
		if (!version.isIntegralNumber()) {
			fail("policy.schema.integer", "thisPolicyFileCompliesToThePolicyVersion", quote(version));
		}
		boolean supported = version.canConvertToInt() && version.intValue() >= AresConstants.MINIMUM_POLICY_VERSION
				&& version.intValue() <= AresConstants.MAXIMUM_POLICY_VERSION;
		if (supported) {
			return;
		}
		if (AresConstants.MINIMUM_POLICY_VERSION == AresConstants.MAXIMUM_POLICY_VERSION) {
			fail("policy.schema.version.single", quote(version), String.valueOf(AresConstants.MAXIMUM_POLICY_VERSION));
		}
		fail("policy.schema.version.range", quote(version), String.valueOf(AresConstants.MINIMUM_POLICY_VERSION),
				String.valueOf(AresConstants.MAXIMUM_POLICY_VERSION));
	}

	/**
	 * Checks the language configuration, package, main class and test classes.
	 *
	 * @param supervisedCode the regardingTheSupervisedCode object.
	 * @return the name rules of the declared language configuration.
	 * @throws MismatchedInputException if one of them is invalid.
	 */
	private static LanguageNameRules validateSupervisedCode(JsonNode supervisedCode) throws MismatchedInputException {
		String language = "theFollowingProgrammingLanguageConfigurationIsUsed";
		requireText(supervisedCode, language, TOP);
		LanguageNameRules nameRules = languageRulesFor(supervisedCode.get(language));
		String packageField = "theSupervisedCodeUsesTheFollowingPackage";
		String mainClassField = "theMainClassInsideThisPackageIs";
		requireOptionalText(supervisedCode, packageField, TOP);
		requireOptionalText(supervisedCode, mainClassField, TOP);
		JsonNode packageNode = supervisedCode.get(packageField);
		if (packageNode != null && !packageNode.isNull() && !nameRules.matchesPackage(packageNode.textValue())) {
			fail("policy.schema.package", packageField, quote(packageNode));
		}
		JsonNode mainClassNode = supervisedCode.get(mainClassField);
		if (mainClassNode != null && !mainClassNode.isNull() && !nameRules.matchesTypeName(mainClassNode.textValue())) {
			fail("policy.schema.main.class", mainClassField, quote(mainClassNode));
		}
		String testClassesField = "theFollowingClassesAreTestClasses";
		JsonNode testClasses = supervisedCode.get(testClassesField);
		if (!testClasses.isArray()) {
			fail("policy.schema.text.list", testClassesField, quote(testClasses), "com.example.MainTest");
		}
		int index = 0;
		for (JsonNode testClass : testClasses) {
			String path = entry(testClassesField, index++);
			if (!testClass.isTextual() || testClass.textValue().isBlank()) {
				fail("policy.schema.text", path, quote(testClass));
			}
			if (!nameRules.matchesClassPath(testClass.textValue())) {
				fail("policy.schema.test.class", path, quote(testClass));
			}
		}
		return nameRules;
	}

	/**
	 * Returns the name rules of the language configuration with the given name.
	 *
	 * @param configurationName the configuration node written in the policy.
	 * @return that configuration's name rules.
	 * @throws MismatchedInputException if no configuration has that name; the
	 *                                  message lists the known ones.
	 */
	private static LanguageNameRules languageRulesFor(JsonNode configurationName) throws MismatchedInputException {
		for (ProgrammingLanguageConfiguration configuration : ProgrammingLanguageConfiguration.values()) {
			if (configuration.name().equals(configurationName.textValue())) {
				return configuration.nameRules();
			}
		}
		String known = Arrays.stream(ProgrammingLanguageConfiguration.values()).map(Enum::name)
				.collect(Collectors.joining(", "));
		throw violation("policy.schema.configuration", "theFollowingProgrammingLanguageConfigurationIsUsed",
				quote(configurationName), known);
	}

	/**
	 * Validates the file permissions.
	 *
	 * @param permissions the regardingFileSystemInteractions node.
	 * @throws MismatchedInputException if an entry is invalid.
	 */
	private static void validateFilePermissions(JsonNode permissions) throws MismatchedInputException {
		String list = "regardingFileSystemInteractions";
		validateObjectArray(permissions, list, FILE_FIELDS, FILE_FIELDS);
		int index = 0;
		for (JsonNode permission : permissions) {
			String path = entry(list, index++);
			requireText(permission, "onThisPathAndAllPathsBelow", path);
			requirePattern(permission, "onThisPathAndAllPathsBelow", path, PolicyValueValidator.FILE_PATH_PATTERN,
					"policy.schema.file.path");
			requireBooleans(permission, FILE_FIELDS, Set.of("onThisPathAndAllPathsBelow"), path);
		}
	}

	/**
	 * Validates the network permissions.
	 *
	 * @param permissions the regardingNetworkConnections node.
	 * @throws MismatchedInputException if an entry is invalid.
	 */
	private static void validateNetworkPermissions(JsonNode permissions) throws MismatchedInputException {
		String list = "regardingNetworkConnections";
		validateObjectArray(permissions, list, NETWORK_FIELDS, NETWORK_FIELDS);
		int index = 0;
		for (JsonNode permission : permissions) {
			String path = entry(list, index++);
			requireText(permission, "onTheHost", path);
			requirePattern(permission, "onTheHost", path, PolicyValueValidator.HOST_PATTERN, "policy.schema.host");
			requireWholeNumberWithin(permission, "onThePort", path, 0L, MAXIMUM_PORT, "policy.schema.port");
			requireBooleans(permission, NETWORK_FIELDS, Set.of("onTheHost", "onThePort"), path);
		}
	}

	/**
	 * Validates the thread permissions.
	 *
	 * @param permissions the regardingThreadCreations node.
	 * @param nameRules   the name rules of the declared language.
	 * @throws MismatchedInputException if an entry is invalid.
	 */
	private static void validateThreadPermissions(JsonNode permissions, LanguageNameRules nameRules)
			throws MismatchedInputException {
		String list = "regardingThreadCreations";
		validateObjectArray(permissions, list, THREAD_FIELDS, THREAD_FIELDS);
		int index = 0;
		for (JsonNode permission : permissions) {
			String path = entry(list, index++);
			requireWholeNumberWithin(permission, "createTheFollowingNumberOfThreads", path, 0L, Integer.MAX_VALUE,
					"policy.schema.thread.count");
			requireText(permission, "ofThisClass", path);
			JsonNode threadClass = permission.get("ofThisClass");
			if (!nameRules.matchesThreadConstruct(threadClass.textValue())) {
				fail("policy.schema.thread.class", child(path, "ofThisClass"), quote(threadClass));
			}
		}
	}

	/**
	 * Validates the package import permissions.
	 *
	 * @param permissions the regardingPackageImports node.
	 * @param nameRules   the name rules of the declared language.
	 * @throws MismatchedInputException if an entry is invalid.
	 */
	private static void validatePackagePermissions(JsonNode permissions, LanguageNameRules nameRules)
			throws MismatchedInputException {
		String list = "regardingPackageImports";
		validateObjectArray(permissions, list, PACKAGE_FIELDS, PACKAGE_REQUIRED_FIELDS);
		int index = 0;
		for (JsonNode permission : permissions) {
			String path = entry(list, index++);
			requireText(permission, "importTheFollowingPackage", path);
			requireOptionalBoolean(permission, "exactMatchOnly", path);
			JsonNode imported = permission.get("importTheFollowingPackage");
			if (!nameRules.matchesPackageImport(imported.textValue())) {
				fail("policy.schema.package.import", child(path, "importTheFollowingPackage"), quote(imported));
			}
		}
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
	 * @throws MismatchedInputException if an entry is invalid.
	 */
	private static void validateCommandArray(JsonNode commands) throws MismatchedInputException {
		String list = "regardingCommandExecutions";
		if (!commands.isArray()) {
			fail("policy.schema.list", list, quote(commands));
		}
		int index = 0;
		for (JsonNode command : commands) {
			String path = entry(list, index++);
			if (command.isTextual()) {
				if (!PolicyValueValidator.matches(command.textValue(), PolicyValueValidator.COMMAND_PATTERN)) {
					fail("policy.schema.command", path, quote(command));
				}
				continue;
			}
			requireObject(command, path, path, COMMAND_FIELDS, COMMAND_FIELDS);
			requireText(command, "executeTheCommand", path);
			requirePattern(command, "executeTheCommand", path, PolicyValueValidator.COMMAND_PATTERN,
					"policy.schema.command");
			validateCommandArguments(command.get("withTheseArguments"), child(path, "withTheseArguments"));
		}
	}

	/**
	 * Requires a list of argument texts; an empty or blank argument is allowed.
	 *
	 * @param arguments the withTheseArguments node.
	 * @param path      the short path of that node.
	 * @throws MismatchedInputException if it is not such a list.
	 */
	private static void validateCommandArguments(JsonNode arguments, String path) throws MismatchedInputException {
		if (!arguments.isArray()) {
			fail("policy.schema.text.list", path, quote(arguments), "--version");
		}
		int index = 0;
		for (JsonNode argument : arguments) {
			String argumentPath = entry(path, index++);
			if (!argument.isTextual()) {
				fail("policy.schema.text.any", argumentPath, quote(argument));
			}
			if (!PolicyValueValidator.matches(argument.textValue(), PolicyValueValidator.COMMAND_ARGUMENT_PATTERN)) {
				fail("policy.schema.command.argument", argumentPath, quote(argument));
			}
		}
	}

	/**
	 * Requires a list whose entries are objects with the given fields.
	 *
	 * @param node           the list node.
	 * @param path           the short path of the list.
	 * @param allowedFields  the fields an entry may have.
	 * @param requiredFields the fields an entry must have.
	 * @throws MismatchedInputException if the list or an entry is invalid.
	 */
	private static void validateObjectArray(JsonNode node, String path, Set<String> allowedFields,
			Set<String> requiredFields) throws MismatchedInputException {
		if (!node.isArray()) {
			fail("policy.schema.list", path, quote(node));
		}
		int index = 0;
		for (JsonNode element : node) {
			String entryPath = entry(path, index++);
			requireObject(element, entryPath, entryPath, allowedFields, requiredFields);
		}
	}

	/**
	 * Requires an object with only known fields, every required one set.
	 *
	 * @param node           the node to check; null counts as not an object.
	 * @param label          how the messages name the object.
	 * @param prefix         the path prefix of its fields, empty for a wrapper.
	 * @param allowedFields  the known fields; an unknown one is listed with them.
	 * @param requiredFields the fields that must be present and not null.
	 * @throws MismatchedInputException if the object breaks one of these rules.
	 */
	private static void requireObject(JsonNode node, String label, String prefix, Set<String> allowedFields,
			Set<String> requiredFields) throws MismatchedInputException {
		if (node == null || !node.isObject()) {
			fail("policy.schema.object", label, quote(node));
		}
		Set<String> actualFields = new HashSet<>();
		Iterator<String> fieldNames = node.fieldNames();
		fieldNames.forEachRemaining(actualFields::add);
		Set<String> unknownFields = new TreeSet<>(actualFields);
		unknownFields.removeAll(allowedFields);
		if (!unknownFields.isEmpty()) {
			failUnknownFields(label, unknownFields, allowedFields);
		}
		Set<String> missingFields = new TreeSet<>(requiredFields);
		missingFields.removeAll(actualFields);
		if (!missingFields.isEmpty()) {
			fail(missingFields.size() == 1 ? "policy.schema.missing.field" : "policy.schema.missing.fields", label,
					String.join(", ", missingFields));
		}
		for (String requiredField : new TreeSet<>(requiredFields)) {
			if (node.get(requiredField).isNull()) {
				fail(LIST_FIELDS.contains(requiredField) ? "policy.schema.null.list" : "policy.schema.null",
						child(prefix, requiredField));
			}
		}
	}

	/**
	 * Refuses unknown fields and lists the known ones, so a typo is easy to see.
	 *
	 * @param label         how the message names the object.
	 * @param unknownFields the unknown fields, sorted.
	 * @param allowedFields the known fields, possibly none.
	 * @throws MismatchedInputException always.
	 */
	private static void failUnknownFields(String label, Set<String> unknownFields, Set<String> allowedFields)
			throws MismatchedInputException {
		String unknown = String.join(", ", unknownFields);
		boolean one = unknownFields.size() == 1;
		if (allowedFields.isEmpty()) {
			fail(one ? "policy.schema.unknown.field.none" : "policy.schema.unknown.fields.none", label, unknown);
		}
		fail(one ? "policy.schema.unknown.field" : "policy.schema.unknown.fields", label, unknown,
				String.join(", ", new TreeSet<>(allowedFields)));
	}

	/**
	 * Accepts a field that is absent, null or a non-blank text.
	 *
	 * @param parent the object holding the field.
	 * @param field  the field name.
	 * @param path   the short path of the object.
	 * @throws MismatchedInputException if the field holds anything else.
	 */
	private static void requireOptionalText(JsonNode parent, String field, String path)
			throws MismatchedInputException {
		JsonNode node = parent.get(field);
		if (node != null && !node.isNull() && (!node.isTextual() || node.textValue().isBlank())) {
			fail("policy.schema.text.optional", child(path, field), quote(node));
		}
	}

	/**
	 * Requires a field to be a non-blank text.
	 *
	 * @param parent the object holding the field.
	 * @param field  the field name.
	 * @param path   the short path of the object.
	 * @throws MismatchedInputException if it is not.
	 */
	private static void requireText(JsonNode parent, String field, String path) throws MismatchedInputException {
		JsonNode node = parent.get(field);
		if (node == null || !node.isTextual() || node.textValue().isBlank()) {
			fail("policy.schema.text", child(path, field), quote(node));
		}
	}

	/**
	 * Requires a field to be a whole number.
	 *
	 * @param parent the object holding the field.
	 * @param field  the field name.
	 * @param path   the short path of the object.
	 * @throws MismatchedInputException if it is not.
	 */
	private static void requireIntegral(JsonNode parent, String field, String path) throws MismatchedInputException {
		JsonNode node = parent.get(field);
		if (node == null || !node.isIntegralNumber()) {
			fail("policy.schema.integer", child(path, field), quote(node));
		}
	}

	/**
	 * Requires a field to be a whole number within a range.
	 * <p>
	 * The whole-number check runs first, so a decimal such as 1.0 is refused as
	 * one. The range is checked on the exact value, so a huge number is never cut
	 * down to a small one that would pass.
	 *
	 * @param parent  the object holding the field.
	 * @param field   the field name.
	 * @param path    the short path of the object.
	 * @param minimum the lowest allowed value.
	 * @param maximum the highest allowed value.
	 * @param key     the message that names the range.
	 * @throws MismatchedInputException if it is not such a number.
	 */
	private static void requireWholeNumberWithin(JsonNode parent, String field, String path, long minimum, long maximum,
			String key) throws MismatchedInputException {
		requireIntegral(parent, field, path);
		JsonNode node = parent.get(field);
		if (!node.canConvertToLong() || node.longValue() < minimum || node.longValue() > maximum) {
			fail(key, child(path, field), quote(node));
		}
	}

	/**
	 * Requires every listed field but the excluded ones to be true or false.
	 *
	 * @param parent   the object holding the fields.
	 * @param fields   the fields to check.
	 * @param excluded the fields that are not booleans.
	 * @param path     the short path of the object.
	 * @throws MismatchedInputException if one is not a boolean.
	 */
	private static void requireBooleans(JsonNode parent, Set<String> fields, Set<String> excluded, String path)
			throws MismatchedInputException {
		for (String field : new TreeSet<>(fields)) {
			if (!excluded.contains(field) && !parent.get(field).isBoolean()) {
				fail("policy.schema.boolean", child(path, field), quote(parent.get(field)));
			}
		}
	}

	/**
	 * Accepts a field that is absent or a boolean, and refuses anything else,
	 * including null and a quoted {@code "true"}.
	 *
	 * @param parent the object holding the field
	 * @param field  the field name
	 * @param path   the short path of the object
	 * @throws MismatchedInputException if the field is present and not a boolean
	 */
	private static void requireOptionalBoolean(JsonNode parent, String field, String path)
			throws MismatchedInputException {
		JsonNode node = parent.get(field);
		if (node != null && !node.isBoolean()) {
			fail("policy.schema.boolean.optional", child(path, field), quote(node));
		}
	}

	/**
	 * Requires a field's text to fully match a validation pattern.
	 *
	 * @param parent  the object holding the field.
	 * @param field   the field name.
	 * @param path    the short path of the object.
	 * @param pattern the required pattern.
	 * @param key     the message that says in words what the pattern accepts.
	 * @throws MismatchedInputException if the text does not match.
	 */
	private static void requirePattern(JsonNode parent, String field, String path, Pattern pattern, String key)
			throws MismatchedInputException {
		JsonNode node = parent.get(field);
		if (!PolicyValueValidator.matches(node.textValue(), pattern)) {
			fail(key, child(path, field), quote(node));
		}
	}

	/**
	 * Joins an object's short path and one of its fields.
	 *
	 * @param path  the object's short path, empty inside a wrapper.
	 * @param field the field name.
	 * @return the field's short path.
	 */
	private static String child(String path, String field) {
		return path.isEmpty() ? field : path + "." + field;
	}

	/**
	 * Names one entry of a list, counting from 0.
	 *
	 * @param path  the list's short path.
	 * @param index the entry's position.
	 * @return the entry's short path, such as {@code regardingTimeouts[0]}.
	 */
	private static String entry(String path, int index) {
		return path + "[" + index + "]";
	}

	/**
	 * Writes a rejected value as it reads in JSON, a text in double quotes; a
	 * missing value as null. A very long value keeps its beginning and its end
	 * around three dots, cut between whole characters.
	 *
	 * @param node the value; may be null.
	 * @return the value for the message.
	 */
	private static String quote(@Nullable JsonNode node) {
		String written = node == null ? "null" : node.toString();
		if (written.codePointCount(0, written.length()) <= QUOTED_VALUE_LIMIT) {
			return written;
		}
		int headEnd = written.offsetByCodePoints(0, QUOTED_HEAD);
		int tailStart = written.offsetByCodePoints(written.length(), -QUOTED_TAIL);
		return written.substring(0, headEnd) + "..." + written.substring(tailStart);
	}

	/**
	 * Throws the violation for a message key and its arguments.
	 *
	 * @param key       the {@code policy.schema.*} key.
	 * @param arguments the arguments of that message.
	 * @throws MismatchedInputException always.
	 */
	private static void fail(String key, Object... arguments) throws MismatchedInputException {
		throw violation(key, arguments);
	}

	/**
	 * Creates the violation for a message key and its arguments, in the language of
	 * the current locale.
	 *
	 * @param key       the {@code policy.schema.*} key.
	 * @param arguments the arguments of that message.
	 * @return the violation.
	 */
	private static PolicySchemaViolation violation(String key, Object... arguments) {
		return new PolicySchemaViolation(Messages.localized(key, arguments));
	}

	/**
	 * The failure of a policy tree that breaks the schema.
	 * <p>
	 * A type of its own lets the reader tell it apart from Jackson's own failures,
	 * so it can put this message, which has no Ares prefix, into its first line.
	 */
	public static final class PolicySchemaViolation extends MismatchedInputException {

		/** Serialisation version, fixed so the class can be serialised safely. */
		private static final long serialVersionUID = 1L;

		/**
		 * Creates the violation.
		 *
		 * @param message the localised message; it carries no Ares prefix.
		 */
		PolicySchemaViolation(@Nonnull String message) {
			super((JsonParser) null, message, SecurityPolicy.class);
		}
	}
}
