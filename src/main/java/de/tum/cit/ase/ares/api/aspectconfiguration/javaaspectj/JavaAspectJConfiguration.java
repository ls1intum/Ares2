package de.tum.cit.ase.ares.api.aspectconfiguration.javaaspectj;

import de.tum.cit.ase.ares.api.aspectconfiguration.AspectConfiguration;
import de.tum.cit.ase.ares.api.policy.SecurityPolicy.ResourceAccesses;

import java.util.stream.Collectors;

/**
 * Aspect configuration for the Java programming language and concrete product of the abstract factory design pattern.
 *
 * @author Markus Paulsen
 * @version 2.0.0
 * @see <a href="https://refactoring.guru/design-patterns/abstract-factory">Abstract Factory Design Pattern</a>
 * @since 2.0.0
 */
public class JavaAspectJConfiguration implements AspectConfiguration {

    public static final String END_OF_LIST = "\n);\n";

    private final JavaAspectJConfigurationSupported javaAspectJConfigurationSupported;
    private final ResourceAccesses resourceAccesses;

    /**
     * Constructor for JavaAspectConfiguration.
     *
     * @param javaAspectJConfigurationSupported Selects the supported aspect configuration in the Java programming language
     * @param resourceAccesses                  Security policy for the aspect configuration
     */
    public JavaAspectJConfiguration(JavaAspectJConfigurationSupported javaAspectJConfigurationSupported, ResourceAccesses resourceAccesses) {
        this.javaAspectJConfigurationSupported = javaAspectJConfigurationSupported;
        this.resourceAccesses = resourceAccesses;
    }

    /**
     * Creates the content of the aspect configuration file in the Java programming language.
     *
     * @return Content of the aspect configuration file
     */
    @Override
    public String createAspectConfigurationFileContent() {
        StringBuilder content = new StringBuilder();
        switch (javaAspectJConfigurationSupported) {
            case FILESYSTEM_INTERACTION -> {
                content.append("private static final List<FilePermission> allowedFileSystemInteractions = List.of(\n");
                content.append(resourceAccesses.regardingFileSystemInteractions().itIsPermittedTo().stream()
                        .map(securityContent -> String.format("new FilePermission(%s, %s, %s, Path.of(\"%s\"))",
                                securityContent.readAllFiles(),
                                securityContent.overwriteAllFiles(),
                                securityContent.executeAllFiles(),
                                securityContent.onThisPathAndAllPathsBelow()))
                        .collect(Collectors.joining(",\n")));
                content.append(END_OF_LIST);
            }
            case NETWORK_CONNECTION -> {
                content.append("private static final List<NetworkPermission> allowedNetworkConnections = List.of(\n");
                content.append(resourceAccesses.regardingNetworkConnections().itIsPermittedTo().stream()
                        .map(securityContent -> String.format("new NetworkPermission(%s, %s, %s, \"%s\", %d)",
                                securityContent.openConnections(),
                                securityContent.sendData(),
                                securityContent.receiveData(),
                                securityContent.onTheHost(),
                                securityContent.onThePort()))
                        .collect(Collectors.joining(",\n")));
                content.append(END_OF_LIST);
            }
            case COMMAND_EXECUTION -> {
                content.append("private static final List<CommandPermission> allowedCommandExecutions = List.of(\n");
                content.append(resourceAccesses.regardingCommandExecutions().itIsPermittedTo().stream()
                        .map(securityContent -> String.format("new CommandPermission(\"%s\", List.of(%s))",
                                securityContent.executeTheCommand(),
                                securityContent.withTheseArguments().stream()
                                        .map(arg -> "\"" + arg + "\"")
                                        .collect(Collectors.joining(", "))))
                        .collect(Collectors.joining(",\n")));
                content.append(END_OF_LIST);
            }
            case THREAD_CREATION -> {
                content.append("private static final List<ThreadPermission> allowedThreadCreations = List.of(\n");
                content.append(resourceAccesses.regardingThreadCreations().itIsPermittedTo().stream()
                        .map(securityContent -> String.format("new ThreadPermission(%d, \"%s\")",
                                securityContent.createTheFollowingNumberOfThreads(),
                                securityContent.ofThisClass()))
                        .collect(Collectors.joining(",\n")));
                content.append(END_OF_LIST);
            }
            default ->
                    throw new UnsupportedOperationException("Unsupported configuration: " + javaAspectJConfigurationSupported);
        }
        return content.toString();
    }

    /**
     * Runs the aspect configuration in Java programming language.
     */
    @Override
    public void runAspectConfiguration() {
        switch (javaAspectJConfigurationSupported) {
            case FILESYSTEM_INTERACTION ->
                    JavaAspectJConfigurationSettings.setAllowedFileSystemInteractions(resourceAccesses == null ? null : resourceAccesses.regardingFileSystemInteractions().itIsPermittedTo());
            case NETWORK_CONNECTION ->
                    JavaAspectJConfigurationSettings.setAllowedNetworkConnections(resourceAccesses == null ? null : resourceAccesses.regardingNetworkConnections().itIsPermittedTo());
            case COMMAND_EXECUTION ->
                    JavaAspectJConfigurationSettings.setAllowedCommandExecutions(resourceAccesses == null ? null : resourceAccesses.regardingCommandExecutions().itIsPermittedTo());
            case THREAD_CREATION ->
                    JavaAspectJConfigurationSettings.setAllowedThreadCreations(resourceAccesses == null ? null : resourceAccesses.regardingThreadCreations().itIsPermittedTo());
            default ->
                    throw new UnsupportedOperationException("Unsupported configuration: " + javaAspectJConfigurationSupported);
        }
    }
}
