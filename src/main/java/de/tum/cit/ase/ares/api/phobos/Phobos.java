package de.tum.cit.ase.ares.api.phobos;

import com.opencsv.exceptions.CsvException;
import de.tum.cit.ase.ares.api.aop.java.JavaAOPTestCase;
import de.tum.cit.ase.ares.api.aop.java.JavaAOPTestCaseSupported;
import de.tum.cit.ase.ares.api.aop.java.javaAOPModeData.JavaCSVFileLoader;
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


public class Phobos {

    private Phobos() {
        throw new IllegalStateException("Utility class should not be instantiated");
    }

    public static List<Path> filesToCopy() {
        return getCopyConfigurationEntries().stream()
                .map(entry -> entry.getFirst().split("/"))
                .map(FileTools::resolveOnPackage)
                .toList();

    }

    public static List<List<String>> getCopyConfigurationEntries() {
        try {
            return (new JavaCSVFileLoader()).loadCopyData();
        } catch (IOException | CsvException e) {
            throw new RuntimeException(e);
        }
    }


    public static List<List<String>> getEditConfigurationEntries() {
        try {
            return (new JavaCSVFileLoader()).loadEditData();
        } catch (IOException | CsvException e) {
            throw new RuntimeException(e);
        }
    }

    public static List<Path> targetsToCopyTo(
            @Nullable Path testFolderPath,
            @Nonnull String packageName
    ) {
        return getCopyConfigurationEntries().stream()
                .map(entry -> entry.get(2).split("/"))
                .map(FileTools::resolveOnPackage)
                .toList();
    }

    public static List<String[]> fileValues(@Nonnull String packageName) {
        return List.of();
    }

    public static Path threePartedFileHeader() {
        return Path.of("");
    }

    public static String threePartedFileBody(
            @Nonnull List<JavaAOPTestCase> javaAOPTestCases,
            @Nullable Path testFolderPath
    ) {
        List<FilePermission> filePermissions =
                extractPermissions(javaAOPTestCases, JavaAOPTestCaseSupported.FILESYSTEM_INTERACTION);
        List<NetworkPermission> networkPermissions =
                extractPermissions(javaAOPTestCases, JavaAOPTestCaseSupported.NETWORK_CONNECTION);
        List<ResourceLimitsPermission> resourceLimitsPermissions = extractPermissions(javaAOPTestCases, JavaAOPTestCaseSupported.TIMEOUT);

        return JavaPhobosTestCase.writePhobosSecurityTestCaseFile(filePermissions, networkPermissions, resourceLimitsPermissions);

    }

    private static <T> List<T> extractPermissions(List<JavaAOPTestCase> testCases, JavaAOPTestCaseSupported supported) {
        return testCases.stream()
                .filter(testCase -> testCase.getAopTestCaseSupported() == supported)
                .map(JavaAOPTestCase::getResourceAccessSupplier)
                .map(Supplier::get)
                .map(permissions -> (List<T>) permissions)
                .flatMap(Collection::stream)
                .toList();
    }

    public static Path threePartedFileFooter() {
        return Path.of("");
    }


    public static String[] fileValue(@Nonnull String packageName) {
        return FileTools.generatePackageNameArray(packageName, 1);
    }

    public static Path targetToCopyTo(Path testFolderPath, String packageName) {
        return getEditConfigurationEntries().stream().map(entry -> entry.get(2).split("/"))
                .map(FileTools::resolveOnPackage)
                .toList().getFirst();
    }
}
