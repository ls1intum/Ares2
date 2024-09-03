package de.tum.cit.ase.ares.api.aspectconfiguration.java.instrumentation;

import de.tum.cit.ase.ares.api.aspectconfiguration.AspectConfiguration;
import de.tum.cit.ase.ares.api.policy.SecurityPolicy.ResourceAccesses;
import de.tum.cit.ase.ares.api.policy.SecurityPolicy.FilePermission;
import de.tum.cit.ase.ares.api.policy.SecurityPolicy.NetworkPermission;
import de.tum.cit.ase.ares.api.policy.SecurityPolicy.CommandPermission;
import de.tum.cit.ase.ares.api.policy.SecurityPolicy.ThreadPermission;

import java.lang.reflect.Field;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * Configures Java instrumentation based on a security policy.
 * Implements the AspectConfiguration interface for managing aspect configurations.
 */
public class JavaInstrumentationConfiguration implements AspectConfiguration {

    //<editor-fold desc="Attributes">
    private final JavaInstrumentationConfigurationSupported javaInstrumentationConfigurationSupported;
    private final ResourceAccesses resourceAccesses;
    //</editor-fold>

    //<editor-fold desc="Constructor">

    /**
     * Initializes the configuration with the given support type and resource accesses.
     */
    public JavaInstrumentationConfiguration(JavaInstrumentationConfigurationSupported javaInstrumentationConfigurationSupported, ResourceAccesses resourceAccesses) {
        this.javaInstrumentationConfigurationSupported = javaInstrumentationConfigurationSupported;
        this.resourceAccesses = resourceAccesses;
    }
    //</editor-fold>

    //<editor-fold desc="Tools">

    /**
     * Formats an advice setting with its corresponding values.
     */
    private static String generateAdviceSettingValue(String dataType, String adviceSetting, List<String> value) {
        return String.format("private static %s %s = new %s {\n%s\n};\n", dataType, adviceSetting, dataType, String.join(",\n", value));
    }

    /**
     * Uses reflection to set the value of a Java advice setting.
     */
    public void setJavaAdviceSettingValue(String adviceSetting, Object value) {
        try {
            ClassLoader customClassLoader = Thread.currentThread().getContextClassLoader();
            Class<?> adviceSettingsClass = Class.forName("de.tum.cit.ase.ares.api.aspectconfiguration.java.instrumentation.JavaInstrumentationConfigurationSettings", true, customClassLoader);
            Field field = adviceSettingsClass.getDeclaredField(adviceSetting);
            field.setAccessible(true);
            field.set(null, value);
            field.setAccessible(false);
        } catch (NoSuchFieldException | ClassNotFoundException | IllegalAccessException e) {
            throw new SecurityException("Failed to set Java adviceandpointcut setting value.", e);
        }
    }
    //</editor-fold>

    //<editor-fold desc="File System Interactions">

    /**
     * Returns the appropriate setting name for file permission types (read, overwrite, execute).
     */
    private String getFilesystemPathAdviceSettingName(String dataType, String filePermission) {
        return switch (filePermission) {
            case "read" -> "pathsAllowedToBeRead";
            case "overwrite" -> "pathsAllowedToBeOverwritten";
            case "execute" -> "pathsAllowedToBeExecuted";
            default -> throw new IllegalArgumentException("Invalid file permission: " + filePermission);
        };
    }

    /**
     * Retrieves the list of file paths that are permitted for the given permission type.
     */
    private List<String> getPermittedFilePaths(String filePermission) {
        Predicate<FilePermission> filter = switch (filePermission) {
            case "read" -> FilePermission::readAllFiles;
            case "overwrite" -> FilePermission::overwriteAllFiles;
            case "execute" -> FilePermission::executeAllFiles;
            default -> throw new IllegalArgumentException("Invalid file permission: " + filePermission);
        };
        return resourceAccesses.regardingFileSystemInteractions()
                .stream()
                .filter(filter)
                .map(FilePermission::onThisPathAndAllPathsBelow)
                .toList();
    }

    private static List<String> extractPaths(List<JavaInstrumentationConfiguration> configs, Predicate<FilePermission> predicate) {
        return configs.stream()
                .filter(config -> config.javaInstrumentationConfigurationSupported == JavaInstrumentationConfigurationSupported.FILESYSTEM_INTERACTION)
                .flatMap(config -> config.resourceAccesses.regardingFileSystemInteractions().stream())
                .filter(predicate)
                .map(FilePermission::onThisPathAndAllPathsBelow)
                .toList();
    }
    //</editor-fold>

    //<editor-fold desc="Network Connections">

    /**
     * Returns the appropriate setting name for network host permissions (connect, send, receive).
     */
    private String getNetworkHostAdviceSettingName(String dataType, String networkPermission) {
        return dataType + (dataType == null ? "" : " ") +
                switch (networkPermission) {
                    case "connect" -> "hostsAllowedToBeConnectedTo";
                    case "send" -> "hostsAllowedToBeSentTo";
                    case "receive" -> "hostsAllowedToBeReceivedFrom";
                    default -> throw new IllegalArgumentException("Invalid network permission: " + networkPermission);
                };
    }

    /**
     * Returns the appropriate setting name for network port permissions (connect, send, receive).
     */
    private String getNetworkPortAdviceSettingName(String dataType, String networkPermission) {
        return dataType + (dataType == null ? "" : " ") +
                switch (networkPermission) {
                    case "connect" -> "portsAllowedToBeConnectedTo";
                    case "send" -> "portsAllowedToBeSentTo";
                    case "receive" -> "portsAllowedToBeReceivedFrom";
                    default -> throw new IllegalArgumentException("Invalid network permission: " + networkPermission);
                };
    }

    /**
     * Retrieves the list of network hosts that are permitted for the given permission type.
     */
    private List<String> getPermittedNetworkHosts(String networkPermission) {
        Predicate<NetworkPermission> filter = switch (networkPermission) {
            case "connect" -> NetworkPermission::openConnections;
            case "send" -> NetworkPermission::sendData;
            case "receive" -> NetworkPermission::receiveData;
            default -> throw new IllegalArgumentException("Invalid network permission: " + networkPermission);
        };
        return resourceAccesses.regardingNetworkConnections()
                .stream()
                .filter(filter)
                .map(NetworkPermission::onTheHost)
                .toList();
    }

    /**
     * Retrieves the list of network ports that are permitted for the given permission type.
     */
    private List<Integer> getPermittedNetworkPorts(String networkPermission) {
        Predicate<NetworkPermission> filter = switch (networkPermission) {
            case "connect" -> NetworkPermission::openConnections;
            case "send" -> NetworkPermission::sendData;
            case "receive" -> NetworkPermission::receiveData;
            default -> throw new IllegalArgumentException("Invalid network permission: " + networkPermission);
        };
        return resourceAccesses.regardingNetworkConnections()
                .stream()
                .filter(filter)
                .map(NetworkPermission::onThePort)
                .toList();
    }

    private static List<String> extractHosts(List<JavaInstrumentationConfiguration> configs, Predicate<NetworkPermission> predicate) {
        return configs.stream()
                .filter(config -> config.javaInstrumentationConfigurationSupported == JavaInstrumentationConfigurationSupported.NETWORK_CONNECTION)
                .flatMap(config -> config.resourceAccesses.regardingNetworkConnections().stream())
                .filter(predicate)
                .map(NetworkPermission::onTheHost)
                .toList();
    }

    private static List<String> extractPorts(List<JavaInstrumentationConfiguration> configs, Predicate<NetworkPermission> predicate) {
        return configs.stream()
                .filter(config -> config.javaInstrumentationConfigurationSupported == JavaInstrumentationConfigurationSupported.NETWORK_CONNECTION)
                .flatMap(config -> config.resourceAccesses.regardingNetworkConnections().stream())
                .filter(predicate)
                .map(NetworkPermission::onThePort)
                .map(String::valueOf)
                .toList();
    }
    //</editor-fold>

    //<editor-fold desc="Command Execution">

    /**
     * Retrieves the list of commands that are permitted to be executed.
     */
    private List<String> getPermittedCommands() {
        return resourceAccesses.regardingCommandExecutions()
                .stream()
                .map(CommandPermission::executeTheCommand)
                .toList();
    }

    /**
     * Retrieves the list of arguments permitted for execution with commands.
     */
    private List<List<String>> getPermittedArguments() {
        return resourceAccesses.regardingCommandExecutions()
                .stream()
                .map(CommandPermission::withTheseArguments)
                .toList();
    }

    private static List<String> extractCommands(List<JavaInstrumentationConfiguration> configs) {
        return configs.stream()
                .filter(config -> config.javaInstrumentationConfigurationSupported == JavaInstrumentationConfigurationSupported.COMMAND_EXECUTION)
                .flatMap(config -> config.resourceAccesses.regardingCommandExecutions().stream())
                .map(CommandPermission::executeTheCommand)
                .toList();
    }

    private static List<String> extractArguments(List<JavaInstrumentationConfiguration> configs) {
        return configs.stream()
                .filter(config -> config.javaInstrumentationConfigurationSupported == JavaInstrumentationConfigurationSupported.COMMAND_EXECUTION)
                .flatMap(config -> config.resourceAccesses.regardingCommandExecutions().stream())
                .map(CommandPermission::withTheseArguments)
                .map(arguments -> "new String[] {" + String.join(",", arguments) + "}")
                .toList();
    }
    //</editor-fold>

    //<editor-fold desc="Thread Creation">

    /**
     * Retrieves the list of permitted thread counts based on the security policy.
     */
    private List<Integer> getPermittedNumberOfThreads() {
        return resourceAccesses.regardingThreadCreations()
                .stream()
                .map(ThreadPermission::createTheFollowingNumberOfThreads)
                .toList();
    }

    /**
     * Retrieves the list of permitted thread classes based on the security policy.
     */
    private List<String> getPermittedThreadClasses() {
        return resourceAccesses.regardingThreadCreations()
                .stream()
                .map(ThreadPermission::ofThisClass)
                .toList();
    }

    private static List<String> extractThreadNumbers(List<JavaInstrumentationConfiguration> configs) {
        return configs.stream()
                .filter(config -> config.javaInstrumentationConfigurationSupported == JavaInstrumentationConfigurationSupported.THREAD_CREATION)
                .flatMap(config -> config.resourceAccesses.regardingThreadCreations().stream())
                .map(ThreadPermission::createTheFollowingNumberOfThreads)
                .map(String::valueOf)
                .toList();
    }

    private static List<String> extractThreadClasses(List<JavaInstrumentationConfiguration> configs) {
        return configs.stream()
                .filter(config -> config.javaInstrumentationConfigurationSupported == JavaInstrumentationConfigurationSupported.THREAD_CREATION)
                .flatMap(config -> config.resourceAccesses.regardingThreadCreations().stream())
                .map(ThreadPermission::ofThisClass)
                .toList();
    }
    //</editor-fold>

    /**
     * Generates the aspect configuration file content based on the supported configuration type.
     */
    @Override
    public String createAspectConfigurationFileContent() {
        return "";
    }

    /**
     * Creates the full aspect configuration file content by aggregating configurations.
     */
    public static String createAspectConfigurationFileFullContent(List<JavaInstrumentationConfiguration> javaInstrumentationConfigurations) {
        StringBuilder fileContentBuilder = new StringBuilder();
        fileContentBuilder.append(generateAdviceSettingValue("String[]", "pathsAllowedToBeRead", extractPaths(javaInstrumentationConfigurations, FilePermission::readAllFiles)));
        fileContentBuilder.append(generateAdviceSettingValue("String[]", " pathsAllowedToBeOverwritten", extractPaths(javaInstrumentationConfigurations, FilePermission::overwriteAllFiles)));
        fileContentBuilder.append(generateAdviceSettingValue("String[]", " pathsAllowedToBeExecuted", extractPaths(javaInstrumentationConfigurations, FilePermission::executeAllFiles)));
        fileContentBuilder.append(generateAdviceSettingValue("String[]", " hostsAllowedToBeConnectedTo", extractHosts(javaInstrumentationConfigurations, NetworkPermission::openConnections)));
        fileContentBuilder.append(generateAdviceSettingValue("int[]", " portsAllowedToBeConnectedTo", extractPorts(javaInstrumentationConfigurations, NetworkPermission::openConnections)));
        fileContentBuilder.append(generateAdviceSettingValue("String[]", " hostsAllowedToBeSentTo", extractHosts(javaInstrumentationConfigurations, NetworkPermission::sendData)));
        fileContentBuilder.append(generateAdviceSettingValue("int[]", " portsAllowedToBeSentTo", extractPorts(javaInstrumentationConfigurations, NetworkPermission::sendData)));
        fileContentBuilder.append(generateAdviceSettingValue("String[]", " hostsAllowedToBeReceivedFrom", extractHosts(javaInstrumentationConfigurations, NetworkPermission::receiveData)));
        fileContentBuilder.append(generateAdviceSettingValue("int[]", " portsAllowedToBeReceivedFrom", extractPorts(javaInstrumentationConfigurations, NetworkPermission::receiveData)));
        fileContentBuilder.append(generateAdviceSettingValue("String[]", " commandsAllowedToBeExecuted", extractCommands(javaInstrumentationConfigurations)));
        fileContentBuilder.append(generateAdviceSettingValue("String[][]", " argumentsAllowedToBePassed", extractArguments(javaInstrumentationConfigurations)));
        fileContentBuilder.append(generateAdviceSettingValue("int[]", " threadNumberAllowedToBeCreated", extractThreadNumbers(javaInstrumentationConfigurations)));
        fileContentBuilder.append(generateAdviceSettingValue("String[]", " threadClassAllowedToBeCreated", extractThreadClasses(javaInstrumentationConfigurations)));
        return fileContentBuilder.toString();
    }

    /**
     * Runs the aspect configuration by setting the necessary Java advice settings.
     */
    @Override
    public void runAspectConfiguration() {
        switch (javaInstrumentationConfigurationSupported) {
            case FILESYSTEM_INTERACTION -> Stream
                    .of("read", "overwrite", "execute")
                    .forEach(filePermission ->
                            setJavaAdviceSettingValue(
                                    getFilesystemPathAdviceSettingName("String[]", filePermission),
                                    getPermittedFilePaths(filePermission).toArray(new String[0])
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