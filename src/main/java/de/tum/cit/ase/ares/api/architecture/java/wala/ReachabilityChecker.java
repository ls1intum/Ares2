package de.tum.cit.ase.ares.api.architecture.java.wala;

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
import com.ibm.wala.util.graph.traverse.DFSPathFinder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Predicate;

/**
 * A utility class to check reachability in a call graph.
 */
public class ReachabilityChecker {

    private ReachabilityChecker() {
        // Utility class
    }

    /**
     * Checks if a target node is reachable from a set of start nodes in a call graph.
     *
     * @param callGraph        The call graph to search in.
     * @param startNodes       The start nodes to search from.
     * @param targetNodeFilter A filter to determine if a node is a target node.
     * @return A list of nodes that are reachable from the start nodes and match the target node filter.
     */
    public static List<CGNode> findReachableMethods(CallGraph callGraph, Iterator<CGNode> startNodes, Predicate<CGNode> targetNodeFilter) {
//        TODO: translate these messages
        if (callGraph == null) {
            throw new IllegalArgumentException("Call graph cannot be null");
        }
        if (startNodes == null) {
            throw new IllegalArgumentException("Start nodes cannot be null");
        }
        if (targetNodeFilter == null) {
            throw new IllegalArgumentException("Target node filter cannot be null");
        }
        return new DFSPathFinder<>(callGraph, startNodes, targetNodeFilter).find();
    }

    /**
     * Get entry points from a student submission.
     *
     * @param classPath      The class path of the student submission.
     * @param applicationCha The class hierarchy of the application.
     * @return A list of entry points from the student submission.
     */
    public static List<DefaultEntrypoint> getEntryPointsFromStudentSubmission(String classPath, ClassHierarchy applicationCha) {
        if (classPath == null || classPath.trim().isEmpty()) {
            throw new IllegalArgumentException("Class path cannot be null or empty");
        }
        if (applicationCha == null) {
            throw new IllegalArgumentException("Application class hierarchy cannot be null");
        }

        // Create CHA of the student submission
        ClassHierarchy targetClasses;
        try {
            targetClasses = ClassHierarchyFactory
                    .make(AnalysisScopeReader.instance
                            .makeJavaBinaryAnalysisScope(classPath, null));
        } catch (ClassHierarchyException | IOException e) {
            throw new SecurityException("Could not create class hierarchy for student submission", e); // $NON-NLS-1$
        }

        // Iterate through all classes in the application classloader
        List<DefaultEntrypoint> customEntryPoints = new ArrayList<>();
        for (IClass klass : targetClasses) {
            if (klass.getClassLoader().getReference().equals(ClassLoaderReference.Application)) {
                // Iterate through all declared methods in each class
                for (IMethod method : klass.getDeclaredMethods()) {
                    // Exclude the 'main' methods from being entry points
                    if (!method.getName().toString().equals("main")) {
                        customEntryPoints.add(new DefaultEntrypoint(method.getReference(), applicationCha));
                    }
                }
            }
        }
        return customEntryPoints;
    }
}

