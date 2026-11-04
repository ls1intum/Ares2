package de.tum.cit.ase.ares.api.localization;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import de.tum.cit.ase.ares.api.aop.java.JavaAOPTestCase;
import de.tum.cit.ase.ares.api.aop.java.instrumentation.pointcut.JavaInstrumentationBindingDefinitions;
import de.tum.cit.ase.ares.api.phobos.JavaPhobosTestCase;
import de.tum.cit.ase.ares.api.util.FileTools;

/**
 * Checks call sites that used to hand a message too few or too many arguments,
 * or named a key that did not exist. Each produced a bare key, a literal
 * {@code %s}, a dropped argument or the wrong exception instead of the intended
 * message. English is pinned so the expected texts can be exact.
 */
class LocalisedMessageCallSiteTest {

	/** The default locale before this class pinned English. */
	private static Locale originalDefault;

	/** The display locale before this class pinned English. */
	private static Locale originalDisplay;

	/** Pins English for every test in this class. */
	@BeforeAll
	static void pinEnglish() {
		originalDefault = Locale.getDefault();
		originalDisplay = Locale.getDefault(Locale.Category.DISPLAY);
		Locale.setDefault(Locale.ENGLISH);
		Locale.setDefault(Locale.Category.DISPLAY, Locale.ENGLISH);
	}

	/** Restores the locales this class changed. */
	@AfterAll
	static void restoreLocale() {
		Locale.setDefault(originalDefault);
		Locale.setDefault(Locale.Category.DISPLAY, originalDisplay);
	}

	/**
	 * The Phobos builder used to pass one argument to a two-argument message, so
	 * formatting failed and a format exception replaced the intended
	 * {@link SecurityException}.
	 */
	@Test
	void phobosBuilderNamesTheMissingArgumentAndTheBuilder() {
		SecurityException thrown = assertThrows(SecurityException.class,
				() -> JavaPhobosTestCase.builder().javaPhobosTestCaseSupported(null));
		assertEquals("Ares Security Error (Reason: Ares-Code; Stage: Creation): javaPhobosTestCaseSupported must not "
				+ "be null in JavaPhobosTestCase.Builder", thrown.getMessage());
	}

	/**
	 * The AOP builder used to report the bare key, and its allowed-classes check
	 * named the wrong argument.
	 */
	@Test
	void aopBuilderNamesTheMissingArgumentAndTheBuilder() {
		SecurityException thrown = assertThrows(SecurityException.class,
				() -> JavaAOPTestCase.builder().allowedClasses(null));
		assertEquals("Ares Security Error (Reason: Ares-Code; Stage: Creation): allowedClasses must not be null in "
				+ "JavaAOPTestCase.Builder", thrown.getMessage());
	}

	/**
	 * The private constructor of the binding definitions used to print a literal
	 * {@code %s} because the class name was never passed.
	 *
	 * @throws ReflectiveOperationException if the constructor cannot be reached
	 */
	@Test
	void utilityConstructorNamesItsClass() throws ReflectiveOperationException {
		Constructor<JavaInstrumentationBindingDefinitions> constructor = JavaInstrumentationBindingDefinitions.class
				.getDeclaredConstructor();
		constructor.setAccessible(true);
		InvocationTargetException thrown = assertThrows(InvocationTargetException.class, constructor::newInstance);
		SecurityException cause = assertInstanceOf(SecurityException.class, thrown.getCause());
		assertEquals("Ares Security Error (Reason: Ares-Code; Stage: Creation): Utility class "
				+ "JavaInstrumentationBindingDefinitions should not be instantiated.", cause.getMessage());
	}

	/**
	 * A failed directory creation used to report a key that was missing from both
	 * catalogues. The target sits below a regular file, so creating its parent
	 * really fails.
	 *
	 * @param tempDir a fresh directory for this test
	 * @throws IOException if the fixture files cannot be written
	 */
	@Test
	void failedTargetDirectoryIsNamed(@TempDir Path tempDir) throws IOException {
		Path header = Files.writeString(tempDir.resolve("header.txt"), "header");
		Path footer = Files.writeString(tempDir.resolve("footer.txt"), "footer");
		Path blocker = Files.writeString(tempDir.resolve("blocker"), "a file, not a directory");
		Path target = blocker.resolve("missing").resolve("Out.java");
		SecurityException thrown = assertThrows(SecurityException.class,
				() -> FileTools.createThreePartedFile(header, "body", footer, target));
		assertEquals("Ares Security Error (Reason: Ares-Code; Stage: Creation): Failed to create the target directory "
				+ target.getParent() + ".", thrown.getMessage());
	}

	/**
	 * Messages whose template once lacked the placeholder for an argument the
	 * caller passes, or whose key was missing, now render that argument.
	 */
	@Test
	void templatesRenderTheArgumentsTheirCallersPass() {
		assertTrue(
				Messages.localized("security.policy.unsupported.operation", "policy.yaml").contains("'policy.yaml'"));
		String unsupportedMode = Messages.localized("security.architecture.testcase.mode.not.supported", "SOME_MODE");
		assertFalse(unsupportedMode.startsWith("!"), unsupportedMode);
		assertTrue(unsupportedMode.contains("SOME_MODE"), unsupportedMode);
	}
}
