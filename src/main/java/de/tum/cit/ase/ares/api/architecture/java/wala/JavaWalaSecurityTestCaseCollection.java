package de.tum.cit.ase.ares.api.architecture.java.wala;

import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ipa.callgraph.CallGraph;
import com.ibm.wala.shrike.shrikeCT.InvalidClassFileException;
import de.tum.cit.ase.ares.api.architecture.java.FileHandlerConstants;

import java.nio.file.Path;
import java.util.List;
import java.util.Set;

import static de.tum.cit.ase.ares.api.aop.java.instrumentation.advice.JavaInstrumentationAdviceToolbox.localize;
import static de.tum.cit.ase.ares.api.architecture.java.CallGraphBuilderUtils.getForbiddenMethods;

/**
 * Collection of security test cases that analyze Java applications using WALA framework.
 * This class provides static methods to verify that analyzed code does not:
 * - Use reflection
 * - Access file system
 * - Access network
 * - Terminate JVM
 * - Execute system commands
 * - Create threads
 *
 * Each security check method accepts a CallGraph and throws AssertionError if a violation is found.
 *
 * @see com.ibm.wala.ipa.callgraph.CallGraph
 * @see ReachabilityChecker
 */
public class JavaWalaSecurityTestCaseCollection {

    /**
     * No reflection test case.
     */
    public static void noReflection(CallGraph cg) {
        createNoClassShouldHaveMethodRule(
                localize("security.architecture.reflection.uses"),
                FileHandlerConstants.WALA_REFLECTION_METHODS,
                cg
        );
    }

    /**
     * No file system access test case.
     */
    public static void noFileSystemAccess(CallGraph cg) {
        createNoClassShouldHaveMethodRule(
                localize("security.architecture.file.system.access"),
                FileHandlerConstants.WALA_FILESYSTEM_METHODS,
                cg);
    }

    /**
     * No network access test case.
     */
    public static void noNetworkAccess(CallGraph cg) {
        createNoClassShouldHaveMethodRule(
                localize("security.architecture.network.access"),
                FileHandlerConstants.WALA_NETWORK_METHODS,
                cg
        );
    }

    /**
     * No JVM termination test case.
     */
    public static void noJVMTermination(CallGraph cg) {
        createNoClassShouldHaveMethodRule(
                localize("security.architecture.terminate.jvm"),
                FileHandlerConstants.WALA_JVM_METHODS,
                cg
        );
    }

    /**
     * No command execution test case.
     */
    public static void noCommandExecution(CallGraph cg) {
        createNoClassShouldHaveMethodRule(
                localize("security.architecture.execute.command"),
                FileHandlerConstants.WALA_COMMAND_EXECUTION_METHODS,
                cg
        );
    }

    /**
     * No thread manipulation test case.
     */
    public static void noThreadManipulation(CallGraph cg) {
        createNoClassShouldHaveMethodRule(
                localize("security.architecture.manipulate.threads"),
                FileHandlerConstants.WALA_THREAD_MANIPULATION_METHODS,
                cg
        );
    }

    /**
     * No serialization test case.
     */
    public static void noSerialization(CallGraph callGraph) {
        createNoClassShouldHaveMethodRule(
                localize("security.architecture.serialize"),
                FileHandlerConstants.WALA_SERIALIZATION_METHODS,
                callGraph
        );
    }

    /**
     * No class loading test case.
     */
    public static void noClassLoading(CallGraph callGraph) {
        createNoClassShouldHaveMethodRule(
                localize("security.architecture.class.loading"),
                FileHandlerConstants.WALA_CLASSLOADER_METHODS,
                callGraph
        );
    }

    /**
     * Creates a rule that checks if a class has a forbidden method.
     */
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
        try {
            String sb = "'" + ruleName + "'\r\n" +
                    "Method <" +
                    reachableNodes.getLast().getMethod().getSignature() +
                    "> calls method <" +
                    reachableNodes.get(reachableNodes.size() - 2).getMethod().getSignature() +
                    "> in (" + reachableNodes.getLast().getMethod().getDeclaringClass().getName().getClassName().toString() + ".java:" + reachableNodes.getLast().getMethod().getSourcePosition(0).getFirstLine() +
                    ") accesses <" +
                    reachableNodes.getFirst().getMethod().getSignature();

            throw new AssertionError(sb);
        } catch (InvalidClassFileException e) {
            throw new SecurityException(localize("security.architecture.invalid.class.file"));
        }
    }
}
