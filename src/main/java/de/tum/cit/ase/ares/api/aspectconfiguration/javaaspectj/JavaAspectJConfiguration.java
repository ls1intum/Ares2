package de.tum.cit.ase.ares.api.aspectconfiguration.javaaspectj;

import de.tum.cit.ase.ares.api.aspectconfiguration.AspectConfiguration;
import de.tum.cit.ase.ares.api.policy.SecurityPolicy.RessourceAccesses;

import java.util.stream.Collectors;

/**
 * Aspect configuration for Java using AspectJ, implementing the AspectConfiguration interface.
 * <p>
 * This class represents the concrete product in the Abstract Factory Design Pattern, specifically
 * for Java configurations that use AspectJ. It generates the content for AspectJ configuration files
 * and applies these configurations at runtime based on the provided security policy.
 * </p>
 *
 * @version 2.0.0
 * @see <a href="https://refactoring.guru/design-patterns/abstract-factory">Abstract Factory Design Pattern</a>
 * @since 2.0.0
 */
public class JavaAspectJConfiguration implements AspectConfiguration {

    private final JavaAspectJConfigurationSupported javaAspectJConfigurationSupported;
    private final RessourceAccesses ressourceAccesses;

    /**
     * Constructs a new JavaAspectJConfiguration with the specified configuration and resource accesses.
     *
     * @param javaAspectJConfigurationSupported the specific type of aspect configuration to support.
     * @param ressourceAccesses                 the resource accesses defined by the security policy.
     */
    public JavaAspectJConfiguration(JavaAspectJConfigurationSupported javaAspectJConfigurationSupported, RessourceAccesses ressourceAccesses) {
        this.javaAspectJConfigurationSupported = javaAspectJConfigurationSupported;
        this.ressourceAccesses = ressourceAccesses;
    }

    /**
     * Creates the content of the aspect configuration file in the Java programming language using AspectJ.
     * <p>
     * This method generates the content of the aspect configuration file based on the supported configuration
     * type (e.g., file system interaction, network connection, command execution, thread creation). It processes
     * the permissions defined in the security policy and converts them into configuration settings.
     * </p>
     *
     * @return the content of the aspect configuration file as a {@link String}.
     */
    @Override
    public String createAspectConfigurationFileContent() {
        StringBuilder content = new StringBuilder();
        switch (javaAspectJConfigurationSupported) {
            case FILESYSTEM_INTERACTION -> {
                content.append("private static final List<FileSystemInteraction> allowedFileSystemInteractions = List.of(\n");
                content.append(ressourceAccesses.regardingFileSystemInteractions().itIsPermittedTo().stream()
                        .map(securityContent -> String.format("new FilePermission(\"%s\", \"%s\", \"%s\", Path.of(\"%s\"))",
                                securityContent.readAllFiles(),
                                securityContent.overwriteAllFiles(),
                                securityContent.executeAllFiles(),
                                securityContent.onThisPathAndAllPathsBelow()))
                        .collect(Collectors.joining(",\n")));
                content.append("\n);\n");
            }
            case NETWORK_CONNECTION -> {
                content.append("private static final List<NetworkConnection> allowedNetworkConnections = List.of(\n");
                content.append(ressourceAccesses.regardingNetworkConnections().itIsPermittedTo().stream()
                        .map(securityContent -> String.format("new NetworkPermission(\"%s\", \"%s\", \"%s\", \"%s\", %d)",
                                securityContent.openConnections(),
                                securityContent.sendData(),
                                securityContent.receiveData(),
                                securityContent.onTheHost(),
                                securityContent.onThePort()))
                        .collect(Collectors.joining(",\n")));
                content.append("\n);\n");
            }
            case COMMAND_EXECUTION -> {
                content.append("private static final List<CommandExecution> allowedCommandExecutions = List.of(\n");
                content.append(ressourceAccesses.regardingCommandExecutions().itIsPermittedTo().stream()
                        .map(securityContent -> String.format("new CommandPermission(\"%s\", List.of(%s))",
                                securityContent.executeTheCommand(),
                                securityContent.withTheseArguments().stream()
                                        .map(arg -> "\"" + arg + "\"")
                                        .collect(Collectors.joining(", "))))
                        .collect(Collectors.joining(",\n")));
                content.append("\n);\n");
            }
            case THREAD_CREATION -> {
                content.append("private static final List<ThreadCreation> allowedThreadCreations = List.of(\n");
                content.append(ressourceAccesses.regardingThreadCreations().itIsPermittedTo().stream()
                        .map(securityContent -> String.format("new ThreadCreation(%d, \"%s\")",
                                securityContent.createTheFollowingNumberOfThreads(),
                                securityContent.ofThisClass()))
                        .collect(Collectors.joining(",\n")));
                content.append("\n);\n");
            }
            default ->
                    throw new UnsupportedOperationException("Unsupported configuration: " + javaAspectJConfigurationSupported);
        }
        return content.toString();
    }

    /**
     * Runs the aspect configuration in the Java programming language using AspectJ.
     * <p>
     * This method applies the aspect configuration settings at runtime by updating the relevant settings
     * in the JavaAspectJConfigurationSettings class. If no resource accesses are defined, the configuration is deactivated.
     * </p>
     */
    @Override
    public void runAspectConfiguration() {
        if (ressourceAccesses == null) {
            deactivateAspectJConfig();
            return;
        }
        // Apply the aspect configuration based on the specified configuration type.
        switch (javaAspectJConfigurationSupported) {
            case FILESYSTEM_INTERACTION ->
                    JavaAspectJConfigurationSettings.allowedFileSystemInteractions = ressourceAccesses.regardingFileSystemInteractions().itIsPermittedTo();
            case NETWORK_CONNECTION ->
                    JavaAspectJConfigurationSettings.allowedNetworkConnections = ressourceAccesses.regardingNetworkConnections().itIsPermittedTo();
            case COMMAND_EXECUTION ->
                    JavaAspectJConfigurationSettings.allowedCommandExecutions = ressourceAccesses.regardingCommandExecutions().itIsPermittedTo();
            case THREAD_CREATION ->
                    JavaAspectJConfigurationSettings.allowedThreadCreations = ressourceAccesses.regardingThreadCreations().itIsPermittedTo();
            default ->
                    throw new UnsupportedOperationException("Unsupported configuration: " + javaAspectJConfigurationSupported);
        }
    }

    /**
     * Deactivates the AspectJ configuration by setting all settings in JavaAspectJConfigurationSettings to null.
     * <p>
     * This method is used to reset or deactivate the AspectJ configuration when resource accesses are not defined.
     * </p>
     */
    public static void deactivateAspectJConfig() {
        JavaAspectJConfigurationSettings.allowedFileSystemInteractions = null;
        JavaAspectJConfigurationSettings.allowedNetworkConnections = null;
        JavaAspectJConfigurationSettings.allowedCommandExecutions = null;
        JavaAspectJConfigurationSettings.allowedThreadCreations = null;
        JavaAspectJConfigurationSettings.allowedPackageImports = null;
    }
}