package de.tum.cit.ase.ares.api.architecture.java.wala;

import java.util.ArrayList;
import java.util.List;
import java.util.OptionalInt;
import java.util.Set;
import java.util.function.Predicate;

import com.ibm.wala.classLoader.IMethod;
import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ipa.callgraph.CallGraph;
import com.ibm.wala.shrike.shrikeCT.InvalidClassFileException;

import de.tum.cit.ase.ares.api.localization.Messages;

public class WalaRule {

	final String ruleName;
	final Set<String> forbiddenMethods;

	public WalaRule(String ruleName, Set<String> forbiddenMethods) {
		this.ruleName = ruleName;
		this.forbiddenMethods = forbiddenMethods;
	}

	public void check(CallGraph cg) {
		long _checkStart = System.nanoTime();
		int _pathCount = 0;
		boolean _violationFound = false;
		try {
		Predicate<CGNode> isForbidden = n -> forbiddenMethods.stream()
				.anyMatch(m -> n.getMethod().getSignature().startsWith(m));

		// Use the DFS finder in a loop: skip paths that are all-infra or transitive-JDK
		// false positives and keep searching until a genuine student violation is found.
		// This prevents a long JDK-internal path (found first by alphabetical DFS) from
		// masking the shorter, direct forbidden-method call in student code.
		CustomDFSPathFinder finder = new CustomDFSPathFinder(cg, cg.getEntrypointNodes().iterator(), isForbidden);
		List<CGNode> path;
		while ((path = finder.find()) != null) {
			_pathCount++;
			if (path.isEmpty()) {
				continue;
			}

			OptionalInt studentIdx = WalaPathClassification.nearestStudentFrame(path);
			if (studentIdx.isEmpty()) {
				continue;
			}

			int callerIdx = studentIdx.getAsInt();
			if (WalaPathClassification.isFalsePositiveTransitivePath(path, callerIdx)) {
				continue;
			}

			int size = path.size();
			CGNode forbiddenNode = path.get(size - 1);

			// In production the forbidden node is always a JDK method (Primordial-loaded),
			// so isInfraFrame classifies it as infra and nearestStudentFrame never returns
			// size-1. This branch is defensive against misconfiguration only.
			// Convert WALA's JVM-descriptor signatures to source-form so the resulting
			// SecurityException matches the format ArchUnit produces (java.lang.String
			// instead of Ljava/lang/String;, no return-type suffix). This keeps
			// expected-exception fixtures consistent across both architecture backends.
			String callerSignature = formatJvmSignature((callerIdx == size - 1)
					? path.get(0).getMethod().getSignature()
					: path.get(callerIdx).getMethod().getSignature());

			String forbiddenSignature = formatJvmSignature(forbiddenNode.getMethod().getSignature());
			String declaringClass = forbiddenNode.getMethod().getDeclaringClass().getName().getClassName().toString();
			String entrySignature = formatJvmSignature(path.get(0).getMethod().getSignature());

			try {
				IMethod.SourcePosition sp = forbiddenNode.getMethod().getSourcePosition(0);
				int lineNumber = sp != null ? sp.getFirstLine() : -1;
				_violationFound = true;
				throw new AssertionError(
						Messages.localized("security.architecture.method.call.message", ruleName,
								callerSignature, forbiddenSignature, declaringClass, lineNumber, entrySignature));
			} catch (InvalidClassFileException e) {
				throw new SecurityException(Messages.localized("security.architecture.invalid.class.file"));
			}
		}
		} finally {
			long _ms = (System.nanoTime() - _checkStart) / 1_000_000L;
			try {
				java.nio.file.Files.writeString(java.nio.file.Paths.get("/tmp/wala-check-times.log"),
						System.currentTimeMillis() + "|" + _ms + "|paths=" + _pathCount + "|violation=" + _violationFound + "|" + ruleName + "\n",
						java.nio.file.StandardOpenOption.CREATE, java.nio.file.StandardOpenOption.APPEND);
			} catch (Exception ignored) {}
		}
	}

	/**
	 * Convert a WALA {@link IMethod#getSignature()} string from JVM-descriptor form
	 * to the source-code form that ArchUnit produces.
	 *
	 * <p>WALA's signature looks like
	 * {@code package.Class.method(Lpackage/Param;[I)Lreturn/Type;}. ArchUnit reports
	 * {@code package.Class.method(package.Param, int[])}. The return-type suffix is
	 * dropped to match ArchUnit's omission. Constructor names {@code <init>} stay as is.
	 *
	 * <p>Returns the original string unchanged when no parenthesised descriptor is
	 * found, so callers can pass any signature without first checking the format.
	 */
	static String formatJvmSignature(String walaSignature) {
		if (walaSignature == null) {
			return null;
		}
		int parenIdx = walaSignature.indexOf('(');
		if (parenIdx < 0) {
			return walaSignature;
		}
		int closeParenIdx = walaSignature.indexOf(')', parenIdx + 1);
		if (closeParenIdx < 0) {
			return walaSignature;
		}
		String classMethod = walaSignature.substring(0, parenIdx);
		String paramsDescriptor = walaSignature.substring(parenIdx + 1, closeParenIdx);
		List<String> params = parseParamDescriptors(paramsDescriptor);
		StringBuilder rendered = new StringBuilder(classMethod).append('(');
		for (int i = 0; i < params.size(); i++) {
			if (i > 0) {
				rendered.append(", ");
			}
			rendered.append(params.get(i));
		}
		rendered.append(')');
		return rendered.toString();
	}

	/**
	 * Parse a JVM parameter-descriptor sequence into a list of source-form types.
	 *
	 * <p>Examples:
	 * <ul>
	 *   <li>{@code "Ljava/lang/String;Ljava/io/File;"} → {@code [java.lang.String, java.io.File]}</li>
	 *   <li>{@code "[I"} → {@code [int[]]}</li>
	 *   <li>{@code "[[Ljava/lang/String;"} → {@code [java.lang.String[][]]}</li>
	 * </ul>
	 *
	 * <p>Malformed descriptors stop the parse early; whatever has been parsed so
	 * far is returned to keep the surrounding error message useful.
	 */
	private static List<String> parseParamDescriptors(String descriptor) {
		List<String> params = new ArrayList<>();
		int i = 0;
		while (i < descriptor.length()) {
			int dimensions = 0;
			while (i < descriptor.length() && descriptor.charAt(i) == '[') {
				dimensions++;
				i++;
			}
			if (i >= descriptor.length()) {
				break;
			}
			char c = descriptor.charAt(i);
			String typeName;
			if (c == 'L') {
				int semicolonIdx = descriptor.indexOf(';', i + 1);
				if (semicolonIdx < 0) {
					break;
				}
				typeName = descriptor.substring(i + 1, semicolonIdx).replace('/', '.');
				i = semicolonIdx + 1;
			} else {
				typeName = primitiveDescriptorToName(c);
				i++;
			}
			StringBuilder rendered = new StringBuilder(typeName);
			for (int d = 0; d < dimensions; d++) {
				rendered.append("[]");
			}
			params.add(rendered.toString());
		}
		return params;
	}

	/**
	 * Map a single primitive JVM-descriptor character to its source-form Java name.
	 *
	 * <p>{@code V} (void) is included for completeness, even though it never appears
	 * inside a parameter list. Unknown characters are echoed back so future JVM
	 * spec extensions do not silently corrupt the message.
	 */
	private static String primitiveDescriptorToName(char descriptorChar) {
		return switch (descriptorChar) {
			case 'B' -> "byte";
			case 'C' -> "char";
			case 'D' -> "double";
			case 'F' -> "float";
			case 'I' -> "int";
			case 'J' -> "long";
			case 'S' -> "short";
			case 'Z' -> "boolean";
			case 'V' -> "void";
			default -> String.valueOf(descriptorChar);
		};
	}
}
