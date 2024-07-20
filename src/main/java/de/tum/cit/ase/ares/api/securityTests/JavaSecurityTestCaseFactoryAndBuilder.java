package de.tum.cit.ase.ares.api.securityTests;

import de.tum.cit.ase.ares.api.architectureTest.JavaArchitectureTestCase;
import de.tum.cit.ase.ares.api.aspectConfiguration.JavaAspectConfiguration;
import de.tum.cit.ase.ares.api.policy.SecurityPolicy;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;

public class JavaSecurityTestCaseFactoryAndBuilder implements SecurityTestCaseAbstractFactoryAndBuilder {

    SecurityPolicy securityPolicy;
    List<JavaArchitectureTestCase> javaArchitectureTestCases;
    List<JavaAspectConfiguration> javaAspectConfigurations;

    public JavaSecurityTestCaseFactoryAndBuilder(SecurityPolicy securityPolicy) {
        this.securityPolicy = securityPolicy;
        javaArchitectureTestCases = new ArrayList<>();
        javaAspectConfigurations = new ArrayList<>();
    }

    @Override
    public List<Path> writeTestCasesToFiles(Path path) {
        try {
            Path architectureTestCaseFile = Files.createFile(path.resolve("ArchitectureTestCase.java"));
            Files.writeString(architectureTestCaseFile,
                    """
                            public class ArchitectureTestCase {
                            
                            """ + String.join(
                            "\n",
                            javaArchitectureTestCases
                                    .stream()
                                    .map(JavaArchitectureTestCase::createArchitectureTestCaseFileContent)
                                    .toList()
                    ) + """
                            
                            }
                            """, StandardOpenOption.WRITE);
            Path aspectConfigurationFile = Files.createFile(path.resolve("AspectConfiguration.java"));
            Files.writeString(architectureTestCaseFile,
                    """
                            public class AspectConfiguration {
                            
                            """ + String.join(
                            "\n",
                            javaAspectConfigurations
                                    .stream()
                                    .map(JavaAspectConfiguration::createAspectConfigurationFileContent)
                                    .toList()
                    ) + """
                            
                            }
                            """, StandardOpenOption.WRITE);
            return List.of(architectureTestCaseFile, aspectConfigurationFile);
        } catch (Exception e) {
            throw new SecurityException("Creating test case file failed.");
        }

    }

    @Override
    public void runSecurityTestCases() {
        javaArchitectureTestCases.forEach(JavaArchitectureTestCase::runArchitectureTestCase);
        javaAspectConfigurations.forEach(JavaAspectConfiguration::runAspectConfiguration);
    }
}
