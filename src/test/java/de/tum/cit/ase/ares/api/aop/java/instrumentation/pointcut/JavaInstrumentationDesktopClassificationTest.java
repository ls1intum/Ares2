package de.tum.cit.ase.ares.api.aop.java.instrumentation.pointcut;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.util.List;

import org.junit.jupiter.api.Test;

/**
 * Pins the file-system classification of {@link java.awt.Desktop}.
 * <p>
 * Desktop's
 * {@code open}/{@code edit}/{@code print}/{@code browse}/{@code browseFileDirectory}
 * hand a file off to an external application; Ares deliberately classifies them
 * as a single file <em>read</em> in both backends (the AspectJ pointcut
 * documents that it mirrors this instrumentation map), never as an execute, so
 * a Desktop call is governed deterministically. {@code moveToTrash} is the only
 * delete.
 * <p>
 * The corresponding end-to-end integration scenarios cannot be exercised
 * reliably: {@code java.awt.Desktop} is a JDK class whose AOP retransformation
 * does not survive across many tests in a single JVM, and a real Desktop call
 * launches an external application (a side effect unsuitable for a test suite).
 * This contract test therefore guards the classification directly and
 * deterministically, catching any accidental reclassification of Desktop to
 * execute/overwrite/create.
 */
class JavaInstrumentationDesktopClassificationTest {

	private static final String DESKTOP = "java.awt.Desktop";

	@Test
	void desktopFileAccessMethodsAreClassifiedAsRead() {
		assertEquals(List.of("open", "edit", "print", "browse", "browseFileDirectory"),
				JavaInstrumentationPointcutDefinitions.methodsWhichCanReadFiles.get(DESKTOP),
				"Desktop open/edit/print/browse/browseFileDirectory must be classified as a file read.");
	}

	@Test
	void desktopIsNeverClassifiedAsExecuteOverwriteOrCreate() {
		assertFalse(JavaInstrumentationPointcutDefinitions.methodsWhichCanExecuteFiles.containsKey(DESKTOP),
				"Desktop must not be classified as a file execute.");
		assertFalse(JavaInstrumentationPointcutDefinitions.methodsWhichCanOverwriteFiles.containsKey(DESKTOP),
				"Desktop must not be classified as a file overwrite.");
		assertFalse(JavaInstrumentationPointcutDefinitions.methodsWhichCanCreateFiles.containsKey(DESKTOP),
				"Desktop must not be classified as a file create.");
	}

	@Test
	void desktopDeleteIsOnlyMoveToTrash() {
		assertEquals(List.of("moveToTrash"),
				JavaInstrumentationPointcutDefinitions.methodsWhichCanDeleteFiles.get(DESKTOP),
				"Only Desktop.moveToTrash deletes a file; the other Desktop methods are reads.");
	}
}
