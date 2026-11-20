package de.tum.cit.ase.ares.integration.precompile;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import de.tum.cit.ase.ares.api.internal.SecurityViolationFeedbackTexts;
import de.tum.cit.ase.ares.api.policy.SecurityPolicyReaderAndDirector;
import de.tum.cit.ase.ares.testutilities.GeneratedCodeRuns;

/**
 * Runs the failure reporting a precompile run generates against the security
 * tests Ares generates into the exercise, and checks that their failures follow
 * the policy's feedback level: unchanged at HIGH, the fixed text at LOW, the
 * report plus the policy category at DEBUG. The architecture tests are
 * stand-ins under their exact generated names, because the generated ones need
 * the copied analysis libraries to compile.
 */
class GeneratedArchitectureFeedbackLevelTest {

	/** The generated ArchUnit security test class of the exercise. */
	private static final String ARCHUNIT_TEST = "com.example.ares.api.architecture.java.archunit.JavaArchunitTestCase";

	/** The generated WALA security test class of the exercise. */
	private static final String WALA_TEST = "com.example.ares.api.architecture.java.wala.JavaWalaTestCase";

	/** A class with the same method, outside the generated security tests. */
	private static final String LOOKALIKE = "com.example.fixtures.JavaArchunitTestCase";

	/** What the architecture check reports, including a path a student chose. */
	private static final String REPORT = "Architecture Violation: Student.read reads /secret/path";

	/**
	 * Ares's own configuration error, as a generated architecture test reports it.
	 */
	private static final String CONFIGURATION_ERROR = "Ares Security Error (Reason: Ares-Code; Stage: Creation): no class matches the scope";

	/** A failure that wraps a blocked action, with a text of its own. */
	private static final String WRAPPED_TEST = "com.example.fixtures.WrappedViolationTest";

	/** The message of the wrapped blocked action. */
	private static final String VIOLATION = "Ares Security Error (Reason: Student-Code; Stage: Execution): blocked read";

	/** The message the policy sets for other failures. */
	private static final String POLICY_MESSAGE = "Ask your tutor.";

	/** The projects shared by every test of this class. */
	@TempDir
	static Path tempDir;

	/** Compiled output for a policy at HIGH with privileged-only reporting on. */
	private static Path full;

	/** Compiled output for a policy at LOW. */
	private static Path low;

	/** Compiled output for a policy at DEBUG. */
	private static Path debug;

	/**
	 * Generates and compiles the precompile output for the three policies.
	 *
	 * @throws IOException if writing or compiling fails
	 */
	@BeforeAll
	static void generateAndCompile() throws IOException {
		full = project("full", "HIGH", true);
		low = project("low", "LOW", false);
		debug = project("debug", "DEBUG", false);
	}

	/**
	 * At HIGH, the generated security tests keep their report even with
	 * privileged-only reporting on, while another failing test gets the policy's
	 * message.
	 *
	 * @throws Exception if the run cannot be set up
	 */
	@Test
	void fullKeepsTheArchitectureReport() throws Exception {
		assertThat(messages(full, ARCHUNIT_TEST, Locale.ENGLISH)).containsExactlyInAnyOrder(REPORT,
				CONFIGURATION_ERROR);
		assertThat(messages(full, WALA_TEST, Locale.ENGLISH)).containsExactly(REPORT);
		assertThat(messages(full, LOOKALIKE, Locale.ENGLISH)).containsExactly(POLICY_MESSAGE);
	}

	/**
	 * At LOW, the generated security tests show only that something was blocked, in
	 * both locales, and nothing of the report.
	 *
	 * @throws Exception if the run cannot be set up
	 */
	@Test
	void lowShowsOnlyTheFixedText() throws Exception {
		for (String test : List.of(ARCHUNIT_TEST, WALA_TEST)) {
			assertThat(messages(low, test, Locale.ENGLISH)).contains(
					"Ares Security Error (Reason: Student-Code; Stage: Execution): Supervised code tried to illegally violate a policy, but was blocked by Ares.")
					.noneMatch(message -> message.contains("/secret/path"));
			assertThat(messages(low, test, Locale.GERMAN)).filteredOn(message -> !message.equals(CONFIGURATION_ERROR))
					.singleElement().asString().startsWith("Ares Sicherheitsfehler").doesNotContain("/secret/path");
		}
	}

	/**
	 * At DEBUG, the generated security tests show the report and the category that
	 * would allow the access.
	 *
	 * @throws Exception if the run cannot be set up
	 */
	@Test
	void debugAddsTheCategory() throws Exception {
		for (String test : List.of(ARCHUNIT_TEST, WALA_TEST)) {
			assertThat(messages(debug, test, Locale.ENGLISH)).filteredOn(message -> message.startsWith(REPORT))
					.singleElement().asString().endsWith(
							"Allow it by adding an entry under regardingFileSystemInteractions in the security policy.");
		}
	}

	/**
	 * At DEBUG, a blocked argument Ares could not inspect gets no allow hint, since
	 * no policy entry can allow it, while a blocked file action still gets one.
	 *
	 * @throws Exception if the generated hook cannot be loaded
	 */
	@Test
	void debugGivesNoHintForAnUninspectableArgument() throws Exception {
		assertThat(renderedAtDebug(new Blocked("uninspectable", "inspect", "a student Path")))
				.isEqualTo("Main.run tried to illegally inspect a student Path but was blocked by Ares.");
		assertThat(renderedAtDebug(new Blocked("file", "read", "/secret/path")))
				.isEqualTo(inEnglish(() -> SecurityViolationFeedbackTexts.debug(
						"Main.run tried to illegally read /secret/path but was blocked by Ares.", "read",
						"/secret/path", java.util.Optional.empty(),
						SecurityViolationFeedbackTexts.lines("- onThisPathAndAllPathsBelow: \"/secret/path\"",
								"  readAllFiles: true", "  overwriteAllFiles: false", "  createAllFiles: false",
								"  executeAllFiles: false", "  deleteAllFiles: false"))));
	}

	/**
	 * At DEBUG, a target no single entry can name exactly gets only the category: a
	 * command with arguments, whose boundaries the target text lost (one argument
	 * holding a space reads as two), a target that is the wildcard {@code *}, and a
	 * target cut at the 200-character field limit. A command without arguments gets
	 * its entry with an empty argument list.
	 *
	 * @throws Exception if the generated hook cannot be loaded
	 */
	@Test
	void debugNamesOnlyTheCategoryWhenNoEntryIsExact() throws Exception {
		String echo = "Main.run tried to illegally execute /bin/echo hello world but was blocked by Ares.";
		assertThat(renderedAtDebug(new Blocked("command", "execute", "/bin/echo hello world")))
				.isEqualTo(inEnglish(() -> SecurityViolationFeedbackTexts.debugCategory(echo,
						"regardingCommandExecutions", java.util.Optional.empty())));
		assertThat(renderedAtDebug(new Blocked("file", "read", "*")))
				.isEqualTo(inEnglish(() -> SecurityViolationFeedbackTexts.debugCategory(
						"Main.run tried to illegally read * but was blocked by Ares.",
						"regardingFileSystemInteractions", java.util.Optional.empty())));
		assertThat(renderedAtDebug(new Blocked("network", "connect", "*:443")))
				.endsWith(inEnglish(() -> SecurityViolationFeedbackTexts.debugCategory("",
						"regardingNetworkConnections", java.util.Optional.empty())));
		String longPath = "/" + "a".repeat(250);
		assertThat(renderedAtDebug(new Blocked("file", "read", longPath)))
				.endsWith(inEnglish(() -> SecurityViolationFeedbackTexts.debugCategory("",
						"regardingFileSystemInteractions", java.util.Optional.empty())));
		assertThat(renderedAtDebug(new Blocked("command", "execute", "/bin/date")))
				.isEqualTo(inEnglish(() -> SecurityViolationFeedbackTexts.debug(
						"Main.run tried to illegally execute /bin/date but was blocked by Ares.", "execute",
						"/bin/date", java.util.Optional.empty(), SecurityViolationFeedbackTexts
								.lines("- executeTheCommand: \"/bin/date\"", "  withTheseArguments: []"))));
	}

	/**
	 * At DEBUG, the report keeps the blocked action's own stack, so an instructor
	 * sees where the action happened.
	 *
	 * @throws Exception if the generated hook cannot be loaded
	 */
	@Test
	void debugKeepsTheBlockedActionsStack() throws Exception {
		Blocked blocked = new Blocked("file", "read", "/secret/path");
		StackTraceElement[] stack = { new StackTraceElement("org.example.Main", "run", "Main.java", 7) };
		blocked.setStackTrace(stack);
		assertThat(invokeTheDebugHookUnchecked("renderedViolation", new Class<?>[] { Throwable.class }, blocked)
				.getStackTrace()).containsExactly(stack);
	}

	/**
	 * At DEBUG, a generated architecture test for a fixed rule, such as reflection,
	 * which no policy entry relaxes, shows its report without a hint, while a
	 * file-system test names its category.
	 *
	 * @throws Exception if the generated hook cannot be loaded
	 */
	@Test
	void debugGivesAFixedArchitectureRuleNoHint() throws Exception {
		SecurityException reflection = new SecurityException("Main.run tried to illegally use reflection.");
		assertThat(inEnglish(() -> invokeTheDebugHookUnchecked("architectureReplacementFor",
				new Class<?>[] { Throwable.class, String.class }, reflection,
				"theFollowingResourceAccessesArePermitted").getMessage()))
						.isEqualTo("Main.run tried to illegally use reflection.");
		SecurityException file = new SecurityException("Main.run tried to illegally access the file system.");
		assertThat(inEnglish(() -> invokeTheDebugHookUnchecked("architectureReplacementFor",
				new Class<?>[] { Throwable.class, String.class }, file, "regardingFileSystemInteractions")
						.getMessage()))
								.isEqualTo(inEnglish(() -> SecurityViolationFeedbackTexts.debugCategory(
										"Main.run tried to illegally access the file system.",
										"regardingFileSystemInteractions", java.util.Optional.empty())));
	}

	/**
	 * Calls one static method of the DEBUG project's failure reporting.
	 *
	 * @param method    the method's name.
	 * @param types     its parameter types.
	 * @param arguments its arguments.
	 * @return the failure it returns
	 */
	private static Throwable invokeTheDebugHookUnchecked(String method, Class<?>[] types, Object... arguments) {
		try (java.net.URLClassLoader loader = new java.net.URLClassLoader(
				new java.net.URL[] { debug.toUri().toURL(),
						debug.resolveSibling("debug-compiled-resources").toUri().toURL() },
				GeneratedArchitectureFeedbackLevelTest.class.getClassLoader())) {
			java.lang.reflect.Method called = loader
					.loadClass("de.tum.cit.ase.ares.generated.GeneratedFailureReporting")
					.getDeclaredMethod(method, types);
			called.setAccessible(true);
			return (Throwable) called.invoke(null, arguments);
		} catch (Exception unexpected) {
			throw new IllegalStateException(unexpected);
		}
	}

	/**
	 * Renders a blocked action as the DEBUG project's failure reporting does, with
	 * English as the display language, so the expected English text does not depend
	 * on the machine's language.
	 *
	 * @param violation the blocked action.
	 * @return the rendered text
	 * @throws Exception if the generated hook cannot be loaded
	 */
	private static String renderedAtDebug(Throwable violation) throws Exception {
		return inEnglish(() -> renderedByTheDebugHookUnchecked(violation));
	}

	/**
	 * Computes a text with English as the display language.
	 *
	 * @param text the text to compute.
	 * @return the text
	 */
	private static String inEnglish(java.util.function.Supplier<String> text) {
		Locale originalLocale = Locale.getDefault(Locale.Category.DISPLAY);
		try {
			Locale.setDefault(Locale.Category.DISPLAY, Locale.ENGLISH);
			return text.get();
		} finally {
			Locale.setDefault(Locale.Category.DISPLAY, originalLocale);
		}
	}

	/**
	 * {@link #renderedByTheDebugHook} with its checked failure wrapped.
	 *
	 * @param violation the blocked action.
	 * @return the rendered text
	 */
	private static String renderedByTheDebugHookUnchecked(Throwable violation) {
		try {
			return renderedByTheDebugHook(violation);
		} catch (Exception unexpected) {
			throw new IllegalStateException(unexpected);
		}
	}

	/**
	 * Loads the DEBUG project's failure reporting and renders a blocked action with
	 * it.
	 *
	 * @param violation the blocked action.
	 * @return the rendered text
	 * @throws Exception if the generated hook cannot be loaded
	 */
	private static String renderedByTheDebugHook(Throwable violation) throws Exception {
		try (java.net.URLClassLoader loader = new java.net.URLClassLoader(
				new java.net.URL[] { debug.toUri().toURL(),
						debug.resolveSibling("debug-compiled-resources").toUri().toURL() },
				GeneratedArchitectureFeedbackLevelTest.class.getClassLoader())) {
			java.lang.reflect.Method rendered = loader
					.loadClass("de.tum.cit.ase.ares.generated.GeneratedFailureReporting")
					.getDeclaredMethod("renderedViolation", Throwable.class);
			rendered.setAccessible(true);
			return ((Throwable) rendered.invoke(null, violation)).getMessage();
		}
	}

	/** A blocked action with the fields the failure reporting reads. */
	public static final class Blocked extends RuntimeException {
		/** Serialisation identifier. */
		private static final long serialVersionUID = 1L;

		/** The kind of action. */
		private final String kind;

		/** The action. */
		private final String action;

		/** What it was aimed at. */
		private final String target;

		/**
		 * A blocked action.
		 *
		 * @param kind   the kind of action.
		 * @param action the action.
		 * @param target what it was aimed at.
		 */
		Blocked(String kind, String action, String target) {
			super("Main.run tried to illegally " + action + " " + target + " but was blocked by Ares.");
			this.kind = kind;
			this.action = action;
			this.target = target;
		}

		/**
		 * The kind of action.
		 *
		 * @return the kind
		 */
		public String kind() {
			return kind;
		}

		/**
		 * The action.
		 *
		 * @return the action
		 */
		public String action() {
			return action;
		}

		/**
		 * What it was aimed at.
		 *
		 * @return the target
		 */
		public String target() {
			return target;
		}
	}

	/**
	 * Ares's own configuration error in a generated architecture test keeps its
	 * text at every level, so it is neither shortened nor given an allow hint.
	 *
	 * @throws Exception if the run cannot be set up
	 */
	@Test
	void aConfigurationErrorKeepsItsText() throws Exception {
		for (Path project : List.of(low, debug)) {
			assertThat(messages(project, ARCHUNIT_TEST, Locale.ENGLISH)).contains(CONFIGURATION_ERROR);
		}
	}

	/**
	 * With privileged-only reporting at HIGH, a failure that wraps a blocked action
	 * reports the blocked action, not the wrapper's own text.
	 *
	 * @throws Exception if the run cannot be set up
	 */
	@Test
	void aWrappedViolationIsReportedWithoutItsWrapper() throws Exception {
		assertThat(messages(full, WRAPPED_TEST, Locale.ENGLISH)).containsExactly(VIOLATION);
	}

	/**
	 * With privileged-only reporting at HIGH, a blocked action carrying a student
	 * failure whose {@code toString} never returns is reported as a copy with its
	 * own text, so printing the report runs no student code, in a JVM of its own.
	 *
	 * @throws Exception if compiling or starting the JVM fails
	 */
	@Test
	void aFullViolationIsReportedAsACopy() throws Exception {
		String output = runProbe("loop");
		assertThat(output).as(output).contains("ares.probe.message=" + VIOLATION).contains("ares.probe.done");
	}

	/**
	 * With privileged-only reporting at HIGH, an ordinary assertion attached to a
	 * blocked action as its cause or suppressed failure shows only the policy's
	 * message, while the blocked action keeps its own text.
	 *
	 * @throws Exception if compiling or starting the JVM fails
	 */
	@Test
	void aFullViolationRedactsWhatIsAttachedToIt() throws Exception {
		String output = runProbe("assertion");
		assertThat(output).as(output).contains("ares.probe.message=" + VIOLATION).contains(POLICY_MESSAGE)
				.doesNotContain("expected=42").doesNotContain("cause=7").contains("ares.probe.done");
	}

	/**
	 * With privileged-only reporting at HIGH, a class named like the copied blocked
	 * action but loaded by another class loader is not treated as one, so its own
	 * text is never shown.
	 *
	 * @throws Exception if compiling or starting the JVM fails
	 */
	@Test
	void aLookalikeViolationFromAnotherLoaderIsNotKept() throws Exception {
		String output = runProbe("lookalike");
		assertThat(output).as(output).contains("ares.probe.message=" + POLICY_MESSAGE)
				.doesNotContain("lookalike-secret").contains("ares.probe.done");
	}

	/**
	 * Runs the compiled probe in a JVM of its own and returns what it printed.
	 *
	 * @param mode what the probe attaches to the blocked action.
	 * @return the probe's output
	 * @throws Exception if compiling or starting the JVM fails
	 */
	private String runProbe(String mode) throws Exception {
		Path probe = compileProbe(Files.createDirectories(tempDir.resolve("probe-" + mode)));
		String classPath = String.join(File.pathSeparator, probe.toString(), full.toString(),
				full.resolveSibling(full.getFileName() + "-resources").toString(),
				System.getProperty("surefire.test.class.path", System.getProperty("java.class.path")));
		Process process = new ProcessBuilder(Path.of(System.getProperty("java.home"), "bin", "java").toString(), "-cp",
				classPath, "com.example.probe.FullViolationProbe", VIOLATION, mode, full.toUri().toString())
						.redirectErrorStream(true).start();
		try {
			assertThat(process.waitFor(60, TimeUnit.SECONDS)).as("the probe finished").isTrue();
			return new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
		} finally {
			process.destroyForcibly();
		}
	}

	/**
	 * Compiles a probe that hands the HIGH project's failure reporting a blocked
	 * action with a looping student failure, or ordinary assertions, attached and
	 * prints the report.
	 *
	 * @param root the probe's folder.
	 * @return the compiled probe's folder
	 * @throws IOException if writing or compiling fails
	 */
	private static Path compileProbe(Path root) throws IOException {
		Path sources = Files.createDirectories(root.resolve("sources/com/example/probe"));
		Files.writeString(sources.resolve("FullViolationProbe.java"),
				"""
						package com.example.probe;

						public final class FullViolationProbe {
							public static void main(String[] arguments) throws Exception {
								ClassLoader loader = FullViolationProbe.class.getClassLoader();
								ClassLoader origin = "lookalike".equals(arguments[1])
										? new java.net.URLClassLoader(new java.net.URL[] { java.net.URI.create(arguments[2]).toURL() }, null)
										: loader;
								Throwable violation = (Throwable) origin
										.loadClass("com.example.ares.api.aop.java.aspectj.adviceandpointcut.AresSecurityViolation")
										.getConstructor(String.class)
										.newInstance("lookalike".equals(arguments[1]) ? "lookalike-secret" : arguments[0]);
								if ("assertion".equals(arguments[1])) {
									violation.initCause(new AssertionError("cause=7"));
									violation.addSuppressed(new AssertionError("expected=42"));
								} else {
									violation.addSuppressed(new RuntimeException("attached") {
										@Override
										public String toString() {
											while (true) {
												Thread.onSpinWait();
											}
										}
									});
								}
								java.lang.reflect.Method replacement = loader
										.loadClass("de.tum.cit.ase.ares.generated.GeneratedFailureReporting")
										.getDeclaredMethod("replacementFor", Throwable.class);
								replacement.setAccessible(true);
								Throwable reported = (Throwable) replacement.invoke(null, violation);
								java.io.StringWriter trace = new java.io.StringWriter();
								reported.printStackTrace(new java.io.PrintWriter(trace));
								System.out.println("ares.probe.message=" + reported.getMessage());
								System.out.println("ares.probe.trace=" + trace);
								System.out.println("ares.probe.done");
								System.exit(0);
							}
						}
						""");
		Path classes = Files.createDirectories(root.resolve("classes"));
		GeneratedCodeRuns.compile(classes, List.of(root.resolve("sources")));
		return classes;
	}

	/**
	 * A class that only looks like a generated security test is not shortened, so
	 * the level applies to Ares's own tests by their exact names alone.
	 *
	 * @throws Exception if the run cannot be set up
	 */
	@Test
	void aLookalikeIsNotTreatedAsASecurityTest() throws Exception {
		assertThat(messages(low, LOOKALIKE, Locale.ENGLISH)).containsExactly(REPORT);
	}

	/**
	 * Runs one class through a nested JUnit session and returns its failure
	 * messages.
	 *
	 * @param project the compiled project.
	 * @param test    the class to run.
	 * @param locale  the locale messages are shown in.
	 * @return the failure messages
	 * @throws Exception if the run cannot be set up
	 */
	private static List<String> messages(Path project, String test, Locale locale) throws Exception {
		String include = Files
				.readAllLines(project.resolveSibling(project.getFileName() + "-resources")
						.resolve("junit-platform.properties"))
				.stream().filter(line -> line.startsWith("junit.jupiter.extensions.autodetection.include="))
				.map(line -> line.substring(line.indexOf('=') + 1)).findFirst().orElseThrow();
		return GeneratedCodeRuns.failureMessages(GeneratedCodeRuns.runFresh(
				GeneratedCodeRuns
						.classPath(List.of(project, project.resolveSibling(project.getFileName() + "-resources"))),
				test, true, include, locale));
	}

	/**
	 * Generates the precompile output for one policy and compiles it with the
	 * stand-ins.
	 *
	 * @param name           the project's folder name.
	 * @param level          the feedback level.
	 * @param privilegedOnly whether only privileged exceptions are reported.
	 * @return the compiled project
	 * @throws IOException if writing or compiling fails
	 */
	private static Path project(String name, String level, boolean privilegedOnly) throws IOException {
		Path project = Files.createDirectory(tempDir.resolve(name));
		Files.writeString(project.resolve("pom.xml"), "<project/>");
		Files.createDirectories(project.resolve("src/main/java"));
		Path testSources = Files.createDirectories(project.resolve("src/test/java"));
		Files.createDirectories(project.resolve("target/classes"));
		Path policy = project.resolve("SecurityPolicy.yaml");
		Files.writeString(policy, policyText(level, privilegedOnly));
		SecurityPolicyReaderAndDirector.builder().securityPolicyFilePath(policy).projectFolderPath(project).build()
				.createTestCases().writeTestCases(testSources);
		Path compiled = Files.createDirectories(tempDir.resolve(name + "-compiled"));
		List<Path> standIns = writeStandIns(Files.createDirectories(tempDir.resolve(name + "-stand-ins")));
		List<Path> singles = new ArrayList<>(standIns);
		singles.add(testSources.resolve("com/example/ares/api/util/LruCache.java"));
		GeneratedCodeRuns.compile(compiled, List.of(testSources.resolve("de/tum/cit/ase/ares/generated"),
				testSources.resolve("com/example/ares/api/localization")), singles.toArray(Path[]::new));
		copyResources(project.resolve("src/test/resources"), tempDir.resolve(name + "-compiled-resources"));
		return compiled;
	}

	/**
	 * Copies the generated test resources next to the compiled project.
	 *
	 * @param from the generated resources.
	 * @param to   where the nested run reads them.
	 * @throws IOException if copying fails
	 */
	private static void copyResources(Path from, Path to) throws IOException {
		try (var files = Files.walk(from)) {
			for (Path file : files.filter(Files::isRegularFile).toList()) {
				Path target = to.resolve(from.relativize(file).toString());
				Files.createDirectories(target.getParent());
				Files.copy(file, target);
			}
		}
	}

	/**
	 * Writes the stand-ins: the two generated security tests and a lookalike, each
	 * failing its file-system check with the same report, the ArchUnit one also
	 * with a configuration error, and a test wrapping a blocked action under the
	 * generated violation's exact name.
	 *
	 * @param sources the folder to write them into.
	 * @return the written files
	 * @throws IOException if writing fails
	 */
	private static List<Path> writeStandIns(Path sources) throws IOException {
		List<Path> files = new ArrayList<>();
		for (String className : List.of(ARCHUNIT_TEST, WALA_TEST, LOOKALIKE)) {
			String configurationTest = className.equals(ARCHUNIT_TEST) ? """
					@Test
					public void networkSystemShouldNotBeAccessed() {
						throw new SecurityException("%s");
					}
					""".formatted(CONFIGURATION_ERROR) : "";
			files.add(writeClass(sources, className, """
					import org.junit.jupiter.api.Test;

					public class %s {
						@Test
						public void filesystemsShouldNotBeAccessed() {
							throw new AssertionError("%s");
						}
						%s
					}
					""".formatted(simpleName(className), REPORT, configurationTest)));
		}
		files.add(
				writeClass(sources, "com.example.ares.api.aop.java.aspectj.adviceandpointcut.AresSecurityViolation", """
						public final class AresSecurityViolation extends SecurityException {
							private static final long serialVersionUID = 1L;
							public AresSecurityViolation(String message) { super(message); }
							public String kind() { return "file"; }
							public String action() { return "read"; }
							public String target() { return "secret.txt"; }
						}
						"""));
		files.add(writeClass(sources, WRAPPED_TEST,
				"""
						import org.junit.jupiter.api.Test;

						public class WrappedViolationTest {
							@Test
							public void wraps() {
								throw new AssertionError("secret expected value",
										new com.example.ares.api.aop.java.aspectj.adviceandpointcut.AresSecurityViolation("%s"));
							}
						}
						"""
						.formatted(VIOLATION)));
		return files;
	}

	/**
	 * Writes one source file under its package's folder.
	 *
	 * @param sources   the source root.
	 * @param className the class's full name.
	 * @param body      the source after the package declaration.
	 * @return the written file
	 * @throws IOException if writing fails
	 */
	private static Path writeClass(Path sources, String className, String body) throws IOException {
		String packageName = className.substring(0, className.lastIndexOf('.'));
		Path file = Files.createDirectories(sources.resolve(packageName.replace('.', '/')))
				.resolve(simpleName(className) + ".java");
		Files.writeString(file, "package " + packageName + ";\n\n" + body);
		return file;
	}

	/**
	 * The simple name of a class.
	 *
	 * @param className the class's full name.
	 * @return the part after the last dot
	 */
	private static String simpleName(String className) {
		return className.substring(className.lastIndexOf('.') + 1);
	}

	/**
	 * A policy for the {@code com.example} exercise with a failure-feedback
	 * category.
	 *
	 * @param level          the feedback level.
	 * @param privilegedOnly whether only privileged exceptions are reported.
	 * @return the policy text
	 */
	private static String policyText(String level, boolean privilegedOnly) {
		return """
				thisPolicyFileCompliesToThePolicyVersion: 2
				regardingTheSupervisedCode:
				  theFollowingProgrammingLanguageConfigurationIsUsed: JAVA_USING_MAVEN_ARCHUNIT_AND_ASPECTJ
				  theSupervisedCodeUsesTheFollowingPackage: "com.example"
				  theMainClassInsideThisPackageIs: "Main"
				  theFollowingClassesAreTestClasses: []
				  theFollowingResourceAccessesArePermitted:
				    regardingFileSystemInteractions: []
				    regardingNetworkConnections: []
				    regardingCommandExecutions: []
				    regardingThreadCreations: []
				    regardingPackageImports: []
				    regardingTimeouts: []
				  theFollowingTestBehaviorIsConfigured:
				    regardingTheFailureFeedbackForTheTestCases:
				      onlyPrivilegedExceptionsAreReported: %s
				      theFailureMessageForOtherExceptionsIs: "%s"
				      theSecurityViolationFeedbackLevelIs: %s
				""".formatted(privilegedOnly, POLICY_MESSAGE, level);
	}
}
