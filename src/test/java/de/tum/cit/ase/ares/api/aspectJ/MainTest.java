package de.tum.cit.ase.ares.api.aspectJ;

import de.tum.cit.ase.ares.api.archunit.SecurityRuleExecutor;
import de.tum.cit.ase.ares.api.aspectJ.main.Main;
import de.tum.cit.ase.ares.api.internal.TestGuardUtils;
import de.tum.cit.ase.ares.api.policy.FileSystemInteraction;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertTrue;

public class MainTest {

    @Before
    public void setup() {
        List<FileSystemInteraction> interactions = List.of(
                new FileSystemInteraction(Path.of("notValid.txt"), true, true, true)
        );
        TestGuardUtils.checkFileAccess(interactions, new SecurityRuleExecutor());
    }

    @Test
    public void testGetPath() {
        List<FileSystemInteraction> interactions = List.of(
                new FileSystemInteraction(Path.of("fileUsingFilesClass.txt"), true, true, true)
        );
        TestGuardUtils.checkFileAccess(interactions, new SecurityRuleExecutor());
        Main mainInstance = new Main();
        mainInstance.getPath("dir", "file.txt");
    }

    @Test
    public void testFilesWrite() {
        List<FileSystemInteraction> interactions = List.of(
                new FileSystemInteraction(Path.of("fileUsingFilesClass.txt"), true, true, true)
        );
        TestGuardUtils.checkFileAccess(interactions, new SecurityRuleExecutor());
        Main.main(null);
    }

    @Test(expected = SecurityException.class)
    public void testFilesWriteAccessDenied() throws IOException {
        List<FileSystemInteraction> interactions = List.of(
                new FileSystemInteraction(Path.of("fileUsingFilesClass.txt"), true, true, true)
        );
        TestGuardUtils.checkFileAccess(interactions, new SecurityRuleExecutor());
        Path deniedPath = Paths.get("deniedFile.txt");
        Files.write(deniedPath, Arrays.asList("This should be blocked"));
    }

    @Test
    public void testFilesWriteAccessGranted() throws IOException {
        List<FileSystemInteraction> interactions = List.of(
                new FileSystemInteraction(Path.of("fileUsingFilesClass.txt"), true, true, true)
        );
        TestGuardUtils.checkFileAccess(interactions, new SecurityRuleExecutor());
        Path allowedPath = Paths.get("fileUsingFilesClass.txt");
        Files.write(allowedPath, Arrays.asList("This should be allowed"));
        String expectedOutput = "Files.write called with path: fileUsingFilesClass.txt and lines: [This should be allowed]";
    }

    @Test(expected = SecurityException.class)
    public void testFilesReadAccessDenied() throws IOException {
        List<FileSystemInteraction> interactions = List.of(
                new FileSystemInteraction(Path.of("fileUsingFilesClass.txt"), true, true, true)
        );
        TestGuardUtils.checkFileAccess(interactions, new SecurityRuleExecutor());
        Path deniedPath = Paths.get("deniedFile.txt");
        Files.readAllBytes(deniedPath);
    }

    @Test
    public void testFilesReadAccessGranted() throws IOException {
        List<FileSystemInteraction> interactions = List.of(
                new FileSystemInteraction(Path.of("fileUsingFilesClass.txt"), true, true, true)
        );
        TestGuardUtils.checkFileAccess(interactions, new SecurityRuleExecutor());
        Path allowedPath = Paths.get("fileUsingFilesClass.txt");
        byte[] bytes = Files.readAllBytes(allowedPath);
        System.out.println("Files.read called with path: " + allowedPath + " and bytes: " + Arrays.toString(bytes));
    }

    /*
    @Test(expected = SecurityException.class)
    public void testFilesDeleteAccessDenied() throws IOException {
        List<FileSystemInteraction> interactions = List.of(
                new FileSystemInteraction(Path.of("fileUsingFilesClass.txt"), true, true, true)
        );
        TestGuardUtils.checkFileAccess(interactions, new SecurityRuleExecutor());
        Path deniedPath = Paths.get("deniedFile.txt");
        Files.delete(deniedPath);
    }

    @Test
    public void testFilesDeleteAccessGranted() throws IOException {
        List<FileSystemInteraction> interactions = List.of(
                new FileSystemInteraction(Path.of("fileUsingFilesClass.txt"), true, true, true)
        );
        TestGuardUtils.checkFileAccess(interactions, new SecurityRuleExecutor());
        Path allowedPath = Paths.get("fileUsingFilesClass.txt");
        Files.delete(allowedPath);
        System.out.println("Files.delete called with path: " + allowedPath);
    }

     */
}