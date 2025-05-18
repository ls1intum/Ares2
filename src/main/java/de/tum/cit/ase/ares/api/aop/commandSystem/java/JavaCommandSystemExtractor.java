package de.tum.cit.ase.ares.api.aop.commandSystem.java;

import de.tum.cit.ase.ares.api.aop.commandSystem.CommandSystemExtractor;
import de.tum.cit.ase.ares.api.policy.policySubComponents.CommandPermission;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.function.Supplier;

public class JavaCommandSystemExtractor implements CommandSystemExtractor {

    /**
     * The supplier for the resource accesses permitted as defined in the security policy.
     */
    @Nonnull
    private final Supplier<List<?>> resourceAccessSupplier;

    /**
     * Constructs a new JavaCommandExecutionExtractor with the specified resource access supplier.
     *
     * @param resourceAccessSupplier the supplier for the resource accesses permitted as defined in the security policy, must not be null.
     */
    public JavaCommandSystemExtractor(@Nonnull Supplier<List<?>> resourceAccessSupplier) {
        this.resourceAccessSupplier = resourceAccessSupplier;
    }

    //<editor-fold desc="Command Execution related methods">

    /**
     * Extracts the permitted commands from the provided configurations.
     *
     * @param configs the list of JavaSecurityTestCase configurations, must not be null.
     * @return a list of permitted commands.
     */
    @Nonnull
    public static List<String> extractCommands(@Nonnull List<CommandPermission> configs) {
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
    public static List<String> extractArguments(@Nonnull List<CommandPermission> configs) {
        return configs.stream()
                .map(CommandPermission::withTheseArguments)
                .map(arguments -> arguments.stream().map(String::valueOf).map(value -> "\"" + value + "\"").toList())
                .map(arguments -> "new String[] {" + String.join(",", arguments) + "}")
                .toList();
    }

    /**
     * Retrieves the list of commands that are permitted to be executed.
     *
     * @return a list of permitted commands, must not be null.
     */
    @Nonnull
    public List<String> getPermittedCommands() {
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
    public List<List<String>> getPermittedArguments() {
        return ((List<CommandPermission>) resourceAccessSupplier.get())
                .stream()
                .map(CommandPermission::withTheseArguments)
                .toList();
    }
    //</editor-fold>
}
