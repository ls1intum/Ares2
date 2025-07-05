package de.tum.cit.ase.ares.api.architecture.java.wala;

//<editor-fold desc="Imports">

import com.google.common.collect.Iterables;
import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ipa.callgraph.CallGraph;
import com.tngtech.archunit.core.domain.JavaClasses;
import de.tum.cit.ase.ares.api.aop.java.instrumentation.advice.JavaInstrumentationAdviceFileSystemToolbox;
import de.tum.cit.ase.ares.api.architecture.java.FileHandlerConstants;
import de.tum.cit.ase.ares.api.architecture.java.archunit.JavaArchUnitTestCaseCollection;
import de.tum.cit.ase.ares.api.policy.policySubComponents.PackagePermission;
import de.tum.cit.ase.ares.api.util.FileTools;

import java.nio.file.Path;
import java.util.List;
import java.util.Set;

//</editor-fold>

/**
 * Collection of security test cases that analyze Java applications using WALA framework.
 * This class provides static methods to verify that analyzed code does not:
 * - Use reflection
 * - Access file system
 * - Access network
 * - Terminate JVM
 * - Execute system commands
 * - Create threads
 */
public class JavaWalaTestCaseCollection {

    //<editor-fold desc="Constructor">
    private JavaWalaTestCaseCollection() {
        throw new
                SecurityException(JavaInstrumentationAdviceFileSystemToolbox.localize
                ("security.general.utility.initialization",
                        JavaWalaTestCaseCollection.class.getName()));
    }
    //</editor-fold>

    //<editor-fold desc="Tool methods">

    /**
     * Creates a rule that checks if a class has a forbidden method.
     */
    private static WalaRule createNoClassShouldHaveMethodRule(
            String ruleName,
            Path methodsFilePath
    ) {
        return new WalaRule(ruleName, FileTools.readMethodsFromGivenPath(methodsFilePath));
    }

    /* -----------------------------------------------------------
     *  Small reusable decorator that ignores WALA-paths consisting
     *  solely of JDK housekeeping helpers.
     * --------------------------------------------------------- */
    private static WalaRule wrapIgnoringJdkHelpers(WalaRule base) {

        return new WalaRule(base.ruleName, base.forbiddenMethods) {

            @Override
            public void check(CallGraph cg) {

                List<CGNode> path = ReachabilityChecker.findReachableMethods(
                        cg,
                        cg.getEntrypointNodes().iterator(),
                        n -> base.forbiddenMethods.stream()
                                .anyMatch(sig -> n.getMethod()
                                        .getSignature()
                                        .startsWith(sig)));

                if (path == null || Iterables.isEmpty(path)) {
                    return;                 // nothing forbidden reached
                }

                CGNode forbidden = path.getLast();

                /* Is the API one helpers legitimately call? */
                boolean helperApi = ALLOWED_HELPER_APIS.stream()
                        .anyMatch(sig -> forbidden.getMethod()
                                .getSignature()
                                .startsWith(sig));

                /* Does the call chain contain at least one helper frame? */
                boolean helperSeen = path.stream().anyMatch(n -> {
                    String cls = n.getMethod()
                            .getDeclaringClass()
                            .getName()
                            .toString();
                    return JDK_THREAD_HELPERS.stream().anyMatch(cls::startsWith);
                });

                if (helperApi && helperSeen) {
                    return;                 // housekeeping path – ignore
                }

                /* Otherwise: genuine violation. */
                base.check(cg);
            }
        };
    }


    //</editor-fold>

    //<editor-fold desc="Dynamic rules">
    //<editor-fold desc="File System related rule">

    /**
     * This method checks if any class in the given package accesses the file system.
     */
    public static final WalaRule NO_CLASS_MUST_ACCESS_FILE_SYSTEM =
            wrapIgnoringJdkHelpers(createNoClassShouldHaveMethodRule(
                    JavaInstrumentationAdviceFileSystemToolbox.localize("security.architecture.file.system.access"),
                    FileHandlerConstants.WALA_FILESYSTEM_METHODS)
            );
    //</editor-fold>

    //<editor-fold desc="Network Connections related rule">

    /**
     * This method checks if any class in the given package accesses the network.
     */
    public static final WalaRule NO_CLASS_MUST_ACCESS_NETWORK =
            wrapIgnoringJdkHelpers(createNoClassShouldHaveMethodRule(
                    JavaInstrumentationAdviceFileSystemToolbox.localize("security.architecture.network.access"),
                    FileHandlerConstants.WALA_NETWORK_METHODS)
            );
    //</editor-fold>

    //<editor-fold desc="Thread Creation related rule">


    // TODO: these values should be imported through CSV parser
    /**
     * Ignore helper classes from the JDK that are known to create threads. (e.g. move from
     */
    private static final List<String> JDK_THREAD_HELPERS = List.of(
            /* descriptor & binary forms */
            "Lsun/nio/fs/", "sun/nio/fs/",
            "Lsun/nio/ch/", "sun/nio/ch/",
            "Ljava/nio/file/Files", "java/nio/file/Files",
            "Ljava/lang/ClassLoader", "java/lang/ClassLoader",
            "Ljava/lang/Class", "java/lang/Class",
            "Ljdk/internal/loader/NativeLibraries", "jdk/internal/loader/NativeLibraries",
            "Ljava/net/InetAddress", "java/net/InetAddress",
            "Ljava/lang/Thread", "java/lang/Thread",
            "Ljava/lang/reflect/Method", "java/lang/reflect/Method"
    );

    private static final List<String> ALLOWED_HELPER_APIS = List.of(
            "java.lang.Thread.<init>",
            "java.lang.Thread.interrupt",
            "java.lang.ClassLoader.getSystemClassLoader",
            "java.lang.ClassLoader.loadLibrary",
            "java.lang.Runtime.load",
            "java.lang.Runtime.loadLibrary",
            "java.io.File.getName",
            "java.lang.Class.forName",
            "java.net.InetAddress.getAllByName",
            "java.lang.Thread.contextClassLoader",
            "java.lang.Class.getDeclaredField",
            "java.lang.reflect.Method.invoke",
            "java.lang.Class.checkMemberAccess",
            "java.lang.Thread.getContextClassLoader",
            "java.lang.Class.getClassLoader"
    );

    /**
     * This method checks if any class in the given package creates threads. It ignores helper threads created by the JDK.
     * (e.g. for Files.move)
     */
    public static final WalaRule NO_CLASS_MUST_CREATE_THREADS =
            wrapIgnoringJdkHelpers(
                    createNoClassShouldHaveMethodRule(
                            JavaInstrumentationAdviceFileSystemToolbox.localize(
                                    "security.architecture.manipulate.threads"),
                            FileHandlerConstants.WALA_THREAD_MANIPULATION_METHODS)
            );
    //</editor-fold>

    //<editor-fold desc="Command Execution related rule">

    /**
     * This method checks if any class in the given package executes commands.
     */
    public static final WalaRule NO_CLASS_MUST_EXECUTE_COMMANDS =
            wrapIgnoringJdkHelpers(createNoClassShouldHaveMethodRule(
                    JavaInstrumentationAdviceFileSystemToolbox.localize("security.architecture.execute.command"),
                    FileHandlerConstants.WALA_COMMAND_EXECUTION_METHODS)
            );
    //</editor-fold>
    //</editor-fold>

    //<editor-fold desc="Semi-dynamic rules">
    //<editor-fold desc="Package Import related rule">

    /**
     * This method checks if any class in the given package imports forbidden packages.
     */
    public static void noClassMustImportForbiddenPackages(JavaClasses javaClasses, Set<PackagePermission> allowedPackages) {
        JavaArchUnitTestCaseCollection
                .noClassMustImportForbiddenPackages(allowedPackages)
                .check(javaClasses);
    }
    //</editor-fold>
    //</editor-fold>

    //<editor-fold desc="Static rules">
    //<editor-fold desc="Reflection related rule">

    /**
     * This method checks if any class in the given package uses reflection.
     */
    public static final WalaRule NO_CLASS_MUST_USE_REFLECTION =
            wrapIgnoringJdkHelpers(createNoClassShouldHaveMethodRule(
                    JavaInstrumentationAdviceFileSystemToolbox.localize("security.architecture.reflection.uses"),
                    FileHandlerConstants.WALA_REFLECTION_METHODS)
            );
    //</editor-fold>

    //<editor-fold desc="Termination related rule">

    /**
     * This method checks if any class in the given package uses the command line.
     */
    public static final WalaRule NO_CLASS_MUST_TERMINATE_JVM =
            wrapIgnoringJdkHelpers(createNoClassShouldHaveMethodRule(
                    JavaInstrumentationAdviceFileSystemToolbox.localize("security.architecture.terminate.jvm"),
                    FileHandlerConstants.WALA_JVM_METHODS)
            );
    //</editor-fold>

    //<editor-fold desc="Serialization related rule">

    /**
     * This method checks if any class in the given package uses serialization.
     */
    public static final WalaRule NO_CLASS_MUST_SERIALIZE =
            wrapIgnoringJdkHelpers(createNoClassShouldHaveMethodRule(
                    JavaInstrumentationAdviceFileSystemToolbox.localize("security.architecture.serialize"),
                    FileHandlerConstants.WALA_SERIALIZATION_METHODS)
            );
    //</editor-fold>

    //<editor-fold desc="Class Loading related rule">
    /**
     * This method checks if any class in the given package uses class loaders.
     */
    public static final WalaRule NO_CLASS_MUST_USE_CLASSLOADERS =
            wrapIgnoringJdkHelpers(
                    createNoClassShouldHaveMethodRule(
                            JavaInstrumentationAdviceFileSystemToolbox.localize("security.architecture.class.loading"),
                            FileHandlerConstants.WALA_CLASSLOADER_METHODS)
            );
    //</editor-fold>
    //</editor-fold>
}
