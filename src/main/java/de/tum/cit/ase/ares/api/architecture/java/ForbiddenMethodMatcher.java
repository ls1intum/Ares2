package de.tum.cit.ase.ares.api.architecture.java;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import de.tum.cit.ase.ares.api.localization.Messages;

/**
 * Loads and matches the shared ArchUnit/WALA forbidden-method policy.
 * <p>
 * Both engines consume the union of their two versioned resource files. This
 * prevents a method covered by one engine from silently being permitted by the
 * other while still allowing each resource tree to retain its native signature
 * notation. All signatures are canonicalised before comparison.
 * </p>
 */
public final class ForbiddenMethodMatcher {

	public static final String POLICY_SCHEMA_VERSION = "2";

	private ForbiddenMethodMatcher() {
		throw new SecurityException(
				Messages.localized("security.general.utility.initialization", "ForbiddenMethodMatcher"));
	}

	/**
	 * Returns the validated, canonical union for either member of a resource pair.
	 */
	@Nonnull
	public static Set<String> effectiveMethods(@Nonnull Path methodsPath) {
		Path counterpart = counterpartOf(methodsPath);
		LinkedHashSet<String> effective = new LinkedHashSet<>();
		effective.addAll(loadValidated(methodsPath));
		if (!counterpart.equals(methodsPath)) {
			effective.addAll(loadValidated(counterpart));
		}
		return Set.copyOf(effective);
	}

	/**
	 * Matches exactly, or on an explicit class/package/method boundary when the
	 * policy entry intentionally omits a parameter list.
	 */
	public static boolean matches(@Nullable String actualSignature, @Nullable String forbiddenSignature) {
		if (actualSignature == null || forbiddenSignature == null) {
			return false;
		}
		String actual = canonicalise(actualSignature);
		String forbidden = canonicalise(forbiddenSignature);
		if (forbidden.indexOf('(') >= 0) {
			return actual.equals(forbidden);
		}
		return actual.equals(forbidden) || actual.startsWith(forbidden + ".") || actual.startsWith(forbidden + "$")
				|| actual.startsWith(forbidden + "(");
	}

	/**
	 * Converts JVM-descriptor and source-form signatures to one source notation.
	 */
	@Nullable
	public static String canonicalise(@Nullable String signature) {
		if (signature == null) {
			return null;
		}
		String stripped = signature.strip();
		int open = stripped.indexOf('(');
		if (open < 0) {
			return stripped;
		}
		int close = stripped.indexOf(')', open + 1);
		if (close < 0) {
			return stripped;
		}
		String parameters = stripped.substring(open + 1, close);
		List<String> canonicalParameters = looksLikeDescriptorSequence(parameters) ? parseDescriptorSequence(parameters)
				: parseSourceParameters(parameters);
		return stripped.substring(0, open) + "(" + String.join(", ", canonicalParameters) + ")";
	}

	@Nonnull
	private static Set<String> loadValidated(@Nonnull Path path) {
		try {
			List<String> lines = Files.readAllLines(path, StandardCharsets.UTF_8);
			LinkedHashSet<String> rawEntries = new LinkedHashSet<>();
			LinkedHashSet<String> canonicalEntries = new LinkedHashSet<>();
			for (int lineNumber = 1; lineNumber <= lines.size(); lineNumber++) {
				String entry = lines.get(lineNumber - 1).strip();
				if (entry.isEmpty() || entry.startsWith("#")) {
					continue;
				}
				if (!isStructurallyValid(entry)) {
					throw invalidEntry(path, lineNumber, entry);
				}
				if (!rawEntries.add(entry)) {
					throw new SecurityException(Messages.localized("security.architecture.blocklist.duplicate.entry",
							path, lineNumber, entry));
				}
				canonicalEntries.add(canonicalise(entry));
			}
			return canonicalEntries;
		} catch (IOException unreadable) {
			throw new SecurityException(Messages.localized("security.file-tools.read.content.failure"), unreadable);
		}
	}

	private static Path counterpartOf(Path path) {
		String value = path.toString();
		String separator = path.getFileSystem().getSeparator();
		String archunit = separator + "archunit" + separator;
		String wala = separator + "wala" + separator;
		if (value.contains(archunit)) {
			return Path.of(value.replace(archunit, wala));
		}
		if (value.contains(wala)) {
			return Path.of(value.replace(wala, archunit));
		}
		return path;
	}

	private static boolean isStructurallyValid(String entry) {
		if (entry.indexOf('\t') >= 0 || entry.indexOf('.') <= 0) {
			return false;
		}
		int open = entry.indexOf('(');
		int close = entry.indexOf(')');
		return open < 0 && close < 0
				|| open > 0 && close > open && entry.indexOf('(', open + 1) < 0 && entry.indexOf(')', close + 1) < 0;
	}

	private static SecurityException invalidEntry(Path path, int lineNumber, String entry) {
		return new SecurityException(
				Messages.localized("security.architecture.blocklist.malformed.entry", path, lineNumber, entry));
	}

	private static boolean looksLikeDescriptorSequence(String parameters) {
		if (parameters.isEmpty() || parameters.indexOf(',') >= 0) {
			return false;
		}
		return "[LBCDFIJSZV".indexOf(parameters.charAt(0)) >= 0;
	}

	private static List<String> parseSourceParameters(String parameters) {
		if (parameters.isBlank()) {
			return List.of();
		}
		return java.util.Arrays.stream(parameters.split(",")).map(String::strip).map(ForbiddenMethodMatcher::sourceType)
				.toList();
	}

	private static String sourceType(String parameter) {
		if (!parameter.startsWith("[")) {
			return parameter;
		}
		List<String> parsed = parseDescriptorSequence(parameter.replace('.', '/'));
		return parsed.size() == 1 ? parsed.get(0) : parameter;
	}

	private static List<String> parseDescriptorSequence(String descriptor) {
		List<String> parameters = new ArrayList<>();
		int index = 0;
		while (index < descriptor.length()) {
			int dimensions = 0;
			while (index < descriptor.length() && descriptor.charAt(index) == '[') {
				dimensions++;
				index++;
			}
			if (index >= descriptor.length()) {
				break;
			}
			char type = descriptor.charAt(index);
			String name;
			if (type == 'L') {
				int semicolon = descriptor.indexOf(';', index + 1);
				if (semicolon < 0) {
					name = descriptor.substring(index + 1).replace('/', '.');
					index = descriptor.length();
				} else {
					name = descriptor.substring(index + 1, semicolon).replace('/', '.');
					index = semicolon + 1;
				}
			} else {
				name = primitiveName(type);
				index++;
			}
			parameters.add(name + "[]".repeat(dimensions));
		}
		return parameters;
	}

	private static String primitiveName(char descriptor) {
		return switch (descriptor) {
		case 'B' -> "byte";
		case 'C' -> "char";
		case 'D' -> "double";
		case 'F' -> "float";
		case 'I' -> "int";
		case 'J' -> "long";
		case 'S' -> "short";
		case 'Z' -> "boolean";
		case 'V' -> "void";
		default -> String.valueOf(descriptor);
		};
	}
}
