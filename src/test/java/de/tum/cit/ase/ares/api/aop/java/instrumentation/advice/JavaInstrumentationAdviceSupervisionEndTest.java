package de.tum.cit.ase.ares.api.aop.java.instrumentation.advice;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import de.tum.cit.ase.ares.api.aop.java.JavaAOPTestCase;
import de.tum.cit.ase.ares.api.aop.java.JavaAOPTestCaseSettings;

import example.student.InstrumentationSecurityProbe;

/**
 * Once a test has ended, the instrumentation advice refuses what code that test
 * left running on another thread tries, while the thread that ended the test
 * goes unchecked, and the walk for Ares' own utility reads stops at the first
 * student frame.
 */
class JavaInstrumentationAdviceSupervisionEndTest {

	/** The package the probe belongs to, standing in for student code. */
	private static final String STUDENT_PACKAGE = "example.student";

	/** A file every policy here allows to be read. */
	private URL readableFile;

	/** Loads the messages the denials use, before any check can block that. */
	@BeforeAll
	static void warmLocalizationBundle() {
		JavaInstrumentationAdviceAbstractToolbox.localize("security.advice.denial.reason.test.ended");
		JavaInstrumentationAdviceAbstractToolbox.localize("security.advice.illegal.file.execution");
	}

	/**
	 * Arms a policy that allows every read, as a running test would.
	 *
	 * @param tempDir the folder of the readable file
	 * @throws Exception if the file cannot be written
	 */
	@BeforeEach
	void armPolicyThatAllowsEveryRead(@TempDir Path tempDir) throws Exception {
		inBothCopies("reset");
		Path file = tempDir.resolve("readable.txt");
		Files.writeString(file, "readable");
		readableFile = file.toUri().toURL();
		JavaAOPTestCase.setJavaAdviceSettingValue("aopMode", "INSTRUMENTATION", "ARCH", "INSTRUMENTATION");
		JavaAOPTestCase.setJavaAdviceSettingValue("restrictedPackage", STUDENT_PACKAGE, "ARCH", "INSTRUMENTATION");
		JavaAOPTestCase.setJavaAdviceSettingValue("allowedListedClasses", new String[0], "ARCH", "INSTRUMENTATION");
		JavaAOPTestCase.setJavaAdviceSettingValue("pathsAllowedToBeRead", new String[] { "*" }, "ARCH",
				"INSTRUMENTATION");
	}

	/** Leaves nothing armed for the next test class. */
	@AfterEach
	void resetSettings() throws ReflectiveOperationException {
		inBothCopies("reset");
	}

	/**
	 * Runs a settings method in the copy the test sees and in the boot copy the
	 * instrumentation advice reads, which can be two classes.
	 *
	 * @param methodName the static method of the settings class
	 * @throws ReflectiveOperationException if the method cannot be run
	 */
	private static void inBothCopies(String methodName) throws ReflectiveOperationException {
		JavaAOPTestCaseSettings.class.getMethod(methodName).invoke(null);
		Class<?> bootCopy = Class.forName(JavaAOPTestCaseSettings.class.getName(), false, null);
		bootCopy.getMethod(methodName).invoke(null);
	}

	/**
	 * Runs the read probe on a thread of its own, as code a test left running.
	 *
	 * @return what the read threw, or {@code null} if it passed
	 * @throws InterruptedException if waiting for the thread is interrupted
	 */
	private Throwable readOnAnotherThread() throws InterruptedException {
		AtomicReference<Throwable> failure = new AtomicReference<>();
		Thread reader = new Thread(() -> {
			try {
				InstrumentationSecurityProbe.checkFileUrlOpenStream(readableFile);
			} catch (Throwable thrown) {
				failure.set(thrown);
			}
		});
		reader.start();
		reader.join();
		return failure.get();
	}

	/** While the test runs, another thread may read what the policy allows. */
	@Test
	void anotherThreadReadsWhileTheTestRuns() throws InterruptedException {
		assertNull(readOnAnotherThread());
	}

	/**
	 * After the test, the same read on another thread is refused and says why,
	 * while the thread that ended the test is not checked.
	 */
	@Test
	void anotherThreadIsRefusedOnceTheTestHasEnded() throws ReflectiveOperationException, InterruptedException {
		inBothCopies("endSupervision");

		Throwable refusal = readOnAnotherThread();
		assertInstanceOf(SecurityException.class, refusal);
		assertTrue(refusal.getMessage().endsWith(
				JavaInstrumentationAdviceAbstractToolbox.localize("security.advice.denial.reason.test.ended") + "."),
				refusal.getMessage());
		assertDoesNotThrow(() -> InstrumentationSecurityProbe.checkFileUrlOpenStream(readableFile));
	}

	/** A phase Ares does not guard leaves every thread unchecked. */
	@Test
	void suspendedSupervisionChecksNoThread() throws ReflectiveOperationException, InterruptedException {
		inBothCopies("endSupervision");
		inBothCopies("suspendSupervision");

		assertNull(readOnAnotherThread());
	}

	/** A trusted utility called by Ares reads for Ares. */
	@Test
	void utilityCalledByAresReadsForAres() {
		assertTrue(
				JavaInstrumentationAdviceAbstractToolbox.isTrustedUtilityReadForAres(
						List.of("java.nio.file.Files", "de.tum.cit.ase.ares.api.util.FileTools",
								"de.tum.cit.ase.ares.api.jupiter.JupiterSecurityExtension").iterator(),
						STUDENT_PACKAGE));
	}

	/**
	 * A utility that student code calls, or that calls student code back before the
	 * read, does not read for Ares.
	 */
	@Test
	void utilityWithStudentCodeAroundItDoesNotReadForAres() {
		assertFalse(JavaInstrumentationAdviceAbstractToolbox.isTrustedUtilityReadForAres(List
				.of("java.nio.file.Files", "de.tum.cit.ase.ares.api.util.FileTools", "example.student.Hog").iterator(),
				STUDENT_PACKAGE));
		assertFalse(
				JavaInstrumentationAdviceAbstractToolbox.isTrustedUtilityReadForAres(
						List.of("java.nio.file.Files", "example.student.Hog", "de.tum.cit.ase.ares.api.util.FileTools",
								"de.tum.cit.ase.ares.api.jupiter.JupiterSecurityExtension").iterator(),
						STUDENT_PACKAGE));
	}

	/**
	 * The walk ends at the first student frame, so a deep student recursion does
	 * not make every check walk the whole stack.
	 */
	@Test
	void walkStopsAtTheFirstStudentFrame() {
		Iterator<String> endlessRecursion = new Iterator<>() {
			private int served;

			@Override
			public boolean hasNext() {
				return true;
			}

			@Override
			public String next() {
				served++;
				if (served > 2) {
					throw new NoSuchElementException("read past the first student frame");
				}
				return served == 1 ? "java.nio.file.Files" : "example.student.Hog";
			}
		};
		assertFalse(JavaInstrumentationAdviceAbstractToolbox.isTrustedUtilityReadForAres(endlessRecursion,
				STUDENT_PACKAGE));
	}
}
