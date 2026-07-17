package de.tum.cit.ase.ares.api.architecture.java.wala;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.Set;

import com.ibm.wala.classLoader.IClass;
import com.ibm.wala.classLoader.IMethod;
import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ipa.callgraph.CallGraph;
import com.ibm.wala.ipa.cha.IClassHierarchy;
import com.ibm.wala.shrike.shrikeCT.InvalidClassFileException;
import com.ibm.wala.types.ClassLoaderReference;
import com.ibm.wala.types.TypeName;
import com.ibm.wala.types.TypeReference;
import com.ibm.wala.util.collections.Iterator2Iterable;
import com.tngtech.archunit.core.domain.JavaAccess;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaClasses;

import de.tum.cit.ase.ares.api.architecture.java.JavaArchitectureTestCase;
import de.tum.cit.ase.ares.api.localization.Messages;
import de.tum.cit.ase.ares.api.policy.policySubComponents.ClassPermission;

public class WalaRule {

	final String ruleName;
	final Set<String> forbiddenMethods;

	public WalaRule(String ruleName, Set<String> forbiddenMethods) {
		this.ruleName = ruleName;
		this.forbiddenMethods = forbiddenMethods;
	}

	public void check(CallGraph cg) {
		check(cg, Set.of());
	}

	/**
	 * Runs the rule, exempting the allow-listed classes: a violation is reported
	 * ONLY when some non-allowed student frame can reach the forbidden API directly
	 * (or through a project-helper frame that is not a transitive-JDK false
	 * positive). If every student frame that reaches the sink is allow-listed, the
	 * sink is exempt. An empty allow-list reproduces the original behaviour.
	 * <p>
	 * Implemented as reverse reachability per forbidden sink rather than a single
	 * forward DFS. The previous {@link CustomDFSPathFinder} marked nodes globally
	 * visited and therefore reported each sink on exactly one path, so an
	 * allow-listed or false-positive caller discovered first could permanently mask
	 * a genuine violation by a different caller of the same sink. By examining
	 * every forbidden sink and every distinct nearest-student approach to it, that
	 * masking cannot occur, while the per-path classification
	 * ({@link WalaPathClassification}) is preserved exactly.
	 *
	 * @since 2.0.0
	 * @author Markus Paulsen
	 * @param cg             the call graph to analyse, must not be null
	 * @param allowedClasses the classes exempt from the rule, must not be null
	 */
	public void check(CallGraph cg, Set<ClassPermission> allowedClasses) {
		Set<CGNode> entryReachable = forwardReachableFromEntrypoints(cg);
		if (entryReachable.isEmpty()) {
			throw new SecurityException(Messages.localized("security.architecture.wala.entrypoints.empty", ruleName));
		}
		// Collect every forbidden sink in the call graph. Sorting by signature keeps
		// the reported violation deterministic across JVM runs (WALA's node iteration
		// order depends on per-JVM identity hashes).
		List<CGNode> sinks = new ArrayList<>();
		for (CGNode node : cg) {
			if (node != null && isForbidden(node)) {
				sinks.add(node);
			}
		}
		if (sinks.isEmpty()) {
			return;
		}
		sinks.sort(Comparator.comparing(n -> n.getMethod().getSignature()));

		for (CGNode sink : sinks) {
			if (!entryReachable.contains(sink)) {
				continue;
			}
			// Evaluate each distinct nearest-student approach to this sink as it is
			// discovered and throw on the first genuine, non-exempt violation, so a real
			// violation among the explored approaches is never masked by an exempt one.
			evaluateSink(cg, sink, allowedClasses, entryReachable);
		}
	}

	/**
	 * Checks direct bytecode accesses before the transitive WALA walk. WALA may
	 * omit an interface call node when dispatch resolves to an internal JDK
	 * implementation, particularly for woven calls such as
	 * {@code Collection.parallelStream()}. ArchUnit's imported access model retains
	 * the API target written in the bytecode, so this complementary pass prevents a
	 * direct forbidden call from disappearing from the WALA result.
	 *
	 * @param javaClasses    imported classes under analysis
	 * @param allowedClasses classes exempted by the policy
	 */
	public void checkDirectAccesses(JavaClasses javaClasses, Set<ClassPermission> allowedClasses) {
		checkDirectAccesses(javaClasses, allowedClasses, null);
	}

	/**
	 * Checks direct bytecode accesses using ArchUnit's imported graph first and the
	 * already-built WALA hierarchy when ArchUnit deliberately left a dependency
	 * unresolved. If neither graph can classify an inherited target, the check
	 * fails closed.
	 *
	 * @param javaClasses    imported classes under analysis
	 * @param allowedClasses classes exempted by the policy
	 * @param classHierarchy WALA's static class hierarchy, or {@code null} when no
	 *                       fallback is available
	 */
	public void checkDirectAccesses(JavaClasses javaClasses, Set<ClassPermission> allowedClasses,
			IClassHierarchy classHierarchy) {
		javaClasses.stream().flatMap(javaClass -> javaClass.getAccessesFromSelf().stream())
				.sorted(Comparator.comparing(access -> access.getOrigin().getFullName() + "->" //$NON-NLS-1$
						+ access.getTarget().getFullName()))
				.filter(access -> !JavaArchitectureTestCase.isAllowedClass(access.getOriginOwner().getFullName(),
						allowedClasses))
				.filter(access -> isDirectlyForbidden(access, classHierarchy)).findFirst()
				.ifPresent(this::throwDirectAccessViolation);
	}

	private boolean isDirectlyForbidden(JavaAccess<?> access, IClassHierarchy classHierarchy) {
		Set<String> targets = new HashSet<>();
		targets.add(access.getTarget().getFullName());
		access.getTarget().resolveMember().ifPresent(member -> targets.add(member.getFullName()));
		if (forbiddenMethods.stream().anyMatch(
				forbidden -> targets.stream().anyMatch(target -> matchesForbiddenMethod(target, forbidden)))) {
			return true;
		}
		return forbiddenMethods.stream()
				.anyMatch(forbidden -> matchesInheritedTarget(access, forbidden, classHierarchy));
	}

	static boolean matchesInheritedTarget(JavaAccess<?> access, String forbidden) {
		return matchesInheritedTarget(access, forbidden, null);
	}

	static boolean matchesInheritedTarget(JavaAccess<?> access, String forbidden, IClassHierarchy classHierarchy) {
		String target = access.getTarget().getFullName();
		int targetParenthesis = target.indexOf('(');
		int forbiddenParenthesis = forbidden.indexOf('(');
		if (targetParenthesis < 0 || forbiddenParenthesis < 0) {
			return false;
		}
		int targetMethodSeparator = target.lastIndexOf('.', targetParenthesis);
		int forbiddenMethodSeparator = forbidden.lastIndexOf('.', forbiddenParenthesis);
		if (targetMethodSeparator < 0 || forbiddenMethodSeparator < 0) {
			return false;
		}
		String forbiddenOwner = forbidden.substring(0, forbiddenMethodSeparator);
		String forbiddenMethod = forbidden.substring(forbiddenMethodSeparator);
		String targetMethod = target.substring(targetMethodSeparator);
		if (forbiddenMethod.startsWith(".<init>") //$NON-NLS-1$
				|| !matchesForbiddenMethod(forbiddenOwner + targetMethod, forbidden)) {
			return false;
		}
		JavaClass targetOwner = access.getTargetOwner();
		if (forbiddenOwner.equals(targetOwner.getFullName())) {
			return true;
		}
		Set<JavaClass> assignableTypes;
		try {
			assignableTypes = targetOwner.getAllClassesSelfIsAssignableTo();
		} catch (RuntimeException incompleteHierarchy) {
			throw new SecurityException("Could not classify inherited target " + targetOwner.getFullName(), //$NON-NLS-1$
					incompleteHierarchy);
		}
		if (assignableTypes.stream().map(JavaClass::getFullName).anyMatch(forbiddenOwner::equals)) {
			return true;
		}
		if (targetOwner.isFullyImported() && assignableTypes.stream().allMatch(JavaClass::isFullyImported)) {
			return false;
		}
		return matchesUsingWalaHierarchy(targetOwner.getFullName(), forbiddenOwner, classHierarchy);
	}

	private static boolean matchesUsingWalaHierarchy(String targetOwner, String forbiddenOwner,
			IClassHierarchy classHierarchy) {
		if (classHierarchy == null) {
			throw new SecurityException("Incomplete hierarchy for inherited target " + targetOwner); //$NON-NLS-1$
		}
		try {
			IClass targetClass = lookupClass(classHierarchy, targetOwner);
			IClass forbiddenClass = lookupClass(classHierarchy, forbiddenOwner);
			if (targetClass == null || forbiddenClass == null) {
				throw new SecurityException("Incomplete hierarchy for inherited target " + targetOwner); //$NON-NLS-1$
			}
			return classHierarchy.isAssignableFrom(forbiddenClass, targetClass);
		} catch (SecurityException exception) {
			throw exception;
		} catch (RuntimeException incompleteHierarchy) {
			throw new SecurityException("Could not classify inherited target " + targetOwner, incompleteHierarchy); //$NON-NLS-1$
		}
	}

	private static IClass lookupClass(IClassHierarchy classHierarchy, String className) {
		TypeName typeName = TypeName.findOrCreate("L" + className.replace('.', '/')); //$NON-NLS-1$
		for (ClassLoaderReference loader : List.of(ClassLoaderReference.Primordial, ClassLoaderReference.Extension,
				ClassLoaderReference.Application)) {
			IClass resolved = classHierarchy.lookupClass(TypeReference.findOrCreate(loader, typeName));
			if (resolved != null) {
				return resolved;
			}
		}
		return null;
	}

	private void throwDirectAccessViolation(JavaAccess<?> access) {
		String caller = access.getOrigin().getFullName();
		String target = access.getTarget().getFullName();
		String declaringClass = access.getTargetOwner().getSimpleName();
		throw new AssertionError(Messages.localized("security.architecture.method.call.message", ruleName, caller, //$NON-NLS-1$
				target, declaringClass, access.getLineNumber(), caller));
	}

	/** Returns {@code true} if the node's method matches a forbidden signature. */
	private boolean isForbidden(CGNode node) {
		String signature = node.getMethod().getSignature();
		return forbiddenMethods.stream().anyMatch(m -> matchesForbiddenMethod(signature, m));
	}

	/**
	 * Classifies a single reconstructed path (entry &rarr; ... &rarr; forbidden
	 * sink) and returns the violation to raise, or empty when the path is an
	 * all-infra path, a transitive-JDK false positive, or fully allow-listed. The
	 * message format is identical to the previous inline reporting so both
	 * architecture backends keep producing matching expected-exception fixtures.
	 */
	private Optional<AssertionError> evaluatePath(List<CGNode> path, Set<ClassPermission> allowedClasses) {
		if (path.isEmpty()) {
			return Optional.empty();
		}
		OptionalInt studentIdx = WalaPathClassification.nearestStudentFrame(path);
		if (studentIdx.isEmpty()) {
			return Optional.empty();
		}
		int callerIdx = studentIdx.getAsInt();
		if (WalaPathClassification.isFalsePositiveTransitivePath(path, callerIdx)) {
			return Optional.empty();
		}
		int size = path.size();
		CGNode forbiddenNode = path.get(size - 1);
		// Exempt allow-listed (essential/test) classes. A path is exempt ONLY when
		// every
		// student frame on it is allow-listed: if a non-allowed student frame reaches
		// the
		// forbidden API by routing through an allow-listed helper, the violation must
		// still be reported (against that non-allowed frame).
		if (!allowedClasses.isEmpty()) {
			int nonAllowedStudentIdx = nearestNonAllowedStudentFrame(path, allowedClasses);
			if (nonAllowedStudentIdx < 0) {
				return Optional.empty();
			}
			callerIdx = nonAllowedStudentIdx;
		}
		CGNode callerNode = (callerIdx == size - 1) ? path.get(0) : path.get(callerIdx);
		// Convert WALA's JVM-descriptor signatures to source-form so the resulting
		// SecurityException matches the format ArchUnit produces.
		String callerSignature = formatJvmSignature(callerNode.getMethod().getSignature());
		String forbiddenSignature = formatJvmSignature(forbiddenNode.getMethod().getSignature());
		String declaringClass = forbiddenNode.getMethod().getDeclaringClass().getName().getClassName().toString();
		String entrySignature = formatJvmSignature(path.get(0).getMethod().getSignature());
		try {
			IMethod.SourcePosition sp = forbiddenNode.getMethod().getSourcePosition(0);
			int lineNumber = sp != null ? sp.getFirstLine() : -1;
			return Optional.of(new AssertionError(Messages.localized("security.architecture.method.call.message",
					ruleName, callerSignature, forbiddenSignature, declaringClass, lineNumber, entrySignature)));
		} catch (InvalidClassFileException e) {
			throw new SecurityException(Messages.localized("security.architecture.invalid.class.file"));
		}
	}

	/** Forward BFS over successor edges from the entry points. */
	private static Set<CGNode> forwardReachableFromEntrypoints(CallGraph cg) {
		Set<CGNode> visited = new HashSet<>();
		Deque<CGNode> queue = new ArrayDeque<>();
		for (CGNode entry : cg.getEntrypointNodes()) {
			if (entry != null && visited.add(entry)) {
				queue.add(entry);
			}
		}
		while (!queue.isEmpty()) {
			CGNode current = queue.poll();
			for (CGNode successor : Iterator2Iterable.make(cg.getSuccNodes(current))) {
				if (successor != null && visited.add(successor)) {
					queue.add(successor);
				}
			}
		}
		return visited;
	}

	/**
	 * Bounds the number of distinct nearest-student approaches evaluated per sink
	 * so a pathological call graph cannot cause an unbounded reverse walk. The
	 * bound is a backstop only: each approach is evaluated as it is discovered and
	 * a genuine violation throws immediately. Reaching the bound also fails closed,
	 * because passing after truncating unexplored approaches would make the result
	 * depend on traversal order.
	 */
	private static final int MAX_APPROACHES_PER_SINK = 64;

	/**
	 * Reverse-walks from the sink through infrastructure frames only and evaluates
	 * each nearest-student approach
	 * {@code [nearestStudentFrame, ...infra..., sink]} the moment it is found,
	 * throwing on the first genuine violation. If the sink is itself
	 * student-authored (a theoretical edge case, since production sinks are JDK
	 * methods) it is its own nearest student frame.
	 */
	private void evaluateSink(CallGraph cg, CGNode sink, Set<ClassPermission> allowedClasses,
			Set<CGNode> entryReachable) {
		if (!WalaPathClassification.isInfraFrame(sink)) {
			evaluateApproach(cg, List.of(sink), allowedClasses, entryReachable);
			return;
		}
		Deque<List<CGNode>> stack = new ArrayDeque<>();
		stack.push(List.of(sink));
		int evaluated = 0;
		while (!stack.isEmpty()) {
			List<CGNode> chain = stack.pop();
			CGNode frontier = chain.get(0);
			for (CGNode predecessor : Iterator2Iterable.make(cg.getPredNodes(frontier))) {
				if (predecessor == null || chain.contains(predecessor)) {
					continue;
				}
				List<CGNode> extended = new ArrayList<>();
				extended.add(predecessor);
				extended.addAll(chain);
				if (WalaPathClassification.isInfraFrame(predecessor)) {
					stack.push(extended);
				} else {
					// A nearest-student approach: evaluate it now; a real violation throws.
					evaluateApproach(cg, extended, allowedClasses, entryReachable);
					evaluated++;
					if (evaluated >= MAX_APPROACHES_PER_SINK) {
						throw new SecurityException(Messages.localized("security.architecture.wala.approaches.limit",
								ruleName, MAX_APPROACHES_PER_SINK, sink.getMethod().getSignature()));
					}
				}
			}
		}
	}

	/**
	 * Builds the full witness path for one nearest-student approach (choosing a
	 * prefix that routes through a non-allowed student frame when the nearest one
	 * is allow-listed) and throws if it classifies as a genuine violation.
	 */
	private void evaluateApproach(CallGraph cg, List<CGNode> suffix, Set<ClassPermission> allowedClasses,
			Set<CGNode> entryReachable) {
		CGNode student = suffix.get(0);
		if (!entryReachable.contains(student)) {
			return;
		}
		List<CGNode> prefix = prefixToEntry(cg, student, allowedClasses);
		if (prefix == null || prefix.isEmpty()) {
			return;
		}
		List<CGNode> full = new ArrayList<>(prefix);
		full.addAll(suffix.subList(1, suffix.size()));
		Optional<AssertionError> violation = evaluatePath(full, allowedClasses);
		if (violation.isPresent()) {
			throw violation.get();
		}
	}

	/**
	 * Chooses an entry-to-student prefix. When the nearest student frame is itself
	 * allow-listed, it prefers a path that routes through a non-allowed student
	 * ancestor, so a violation hidden behind an allow-listed nearest frame is still
	 * attributed to the real caller. Falls back to any entry path.
	 */
	private List<CGNode> prefixToEntry(CallGraph cg, CGNode student, Set<ClassPermission> allowedClasses) {
		if (!allowedClasses.isEmpty() && isAllowedStudentFrame(student, allowedClasses)) {
			List<CGNode> viaNonAllowed = prefixThroughNonAllowed(cg, student, allowedClasses);
			if (viaNonAllowed != null) {
				return viaNonAllowed;
			}
		}
		return reversePathToEntry(cg, student);
	}

	/**
	 * Reverse BFS from {@code target} to any entry point; returns entry &rarr;
	 * target.
	 */
	private static List<CGNode> reversePathToEntry(CallGraph cg, CGNode target) {
		Set<CGNode> entries = new HashSet<>();
		for (CGNode entry : cg.getEntrypointNodes()) {
			if (entry != null) {
				entries.add(entry);
			}
		}
		if (entries.contains(target)) {
			List<CGNode> single = new ArrayList<>();
			single.add(target);
			return single;
		}
		Map<CGNode, CGNode> toward = new HashMap<>();
		Deque<CGNode> queue = new ArrayDeque<>();
		Set<CGNode> visited = new HashSet<>();
		visited.add(target);
		queue.add(target);
		while (!queue.isEmpty()) {
			CGNode current = queue.poll();
			for (CGNode predecessor : Iterator2Iterable.make(cg.getPredNodes(current))) {
				if (predecessor == null || !visited.add(predecessor)) {
					continue;
				}
				toward.put(predecessor, current);
				if (entries.contains(predecessor)) {
					return reconstruct(predecessor, toward, target);
				}
				queue.add(predecessor);
			}
		}
		return null;
	}

	/**
	 * Reverse BFS from {@code student} to the nearest non-allowed student ancestor,
	 * then prefixes that ancestor's own entry path, so the assembled path contains
	 * a non-allowed student frame whenever one can reach {@code student}.
	 */
	private List<CGNode> prefixThroughNonAllowed(CallGraph cg, CGNode student, Set<ClassPermission> allowedClasses) {
		Map<CGNode, CGNode> toward = new HashMap<>();
		Deque<CGNode> queue = new ArrayDeque<>();
		Set<CGNode> visited = new HashSet<>();
		visited.add(student);
		queue.add(student);
		CGNode found = null;
		while (!queue.isEmpty() && found == null) {
			CGNode current = queue.poll();
			for (CGNode predecessor : Iterator2Iterable.make(cg.getPredNodes(current))) {
				if (predecessor == null || !visited.add(predecessor)) {
					continue;
				}
				toward.put(predecessor, current);
				if (!WalaPathClassification.isInfraFrame(predecessor)
						&& !isAllowedStudentFrame(predecessor, allowedClasses)) {
					found = predecessor;
					break;
				}
				queue.add(predecessor);
			}
		}
		if (found == null) {
			return null;
		}
		List<CGNode> entryToFound = reversePathToEntry(cg, found);
		if (entryToFound == null) {
			return null;
		}
		List<CGNode> foundToStudent = reconstruct(found, toward, student);
		List<CGNode> prefix = new ArrayList<>(entryToFound);
		prefix.addAll(foundToStudent.subList(1, foundToStudent.size()));
		return prefix;
	}

	/**
	 * Follows {@code toward} pointers from {@code start} to {@code target},
	 * inclusive.
	 */
	private static List<CGNode> reconstruct(CGNode start, Map<CGNode, CGNode> toward, CGNode target) {
		List<CGNode> path = new ArrayList<>();
		CGNode current = start;
		path.add(current);
		while (current != target) {
			current = toward.get(current);
			if (current == null) {
				return path;
			}
			path.add(current);
		}
		return path;
	}

	/** Returns {@code true} when the node is a student frame on the allow-list. */
	private static boolean isAllowedStudentFrame(CGNode node, Set<ClassPermission> allowedClasses) {
		try {
			com.ibm.wala.types.TypeName typeName = node.getMethod().getDeclaringClass().getName();
			String fullyQualifiedName = typeName == null ? null : walaTypeToFullyQualifiedName(typeName.toString());
			return JavaArchitectureTestCase.isAllowedClass(fullyQualifiedName, allowedClasses);
		} catch (RuntimeException | com.ibm.wala.util.debug.UnimplementedError unclassifiable) {
			return false;
		}
	}

	/**
	 * Returns the index of the student frame nearest the forbidden sink whose class
	 * is NOT allow-listed, or {@code -1} when every student (non-infra) frame on
	 * the path is allow-listed. Infrastructure frames (including malformed ones,
	 * which {@link WalaPathClassification#isInfraFrame} already treats as infra)
	 * are not student frames and are skipped; the local catch is a defensive
	 * fallback that does not exempt a frame it cannot classify.
	 */
	private static int nearestNonAllowedStudentFrame(List<CGNode> path, Set<ClassPermission> allowedClasses) {
		for (int i = path.size() - 1; i >= 0; i--) {
			CGNode frame = path.get(i);
			if (WalaPathClassification.isInfraFrame(frame)) {
				// Not student-authored (JDK / Ares / test-helper infrastructure).
				continue;
			}
			String fullyQualifiedName;
			try {
				com.ibm.wala.types.TypeName typeName = frame.getMethod().getDeclaringClass().getName();
				fullyQualifiedName = typeName == null ? null : walaTypeToFullyQualifiedName(typeName.toString());
			} catch (RuntimeException | com.ibm.wala.util.debug.UnimplementedError unclassifiable) {
				return i; // cannot classify -> do not exempt
			}
			if (!JavaArchitectureTestCase.isAllowedClass(fullyQualifiedName, allowedClasses)) {
				return i;
			}
		}
		return -1;
	}

	/**
	 * Converts a WALA {@code TypeName} string ({@code Lcom/foo/Bar} or
	 * {@code Lcom/foo/Bar$Inner}) to a dotted fully-qualified name
	 * ({@code com.foo.Bar} / {@code com.foo.Bar$Inner}) so it can be compared with
	 * the policy's {@link ClassPermission} names. The {@code '$'} nested-class
	 * separator is preserved for boundary-aware matching.
	 */
	private static String walaTypeToFullyQualifiedName(String walaTypeName) {
		if (walaTypeName == null) {
			return null;
		}
		String withoutPrefix = walaTypeName.startsWith("L") ? walaTypeName.substring(1) : walaTypeName;
		return withoutPrefix.replace('/', '.');
	}

	private static boolean matchesForbiddenMethod(String actualSignature, String forbiddenSignature) {
		if (actualSignature == null || forbiddenSignature == null) {
			return false;
		}
		if (actualSignature.startsWith(forbiddenSignature)) {
			return true;
		}
		String actualWithoutReturnType = stripReturnType(actualSignature);
		String forbiddenWithoutReturnType = stripReturnType(forbiddenSignature);
		if (actualWithoutReturnType.equals(forbiddenWithoutReturnType)) {
			return true;
		}
		String actualFormatted = formatJvmSignature(actualWithoutReturnType);
		String forbiddenFormatted = formatJvmSignature(forbiddenWithoutReturnType);
		// startsWith already covers the exact-match case, since a string always starts
		// with itself.
		return actualFormatted.startsWith(forbiddenFormatted);
	}

	private static String stripReturnType(String signature) {
		int closeParenIdx = signature.indexOf(')');
		if (closeParenIdx < 0) {
			return signature;
		}
		return signature.substring(0, closeParenIdx + 1);
	}

	/**
	 * Convert a WALA {@link IMethod#getSignature()} string from JVM-descriptor form
	 * to the source-code form that ArchUnit produces.
	 * <p>
	 * WALA's signature looks like
	 * {@code package.Class.method(Lpackage/Param;[I)Lreturn/Type;}. ArchUnit
	 * reports {@code package.Class.method(package.Param, int[])}. The return-type
	 * suffix is dropped to match ArchUnit's omission. Constructor names
	 * {@code <init>} stay as is.
	 * <p>
	 * Returns the original string unchanged when no parenthesised descriptor is
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
	 * <p>
	 * Examples:
	 * <ul>
	 * <li>{@code "Ljava/lang/String;Ljava/io/File;"} →
	 * {@code [java.lang.String, java.io.File]}</li>
	 * <li>{@code "[I"} → {@code [int[]]}</li>
	 * <li>{@code "[[Ljava/lang/String;"} → {@code [java.lang.String[][]]}</li>
	 * </ul>
	 * <p>
	 * Malformed descriptors stop the parse early; whatever has been parsed so far
	 * is returned to keep the surrounding error message useful.
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
	 * <p>
	 * {@code V} (void) is included for completeness, even though it never appears
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
