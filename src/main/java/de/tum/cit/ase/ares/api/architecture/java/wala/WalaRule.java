package de.tum.cit.ase.ares.api.architecture.java.wala;

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
			String callerSignature = (callerIdx == size - 1)
					? path.get(0).getMethod().getSignature()
					: path.get(callerIdx).getMethod().getSignature();

			String forbiddenSignature = forbiddenNode.getMethod().getSignature();
			String declaringClass = forbiddenNode.getMethod().getDeclaringClass().getName().getClassName().toString();
			String entrySignature = path.get(0).getMethod().getSignature();

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
}
