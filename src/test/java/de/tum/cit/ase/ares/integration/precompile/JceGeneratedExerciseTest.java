package de.tum.cit.ase.ares.integration.precompile;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.management.ManagementFactory;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import de.tum.cit.ase.ares.api.localization.Messages;
import de.tum.cit.ase.ares.api.policy.SecurityPolicyReaderAndDirector;
import de.tum.cit.ase.ares.api.policy.policySubComponents.ProgrammingLanguageConfiguration;
import de.tum.cit.ase.ares.integration.jce.JceTraceAgent;

/**
 * Exercises the generated AspectJ startup boundary before exemptions can be
 * removed. Generated AspectJ exercises run without any agent; instrumentation
 * runs with its own agent.
 */
class JceGeneratedExerciseTest {

	/** The JVM argument that lets the aspect read the JVM's start-up values. */
	private static final String START_UP_EXPORT = "--add-exports=java.base/jdk.internal.misc=ALL-UNNAMED";

	/**
	 * The JVM argument that also lets the aspect read them, by opening the package.
	 */
	private static final String START_UP_OPENS = "--add-opens=java.base/jdk.internal.misc=ALL-UNNAMED";

	/**
	 * The JVM argument that lets the aspect read the JDK's own default temp
	 * directory.
	 */
	private static final String JAVA_IO_OPENS = "--add-opens=java.base/java.io=ALL-UNNAMED";

	/**
	 * Generates and compiles actual advice, then tries poisoning its home before
	 * first use. Instrumentation initialises its checks before the change; AspectJ
	 * initialises its aspect after it and still trusts only the start-up home.
	 */
	@ParameterizedTest
	@EnumSource(ProgrammingLanguageConfiguration.class)
	void generatedExerciseJavaHomeChangeBeforeFirstAdviceDoesNotAuthoriseStudentRead(
			ProgrammingLanguageConfiguration configuration) throws Exception {
		Path project = generate(configuration);
		String control = launch(configuration, project, "control");
		assertTrue(control.contains("GENERATED_READ_DENIED"), control);
		String output = launch(configuration, project, "snapshot");
		assertFalse(output.contains("GENERATED_READ_RETURNED"), output);
		assertTrue(output.contains("GENERATED_READ_DENIED"), output);
		String backend = isAspectJ(configuration)
				? "example/jce/ares/api/aop/java/aspectj/adviceandpointcut/JavaAspectJFileSystemAdviceDefinitions"
				: "example/jce/ares/api/aop/java/instrumentation/advice/JavaInstrumentationAdviceFileSystemToolbox";
		int initialisation = output.indexOf("Initializing '" + backend + "'");
		int change = output.indexOf("GENERATED_PROPERTY_CHANGE");
		assertTrue(initialisation >= 0 && change >= 0, output);
		assertEquals(isAspectJ(configuration), initialisation > change, output);
	}

	/** Requires real generated JCE setup after the enforcement startup marker. */
	@ParameterizedTest
	@EnumSource(ProgrammingLanguageConfiguration.class)
	void generatedExerciseAllowsJcePolicyReads(ProgrammingLanguageConfiguration configuration) throws Exception {
		Path project = generate(configuration);
		String output = launch(configuration, project, "crypto", true, List.of(), List.of());
		int initialisation = output.indexOf("Initializing 'javax/crypto/JceSecurity'");
		assertTrue(initialisation > output.indexOf("GENERATED_FIRST_CRYPTO"), output);
		assertTrue(output.contains("GENERATED_PERMITTED_READ"), output);
		assertTrue(output.contains("GENERATED_READ_DENIED"), output);
		String backend = isAspectJ(configuration) ? "JavaAspectJFileSystemAdviceDefinitions"
				: "JavaInstrumentationAdviceFileSystemToolbox";
		assertTrue(output.contains("JCE_BACKEND_ENTRY example.jce.ares.api.aop.java.")
				&& output.contains(backend + " STUDENT"), output);
		assertEquals(configuration.name().endsWith("INSTRUMENTATION"), output.contains(backend + " JDK_JCE"), output);
		String untraced = launch(configuration, project, "crypto");
		assertTrue(untraced.contains("GENERATED_PERMITTED_READ") && untraced.contains("GENERATED_READ_DENIED"),
				untraced);
	}

	/**
	 * Verifies actual file operations and matching controls in the copied backends.
	 */
	@ParameterizedTest
	@EnumSource(ProgrammingLanguageConfiguration.class)
	void generatedExerciseRetainsFilePermissions(ProgrammingLanguageConfiguration configuration) throws Exception {
		Path project = generate(configuration);
		String output = launch(configuration, project, "boundary");
		assertTrue(output.contains("JCE_BOUNDARY_PASSED"), output);
		assertTrue(output.contains("JCE_PROVIDER_CALLBACK_DENIED_AND_PERMITTED"), output);
		assertTrue(output.contains("JCE_CLASS_IDENTITY_DENIED_AND_PERMITTED"), output);
	}

	/**
	 * A second real generation keeps both emitted contents and executable
	 * behaviour.
	 */
	@ParameterizedTest
	@EnumSource(ProgrammingLanguageConfiguration.class)
	void regeneratingExercisePreservesBehaviour(ProgrammingLanguageConfiguration configuration) throws Exception {
		Path project = generate(configuration);
		Path sources = project.resolve("src/test/java");
		var before = sourceContents(project.resolve("src"));
		SecurityPolicyReaderAndDirector.builder().projectFolderPath(project)
				.securityPolicyFilePath(project.resolve("policy.yaml")).build().createTestCases()
				.writeTestCases(sources);
		assertEquals(before, sourceContents(project.resolve("src")));
		compile(sources, project.resolve("compiled"), project.resolve("regenerated-compile.log"));
		if (!isAspectJ(configuration)) {
			createAgent(sources, project.resolve("compiled"), project.resolve("agent.jar"));
		}
		assertTrue(launch(configuration, project, "boundary").contains("JCE_BOUNDARY_PASSED"));
	}

	/**
	 * Reads generated source and resource bytes as text for deterministic
	 * regeneration checks.
	 */
	private static java.util.Map<String, String> sourceContents(Path sources) throws Exception {
		var contents = new java.util.TreeMap<String, String>();
		try (var files = Files.walk(sources)) {
			for (Path file : files.filter(Files::isRegularFile).toList()) {
				contents.put(sources.relativize(file).toString(), Files.readString(file));
			}
		}
		return contents;
	}

	/**
	 * A copied AspectJ exercise that cannot read the JVM's start-up values refuses
	 * a read whose verdict depends on them, and never returns the file.
	 */
	@ParameterizedTest
	@EnumSource(value = ProgrammingLanguageConfiguration.class, names = ".*ASPECTJ", mode = EnumSource.Mode.MATCH_ALL)
	void generatedAspectJWithoutTheModuleGrantsFailsClosed(ProgrammingLanguageConfiguration configuration)
			throws Exception {
		Path project = generate(configuration);
		String output = launch(configuration, project, "control", false, List.of(START_UP_EXPORT, START_UP_OPENS),
				List.of());
		assertTrue(output.contains(
				"GENERATED_READ_DENIED: " + Messages.localized("security.advice.startup.properties.unavailable")),
				output);
		assertFalse(output.contains("GENERATED_READ_RETURNED"), output);
	}

	/**
	 * Either the export or the opening of the JDK package alone lets a copied
	 * AspectJ exercise read the start-up values, so its denial names the file.
	 */
	@ParameterizedTest
	@EnumSource(value = ProgrammingLanguageConfiguration.class, names = ".*ASPECTJ", mode = EnumSource.Mode.MATCH_ALL)
	void generatedAspectJNeedsOnlyOneModuleGrant(ProgrammingLanguageConfiguration configuration) throws Exception {
		Path project = generate(configuration);
		for (String removed : List.of(START_UP_EXPORT, START_UP_OPENS)) {
			String output = launch(configuration, project, "control", false, List.of(removed), List.of());
			assertTrue(output.contains("GENERATED_READ_DENIED"), output);
			assertFalse(output.contains(Messages.localized("security.advice.startup.properties.unavailable")), output);
		}
	}

	/**
	 * Without an agent, temporary files without a directory land in the start-up
	 * temp directory, with both {@code File} and {@code Files}.
	 */
	@ParameterizedTest
	@EnumSource(value = ProgrammingLanguageConfiguration.class, names = ".*ASPECTJ", mode = EnumSource.Mode.MATCH_ALL)
	void generatedAspectJAllowsTempFilesInTheStartUpDirectory(ProgrammingLanguageConfiguration configuration)
			throws Exception {
		Path project = generate(configuration);
		String output = launch(configuration, project, "tempDefault");
		String startUp = value(output, "GENERATED_START_TEMP=");
		assertEquals(startUp, value(output, "GENERATED_TEMP_IO_CREATED="), output);
		assertEquals(startUp, value(output, "GENERATED_TEMP_IO_NULL_CREATED="), output);
		assertEquals(startUp, value(output, "GENERATED_TEMP_NIO_CREATED="), output);
	}

	/**
	 * Student code that the JDK runs while preparing its default temp directory, a
	 * random-number provider here, stays checked: its forbidden read is refused and
	 * its permitted read returns.
	 */
	@ParameterizedTest
	@EnumSource(value = ProgrammingLanguageConfiguration.class, names = ".*ASPECTJ", mode = EnumSource.Mode.MATCH_ALL)
	void generatedAspectJChecksStudentCodeRunWhileTheJdkPreparesItsTempDirectory(
			ProgrammingLanguageConfiguration configuration) throws Exception {
		Path project = generate(configuration);
		String output = launch(configuration, project, "tempProvider");
		assertTrue(output.contains("GENERATED_PROVIDER_FORBIDDEN_DENIED"), output);
		assertFalse(output.contains("GENERATED_PROVIDER_FORBIDDEN_RETURNED"), output);
		assertEquals("permitted fixture", value(output, "GENERATED_PROVIDER_PERMITTED_RETURNED="), output);
		assertEquals(value(output, "GENERATED_START_TEMP="), value(output, "GENERATED_TEMP_IO_CREATED="), output);
		assertFalse(output.contains("GENERATED_TEMP_IO_DENIED"), output);
	}

	/**
	 * Without the opening of {@code java.io}, only a {@code File} temp file without
	 * a directory is refused, naming that argument and keeping the JDK's reason;
	 * {@code Files} and permitted directories still work.
	 */
	@ParameterizedTest
	@EnumSource(value = ProgrammingLanguageConfiguration.class, names = ".*ASPECTJ", mode = EnumSource.Mode.MATCH_ALL)
	void generatedAspectJWithoutTheJavaIoOpeningRefusesOnlyTheDefaultFileTempFile(
			ProgrammingLanguageConfiguration configuration) throws Exception {
		Path project = generate(configuration);
		String output = launch(configuration, project, "tempExplicit", false, List.of(JAVA_IO_OPENS), List.of());
		String allowed = project.resolve("allowed-temp").toRealPath().toString();
		assertEquals(allowed, value(output, "GENERATED_TEMP_ALLOWED_IO_CREATED="), output);
		assertEquals(allowed, value(output, "GENERATED_TEMP_ALLOWED_NIO_CREATED="), output);
		assertEquals(Messages.localized("security.advice.temp.directory.holder.unreadable"),
				value(output, "GENERATED_TEMP_IO_DENIED="), output);
		assertEquals("java.lang.reflect.InaccessibleObjectException", value(output, "GENERATED_TEMP_IO_CAUSE="),
				output);
		assertEquals(value(output, "GENERATED_START_TEMP="), value(output, "GENERATED_TEMP_NIO_CREATED="), output);
	}

	/**
	 * A default temp directory the policy allows needs none of the start-up values
	 * for {@code File}, whose directory the JDK holds, while {@code Files}, whose
	 * directory is the start-up value, is refused without them.
	 */
	@ParameterizedTest
	@EnumSource(value = ProgrammingLanguageConfiguration.class, names = ".*ASPECTJ", mode = EnumSource.Mode.MATCH_ALL)
	void generatedAspectJAllowsAPermittedDefaultDirectoryWithoutTheModuleGrants(
			ProgrammingLanguageConfiguration configuration) throws Exception {
		Path project = generate(configuration);
		Path allowed = project.resolve("allowed-temp");
		String output = launch(configuration, project, "tempDefault", false, List.of(START_UP_EXPORT, START_UP_OPENS),
				List.of("-Djava.io.tmpdir=" + allowed));
		assertEquals(allowed.toRealPath().toString(), value(output, "GENERATED_TEMP_IO_CREATED="), output);
		assertEquals(allowed.toRealPath().toString(), value(output, "GENERATED_TEMP_IO_NULL_CREATED="), output);
		assertEquals(Messages.localized("security.advice.startup.properties.unavailable"),
				value(output, "GENERATED_TEMP_NIO_DENIED="), output);
	}

	/**
	 * Only the Maven repository the JVM started with is trusted for {@code .jar}
	 * reads; one named later through {@code maven.repo.local} or {@code user.home}
	 * is not.
	 */
	@ParameterizedTest
	@EnumSource(value = ProgrammingLanguageConfiguration.class, names = ".*ASPECTJ", mode = EnumSource.Mode.MATCH_ALL)
	void generatedAspectJTrustsOnlyTheStartUpMavenRepository(ProgrammingLanguageConfiguration configuration)
			throws Exception {
		Path project = generate(configuration);
		for (String repository : List.of("repo", "late-repo", "late-home/.m2/repository")) {
			Files.writeString(Files.createDirectories(project.resolve(repository)).resolve("lib.jar"), "jar fixture");
		}
		String output = launch(configuration, project, "jarRead", false, List.of(),
				List.of("-Dmaven.repo.local=" + project.resolve("repo")));
		assertEquals("jar fixture", value(output, "GENERATED_JAR_START_RETURNED="), output);
		assertTrue(output.contains("GENERATED_JAR_LATE_REPOSITORY_DENIED"), output);
		assertTrue(output.contains("GENERATED_JAR_LATE_HOME_DENIED"), output);
	}

	/**
	 * Without {@code maven.repo.local}, the trusted repository lies under the
	 * {@code user.home} the JVM started with; one changed before the first check is
	 * not trusted.
	 */
	@ParameterizedTest
	@EnumSource(value = ProgrammingLanguageConfiguration.class, names = ".*ASPECTJ", mode = EnumSource.Mode.MATCH_ALL)
	void generatedAspectJTrustsOnlyTheStartUpHomeRepository(ProgrammingLanguageConfiguration configuration)
			throws Exception {
		Path project = generate(configuration);
		for (String home : List.of("start-home", "late-home")) {
			Files.writeString(Files.createDirectories(project.resolve(home + "/.m2/repository")).resolve("lib.jar"),
					"jar fixture");
		}
		String output = launch(configuration, project, "jarReadHome", false, List.of(),
				List.of("-Duser.home=" + project.resolve("start-home")));
		assertTrue(output.contains("GENERATED_JAR_LATE_HOME_DENIED"), output);
		assertEquals("jar fixture", value(output, "GENERATED_JAR_START_HOME_RETURNED="), output);
	}

	/**
	 * On JDK 17 an allowed call with a directory can make the JDK keep a changed
	 * {@code java.io.tmpdir}; a later call without a directory must then be refused
	 * rather than write there. Later JDKs keep the start-up directory.
	 */
	@ParameterizedTest
	@EnumSource(value = ProgrammingLanguageConfiguration.class, names = ".*ASPECTJ", mode = EnumSource.Mode.MATCH_ALL)
	void generatedAspectJRefusesATempDirectoryTheJdkKeptAfterAChange(ProgrammingLanguageConfiguration configuration)
			throws Exception {
		Path project = generate(configuration);
		String output = launch(configuration, project, "tempHolder");
		String startUp = value(output, "GENERATED_START_TEMP=");
		assertEquals(startUp, value(output, "GENERATED_TEMP_EXPLICIT_CREATED="), output);
		assertTrue(output.contains("GENERATED_CHANGED_FILES=0"), output);
		int feature = Integer.parseInt(value(output, "GENERATED_JDK="));
		if (feature == 17) {
			assertTrue(output.contains("GENERATED_TEMP_IO_DENIED="), output);
		} else if (feature >= 21) {
			assertEquals(startUp, value(output, "GENERATED_TEMP_IO_CREATED="), output);
		}
	}

	/**
	 * A link in the start-up temp directory that is moved after the first check
	 * does not move the trusted directory: the unchanged link is allowed, the moved
	 * one refused, and nothing appears at the new target.
	 */
	@ParameterizedTest
	@EnumSource(value = ProgrammingLanguageConfiguration.class, names = ".*ASPECTJ", mode = EnumSource.Mode.MATCH_ALL)
	void generatedAspectJRefusesATempLinkMovedAfterStartUp(ProgrammingLanguageConfiguration configuration)
			throws Exception {
		Path project = generate(configuration);
		Path target = Files.createDirectories(project.resolve("linked-temp"));
		Path link = project.resolve("link-temp");
		try {
			Files.createSymbolicLink(link, target);
		} catch (UnsupportedOperationException | java.io.IOException unsupported) {
			Assumptions.abort("Symbolic links are unavailable here: " + unsupported);
		}
		String output = launch(configuration, project, "tempLink", false, List.of(),
				List.of("-Djava.io.tmpdir=" + link));
		assertEquals(target.toRealPath().toString(), value(output, "GENERATED_TEMP_NIO_CREATED="), output);
		assertTrue(output.contains("GENERATED_TEMP_REDIRECTED_DENIED="), output);
		assertTrue(output.contains("GENERATED_REDIRECTED_FILES=0"), output);
	}

	/**
	 * A directory the policy allows needs none of the JVM's start-up values, while
	 * a temp file without a directory is refused when they cannot be read.
	 */
	@ParameterizedTest
	@EnumSource(value = ProgrammingLanguageConfiguration.class, names = ".*ASPECTJ", mode = EnumSource.Mode.MATCH_ALL)
	void generatedAspectJAllowsPermittedTempDirectoriesWithoutTheModuleGrants(
			ProgrammingLanguageConfiguration configuration) throws Exception {
		Path project = generate(configuration);
		String output = launch(configuration, project, "tempExplicit", false, List.of(START_UP_EXPORT, START_UP_OPENS),
				List.of());
		String allowed = project.resolve("allowed-temp").toRealPath().toString();
		assertEquals(allowed, value(output, "GENERATED_TEMP_ALLOWED_IO_CREATED="), output);
		assertEquals(allowed, value(output, "GENERATED_TEMP_ALLOWED_NIO_CREATED="), output);
		String unavailable = Messages.localized("security.advice.startup.properties.unavailable");
		assertEquals(unavailable, value(output, "GENERATED_TEMP_IO_DENIED="), output);
		assertEquals(unavailable, value(output, "GENERATED_TEMP_NIO_DENIED="), output);
	}

	/** Returns the rest of the first output line that starts with a label. */
	private static String value(String output, String label) {
		return output.lines().filter(line -> line.startsWith(label)).map(line -> line.substring(label.length()))
				.findFirst().orElseThrow(() -> new AssertionError("Missing " + label + " in:\n" + output));
	}

	/**
	 * Tells whether a configuration enforces with AspectJ, so without any agent.
	 */
	private static boolean isAspectJ(ProgrammingLanguageConfiguration configuration) {
		return configuration.name().endsWith("ASPECTJ");
	}

	/** Emits and compiles a fresh project through the real generation pipeline. */
	static Path generate(ProgrammingLanguageConfiguration configuration) throws Exception {
		Path project = Files.createTempDirectory(Path.of("target").toAbsolutePath(), "jce-generated-capture-");
		Path testSources = Files.createDirectories(project.resolve("src/test/java"));
		Files.createDirectories(project.resolve("src/main/java"));
		Files.createDirectories(project.resolve("allowed-temp"));
		boolean gradle = configuration.name().contains("GRADLE");
		Files.createDirectories(project.resolve(gradle ? "build/classes/java/main" : "target/classes"));
		Files.writeString(project.resolve(gradle ? "build.gradle" : "pom.xml"),
				gradle ? "plugins { id 'java' }" : "<project/>");
		Path policy = project.resolve("policy.yaml");
		Files.writeString(policy, policy(configuration, project));
		SecurityPolicyReaderAndDirector.builder().projectFolderPath(project).securityPolicyFilePath(policy).build()
				.createTestCases().writeTestCases(testSources);
		copySubject(testSources);
		Path launcher = testSources.resolve("fixture/GeneratedCaptureLauncher.java");
		Files.createDirectories(launcher.getParent());
		Files.writeString(launcher, launcher());
		Path classes = Files.createDirectories(project.resolve("compiled"));
		compile(testSources, classes, project.resolve("compile.log"));
		if (!isAspectJ(configuration)) {
			createAgent(testSources, classes, project.resolve("agent.jar"));
		}
		Files.writeString(project.resolve("allowed.txt"), "permitted fixture");
		return project;
	}

	/**
	 * Copies the student fixture's source so the generated aspects weave its real
	 * call.
	 */
	private static void copySubject(Path sources) throws Exception {
		for (String name : new String[] { "JceCryptoSubject", "JceProviderService", "JceTempFileSubject",
				"JceRandomProvider", "JceRandomSpi" }) {
			Path destination = sources.resolve("example/jce/" + name + ".java");
			Files.createDirectories(destination.getParent());
			Files.writeString(destination, Files.readString(Path.of("src/test/java/example/jce", name + ".java")));
		}
		Path helper = Path.of("src/test/java/de/tum/cit/ase/ares/integration/jce/JceRuntimeContract.java");
		Path destination = sources.resolve("de/tum/cit/ase/ares/integration/jce/JceRuntimeContract.java");
		Files.createDirectories(destination.getParent());
		Files.writeString(destination, Files.readString(helper));
	}

	/**
	 * Compiles the emitted Java and AspectJ sources with the project's pinned
	 * compiler.
	 */
	private static void compile(Path sources, Path classes, Path log) throws Exception {
		List<String> command = new ArrayList<>();
		command.add(javaExecutable());
		command.add("-cp");
		command.add(compiler().toString());
		command.add("org.aspectj.tools.ajc.Main");
		command.addAll(List.of("-17", "-d", classes.toString(), "-classpath", classpath(), "-sourceroots",
				sources.toString()));
		requireSuccess(command, log);
	}

	/**
	 * Launches generated settings without postcompile policy preparation warming
	 * its aspect: AspectJ with no agent at all, instrumentation with its own agent
	 * and the trace agent.
	 */
	private static String launch(ProgrammingLanguageConfiguration configuration, Path project, String operation)
			throws Exception {
		return launch(configuration, project, operation, false, List.of(), List.of());
	}

	/**
	 * Runs the actual copied classes. AspectJ gets no agent unless traced, which
	 * the child confirms by counting its own agents; the given parent JVM arguments
	 * are left out and the extra ones added.
	 */
	private static String launch(ProgrammingLanguageConfiguration configuration, Path project, String operation,
			boolean traceAspectJ, List<String> withoutArguments, List<String> extraArguments) throws Exception {
		Path fakeHome = Files.createDirectories(project.resolve("fake-home"));
		Path file = fakeHome.resolve("default_local.policy");
		Files.writeString(file, "protected fixture");
		Path log = project.resolve(operation + ".log");
		boolean agentFree = isAspectJ(configuration) && !traceAspectJ;
		List<String> command = new ArrayList<>();
		command.add(javaExecutable());
		ManagementFactory.getRuntimeMXBean().getInputArguments().stream()
				.filter(argument -> argument.startsWith("-Xbootclasspath/a:") || argument.startsWith("--add-opens=")
						|| argument.startsWith("--add-exports="))
				.filter(argument -> !withoutArguments.contains(argument)).forEach(command::add);
		command.addAll(extraArguments);
		if (!isAspectJ(configuration)) {
			command.add("-javaagent:" + project.resolve("agent.jar"));
		}
		if (!agentFree) {
			command.add("-javaagent:" + JceTraceAgent.packageAgent());
		}
		command.addAll(List.of("-Xlog:class+init=info", "-cp",
				project.resolve("compiled") + java.io.File.pathSeparator + project.resolve("src/test/resources")
						+ java.io.File.pathSeparator + classpath(),
				"fixture.GeneratedCaptureLauncher", file.toString(), operation,
				project.resolve("allowed.txt").toString()));
		requireSuccess(command, log);
		String output = Files.readString(log);
		if (agentFree) {
			assertTrue(output.contains("GENERATED_JAVA_AGENTS=0"), output);
		}
		return output;
	}

	/** Packages compiled generated classes with the generated startup manifest. */
	private static void createAgent(Path sources, Path classes, Path destination) throws Exception {
		java.util.jar.Manifest manifest;
		try (var input = Files.newInputStream(sources.resolve("example/jce/META-INF/MANIFEST.MF"))) {
			manifest = new java.util.jar.Manifest(input);
		}
		try (var output = new java.util.jar.JarOutputStream(Files.newOutputStream(destination), manifest);
				var files = Files.walk(classes)) {
			for (Path file : files.filter(Files::isRegularFile).toList()) {
				output.putNextEntry(new java.util.jar.JarEntry(classes.relativize(file).toString().replace('\\', '/')));
				Files.copy(file, output);
				output.closeEntry();
			}
		}
	}

	/** Executes a compiler or fixture in a child, retaining logs for review. */
	private static void requireSuccess(List<String> command, Path log) throws Exception {
		Process child = new ProcessBuilder(command).redirectErrorStream(true).redirectOutput(log.toFile()).start();
		try {
			if (!child.waitFor(90, TimeUnit.SECONDS)) {
				throw new AssertionError("Generated exercise command timed out: " + log);
			}
			assertEquals(0, child.exitValue(), () -> readLog(log));
		} finally {
			child.destroyForcibly();
		}
	}

	/**
	 * Reads command evidence without hiding an unreadable log behind the initial
	 * failure.
	 */
	private static String readLog(Path log) {
		try {
			return Files.readString(log);
		} catch (java.io.IOException failure) {
			throw new IllegalStateException("Cannot read generated-exercise log: " + log, failure);
		}
	}

	/**
	 * Uses the requested fixture JVM, or the test JVM when no override was
	 * supplied.
	 */
	static String javaExecutable() {
		return System.getProperty("ares.jce.java", Path.of(System.getProperty("java.home"), "bin", "java").toString());
	}

	/** Returns the actual dependency classpath provided by Surefire. */
	static String classpath() {
		return System.getProperty("surefire.test.class.path", System.getProperty("java.class.path"));
	}

	/**
	 * Finds the compiler matching the AspectJ runtime Maven resolved for this
	 * build.
	 */
	static Path compiler() {
		Path runtime = java.util.Arrays
				.stream(classpath().split(java.util.regex.Pattern.quote(java.io.File.pathSeparator))).map(Path::of)
				.filter(path -> path.getFileName().toString().startsWith("aspectjrt-")
						&& path.getFileName().toString().endsWith(".jar"))
				.findFirst()
				.orElseThrow(() -> new IllegalStateException("AspectJ runtime is absent from the classpath"));
		return runtime.getParent().getParent().getParent().resolve("aspectjtools")
				.resolve(runtime.getParent().getFileName())
				.resolve(runtime.getFileName().toString().replace("aspectjrt-", "aspectjtools-"));
	}

	/**
	 * Defines a narrow filesystem permission in the real precompile policy, plus
	 * one directory temporary files may be created in.
	 */
	private static String policy(ProgrammingLanguageConfiguration configuration, Path project) {
		return """
				thisPolicyFileCompliesToThePolicyVersion: 1
				regardingTheSupervisedCode:
				  theFollowingProgrammingLanguageConfigurationIsUsed: %s
				  theSupervisedCodeUsesTheFollowingPackage: "example.jce"
				  theMainClassInsideThisPackageIs: "JceCryptoSubject"
				  theFollowingClassesAreTestClasses: []
				  theFollowingResourceAccessesArePermitted:
				    regardingFileSystemInteractions:
				      - readAllFiles: true
				        overwriteAllFiles: false
				        createAllFiles: false
				        executeAllFiles: false
				        deleteAllFiles: false
				        onThisPathAndAllPathsBelow: "%s"
				      - readAllFiles: false
				        overwriteAllFiles: false
				        createAllFiles: true
				        executeAllFiles: false
				        deleteAllFiles: false
				        onThisPathAndAllPathsBelow: "%s"
				    regardingNetworkConnections:
				              - onTheHost: "localhost"
				                onThePort: 1
				                openConnections: false
				                sendData: false
				                receiveData: false
				    regardingCommandExecutions: []
				    regardingThreadCreations: []
				    regardingPackageImports:
				              - importTheFollowingPackage: "javax.crypto"
				              - importTheFollowingPackage: "javax.net.ssl"
				    regardingTimeouts: []
				""".formatted(configuration, project.resolve("allowed.txt").toString(),
				project.resolve("allowed-temp").toString());
	}

	/**
	 * Provides trusted setup outside the supervised package; only the read enters
	 * it.
	 */
	private static String launcher() {
		return """
				package fixture;
				/** Changes a property before entering the woven student fixture. */
				public final class GeneratedCaptureLauncher {
				    /** Tries the first student read against a fake JDK home, or creates temporary files. */
				    public static void main(String[] arguments) throws Exception {
				        java.nio.file.Path file = java.nio.file.Path.of(arguments[0]);
				        long agents = java.lang.management.ManagementFactory.getRuntimeMXBean().getInputArguments().stream()
				                .filter(argument -> argument.startsWith("-javaagent:")).count();
				        System.out.println("GENERATED_JAVA_AGENTS=" + agents);
				        if (arguments[1].startsWith("temp")) {
				            temp(arguments[1], java.nio.file.Path.of(arguments[2]).getParent(), file);
				            return;
				        }
				        if (arguments[1].equals("jarRead")) {
				            jarRead(java.nio.file.Path.of(arguments[2]).getParent());
				            return;
				        }
				        if (arguments[1].equals("jarReadHome")) {
				            java.nio.file.Path project = java.nio.file.Path.of(arguments[2]).getParent();
				            System.setProperty("user.home", project.resolve("late-home").toString());
				            readJar("GENERATED_JAR_LATE_HOME", project.resolve("late-home/.m2/repository/lib.jar"));
				            readJar("GENERATED_JAR_START_HOME", project.resolve("start-home/.m2/repository/lib.jar"));
				            return;
				        }
				        String original = System.getProperty("java.home");
				        boolean poisonHome = arguments[1].equals("snapshot");
				        System.out.println("GENERATED_ARMED");
				        System.out.println(poisonHome ? "GENERATED_PROPERTY_CHANGE" : "GENERATED_REAL_HOME_CONTROL");
				        try {
				            if (poisonHome) {
				                System.setProperty("java.home", file.getParent().toString());
				            }
				            if (arguments[1].equals("boundary")) {
				                de.tum.cit.ase.ares.integration.jce.JceRuntimeContract.verify(file.getParent(), "example.jce.ares");
				                return;
				            }
				            if (arguments[1].equals("crypto")) {
				                java.security.SecureRandom random = new FixedRandom();
				                System.out.println("GENERATED_FIRST_CRYPTO");
				                if (example.jce.JceCryptoSubject.initialiseCrypto(random) < 128
				                        || example.jce.JceCryptoSubject.initialiseCrypto(random) < 128) {
				                    throw new AssertionError("AES is unavailable");
				                }
				                String permitted = example.jce.JceCryptoSubject.read(java.nio.file.Path.of(arguments[2]));
				                if (!permitted.equals("permitted fixture")) {
				                    throw new AssertionError(permitted);
				                }
				                System.out.println("GENERATED_PERMITTED_READ");
				            }
				            String contents = example.jce.JceCryptoSubject.read(file);
				            System.out.println("GENERATED_READ_RETURNED: " + contents);
				        } catch (SecurityException denied) {
				            if (!denied.getMessage().contains(file.toString()) && !denied.getMessage().equals(example.jce.ares.api.localization.Messages.localized("security.advice.startup.properties.unavailable"))) {
				                throw new AssertionError("The denial did not identify the attempted file", denied);
				            }
				            System.out.println("GENERATED_READ_DENIED: " + denied.getMessage());
				        } finally {
				            System.setProperty("java.home", original);
				        }
				    }
				    /** Names a later Maven repository and home, then reads a jar from each and from the start-up one. */
				    private static void jarRead(java.nio.file.Path project) throws Exception {
				        System.setProperty("maven.repo.local", project.resolve("late-repo").toString());
				        System.setProperty("user.home", project.resolve("late-home").toString());
				        readJar("GENERATED_JAR_LATE_REPOSITORY", project.resolve("late-repo/lib.jar"));
				        readJar("GENERATED_JAR_LATE_HOME", project.resolve("late-home/.m2/repository/lib.jar"));
				        readJar("GENERATED_JAR_START", project.resolve("repo/lib.jar"));
				    }
				    /** Prints what a student read of one jar returned, or that it was denied. */
				    private static void readJar(String label, java.nio.file.Path jar) throws Exception {
				        try {
				            System.out.println(label + "_RETURNED=" + example.jce.JceCryptoSubject.read(jar));
				        } catch (SecurityException denied) {
				            System.out.println(label + "_DENIED=" + denied.getMessage());
				        }
				    }
				    /** Creates temporary files through the student fixture and reports where they went. */
				    private static void temp(String operation, java.nio.file.Path project, java.nio.file.Path forbidden) throws Exception {
				        String startUp = System.getProperty("java.io.tmpdir");
				        System.out.println("GENERATED_START_TEMP=" + java.nio.file.Path.of(startUp).toRealPath());
				        System.out.println("GENERATED_JDK=" + Runtime.version().feature());
				        switch (operation) {
				        case "tempDefault" -> {
				            report("GENERATED_TEMP_IO", () -> example.jce.JceTempFileSubject.createWithFile(null).toPath());
				            report("GENERATED_TEMP_IO_NULL", () -> example.jce.JceTempFileSubject.createWithFileAndNullDirectory().toPath());
				            report("GENERATED_TEMP_NIO", () -> example.jce.JceTempFileSubject.createWithFiles(null));
				        }
				        case "tempProvider" -> {
				            example.jce.JceRandomSpi.forbidden = forbidden;
				            example.jce.JceRandomSpi.permitted = project.resolve("allowed.txt");
				            java.security.Security.insertProviderAt(new example.jce.JceRandomProvider(), 1);
				            report("GENERATED_TEMP_IO", () -> example.jce.JceTempFileSubject.createWithFile(null).toPath());
				        }
				        case "tempHolder" -> {
				            java.nio.file.Path changed = java.nio.file.Files.createDirectories(project.resolve("changed-temp"));
				            System.setProperty("java.io.tmpdir", changed.toString());
				            try {
				                report("GENERATED_TEMP_EXPLICIT", () -> example.jce.JceTempFileSubject.createWithFile(new java.io.File(startUp)).toPath());
				            } finally {
				                System.setProperty("java.io.tmpdir", startUp);
				            }
				            report("GENERATED_TEMP_IO", () -> example.jce.JceTempFileSubject.createWithFile(null).toPath());
				            System.out.println("GENERATED_CHANGED_FILES=" + count(changed));
				        }
				        case "tempLink" -> {
				            report("GENERATED_TEMP_NIO", () -> example.jce.JceTempFileSubject.createWithFiles(null));
				            java.nio.file.Path link = java.nio.file.Path.of(startUp);
				            java.nio.file.Path redirected = java.nio.file.Files.createDirectories(project.resolve("redirected-temp"));
				            java.nio.file.Files.delete(link);
				            java.nio.file.Files.createSymbolicLink(link, redirected);
				            report("GENERATED_TEMP_REDIRECTED", () -> example.jce.JceTempFileSubject.createWithFiles(null));
				            System.out.println("GENERATED_REDIRECTED_FILES=" + count(redirected));
				        }
				        case "tempExplicit" -> {
				            java.nio.file.Path allowed = project.resolve("allowed-temp");
				            report("GENERATED_TEMP_ALLOWED_IO", () -> example.jce.JceTempFileSubject.createWithFile(allowed.toFile()).toPath());
				            report("GENERATED_TEMP_ALLOWED_NIO", () -> example.jce.JceTempFileSubject.createWithFiles(allowed));
				            report("GENERATED_TEMP_IO", () -> example.jce.JceTempFileSubject.createWithFile(null).toPath());
				            report("GENERATED_TEMP_NIO", () -> example.jce.JceTempFileSubject.createWithFiles(null));
				        }
				        default -> throw new IllegalArgumentException(operation);
				        }
				    }
				    /** Prints where a created file went, or the denial, and removes the file again. */
				    private static void report(String label, java.util.concurrent.Callable<java.nio.file.Path> creation) throws Exception {
				        try {
				            java.nio.file.Path created = creation.call();
				            System.out.println(label + "_CREATED=" + created.getParent().toRealPath());
				            java.nio.file.Files.delete(created);
				        } catch (SecurityException denied) {
				            System.out.println(label + "_DENIED=" + denied.getMessage());
				            System.out.println(label + "_CAUSE=" + (denied.getCause() == null ? "none" : denied.getCause().getClass().getName()));
				        }
				    }
				    /** Counts the entries of a directory. */
				    private static long count(java.nio.file.Path directory) throws Exception {
				        try (var entries = java.nio.file.Files.list(directory)) {
				            return entries.count();
				        }
				    }
				    /** Supplies test entropy without granting access to an entropy device. */
				    private static final class FixedRandom extends java.security.SecureRandom {
				        /** Preserves the serial form required by SecureRandom. */
				        private static final long serialVersionUID = 1L;
				        /** Fills the bytes with deterministic test input. */
				        @Override
				        public void nextBytes(byte[] bytes) {
				            java.util.Arrays.fill(bytes, (byte) 1);
				        }
				    }
				}
				""";
	}
}
