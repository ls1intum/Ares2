package de.tum.cit.ase.ares.api.policy.reader;

import de.tum.cit.ase.ares.api.policy.SecurityPolicy;
import javax.annotation.Nonnull;
import java.nio.file.Path;

/**
 * Functional interface for reading a SecurityPolicy from a file.
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
@FunctionalInterface
public interface SecurityPolicyReader {

    /**
     * Reads and parses a SecurityPolicy from the specified file path.
     *
     * @since 2.0.0
     * @author Markus Paulsen
     * @param securityPolicyPath the non-null file system path to the security policy file.
     * @return a non-null SecurityPolicy instance created from the file content.
     * @throws SecurityException if an error occurs during reading or parsing the file.
     */
    SecurityPolicy readSecurityPolicyFrom(@Nonnull Path securityPolicyPath);
}