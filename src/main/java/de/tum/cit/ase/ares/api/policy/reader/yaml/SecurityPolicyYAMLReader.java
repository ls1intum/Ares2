package de.tum.cit.ase.ares.api.policy.reader.yaml;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.yaml.snakeyaml.error.Mark;
import org.yaml.snakeyaml.error.MarkedYAMLException;

import com.fasterxml.jackson.core.JsonLocation;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.exc.StreamReadException;
import com.fasterxml.jackson.databind.DatabindException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLParser;

import de.tum.cit.ase.ares.api.localization.Messages;
import de.tum.cit.ase.ares.api.policy.SecurityPolicy;
import de.tum.cit.ase.ares.api.policy.reader.SecurityPolicyReader;
import de.tum.cit.ase.ares.api.policy.reader.SecurityPolicySchemaValidator;
import de.tum.cit.ase.ares.api.util.FileTools;

/**
 * YAML security policy reader.
 * <p>
 * Description: Reads a {@link SecurityPolicy} from a YAML file using the
 * Jackson YAML library. It validates the provided file path and translates
 * common exceptions into a {@code SecurityException} to clearly signal issues
 * during policy reading. The first line of that exception names the file and
 * the concrete cause; the original exception stays attached as its cause.
 * <p>
 * Design Rationale: By employing Jackson's YAMLFactory and ObjectMapper, this
 * class minimises boilerplate and ensures efficient parsing. Its focused
 * responsibility and robust exception handling align with modern best practices
 * for immutable and maintainable code.
 *
 * @since 2.0.0
 * @author Markus Paulsen
 * @version 2.0.0
 */
public class SecurityPolicyYAMLReader extends SecurityPolicyReader {

	/**
	 * The prefix of an Ares message in English or German, removed from a cause
	 * before it is quoted, so the reader's own prefix is the only one.
	 */
	private static final Pattern ARES_PREFIX = Pattern.compile(
			"Ares (?:Security Error \\(Reason: [^;()]+; Stage: [^()]+\\)|Sicherheitsfehler \\(Grund: [^;()]+; Phase: [^()]+\\)): ");

	/**
	 * The project root for {@code ${PROJECT_ROOT}}, or null for the file's folder.
	 */
	@Nullable
	private final Path projectRootPath;

	// <editor-fold desc="Constructor">

	/**
	 * Constructs a new SecurityPolicyYAMLReader with the specified YAML factory and
	 * object mapper.
	 *
	 * @since 2.0.0
	 * @author Markus Paulsen
	 * @param yamlMapper the non-null YAML object mapper for reading YAML files.
	 */
	public SecurityPolicyYAMLReader(@Nonnull YAMLMapper yamlMapper) {
		this(yamlMapper, null);
	}

	/**
	 * Constructs a new SecurityPolicyYAMLReader.
	 * <p>
	 * The supplied {@code yamlMapper} is hardened in place with the strict parsing
	 * features the reader relies on. It is used rather than copied, so a caller's
	 * custom (de)serialisers keep working, at the cost of changing that mapper.
	 *
	 * @since 2.0.0
	 * @author Markus Paulsen
	 * @param yamlMapper      the non-null YAML mapper, changed in place.
	 * @param projectRootPath the project root for {@code ${PROJECT_ROOT}}, or null
	 *                        to use the policy file's folder at read time.
	 */
	public SecurityPolicyYAMLReader(@Nonnull YAMLMapper yamlMapper, @Nullable Path projectRootPath) {
		super(yamlMapper);
		yamlMapper.enable(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES);
		yamlMapper.enable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
		yamlMapper.getFactory().enable(JsonParser.Feature.STRICT_DUPLICATE_DETECTION);
		yamlMapper.getFactory().enable(YAMLParser.Feature.PARSE_BOOLEAN_LIKE_WORDS_AS_STRINGS);
		this.projectRootPath = projectRootPath == null ? null : projectRootPath.toAbsolutePath().normalize();
	}
	// </editor-fold>

	// <editor-fold desc="Read policy methods">

	/**
	 * Reads a SecurityPolicy from the specified YAML file path.
	 *
	 * @since 2.0.0
	 * @author Markus Paulsen
	 * @param securityPolicyPath the non-null path to the YAML security policy file.
	 * @return a non-null SecurityPolicy instance derived from the file content.
	 */
	@Override
	@Nonnull
	public SecurityPolicy readSecurityPolicyFrom(@Nonnull Path securityPolicyPath) {
		return readSecurityPolicy(securityPolicyPath, projectRoot -> FileTools
				.readYamlTree(FileTools.readFile(securityPolicyPath), objectMapper, projectRoot));
	}

	/**
	 * Reads a SecurityPolicy from given bytes of a YAML policy file and given
	 * placeholder values, reading neither the file nor a system property again. The
	 * path names the file in a refusal.
	 *
	 * @param securityPolicyPath the policy file the bytes were read from
	 * @param content            the file's bytes
	 * @param propertyValues     the property values placeholders expand to
	 * @return the policy these inputs describe
	 */
	@Nonnull
	public SecurityPolicy readSecurityPolicyFrom(@Nonnull Path securityPolicyPath, @Nonnull byte[] content,
			@Nonnull Map<String, String> propertyValues) {
		Objects.requireNonNull(content, "The security policy content must not be null");
		return readSecurityPolicy(securityPolicyPath,
				projectRoot -> FileTools.readYamlTree(content, objectMapper, projectRoot, propertyValues));
	}

	/**
	 * Reads the YAML tree of a policy for a project root.
	 */
	@FunctionalInterface
	private interface PolicyTreeSource {

		/**
		 * Reads the tree.
		 *
		 * @param projectRoot the project root for {@code ${PROJECT_ROOT}}
		 * @return the tree, placeholders expanded
		 * @throws IOException if the policy cannot be read or parsed
		 */
		@Nonnull
		JsonNode read(@Nonnull Path projectRoot) throws IOException;
	}

	/**
	 * Reads, validates and binds a policy, translating every failure into a refusal
	 * that names the policy file.
	 *
	 * @param securityPolicyPath the policy file
	 * @param source             where the YAML tree comes from
	 * @return the policy
	 */
	@Nonnull
	private SecurityPolicy readSecurityPolicy(@Nonnull Path securityPolicyPath, @Nonnull PolicyTreeSource source) {
		@Nonnull
		Path path = Objects.requireNonNull(securityPolicyPath, "The security policy path must not be null");
		@Nonnull
		Class<SecurityPolicy> yamlClass = Objects.requireNonNull(SecurityPolicy.class,
				"The security policy class must not be null.");
		try {
			Path effectiveProjectRoot = projectRootPath;
			if (effectiveProjectRoot == null) {
				Path absolutePolicyPath = path.toAbsolutePath().normalize();
				effectiveProjectRoot = Objects.requireNonNullElse(absolutePolicyPath.getParent(), absolutePolicyPath);
			}
			JsonNode root = source.read(effectiveProjectRoot);
			SecurityPolicySchemaValidator.validate(root);
			SecurityPolicy policy = objectMapper.treeToValue(root, yamlClass);
			if (policy == null) {
				throw new SecurityException(Messages.localized("security.policy.data.bind.failed", path.toString(),
						withoutAresPrefix(Messages.localized("security.file-tools.yaml.root.null"))));
			}
			return policy;
		} catch (StreamReadException e) {
			throw new SecurityException(parseFailureMessage(path, e), e);
		} catch (DatabindException e) {
			throw new SecurityException(
					Messages.localized("security.policy.data.bind.failed", path.toString(), causeOf(e)), e);
		} catch (UnsupportedOperationException e) {
			throw new SecurityException(
					Messages.localized("security.policy.unsupported.operation", path.toString(), causeOf(e)), e);
		} catch (IOException e) {
			throw new SecurityException(
					Messages.localized("security.policy.io.exception.cause", path.toString(), causeOf(e)), e);
		}
	}

	/**
	 * Where the parser stopped in the policy file, and what it said.
	 *
	 * @param line   the line, counted from 1.
	 * @param column the column, counted from 1.
	 * @param text   the parser's own text, in English; never blank.
	 */
	record ParserProblem(int line, int column, @Nonnull String text) {
	}

	/**
	 * Builds the refusal of a file the parser could not read: with the line and
	 * column the parser reported, or, if it reported none that can be trusted, the
	 * general reading failure.
	 *
	 * @param path    the policy file.
	 * @param failure the parser's failure.
	 * @return the message for the reader's refusal.
	 */
	@Nonnull
	static String parseFailureMessage(@Nonnull Path path, @Nonnull StreamReadException failure) {
		ParserProblem problem = parserProblemOf(failure);
		if (problem == null) {
			return Messages.localized("security.policy.read.failed", path.toString(), causeOf(failure));
		}
		return Messages.localized("security.policy.parse.failed.at", path.toString(), problem.line(), problem.column(),
				problem.text());
	}

	/**
	 * Finds where the parser stopped.
	 * <p>
	 * SnakeYAML's own mark comes first, the innermost one in the cause chain,
	 * because Jackson's location points past a SnakeYAML failure. Jackson's
	 * location is used only when no mark exists, and only with a known line and
	 * column, so the message never names a wrong place.
	 *
	 * @param failure the parser's failure.
	 * @return the position and text, or null if there is no trustworthy position.
	 */
	@Nullable
	static ParserProblem parserProblemOf(@Nonnull StreamReadException failure) {
		MarkedYAMLException marked = innermostMarkedFailure(failure);
		if (marked != null) {
			Mark mark = marked.getProblemMark();
			return new ParserProblem(mark.getLine() + 1, mark.getColumn() + 1,
					textOrClassName(marked.getMessage(), marked));
		}
		JsonLocation location = failure.getLocation();
		if (location == null || location.getLineNr() <= 0 || location.getColumnNr() <= 0) {
			return null;
		}
		return new ParserProblem(location.getLineNr(), location.getColumnNr(),
				textOrClassName(failure.getOriginalMessage(), failure));
	}

	/**
	 * Finds the innermost SnakeYAML failure in the cause chain with a usable mark,
	 * stopping if the chain loops back on itself.
	 *
	 * @param failure the outermost failure.
	 * @return that SnakeYAML failure, or null if there is none.
	 */
	@Nullable
	private static MarkedYAMLException innermostMarkedFailure(@Nonnull Throwable failure) {
		MarkedYAMLException innermost = null;
		Set<Throwable> seen = Collections.newSetFromMap(new IdentityHashMap<>());
		for (Throwable current = failure; current != null && seen.add(current); current = current.getCause()) {
			if (current instanceof MarkedYAMLException marked && hasUsableMark(marked)) {
				innermost = marked;
			}
		}
		return innermost;
	}

	/**
	 * Tells whether a SnakeYAML failure carries a mark with a real position.
	 *
	 * @param failure the SnakeYAML failure.
	 * @return true if its problem mark exists and is not negative.
	 */
	private static boolean hasUsableMark(@Nonnull MarkedYAMLException failure) {
		Mark mark = failure.getProblemMark();
		return mark != null && mark.getLine() >= 0 && mark.getColumn() >= 0;
	}

	/**
	 * Returns a parser text stripped of surrounding whitespace, or the class name
	 * of the failure if the text is missing or blank, so the message never ends
	 * empty.
	 *
	 * @param text    the parser's text; may be null.
	 * @param failure the failure it came from.
	 * @return the text to quote.
	 */
	@Nonnull
	private static String textOrClassName(@Nullable String text, @Nonnull Throwable failure) {
		return text == null || text.isBlank() ? failure.getClass().getSimpleName() : text.strip();
	}

	/**
	 * Finds the text that says what is wrong with the policy file.
	 * <p>
	 * It is the message of the innermost exception in the chain that is an Ares
	 * message or a schema violation, without its own prefix and without Jackson's
	 * framing. If there is none, it is the innermost exception's class and message,
	 * so it is never empty.
	 *
	 * @param failure the exception the reader caught.
	 * @return the cause for the reader's first line.
	 */
	@Nonnull
	static String causeOf(@Nonnull Throwable failure) {
		String cause = null;
		Throwable innermost = failure;
		Set<Throwable> seen = Collections.newSetFromMap(new IdentityHashMap<>());
		for (Throwable current = failure; current != null && seen.add(current); current = current.getCause()) {
			innermost = current;
			String text = ownMessage(current);
			if (current instanceof SecurityPolicySchemaValidator.PolicySchemaViolation) {
				cause = text;
			} else if (text != null && ARES_PREFIX.matcher(text).find()) {
				cause = withoutAresPrefix(text);
			}
		}
		if (cause != null) {
			return cause;
		}
		String innermostText = ownMessage(innermost);
		return innermost.getClass().getSimpleName() + (innermostText == null ? "" : ": " + innermostText);
	}

	/**
	 * Returns an exception's message without Jackson's location suffix.
	 *
	 * @param failure the exception.
	 * @return its message, stripped of surrounding whitespace, or null if it has
	 *         none.
	 */
	@Nullable
	private static String ownMessage(@Nonnull Throwable failure) {
		String message = failure instanceof JsonProcessingException jackson ? jackson.getOriginalMessage()
				: failure.getMessage();
		return message == null || message.isBlank() ? null : message.strip();
	}

	/**
	 * Cuts everything up to and including the first Ares prefix, which also drops
	 * Jackson's framing in front of it, such as {@code Cannot construct instance
	 * of ..., problem: }.
	 *
	 * @param text a message that may hold an Ares prefix.
	 * @return the text after the prefix, or the text unchanged if it has none.
	 */
	@Nonnull
	private static String withoutAresPrefix(@Nonnull String text) {
		Matcher matcher = ARES_PREFIX.matcher(text);
		return matcher.find() ? text.substring(matcher.end()) : text;
	}
	// </editor-fold>

	// <editor-fold desc="Builder">
	public static Builder yamlBuilder() {
		return new Builder();
	}

	public static class Builder {
		@Nullable
		private YAMLMapper yamlMapper;
		@Nullable
		private Path projectRootPath;

		@Nonnull
		public Builder yamlMapper(@Nonnull YAMLMapper yamlMapper) {
			this.yamlMapper = Objects.requireNonNull(yamlMapper, "yamlMapper must not be null");
			return this;
		}

		@Nonnull
		public Builder projectRootPath(@Nonnull Path projectRootPath) {
			this.projectRootPath = Objects.requireNonNull(projectRootPath, "projectRootPath must not be null");
			return this;
		}

		@Nonnull
		public SecurityPolicyYAMLReader build() {
			return new SecurityPolicyYAMLReader(Objects.requireNonNull(yamlMapper, "yamlMapper must not be null"),
					projectRootPath);
		}
	}
	// </editor-fold>
}
