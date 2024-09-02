package de.tum.cit.ase.ares.api.securitytest.java;

import com.google.common.collect.Streams;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import de.tum.cit.ase.ares.api.architecturetest.java.archunit.JavaArchUnitTestCase;
import de.tum.cit.ase.ares.api.architecturetest.java.archunit.JavaArchUnitTestCaseSupported;
import de.tum.cit.ase.ares.api.architecturetest.java.archunit.postcompile.JavaArchitectureTestCaseCollection;
import de.tum.cit.ase.ares.api.aspectconfiguration.java.aspectj.JavaAspectJConfiguration;
import de.tum.cit.ase.ares.api.aspectconfiguration.java.aspectj.JavaAspectJConfigurationSupported;
import de.tum.cit.ase.ares.api.aspectconfiguration.java.instrumentation.JavaInstrumentationConfiguration;
import de.tum.cit.ase.ares.api.aspectconfiguration.java.instrumentation.JavaInstrumentationConfigurationSupported;
import de.tum.cit.ase.ares.api.policy.SecurityPolicy;
import de.tum.cit.ase.ares.api.policy.SecurityPolicy.ResourceAccesses;
import de.tum.cit.ase.ares.api.securitytest.SecurityTestCaseAbstractFactoryAndBuilder;
import de.tum.cit.ase.ares.api.aspectconfiguration.java.JavaAOPMode;
import de.tum.cit.ase.ares.api.architecturetest.java.JavaArchitectureMode;
import de.tum.cit.ase.ares.api.buildtoolconfiguration.java.JavaBuildMode;
import de.tum.cit.ase.ares.api.util.FileTools;
import de.tum.cit.ase.ares.api.util.ProjectSourcesFinder;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.function.Supplier;

import static com.google.common.collect.Iterables.isEmpty;

/**
 * Factory and builder class for creating and running Java security test cases.
 * <p>
 * This class is responsible for generating the necessary security test cases based on the
 * provided build tool, AOP mode, and security policy. It also facilitates the writing of
 * these test cases to files and their execution.
 * </p>
 */
public class JavaSecurityTestCaseFactoryAndBuilder implements SecurityTestCaseAbstractFactoryAndBuilder {

    //<editor-fold desc="Attributes">
    /**
     * The build tool used in the project (e.g., Maven or Gradle).
     */
    private final JavaBuildMode javaBuildMode;

    private final JavaArchitectureMode javaArchitectureMode;

    /**
     * The Aspect-Oriented Programming (AOP) mode used in the project (e.g., AspectJ or Instrumentation).
     */
    private final JavaAOPMode javaAOPMode;

    /**
     * List of architecture test cases to be generated based on the security policy.
     */
    private final List<JavaArchUnitTestCase> javaArchUnitTestCases = new ArrayList<>();

    /**
     * List of AspectJ configurations to be generated based on the security policy.
     */
    private final List<JavaAspectJConfiguration> javaAspectJConfigurations = new ArrayList<>();

    /**
     * List of Instrumentation configurations to be generated based on the security policy.
     */
    private final List<JavaInstrumentationConfiguration> javaInstrumentationConfigurations = new ArrayList<>();

    private final String packageName;

    private final ResourceAccesses ressourceAccesses;

    /**
     * The path within the project where the test cases will be applied.
     */
    private final Path projectPath;
    //</editor-fold>

    //<editor-fold desc="Constructor">

    /**
     * Constructs a new {@link JavaSecurityTestCaseFactoryAndBuilder} with the specified build tool, AOP mode,
     * security policy, and within path.
     *
     * @param javaBuildMode  the build tool used in the project.
     * @param javaAOPMode    the AOP mode used in the project.
     * @param securityPolicy the security policy to be enforced.
     * @param projectPath    the path within the project where the test cases will be applied.
     */
    public JavaSecurityTestCaseFactoryAndBuilder(JavaBuildMode javaBuildMode, JavaArchitectureMode javaArchitectureMode, JavaAOPMode javaAOPMode, SecurityPolicy securityPolicy, Path projectPath) {
        this.javaBuildMode = javaBuildMode;
        this.javaArchitectureMode = javaArchitectureMode;
        this.javaAOPMode = javaAOPMode;
        this.packageName = securityPolicy.regardingTheSupervisedCode().theProgrammingLanguageUsesTheFollowingPackage();
        this.ressourceAccesses = securityPolicy.regardingTheSupervisedCode().theFollowingResourceAccessesArePermitted();
        this.projectPath = projectPath;
        this.parseTestCasesToBeCreated();
    }
    //</editor-fold>

    //<editor-fold desc="Toolbox methods">

    /**
     * Parses the security policy to determine which test cases and configurations need to be created.
     * <p>
     * This method checks the security policy for specific permissions and based on the results,
     * generates the appropriate architecture test cases or AspectJ/Instrumentation configurations.
     * </p>
     */
    private void parseTestCasesToBeCreated() {
        javaAOPMode.reset();

        //<editor-fold desc="Only architecture test case">
        javaArchUnitTestCases.add(
                new JavaArchUnitTestCase(
                        JavaArchUnitTestCaseSupported.PACKAGE_IMPORT,
                        new HashSet<>(ressourceAccesses.regardingPackageImports())
                )
        );
        //</editor-fold>

        //<editor-fold desc="Architecture test case and AOP">
        Supplier<List<?>>[] methods = new Supplier[]{
                ressourceAccesses::regardingFileSystemInteractions,
                ressourceAccesses::regardingNetworkConnections
//                securityPolicy::iAllowTheFollowingCommandExecutionsForTheStudents,
//                securityPolicy::iAllowTheFollowingThreadCreationsForTheStudents,
        };

        for (int i = 0; i < methods.length; i++) {
            // TODO: What if JavaArchUnitTestCaseSupported, JavaAspectJConfigurationSupported and JavaInstrumentationConfigurationSupported are not in the same order or smaller than methods?
            JavaArchUnitTestCaseSupported javaArchitectureTestCasesSupportedValue = JavaArchUnitTestCaseSupported.values()[i];
            JavaAspectJConfigurationSupported javaAspectJConfigurationSupportedValue = JavaAspectJConfigurationSupported.values()[i];
            JavaInstrumentationConfigurationSupported javaInstrumentationConfigurationSupportedValue = JavaInstrumentationConfigurationSupported.values()[i];

            if (isEmpty(methods[i].get())) {
                //<editor-fold desc="Architecture test case">
                switch (javaArchitectureMode) {
                    case ARCHUNIT -> javaArchUnitTestCases.add(
                            new JavaArchUnitTestCase(
                                    javaArchitectureTestCasesSupportedValue
                            )
                    );
                }
                //</editor-fold>
            } else {
                //<editor-fold desc="AOP">
                switch (javaAOPMode) {
                    case ASPECTJ -> javaAspectJConfigurations.add(
                            new JavaAspectJConfiguration(
                                    javaAspectJConfigurationSupportedValue,
                                    ressourceAccesses
                            )
                    );
                    case INSTRUMENTATION -> javaInstrumentationConfigurations.add(
                            new JavaInstrumentationConfiguration(
                                    javaInstrumentationConfigurationSupportedValue,
                                    ressourceAccesses
                            )
                    );
                }
                //</editor-fold>
            }
        }
        //</editor-fold>
    }
    //</editor-fold>

    //<editor-fold desc="Inherited methods">

    /**
     * Writes the generated test cases and configurations to files in the specified path.
     * <p>
     * This method copies the relevant files based on the AOP mode, creates the necessary
     * architecture test case files, and combines them with the configuration files.
     * </p>
     *
     * @param projectPath the path where the test cases and configurations will be written.
     * @return a list of paths representing the files that were created.
     */
    @Override
    public List<Path> writeTestCasesToFiles(Path projectPath) {
        //<editor-fold desc="Create architecture test case files">
        List<Path> javaCopiedArchitectureFiles = FileTools.copyJavaFiles(
                javaArchitectureMode.filesToCopy(),
                javaArchitectureMode.targetsToCopyTo(projectPath, packageName),
                javaArchitectureMode.fileValues(packageName)
        );
        Path javaArchitectureTestCaseCollectionFile = FileTools.createThreePartedJavaFile(
                javaArchitectureMode.threePartedFileHeader(),
                javaArchitectureMode.threePartedFileBody(javaArchUnitTestCases),
                javaArchitectureMode.threePartedFileFooter(),
                javaArchitectureMode.targetToCopyTo(projectPath, packageName),
                javaArchitectureMode.fileValue(packageName)
        );
        //</editor-fold>

        //<editor-fold desc="Create aspect configuration files">
        List<Path> javaCopiedAspectFiles = FileTools.copyJavaFiles(
                javaAOPMode.filesToCopy(),
                javaAOPMode.targetsToCopyTo(projectPath, packageName),
                javaAOPMode.fileValues(packageName)
        );
        List<?> javaAspectConfigurations = javaAOPMode.equals(JavaAOPMode.ASPECTJ) ? this.javaAspectJConfigurations : this.javaInstrumentationConfigurations;
        Path javaAspectConfigurationCollectionFile = FileTools.createThreePartedJavaFile(
                javaAOPMode.threePartedFileHeader(),
                javaAOPMode.threePartedFileBody(javaAspectConfigurations),
                javaAOPMode.threePartedFileFooter(),
                javaAOPMode.targetToCopyTo(projectPath, packageName),
                javaAOPMode.fileValue(packageName)
        );
        //</editor-fold>

        //<editor-fold desc="Combine files">
        return new ArrayList<>(Streams.concat(javaCopiedArchitectureFiles.stream(), javaCopiedAspectFiles.stream()).toList()) {{
            add(javaArchitectureTestCaseCollectionFile);
            add(javaAspectConfigurationCollectionFile);
        }};
        //</editor-fold>
    }

    /**
     * Executes the generated security test cases.
     * <p>
     * This method imports the classes from the specified path and runs the security test cases
     * defined in the architecture test cases and aspect configurations. It checks for certain
     * architecture violations and applies aspect-oriented configurations if needed.
     * </p>
     */
    @Override
    public void runSecurityTestCases() {
        //<editor-fold desc="Load classes">
        JavaClasses classes = new ClassFileImporter().importPath(Paths.get(ProjectSourcesFinder.isGradleProject() ? "build" : "target", projectPath.toString()).toString());
        //</editor-fold>#

        //<editor-fold desc="Enforce fixed rules">
        JavaArchitectureTestCaseCollection.NO_CLASSES_SHOULD_USE_REFLECTION.check(classes);
        JavaArchitectureTestCaseCollection.NO_CLASSES_SHOULD_TERMINATE_JVM.check(classes);
        //</editor-fold>

        //<editor-fold desc="Enforce variable rules">
        javaArchUnitTestCases.forEach(archTest -> archTest.runArchitectureTestCase(classes));
        switch (javaAOPMode) {
            case ASPECTJ -> javaAspectJConfigurations.forEach(JavaAspectJConfiguration::runAspectConfiguration);
            case INSTRUMENTATION ->
                    javaInstrumentationConfigurations.forEach(JavaInstrumentationConfiguration::runAspectConfiguration);
        }
        //</editor-fold>
    }
    //</editor-fold>
}