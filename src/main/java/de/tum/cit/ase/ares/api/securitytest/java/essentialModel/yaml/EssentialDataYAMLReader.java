package de.tum.cit.ase.ares.api.securitytest.java.essentialModel.yaml;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Map;
import java.util.Objects;

import javax.annotation.Nonnull;

import com.fasterxml.jackson.core.exc.StreamReadException;
import com.fasterxml.jackson.databind.DatabindException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import de.tum.cit.ase.ares.api.securitytest.java.essentialModel.EssentialClasses;
import de.tum.cit.ase.ares.api.securitytest.java.essentialModel.EssentialDataReader;
import de.tum.cit.ase.ares.api.securitytest.java.essentialModel.EssentialPackages;
import de.tum.cit.ase.ares.api.util.FileTools;
import de.tum.cit.ase.ares.api.util.LruCache;
import de.tum.cit.ase.ares.api.util.YamlPlaceholderResolver;

/**
 * Reads essential configurations from YAML files.
 * <p>
 * Description: This class implements the EssentialReader interface to provide
 * functionality for reading essential classes and packages from YAML files. It
 * utilises Jackson's YAMLFactory and ObjectMapper for parsing YAML content.
 * </p>
 * <p>
 * Design Rationale: By encapsulating YAML parsing logic in this class, it
 * allows for clear separation of concerns and easy modification of the parsing
 * mechanism.
 * </p>
 *
 * @since 2.0.0
 * @author Markus Paulsen
 * @version 2.0.0
 */
public class EssentialDataYAMLReader implements EssentialDataReader {

	/**
	 * The largest number of read essential lists kept. Ares reads two files, its
	 * essential packages and classes, so 16 is ample; a dropped list is only read
	 * again.
	 */
	private static final int MAX_READ_ESSENTIALS = 16;

	/**
	 * Essential lists already read in this JVM, keyed by the target type, the
	 * folder placeholders resolve against, the file's bytes and the system
	 * properties a placeholder expands to. Callers always receive a fresh copy.
	 */
	private static final Map<EssentialFileContent, Object> READ_ESSENTIALS = LruCache
			.synchronizedCache(MAX_READ_ESSENTIALS);

	/**
	 * Everything reading one essential file depends on.
	 *
	 * @param valueType      the record the file is bound to
	 * @param folder         the folder of the file read, the placeholder root
	 * @param bytes          the file's content
	 * @param propertyValues the system property values placeholders expand to
	 */
	private record EssentialFileContent(Class<?> valueType, Path folder, ByteBuffer bytes,
			Map<String, String> propertyValues) {

		/**
		 * Captures the input for one essential file.
		 *
		 * @param valueType the record the file is bound to
		 * @param file      the file read
		 * @return the input
		 * @throws IOException if the file cannot be read
		 */
		static EssentialFileContent of(Class<?> valueType, File file) throws IOException {
			Path absolute = file.toPath().toAbsolutePath().normalize();
			return new EssentialFileContent(valueType, absolute.getParent(),
					ByteBuffer.wrap(Files.readAllBytes(absolute)), YamlPlaceholderResolver.currentPropertyValues());
		}
	}

	/**
	 * Binds an essential file, reusing an earlier binding of identical input. The
	 * file is read once and bound from exactly those bytes and the property values
	 * of its key, so a binding is never remembered under input it was not made
	 * from.
	 *
	 * @param file      the file to bind
	 * @param valueType the record to bind it to
	 * @param <T>       the record type
	 * @return the bound record, shared with later reads of identical input
	 * @throws IOException if the file cannot be read or bound
	 */
	@Nonnull
	private static <T> T readEssentialFile(@Nonnull File file, @Nonnull Class<T> valueType) throws IOException {
		EssentialFileContent content = EssentialFileContent.of(valueType, file);
		Object known = READ_ESSENTIALS.get(content);
		if (known != null) {
			return valueType.cast(known);
		}
		T read = FileTools.readYamlFile(content.bytes().array(), valueType, new ObjectMapper(new YAMLFactory()),
				content.folder(), content.propertyValues());
		READ_ESSENTIALS.putIfAbsent(content, read);
		return read;
	}

	/**
	 * Copies essential classes into fresh lists, so that no caller can change the
	 * kept ones.
	 *
	 * @param classes the classes to copy
	 * @return the copy
	 */
	@Nonnull
	private static EssentialClasses copyOf(@Nonnull EssentialClasses classes) {
		return new EssentialClasses(new ArrayList<>(classes.essentialJavaClasses()),
				new ArrayList<>(classes.essentialArchunitClasses()), new ArrayList<>(classes.essentialWalaClasses()),
				new ArrayList<>(classes.essentialAspectJClasses()),
				new ArrayList<>(classes.essentialInstrumentationClasses()),
				new ArrayList<>(classes.essentialAresClasses()), new ArrayList<>(classes.essentialJUnitClasses()));
	}

	/**
	 * Copies essential packages into fresh lists, so that no caller can change the
	 * kept ones.
	 *
	 * @param packages the packages to copy
	 * @return the copy
	 */
	@Nonnull
	private static EssentialPackages copyOf(@Nonnull EssentialPackages packages) {
		return new EssentialPackages(new ArrayList<>(packages.essentialJavaPackages()),
				new ArrayList<>(packages.essentialArchunitPackages()),
				new ArrayList<>(packages.essentialWalaPackages()), new ArrayList<>(packages.essentialAspectJPackages()),
				new ArrayList<>(packages.essentialInstrumentationPackages()),
				new ArrayList<>(packages.essentialAresPackages()), new ArrayList<>(packages.essentialJUnitPackages()));
	}

	/**
	 * Reads essential classes from the specified YAML file.
	 *
	 * @since 2.0.0
	 * @author Markus Paulsen
	 * @param essentialClassesPath the path to the YAML file containing essential
	 *                             classes
	 * @return an EssentialClasses instance parsed from the YAML file
	 */
	@Override
	@Nonnull
	public EssentialClasses readEssentialClassesFrom(@Nonnull Path essentialClassesPath) {
		@Nonnull
		Path path = Objects.requireNonNull(essentialClassesPath, "The essential classes path must not be null!");
		@Nonnull
		Class<EssentialClasses> yamlClass = Objects.requireNonNull(EssentialClasses.class,
				"The essential classes class must not be null!");
		try {
			return copyOf(readEssentialFile(FileTools.readFile(path), yamlClass));
		} catch (StreamReadException e) {
			throw readerError("essential.classes.read.failed", path.toString(), e);
		} catch (DatabindException e) {
			throw readerError("essential.classes.data.bind.failed", path.toString(), e);
		} catch (UnsupportedOperationException e) {
			throw readerError("essential.classes.unsupported.operation", path.toString(), e);
		} catch (IOException e) {
			throw readerError("security.policy.io.exception", path.toString(), e);
		}
	}

	/**
	 * Reads essential packages from the specified YAML file.
	 *
	 * @since 2.0.0
	 * @author Markus Paulsen
	 * @param essentialPackagesPath the path to the YAML file containing essential
	 *                              packages
	 * @return an EssentialPackages instance parsed from the YAML file
	 */
	@Override
	@Nonnull
	public EssentialPackages readEssentialPackagesFrom(@Nonnull Path essentialPackagesPath) {
		@Nonnull
		Path path = Objects.requireNonNull(essentialPackagesPath, "The essential packages path must not be null!");
		@Nonnull
		Class<EssentialPackages> yamlClass = Objects.requireNonNull(EssentialPackages.class,
				"The essential packages class must not be null!");
		try {
			return copyOf(readEssentialFile(FileTools.readFile(path), yamlClass));
		} catch (StreamReadException e) {
			throw readerError("essential.packages.read.failed", path.toString(), e);
		} catch (DatabindException e) {
			throw readerError("essential.packages.data.bind.failed", path.toString(), e);
		} catch (UnsupportedOperationException e) {
			throw readerError("essential.packages.unsupported.operation", path.toString(), e);
		} catch (IOException e) {
			throw readerError("security.policy.io.exception", path.toString(), e);
		}
	}
}
