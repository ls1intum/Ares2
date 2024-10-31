package de.tum.cit.ase.ares.api.architecture.java.wala;

import com.ibm.wala.core.java11.Java9AnalysisScopeReader;
import com.ibm.wala.ipa.callgraph.*;
import com.ibm.wala.ipa.callgraph.impl.DefaultEntrypoint;
import com.ibm.wala.ipa.callgraph.impl.Util;
import com.ibm.wala.ipa.callgraph.propagation.InstanceKey;
import com.ibm.wala.ipa.cha.ClassHierarchy;
import com.ibm.wala.ipa.cha.ClassHierarchyException;
import com.ibm.wala.ipa.cha.ClassHierarchyFactory;

import java.io.IOException;
import java.util.List;

/**
 * A utility class to check security rules in a call graph.
 */
public class JavaWalaSecurityTestCase {

    /**
     * Build a call graph from a class path.
     */
    public static CallGraph buildCallGraph(String classPath) {
        try {
            long startTime = System.currentTimeMillis();

            AnalysisScope scope = Java9AnalysisScopeReader.instance.makeJavaBinaryAnalysisScope(
                    System.getProperty("java.class.path"),
                    null
            );

            // Build the class hierarchy
            ClassHierarchy cha = ClassHierarchyFactory.make(scope);

            // Create a list to store entry points
            List<DefaultEntrypoint> customEntryPoints = ReachabilityChecker.getEntryPointsFromStudentSubmission(classPath, cha);

            // Create AnalysisOptions for call graph
            AnalysisOptions options = new AnalysisOptions(scope, customEntryPoints);
            options.setTraceStringConstants(false);
            options.setHandleZeroLengthArray(false);
            options.setReflectionOptions(AnalysisOptions.ReflectionOptions.NONE);

            // Create call graph builder (n-CFA, context-sensitive, etc.)
            CallGraphBuilder<InstanceKey> builder = Util.makeNCFABuilder(1, options, new AnalysisCacheImpl(), cha);

            // Generate the call graph
            CallGraph callGraph = builder.makeCallGraph(options, null);

            List<CGNode> violatingMethods = ReachabilityChecker.isReachable(callGraph, callGraph.getEntrypointNodes().iterator(), node -> node.getMethod().getSignature().startsWith("sun.nio.fs.UnixFileSystemProvider") || node.getMethod().getSignature().startsWith("java.lang.CLassLoader.findResource"));

            if (violatingMethods != null && !violatingMethods.isEmpty()) {
                throw new SecurityException(violatingMethods.toString());
            }
            return callGraph;
        } catch (CallGraphBuilderCancelException | ClassHierarchyException | IOException e) {
            throw new SecurityException("Error building call graph", e); //$NON-NLS-1$
        }
    }
}
