package de.tum.cit.ase.ares.api.aop.fileSystem.java;

import java.util.List;
import java.util.function.Predicate;
import java.util.function.Supplier;

import javax.annotation.Nonnull;

import de.tum.cit.ase.ares.api.aop.fileSystem.FileSystemExtractor;
import de.tum.cit.ase.ares.api.localization.Messages;
import de.tum.cit.ase.ares.api.policy.policySubComponents.FilePermission;

public class JavaFileSystemExtractor implements FileSystemExtractor {

	/**
	 * The supplier for the resource accesses permitted as defined in the security
	 * policy.
	 */
	@Nonnull
	private final Supplier<List<?>> resourceAccessSupplier;

	/**
	 * Constructs a new JavaFileSystemExtractor with the specified resource access
	 * supplier.
	 *
	 * @param resourceAccessSupplier the supplier for the resource accesses
	 *                               permitted as defined in the security policy,
	 *                               must not be null.
	 */
	public JavaFileSystemExtractor(@Nonnull Supplier<List<?>> resourceAccessSupplier) {
		this.resourceAccessSupplier = resourceAccessSupplier;
	}

	// <editor-fold desc="File System Interactions related methods">

	/**
	 * Extracts the permitted file paths from the provided configurations based on
	 * the given predicate.
	 *
	 * @param configs   the list of JavaTestCase configurations, must not be null.
	 * @param predicate a filter for determining which paths are permitted, must not
	 *                  be null.
	 * @return a list of permitted paths.
	 */
	@Nonnull
	public static List<String> extractPaths(@Nonnull List<FilePermission> configs,
			@Nonnull Predicate<FilePermission> predicate) {
		return configs.stream().filter(predicate).map(FilePermission::onThisPathAndAllPathsBelow).toList();
	}

	/**
	 * Retrieves the list of file paths that are permitted for the given permission
	 * type.
	 *
	 * @param filePermission the type of file permission to filter by (e.g., "read",
	 *                       "overwrite", "create"), must not be null.
	 * @return a list of permitted file paths for the specified file permission
	 *         type.
	 * @throws SecurityException if the filePermission is not a valid permission
	 *                           type
	 */
	@Nonnull
	public List<String> getPermittedFilePaths(@Nonnull String filePermission) {
		@Nonnull
		Predicate<FilePermission> filter = switch (filePermission) {
		case "read" -> FilePermission::readAllFiles;
		case "overwrite" -> FilePermission::overwriteAllFiles;
		case "create" -> FilePermission::createAllFiles;
		case "execute" -> FilePermission::executeAllFiles;
		case "delete" -> FilePermission::deleteAllFiles;
		default -> throw new SecurityException(
				Messages.localized("security.advice.settings.invalid.file.permission", filePermission));
		};
		return ((List<FilePermission>) resourceAccessSupplier.get()).stream().filter(filter)
				.map(FilePermission::onThisPathAndAllPathsBelow).toList();
	}

	// </editor-fold>
}
