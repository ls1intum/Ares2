package de.tum.cit.ase.ares.api.aop.threadCreation;

import javax.annotation.Nonnull;
import java.util.List;

public interface ThreadCreationExtractor {

    /**
     * Retrieves the list of permitted thread counts based on the security policy.
     *
     * @return a list of permitted thread numbers, must not be null.
     */
    @Nonnull
    public abstract List<Integer> getPermittedNumberOfThreads();

    /**
     * Retrieves the list of permitted thread classes based on the security policy.
     *
     * @return a list of permitted thread classes, must not be null.
     */
    @Nonnull
    public abstract List<String> getPermittedThreadClasses();
}
