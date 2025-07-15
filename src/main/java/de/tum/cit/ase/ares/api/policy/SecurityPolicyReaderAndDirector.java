package de.tum.cit.ase.ares.api.policy;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.io.MoreFiles;
import de.tum.cit.ase.ares.api.policy.director.SecurityPolicyDirector;
import de.tum.cit.ase.ares.api.policy.reader.SecurityPolicyReader;
import de.tum.cit.ase.ares.api.securitytest.TestCaseAbstractFactoryAndBuilder;
import org.apache.commons.io.FilenameUtils;

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
@SuppressWarnings("unused")
public class SecurityPolicyReaderAndDirector {

    //<editor-fold desc="Attributes">
    /**
     * The reader for the security policy file (first dependency injection).
     */
    @Nullable
    private SecurityPolicyReader securityPolicyReader;

    /**
     * The director for creating security test cases based on the security policy (second dependency injection).
     */
    @Nullable
    private SecurityPolicyDirector securityPolicyDirector;

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
    @Nullable
    private TestCaseAbstractFactoryAndBuilder securityTestCaseFactoryAndBuilder;
    //</editor-fold>

    //<editor-fold desc="Constructors">

    /**
     * Constructs a SecurityPolicyReaderAndDirector instance.
     *
     * @since 2.0.0
     * @author Markus Paulsen
     * @param securityPolicyFilePath the path to the security policy file.
     * @param projectFolderPath the path to the project folder.
     */
    public SecurityPolicyReaderAndDirector(
            @Nullable Path securityPolicyFilePath,
            @Nullable Path projectFolderPath
    ) {
        this.securityPolicyFilePath = securityPolicyFilePath;
        this.projectFolderPath = projectFolderPath;
    }
    //</editor-fold>

    //<editor-fold desc="Create method">

    /**
     * Creates the security test cases.
     *
     * @since 2.0.0
     * @author Markus Paulsen
     */
    public SecurityPolicyReaderAndDirector createTestCases() {
        @Nullable SecurityPolicy securityPolicy = Optional
                .fromNullable(securityPolicyFilePath)
                .transform(securityPolicyFilePath -> {
                    securityPolicyReader = SecurityPolicyReader.selectSecurityPolicyReader(this.securityPolicyFilePath);
                    if(securityPolicyReader != null) {
                        return securityPolicyReader.readSecurityPolicyFrom(securityPolicyFilePath);
                    } else {
                        throw new IllegalArgumentException("No suitable SecurityPolicyReader found for the provided file path: " + securityPolicyFilePath);
                    }
                })
                .orNull();
        this.securityTestCaseFactoryAndBuilder = Optional
                .fromNullable(securityPolicy)
                .transform(securityPolicyExisting -> {
                    securityPolicyDirector = SecurityPolicyDirector.selectSecurityPolicyDirector(securityPolicyExisting);
                    return securityPolicyDirector.createTestCases(securityPolicyExisting, projectFolderPath);
                })
                .orNull();
                //.or(SecurityPolicyDirector.selectSecurityPolicyDirector(null).createTestCases(null, projectFolderPath));
        return this;
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
    public List<Path> writeTestCases(Path testFolderPath) {
        return Preconditions.checkNotNull(this.securityTestCaseFactoryAndBuilder, "securityTestCaseFactoryAndBuilder must not be null")
                .writeTestCases(testFolderPath);
    }

    /**
     * Writes the security test cases to the project folder and continues.
     *
     * @since 2.0.0
     * @author Markus Paulsen
     * @return a list of Paths where the security test cases were written.
     */
    @Nonnull
    public SecurityPolicyReaderAndDirector writeTestCasesAndContinue(Path testFolderPath) {
        Preconditions.checkNotNull(this.securityTestCaseFactoryAndBuilder, "securityTestCaseFactoryAndBuilder must not be null")
                .writeTestCases(testFolderPath);
        return this;
    }
    //</editor-fold>

    //<editor-fold desc="Execute method">

    /**
     * Executes the security test cases.
     *
     * @since 2.0.0
     * @author Markus Paulsen
     */
    public SecurityPolicyReaderAndDirector executeTestCases() {
        Preconditions.checkNotNull(this.securityTestCaseFactoryAndBuilder, "securityTestCaseFactoryAndBuilder must not be null")
                .executeTestCases();
        return this;
    }
    //</editor-fold>

    // <editor-fold desc="Builder">
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        @Nullable
        private Path securityPolicyFilePath;
        @Nullable
        private Path projectFolderPath;

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
                    securityPolicyFilePath,
                    projectFolderPath
            );
        }
    }
    // </editor-fold>

}