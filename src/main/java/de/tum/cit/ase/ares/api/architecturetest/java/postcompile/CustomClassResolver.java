package de.tum.cit.ase.ares.api.architecturetest.java.postcompile;

import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.importer.ClassFileImporter;

import java.net.URL;
import java.util.Optional;

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
        URL url = CustomClassResolver.class.getResource("/" + typeName.replace(".", "/") + ".class");
        return url != null ? Optional.of(classFileImporter.importUrl(url).get(typeName)) : Optional.empty();
    }
}
