package de.tum.cit.ase.ares.api.policy.reader;

import java.nio.file.Path;
import java.util.Locale;
import java.util.Set;

import javax.annotation.Nonnull;

import de.tum.cit.ase.ares.api.localization.Messages;

/**
 * The supported security-policy file formats, identified by file extension.
 * <p>
 * Description: Each constant names one format Ares can read and the file
 * extensions that select it.
 * <p>
 * Design Rationale: Keeping the formats in one enum lets
 * {@link SecurityPolicyReader#selectSecurityPolicyReader(Path, Path)} switch
 * without a default branch, so the compiler forces it to handle a new format.
 *
 * @since 2.1.0
 * @author Markus Paulsen
 */
enum SecurityPolicyFileFormat {

	/** YAML, recognised by the {@code .yaml} and {@code .yml} extensions. */
	YAML(Set.of("yaml", "yml"));

	/** The lower-case file extensions, without the dot, that select this format. */
	@Nonnull
	private final Set<String> extensions;

	/**
	 * Creates a format selected by the given extensions.
	 *
	 * @param extensions the lower-case extensions, without the dot.
	 */
	SecurityPolicyFileFormat(@Nonnull Set<String> extensions) {
		this.extensions = extensions;
	}

	/**
	 * Resolves the format of a policy file from its extension.
	 *
	 * @since 2.1.0
	 * @author Markus Paulsen
	 * @param path the path whose format is resolved; must not be null.
	 * @return the matching format.
	 * @throws IllegalArgumentException if the extension matches no supported
	 *                                  format.
	 */
	@Nonnull
	static SecurityPolicyFileFormat fromPath(@Nonnull Path path) {
		String extension = fileExtensionOf(path);
		for (SecurityPolicyFileFormat format : values()) {
			if (format.extensions.contains(extension)) {
				return format;
			}
		}
		throw new IllegalArgumentException(Messages.localized("policy.reader.unsupported.format", extension));
	}

	/**
	 * Returns the lowercase file extension (the part after the last {@code '.'} in
	 * the file name), or the empty string if there is none. Replaces Guava's
	 * {@code MoreFiles.getFileExtension}.
	 *
	 * @param path the path whose file extension is returned.
	 * @return the file extension without the leading dot, or {@code ""}.
	 */
	private static String fileExtensionOf(@Nonnull Path path) {
		Path fileName = path.getFileName();
		if (fileName == null) {
			return "";
		}
		String name = fileName.toString();
		int lastDotIndex = name.lastIndexOf('.');
		return (lastDotIndex == -1) ? "" : name.substring(lastDotIndex + 1).toLowerCase(Locale.ROOT);
	}
}
