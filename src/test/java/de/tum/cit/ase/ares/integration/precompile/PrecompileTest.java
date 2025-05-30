package de.tum.cit.ase.ares.integration.precompile;

import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import de.tum.cit.ase.ares.api.policy.SecurityPolicyReaderAndDirector;
import de.tum.cit.ase.ares.api.policy.director.SecurityPolicyDirector;
import de.tum.cit.ase.ares.api.policy.director.java.SecurityPolicyJavaDirector;
import de.tum.cit.ase.ares.api.policy.reader.SecurityPolicyReader;
import de.tum.cit.ase.ares.api.policy.reader.yaml.SecurityPolicyYAMLReader;
import de.tum.cit.ase.ares.api.securitytest.java.creator.JavaCreator;
import de.tum.cit.ase.ares.api.securitytest.java.essentialModel.yaml.EssentialDataYAMLReader;
import de.tum.cit.ase.ares.api.securitytest.java.executer.JavaExecuter;
import de.tum.cit.ase.ares.api.securitytest.java.projectScanner.JavaProjectScanner;
import de.tum.cit.ase.ares.api.securitytest.java.writer.JavaWriter;
import de.tum.cit.ase.ares.api.util.FileTools;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

public class PrecompileTest {
    /*@BeforeEach
    void clean() {
        Path target = Paths.get("src/test/resources/dump");
        if (!Files.exists(target) || !Files.isDirectory(target)) {
            System.err.println("Target path does not exist or is not a directory: " + target);
            return;
        }
        try (DirectoryStream<Path> entries = Files.newDirectoryStream(target)) {
            for (Path entry : entries) {
                Files.walkFileTree(entry, new SimpleFileVisitor<>() {

                    @Override
                    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                        Files.delete(file);
                        return FileVisitResult.CONTINUE;
                    }

                    @Override
                    public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                        Files.delete(dir);
                        return FileVisitResult.CONTINUE;
                    }

                });
            }
        } catch (IOException e) {
            System.err.println("Error cleaning directory: " + e.getMessage());
        }
    }*/

    @Test
    void testPrecompileJavaMavenArchunitInstrumentation() {
        SecurityPolicyReaderAndDirector.builder()
                .securityPolicyFilePath(Path.of(""))
                .projectFolderPath(Path.of(""))
                .build()
                .createTestCases()
                .writeTestCases(Path.of(""));
    }
}
