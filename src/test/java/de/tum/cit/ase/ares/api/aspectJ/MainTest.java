package de.tum.cit.ase.ares.api.aspectJ;

import de.tum.cit.ase.ares.api.aspectJ.main.Main;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

import static org.junit.Assert.assertTrue;

public class MainTest {

    //Folgende Tests demonstrieren wie man einen gültigen Pfad weiterleiten könnte und dieser file access hätte und wie ein nicht erlaubter Pfad das nicht könnte.
    //Anpassungen aber noch nötig aber das sollte möglich sein. Z.b statt text datei konkreten filepfad angeben etc. das kann man auch mit joinpoints ausgeben

    @Test
    public void testGetPath() {
        Main mainInstance = new Main();
        mainInstance.getPath("dir", "file.txt");
    }

    @Test
    public void testFilesWrite() {
        Main.main(null);
    }

    @Test(expected = SecurityException.class)
    public void testFilesWriteAccessDenied() throws IOException {
        Path deniedPath = Paths.get("deniedFile.txt");
        Files.write(deniedPath, Arrays.asList("This should be blocked"));
    }

    @Test
    public void testFilesWriteAccessGranted() throws IOException {
        Path allowedPath = Paths.get("fileUsingFilesClass.txt");
        Files.write(allowedPath, Arrays.asList("This should be allowed"));
        String expectedOutput = "Files.write called with path: fileUsingFilesClass.txt and lines: [This should be allowed]";
    }


}
