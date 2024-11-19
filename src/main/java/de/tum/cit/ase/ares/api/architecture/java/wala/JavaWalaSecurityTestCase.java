package de.tum.cit.ase.ares.api.architecture.java.wala;

import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ipa.callgraph.CallGraph;
import de.tum.cit.ase.ares.api.architecture.ArchitectureSecurityTestCase;
import de.tum.cit.ase.ares.api.architecture.java.JavaArchitecturalTestCaseSupported;

import java.util.function.Predicate;

import static de.tum.cit.ase.ares.api.localization.Messages.localized;

/**
 * A utility class to check security rules in a call graph.
 */
public class JavaWalaSecurityTestCase implements ArchitectureSecurityTestCase {

    /**
     * Selects the supported architecture test case in the Java programming language.
     */
    private final JavaArchitecturalTestCaseSupported javaArchitectureTestCaseSupported;

    private final Predicate<CGNode> targetNodeFilter;

    public JavaWalaSecurityTestCase(JavaArchitecturalTestCaseSupported javaSecurityTestCaseSupported, Predicate<CGNode> targetNodeFilter) {
        this.javaArchitectureTestCaseSupported = javaSecurityTestCaseSupported;
        this.targetNodeFilter = targetNodeFilter;
    }

    @Override
    public String writeArchitectureTestCase() {
        // TODO: For further releases
        return "";
    }

    @Override
    public void executeArchitectureTestCase(Object callGraph) {
        if (!(callGraph instanceof CallGraph cg)) {
            throw new IllegalArgumentException(localized("security.archunit.invalid.argument"));
        }
        try {
            switch (this.javaArchitectureTestCaseSupported) {
                case REFLECTION -> ReachabilityChecker.findReachableMethods(cg, cg.getEntrypointNodes().iterator(), targetNodeFilter);
                case FILESYSTEM_INTERACTION -> throw new UnsupportedOperationException("Not implemented yet");
                case TERMINATE_JVM -> throw new UnsupportedOperationException("Not implemented yet");
                case NETWORK_CONNECTION -> throw new UnsupportedOperationException("Not implemented yet");
                case COMMAND_EXECUTION -> throw new UnsupportedOperationException("Not implemented yet");
                case PACKAGE_IMPORT -> throw new UnsupportedOperationException("Not implemented yet");
                case THREAD_CREATION -> throw new UnsupportedOperationException("Not implemented yet");
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
