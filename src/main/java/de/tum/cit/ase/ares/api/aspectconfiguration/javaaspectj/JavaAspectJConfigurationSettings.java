package de.tum.cit.ase.ares.api.aspectconfiguration.javaaspectj;

import de.tum.cit.ase.ares.api.policy.SecurityPolicy.FilePermission;
import de.tum.cit.ase.ares.api.policy.SecurityPolicy.NetworkPermission;
import de.tum.cit.ase.ares.api.policy.SecurityPolicy.CommandPermission;
import de.tum.cit.ase.ares.api.policy.SecurityPolicy.ThreadPermission;
import de.tum.cit.ase.ares.api.policy.SecurityPolicy.PackagePermission;

import java.util.List;

public class JavaAspectJConfigurationSettings {

    static List<FilePermission> allowedFileSystemInteractions = null;

    static List<NetworkPermission> allowedNetworkConnections = null;

    static List<CommandPermission> allowedCommandExecutions = null;

    static List<ThreadPermission> allowedThreadCreations = null;

    static List<PackagePermission> allowedPackageImports = null;

}
