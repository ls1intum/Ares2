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
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static de.tum.cit.ase.ares.api.aop.java.instrumentation.advice.JavaInstrumentationAdviceFileSystemToolbox.localize;
//</editor-fold>

/**
 * Utility class providing file-related operations such as copying files,
 * reading file content,
 * and writing content to files. These methods are intended for use in scenarios
 * where
 * secure and reliable file handling is required.
 */
public class FileTools {

    // <editor-fold desc="Constructor">

    /**
     * A private constructor to prevent instantiation of this utility class.
     * <p>
     * Since this is a utility class, it is not intended to be instantiated. The
     * constructor
     * throws an {@link UnsupportedOperationException} to enforce this restriction.
     * </p>
     */
    private FileTools() {
        throw new UnsupportedOperationException("FileTools is a utility class and should not be instantiated");
    }
    // </editor-fold>

    // <editor-fold desc="Resolve">
    public static Path resolveOnPath(@Nonnull Path path, @Nonnull String... furtherPathParts) {
        return Stream
                .of(furtherPathParts)
                .reduce(path, Path::resolve, Path::resolve);
    }

    private static Path resolveSourceDirectory() {
        try {
            URL url = Class.forName("de.tum.cit.ase.ares.api.util.FileTools")
                    .getProtectionDomain()
                    .getCodeSource()
                    .getLocation();

            Path classesDir = Path.of(url.toURI()).toAbsolutePath().normalize();
            return resolveOnPath(classesDir, "de", "tum", "cit", "ase", "ares", "api");
        }
        catch (ClassNotFoundException | URISyntaxException e) {
            throw new SecurityException(e.getMessage());
        }
    }

    public static Path resolveFileOnSourceDirectory(@Nonnull String... furtherPathParts) {
        return resolveOnPath(resolveSourceDirectory(), furtherPathParts);
    }

    public static Path resolveFileOnTargetDirectory(@Nonnull Path targetPath, @Nonnull String... furtherPathParts) {
        return resolveOnPath(targetPath, furtherPathParts);
    }
    // </editor-fold>

    // <editor-fold desc="Read">

    public static File readFile(Path sourceFilePath) {
        try {
            return sourceFilePath.toFile();
        } catch (OutOfMemoryError e) {
            throw new SecurityException("Ares Security Error (Stage: Creation): Out of memory while reading content.",
                    e);
        } catch (IllegalFormatException e) {
            throw new SecurityException("Ares Security Error (Stage: Creation): An illegal format in content.", e);
        } catch (NullPointerException e) {
            throw new SecurityException("Ares Security Error (Stage: Creation): A null pointer encountered while reading file.",
                    e);
        } catch (UnsupportedOperationException e) {
            throw new SecurityException("Ares Security Error (Stage: Creation): Unsupported operation while reading file.",
                    e);
        }
    }

    /**
     * Loads data from the corresponding CSV sourceFile.
     */
    public static List<List<String>> readCSVFile(File sourceFile) throws IOException, CsvException {
        CSVParser csvParserBuilder = new CSVParserBuilder()
                .withSeparator(',')
                .withQuoteChar('"')
                .build();
        try (
                CSVReader csvReaderBuilder = new CSVReaderBuilder(new FileReader(sourceFile))
                        .withCSVParser(csvParserBuilder)
                        .withSkipLines(1)
                        .build()) {
            List<String[]> csvRowList = csvReaderBuilder.readAll();
            return csvRowList.stream().map(Arrays::asList).toList();
        }
    }

    /**
     * Reads the content of a sourceFile and returns it as a set of strings.
     *
     * @param sourceFile The sourceFile to read
     * @return a set of strings representing the content of the sourceFile
     */
    public static Set<String> readMethodsFile(File sourceFile) {
        try {
            String fileContent = Files.readString(sourceFile.toPath(), StandardCharsets.UTF_8);
            String normalizedContent = fileContent.replace("\r\n", "\n").replace("\r", "\n");
            List<String> methods = Arrays.stream(normalizedContent.split("\n"))
                    // Filter out comments
                    .filter(str -> !str.startsWith("#"))
                    .toList();
            return new HashSet<>(methods);
        } catch (IOException e) {
            throw new SecurityException(localize("security.file-tools.read.content.failure"), e);
        } catch (OutOfMemoryError e) {
            throw new SecurityException("Ares Security Error (Stage: Creation): Out of memory while reading content.", e);
        }
    }

    /**
     * Loads data from the corresponding Rule sourceFile.
     */
    public static List<String> readRuleFile(File sourceFile) throws IOException {
        return Files.readAllLines(sourceFile.toPath(), StandardCharsets.UTF_8);
    }
    // </editor-fold>

    // <editor-fold desc="Write">

    // Common write helper used across write operations
    private static Path writeFile(Path targetPath, String content, java.nio.file.OpenOption... options) {
        try {
            return Files.writeString(targetPath, content, options);
        } catch (IllegalArgumentException e) {
            throw new SecurityException(
                    "Ares Security Error (Stage: Creation): Illegal argument provided for targetPath writing.", e);
        } catch (IOException e) {
            throw new SecurityException("Ares Security Error (Stage: Creation): IO error occurred during targetPath writing.",
                    e);
        } catch (NullPointerException e) {
            throw new SecurityException(
                    "Ares Security Error (Stage: Creation): A null pointer encountered during targetPath writing.", e);
        } catch (UnsupportedOperationException e) {
            throw new SecurityException(
                    "Ares Security Error (Stage: Creation): Unsupported operation during targetPath writing.", e);
        }
    }
    // </editor-fold>

    // <editor-fold desc="Copy">

    /**
     * Copies a list of files to a specified target directory.
     * <p>
     * This method iterates over the provided list of source file paths, copying
     * each file
     * to the specified target directory. It handles various exceptions that might
     * occur
     * during the copy process, wrapping them in a {@link SecurityException}.
     * </p>
     *
     * @param sourceFilePaths a {@link List} of {@link Path} objects representing
     *                        the source files to copy.
     * @param targetFilePaths a {@link List} of {@link Path} objects representing
     *                        the target paths for the copied files.
     * @return a {@link List} of {@link Path} objects representing the paths of the
     *         copied files.
     * @throws SecurityException if an error occurs during the file copy process.
     */
    private static List<Path> copyFiles(List<Path> sourceFilePaths, List<Path> targetFilePaths) {
        return IntStream.range(0, sourceFilePaths.size()).mapToObj(i -> {
            try {
                Path parentPath = targetFilePaths.get(i).getParent();
                if (parentPath != null && !Files.exists(parentPath)) {
                    Files.createDirectories(parentPath);
                }
                Files.copy(sourceFilePaths.get(i), targetFilePaths.get(i), StandardCopyOption.REPLACE_EXISTING);
            } catch (InvalidPathException e) {
                throw new SecurityException(
                        "Ares Security Error (Stage: Creation): Invalid path provided during file copy.", e);
            } catch (UnsupportedOperationException e) {
                throw new SecurityException(
                        "Ares Security Error (Stage: Creation): Unsupported operation during file copy.", e);
            } catch (FileAlreadyExistsException e) {
                throw new SecurityException(
                        "Ares Security Error (Stage: Creation): File already exists at target location.", e);
            } catch (IOException e) {
                throw new SecurityException(
                        "Ares Security Error (Stage: Creation): IO error occurred during file copy.", e);
            }
            return targetFilePaths.get(i);
        }).toList();
    }

    /**
     * Copies Java files, formats the content, and writes it to the target
     * directory.
     * <p>
     * This method copies files from the source to the target and formats the copied
     * files' content based
     * on the provided format values.
     * </p>
     *
     * @param sourceFilePaths the source file paths.
     * @param targetFilePaths the target file paths.
     * @param formatValues    the format values for formatting the files.
     * @return a list of copied paths.
     * @throws SecurityException if an error occurs during the process.
     */
    public static List<Path> copyAndFormatFSFiles(List<Path> sourceFilePaths, List<Path> targetFilePaths,
                                                  List<String[]> formatValues) {
        List<Path> copiedFiles = copyFiles(sourceFilePaths, targetFilePaths);
        formatFSFiles(copiedFiles, formatValues);
        return copiedFiles;
    }

    public static List<Path> copyAndFormatNonFSFiles(List<Path> sourceFilePaths, List<Path> targetFilePaths,
                                                     List<String[]> placeholderValues, List<String[]> formatValues) {
        List<Path> copiedFiles = copyFiles(sourceFilePaths, targetFilePaths);
        formatNonFSFiles(copiedFiles, placeholderValues, formatValues);
        return copiedFiles;
    }
    // </editor-fold>

    // <editor-fold desc="Format">

    private static int countOccurrences(String content, String needle) {
        if (content == null || needle == null || needle.isEmpty())
            return 0;
        int count = 0, idx = 0, step = needle.length();
        while ((idx = content.indexOf(needle, idx)) != -1) {
            count++;
            idx += step;
        }
        return count;
    }

    private static String replacePlaceholdersWithFormatSpec(String content, String[] placeholders) {
        if (content == null || placeholders == null)
            return content;
        String result = content;
        for (String ph : placeholders) {
            if (ph != null && !ph.isEmpty()) {
                result = result.replace(ph, "%s");
            }
        }
        return result;
    }

    private static String formatTemplate(String template, String[] values, Path pathForError) {
        try {
            return String.format(template, (Object[]) values);
        } catch (IllegalFormatException e) {
            throw new SecurityException(
                    "Ares Security Error (Stage: Creation): Illegal format in " + pathForError.toAbsolutePath() + ".",
                    e);
        }
    }

    /**
     * Formats files by applying format values to their content.
     * <p>
     * This method reads the content of each file, applies the corresponding format
     * values using
     * {@link String#format(String, Object...)}, and writes the formatted content
     * back to the file.
     * </p>
     *
     * @param formatValues    the format values for formatting the files.
     * @throws SecurityException if an error occurs during the formatting process.
     */
    private static void formatFSFiles(List<Path> targetFilePaths,
                                      List<String[]> formatValues) {
        IntStream.range(0, targetFilePaths.size()).forEach(i -> {
            Path target = targetFilePaths.get(i);
            try {
                String template = Files.readString(FileTools.readFile(target).toPath(), StandardCharsets.UTF_8);
                String formatted = formatTemplate(template, formatValues.get(i), target);
                writeFile(target, formatted, StandardOpenOption.WRITE);
            } catch (IOException e) {
                throw new SecurityException();
            }
        });
    }

    private static String processPlaceholdersAndFormat(String content, String[] placeholderValues,
                                                       String[] formatValues, Path pathForError) {
        content = replacePlaceholdersWithFormatSpec(content, placeholderValues);
        String[] values = (formatValues != null) ? formatValues : new String[0];
        int valuesCount = values.length;
        int placeholderCount = countOccurrences(content, "%s");
        if (placeholderCount != valuesCount) {
            throw new SecurityException(
                    "Ares Security Error (Stage: Creation): Placeholder count mismatch in "
                            + pathForError.toAbsolutePath() + ". Expected " + valuesCount
                            + " '%s' placeholders, but found " + placeholderCount + ".");
        }
        return formatTemplate(content, values, pathForError);
    }

    /**
     * Replaces custom placeholders with "%s" and then formats files using the
     * provided values.
     * <p>
     * For each target file, all occurrences of the corresponding placeholders (from
     * {@code placeholderValues})
     * are replaced literally with "%s". After replacement, the method validates
     * that the number of "%s"
     * occurrences equals the number of format values for that file. If the counts
     * mismatch, a
     * {@link SecurityException} is thrown.
     * </p>
     *
     * @param targetFilePaths   the target files to process.
     * @param placeholderValues per-file arrays of placeholder literals to be
     *                          replaced with "%s".
     * @param formatValues      per-file arrays of values to inject via
     *                          {@link String#format(String, Object...)}.
     * @throws SecurityException if IO fails or placeholder count does not match
     *                           format values count.
     */
    private static void formatNonFSFiles(List<Path> targetFilePaths,
                                         List<String[]> placeholderValues,
                                         List<String[]> formatValues) {
        IntStream.range(0, targetFilePaths.size()).forEach(i -> {
            Path targetPath = targetFilePaths.get(i);
            try {
                String content = Files.readString(FileTools.readFile(targetPath).toPath(), StandardCharsets.UTF_8);
                String[] perFilePlaceholders = (placeholderValues != null && placeholderValues.size() > i)
                        ? placeholderValues.get(i)
                        : null;
                String[] values = (formatValues != null && formatValues.size() > i && formatValues.get(i) != null)
                        ? formatValues.get(i)
                        : new String[0];
                String formatted = processPlaceholdersAndFormat(content, perFilePlaceholders, values, targetPath);
                writeFile(targetPath, formatted, StandardOpenOption.WRITE);
            } catch (IOException e) {
                throw new SecurityException();
            }
        });
    }

    /**
     * Generates an array of package name strings.
     * <p>
     * This method creates an array containing multiple copies of the provided
     * package name.
     * The length of the array is determined by the {@code numberOfEntries}
     * parameter.
     * </p>
     *
     * @param packageName     the package name to be repeated in the array.
     * @param numberOfEntries the number of times the package name should be
     *                        repeated in the array.
     * @return a {@link String} array where each element is the provided package
     *         name.
     */
    public static String[] generatePackageNameArray(String packageName, int numberOfEntries) {
        return IntStream.range(0, numberOfEntries)
                .mapToObj(i -> packageName)
                .toArray(String[]::new);
    }
    // </editor-fold>

    // <editor-fold desc="Three Parted Java File">

    /**
     * Creates a new file by combining the content of a header file, a body string,
     * and a footer file.
     * <p>
     * This method reads the content from the specified header and footer files,
     * combines it with the provided body string,
     * and writes the combined content to a new file in the target directory with
     * the specified file name.
     * </p>
     *
     * @param sourceHeaderPath the {@link Path} of the header file.
     * @param sourceBody       the body content as a {@link String}.
     * @param sourceFooterPath the {@link Path} of the footer file.
     * @param target           the target directory {@link Path} where the file will
     *                         be created.
     * @return the {@link Path} of the created file.
     * @throws SecurityException if an error occurs during the file creation or
     *                           writing process.
     */
    public static Path createThreePartedFile(
            Path sourceHeaderPath, String sourceBody, Path sourceFooterPath,
            Path target) {
        try {
            String sourceHeaderContent = Files.readString(FileTools.readFile(sourceHeaderPath).toPath(), StandardCharsets.UTF_8);
            String sourceFooterContent = Files.readString(FileTools.readFile(sourceFooterPath).toPath(), StandardCharsets.UTF_8);
            String fileContent = String.join("\n", List.of(sourceHeaderContent, sourceBody, sourceFooterContent));
            Path fullTargetPath = target.getParent().resolve(target.getFileName());

            if (!Files.exists(target.getParent())) {
                try {
                    Files.createDirectories(target.getParent());
                } catch (IOException e) {
                    throw new SecurityException(localize("security.file-tools.create.target.directory.failed"), e);
                }
            }
            return FileTools.writeFile(fullTargetPath, fileContent, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        } catch (IOException e) {
            throw new SecurityException(localize("security.file-tools.create.target.directory.failed"), e);
        }
    }

    /**
     * Creates a new file by concatenating a header file, an in-memory body string,
     * and a footer file,
     * then formats the resulting content using
     * {@link String#format(String, Object...)}.
     * <p>
     * The method performs two steps:
     * <ol>
     * <li>Creates the file at {@code target} by joining the content of
     * {@code sourceHeaderPath},
     * {@code sourceBody}, and {@code sourceFooterPath} (in that order).</li>
     * <li>Reads the created file, applies {@link String#format(String, Object...)}
     * with the provided
     * {@code formatValues}, and writes the formatted content back to the same
     * file.</li>
     * </ol>
     * </p>
     *
     * @param sourceHeaderPath the path (within the classpath resources) of the
     *                         header file whose content
     *                         will be placed at the beginning of the created file.
     * @param sourceBody       the body text to be inserted between header and
     *                         footer.
     * @param sourceFooterPath the path (within the classpath resources) of the
     *                         footer file whose content
     *                         will be placed at the end of the created file.
     * @param target           the target path (on the filesystem) of the file to
     *                         create and format.
     * @param formatValues     the values to be injected into format specifiers
     *                         (e.g., {@code %s}) contained
     *                         in the concatenated content.
     * @return the path to the created and formatted file.
     * @throws SecurityException      if an I/O error occurs while reading or
     *                                writing the file content.
     * @throws IllegalFormatException if the format string contains an illegal
     *                                syntax for the provided values.
     */
    public static Path createThreePartedFormatStringFile(
            Path sourceHeaderPath, String sourceBody, Path sourceFooterPath,
            Path target, String[] formatValues) {
        try {
            Path createdFile = createThreePartedFile(sourceHeaderPath, sourceBody, sourceFooterPath, target);
            String template = Files.readString(FileTools.readFile(createdFile).toPath(), StandardCharsets.UTF_8);
            String formatted = formatTemplate(template, formatValues, createdFile);
            writeFile(createdFile, formatted, StandardOpenOption.WRITE);
            return createdFile;
        } catch (IOException e) {
            throw new SecurityException();
        }
    }
    // </editor-fold>
}
