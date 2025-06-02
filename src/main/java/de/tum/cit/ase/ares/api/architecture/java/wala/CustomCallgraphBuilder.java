package de.tum.cit.ase.ares.api.architecture.java.wala;

import com.ibm.wala.classLoader.IClass;
import com.ibm.wala.classLoader.Language;
import com.ibm.wala.core.java11.Java9AnalysisScopeReader;
import com.ibm.wala.ipa.callgraph.AnalysisCacheImpl;
import com.ibm.wala.ipa.callgraph.AnalysisOptions;
import com.ibm.wala.ipa.callgraph.AnalysisScope;
import com.ibm.wala.ipa.callgraph.CallGraph;
import com.ibm.wala.ipa.callgraph.CallGraphBuilder;
import com.ibm.wala.ipa.callgraph.impl.DefaultEntrypoint;
import com.ibm.wala.ipa.callgraph.impl.Util;
import com.ibm.wala.ipa.cha.ClassHierarchy;
import com.ibm.wala.ipa.cha.ClassHierarchyException;
import com.ibm.wala.ipa.cha.ClassHierarchyFactory;
import com.ibm.wala.types.ClassLoaderReference;
import com.ibm.wala.types.TypeReference;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import de.tum.cit.ase.ares.api.aop.java.instrumentation.advice.JavaInstrumentationAdviceFileSystemToolbox;
import de.tum.cit.ase.ares.api.util.FileTools;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Utility to build a call graph for Java binaries using WALA framework.
 *
 * <p>Description: This class reads .class files from a specified classpath builds an analysis scope constructs a class hierarchy and generates a call graph representing method call relationships.
 *
 * <p>Design Rationale: To provide a reusable component for architecture tests that require static analysis of Java bytecode call relationships enabling detection of forbidden method usage.
 *
 * @since 2.0.0
 * @author Sarp Sahinalp
 * @version 2.0.0
 */
public class CustomCallgraphBuilder {

    /**
     * Importer for reading .class files from URLs into ArchUnit domain model.
     *
     * <p>Description: Used to import individual class files skipping Java runtime modules.
     */
    private final ClassFileImporter classFileImporter; // loads class files into importer

    /**
     * Analysis scope containing application classes and required dependencies.
     *
     * <p>Description: Defines which classes are included in the static analysis context.
     */
    private final AnalysisScope scope; // controls included/excluded code

    /**
     * Class hierarchy derived from analysis scope to resolve types.
     *
     * <p>Description: Used to look up classes and their relationships based on loaded dependencies.
     */
    private final ClassHierarchy classHierarchy; // holds information on class inheritance

    /**
     * Temporary storage for generated call graph once built.
     *
     * <p>Description: Caches the result of buildCallGraph to avoid rebuilding multiple times.
     */
    private CallGraph callGraph = null; // cached call graph instance

    /**
     * Initializes importer, analysis scope, and class hierarchy for building call graphs.
     *
     * <p>Description: Sets up WALA analysis by reading classpath, loading exclusions, and constructing hierarchy.
     *
     * @since 2.0.0
     * @author Sarp Sahinalp
     */
    public CustomCallgraphBuilder() {
        // Prepare importer for class file URLs
        classFileImporter = new ClassFileImporter();
        try {
            // Build analysis scope from current classpath and exclusion list
            scope = Java9AnalysisScopeReader.instance.makeJavaBinaryAnalysisScope(
                    System.getProperty("java.class.path"),
                    FileTools.getResourceAsFile("de/tum/cit/ase/ares/api/templates/architecture/java/exclusions.txt")
            );
            // Construct class hierarchy from scope
            classHierarchy = ClassHierarchyFactory.make(scope);
        } catch (ClassHierarchyException | IOException e) {
            // Fail fast if hierarchy cannot be built
            throw new SecurityException(JavaInstrumentationAdviceFileSystemToolbox.localize("security.architecture.class.hierarchy.error"));
        }
    }

    /**
     * Converts a Java type name to a resource path for its .class file.
     *
     * <p>Description: Transforms "com.example.MyClass" into "/com/example/MyClass.class".
     *
     * @since 2.0.0
     * @author Sarp Sahinalp
     * @param typeName binary name of the class
     * @return resource path string
     */
    private static String convertTypeNameToClassName(String typeName) {
        if (typeName == null || typeName.isEmpty()) {
            // Prevent invalid names
            throw new SecurityException(JavaInstrumentationAdviceFileSystemToolbox.localize("security.architecture.class.type.resolution.error"));
        }
        // Replace dots with slashes and add .class suffix
        return "/" + typeName.replace(".", "/") + ".class";
    }

    /**
     * Converts a Java type name to WALA's internal format.
     *
     * <p>Description: Transforms "com.example.MyClass" into "Lcom/example/MyClass" for WALA lookups.
     *
     * @since 2.0.0
     * @author Sarp Sahinalp
     * @param typeName binary name of the class
     * @return WALA type reference string
     */
    private static String convertTypeNameToWalaName(String typeName) {
        if (typeName == null || typeName.isEmpty()) {
            // Prevent invalid names
            throw new SecurityException(JavaInstrumentationAdviceFileSystemToolbox.localize("security.architecture.class.type.resolution.error"));
        }
        // Prefix with L and replace dots with slashes
        return "L" + typeName.replace('.', '/');
    }

    /**
     * Attempts to resolve a class by its name, ignoring certain known types.
     *
     * <p>Description: Skips excluded advice classes, locates the .class resource, imports it, and returns the JavaClass.
     *
     * @since 2.0.0
     * @author Sarp Sahinalp
     * @param typeName binary name of the target class
     * @return Optional containing the resolved JavaClass or empty
     */
    private Optional<JavaClass> tryResolve(String typeName) {
        // Define classes to ignore to avoid infinite loops
        List<String> ignoredTypeNames = List.of(
                "de.tum.cit.ase.ares.api.aop.java.aspectj.adviceandpointcut.JavaAspectJFileSystemAdviceDefinitions"
        );
        if (ignoredTypeNames.contains(typeName)) {
            return Optional.empty(); // skip forbidden names
        }
        // Locate resource URL for .class file
        return Optional.ofNullable(
                        CustomCallgraphBuilder.class.getResource(convertTypeNameToClassName(typeName))
                )
                // Import using ArchUnit, excluding jrt modules
                .map(location -> classFileImporter.withImportOption(loc -> !loc.contains("jrt")).importUrl(location))
                // Extract JavaClass by name, handle missing
                .map(imported -> {
                    try {
                        return imported.get(typeName); // get class by name
                    } catch (IllegalArgumentException e) {
                        return null; // not found
                    }
                });
    }

    /**
     * Finds immediate subclasses of a given class name.
     *
     * <p>Description: Uses WALA ClassHierarchy to retrieve direct descendants of the specified type.
     *
     * @since 2.0.0
     * @author Sarp Sahinalp
     * @param typeName binary name of the superclass
     * @return set of JavaClass representing subclasses
     */
    @SuppressWarnings("unused")
    public Set<JavaClass> getImmediateSubclasses(String typeName) {
        // Create WALA type reference for application loader
        TypeReference reference = TypeReference.find(
                ClassLoaderReference.Application,
                convertTypeNameToWalaName(typeName)
        );
        if (reference == null) {
            return Collections.emptySet(); // no reference
        }
        // Find IClass in hierarchy, return empty if absent
        IClass clazz = classHierarchy.lookupClass(reference);
        if (clazz == null) {
            return Collections.emptySet();
        }
        // Map subclasses to ArchUnit JavaClass instances
        return classHierarchy.getImmediateSubclasses(clazz)
                .stream()
                .map(IClass::getName) // get name object
                .map(Object::toString) // convert to string
                .map(this::tryResolve) // attempt resolution
                .filter(Optional::isPresent) // only those found
                .map(Optional::get) // unwrap
                .collect(Collectors.toSet()); // gather
    }

    /**
     * Builds and caches a call graph for analysis.
     *
     * <p>Description: Computes method call relationships for all non-main methods in the provided class path using a 0-1-CFA builder.
     *
     * @since 2.0.0
     * @author Sarp Sahinalp
     * @param classPathToAnalyze the path or JAR to analyze
     * @return generated CallGraph instance
     */
    public CallGraph buildCallGraph(String classPathToAnalyze) {
        try {
            // Return existing graph if already built
            if (callGraph != null) {
                return callGraph;
            }
            // Determine entry points: all methods except main in student code
            List<DefaultEntrypoint> customEntryPoints = ReachabilityChecker.getEntryPointsFromStudentSubmission(
                    classPathToAnalyze,
                    classHierarchy
            );
            // Configure analysis options with scope and entry points
            AnalysisOptions options = new AnalysisOptions(scope, customEntryPoints);
            // Create context-sensitive call graph builder
            CallGraphBuilder<?> builder = Util.makeZeroOneCFABuilder(
                    Language.JAVA,
                    options,
                    new AnalysisCacheImpl(),
                    classHierarchy
            );
            // Generate call graph
            callGraph = builder.makeCallGraph(options, null);
            return callGraph;
        } catch (Exception e) {
            // Wrap builder failures as security exceptions
            throw new SecurityException(JavaInstrumentationAdviceFileSystemToolbox.localize("security.architecture.build.call.graph.error"));
        }
    }
}