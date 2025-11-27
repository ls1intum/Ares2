package de.tum.cit.ase.ares.api.phobos;

import com.google.common.base.Preconditions;
import de.tum.cit.ase.ares.api.aop.AOPTestCaseSupported;
import de.tum.cit.ase.ares.api.aop.commandSystem.CommandSystemExtractor;
import de.tum.cit.ase.ares.api.aop.fileSystem.FileSystemExtractor;
import de.tum.cit.ase.ares.api.aop.networkSystem.NetworkSystemExtractor;
import de.tum.cit.ase.ares.api.aop.resourceLimits.ResourceLimitsExtractor;
import de.tum.cit.ase.ares.api.aop.threadSystem.ThreadSystemExtractor;

import javax.annotation.Nonnull;

public abstract class PhobosTestCase {
    //<editor-fold desc="Attributes">
    /**
     * Defines the type of phobos test case used in this test case.
     * Determines which phobos rules and validations will be applied during analysis of this test case.
     */
    @Nonnull
    protected final PhobosTestCaseSupported phobosTestCaseSupported;

    @Nonnull
    protected final FileSystemExtractor fileSystemExtractor;

    @Nonnull
    protected final NetworkSystemExtractor networkConnectionExtractor;

    @Nonnull
    protected final ResourceLimitsExtractor resourceLimitsExtractor;

    //</editor-fold>

    //<editor-fold desc="Constructor">
    /**
     * Constructs a new abstract Phobos test case with the specified parameters.
     *
     * @since 2.0.0
     * @author Markus Paulsen
     * @param phobosTestCaseSupported The type of phobos test case supported, determining which rules to apply
     * @param fileSystemExtractor The extractor for file system operations
     * @param networkConnectionExtractor The extractor for network system operations
     * @param resourceLimitsExtractor The extractor for resource limits such as timeouts and memory limits
     */
    protected PhobosTestCase(
            @Nonnull PhobosTestCaseSupported phobosTestCaseSupported,
            @Nonnull FileSystemExtractor fileSystemExtractor,
            @Nonnull NetworkSystemExtractor networkConnectionExtractor,
            @Nonnull ResourceLimitsExtractor resourceLimitsExtractor
    ) {
        this.phobosTestCaseSupported = Preconditions.checkNotNull(phobosTestCaseSupported, "phobosTestCaseSupported must not be null");
        this.fileSystemExtractor = Preconditions.checkNotNull(fileSystemExtractor, "fileSystemExtractor must not be null");
        this.networkConnectionExtractor = Preconditions.checkNotNull(networkConnectionExtractor, "networkConnectionExtractor must not be null");
        this.resourceLimitsExtractor = Preconditions.checkNotNull(resourceLimitsExtractor, "resourceLimitsExtractor must not be null");
    }
    //</editor-fold>


    /**
     * Writes the content of the Phobos test cases for phobos.
     *
     * @since 2.0.1
     * @author Ajayvir Singh
     * @return the Phobos test case content as a string.
     */
    @Nonnull
    public abstract String writePhobosTestCase();

    //<editor-fold desc="Getter methods">
    @Nonnull
    public PhobosTestCaseSupported getPhobosTestCaseSupported() {
        return phobosTestCaseSupported;
    }

    @Nonnull
    public FileSystemExtractor getFileSystemExtractor() {
        return fileSystemExtractor;
    }

    @Nonnull
    public NetworkSystemExtractor getNetworkConnectionExtractor() {
        return networkConnectionExtractor;
    }

    @Nonnull
    public ResourceLimitsExtractor getResourceLimitsExtractor() {
        return resourceLimitsExtractor;
    }
    //</editor-fold>

}
