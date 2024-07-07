package de.tum.cit.ase.ares.api.aspectJ.main;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.Objects;

public class Main {

    public final Path getPath(String first, String... more) {
        Objects.requireNonNull(first);
        String path;
        if (more.length == 0) {
            path = first;
        } else {
            StringBuilder sb = new StringBuilder();
            sb.append(first);
            for (String segment: more) {
                if (!segment.isEmpty()) {
                    if (sb.length() > 0)
                        sb.append('/');
                    sb.append(segment);
                }
            }
            path = sb.toString();
        }
        return Paths.get(path);
    }

    public void writeMethod(String filePath) {
        Objects.requireNonNull(filePath);
        try {
            Files.write(Paths.get(filePath), Arrays.asList("Hello World"), StandardOpenOption.CREATE);
            System.out.println("Successfully wrote to the file.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        String filePath = "fileUsingFilesClass.txt";

        try {
            Files.write(Paths.get(filePath), Arrays.asList("Hello World"), StandardOpenOption.CREATE);
            System.out.println("Successfully wrote to the file.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}