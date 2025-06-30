package de.tum.cit.ase.ares.api.aop.java.javaAOPModeData;

import com.opencsv.exceptions.CsvException;
import com.opencsv.exceptions.CsvMalformedLineException;
import de.tum.cit.ase.ares.api.aop.AOPMode;
import de.tum.cit.ase.ares.api.architecture.ArchitectureMode;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.io.TempDir;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

public class JavaCSVFileLoaderTest {

    @TempDir
    Path tempDir;

    @Test
    public void testLoadCopyDataSuccess() throws IOException, CsvException {
        // Create a dummy CSV with header and one data row
        Path csvPath = tempDir.resolve("dummyCopy.csv");
        File csvFile = csvPath.toFile();
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(csvFile))) {
            writer.write("srcFile,targetDir\n");
            writer.write("Example.java,output/");
        }

        JavaCSVFileLoader loader = new JavaCSVFileLoader() {
            @Override
            public File getCopyPaths(ArchitectureMode mode) {
                return csvFile;
            }
        };

        List<List<String>> rows = loader.loadCopyData(ArchitectureMode.ARCHUNIT);
        Assertions.assertEquals(1, rows.size());
        // Only data row (header is skipped)
        Assertions.assertEquals(List.of("Example.java", "output/"), rows.get(0));
    }

    @Test
    public void testLoadCopyDataFileNotFoundExplicit() {
        // Use a non-existent file
        File missing = tempDir.resolve("no-such.csv").toFile();
        JavaCSVFileLoader loader = new JavaCSVFileLoader() {
            @Override
            public File getCopyPaths(AOPMode mode) {
                return missing;
            }
        };
        Assertions.assertThrows(IOException.class, () ->
                loader.loadCopyData(AOPMode.ASPECTJ),
                "Expected IOException for missing file");
    }

    @Test
    public void testLoadCopyDataMalformed() throws IOException {
        // Create a malformed CSV (unclosed quote)
        Path csvPath = tempDir.resolve("malformedCopy.csv");
        File csvFile = csvPath.toFile();
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(csvFile))) {
            writer.write("col1,col2\n");
            writer.write("\"bad,entry"); // missing closing quote
        }

        JavaCSVFileLoader loader = new JavaCSVFileLoader() {
            @Override
            public File getCopyPaths(ArchitectureMode mode) {
                return csvFile;
            }
        };

        CsvMalformedLineException exception = Assertions.assertThrows(CsvMalformedLineException.class, () ->
                loader.loadCopyData(ArchitectureMode.WALA),
                "Expected CsvException for malformed content");
    }

    @Test
    public void testLoadEditDataSuccess() throws IOException, CsvException {
        // Create a dummy CSV with header and one data row
        Path csvPath = tempDir.resolve("dummyEdit.csv");
        File csvFile = csvPath.toFile();
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(csvFile))) {
            writer.write("fileToEdit,editInstruction\n");
            writer.write("Example.java,replaceText");
        }

        JavaCSVFileLoader loader = new JavaCSVFileLoader() {
            @Override
            public File getEditPaths(AOPMode mode) {
                return csvFile;
            }
        };

        List<List<String>> rows = loader.loadEditData(AOPMode.INSTRUMENTATION);
        Assertions.assertEquals(1, rows.size());
        // Only data row (header is skipped)
        Assertions.assertEquals(List.of("Example.java", "replaceText"), rows.get(0));
    }

    @Test
    public void testLoadEditDataFileNotFoundExplicit() {
        // Use a non-existent file
        File missing = tempDir.resolve("no-edit.csv").toFile();
        JavaCSVFileLoader loader = new JavaCSVFileLoader() {
            @Override
            public File getEditPaths(ArchitectureMode mode) {
                return missing;
            }
        };
        Assertions.assertThrows(IOException.class, () ->
                loader.loadEditData(ArchitectureMode.ARCHUNIT),
                "Expected IOException for missing file");
    }

    @Test
    public void testLoadEditDataMalformed() throws IOException {
        // Create a malformed CSV (unclosed quote)
        Path csvPath = tempDir.resolve("malformedEdit.csv");
        File csvFile = csvPath.toFile();
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(csvFile))) {
            writer.write("colA,colB\n");
            writer.write("\"bad,entry");
        }

        JavaCSVFileLoader loader = new JavaCSVFileLoader() {
            @Override
            public File getEditPaths(AOPMode mode) {
                return csvFile;
            }
        };

        Assertions.assertThrows(CsvMalformedLineException.class, () ->
                loader.loadEditData(AOPMode.ASPECTJ),
                "Expected CsvException for malformed content");
    }
}
