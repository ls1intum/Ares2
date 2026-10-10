package de.tum.cit.ase.ares.api.aop.java.aspectj.adviceandpointcut;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.api.parallel.Isolated;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import de.tum.cit.ase.ares.api.localization.Messages;

/**
 * Checks that the AspectJ file checks take their trusted directories from the
 * values the JVM started with, without any agent, so a later property change
 * moves nothing. The forked runs in {@code JceGeneratedExerciseTest} cover a
 * JVM that cannot read them.
 */
@Isolated
class JavaAspectJStartUpValuesTest {

	/**
	 * A {@code java.home} changed after start-up changes neither the start-up value
	 * nor the trusted Java installation.
	 */
	@Test
	void aJavaHomeChangeAfterStartUpIsIgnored(@TempDir Path forbidden) throws Exception {
		String original = System.getProperty("java.home");
		try {
			System.setProperty("java.home", forbidden.toString());
			assertEquals(original, invoke("startUpProperty", new Class<?>[] { String.class }, "java.home"));
			assertEquals(original, staticValue("TRUSTED_JAVA_HOME"));
		} finally {
			System.setProperty("java.home", original);
		}
	}

	/**
	 * The trusted default temp directory is the start-up {@code java.io.tmpdir}
	 * with links followed, and the start-up values are readable in this JVM.
	 */
	@Test
	void theTrustedTempDirectoryIsTheRealStartUpDirectory() throws Exception {
		invoke("requireStartUpProperties", new Class<?>[0]);
		String startUp = (String) invoke("startUpProperty", new Class<?>[] { String.class }, "java.io.tmpdir");
		assertEquals(Path.of(startUp).toRealPath(), staticValue("TRUSTED_DEFAULT_TEMP_DIRECTORY"));
	}

	/**
	 * The directory read from the JDK is the one {@code File.createTempFile} really
	 * writes to when given no directory.
	 */
	@Test
	void theJdkTempDirectoryIsWhereFileWritesTempFiles() throws Exception {
		Path created = example.jce.JceTempFileSubject.createWithFile(null).toPath();
		try {
			File read = (File) invoke("readJdkFileTempDirectory", new Class<?>[0]);
			assertEquals(created.getParent().toRealPath(), read.toPath().toRealPath());
		} finally {
			Files.delete(created);
		}
	}

	/**
	 * Only a class with exactly one static {@code File} field yields a field; none
	 * or two yield nothing, so the read fails closed.
	 */
	@Test
	void onlyASingleStaticFileFieldIsAccepted() throws Exception {
		Class<?>[] parameter = { Class.class };
		Field single = (Field) invoke("findSingleStaticFileField", parameter, OneStaticFile.class);
		assertEquals("directory", single.getName());
		assertNull(invoke("findSingleStaticFileField", parameter, TwoStaticFiles.class));
		assertNull(invoke("findSingleStaticFileField", parameter, NoStaticFile.class));
	}

	/**
	 * Both configuration messages exist in English and German, each in its own
	 * wording rather than a fallback, and name the JVM argument that fixes them.
	 *
	 * @param language the language the messages are shown in
	 * @param wording  a phrase only that language's messages contain
	 */
	@ParameterizedTest
	@CsvSource({ "en,Pass the JVM argument", "de,Übergeben Sie der Test-JVM das Argument" })
	void theConfigurationMessagesNameTheMissingArgument(String language, String wording) {
		Locale before = Locale.getDefault(Locale.Category.DISPLAY);
		try {
			Locale.setDefault(Locale.Category.DISPLAY, Locale.forLanguageTag(language));
			Messages.init();
			String unavailable = Messages.localized("security.advice.startup.properties.unavailable");
			String unreadable = Messages.localized("security.advice.temp.directory.holder.unreadable");
			assertTrue(unavailable.contains(wording) && unreadable.contains(wording), unavailable + unreadable);
			assertTrue(unavailable.contains("--add-exports java.base/jdk.internal.misc=ALL-UNNAMED"), unavailable);
			assertTrue(unreadable.contains("--add-opens java.base/java.io=ALL-UNNAMED"), unreadable);
		} finally {
			Locale.setDefault(Locale.Category.DISPLAY, before);
			Messages.init();
		}
	}

	/** Calls a private static method of the aspect. */
	private static Object invoke(String name, Class<?>[] parameterTypes, Object... arguments) throws Exception {
		Method method = JavaAspectJFileSystemAdviceDefinitions.class.getDeclaredMethod(name, parameterTypes);
		method.setAccessible(true);
		return method.invoke(null, arguments);
	}

	/** Reads a private static field of the aspect. */
	private static Object staticValue(String name) throws Exception {
		Field field = JavaAspectJFileSystemAdviceDefinitions.class.getDeclaredField(name);
		field.setAccessible(true);
		return field.get(null);
	}

	/** Declares exactly one static {@code File}, as the JDK's holder does. */
	private static final class OneStaticFile {
		/** The only static directory. */
		static File directory;

		/** An instance field, which does not count. */
		File instanceDirectory;
	}

	/** Declares two static {@code File} fields, which is ambiguous. */
	private static final class TwoStaticFiles {
		/** The first static directory. */
		static File first;

		/** The second static directory. */
		static File second;
	}

	/** Declares no static {@code File} field. */
	private static final class NoStaticFile {
		/** A static value of another type. */
		static String name;
	}
}
