package de.tum.cit.ase.ares.api.policy.director.java;

import de.tum.cit.ase.ares.api.aop.java.JavaAOPMode;
import de.tum.cit.ase.ares.api.architecture.java.JavaArchitectureMode;
import de.tum.cit.ase.ares.api.buildtoolconfiguration.java.JavaBuildMode;
import de.tum.cit.ase.ares.api.policy.SecurityPolicy;
import de.tum.cit.ase.ares.api.policy.director.SecurityPolicyDirector;
import de.tum.cit.ase.ares.api.policy.policySubComponents.ProgrammingLanguageConfiguration;
import de.tum.cit.ase.ares.api.securitytest.SecurityTestCaseAbstractFactoryAndBuilder;
import de.tum.cit.ase.ares.api.securitytest.java.JavaSecurityTestCaseFactoryAndBuilder;
import de.tum.cit.ase.ares.api.securitytest.java.essentialModel.EssentialYAMLReader;
import de.tum.cit.ase.ares.api.securitytest.java.scanner.JavaProgrammingExerciseScanner;
import de.tum.cit.ase.ares.api.securitytest.java.scanner.JavaScanner;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.nio.file.Path;
import java.util.Objects;

/**
 * Java security policy director.
 *
 * <p>Description: A director for creating security test cases for Java projects based on a given SecurityPolicy.
 * It creates and configures instances of JavaSecurityTestCaseFactoryAndBuilder using information extracted from the provided
 * security policy. If no security policy is provided, a default configuration is used.
 *
 * <p>Design Rationale: This class encapsulates the logic required to select the appropriate Java build mode, architecture mode,
 * and AOP mode based on the security policy. It utilises a switch expression to map each ProgrammingLanguageConfiguration to a
 * corresponding test case factory configuration, and the use of an essential classes YAML reader abstracts the details of reading
 * supporting metadata, adhering to the Single Responsibility Principle.
 *
 * @since 2.0.0
 * @author Markus Paulsen
 * @version 2.0.0
 */
public class SecurityPolicyJavaDirector implements SecurityPolicyDirector {

    /**
     * Default path to the essential packages YAML file.
     */
    @Nonnull
    private static final String DEFAULT_ESSENTIAL_PACKAGES_PATH = "src/main/java/de/tum/cit/ase/ares/api/securitytest/java/essentialModel/EssentialPackages.yaml";

    /**
     * Default path to the essential classes YAML file.
     */
    @Nonnull
    private static final String DEFAULT_ESSENTIAL_CLASSES_PATH = "src/main/java/de/tum/cit/ase/ares/api/securitytest/java/essentialModel/EssentialClasses.yaml";

    /**
     * Reader for essential classes YAML files.
     */
    @Nonnull
    private final EssentialYAMLReader essentialYAMLReader;

    /**
     * Scanner for Java programming exercises.
     */
    @Nonnull
    private final JavaScanner javaScanner;

    /**
     * Path to the essential packages YAML file.
     */
    @Nonnull
    private final Path essentialPackagesPath;

    /**
     * Path to the essential classes YAML file.
     */
    @Nonnull
    private final Path essentialClassesPath;

    /**
     * Constructs a new SecurityPolicyJavaDirector with default settings.
     *
     * @since 2.0.0
     * @author Markus Paulsen
     */
    public SecurityPolicyJavaDirector() {
        this(new EssentialYAMLReader(), new JavaProgrammingExerciseScanner(), Path.of(DEFAULT_ESSENTIAL_PACKAGES_PATH), Path.of(DEFAULT_ESSENTIAL_CLASSES_PATH));
    }

    /**
     * Constructs a new SecurityPolicyJavaDirector with provided dependencies.
     *
     * @since 2.0.0
     * @author Markus Paulsen
     * @param essentialYAMLReader the reader for essential classes YAML files; must not be null.
     * @param javaScanner the scanner for Java programming exercises; must not be null.
     * @param essentialPackagesPath the path to the essential packages YAML file; must not be null.
     * @param essentialClassesPath the path to the essential classes YAML file; must not be null.
     */
    public SecurityPolicyJavaDirector(@Nonnull EssentialYAMLReader essentialYAMLReader, @Nonnull JavaScanner javaScanner, @Nonnull Path essentialPackagesPath, @Nonnull Path essentialClassesPath) {
        this.essentialYAMLReader = Objects.requireNonNull(essentialYAMLReader, "Essential YAML reader must not be null");
        this.javaScanner = Objects.requireNonNull(javaScanner, "Java scanner must not be null");
        this.essentialPackagesPath = Objects.requireNonNull(essentialPackagesPath, "Essential packages path must not be null");
        this.essentialClassesPath = Objects.requireNonNull(essentialClassesPath, "Essential classes path must not be null");
    }

    /**
     * Generates a JavaSecurityTestCaseFactoryAndBuilder with the provided configuration.
     *
     * @since 2.0.0
     * @author Markus Paulsen
     * @param javaBuildMode the Java build mode to use; may be null.
     * @param javaArchitectureMode the Java architecture mode to use; may be null.
     * @param javaAOPMode the Java AOP mode to use; may be null.
     * @param projectPath the project directory path where test cases will be applied; may be null.
     * @param securityPolicy the security policy to base test case creation on; may be null.
     * @return a non-null instance of JavaSecurityTestCaseFactoryAndBuilder configured according to the provided parameters.
     */
    private SecurityTestCaseAbstractFactoryAndBuilder generateJavaSecurityTestCaseFactoryAndBuilder(@Nullable JavaBuildMode javaBuildMode, @Nullable JavaArchitectureMode javaArchitectureMode, @Nullable JavaAOPMode javaAOPMode, @Nullable Path projectPath, SecurityPolicy securityPolicy) {
        return new JavaSecurityTestCaseFactoryAndBuilder(essentialYAMLReader, essentialPackagesPath, essentialClassesPath, javaScanner, javaBuildMode, javaArchitectureMode, javaAOPMode, projectPath, securityPolicy);

    }

    /**
     * Creates security test cases based on the provided security policy and project path.
     *
     * @since 2.0.0
     * @author Markus Paulsen
     * @param securityPolicy the security policy to base test case creation on; may be null.
     * @param projectPath the project directory path where test cases will be applied; may be null.
     * @return a non-null instance of SecurityTestCaseAbstractFactoryAndBuilder configured for Java security tests.
     */
    @Nonnull
    @Override
    public SecurityTestCaseAbstractFactoryAndBuilder createSecurityTestCases(@Nullable SecurityPolicy securityPolicy, @Nullable Path projectPath) {
        if (securityPolicy == null) {
            return generateJavaSecurityTestCaseFactoryAndBuilder(null, null, null, null, null);
        }
        ProgrammingLanguageConfiguration config = securityPolicy.regardingTheSupervisedCode().theFollowingProgrammingLanguageConfigurationIsUsed();

        return switch (config) {
            case ProgrammingLanguageConfiguration.JAVA_USING_MAVEN_ARCHUNIT_AND_ASPECTJ ->
                    generateJavaSecurityTestCaseFactoryAndBuilder(JavaBuildMode.MAVEN, JavaArchitectureMode.ARCHUNIT, JavaAOPMode.ASPECTJ, projectPath, securityPolicy);
            case ProgrammingLanguageConfiguration.JAVA_USING_MAVEN_ARCHUNIT_AND_INSTRUMENTATION ->
                    generateJavaSecurityTestCaseFactoryAndBuilder(JavaBuildMode.MAVEN, JavaArchitectureMode.ARCHUNIT, JavaAOPMode.INSTRUMENTATION, projectPath, securityPolicy);
            case ProgrammingLanguageConfiguration.JAVA_USING_MAVEN_WALA_AND_ASPECTJ ->
                    generateJavaSecurityTestCaseFactoryAndBuilder(JavaBuildMode.MAVEN, JavaArchitectureMode.WALA, JavaAOPMode.ASPECTJ, projectPath, securityPolicy);
            case ProgrammingLanguageConfiguration.JAVA_USING_MAVEN_WALA_AND_INSTRUMENTATION ->
                    generateJavaSecurityTestCaseFactoryAndBuilder(JavaBuildMode.MAVEN, JavaArchitectureMode.WALA, JavaAOPMode.INSTRUMENTATION, projectPath, securityPolicy);
            case ProgrammingLanguageConfiguration.JAVA_USING_GRADLE_ARCHUNIT_AND_ASPECTJ ->
                    generateJavaSecurityTestCaseFactoryAndBuilder(JavaBuildMode.GRADLE, JavaArchitectureMode.ARCHUNIT, JavaAOPMode.ASPECTJ, projectPath, securityPolicy);
            case ProgrammingLanguageConfiguration.JAVA_USING_GRADLE_ARCHUNIT_AND_INSTRUMENTATION ->
                    generateJavaSecurityTestCaseFactoryAndBuilder(JavaBuildMode.GRADLE, JavaArchitectureMode.ARCHUNIT, JavaAOPMode.INSTRUMENTATION, projectPath, securityPolicy);
            case ProgrammingLanguageConfiguration.JAVA_USING_GRADLE_WALA_AND_ASPECTJ ->
                    generateJavaSecurityTestCaseFactoryAndBuilder(JavaBuildMode.GRADLE, JavaArchitectureMode.WALA, JavaAOPMode.ASPECTJ, projectPath, securityPolicy);
            case ProgrammingLanguageConfiguration.JAVA_USING_GRADLE_WALA_AND_INSTRUMENTATION ->
                    generateJavaSecurityTestCaseFactoryAndBuilder(JavaBuildMode.GRADLE, JavaArchitectureMode.WALA, JavaAOPMode.INSTRUMENTATION, projectPath, securityPolicy);
        };
    }
}