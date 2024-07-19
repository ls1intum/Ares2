package de.tum.cit.ase.ares.api.aspect;

import de.tum.cit.ase.ares.api.policy.SecurityPolicy;

public class JavaAspectConfiguration implements AspectConfiguration {

    @Override
    public void runAspectJConfiguration(SecurityPolicy securityPolicy) {
        FileSystemInteractionList.setAllowedFileSystemInteractions(securityPolicy.iAllowTheFollowingFileSystemInteractionsForTheStudents());
    }

    @Override
    public String createAspectConfiguration(SecurityPolicy securityPolicy) {
        // Not implemented
        return null;
    }

}
