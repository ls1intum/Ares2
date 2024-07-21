package de.tum.cit.ase.ares.api.architecturetest.java.postcompile;

import com.tngtech.archunit.base.DescribedPredicate;
import com.tngtech.archunit.core.domain.JavaAccess;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.lang.syntax.ArchRuleDefinition;
import de.tum.cit.ase.ares.api.architecturetest.ArchitectureTestCaseStorage;

import java.util.Objects;

import static de.tum.cit.ase.ares.api.architecturetest.java.JavaSupportedArchitectureTestCase.FILESYSTEM_INTERACTION;

public class SecurityRules {

    private SecurityRules() {
        throw new IllegalArgumentException("Utility class");
    }

    public static final ArchRule FILE_SYSTEM_INTERACTION_RULE = ArchRuleDefinition.noClasses()
            .should(new TransitivelyAccessesMethodsCondition(new DescribedPredicate<>("accesses file system") {
                @Override
                public boolean test(JavaAccess<?> javaAccess) {
                    return Objects.requireNonNull(ArchitectureTestCaseStorage
                                    .FORBIDDEN_METHODS_FOR_SUPPORTED_ARCHITECTURAL_TEST_CASE.build().get(FILESYSTEM_INTERACTION.name()))
                            .contains(javaAccess.getTarget().getFullName());
                }
            }));
}
