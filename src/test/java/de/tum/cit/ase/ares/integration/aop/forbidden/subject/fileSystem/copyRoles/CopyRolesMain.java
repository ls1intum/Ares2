package de.tum.cit.ase.ares.integration.aop.forbidden.subject.fileSystem.copyRoles;

import java.io.IOException;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Student code copying a file, once through {@code Files.copy} and once through
 * the file system provider that {@code Files.copy} hands the work to.
 */
public final class CopyRolesMain {

	/**
	 * Refuses instantiation, since the class only offers static methods.
	 */
	private CopyRolesMain() {
		throw new SecurityException(
				"Ares Security Error (Reason: Ares-Code; Stage: Test): Main is a utility class and should not be instantiated.");
	}

	/**
	 * Copies a file with {@code Files.copy}.
	 *
	 * @param source  the file to copy
	 * @param target  the copy
	 * @param options how to copy
	 * @throws IOException if the copy fails
	 */
	public static void copyWithFiles(Path source, Path target, CopyOption... options) throws IOException {
		Files.copy(source, target, options);
	}

	/**
	 * Copies a file by calling the provider of its file system directly.
	 *
	 * @param source  the file to copy
	 * @param target  the copy
	 * @param options how to copy
	 * @throws IOException if the copy fails
	 */
	public static void copyWithProvider(Path source, Path target, CopyOption... options) throws IOException {
		source.getFileSystem().provider().copy(source, target, options);
	}
}
