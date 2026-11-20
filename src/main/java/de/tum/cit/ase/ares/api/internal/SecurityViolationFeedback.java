package de.tum.cit.ase.ares.api.internal;

import static de.tum.cit.ase.ares.api.localization.Messages.localized;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

import de.tum.cit.ase.ares.api.policy.policySubComponents.SecurityViolationFeedbackLevel;

/**
 * Renders an action Ares blocked at the feedback level the policy chooses:
 * {@code HIGH} keeps the message, {@code MEDIUM} and {@code LOW} shorten it,
 * and {@code DEBUG} adds the policy entry that would allow it. Only the
 * violation classes of Ares's own checks are recognised, loaded by Ares or, for
 * the instrumentation copy, by the boot loader.
 */
@API(status = Status.INTERNAL)
public final class SecurityViolationFeedback {

	/** The violation classes, one per checking package. */
	private static final Set<String> VIOLATION_CLASSES = Set.of(
			"de.tum.cit.ase.ares.api.aop.java.aspectj.adviceandpointcut.AresSecurityViolation",
			"de.tum.cit.ase.ares.api.aop.java.instrumentation.advice.AresSecurityViolation",
			"de.tum.cit.ase.ares.api.architecture.java.AresSecurityViolation");

	/** The permission flags of a file entry, in the order the manual lists them. */
	private static final List<String> FILE_FLAGS = List.of("readAllFiles", "overwriteAllFiles", "createAllFiles",
			"executeAllFiles", "deleteAllFiles");

	/**
	 * The permission flags of a network entry, in the order the manual lists them.
	 */
	private static final List<String> NETWORK_FLAGS = List.of("openConnections", "sendData", "receiveData");

	/** Not instantiable. */
	private SecurityViolationFeedback() {
	}

	/**
	 * Returns the blocked action a throwable reports, if it is a violation of
	 * Ares's own checks.
	 *
	 * @param throwable the failure.
	 * @return the violation; empty for any other throwable.
	 */
	public static Optional<Violation> violationOf(Throwable throwable) {
		Class<?> type = throwable.getClass();
		ClassLoader loader = type.getClassLoader();
		if (!VIOLATION_CLASSES.contains(type.getName())
				|| loader != null && loader != SecurityViolationFeedback.class.getClassLoader()) {
			return Optional.empty();
		}
		try {
			return Optional.of(new Violation(call(throwable, "kind"), call(throwable, "action"),
					call(throwable, "target"), String.valueOf(throwable.getMessage())));
		} catch (ReflectiveOperationException unreadable) {
			return Optional.empty();
		}
	}

	/**
	 * Renders a violation at a level, for a policy built in code or whose file name
	 * is not known.
	 *
	 * @param violation the violation.
	 * @param level     the level.
	 * @return the text the student sees.
	 */
	public static String render(Violation violation, SecurityViolationFeedbackLevel level) {
		return render(violation, level, Optional.empty());
	}

	/**
	 * Renders a violation at a level: {@code HIGH} keeps the message,
	 * {@code MEDIUM} names only the kind of action, {@code LOW} only that a policy
	 * was violated, and {@code DEBUG} adds the policy entry that would allow it.
	 *
	 * @param violation  the violation.
	 * @param level      the level.
	 * @param policyFile the name of the policy file, if known.
	 * @return the text the student sees.
	 */
	public static String render(Violation violation, SecurityViolationFeedbackLevel level,
			Optional<String> policyFile) {
		return switch (level) {
		case HIGH -> violation.message();
		case MEDIUM -> phraseOf(violation).map(phrase -> localized("reporting.security.violation.medium", phrase))
				.orElseGet(() -> localized("reporting.security.violation.low"));
		case LOW -> localized("reporting.security.violation.low");
		case DEBUG -> isAllowable(violation)
				? violation.message() + System.lineSeparator() + hintOf(violation, policyFile)
				: violation.message();
		};
	}

	/**
	 * Whether a policy entry could allow the action. An argument Ares refused to
	 * inspect, a student type it cannot read without running student code, cannot
	 * be allowed, so it gets no hint.
	 *
	 * @param violation the violation.
	 * @return true when a hint can name an entry
	 */
	static boolean isAllowable(Violation violation) {
		return !UNINSPECTABLE.equals(violation.kind());
	}

	/** The kind Ares's checks give an argument they refused to inspect. */
	private static final String UNINSPECTABLE = "uninspectable";

	/**
	 * Returns the DEBUG hint: the permission, its target, the policy and the entry
	 * to paste, or only the category when no single entry allows the action.
	 *
	 * @param violation  the violation.
	 * @param policyFile the name of the policy file, if known.
	 * @return the hint.
	 */
	private static String hintOf(Violation violation, Optional<String> policyFile) {
		String policy = policyFile.map(name -> localized("reporting.security.violation.policy.named", name))
				.orElseGet(() -> localized("reporting.security.violation.policy.unnamed"));
		Optional<String> entry = entryOf(violation);
		Optional<String> permission = permissionOf(violation);
		if (entry.isEmpty() || permission.isEmpty()) {
			return localized("reporting.security.violation.hint.category", categoryOf(violation), policy);
		}
		return localized("reporting.security.violation.hint", permission.get(), violation.target(), policy)
				+ System.lineSeparator() + entry.get();
	}

	/**
	 * Returns the kind of resource an action reached for. An architecture finding
	 * carries its rule key, such as {@code file.system.access}, or {@code import}.
	 *
	 * @param violation the violation.
	 * @return file, network, command, thread, import or other.
	 */
	static String kindOf(Violation violation) {
		return "architecture".equals(violation.kind()) ? kindFromAction(violation.action()) : violation.kind();
	}

	/**
	 * Returns the policy category that would allow the action.
	 *
	 * @param violation the violation.
	 * @return the category's key.
	 */
	static String categoryOf(Violation violation) {
		return switch (kindOf(violation)) {
		case "file" -> "regardingFileSystemInteractions";
		case "network" -> "regardingNetworkConnections";
		case "command" -> "regardingCommandExecutions";
		case "thread" -> "regardingThreadCreations";
		case "import" -> "regardingPackageImports";
		default -> "theFollowingResourceAccessesArePermitted";
		};
	}

	/**
	 * Returns the action in the words of the policy, such as {@code read} or
	 * {@code open}, or nothing when no policy flag names it.
	 *
	 * @param violation the violation.
	 * @return the operation.
	 */
	private static Optional<String> operationOf(Violation violation) {
		String action = violation.action().toLowerCase(java.util.Locale.ROOT);
		return switch (violation.kind()) {
		case "file" -> Optional.of(fileOperationOf(action));
		case "network" -> switch (action) {
			case "connect" -> Optional.of("open");
			case "send", "receive" -> Optional.of(action);
			default -> Optional.empty();
			};
		case "command" -> "execute".equals(action) ? Optional.of("execute") : Optional.empty();
		case "thread" -> "create".equals(action) ? Optional.of("create") : Optional.empty();
		case "architecture" -> isKnownImport(violation) ? Optional.of("import") : Optional.empty();
		default -> Optional.empty();
		};
	}

	/**
	 * Whether an architecture finding is a forbidden import whose package is known.
	 *
	 * @param violation the violation.
	 * @return true for an import with a package.
	 */
	private static boolean isKnownImport(Violation violation) {
		return "import".equals(kindOf(violation)) && !violation.target().startsWith("unknown");
	}

	/**
	 * Returns the MEDIUM phrase for the action, such as "read a File", or nothing
	 * for an architecture finding, whose action at run time is not known.
	 *
	 * @param violation the violation.
	 * @return the localised phrase.
	 */
	static Optional<String> phraseOf(Violation violation) {
		if ("architecture".equals(violation.kind())) {
			return Optional.empty();
		}
		return operationOf(violation)
				.flatMap(operation -> Optional.ofNullable(phraseKeyOf(kindOf(violation), operation)))
				.map(key -> localized(key));
	}

	/**
	 * Returns the message key of a phrase.
	 *
	 * @param kind      the resource kind.
	 * @param operation the operation.
	 * @return the key; null when there is none.
	 */
	private static String phraseKeyOf(String kind, String operation) {
		return switch (kind + "." + operation) {
		case "file.read" -> "reporting.security.violation.phrase.file.read";
		case "file.overwrite" -> "reporting.security.violation.phrase.file.overwrite";
		case "file.create" -> "reporting.security.violation.phrase.file.create";
		case "file.execute" -> "reporting.security.violation.phrase.file.execute";
		case "file.delete" -> "reporting.security.violation.phrase.file.delete";
		case "network.open" -> "reporting.security.violation.phrase.network.open";
		case "network.send" -> "reporting.security.violation.phrase.network.send";
		case "network.receive" -> "reporting.security.violation.phrase.network.receive";
		case "command.execute" -> "reporting.security.violation.phrase.command.execute";
		case "thread.create" -> "reporting.security.violation.phrase.thread.create";
		case "import.import" -> "reporting.security.violation.phrase.import.import";
		default -> null;
		};
	}

	/**
	 * Returns the localised name of the permission the DEBUG hint grants.
	 *
	 * @param violation the violation.
	 * @return the permission; empty when no entry allows the action.
	 */
	private static Optional<String> permissionOf(Violation violation) {
		return operationOf(violation).map(operation -> switch (operation) {
		case "read" -> localized("reporting.security.violation.permission.read");
		case "overwrite" -> localized("reporting.security.violation.permission.overwrite");
		case "create" -> localized("reporting.security.violation.permission.create");
		case "execute" -> localized("reporting.security.violation.permission.execute");
		case "delete" -> localized("reporting.security.violation.permission.delete");
		case "open" -> localized("reporting.security.violation.permission.open");
		case "send" -> localized("reporting.security.violation.permission.send");
		case "receive" -> localized("reporting.security.violation.permission.receive");
		default -> localized("reporting.security.violation.permission.import");
		});
	}

	/**
	 * Returns the block-style policy entry that allows exactly the action, every
	 * other flag of the category off, or nothing when no single entry does.
	 *
	 * @param violation the violation.
	 * @return the entry, as YAML.
	 */
	static Optional<String> entryOf(Violation violation) {
		Optional<String> operation = operationOf(violation);
		if (operation.isEmpty()) {
			return Optional.empty();
		}
		return switch (kindOf(violation)) {
		case "file" -> Optional.of(flagEntry("onThisPathAndAllPathsBelow: " + yamlString(violation.target()),
				FILE_FLAGS, fileFlagOf(operation.get())));
		case "network" -> networkEntryOf(violation.target(),
				NETWORK_FLAGS.get(List.of("open", "send", "receive").indexOf(operation.get())));
		case "command" -> Optional.of(commandEntryOf(violation.target()));
		case "thread" -> Optional.of("- createTheFollowingNumberOfThreads: 1" + System.lineSeparator()
				+ "  ofThisClass: " + yamlString(violation.target()));
		case "import" -> Optional.of("- importTheFollowingPackage: " + yamlString(violation.target())
				+ System.lineSeparator() + "  exactMatchOnly: true");
		default -> Optional.empty();
		};
	}

	/**
	 * Builds an entry whose first line names the resource and whose flags are all
	 * off except the granted one.
	 *
	 * @param first   the first key and value.
	 * @param flags   the category's flags, in the manual's order.
	 * @param granted the flag to set.
	 * @return the entry, as YAML.
	 */
	private static String flagEntry(String first, List<String> flags, String granted) {
		StringBuilder entry = new StringBuilder("- ").append(first);
		for (String flag : flags) {
			entry.append(System.lineSeparator()).append("  ").append(flag).append(": ").append(flag.equals(granted));
		}
		return entry.toString();
	}

	/**
	 * Builds a network entry from a {@code host:port} target.
	 *
	 * @param target  the target.
	 * @param granted the flag to set.
	 * @return the entry; empty when the target names no port.
	 */
	private static Optional<String> networkEntryOf(String target, String granted) {
		int colon = target.lastIndexOf(':');
		if (colon <= 0 || colon == target.length() - 1
				|| !target.substring(colon + 1).chars().allMatch(Character::isDigit)) {
			return Optional.empty();
		}
		String host = target.substring(0, colon);
		return Optional.of(flagEntry("onTheHost: " + yamlString(host) + System.lineSeparator() + "  onThePort: "
				+ target.substring(colon + 1), NETWORK_FLAGS, granted));
	}

	/**
	 * Builds a command entry from a target of the command and its arguments,
	 * separated by spaces.
	 *
	 * @param target the target.
	 * @return the entry, as YAML.
	 */
	private static String commandEntryOf(String target) {
		List<String> words = List.of(target.split(" ", -1));
		String arguments = words.stream().skip(1).map(SecurityViolationFeedback::yamlString)
				.collect(java.util.stream.Collectors.joining(", ", "[", "]"));
		return "- executeTheCommand: " + yamlString(words.get(0)) + System.lineSeparator() + "  withTheseArguments: "
				+ arguments;
	}

	/**
	 * Quotes a value as a double-quoted YAML scalar, escaping backslashes, quotes
	 * and control characters, so a Windows path reads back unchanged.
	 *
	 * @param value the value.
	 * @return the quoted value.
	 */
	static String yamlString(String value) {
		StringBuilder quoted = new StringBuilder("\"");
		value.codePoints().forEach(c -> quoted.append(yamlEscape(c)));
		return quoted.append('"').toString();
	}

	/**
	 * Escapes one code point for a double-quoted YAML scalar.
	 *
	 * @param c the code point.
	 * @return the code point, escaped where YAML requires it.
	 */
	private static String yamlEscape(int c) {
		if (c == '\\' || c == '"') {
			return "\\" + (char) c;
		}
		return c < 0x20 || c == 0x7f ? String.format(java.util.Locale.ROOT, "\\x%02x", c) : Character.toString(c);
	}

	/**
	 * Maps a file action to its operation, as the file flags name it.
	 *
	 * @param action the action, in lower case.
	 * @return read, overwrite, create, execute or delete.
	 */
	private static String fileOperationOf(String action) {
		if (action.contains("delete")) {
			return "delete";
		}
		if (action.contains("execute")) {
			return "execute";
		}
		if (action.contains("create")) {
			return "create";
		}
		if (action.contains("write") || action.contains("overwrite")) {
			return "overwrite";
		}
		return "read";
	}

	/**
	 * Maps a file operation to the flag allowing it.
	 *
	 * @param operation the operation.
	 * @return the flag's key.
	 */
	private static String fileFlagOf(String operation) {
		return switch (operation) {
		case "delete" -> "deleteAllFiles";
		case "execute" -> "executeAllFiles";
		case "create" -> "createAllFiles";
		case "overwrite" -> "overwriteAllFiles";
		default -> "readAllFiles";
		};
	}

	/**
	 * Returns the resource kind of an architecture finding from the rule key it
	 * carries, which no language changes. Any other rule is "other".
	 *
	 * @param action the rule key, or {@code import} for a forbidden import.
	 * @return file, network, command, thread, import or other.
	 */
	private static String kindFromAction(String action) {
		return switch (action) {
		case "file.system.access" -> "file";
		case "network.access" -> "network";
		case "execute.command" -> "command";
		case "manipulate.threads" -> "thread";
		case "package.import", "import" -> "import";
		default -> "other";
		};
	}

	/**
	 * Calls a public no-argument getter, the same on every copy of the class.
	 *
	 * @param target the violation.
	 * @param getter the getter's name.
	 * @return the value as text.
	 * @throws ReflectiveOperationException when the getter cannot be called.
	 */
	private static String call(Throwable target, String getter) throws ReflectiveOperationException {
		Method method = target.getClass().getMethod(getter);
		return String.valueOf(method.invoke(target));
	}

	/**
	 * A blocked action, read from its violation.
	 *
	 * @param kind    file, network, command, thread or architecture.
	 * @param action  what was attempted; for an architecture finding, its rule key.
	 * @param target  what the action targeted.
	 * @param message the full message.
	 */
	public record Violation(String kind, String action, String target, String message) {
	}
}
