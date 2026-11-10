package de.tum.cit.ase.ares.api.aop.java.instrumentation.advice;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

import java.lang.reflect.Method;
import java.util.StringTokenizer;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * Checks that both backends split a Runtime.exec(String) command into the same
 * arguments the JDK starts the process with, so a check never reads a different
 * argument list than the one that runs.
 */
class CommandStringSplittingTest {

	/**
	 * Class of the AspectJ command advice, which holds its own copy of the
	 * splitter.
	 */
	private static final String ASPECTJ_ADVICE = "de.tum.cit.ase.ares.api.aop.java.aspectj.adviceandpointcut.JavaAspectJCommandSystemAdviceDefinitions";

	/**
	 * Compares both backends with the JDK for commands with Windows paths, quotes,
	 * backslashes and tabs.
	 *
	 * @param command the command string a student passes to Runtime.exec
	 * @throws ReflectiveOperationException if a splitter cannot be reached
	 */
	@ParameterizedTest
	@ValueSource(strings = { "cmd /c type src\\main\\resources\\hidden-solution.txt", "cat 'a b'", "sh -c \"echo x\"",
			"echo a\\ b", "ls\t-la  /tmp" })
	void bothBackendsSplitAsTheJdkDoes(String command) throws ReflectiveOperationException {
		String[] expected = jdkSplit(command);
		assertArrayEquals(expected, split(JavaInstrumentationAdviceCommandSystemToolbox.class, command));
		assertArrayEquals(expected, split(Class.forName(ASPECTJ_ADVICE), command));
	}

	/**
	 * Splits a command the way Runtime.exec(String) does.
	 *
	 * @param command the command string
	 * @return the arguments the JDK uses
	 */
	private static String[] jdkSplit(String command) {
		StringTokenizer tokens = new StringTokenizer(command);
		String[] args = new String[tokens.countTokens()];
		for (int i = 0; i < args.length; i++) {
			args[i] = tokens.nextToken();
		}
		return args;
	}

	/**
	 * Calls the private splitter of one backend.
	 *
	 * @param owner   the class that declares the splitter
	 * @param command the command string
	 * @return the arguments that backend checks
	 * @throws ReflectiveOperationException if the splitter cannot be reached
	 */
	private static String[] split(Class<?> owner, String command) throws ReflectiveOperationException {
		Method splitter = owner.getDeclaredMethod("parseCommandString", String.class);
		splitter.setAccessible(true);
		return (String[]) splitter.invoke(null, command);
	}
}
