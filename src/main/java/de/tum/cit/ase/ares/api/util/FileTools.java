package de.tum.cit.ase.ares.api.util;

//<editor-fold desc="Import">

import com.opencsv.CSVParser;
import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.exceptions.CsvException;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static de.tum.cit.ase.ares.api.aop.java.instrumentation.advice.JavaInstrumentationAdviceFileSystemToolbox.localize;
//</editor-fold>

/**
 * Utility class providing file-related operations such as copying files, reading file content,
 * and writing content to files. These methods are intended for use in scenarios where
 * secure and reliable file handling is required.
 */
public class FileTools {

    //<editor-fold desc="Constructor">

    /**
     * Private constructor to prevent instantiation of this utility class.
     * <p>
     * Since this is a utility class, it is not intended to be instantiated. The constructor
     * throws an {@link UnsupportedOperationException} to enforce this restriction.
     * </p>
     */
    private FileTools() {
        throw new UnsupportedOperationException("FileTools is a utility class and should not be instantiated");
    }
    //</editor-fold>

    //<editor-fold desc="Copy">

    /**
     * Copies a list of files to a specified target directory.
     * <p>
     * This method iterates over the provided list of source file paths, copying each file
     * to the specified target directory. It handles various exceptions that might occur
     * during the copy process, wrapping them in a {@link SecurityException}.
     * </p>
     *
     * @param sourceFilePaths a {@link List} of {@link Path} objects representing the source files to copy.
     * @param targetFilePaths a {@link List} of {@link Path} objects representing the target paths for the copied files.
     * @return a {@link List} of {@link Path} objects representing the paths of the copied files.
     * @throws SecurityException if an error occurs during the file copy process.
     */
    public static List<Path> copyFiles(List<Path> sourceFilePaths, List<Path> targetFilePaths) {
        return IntStream.range(0, sourceFilePaths.size()).mapToObj(i -> {
            try (InputStream sourceStream = FileTools.class.getResourceAsStream("/" + sourceFilePaths.get(i).toString())) {
                if (sourceStream == null) {
                    throw new IOException("Resource not found: " + sourceFilePaths.get(i));
                }
                if (!Files.exists(targetFilePaths.get(i).getParent())) {
                    Files.createDirectories(targetFilePaths.get(i).getParent());
                }
                Files.copy(sourceStream, targetFilePaths.get(i), StandardCopyOption.REPLACE_EXISTING);
            } catch (InvalidPathException e) {
                throw new SecurityException("Ares Security Error (Stage: Creation): Invalid path provided during file copy.", e);
            } catch (UnsupportedOperationException e) {
                throw new SecurityException("Ares Security Error (Stage: Creation): Unsupported operation during file copy.", e);
            } catch (FileAlreadyExistsException e) {
                throw new SecurityException("Ares Security Error (Stage: Creation): File already exists at target location.", e);
            } catch (IOException e) {
                throw new SecurityException("Ares Security Error (Stage: Creation): IO error occurred during file copy.", e);
            }
            return targetFilePaths.get(i);
        }).toList();
    }

    /**
     * Copies Java files, formats the content, and writes it to the target directory.
     * <p>
     * This method copies files from the source to the target and formats the copied files' content based
     * on the provided format values.
     * </p>
     *
     * @param sourceFilePaths the source file paths.
     * @param targetFilePaths the target file paths.
     * @param formatValues the format values for formatting the files.
     * @return a list of copied paths.
     * @throws SecurityException if an error occurs during the process.
     */
    public static List<Path> copyFormatStringFiles(List<Path> sourceFilePaths, List<Path> targetFilePaths, List<String[]> formatValues) {
        List<Path> copiedFiles = copyFiles(sourceFilePaths, targetFilePaths);
        for (int i = 0; i < copiedFiles.size(); i++) {
            try {
                String formatedFile;
                try {
                    formatedFile = String.format(Files.readString(copiedFiles.get(i)), (String[]) formatValues.get(i));
                } catch (IllegalFormatException e) {
                    throw new SecurityException("Ares Security Error (Stage: Creation): Illegal format in " + copiedFiles.get(i).toAbsolutePath() + ".", e);
                }
                Files.writeString(
                        copiedFiles.get(i),
                        formatedFile,
                        StandardOpenOption.WRITE
                );
            } catch (IOException e) {
                throw new SecurityException(localize("security.file-tools.read.content.failure"), e);
            }
        }
        return copiedFiles;
    }
    //</editor-fold>

    //<editor-fold desc="Read">

    /**
     * Reads the content of a file from the specified path.
     * <p>
     * This method reads the content of a file from the given source file path and returns it as a string.
     * Various exceptions that might occur during the file read process are caught and wrapped in a
     * {@link SecurityException}.
     * </p>
     *
     * @param sourceFilePath the {@link Path} of the file to read.
     * @return the content of the file as a {@link String}.
     * @throws SecurityException if an error occurs during the file read process.
     */
    public static String readFile(Path sourceFilePath) {
        try {

            InputStream sourceStream = FileTools.class.getResourceAsStream("/" + sourceFilePath.toString());

            if (sourceStream == null) {
                throw new IOException("Resource not found: " + sourceFilePath);
            }

            try (Scanner scanner = new Scanner(sourceStream, StandardCharsets.UTF_8)) {
                // Check if the scanner has any content
                if (scanner.hasNext()) {
                    return scanner.useDelimiter("\\A").next();
                } else {
                    return "";  // Return an empty string if the file is empty
                }
            }

        } catch (IOException e) {
            throw new SecurityException(localize("security.file-tools.read.content.failure"), e);
        } catch (OutOfMemoryError e) {
            throw new SecurityException("Ares Security Error (Stage: Creation): Out of memory while reading content.", e);
        } catch (IllegalFormatException e) {
            throw new SecurityException("Ares Security Error (Stage: Creation): Illegal format in content.", e);
        }
    }

    /**
     * Loads data from the corresponding CSV file.
     */
    public static List<List<String>> readCSVFile(File file) throws IOException, CsvException {
        CSVParser csvParserBuilder = new CSVParserBuilder()
                .withSeparator(',')
                .withQuoteChar('"')
                .build();
        try (
                CSVReader csvReaderBuilder = new CSVReaderBuilder(new FileReader(file))
                        .withCSVParser(csvParserBuilder)
                        .withSkipLines(1)
                        .build()
        ) {
            List<String[]> csvRowList = csvReaderBuilder.readAll();
            return csvRowList.stream().map(Arrays::asList).toList();
        }
    }

    /**
     * Loads data from the corresponding Rule file.
     */
    public static List<String> readRuleFile(Path sourceRulePath) throws IOException {
        return Files.readAllLines(getResourceAsFile(sourceRulePath.toString()).toPath());
    }
    //</editor-fold>

    //<editor-fold desc="Write">

    /**
     * Writes content to a file in the specified target directory.
     * <p>
     * This method creates a new file in the target directory with the specified file name and writes the provided
     * content to it. If the file creation or writing process fails, it throws a {@link SecurityException}.
     * </p>
     *
     * @param targetPath  the target directory {@link Path} where the file will be created.
     * @param fileName    the name of the file to create.
     * @param fileContent the content to write to the file.
     * @return the {@link Path} of the created and written file.
     * @throws SecurityException if an error occurs during the file creation or writing process.
     */
    private static Path writeFile(Path targetPath, String fileName, String fileContent) {
        Path fullTargetPath = targetPath.resolve(fileName);

        try {
            if (!Files.exists(targetPath)) {
                Files.createDirectories(targetPath);
            }
        } catch (InvalidPathException e) {
            throw new SecurityException("Ares Security Error (Stage: Creation): Invalid path provided for file creation.", e);
        } catch (UnsupportedOperationException e) {
            throw new SecurityException("Ares Security Error (Stage: Creation): Unsupported operation during directory creation.", e);
        } catch (IOException e) {
            throw new SecurityException("Ares Security Error (Stage: Creation): IO error occurred during directory creation.", e);
        }

        try {
            return Files.writeString(fullTargetPath, fileContent, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        } catch (IllegalArgumentException e) {
            throw new SecurityException("Ares Security Error (Stage: Creation): Illegal argument provided for file writing.", e);
        } catch (IOException e) {
            throw new SecurityException("Ares Security Error (Stage: Creation): IO error occurred during file writing.", e);
        } catch (NullPointerException e) {
            throw new SecurityException("Ares Security Error (Stage: Creation): Null pointer encountered during file writing.", e);
        } catch (UnsupportedOperationException e) {
            throw new SecurityException("Ares Security Error (Stage: Creation): Unsupported operation during file writing.", e);
        }
    }
    //</editor-fold>

    //<editor-fold desc="Resolve">

    /**
     * Resolves a path based on the target and additional path parts.
     *
     * @param target the base path to resolve.
     * @param furtherPathParts additional path parts.
     * @return the resolved path.
     */
    public static Path resolveOnTarget(Path target, String... furtherPathParts) {
        return Stream
                .of(furtherPathParts)
                .reduce(target, Path::resolve, Path::resolve);
    }

    public static Path resolveOn(@Nonnull Path projectPath, @Nonnull String packageName, String... furtherPathParts){
        String[] packageParts = packageName.split("\\.");
        String[] allParts = Stream.concat(Arrays.stream(packageParts), Arrays.stream(furtherPathParts))
                .toArray(String[]::new);
        return resolveOnTarget(projectPath, allParts);
    }

    /**
     * Resolves a path based on "de/tum/cit/ase/ares/api" and additional path parts.
     *
     * @param furtherPathParts additional path parts.
     * @return the resolved path.
     */
    public static Path resolveOnPackage(String... furtherPathParts) {
        Path target = Paths.get("de", "tum", "cit", "ase", "ares", "api");
        return resolveOnTarget(target, furtherPathParts);
    }

    /**
     * Resolves a path based on "src/main/java/[package]" and additional path parts.
     *
     * @param furtherPathParts additional path parts.
     * @return the resolved path.
     */
    public static Path resolveOnSource(Path projectPath, String packageName, String... furtherPathParts) {
        String[] prefix = new String[]{"src", "main", "java"};
        String[] infix = packageName.split("\\.");
        String[] newPrefix = Stream.concat(Arrays.stream(prefix), Arrays.stream(infix)).toArray(String[]::new);
        String[] newFurtherPathParts = Stream.concat(Arrays.stream(newPrefix), Arrays.stream(furtherPathParts)).toArray(String[]::new);
        return resolveOnTarget(projectPath, newFurtherPathParts);
    }

    /**
     * Resolves a path based on "src/test/java/[package]" and additional path parts.
     *
     * @param furtherPathParts additional path parts.
     * @return the resolved path.
     */
    public static Path resolveOnTests(Path projectPath, String packageName, String... furtherPathParts) {
        String[] prefix = new String[]{"src", "test", "java"};
        String[] infix = packageName.split("\\.");
        String[] newPrefix = Stream.concat(Arrays.stream(prefix), Arrays.stream(infix)).toArray(String[]::new);
        String[] newFurtherPathParts = Stream.concat(Arrays.stream(newPrefix), Arrays.stream(furtherPathParts)).toArray(String[]::new);
        return resolveOnTarget(projectPath, newFurtherPathParts);
    }
    //</editor-fold>

    //<editor-fold desc="Three Parted Java File">

    /**
     * Creates a new file by combining the content of a header file, a body string, and a footer file.
     * <p>
     * This method reads the content from the specified header and footer files, combines it with the provided body string,
     * and writes the combined content to a new file in the target directory with the specified file name.
     * </p>
     *
     * @param sourceHeaderPath the {@link Path} of the header file.
     * @param sourceBody       the body content as a {@link String}.
     * @param sourceFooterPath the {@link Path} of the footer file.
     * @param target           the target directory {@link Path} where the file will be created.
     * @return the {@link Path} of the created file.
     * @throws SecurityException if an error occurs during the file creation or writing process.
     */
    public static Path createThreePartedFile(
            Path sourceHeaderPath, String sourceBody, Path sourceFooterPath,
            Path target
    ) {
        String sourceHeaderContent = readFile(sourceHeaderPath);
        String sourceFooterContent = readFile(sourceFooterPath);
        String fileContent = String.join("\n", List.of(sourceHeaderContent, sourceBody, sourceFooterContent));
        return writeFile(target.getParent(), target.getFileName().toString(), fileContent);
    }

    public static Path createThreePartedFormatStringFile(
            Path sourceHeaderPath, String sourceBody, Path sourceFooterPath,
            Path target, String[] formatValues
    ) {
        Path createdFile = createThreePartedFile(sourceHeaderPath, sourceBody, sourceFooterPath, target);
        try {
            Files.writeString(
                    createdFile,
                    String.format(Files.readString(createdFile), (String[]) formatValues),
                    StandardOpenOption.WRITE
            );
        } catch (IOException e) {
            throw new SecurityException(localize("security.file-tools.read.content.failure"), e);
        }
        return createdFile;
    }
    //</editor-fold>

    //<editor-fold desc="Rest">

    /**
     * Generates an array of package name strings.
     * <p>
     * This method creates an array containing multiple copies of the provided package name.
     * The length of the array is determined by the {@code numberOfEntries} parameter.
     * </p>
     *
     * @param packageName     the package name to be repeated in the array.
     * @param numberOfEntries the number of times the package name should be repeated in the array.
     * @return a {@link String} array where each element is the provided package name.
     */
    public static String[] generatePackageNameArray(String packageName, int numberOfEntries) {
        return IntStream.range(0, numberOfEntries).mapToObj(i -> packageName).toArray(String[]::new);
    }

    /**
     * Reads the content of a file and returns it as a set of strings.
     * @param filePath The path to the file
     * @return a set of strings representing the content of the file
     */
    public static Set<String> readMethodsFromGivenPath(Path filePath) {
        String fileContent = FileTools.readFile(filePath);
        String normalizedContent = fileContent.replace("\r\n", "\n").replace("\r", "\n");
        List<String> methods = Arrays.stream(normalizedContent.split("\n"))
                // Filter out comments
                .filter(str -> !str.startsWith("#"))
                .toList();
        return new HashSet<>(methods);
    }

    /**
     * Reads the content of a resource file and returns a File
     * @param resourcePath The path to the resource file
     * @return The File object representing the resource file
     * @implNote This method creates a temporary file that will be deleted when the JVM exits
     */
    public static File getResourceAsFile(String resourcePath) throws IOException {
        // Load the resource as an InputStream
        try (InputStream inputStream = FileTools.class.getClassLoader().getResourceAsStream(resourcePath)) {
            if (inputStream == null) {
                throw new SecurityException(localize("file.tools.resource.not.found", resourcePath));
            }

            // Create a temporary file using modern NIO APIs
            Path tempFilePath;
            try {
                tempFilePath = Files.createTempFile("resource-", ".txt");
                // Register for deletion on JVM exit
                tempFilePath.toFile().deleteOnExit();
            } catch (IOException e) {
                throw new SecurityException(localize("file.tools.create.temp.file.error", resourcePath));
            }

            // Write the content of the InputStream to the temp file
            try {
                Files.copy(inputStream, tempFilePath, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
                throw new SecurityException(localize("file.tools.resource.not.writeable", tempFilePath.toAbsolutePath()), e);
            }

            // Return the temporary file
            return tempFilePath.toFile();
        } catch (IOException e) {
            throw new SecurityException(localize("file.tools.io.error", resourcePath), e);
        }
    }

    public static List<List<String>> readCFGFile(Path sourceCFGPath) throws IOException {
        return Files
                .lines(sourceCFGPath)
                .map(String::trim)
                .filter(line -> !line.isEmpty())
                .map(line -> List.of(line.split("\\s+")))
                .peek(parts -> {
                    if (parts.size() != 3) {
                        throw new IllegalArgumentException(
                                "Invalid cfg format (expected exactly 3 tokens per line): " + parts);
                    }
                })
                .collect(Collectors.toList());
    }
    //</editor-fold>
}