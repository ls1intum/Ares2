package de.tum.cit.ase.ares.integration.aop.allowed.subject.fileSystem.openingRoles;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Text the supervised code wrote itself that, when the JDK turns it into a
 * string, first puts a link to another file where the text is about to be
 * written.
 */
public final class LinkSwappingText implements CharSequence {

	/**
	 * Where the link goes, the file the text is written to.
	 */
	private final Path link;

	/**
	 * The file the link points to.
	 */
	private final Path target;

	/**
	 * The text itself.
	 */
	private final String text;

	/**
	 * Makes the text.
	 *
	 * @param link   where the link goes
	 * @param target the file the link points to
	 * @param text   the text
	 */
	public LinkSwappingText(Path link, Path target, String text) {
		this.link = link;
		this.target = target;
		this.text = text;
	}

	/**
	 * Puts the link in place, then returns the text.
	 *
	 * @return the text
	 */
	@Override
	public String toString() {
		try {
			Files.createSymbolicLink(link, target.toAbsolutePath());
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
		return text;
	}

	/**
	 * Returns the length of the text.
	 *
	 * @return the length
	 */
	@Override
	public int length() {
		return text.length();
	}

	/**
	 * Returns one character of the text.
	 *
	 * @param index the position
	 * @return the character
	 */
	@Override
	public char charAt(int index) {
		return text.charAt(index);
	}

	/**
	 * Returns a part of the text.
	 *
	 * @param start the first position
	 * @param end   the position after the last
	 * @return the part
	 */
	@Override
	public CharSequence subSequence(int start, int end) {
		return text.subSequence(start, end);
	}
}
