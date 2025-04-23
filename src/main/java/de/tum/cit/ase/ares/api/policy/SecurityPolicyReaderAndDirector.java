package de.tum.cit.ase.ares.api.policy;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import de.tum.cit.ase.ares.api.policy.director.SecurityPolicyDirector;
import de.tum.cit.ase.ares.api.policy.reader.SecurityPolicyReader;
import de.tum.cit.ase.ares.api.securitytest.SecurityTestCaseAbstractFactoryAndBuilder;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;

/**
 * Security policy file reader and test case director.
 *
 * <p>Description:
 * Handles the reading of a security policy from a security policy file and manages the respective creation,
 * the writing, and the execution of security test cases.
 * Acts as a client of the Abstract Factory Design Pattern, and a director in the Builder Design Pattern.
 *
 * <p>Design Rationale: By separating the responsibilities of file reading and test case management, this class adheres to the
 * Single Responsibility Principle, ensuring that each component handles its specific functionality.
 *
 * @since 2.0.0
 * @author Markus Paulsen
 * @version 2.0.0
 * @see <a href="https://refactoring.guru/design-patterns/abstract-factory">Abstract Factory Design Pattern</a>
 * @see <a href="https://refactoring.guru/design-patterns/builder">Builder Design Pattern</a>
 */
public class SecurityPolicyReaderAndDirector {

    //<editor-fold desc="Attributes">
    /**
     * The reader for the security policy file (first dependency injection).
     */
    @Nonnull
    private final SecurityPolicyReader securityPolicyReader;

    /**
     * The director for creating security test cases based on the security policy (second dependency injection).
     */
    @Nonnull
    private final SecurityPolicyDirector securityPolicyDirector;

    /**
     * The path to the security policy file.
     */
    @Nullable
    private final Path securityPolicyFilePath;

    /**
     * The path to the project folder.
     */
    @Nullable
    private final Path projectFolderPath;

    /**
     * The manager for creating and handling security test cases.
     */
    @Nonnull
    private SecurityTestCaseAbstractFactoryAndBuilder securityTestCaseFactoryAndBuilder;
    //</editor-fold>

    //<editor-fold desc="Constructors">

    /**
     * Constructs a SecurityPolicyReaderAndDirector instance.
     *
     * @since 2.0.0
     * @author Markus Paulsen
     * @param securityPolicyReader the non-null reader for the security policy file.
     * @param securityPolicyDirector the non-null director for creating security test cases.
     * @param securityPolicyFilePath the path to the security policy file.
     * @param projectFolderPath the path to the project folder.
     */
    public SecurityPolicyReaderAndDirector(
            @Nonnull SecurityPolicyReader securityPolicyReader,
            @Nonnull SecurityPolicyDirector securityPolicyDirector,
            @Nullable Path securityPolicyFilePath,
            @Nullable Path projectFolderPath
    ) {
        this.securityPolicyReader = Preconditions.checkNotNull(securityPolicyReader, "securityPolicyReader must not be null");
        this.securityPolicyDirector = Preconditions.checkNotNull(securityPolicyDirector, "securityPolicyDirector must not be null");
        this.securityPolicyFilePath = securityPolicyFilePath;
        this.projectFolderPath = projectFolderPath;
        createSecurityTestCases();
    }
    //</editor-fold>

    //<editor-fold desc="Create method">

    /**
     * Creates the security test cases.
     *
     * @since 2.0.0
     * @author Markus Paulsen
     */
    public void createSecurityTestCases() {
        @Nullable SecurityPolicy securityPolicy = Optional
                .fromNullable(securityPolicyFilePath)
                .transform(securityPolicyReader::readSecurityPolicyFrom)
                .orNull();
        this.securityTestCaseFactoryAndBuilder = securityPolicyDirector.createSecurityTestCases(
                securityPolicy,
                projectFolderPath
        );
    }
    //</editor-fold>

    //<editor-fold desc="Write method">

    /**
     * Writes the security test cases to the project folder.
     *
     * @since 2.0.0
     * @author Markus Paulsen
     * @return a list of Paths where the security test cases were written.
     */
    @Nonnull
    public List<Path> writeSecurityTestCases(Path targetFolderPath) {
        return Preconditions.checkNotNull(this.securityTestCaseFactoryAndBuilder, "testCaseManager must not be null")
                .writeSecurityTestCases(targetFolderPath);
    }
    //</editor-fold>

    //<editor-fold desc="Execute method">

    /**
     * Executes the security test cases.
     *
     * @since 2.0.0
     * @author Markus Paulsen
     */
    public void executeSecurityTestCases() {
        Preconditions.checkNotNull(this.securityTestCaseFactoryAndBuilder, "testCaseManager must not be null")
                .executeSecurityTestCases();
    }
    //</editor-fold>

    // <editor-fold desc="Builder">
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        @Nullable
        private SecurityPolicyReader securityPolicyReader;
        @Nullable
        private SecurityPolicyDirector securityPolicyDirector;
        @Nullable
        private Path securityPolicyFilePath;
        @Nullable
        private Path projectFolderPath;

        @Nonnull
        public Builder securityPolicyReader(@Nonnull SecurityPolicyReader reader) {
            this.securityPolicyReader = Objects.requireNonNull(reader, "reader must not be null");
            return this;
        }

        @Nonnull
        public Builder securityPolicyDirector(@Nonnull SecurityPolicyDirector director) {
            this.securityPolicyDirector = Objects.requireNonNull(director, "director must not be null");
            return this;
        }

        @Nonnull
        public Builder securityPolicyFilePath(@Nullable Path securityPolicyFilePath) {
            this.securityPolicyFilePath = securityPolicyFilePath;
            return this;
        }

        @Nonnull
        public Builder projectFolderPath(@Nullable Path projectFolderPath) {
            this.projectFolderPath = projectFolderPath;
            return this;
        }

        @Nonnull
        public SecurityPolicyReaderAndDirector build() {
            return new SecurityPolicyReaderAndDirector(
                    Objects.requireNonNull(securityPolicyReader, "securityPolicyReader must not be null"),
                    Objects.requireNonNull(securityPolicyDirector, "securityPolicyDirector must not be null"),
                    securityPolicyFilePath,
                    projectFolderPath
            );
        }
    }
    // </editor-fold>


}