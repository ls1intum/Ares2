package de.tum.cit.ase.ares.api.aop.fileSystem.java;

import de.tum.cit.ase.ares.api.policy.policySubComponents.FilePermission;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.function.Supplier;

/**
 * Unit tests for JavaFileSystemExtractor.
 *
 * <p>Description: Verifies extraction and filtering of file system permissions for read, overwrite, execute, and delete operations.
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
    public void testExtractPathsAllPermissionTypesAndValid() {
        List<FilePermission> configs = List.of(
                FilePermission.builder().onThisPathAndAllPathsBelow("/a").readAllFiles(true).overwriteAllFiles(true).executeAllFiles(true).deleteAllFiles(true).build(),
                FilePermission.builder().onThisPathAndAllPathsBelow("/b").readAllFiles(true).overwriteAllFiles(false).executeAllFiles(false).deleteAllFiles(false).build(),
                FilePermission.builder().onThisPathAndAllPathsBelow("/c").readAllFiles(false).overwriteAllFiles(true).executeAllFiles(false).deleteAllFiles(false).build(),
                FilePermission.builder().onThisPathAndAllPathsBelow("/d").readAllFiles(false).overwriteAllFiles(false).executeAllFiles(true).deleteAllFiles(false).build(),
                FilePermission.builder().onThisPathAndAllPathsBelow("/e").readAllFiles(false).overwriteAllFiles(false).executeAllFiles(false).deleteAllFiles(true).build(),
                FilePermission.builder().onThisPathAndAllPathsBelow("/f").readAllFiles(false).overwriteAllFiles(false).executeAllFiles(false).deleteAllFiles(false).build()
        );
        // read
        List<String> readPathsExpected = List.of("/a", "/b");
        List<String> readPathsActual = JavaFileSystemExtractor.extractPaths(configs, FilePermission::readAllFiles);
        Assertions.assertEquals(readPathsExpected, readPathsActual);

        // overwrite
        List<String> overwritePathsExpected = List.of("/a", "/c");
        List<String> overwritePathsActual = JavaFileSystemExtractor.extractPaths(configs, FilePermission::overwriteAllFiles);
        Assertions.assertEquals(overwritePathsExpected, overwritePathsActual);

        // execute
        List<String> executePathsExpected = List.of("/a", "/d");
        List<String> executePathsActual = JavaFileSystemExtractor.extractPaths(configs, FilePermission::executeAllFiles);
        Assertions.assertEquals(executePathsExpected, executePathsActual);

        // delete
        List<String> deletePathsExpected = List.of("/a", "/e");
        List<String> deletePathsActual = JavaFileSystemExtractor.extractPaths(configs, FilePermission::deleteAllFiles);
        Assertions.assertEquals(deletePathsExpected, deletePathsActual);
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
    public void testGetPermittedFilePathsAllPermissionTypesAndInvalid() {
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
        List<String> readPathsExpected = List.of("/a", "/b");
        List<String> readPathsActual = extractor.getPermittedFilePaths("read");
        Assertions.assertEquals(readPathsExpected, readPathsActual);

        // overwrite
        List<String> overwritePathsExpected = List.of("/a", "/c");
        List<String> overwritePathsActual = extractor.getPermittedFilePaths("overwrite");
        Assertions.assertEquals(overwritePathsExpected, overwritePathsActual);

        // execute
        List<String> executePathsExpected = List.of("/a", "/d");
        List<String> executePathsActual = extractor.getPermittedFilePaths("execute");
        Assertions.assertEquals(executePathsExpected, executePathsActual);

        // delete
        List<String> deletePathsExpected = List.of("/a", "/e");
        List<String> deletePathsActual = extractor.getPermittedFilePaths("delete");
        Assertions.assertEquals(deletePathsExpected, deletePathsActual);

        // invalid
        Assertions.assertThrows(IllegalArgumentException.class, () -> extractor.getPermittedFilePaths("kill"));
    }
}
