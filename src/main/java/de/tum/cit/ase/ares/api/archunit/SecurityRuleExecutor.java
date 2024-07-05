package de.tum.cit.ase.ares.api.archunit;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import org.apiguardian.api.API;
import org.slf4j.LoggerFactory;

import java.util.logging.Logger;

/**
 * Executes the rules specified in the security configuration.
 */
@API(status = API.Status.MAINTAINED)
public class SecurityRuleExecutor {

    private static final org.slf4j.Logger log = LoggerFactory.getLogger(SecurityRuleExecutor.class);
    /**
     * Returns the security rules to be executed according to the configuration file
     */
    private final SecurityRuleFactory securityRuleFactory = new SecurityRuleFactory();


    /**
     * The path to the student submission
     */
    private final String studentSubmissionPackage;

    /**
     *
     * @param studentSubmissionPackage The path to the student submission
     */
    public SecurityRuleExecutor(String studentSubmissionPackage) {
        this.studentSubmissionPackage = studentSubmissionPackage;
    }


    /**
     * Executes the security rules
     */
    public void executeSecurityRules() {
        JavaClasses classes = new ClassFileImporter()
                .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
                .importPackages("..");
        try {
            securityRuleFactory.getSecurityRules(studentSubmissionPackage)
                    .forEach(rule -> rule.check(classes));
        } catch (AssertionError e) {
            // TODO improve error messages
            log.error("Security rules violated", e);
        }
    }
}
