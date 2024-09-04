package de.tum.cit.ase.ares.api.aop.java;

//<editor-fold desc="Imports">

import de.tum.cit.ase.ares.api.aop.AOPSecurityTestCase;
import de.tum.cit.ase.ares.api.policy.SecurityPolicy.ResourceAccesses;
import de.tum.cit.ase.ares.api.policy.SecurityPolicy.FilePermission;
import de.tum.cit.ase.ares.api.policy.SecurityPolicy.NetworkPermission;
import de.tum.cit.ase.ares.api.policy.SecurityPolicy.CommandPermission;
import de.tum.cit.ase.ares.api.policy.SecurityPolicy.ThreadPermission;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Collectors;
//</editor-fold>

/**
 * Configures Java instrumentation based on a security policy.
 * Implements the AOPSecurityTestCase interface for managing aspect configurations.
 */
public class JavaSecurityTestCase implements AOPSecurityTestCase {

    //<editor-fold desc="Attributes">
    private final JavaSecurityTestCaseSupported javaSecurityTestCaseSupported;
    private final ResourceAccesses resourceAccesses;
    //</editor-fold>

    //<editor-fold desc="Constructor">

    /**
     * Initializes the configuration with the given support type and resource accesses.
     */
    public JavaSecurityTestCase(JavaSecurityTestCaseSupported javaSecurityTestCaseSupported, ResourceAccesses resourceAccesses) {
        this.javaSecurityTestCaseSupported = javaSecurityTestCaseSupported;
        this.resourceAccesses = resourceAccesses;
    }
    //</editor-fold>

    //<editor-fold desc="Tool methods">

    /**
     * Formats an advice setting with its corresponding values.
     */

    private static String generateAdviceSettingValue(String dataType, String adviceSetting, Object value) {
        if (value == null) {
            return String.format("private static %s %s = null;%n", dataType, adviceSetting);
        }

        return switch (dataType) {
            case "String" -> {
                if (!(value instanceof String)) {
                    throw new SecurityException(String.format(
                            "Invalid value type for String advice setting: %s but should be String",
                            value.getClass()));
                }
                yield String.format("private static String %s = \"%s\";%n", adviceSetting, value);
            }
            case "String[]" -> {
                if (!(value instanceof List<?>)) {
                    throw new SecurityException(String.format(
                            "Invalid value type for String[] advice setting: %s but should be List<String>",
                            value.getClass()));
                }
                String stringArrayValue = ((List<?>) value).stream()
                        .map(Object::toString)
                        .map(s -> String.format("\"%s\"", s))
                        .collect(Collectors.joining(", "));
                yield String.format("private static String[] %s = new String[] {%s};%n", adviceSetting, stringArrayValue);
            }
            case "String[][]" -> {
                if (!(value instanceof List<?>)) {
                    throw new SecurityException(String.format(
                            "Invalid value type for String[][] advice setting: %s but should be List<List<String>>",
                            value.getClass()));
                }
                String stringArrayArrayValue = ((List<?>) value).stream()
                        .filter(e -> e instanceof List<?>)
                        .map(e -> (List<?>) e)
                        .map(innerList -> innerList.stream()
                                .map(Object::toString)
                                .map(s -> String.format("\"%s\"", s))
                                .collect(Collectors.joining(", ")))
                        .map(innerArray -> "new String[]{" + innerArray + "}")
                        .collect(Collectors.joining(", "));
                yield String.format("private static String[][] %s = new String[][] {%s};%n", adviceSetting, stringArrayArrayValue);
            }
            case "int[]" -> {
                if (!(value instanceof List<?>)) {
                    throw new SecurityException(String.format(
                            "Invalid value type for int[] advice setting: %s but should be List<Integer>",
                            value.getClass()));
                }
                String intArrayValue = ((List<?>) value).stream()
                        .map(Object::toString)
                        .collect(Collectors.joining(", "));
                yield String.format("private static int[] %s = new int[] {%s};%n", adviceSetting, intArrayValue);
            }
            default -> throw new IllegalArgumentException(String.format("Unsupported data type: %s", dataType));
        };
    }

    /**
     * Uses reflection to set the value of a Java advice setting.
     */
    public void setJavaAdviceSettingValue(String adviceSetting, Object value) {
        try {
            ClassLoader customClassLoader = Thread.currentThread().getContextClassLoader();
            Class<?> adviceSettingsClass = Class.forName("de.tum.cit.ase.ares.api.aop.java.JavaSecurityTestCaseSettings", true, customClassLoader);
            Field field = adviceSettingsClass.getDeclaredField(adviceSetting);
            field.setAccessible(true);
            field.set(null, value);
            field.setAccessible(false);
        } catch (ClassNotFoundException e) {
            throw new SecurityException("Failed to find the class for setting Java advice setting: " + adviceSetting, e);
        } catch (NoSuchFieldException e) {
            throw new SecurityException("Failed to find the field for setting Java advice setting: " + adviceSetting, e);
        } catch (IllegalAccessException e) {
            throw new SecurityException("Illegal access while setting Java advice setting: " + adviceSetting, e);
        } catch (Exception e) {
            throw new SecurityException("An unexpected error occurred while setting Java advice setting: " + adviceSetting, e);
        }
    }
    //</editor-fold>

    //<editor-fold desc="File System Interactions related methods">

    private static List<String> extractPaths(List<JavaSecurityTestCase> configs, Predicate<FilePermission> predicate) {
        return configs.stream()
                .filter(config -> config.javaSecurityTestCaseSupported == JavaSecurityTestCaseSupported.FILESYSTEM_INTERACTION)
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
            case "delete" -> FilePermission::deleteAllFiles;
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

    private static List<String> extractHosts(List<JavaSecurityTestCase> configs, Predicate<NetworkPermission> predicate) {
        return configs.stream()
                .filter(config -> config.javaSecurityTestCaseSupported == JavaSecurityTestCaseSupported.NETWORK_CONNECTION)
                .map(config -> config.resourceAccesses.regardingNetworkConnections())
                .flatMap(List::stream)
                .filter(predicate)
                .map(NetworkPermission::onTheHost)
                .toList();
    }

    private static List<String> extractPorts(List<JavaSecurityTestCase> configs, Predicate<NetworkPermission> predicate) {
        return configs.stream()
                .filter(config -> config.javaSecurityTestCaseSupported == JavaSecurityTestCaseSupported.NETWORK_CONNECTION)
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

    private static List<String> extractCommands(List<JavaSecurityTestCase> configs) {
        return configs.stream()
                .filter(config -> config.javaSecurityTestCaseSupported == JavaSecurityTestCaseSupported.COMMAND_EXECUTION)
                .map(config -> config.resourceAccesses.regardingCommandExecutions())
                .flatMap(List::stream)
                .map(CommandPermission::executeTheCommand)
                .toList();
    }

    private static List<String> extractArguments(List<JavaSecurityTestCase> configs) {
        return configs.stream()
                .filter(config -> config.javaSecurityTestCaseSupported == JavaSecurityTestCaseSupported.COMMAND_EXECUTION)
                .map(config -> config.resourceAccesses.regardingCommandExecutions())
                .flatMap(List::stream)
                .map(CommandPermission::withTheseArguments)
                .filter(Objects::nonNull)
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
    private static List<String> extractThreadNumbers(List<JavaSecurityTestCase> configs) {
        return configs.stream()
                .filter(config -> config.javaSecurityTestCaseSupported == JavaSecurityTestCaseSupported.THREAD_CREATION)
                .map(config -> config.resourceAccesses.regardingThreadCreations())
                .flatMap(List::stream)
                .map(ThreadPermission::createTheFollowingNumberOfThreads)
                .map(String::valueOf)
                .toList();
    }

    private static List<String> extractThreadClasses(List<JavaSecurityTestCase> configs) {
        return configs.stream()
                .filter(config -> config.javaSecurityTestCaseSupported == JavaSecurityTestCaseSupported.THREAD_CREATION)
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
    public static String writeAOPSecurityTestCaseFile(
            String aomMode,
            String restrictedPackage,
            List<String> allowedListedClasses,
            List<JavaSecurityTestCase> javaSecurityTestCases
    ) {
        StringBuilder fileContentBuilder = new StringBuilder();
        fileContentBuilder.append(generateAdviceSettingValue("String", "aomMode", aomMode));
        fileContentBuilder.append(generateAdviceSettingValue("String", "restrictedPackage", restrictedPackage));
        fileContentBuilder.append(generateAdviceSettingValue("String[]", "allowedListedClasses", allowedListedClasses));
        fileContentBuilder.append(generateAdviceSettingValue("String[]", "pathsAllowedToBeRead", extractPaths(javaSecurityTestCases, FilePermission::readAllFiles)));
        fileContentBuilder.append(generateAdviceSettingValue("String[]", " pathsAllowedToBeOverwritten", extractPaths(javaSecurityTestCases, FilePermission::overwriteAllFiles)));
        fileContentBuilder.append(generateAdviceSettingValue("String[]", " pathsAllowedToBeExecuted", extractPaths(javaSecurityTestCases, FilePermission::executeAllFiles)));
        fileContentBuilder.append(generateAdviceSettingValue("String[]", " pathsAllowedToBeDeleted", extractPaths(javaSecurityTestCases, FilePermission::deleteAllFiles)));
        fileContentBuilder.append(generateAdviceSettingValue("String[]", " hostsAllowedToBeConnectedTo", extractHosts(javaSecurityTestCases, NetworkPermission::openConnections)));
        fileContentBuilder.append(generateAdviceSettingValue("int[]", " portsAllowedToBeConnectedTo", extractPorts(javaSecurityTestCases, NetworkPermission::openConnections)));
        fileContentBuilder.append(generateAdviceSettingValue("String[]", " hostsAllowedToBeSentTo", extractHosts(javaSecurityTestCases, NetworkPermission::sendData)));
        fileContentBuilder.append(generateAdviceSettingValue("int[]", " portsAllowedToBeSentTo", extractPorts(javaSecurityTestCases, NetworkPermission::sendData)));
        fileContentBuilder.append(generateAdviceSettingValue("String[]", " hostsAllowedToBeReceivedFrom", extractHosts(javaSecurityTestCases, NetworkPermission::receiveData)));
        fileContentBuilder.append(generateAdviceSettingValue("int[]", " portsAllowedToBeReceivedFrom", extractPorts(javaSecurityTestCases, NetworkPermission::receiveData)));
        fileContentBuilder.append(generateAdviceSettingValue("String[]", " commandsAllowedToBeExecuted", extractCommands(javaSecurityTestCases)));
        fileContentBuilder.append(generateAdviceSettingValue("String[][]", " argumentsAllowedToBePassed", extractArguments(javaSecurityTestCases)));
        fileContentBuilder.append(generateAdviceSettingValue("int[]", " threadNumberAllowedToBeCreated", extractThreadNumbers(javaSecurityTestCases)));
        fileContentBuilder.append(generateAdviceSettingValue("String[]", " threadClassAllowedToBeCreated", extractThreadClasses(javaSecurityTestCases)));
        return fileContentBuilder.toString();
    }
    //</editor-fold>

    //<editor-fold desc="Execute security test case methods">

    /**
     * Runs the aspect configuration by setting the necessary Java advice settings.
     */
    @Override
    public void executeAOPSecurityTestCase() {
        switch (javaSecurityTestCaseSupported) {
            case FILESYSTEM_INTERACTION -> Map.of(
                    "pathsAllowedToBeRead", getPermittedFilePaths("read"),
                    "pathsAllowedToBeOverwritten", getPermittedFilePaths("overwrite"),
                    "pathsAllowedToBeExecuted", getPermittedFilePaths("execute"),
                    "pathsAllowedToBeDeleted", getPermittedFilePaths("delete")
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