package de.tum.cit.ase.ares.api.aop;

import com.google.common.base.Preconditions;
import de.tum.cit.ase.ares.api.aop.commandExecution.CommandExecutionExtractor;
import de.tum.cit.ase.ares.api.aop.fileSystem.FileSystemExtractor;
import de.tum.cit.ase.ares.api.aop.networkConnection.NetworkConnectionExtractor;
import de.tum.cit.ase.ares.api.aop.threadCreation.ThreadCreationExtractor;
import de.tum.cit.ase.ares.api.architecture.ArchitectureTestCaseSupported;

import javax.annotation.Nonnull;

/**
 * Interface for AOP test case configurations.
 *
 * <p>Description: Defines methods for generating and executing aspect configuration files that enforce security via aspect-oriented programming.</p>
 *
 * <p>Design Rationale: Abstracting AOP configurations into a unified interface allows for consistent integration and language-specific implementation of security measures.</p>
 *
 * @since 2.0.0
 * @author Markus Paulsen
 * @version 2.0.0
 */
public abstract class AOPTestCase {

    //<editor-fold desc="Attributes">
    /**
     * Defines the type of aop test case used in this test case.
     * Determines which aop rules and validations will be applied during analysis of this test case.
     */
    @Nonnull
    protected final AOPTestCaseSupported aopTestCaseSupported;

    @Nonnull
    protected final FileSystemExtractor fileSystemExtractor;

    @Nonnull
    protected final NetworkConnectionExtractor networkConnectionExtractor;

    @Nonnull
    protected final CommandExecutionExtractor commandExecutionExtractor;

    @Nonnull
    protected final ThreadCreationExtractor threadCreationExtractor;
    //</editor-fold>

    //<editor-fold desc="Constructor">
    /**
     * Constructs a new abstract aop test case with the specified parameters.
     *
     * @since 2.0.0
     * @author Markus Paulsen
     * @param aopTestCaseSupported The type of aop test case supported, determining which rules to apply
     */
    protected AOPTestCase(
            @Nonnull AOPTestCaseSupported aopTestCaseSupported,
            @Nonnull FileSystemExtractor fileSystemExtractor,
            @Nonnull NetworkConnectionExtractor networkConnectionExtractor,
            @Nonnull CommandExecutionExtractor commandExecutionExtractor,
            @Nonnull ThreadCreationExtractor threadCreationExtractor
    ) {
        this.aopTestCaseSupported = Preconditions.checkNotNull(aopTestCaseSupported, "aopTestCaseSupported must not be null");
        this.fileSystemExtractor = Preconditions.checkNotNull(fileSystemExtractor, "fileSystemExtractor must not be null");
        this.networkConnectionExtractor = Preconditions.checkNotNull(networkConnectionExtractor, "networkConnectionExtractor must not be null");
        this.commandExecutionExtractor = Preconditions.checkNotNull(commandExecutionExtractor, "commandExecutionExtractor must not be null");
        this.threadCreationExtractor = Preconditions.checkNotNull(threadCreationExtractor, "threadCreationExtractor must not be null");
    }
    //</editor-fold>

    //<editor-fold desc="Abstract Methods">
    /**
     * Writes the content of the AOP test case configuration for the specified AOP mode.
     *
     * @since 2.0.0
     * @author Markus Paulsen
     * @param aopMode the identifier for the AOP mode.
     * @return the AOP test case configuration content as a string.
     */
    @Nonnull
    public abstract String writeAOPSecurityTestCase(@Nonnull String architectureMode, @Nonnull String aopMode);

    /**
     * Executes the AOP test case using the provided architecture mode and AOP mode.
     *
     * @since 2.0.0
     * @author Markus Paulsen
     * @param architectureMode the identifier for the architecture mode.
     * @param aopMode the identifier for the AOP mode.
     */
    public abstract void executeAOPSecurityTestCase(@Nonnull String architectureMode, @Nonnull String aopMode);
    //</editor-fold>
}