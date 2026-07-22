package de.tum.cit.ase.ares.api.policy.reader.yaml;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Objects;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.fasterxml.jackson.core.JsonParser;
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
 * during policy reading.
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
			JsonNode root = FileTools.readYamlTree(FileTools.readFile(path), objectMapper, effectiveProjectRoot);
			SecurityPolicySchemaValidator.validate(root);
			SecurityPolicy policy = objectMapper.treeToValue(root, yamlClass);
			if (policy == null) {
				throw new SecurityException(Messages.localized("security.policy.data.bind.failed", path.toString()));
			}
			return policy;
		} catch (StreamReadException e) {
			throw new SecurityException(Messages.localized("security.policy.read.failed", path.toString()), e);
		} catch (DatabindException e) {
			throw new SecurityException(Messages.localized("security.policy.data.bind.failed", path.toString()), e);
		} catch (UnsupportedOperationException e) {
			throw new SecurityException(Messages.localized("security.policy.unsupported.operation", path.toString()),
					e);
		} catch (IOException e) {
			throw new SecurityException(Messages.localized("security.policy.io.exception", path.toString()), e);
		}
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
