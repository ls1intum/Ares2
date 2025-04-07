package de.tum.cit.ase.ares.api.architecture.java.wala;

import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ipa.callgraph.CallGraph;
import com.ibm.wala.shrike.shrikeCT.InvalidClassFileException;

import java.util.List;
import java.util.Set;

import static com.google.common.collect.Iterables.isEmpty;
import static de.tum.cit.ase.ares.api.aop.java.instrumentation.advice.JavaInstrumentationAdviceFileSystemToolbox.localize;

public class WalaRule {
    String ruleName;
    Set<String> forbiddenMethods;

    public WalaRule(String ruleName, Set<String> forbiddenMethods) {
        this.ruleName = ruleName;
        this.forbiddenMethods = forbiddenMethods;
    }

    public void check(CallGraph cg) {
        List<CGNode> reachableNodes= ReachabilityChecker.findReachableMethods(cg, cg.getEntrypointNodes().iterator(),
                cgNode -> forbiddenMethods.stream()
                        .anyMatch(method -> cgNode
                                .getMethod()
                                .getSignature()
                                .startsWith(method)));

        if (reachableNodes == null || isEmpty(reachableNodes)) {
            return;
        }
        try {
            throw new AssertionError(localize("security.architecture.method.call.message",
                    ruleName,
                    reachableNodes.getLast().getMethod().getSignature(),
                    reachableNodes.get(reachableNodes.size() - 2).getMethod().getSignature(),
                    reachableNodes.getLast().getMethod().getDeclaringClass().getName().getClassName().toString(),
                    reachableNodes.getLast().getMethod().getSourcePosition(0).getFirstLine(),
                    reachableNodes.getFirst().getMethod().getSignature()
            ));
        } catch (InvalidClassFileException e) {
            throw new SecurityException(localize("security.architecture.invalid.class.file"));
        }
    }
}
