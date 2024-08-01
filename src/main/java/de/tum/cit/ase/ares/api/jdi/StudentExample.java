package de.tum.cit.ase.ares.api.jdi;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class StudentExample {

    public static void main(String[] args) throws IOException, InterruptedException {
        Files.readString(Path.of("src/main/java/org/example/StudentExample.java"));
    }
}
