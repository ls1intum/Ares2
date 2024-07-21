package de.tum.cit.ase.ares.api.architecturetest.java.postcompile;

import com.tngtech.archunit.base.DescribedPredicate;
import com.tngtech.archunit.core.domain.JavaAccess;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.lang.syntax.ArchRuleDefinition;
import de.tum.cit.ase.ares.api.architecturetest.java.ArchitectureTestCaseStorage;

import static de.tum.cit.ase.ares.api.architecturetest.java.JavaSupportedArchitectureTestCase.FILESYSTEM_INTERACTION;

/**
 * Security rules for the Java programming language for post-compile mode.
 */
public class SecurityRules {

    private SecurityRules() {
        throw new IllegalArgumentException("Do not instantiate this class");
    }

    /**
     * Rule for file system interaction.
     */
    public static final ArchRule FILE_SYSTEM_INTERACTION_RULE = ArchRuleDefinition.noClasses()
            .should(new TransitivelyAccessesMethodsCondition(new DescribedPredicate<>("accesses file system") {
                @Override
                public boolean test(JavaAccess<?> javaAccess) {
                    return ArchitectureTestCaseStorage.getForbiddenMethods(FILESYSTEM_INTERACTION.name())
                            .contains(javaAccess.getTarget().getFullName());
                }
            }));
}
