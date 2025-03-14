package de.tum.cit.ase.ares.api.securitytest.java.creator;

import com.ibm.wala.ipa.callgraph.CallGraph;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import de.tum.cit.ase.ares.api.aop.java.JavaAOPMode;
import de.tum.cit.ase.ares.api.aop.java.JavaAOPTestCase;
import de.tum.cit.ase.ares.api.aop.java.JavaAOPTestCaseSupported;
import de.tum.cit.ase.ares.api.architecture.java.CustomCallgraphBuilder;
import de.tum.cit.ase.ares.api.architecture.java.JavaArchitectureTestCaseSupported;
import de.tum.cit.ase.ares.api.architecture.java.JavaArchitectureMode;
import de.tum.cit.ase.ares.api.architecture.java.JavaArchitectureTestCase;
import de.tum.cit.ase.ares.api.buildtoolconfiguration.java.JavaBuildMode;
import de.tum.cit.ase.ares.api.policy.policySubComponents.PackagePermission;
import de.tum.cit.ase.ares.api.policy.policySubComponents.ResourceAccesses;

import javax.annotation.Nonnull;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@SuppressWarnings("FieldCanBeLocal")
public class JavaCreator implements Creator {

    //<editor-fold desc="Helper methods">

    /**
     * Caches the result of a supplier to avoid recomputation.
     */
    public static <T> Supplier<T> cacheResult(Supplier<T> supplier) {
        final Object[] cache = {null};
        return () -> (T) (cache[0] == null ? (cache[0] = supplier.get()) : cache[0]);
    }

    /**
     * Prepares the set of allowed packages based on the essential packages and security policy.
     */
    @Nonnull
    private Set<PackagePermission> prepareAllowedPackages(ResourceAccesses resourceAccesses, List<String> essentialPackages, String packageName) {
        return Stream.of(
                // Essential packages are allowed to do anything
                essentialPackages.stream().map(PackagePermission::new),
                // The permitted packages are allowed
                resourceAccesses.regardingPackageImports().stream(),
                /* The package of the restricted student code is allowed
                 * (else the student would not be able to use his/her own code)
                 */
                Stream.of(new PackagePermission(packageName))

        ).flatMap(Function.identity()).collect(Collectors.toSet());
    }

    /**
     * Prepares the set of allowed classes based on the essential classes and security policy.
     */
    @Nonnull
    private Set<String> prepareAllowedClasses(List<String> essentialClasses, String[] testClasses) {
        return Stream.of(
                // Essential classes are allowed to do anything
                essentialClasses.stream(),
                // Test classes are allowed to do anything
                Arrays.stream(testClasses)).flatMap(Function.identity()).collect(Collectors.toSet());
    }

    /**
     * Creates a JavaArchitectureTestCase with the given parameters.
     */
    @Nonnull
    private JavaArchitectureTestCase createArchitectureTestCase(JavaArchitectureTestCaseSupported supported, JavaClasses classes, CallGraph callGraph, Set<PackagePermission> allowedPackages, Set<String> allowedClasses) {
        return JavaArchitectureTestCase.builder()
                // The architecture test case checks for the following aspect
                .javaArchitecturalTestCaseSupported(supported)
                // The architecture test cases are built over the following classes
                .javaClasses(classes)
                // The architecture test cases are built over the following call graph
                .callGraph(callGraph)
                // The following packages are allowed
                .allowedPackages(allowedPackages)
                // Build the architecture test case
                .build();
    }

    /**
     * Creates a JavaAOPTestCase with the given parameters.
     */
    @Nonnull
    private JavaAOPTestCase createAOPTestCase(
            ResourceAccesses resourceAccesses, List<JavaArchitectureTestCase> javaArchitectureTestCases,
            JavaAOPTestCaseSupported supported,
            JavaClasses classes, CallGraph callGraph,
            Set<PackagePermission> allowedPackages, Set<String> allowedClasses
    ) {

        Supplier<List<?>> resourceAccessSupplier = List.of((Supplier<List<?>>) resourceAccesses::regardingFileSystemInteractions, resourceAccesses::regardingNetworkConnections, resourceAccesses::regardingCommandExecutions, resourceAccesses::regardingThreadCreations).get(supported.ordinal());

        if (resourceAccessSupplier.get().isEmpty()) {
            javaArchitectureTestCases.add(JavaArchitectureTestCase.builder().javaArchitecturalTestCaseSupported(JavaArchitectureTestCaseSupported.valueOf(supported.name())).javaClasses(classes).callGraph(callGraph).allowedPackages(allowedPackages).build());
        }

        return JavaAOPTestCase.builder()
                // The AOP test case checks for the following aspect
                .javaAOPTestCaseSupported(supported)
                .resourceAccessSupplier(resourceAccessSupplier)
                // The following classes are allowed
                .allowedClasses(allowedClasses)
                // Build the AOP test case
                .build();
    }

    /**
     * Adds fixed architecture test cases that are always required.
     */
    private void addFixedTestCases(List<JavaArchitectureTestCase> javaArchitectureTestCases, JavaClasses classes, CallGraph callGraph, Set<PackagePermission> allowedPackages, Set<String> allowedClasses) {
        javaArchitectureTestCases.addAll(JavaArchitectureTestCaseSupported
                // The choice of using TERMINATE_JVM was taken randomly for getting an instance of JavaArchitectureTestCaseSupported (otherwise we cannot operate over an interface)
                .TERMINATE_JVM.getStatic().stream().map(fixedCase -> createArchitectureTestCase((JavaArchitectureTestCaseSupported) fixedCase, classes, callGraph, allowedPackages, allowedClasses)).toList());
    }

    /**
     * Adds variable architecture test cases that are generated based on the security policy.
     */
    private void addVariableTestCases(ResourceAccesses resourceAccesses, List<JavaArchitectureTestCase> javaArchitectureTestCases, List<JavaAOPTestCase> javaAOPTestCases, JavaClasses classes, CallGraph callGraph, Set<PackagePermission> allowedPackages, Set<String> allowedClasses) {
        javaAOPTestCases.addAll(JavaAOPTestCaseSupported
                // The choice of using TERMINATE_JVM was taken randomly for getting an instance of JavaAOPTestCaseSupported (otherwise we cannot operate over an interface)
                .FILESYSTEM_INTERACTION.getDynamic().stream().map(fixedCase -> createAOPTestCase(resourceAccesses, javaArchitectureTestCases, (JavaAOPTestCaseSupported) fixedCase, classes, callGraph, allowedPackages, allowedClasses)).toList());
    }
    //</editor-fold>

    //<editor-fold desc="Create security test cases methods">

    /**
     * Creates the security test cases based on the security policy.
     * <p>
     * This method generates the architecture test cases and aspect configurations based on the
     * security policy and the resource accesses permitted.
     * </p>
     */
    public void createSecurityTestCases(
            JavaBuildMode javaBuildMode,
            JavaArchitectureMode javaArchitectureMode,
            JavaAOPMode javaAOPMode,
            List<String> essentialPackages,
            List<String> essentialClasses,
            String[] testClasses,
            String packageName,
            String mainClassInPackageName,
            ResourceAccesses resourceAccesses,
            Path projectPath,
            @Nonnull List<JavaArchitectureTestCase> javaArchitectureTestCases,
            @Nonnull List<JavaAOPTestCase> javaAOPTestCases
    ) {
        //<editor-fold desc="Preparation">
        String buildDir = javaBuildMode == JavaBuildMode.GRADLE ? "build" : "target";
        String classPath = Paths.get(buildDir, projectPath.toString()).toString();
        JavaClasses classes = cacheResult(() -> new ClassFileImporter().importPath(classPath)).get();
        CallGraph callGraph = cacheResult(() -> javaArchitectureMode == JavaArchitectureMode.WALA ? new CustomCallgraphBuilder().buildCallGraph(classPath) : null).get();
        Set<PackagePermission> allowedPackages = prepareAllowedPackages(resourceAccesses, essentialPackages, packageName);
        Set<String> allowedClasses = prepareAllowedClasses(essentialClasses, testClasses);
        //</editor-fold>

        //<editor-fold desc="Create variable rules code">
        addVariableTestCases(resourceAccesses, javaArchitectureTestCases, javaAOPTestCases, classes, callGraph, allowedPackages, allowedClasses);
        //</editor-fold>

        //<editor-fold desc="Create fixed rules code">
        addFixedTestCases(javaArchitectureTestCases, classes, callGraph, allowedPackages, allowedClasses);
        //</editor-fold>
    }
    //</editor-fold>
}
