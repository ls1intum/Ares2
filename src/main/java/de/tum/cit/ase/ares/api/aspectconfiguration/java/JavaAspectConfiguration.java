package de.tum.cit.ase.ares.api.aspectconfiguration.java;

import de.tum.cit.ase.ares.api.aspectconfiguration.AspectConfiguration;
import de.tum.cit.ase.ares.api.policy.SecurityPolicy;

import java.nio.file.Path;
import java.util.stream.Collectors;

/**
 * Aspect configuration for the Java programming language and concrete product of the abstract factory design pattern.
 *
 * @author Markus Paulsen
 * @version 2.0.0
 * @see <a href="https://refactoring.guru/design-patterns/abstract-factory">Abstract Factory Design Pattern</a>
 * @since 2.0.0
 */
public class JavaAspectConfiguration implements AspectConfiguration {

    private final JavaSupportedAspectConfiguration javaSupportedAspectConfiguration;
    private final SecurityPolicy securityPolicy;
    // TODO: this is currently not used
    private final Path withinPath;

    /**
     * Constructor for JavaAspectConfiguration.
     *
     * @param javaSupportedAspectConfiguration Selects the supported aspect configuration in the Java programming language
     * @param securityPolicy                   Security policy for the aspect configuration
     * @param withinPath
     */
    public JavaAspectConfiguration(JavaSupportedAspectConfiguration javaSupportedAspectConfiguration, SecurityPolicy securityPolicy, Path withinPath) {
        this.javaSupportedAspectConfiguration = javaSupportedAspectConfiguration;
        this.securityPolicy = securityPolicy;
        this.withinPath = withinPath;
    }

    /**
     * Creates the content of the aspect configuration file in the Java programming language.
     *
     * @return Content of the aspect configuration file
     */
    @Override
    public String createAspectConfigurationFileContent() {
        StringBuilder content = new StringBuilder();
        switch (javaSupportedAspectConfiguration) {
            case FILESYSTEM_INTERACTION -> {
                content.append("private static final List<FileSystemInteraction> allowedFileSystemInteractions = List.of(\n");
                content.append(securityPolicy.iAllowTheFollowingFileSystemInteractionsForTheStudents().stream()
                        .map(securityContent -> String.format("new FileSystemInteraction(Path.of(\"%s\"), %s, %s, %s)",
                                securityContent.onThisPathAndAllPathsBelow().toString(),
                                securityContent.studentsAreAllowedToOverwriteAllFiles(),
                                securityContent.studentsAreAllowedToReadAllFiles(),
                                securityContent.studentsAreAllowedToDeleteAllFiles()))
                        .collect(Collectors.joining(",\n")));
                content.append("\n);\n");
            }
            case NETWORK_CONNECTION -> {
                content.append("private static final List<NetworkConnection> allowedNetworkConnections = List.of(\n");
                content.append(securityPolicy.iAllowTheFollowingNetworkConnectionsForTheStudents().stream()
                        .map(securityContent -> String.format("new NetworkConnection(\"%s\", \"%s\", %d)",
                                securityContent.forThisDomain(),
                                securityContent.forThisIPAddress(),
                                securityContent.iAllowTheStudentsToAccessThePort()))
                        .collect(Collectors.joining(",\n")));
                content.append("\n);\n");
            }
            case COMMAND_EXECUTION -> {
                content.append("private static final List<CommandExecution> allowedCommandExecutions = List.of(\n");
                content.append(securityPolicy.iAllowTheFollowingCommandExecutionsForTheStudents().stream()
                        .map(securityContent -> String.format("new CommandExecution(\"%s\", List.of(%s))",
                                securityContent.iAllowTheStudentsToUseTheCommand(),
                                securityContent.withTheFollowingArguments().stream()
                                        .map(arg -> "\"" + arg + "\"")
                                        .collect(Collectors.joining(", "))))
                        .collect(Collectors.joining(",\n")));
                content.append("\n);\n");
            }
            case THREAD_CREATION -> {
                content.append("private static final List<ThreadCreation> allowedThreadCreations = List.of(\n");
                content.append(securityPolicy.iAllowTheFollowingThreadCreationsForTheStudents().stream()
                        .map(securityContent -> String.format("new ThreadCreation(\"%s\")",
                                securityContent.iAllowTheStudentsToCreateOneThreadFromTheFollowingClass()))
                        .collect(Collectors.joining(",\n")));
                content.append("\n);\n");
            }
            case PACKAGE_IMPORT -> {
                content.append("private static final List<PackageImport> allowedPackageImports = List.of(\n");
                content.append(securityPolicy.iAllowTheFollowingPackageImportForTheStudents().stream()
                        .map(securityContent -> String.format("new PackageImport(\"%s\")",
                                securityContent.iAllowTheStudentsToImportTheFollowingPackage()))
                        .collect(Collectors.joining(",\n")));
                content.append("\n);\n");
            }
            default -> throw new UnsupportedOperationException("Unsupported configuration: " + javaSupportedAspectConfiguration);
        }
        return content.toString();
    }

    /**
     * Runs the aspect configuration in Java programming language.
     */
    @Override
    public void runAspectConfiguration() {
        if (securityPolicy == null) {
            deactivateAspectJConfig();
            return;
        }
        // TODO: This is definitely a code smell. Why do we change static stuff from a non-static method take a look at java:S2696
        switch (javaSupportedAspectConfiguration) {
            case FILESYSTEM_INTERACTION -> JavaAspectConfigurationLists.allowedFileSystemInteractions = securityPolicy.iAllowTheFollowingFileSystemInteractionsForTheStudents();
            case NETWORK_CONNECTION -> JavaAspectConfigurationLists.allowedNetworkConnections = securityPolicy.iAllowTheFollowingNetworkConnectionsForTheStudents();
            case COMMAND_EXECUTION -> JavaAspectConfigurationLists.allowedCommandExecutions = securityPolicy.iAllowTheFollowingCommandExecutionsForTheStudents();
            case THREAD_CREATION -> JavaAspectConfigurationLists.allowedThreadCreations = securityPolicy.iAllowTheFollowingThreadCreationsForTheStudents();
            case PACKAGE_IMPORT -> JavaAspectConfigurationLists.allowedPackageImports = securityPolicy.iAllowTheFollowingPackageImportForTheStudents();
            default -> throw new UnsupportedOperationException("Unsupported configuration: " + javaSupportedAspectConfiguration);
        }
    }

    // TODO: Why do we even need this??? This should be handled more elegantly.
    public static void deactivateAspectJConfig () {
        JavaAspectConfigurationLists.allowedFileSystemInteractions = null;
        JavaAspectConfigurationLists.allowedNetworkConnections = null;
        JavaAspectConfigurationLists.allowedCommandExecutions = null;
        JavaAspectConfigurationLists.allowedThreadCreations = null;
        JavaAspectConfigurationLists.allowedPackageImports = null;
    }
}
