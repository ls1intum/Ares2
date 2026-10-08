package de.tum.cit.ase.ares.api.aop.java.instrumentation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import de.tum.cit.ase.ares.testutilities.StartupFreezeProbe;

/**
 * Checks, each in a fresh JVM, that values the file-system checks trust are
 * fixed when the agent starts, before any student code: a later change of a JVM
 * property must not move temp files or the trusted Java home. Each check first
 * runs without the agent to show it can fail.
 */
class TrustedStartupFreezeTest {

	/**
	 * Options of this JVM that a forked JVM needs to load Ares the same way, each
	 * written as one argument.
	 */
	private static final List<String> COPIED_OPTION_PREFIXES = List.of("-Xbootclasspath/a:", "--add-opens=",
			"--add-exports=", "--add-reads=", "--add-modules=");

	/**
	 * Options of this JVM that take their value as the next argument.
	 */
	private static final List<String> COPIED_OPTIONS_WITH_VALUE = List.of("--add-opens", "--add-exports", "--add-reads",
			"--add-modules");

	/**
	 * Without a freeze, a JDK that lets the property redirect temp files writes to
	 * the new directory; with the agent's freeze it keeps the start-up directory.
	 * Skipped on a JDK whose temp files a property change never redirects, since
	 * the freeze can make no difference there.
	 */
	@Test
	void aTempDirectoryChangeAfterStartupDoesNotRedirectTempFiles(@TempDir Path redirect) throws Exception {
		String redirected = redirect.toRealPath().toString();
		String withoutAgent = runProbe(false, "temp", redirect);
		Assumptions.assumeTrue(withoutAgent.equals(redirected),
				"this JDK never lets a property change redirect temp files, so the freeze cannot make a difference");
		String startUpDirectory = Path.of(System.getProperty("java.io.tmpdir")).toRealPath().toString();
		assertEquals(startUpDirectory, runProbe(true, "temp", redirect));
	}

	/**
	 * Without a freeze, the aspect trusts whatever Java home is set before its
	 * first use; with the agent's freeze it keeps the start-up Java home.
	 */
	@Test
	void aJavaHomeChangeAfterStartupDoesNotChangeTheTrustedJavaHome(@TempDir Path forbidden) throws Exception {
		assertEquals(forbidden.toString(), runProbe(false, "javaHome", forbidden),
				"without the freeze the aspect should trust the changed Java home, or this check proves nothing");
		assertEquals(System.getProperty("java.home"), runProbe(true, "javaHome", forbidden));
	}

	/**
	 * Runs {@link StartupFreezeProbe} in a fresh JVM and returns what it printed.
	 *
	 * @param withAgent whether the Ares agent is attached
	 * @param probe     the probe to run
	 * @param directory the directory the probe points the property at
	 * @return the probe's result
	 * @throws IOException          if the JVM cannot be started
	 * @throws InterruptedException if waiting is interrupted
	 */
	private static String runProbe(boolean withAgent, String probe, Path directory)
			throws IOException, InterruptedException {
		List<String> command = new ArrayList<>();
		command.add(Path.of(System.getProperty("java.home"), "bin", "java").toString());
		command.addAll(copiedOptions(withAgent));
		command.add("-Djava.io.tmpdir=" + System.getProperty("java.io.tmpdir"));
		command.add("-cp");
		command.add(System.getProperty("java.class.path"));
		command.add(StartupFreezeProbe.class.getName());
		command.add(probe);
		command.add(directory.toString());
		Process process = new ProcessBuilder(command).redirectErrorStream(true).start();
		String output = new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
		assertTrue(process.waitFor(60, TimeUnit.SECONDS), "the probe JVM did not finish");
		Optional<String> result = output.lines().filter(line -> line.startsWith("RESULT=")).findFirst();
		if (result.isEmpty()) {
			fail("the probe JVM printed no result:\n" + output);
		}
		return result.get().substring("RESULT=".length());
	}

	/**
	 * Returns the options of this JVM a forked JVM needs: the boot class path and
	 * module flags always, and the agent only if asked for.
	 *
	 * @param withAgent whether to include the agent
	 * @return the options
	 */
	private static List<String> copiedOptions(boolean withAgent) {
		List<String> arguments = ManagementFactory.getRuntimeMXBean().getInputArguments();
		List<String> copied = new ArrayList<>();
		for (int index = 0; index < arguments.size(); index++) {
			String argument = arguments.get(index);
			if (argument.startsWith("-javaagent:")) {
				if (withAgent) {
					copied.add(argument);
				}
			} else if (COPIED_OPTION_PREFIXES.stream().anyMatch(argument::startsWith)) {
				copied.add(argument);
			} else if (COPIED_OPTIONS_WITH_VALUE.contains(argument) && index + 1 < arguments.size()) {
				copied.add(argument);
				copied.add(arguments.get(++index));
			}
		}
		return copied;
	}
}
