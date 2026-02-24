package de.tum.cit.ase.ares.api.phobos;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.function.Supplier;

import javax.annotation.Nonnull;

import com.opencsv.exceptions.CsvException;

import de.tum.cit.ase.ares.api.aop.java.javaAOPModeData.JavaCSVFileLoader;
import de.tum.cit.ase.ares.api.phobos.java.JavaPhobosTestCaseSupported;
import de.tum.cit.ase.ares.api.policy.policySubComponents.FilePermission;
import de.tum.cit.ase.ares.api.policy.policySubComponents.NetworkPermission;
import de.tum.cit.ase.ares.api.policy.policySubComponents.ResourceLimitsPermission;
import de.tum.cit.ase.ares.api.util.FileTools;

/**
 * Phobos is a utility class that provides methods to handle/parse file copying
 * and configuration for the Phobos security test cases.
 * <p>
 * Description: This class contains methods to retrieve file paths,
 * configuration entries, and generate security test case files based on
 * permissions.
 * <p>
 * Design Rationale: The separation of concerns allows for clear organization of
 * file handling and configuration management, facilitating the generation of
 * security test cases.
 *
 * @since 2.0.0
 * @author Ajayvir Singh
 */
public class Phobos {

	private Phobos() {
		throw new IllegalStateException("Utility class should not be instantiated");
	}

	/**
	 * Retrieves the paths of files to be copied based on the copy configuration
	 * entries for Phobos.
	 *
	 * @since 2.0.0
	 * @author Ajayvir Singh
	 * @return the resolved path
	 */
	public static List<Path> filesToCopy() {
		return getCopyConfigurationEntries().stream().map(entry -> entry.get(0).split("/"))
				.map(FileTools::resolveFileOnSourceDirectory).toList();
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
			return (new JavaCSVFileLoader()).loadPhobosCopyData();
		} catch (IOException | CsvException e) {
			throw new RuntimeException("Failed to load Phobos copy configuration from CSV.", e);
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
			return (new JavaCSVFileLoader()).loadPhobosEditData();
		} catch (IOException | CsvException e) {
			throw new RuntimeException("Failed to load Phobos edit configuration from CSV.", e);
		}
	}

	/**
	 * Retrieves the paths where files should be copied to based on the test folder
	 * path and package name.
	 *
	 * @since 2.0.0
	 * @author Ajayvir Singh
	 * @param targetPath the path of the test folder, can be null
	 * @return a list of paths where files should be copied to
	 */
	public static List<Path> targetsToCopyTo(@Nonnull Path targetPath) {
		return getCopyConfigurationEntries().stream().map(entry -> entry.get(2).split("/"))
				.map(path -> FileTools.resolveFileOnTargetDirectory(targetPath, path)).toList();
	}

	/**
	 * Retrieves the file values for a given package name containing file content
	 * with wildcards (e.g. %s).
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
		return getEditConfigurationEntries().stream().map(entry -> entry.get(0).split("/"))
				.map(FileTools::resolveFileOnSourceDirectory).toList().get(0);
	}

	/**
	 * Generates the body of a three-parted file format based on the provided
	 * security policy configuration and test folder path.
	 *
	 * @since 2.0.0
	 * @author Ajayvir Singh
	 * @param javaPhobosTestCases the list of Java AOP test cases
	 * @return a string representing the body of the three-parted file
	 */
	public static String threePartedFileBody(@Nonnull List<JavaPhobosTestCase> javaPhobosTestCases) {
		List<FilePermission> filePermissions = extractPermissions(javaPhobosTestCases,
				JavaPhobosTestCaseSupported.FILESYSTEM_INTERACTION);
		List<NetworkPermission> networkPermissions = extractPermissions(javaPhobosTestCases,
				JavaPhobosTestCaseSupported.NETWORK_CONNECTION);
		List<ResourceLimitsPermission> resourceLimitsPermissions = extractPermissions(javaPhobosTestCases,
				JavaPhobosTestCaseSupported.TIMEOUT);

		return JavaPhobosTestCase.writePhobosSecurityTestCaseFile(filePermissions, networkPermissions,
				resourceLimitsPermissions);
	}

	/**
	 * Extracts permissions from the provided security policy configuration on the
	 * specified supported type.
	 *
	 * @since 2.0.0
	 * @author Ajayvir Singh
	 * @param testCases the list of Java AOP test cases
	 * @param supported the type of supported permissions to extract
	 * @return a list of permissions extracted from the test cases
	 */
	@SuppressWarnings("unchecked")
	private static <T> List<T> extractPermissions(List<JavaPhobosTestCase> testCases,
			JavaPhobosTestCaseSupported supported) {
		return testCases.stream().filter(testCase -> testCase.getPhobosTestCaseSupported() == supported)
				.map(JavaPhobosTestCase::getResourceAccessSupplier).map(Supplier::get)
				.map(permissions -> (List<T>) permissions).flatMap(Collection::stream).toList();
	}

	/**
	 * Provides the footer for a three-parted file format.
	 *
	 * @since 2.0.0
	 * @author Ajayvir Singh
	 * @return the path to the footer file
	 */
	public static Path threePartedFileFooter() {
		return getEditConfigurationEntries().stream().map(entry -> entry.get(1).split("/"))
				.map(FileTools::resolveFileOnSourceDirectory).toList().get(0);
	}

	public static String[] fileValue(@Nonnull String packageName) {
		return FileTools.generatePackageNameArray(packageName, 1);
	}

	/**
	 * Resolves the target path to copy files to based on the test folder path and
	 * package name.
	 *
	 * @since 2.0.0
	 * @author Ajayvir Singh
	 * @param targetPath the path of the test folder, can be null
	 * @return the resolved target path
	 */
	public static Path targetToCopyTo(Path targetPath) {
		return getEditConfigurationEntries().stream().map(entry -> entry.get(2).split("/"))
				.map(path -> FileTools.resolveFileOnTargetDirectory(targetPath, path)).toList().get(0);
	}
}
