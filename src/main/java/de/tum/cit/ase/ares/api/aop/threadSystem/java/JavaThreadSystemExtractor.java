package de.tum.cit.ase.ares.api.aop.threadSystem.java;

import de.tum.cit.ase.ares.api.aop.threadSystem.ThreadSystemExtractor;
import de.tum.cit.ase.ares.api.policy.policySubComponents.ThreadPermission;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.function.Supplier;

public class JavaThreadSystemExtractor implements ThreadSystemExtractor {

    /**
     * The supplier for the resource accesses permitted as defined in the security policy.
     */
    @Nonnull
    private final Supplier<List<?>> resourceAccessSupplier;

    /**
     * Constructs a new JavaThreadCreationExtractor with the specified resource access supplier.
     *
     * @param resourceAccessSupplier the supplier for the resource accesses permitted as defined in the security policy, must not be null.
     */
    public JavaThreadSystemExtractor(@Nonnull Supplier<List<?>> resourceAccessSupplier) {
        this.resourceAccessSupplier = resourceAccessSupplier;
    }

    //<editor-fold desc="Thread Creation related methods">

    /**
     * Retrieves the list of permitted thread counts based on the security policy.
     *
     * @param configs the list of JavaTestCase configurations, must not be null.
     * @return a list of permitted thread numbers.
     */
    @Nonnull
    public static List<String> extractThreadNumbers(@Nonnull List<ThreadPermission> configs) {
        return configs.stream()
                .map(ThreadPermission::createTheFollowingNumberOfThreads)
                .map(String::valueOf)
                .toList();
    }

    /**
     * Retrieves the list of permitted thread classes based on the security policy.
     *
     * @param configs the list of JavaTestCase configurations, must not be null.
     * @return a list of permitted thread classes.
     */
    @Nonnull
    public static List<String> extractThreadClasses(@Nonnull List<ThreadPermission> configs) {
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
    public List<Integer> getPermittedNumberOfThreads() {
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
    public List<String> getPermittedThreadClasses() {
        return ((List<ThreadPermission>) resourceAccessSupplier.get())
                .stream()
                .map(ThreadPermission::ofThisClass)
                .toList();
    }
    //</editor-fold>
}
