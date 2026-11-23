package de.tum.cit.ase.ares.api.io;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Scanner;

import org.junit.jupiter.api.*;

import de.tum.cit.ase.ares.api.localization.AdviceActionWording;
import de.tum.cit.ase.ares.api.localization.Messages;

@SuppressWarnings("static-method")
class IOTesterTest {

	private IOTester tester;

	@BeforeEach
	void testInstall() {
		tester = IOTester.installNew(false, 1_000_000);
	}

	@AfterEach
	void testUninstall() {
		IOTester.uninstallCurrent();
	}

	@Test
	void testReset() {
		try (Scanner scanner = new Scanner(System.in)) {
			tester.in().addLinesToInput("X", "Y");

			System.out.println("A");
			System.err.println("B");

			assertThat(tester.out().getLines()).isNotEmpty();
			assertThat(tester.err().getLines()).isNotEmpty();
			assertThat(scanner.nextLine()).isEqualTo("X");

			tester.in().addLinesToInput("Z");

			tester.reset();

			assertThat(tester.out().getLines()).isEmpty();
			assertThat(tester.err().getLines()).isEmpty();
			assertThrows(IllegalStateException.class, scanner::nextLine);
		}
	}

	@Test
	void testIn() {
		assertThat(tester.in()).isNotNull().isSameAs(tester.getInTester());
	}

	@Test
	void testOut() {
		assertThat(tester.out()).isNotNull().isSameAs(tester.getOutTester());
	}

	@Test
	void testErr() {
		assertThat(tester.err()).isNotNull().isSameAs(tester.getErrTester());
	}

	/**
	 * Printing more than the output limit fails with a prefixed
	 * {@link SecurityException} that blames the student code while it runs, in
	 * English and German, and names the number of characters printed.
	 */
	@Test
	void outputLimitIsAPrefixedSecurityExceptionBlamingTheStudentCode() {
		String english = AdviceActionWording.inLocale(Locale.ENGLISH, IOTesterTest::printPastTheLimit);
		String german = AdviceActionWording.inLocale(Locale.GERMAN, IOTesterTest::printPastTheLimit);

		assertThat(english).startsWith("Ares Security Error (Reason: Student-Code; Stage: Execution): ") //$NON-NLS-1$
				.isEqualTo(AdviceActionWording.inLocale(Locale.ENGLISH,
						() -> Messages.localized("output_tester.output_maxExceeded", 6L))); //$NON-NLS-1$
		assertThat(german).startsWith("Ares Sicherheitsfehler (Grund: Student-Code; Phase: Ausführung): ") //$NON-NLS-1$
				.contains("6"); //$NON-NLS-1$
	}

	/**
	 * Installs a fresh tester with a limit of five characters, prints six and
	 * returns the text of the failure.
	 *
	 * @return the text of the output-limit failure
	 */
	private static String printPastTheLimit() {
		IOTester.uninstallCurrent();
		IOTester.installNew(false, 5);
		byte[] sixCharacters = "123456".getBytes(StandardCharsets.UTF_8); //$NON-NLS-1$
		return assertThrows(SecurityException.class, () -> System.out.write(sixCharacters, 0, sixCharacters.length))
				.getMessage();
	}

	@Test
	void staleInstanceCannotReplaceOrUninstallCurrentCapture() {
		IOTester stale = tester;
		IOTester.uninstallCurrent();
		tester = IOTester.installNew(false, 1_000_000);
		var currentInput = System.in;
		var currentOutput = System.out;
		var currentError = System.err;

		assertThrows(IllegalStateException.class, stale::install);
		assertThrows(IllegalStateException.class, stale::uninstall);
		assertThrows(IllegalStateException.class, stale::reset);
		assertThat(System.in).isSameAs(currentInput);
		assertThat(System.out).isSameAs(currentOutput);
		assertThat(System.err).isSameAs(currentError);
		assertThat(IOTester.isInstalled()).isTrue();
	}
}
