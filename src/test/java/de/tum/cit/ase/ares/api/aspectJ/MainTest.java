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

    //Folgende Tests demonstrieren wie man einen gültigen Pfad weiterleiten könnte und dieser file access hätte und wie ein nicht erlaubter Pfad das nicht könnte.
    //Anpassungen aber noch nötig aber das sollte möglich sein. Z.b statt text datei konkreten filepfad angeben etc. das kann man auch mit joinpoints ausgeben

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


}
