package de.tum.cit.ase.ares.api.architecture.java.archunit.postcompile;

//<editor-fold desc="Imports">
import com.ibm.wala.classLoader.IClass;
import com.ibm.wala.core.java11.Java9AnalysisScopeReader;
import com.ibm.wala.ipa.callgraph.AnalysisScope;
import com.ibm.wala.ipa.cha.ClassHierarchy;
import com.ibm.wala.ipa.cha.ClassHierarchyException;
import com.ibm.wala.ipa.cha.ClassHierarchyFactory;
import com.ibm.wala.types.ClassLoaderReference;
import com.ibm.wala.types.TypeReference;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URL;
import java.util.Collections;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
//</editor-fold>

/**
 * Custom class resolver to resolve classes that are outside classpath to be able to analyze them transitively.
 */
public class CustomClassResolver {

    private static final Logger log = LoggerFactory.getLogger(CustomClassResolver.class);

    private CustomClassResolver() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * Class file importer to import the class files.
     * This is used to import the class files from the URL.
     */
    private static final ClassFileImporter classFileImporter = new ClassFileImporter();

    private static final ClassHierarchy classHierarchy;

    static {
        try {
            AnalysisScope scope = Java9AnalysisScopeReader.instance.makeJavaBinaryAnalysisScope(
                    System.getProperty("java.class.path"),
                    null
            );

            // Build the class hierarchy
            long start = System.currentTimeMillis();
            classHierarchy = ClassHierarchyFactory.makeWithRoot(scope);
            log.info("Class hierarchy built in {}ms", System.currentTimeMillis() - start);
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
        URL url = CustomClassResolver.class.getResource("/" + typeName.replace(".", "/") + ".class");
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
     * @param typeName The type name of the class to get the immediate subclasses.
     * @return The immediate subclasses of the given type name.
     */
    public static Set<JavaClass> getImmediateSubclasses(String typeName) {
        TypeReference reference = TypeReference.find(ClassLoaderReference.Application, convertTypeName(typeName));
        if (reference == null) {
            return Collections.emptySet();
        }
        IClass clazz = classHierarchy.lookupClass(TypeReference.find(ClassLoaderReference.Application, convertTypeName(typeName)));
        if (clazz == null) {
            return Collections.emptySet();
        }
        return classHierarchy
                .getImmediateSubclasses(clazz)
                .stream()
                .map(iClass -> tryResolve(iClass.getName().toString()))
                .filter(Optional::isPresent)
                .map(Optional::get).collect(Collectors.toSet());
    }

    /**
     * Convert the type name to the format that can be used in the class file.
     * @param typeName The type name to convert.
     * @return The converted type name.
     */
    public static String convertTypeName(String typeName) {
        if (typeName == null || typeName.isEmpty()) {
            throw new IllegalArgumentException("Type name cannot be null or empty");
        }
        return "L" + typeName.replace('.', '/');
    }

}
