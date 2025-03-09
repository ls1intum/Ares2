package de.tum.cit.ase.ares.api.policy.reader.yaml;

import com.fasterxml.jackson.core.exc.StreamReadException;
import com.fasterxml.jackson.databind.DatabindException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import de.tum.cit.ase.ares.api.policy.SecurityPolicy;
import de.tum.cit.ase.ares.api.policy.reader.SecurityPolicyReader;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

import static de.tum.cit.ase.ares.api.aop.java.instrumentation.advice.JavaInstrumentationAdviceToolbox.localize;

/**
 * YAML security policy reader.
 *
 * <p>Description: Reads a {@link SecurityPolicy} from a YAML file using the Jackson YAML library.
 * It validates the provided file path and translates common exceptions into a {@code SecurityException}
 * to clearly signal issues during policy reading.
 *
 * <p>Design Rationale: By employing Jackson's YAMLFactory and ObjectMapper, this class minimises boilerplate
 * and ensures efficient parsing. Its focused responsibility and robust exception handling align with modern
 * best practices for immutable and maintainable code.
 *
 * @since 2.0.0
 * @author Markus Paulsen
 * @version 2.0.0
 */
@SuppressWarnings("FieldCanBeLocal")
public class SecurityPolicyYAMLReader implements SecurityPolicyReader {

    /**
     * YAML factory for parsing YAML files.
     */
    @Nonnull
    private final YAMLFactory yamlFactory;

    /**
     * YAML object mapper for reading YAML files.
     */
    @Nonnull
    private final ObjectMapper yamlMapper;

    /**
     * Constructs a new SecurityPolicyYAMLReader with default YAML parsing settings.
     *
     * @since 2.0.0
     * @author Markus Paulsen
     */
    public SecurityPolicyYAMLReader() {
        this(
                new YAMLFactory(),
                new ObjectMapper(new YAMLFactory())
        );
    }

    /**
     * Constructs a new SecurityPolicyYAMLReader with the specified YAML factory and object mapper.
     *
     * @since 2.0.0
     * @author Markus Paulsen
     * @param yamlFactory the non-null YAML factory for parsing YAML files.
     * @param yamlMapper the non-null YAML object mapper for reading YAML files.
     */
    public SecurityPolicyYAMLReader(@Nonnull YAMLFactory yamlFactory, @Nonnull ObjectMapper yamlMapper) {
        this.yamlFactory = yamlFactory;
        this.yamlMapper = yamlMapper;
    }

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
        Objects.requireNonNull(securityPolicyPath, "Security policy path must not be null");
        if (!Files.exists(securityPolicyPath)) {
            throw new SecurityException(localize("security.policy.file.not.found", securityPolicyPath));
        }
        if (!Files.isReadable(securityPolicyPath)) {
            throw new SecurityException(localize("security.policy.file.not.readable", securityPolicyPath));
        }
        File yamlFile = Objects.requireNonNull(securityPolicyPath.toFile(), "The security policy file must not be null.");
        Class<SecurityPolicy> yamlClass = Objects.requireNonNull(SecurityPolicy.class, "The security policy class must not be null.");
        try {
            return yamlMapper.readValue(yamlFile, yamlClass);
        } catch (StreamReadException e) {
            throw new SecurityException(localize("security.policy.read.failed", securityPolicyPath.toString()), e);
        } catch (DatabindException e) {
            throw new SecurityException(localize("security.policy.data.bind.failed", securityPolicyPath.toString()), e);
        } catch (UnsupportedOperationException e) {
            throw new SecurityException(localize("security.policy.unsupported.operation"), e);
        } catch (IOException e) {
            throw new SecurityException(localize("security.policy.io.exception", securityPolicyPath.toString()), e);
        }
    }
}