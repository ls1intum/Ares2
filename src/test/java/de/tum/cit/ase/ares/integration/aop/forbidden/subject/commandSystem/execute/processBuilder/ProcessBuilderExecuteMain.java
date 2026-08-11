package de.tum.cit.ase.ares.integration.aop.forbidden.subject.commandSystem.execute.processBuilder;

import java.io.IOException;
import java.util.List;

public class ProcessBuilderExecuteMain {

	private ProcessBuilderExecuteMain() {
		throw new SecurityException(
				"Ares Security Error (Reason: Ares-Code; Stage: Test): Main is a utility class and should not be instantiated.");
	}

	/**
	 * Execute a forbidden command using the {@link ProcessBuilder} class.
	 */
	public static Process executeCommandViaProcessBuilder() throws IOException {
		ProcessBuilder processBuilder = new ProcessBuilder("forbidden-command");
		return processBuilder.start();
	}

	/**
	 * Execute a forbidden command via {@link ProcessBuilder#startPipeline(List)}.
	 * <p>
	 * Both builders are constructed with the allow-listed command, then one is
	 * mutated to a forbidden command before {@code startPipeline} is invoked - the
	 * bypass this reproduces re-reads each builder's command only at construction
	 * time instead of live, at call time.
	 */
	public static List<Process> executeCommandViaProcessBuilderStartPipeline() throws IOException {
		ProcessBuilder producer = new ProcessBuilder("echo", "hello");
		ProcessBuilder consumer = new ProcessBuilder("echo", "hello");
		consumer.command(List.of("forbidden-command"));
		return ProcessBuilder.startPipeline(List.of(producer, consumer));
	}
}
