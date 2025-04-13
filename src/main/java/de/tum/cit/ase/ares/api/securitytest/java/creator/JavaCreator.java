package de.tum.cit.ase.ares.api.securitytest.java.creator;

import com.ibm.wala.ipa.callgraph.CallGraph;
import com.tngtech.archunit.core.domain.JavaClasses;
import de.tum.cit.ase.ares.api.aop.java.JavaAOPMode;
import de.tum.cit.ase.ares.api.aop.java.JavaAOPTestCase;
import de.tum.cit.ase.ares.api.aop.java.JavaAOPTestCaseSupported;
import de.tum.cit.ase.ares.api.architecture.java.JavaArchitectureTestCaseSupported;
import de.tum.cit.ase.ares.api.architecture.java.JavaArchitectureMode;
import de.tum.cit.ase.ares.api.architecture.java.JavaArchitectureTestCase;
import de.tum.cit.ase.ares.api.buildtoolconfiguration.java.JavaBuildMode;
import de.tum.cit.ase.ares.api.policy.policySubComponents.ClassPermission;
import de.tum.cit.ase.ares.api.policy.policySubComponents.PackagePermission;
import de.tum.cit.ase.ares.api.policy.policySubComponents.ResourceAccesses;

import javax.annotation.Nonnull;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Creates security test cases for Java projects based on security policies.
 *
 * <p>Description: This class is responsible for generating architecture and aspect-oriented
 * programming (AOP) test cases based on the provided security policies. It extracts necessary
 * information from the project, prepares allowed packages and classes, and creates both fixed
 * and variable test cases.
 *
 * <p>Design Rationale: The JavaCreator follows the Creator design pattern to encapsulate the
 * complex process of generating security test cases. It separates the creation logic from the
 * execution and writing logic, adhering to the Single Responsibility Principle.
 *
 * @since 2.0.0
 * @author Markus Paulsen
 * @version 2.0.0
 */
@SuppressWarnings("FieldCanBeLocal")
public class JavaCreator implements Creator {

    //<editor-fold desc="Helper methods">

    // TODO Markus: Move to a better place
    /**
     * Caches the result of a supplier to avoid recomputation.
     *
     * @since 2.0.0
     * @author Markus Paulsen
     * @param supplier the supplier to cache the result for; must not be null
     * @param <T> the type of the result
     * @return a supplier that returns the cached result; never null
     */
    public static <T> Supplier<T> cacheResult(@Nonnull Supplier<T> supplier) {
        @Nonnull final Object[] cache = {null};
        return () -> (T) (cache[0] == null ? (cache[0] = supplier.get()) : cache[0]);
    }

    /**
     * Prepares the set of allowed packages based on essential packages and security policy.
     *
     * @since 2.0.0
     * @author Markus Paulsen
     * @param resourceAccesses the resource accesses permitted by the security policy; must not be null
     * @param essentialPackages the list of essential packages; must not be null
     * @param packageName the name of the package containing the main class; must not be null
     * @return a set of allowed package permissions; never null
     */
    @Nonnull
    private Set<PackagePermission> prepareAllowedPackages(
            @Nonnull List<String> essentialPackages,
            @Nonnull ResourceAccesses resourceAccesses,
            @Nonnull String packageName
    ) {
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
     *
     * @since 2.0.0
     * @author Markus Paulsen
     * @param essentialClasses the list of essential classes; must not be null
     * @param testClasses the list of test classes; must not be null
     * @return a set of allowed class names; never null
     */
    @Nonnull
    private Set<ClassPermission> prepareAllowedClasses(
            @Nonnull List<String> essentialClasses,
            @Nonnull List<String> testClasses
    ) {
        return Stream.of(
                // Essential classes are allowed to do anything
                essentialClasses.stream().map(ClassPermission::new),
                // Test classes are allowed to do anything
                testClasses.stream().map(ClassPermission::new)
        ).flatMap(Function.identity()).collect(Collectors.toSet());
    }

    /**
     * Creates a JavaArchitectureTestCase with the given parameters.
     *
     * @since 2.0.0
     * @author Markus Paulsen
     * @param supported the supported architecture test case type; must not be null
     * @param classes the Java classes to analyze; must not be null
     * @param callGraph the call graph to analyze; must not be null
     * @param allowedPackages the set of allowed package permissions; must not be null
     * @param allowedClasses the set of allowed class names; must not be null
     * @return a new JavaArchitectureTestCase; never null
     */
    @Nonnull
    private JavaArchitectureTestCase createArchitectureTestCase(
            @Nonnull JavaArchitectureTestCaseSupported supported,
            @Nonnull JavaClasses classes,
            @Nonnull CallGraph callGraph,
            @Nonnull Set<PackagePermission> allowedPackages,
            @Nonnull Set<ClassPermission> allowedClasses
    ) {
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
     *
     * @since 2.0.0
     * @author Markus Paulsen
     * @param resourceAccesses the resource accesses permitted by the security policy; must not be null
     * @param javaArchitectureTestCases the list of architecture test cases to populate; must not be null
     * @param supported the supported AOP test case type; must not be null
     * @param classes the Java classes to analyze; must not be null
     * @param callGraph the call graph to analyze; must not be null
     * @param allowedPackages the set of allowed package permissions; must not be null
     * @param allowedClasses the set of allowed class names; must not be null
     * @return a new JavaAOPTestCase; never null
     */
    @Nonnull
    private JavaAOPTestCase createAOPTestCase(
            @Nonnull ResourceAccesses resourceAccesses,
            @Nonnull List<JavaArchitectureTestCase> javaArchitectureTestCases,
            @Nonnull JavaAOPTestCaseSupported supported,
            @Nonnull JavaClasses classes,
            @Nonnull CallGraph callGraph,
            @Nonnull Set<PackagePermission> allowedPackages,
            @Nonnull Set<ClassPermission> allowedClasses
    ) {
        @Nonnull Supplier<List<?>> resourceAccessSupplier = List.of((Supplier<List<?>>) resourceAccesses::regardingFileSystemInteractions, resourceAccesses::regardingNetworkConnections, resourceAccesses::regardingCommandExecutions, resourceAccesses::regardingThreadCreations).get(supported.ordinal());
        if (resourceAccessSupplier.get().isEmpty()) {
            javaArchitectureTestCases.add(
                    JavaArchitectureTestCase.builder()
                            .javaArchitecturalTestCaseSupported(JavaArchitectureTestCaseSupported.valueOf(supported.name()))
                            .javaClasses(classes)
                            .callGraph(callGraph)
                            .allowedPackages(allowedPackages)
                            .build()
            );
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
     *
     * @since 2.0.0
     * @author Markus Paulsen
     * @param javaArchitectureTestCases the list to populate with architecture test cases; must not be null
     * @param classes the Java classes to analyze; must not be null
     * @param callGraph the call graph to analyze; must not be null
     * @param allowedPackages the set of allowed package permissions; must not be null
     * @param allowedClasses the set of allowed class names; must not be null
     */
    private void addFixedTestCases(
            @Nonnull List<JavaArchitectureTestCase> javaArchitectureTestCases,
            @Nonnull JavaClasses classes,
            @Nonnull CallGraph callGraph,
            @Nonnull Set<PackagePermission> allowedPackages,
            @Nonnull Set<ClassPermission> allowedClasses
    ) {
        javaArchitectureTestCases.addAll(JavaArchitectureTestCaseSupported
                // The choice of using TERMINATE_JVM was taken randomly for getting an instance of JavaArchitectureTestCaseSupported (otherwise we cannot operate over an interface)
                .TERMINATE_JVM.getStatic().stream().map(fixedCase -> createArchitectureTestCase((JavaArchitectureTestCaseSupported) fixedCase, classes, callGraph, allowedPackages, allowedClasses)).toList());
    }

    /**
     * Adds variable architecture test cases that are generated based on the security policy.
     *
     * @since 2.0.0
     * @author Markus Paulsen
     * @param resourceAccesses the resource accesses permitted by the security policy; must not be null
     * @param javaArchitectureTestCases the list to populate with architecture test cases; must not be null
     * @param javaAOPTestCases the list to populate with AOP test cases; must not be null
     * @param classes the Java classes to analyze; must not be null
     * @param callGraph the call graph to analyze; must not be null
     * @param allowedPackages the set of allowed package permissions; must not be null
     * @param allowedClasses the set of allowed class names; must not be null
     */
    private void addVariableTestCases(
            @Nonnull List<JavaArchitectureTestCase> javaArchitectureTestCases,
            @Nonnull List<JavaAOPTestCase> javaAOPTestCases,
            @Nonnull JavaClasses classes,
            @Nonnull CallGraph callGraph,
            @Nonnull Set<PackagePermission> allowedPackages,
            @Nonnull Set<ClassPermission> allowedClasses,
            @Nonnull ResourceAccesses resourceAccesses
    ) {
        javaAOPTestCases.addAll(JavaAOPTestCaseSupported
                // The choice of using TERMINATE_JVM was taken randomly for getting an instance of JavaAOPTestCaseSupported (otherwise we cannot operate over an interface)
                .FILESYSTEM_INTERACTION.getDynamic().stream().map(fixedCase -> createAOPTestCase(resourceAccesses, javaArchitectureTestCases, (JavaAOPTestCaseSupported) fixedCase, classes, callGraph, allowedPackages, allowedClasses)).toList());
    }
    //</editor-fold>

    //<editor-fold desc="Create security test cases methods">

    /**
     * Creates the security test cases based on the security policy.
     *
     * @since 2.0.0
     * @author Markus Paulsen
     * @param javaBuildMode the Java build mode to use; must not be null
     * @param javaArchitectureMode the Java architecture mode to use; must not be null
     * @param javaAOPMode the Java AOP mode to use; must not be null
     * @param essentialPackages the list of essential packages; must not be null
     * @param essentialClasses the list of essential classes; must not be null
     * @param testClasses the list of test classes; must not be null
     * @param packageName the name of the package containing the main class; must not be null
     * @param mainClassInPackageName the name of the main class; must not be null
     * @param javaArchitectureTestCases the list to populate with architecture test cases; must not be null
     * @param javaAOPTestCases the list to populate with AOP test cases; must not be null
     * @param resourceAccesses the resource accesses permitted by the security policy; must not be null
     * @param projectPath the path to the project; must not be null
     */
    public void createSecurityTestCases(
            @Nonnull JavaBuildMode javaBuildMode,
            @Nonnull JavaArchitectureMode javaArchitectureMode,
            @Nonnull JavaAOPMode javaAOPMode,
            @Nonnull List<String> essentialPackages,
            @Nonnull List<String> essentialClasses,
            @Nonnull List<String> testClasses,
            @Nonnull String packageName,
            @Nonnull String mainClassInPackageName,
            @Nonnull List<JavaArchitectureTestCase> javaArchitectureTestCases,
            @Nonnull List<JavaAOPTestCase> javaAOPTestCases,
            @Nonnull ResourceAccesses resourceAccesses,
            @Nonnull Path projectPath
    ) {

        //<editor-fold desc="Extraction">
        @Nonnull String classPath = javaBuildMode.getClasspath(projectPath);
        @Nonnull JavaClasses javaClasses = cacheResult(() -> javaArchitectureMode.getJavaClasses(classPath)).get();
        @Nonnull CallGraph callGraph = cacheResult(() -> javaArchitectureMode.getCallGraph(classPath)).get();
        //</editor-fold>

        //<editor-fold desc="Preparation">
        @Nonnull Set<PackagePermission> allowedPackages = prepareAllowedPackages(essentialPackages, resourceAccesses, packageName);
        @Nonnull Set<ClassPermission> allowedClasses = prepareAllowedClasses(essentialClasses, testClasses);
        //</editor-fold>

        //<editor-fold desc="Create variable rules code">
        addVariableTestCases(javaArchitectureTestCases, javaAOPTestCases, javaClasses, callGraph, allowedPackages, allowedClasses, resourceAccesses);
        //</editor-fold>

        //<editor-fold desc="Create fixed rules code">
        addFixedTestCases(javaArchitectureTestCases, javaClasses, callGraph, allowedPackages, allowedClasses);
        //</editor-fold>
    }
    //</editor-fold>
}
