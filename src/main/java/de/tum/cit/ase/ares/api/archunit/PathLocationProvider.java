package de.tum.cit.ase.ares.api.archunit;

import com.tngtech.archunit.core.importer.Location;
import com.tngtech.archunit.junit.LocationProvider;

import java.nio.file.Paths;
import java.util.Collections;
import java.util.Set;

public class PathLocationProvider implements LocationProvider {
    @Override
    public Set<Location> get(Class<?> testClass) {
        if (!testClass.isAnnotationPresent(StudentPath.class)) {
            throw new IllegalArgumentException(
                    String.format("%s can only be used on classes annotated with @%s",
                            getClass().getSimpleName(), StudentPath.class.getSimpleName()));
        }

        String path = testClass.getAnnotation(StudentPath.class).value();
        return Collections.singleton(Location.of(Paths.get(path)));
    }
}
