package de.tum.cit.ase.ares.api.policy.reader;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import com.google.common.base.Preconditions;
import com.google.common.io.MoreFiles;
import de.tum.cit.ase.ares.api.policy.SecurityPolicy;
import de.tum.cit.ase.ares.api.policy.reader.yaml.SecurityPolicyYAMLReader;

import javax.annotation.Nonnull;
import java.nio.file.Path;

/**
 * Abstractr class for reading a SecurityPolicy from a file.
 *
 * <p>Description: This interface defines the contract for classes that parse and produce a SecurityPolicy from a given file system path.
 * Implementations should perform any necessary validation and error handling, and must return a valid SecurityPolicy instance.
 *
 * <p>Design Rationale: Declaring this as a functional interface promotes concise implementations using lambda expressions or method references.
 * It cleanly separates the concern of reading and parsing security policies from the rest of the system.
 *
 * @since 2.0.0
 * @author Markus Paulsen
 * @version 2.0.0
 */
public abstract class SecurityPolicyReader {

    //<editor-fold desc="Attributes">
    /**
     * Object mapper for reading files.
     */
    @Nonnull
    protected final ObjectMapper objectMapper;
    //</editor-fold>

    //<editor-fold desc="Constructor">
    /**
     * Constructs a new SecurityPolicyReader with the specified factory and object mapper.
     *
     * @since 2.0.0
     * @author Markus Paulsen
     * @param objectMapper the non-null object mapper for reading files.
     */
    public SecurityPolicyReader(@Nonnull ObjectMapper objectMapper) {
        this.objectMapper = Preconditions.checkNotNull(objectMapper, "objectMapper must not be null");
    }
    //</editor-fold>

    //<editor-fold desc="Abstract methods">
    /**
     * Reads and parses a SecurityPolicy from the specified file path.
     *
     * @since 2.0.0
     * @author Markus Paulsen
     * @param securityPolicyPath the non-null file system path to the security policy file.
     * @return a non-null SecurityPolicy instance created from the file content.
     * @throws SecurityException if an error occurs during reading or parsing the file.
     */
    @Nonnull
    public abstract SecurityPolicy readSecurityPolicyFrom(@Nonnull Path securityPolicyPath);
    //</editor-fold>

    //<editor-fold desc="Static methods">
    public static SecurityPolicyReader selectSecurityPolicyReader(Path securityPolicyFilePath) {
        Preconditions.checkNotNull(securityPolicyFilePath, "securityPolicyFilePath must not be null");
        return switch (MoreFiles.getFileExtension(securityPolicyFilePath)) {
            case "yaml", "yml" ->
                SecurityPolicyYAMLReader.yamlBuilder()
                        .yamlMapper(new YAMLMapper())
                        .build();
            default -> throw new IllegalArgumentException("Unsupported security policy file format: " + MoreFiles.getFileExtension(securityPolicyFilePath));
        };
    }
    //</editor-fold>
}