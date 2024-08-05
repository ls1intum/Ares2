package de.tum.cit.ase.ares.api.architecturetest.java.postcompile;

import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;

import java.util.Optional;

/**
 * Custom class resolver to resolve classes that are outside classpath to be able to analyze them transitively.
 */
public class CustomClassResolver {
    /**
     * Class file importer to import the class files.
     * This is used to import the class files from the URL.
     */
    private final JavaClasses allClasses;

    public CustomClassResolver() {
        // We need to import all classes to be able to resolve them later.
        // TODO: We definitely need to improve this. We should not import all classes as it is not memory efficient.
        // https://www.javadoc.io/doc/com.tngtech.archunit/archunit/0.10.2/com/tngtech/archunit/core/importer/ClassFileImporter.html
        allClasses = new ClassFileImporter()
                .importClasspath();
    }

    /**
     * Try to resolve the class by the given type name.
     *
     * @param typeName The type name of the class to resolve.
     * @return The resolved class if it exists.
     */
    public Optional<JavaClass> tryResolve(String typeName) {
        try {
            // Try to resolve the class by the given type name.
            return Optional.ofNullable(allClasses.get(typeName));
        } catch (IllegalArgumentException e) {
            return Optional.empty();
        }
    }
}
