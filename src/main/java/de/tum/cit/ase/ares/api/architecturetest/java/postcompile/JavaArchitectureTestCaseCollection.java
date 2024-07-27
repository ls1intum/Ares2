package de.tum.cit.ase.ares.api.architecturetest.java.postcompile;

import com.tngtech.archunit.base.DescribedPredicate;
import com.tngtech.archunit.core.domain.JavaAccess;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.lang.syntax.ArchRuleDefinition;
import de.tum.cit.ase.ares.api.architecturetest.java.ArchitectureTestCaseStorage;

import java.util.List;

import static de.tum.cit.ase.ares.api.architecturetest.java.JavaSupportedArchitectureTestCase.FILESYSTEM_INTERACTION;

/**
 * This class runs the security rules on the architecture for the post-compile mode.
 */
public class JavaArchitectureTestCaseCollection {

    private JavaArchitectureTestCaseCollection() {
        throw new IllegalArgumentException("This class should not be instantiated");
    }

    /**
     * The packages that should not be accessed by the student submission.
     */
    private static final List<String> bannedFileSystemAccessPackages = List.of(
            "java.nio.file",
            "java.util.prefs",
            "sun.print",
            "sun.security",
            "java.util.jar",
            "java.util.zip",
            "sun.awt.X11",
            "javax.imageio",
            "javax.sound.midi",
            "javax.swing.filechooser",
            "java.awt.desktop");

    /**
     * This method checks if any class in the given package accesses the file system.
     */
    public static final ArchRule NO_CLASS_SHOULD_ACCREE_FILE_SYSTEM = ArchRuleDefinition.noClasses()
            .should(new TransitivelyAccessesMethodsCondition(new DescribedPredicate<>("accesses file system") {
                @Override
                public boolean test(JavaAccess<?> javaAccess) {
                    if (bannedFileSystemAccessPackages.stream().anyMatch(p -> javaAccess.getTarget().getFullName().startsWith(p))) {
                        return true;
                    }
                    return ArchitectureTestCaseStorage.getForbiddenMethods(FILESYSTEM_INTERACTION.name())
                            .contains(javaAccess.getTarget().getFullName());
                }
            }));
}