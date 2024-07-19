package de.tum.cit.ase.ares.api.aspectJ;

import de.tum.cit.ase.ares.api.aspectJ.readWrite.ReadWrite;
import de.tum.cit.ase.ares.api.internal.TestGuardUtils;
import de.tum.cit.ase.ares.api.policy.FileSystemInteraction;
import de.tum.cit.ase.ares.api.policy.SecurityPolicy;
import de.tum.cit.ase.ares.api.policy.SupportedProgrammingLanguage;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

public class ReadWriteTest {

    // Example paths and their permissions
    private static final List<FileSystemInteraction> INTERACTIONS = List.of(
            new FileSystemInteraction(Path.of("ReadWrite.java"), true, true, false)
    );

    private static final SecurityPolicy SECURITY_POLICY = new SecurityPolicy(
            SupportedProgrammingLanguage.JAVA,
            INTERACTIONS
    );

    private static final List<FileSystemInteraction> INTERACTIONS2 = List.of(
            new FileSystemInteraction(Path.of("Random.java"), true, true, true)
    );

    private static final SecurityPolicy SECURITY_POLICY2 = new SecurityPolicy(
            SupportedProgrammingLanguage.JAVA,
            INTERACTIONS2
    );

    @Before
    public void setup() {
        TestGuardUtils.checkFileAccess(SECURITY_POLICY);
    }

    @Test(expected = SecurityException.class)
    public void testFilesWriteAccessDenied() throws IOException {
        TestGuardUtils.checkFileAccess(SECURITY_POLICY2);
        Path deniedPath = Paths.get("deniedFile.txt");
        ReadWrite readWrite = new ReadWrite();
        readWrite.writeMethod(deniedPath.toString());
    }

    @Test
    public void testFilesWriteAccessGranted() throws IOException {
        TestGuardUtils.checkFileAccess(SECURITY_POLICY);
        Path allowedPath = Paths.get("fileUsingFilesClass.txt");
        ReadWrite readWrite = new ReadWrite();
        readWrite.writeMethod(allowedPath.toString());
    }

    @Test(expected = SecurityException.class)
    public void testFilesReadAccessDenied() throws IOException {
        TestGuardUtils.checkFileAccess(SECURITY_POLICY2);
        Path deniedPath = Paths.get("deniedFile.txt");
        ReadWrite readWrite = new ReadWrite();
        readWrite.readMethod(deniedPath.toString());
    }

    @Test
    public void testFilesReadAccessGranted() throws IOException {
        TestGuardUtils.checkFileAccess(SECURITY_POLICY);
        Path allowedPath = Paths.get("fileUsingFilesClass.txt");
        ReadWrite readWrite = new ReadWrite();
        readWrite.readMethod(allowedPath.toString());
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