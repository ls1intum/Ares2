package de.tum.cit.ase.ares.api.securitytest.java;

import com.tngtech.archunit.core.importer.Location;
import com.tngtech.archunit.junit.LocationProvider;

import java.nio.file.Paths;
import java.util.Collections;
import java.util.Set;

/**
 * Provides the location of the compiled student classes.
 * Is used by the pre-compile mode tests to analyze compiled classes within the test repository
 */
public class PathLocationProvider implements LocationProvider {
    @Override
    public Set<Location> get(Class<?> testClass) {
        if (!testClass.isAnnotationPresent(StudentCompiledClassesPath.class)) {
            throw new IllegalArgumentException(
                    String.format("%s can only be used on classes annotated with @%s",
                            getClass().getSimpleName(), StudentCompiledClassesPath.class.getSimpleName()));
        }

        String path = testClass.getAnnotation(StudentCompiledClassesPath.class).value();
        return Collections.singleton(Location.of(Paths.get(path)));
    }
}
