package de.tum.cit.ase.ares.api.architecturetest.java.postcompile;

import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;

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
        allClasses = new ClassFileImporter()
                .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
                .withImportOption(location -> location.toString().contains("jrt:/"))
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
            return Optional.ofNullable(allClasses.get(typeName));
        } catch (IllegalArgumentException e) {
            return Optional.empty();
        }
    }
}
