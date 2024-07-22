package de.tum.cit.ase.ares.api.architecturetest.java.postcompile;

import com.tngtech.archunit.base.DescribedPredicate;
import com.tngtech.archunit.core.domain.JavaAccess;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.lang.syntax.ArchRuleDefinition;
import de.tum.cit.ase.ares.api.architecturetest.java.ArchitectureTestCaseStorage;

import java.util.List;

import static de.tum.cit.ase.ares.api.architecturetest.java.JavaSupportedArchitectureTestCase.FILESYSTEM_INTERACTION;

/**
 * Defines security rules for the Java programming language in post-compile mode.
 */
public class SecurityRules {

    private SecurityRules() {
        throw new IllegalArgumentException("This class should not be instantiated");
    }

    /**
     * The packages that should not be accessed by the student submission.
     */
    private static final List<String> bannedPackages = List.of(
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

    /**
     * Rule for file system interaction.
     */
    public static final ArchRule FILE_SYSTEM_INTERACTION_RULE = ArchRuleDefinition.noClasses()
            .should(new TransitivelyAccessesMethodsCondition(new DescribedPredicate<>("accesses file system") {
                @Override
                public boolean test(JavaAccess<?> javaAccess) {
                    if (bannedPackages.contains(javaAccess.getTargetOwner().getPackageName())) {
                        return true;
                    }
                    return ArchitectureTestCaseStorage.getForbiddenMethods(FILESYSTEM_INTERACTION.name())
                            .contains(javaAccess.getTarget().getFullName());
                }
            }));
}
