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
 * should perform any necessary validation and error handling, and must return a
 * valid SecurityPolicy instance.
 * <p>
 * Design Rationale: Declaring this as an abstract class lets each format share
 * the common object-mapper handling while supplying its own parsing. It cleanly
 * separates the concern of reading and parsing security policies from the rest
 * of the system.
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

	public static SecurityPolicyReader selectSecurityPolicyReader(Path securityPolicyFilePath, Path projectRootPath) {
		Objects.requireNonNull(securityPolicyFilePath, "securityPolicyFilePath must not be null");
		Path absolutePolicy = securityPolicyFilePath.toAbsolutePath().normalize();
		Path effectiveRoot = projectRootPath == null
				? Objects.requireNonNullElse(absolutePolicy.getParent(), absolutePolicy)
				: projectRootPath;
		// SecurityPolicyFileFormat.fromPath rejects an unsupported extension, so the
		// switch over the resolved format is exhaustive and needs no default branch: a
		// new format is a new enum constant that the compiler forces us to handle here.
		return switch (SecurityPolicyFileFormat.fromPath(securityPolicyFilePath)) {
		case YAML -> SecurityPolicyYAMLReader.yamlBuilder().yamlMapper(new YAMLMapper()).projectRootPath(effectiveRoot)
				.build();
		};
	}
	// </editor-fold>
}
