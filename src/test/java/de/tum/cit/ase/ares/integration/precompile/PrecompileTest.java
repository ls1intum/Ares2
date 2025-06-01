package de.tum.cit.ase.ares.integration.precompile;

import de.tum.cit.ase.ares.api.policy.SecurityPolicyReaderAndDirector;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;

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
                .securityPolicyFilePath(Path.of("\\Users\\ajayv\\OneDrive\\Desktop\\itp2425retake-R02E01_-_Seal_Rebels_Communication_System-tests\\test\\de\\tum\\cit\\ase\\Configuration.yaml"))
                .projectFolderPath(Path.of("\\Users\\ajayv\\OneDrive\\Desktop\\itp2425retake-R02E01_-_Seal_Rebels_Communication_System-tests\\assignment\\src"))
                .build()
        .createTestCases()
        .writeTestCases(Path.of("\\Users\\ajayv\\OneDrive\\Desktop\\itp2425retake-R02E01_-_Seal_Rebels_Communication_System-tests\\test"));

    }
}
