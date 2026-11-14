package de.tum.cit.ase.ares.api.internal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import de.tum.cit.ase.ares.api.io.AresIOContext;
import de.tum.cit.ase.ares.api.io.IOManager;
import de.tum.cit.ase.ares.api.policy.policySubComponents.ProgrammingLanguageConfiguration;
import de.tum.cit.ase.ares.api.policy.policySubComponents.ResourceAccesses;
import de.tum.cit.ase.ares.api.policy.policySubComponents.SupervisedCode;

/**
 * Checks the custom I/O manager contract: only the instructor's own test
 * classes, loaded from elsewhere than the student code, qualify.
 */
class IOManagerReferenceTest {

	/** This class's canonical name, listed as a test class. */
	private static final String THIS = IOManagerReferenceTest.class.getCanonicalName();

	/**
	 * A package compiled into the main output, standing in for student code, with a
	 * class there as its main class.
	 */
	private static final String STUDENT_PACKAGE = "de.tum.cit.ase.ares.api.policy.policySubComponents";

	/** The default locale before a test changed it. */
	private final Locale previous = Locale.getDefault();

	/** Restores the default locale. */
	@AfterEach
	void restoreLocale() {
		Locale.setDefault(previous);
	}

	/** A listed, concrete manager from the test output is accepted. */
	@Test
	void aListedManagerFromTheTestOutputIsAccepted() {
		assertThat(IOManagerReference.resolve(THIS + ".RecordingManager",
				policy(List.of(THIS), STUDENT_PACKAGE, "PolicyDuration"), loader())).isEqualTo(RecordingManager.class);
	}

	/** A manager neither listed nor nested in a listed class is refused. */
	@Test
	void anUnlistedManagerIsRefused() {
		Locale.setDefault(Locale.ENGLISH);
		assertThatThrownBy(() -> IOManagerReference.resolve(THIS + ".RecordingManager",
				policy(List.of(), STUDENT_PACKAGE, "PolicyDuration"), loader())).isInstanceOf(SecurityException.class)
						.hasMessageContaining("not listed in theFollowingClassesAreTestClasses");
	}

	/** A class that cannot be loaded is refused. */
	@Test
	void aMissingManagerIsRefused() {
		assertThatThrownBy(() -> IOManagerReference.resolve("org.example.Missing",
				policy(List.of("org.example.Missing"), STUDENT_PACKAGE, "PolicyDuration"), loader()))
						.isInstanceOf(SecurityException.class);
	}

	/** A manager loaded from where the student code is loaded from is refused. */
	@Test
	void aManagerFromTheStudentOutputIsRefused() {
		Locale.setDefault(Locale.ENGLISH);
		assertThatThrownBy(() -> IOManagerReference.resolve(THIS + ".RecordingManager",
				policy(List.of(THIS), "de.tum.cit.ase.ares.api.internal", "IOManagerReferenceTest"), loader()))
						.isInstanceOf(SecurityException.class).hasMessageContaining("where the student code is");
	}

	/** Without any student class to compare with, the manager is refused. */
	@Test
	void withoutAReferenceTheManagerIsRefused() {
		Locale.setDefault(Locale.ENGLISH);
		assertThatThrownBy(() -> IOManagerReference.resolve(THIS + ".RecordingManager",
				policy(List.of(THIS), "org.nothing.here", null), loader())).isInstanceOf(SecurityException.class)
						.hasMessageContaining("cannot be checked");
	}

	/**
	 * Without a main class, the classes of the supervised package are where the
	 * student code is, so a manager from the test output is accepted.
	 *
	 * @param folder a folder for the compiled student class.
	 * @throws IOException if the class cannot be written
	 */
	@Test
	void aPackageScanWithoutAMainClassAcceptsATestOutputManager(@TempDir Path folder) throws IOException {
		try (URLClassLoader student = compiled(folder, "studentpkg/Penguin",
				"package studentpkg; public class Penguin { }")) {
			assertThat(IOManagerReference.resolve(THIS + ".RecordingManager", policy(List.of(THIS), "studentpkg", null),
					student)).isEqualTo(RecordingManager.class);
		}
	}

	/**
	 * Without a main class, an unlisted class of the supervised package counts as
	 * student code even in the test output, so a manager there is refused.
	 */
	@Test
	void aPackageScanCountsUnlistedTestOutputClassesAsStudentCode() {
		Locale.setDefault(Locale.ENGLISH);
		assertThatThrownBy(() -> IOManagerReference.resolve(THIS + ".RecordingManager",
				policy(List.of(THIS), "de.tum.cit.ase.ares.testutilities", null), loader()))
						.isInstanceOf(SecurityException.class).hasMessageContaining("where the student code is");
	}

	/**
	 * Without a main class, student classes in subpackages count, so a package
	 * whose classes all sit in subpackages still gives a reference.
	 *
	 * @param folder a folder for the compiled student class.
	 * @throws IOException if the class cannot be written
	 */
	@Test
	void aPackageScanIncludesSubpackages(@TempDir Path folder) throws IOException {
		try (URLClassLoader student = compiled(folder, "subpkg/solution/Penguin",
				"package subpkg.solution; public class Penguin { }")) {
			assertThat(IOManagerReference.resolve(THIS + ".RecordingManager", policy(List.of(THIS), "subpkg", null),
					student)).isEqualTo(RecordingManager.class);
		}
	}

	/**
	 * A top-level student class with a dollar sign in its name is found, so it
	 * gives a reference rather than being taken for a nested class.
	 *
	 * @param folder a folder for the compiled student class.
	 * @throws IOException if the class cannot be written
	 */
	@Test
	void aTopLevelClassWithADollarSignIsFound(@TempDir Path folder) throws IOException {
		try (URLClassLoader student = compiled(folder, "dollarpkg/Payload$Hidden",
				"package dollarpkg; public class Payload$Hidden { }")) {
			assertThat(IOManagerReference.resolve(THIS + ".RecordingManager", policy(List.of(THIS), "dollarpkg", null),
					student)).isEqualTo(RecordingManager.class);
		}
	}

	/**
	 * A student class that cannot be loaded refuses the manager, since its origin
	 * is unknown.
	 *
	 * @param folder a folder for the unreadable class file.
	 * @throws IOException if the file cannot be written
	 */
	@Test
	void anUnloadableStudentClassRefusesTheManager(@TempDir Path folder) throws IOException {
		Locale.setDefault(Locale.ENGLISH);
		Files.write(Files.createDirectories(folder.resolve("brokenpkg")).resolve("Broken.class"), new byte[] { 1, 2 });
		try (URLClassLoader student = new URLClassLoader(new URL[] { folder.toUri().toURL() }, loader())) {
			assertThatThrownBy(() -> IOManagerReference.resolve(THIS + ".RecordingManager",
					policy(List.of(THIS), "brokenpkg", null), student)).isInstanceOf(SecurityException.class)
							.hasMessageContaining("brokenpkg.Broken").hasMessageContaining("cannot be determined");
		}
	}

	/**
	 * A supervised package served from somewhere other than a folder, here a jar,
	 * refuses the manager in both languages.
	 *
	 * @param folder a folder for the jar.
	 * @throws IOException if the jar cannot be written
	 */
	@Test
	void aPackageOutsideAFolderRefusesTheManager(@TempDir Path folder) throws IOException {
		Path jar = folder.resolve("student.jar");
		try (OutputStream out = Files.newOutputStream(jar); JarOutputStream entries = new JarOutputStream(out)) {
			entries.putNextEntry(new JarEntry("jarpkg/"));
			entries.closeEntry();
		}
		try (URLClassLoader student = new URLClassLoader(new URL[] { jar.toUri().toURL() }, loader())) {
			for (Locale locale : List.of(Locale.ENGLISH, Locale.GERMAN)) {
				Locale.setDefault(locale);
				assertThatThrownBy(() -> IOManagerReference.resolve(THIS + ".RecordingManager",
						policy(List.of(THIS), "jarpkg", null), student)).isInstanceOf(SecurityException.class)
								.hasMessageContaining(
										locale == Locale.GERMAN ? "nicht bestimmen" : "cannot be determined");
			}
		}
	}

	/** A package scan that finds no class at all refuses, in German too. */
	@Test
	void anEmptyPackageScanIsRefusedInGerman() {
		Locale.setDefault(Locale.GERMAN);
		assertThatThrownBy(() -> IOManagerReference.resolve(THIS + ".RecordingManager",
				policy(List.of(THIS), "org.nothing.here", null), loader())).isInstanceOf(SecurityException.class)
						.hasMessageContaining("Ares Sicherheitsfehler");
	}

	/** A listed class that is not a usable manager is refused. */
	@Test
	void aClassOfTheWrongShapeIsRefused() {
		Locale.setDefault(Locale.ENGLISH);
		for (String shape : List.of(THIS + ".NotAManager", THIS + ".AbstractManager",
				THIS + ".ManagerWithoutDefaultConstructor")) {
			assertThatThrownBy(() -> IOManagerReference.resolve(shape,
					policy(List.of(THIS), STUDENT_PACKAGE, "PolicyDuration"), loader()))
							.isInstanceOf(SecurityException.class).hasMessageContaining("must be a concrete class");
		}
	}

	/** The refusal is localised in German. */
	@Test
	void theRefusalIsLocalisedInGerman() {
		Locale.setDefault(Locale.GERMAN);
		assertThatThrownBy(() -> IOManagerReference.resolve(THIS + ".RecordingManager",
				policy(List.of(), STUDENT_PACKAGE, "PolicyDuration"), loader()))
						.hasMessageContaining("Ares Sicherheitsfehler");
	}

	/**
	 * A policy part with the given test classes and student package.
	 *
	 * @param testClasses the listed test classes.
	 * @param packageName the supervised package.
	 * @param mainClass   the main class, or null.
	 * @return the policy part.
	 */
	private static SupervisedCode policy(List<String> testClasses, String packageName, String mainClass) {
		ResourceAccesses none = ResourceAccesses.builder().regardingFileSystemInteractions(List.of())
				.regardingNetworkConnections(List.of()).regardingCommandExecutions(List.of())
				.regardingThreadCreations(List.of()).regardingPackageImports(List.of()).build();
		return new SupervisedCode(ProgrammingLanguageConfiguration.JAVA_USING_MAVEN_ARCHUNIT_AND_ASPECTJ, packageName,
				mainClass, testClasses, none, null);
	}

	/**
	 * Compiles one student class into a folder and returns a loader serving it on
	 * top of the test classes.
	 *
	 * @param folder the folder.
	 * @param path   the class's path without extension, such as {@code pkg/Name}.
	 * @param code   the class's source.
	 * @return the loader.
	 * @throws IOException if the class cannot be written
	 */
	private static URLClassLoader compiled(Path folder, String path, String code) throws IOException {
		Path source = folder.resolve("src").resolve(path + ".java");
		Files.createDirectories(source.getParent());
		Files.writeString(source, code);
		Path classes = Files.createDirectories(folder.resolve("classes"));
		JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
		assertThat(compiler.run(null, null, null, "-d", classes.toString(), source.toString())).isZero();
		return new URLClassLoader(new URL[] { classes.toUri().toURL() }, loader());
	}

	/** The loader of the test classes. */
	private static ClassLoader loader() {
		return IOManagerReferenceTest.class.getClassLoader();
	}

	/** A usable custom manager. */
	public static final class RecordingManager implements IOManager<Void> {

		/** Does nothing before a test. */
		@Override
		public void beforeTestExecution(AresIOContext context) {
			// Nothing to prepare.
		}

		/** Does nothing after a test. */
		@Override
		public void afterTestExecution(AresIOContext context) {
			// Nothing to clean up.
		}

		/** Provides no controller. */
		@Override
		public Void getControllerInstance(AresIOContext context) {
			return null;
		}

		/** Provides no controller type. */
		@Override
		public Class<Void> getControllerClass() {
			return null;
		}
	}

	/** Not a manager at all. */
	public static final class NotAManager {
	}

	/** A manager that cannot be created. */
	public abstract static class AbstractManager implements IOManager<Void> {
	}

	/** A manager without a constructor without parameters. */
	public static final class ManagerWithoutDefaultConstructor implements IOManager<Void> {

		/**
		 * Needs an argument.
		 *
		 * @param ignored any value.
		 */
		ManagerWithoutDefaultConstructor(String ignored) {
		}

		/** Does nothing before a test. */
		@Override
		public void beforeTestExecution(AresIOContext context) {
			// Nothing to prepare.
		}

		/** Does nothing after a test. */
		@Override
		public void afterTestExecution(AresIOContext context) {
			// Nothing to clean up.
		}

		/** Provides no controller. */
		@Override
		public Void getControllerInstance(AresIOContext context) {
			return null;
		}

		/** Provides no controller type. */
		@Override
		public Class<Void> getControllerClass() {
			return null;
		}
	}
}
