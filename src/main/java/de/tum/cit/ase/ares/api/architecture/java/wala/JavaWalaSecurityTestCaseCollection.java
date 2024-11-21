package de.tum.cit.ase.ares.api.architecture.java.wala;

import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ipa.callgraph.CallGraph;
import com.ibm.wala.shrike.shrikeCT.InvalidClassFileException;
import de.tum.cit.ase.ares.api.architecture.java.archunit.FileHandlerConstants;

import java.nio.file.Path;
import java.util.List;
import java.util.Set;

import static de.tum.cit.ase.ares.api.aop.java.instrumentation.advice.JavaInstrumentationAdviceToolbox.localize;
import static de.tum.cit.ase.ares.api.architecture.java.CallGraphBuilderUtils.getForbiddenMethods;

public class JavaWalaSecurityTestCaseCollection {

    public static void noReflection(CallGraph cg) {
        createNoClassShouldHaveMethodRule(
                localize("security.architecture.reflection.uses"),
                FileHandlerConstants.JAVA_REFLECTION_METHODS,
                cg
        );
    }

    public static void noFileSystemAccess(CallGraph cg) {
        createNoClassShouldHaveMethodRule(
                localize("security.architecture.file.system.access"),
                FileHandlerConstants.JAVA_FILESYSTEM_INTERACTION_METHODS,
                cg);
    }

    // create a helper method reachability checker
    public static void noNetworkAccess(CallGraph cg) {
        createNoClassShouldHaveMethodRule(
                localize("security.architecture.network.access"),
                FileHandlerConstants.JAVA_NETWORK_ACCESS_METHODS,
                cg
        );
    }

    public static void noJVMTermination(CallGraph cg) {
        createNoClassShouldHaveMethodRule(
                localize("security.architecture.terminate.jvm"),
                FileHandlerConstants.JAVA_JVM_TERMINATION_METHODS,
                cg
        );
    }

    public static void noCommandExecution(CallGraph cg) {
        createNoClassShouldHaveMethodRule(
                localize("security.architecture.execute.command"),
                FileHandlerConstants.JAVA_COMMAND_EXECUTION_METHODS,
                cg
        );
    }

    public static void noThreadCreation(CallGraph cg) {
        createNoClassShouldHaveMethodRule(
                localize("security.architecture.manipulate.threads"),
                FileHandlerConstants.JAVA_THREAD_CREATION_METHODS,
                cg
        );
    }

    private static void createNoClassShouldHaveMethodRule(
            String ruleName,
            Path methodsFilePath,
            CallGraph cg
    ) {
        Set<String> forbiddenMethods = getForbiddenMethods(methodsFilePath);
        List<CGNode> reachableNodes= ReachabilityChecker.findReachableMethods(cg, cg.getEntrypointNodes().iterator(),
                        cgNode -> forbiddenMethods.stream()
                                .anyMatch(method -> cgNode
                                        .getMethod()
                                        .getSignature()
                                        .startsWith(method)));

        // TODO Sarp: Error message
        try {
            throw new AssertionError(reachableNodes.getFirst().getMethod().getSignature() + " " + ruleName + "\n" + reachableNodes.getLast().getMethod().getSourcePosition(0).getFirstLine());
        } catch (InvalidClassFileException e) {
            e.printStackTrace();
        }
    }
}
