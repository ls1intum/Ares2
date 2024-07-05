package de.tum.cit.ase.ares.api.archunit;

import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.thirdparty.com.google.common.collect.ImmutableList;
import org.apiguardian.api.API;

import java.util.List;

/**
 * This class defines the security rules on the architecture to be executed with their respective configurations
 */
@API(status = API.Status.INTERNAL)
public class SecurityRuleFactory {

    /**
     * The security configuration
     */
    private final ArchSecurityConfiguration archSecurityConfiguration;

    public SecurityRuleFactory() {
        this.archSecurityConfiguration = ConfigurationParser.parseConfiguration();
    }

    public List<ArchRule> getSecurityRules(String studentSubmissionPackage) {
        ImmutableList.Builder<ArchRule> rulesBuilder = ImmutableList.builder();
        if (!archSecurityConfiguration.allowFileSystemInteractions()) {
            rulesBuilder.add(SecurityRules.noClassesShouldAccessFileSystem(studentSubmissionPackage));
        }
        return rulesBuilder.build();
    }
}
