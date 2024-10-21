package de.tum.cit.ase.ares.api.architecture.java.archunit.postcompile;

//<editor-fold desc="Imports">
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.importer.ClassFileImporter;

import java.net.URL;
import java.util.Optional;
//</editor-fold>

/**
 * Custom class resolver to resolve classes that are outside classpath to be able to analyze them transitively.
 */
public class CustomClassResolver {

    private CustomClassResolver() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * Class file importer to import the class files.
     * This is used to import the class files from the URL.
     */
    private static final ClassFileImporter classFileImporter = new ClassFileImporter();

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
}
