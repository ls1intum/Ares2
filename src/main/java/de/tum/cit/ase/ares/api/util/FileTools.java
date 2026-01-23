package de.tum.cit.ase.ares.api.util;

//<editor-fold desc="Import">

import static de.tum.cit.ase.ares.api.aop.java.instrumentation.advice.JavaInstrumentationAdviceFileSystemToolbox.localize;
import static de.tum.cit.ase.ares.api.localization.Messages.localized;

import java.io.File;
import java.io.FileReader;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import javax.annotation.Nonnull;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.google.common.io.Closer;
import com.opencsv.CSVParser;
import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.exceptions.CsvException;
//</editor-fold>

/**
 * Utility class providing file-related operations such as copying files,
 * reading file content, and writing content to files. These methods are
 * intended for use in scenarios where secure and reliable file handling is
 * required.
 */
public class FileTools {

	private static final Pattern JAR_PATH_PATTERN = Pattern.compile("(?i)^(.*?\\.jar)(?:!|[\\\\/])(.+)$");

	// <editor-fold desc="Constructor">

	/**
	 * A private constructor to prevent instantiation of this utility class.
	 * <p>
	 * Since this is a utility class, it is not intended to be instantiated. The
	 * constructor throws an {@link UnsupportedOperationException} to enforce this
	 * restriction.
	 * </p>
	 */
	private FileTools() {
		throw new UnsupportedOperationException("FileTools is a utility class and should not be instantiated");
	}
	// </editor-fold>

	// <editor-fold desc="Resolve">
	/**
	 * Resolves a sequence of path parts against a base path.
	 * <p>
	 * This method takes a base {@link Path} and a variable number of string
	 * segments, resolving them in order to produce a final {@link Path}. It uses
	 * {@link Path#resolve(String)} to combine the base path with each subsequent
	 * segment.
	 * </p>
	 *
	 * @param path the base {@link Path} to resolve against.
	 * @param furtherPathParts the additional path segments to append to the base
	 *            path.
	 * @return the resolved {@link Path} combining the base path and all provided
	 *         segments.
	 * @throws NullPointerException if either {@code path} or
	 *             {@code furtherPathParts} is null.
	 */
	public static Path resolveOnPath(@Nonnull Path path, @Nonnull String... furtherPathParts) {
		return Stream.of(furtherPathParts).reduce(path, Path::resolve, Path::resolve);
	}

	/**
	 * Resolves the source directory path based on the location of the FileTools
	 * class.
	 * <p>
	 * This method determines the absolute path to the source directory by locating
	 * the compiled FileTools class and navigating to the expected source directory
	 * structure.
	 * </p>
	 *
	 * @return the resolved source directory {@link Path}.
	 * @throws SecurityException if the FileTools class cannot be found or if there
	 *             is an error converting its location to a URI.
	 */
	private static Path resolveSourceDirectory() {
		try {
			URL url = Class.forName("de.tum.cit.ase.ares.api.util.FileTools").getProtectionDomain().getCodeSource().getLocation();

			Path classesDir = Path.of(url.toURI()).toAbsolutePath().normalize();
			String[] packageParts = "de.tum.cit.ase".split("\\.");
			String[] furtherPathParts = { "ares", "api" };
			return resolveOnPath(classesDir, Stream.concat(Arrays.stream(packageParts), Arrays.stream(furtherPathParts)).toArray(String[]::new));
		} catch (ClassNotFoundException | URISyntaxException e) {
			throw new SecurityException(e.getMessage());
		}
	}

	/**
	 * Resolves a file path within the source directory.
	 * <p>
	 * This method constructs a full {@link Path} by resolving the provided path
	 * segments against the source directory path determined by
	 * {@link #resolveSourceDirectory()}.
	 * </p>
	 *
	 * @param furtherPathParts the additional path segments to append to the source
	 *            directory path.
	 * @return the resolved {@link Path} within the source directory.
	 * @throws NullPointerException if {@code furtherPathParts} is null.
	 */
	public static Path resolveFileOnSourceDirectory(@Nonnull String... furtherPathParts) {
		return resolveOnPath(resolveSourceDirectory(), furtherPathParts);
	}

	/**
	 * Resolves a file path within a specified target directory.
	 * <p>
	 * This method constructs a full {@link Path} by resolving the provided path
	 * segments against the given target directory path.
	 * </p>
	 *
	 * @param targetPath the base {@link Path} of the target directory.
	 * @param furtherPathParts the additional path segments to append to the target
	 *            directory path.
	 * @return the resolved {@link Path} within the specified target directory.
	 * @throws NullPointerException if either {@code targetPath} or
	 *             {@code furtherPathParts} is null.
	 */
	public static Path resolveFileOnTargetDirectory(@Nonnull Path targetPath, @Nonnull String... furtherPathParts) {
		return resolveOnPath(targetPath, furtherPathParts);
	}
	// </editor-fold>

	// <editor-fold desc="Read">

	@Nonnull
	private static InputStream createInputStream(@Nonnull Path path) throws IOException {
		Matcher jarMatcher = JAR_PATH_PATTERN.matcher(path.normalize().toString());
		if (!jarMatcher.matches()) {
			return Files.newInputStream(path.normalize());
		}
		JarFile jar = new JarFile(Path.of(jarMatcher.group(1)).toFile());
		Closer closer = Closer.create();
		try {
			closer.register(jar);
			JarEntry entry = jar.getJarEntry(jarMatcher.group(2).replace('\\', '/'));
			if (entry == null) {
				throw new SecurityException("Jar entry not found: " + jarMatcher.group(2).replace('\\', '/') + " in " + Path.of(jarMatcher.group(1)));
			}
			InputStream entryStream = closer.register(jar.getInputStream(entry));
			return new FilterInputStream(entryStream) {
				@Override
				public void close() throws IOException {
					closer.close();
				}
			};
		} catch (Exception e) {
			closer.close();
			throw e;
		}
	}

	/**
	 * Reads a file from the specified path and returns it as a {@link File}.
	 * <p>
	 * If the path contains a {@code .jar} segment followed by an entry path (for
	 * example {@code /opt/lib/app.jar!/config/app.yml} or
	 * {@code /opt/lib/app.jar/config/app.yml}), the entry is extracted to a
	 * temporary file which is deleted on JVM exit, then that temporary {@link File}
	 * is returned.
	 *
	 * @param sourceFilePath The path to the source file to read
	 * @return The {@link File} representing the source file or the extracted JAR
	 *         entry
	 * @throws SecurityException if an error occurs while reading the file
	 */
	public static File readFile(@Nonnull Path sourceFilePath) {
		try {
			String normalised = sourceFilePath.normalize().toString();
			Pattern jarSplit = Pattern.compile("(?i)^(.*?\\.jar)(?:!|[\\\\/])(.+)$");
			Matcher jarMatcher = jarSplit.matcher(normalised);
			if (!jarMatcher.matches()) {
				return sourceFilePath.toFile();
			}
			try (InputStream in = createInputStream(sourceFilePath)) {
				String entryPath = jarMatcher.group(2).replace('\\', '/');
				String entryFileName = Paths.get(entryPath).getFileName().toString();
				String safeBase = entryFileName.replaceAll("[^A-Za-z0-9._]", "_");
				String suffix = "";
				int dot = safeBase.lastIndexOf('.');
				if (dot >= 0) {
					suffix = safeBase.substring(dot);
					safeBase = safeBase.substring(0, dot);
				}
				if (safeBase.length() < 3) {
					safeBase = "ares";
				}
				Path tmp = Files.createTempFile(safeBase + "_", suffix);
				Files.copy(in, tmp, StandardCopyOption.REPLACE_EXISTING);
				tmp.toFile().deleteOnExit();
				return tmp.toFile();
			}
		} catch (OutOfMemoryError e) {
			throw new SecurityException("Ares Security Error (Stage: Creation): Out of memory while reading content.", e);
		} catch (IllegalFormatException e) {
			throw new SecurityException("Ares Security Error (Stage: Creation): An illegal format in content.", e);
		} catch (NullPointerException e) {
			throw new SecurityException("Ares Security Error (Stage: Creation): A null pointer encountered while reading file.", e);
		} catch (UnsupportedOperationException e) {
			throw new SecurityException("Ares Security Error (Stage: Creation): Unsupported operation while reading file.", e);
		} catch (IOException e) {
			throw new SecurityException("Ares Security Error (Stage: Creation): I/O error while reading file.", e);
		}
	}

	/**
	 * Reads a YAML file from the specified source file and deserializes it into an
	 * object of the specified type.
	 *
	 * @param sourceFile The YAML file to read
	 * @param valueType The class type to deserialize the YAML content into
	 * @param <T> The type of the object to be returned
	 * @return An object of type T deserialized from the YAML file
	 * @throws IOException if an I/O error occurs while reading the file
	 */
	@Nonnull
	public static <T> T readYamlFile(@Nonnull File sourceFile, @Nonnull Class<T> valueType) throws IOException {
		File protectedPath = Objects.requireNonNull(sourceFile, "sourceFile must not be null");
		Class<T> protectedValueType = Objects.requireNonNull(valueType, "valueType must not be null");
		try (InputStream in = createInputStream(protectedPath.toPath())) {
			T result = new ObjectMapper(new YAMLFactory()).readValue(in, protectedValueType);
			return Objects.requireNonNull(result, () -> "YAML file " + protectedPath + " deserialized to null " + "for type " + protectedValueType.getName());
		}
	}

	/**
	 * Reads a CSV file and returns its content as a list of string lists.
	 *
	 * @param sourceFile The CSV file to read
	 * @return A list of string lists representing the rows and columns of the CSV
	 *         file
	 * @throws IOException if an I/O error occurs while reading the file
	 * @throws CsvException if a parsing error occurs while processing the CSV
	 *             content
	 */
	public static List<List<String>> readCSVFile(@Nonnull File sourceFile) throws IOException, CsvException {
		CSVParser csvParserBuilder = new CSVParserBuilder().withSeparator(',').withQuoteChar('"').build();
		try (CSVReader csvReaderBuilder = new CSVReaderBuilder(new FileReader(sourceFile)).withCSVParser(csvParserBuilder).withSkipLines(1).build()) {
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
	public static Set<String> readMethodsFile(@Nonnull File sourceFile) {
		try {
			String fileContent = Files.readString(sourceFile.toPath(), StandardCharsets.UTF_8);
			String normalizedContent = fileContent.replace("\r\n", "\n").replace("\r", "\n");
			List<String> methods = Arrays.stream(normalizedContent.split("\n"))
					// Filter out comments
					.filter(str -> !str.startsWith("#")).toList();
			return new HashSet<>(methods);
		} catch (IOException e) {
			throw new SecurityException(localize("security.file-tools.read.content.failure"), e);
		} catch (OutOfMemoryError e) {
			throw new SecurityException("Ares Security Error (Stage: Creation): Out of memory while reading content.", e);
		}
	}

	/**
	 * Reads the content of a sourceFile and returns it as a list of strings.
	 *
	 * @param sourceFile The sourceFile to read
	 * @return a list of strings representing the content of the sourceFile
	 * @throws IOException if an I/O error occurs while reading the file
	 */
	public static List<String> readRuleFile(@Nonnull File sourceFile) throws IOException {
		return Files.readAllLines(sourceFile.toPath(), StandardCharsets.UTF_8);
	}
	// </editor-fold>

	// <editor-fold desc="Write">

	/**
	 * Writes the specified content to a file at the given target path.
	 * <p>
	 * This method attempts to write the provided content to the file located at
	 * {@code targetPath}. It accepts optional {@link java.nio.file.OpenOption}
	 * parameters to customize the write operation, such as
	 * {@link StandardOpenOption#CREATE} or
	 * {@link StandardOpenOption#TRUNCATE_EXISTING}.
	 * </p>
	 *
	 * @param targetPath the path to the target file where the content will be
	 *            written.
	 * @param content the content to write to the file.
	 * @param options optional {@link java.nio.file.OpenOption} parameters to
	 *            customize the write operation.
	 * @return the path to the file that was written.
	 * @throws SecurityException if an error occurs during the write operation,
	 *             including invalid arguments, I/O errors, null pointers, or
	 *             unsupported operations.
	 */
	private static File writeFile(Path targetPath, String content, java.nio.file.OpenOption... options) {
		try {
			return Files.writeString(targetPath, content, options).toFile();
		} catch (IllegalArgumentException e) {
			throw new SecurityException("Ares Security Error (Stage: Creation): Illegal argument provided for targetPath writing.", e);
		} catch (IOException e) {
			throw new SecurityException("Ares Security Error (Stage: Creation): IO error occurred during targetPath writing.", e);
		} catch (NullPointerException e) {
			throw new SecurityException("Ares Security Error (Stage: Creation): A null pointer encountered during targetPath writing.", e);
		} catch (UnsupportedOperationException e) {
			throw new SecurityException("Ares Security Error (Stage: Creation): Unsupported operation during targetPath writing.", e);
		}
	}
	// </editor-fold>

	// <editor-fold desc="Copy">

	/**
	 * Copies a list of files to a specified target directory.
	 * <p>
	 * This method iterates over the provided list of source file paths, copying
	 * each file to the specified target directory. It handles various exceptions
	 * that might occur during the copy process, wrapping them in a
	 * {@link SecurityException}.
	 * </p>
	 *
	 * @param sourceFilePaths a {@link List} of {@link Path} objects representing
	 *            the source files to copy.
	 * @param targetFilePaths a {@link List} of {@link Path} objects representing
	 *            the target paths for the copied files.
	 * @return a {@link List} of {@link Path} objects representing the paths of the
	 *         copied files.
	 * @throws SecurityException if an error occurs during the file copy process.
	 */
	public static List<Path> copyFiles(List<Path> sourceFilePaths, List<Path> targetFilePaths) {
		return IntStream.range(0, sourceFilePaths.size()).mapToObj(i -> {
			try {
				Path parentPath = targetFilePaths.get(i).getParent();
				if (parentPath != null && !Files.exists(parentPath)) {
					Files.createDirectories(parentPath);
				}
				Files.copy(FileTools.readFile(sourceFilePaths.get(i)).toPath(), targetFilePaths.get(i), StandardCopyOption.REPLACE_EXISTING);
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
	 * Copies Java files, formats the content, and writes it to the target
	 * directory.
	 * <p>
	 * This method copies files from the source to the target and formats the copied
	 * files' content based on the provided format values.
	 * </p>
	 *
	 * @param sourceFilePaths the source file paths.
	 * @param targetFilePaths the target file paths.
	 * @param formatValues the format values for formatting the files.
	 * @return a list of copied paths.
	 * @throws SecurityException if an error occurs during the process.
	 */
	public static List<Path> copyAndFormatFSFiles(List<Path> sourceFilePaths, List<Path> targetFilePaths, List<String[]> formatValues) {
		List<Path> copiedFiles = copyFiles(sourceFilePaths, targetFilePaths);

		// Skip formatting for localization resource files (*.properties)
		List<Path> filesToFormat = copiedFiles.stream().filter(p -> !p.toString().contains("localization")).filter(p -> !p.toString().endsWith(".properties")).toList();

		formatFSFiles(filesToFormat, formatValues);

		return copiedFiles;
	}

	/**
	 * Copies files from source to target and formats their content by replacing
	 * placeholders and applying format values.
	 * <p>
	 * This method first copies the files from the specified source paths to the
	 * target paths. After copying, it formats the content of each copied file by
	 * replacing specified placeholders with "%s" and then applying the provided
	 * format values using {@link String#format(String, Object...)}.
	 * </p>
	 *
	 * @param sourceFilePaths the list of source file paths to copy.
	 * @param targetFilePaths the list of target file paths where the files will be
	 *            copied.
	 * @param placeholderValues a list of string arrays, where each array contains
	 *            placeholders to be replaced in the corresponding file.
	 * @param formatValues a list of string arrays, where each array contains values
	 *            to be injected into the format specifiers in the corresponding
	 *            file.
	 * @return a list of paths representing the copied and formatted files.
	 * @throws SecurityException if an error occurs during file copying or
	 *             formatting.
	 */
	public static List<Path> copyAndFormatNonFSFiles(List<Path> sourceFilePaths, List<Path> targetFilePaths, List<String[]> placeholderValues, List<String[]> formatValues) {
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
			throw new SecurityException("Ares Security Error (Stage: Creation): Illegal format in " + pathForError.toAbsolutePath() + ".", e);
		}
	}

	/**
	 * Formats files by applying format values to their content.
	 * <p>
	 * This method reads the content of each file, applies the corresponding format
	 * values using {@link String#format(String, Object...)}, and writes the
	 * formatted content back to the file.
	 * </p>
	 *
	 * @param formatValues the format values for formatting the files.
	 * @throws SecurityException if an error occurs during the formatting process.
	 */
	private static void formatFSFiles(List<Path> targetFilePaths, List<String[]> formatValues) {
		IntStream.range(0, targetFilePaths.size()).forEach(i -> {
			Path target = targetFilePaths.get(i);
			try {
				String template = Files.readString(FileTools.readFile(target).toPath(), StandardCharsets.UTF_8);
				String formatted = formatTemplate(template, formatValues.get(i), target);
				writeFile(target, formatted, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
			} catch (IOException e) {
				throw new SecurityException(localized("file.tools.format.file.failed", target), e);
			}
		});
	}

	private static String processPlaceholdersAndFormat(String content, String[] placeholderValues, String[] formatValues, Path pathForError) {
		content = replacePlaceholdersWithFormatSpec(content, placeholderValues);
		String[] values = (formatValues != null) ? formatValues : new String[0];
		int valuesCount = values.length;
		int placeholderCount = countOccurrences(content, "%s");
		if (placeholderCount != valuesCount) {
			throw new SecurityException("Ares Security Error (Stage: Creation): Placeholder count mismatch in " + pathForError.toAbsolutePath() + ". Expected " + valuesCount
					+ " '%s' placeholders, but found " + placeholderCount + ".");
		}
		return formatTemplate(content, values, pathForError);
	}

	/**
	 * Replaces custom placeholders with "%s" and then formats files using the
	 * provided values.
	 * <p>
	 * For each target file, all occurrences of the corresponding placeholders (from
	 * {@code placeholderValues}) are replaced literally with "%s". After
	 * replacement, the method validates that the number of "%s" occurrences equals
	 * the number of format values for that file. If the counts mismatch, a
	 * {@link SecurityException} is thrown.
	 * </p>
	 *
	 * @param targetFilePaths the target files to process.
	 * @param placeholderValues per-file arrays of placeholder literals to be
	 *            replaced with "%s".
	 * @param formatValues per-file arrays of values to inject via
	 *            {@link String#format(String, Object...)}.
	 * @throws SecurityException if IO fails or placeholder count does not match
	 *             format values count.
	 */
	private static void formatNonFSFiles(List<Path> targetFilePaths, List<String[]> placeholderValues, List<String[]> formatValues) {
		IntStream.range(0, targetFilePaths.size()).forEach(i -> {
			Path targetPath = targetFilePaths.get(i);
			try {
				String content = Files.readString(FileTools.readFile(targetPath).toPath(), StandardCharsets.UTF_8);
				String[] perFilePlaceholders = (placeholderValues != null && placeholderValues.size() > i) ? placeholderValues.get(i) : null;
				String[] values = (formatValues != null && formatValues.size() > i && formatValues.get(i) != null) ? formatValues.get(i) : new String[0];
				String formatted = processPlaceholdersAndFormat(content, perFilePlaceholders, values, targetPath);
				writeFile(targetPath, formatted, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING);
			} catch (IOException e) {
				throw new SecurityException(localized("file.tools.process.format.file.failed", targetPath), e);
			}
		});
	}

	/**
	 * Generates an array of package name strings.
	 * <p>
	 * This method creates an array containing multiple copies of the provided
	 * package name. The length of the array is determined by the
	 * {@code numberOfEntries} parameter.
	 * </p>
	 *
	 * @param packageName the package name to be repeated in the array.
	 * @param numberOfEntries the number of times the package name should be
	 *            repeated in the array.
	 * @return a {@link String} array where each element is the provided package
	 *         name.
	 */
	public static String[] generatePackageNameArray(String packageName, int numberOfEntries) {
		return IntStream.range(0, numberOfEntries).mapToObj(i -> packageName).toArray(String[]::new);
	}
	// </editor-fold>

	// <editor-fold desc="Three Parted Java File">

	/**
	 * Creates a new file by combining the content of a header file, a body string,
	 * and a footer file.
	 * <p>
	 * This method reads the content from the specified header and footer files,
	 * combines it with the provided body string, and writes the combined content to
	 * a new file in the target directory with the specified file name.
	 * </p>
	 *
	 * @param sourceHeaderPath the {@link Path} of the header file.
	 * @param sourceBody the body content as a {@link String}.
	 * @param sourceFooterPath the {@link Path} of the footer file.
	 * @param target the target directory {@link Path} where the file will be
	 *            created.
	 * @return the {@link Path} of the created file.
	 * @throws SecurityException if an error occurs during the file creation or
	 *             writing process.
	 */
	public static Path createThreePartedFile(Path sourceHeaderPath, String sourceBody, Path sourceFooterPath, Path target) {
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
			return FileTools.writeFile(fullTargetPath, fileContent, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING).toPath();
		} catch (IOException e) {
			throw new SecurityException(localize("security.file-tools.create.target.directory.failed"), e);
		}
	}

	/**
	 * Creates a new file by concatenating a header file, an in-memory body string,
	 * and a footer file, then formats the resulting content using
	 * {@link String#format(String, Object...)}.
	 * <p>
	 * The method performs two steps:
	 * <ol>
	 * <li>Creates the file at {@code target} by joining the content of
	 * {@code sourceHeaderPath}, {@code sourceBody}, and {@code sourceFooterPath}
	 * (in that order).</li>
	 * <li>Reads the created file, applies {@link String#format(String, Object...)}
	 * with the provided {@code formatValues}, and writes the formatted content back
	 * to the same file.</li>
	 * </ol>
	 *
	 * @param sourceHeaderPath the path (within the classpath resources) of the
	 *            header file whose content will be placed at the beginning of the
	 *            created file.
	 * @param sourceBody the body text to be inserted between header and footer.
	 * @param sourceFooterPath the path (within the classpath resources) of the
	 *            footer file whose content will be placed at the end of the created
	 *            file.
	 * @param target the target path (on the filesystem) of the file to create and
	 *            format.
	 * @param formatValues the values to be injected into format specifiers (e.g.,
	 *            {@code %s}) contained in the concatenated content.
	 * @return the path to the created and formatted file.
	 * @throws SecurityException if an I/O error occurs while reading or writing the
	 *             file content.
	 * @throws IllegalFormatException if the format string contains an illegal
	 *             syntax for the provided values.
	 */
	public static Path createThreePartedFormatStringFile(Path sourceHeaderPath, String sourceBody, Path sourceFooterPath, Path target, String[] formatValues) {
		Path createdFile = null;
		try {
			createdFile = createThreePartedFile(sourceHeaderPath, sourceBody, sourceFooterPath, target);
			String template = Files.readString(FileTools.readFile(createdFile).toPath(), StandardCharsets.UTF_8);
			String formatted = formatTemplate(template, formatValues, createdFile);
			writeFile(createdFile, formatted, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING);
			return createdFile;
		} catch (IOException e) {
			throw new SecurityException(localized("file.tools.create.format.file.failed", createdFile), e);
		}
	}

	/**
	 * Reads a Phobos configuration file and returns its content as a list of lists
	 * of strings.
	 * <p>
	 * Each line in the configuration file is expected to contain exactly three
	 * tokens separated by whitespace.
	 * </p>
	 *
	 * @param sourceCFGPath the path to the configuration file.
	 * @return a list of lists, where each inner list contains three tokens from a
	 *         line in the configuration file.
	 * @throws IOException if an I/O error occurs while reading the file.
	 */
	public static List<List<String>> readCFGFile(Path sourceCFGPath) throws IOException {
		return Files.lines(sourceCFGPath).map(String::trim).filter(line -> !line.isEmpty()).map(line -> List.of(line.split("\\s+"))).peek(parts -> {
			if (parts.size() != 3) {
				throw new IllegalArgumentException("Invalid cfg format (expected exactly 3 tokens per line): " + parts);
			}
		}).collect(Collectors.toList());
	}
	// </editor-fold>
}