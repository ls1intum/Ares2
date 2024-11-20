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

import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;
//</editor-fold>

/**
 * Utility class to build a call graph from a class path.
 */
public class CallGraphBuilderUtils {

    private CallGraphBuilderUtils() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * Class file importer to import the class files.
     * This is used to import the class files from the URL.
     */
    private static final ClassFileImporter classFileImporter = new ClassFileImporter();

    private static final ClassHierarchy classHierarchy;

    private static final AnalysisScope scope;

    static {
        try {
            scope = Java9AnalysisScopeReader.instance.makeJavaBinaryAnalysisScope(
                    System.getProperty("java.class.path"),
                    null
            );

            // Build the class hierarchy
            classHierarchy = ClassHierarchyFactory.make(scope);
        } catch (ClassHierarchyException | IOException e) {
            throw new SecurityException("Could not create class hierarchy for student submission", e); // $NON-NLS-1$
        }
    }


    /**
     * Try to resolve the class by the given type name.
     *
     * @param typeName The type name of the class to resolve.
     * @return The resolved class if it exists.
     */
    public static Optional<JavaClass> tryResolve(String typeName) {
        // Advice definition uses Reflection and therefor should not be resolved
        if (typeName.startsWith("de.tum.cit.ase.ares.api.aop.java.aspectj.adviceandpointcut.JavaAspectJFileSystemAdviceDefinitions")) {
            return Optional.empty();
        }
        URL url = CallGraphBuilderUtils.class.getResource("/" + typeName.replace(".", "/") + ".class");
        try {
            if (url == null) {
                return Optional.empty();
            }
            return Optional.of(classFileImporter.withImportOption(location -> !location.contains("jrt")).importUrl(url).get(typeName));
        } catch (IllegalArgumentException e) {
            return Optional.empty();
        }
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
            throw new IllegalArgumentException("Type name cannot be null or empty");
        }
        return "L" + typeName.replace('.', '/');
    }

    /**
     * Build a call graph from a class path and the passed in predicate
     */
    public static CallGraph buildCallGraph(String classPathToAnalyze) {
        try {
            // Create a list to store entry points
            List<DefaultEntrypoint> customEntryPoints = ReachabilityChecker.getEntryPointsFromStudentSubmission(classPathToAnalyze, classHierarchy);

            // Create AnalysisOptions for call graph
            AnalysisOptions options = new AnalysisOptions(scope, customEntryPoints);
            options.setTraceStringConstants(false);
            options.setHandleZeroLengthArray(false);
            options.setReflectionOptions(AnalysisOptions.ReflectionOptions.NONE);

            // Create call graph builder (n-CFA, context-sensitive, etc.)
            com.ibm.wala.ipa.callgraph.CallGraphBuilder<InstanceKey> builder = Util.makeZeroOneCFABuilder(Language.JAVA, options, new AnalysisCacheImpl(), classHierarchy);

            // Generate the call graph
            return builder.makeCallGraph(options, null);
        } catch (CallGraphBuilderCancelException e) {
            throw new SecurityException("Error building call graph", e); //$NON-NLS-1$
        }
    }

    /**
     * Get the content of a file from the architectural rules storage
     */
    public static Set<String> getForbiddenMethods(Path filePath) {
        return new HashSet<>(List.of(FileTools.readFile(filePath).split("\r\n")));
    }
}
