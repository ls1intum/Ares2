package de.tum.cit.ase.ares.api.util;

import java.util.Map;

import javax.annotation.Nonnull;

/**
 * Resolves a fixed set of placeholders inside YAML content before parsing.
 *
 * <p>Description: Provides string-level expansion of four well-defined placeholders
 * ({@code ${PROJECT_ROOT}}, {@code ${java.home}}, {@code ${user.home}},
 * {@code ${java.io.tmpdir}}) so that security-policy YAML files can be authored
 * portably across local, CI, and Docker environments without hard-coding user-
 * or platform-specific paths.
 *
 * <p>Design Rationale: A small fixed set of placeholders keeps the substitution surface
 * predictable and avoids accidental coupling of policy files to arbitrary system
 * properties. Unknown placeholders are left untouched so legitimate text containing
 * {@code ${...}} sequences cannot be silently mangled.
 *
 * @since 2.0.1
 * @author Markus Paulsen
 * @version 2.0.1
 */
public final class YamlPlaceholderResolver {

	@Nonnull
	private static final Map<String, String> PLACEHOLDER_PROPERTIES = Map.of(
			"${PROJECT_ROOT}", "user.dir",
			"${java.home}", "java.home",
			"${user.home}", "user.home",
			"${java.io.tmpdir}", "java.io.tmpdir");

	private YamlPlaceholderResolver() {
		throw new UnsupportedOperationException("YamlPlaceholderResolver is a utility class and cannot be instantiated");
	}

	/**
	 * Expand the three supported placeholders in the supplied YAML content.
	 *
	 * @param content the raw YAML content; may contain placeholder tokens.
	 * @return the content with every supported placeholder replaced by the corresponding system property value.
	 *         When a system property is unset, the placeholder is replaced with the empty string. Unknown
	 *         placeholders remain unchanged.
	 */
	@Nonnull
	public static String expand(@Nonnull String content) {
		String expanded = content;
		for (Map.Entry<String, String> entry : PLACEHOLDER_PROPERTIES.entrySet()) {
			String token = entry.getKey();
			if (!expanded.contains(token)) {
				continue;
			}
			String value = System.getProperty(entry.getValue(), "");
			expanded = expanded.replace(token, value);
		}
		return expanded;
	}
}
