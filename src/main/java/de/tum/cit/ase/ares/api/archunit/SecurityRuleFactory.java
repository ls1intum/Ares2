package de.tum.cit.ase.ares.api.archunit;

import com.tngtech.archunit.core.domain.Dependency;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.lang.ArchRule;
import de.tum.cit.ase.ares.api.util.ProjectSourcesFinder;
import org.apiguardian.api.API;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * This class defines the security rules on the architecture to be executed with their respective configurations
 */
@API(status = API.Status.INTERNAL)
public class SecurityRuleFactory {

    /**
     * The security configuration
     */
    private final ArchSecurityConfiguration archSecurityConfiguration;

    /**
     * The configuration parser
     */
    private final ConfigurationParser configurationParser;

    public SecurityRuleFactory() {
        this.configurationParser = new ConfigurationParser();
        this.archSecurityConfiguration = configurationParser.parseConfiguration();
    }

    public List<ArchRule> getSecurityRules(String studentSubmissionPath) {
        // TODO: Implement the logic to generate the security rules based on the configuration
        return List.of(SecurityRules.noClassesShouldAccessFileSystem(studentSubmissionPath));
    }

    public Set<Dependency> getDependencies() {
        var path = ProjectSourcesFinder.findProjectSourcesPath().orElseThrow(() -> //$NON-NLS-1$
                new AssertionError("Could not find project sources folder." //$NON-NLS-1$
                        + " Make sure the build file is configured correctly." //$NON-NLS-1$
                        + " If it is not located in the execution folder directly," //$NON-NLS-1$
                        + " set the location using AresConfiguration methods.")); //$NON-NLS-1$

        return new ClassFileImporter()
                // TODO change with student submission package
                .importPath(path)
                .stream().map(JavaClass::getTransitiveDependenciesFromSelf)
                .flatMap(Set::stream)
                .collect(Collectors.toSet());
    }
}
