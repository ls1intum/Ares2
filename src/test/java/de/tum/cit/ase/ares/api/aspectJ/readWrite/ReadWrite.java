package de.tum.cit.ase.ares.api.aspectJ.readWrite;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.Objects;

public class ReadWrite {

    public void writeMethod(String filePath) {
        Objects.requireNonNull(filePath);
        try {
            Files.write(Paths.get(filePath), Arrays.asList("Hello World"), StandardOpenOption.CREATE);
            System.out.println("Successfully wrote to the file.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void readMethod(String filePath) {
        Objects.requireNonNull(filePath);
        try {
            String content = new String(Files.readAllBytes(Paths.get(filePath)));
            System.out.println("File content: " + content);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}