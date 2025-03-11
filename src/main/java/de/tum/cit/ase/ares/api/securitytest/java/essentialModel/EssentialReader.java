package de.tum.cit.ase.ares.api.securitytest.java.essentialModel;

import javax.annotation.Nonnull;
import java.nio.file.Path;

import static de.tum.cit.ase.ares.api.aop.java.instrumentation.advice.JavaInstrumentationAdviceFileSystemToolbox.localize;

/**
 * Interface for reading essential configurations from YAML files.
 *
 * <p>Description: This interface defines methods for reading essential classes and packages from YAML files.
 * Implementations should provide mechanisms for parsing YAML configuration files.</p>
 *
 * <p>Design Rationale: Abstracting the reading of essential configuration data into an interface allows for flexible and interchangeable implementations.</p>
 *
 * @since 2.0.0
 * @author Markus Paulsen
 * @version 2.0.0
 */
public interface EssentialReader {

    /**
     * Reads essential classes from the specified YAML file.
     *
     * @since 2.0.0
     * @author Markus Paulsen
     * @param securityPolicyPath the path to the YAML file containing essential classes
     * @return an EssentialClasses instance parsed from the YAML file
     */
    EssentialClasses readEssentialClassesFrom(@Nonnull Path securityPolicyPath);

    /**
     * Reads essential packages from the specified YAML file.
     *
     * @since 2.0.0
     * @author Markus Paulsen
     * @param securityPolicyPath the path to the YAML file containing essential packages
     * @return an EssentialPackages instance parsed from the YAML file
     */
    EssentialPackages readEssentialPackagesFrom(@Nonnull Path securityPolicyPath);

    /**
     * Throws a SecurityException with a localized error message.
     *
     * @since 2.0.0
     * @author Markus Paulsen
     * @param errorMessageIdentifier the identifier for the error message
     * @param errorMessageParameter a parameter to be included in the error message
     * @param e the exception that triggered the error
     */
    default void throwReaderErrorMessage(String errorMessageIdentifier, String errorMessageParameter, Exception e) {
        throw new SecurityException(localize(errorMessageIdentifier, errorMessageParameter), e);
    }
}