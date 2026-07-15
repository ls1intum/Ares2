package de.tum.cit.ase.ares.api.architecture.java.wala;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import com.ibm.wala.classLoader.IMethod;
import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ipa.callgraph.CallGraph;
import com.ibm.wala.ipa.callgraph.impl.DefaultEntrypoint;
import com.ibm.wala.ipa.cha.ClassHierarchy;
import com.ibm.wala.types.ClassLoaderReference;

import de.tum.cit.ase.ares.api.localization.Messages;

/**
 * A utility class to check reachability in a call graph.
 */
public final class ReachabilityChecker {
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
			throw new SecurityException(
					Messages.localized("security.common.not.null", "CallGraph", "findReachableMethods"));
		}
		if (startNodes == null) {
			throw new SecurityException(
					Messages.localized("security.common.not.null", "startNodes", "findReachableMethods"));
		}
		if (targetNodeFilter == null) {
			throw new SecurityException(
					Messages.localized("security.common.not.null", "targetNodeFilter", "findReachableMethods"));
		}
		return new CustomDFSPathFinder(callGraph, startNodes, targetNodeFilter).find();
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
			throw new SecurityException(
					Messages.localized("security.common.not.null", "classPath", "getEntryPointsFromStudentSubmission"));
		}
		if (applicationClassHierarchy == null) {
			throw new SecurityException(Messages.localized("security.common.not.null", "ClassHierarchy",
					"getEntryPointsFromStudentSubmission"));
		}
		List<DefaultEntrypoint> entryPoints = StreamSupport.stream(applicationClassHierarchy.spliterator(), false)
				.filter(iClass -> iClass.getClassLoader().getReference().equals(ClassLoaderReference.Application))
				.flatMap(iClass -> iClass.getDeclaredMethods().stream()).map(IMethod::getReference)
				.map(methodReference -> new DefaultEntrypoint(methodReference, applicationClassHierarchy))
				.collect(Collectors.toCollection(ArrayList::new));
		return Collections.unmodifiableList(entryPoints);
	}
}
