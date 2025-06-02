package de.tum.cit.ase.ares.api.aop.fileSystem.java;

import de.tum.cit.ase.ares.api.policy.policySubComponents.FilePermission;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.function.Supplier;

/**
 * Unit tests for JavaFileSystemExtractor.
 *
 * <p>Description: Verifies extraction and filtering of file system permissions for various operations.
 *
 * <p>Design Rationale: Ensures full coverage across all permission types and invalid inputs.
 *
 * @since 2.0.0
 * @author Markus Paulsen
 * @version 2.0.0
 */
public class JavaFileSystemExtractorTest {

    /**
     * Tests extractPaths for all permission predicates: read, overwrite, execute, delete.
     *
     * <p>Description: Supplies mixed FilePermission instances and asserts correct filtering per predicate.
     *
     * @since 2.0.0
     * @author Markus Paulsen
     */
    @Test
    public void testExtractPaths_allPermissionTypes() {
        List<FilePermission> configs = List.of(
                FilePermission.builder().onThisPathAndAllPathsBelow("/a").readAllFiles(true).overwriteAllFiles(true).executeAllFiles(true).deleteAllFiles(true).build(),
                FilePermission.builder().onThisPathAndAllPathsBelow("/b").readAllFiles(true).overwriteAllFiles(false).executeAllFiles(false).deleteAllFiles(false).build(),
                FilePermission.builder().onThisPathAndAllPathsBelow("/c").readAllFiles(false).overwriteAllFiles(true).executeAllFiles(false).deleteAllFiles(false).build(),
                FilePermission.builder().onThisPathAndAllPathsBelow("/d").readAllFiles(false).overwriteAllFiles(false).executeAllFiles(true).deleteAllFiles(false).build(),
                FilePermission.builder().onThisPathAndAllPathsBelow("/e").readAllFiles(false).overwriteAllFiles(false).executeAllFiles(false).deleteAllFiles(true).build(),
                FilePermission.builder().onThisPathAndAllPathsBelow("/f").readAllFiles(false).overwriteAllFiles(false).executeAllFiles(false).deleteAllFiles(false).build()
        );
        // read
        List<String> readPaths = JavaFileSystemExtractor.extractPaths(configs, FilePermission::readAllFiles);
        Assertions.assertEquals(List.of("/a", "/b"), readPaths);

        // overwrite
        List<String> overwritePaths = JavaFileSystemExtractor.extractPaths(configs, FilePermission::overwriteAllFiles);
        Assertions.assertEquals(List.of("/a", "/c"), overwritePaths);

        // execute
        List<String> executePaths = JavaFileSystemExtractor.extractPaths(configs, FilePermission::executeAllFiles);
        Assertions.assertEquals(List.of("/a", "/d"), executePaths);

        // delete
        List<String> deletePaths = JavaFileSystemExtractor.extractPaths(configs, FilePermission::deleteAllFiles);
        Assertions.assertEquals(List.of("/a", "/e"), deletePaths);
    }

    /**
     * Tests getPermittedFilePaths for each permission type and verifies invalid input.
     *
     * <p>Description: Uses a stubbed supplier and asserts correct results for read, overwrite, execute, delete, and exception case.
     *
     * @since 2.0.0
     * @author Markus Paulsen
     */
    @Test
    public void testGetPermittedFilePaths_allTypesAndInvalid() {
        Supplier<List<?>> supplier = () -> List.of(
                FilePermission.builder().onThisPathAndAllPathsBelow("/a").readAllFiles(true).overwriteAllFiles(true).executeAllFiles(true).deleteAllFiles(true).build(),
                FilePermission.builder().onThisPathAndAllPathsBelow("/b").readAllFiles(true).overwriteAllFiles(false).executeAllFiles(false).deleteAllFiles(false).build(),
                FilePermission.builder().onThisPathAndAllPathsBelow("/c").readAllFiles(false).overwriteAllFiles(true).executeAllFiles(false).deleteAllFiles(false).build(),
                FilePermission.builder().onThisPathAndAllPathsBelow("/d").readAllFiles(false).overwriteAllFiles(false).executeAllFiles(true).deleteAllFiles(false).build(),
                FilePermission.builder().onThisPathAndAllPathsBelow("/e").readAllFiles(false).overwriteAllFiles(false).executeAllFiles(false).deleteAllFiles(true).build(),
                FilePermission.builder().onThisPathAndAllPathsBelow("/f").readAllFiles(false).overwriteAllFiles(false).executeAllFiles(false).deleteAllFiles(false).build()
        );
        JavaFileSystemExtractor extractor = new JavaFileSystemExtractor(supplier);

        // read
        Assertions.assertEquals(List.of("/a", "/b"), extractor.getPermittedFilePaths("read"));
        // overwrite
        Assertions.assertEquals(List.of("/a", "/c"), extractor.getPermittedFilePaths("overwrite"));
        // execute
        Assertions.assertEquals(List.of("/a", "/d"), extractor.getPermittedFilePaths("execute"));
        // delete
        Assertions.assertEquals(List.of("/a", "/e"), extractor.getPermittedFilePaths("delete"));

        // invalid
        Assertions.assertThrows(IllegalArgumentException.class, () -> extractor.getPermittedFilePaths("kill"));
    }
}
