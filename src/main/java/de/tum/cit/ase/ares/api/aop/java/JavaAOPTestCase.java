package de.tum.cit.ase.ares.api.aop.java;

//<editor-fold desc="Imports">

import de.tum.cit.ase.ares.api.aop.AOPTestCase;
import de.tum.cit.ase.ares.api.aop.java.javaAOPTestCaseToolbox.JavaAOPAdviceSettingTriple;
import de.tum.cit.ase.ares.api.aop.java.javaAOPTestCaseToolbox.JavaAOPTestCaseToolbox;
import de.tum.cit.ase.ares.api.policy.SecurityPolicy;
import de.tum.cit.ase.ares.api.policy.policySubComponents.CommandPermission;
import de.tum.cit.ase.ares.api.policy.policySubComponents.FilePermission;
import de.tum.cit.ase.ares.api.policy.policySubComponents.NetworkPermission;
import de.tum.cit.ase.ares.api.policy.policySubComponents.ThreadPermission;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.reflect.Field;
import java.lang.reflect.InaccessibleObjectException;
import java.util.IllegalFormatException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static de.tum.cit.ase.ares.api.aop.java.instrumentation.advice.JavaInstrumentationAdviceFileSystemToolbox.localize;
import static de.tum.cit.ase.ares.api.localization.Messages.localized;
//</editor-fold>

/**
 * Configures Java instrumentation based on a security policy.
 * Implements the AOPSecurityTestCase interface for managing aspect configurations.
 */
public class JavaAOPTestCase implements AOPTestCase {

    //<editor-fold desc="Attributes">
    /**
     * The type of security test case supported by this class (e.g., file system, network, etc.).
     */
    @Nonnull
    private final JavaAOPTestCaseSupported javaAOPTestCaseSupported;

    /**
     * The supplier for the resource accesses permitted as defined in the security policy.
     */
    @Nonnull
    private final Supplier<List<?>> resourceAccessSupplier;

    /**
     * The list of allowed classes in the restricted package.
     */
    @Nonnull
    private final Set<String> allowedClasses;
    //</editor-fold>

    //<editor-fold desc="Constructor">

    /**
     * Initializes the configuration with the given support type and resource accesses.
     *
     * @param javaSecurityTestCaseSupported the type of security test case being supported, must not be null.
     * @param resourceAccessSupplier the resource accesses permitted as defined in the security policy, must not be null.
     */
    public JavaAOPTestCase(@Nonnull JavaAOPTestCaseSupported javaSecurityTestCaseSupported, @Nonnull Supplier<List<?>> resourceAccessSupplier, @Nonnull Set<String> allowedClasses) {
        this.javaAOPTestCaseSupported = javaSecurityTestCaseSupported;
        this.resourceAccessSupplier = resourceAccessSupplier;
        this.allowedClasses = allowedClasses;
        //this.resourceAccesses = resourceAccesses;
    }
    //</editor-fold>

    //<editor-fold desc="Getter">
    @Nonnull
    public JavaAOPTestCaseSupported getJavaAOPTestCaseSupported() {
        return javaAOPTestCaseSupported;
    }

    @Nonnull
    public Supplier<List<?>> getResourceAccessSupplier() {
        return resourceAccessSupplier;
    }

    @Nonnull
    public Set<String> getAllowedClasses() {
        return allowedClasses;
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
     * @param dataType      the data type of the advice setting (e.g., "String", "String[]"), must not be null.
     * @param adviceSetting the name of the advice setting to generate, must not be null.
     * @param value         the value to be assigned to the advice setting, can be null.
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
                case "String" -> JavaAOPTestCaseToolbox.getStringAssignment(adviceSetting, value);
                case "String[]" -> JavaAOPTestCaseToolbox.getStringOneDArrayAssignment(adviceSetting, value);
                case "String[][]" -> JavaAOPTestCaseToolbox.getStringTwoDArrayAssignment(adviceSetting, value);
                case "int", "Integer" -> JavaAOPTestCaseToolbox.getIntegerAssignment(adviceSetting, value);
                case "int[]", "Integer[]" -> JavaAOPTestCaseToolbox.getIntegerOneDArrayAssignment(adviceSetting, value);
                case "int[][]", "Integer[][]" ->
                        JavaAOPTestCaseToolbox.getIntegerTwoDArrayAssignment(adviceSetting, value);
                default ->
                        throw new SecurityException(localize("security.advice.settings.data.type.unknown", value, dataType, adviceSetting));
            };
        } catch (IllegalFormatException e) {
            throw new SecurityException(localize("security.advice.invalid.format", value, dataType, adviceSetting));
        }
    }

    /**
     * Generates a formatted advice setting string with its corresponding values.
     * <p>
     * This method creates a static field in a Java aspect configuration file based on the specified data type,
     * advice setting name, and its corresponding value. Supports multiple data types like String, String[],
     * String[][], and int[].
     * </p>
     *
     * @param adviceSettingTriple the advice setting triple to generate, must not be null.
     * @return a formatted string representing the advice setting definition.
     * @throws SecurityException if the value does not match the expected data type or formatting errors occur.
     */
    @Nonnull
    private static String generateAdviceSettingValue(@Nonnull JavaAOPAdviceSettingTriple adviceSettingTriple) {
        return generateAdviceSettingValue(adviceSettingTriple.dataTyp(), adviceSettingTriple.adviceSetting(), adviceSettingTriple.value());
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
    public static void setJavaAdviceSettingValue(@Nonnull String adviceSetting, @Nullable Object value, @Nonnull String architectureMode, @Nonnull String aopMode) {
        try {
            @Nullable ClassLoader customClassLoader = Thread.currentThread().getContextClassLoader();
            @Nonnull Class<?> adviceSettingsClass = Class.forName("de.tum.cit.ase.ares.api.aop.java.JavaAOPTestCaseSettings", true, aopMode.equals("INSTRUMENTATION") ? null : customClassLoader);
            @Nonnull Field field = adviceSettingsClass.getDeclaredField(adviceSetting);
            field.setAccessible(true);
            field.set(null, value);
            field.setAccessible(false);
        } catch (LinkageError e) {
            throw new SecurityException(
                    localize("security.advice.linkage.exception", adviceSetting),
                    e);
        } catch (ClassNotFoundException e) {
            /*throw new SecurityException(
                    localize("security.advice.class.not.found.exception", adviceSetting),
                    e);*/
        } catch (NoSuchFieldException e) {
            throw new SecurityException(
                    localize("security.advice.no.such.field.exception", adviceSetting),
                    e);
        } catch (NullPointerException e) {
            throw new SecurityException(
                    localize("security.advice.null.pointer.exception", adviceSetting),
                    e);
        } catch (IllegalAccessException e) {
            throw new SecurityException(
                    localize("security.advice.illegal.access.exception", adviceSetting),
                    e);
        } catch (IllegalArgumentException e) {
            throw new SecurityException(
                    localize("security.advice.illegal.argument.exception", adviceSetting, value),
                    e);
        } catch (InaccessibleObjectException e) {
            throw new SecurityException(
                    localize("security.advice.inaccessible.object.exception", adviceSetting),
                    e);
        }
    }
    //</editor-fold>

    //<editor-fold desc="File System Interactions related methods">

    /**
     * Extracts the permitted file paths from the provided configurations based on the given predicate.
     *
     * @param configs   the list of JavaSecurityTestCase configurations, must not be null.
     * @param predicate a filter for determining which paths are permitted, must not be null.
     * @return a list of permitted paths.
     */
    @Nonnull
    private static List<String> extractPaths(@Nonnull List<FilePermission> configs, @Nonnull Predicate<FilePermission> predicate) {
        return configs.stream()
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
            default ->
                    throw new IllegalArgumentException(localize("security.advice.settings.invalid.file.permission", filePermission));
        };
        return ((List<FilePermission>) resourceAccessSupplier.get())
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
     * @param configs   the list of JavaSecurityTestCase configurations, must not be null.
     * @param predicate a filter for determining which hosts are permitted, must not be null.
     * @return a list of permitted hosts.
     */
    @Nonnull
    private static List<String> extractHosts(@Nonnull List<NetworkPermission> configs, @Nonnull Predicate<NetworkPermission> predicate) {
        return configs.stream()
                .filter(predicate)
                .map(NetworkPermission::onTheHost)
                .toList();
    }

    /**
     * Extracts the permitted network ports from the provided configurations based on the given predicate.
     *
     * @param configs   the list of JavaSecurityTestCase configurations, must not be null.
     * @param predicate a filter for determining which ports are permitted, must not be null.
     * @return a list of permitted ports.
     */
    @Nonnull
    private static List<String> extractPorts(@Nonnull List<NetworkPermission> configs, @Nonnull Predicate<NetworkPermission> predicate) {
        return configs.stream()
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
            default ->
                    throw new IllegalArgumentException(localize("security.advice.settings.invalid.network.permission", networkPermission));
        };
        return ((List<NetworkPermission>) resourceAccessSupplier.get())
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
            default ->
                    throw new IllegalArgumentException(localize("security.advice.settings.invalid.network.permission", networkPermission));
        };
        return ((List<NetworkPermission>) resourceAccessSupplier.get())
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
    private static List<String> extractCommands(@Nonnull List<CommandPermission> configs) {
        return configs.stream()
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
    private static List<String> extractArguments(@Nonnull List<CommandPermission> configs) {
        return configs.stream()
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
        return ((List<CommandPermission>) resourceAccessSupplier.get())
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
        return ((List<CommandPermission>) resourceAccessSupplier.get())
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
    private static List<String> extractThreadNumbers(@Nonnull List<ThreadPermission> configs) {
        return configs.stream()
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
    private static List<String> extractThreadClasses(@Nonnull List<ThreadPermission> configs) {
        return configs.stream()
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
        return ((List<ThreadPermission>) resourceAccessSupplier.get())
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
        return ((List<ThreadPermission>) resourceAccessSupplier.get())
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
    public String writeAOPSecurityTestCase(@Nonnull String architectureMode, @Nonnull String aopMode) {
        return "";
    }
    //</editor-fold>

    //<editor-fold desc="Write security test case file methods">

    /**
     * Writes the aspect configuration content based on the provided security test cases.
     *
     * @param aopMode               the AOP mode (AspectJ or Instrumentation), must not be null.
     * @param restrictedPackage     the restricted package, must not be null.
     * @param allowedListedClasses  the list of allowed classes in the restricted package, must not be null.
     * @param filePermissions       the list of file permissions, must not be null.
     * @param networkPermissions    the list of network permissions, must not be null.
     * @param commandPermissions    the list of command permissions, must not be null.
     * @param threadPermissions     the list of thread permissions, must not be null.
     * @return a string representing the content of the AOP security test case configuration file.
     */
    @Nonnull
    public static String writeAOPSecurityTestCaseFile(
            @Nonnull String aopMode,
            @Nonnull String restrictedPackage,
            @Nonnull List<String> allowedListedClasses,
            @Nonnull List<FilePermission> filePermissions,
            @Nonnull List<NetworkPermission> networkPermissions,
            @Nonnull List<CommandPermission> commandPermissions,
            @Nonnull List<ThreadPermission> threadPermissions
    ) {
        @Nonnull StringBuilder fileContentBuilder = new StringBuilder();
        Stream.of(
                        new JavaAOPAdviceSettingTriple("String", " aopMode", aopMode),
                        new JavaAOPAdviceSettingTriple("String", " restrictedPackage", restrictedPackage),
                        new JavaAOPAdviceSettingTriple("String[]", " allowedListedClasses", allowedListedClasses),
                        new JavaAOPAdviceSettingTriple("String[]", " pathsAllowedToBeRead", extractPaths(filePermissions, FilePermission::readAllFiles)),
                        new JavaAOPAdviceSettingTriple("String[]", " pathsAllowedToBeOverwritten", extractPaths(filePermissions, FilePermission::overwriteAllFiles)),
                        new JavaAOPAdviceSettingTriple("String[]", " pathsAllowedToBeExecuted", extractPaths(filePermissions, FilePermission::executeAllFiles)),
                        new JavaAOPAdviceSettingTriple("String[]", " pathsAllowedToBeDeleted", extractPaths(filePermissions, FilePermission::deleteAllFiles)),
                        new JavaAOPAdviceSettingTriple("String[]", " hostsAllowedToBeConnectedTo", extractHosts(networkPermissions, NetworkPermission::openConnections)),
                        new JavaAOPAdviceSettingTriple("int[]", " portsAllowedToBeConnectedTo", extractPorts(networkPermissions, NetworkPermission::openConnections)),
                        new JavaAOPAdviceSettingTriple("String[]", " hostsAllowedToBeSentTo", extractHosts(networkPermissions, NetworkPermission::sendData)),
                        new JavaAOPAdviceSettingTriple("int[]", " portsAllowedToBeSentTo", extractPorts(networkPermissions, NetworkPermission::sendData)),
                        new JavaAOPAdviceSettingTriple("String[]", " hostsAllowedToBeReceivedFrom", extractHosts(networkPermissions, NetworkPermission::receiveData)),
                        new JavaAOPAdviceSettingTriple("int[]", " portsAllowedToBeReceivedFrom", extractPorts(networkPermissions, NetworkPermission::receiveData)),
                        new JavaAOPAdviceSettingTriple("String[]", " commandsAllowedToBeExecuted", extractCommands(commandPermissions)),
                        new JavaAOPAdviceSettingTriple("String[][]", " argumentsAllowedToBePassed", extractArguments(commandPermissions)),
                        new JavaAOPAdviceSettingTriple("int[]", " threadNumberAllowedToBeCreated", extractThreadNumbers(threadPermissions)),
                        new JavaAOPAdviceSettingTriple("String[]", " threadClassAllowedToBeCreated", extractThreadClasses(threadPermissions))
                )
                .map(JavaAOPTestCase::generateAdviceSettingValue)
                .forEach(fileContentBuilder::append);
        return fileContentBuilder.toString();
    }
    //</editor-fold>

    //<editor-fold desc="Execute security test case methods">

    /**
     * Executes the AOP security test case by setting Java advice settings.
     */
    @Override
    public void executeAOPSecurityTestCase(@Nonnull String architectureMode, @Nonnull String aopMode) {
        switch (javaAOPTestCaseSupported) {
            case FILESYSTEM_INTERACTION -> Map.of(
                    "pathsAllowedToBeRead", getPermittedFilePaths("read").toArray(String[]::new),
                    "pathsAllowedToBeOverwritten", getPermittedFilePaths("overwrite").toArray(String[]::new),
                    "pathsAllowedToBeExecuted", getPermittedFilePaths("execute").toArray(String[]::new),
                    "pathsAllowedToBeDeleted", getPermittedFilePaths("delete").toArray(String[]::new)
            ).forEach((k, v) -> JavaAOPTestCase.setJavaAdviceSettingValue(k, v, architectureMode, aopMode));
            case NETWORK_CONNECTION -> Map.of(
                    "hostsAllowedToBeConnectedTo", getPermittedNetworkHosts("connect").toArray(String[]::new),
                    "portsAllowedToBeConnectedTo", getPermittedNetworkPorts("connect").stream().mapToInt(Integer::intValue).toArray(),
                    "hostsAllowedToBeSentTo", getPermittedNetworkHosts("send").toArray(String[]::new),
                    "portsAllowedToBeSentTo", getPermittedNetworkPorts("send").stream().mapToInt(Integer::intValue).toArray(),
                    "hostsAllowedToBeReceivedFrom", getPermittedNetworkHosts("receive").toArray(String[]::new),
                    "portsAllowedToBeReceivedFrom", getPermittedNetworkPorts("receive").stream().mapToInt(Integer::intValue).toArray()
            ).forEach((k, v) -> JavaAOPTestCase.setJavaAdviceSettingValue(k, v, architectureMode, aopMode));
            case COMMAND_EXECUTION -> Map.of(
                    "commandsAllowedToBeExecuted", getPermittedCommands().toArray(String[]::new),
                    "argumentsAllowedToBePassed", getPermittedArguments().stream().map(innerList -> innerList.toArray(new String[0])).toArray(String[][]::new)
            ).forEach((k, v) -> JavaAOPTestCase.setJavaAdviceSettingValue(k, v, architectureMode, aopMode));
            case THREAD_CREATION -> Map.of(
                    "threadNumberAllowedToBeCreated", getPermittedNumberOfThreads().stream().mapToInt(Integer::intValue).toArray(),
                    "threadClassAllowedToBeCreated", getPermittedThreadClasses().toArray(String[]::new)
            ).forEach((k, v) -> JavaAOPTestCase.setJavaAdviceSettingValue(k, v, architectureMode, aopMode));
        }
    }
    //</editor-fold>

    //<editor-fold desc="Builder">

    /**
     * Starts the builder for the Java architecture test case.
     */
    public static JavaAOPTestCase.Builder builder() {
        return new JavaAOPTestCase.Builder();
    }

    /**
     * Builder for the Java architecture test case.
     */
    public static class Builder {
        private JavaAOPTestCaseSupported javaAOPTestCaseSupported;
        private Supplier<List<?>> resourceAccessSupplier;
        private Set<String> allowedClasses;

        public JavaAOPTestCase.Builder javaAOPTestCaseSupported(JavaAOPTestCaseSupported javaAOPTestCaseSupported) {
            if (javaAOPTestCaseSupported == null) {
                throw new SecurityException(localized("security.common.not.null", "javaAOPTestCaseSupported"));
            }
            this.javaAOPTestCaseSupported = javaAOPTestCaseSupported;
            return this;
        }

        public JavaAOPTestCase.Builder allowedClasses(Set<String> allowedClasses) {
            if (allowedClasses == null) {
                throw new SecurityException(localized("security.common.not.null", "resourceAccessSupplier"));
            }
            this.allowedClasses = allowedClasses;
            return this;
        }

        public JavaAOPTestCase.Builder resourceAccessSupplier(Supplier<List<?>> resourceAccessSupplier) {
            if (resourceAccessSupplier == null) {
                throw new SecurityException(localized("security.common.not.null", "resourceAccessSupplier"));
            }
            this.resourceAccessSupplier = resourceAccessSupplier;
            return this;
        }

        public JavaAOPTestCase build() {
            if (javaAOPTestCaseSupported == null) {
                throw new SecurityException(localized("security.common.not.null", "javaArchitectureTestCaseSupported"));
            }
            if (resourceAccessSupplier == null) {
                throw new SecurityException(localized("security.common.not.null", "resourceAccessSupplier"));
            }
            if(allowedClasses == null) {
                throw new SecurityException(localized("security.common.not.null", "allowedClasses"));
            }
            return new JavaAOPTestCase(javaAOPTestCaseSupported, resourceAccessSupplier, allowedClasses);
        }
    }
    //</editor-fold>
}