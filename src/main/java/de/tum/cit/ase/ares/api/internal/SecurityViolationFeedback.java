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
 * Renders an action Ares blocked at the feedback level the policy chooses.
 * {@code FULL} keeps the message, {@code MINIMIZED} only says that something
 * was blocked, and {@code DEBUG} adds the policy entry that would allow it.
 * Only the violation classes of Ares's own checks are recognised, loaded by
 * Ares or, for the instrumentation copy, by the boot loader.
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

	/** The stage every blocked action reports. */
	private static final String STAGE = "Execution";

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
	 * Renders a violation at a level.
	 *
	 * @param violation the violation.
	 * @param level     the level.
	 * @return the text the student sees.
	 */
	public static String render(Violation violation, SecurityViolationFeedbackLevel level) {
		return switch (level) {
		case FULL -> violation.message();
		case MINIMIZED -> localized("reporting.security.violation.minimized", STAGE);
		case DEBUG -> violation.message() + System.lineSeparator()
				+ localized("reporting.security.violation.hint", categoryOf(violation), entryOf(violation));
		};
	}

	/**
	 * Returns the policy category that would allow the action.
	 *
	 * @param violation the violation.
	 * @return the category's key.
	 */
	static String categoryOf(Violation violation) {
		String kind = "architecture".equals(violation.kind()) ? kindFromAction(violation.action()) : violation.kind();
		return switch (kind) {
		case "file" -> "regardingFileSystemInteractions";
		case "network" -> "regardingNetworkConnections";
		case "command" -> "regardingCommandExecutions";
		case "thread" -> "regardingThreadCreations";
		case "import" -> "regardingPackageImports";
		default -> "theFollowingResourceAccessesArePermitted";
		};
	}

	/**
	 * Returns the entry that would allow the action: a complete file entry the
	 * policy reader accepts, with every other permission off, a description
	 * otherwise, and the category alone for the architecture stage.
	 *
	 * @param violation the violation.
	 * @return the entry, as YAML or as words.
	 */
	static String entryOf(Violation violation) {
		if ("architecture".equals(violation.kind())) {
			return "an entry for " + violation.action();
		}
		if ("file".equals(violation.kind())) {
			return fileEntryOf(violation);
		}
		return "an entry that allows " + violation.action() + " for " + violation.target();
	}

	/**
	 * Builds a file entry granting only the flag for the violation's action.
	 *
	 * @param violation the file violation.
	 * @return the entry, as a YAML flow mapping.
	 */
	private static String fileEntryOf(Violation violation) {
		String granted = fileFlagOf(violation.action());
		StringBuilder entry = new StringBuilder("{ onThisPathAndAllPathsBelow: ")
				.append(yamlString(violation.target()));
		for (String flag : FILE_FLAGS) {
			entry.append(", ").append(flag).append(": ").append(flag.equals(granted));
		}
		return entry.append(" }").toString();
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
	 * Maps a file action to the flag allowing it.
	 *
	 * @param action the action.
	 * @return the flag's key.
	 */
	private static String fileFlagOf(String action) {
		String lower = action.toLowerCase(java.util.Locale.ROOT);
		if (lower.contains("delete")) {
			return "deleteAllFiles";
		}
		if (lower.contains("execute")) {
			return "executeAllFiles";
		}
		if (lower.contains("create")) {
			return "createAllFiles";
		}
		if (lower.contains("write") || lower.contains("overwrite")) {
			return "overwriteAllFiles";
		}
		return "readAllFiles";
	}

	/**
	 * Guesses the resource kind of an architecture-stage action from its words.
	 *
	 * @param action the action.
	 * @return file, network, command, thread, import or other.
	 */
	private static String kindFromAction(String action) {
		String lower = action.toLowerCase(java.util.Locale.ROOT);
		if (lower.contains("file")) {
			return "file";
		}
		if (lower.contains("network") || lower.contains("socket") || lower.contains("connect")) {
			return "network";
		}
		if (lower.contains("command") || lower.contains("process")) {
			return "command";
		}
		if (lower.contains("thread")) {
			return "thread";
		}
		if (lower.contains("import")) {
			return "import";
		}
		return "other";
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
	 * @param action  what was attempted.
	 * @param target  what the action targeted.
	 * @param message the full message.
	 */
	public record Violation(String kind, String action, String target, String message) {
	}
}
