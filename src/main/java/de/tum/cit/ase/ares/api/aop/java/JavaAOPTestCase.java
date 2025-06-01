package de.tum.cit.ase.ares.api.aop.java;

//<editor-fold desc="Imports">

import de.tum.cit.ase.ares.api.aop.AOPTestCase;
import de.tum.cit.ase.ares.api.aop.commandSystem.java.JavaCommandSystemExtractor;
import de.tum.cit.ase.ares.api.aop.fileSystem.java.JavaFileSystemExtractor;
import de.tum.cit.ase.ares.api.aop.java.instrumentation.advice.JavaInstrumentationAdviceFileSystemToolbox;
import de.tum.cit.ase.ares.api.aop.java.javaAOPTestCaseToolbox.JavaAOPAdviceSettingTriple;
import de.tum.cit.ase.ares.api.aop.java.javaAOPTestCaseToolbox.JavaAOPTestCaseToolbox;
import de.tum.cit.ase.ares.api.aop.networkSystem.java.JavaNetworkSystemExtractor;
import de.tum.cit.ase.ares.api.aop.threadSystem.java.JavaThreadSystemExtractor;
import de.tum.cit.ase.ares.api.localization.Messages;
import de.tum.cit.ase.ares.api.policy.policySubComponents.ClassPermission;
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
import java.util.function.Supplier;
import java.util.stream.Stream;
//</editor-fold>

/**
 * Configures Java instrumentation based on a security policy.
 * Implements the AOPTestCase interface for managing aspect configurations.
 */
public class JavaAOPTestCase extends AOPTestCase {

    //<editor-fold desc="Attributes">

    /**
     * The supplier for the resource accesses permitted as defined in the security policy.
     */
    @Nonnull
    private final Supplier<List<?>> resourceAccessSupplier;

    /**
     * The list of allowed classes in the restricted package.
     */
    @Nonnull
    private final Set<ClassPermission> allowedClasses;
    //</editor-fold>

    //<editor-fold desc="Constructor">

    /**
     * Initializes the configuration with the given support type and resource accesses.
     *
     * @param javaTestCaseSupported the type of security test case being supported, must not be null.
     * @param resourceAccessSupplier the resource accesses permitted as defined in the security policy, must not be null.
     */
    public JavaAOPTestCase(
            @Nonnull JavaAOPTestCaseSupported javaTestCaseSupported,
            @Nonnull Supplier<List<?>> resourceAccessSupplier,
            @Nonnull Set<ClassPermission> allowedClasses
    ) {
        super(
                javaTestCaseSupported,
                new JavaFileSystemExtractor(resourceAccessSupplier),
                new JavaNetworkSystemExtractor(resourceAccessSupplier),
                new JavaCommandSystemExtractor(resourceAccessSupplier),
                new JavaThreadSystemExtractor(resourceAccessSupplier)
        );
        this.resourceAccessSupplier = resourceAccessSupplier;
        this.allowedClasses = allowedClasses;
    }
    //</editor-fold>

    //<editor-fold desc="Getter">

    @Nonnull
    public Supplier<List<?>> getResourceAccessSupplier() {
        return resourceAccessSupplier;
    }

    @Nonnull
    public Set<ClassPermission> getAllowedClasses() {
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
                        throw new SecurityException(JavaInstrumentationAdviceFileSystemToolbox.localize("security.advice.settings.data.type.unknown", value, dataType, adviceSetting));
            };
        } catch (IllegalFormatException e) {
            throw new SecurityException(JavaInstrumentationAdviceFileSystemToolbox.localize("security.advice.invalid.format", value, dataType, adviceSetting));
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
     * <p>This method accesses the field of the {@code JavaAOPTestCaseSettings} class using reflection
     * and assigns the specified value to it. This allows dynamically setting advice configurations based
     * on the security test case being executed.</p>
     *
     * @param adviceSetting the name of the advice setting field, must not be null.
     * @param value         the value to assign to the advice setting, can be null.
     * @throws SecurityException if there is any error during field access or value assignment.
     */
    public static void setJavaAdviceSettingValue(@Nonnull String adviceSetting, @Nullable Object value, @Nonnull String architectureMode, @Nonnull String aopMode) {
        /*while (!settingsExist()) {
            try {
                TimeUnit.MILLISECONDS.sleep(100);
            } catch (InterruptedException ignored) {}
        }*/
        try {
            @Nullable ClassLoader customClassLoader = Thread.currentThread().getContextClassLoader();
            // Use the current context class loader during tests to ensure the class can be found
            ClassLoader classLoader = customClassLoader != null ? customClassLoader : JavaAOPTestCase.class.getClassLoader();
            @Nonnull Class<?> adviceSettingsClass = Class.forName("de.tum.cit.ase.ares.api.aop.java.JavaAOPTestCaseSettings", true, classLoader);
            @Nonnull Field field = adviceSettingsClass.getDeclaredField(adviceSetting);
            field.setAccessible(true);
            field.set(null, value);
            field.setAccessible(false);
        } catch (LinkageError e) {
            throw new SecurityException(
                    JavaInstrumentationAdviceFileSystemToolbox.localize("security.advice.linkage.exception", adviceSetting),
                    e);
        } catch (ClassNotFoundException e) {
            throw new SecurityException(
                    JavaInstrumentationAdviceFileSystemToolbox.localize("security.advice.class.not.found.exception", adviceSetting),
                    e);
        } catch (NoSuchFieldException e) {
            throw new SecurityException(
                    JavaInstrumentationAdviceFileSystemToolbox.localize("security.advice.no.such.field.exception", adviceSetting),
                    e);
        } catch (NullPointerException e) {
            throw new SecurityException(
                    JavaInstrumentationAdviceFileSystemToolbox.localize("security.advice.null.pointer.exception", adviceSetting),
                    e);
        } catch (IllegalAccessException e) {
            throw new SecurityException(
                    JavaInstrumentationAdviceFileSystemToolbox.localize("security.advice.illegal.access.exception", adviceSetting),
                    e);
        } catch (IllegalArgumentException e) {
            throw new SecurityException(
                    JavaInstrumentationAdviceFileSystemToolbox.localize("security.advice.illegal.argument.exception", adviceSetting, value),
                    e);
        } catch (InaccessibleObjectException e) {
            throw new SecurityException(
                    JavaInstrumentationAdviceFileSystemToolbox.localize("security.advice.inaccessible.object.exception", adviceSetting),
                    e);
        }
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
    public String writeAOPTestCase(@Nonnull String architectureMode, @Nonnull String aopMode) {
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
    public static String writeAOPTestCaseFile(
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
                        new JavaAOPAdviceSettingTriple("String[]", " pathsAllowedToBeRead", JavaFileSystemExtractor.extractPaths(filePermissions, FilePermission::readAllFiles)),
                        new JavaAOPAdviceSettingTriple("String[]", " pathsAllowedToBeOverwritten", JavaFileSystemExtractor.extractPaths(filePermissions, FilePermission::overwriteAllFiles)),
                        new JavaAOPAdviceSettingTriple("String[]", " pathsAllowedToBeExecuted", JavaFileSystemExtractor.extractPaths(filePermissions, FilePermission::executeAllFiles)),
                        new JavaAOPAdviceSettingTriple("String[]", " pathsAllowedToBeDeleted", JavaFileSystemExtractor.extractPaths(filePermissions, FilePermission::deleteAllFiles)),
                        new JavaAOPAdviceSettingTriple("String[]", " hostsAllowedToBeConnectedTo", JavaNetworkSystemExtractor.extractHosts(networkPermissions, NetworkPermission::openConnections)),
                        new JavaAOPAdviceSettingTriple("int[]", " portsAllowedToBeConnectedTo", JavaNetworkSystemExtractor.extractPorts(networkPermissions, NetworkPermission::openConnections)),
                        new JavaAOPAdviceSettingTriple("String[]", " hostsAllowedToBeSentTo", JavaNetworkSystemExtractor.extractHosts(networkPermissions, NetworkPermission::sendData)),
                        new JavaAOPAdviceSettingTriple("int[]", " portsAllowedToBeSentTo", JavaNetworkSystemExtractor.extractPorts(networkPermissions, NetworkPermission::sendData)),
                        new JavaAOPAdviceSettingTriple("String[]", " hostsAllowedToBeReceivedFrom", JavaNetworkSystemExtractor.extractHosts(networkPermissions, NetworkPermission::receiveData)),
                        new JavaAOPAdviceSettingTriple("int[]", " portsAllowedToBeReceivedFrom", JavaNetworkSystemExtractor.extractPorts(networkPermissions, NetworkPermission::receiveData)),
                        new JavaAOPAdviceSettingTriple("String[]", " commandsAllowedToBeExecuted", JavaCommandSystemExtractor.extractCommands(commandPermissions)),
                        new JavaAOPAdviceSettingTriple("String[][]", " argumentsAllowedToBePassed", JavaCommandSystemExtractor.extractArguments(commandPermissions)),
                        new JavaAOPAdviceSettingTriple("int[]", " threadNumberAllowedToBeCreated", JavaThreadSystemExtractor.extractThreadNumbers(threadPermissions)),
                        new JavaAOPAdviceSettingTriple("String[]", " threadClassAllowedToBeCreated", JavaThreadSystemExtractor.extractThreadClasses(threadPermissions))
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
    public void executeAOPTestCase(@Nonnull String architectureMode, @Nonnull String aopMode) {
        switch ((JavaAOPTestCaseSupported) aopTestCaseSupported) {
            case JavaAOPTestCaseSupported.FILESYSTEM_INTERACTION -> Map.of(
                    "pathsAllowedToBeRead", fileSystemExtractor.getPermittedFilePaths("read").toArray(String[]::new),
                    "pathsAllowedToBeOverwritten", fileSystemExtractor.getPermittedFilePaths("overwrite").toArray(String[]::new),
                    "pathsAllowedToBeExecuted", fileSystemExtractor.getPermittedFilePaths("execute").toArray(String[]::new),
                    "pathsAllowedToBeDeleted", fileSystemExtractor.getPermittedFilePaths("delete").toArray(String[]::new)
            ).forEach((k, v) -> JavaAOPTestCase.setJavaAdviceSettingValue(k, v, architectureMode, aopMode));
            case JavaAOPTestCaseSupported.NETWORK_CONNECTION -> Map.of(
                    "hostsAllowedToBeConnectedTo", networkConnectionExtractor.getPermittedNetworkHosts("connect").toArray(String[]::new),
                    "portsAllowedToBeConnectedTo", networkConnectionExtractor.getPermittedNetworkPorts("connect").stream().mapToInt(Integer::intValue).toArray(),
                    "hostsAllowedToBeSentTo", networkConnectionExtractor.getPermittedNetworkHosts("send").toArray(String[]::new),
                    "portsAllowedToBeSentTo", networkConnectionExtractor.getPermittedNetworkPorts("send").stream().mapToInt(Integer::intValue).toArray(),
                    "hostsAllowedToBeReceivedFrom", networkConnectionExtractor.getPermittedNetworkHosts("receive").toArray(String[]::new),
                    "portsAllowedToBeReceivedFrom", networkConnectionExtractor.getPermittedNetworkPorts("receive").stream().mapToInt(Integer::intValue).toArray()
            ).forEach((k, v) -> JavaAOPTestCase.setJavaAdviceSettingValue(k, v, architectureMode, aopMode));
            case JavaAOPTestCaseSupported.COMMAND_EXECUTION -> Map.of(
                    "commandsAllowedToBeExecuted", commandExecutionExtractor.getPermittedCommands().toArray(String[]::new),
                    "argumentsAllowedToBePassed", commandExecutionExtractor.getPermittedArguments().stream().map(innerList -> innerList.toArray(new String[0])).toArray(String[][]::new)
            ).forEach((k, v) -> JavaAOPTestCase.setJavaAdviceSettingValue(k, v, architectureMode, aopMode));
            case JavaAOPTestCaseSupported.THREAD_CREATION -> Map.of(
                    "threadNumberAllowedToBeCreated", threadCreationExtractor.getPermittedNumberOfThreads().stream().mapToInt(Integer::intValue).toArray(),
                    "threadClassAllowedToBeCreated", threadCreationExtractor.getPermittedThreadClasses().toArray(String[]::new)
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
        private Set<ClassPermission> allowedClasses;

        public JavaAOPTestCase.Builder javaAOPTestCaseSupported(JavaAOPTestCaseSupported javaAOPTestCaseSupported) {
            if (javaAOPTestCaseSupported == null) {
                throw new SecurityException(Messages.localized("security.common.not.null", "javaAOPTestCaseSupported"));
            }
            this.javaAOPTestCaseSupported = javaAOPTestCaseSupported;
            return this;
        }

        public JavaAOPTestCase.Builder allowedClasses(Set<ClassPermission> allowedClasses) {
            if (allowedClasses == null) {
                throw new SecurityException(Messages.localized("security.common.not.null", "resourceAccessSupplier"));
            }
            this.allowedClasses = allowedClasses;
            return this;
        }

        public JavaAOPTestCase.Builder resourceAccessSupplier(Supplier<List<?>> resourceAccessSupplier) {
            if (resourceAccessSupplier == null) {
                throw new SecurityException(Messages.localized("security.common.not.null", "resourceAccessSupplier"));
            }
            this.resourceAccessSupplier = resourceAccessSupplier;
            return this;
        }

        public JavaAOPTestCase build() {
            if (javaAOPTestCaseSupported == null) {
                throw new SecurityException(Messages.localized("security.common.not.null", "javaAOPTestCaseSupported", "JavaAOPTestCase.Builder"));
            }
            if (resourceAccessSupplier == null) {
                throw new SecurityException(Messages.localized("security.common.not.null", "resourceAccessSupplier", "JavaAOPTestCase.Builder"));
            }
            if(allowedClasses == null) {
                throw new SecurityException(Messages.localized("security.common.not.null", "allowedClasses", "JavaAOPTestCase.Builder"));
            }
            return new JavaAOPTestCase(javaAOPTestCaseSupported, resourceAccessSupplier, allowedClasses);
        }
    }
    //</editor-fold>
}