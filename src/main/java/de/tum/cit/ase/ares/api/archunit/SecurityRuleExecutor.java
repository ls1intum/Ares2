package de.tum.cit.ase.ares.api.archunit;

import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import org.apiguardian.api.API;
import org.slf4j.LoggerFactory;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Executes the rules specified in the security configuration.
 */
@API(status = API.Status.MAINTAINED)
public class SecurityRuleExecutor {

    private static final org.slf4j.Logger log = LoggerFactory.getLogger(SecurityRuleExecutor.class);

    /**
     * The class names to exclude from the security rules to improve the performance of the ClassFileImport by 2.5 times
     */
    private final Set<String> classNamesToExclude = Set.of(
            "java.lang.Object",
            "java.lang.String",
            "java.lang.StringBuffer",
            "java.lang.NullPointerException",
            "java.util.TimeZone",
            "java.util.SimpleTimeZone"
    );
    /**
     * Returns the security rules to be executed according to the configuration file
     */
    private final SecurityRuleFactory securityRuleFactory = new SecurityRuleFactory();

    /**
     * The path to the student submission
     */
    private final String studentSubmissionPackage;

    /**
     * The class file importer
     */
    private final ClassFileImporter classFileImporter = new ClassFileImporter();

    /**
     * @param studentSubmissionPackage The path to the student submission
     */
    private SecurityRuleExecutor(String studentSubmissionPackage) {
        this.studentSubmissionPackage = studentSubmissionPackage;
    }

    /**
     * Creates a new instance of the SecurityRuleExecutor
     *
     * @param studentSubmissionPackage The path to the student submission
     * @return A new instance of the SecurityRuleExecutor
     */
    public static SecurityRuleExecutor withinPackage(String studentSubmissionPackage) {
        return new SecurityRuleExecutor(studentSubmissionPackage);
    }

    /**
     * Returns the dependencies of the student submission
     */
    public Set<String> getDependencies(JavaClasses classes) {
        return classes.stream()
                .map(JavaClass::getTransitiveDependenciesFromSelf)
                .flatMap(Set::stream)
                .flatMap(dependency -> Stream.of(
                        dependency.getTargetClass().getPackageName(),
                        dependency.getOriginClass().getPackageName()
                ))
                .collect(Collectors.toSet());
    }


    /**
     * Executes the security rules
     */
    public void executeSecurityRules() {
        Set<String> packages = getDependencies(classFileImporter.importPath("target/classes"));
        if (packages.isEmpty()) {
            log.warn("No dependencies found");
            throw new SecurityException("Given package does not have any dependencies");
        }

        JavaClasses classes = classFileImporter
                .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
                .withImportOption(location -> classNamesToExclude.stream().noneMatch(location::contains))
                .importPath("target/classes");

        securityRuleFactory.getSecurityRules(studentSubmissionPackage)
                .forEach(rule ->
                        rule.check(classes));
    }
}
