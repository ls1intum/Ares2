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

/**
 * Phobos is a utility class that provides methods to handle/parse file copying and configuration
 * for the Phobos security test cases.
 *
 * <p>Description: This class contains methods to retrieve file paths, configuration entries,
 * and generate security test case files based on permissions.
 *
 * <p>Design Rationale: The separation of concerns allows for clear organization of file handling
 * and configuration management, facilitating the generation of security test cases.
 *
 * @since 2.0.0
 * @author Ajayvir Singh
 */
public class Phobos {

    private Phobos() {
        throw new IllegalStateException("Utility class should not be instantiated");
    }

    /**
     * Retrieves the paths of files to be copied based on the copy configuration entries for Phobos.
     *
     * @since 2.0.0
     * @author Ajayvir Singh
     * @return the resolved path
     */
    public static List<Path> filesToCopy() {
        return getCopyConfigurationEntries().stream()
                .map(entry -> entry.getFirst().split("/"))
                .map(FileTools::resolveOnPackage)
                .toList();

    }

    /**
     * Retrieves the copy configuration entries from the JavaCSVFileLoader.
     *
     * @since 2.0.0
     * @author Ajayvir Singh
     * @return a list of lists containing the copy configuration entries
     */
    public static List<List<String>> getCopyConfigurationEntries() {
        try {
            return (new JavaCSVFileLoader()).loadCopyData();
        } catch (IOException | CsvException e) {
            throw new RuntimeException(e);
        }
    }


    /**
     * Retrieves the edit configuration entries from the JavaCSVFileLoader.
     *
     * @since 2.0.0
     * @author Ajayvir Singh
     * @return a list of lists containing the edit configuration entries
     */
    public static List<List<String>> getEditConfigurationEntries() {
        try {
            return (new JavaCSVFileLoader()).loadEditData();
        } catch (IOException | CsvException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Retrieves the paths where files should be copied to based on the test folder path and package name.
     *
     * @since 2.0.0
     * @author Ajayvir Singh
     * @param testFolderPath the path of the test folder, can be null
     * @param packageName    the name of the package
     * @return a list of paths where files should be copied to
     */
    public static List<Path> targetsToCopyTo(
            @Nullable Path testFolderPath,
            @Nonnull String packageName
    ) {
        return getCopyConfigurationEntries().stream()
                .map(entry -> entry.get(2).split("/"))
                .map(FileTools::resolveOnPackage)
                .toList();
    }

    /**
     * Retrieves the file values for a given package name containing file content with wildcards (e.g. %s).
     *
     *
     * @since 2.0.0
     * @author Ajayvir Singh
     * @param packageName the name of the package
     * @return a list of string arrays containing file values
     */
    public static List<String[]> fileValues(@Nonnull String packageName) {
        return List.of();
    }

    /**
     * Provides the header for a three-parted file format.
     *
     * @since 2.0.0
     * @author Ajayvir Singh
     * @return the path to the header file
     */
    public static Path threePartedFileHeader() {
        return Path.of("");
    }

    /**
     * Generates the body of a three-parted file format based on the provided security policy configuration and test folder path.
     *
     * @since 2.0.0
     * @author Ajayvir Singh
     * @param javaAOPTestCases the list of Java AOP test cases
     * @param testFolderPath   the path to the test folder, can be null
     * @return a string representing the body of the three-parted file
     */
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


    /**
     * Extracts permissions from the provided security policy configuration on the specified supported type.
     *
     * @since 2.0.0
     * @author Ajayvir Singh
     * @param testCases the list of Java AOP test cases
     * @param supported the type of supported permissions to extract
     * @return a list of permissions extracted from the test cases
     */
    private static <T> List<T> extractPermissions(List<JavaAOPTestCase> testCases, JavaAOPTestCaseSupported supported) {
        return testCases.stream()
                .filter(testCase -> testCase.getAopTestCaseSupported() == supported)
                .map(JavaAOPTestCase::getResourceAccessSupplier)
                .map(Supplier::get)
                .map(permissions -> (List<T>) permissions)
                .flatMap(Collection::stream)
                .toList();
    }

    /**
     * Provides the footer for a three-parted file format.
     *
     * @since 2.0.0
     * @author Ajayvir Singh
     * @return the path to the footer file
     */
    public static Path threePartedFileFooter() {
        return Path.of("");
    }


    public static String[] fileValue(@Nonnull String packageName) {
        return FileTools.generatePackageNameArray(packageName, 1);
    }

    /**
     * Resolves the target path to copy files to based on the test folder path and package name.
     *
     * @since 2.0.0
     * @author Ajayvir Singh
     * @param testFolderPath the path of the test folder, can be null
     * @param packageName    the name of the package
     * @return the resolved target path
     */
    public static Path targetToCopyTo(Path testFolderPath, String packageName) {
        return getEditConfigurationEntries().stream().map(entry -> entry.get(2).split("/"))
                .map(FileTools::resolveOnPackage)
                .toList().getFirst();
    }
}
