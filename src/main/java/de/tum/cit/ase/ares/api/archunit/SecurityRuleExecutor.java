package de.tum.cit.ase.ares.api.archunit;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import org.apiguardian.api.API;

/**
 * Executes the rules specified in the security configuration.
 */
@API(status = API.Status.MAINTAINED)
public class SecurityRuleExecutor {

    /**
     * Returns the security rules to be executed according to the configuration file
     */
    private final SecurityRuleFactory securityRuleFactory = new SecurityRuleFactory();

    /**
     * The path to the student submission
     */
    private final String studentSubmissionPath;

    /**
     * Returns the Java classes to be analyzed
     * @return The Java classes to be analyzed
     */
    private JavaClasses javaClassesToAnalyze() {
        return new ClassFileImporter()
                .importPackages(securityRuleFactory.getDependencies()
                        .stream()
                        .filter(dependency -> !dependency.getOriginClass().getPackageName().equals("java.lang"))
                        .map(dependency -> dependency.getOriginClass().getPackageName()).toArray(String[]::new));
    }

    /**
     * Constructor
     * @param studentSubmissionPath The path to the student submission
     */
    public SecurityRuleExecutor(String studentSubmissionPath) {
        this.studentSubmissionPath = studentSubmissionPath;
    }

    /**
     * Executes the security rules
     */
    public void executeSecurityRules() {
        JavaClasses javaClasses = javaClassesToAnalyze();
        securityRuleFactory.getSecurityRules(studentSubmissionPath).forEach(rule -> rule.check(javaClasses));
    }
}
