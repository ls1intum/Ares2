package de.tum.cit.ase.ares.api.aspectconfiguration.java;

import de.tum.cit.ase.ares.api.policy.*;

import java.util.List;

public class JavaAspectConfigurationLists {

    private JavaAspectConfigurationLists() {
        throw new IllegalStateException("Utility class");
    }

    private static List<FileSystemInteraction> allowedFileSystemInteractions = null;

    private static List<NetworkConnection> allowedNetworkConnections = null;

    private static List<CommandExecution> allowedCommandExecutions = null;

    private static List<ThreadCreation> allowedThreadCreations = null;

    private static List<PackageImport> allowedPackageImports = null;

    public static List<FileSystemInteraction> getAllowedFileSystemInteractions() {
        return allowedFileSystemInteractions;
    }

    public static void setAllowedFileSystemInteractions(List<FileSystemInteraction> allowedFileSystemInteractions) {
        JavaAspectConfigurationLists.allowedFileSystemInteractions = allowedFileSystemInteractions;
    }

    public static List<NetworkConnection> getAllowedNetworkConnections() {
        return allowedNetworkConnections;
    }

    public static void setAllowedNetworkConnections(List<NetworkConnection> allowedNetworkConnections) {
        JavaAspectConfigurationLists.allowedNetworkConnections = allowedNetworkConnections;
    }

    public static List<CommandExecution> getAllowedCommandExecutions() {
        return allowedCommandExecutions;
    }

    public static void setAllowedCommandExecutions(List<CommandExecution> allowedCommandExecutions) {
        JavaAspectConfigurationLists.allowedCommandExecutions = allowedCommandExecutions;
    }

    public static List<ThreadCreation> getAllowedThreadCreations() {
        return allowedThreadCreations;
    }

    public static void setAllowedThreadCreations(List<ThreadCreation> allowedThreadCreations) {
        JavaAspectConfigurationLists.allowedThreadCreations = allowedThreadCreations;
    }

    public static List<PackageImport> getAllowedPackageImports() {
        return allowedPackageImports;
    }

    public static void setAllowedPackageImports(List<PackageImport> allowedPackageImports) {
        JavaAspectConfigurationLists.allowedPackageImports = allowedPackageImports;
    }

    // Set all the lists to null
    public static void reset() {
        JavaAspectConfigurationLists.allowedFileSystemInteractions = null;
        JavaAspectConfigurationLists.allowedNetworkConnections = null;
        JavaAspectConfigurationLists.allowedCommandExecutions = null;
        JavaAspectConfigurationLists.allowedThreadCreations = null;
        JavaAspectConfigurationLists.allowedPackageImports = null;
    }
}
