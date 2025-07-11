package de.tum.cit.ase.ares.api.architecture.java.wala;

import com.google.common.collect.Iterables;
import com.ibm.wala.classLoader.IMethod;
import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ipa.callgraph.CallGraph;
import com.ibm.wala.shrike.shrikeCT.InvalidClassFileException;
import de.tum.cit.ase.ares.api.aop.java.instrumentation.advice.JavaInstrumentationAdviceFileSystemToolbox;

import java.util.List;
import java.util.Set;

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

        if (reachableNodes == null || Iterables.isEmpty(reachableNodes)) {
            return;
        }
        try {
            IMethod.SourcePosition sourcePosition = reachableNodes.getLast().getMethod().getSourcePosition(0);
            int lineNumber = sourcePosition != null ? sourcePosition.getFirstLine() : -1;
            throw new AssertionError(JavaInstrumentationAdviceFileSystemToolbox.localize("security.architecture.method.call.message",
                    ruleName,
                    reachableNodes.getLast().getMethod().getSignature(),
                    reachableNodes.get(reachableNodes.size() - 2).getMethod().getSignature(),
                    reachableNodes.getLast().getMethod().getDeclaringClass().getName().getClassName().toString(),
                    lineNumber,
                    reachableNodes.getFirst().getMethod().getSignature()
            ));
        } catch (InvalidClassFileException e) {
            throw new SecurityException(JavaInstrumentationAdviceFileSystemToolbox.localize("security.architecture.invalid.class.file"));
        }
    }
}
