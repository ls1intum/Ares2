package de.tum.cit.ase.ares.api.policy.director;

import com.google.common.base.Preconditions;
import de.tum.cit.ase.ares.api.policy.SecurityPolicy;
import de.tum.cit.ase.ares.api.policy.director.java.SecurityPolicyJavaDirector;
import de.tum.cit.ase.ares.api.securitytest.TestCaseAbstractFactoryAndBuilder;
import de.tum.cit.ase.ares.api.securitytest.java.creator.Creator;
import de.tum.cit.ase.ares.api.securitytest.java.creator.JavaCreator;
import de.tum.cit.ase.ares.api.securitytest.java.essentialModel.EssentialDataReader;
import de.tum.cit.ase.ares.api.securitytest.java.essentialModel.yaml.EssentialDataYAMLReader;
import de.tum.cit.ase.ares.api.securitytest.java.executer.Executer;
import de.tum.cit.ase.ares.api.securitytest.java.executer.JavaExecuter;
import de.tum.cit.ase.ares.api.securitytest.java.projectScanner.JavaProjectScanner;
import de.tum.cit.ase.ares.api.securitytest.java.projectScanner.ProjectScanner;
import de.tum.cit.ase.ares.api.securitytest.java.writer.JavaWriter;
import de.tum.cit.ase.ares.api.securitytest.java.writer.Writer;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.nio.file.Path;

/**
 * Abstract class for directing the creation of security test cases.
 *
 * <p>Description: This interface defines the contract for a director that creates security test cases based on a given SecurityPolicy.
 * It abstracts the instantiation and configuration process to a concrete implementation, working in tandem with an abstract factory
 * to ensure that test cases are built in a consistent and modular manner.
 *
 * <p>Design Rationale: This interface supports loose coupling by decoupling the test case creation logic from other system components.
 * Its usage ensures adherence to the Single Responsibility Principle and makes the security test case generation process transparent and maintainable.
 *
 * @since 2.0.0
 * @author Markus Paulsen
 * @version 2.0.0
 */
public abstract class SecurityPolicyDirector {

    //<editor-fold desc="Attributes">
    /**
     * Creator for Java security test cases.
     */
    @Nonnull
    protected final Creator creator;

    /**
     * Writer for Java security test cases.
     */
    @Nonnull
    protected final Writer writer;

    /**
     * Executer for Java security test cases.
     */
    @Nonnull
    protected final Executer executer;

    /**
     * Reader for essential classes YAML files.
     */
    @Nonnull
    protected final EssentialDataReader essentialDataReader;

    /**
     * Scanner for Java programming exercises.
     */
    @Nonnull
    protected final ProjectScanner projectScanner;

    /**
     * Path to the essential packages YAML file.
     */
    @Nonnull
    protected final Path essentialPackagesPath;

    /**
     * Path to the essential classes YAML file.
     */
    @Nonnull
    protected final Path essentialClassesPath;
    //</editor-fold>

    //<editor-fold desc="Constructor">
    /**
     * Constructs a new SecurityPolicyDirector with provided dependencies.
     *
     * @since 2.0.0
     * @author Markus Paulsen
     * @param creator the creator for Java security test cases; must not be null.
     * @param writer the writer for Java security test cases; must not be null.
     * @param executer the executer for Java security test cases; must not be null.
     * @param essentialDataReader the reader for essential data files; must not be null.
     * @param projectScanner the scanner for Java programming exercises; must not be null.
     * @param essentialPackagesPath the path to the essential packages YAML file; must not be null.
     * @param essentialClassesPath the path to the essential classes YAML file; must not be null.
     */
    public SecurityPolicyDirector(@Nonnull Creator creator, @Nonnull Writer writer, @Nonnull Executer executer, @Nonnull EssentialDataYAMLReader essentialDataReader, @Nonnull ProjectScanner projectScanner, @Nonnull Path essentialPackagesPath, @Nonnull Path essentialClassesPath) {
        this.creator = Preconditions.checkNotNull(creator, "creator must not be null");
        this.writer = Preconditions.checkNotNull(writer, "writer must not be null");
        this.executer = Preconditions.checkNotNull(executer, "executer must not be null");
        this.essentialDataReader = Preconditions.checkNotNull(essentialDataReader, "essentialDataReader must not be null");
        this.projectScanner = Preconditions.checkNotNull(projectScanner, "javaScanner must not be null");
        this.essentialPackagesPath = Preconditions.checkNotNull(essentialPackagesPath, "essentialPackagesPath must not be null");
        this.essentialClassesPath = Preconditions.checkNotNull(essentialClassesPath, "essentialClassesPath must not be null");
    }
    //</editor-fold>

    //<editor-fold desc="Abstract methods ">
    /**
     * Creates and configures security test cases based on the provided SecurityPolicy and project path.
     *
     * @since 2.0.0
     * @author Markus Paulsen
     * @param securityPolicy the SecurityPolicy driving the test case creation; may be null if no policy is provided.
     * @param projectFolderPath the project directory path where test cases should be applied; may be null.
     * @return a non-null instance of TestCaseAbstractFactoryAndBuilder configured according to the security policy.
     */
    @Nonnull
    public abstract TestCaseAbstractFactoryAndBuilder createTestCases(@Nullable SecurityPolicy securityPolicy, @Nullable Path projectFolderPath);
    //</editor-fold>

    //<editor-fold desc="Static methods">
    @Nonnull
    public static SecurityPolicyDirector selectSecurityPolicyDirector(@Nullable SecurityPolicy securityPolicy) {
        if(securityPolicy == null) {
            return SecurityPolicyJavaDirector.javaBuilder()
                    .creator(new JavaCreator())
                    .writer(new JavaWriter())
                    .executer(new JavaExecuter())
                    .essentialDataReader(new EssentialDataYAMLReader())
                    .javaScanner(new JavaProjectScanner())
                    .essentialPackagesPath(SecurityPolicyJavaDirector.DEFAULT_ESSENTIAL_PACKAGES_PATH)
                    .essentialClassesPath(SecurityPolicyJavaDirector.DEFAULT_ESSENTIAL_CLASSES_PATH)
                    .build();
        } else {
            return switch (securityPolicy.regardingTheSupervisedCode().theFollowingProgrammingLanguageConfigurationIsUsed()) {
                case JAVA_USING_GRADLE_ARCHUNIT_AND_ASPECTJ, JAVA_USING_GRADLE_ARCHUNIT_AND_INSTRUMENTATION, JAVA_USING_GRADLE_WALA_AND_ASPECTJ, JAVA_USING_GRADLE_WALA_AND_INSTRUMENTATION, JAVA_USING_MAVEN_ARCHUNIT_AND_ASPECTJ, JAVA_USING_MAVEN_ARCHUNIT_AND_INSTRUMENTATION, JAVA_USING_MAVEN_WALA_AND_ASPECTJ, JAVA_USING_MAVEN_WALA_AND_INSTRUMENTATION ->
                        SecurityPolicyJavaDirector.javaBuilder()
                                .creator(new JavaCreator())
                                .writer(new JavaWriter())
                                .executer(new JavaExecuter())
                                .essentialDataReader(new EssentialDataYAMLReader())
                                .javaScanner(new JavaProjectScanner())
                                .essentialPackagesPath(SecurityPolicyJavaDirector.DEFAULT_ESSENTIAL_PACKAGES_PATH)
                                .essentialClassesPath(SecurityPolicyJavaDirector.DEFAULT_ESSENTIAL_CLASSES_PATH)
                                .build();
            };
        }
    }
    //</editor-fold>
}