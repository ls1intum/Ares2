package de.tum.cit.ase.ares.api.archunit;

import com.tngtech.archunit.base.DescribedPredicate;
import com.tngtech.archunit.core.domain.*;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.lang.syntax.ArchRuleDefinition;
import de.tum.cit.ase.ares.api.archunit.conditions.TransitivelyAccessesMethodsCondition;
import org.apiguardian.api.API;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

/**
 * This class executes the security rules on the architecture.
 */
@API(status = API.Status.INTERNAL)
public class SecurityRules {

    private static final Logger log = LoggerFactory.getLogger(SecurityRules.class);

    private SecurityRules() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * This method checks if any class in the given package accesses the file system.
     *
     * @return The ArchRule to check for file system access.
     */
    public static ArchRule noClassesShouldAccessFileSystem(String studentPackage) {
        // read from the native access classes
        final List<String> nativeAccessClasses = new ArrayList<>();
        try {
            nativeAccessClasses.addAll(Files.readAllLines(Path.of("src/main/resources/archunit/files/native-access-classes")));
        } catch (Exception e) {
            log.error("Error reading native access classes file", e);
        }

        // read from the file system access methods
        final List<String> fileSystemAccessMethods = new ArrayList<>();
        try {
            fileSystemAccessMethods.addAll(Files.readAllLines(Path.of("src/main/resources/archunit/files/file-system-access-methods")));
        } catch (Exception e) {
            log.error("Error reading file system access methods file", e);
        }

        // Packages that should not be accessed
        List<String> bannedPackages = List.of(
                "java.util.prefs",
                "sun.print",
                "sun.security",
                "java.util.jar",
                "sun.awt.X11",
                "sun.awt",
                "javax.imageio.stream",
                "javax.sound",
                "javax.swing.filechooser",
                "java.awt.desktop");

        return ArchRuleDefinition.noClasses()
                .that()
                .resideInAPackage(studentPackage + "..")
                .should(new TransitivelyAccessesMethodsCondition(new DescribedPredicate<>("accesses file system") {
                    @Override
                    public boolean test(JavaAccess<?> javaAccess) {
                        // These classes are called transitively when accessing the filesystem
                        if ("java.io.FileSystem".equals(javaAccess.getTargetOwner().getFullName()) ||
                                "java.nio.file.FileSystem".equals(javaAccess.getTargetOwner().getFullName())) {
                            return true;
                        }

                        // These methods are called when accessing the filesystem
                        if (fileSystemAccessMethods.contains(javaAccess.getTarget().getFullName())) {
                            return true;
                        }

                        // These classes call native methods that access the filesystem
                        if (nativeAccessClasses.contains(javaAccess.getTargetOwner().getFullName())) {
                            return true;
                        }

                        // These packages should not be accessed
                        return bannedPackages.contains(javaAccess.getTargetOwner().getPackageName());
                    }
                }));
    }
}
