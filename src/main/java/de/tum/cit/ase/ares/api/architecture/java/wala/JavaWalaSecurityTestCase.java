package de.tum.cit.ase.ares.api.architecture.java.wala;

import com.ibm.wala.classLoader.Language;
import com.ibm.wala.core.java11.Java9AnalysisScopeReader;
import com.ibm.wala.ipa.callgraph.*;
import com.ibm.wala.ipa.callgraph.impl.DefaultEntrypoint;
import com.ibm.wala.ipa.callgraph.impl.Util;
import com.ibm.wala.ipa.callgraph.propagation.InstanceKey;
import com.ibm.wala.ipa.cha.ClassHierarchy;
import com.ibm.wala.ipa.cha.ClassHierarchyException;
import com.ibm.wala.ipa.cha.ClassHierarchyFactory;
import de.tum.cit.ase.ares.api.architecture.ArchitectureSecurityTestCase;
import de.tum.cit.ase.ares.api.architecture.java.archunit.JavaArchUnitTestCaseSupported;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static de.tum.cit.ase.ares.api.localization.Messages.localized;

/**
 * A utility class to check security rules in a call graph.
 */
public class JavaWalaSecurityTestCase implements ArchitectureSecurityTestCase {

    /**
     * Selects the supported architecture test case in the Java programming language.
     */
    // TODO Sarp: Generalize this
    private final JavaArchUnitTestCaseSupported javaArchitectureTestCaseSupported;

    public JavaWalaSecurityTestCase(JavaArchUnitTestCaseSupported javaSecurityTestCaseSupported) {
        this.javaArchitectureTestCaseSupported = javaSecurityTestCaseSupported;
    }

    /**
     * Build a call graph from a class path.
     */
    public static CallGraph buildCallGraph(String classPathToConsider, String classPathToAnalyze, Predicate<CGNode> securityViolationCheck) {
        try {
            AnalysisScope scope = Java9AnalysisScopeReader.instance.makeJavaBinaryAnalysisScope(
                    classPathToConsider,
                    new File("src/main/java/de/tum/cit/ase/ares/api/architecture/java/wala/exclusions.txt")
            );

            // Build the class hierarchy
            ClassHierarchy cha = ClassHierarchyFactory.make(scope);

            // Create a list to store entry points
            List<DefaultEntrypoint> customEntryPoints = ReachabilityChecker.getEntryPointsFromStudentSubmission(classPathToAnalyze, cha);

            // Create AnalysisOptions for call graph
            AnalysisOptions options = new AnalysisOptions(scope, customEntryPoints);
            options.setTraceStringConstants(false);
            options.setHandleZeroLengthArray(false);
            options.setReflectionOptions(AnalysisOptions.ReflectionOptions.NONE);

            // Create call graph builder (n-CFA, context-sensitive, etc.)
            CallGraphBuilder<InstanceKey> builder = Util.makeZeroOneCFABuilder(Language.JAVA, options, new AnalysisCacheImpl(), cha);

            // Generate the call graph
            CallGraph callGraph = builder.makeCallGraph(options, null);

            List<CGNode> violatingMethods = ReachabilityChecker.findReachableMethods(callGraph, callGraph.getEntrypointNodes().iterator(), securityViolationCheck);

            if (violatingMethods != null && !violatingMethods.isEmpty()) {
                throw new SecurityException(String.format(
                        "Security violation: Detected %d unauthorized API call(s):%n%s",
                        violatingMethods.size(),
                        violatingMethods.stream()
                                .map(node -> String.format("- %s", node.getMethod().getSignature()))
                                .collect(Collectors.joining("%n"))
                ));
            }
            return callGraph;
        } catch (CallGraphBuilderCancelException | ClassHierarchyException | IOException e) {
            throw new SecurityException("Error building call graph", e); //$NON-NLS-1$
        }
    }

    @Override
    public String writeArchitectureTestCase() {
        // TODO: For further releases
        return "";
    }

    @Override
    public void executeArchitectureTestCase(Object path) {
        if (!(path instanceof String claasPath)) {
            throw new IllegalArgumentException(localized("security.archunit.invalid.argument"));
        }
        try {
            // TODO Sarp: implement this more efficiently only pass the predicate that we need!!!!! also fpr ArchUnit
            switch (this.javaArchitectureTestCaseSupported) {
                default -> throw new UnsupportedOperationException("Not implemented yet");
            }
        } catch (AssertionError e) {
            String[] split = null;
            if (e.getMessage() == null || e.getMessage().split("\n").length < 2) {
                throw new SecurityException(localized("security.archunit.illegal.execution", e.getMessage()));
            }
            split = e.getMessage().split("\n");
            throw new SecurityException(localized("security.archunit.violation.error", split[0].replaceAll(".*?'(.*?)'.*", "$1"), split[1]));
        }
    }
}
