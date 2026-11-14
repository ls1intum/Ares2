package de.tum.cit.ase.ares.integration.aop.forbidden.subject.fileSystem.fileSystemView;

import java.io.File;
import java.io.IOException;

import javax.swing.filechooser.FileSystemView;

/**
 * A student's own FileSystemView, which overrides getChild, so that a call
 * through it carries the student's class name rather than the JDK's.
 */
public final class StudentFileSystemView extends FileSystemView {

	/**
	 * Looks up a child as the JDK does.
	 *
	 * @param parent   the folder to look in
	 * @param fileName the child's name
	 * @return the child
	 */
	@Override
	public File getChild(File parent, String fileName) {
		return super.getChild(parent, fileName);
	}

	/**
	 * Creates nothing; this view is only ever asked for children.
	 *
	 * @param containingDir the folder a new folder would go into
	 * @return never
	 * @throws IOException always
	 */
	@Override
	public File createNewFolder(File containingDir) throws IOException {
		throw new IOException("StudentFileSystemView creates no folders");
	}
}
