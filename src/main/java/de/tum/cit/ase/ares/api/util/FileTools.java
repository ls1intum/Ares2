package de.tum.cit.ase.ares.api.util;

import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.IllegalFormatException;
import java.util.List;

/**
 * Utility class providing file-related operations such as copying files, reading file content,
 * and writing content to files. These methods are intended for use in scenarios where
 * secure and reliable file handling is required.
 */
public class FileTools {

    /**
     * Private constructor to prevent instantiation of this utility class.
     * <p>
     * Since this is a utility class, it is not intended to be instantiated. The constructor
     * throws an {@link IllegalStateException} to enforce this restriction.
     * </p>
     */
    private FileTools() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * Copies a list of files to a specified target directory.
     * <p>
     * This method iterates over the provided list of source file paths, copying each file
     * to the specified target directory. It handles various exceptions that might occur
     * during the copy process, wrapping them in a {@link SecurityException}.
     * </p>
     *
     * @param sourceFilePaths a {@link List} of {@link Path} objects representing the source files to copy.
     * @param targetPath the target directory {@link Path} where the files will be copied.
     * @return a {@link List} of {@link Path} objects representing the paths of the copied files.
     * @throws SecurityException if an error occurs during the file copy process.
     */
    public static List<Path> copyFiles(List<Path> sourceFilePaths, Path targetPath) {
        return sourceFilePaths.stream().map(sourceFilePath -> {
            try {
                return Files.copy(sourceFilePath, targetPath.resolve(sourceFilePath.getFileName()));
            } catch (InvalidPathException e) {
                throw new SecurityException("Ares Security Error (Stage: Creation): Invalid path provided during file copy.", e);
            } catch (UnsupportedOperationException e) {
                throw new SecurityException("Ares Security Error (Stage: Creation): Unsupported operation during file copy.", e);
            } catch (FileAlreadyExistsException e) {
                throw new SecurityException("Ares Security Error (Stage: Creation): File already exists at target location.", e);
            } catch (IOException e) {
                throw new SecurityException("Ares Security Error (Stage: Creation): IO error occurred during file copy.", e);
            }
        }).toList();
    }

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
    private static String readFile(Path sourceFilePath) {
        try {
            return Files.readString(sourceFilePath);
        } catch (IOException e) {
            throw new SecurityException("Ares Security Error (Stage: Creation): Failed to read content from source file.", e);
        } catch (OutOfMemoryError e) {
            throw new SecurityException("Ares Security Error (Stage: Creation): Out of memory while reading content.", e);
        } catch (IllegalFormatException e) {
            throw new SecurityException("Ares Security Error (Stage: Creation): Illegal format in content.", e);
        }
    }

    /**
     * Writes content to a file in the specified target directory.
     * <p>
     * This method creates a new file in the target directory with the specified file name and writes the provided
     * content to it. If the file creation or writing process fails, it throws a {@link SecurityException}.
     * </p>
     *
     * @param targetPath the target directory {@link Path} where the file will be created.
     * @param fileName the name of the file to create.
     * @param fileContent the content to write to the file.
     * @return the {@link Path} of the created and written file.
     * @throws SecurityException if an error occurs during the file creation or writing process.
     */
    private static Path writeFile(Path targetPath, String fileName, String fileContent) {
        Path fullTargetPath;

        try {
            fullTargetPath = Files.createFile(targetPath.resolve(fileName));
        } catch (InvalidPathException e) {
            throw new SecurityException("Ares Security Error (Stage: Creation): Invalid path provided for file creation.", e);
        } catch (UnsupportedOperationException e) {
            throw new SecurityException("Ares Security Error (Stage: Creation): Unsupported operation during file creation.", e);
        } catch (FileAlreadyExistsException e) {
            throw new SecurityException("Ares Security Error (Stage: Creation): Target file already exists.", e);
        } catch (IOException e) {
            throw new SecurityException("Ares Security Error (Stage: Creation): IO error occurred during file creation.", e);
        }

        try {
            return Files.writeString(fullTargetPath, fileContent, StandardOpenOption.WRITE);
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

    /**
     * Creates a new file by combining the content of a header file, a body string, and a footer file.
     * <p>
     * This method reads the content from the specified header and footer files, combines it with the provided body string,
     * and writes the combined content to a new file in the target directory with the specified file name.
     * </p>
     *
     * @param sourceHeaderPath the {@link Path} of the header file.
     * @param sourceBody the body content as a {@link String}.
     * @param sourceFooterPath the {@link Path} of the footer file.
     * @param fileName the name of the new file to create.
     * @param targetPath the target directory {@link Path} where the new file will be created.
     * @return the {@link Path} of the created file.
     * @throws SecurityException if an error occurs during the file creation or writing process.
     */
    public static Path createThreePartedFile(
            Path sourceHeaderPath, String sourceBody, Path sourceFooterPath,
            String fileName, Path targetPath
    ) {
        String sourceHeaderContent = readFile(sourceHeaderPath);
        String sourceFooterContent = readFile(sourceFooterPath);
        String fileContent = String.join("\n", List.of(sourceHeaderContent, sourceBody, sourceFooterContent));
        return writeFile(targetPath, fileName, fileContent);
    }
}