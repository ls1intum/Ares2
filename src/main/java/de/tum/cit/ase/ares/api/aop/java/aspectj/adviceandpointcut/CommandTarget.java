package de.tum.cit.ase.ares.api.aop.java.aspectj.adviceandpointcut;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Internal value type representing a resolved command target.
 * <p>
 * Description: Pairs a nullable command name with an argument array. Used
 * throughout the aspect as the canonical representation of a command
 * invocation, regardless of whether it originated from a {@code String}, a
 * {@code String[]}, or a {@code List<String>}.
 * <p>
 * Design Rationale: Kept as its own top-level type (rather than a record nested
 * inside {@link JavaAspectJCommandSystemAdviceDefinitions}) to mirror the
 * structure of the instrumentation backend's
 * {@code de.tum.cit.ase.ares.api.aop.java.instrumentation.advice.CommandTarget}.
 * It is package-private because only the same-package aspect references it; the
 * instrumentation counterpart is {@code public} solely because its agent
 * injects it across packages, a constraint that does not apply to AspectJ
 * weaving.
 *
 * @since 2.0.0
 * @author Markus Paulsen
 */
record CommandTarget(@Nullable String command, @Nonnull String[] arguments) {

	/**
	 * Returns a human-readable string representation of this command target.
	 * <p>
	 * Description: Formats the command and arguments as a single space-separated
	 * string. If there are no arguments, returns just the command name.
	 *
	 * @return non-null display string of the command invocation
	 * @since 2.0.0
	 * @author Markus Paulsen
	 */
	@Nonnull
	String toDisplayString() {
		if (command == null) {
			return "<unknown>";
		}
		if (arguments.length == 0) {
			return command;
		}
		return command + " " + String.join(" ", arguments);
	}
}
