package de.tum.cit.ase.ares.api.aspectconfiguration.java;

import de.tum.cit.ase.ares.api.policy.*;

import java.util.ArrayList;
import java.util.List;

public class JavaAspectConfigurationLists {

    static List<FileSystemInteraction> allowedFileSystemInteractions = new ArrayList<>();

    static List<NetworkConnection> allowedNetworkConnections = new ArrayList<>();

    static List<CommandExecution> allowedCommandExecutions = new ArrayList<>();

    static List<ThreadCreation> allowedThreadCreations = new ArrayList<>();

    static List<PackageImport> allowedPackageImports = new ArrayList<>();

}
