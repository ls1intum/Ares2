package de.tum.cit.ase.ares.api.util;

import java.nio.file.Path;
import java.util.Map;
import java.util.Objects;

import javax.annotation.Nonnull;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;

/**
 * Resolves a fixed set of placeholders inside textual scalars after YAML
 * parsing.
 * <p>
 * Description: Provides string-level expansion of four well-defined
 * placeholders ({@code ${PROJECT_ROOT}}, {@code ${java.home}},
 * {@code ${user.home}}, {@code ${java.io.tmpdir}}) so that security-policy YAML
 * files can be authored portably across local, CI, and Docker environments
 * without hard-coding user- or platform-specific paths.
 * <p>
 * Design Rationale: A small fixed set of placeholders keeps the substitution
 * surface predictable and avoids accidental coupling of policy files to
 * arbitrary system properties. Unknown placeholders are left untouched so
 * legitimate text containing {@code ${...}} sequences cannot be silently
 * mangled.
 *
 * @since 2.0.1
 * @author Markus Paulsen
 * @version 2.0.1
 */
public final class YamlPlaceholderResolver {

	@Nonnull
	private static final Map<String, String> PLACEHOLDER_PROPERTIES = Map.of("${java.home}", "java.home",
			"${user.home}", "user.home", "${java.io.tmpdir}", "java.io.tmpdir");

	private YamlPlaceholderResolver() {
		throw new UnsupportedOperationException(
				"YamlPlaceholderResolver is a utility class and cannot be instantiated");
	}

	/**
	 * Expands the four supported placeholders in the supplied scalar content.
	 *
	 * @param content the scalar content; may contain placeholder tokens.
	 * @return the content with every supported placeholder replaced by the
	 *         corresponding system property value. When a system property is unset,
	 *         the placeholder is replaced with the empty string. Unknown
	 *         placeholders remain unchanged.
	 */
	@Nonnull
	public static String expand(@Nonnull String content) {
		return expand(content, Path.of(System.getProperty("user.dir", "")));
	}

	/**
	 * Expands placeholders in one already-parsed scalar value.
	 *
	 * @param content     the scalar content
	 * @param projectRoot the explicit project root used for {@code PROJECT_ROOT}
	 * @return the expanded scalar content
	 */
	@Nonnull
	public static String expand(@Nonnull String content, @Nonnull Path projectRoot) {
		String expanded = Objects.requireNonNull(content, "content must not be null");
		expanded = expanded.replace("${PROJECT_ROOT}", Objects
				.requireNonNull(projectRoot, "projectRoot must not be null").toAbsolutePath().normalize().toString());
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

	/**
	 * Expands placeholders only inside textual nodes of a parsed YAML tree. YAML is
	 * parsed before substitution so replacement text can never create keys,
	 * comments, aliases or additional documents.
	 *
	 * @param node        the YAML tree to update
	 * @param projectRoot the explicit project root
	 * @return the same tree with textual scalar values expanded
	 */
	@Nonnull
	public static JsonNode expandScalars(@Nonnull JsonNode node, @Nonnull Path projectRoot) {
		Objects.requireNonNull(node, "node must not be null");
		Objects.requireNonNull(projectRoot, "projectRoot must not be null");
		if (node.isObject()) {
			ObjectNode objectNode = (ObjectNode) node;
			objectNode.properties()
					.forEach(entry -> objectNode.set(entry.getKey(), expandScalars(entry.getValue(), projectRoot)));
			return objectNode;
		}
		if (node.isArray()) {
			ArrayNode arrayNode = (ArrayNode) node;
			for (int index = 0; index < arrayNode.size(); index++) {
				arrayNode.set(index, expandScalars(arrayNode.get(index), projectRoot));
			}
			return arrayNode;
		}
		if (node.isTextual()) {
			return TextNode.valueOf(expand(node.textValue(), projectRoot));
		}
		return node;
	}
}
