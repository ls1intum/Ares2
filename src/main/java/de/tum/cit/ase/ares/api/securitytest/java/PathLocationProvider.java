package de.tum.cit.ase.ares.api.securitytest.java;

import com.tngtech.archunit.core.importer.Location;
import com.tngtech.archunit.junit.LocationProvider;

import javax.annotation.Nonnull;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Set;

/**
 * Provides the location of the compiled student classes for analysis during tests.
 * <p>
 * This class implements the {@link LocationProvider} interface to supply the
 * location of the compiled student classes, which are used in pre-compile mode tests
 * to analyze the compiled classes within the test repository. The location is
 * determined based on the {@link StudentCompiledClassesPath} annotation present
 * on the test class.
 * </p>
 */
public class PathLocationProvider implements LocationProvider {

    /**
     * Retrieves the location of the compiled student classes for the specified test class.
     * <p>
     * This method checks if the provided test class is annotated with {@link StudentCompiledClassesPath}.
     * If the annotation is present, it extracts the path to the compiled classes from the annotation's value.
     * Otherwise, it throws an {@link IllegalArgumentException}.
     * </p>
     *
     * @param testClass the class on which the test is being performed; must be annotated with {@link StudentCompiledClassesPath}.
     * @return a {@link Set} containing the {@link Location} of the compiled student classes.
     * @throws IllegalArgumentException if the provided test class is not annotated with {@link StudentCompiledClassesPath}.
     */
    @Override
    @Nonnull
    public Set<Location> get(@Nonnull Class<?> testClass) {
        if (!testClass.isAnnotationPresent(StudentCompiledClassesPath.class)) {

            //TODO Ajay: An IllegalArgumentException should be thrown here instead of SecurityException
            throw new SecurityException(
                    String.format("Ares Security Error (Reason: Ares-Code; Stage: Creation): %s can only be used on classes annotated with @%s",
                            getClass().getSimpleName(), StudentCompiledClassesPath.class.getSimpleName()));
        }

        @Nonnull String path = testClass.getAnnotation(StudentCompiledClassesPath.class).value();
        return Collections.singleton(Location.of(Paths.get(path)));
    }
}