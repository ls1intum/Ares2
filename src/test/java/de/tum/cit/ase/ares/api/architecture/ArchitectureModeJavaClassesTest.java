package de.tum.cit.ase.ares.api.architecture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.ArrayList;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.FutureTask;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;

import de.tum.cit.ase.ares.api.localization.Messages;
import de.tum.cit.ase.ares.testutilities.JavacFixtures;

/**
 * Tests how {@link ArchitectureMode} imports the classes of a folder or jar,
 * when it reuses an earlier import, and how it reports an input it cannot read.
 */
class ArchitectureModeJavaClassesTest {

	/**
	 * A fresh folder for each test, so the import cache of one test never serves
	 * another.
	 */
	@TempDir
	Path temporaryDirectory;

	/**
	 * Checks that a class of the project is imported and a class under the Ares
	 * package is left out.
	 */
	@Test
	void leavesAresOwnClassesOutOfTheImport() throws Exception {
		Path classes = compile("classes", "p/Outside", "package p; public class Outside {}",
				"de/tum/cit/ase/ares/api/probe/Inside",
				"package de.tum.cit.ase.ares.api.probe; public class Inside {}");

		Set<String> names = names(ArchitectureMode.ARCHUNIT.getJavaClasses(classes.toString()));

		assertThat(names).contains("p.Outside").doesNotContain("de.tum.cit.ase.ares.api.probe.Inside");
	}

	/**
	 * Checks that replacing the class of a folder gives a new import with the new
	 * class and without the old one.
	 */
	@Test
	void reimportsAFolderWhoseClassesChanged() throws Exception {
		Path classes = compile("classes", "p/A", "package p; public class A {}");
		assertThat(names(ArchitectureMode.ARCHUNIT.getJavaClasses(classes.toString()))).contains("p.A");

		Files.delete(classes.resolve("p").resolve("A.class"));
		compile("classes", "p/B", "package p; public class B {}");
		Set<String> names = names(ArchitectureMode.ARCHUNIT.getJavaClasses(classes.toString()));

		assertThat(names).contains("p.B").doesNotContain("p.A");
	}

	/**
	 * Checks that a folder whose name ends in .class is not read as a class file.
	 */
	@Test
	void skipsAFolderNamedLikeAClass() throws Exception {
		Path classes = compile("classes", "p/A", "package p; public class A {}");
		Files.createDirectories(classes.resolve("trap.class"));

		Set<String> names = names(ArchitectureMode.ARCHUNIT.getJavaClasses(classes.toString()));

		assertThat(names).contains("p.A");
	}

	/**
	 * Checks that an import started for one state of a folder is thrown away when
	 * the folder changes during it, and that the import is repeated.
	 */
	@Test
	void retriesAnImportWhoseInputChanged() throws Exception {
		Path classes = compile("classes", "p/A", "package p; public class A {}");
		Path spare = compile("spare", "p/B", "package p; public class B {}");
		Path analysisPath = classes.toAbsolutePath().normalize();
		String key = analysisPath + "\0" + fingerprint(analysisPath);
		FutureTask<JavaClasses> injectedTask = new FutureTask<>(() -> {
			Files.copy(spare.resolve("p").resolve("B.class"), classes.resolve("p").resolve("B.class"));
			return new ClassFileImporter().importClasses();
		});
		Map<String, FutureTask<JavaClasses>> cache = importCache();
		cache.put(key, injectedTask);
		try {
			Set<String> names = names(ArchitectureMode.ARCHUNIT.getJavaClasses(classes.toString()));

			assertThat(names).contains("p.A", "p.B");
		} finally {
			cache.remove(key, injectedTask);
		}
	}

	/**
	 * Checks that a file that is neither a class nor a jar does not cause a new
	 * import.
	 */
	@Test
	void keepsTheImportWhenOnlyAnotherFileChanged() throws Exception {
		Path classes = compile("classes", "p/A", "package p; public class A {}");
		JavaClasses first = ArchitectureMode.ARCHUNIT.getJavaClasses(classes.toString());

		Files.writeString(classes.resolve("readme.txt"), "not a class");

		assertThat(ArchitectureMode.ARCHUNIT.getJavaClasses(classes.toString())).isSameAs(first);
	}

	/**
	 * Checks that moving a class file into a sub-folder, with the same bytes,
	 * causes a new import.
	 */
	@Test
	void reimportsWhenAClassFileMoves() throws Exception {
		Path classes = compile("classes", "p/A", "package p; public class A {}");
		JavaClasses first = ArchitectureMode.ARCHUNIT.getJavaClasses(classes.toString());

		Path moved = classes.resolve("p").resolve("x");
		Files.createDirectories(moved);
		Files.move(classes.resolve("p").resolve("A.class"), moved.resolve("A.class"));

		assertThat(ArchitectureMode.ARCHUNIT.getJavaClasses(classes.toString())).isNotSameAs(first);
	}

	/**
	 * Checks that a jar whose content changes gives a new import, not the cached
	 * one.
	 */
	@Test
	void reimportsAJarWhoseContentChanged() throws Exception {
		Path classes = compile("classes", "p/A", "package p; public class A {}", "p/B", "package p; public class B {}");
		Path jar = temporaryDirectory.resolve("input.jar");
		writeJar(jar, classes.resolve("p").resolve("A.class"), "p/A.class");
		JavaClasses first = ArchitectureMode.ARCHUNIT.getJavaClasses(jar.toString());

		writeJar(jar, classes.resolve("p").resolve("B.class"), "p/B.class");
		JavaClasses second = ArchitectureMode.ARCHUNIT.getJavaClasses(jar.toString());

		assertThat(second).isNotSameAs(first);
	}

	/**
	 * Checks that the file name and the content of a class file cannot be confused
	 * with each other, nor one file with the next.
	 */
	@Test
	void separatesPathAndContentInTheFingerprint() throws Exception {
		assertThat(fingerprintOf("one", "a.class", "b.classZ"))
				.isNotEqualTo(fingerprintOf("two", "a.classb.class", "Z"));
		Path first = writeFiles("three", "a.class", "P", "b.class", "Q");
		Path second = writeFiles("four", "a.class", "Pb.class\0Q");
		assertThat(fingerprint(first)).isNotEqualTo(fingerprint(second));
	}

	/**
	 * Checks that a class file that cannot be read is reported as an unreadable
	 * input and that the file's permissions are restored.
	 */
	@Test
	void reportsAnUnreadableInput() throws Exception {
		assumeTrue(FileSystems.getDefault().supportedFileAttributeViews().contains("posix"));
		Path classes = compile("classes", "p/A", "package p; public class A {}");
		Path classFile = classes.resolve("p").resolve("A.class");
		Files.setPosixFilePermissions(classFile, PosixFilePermissions.fromString("---------"));
		try {
			assumeTrue(!Files.isReadable(classFile));

			assertThatThrownBy(() -> ArchitectureMode.ARCHUNIT.getJavaClasses(classes.toString()))
					.isInstanceOf(SecurityException.class)
					.hasMessage(Messages.localized("security.architecture.mode.analysis.input.failure",
							classes.toAbsolutePath().normalize()))
					.hasCauseInstanceOf(IOException.class);
		} finally {
			Files.setPosixFilePermissions(classFile, PosixFilePermissions.fromString("rw-------"));
		}
	}

	/**
	 * Checks that a runtime exception and an error of a failed import are thrown as
	 * they are.
	 */
	@Test
	void rethrowsARuntimeExceptionAndAnErrorOfTheImport() throws Exception {
		IllegalStateException runtimeFailure = new IllegalStateException("runtime");
		LinkageError linkageFailure = new LinkageError("linkage");

		assertThat(awaitFailure(failedTask(runtimeFailure))).isSameAs(runtimeFailure);
		assertThat(awaitFailure(failedTask(linkageFailure))).isSameAs(linkageFailure);
	}

	/**
	 * Checks that a checked exception of a failed import is wrapped into a security
	 * error with the exception as cause.
	 */
	@Test
	void wrapsACheckedFailureOfTheImport() throws Exception {
		IOException checkedFailure = new IOException("unreadable");

		Throwable thrown = awaitFailure(failedTask(checkedFailure));

		assertThat(thrown).isInstanceOf(SecurityException.class)
				.hasMessage(Messages.localized("security.architecture.mode.analysis.input.failure", temporaryDirectory))
				.hasCause(checkedFailure);
	}

	/**
	 * Checks that waiting on an import in an interrupted thread ends with a
	 * security error and leaves the thread interrupted.
	 */
	@Test
	void keepsTheInterruptWhenWaitingForTheImport() throws Exception {
		FutureTask<JavaClasses> neverRun = new FutureTask<>(() -> new ClassFileImporter().importClasses());
		Throwable thrown;
		boolean stillInterrupted;
		Thread.currentThread().interrupt();
		try {
			thrown = awaitFailure(neverRun);
			stillInterrupted = Thread.currentThread().isInterrupted();
		} finally {
			Thread.interrupted();
		}

		assertThat(thrown).isInstanceOf(SecurityException.class)
				.hasMessage(Messages.localized("security.architecture.mode.analysis.input.failure", temporaryDirectory))
				.hasCauseInstanceOf(InterruptedException.class);
		assertThat(stillInterrupted).isTrue();
	}

	/**
	 * Writes the given sources, as pairs of class path and source text, and
	 * compiles them into the named folder.
	 */
	private Path compile(String folder, String... pathsAndSources) throws IOException {
		Path sources = temporaryDirectory.resolve("sources-" + folder);
		Path output = temporaryDirectory.resolve(folder);
		Files.createDirectories(output);
		ArrayList<Path> files = new ArrayList<>();
		for (int index = 0; index < pathsAndSources.length; index += 2) {
			Path file = sources.resolve(pathsAndSources[index] + ".java");
			Files.createDirectories(file.getParent());
			Files.writeString(file, pathsAndSources[index + 1]);
			files.add(file);
		}
		return JavacFixtures.compileSources(JavacFixtures.testClasses().toString(), output, files);
	}

	/**
	 * Writes a jar that holds the given file under the given entry name.
	 */
	private static void writeJar(Path jar, Path file, String entryName) throws IOException {
		try (OutputStream out = Files.newOutputStream(jar); JarOutputStream jarOut = new JarOutputStream(out)) {
			jarOut.putNextEntry(new JarEntry(entryName));
			jarOut.write(Files.readAllBytes(file));
			jarOut.closeEntry();
		}
	}

	/**
	 * Writes the given files, as pairs of name and text, into a new folder.
	 */
	private Path writeFiles(String folder, String... namesAndTexts) throws IOException {
		Path directory = temporaryDirectory.resolve(folder);
		Files.createDirectories(directory);
		for (int index = 0; index < namesAndTexts.length; index += 2) {
			Files.write(directory.resolve(namesAndTexts[index]),
					namesAndTexts[index + 1].getBytes(StandardCharsets.UTF_8));
		}
		return directory;
	}

	/**
	 * Returns the fingerprint of a new folder holding one file.
	 */
	private String fingerprintOf(String folder, String name, String text) throws Exception {
		return fingerprint(writeFiles(folder, name, text));
	}

	/**
	 * Returns the names of the classes of an import.
	 */
	private static Set<String> names(JavaClasses classes) {
		return classes.stream().map(JavaClass::getName).collect(Collectors.toSet());
	}

	/**
	 * Calls the private fingerprint method of the architecture mode.
	 */
	private static String fingerprint(Path folder) throws Exception {
		Method method = ArchitectureMode.class.getDeclaredMethod("analysisInputFingerprint", Path.class);
		method.setAccessible(true);
		return (String) method.invoke(null, folder.toAbsolutePath().normalize());
	}

	/**
	 * Returns the private import cache of the architecture mode.
	 */
	@SuppressWarnings("unchecked")
	private static Map<String, FutureTask<JavaClasses>> importCache() throws Exception {
		Field field = ArchitectureMode.class.getDeclaredField("JAVA_CLASSES_CACHE");
		field.setAccessible(true);
		return (Map<String, FutureTask<JavaClasses>>) field.get(null);
	}

	/**
	 * Returns a task that has run and ended with the given failure.
	 */
	private static FutureTask<JavaClasses> failedTask(Throwable failure) {
		FutureTask<JavaClasses> task = new FutureTask<>(() -> {
			throw sneaky(failure);
		});
		task.run();
		return task;
	}

	/**
	 * Throws any throwable without declaring it.
	 */
	@SuppressWarnings("unchecked")
	private static <T extends Throwable> T sneaky(Throwable failure) throws T {
		throw (T) failure;
	}

	/**
	 * Calls the private await method with the temporary folder as path and returns
	 * what it throws.
	 */
	private Throwable awaitFailure(FutureTask<JavaClasses> task) throws Exception {
		Method method = ArchitectureMode.class.getDeclaredMethod("awaitJavaClasses", FutureTask.class, Path.class);
		method.setAccessible(true);
		try {
			method.invoke(null, task, temporaryDirectory);
		} catch (InvocationTargetException thrown) {
			return thrown.getCause();
		}
		throw new AssertionError("the await method returned normally");
	}
}
