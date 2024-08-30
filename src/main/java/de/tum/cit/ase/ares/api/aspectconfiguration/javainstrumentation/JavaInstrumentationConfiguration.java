package de.tum.cit.ase.ares.api.aspectconfiguration.javainstrumentation;

import de.tum.cit.ase.ares.api.aspectconfiguration.AspectConfiguration;
import de.tum.cit.ase.ares.api.policy.SecurityPolicy.RessourceAccesses;
import de.tum.cit.ase.ares.api.policy.SecurityPolicy.FilePermission;
import de.tum.cit.ase.ares.api.policy.SecurityPolicy.NetworkPermission;
import de.tum.cit.ase.ares.api.policy.SecurityPolicy.CommandPermission;
import de.tum.cit.ase.ares.api.policy.SecurityPolicy.ThreadPermission;

import java.lang.reflect.Field;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * Aspect configuration for Java instrumentation, implementing the AspectConfiguration interface.
 * <p>
 * This class represents the concrete product in the Abstract Factory Design Pattern, specifically
 * for Java instrumentation configurations. It generates the content for aspect configuration files
 * and applies these configurations at runtime based on the provided security policy.
 * </p>
 *
 * @version 2.0.0
 * @see <a href="https://refactoring.guru/design-patterns/abstract-factory">Abstract Factory Design Pattern</a>
 * @since 2.0.0
 */
public class JavaInstrumentationConfiguration implements AspectConfiguration {

    private final JavaInstrumentationConfigurationSupported javaInstrumentationConfigurationSupported;
    private final RessourceAccesses ressourceAccesses;

    /**
     * Constructs a new JavaInstrumentationConfiguration with the specified configuration and resource accesses.
     *
     * @param javaInstrumentationConfigurationSupported the specific type of aspect configuration to support.
     * @param ressourceAccesses                         the resource accesses defined by the security policy.
     */
    public JavaInstrumentationConfiguration(JavaInstrumentationConfigurationSupported javaInstrumentationConfigurationSupported, RessourceAccesses ressourceAccesses) {
        this.javaInstrumentationConfigurationSupported = javaInstrumentationConfigurationSupported;
        this.ressourceAccesses = ressourceAccesses;
    }

    //<editor-fold desc="Tools">

    /**
     * Generates a formatted string representing an advice setting value.
     *
     * @param adviceSetting the name of the advice setting.
     * @param value         the values to be set in the advice configuration.
     * @return a formatted string representing the advice setting.
     */
    private String generateAdviceSettingValue(String adviceSetting, List<String> value) {
        return String.format(
                "private static %s = {\n%s\n};\n",
                adviceSetting,
                String.join(",\n", value)
        );
    }

    /**
     * Sets the value of a specific Java advice setting using reflection.
     *
     * @param adviceSetting the name of the advice setting.
     * @param value         the value to set for the advice setting.
     */
    public void setJavaAdviceSettingValue(String adviceSetting, Object value) {
        try {
            Class<?> adviceSettingsClass = Class.forName("de.tum.cit.ase.advice.AdviceSettings", true, null);
            Field field = adviceSettingsClass.getDeclaredField(adviceSetting);
            field.setAccessible(true);
            field.set(null, value);
            field.setAccessible(false);
        } catch (NoSuchFieldException | ClassNotFoundException | IllegalAccessException e) {
            throw new SecurityException("Failed to set Java advice setting value.", e);
        }
    }
    //</editor-fold>

    //<editor-fold desc="File System Interactions">

    /**
     * Generates the name of the advice setting for file system path permissions.
     *
     * @param dataType       the data type of the advice setting (e.g., String[]).
     * @param filePermission the type of file permission (read, overwrite, execute).
     * @return the name of the advice setting.
     */
    private String getFilesystemPathAdviceSettingName(String dataType, String filePermission) {
        return dataType +
                (dataType == null ? "" : " ") +
                switch (filePermission) {
                    case "read" -> "pathsAllowedToBeRead";
                    case "overwrite" -> "pathsAllowedToBeOverwritten";
                    case "execute" -> "pathsAllowedToBeExecuted";
                    default -> throw new IllegalArgumentException("Invalid file permission: " + filePermission);
                };
    }

    /**
     * Retrieves the file paths permitted for a specific file permission.
     *
     * @param filePermission the type of file permission (read, overwrite, execute).
     * @return a list of permitted file paths.
     */
    private List<String> getPermittedFilePaths(String filePermission) {
        Predicate<FilePermission> filter = switch (filePermission) {
            case "read" -> FilePermission::readAllFiles;
            case "overwrite" -> FilePermission::overwriteAllFiles;
            case "execute" -> FilePermission::executeAllFiles;
            default -> throw new IllegalArgumentException("Invalid file permission: " + filePermission);
        };
        return ressourceAccesses
                .regardingFileSystemInteractions()
                .itIsPermittedTo()
                .stream()
                .filter(filter)
                .map(FilePermission::onThisPathAndAllPathsBelow)
                .toList();
    }

    /**
     * Generates the advice setting content for file system interactions.
     *
     * @param filePermission the type of file permission (read, overwrite, execute).
     * @return the advice setting content as a string.
     */
    private String getFilePermissionPathsAdviceSetting(String filePermission) {
        return generateAdviceSettingValue(
                getFilesystemPathAdviceSettingName("String[] ", filePermission),
                getPermittedFilePaths(filePermission)
        );
    }
    //</editor-fold>

    //<editor-fold desc="Network Connections">

    /**
     * Generates the name of the advice setting for network host permissions.
     *
     * @param dataType          the data type of the advice setting (e.g., String[]).
     * @param networkPermission the type of network permission (connect, send, receive).
     * @return the name of the advice setting.
     */
    private String getNetworkHostAdviceSettingName(String dataType, String networkPermission) {
        return dataType +
                (dataType == null ? "" : " ") +
                switch (networkPermission) {
                    case "connect" -> "hostsAllowedToBeConnectedTo";
                    case "send" -> "hostsAllowedToBeSentTo";
                    case "receive" -> "hostsAllowedToBeReceivedFrom";
                    default -> throw new IllegalArgumentException("Invalid network permission: " + networkPermission);
                };
    }

    /**
     * Generates the name of the advice setting for network port permissions.
     *
     * @param dataType          the data type of the advice setting (e.g., int[]).
     * @param networkPermission the type of network permission (connect, send, receive).
     * @return the name of the advice setting.
     */
    private String getNetworkPortAdviceSettingName(String dataType, String networkPermission) {
        return dataType +
                (dataType == null ? "" : " ") +
                switch (networkPermission) {
                    case "connect" -> "portsAllowedToBeConnectedTo";
                    case "send" -> "portsAllowedToBeSentTo";
                    case "receive" -> "portsAllowedToBeReceivedFrom";
                    default -> throw new IllegalArgumentException("Invalid network permission: " + networkPermission);
                };
    }

    /**
     * Retrieves the network hosts permitted for a specific network permission.
     *
     * @param networkPermission the type of network permission (connect, send, receive).
     * @return a list of permitted network hosts.
     */
    private List<String> getPermittedNetworkHosts(String networkPermission) {
        Predicate<NetworkPermission> filter = switch (networkPermission) {
            case "connect" -> NetworkPermission::openConnections;
            case "send" -> NetworkPermission::sendData;
            case "receive" -> NetworkPermission::receiveData;
            default -> throw new IllegalArgumentException("Invalid network permission: " + networkPermission);
        };
        return ressourceAccesses
                .regardingNetworkConnections()
                .itIsPermittedTo()
                .stream()
                .filter(filter)
                .map(NetworkPermission::onTheHost)
                .toList();
    }

    /**
     * Retrieves the network ports permitted for a specific network permission.
     *
     * @param networkPermission the type of network permission (connect, send, receive).
     * @return a list of permitted network ports.
     */
    private List<Integer> getPermittedNetworkPorts(String networkPermission) {
        Predicate<NetworkPermission> filter = switch (networkPermission) {
            case "connect" -> NetworkPermission::openConnections;
            case "send" -> NetworkPermission::sendData;
            case "receive" -> NetworkPermission::receiveData;
            default -> throw new IllegalArgumentException("Invalid network permission: " + networkPermission);
        };
        return ressourceAccesses
                .regardingNetworkConnections()
                .itIsPermittedTo()
                .stream()
                .filter(filter)
                .map(NetworkPermission::onThePort)
                .toList();
    }

    /**
     * Generates the advice setting content for network connections.
     *
     * @param networkPermission the type of network permission (connect, send, receive).
     * @return the advice setting content as a string.
     */
    private String getNetworkConnectionAdviceSetting(String networkPermission) {
        return generateAdviceSettingValue(
                getNetworkHostAdviceSettingName("String[] ", networkPermission),
                getPermittedNetworkHosts(networkPermission)
        ) + generateAdviceSettingValue(
                getNetworkPortAdviceSettingName("int[] ", networkPermission),
                getPermittedNetworkPorts(networkPermission).stream().map(String::valueOf).toList()
        );
    }
    //</editor-fold>

    //<editor-fold desc="Command Execution">

    /**
     * Retrieves the list of commands that are permitted to be executed based on the security policy.
     *
     * @return a list of permitted commands.
     */
    private List<String> getPermittedCommands() {
        return ressourceAccesses
                .regardingCommandExecutions()
                .itIsPermittedTo()
                .stream()
                .map(CommandPermission::executeTheCommand)
                .toList();
    }

    /**
     * Retrieves the list of arguments that are permitted to be passed to commands based on the security policy.
     *
     * @return a list of lists, each representing the arguments for a permitted command.
     */
    private List<List<String>> getPermittedArguments() {
        return ressourceAccesses
                .regardingCommandExecutions()
                .itIsPermittedTo()
                .stream()
                .map(CommandPermission::withTheseArguments)
                .toList();
    }

    /**
     * Generates the advice setting content for command execution.
     *
     * @param ignored a placeholder parameter to match the method signature used in Stream operations.
     * @return the advice setting content as a string.
     */
    private String getCommandExecutionAdviceSetting(String ignored) {
        return generateAdviceSettingValue(
                "String[] commandsAllowedToBeExecuted",
                getPermittedCommands()
        ) + generateAdviceSettingValue(
                "String[][] argumentsAllowedToBePassed",
                getPermittedArguments()
                        .stream()
                        .map(arguments -> "new String[]{" + String.join(",", arguments) + "}")
                        .toList()
        );
    }
    //</editor-fold>

    //<editor-fold desc="Thread Creation">

    /**
     * Retrieves the number of threads that are permitted to be created based on the security policy.
     *
     * @return a list of integers representing the permitted number of threads.
     */
    private List<Integer> getPermittedNumberOfThreads() {
        return ressourceAccesses
                .regardingThreadCreations()
                .itIsPermittedTo()
                .stream()
                .map(ThreadPermission::createTheFollowingNumberOfThreads)
                .toList();
    }

    /**
     * Retrieves the classes of threads that are permitted to be created based on the security policy.
     *
     * @return a list of class names representing the permitted thread classes.
     */
    private List<String> getPermittedThreadClasses() {
        return ressourceAccesses
                .regardingThreadCreations()
                .itIsPermittedTo()
                .stream()
                .map(ThreadPermission::ofThisClass)
                .toList();
    }

    /**
     * Generates the advice setting content for thread creation.
     *
     * @param ignored a placeholder parameter to match the method signature used in Stream operations.
     * @return the advice setting content as a string.
     */
    private String getThreadCreationAdviceSetting(String ignored) {
        return generateAdviceSettingValue(
                "int[] threadNumberAllowedToBeCreated",
                getPermittedNumberOfThreads().stream().map(String::valueOf).toList()
        ) + generateAdviceSettingValue(
                "String[] threadClassAllowedToBeCreated",
                getPermittedThreadClasses()
        );
    }
    //</editor-fold>

    /**
     * Creates the content of the aspect configuration file in the Java programming language.
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
        switch (javaInstrumentationConfigurationSupported) {
            case FILESYSTEM_INTERACTION -> Stream
                    .of("read", "overwrite", "execute")
                    .map(this::getFilePermissionPathsAdviceSetting)
                    .forEach(content::append);
            case NETWORK_CONNECTION -> Stream
                    .of("connect", "send", "receive")
                    .map(this::getNetworkConnectionAdviceSetting)
                    .forEach(content::append);
            case COMMAND_EXECUTION -> Stream
                    .of("")
                    .map(this::getCommandExecutionAdviceSetting)
                    .forEach(content::append);
            case THREAD_CREATION -> Stream
                    .of("")
                    .map(this::getThreadCreationAdviceSetting)
                    .forEach(content::append);
        }
        return content.toString();
    }

    /**
     * Runs the aspect configuration in the Java programming language.
     * <p>
     * This method applies the aspect configuration settings at runtime by updating the relevant advice settings
     * in the application. It uses reflection to set the appropriate values based on the security policy.
     * </p>
     */
    @Override
    public void runAspectConfiguration() {
        switch (javaInstrumentationConfigurationSupported) {
            case FILESYSTEM_INTERACTION -> Stream
                    .of("read", "overwrite", "execute")
                    .forEach(filePermission ->
                            setJavaAdviceSettingValue(
                                    getFilesystemPathAdviceSettingName(null, filePermission),
                                    getPermittedFilePaths(filePermission)
                            )
                    );
            case NETWORK_CONNECTION -> Stream
                    .of("connect", "send", "receive")
                    .forEach(networkPermission -> {
                        setJavaAdviceSettingValue(
                                getNetworkHostAdviceSettingName(null, networkPermission),
                                getPermittedNetworkHosts(networkPermission)
                        );
                        setJavaAdviceSettingValue(
                                getNetworkPortAdviceSettingName(null, networkPermission),
                                getPermittedNetworkPorts(networkPermission)
                        );
                    });
            case COMMAND_EXECUTION -> Stream
                    .of("")
                    .forEach(commandPermission -> {
                        setJavaAdviceSettingValue(
                                "commandsAllowedToBeExecuted",
                                getPermittedCommands()
                        );
                        setJavaAdviceSettingValue(
                                "argumentsAllowedToBePassed",
                                getPermittedArguments()
                        );
                    });
            case THREAD_CREATION -> Stream.of("")
                    .forEach(threadPermission -> {
                        setJavaAdviceSettingValue(
                                "threadNumberAllowedToBeCreated",
                                getPermittedNumberOfThreads()
                        );
                        setJavaAdviceSettingValue(
                                "threadClassAllowedToBeCreated",
                                getPermittedThreadClasses()
                        );
                    });
        }
    }
}