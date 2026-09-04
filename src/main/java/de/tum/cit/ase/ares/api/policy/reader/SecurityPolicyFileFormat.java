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
 * extensions that select it. Resolving a path to a format, and the extension
 * extraction that used to live in {@link SecurityPolicyReader}, are owned here,
 * so the reader's selection is a single exhaustive switch over this enum.
 * <p>
 * Design Rationale: Keeping the set of formats in one enum lets
 * {@link SecurityPolicyReader#selectSecurityPolicyReader(Path, Path)} switch
 * without a default branch: a new format is a new constant, and the compiler
 * then forces the switch to handle it, rather than a string {@code default}
 * that silently accepts the omission.
 *
 * @since 2.1.0
 * @author Markus Paulsen
 */
enum SecurityPolicyFileFormat {

	/** YAML, recognised by the {@code .yaml} and {@code .yml} extensions. */
	YAML(Set.of("yaml", "yml"));

	@Nonnull
	private final Set<String> extensions;

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
