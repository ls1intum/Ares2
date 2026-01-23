package de.tum.cit.ase.ares.api.securitytest.java.essentialModel.yaml;

import java.io.IOException;
import java.nio.file.Path;

import javax.annotation.Nonnull;

import com.fasterxml.jackson.core.exc.StreamReadException;
import com.fasterxml.jackson.databind.DatabindException;
import com.google.common.base.Preconditions;

import de.tum.cit.ase.ares.api.securitytest.java.essentialModel.EssentialClasses;
import de.tum.cit.ase.ares.api.securitytest.java.essentialModel.EssentialDataReader;
import de.tum.cit.ase.ares.api.securitytest.java.essentialModel.EssentialPackages;
import de.tum.cit.ase.ares.api.util.FileTools;

/**
 * Reads essential configurations from YAML files.
 *
 * <p>
 * Description: This class implements the EssentialReader interface to provide
 * functionality for reading essential classes and packages from YAML files. It
 * utilises Jackson's YAMLFactory and ObjectMapper for parsing YAML content.
 * </p>
 *
 * <p>
 * Design Rationale: By encapsulating YAML parsing logic in this class, it
 * allows for clear separation of concerns and easy modification of the parsing
 * mechanism.
 * </p>
 *
 * @since 2.0.0
 * @author Markus Paulsen
 * @version 2.0.0
 */
public class EssentialDataYAMLReader implements EssentialDataReader {

	/**
	 * Reads essential classes from the specified YAML file.
	 *
	 * @since 2.0.0
	 * @author Markus Paulsen
	 * @param essentialClassesPath the path to the YAML file containing essential
	 *            classes
	 * @return an EssentialClasses instance parsed from the YAML file
	 */
	@Override
	@Nonnull
	public EssentialClasses readEssentialClassesFrom(@Nonnull Path essentialClassesPath) {
		@Nonnull
		Path path = Preconditions.checkNotNull(essentialClassesPath, "The essential classes path must not be null!");
		@Nonnull
		Class<EssentialClasses> yamlClass = Preconditions.checkNotNull(EssentialClasses.class, "The essential classes class must not be null!");
		try {
			return FileTools.readYamlFile(FileTools.readFile(path), yamlClass);
		} catch (StreamReadException e) {
			throwReaderErrorMessage("essential.classes.read.failed", path.toString(), e);
		} catch (DatabindException e) {
			throwReaderErrorMessage("essential.classes.data.bind.failed", path.toString(), e);
		} catch (UnsupportedOperationException e) {
			throwReaderErrorMessage("essential.classes.unsupported.operation", path.toString(), e);
		} catch (IOException e) {
			throwReaderErrorMessage("security.policy.io.exception", path.toString(), e);
		}
		return null;
	}

	/**
	 * Reads essential packages from the specified YAML file.
	 *
	 * @since 2.0.0
	 * @author Markus Paulsen
	 * @param essentialPackagesPath the path to the YAML file containing essential
	 *            packages
	 * @return an EssentialPackages instance parsed from the YAML file
	 */
	@Override
	@Nonnull
	public EssentialPackages readEssentialPackagesFrom(@Nonnull Path essentialPackagesPath) {
		@Nonnull
		Path path = Preconditions.checkNotNull(essentialPackagesPath, "The essential packages path must not be null!");
		@Nonnull
		Class<EssentialPackages> yamlClass = Preconditions.checkNotNull(EssentialPackages.class, "The essential packages class must not be null!");
		try {
			return FileTools.readYamlFile(FileTools.readFile(path), yamlClass);
		} catch (StreamReadException e) {
			throwReaderErrorMessage("essential.packages.read.failed", path.toString(), e);
		} catch (DatabindException e) {
			throwReaderErrorMessage("essential.packages.data.bind.failed", path.toString(), e);
		} catch (UnsupportedOperationException e) {
			throwReaderErrorMessage("essential.packages.unsupported.operation", path.toString(), e);
		} catch (IOException e) {
			throwReaderErrorMessage("security.policy.io.exception", path.toString(), e);
		}
		return null;
	}
}