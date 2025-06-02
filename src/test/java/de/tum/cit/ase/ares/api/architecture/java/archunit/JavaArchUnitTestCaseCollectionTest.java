package de.tum.cit.ase.ares.api.architecture.java.archunit;

import com.tngtech.archunit.lang.ArchRule;
import de.tum.cit.ase.ares.api.localization.Messages;
import de.tum.cit.ase.ares.api.policy.policySubComponents.PackagePermission;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class JavaArchUnitTestCaseCollectionTest {

    private static Path tempMethodsFile;

    @BeforeAll
    static void setUpResources() throws IOException {
        // Create a temporary file under src/test/resources for readMethodsFromGivenPath
        Path resourcesDir = Paths.get("src/test/resources");
        if (!Files.exists(resourcesDir)) {
            Files.createDirectories(resourcesDir);
        }
        tempMethodsFile = resourcesDir.resolve("tempMethods.txt");
        String content = "#commentLine\nmethodA\n\nmethodB\n#anotherComment";
        Files.writeString(tempMethodsFile, content, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
    }

    @Test
    void readFile_nonexistentPath_throwsSecurityException() {
        Path invalidPath = Paths.get("nonexistentfile.txt");
        assertThrows(SecurityException.class,
            () -> JavaArchUnitTestCaseCollection.readFile(invalidPath));
    }

    @Test
    void noClassMustImportForbiddenPackages_returnsRuleWithExpectedDescription() {
        Set<PackagePermission> allowedPackages = new HashSet<>();
        allowedPackages.add(new PackagePermission("com.allowed"));
        ArchRule rule = JavaArchUnitTestCaseCollection.noClassMustImportForbiddenPackages(allowedPackages);
        String expectedDescription = Messages.localized("security.architecture.package.import");
        assertEquals(expectedDescription, rule.getDescription());
    }

    @Test
    void staticRules_areNotNull() {
        assertNotNull(JavaArchUnitTestCaseCollection.NO_CLASS_MUST_ACCESS_FILE_SYSTEM);
        assertNotNull(JavaArchUnitTestCaseCollection.NO_CLASS_MUST_ACCESS_NETWORK);
        assertNotNull(JavaArchUnitTestCaseCollection.NO_CLASS_MUST_CREATE_THREADS);
        assertNotNull(JavaArchUnitTestCaseCollection.NO_CLASS_MUST_EXECUTE_COMMANDS);
        assertNotNull(JavaArchUnitTestCaseCollection.NO_CLASS_MUST_USE_REFLECTION);
        assertNotNull(JavaArchUnitTestCaseCollection.NO_CLASS_MUST_TERMINATE_JVM);
        assertNotNull(JavaArchUnitTestCaseCollection.NO_CLASS_MUST_SERIALIZE);
        assertNotNull(JavaArchUnitTestCaseCollection.NO_CLASS_MUST_USE_CLASSLOADERS);
    }
}
