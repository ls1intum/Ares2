package de.tum.cit.ase.ares.api.aop;

import static de.tum.cit.ase.ares.api.aop.java.instrumentation.advice.JavaInstrumentationAdviceAbstractToolbox.localize;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.function.Supplier;

import javax.annotation.Nonnull;

import com.opencsv.exceptions.CsvException;

import de.tum.cit.ase.ares.api.aop.java.JavaAOPTestCase;
import de.tum.cit.ase.ares.api.aop.java.JavaAOPTestCaseSupported;
import de.tum.cit.ase.ares.api.aop.java.javaAOPModeData.JavaCSVFileLoader;
import de.tum.cit.ase.ares.api.aop.java.javaAOPModeData.JavaFileLoader;
import de.tum.cit.ase.ares.api.policy.policySubComponents.CommandPermission;
import de.tum.cit.ase.ares.api.policy.policySubComponents.FilePermission;
import de.tum.cit.ase.ares.api.policy.policySubComponents.NetworkPermission;
import de.tum.cit.ase.ares.api.policy.policySubComponents.ThreadPermission;
import de.tum.cit.ase.ares.api.util.FileTools;

/**
 * Enum representing the AOP modes for Java security test cases.
 * <p>
 * Description: Provides different modes for AOP test case generation and
 * execution in Java. The modes determine how files and settings are copied and
 * resolved based on the underlying AOP tool.
 * </p>
 * <p>
 * Design Rationale: Using an enum to represent AOP modes centralises
 * configuration and enables future extensions (e.g. supporting INSTRUMENTATION)
 * while ensuring that file handling and resource resolution are consistently
 * applied.
 * </p>
 *
 * @since 2.0.0
 * @author Markus Paulsen
 * @version 2.0.0
 */
public enum AOPMode {

	/**
	 * Instrumentation mode using Java Instrumentation for load-time weaving.
	 */
	INSTRUMENTATION,

	/**
	 * AspectJ mode using AspectJ for compile-time weaving.
	 */
	ASPECTJ;

	private static JavaFileLoader fileLoader = new JavaCSVFileLoader();

	public static void setFileLoader(JavaFileLoader loader) {
		fileLoader = java.util.Objects.requireNonNull(loader, "loader must not be null");
	}

	// <editor-fold desc="Load configuration">
	public List<List<String>> getCopyFSConfigurationEntries() {
		try {
			List<List<String>> entries = fileLoader.loadCopyData(this, true);
			validateConfigurationRows(entries, 3, "copy.fs", false);
			return entries;
		} catch (IOException | CsvException e) {
			throw new SecurityException(localize("security.aop.mode.configuration.copy.fs.load.failure", name()), e);
		}
	}

	public List<List<String>> getCopyNonFSConfigurationEntries() {
		try {
			List<List<String>> entries = fileLoader.loadCopyData(this, false);
			validateConfigurationRows(entries, 3, "copy.nonfs", false);
			return entries;
		} catch (IOException | CsvException e) {
			throw new SecurityException(localize("security.aop.mode.configuration.copy.nonfs.load.failure", name()), e);
		}
	}

	public List<List<String>> getEditConfigurationEntries() {
		try {
			List<List<String>> entries = fileLoader.loadEditData(this);
			// Edit entries are also consumed positionally via .get(0) on the whole list by
			// the single-file methods, so the list must be non-empty as well.
			validateConfigurationRows(entries, 3, "edit", true);
			return entries;
		} catch (IOException | CsvException e) {
			throw new SecurityException(localize("security.aop.mode.configuration.edit.load.failure", name()), e);
		}
	}

	/**
	 * Validates the shape of loaded configuration rows so a malformed or short CSV
	 * fails with a clear, localized error instead of an opaque
	 * {@link IndexOutOfBoundsException} or {@link NumberFormatException} deep in
	 * the positional accessors below.
	 *
	 * @param entries         the loaded rows
	 * @param minColumns      the minimum number of columns each row must have
	 * @param context         a short label identifying the configuration kind
	 * @param requireNonEmpty whether the list itself must contain at least one row
	 */
	/**
	 * Upper bound for the copy-configuration count column (the package-depth used
	 * by {@code generatePackageNameArray}). A value beyond this is treated as
	 * malformed rather than allocating an absurd number of path segments.
	 */
	private static final int MAX_CONFIGURATION_COUNT = 100;

	private void validateConfigurationRows(@Nonnull List<List<String>> entries, int minColumns, @Nonnull String context,
			boolean requireNonEmpty) {
		if (requireNonEmpty && entries.isEmpty()) {
			throw new SecurityException(localize("security.aop.mode.configuration.empty", context, name()));
		}
		// Copy configurations ("copy.fs"/"copy.nonfs") carry a numeric count in column
		// 1
		// that is later Integer.parseInt-ed by placeholderValues()/fsFormatValues();
		// edit
		// configurations carry a path there instead, so the numeric check is copy-only.
		boolean validateCountColumn = context.startsWith("copy");
		for (int row = 0; row < entries.size(); row++) {
			List<String> entry = entries.get(row);
			if (entry == null || entry.size() < minColumns) {
				throw new SecurityException(localize("security.aop.mode.configuration.malformed.row", context, row,
						name(), minColumns, entry == null ? 0 : entry.size()));
			}
			// Reject blank required cells: a blank source/target path or count would
			// otherwise surface much later as an opaque NumberFormatException or a write to
			// a malformed path during test-case generation.
			for (int column = 0; column < minColumns; column++) {
				String cell = entry.get(column);
				if (cell == null || cell.isBlank()) {
					throw new SecurityException(
							localize("security.aop.mode.configuration.blank.cell", context, row, column, name()));
				}
			}
			if (validateCountColumn) {
				String countCell = entry.get(1).trim();
				int count;
				try {
					count = Integer.parseInt(countCell);
				} catch (NumberFormatException notAnInteger) {
					throw new SecurityException(
							localize("security.aop.mode.configuration.invalid.count", context, row, name(), countCell),
							notAnInteger);
				}
				if (count < 0 || count > MAX_CONFIGURATION_COUNT) {
					throw new SecurityException(
							localize("security.aop.mode.configuration.invalid.count", context, row, name(), countCell));
				}
			}
		}
	}
	// </editor-fold>

	// <editor-fold desc="Multi-file methods">

	@Nonnull
	public List<Path> fsFilesToCopy() {
		return getCopyFSConfigurationEntries().stream().map(entry -> entry.get(0).split("/"))
				.map(FileTools::resolveFileOnSourceDirectory).toList();
	}

	@Nonnull
	public List<Path> nonFSFilesToCopy() {
		return getCopyNonFSConfigurationEntries().stream().map(entry -> entry.get(0).split("/"))
				.map(FileTools::resolveFileOnSourceDirectory).toList();
	}

	@Nonnull
	public List<String[]> placeholderValues() {
		return getCopyNonFSConfigurationEntries().stream().map(entry -> entry.get(1)).map(Integer::parseInt)
				.map(entry -> switch (entry) {
				case 0 -> new String[] { "de.tum.cit.ase", "de.tum.cit.ase", "Main" };
				default -> FileTools.generatePackageNameArray("de.tum.cit.ase", entry);
				}).toList();
	}

	@Nonnull
	public List<String[]> fsFormatValues(@Nonnull String packageName, @Nonnull String mainClassInPackageName) {
		return getCopyFSConfigurationEntries().stream().map(entry -> entry.get(1)).map(Integer::parseInt)
				.map(entry -> switch (entry) {
				case 0 -> new String[] { packageName, packageName, mainClassInPackageName };
				default -> FileTools.generatePackageNameArray(packageName, entry);
				}).toList();
	}

	@Nonnull
	public List<String[]> nonFSFormatValues(@Nonnull String packageName, @Nonnull String mainClassInPackageName) {
		return getCopyNonFSConfigurationEntries().stream().map(entry -> entry.get(1)).map(Integer::parseInt)
				.map(entry -> switch (entry) {
				case 0 -> new String[] { packageName, packageName, mainClassInPackageName };
				default -> FileTools.generatePackageNameArray(packageName, entry);
				}).toList();
	}

	@Nonnull
	public List<Path> fsTargetsToCopyTo(@Nonnull Path targetPath) {
		return getCopyFSConfigurationEntries().stream().map(entry -> entry.get(2).split("/"))
				.map(path -> FileTools.resolveFileOnTargetDirectory(targetPath, path)).toList();
	}

	@Nonnull
	public List<Path> nonFSTargetsToCopyTo(@Nonnull Path targetPath) {
		return getCopyNonFSConfigurationEntries().stream().map(entry -> entry.get(2).split("/"))
				.map(path -> FileTools.resolveFileOnTargetDirectory(targetPath, path)).toList();
	}
	// </editor-fold>

	// <editor-fold desc="Single-file methods">

	/**
	 * Retrieves the path to the file header template for the three-parted file.
	 *
	 * @since 2.0.0
	 * @author Markus Paulsen
	 * @return the path to the file header template.
	 */
	@Nonnull
	public Path threePartedFileHeader() {
		return getEditConfigurationEntries().stream().map(entry -> entry.get(0).split("/"))
				.map(FileTools::resolveFileOnSourceDirectory).toList().get(0);
	}

	/**
	 * Generates the body content for the three-parted security test case file.
	 *
	 * @since 2.0.0
	 * @author Markus Paulsen
	 * @param aopMode              the AOP mode identifier.
	 * @param restrictedPackage    the package being restricted.
	 * @param allowedListedClasses the list of allowed classes.
	 * @param javaAOPTestCases     the list of security test cases.
	 * @return a string representing the body content.
	 */
	@Nonnull
	public String threePartedFileBody(@Nonnull String aopMode, @Nonnull String restrictedPackage,
			@Nonnull List<String> allowedListedClasses, @Nonnull List<JavaAOPTestCase> javaAOPTestCases) {
		List<FilePermission> filePermissions = extractPermissions(javaAOPTestCases,
				JavaAOPTestCaseSupported.FILESYSTEM_INTERACTION);
		List<NetworkPermission> networkPermissions = extractPermissions(javaAOPTestCases,
				JavaAOPTestCaseSupported.NETWORK_CONNECTION);
		List<CommandPermission> commandPermissions = extractPermissions(javaAOPTestCases,
				JavaAOPTestCaseSupported.COMMAND_EXECUTION);
		List<ThreadPermission> threadPermissions = extractPermissions(javaAOPTestCases,
				JavaAOPTestCaseSupported.THREAD_CREATION);

		return JavaAOPTestCase.writeAOPTestCaseFile(aopMode, restrictedPackage, allowedListedClasses, filePermissions,
				networkPermissions, commandPermissions, threadPermissions);
	}

	/**
	 * Extracts and flattens the permission lists for the given supported type.
	 *
	 * @param <T>       the type of permission.
	 * @param testCases the list of JavaAOPTestCase.
	 * @param supported the JavaAOPTestCaseSupported filter.
	 * @return a flattened list of permissions of type T.
	 */
	@SuppressWarnings("unchecked")
	private <T> List<T> extractPermissions(List<JavaAOPTestCase> testCases, JavaAOPTestCaseSupported supported) {
		return testCases.stream().filter(testCase -> testCase.getAopTestCaseSupported() == supported)
				.map(JavaAOPTestCase::getResourceAccessSupplier).map(Supplier::get)
				.map(permissions -> (List<T>) permissions).flatMap(Collection::stream).toList();
	}

	/**
	 * Retrieves the path to the file footer template for the three-parted file.
	 *
	 * @since 2.0.0
	 * @author Markus Paulsen
	 * @return the path to the file footer template.
	 */
	@Nonnull
	public Path threePartedFileFooter() {
		return getEditConfigurationEntries().stream().map(entry -> entry.get(1).split("/"))
				.map(FileTools::resolveFileOnSourceDirectory).toList().get(0);
	}

	/**
	 * Generates the file value array based on the provided package name.
	 *
	 * @since 2.0.0
	 * @author Markus Paulsen
	 * @param packageName the base package name.
	 * @return an array of strings representing the file value.
	 */
	@Nonnull
	public String[] formatValues(@Nonnull String packageName) {
		return switch (this) {
		case ASPECTJ -> FileTools.generatePackageNameArray(packageName, 1);
		case INSTRUMENTATION -> FileTools.generatePackageNameArray(packageName, 1);
		};
	}

	/**
	 * Determines the target path for copying the main AOP test case file based on
	 * the project path and package name.
	 *
	 * @since 2.0.0
	 * @author Markus Paulsen
	 * @param targetPath the project path.
	 * @return the target path for the main AOP test case file.
	 */
	@Nonnull
	public Path targetToCopyTo(@Nonnull Path targetPath) {
		return getEditConfigurationEntries().stream().map(entry -> entry.get(2).split("/"))
				.map(path -> FileTools.resolveFileOnTargetDirectory(targetPath, path)).toList().get(0);
	}
	// </editor-fold>

	// <editor-fold desc="Reset methods">

	/**
	 * Resets the AOP test case settings by invoking the reset method using
	 * reflection.
	 *
	 * @since 2.0.0
	 * @author Markus Paulsen
	 */
	public void reset() {
		try {
			Class<?> settingsBootstrapClass = Class.forName("de.tum.cit.ase.ares.api.aop.java.JavaAOPTestCaseSettings",
					true, null);
			Method bootstrapMethod = settingsBootstrapClass.getDeclaredMethod("reset");
			bootstrapMethod.setAccessible(true);
			bootstrapMethod.invoke(null);
			bootstrapMethod.setAccessible(false);

			ClassLoader customClassLoader = Thread.currentThread().getContextClassLoader();
			Class<?> settingsClassloaderClass = Class
					.forName("de.tum.cit.ase.ares.api.aop.java.JavaAOPTestCaseSettings", true, customClassLoader);
			Method classloaderMethod = settingsClassloaderClass.getDeclaredMethod("reset");
			classloaderMethod.setAccessible(true);
			classloaderMethod.invoke(null);
			classloaderMethod.setAccessible(false);
		} catch (ClassNotFoundException e) {
			throw new SecurityException(localize("security.creation.reset.class.not.found.exception"), e);
		} catch (NoSuchMethodException e) {
			throw new SecurityException(localize("security.creation.reset.no.method.exception"), e);
		} catch (IllegalAccessException e) {
			throw new SecurityException(localize("security.creation.reset.illegal.access.exception"), e);
		} catch (InvocationTargetException e) {
			throw new SecurityException(localize("security.creation.reset.invocation.target.exception"), e);
		}
	}
	// </editor-fold>
}
