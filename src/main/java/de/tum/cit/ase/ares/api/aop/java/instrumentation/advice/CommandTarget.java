package de.tum.cit.ase.ares.api.aop.java.instrumentation.advice;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Internal value type representing a resolved command target.
 * <p>
 * Description: Pairs a nullable command name with an argument array.
 * Used throughout the toolbox as the canonical representation of a command
 * invocation, regardless of whether it originated from a {@code String},
 * a {@code String[]}, or a {@code List<String>}.
 * <p>
 * Design Rationale: Extracted from
 * {@link JavaInstrumentationAdviceCommandSystemToolbox} into its own top-level
 * class so that the JVM bootstrap class-loader injection performed by
 * {@link de.tum.cit.ase.ares.api.aop.java.instrumentation.JavaInstrumentationAgent}
 * can load it independently. Inner / nested classes compiled as separate
 * {@code .class} files are not picked up by
 * {@link net.bytebuddy.dynamic.ClassFileLocator.ForClassLoader#read(Class)}
 * when only the enclosing class is specified.
 *
 * @since 2.0.0
 * @author Markus Paulsen
 */
public record CommandTarget(@Nullable String command, @Nonnull String[] arguments) {

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
	public String toDisplayString() {
		if (command == null) {
			return "<unknown>";
		}
		if (arguments.length == 0) {
			return command;
		}
		return command + " " + String.join(" ", arguments);
	}
}
