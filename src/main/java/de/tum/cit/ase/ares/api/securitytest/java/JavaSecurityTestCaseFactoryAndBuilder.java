package de.tum.cit.ase.ares.api.securitytest.java;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import de.tum.cit.ase.ares.api.architecturetest.java.JavaArchitectureTestCase;
import de.tum.cit.ase.ares.api.architecturetest.java.JavaSupportedArchitectureTestCase;
import de.tum.cit.ase.ares.api.architecturetest.java.postcompile.JavaArchitectureTestCaseCollection;
import de.tum.cit.ase.ares.api.aspectconfiguration.javaaspectj.JavaAspectJConfiguration;
import de.tum.cit.ase.ares.api.aspectconfiguration.javaaspectj.JavaAspectJConfigurationSupported;
import de.tum.cit.ase.ares.api.aspectconfiguration.javainstrumentation.JavaInstrumentationConfiguration;
import de.tum.cit.ase.ares.api.aspectconfiguration.javainstrumentation.JavaInstrumentationConfigurationSupported;
import de.tum.cit.ase.ares.api.policy.SecurityPolicy;
import de.tum.cit.ase.ares.api.securitytest.SecurityTestCaseAbstractFactoryAndBuilder;
import de.tum.cit.ase.ares.api.util.FileTools;
import de.tum.cit.ase.ares.api.util.ProjectSourcesFinder;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
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
    private final JavaBuildTool javaBuildTool;

    /**
     * The Aspect-Oriented Programming (AOP) mode used in the project (e.g., AspectJ or Instrumentation).
     */
    private final JavaAOPMode javaAOPMode;

    /**
     * The security policy to be enforced during the security tests.
     */
    private final SecurityPolicy securityPolicy;

    /**
     * List of architecture test cases to be generated based on the security policy.
     */
    private final List<JavaArchitectureTestCase> javaArchitectureTestCases = new ArrayList<>();

    /**
     * List of AspectJ configurations to be generated based on the security policy.
     */
    private final List<JavaAspectJConfiguration> javaAspectJConfigurations = new ArrayList<>();

    /**
     * List of Instrumentation configurations to be generated based on the security policy.
     */
    private final List<JavaInstrumentationConfiguration> javaInstrumentationConfigurations = new ArrayList<>();

    /**
     * The path within the project where the test cases will be applied.
     */
    private final Path withinPath;
    //</editor-fold>

    //<editor-fold desc="Constructor">
    /**
     * Constructs a new {@link JavaSecurityTestCaseFactoryAndBuilder} with the specified build tool, AOP mode,
     * security policy, and within path.
     *
     * @param javaBuildTool the build tool used in the project.
     * @param javaAOPMode the AOP mode used in the project.
     * @param securityPolicy the security policy to be enforced.
     * @param withinPath the path within the project where the test cases will be applied.
     */
    public JavaSecurityTestCaseFactoryAndBuilder(JavaBuildTool javaBuildTool, JavaAOPMode javaAOPMode, SecurityPolicy securityPolicy, Path withinPath) {
        this.javaBuildTool = javaBuildTool;
        this.javaAOPMode = javaAOPMode;
        this.securityPolicy = securityPolicy;
        this.withinPath = withinPath;
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
        Arrays.stream(new Supplier[]{
                        securityPolicy.regardingTheSupervisedCode().theFollowingRessourceAccessesArePermitted()::regardingFileSystemInteractions,
                        securityPolicy.regardingTheSupervisedCode().theFollowingRessourceAccessesArePermitted()::regardingNetworkConnections
                })
                .map(method -> (Supplier<List<?>>) method)
                .forEach(method -> {
                    if (isEmpty(method.get())) {
                        javaArchitectureTestCases.add(new JavaArchitectureTestCase(JavaSupportedArchitectureTestCase.FILESYSTEM_INTERACTION));
                    } else {
                        switch (javaAOPMode) {
                            case ASPECTJ ->
                                    javaAspectJConfigurations.add(new JavaAspectJConfiguration(JavaAspectJConfigurationSupported.FILESYSTEM_INTERACTION, securityPolicy.regardingTheSupervisedCode().theFollowingRessourceAccessesArePermitted()));
                            case INSTRUMENTATION ->
                                    javaInstrumentationConfigurations.add(new JavaInstrumentationConfiguration(JavaInstrumentationConfigurationSupported.FILESYSTEM_INTERACTION, securityPolicy.regardingTheSupervisedCode().theFollowingRessourceAccessesArePermitted()));
                        }
                    }
                });

        javaArchitectureTestCases.add(new JavaArchitectureTestCase(JavaSupportedArchitectureTestCase.PACKAGE_IMPORT, new HashSet<>(securityPolicy.regardingTheSupervisedCode().theFollowingRessourceAccessesArePermitted().regardingPackageImports().itIsPermittedTo())));
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
     * @param path the path where the test cases and configurations will be written.
     * @return a list of paths representing the files that were created.
     */
    @Override
    public List<Path> writeTestCasesToFiles(Path path) {
        //<editor-fold desc="Copy files">
        List<Path> copiedFiles = FileTools.copyFiles(
                switch (javaAOPMode) {
                    case ASPECTJ -> List.of(
                            Paths.get("src", "main", "resources", "aspectOrientedProgrammingFiles", "JavaAspectJFileSystemAdviceDefinitions.aj"),
                            Paths.get("src", "main", "resources", "aspectOrientedProgrammingFiles", "JavaAspectJFileSystemPointcutDefinitions.aj"),
                            Paths.get("src", "main", "resources", "aspectOrientedProgrammingFiles", "JavaAspectJConfigurationSupported.java")
                    );
                    case INSTRUMENTATION -> List.of(
                            Paths.get("src", "main", "java", "de", "tum", "cit", "ase", "ares", "api", "aspectconfiguration", "javainstrumentation", "advice", "JavaInstrumentationAdviceToolbox.java"),
                            Paths.get("src", "main", "java", "de", "tum", "cit", "ase", "ares", "api", "aspectconfiguration", "javainstrumentation", "advice", "JavaInstrumentationReadPathAdvice.java"),
                            Paths.get("src", "main", "java", "de", "tum", "cit", "ase", "ares", "api", "aspectconfiguration", "javainstrumentation", "advice", "JavaInstrumentationOverwritePathAdvice.java"),
                            Paths.get("src", "main", "java", "de", "tum", "cit", "ase", "ares", "api", "aspectconfiguration", "javainstrumentation", "advice", "JavaInstrumentationExecutePathAdvice.java"),
                            Paths.get("src", "main", "java", "de", "tum", "cit", "ase", "ares", "api", "aspectconfiguration", "javainstrumentation", "JavaInstrumentationPointcutDefinitions.java"),
                            Paths.get("src", "main", "java", "de", "tum", "cit", "ase", "ares", "api", "aspectconfiguration", "javainstrumentation", "JavaInstrumentationBindingDefinitions.java"),
                            Paths.get("src", "main", "java", "de", "tum", "cit", "ase", "ares", "api", "aspectconfiguration", "javainstrumentation", "JavaInstrumentationAgent.java")
                    );
                },
                path
        );
        //</editor-fold>

        //<editor-fold desc="Create architecture test case file">
        Path javaArchitectureTestCaseCollectionFile = FileTools.createThreePartedFile(
                Paths.get("src", "main", "resources", "templates", "JavaArchUnitTestCaseCollectionHeader.txt"),
                String.join("\n", javaArchitectureTestCases.stream().map(JavaArchitectureTestCase::createArchitectureTestCaseFileContent).toList()),
                Paths.get("src", "main", "resources", "templates", "JavaArchUnitTestCaseCollectionFooter.txt"),
                "JavaArchitectureTestCaseCollection.java",
                path
        );
        //</editor-fold>

        //<editor-fold desc="Create aspect configuration file">
        Path javaAspectConfigurationCollectionFile = switch (javaAOPMode) {
            case ASPECTJ -> FileTools.createThreePartedFile(
                    Paths.get("src", "main", "resources", "templates", "JavaAspectJConfigurationCollectionHeader.txt"),
                    String.join("\n", javaAspectJConfigurations.stream().map(JavaAspectJConfiguration::createAspectConfigurationFileContent).toList()),
                    Paths.get("src", "main", "resources", "templates", "JavaAspectJConfigurationCollectionFooter.txt"),
                    "JavaAspectJConfigurationSettings.java",
                    path
            );
            case INSTRUMENTATION -> FileTools.createThreePartedFile(
                    Paths.get("src", "main", "resources", "templates", "JavaInstrumentationConfigurationCollectionHeader.txt"),
                    String.join("\n", javaInstrumentationConfigurations.stream().map(JavaInstrumentationConfiguration::createAspectConfigurationFileContent).toList()),
                    Paths.get("src", "main", "resources", "templates", "JavaInstrumentationConfigurationCollectionFooter.txt"),
                    "JavaInstrumentationConfigurationSettings.java",
                    path
            );
        };
        //</editor-fold>

        //<editor-fold desc="Combine files">
        return new ArrayList<>(copiedFiles) {{
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
        JavaClasses classes = new ClassFileImporter().importPath(Paths.get(ProjectSourcesFinder.isGradleProject() ? "build" : "target", withinPath.toString()).toString());
        JavaArchitectureTestCaseCollection.NO_CLASSES_SHOULD_USE_REFLECTION.check(classes);
        JavaArchitectureTestCaseCollection.NO_CLASSES_SHOULD_TERMINATE_JVM.check(classes);
        javaArchitectureTestCases.forEach(archTest -> archTest.runArchitectureTestCase(classes));
        switch (javaAOPMode) {
            case ASPECTJ -> javaAspectJConfigurations.forEach(JavaAspectJConfiguration::runAspectConfiguration);
            case INSTRUMENTATION ->
                    javaInstrumentationConfigurations.forEach(JavaInstrumentationConfiguration::runAspectConfiguration);
        }
    }
    //</editor-fold>
}