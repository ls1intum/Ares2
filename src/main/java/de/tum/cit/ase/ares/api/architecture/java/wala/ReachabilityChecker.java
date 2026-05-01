package de.tum.cit.ase.ares.api.architecture.java.wala;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;

import com.ibm.wala.classLoader.IClass;
import com.ibm.wala.classLoader.IMethod;
import com.ibm.wala.core.util.config.AnalysisScopeReader;
import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ipa.callgraph.CallGraph;
import com.ibm.wala.ipa.callgraph.impl.DefaultEntrypoint;
import com.ibm.wala.ipa.cha.ClassHierarchy;
import com.ibm.wala.ipa.cha.ClassHierarchyException;
import com.ibm.wala.ipa.cha.ClassHierarchyFactory;
import com.ibm.wala.types.ClassLoaderReference;
import com.ibm.wala.util.debug.UnimplementedError;

import de.tum.cit.ase.ares.api.localization.Messages;

/**
 * A utility class to check reachability in a call graph.
 */
public class ReachabilityChecker {
	// <editor-fold desc="Constructor">
	private ReachabilityChecker() {
		throw new SecurityException(
				Messages.localized("security.general.utility.initialization", "ReachabilityChecker"));
	}
	// </editor-fold>

	/**
	 * Checks if a target node is reachable from a set of start nodes in a call
	 * graph.
	 *
	 * @param callGraph        The call graph to search in.
	 * @param startNodes       The start nodes to search from.
	 * @param targetNodeFilter A filter to determine if a node is a target node.
	 * @return A list of nodes that are reachable from the start nodes and match the
	 *         target node filter.
	 */
	public static List<CGNode> findReachableMethods(CallGraph callGraph, Iterator<CGNode> startNodes,
			Predicate<CGNode> targetNodeFilter) {
		if (callGraph == null) {
			throw new SecurityException(Messages.localized("security.common.not.null", "CallGraph", "findReachableMethods"));
		}
		if (startNodes == null) {
			throw new SecurityException(Messages.localized("security.common.not.null", "startNodes", "findReachableMethods"));
		}
		if (targetNodeFilter == null) {
			throw new SecurityException(Messages.localized("security.common.not.null", "targetNodeFilter", "findReachableMethods"));
		}
		return new CustomDFSPathFinder(callGraph, startNodes, targetNodeFilter).find();
	}

	/**
	 * Cache for the entry-point list keyed by {@code (applicationClassHierarchy
	 * identity, classPath)}. The hierarchy reference is part of the key because
	 * {@link DefaultEntrypoint} captures it: a different application hierarchy must
	 * not reuse another's entry points.
	 *
	 * <p>The intermediate {@link ClassHierarchy} created by {@link #createClassHierarchy}
	 * is intentionally NOT cached separately. Each inner hierarchy reloads the full
	 * JDK primordial scope, so caching ~13 hierarchies per phase exhausts the 512 MB
	 * Gradle test-worker heap and crashes the JVM (observed empirically: testPermitted
	 * died with "Could not complete execution for Gradle Test Executor"). Caching the
	 * entry-point list alone is sufficient: on cache hit the inner-hierarchy build is
	 * skipped entirely; on miss the hierarchy is built once, used to enumerate methods,
	 * and then released for GC since {@link DefaultEntrypoint} only retains references
	 * to {@link com.ibm.wala.types.MethodReference} and the application hierarchy.
	 */
	private static final ConcurrentHashMap<String, List<DefaultEntrypoint>> ENTRYPOINTS_CACHE = new ConcurrentHashMap<>();

	private static ClassHierarchy createClassHierarchy(String classPath) throws IOException, ClassHierarchyException {
		return ClassHierarchyFactory.make(AnalysisScopeReader.instance.makeJavaBinaryAnalysisScope(classPath, null));
	}

	/**
	 * Get entry points from a student submission.
	 *
	 * @param classPath                 The path to the student submission.
	 * @param applicationClassHierarchy The class hierarchy of the application.
	 * @return A list of entry points from the student submission.
	 */
	public static List<DefaultEntrypoint> getEntryPointsFromStudentSubmission(String classPath,
			ClassHierarchy applicationClassHierarchy) {
		if (classPath == null || classPath.trim().isEmpty()) {
			throw new SecurityException(Messages.localized("security.common.not.null", "classPath", "getEntryPointsFromStudentSubmission"));
		}
		if (applicationClassHierarchy == null) {
			throw new SecurityException(Messages.localized("security.common.not.null", "ClassHierarchy", "getEntryPointsFromStudentSubmission"));
		}
		String cacheKey = System.identityHashCode(applicationClassHierarchy) + "::" + classPath;
		List<DefaultEntrypoint> cached = ENTRYPOINTS_CACHE.get(cacheKey);
		if (cached != null) {
			return cached;
		}
		try {
			List<DefaultEntrypoint> fresh = new ArrayList<>(io.vavr.collection.Stream.ofAll(createClassHierarchy(classPath)).toJavaStream()
					.filter(iClass -> iClass.getClassLoader().getReference().equals(ClassLoaderReference.Application))
					.map(IClass::getDeclaredMethods).map(io.vavr.collection.Stream::ofAll)
					.flatMap(io.vavr.collection.Stream::toJavaStream)
					.map(IMethod::getReference)
					.map(methodReference -> new DefaultEntrypoint(methodReference, applicationClassHierarchy))
					.toList());
			List<DefaultEntrypoint> immutable = Collections.unmodifiableList(fresh);
			List<DefaultEntrypoint> existing = ENTRYPOINTS_CACHE.putIfAbsent(cacheKey, immutable);
			return existing != null ? existing : immutable;
		} catch (ClassHierarchyException | IOException | UnimplementedError e) {
			throw new SecurityException(Messages.localized("security.architecture.class.hierarchy.error"));
		}
	}
}
