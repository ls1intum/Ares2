package de.tum.cit.ase.ares.api.aop.java.aspectj;

import de.tum.cit.ase.ares.api.policy.SecurityPolicy.FilePermission;
import de.tum.cit.ase.ares.api.policy.SecurityPolicy.NetworkPermission;
import de.tum.cit.ase.ares.api.policy.SecurityPolicy.CommandPermission;
import de.tum.cit.ase.ares.api.policy.SecurityPolicy.ThreadPermission;

import java.util.List;

public class JavaAspectJConfigurationSettings {

    private JavaAspectJConfigurationSettings() {
        throw new IllegalStateException("Utility class");
    }

    private static List<FilePermission> allowedFileSystemInteractions = null;

    private static List<NetworkPermission> allowedNetworkConnections = null;

    private static List<CommandPermission> allowedCommandExecutions = null;

    private static List<ThreadPermission> allowedThreadCreations = null;

    public static List<FilePermission> getAllowedFileSystemInteractions() {
        return allowedFileSystemInteractions;
    }

    public static void setAllowedFileSystemInteractions(List<FilePermission> allowedFileSystemInteractions) {
        JavaAspectJConfigurationSettings.allowedFileSystemInteractions = allowedFileSystemInteractions;
    }

    public static List<NetworkPermission> getAllowedNetworkConnections() {
        return allowedNetworkConnections;
    }

    public static void setAllowedNetworkConnections(List<NetworkPermission> allowedNetworkConnections) {
        JavaAspectJConfigurationSettings.allowedNetworkConnections = allowedNetworkConnections;
    }

    public static List<CommandPermission> getAllowedCommandExecutions() {
        return allowedCommandExecutions;
    }

    public static void setAllowedCommandExecutions(List<CommandPermission> allowedCommandExecutions) {
        JavaAspectJConfigurationSettings.allowedCommandExecutions = allowedCommandExecutions;
    }

    public static List<ThreadPermission> getAllowedThreadCreations() {
        return allowedThreadCreations;
    }

    public static void setAllowedThreadCreations(List<ThreadPermission> allowedThreadCreations) {
        JavaAspectJConfigurationSettings.allowedThreadCreations = allowedThreadCreations;
    }

    // Set all the lists to null
    public static void reset() {
        JavaAspectJConfigurationSettings.allowedFileSystemInteractions = null;
        JavaAspectJConfigurationSettings.allowedNetworkConnections = null;
        JavaAspectJConfigurationSettings.allowedCommandExecutions = null;
        JavaAspectJConfigurationSettings.allowedThreadCreations = null;
    }
}
