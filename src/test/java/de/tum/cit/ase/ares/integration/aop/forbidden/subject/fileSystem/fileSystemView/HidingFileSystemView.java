package de.tum.cit.ase.ares.integration.aop.forbidden.subject.fileSystem.fileSystemView;

import java.io.File;
import java.io.IOException;

import javax.swing.filechooser.FileSystemView;

/**
 * A student's own FileSystemView whose hiding rule asks the JDK whether a file
 * is a link, a question Ares does not check as a file read. On Windows the JDK
 * answers it on its COM thread while the student's rule is still running.
 */
public final class HidingFileSystemView extends FileSystemView {

	/**
	 * Hides links and whatever the JDK hides.
	 *
	 * @param f the file to judge
	 * @return whether to hide it
	 */
	@Override
	public boolean isHiddenFile(File f) {
		return isLink(new File(f.getPath())) || super.isHiddenFile(f);
	}

	/**
	 * Creates nothing; this view is only ever asked to list.
	 *
	 * @param containingDir the folder a new folder would go into
	 * @return never
	 * @throws IOException always
	 */
	@Override
	public File createNewFolder(File containingDir) throws IOException {
		throw new IOException("HidingFileSystemView creates no folders");
	}
}
