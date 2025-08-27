package de.tum.cit.ase.ares.api;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Collectors;

/**
 * Utility class to list and read Java files from the de.tum.cit.ase.ares.api library.
 */
public class JavaFileLister {

    private static final String ARES_API_PACKAGE = "de/tum/cit/ase/ares/api";

    /**
     * Lists all .java files from the de.tum.cit.ase.ares.api library.
     *
     * @return List of Java file paths within the ares.api package
     */
    public List<String> listJavaFiles() {
        try {
            String path = Class.forName("de.tum.cit.ase.ares.api.util.FileTools").getProtectionDomain().getCodeSource().getLocation().getPath();

            if (path.endsWith(".jar")) {
                try (JarFile jar = new JarFile(path)) {
                    return jar.stream()
                            .map(JarEntry::getName)
                            .filter(name -> name.startsWith(ARES_API_PACKAGE) && name.endsWith(".java"))
                            .sorted()
                            .collect(Collectors.toList());
                }
            } else {
                URL resource = getClass().getClassLoader().getResource(ARES_API_PACKAGE);
                return resource == null ? Collections.emptyList() :
                        Files.walk(Path.of(resource.getPath()))
                                .filter(Files::isRegularFile)
                                .filter(filePath -> filePath.toString().endsWith(".java"))
                                .map(p -> ARES_API_PACKAGE + "/" + Path.of(resource.getPath()).relativize(p).toString().replace('\\', '/'))
                                .sorted()
                                .collect(Collectors.toList());
            }
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    /**
     * Reads the content of a Java file from the ares.api library.
     *
     * @param name The name/path of the Java file to read
     * @return The content of the file as a String, or null if not found or error occurred
     */
    public String read(String name) {
        String filePath = name.startsWith(ARES_API_PACKAGE) ? name :
                listJavaFiles().stream()
                        .filter(f -> f.endsWith("/" + (name.endsWith(".java") ? name : name + ".java")) ||
                                f.equals(name.endsWith(".java") ? name : name + ".java"))
                        .findFirst().orElse(name);

        try (var stream = getClass().getClassLoader().getResourceAsStream(filePath)) {
            if (stream == null) return null;
            try (var reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
                return reader.lines().collect(Collectors.joining(System.lineSeparator()));
            }
        } catch (IOException e) {
            return null;
        }
    }

    /**
     * Main method for testing the JavaFileLister functionality.
     */
    public static void main(String[] args) {
        JavaFileLister lister = new JavaFileLister();

        System.out.println("Listing Java files from de.tum.cit.ase.ares.api:");
        List<String> javaFiles = lister.listJavaFiles();

        for (String file : javaFiles) {
            System.out.println("- " + file);
        }

        System.out.println("\nTotal files found: " + javaFiles.size());

        // Example of reading a file (if any files were found)
        if (!javaFiles.isEmpty()) {
            String firstFile = javaFiles.get(0);
            System.out.println("\nReading content of first file: " + firstFile);
            String content = lister.read(firstFile);
            if (content != null) {
                System.out.println("Content length: " + content.length() + " characters");
                System.out.println("First 200 characters:");
                System.out.println(content.substring(0, Math.min(200, content.length())) + "...");
            }
        }
    }
}
