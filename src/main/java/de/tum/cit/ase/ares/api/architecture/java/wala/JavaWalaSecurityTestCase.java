package de.tum.cit.ase.ares.api.architecture.java.wala;

import com.ibm.wala.classLoader.IClass;
import com.ibm.wala.classLoader.IMethod;
import com.ibm.wala.classLoader.Language;
import com.ibm.wala.core.util.config.AnalysisScopeReader;
import com.ibm.wala.ipa.callgraph.*;
import com.ibm.wala.ipa.callgraph.impl.DefaultEntrypoint;
import com.ibm.wala.ipa.callgraph.impl.Util;
import com.ibm.wala.ipa.callgraph.propagation.InstanceKey;
import com.ibm.wala.ipa.cha.ClassHierarchy;
import com.ibm.wala.ipa.cha.ClassHierarchyException;
import com.ibm.wala.ipa.cha.ClassHierarchyFactory;
import com.ibm.wala.types.ClassLoaderReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class JavaWalaSecurityTestCase {

    private static final Logger log = LoggerFactory.getLogger(JavaWalaSecurityTestCase.class);

    public static CallGraph buildCallGraph() {
        try {
            // Create an AnalysisScope for the Java 21 JRT modules
            AnalysisScope.createJavaAnalysisScope();
            // get entire classpath of the project
            String classpath = System.getProperty("java.class.path");
            AnalysisScope scope = AnalysisScopeReader.instance.makeJavaBinaryAnalysisScope(
                    classpath,
                    null);

            // Build the class hierarchy
            ClassHierarchy chaWholeApplication = ClassHierarchyFactory.make(scope);

            // Create a list to store entry points
            List<Entrypoint> customEntryPoints = new ArrayList<>();

            // Create AnalysisOptions for call graph
            AnalysisOptions options = new AnalysisOptions(scope, customEntryPoints);


            // Create call graph builder (n-CFA, context-sensitive, etc.)
            CallGraphBuilder<InstanceKey> builder = Util.makeZeroCFABuilder(Language.JAVA, options, new AnalysisCacheImpl(), chaWholeApplication);

            long startTime = System.currentTimeMillis();
            // Generate the call graph
            CallGraph callGraph = builder.makeCallGraph(options, null);
            long endTime = System.currentTimeMillis();

            log.info("Building call graph took {} ms", endTime - startTime);

            List<CGNode> violatingMethods = ReachabilityChecker.isReachable(callGraph, callGraph.getEntrypointNodes().iterator(), node -> node.getMethod().getSignature().startsWith("sun.nio.fs.UnixFileSystemProvider") || node.getMethod().getSignature().startsWith("java.security.AccessController") || node.getMethod().getSignature().startsWith("java.lang.CLassLoader.findResource"));
            log.info("Violating methods: {}", violatingMethods);
            return callGraph;
        } catch (IOException  | CallGraphBuilderCancelException | ClassHierarchyException e) {
            throw new SecurityException("Error building call graph", e); //$NON-NLS-1$
        }
    }

    // Write the CallGraph to a DOT file
    public static void writeCallGraphToDot(CallGraph callGraph, String filePath) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            writer.write("digraph G {\n");

            // Iterate through the nodes in the call graph
            for (CGNode node : callGraph) {
                // Write the node representation
                writer.write("    \"" + node.getMethod().getSignature() + "\";\n");

                // Iterate through the edges (call sites) from the current node
                for (Iterator<CGNode> it = callGraph.getSuccNodes(node); it.hasNext(); ) {
                    CGNode successor = it.next();
                    // Write the edge representation
                    writer.write("    \"" + node.getMethod().getSignature() + "\" -> \"" + successor.getMethod().getSignature() + "\";\n");
                }
            }
            writer.write("}\n");
        }
    }

    public static void main(String[] args) throws IOException {
        CallGraph callGraph = buildCallGraph();
        writeCallGraphToDot(callGraph, "callgraph.dot");
    }
}
