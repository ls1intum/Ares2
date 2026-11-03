package de.tum.cit.ase.ares.api.policy.reader;

import java.nio.file.Path;
import java.util.Objects;

import javax.annotation.Nonnull;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;

import de.tum.cit.ase.ares.api.policy.SecurityPolicy;
import de.tum.cit.ase.ares.api.policy.reader.yaml.SecurityPolicyYAMLReader;

/**
 * Abstract class for reading a SecurityPolicy from a file.
 * <p>
 * Description: This abstract class defines the contract for classes that parse
 * and produce a SecurityPolicy from a given file system path. Implementations
 * must return a valid SecurityPolicy instance.
 * <p>
 * Design Rationale: As an abstract class, it lets each format share the
 * object-mapper handling while supplying its own parsing.
 *
 * @since 2.0.0
 * @author Markus Paulsen
 * @version 2.0.0
 */
public abstract class SecurityPolicyReader {

	// <editor-fold desc="Attributes">
	/**
	 * Object mapper for reading files.
	 */
	@Nonnull
	protected final ObjectMapper objectMapper;
	// </editor-fold>

	// <editor-fold desc="Constructor">
	/**
	 * Constructs a new SecurityPolicyReader with the specified factory and object
	 * mapper.
	 *
	 * @since 2.0.0
	 * @author Markus Paulsen
	 * @param objectMapper the non-null object mapper for reading files.
	 */
	public SecurityPolicyReader(@Nonnull ObjectMapper objectMapper) {
		this.objectMapper = Objects.requireNonNull(objectMapper, "objectMapper must not be null");
	}
	// </editor-fold>

	// <editor-fold desc="Abstract methods">
	/**
	 * Reads and parses a SecurityPolicy from the specified file path.
	 *
	 * @since 2.0.0
	 * @author Markus Paulsen
	 * @param securityPolicyPath the non-null file system path to the security
	 *                           policy file.
	 * @return a non-null SecurityPolicy instance created from the file content.
	 * @throws SecurityException if an error occurs during reading or parsing the
	 *                           file.
	 */
	@Nonnull
	public abstract SecurityPolicy readSecurityPolicyFrom(@Nonnull Path securityPolicyPath);
	// </editor-fold>

	// <editor-fold desc="Static methods">
	public static SecurityPolicyReader selectSecurityPolicyReader(Path securityPolicyFilePath) {
		return selectSecurityPolicyReader(securityPolicyFilePath, Path.of("").toAbsolutePath());
	}

	/**
	 * Selects the reader for a policy file's format.
	 * <p>
	 * The switch has no default branch on purpose: an unsupported extension is
	 * already rejected by {@link SecurityPolicyFileFormat#fromPath}, and a new
	 * format then fails to compile here until it is handled.
	 *
	 * @param securityPolicyFilePath the policy file; must not be null.
	 * @param projectRootPath        the project root, or null to use the policy
	 *                               file's folder.
	 * @return the reader for that format.
	 */
	public static SecurityPolicyReader selectSecurityPolicyReader(Path securityPolicyFilePath, Path projectRootPath) {
		Objects.requireNonNull(securityPolicyFilePath, "securityPolicyFilePath must not be null");
		Path absolutePolicy = securityPolicyFilePath.toAbsolutePath().normalize();
		Path effectiveRoot = projectRootPath == null
				? Objects.requireNonNullElse(absolutePolicy.getParent(), absolutePolicy)
				: projectRootPath;
		return switch (SecurityPolicyFileFormat.fromPath(securityPolicyFilePath)) {
		case YAML -> SecurityPolicyYAMLReader.yamlBuilder().yamlMapper(new YAMLMapper()).projectRootPath(effectiveRoot)
				.build();
		};
	}
	// </editor-fold>
}
