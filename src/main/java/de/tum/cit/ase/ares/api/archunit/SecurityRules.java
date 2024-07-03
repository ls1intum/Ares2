package de.tum.cit.ase.ares.api.archunit;

import com.tngtech.archunit.base.DescribedPredicate;
import com.tngtech.archunit.core.domain.*;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.lang.syntax.ArchRuleDefinition;
import de.tum.cit.ase.ares.api.archunit.conditions.TransitivelyAccessesMethodsCondition;
import org.apiguardian.api.API;

import java.util.*;

/**
 * This class executes the security rules on the architecture.
 */
@API(status = API.Status.INTERNAL)
public class SecurityRules {

    private SecurityRules() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * This method checks if any class in the given package accesses the file system.
     *
     * @param assignmentPackage The package to check for file system access.
     * @return The ArchRule to check for file system access.
     */
    public static ArchRule noClassesShouldAccessFileSystem(String assignmentPackage) {
        List<String> bannedPackages = List.of(
                "java.util.prefs",
                "sun.security",
                "java.util.jar",
                "javax.imageio.stream",
                "javax.sound",
                "javax.swing.filechooser",
                "java.awt.desktop");

        return ArchRuleDefinition.noClasses()
                .that()
                .resideInAPackage(assignmentPackage)
                .should(new TransitivelyAccessesMethodsCondition(new DescribedPredicate<>("never matches") {
                    @Override
                    public boolean test(JavaAccess<?> javaAccess) {
                        if ("java.io.FileSystem".equals(javaAccess.getTargetOwner().getFullName()) ||
                                "java.nio.file.FileSystem".equals(javaAccess.getTargetOwner().getFullName()) ||
                                "java.util.zip.ZipFile".equals(javaAccess.getTargetOwner().getFullName())){
                            return true;
                        }
                        return bannedPackages.contains(javaAccess.getTargetOwner().getPackageName());
                    }
                }));
    }
}
