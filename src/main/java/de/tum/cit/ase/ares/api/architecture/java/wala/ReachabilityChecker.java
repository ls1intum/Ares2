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
import de.tum.cit.ase.ares.api.aop.java.instrumentation.advice.JavaInstrumentationAdviceFileSystemToolbox;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Predicate;

/**
 * A utility class to check reachability in a call graph.
 */
public class ReachabilityChecker {
    //<editor-fold desc="Constructor">
    private ReachabilityChecker() {
        throw new SecurityException(JavaInstrumentationAdviceFileSystemToolbox.localize("security.general.utility.initialization", ReachabilityChecker.class.getName()));
    }
    //</editor-fold>

    /**
     * Checks if a target node is reachable from a set of start nodes in a call graph.
     *
     * @param callGraph        The call graph to search in.
     * @param startNodes       The start nodes to search from.
     * @param targetNodeFilter A filter to determine if a node is a target node.
     * @return A list of nodes that are reachable from the start nodes and match the target node filter.
     */
    public static List<CGNode> findReachableMethods(CallGraph callGraph, Iterator<CGNode> startNodes, Predicate<CGNode> targetNodeFilter) {
        if (callGraph == null) {
            throw new SecurityException(JavaInstrumentationAdviceFileSystemToolbox.localize("security.common.not.null", "CallGraph"));
        }
        if (startNodes == null) {
            throw new SecurityException(JavaInstrumentationAdviceFileSystemToolbox.localize("security.common.not.null", "startNodes"));
        }
        if (targetNodeFilter == null) {
            throw new SecurityException(JavaInstrumentationAdviceFileSystemToolbox.localize("security.common.not.null", "targetNodeFilter"));
        }
        return new CustomDFSPathFinder(callGraph, startNodes, targetNodeFilter).find();
    }

    private static ClassHierarchy createClassHierarchy(String classPath) throws IOException, ClassHierarchyException {
        return ClassHierarchyFactory.make(
                AnalysisScopeReader
                        .instance
                        .makeJavaBinaryAnalysisScope(classPath, null)
        );
    }

    /**
     * Get entry points from a student submission.
     *
     * @param classPath                The path to the student submission.
     * @param applicationClassHierarchy The class hierarchy of the application.
     * @return A list of entry points from the student submission.
     */
    public static List<DefaultEntrypoint> getEntryPointsFromStudentSubmission(String classPath, ClassHierarchy applicationClassHierarchy) {
        if (classPath == null || classPath.trim().isEmpty()) {
            throw new SecurityException(JavaInstrumentationAdviceFileSystemToolbox.localize("security.common.not.null", "classPath"));
        }
        if (applicationClassHierarchy == null) {
            throw new SecurityException(JavaInstrumentationAdviceFileSystemToolbox.localize("security.common.not.null", "ClassHierarchy"));
        }
        try {
            return new ArrayList<>(
                    io.vavr.collection.Stream.ofAll(createClassHierarchy(classPath))
                            .toJavaStream()
                            .filter(iClass -> iClass.getClassLoader().getReference().equals(ClassLoaderReference.Application))
                            .map(IClass::getDeclaredMethods)
                            .map(io.vavr.collection.Stream::ofAll)
                            .flatMap(io.vavr.collection.Stream::toJavaStream)
                            .filter(iMethod -> !iMethod.getName().toString().equals("main"))
                            .map(IMethod::getReference)
                            .map(methodReference -> new DefaultEntrypoint(methodReference, applicationClassHierarchy))
                            .toList()
            );
        } catch (ClassHierarchyException | IOException e) {
            throw new SecurityException(JavaInstrumentationAdviceFileSystemToolbox.localize("security.architecture.class.hierarchy.error"));
        } catch (com.ibm.wala.util.debug.UnimplementedError e) {
            throw new SecurityException(JavaInstrumentationAdviceFileSystemToolbox.localize("security.architecture.class.hierarchy.error"));
        }
    }
}

