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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@SuppressWarnings("FieldCanBeLocal")
public class JavaCreator implements SecurityTestCaseCreator {

    //<editor-fold desc="Attributes">

    //<editor-fold desc="Modes">
    /**
     * The build mode used in the project (e.g., Maven or Gradle).
     */
    @Nonnull
    private final JavaBuildMode javaBuildMode;

    /**
     * The Architecture mode used in the project (e.g., ArchUnit or Wala).
     */
    @Nonnull
    private final JavaArchitectureMode javaArchitectureMode;

    /**
     * The Aspect-Oriented Programming (AOP) mode used in the project (e.g., AspectJ or Instrumentation).
     */
    @Nonnull
    private final JavaAOPMode javaAOPMode;
    //</editor-fold>

    //<editor-fold desc="Untested-Domain">

    /**
     * These packages are essential for the execution of the security test cases and are therefore not subject to the security policy.
     */
    @Nonnull
    private final List<String> essentialPackages;

    /**
     * These classes are essential for the execution of the security test cases and are therefore not subject to the security policy.
     */
    @Nonnull
    private final List<String> essentialClasses;

    /**
     * These classes are part of the unrestricted test code and are therefore not subject to the security policy.
     */
    @Nonnull
    private final String[] testClasses;
    //</editor-fold>

    //<editor-fold desc="Tested-Domain">

    /**
     * The classes of this package are part of the restricted student code and are therefore subject to the security policy.
     */
    @Nonnull
    private final String packageName;

    /**
     * The path to the main class within the package of restricted student code.
     */
    @Nonnull
    private final String mainClassInPackageName;
    //</editor-fold>

    //<editor-fold desc="Configuration">
    /**
     * The resource accesses permitted as defined in the security policy.
     */
    @Nonnull
    private final ResourceAccesses resourceAccesses;

    /**
     * The path within the project where the test cases will be generated.
     */
    @Nonnull
    private final Path projectPath;
    //</editor-fold>

    //<editor-fold desc="Generated Test Cases">
    /**
     * List of architecture test cases to be generated based on the security policy.
     */
    @Nonnull
    private final List<JavaArchitectureTestCase> javaArchitectureTestCases = new ArrayList<>();

    /**
     * List of AOP test cases to be generated based on the security policy.
     */
    @Nonnull
    private final List<JavaAOPTestCase> javaAOPTestCases = new ArrayList<>();
    //</editor-fold>

    //</editor-fold>

    //<editor-fold desc="Constructor">
    public JavaCreator(@Nonnull JavaBuildMode javaBuildMode, @Nonnull JavaArchitectureMode javaArchitectureMode, @Nonnull JavaAOPMode javaAOPMode, @Nonnull List<String> essentialPackages, @Nonnull List<String> essentialClasses, @Nonnull String[] testClasses, @Nonnull String packageName, @Nonnull String mainClassInPackageName, @Nonnull ResourceAccesses resourceAccesses, @Nonnull Path projectPath) {
        this.essentialPackages = essentialPackages;
        this.essentialClasses = essentialClasses;
        this.testClasses = testClasses;
        this.packageName = packageName;
        this.mainClassInPackageName = mainClassInPackageName;
        this.javaBuildMode = javaBuildMode;
        this.javaArchitectureMode = javaArchitectureMode;
        this.javaAOPMode = javaAOPMode;
        this.resourceAccesses = resourceAccesses;
        this.projectPath = projectPath;
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
    public void createSecurityTestCases() {
        //<editor-fold desc="Preparation">
        String buildDir = javaBuildMode == JavaBuildMode.GRADLE ? "build" : "target";
        String classPath = Paths.get(buildDir, projectPath.toString()).toString();
        JavaClasses classes = cacheResult(() -> new ClassFileImporter().importPath(classPath)).get();
        CallGraph callGraph = cacheResult(() -> javaArchitectureMode == JavaArchitectureMode.WALA ? new CustomCallgraphBuilder().buildCallGraph(classPath) : null).get();
        Set<PackagePermission> allowedPackages = prepareAllowedPackages();
        Set<String> allowedClasses = prepareAllowedClasses();
        //</editor-fold>

        //<editor-fold desc="Create variable rules code">
        addVariableTestCases(classes, callGraph, allowedPackages, allowedClasses);
        //</editor-fold>

        //<editor-fold desc="Create fixed rules code">
        addFixedTestCases(classes, callGraph, allowedPackages, allowedClasses);
        //</editor-fold>
    }
    //</editor-fold>

    //<editor-fold desc="Helper methods">

    //<editor-fold desc="Caching methods">

    /**
     * Caches the result of a supplier to avoid recomputation.
     */
    public static <T> Supplier<T> cacheResult(Supplier<T> supplier) {
        final Object[] cache = {null};
        return () -> (T) (cache[0] == null ? (cache[0] = supplier.get()) : cache[0]);
    }
    //</editor-fold>

    //<editor-fold desc="Prepare methods">

    /**
     * Prepares the set of allowed packages based on the essential packages and security policy.
     */
    @Nonnull
    private Set<PackagePermission> prepareAllowedPackages() {
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
    private Set<String> prepareAllowedClasses() {
        return Stream.of(
                // Essential classes are allowed to do anything
                essentialClasses.stream(),
                // Test classes are allowed to do anything
                Arrays.stream(testClasses)).flatMap(Function.identity()).collect(Collectors.toSet());
    }
    //</editor-fold>

    //<editor-fold desc="Create security test case methods">

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
    private JavaAOPTestCase createAOPTestCase(JavaAOPTestCaseSupported supported, JavaClasses classes, CallGraph callGraph, Set<PackagePermission> allowedPackages, Set<String> allowedClasses) {

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
    //</editor-fold>

    //<editor-fold desc="Add test cases methods">

    /**
     * Adds fixed architecture test cases that are always required.
     */
    private void addFixedTestCases(JavaClasses classes, CallGraph callGraph, Set<PackagePermission> allowedPackages, Set<String> allowedClasses) {
        javaArchitectureTestCases.addAll(JavaArchitectureTestCaseSupported
                // The choice of using TERMINATE_JVM was taken randomly for getting an instance of JavaArchitectureTestCaseSupported (otherwise we cannot operate over an interface)
                .TERMINATE_JVM.getStatic().stream().map(fixedCase -> createArchitectureTestCase((JavaArchitectureTestCaseSupported) fixedCase, classes, callGraph, allowedPackages, allowedClasses)).toList());
    }

    /**
     * Adds variable architecture test cases that are generated based on the security policy.
     */
    private void addVariableTestCases(JavaClasses classes, CallGraph callGraph, Set<PackagePermission> allowedPackages, Set<String> allowedClasses) {
        javaAOPTestCases.addAll(JavaAOPTestCaseSupported
                // The choice of using TERMINATE_JVM was taken randomly for getting an instance of JavaAOPTestCaseSupported (otherwise we cannot operate over an interface)
                .FILESYSTEM_INTERACTION.getDynamic().stream().map(fixedCase -> createAOPTestCase((JavaAOPTestCaseSupported) fixedCase, classes, callGraph, allowedPackages, allowedClasses)).toList());
    }
    //</editor-fold>
    //</editor-fold>
}
