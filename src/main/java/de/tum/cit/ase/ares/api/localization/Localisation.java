package de.tum.cit.ase.ares.api.localization;

import com.opencsv.exceptions.CsvException;
import de.tum.cit.ase.ares.api.aop.java.javaAOPModeData.JavaCSVFileLoader;
import de.tum.cit.ase.ares.api.phobos.JavaPhobosTestCase;
import de.tum.cit.ase.ares.api.phobos.java.JavaPhobosTestCaseSupported;
import de.tum.cit.ase.ares.api.policy.policySubComponents.FilePermission;
import de.tum.cit.ase.ares.api.policy.policySubComponents.NetworkPermission;
import de.tum.cit.ase.ares.api.policy.policySubComponents.ResourceLimitsPermission;
import de.tum.cit.ase.ares.api.util.FileTools;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.function.Supplier;

public class Localisation {

    private Localisation() {
        throw new IllegalStateException("Utility class should not be instantiated");
    }

    public static List<Path> filesToCopy() {
        return getCopyConfigurationEntries().stream()
                .map(entry -> entry.getFirst().split("/"))
                .map(FileTools::resolveFileOnSourceDirectory)
                .toList();

    }

    public static List<List<String>> getCopyConfigurationEntries() {
        try {
            return (new JavaCSVFileLoader()).loadLocalisationCopyData();
        } catch (IOException | CsvException e) {
            throw new RuntimeException(e);
        }
    }

    public static List<Path> targetsToCopyTo(
            @Nonnull Path targetPath
    ) {
        return getCopyConfigurationEntries().stream()
                .map(entry -> entry.get(2).split("/"))
                .map(path -> FileTools.resolveFileOnTargetDirectory(targetPath, path))
                .toList();
    }
}
