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
     * @return The ArchRule to check for file system access.
     */
    public static ArchRule noClassesShouldAccessFileSystem() {
        // Packages that should not be accessed
        List<String> bannedPackages = List.of(
                "java.nio.file",
                "java.util.prefs",
                "sun.print",
                "sun.security",
                "java.util.jar",
                "sun.awt.X11",
                "javax.imageio.stream",
                "javax.sound.midi",
                "javax.swing.filechooser",
                "java.awt.desktop");

        return ArchRuleDefinition.noClasses()
                .should(new TransitivelyAccessesMethodsCondition(new DescribedPredicate<>("accesses file system") {
                    @Override
                    public boolean test(JavaAccess<?> javaAccess) {
                        if (javaAccess.getTarget().getFullName().equals("java.io.FilePermission.<init>(java.lang.String, java.lang.String)")) {
                            return true;
                        }

                        // These packages should not be accessed
                        return bannedPackages.contains(javaAccess.getTargetOwner().getPackageName());
                    }
                }));
    }
}