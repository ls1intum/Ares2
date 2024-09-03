package de.tum.cit.ase.ares.api.aop.java.instrumentation;

//<editor-fold desc="Imports">

import de.tum.cit.ase.ares.api.aop.AOPSecurityTestCase;
import de.tum.cit.ase.ares.api.policy.SecurityPolicy.ResourceAccesses;
import de.tum.cit.ase.ares.api.policy.SecurityPolicy.FilePermission;
import de.tum.cit.ase.ares.api.policy.SecurityPolicy.NetworkPermission;
import de.tum.cit.ase.ares.api.policy.SecurityPolicy.CommandPermission;
import de.tum.cit.ase.ares.api.policy.SecurityPolicy.ThreadPermission;

import java.lang.reflect.Field;
import java.security.Permission;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
//</editor-fold>

/**
 * Configures Java instrumentation based on a security policy.
 * Implements the AOPSecurityTestCase interface for managing aspect configurations.
 */
public class JavaInstrumentationSecurityTestCase implements AOPSecurityTestCase {

    //<editor-fold desc="Attributes">
    private final JavaInstrumentationConfigurationSupported javaInstrumentationConfigurationSupported;
    private final ResourceAccesses resourceAccesses;
    //</editor-fold>

    //<editor-fold desc="Constructor">

    /**
     * Initializes the configuration with the given support type and resource accesses.
     */
    public JavaInstrumentationSecurityTestCase(JavaInstrumentationConfigurationSupported javaInstrumentationConfigurationSupported, ResourceAccesses resourceAccesses) {
        this.javaInstrumentationConfigurationSupported = javaInstrumentationConfigurationSupported;
        this.resourceAccesses = resourceAccesses;
    }
    //</editor-fold>

    //<editor-fold desc="Tool methods">

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
            Class<?> adviceSettingsClass = Class.forName("de.tum.cit.ase.ares.api.aop.java.instrumentation.JavaInstrumentationSecurityTestCaseSettings", true, customClassLoader);
            Field field = adviceSettingsClass.getDeclaredField(adviceSetting);
            field.setAccessible(true);
            field.set(null, value);
            field.setAccessible(false);
        } catch (NoSuchFieldException | ClassNotFoundException | IllegalAccessException e) {
            throw new SecurityException("Failed to set Java adviceandpointcut setting value.", e);
        }
    }
    //</editor-fold>

    //<editor-fold desc="File System Interactions related methods">

    private static List<String> extractPaths(List<JavaInstrumentationSecurityTestCase> configs, Predicate<FilePermission> predicate) {
        return configs.stream()
                .filter(config -> config.javaInstrumentationConfigurationSupported == JavaInstrumentationConfigurationSupported.FILESYSTEM_INTERACTION)
                .map(config -> config.resourceAccesses.regardingFileSystemInteractions())
                .flatMap(List::stream)
                .filter(predicate)
                .map(FilePermission::onThisPathAndAllPathsBelow)
                .toList();
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

    //</editor-fold>

    //<editor-fold desc="Network Connections related methods">

    private static List<String> extractHosts(List<JavaInstrumentationSecurityTestCase> configs, Predicate<NetworkPermission> predicate) {
        return configs.stream()
                .filter(config -> config.javaInstrumentationConfigurationSupported == JavaInstrumentationConfigurationSupported.NETWORK_CONNECTION)
                .map(config -> config.resourceAccesses.regardingNetworkConnections())
                .flatMap(List::stream)
                .filter(predicate)
                .map(NetworkPermission::onTheHost)
                .toList();
    }

    private static List<String> extractPorts(List<JavaInstrumentationSecurityTestCase> configs, Predicate<NetworkPermission> predicate) {
        return configs.stream()
                .filter(config -> config.javaInstrumentationConfigurationSupported == JavaInstrumentationConfigurationSupported.NETWORK_CONNECTION)
                .map(config -> config.resourceAccesses.regardingNetworkConnections())
                .flatMap(List::stream)
                .filter(predicate)
                .map(NetworkPermission::onThePort)
                .map(String::valueOf)
                .toList();
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
    //</editor-fold>

    //<editor-fold desc="Command Execution related methods">

    private static List<String> extractCommands(List<JavaInstrumentationSecurityTestCase> configs) {
        return configs.stream()
                .filter(config -> config.javaInstrumentationConfigurationSupported == JavaInstrumentationConfigurationSupported.COMMAND_EXECUTION)
                .map(config -> config.resourceAccesses.regardingCommandExecutions())
                .flatMap(List::stream)
                .map(CommandPermission::executeTheCommand)
                .toList();
    }

    private static List<String> extractArguments(List<JavaInstrumentationSecurityTestCase> configs) {
        return configs.stream()
                .filter(config -> config.javaInstrumentationConfigurationSupported == JavaInstrumentationConfigurationSupported.COMMAND_EXECUTION)
                .map(config -> config.resourceAccesses.regardingCommandExecutions())
                .flatMap(List::stream)
                .map(CommandPermission::withTheseArguments)
                .map(arguments -> "new String[] {" + String.join(",", arguments) + "}")
                .toList();
    }

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
    //</editor-fold>

    //<editor-fold desc="Thread Creation related methods">

    /**
     * Retrieves the list of permitted thread counts based on the security policy.
     */
    private static List<String> extractThreadNumbers(List<JavaInstrumentationSecurityTestCase> configs) {
        return configs.stream()
                .filter(config -> config.javaInstrumentationConfigurationSupported == JavaInstrumentationConfigurationSupported.THREAD_CREATION)
                .map(config -> config.resourceAccesses.regardingThreadCreations())
                .flatMap(List::stream)
                .map(ThreadPermission::createTheFollowingNumberOfThreads)
                .map(String::valueOf)
                .toList();
    }

    private static List<String> extractThreadClasses(List<JavaInstrumentationSecurityTestCase> configs) {
        return configs.stream()
                .filter(config -> config.javaInstrumentationConfigurationSupported == JavaInstrumentationConfigurationSupported.THREAD_CREATION)
                .map(config -> config.resourceAccesses.regardingThreadCreations())
                .flatMap(List::stream)
                .map(ThreadPermission::ofThisClass)
                .toList();
    }

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
    //</editor-fold>

    //<editor-fold desc="Write security test case methods">

    /**
     * Generates the aspect configuration file content based on the supported configuration type.
     */
    @Override
    public String writeAOPSecurityTestCase() {
        return "";
    }
    //</editor-fold>

    //<editor-fold desc="Write security test case file methods">

    /**
     * Creates the full aspect configuration file content by aggregating configurations.
     */
    public static String writeAOPSecurityTestCaseFile(List<JavaInstrumentationSecurityTestCase> javaInstrumentationSecurityTestCases) {
        StringBuilder fileContentBuilder = new StringBuilder();
        fileContentBuilder.append(generateAdviceSettingValue("String[]", "pathsAllowedToBeRead", extractPaths(javaInstrumentationSecurityTestCases, FilePermission::readAllFiles)));
        fileContentBuilder.append(generateAdviceSettingValue("String[]", " pathsAllowedToBeOverwritten", extractPaths(javaInstrumentationSecurityTestCases, FilePermission::overwriteAllFiles)));
        fileContentBuilder.append(generateAdviceSettingValue("String[]", " pathsAllowedToBeExecuted", extractPaths(javaInstrumentationSecurityTestCases, FilePermission::executeAllFiles)));
        fileContentBuilder.append(generateAdviceSettingValue("String[]", " hostsAllowedToBeConnectedTo", extractHosts(javaInstrumentationSecurityTestCases, NetworkPermission::openConnections)));
        fileContentBuilder.append(generateAdviceSettingValue("int[]", " portsAllowedToBeConnectedTo", extractPorts(javaInstrumentationSecurityTestCases, NetworkPermission::openConnections)));
        fileContentBuilder.append(generateAdviceSettingValue("String[]", " hostsAllowedToBeSentTo", extractHosts(javaInstrumentationSecurityTestCases, NetworkPermission::sendData)));
        fileContentBuilder.append(generateAdviceSettingValue("int[]", " portsAllowedToBeSentTo", extractPorts(javaInstrumentationSecurityTestCases, NetworkPermission::sendData)));
        fileContentBuilder.append(generateAdviceSettingValue("String[]", " hostsAllowedToBeReceivedFrom", extractHosts(javaInstrumentationSecurityTestCases, NetworkPermission::receiveData)));
        fileContentBuilder.append(generateAdviceSettingValue("int[]", " portsAllowedToBeReceivedFrom", extractPorts(javaInstrumentationSecurityTestCases, NetworkPermission::receiveData)));
        fileContentBuilder.append(generateAdviceSettingValue("String[]", " commandsAllowedToBeExecuted", extractCommands(javaInstrumentationSecurityTestCases)));
        fileContentBuilder.append(generateAdviceSettingValue("String[][]", " argumentsAllowedToBePassed", extractArguments(javaInstrumentationSecurityTestCases)));
        fileContentBuilder.append(generateAdviceSettingValue("int[]", " threadNumberAllowedToBeCreated", extractThreadNumbers(javaInstrumentationSecurityTestCases)));
        fileContentBuilder.append(generateAdviceSettingValue("String[]", " threadClassAllowedToBeCreated", extractThreadClasses(javaInstrumentationSecurityTestCases)));
        return fileContentBuilder.toString();
    }
    //</editor-fold>

    //<editor-fold desc="Execute security test case methods">

    /**
     * Runs the aspect configuration by setting the necessary Java advice settings.
     */
    @Override
    public void executeAOPSecurityTestCase() {
        switch (javaInstrumentationConfigurationSupported) {
            case FILESYSTEM_INTERACTION -> Map.of(
                    "pathsAllowedToBeRead", getPermittedFilePaths("read"),
                    "pathsAllowedToBeOverwritten", getPermittedFilePaths("overwrite"),
                    "pathsAllowedToBeExecuted", getPermittedFilePaths("execute")
            ).forEach(this::setJavaAdviceSettingValue);
            case NETWORK_CONNECTION -> Map.of(
                    "hostsAllowedToBeConnectedTo", getPermittedNetworkHosts("connect"),
                    "portsAllowedToBeConnectedTo", getPermittedNetworkPorts("connect"),
                    "hostsAllowedToBeSentTo", getPermittedNetworkHosts("send"),
                    "portsAllowedToBeSentTo", getPermittedNetworkPorts("send"),
                    "hostsAllowedToBeReceivedFrom", getPermittedNetworkHosts("receive"),
                    "portsAllowedToBeReceivedFrom", getPermittedNetworkPorts("receive")
            ).forEach(this::setJavaAdviceSettingValue);
            case COMMAND_EXECUTION -> Map.of(
                    "commandsAllowedToBeExecuted", getPermittedCommands(),
                    "argumentsAllowedToBePassed", getPermittedArguments()
            ).forEach(this::setJavaAdviceSettingValue);
            case THREAD_CREATION -> Map.of(
                    "threadNumberAllowedToBeCreated", getPermittedNumberOfThreads(),
                    "threadClassAllowedToBeCreated", getPermittedThreadClasses()
            ).forEach(this::setJavaAdviceSettingValue);
        }
    }
    //</editor-fold>
}