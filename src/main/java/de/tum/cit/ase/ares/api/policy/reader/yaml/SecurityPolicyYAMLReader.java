package de.tum.cit.ase.ares.api.policy.reader.yaml;

import com.fasterxml.jackson.core.exc.StreamReadException;
import com.fasterxml.jackson.databind.DatabindException;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import com.google.common.base.Preconditions;
import de.tum.cit.ase.ares.api.aop.java.instrumentation.advice.JavaInstrumentationAdviceFileSystemToolbox;
import org.apache.commons.io.FileUtils;
import de.tum.cit.ase.ares.api.policy.SecurityPolicy;
import de.tum.cit.ase.ares.api.policy.reader.SecurityPolicyReader;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

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
public class SecurityPolicyYAMLReader extends SecurityPolicyReader {

    //<editor-fold desc="Constructor">
    /**
     * Constructs a new SecurityPolicyYAMLReader with the specified YAML factory and object mapper.
     *
     * @since 2.0.0
     * @author Markus Paulsen
     * @param yamlMapper the non-null YAML object mapper for reading YAML files.
     */
    public SecurityPolicyYAMLReader(@Nonnull YAMLMapper yamlMapper) {
        super(yamlMapper);
    }
    //</editor-fold>

    //<editor-fold desc="Read policy methods">
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
        Preconditions.checkNotNull(securityPolicyPath, "Security policy path must not be null");
        Preconditions.checkArgument(FileUtils.isFileNewer(securityPolicyPath.toFile(), 0), JavaInstrumentationAdviceFileSystemToolbox.localize("security.policy.file.not.found", securityPolicyPath));
        Preconditions.checkArgument(Files.isReadable(securityPolicyPath), JavaInstrumentationAdviceFileSystemToolbox.localize("security.policy.file.not.readable", securityPolicyPath));
        @Nonnull File yamlFile = Preconditions.checkNotNull(securityPolicyPath.toFile(), "The security policy file must not be null.");
        @Nonnull Class<SecurityPolicy> yamlClass = Preconditions.checkNotNull(SecurityPolicy.class, "The security policy class must not be null.");
        try {
            return objectMapper.readValue(yamlFile, yamlClass);
        } catch (StreamReadException e) {
            throw new SecurityException(JavaInstrumentationAdviceFileSystemToolbox.localize("security.policy.read.failed", securityPolicyPath.toString()), e);
        } catch (DatabindException e) {
            throw new SecurityException(JavaInstrumentationAdviceFileSystemToolbox.localize("security.policy.data.bind.failed", securityPolicyPath.toString()), e);
        } catch (UnsupportedOperationException e) {
            throw new SecurityException(JavaInstrumentationAdviceFileSystemToolbox.localize("security.policy.unsupported.operation"), e);
        } catch (IOException e) {
            throw new SecurityException(JavaInstrumentationAdviceFileSystemToolbox.localize("security.policy.io.exception", securityPolicyPath.toString()), e);
        }
    }
    //</editor-fold>

    //<editor-fold desc="Builder">
    public static Builder yamlBuilder() {
        return new Builder();
    }

    public static class Builder {
        @Nullable
        private YAMLMapper yamlMapper;

        @Nonnull
        public Builder yamlMapper(@Nullable YAMLMapper yamlMapper) {
            this.yamlMapper = Objects.requireNonNull(yamlMapper, "yamlMapper must not be null");
            return this;
        }

        @Nonnull
        public SecurityPolicyYAMLReader build() {
            return new SecurityPolicyYAMLReader(
                    Objects.requireNonNull(yamlMapper, "yamlMapper must not be null")
            );
        }
    }
    //</editor-fold>
}