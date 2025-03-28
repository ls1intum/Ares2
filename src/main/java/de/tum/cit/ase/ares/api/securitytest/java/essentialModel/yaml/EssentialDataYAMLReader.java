package de.tum.cit.ase.ares.api.securitytest.java.essentialModel.yaml;

import com.fasterxml.jackson.core.exc.StreamReadException;
import com.fasterxml.jackson.databind.DatabindException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import de.tum.cit.ase.ares.api.securitytest.java.essentialModel.EssentialClasses;
import de.tum.cit.ase.ares.api.securitytest.java.essentialModel.EssentialDataReader;
import de.tum.cit.ase.ares.api.securitytest.java.essentialModel.EssentialPackages;
import de.tum.cit.ase.ares.api.util.FileTools;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Objects;

/**
 * Reads essential configurations from YAML files.
 *
 * <p>Description: This class implements the EssentialReader interface to provide functionality for reading essential classes and packages from YAML files.
 * It utilises Jackson's YAMLFactory and ObjectMapper for parsing YAML content.</p>
 *
 * <p>Design Rationale: By encapsulating YAML parsing logic in this class, it allows for clear separation of concerns and easy modification of the parsing mechanism.</p>
 *
 * @since 2.0.0
 * @author Markus Paulsen
 * @version 2.0.0
 */
public class EssentialDataYAMLReader implements EssentialDataReader {

    /**
     * YAMLFactory instance used to create a YAML parser.
     */
    @Nonnull
    private final YAMLFactory yamlFactory = new YAMLFactory();

    /**
     * ObjectMapper instance configured with YAMLFactory for parsing YAML files.
     */
    @Nonnull
    private final ObjectMapper yamlMapper = new ObjectMapper(yamlFactory);

    /**
     * Reads a YAML file and maps its content to the specified type.
     * This method never returns null and throws an exception if mapping fails.
     *
     * @since 2.0.0
     * @author Markus Paulsen
     * @param path the path to the YAML file
     * @param valueType the target class type for mapping
     * @param errorMessagePrefix prefix for error messages in case of failure
     * @param <T> the type parameter for the mapped object
     * @return the mapped object of type T
     * @throws IllegalStateException if mapping fails due to any exception
     */
    @Nonnull
    private <T> T readYamlFile(@Nonnull Path path, @Nonnull Class<T> valueType, @Nonnull String errorMessagePrefix) {
        try {
            File yamlFile = FileTools.getResourceAsFile(path.toString());
            return Objects.requireNonNull(yamlMapper.readValue(yamlFile, valueType),
                    () -> errorMessagePrefix + ".mapping.result.null");
        } catch (StreamReadException e) {
            throwReaderErrorMessage(errorMessagePrefix + ".read.failed", path.toString(), e);
        } catch (DatabindException e) {
            throwReaderErrorMessage(errorMessagePrefix + ".data.bind.failed", path.toString(), e);
        } catch (UnsupportedOperationException e) {
            throwReaderErrorMessage(errorMessagePrefix + ".unsupported.operation", path.toString(), e);
        } catch (IOException e) {
            throwReaderErrorMessage(errorMessagePrefix + ".io.exception", path.toString(), e);
        } catch (Exception e) {
            throwReaderErrorMessage(errorMessagePrefix + ".unknown.exception", path.toString(), e);
        }
        throw new IllegalStateException(errorMessagePrefix + ".unexpected.error");
    }

    /**
     * Reads essential classes from the specified YAML file.
     *
     * @since 2.0.0
     * @author Markus Paulsen
     * @param essentialClassesPath the path to the YAML file containing essential classes
     * @return an EssentialClasses instance parsed from the YAML file
     */
    @Override
    @Nonnull
    public EssentialClasses readEssentialClassesFrom(@Nonnull Path essentialClassesPath) {
        return readYamlFile(essentialClassesPath, EssentialClasses.class, "essential.classes");
    }

    /**
     * Reads essential packages from the specified YAML file.
     *
     * @since 2.0.0
     * @author Markus Paulsen
     * @param essentialPackagesPath the path to the YAML file containing essential packages
     * @return an EssentialPackages instance parsed from the YAML file
     */
    @Override
    @Nonnull
    public EssentialPackages readEssentialPackagesFrom(@Nonnull Path essentialPackagesPath) {
        return readYamlFile(essentialPackagesPath, EssentialPackages.class, "essential.packages");
    }
}