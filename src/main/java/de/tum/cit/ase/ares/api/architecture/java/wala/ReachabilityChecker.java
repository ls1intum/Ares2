package de.tum.cit.ase.ares.api.architecture.java.wala;

import com.ibm.wala.classLoader.IClass;
import com.ibm.wala.classLoader.IMethod;
import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ipa.callgraph.CallGraph;
import com.ibm.wala.ipa.callgraph.impl.DefaultEntrypoint;
import com.ibm.wala.ipa.cha.ClassHierarchy;
import com.ibm.wala.types.ClassLoaderReference;
import com.ibm.wala.util.graph.traverse.DFSPathFinder;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Predicate;

/**
 * A utility class to check reachability in a call graph.
 */
class ReachabilityChecker {

    private ReachabilityChecker() {
        // Utility class
    }

    /**
     * Checks if a target node is reachable from a set of start nodes in a call graph.
     * @param callGraph The call graph to search in.
     * @param startNodes The start nodes to search from.
     * @param targetNodeFilter A filter to determine if a node is a target node.
     * @return A list of nodes that are reachable from the start nodes and match the target node filter.
     */
    public static List<CGNode> isReachable(CallGraph callGraph, Iterator<CGNode> startNodes, Predicate<CGNode> targetNodeFilter) {
        // TODO: Implement our own pathfinder instead of the default
        return new DFSPathFinder<>(callGraph, startNodes, targetNodeFilter).find();
    }

    // TODO: This is currently not working as expected!!!
    public static List<DefaultEntrypoint> getEntryPointsFromStudentSubmission(CallGraph callGraph, Iterator<CGNode> startNodes, ClassHierarchy cha, ClassHierarchy chaClassPath) {
        // Iterate through all classes in the application classloader
        List<DefaultEntrypoint> customEntryPoints = new ArrayList<>();
            for (IClass klass : cha) {
                if (klass.getClassLoader().getReference().equals(ClassLoaderReference.Application)) {
                    // Iterate through all declared methods in each class
                    for (IMethod method : klass.getDeclaredMethods()) {
                        // Exclude the 'main' methods from being entry points
                        if (!method.getName().toString().equals("main")) {
                            customEntryPoints.add(new DefaultEntrypoint(method.getReference(), chaClassPath));
                        }
                    }
                }
            }
        return customEntryPoints;
    }
}

