package de.tum.cit.ase.ares.api.securitytest.java.essentialModel.yaml;

import com.fasterxml.jackson.core.exc.StreamReadException;
import com.fasterxml.jackson.databind.DatabindException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import de.tum.cit.ase.ares.api.securitytest.java.essentialModel.EssentialClasses;
import de.tum.cit.ase.ares.api.securitytest.java.essentialModel.EssentialDataReader;
import de.tum.cit.ase.ares.api.securitytest.java.essentialModel.EssentialPackages;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

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
    private final YAMLFactory yamlFactory = new YAMLFactory();

    /**
     * ObjectMapper instance configured with YAMLFactory for parsing YAML files.
     */
    private final ObjectMapper yamlMapper = new ObjectMapper(yamlFactory);

    /**
     * Default constructor for EssentialYAMLReader.
     *
     * @since 2.0.0
     * @author Markus Paulsen
     */
    public EssentialDataYAMLReader() {
    }

    /**
     * Reads a YAML file and maps its content to the specified type.
     *
     * @since 2.0.0
     * @author Markus Paulsen
     * @param path the path to the YAML file
     * @param valueType the target class type for mapping
     * @param errorMessagePrefix prefix for error messages in case of failure
     * @param <T> the type parameter for the mapped object
     * @return the mapped object of type T, or null if an error occurs
     */
    private <T> T readYamlFile(Path path, Class<T> valueType, String errorMessagePrefix) {
        try {
            File yamlFile = path.toFile();
            return yamlMapper.readValue(yamlFile, valueType);
        } catch (StreamReadException e) {
            throwReaderErrorMessage(errorMessagePrefix + ".read.failed", path.toString(), e);
        } catch (DatabindException e) {
            throwReaderErrorMessage(errorMessagePrefix + ".data.bind.failed", path.toString(), e);
        } catch (UnsupportedOperationException e) {
            throwReaderErrorMessage(errorMessagePrefix + ".unsupported.operation", path.toString(), e);
        } catch (IOException e) {
            throwReaderErrorMessage(errorMessagePrefix + ".io.exception", path.toString(), e);
        }
        return null;
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
    public EssentialPackages readEssentialPackagesFrom(@Nonnull Path essentialPackagesPath) {
        return readYamlFile(essentialPackagesPath, EssentialPackages.class, "essential.packages");
    }
}