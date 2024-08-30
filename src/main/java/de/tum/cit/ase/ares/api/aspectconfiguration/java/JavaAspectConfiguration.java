package de.tum.cit.ase.ares.api.aspectconfiguration.java;

import de.tum.cit.ase.ares.api.aspectconfiguration.AspectConfiguration;
import de.tum.cit.ase.ares.api.policy.SecurityPolicy;

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

    public static final String END_OF_LIST = "\n);\n";
    
    private final JavaSupportedAspectConfiguration javaSupportedAspectConfiguration;
    private final SecurityPolicy securityPolicy;

    /**
     * Constructor for JavaAspectConfiguration.
     *
     * @param javaSupportedAspectConfiguration Selects the supported aspect configuration in the Java programming language
     * @param securityPolicy                   Security policy for the aspect configuration
     */
    public JavaAspectConfiguration(JavaSupportedAspectConfiguration javaSupportedAspectConfiguration, SecurityPolicy securityPolicy) {
        this.javaSupportedAspectConfiguration = javaSupportedAspectConfiguration;
        this.securityPolicy = securityPolicy;
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
                content.append(END_OF_LIST);
            }
            case NETWORK_CONNECTION -> {
                content.append("private static final List<NetworkConnection> allowedNetworkConnections = List.of(\n");
                content.append(securityPolicy.iAllowTheFollowingNetworkConnectionsForTheStudents().stream()
                        .map(securityContent -> String.format("new NetworkConnection(\"%s\", \"%s\", %d)",
                                securityContent.forThisDomain(),
                                securityContent.forThisIPAddress(),
                                securityContent.iAllowTheStudentsToAccessThePort()))
                        .collect(Collectors.joining(",\n")));
                content.append(END_OF_LIST);
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
                content.append(END_OF_LIST);
            }
            case THREAD_CREATION -> {
                content.append("private static final List<ThreadCreation> allowedThreadCreations = List.of(\n");
                content.append(securityPolicy.iAllowTheFollowingThreadCreationsForTheStudents().stream()
                        .map(securityContent -> String.format("new ThreadCreation(\"%s\")",
                                securityContent.iAllowTheStudentsToCreateOneThreadFromTheFollowingClass()))
                        .collect(Collectors.joining(",\n")));
                content.append(END_OF_LIST);
            }
            case PACKAGE_IMPORT -> {
                content.append("private static final List<PackageImport> allowedPackageImports = List.of(\n");
                content.append(securityPolicy.iAllowTheFollowingPackageImportForTheStudents().stream()
                        .map(securityContent -> String.format("new PackageImport(\"%s\")",
                                securityContent.iAllowTheStudentsToImportTheFollowingPackage()))
                        .collect(Collectors.joining(",\n")));
                content.append(END_OF_LIST);
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
        switch (javaSupportedAspectConfiguration) {
            case FILESYSTEM_INTERACTION -> JavaAspectConfigurationLists.setAllowedFileSystemInteractions(securityPolicy == null ? null : securityPolicy.iAllowTheFollowingFileSystemInteractionsForTheStudents());
            case NETWORK_CONNECTION -> JavaAspectConfigurationLists.setAllowedNetworkConnections(securityPolicy == null ? null : securityPolicy.iAllowTheFollowingNetworkConnectionsForTheStudents());
            case COMMAND_EXECUTION -> JavaAspectConfigurationLists.setAllowedCommandExecutions(securityPolicy == null ? null : securityPolicy.iAllowTheFollowingCommandExecutionsForTheStudents());
            case THREAD_CREATION -> JavaAspectConfigurationLists.setAllowedThreadCreations(securityPolicy == null ? null : securityPolicy.iAllowTheFollowingThreadCreationsForTheStudents());
            case PACKAGE_IMPORT -> JavaAspectConfigurationLists.setAllowedPackageImports(securityPolicy == null ? null : securityPolicy.iAllowTheFollowingPackageImportForTheStudents());
            default -> throw new UnsupportedOperationException("Unsupported configuration: " + javaSupportedAspectConfiguration);
        }
    }
}
