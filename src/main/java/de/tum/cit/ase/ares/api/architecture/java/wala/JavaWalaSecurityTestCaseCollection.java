package de.tum.cit.ase.ares.api.architecture.java.wala;

import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ipa.callgraph.CallGraph;
import de.tum.cit.ase.ares.api.architecture.java.CallGraphBuilderUtils;
import de.tum.cit.ase.ares.api.architecture.java.JavaArchitecturalTestCaseSupported;

import java.util.function.Predicate;

public class JavaWalaSecurityTestCaseCollection {

    public static void noReflection(CallGraph cg) {
        // TODO Sarp: Create predicate self
        Predicate<CGNode> targetNodeFilter = node -> true;
        ReachabilityChecker.findReachableMethods(cg, cg.getEntrypointNodes().iterator(), targetNodeFilter);
    }
}
