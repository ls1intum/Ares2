package de.tum.cit.ase.ares.integration.precompile;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.management.ManagementFactory;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import de.tum.cit.ase.ares.api.localization.Messages;
import de.tum.cit.ase.ares.api.policy.SecurityPolicyReaderAndDirector;
import de.tum.cit.ase.ares.api.policy.policySubComponents.ProgrammingLanguageConfiguration;
import de.tum.cit.ase.ares.testutilities.MessageTemplates;

/**
 * A Precompile exercise must check a student's {@code notifyAll} on a thread
 * object in both AOP modes, as Postcompile does, and must keep allowed thread
 * and archive calls working. AspectJ checks where it weaves; instrumentation
 * only after rewriting the calls in the student's package, which the generated
 * agent has to set up on its own.
 */
class PrecompileThreadMonitorTest {

	/** The package of the generated exercise's student code. */
	private static final String STUDENT_PACKAGE = "example.monitor";

	/** The catalogue key of the thread denial sentence, which takes five values. */
	private static final String THREAD_DENIAL = "security.advice.illegal.thread.execution";

	/** The marker before a refused monitor call on the running thread. */
	private static final String CURRENT_THREAD_DENIED = "CURRENT_THREAD_MONITOR_DENIED: ";

	/** The file name of the archive the archive exercise may read. */
	private static final String ARCHIVE = "allowed.jar";

	/**
	 * Without a thread rule, the student's {@code notifyAll} on the running thread
	 * is refused because no rule exists, while the same call in the launcher and a
	 * call on a plain object still return.
	 *
	 * @param configuration the build tool and the two Ares modes
	 * @throws Exception if a file cannot be written or a child cannot be started; a
	 *                   failing build or launch fails an assertion instead
	 */
	@ParameterizedTest
	@EnumSource(ProgrammingLanguageConfiguration.class)
	void withoutAThreadRuleTheRunningThreadIsRefused(ProgrammingLanguageConfiguration configuration) throws Exception {
		String output = launch(configuration, generate(configuration, "", false), "noRule");
		assertTrue(output.contains("LAUNCHER_MONITOR_RETURNED"), output);
		assertTrue(output.contains("OBJECT_MONITOR_RETURNED"), output);
		assertRefusedRunningThread(output, "security.advice.denial.reason.no.rule.thread");
	}

	/**
	 * With a rule allowing one thread of the student's task class, monitor calls on
	 * that thread return, the thread starts and finishes once, and the running
	 * thread is still refused because no rule covers it.
	 *
	 * @param configuration the build tool and the two Ares modes
	 * @throws Exception if a file cannot be written or a child cannot be started; a
	 *                   failing build or launch fails an assertion instead
	 */
	@ParameterizedTest
	@EnumSource(ProgrammingLanguageConfiguration.class)
	void withAThreadRuleTheAllowedThreadWorksAndTheRunningThreadIsRefused(
			ProgrammingLanguageConfiguration configuration) throws Exception {
		String rule = "\n      - createTheFollowingNumberOfThreads: 1\n        ofThisClass: \"" + STUDENT_PACKAGE
				+ ".Task\"";
		String output = launch(configuration, generate(configuration, rule, false), "oneThread");
		assertTrue(output.contains("LAUNCHER_MONITOR_RETURNED"), output);
		assertTrue(output.contains("TASK_MONITOR_RETURNED"), output);
		assertTrue(output.contains("TASK_STARTED_AND_JOINED"), output);
		assertRefusedRunningThread(output, "security.advice.denial.reason.target.not.allowed.thread");
	}

	/**
	 * With a rule allowing the archive to be read, the student lists its entries
	 * and reads one; in instrumentation these calls run through the rewritten
	 * archive calls.
	 *
	 * @param configuration the build tool and the two Ares modes
	 * @throws Exception if a file cannot be written or a child cannot be started; a
	 *                   failing build or launch fails an assertion instead
	 */
	@ParameterizedTest
	@EnumSource(ProgrammingLanguageConfiguration.class)
	void anAllowedArchiveCanStillBeRead(ProgrammingLanguageConfiguration configuration) throws Exception {
		String output = launch(configuration, generate(configuration, "", true), "archive");
		assertTrue(output.contains("ARCHIVE_READ_RETURNED"), output);
	}

	/**
	 * Asserts that the student's call on the running thread was refused with the
	 * thread sentence: by the student method, for manipulating, via
	 * {@code notifyAll}, for the given reason.
	 *
	 * @param output the launcher's output
	 * @param reason the key of the expected reason
	 */
	private static void assertRefusedRunningThread(String output, String reason) {
		int start = output.indexOf(CURRENT_THREAD_DENIED);
		assertTrue(start >= 0, output);
		String denial = output.substring(start + CURRENT_THREAD_DENIED.length()).lines().findFirst().orElse("");
		List<String> arguments = MessageTemplates.argumentsOf(THREAD_DENIAL, 5, denial)
				.orElseThrow(() -> new AssertionError("Not the thread denial sentence: " + denial));
		assertTrue(arguments.get(0).contains(STUDENT_PACKAGE + ".MonitorSubject"), denial);
		assertEquals(Messages.localized("security.advice.action.manipulate"), arguments.get(1), denial);
		assertTrue(arguments.get(3).contains("notifyAll"), denial);
		assertEquals(Messages.localized(reason), arguments.get(4), denial);
	}

	/**
	 * Generates, compiles and, for instrumentation, packages the exercise.
	 *
	 * @param configuration the build tool and the two Ares modes
	 * @param threadRules   the thread rules, as policy lines, or the empty string
	 * @param withArchive   whether to write the archive and a rule to read it
	 * @return the project folder
	 * @throws Exception if a file cannot be written or the compiler cannot be
	 *                   started; a failing compile fails an assertion instead
	 */
	private static Path generate(ProgrammingLanguageConfiguration configuration, String threadRules,
			boolean withArchive) throws Exception {
		Path project = Files.createTempDirectory(Path.of("target").toAbsolutePath(), "monitor-generated-");
		Path testSources = Files.createDirectories(project.resolve("src/test/java"));
		Files.createDirectories(project.resolve("src/main/java"));
		boolean gradle = configuration.name().contains("GRADLE");
		Files.createDirectories(project.resolve(gradle ? "build/classes/java/main" : "target/classes"));
		Files.writeString(project.resolve(gradle ? "build.gradle" : "pom.xml"),
				gradle ? "plugins { id 'java' }" : "<project/>");
		String fileRules = withArchive ? archiveRule(writeArchive(project.resolve(ARCHIVE))) : "";
		Path policy = Files.writeString(project.resolve("policy.yaml"), policy(configuration, fileRules, threadRules));
		SecurityPolicyReaderAndDirector.builder().projectFolderPath(project).securityPolicyFilePath(policy).build()
				.createTestCases().writeTestCases(testSources);
		write(testSources.resolve("example/monitor/MonitorSubject.java"), subject());
		write(testSources.resolve("example/monitor/Task.java"), task());
		write(testSources.resolve("fixture/MonitorLauncher.java"), launcher());
		Path classes = Files.createDirectories(project.resolve("compiled"));
		JceGeneratedExerciseTest.compile(testSources, classes, project.resolve("compile.log"));
		if (!isAspectJ(configuration)) {
			JceGeneratedExerciseTest.createAgent(testSources.resolve("example/monitor/META-INF/MANIFEST.MF"), classes,
					project.resolve("agent.jar"));
		}
		return project;
	}

	/**
	 * Writes an archive with one text entry.
	 *
	 * @param archive the archive to write
	 * @return the archive
	 * @throws Exception if it cannot be written
	 */
	private static Path writeArchive(Path archive) throws Exception {
		try (var output = new java.util.jar.JarOutputStream(Files.newOutputStream(archive))) {
			output.putNextEntry(new java.util.jar.JarEntry("entry.txt"));
			output.write("archive fixture".getBytes(java.nio.charset.StandardCharsets.UTF_8));
			output.closeEntry();
		}
		return archive;
	}

	/**
	 * The file rule that allows reading the archive and nothing else.
	 *
	 * @param archive the archive
	 * @return the rule, as policy lines
	 */
	private static String archiveRule(Path archive) {
		return "\n      - readAllFiles: true\n        overwriteAllFiles: false\n        createAllFiles: false"
				+ "\n        executeAllFiles: false\n        deleteAllFiles: false\n        onThisPathAndAllPathsBelow: "
				+ JceGeneratedExerciseTest.singleQuoted(archive);
	}

	/**
	 * Starts the exercise's launcher, with the generated agent for instrumentation.
	 *
	 * @param configuration the build tool and the two Ares modes
	 * @param project       the project folder
	 * @param exercise      which calls the launcher makes
	 * @return the output
	 * @throws Exception if the launcher cannot be started or its log read; a
	 *                   failing launch fails an assertion instead
	 */
	private static String launch(ProgrammingLanguageConfiguration configuration, Path project, String exercise)
			throws Exception {
		List<String> command = new ArrayList<>();
		command.add(JceGeneratedExerciseTest.javaExecutable());
		ManagementFactory.getRuntimeMXBean().getInputArguments().stream()
				.filter(argument -> argument.startsWith("-Xbootclasspath/a:") || argument.startsWith("--add-opens=")
						|| argument.startsWith("--add-exports="))
				.forEach(command::add);
		if (!isAspectJ(configuration)) {
			command.add("-javaagent:" + project.resolve("agent.jar"));
		}
		command.addAll(List.of("-cp",
				project.resolve("compiled") + java.io.File.pathSeparator + project.resolve("src/test/resources")
						+ java.io.File.pathSeparator + JceGeneratedExerciseTest.classpath(),
				"fixture.MonitorLauncher", exercise, project.resolve(ARCHIVE).toString()));
		Path log = project.resolve(exercise + ".log");
		JceGeneratedExerciseTest.requireSuccess(command, log);
		return Files.readString(log);
	}

	/**
	 * Writes a source file, creating its folder.
	 *
	 * @param file   the file
	 * @param source its text
	 * @throws Exception if it cannot be written
	 */
	private static void write(Path file, String source) throws Exception {
		Files.createDirectories(file.getParent());
		Files.writeString(file, source);
	}

	/**
	 * Whether the configuration enforces with AspectJ.
	 *
	 * @param configuration the configuration
	 * @return whether it ends in AspectJ
	 */
	private static boolean isAspectJ(ProgrammingLanguageConfiguration configuration) {
		return configuration.name().endsWith("ASPECTJ");
	}

	/**
	 * The policy: the student package and the given rules, nothing else.
	 *
	 * @param configuration the configuration
	 * @param fileRules     the file rules, as policy lines, or the empty string
	 * @param threadRules   the thread rules, as policy lines, or the empty string
	 * @return the policy text
	 */
	private static String policy(ProgrammingLanguageConfiguration configuration, String fileRules, String threadRules) {
		return """
				thisPolicyFileCompliesToThePolicyVersion: 1
				regardingTheSupervisedCode:
				  theFollowingProgrammingLanguageConfigurationIsUsed: %s
				  theSupervisedCodeUsesTheFollowingPackage: "%s"
				  theMainClassInsideThisPackageIs: "MonitorSubject"
				  theFollowingClassesAreTestClasses: []
				  theFollowingResourceAccessesArePermitted:
				    regardingFileSystemInteractions:%s
				    regardingNetworkConnections: []
				    regardingCommandExecutions: []
				    regardingThreadCreations:%s
				    regardingPackageImports: []
				    regardingTimeouts: []
				""".formatted(configuration, STUDENT_PACKAGE, fileRules.isEmpty() ? " []" : fileRules,
				threadRules.isEmpty() ? " []" : threadRules);
	}

	/**
	 * The student class: monitor calls on a plain object, on the running thread and
	 * on a thread of its own task class, and an archive read.
	 *
	 * @return the source
	 */
	private static String subject() {
		return """
				package example.monitor;
				public final class MonitorSubject {
				    private MonitorSubject() {
				    }
				    public static String objectMonitor() {
				        Object lock = new Object();
				        synchronized (lock) {
				            lock.notifyAll();
				        }
				        return "OBJECT_MONITOR_RETURNED";
				    }
				    public static String currentThreadMonitor() {
				        Thread current = Thread.currentThread();
				        try {
				            synchronized (current) {
				                current.notifyAll();
				            }
				            return "CURRENT_THREAD_MONITOR_RETURNED";
				        } catch (SecurityException denied) {
				            return "CURRENT_THREAD_MONITOR_DENIED: " + denied.getMessage();
				        }
				    }
				    public static String allowedThread() throws InterruptedException {
				        Thread task = new Thread(new Task());
				        try {
				            synchronized (task) {
				                task.notifyAll();
				                task.wait(1L);
				            }
				        } catch (SecurityException denied) {
				            return "TASK_MONITOR_DENIED: " + denied.getMessage();
				        }
				        try {
				            task.start();
				            task.join(10_000L);
				        } catch (SecurityException denied) {
				            return "TASK_MONITOR_RETURNED\\nTASK_START_DENIED: " + denied.getMessage();
				        }
				        return "TASK_MONITOR_RETURNED\\n" + (task.isAlive() ? "TASK_STILL_ALIVE" : "TASK_STARTED_AND_JOINED");
				    }
				    public static String allowedArchive(String path) throws java.io.IOException {
				        try (java.util.jar.JarFile archive = new java.util.jar.JarFile(path)) {
				            java.util.Enumeration<java.util.jar.JarEntry> entries = archive.entries();
				            java.util.jar.JarEntry entry = entries.nextElement();
				            try (java.io.InputStream input = archive.getInputStream(entry)) {
				                return new String(input.readAllBytes(), java.nio.charset.StandardCharsets.UTF_8)
				                        .equals("archive fixture") ? "ARCHIVE_READ_RETURNED" : "ARCHIVE_READ_WRONG";
				            }
				        } catch (SecurityException denied) {
				            return "ARCHIVE_READ_DENIED: " + denied.getMessage();
				        }
				    }
				}
				""";
	}

	/**
	 * The student's task class, which the thread rule allows.
	 *
	 * @return the source
	 */
	private static String task() {
		return """
				package example.monitor;
				public final class Task implements Runnable {
				    @Override
				    public void run() {
				    }
				}
				""";
	}

	/**
	 * The launcher outside the student package: it first makes the same monitor
	 * call on its own thread, then runs the student calls of the chosen exercise.
	 *
	 * @return the source
	 */
	private static String launcher() {
		return """
				package fixture;
				public final class MonitorLauncher {
				    private MonitorLauncher() {
				    }
				    public static void main(String[] arguments) throws Exception {
				        Thread own = Thread.currentThread();
				        synchronized (own) {
				            own.notifyAll();
				        }
				        System.out.println("LAUNCHER_MONITOR_RETURNED");
				        if (arguments[0].equals("archive")) {
				            System.out.println(example.monitor.MonitorSubject.allowedArchive(arguments[1]));
				            return;
				        }
				        if (arguments[0].equals("oneThread")) {
				            System.out.println(example.monitor.MonitorSubject.allowedThread());
				        } else {
				            System.out.println(example.monitor.MonitorSubject.objectMonitor());
				        }
				        System.out.println(example.monitor.MonitorSubject.currentThreadMonitor());
				    }
				}
				""";
	}
}
