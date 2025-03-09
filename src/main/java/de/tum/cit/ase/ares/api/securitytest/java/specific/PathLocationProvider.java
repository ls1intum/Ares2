package de.tum.cit.ase.ares.api.securitytest.java.specific;

import com.tngtech.archunit.core.importer.Location;
import com.tngtech.archunit.junit.LocationProvider;
import de.tum.cit.ase.ares.api.securitytest.java.StudentCompiledClassesPath;

import javax.annotation.Nonnull;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Set;

/**
 * Provides the location of the compiled student classes.
 *
 * <p>Description: This class implements the {@link LocationProvider} interface to supply the location of compiled student classes for analysis during tests.
 * The location is determined based on the {@link StudentCompiledClassesPath} annotation present on the test class.
 * If the annotation is not present, a SecurityException is thrown.</p>
 *
 * <p>Design Rationale: By utilising an annotation-based approach, this design supports flexible integration of student class locations within the testing framework,
 * enhancing modularity and maintainability.</p>
 *
 * @since 2.0.0
 * @author Sarp Sahinalp
 * @version 2.0.0
 */
public class PathLocationProvider implements LocationProvider {

    /**
     * Retrieves the location of the compiled student classes from the specified test class.
     *
     * @since 2.0.0
     * @author Sarp Sahinalp
     * @param testClass the class on which the test is performed; must be annotated with {@link StudentCompiledClassesPath}
     * @return a {@link Set} containing the {@link Location} of the compiled student classes
     * @throws SecurityException if the provided test class is not annotated with {@link StudentCompiledClassesPath}
     */
    @Override
    @Nonnull
    public Set<Location> get(@Nonnull Class<?> testClass) {
        if (!testClass.isAnnotationPresent(StudentCompiledClassesPath.class)) {
            throw new SecurityException(
                    String.format("Ares Security Error (Reason: Ares-Code; Stage: Creation): %s can only be used on classes annotated with @%s",
                            getClass().getSimpleName(), StudentCompiledClassesPath.class.getSimpleName()));
        }

        @Nonnull String path = testClass.getAnnotation(StudentCompiledClassesPath.class).value();
        return Collections.singleton(Location.of(Paths.get(path)));
    }
}