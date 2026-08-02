package de.tum.cit.ase.ares.api.securitytest.java.projectScanner;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;

import de.tum.cit.ase.ares.api.buildtoolconfiguration.BuildMode;
import de.tum.cit.ase.ares.api.buildtoolconfiguration.BuildToolConfiguration;

/**
 * Covers the diagnostic that the no-policy scan emits about the test classes it
 * recognised.
 * <p>
 * The split matters for more than tidiness. The count tells an exercise author
 * that the scan ran and how much it found, and is harmless wherever build
 * output ends up. The fully-qualified names would disclose the hidden-test
 * structure to anyone who can read that output, students included, so they must
 * stay behind a level the shipped {@code logback.xml} does not enable.
 * </p>
 */
class JavaProjectScannerDiagnosticsTest {
	@TempDir
	Path root;

	private Logger scannerLogger;

	private ListAppender<ILoggingEvent> appender;

	private Level originalLevel;

	@BeforeEach
	void attachAppender() {
		scannerLogger = (Logger) LoggerFactory.getLogger(JavaProjectScanner.class);
		originalLevel = scannerLogger.getLevel();
		appender = new ListAppender<>();
		appender.start();
		scannerLogger.addAppender(appender);
	}

	@AfterEach
	void detachAppender() {
		scannerLogger.detachAppender(appender);
		appender.stop();
		// A null original level means the logger inherited one, and setLevel(null)
		// restores exactly that, so no other test sees a level this one pinned.
		scannerLogger.setLevel(originalLevel);
	}

	@Test
	@DisplayName("Reports the count at INFO and withholds the class names below TRACE")
	void reportsCountAndWithholdsNames() throws IOException {
		JavaProjectScanner scanner = scannerOverTwoTestClasses();
		// DEBUG is what the shipped logback.xml gives this logger, and it is pinned
		// here so the assertion does not depend on ambient configuration.
		scannerLogger.setLevel(Level.DEBUG);

		String[] recognised = scanner.scanForTestClasses();

		List<ILoggingEvent> events = List.copyOf(appender.list);
		assertEquals(1, events.size(), "the scan should report itself exactly once");
		assertEquals(Level.INFO, events.get(0).getLevel());
		String message = events.get(0).getFormattedMessage();
		assertTrue(message.contains("2 test class(es)"), "the count belongs in the message, was: " + message);
		// Checked against what the scan actually recognised, and against the simple
		// names too. Asserting only on the package prefix would still pass if a
		// future change logged bare class names, which discloses the same structure.
		for (String testClass : recognised) {
			assertFalse(message.contains(testClass),
					"the qualified name " + testClass + " must not appear below TRACE");
			String simpleName = testClass.substring(testClass.lastIndexOf('.') + 1);
			assertFalse(message.contains(simpleName), "the simple name " + simpleName + " must not appear below TRACE");
		}
	}

	@Test
	@DisplayName("Reports the class names only once TRACE is enabled for this logger")
	void reportsNamesAtTrace() throws IOException {
		JavaProjectScanner scanner = scannerOverTwoTestClasses();
		scannerLogger.setLevel(Level.TRACE);

		scanner.scanForTestClasses();

		List<ILoggingEvent> events = List.copyOf(appender.list);
		assertEquals(2, events.size(), "TRACE adds the names to the count, it does not replace it");
		assertEquals(Level.INFO, events.get(0).getLevel());
		assertEquals(Level.TRACE, events.get(1).getLevel());
		String names = events.get(1).getFormattedMessage();
		assertTrue(names.contains("checks.AlphaTest"), "was: " + names);
		assertTrue(names.contains("checks.BetaTest"), "was: " + names);
	}

	private JavaProjectScanner scannerOverTwoTestClasses() throws IOException {
		Path production = Files.createDirectories(root.resolve("custom/main"));
		Path tests = Files.createDirectories(root.resolve("custom/test"));
		Files.writeString(production.resolve("Application.java"), """
				package checks;
				class Application { public static void main(java.lang.String... arguments) {} }
				""");
		Files.writeString(tests.resolve("AlphaTest.java"), """
				package checks;
				import org.junit.jupiter.api.Test;
				class AlphaTest { @Test void alpha() {} }
				""");
		Files.writeString(tests.resolve("BetaTest.java"), """
				package checks;
				import de.tum.cit.ase.ares.api.jupiter.PublicTest;
				class BetaTest { @PublicTest void beta() {} }
				""");
		return new JavaProjectScanner(new BuildToolConfiguration(BuildMode.MAVEN, root, List.of(production),
				List.of(tests), root.resolve("target/classes"), root.resolve("target/test-classes")));
	}
}
