package de.tum.cit.ase.ares.api.aop.java.javaAOPTestCaseToolbox;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import de.tum.cit.ase.ares.api.localization.Messages;

public final class JavaAOPTestCaseToolbox {
	private JavaAOPTestCaseToolbox() {
		throw new SecurityException(
				Messages.localized("security.instrumentation.utility.initialization", "JavaAOPTestCaseToolbox"));
	}

	// <editor-fold desc="Basic Types">
	public static String getStringAssignment(@Nonnull String adviceSetting, @Nullable Object value) {
		if (!(value instanceof String)) {
			throw new SecurityException(Messages.localized("security.advice.settings.data.type.mismatch.string",
					value != null ? value.getClass() : null));
		}
		return String.format("private static String %s = \"%s\";%n", adviceSetting, escapeJavaString((String) value));
	}

	/**
	 * Escapes a policy value so it is a safe Java string-literal body: a quote,
	 * backslash (e.g. a Windows path {@code C:\Users}), or newline can no longer
	 * break out of the generated literal or fail to compile.
	 *
	 * @since 2.0.0
	 * @author Markus Paulsen
	 * @param raw the raw value to embed
	 * @return the escaped literal body
	 */
	@Nonnull
	private static String escapeJavaString(@Nonnull String raw) {
		StringBuilder escaped = new StringBuilder(raw.length() + 8);
		for (int index = 0; index < raw.length(); index++) {
			char character = raw.charAt(index);
			switch (character) {
			case '\\' -> escaped.append("\\\\");
			case '"' -> escaped.append("\\\"");
			case '\n' -> escaped.append("\\n");
			case '\r' -> escaped.append("\\r");
			case '\t' -> escaped.append("\\t");
			default -> escaped.append(character);
			}
		}
		return escaped.toString();
	}

	public static String getIntegerAssignment(@Nonnull String adviceSetting, @Nullable Object value) {
		if (!(value instanceof Integer)) {
			throw new SecurityException(Messages.localized("security.advice.settings.data.type.mismatch.int",
					value != null ? value.getClass() : null));
		}
		return String.format("private static int %s = %d;%n", adviceSetting, value);
	}
	// </editor-fold>

	// <editor-fold desc="1-Dimensional Array Types">
	public static String getStringOneDArrayAssignment(@Nonnull String adviceSetting, @Nullable Object value) {
		if (!(value instanceof List<?>)) {
			throw new SecurityException(Messages.localized("security.advice.settings.data.type.mismatch.string[]",
					value != null ? value.getClass() : null));
		}
		String stringArrayValue = ((List<?>) value).stream().map(Object::toString)
				.map(s -> "\"" + escapeJavaString(s) + "\"").collect(Collectors.joining(", "));
		return String.format("private static String[] %s = new String[] {%s};%n", adviceSetting, stringArrayValue);
	}

	public static String getIntegerOneDArrayAssignment(@Nonnull String adviceSetting, @Nullable Object value) {
		if (!(value instanceof List<?>)) {
			throw new SecurityException(Messages.localized("security.advice.settings.data.type.mismatch.int[]",
					value != null ? value.getClass() : null));
		}
		String intArrayValue = ((List<?>) value).stream().map(Object::toString).collect(Collectors.joining(", "));
		return String.format("private static int[] %s = new int[] {%s};%n", adviceSetting, intArrayValue);
	}
	// </editor-fold>

	// <editor-fold desc="2-Dimensional Array Types">
	public static String getStringTwoDArrayAssignment(@Nonnull String adviceSetting, @Nullable Object value) {
		if (!(value instanceof List<?> || value instanceof Object[])) {
			throw new SecurityException(Messages.localized("security.advice.settings.data.type.mismatch.string[][]",
					value != null ? value.getClass() : null));
		}
		Stream<?> outerStream = (value instanceof List<?> l) ? l.stream() : Arrays.stream((Object[]) value);
		// Validate and format inner elements; allow List, String[], Object[], or single
		// values
		String stringArrayArrayValue = outerStream.map(inner -> {
			if (inner instanceof List<?> || inner instanceof String[] || inner instanceof Object[]) {
				return formatInnerStringArray(inner);
			} else if (inner == null) {
				// Represent null inner as empty array
				return "new String[] {}";
			} else if (inner instanceof CharSequence) {
				String s = inner.toString().trim();
				// If already a formatted array expression, pass through
				if (s.startsWith("new String[]")) {
					return s;
				}
				// Treat text as single value otherwise
				return "new String[] {\"" + escapeJavaString(s) + "\"}";
			} else {
				// Treat single value as a one-element string array
				return "new String[] {\"" + escapeJavaString(String.valueOf(inner)) + "\"}";
			}
		}).collect(Collectors.joining(", "));
		// No trailing semicolon/newline to match expected output
		return String.format("private static String[][] %s = new String[][] {%s};%n", adviceSetting,
				stringArrayArrayValue);
	}

	private static String formatInnerStringArray(Object inner) {
		if (inner instanceof List<?>) {
			String innerVals = ((List<?>) inner).stream()
					.map(obj -> obj == null ? "null" : "\"" + escapeJavaString(obj.toString()) + "\"")
					.collect(Collectors.joining(", "));
			return "new String[] {" + innerVals + "}";
		} else if (inner instanceof String[]) {
			String innerVals = Arrays.stream((String[]) inner)
					.map(s -> s == null ? "null" : "\"" + escapeJavaString(s) + "\"").collect(Collectors.joining(", "));
			return "new String[] {" + innerVals + "}";
		} else if (inner instanceof Object[]) {
			String innerVals = Arrays.stream((Object[]) inner)
					.map(obj -> obj == null ? "null" : "\"" + escapeJavaString(obj.toString()) + "\"")
					.collect(Collectors.joining(", "));
			return "new String[] {" + innerVals + "}";
		}
		throw new SecurityException(Messages.localized("security.advice.settings.data.type.mismatch.string[][]",
				inner != null ? inner.getClass() : null));
	}

	public static String getIntegerTwoDArrayAssignment(@Nonnull String adviceSetting, @Nullable Object value) {
		if (!(value instanceof List<?>)) {
			throw new SecurityException(Messages.localized("security.advice.settings.data.type.mismatch.int[][]",
					value != null ? value.getClass() : null));
		}
		List<?> outer = (List<?>) value;
		// Ensure all inner elements are lists; otherwise, fail fast like other methods
		boolean allInnerAreLists = outer.stream().allMatch(e -> e instanceof List<?>);
		if (!allInnerAreLists) {
			throw new SecurityException(Messages.localized("security.advice.settings.data.type.mismatch.int[][]",
					value != null ? value.getClass() : null));
		}
		String intArrayArrayValue = outer.stream().map(e -> (List<?>) e)
				.map(innerList -> innerList.stream().map(Object::toString).collect(Collectors.joining(", ")))
				.map(innerArray -> "new int[]{" + innerArray + "}").collect(Collectors.joining(", "));
		return String.format("private static int[][] %s = new int[][] {%s};%n", adviceSetting, intArrayArrayValue);
	}
	// </editor-fold>
}
