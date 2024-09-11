package de.tum.cit.ase.ares.api.aop.java;

//<editor-fold desc="Imports">

import de.tum.cit.ase.ares.api.aop.AOPSecurityTestCase;
import de.tum.cit.ase.ares.api.policy.SecurityPolicy.ResourceAccesses;
import de.tum.cit.ase.ares.api.policy.SecurityPolicy.FilePermission;
import de.tum.cit.ase.ares.api.policy.SecurityPolicy.NetworkPermission;
import de.tum.cit.ase.ares.api.policy.SecurityPolicy.CommandPermission;
import de.tum.cit.ase.ares.api.policy.SecurityPolicy.ThreadPermission;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.reflect.Field;
import java.lang.reflect.InaccessibleObjectException;
import java.util.IllegalFormatException;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;
//</editor-fold>

/**
 * Configures Java instrumentation based on a security policy.
 * Implements the AOPSecurityTestCase interface for managing aspect configurations.
 */
public class JavaSecurityTestCase implements AOPSecurityTestCase {

    //<editor-fold desc="Attributes">
    /**
     * The type of security test case supported by this class (e.g., file system, network, etc.).
     */
    @Nonnull private final JavaSecurityTestCaseSupported javaSecurityTestCaseSupported;

    /**
     * The resource accesses permitted as defined in the security policy.
     */
    @Nonnull private final ResourceAccesses resourceAccesses;
    //</editor-fold>

    //<editor-fold desc="Constructor">

    /**
     * Initializes the configuration with the given support type and resource accesses.
     *
     * @param javaSecurityTestCaseSupported the type of security test case being supported, must not be null.
     * @param resourceAccesses the resource accesses permitted by the security policy, must not be null.
     */
    public JavaSecurityTestCase(@Nonnull JavaSecurityTestCaseSupported javaSecurityTestCaseSupported, @Nonnull ResourceAccesses resourceAccesses) {
        this.javaSecurityTestCaseSupported = javaSecurityTestCaseSupported;
        this.resourceAccesses = resourceAccesses;
    }
    //</editor-fold>

    //<editor-fold desc="Tool methods">

    /**
     * Generates a formatted advice setting string with its corresponding values.
     * <p>
     * This method creates a static field in a Java aspect configuration file based on the specified data type,
     * advice setting name, and its corresponding value. Supports multiple data types like String, String[],
     * String[][], and int[].
     * </p>
     *
     * @param dataType the data type of the advice setting (e.g., "String", "String[]"), must not be null.
     * @param adviceSetting the name of the advice setting to generate, must not be null.
     * @param value the value to be assigned to the advice setting, can be null.
     * @return a formatted string representing the advice setting definition.
     * @throws SecurityException if the value does not match the expected data type or formatting errors occur.
     */
    @Nonnull
    private static String generateAdviceSettingValue(@Nonnull String dataType, @Nonnull String adviceSetting, @Nullable Object value) {
        try {
            if (value == null) {
                return String.format("private static %s %s = null;%n", dataType, adviceSetting);
            }

            return switch (dataType) {
                case "String" -> {
                    if (!(value instanceof String)) {
                        throw new SecurityException(String.format(
                                "Ares Security Error (Reason: Ares-Code; Stage: Creation): "
                                        + "Invalid value type for String advice setting: "
                                        + "%s but should be String.",
                                value.getClass()));
                    }
                    yield String.format("private static String %s = \"%s\";%n", adviceSetting, value);
                }
                case "String[]" -> {
                    if (!(value instanceof List<?>)) {
                        throw new SecurityException(String.format(
                                "Ares Security Error (Reason: Ares-Code; Stage: Creation): "
                                        + "Invalid value type for String[] advice setting: "
                                        + "%s but should be List<String>.",
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
                                "Ares Security Error (Reason: Ares-Code; Stage: Creation): "
                                        + "Invalid value type for String[][] advice setting: "
                                        + "%s but should be List<List<String>>.",
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
                                "Ares Security Error (Reason: Ares-Code; Stage: Creation): "
                                        + "Invalid value type for int[] advice setting: "
                                        + "%s but should be List<Integer>.",
                                value.getClass()));
                    }
                    String intArrayValue = ((List<?>) value).stream()
                            .map(Object::toString)
                            .collect(Collectors.joining(", "));
                    yield String.format("private static int[] %s = new int[] {%s};%n", adviceSetting, intArrayValue);
                }
                default -> throw new SecurityException(
                        "Ares Security Error (Reason: Ares-Code; Stage: Creation): "
                                + "Unknown data type while creating the value "
                                + value
                                + " for the advice settings "
                                + dataType
                                + " "
                                + adviceSetting
                );
            };
        } catch (IllegalFormatException e) {
            throw new SecurityException(
                    "Ares Security Error (Reason: Ares-Code; Stage: Creation): "
                            + "Format error while creating the value "
                            + value
                            + " for the advice settings "
                            + dataType
                            + " "
                            + adviceSetting,
                    e
            );
        }

    }

    /**
     * Sets the value for a given Java advice setting using reflection.
     * <p>This method accesses the field of the {@code JavaSecurityTestCaseSettings} class using reflection
     * and assigns the specified value to it. This allows dynamically setting advice configurations based
     * on the security test case being executed.</p>
     *
     * @param adviceSetting the name of the advice setting field, must not be null.
     * @param value         the value to assign to the advice setting, can be null.
     * @throws SecurityException if there is any error during field access or value assignment.
     */
    public void setJavaAdviceSettingValue(@Nonnull String adviceSetting, @Nullable Object value) {
        try {
            @Nullable ClassLoader customClassLoader = Thread.currentThread().getContextClassLoader();
            @Nonnull Class<?> adviceSettingsClass = Class.forName("de.tum.cit.ase.ares.api.aop.java.JavaSecurityTestCaseSettings", true, customClassLoader);
            @Nonnull Field field = adviceSettingsClass.getDeclaredField(adviceSetting);
            field.setAccessible(true);
            field.set(null, value);
            field.setAccessible(false);
        } catch (LinkageError e) {
            throw new SecurityException(
                    "Ares Security Error (Reason: Ares-Code; Stage: Creation):"
                            + "Linkage error while accessing field '"
                            + adviceSetting
                            + "' in AdviceSettings",
                    e);
        } catch (ClassNotFoundException e) {
            throw new SecurityException(
                    "Ares Security Error (Reason: Ares-Code; Stage: Creation):"
                            + "Could not find 'JavaSecurityTestCaseSettings' class to access field '"
                            + adviceSetting
                            + "'",
                    e);
        } catch (NoSuchFieldException e) {
            throw new SecurityException(
                    "Ares Security Error (Reason: Ares-Code; Stage: Creation):"
                            + "Field '"
                            + adviceSetting
                            + "' not found in AdviceSettings",
                    e);
        } catch (NullPointerException e) {
            throw new SecurityException(
                    "Ares Security Error (Reason: Ares-Code; Stage: Creation):"
                            + "Null pointer exception while accessing field '"
                            + adviceSetting
                            + "' in AdviceSettings",
                    e);
        } catch (IllegalAccessException e) {
            throw new SecurityException(
                    "Ares Security Error (Reason: Ares-Code; Stage: Creation):"
                            + "Field '"
                            + adviceSetting
                            + "' is not accessible in AdviceSettings",
                    e);
        } catch (IllegalArgumentException e) {
            throw new SecurityException(
                    "Ares Security Error (Reason: Ares-Code; Stage: Creation):"
                            + "Illegal argument while setting field '"
                            + adviceSetting
                            + "' in AdviceSettings with value "
                            + value,
                    e);
        } catch (InaccessibleObjectException e) {
            throw new SecurityException(
                    "Ares Security Error (Reason: Ares-Code; Stage: Creation):"
                            + "Field '"
                            + adviceSetting
                            + "' is inaccessible in AdviceSettings",
                    e);
        }
    }
    //</editor-fold>

    //<editor-fold desc="File System Interactions related methods">

    /**
     * Extracts the permitted file paths from the provided configurations based on the given predicate.
     *
     * @param configs the list of JavaSecurityTestCase configurations, must not be null.
     * @param predicate a filter for determining which paths are permitted, must not be null.
     * @return a list of permitted paths.
     */
    @Nonnull
    private static List<String> extractPaths(@Nonnull List<JavaSecurityTestCase> configs, @Nonnull Predicate<FilePermission> predicate) {
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
     *
     * @param filePermission the type of file permission to filter by (e.g., "read", "overwrite"), must not be null.
     * @return a list of permitted file paths for the specified file permission type.
     */
    @Nonnull
    private List<String> getPermittedFilePaths(@Nonnull String filePermission) {
        @Nonnull Predicate<FilePermission> filter = switch (filePermission) {
            case "read" -> FilePermission::readAllFiles;
            case "overwrite" -> FilePermission::overwriteAllFiles;
            case "execute" -> FilePermission::executeAllFiles;
            case "delete" -> FilePermission::deleteAllFiles;
            default -> throw new IllegalArgumentException("Ares Security Error (Reason: Ares-Code; Stage: Creation): Invalid file permission: " + filePermission);
        };
        return resourceAccesses.regardingFileSystemInteractions()
                .stream()
                .filter(filter)
                .map(FilePermission::onThisPathAndAllPathsBelow)
                .toList();
    }

    //</editor-fold>

    //<editor-fold desc="Network Connections related methods">

    /**
     * Extracts the permitted network hosts from the provided configurations based on the given predicate.
     *
     * @param configs the list of JavaSecurityTestCase configurations, must not be null.
     * @param predicate a filter for determining which hosts are permitted, must not be null.
     * @return a list of permitted hosts.
     */
    @Nonnull
    private static List<String> extractHosts(@Nonnull List<JavaSecurityTestCase> configs, @Nonnull Predicate<NetworkPermission> predicate) {
        return configs.stream()
                .filter(config -> config.javaSecurityTestCaseSupported == JavaSecurityTestCaseSupported.NETWORK_CONNECTION)
                .map(config -> config.resourceAccesses.regardingNetworkConnections())
                .flatMap(List::stream)
                .filter(predicate)
                .map(NetworkPermission::onTheHost)
                .toList();
    }

    /**
     * Extracts the permitted network ports from the provided configurations based on the given predicate.
     *
     * @param configs the list of JavaSecurityTestCase configurations, must not be null.
     * @param predicate a filter for determining which ports are permitted, must not be null.
     * @return a list of permitted ports.
     */
    @Nonnull
    private static List<String> extractPorts(@Nonnull List<JavaSecurityTestCase> configs, @Nonnull Predicate<NetworkPermission> predicate) {
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
     *
     * @param networkPermission the type of network permission to filter by (e.g., "connect", "send"), must not be null.
     * @return a list of permitted network hosts for the specified network permission type.
     */
    @Nonnull
    private List<String> getPermittedNetworkHosts(@Nonnull String networkPermission) {
        @Nonnull Predicate<NetworkPermission> filter = switch (networkPermission) {
            case "connect" -> NetworkPermission::openConnections;
            case "send" -> NetworkPermission::sendData;
            case "receive" -> NetworkPermission::receiveData;
            default -> throw new IllegalArgumentException("Ares Security Error (Reason: Ares-Code; Stage: Creation): Invalid network permission: " + networkPermission);
        };
        return resourceAccesses.regardingNetworkConnections()
                .stream()
                .filter(filter)
                .map(NetworkPermission::onTheHost)
                .toList();
    }

    /**
     * Retrieves the list of network ports that are permitted for the given permission type.
     *
     * @param networkPermission the type of network permission to filter by (e.g., "connect", "send"), must not be null.
     * @return a list of permitted network ports for the specified network permission type.
     */
    @Nonnull
    private List<Integer> getPermittedNetworkPorts(@Nonnull String networkPermission) {
        @Nonnull Predicate<NetworkPermission> filter = switch (networkPermission) {
            case "connect" -> NetworkPermission::openConnections;
            case "send" -> NetworkPermission::sendData;
            case "receive" -> NetworkPermission::receiveData;
            default -> throw new IllegalArgumentException("Ares Security Error (Reason: Ares-Code; Stage: Creation): Invalid network permission: " + networkPermission);
        };
        return resourceAccesses.regardingNetworkConnections()
                .stream()
                .filter(filter)
                .map(NetworkPermission::onThePort)
                .toList();
    }
    //</editor-fold>

    //<editor-fold desc="Command Execution related methods">

    /**
     * Extracts the permitted commands from the provided configurations.
     *
     * @param configs the list of JavaSecurityTestCase configurations, must not be null.
     * @return a list of permitted commands.
     */
    @Nonnull
    private static List<String> extractCommands(@Nonnull List<JavaSecurityTestCase> configs) {
        return configs.stream()
                .filter(config -> config.javaSecurityTestCaseSupported == JavaSecurityTestCaseSupported.COMMAND_EXECUTION)
                .map(config -> config.resourceAccesses.regardingCommandExecutions())
                .flatMap(List::stream)
                .map(CommandPermission::executeTheCommand)
                .toList();
    }

    /**
     * Extracts the permitted arguments for command execution from the provided configurations.
     *
     * @param configs the list of JavaSecurityTestCase configurations, must not be null.
     * @return a list of permitted command arguments.
     */
    @Nonnull
    private static List<String> extractArguments(@Nonnull List<JavaSecurityTestCase> configs) {
        return configs.stream()
                .filter(config -> config.javaSecurityTestCaseSupported == JavaSecurityTestCaseSupported.COMMAND_EXECUTION)
                .map(config -> config.resourceAccesses.regardingCommandExecutions())
                .flatMap(List::stream)
                .map(CommandPermission::withTheseArguments)
                .map(arguments -> "new String[] {" + String.join(",", arguments) + "}")
                .toList();
    }

    /**
     * Retrieves the list of commands that are permitted to be executed.
     *
     * @return a list of permitted commands, must not be null.
     */
    @Nonnull
    private List<String> getPermittedCommands() {
        return resourceAccesses.regardingCommandExecutions()
                .stream()
                .map(CommandPermission::executeTheCommand)
                .toList();
    }

    /**
     * Retrieves the list of arguments permitted for execution with commands.
     *
     * @return a list of arguments permitted for command execution, must not be null.
     */
    @Nonnull
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
     *
     * @param configs the list of JavaSecurityTestCase configurations, must not be null.
     * @return a list of permitted thread numbers.
     */
    @Nonnull
    private static List<String> extractThreadNumbers(@Nonnull List<JavaSecurityTestCase> configs) {
        return configs.stream()
                .filter(config -> config.javaSecurityTestCaseSupported == JavaSecurityTestCaseSupported.THREAD_CREATION)
                .map(config -> config.resourceAccesses.regardingThreadCreations())
                .flatMap(List::stream)
                .map(ThreadPermission::createTheFollowingNumberOfThreads)
                .map(String::valueOf)
                .toList();
    }

    /**
     * Retrieves the list of permitted thread classes based on the security policy.
     *
     * @param configs the list of JavaSecurityTestCase configurations, must not be null.
     * @return a list of permitted thread classes.
     */
    @Nonnull
    private static List<String> extractThreadClasses(@Nonnull List<JavaSecurityTestCase> configs) {
        return configs.stream()
                .filter(config -> config.javaSecurityTestCaseSupported == JavaSecurityTestCaseSupported.THREAD_CREATION)
                .map(config -> config.resourceAccesses.regardingThreadCreations())
                .flatMap(List::stream)
                .map(ThreadPermission::ofThisClass)
                .toList();
    }

    /**
     * Retrieves the list of permitted thread counts based on the security policy.
     *
     * @return a list of permitted thread numbers, must not be null.
     */
    @Nonnull
    private List<Integer> getPermittedNumberOfThreads() {
        return resourceAccesses.regardingThreadCreations()
                .stream()
                .map(ThreadPermission::createTheFollowingNumberOfThreads)
                .toList();
    }

    /**
     * Retrieves the list of permitted thread classes based on the security policy.
     *
     * @return a list of permitted thread classes, must not be null.
     */
    @Nonnull
    private List<String> getPermittedThreadClasses() {
        return resourceAccesses.regardingThreadCreations()
                .stream()
                .map(ThreadPermission::ofThisClass)
                .toList();
    }
    //</editor-fold>

    //<editor-fold desc="Write security test case methods">

    /**
     * Generates the content for the AOP security test case.
     * <p>This method provides an empty implementation for now but will be overridden in future
     * configurations to generate aspect configuration files based on the provided security policies.</p>
     *
     * @return a string representing the content of the aspect configuration.
     */
    @Override
    @Nonnull
    public String writeAOPSecurityTestCase() {
        return "";
    }
    //</editor-fold>

    //<editor-fold desc="Write security test case file methods">

    /**
     * Writes the aspect configuration content based on the provided security test cases.
     *
     * @param aomMode             the AOP mode (AspectJ or Instrumentation), must not be null.
     * @param restrictedPackage    the restricted package, must not be null.
     * @param allowedListedClasses the list of allowed classes in the restricted package, must not be null.
     * @param javaSecurityTestCases the list of security test cases to be used, must not be null.
     * @return a string representing the content of the AOP security test case configuration file.
     */
    @SuppressWarnings("StringBufferReplaceableByString")
    @Nonnull
    public static String writeAOPSecurityTestCaseFile(
            @Nonnull String aomMode,
            @Nonnull String restrictedPackage,
            @Nonnull List<String> allowedListedClasses,
            @Nonnull List<JavaSecurityTestCase> javaSecurityTestCases
    ) {
        @Nonnull StringBuilder fileContentBuilder = new StringBuilder();
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
     * Executes the AOP security test case by setting Java advice settings.
     */
    @Override
    public void executeAOPSecurityTestCase() {
        switch (javaSecurityTestCaseSupported) {
            case FILESYSTEM_INTERACTION -> Map.of(
                    "pathsAllowedToBeRead", getPermittedFilePaths("read").toArray(String[]::new),
                    "pathsAllowedToBeOverwritten", getPermittedFilePaths("overwrite").toArray(String[]::new),
                    "pathsAllowedToBeExecuted", getPermittedFilePaths("execute").toArray(String[]::new),
                    "pathsAllowedToBeDeleted", getPermittedFilePaths("delete").toArray(String[]::new)
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