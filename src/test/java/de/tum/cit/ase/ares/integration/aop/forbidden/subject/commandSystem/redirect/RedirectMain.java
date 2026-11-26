package de.tum.cit.ase.ares.integration.aop.forbidden.subject.commandSystem.redirect;

import java.io.File;
import java.io.IOException;

/**
 * Student code that runs the allowed command with its input or output
 * redirected to files.
 */
public final class RedirectMain {

	/** A file no policy allows, which a redirect must not create. */
	private static final File UNLISTED = new File(
			"src/test/java/de/tum/cit/ase/ares/integration/aop/forbidden/subject/redirected.txt");

	/** The forbidden file a redirect must not read. */
	private static final File NOT_TRUSTED = new File(
			"src/test/java/de/tum/cit/ase/ares/integration/aop/forbidden/subject/nottrusted.txt");

	/** Prevents creating instances of this utility class. */
	private RedirectMain() {
		throw new SecurityException(
				"Ares Security Error (Reason: Ares-Code; Stage: Test): Main is a utility class and should not be instantiated.");
	}

	/**
	 * Writes the output of the allowed command into a file.
	 *
	 * @throws IOException          if the command cannot run
	 * @throws InterruptedException if waiting is interrupted
	 */
	public static void redirectOutput() throws IOException, InterruptedException {
		new ProcessBuilder("echo", "hello").redirectOutput(UNLISTED).start().waitFor();
	}

	/**
	 * Appends the output of the allowed command to a file.
	 *
	 * @throws IOException          if the command cannot run
	 * @throws InterruptedException if waiting is interrupted
	 */
	public static void appendOutput() throws IOException, InterruptedException {
		new ProcessBuilder("echo", "hello").redirectOutput(ProcessBuilder.Redirect.appendTo(UNLISTED)).start()
				.waitFor();
	}

	/**
	 * Feeds a forbidden file to the allowed command.
	 *
	 * @throws IOException          if the command cannot run
	 * @throws InterruptedException if waiting is interrupted
	 */
	public static void redirectInput() throws IOException, InterruptedException {
		new ProcessBuilder("echo", "hello").redirectInput(NOT_TRUSTED).start().waitFor();
	}
}
