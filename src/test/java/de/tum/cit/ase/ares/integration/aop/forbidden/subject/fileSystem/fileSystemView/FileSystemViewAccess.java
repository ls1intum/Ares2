package de.tum.cit.ase.ares.integration.aop.forbidden.subject.fileSystem.fileSystemView;

import java.io.File;
import java.io.IOException;

import javax.swing.filechooser.FileSystemView;

/**
 * Supervised code that lists and creates folders through FileSystemView, whose
 * listing happens inside the JDK, where the AspectJ compiler weaves nothing.
 */
public final class FileSystemViewAccess {

	/** A folder the policy lets supervised code read and create in. */
	public static final File PERMITTED = new File(
			"src/test/java/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/fileSystemView/permitted");

	/** A folder the policy lets supervised code neither read nor create in. */
	public static final File FORBIDDEN = new File(
			"src/test/java/de/tum/cit/ase/ares/integration/aop/forbidden/subject/fileSystem/fileSystemView/forbidden");

	/** Prevents creating instances of this utility class. */
	private FileSystemViewAccess() {
		throw new SecurityException(
				"Ares Security Error (Reason: Ares-Code; Stage: Test): FileSystemViewAccess is a utility class and should not be instantiated.");
	}

	/**
	 * Lists a folder with {@code getFiles}.
	 *
	 * @param folder the folder to list
	 * @return its entries
	 */
	public static File[] list(File folder) {
		return FileSystemView.getFileSystemView().getFiles(folder, false);
	}

	/**
	 * Looks up a child with {@code getChild}, which lists a special folder.
	 *
	 * @param folder the folder to look in
	 * @param name   the child's name
	 * @return the child
	 */
	public static File child(File folder, String name) {
		return FileSystemView.getFileSystemView().getChild(folder, name);
	}

	/**
	 * Looks up a child of the default directory, which the JDK hands out as a
	 * special folder, so looking in it lists it.
	 *
	 * @param name the child's name
	 * @return the child
	 */
	public static File childOfDefaultDirectory(String name) {
		FileSystemView view = FileSystemView.getFileSystemView();
		return view.getChild(view.getDefaultDirectory(), name);
	}

	/**
	 * Asks with {@code isParent} whether a folder contains an entry.
	 *
	 * @param folder the folder
	 * @return whether it contains its entry.txt
	 */
	public static boolean containsEntry(File folder) {
		return FileSystemView.getFileSystemView().isParent(folder, new File(folder, "entry.txt"));
	}

	/**
	 * Creates a new folder with {@code createNewFolder}.
	 *
	 * @param folder the folder to create it in
	 * @return the created folder
	 * @throws IOException if the JDK cannot create it
	 */
	public static File newFolderIn(File folder) throws IOException {
		return FileSystemView.getFileSystemView().createNewFolder(folder);
	}

	/**
	 * Looks up a child through a student's own FileSystemView subclass.
	 *
	 * @param folder the folder to look in
	 * @param name   the child's name, which is not a path
	 * @return the child
	 */
	public static File childThroughOwnView(File folder, String name) {
		return new StudentFileSystemView().getChild(folder, name);
	}
}
