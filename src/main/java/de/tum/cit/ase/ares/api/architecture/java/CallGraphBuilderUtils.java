package de.tum.cit.ase.ares.api.architecture.java;

//<editor-fold desc="Imports">

import com.ibm.wala.classLoader.IClass;
import com.ibm.wala.classLoader.Language;
import com.ibm.wala.core.java11.Java9AnalysisScopeReader;
import com.ibm.wala.ipa.callgraph.*;
import com.ibm.wala.ipa.callgraph.impl.DefaultEntrypoint;
import com.ibm.wala.ipa.callgraph.impl.Util;
import com.ibm.wala.ipa.callgraph.propagation.InstanceKey;
import com.ibm.wala.ipa.cha.ClassHierarchy;
import com.ibm.wala.ipa.cha.ClassHierarchyException;
import com.ibm.wala.ipa.cha.ClassHierarchyFactory;
import com.ibm.wala.types.ClassLoaderReference;
import com.ibm.wala.types.TypeReference;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import de.tum.cit.ase.ares.api.architecture.java.wala.ReachabilityChecker;
import de.tum.cit.ase.ares.api.util.FileTools;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

import static de.tum.cit.ase.ares.api.aop.java.instrumentation.advice.JavaInstrumentationAdviceToolbox.localize;
//</editor-fold>

/**
 * Utility class to build a call graph from a class path.
 */
public class CallGraphBuilderUtils {

    private CallGraphBuilderUtils() {
        throw new SecurityException(localize("security.general.utility.initialization", CallGraphBuilderUtils.class.getName()));
    }

    /**
     * Class file importer to import the class files.
     * This is used to import the class files from the URL.
     */
    private static final ClassFileImporter classFileImporter;

    private static final ClassHierarchy classHierarchy;

    private static final AnalysisScope scope;

    static {
        try {
            // Create a class file importer
            classFileImporter = new ClassFileImporter();

            // Create an analysis scope
            scope = Java9AnalysisScopeReader.instance.makeJavaBinaryAnalysisScope(
                    System.getProperty("java.class.path"),
                    // File translates the path name for Windows and Unix
                    new File("src/main/java/de/tum/cit/ase/ares/api/architecture/java/wala/exclusions.txt")
            );

            // Build the class hierarchy
            classHierarchy = ClassHierarchyFactory.make(scope);
        } catch (ClassHierarchyException | IOException e) {
            throw new SecurityException(localize("security.architecture.class.hierarchy.error")); // $NON-NLS-1$
        }
    }


    /**
     * Try to resolve the class by the given type name.
     *
     * Ignore jrt URLs as they cause infinite loops and are not needed for the analysis for ArchUnit
     *
     * @param typeName The type name of the class to resolve.
     * @return The resolved class if it exists.
     */
    public static Optional<JavaClass> tryResolve(String typeName) {
        List<String> ignoredTypeNames = List.of(
                // Advice definition uses Reflection and therefor should not be resolved
                "de.tum.cit.ase.ares.api.aop.java.aspectj.adviceandpointcut.JavaAspectJFileSystemAdviceDefinitions"
        );
        // Advice definition uses Reflection and therefor should not be resolved
        if (ignoredTypeNames.contains(typeName)) {
            return Optional.empty();
        }
        // TODO: Check if FileTools supports this approach
        return Optional.ofNullable(CallGraphBuilderUtils.class.getResource("/" + typeName.replace(".", "/") + ".class"))
                .map(location -> classFileImporter
                        .withImportOption(loc -> !loc.contains("jrt"))
                        .importUrl(location))
                .map(imported -> {
                    try {
                        return imported.get(typeName);
                    } catch (IllegalArgumentException e) {
                        return null; // Return null so that Optional.empty() is created
                    }
                });
    }

    /**
     * Get the immediate subclasses of the given type name.
     *
     * @param typeName The type name of the class to get the immediate subclasses.
     * @return The immediate subclasses of the given type name.
     */
    public static Set<JavaClass> getImmediateSubclasses(String typeName) {
        TypeReference reference = TypeReference.find(ClassLoaderReference.Application, convertTypeName(typeName));
        if (reference == null) {
            return Collections.emptySet();
        }
        IClass clazz = classHierarchy.lookupClass(reference);
        if (clazz == null) {
            return Collections.emptySet();
        }
        return classHierarchy
                .getImmediateSubclasses(clazz)
                .stream()
                .map(IClass::getName)
                .map(Object::toString)
                .map(CallGraphBuilderUtils::tryResolve)
                .filter(Optional::isPresent)
                .map(Optional::get).collect(Collectors.toSet());
    }

    /**
     * Convert the type name to the format that can be used in the class file.
     *
     * @param typeName The type name to convert.
     * @return The converted type name.
     */
    public static String convertTypeName(String typeName) {
        if (typeName == null || typeName.isEmpty()) {
            throw new SecurityException(localize("security.architecture.class.type.resolution.error"));
        }
        return "L" + typeName.replace('.', '/');
    }

    /**
     * Build a call graph from a class path and the passed in predicate
     */
    public static CallGraph buildCallGraph(String classPathToAnalyze) {
        try {
            // Create a list to store entry points
            // TODO: Explain what an entry point is
            List<DefaultEntrypoint> customEntryPoints = ReachabilityChecker.getEntryPointsFromStudentSubmission(classPathToAnalyze, classHierarchy);

            // Create AnalysisOptions for call graph
            AnalysisOptions options = new AnalysisOptions(scope, customEntryPoints);
            // TODO: Write why they are important
            options.setTraceStringConstants(false);
            options.setHandleZeroLengthArray(false);
            options.setReflectionOptions(AnalysisOptions.ReflectionOptions.NONE);

            // Create call graph builder (n-CFA, context-sensitive, etc.)
            com.ibm.wala.ipa.callgraph.CallGraphBuilder<InstanceKey> builder = Util.makeZeroOneCFABuilder(Language.JAVA, options, new AnalysisCacheImpl(), classHierarchy);

            // Generate the call graph
            return builder.makeCallGraph(options, null);
        } catch (CallGraphBuilderCancelException e) {
            throw new SecurityException(localize("security.architecture.build.call.graph.error")); //$NON-NLS-1$
        }
    }
}
