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

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import de.tum.cit.ase.ares.api.policy.SecurityPolicyReaderAndDirector;
import de.tum.cit.ase.ares.api.policy.policySubComponents.ProgrammingLanguageConfiguration;
import de.tum.cit.ase.ares.integration.jce.JceTraceAgent;

/**
 * Exercises the generated AspectJ startup boundary before exemptions can be
 * removed.
 */
class JceGeneratedExerciseTest {

	/**
	 * Generates and compiles actual advice, then tries poisoning its home before
	 * first use.
	 */
	@ParameterizedTest
	@EnumSource(ProgrammingLanguageConfiguration.class)
	void generatedExerciseJavaHomeChangeBeforeFirstAdviceDoesNotAuthoriseStudentRead(
			ProgrammingLanguageConfiguration configuration) throws Exception {
		Path project = generate(configuration);
		Path classes = project.resolve("compiled");
		String control = launch(classes, project, "control");
		assertTrue(control.contains("GENERATED_READ_DENIED"), control);
		String output = launch(classes, project, "snapshot");
		assertFalse(output.contains("GENERATED_READ_RETURNED"), output);
		assertTrue(output.contains("GENERATED_READ_DENIED"), output);
		String backend = configuration.name().endsWith("ASPECTJ")
				? "example/jce/ares/api/aop/java/aspectj/adviceandpointcut/JavaAspectJFileSystemAdviceDefinitions"
				: "example/jce/ares/api/aop/java/instrumentation/advice/JavaInstrumentationAdviceFileSystemToolbox";
		int initialisation = output.indexOf("Initializing '" + backend + "'");
		assertTrue(initialisation >= 0 && initialisation < output.indexOf("GENERATED_PROPERTY_CHANGE"), output);
	}

	/** Requires real generated JCE setup after the enforcement startup marker. */
	@ParameterizedTest
	@EnumSource(ProgrammingLanguageConfiguration.class)
	void generatedExerciseAllowsJcePolicyReads(ProgrammingLanguageConfiguration configuration) throws Exception {
		Path project = generate(configuration);
		String output = launch(project.resolve("compiled"), project, "crypto");
		int initialisation = output.indexOf("Initializing 'javax/crypto/JceSecurity'");
		assertTrue(initialisation > output.indexOf("GENERATED_FIRST_CRYPTO"), output);
		assertTrue(output.contains("GENERATED_PERMITTED_READ"), output);
		assertTrue(output.contains("GENERATED_READ_DENIED"), output);
		String backend = configuration.name().endsWith("ASPECTJ") ? "JavaAspectJFileSystemAdviceDefinitions"
				: "JavaInstrumentationAdviceFileSystemToolbox";
		assertTrue(output.contains("JCE_BACKEND_ENTRY example.jce.ares.api.aop.java.")
				&& output.contains(backend + " STUDENT"), output);
		assertEquals(configuration.name().endsWith("INSTRUMENTATION"), output.contains(backend + " JDK_JCE"), output);
	}

	/**
	 * Verifies actual file operations and matching controls in the copied backends.
	 */
	@ParameterizedTest
	@EnumSource(ProgrammingLanguageConfiguration.class)
	void generatedExerciseRetainsFilePermissions(ProgrammingLanguageConfiguration configuration) throws Exception {
		Path project = generate(configuration);
		String output = launch(project.resolve("compiled"), project, "boundary");
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
		createAgent(sources, project.resolve("compiled"), project.resolve("agent.jar"));
		assertTrue(launch(project.resolve("compiled"), project, "boundary").contains("JCE_BOUNDARY_PASSED"));
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
	 * A copied AspectJ exercise fails closed when its trusted startup hook is
	 * omitted.
	 */
	@ParameterizedTest
	@EnumSource(value = ProgrammingLanguageConfiguration.class, names = ".*ASPECTJ", mode = EnumSource.Mode.MATCH_ALL)
	void generatedAspectJRequiresTrustedStartup(ProgrammingLanguageConfiguration configuration) throws Exception {
		Path project = generate(configuration);
		String output = launch(project.resolve("compiled"), project, "control", false);
		assertTrue(output.contains(
				de.tum.cit.ase.ares.api.localization.Messages.localized("security.advice.trusted.startup.missing")),
				output);
		assertFalse(output.contains("GENERATED_READ_RETURNED"), output);
	}

	/** Emits and compiles a fresh project through the real generation pipeline. */
	static Path generate(ProgrammingLanguageConfiguration configuration) throws Exception {
		Path project = Files.createTempDirectory(Path.of("target").toAbsolutePath(), "jce-generated-capture-");
		Path testSources = Files.createDirectories(project.resolve("src/test/java"));
		Files.createDirectories(project.resolve("src/main/java"));
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
		createAgent(testSources, classes, project.resolve("agent.jar"));
		Files.writeString(project.resolve("allowed.txt"), "permitted fixture");
		return project;
	}

	/**
	 * Copies the student fixture's source so the generated aspects weave its real
	 * call.
	 */
	private static void copySubject(Path sources) throws Exception {
		for (String name : new String[] { "JceCryptoSubject", "JceProviderService" }) {
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
	 * its aspect.
	 */
	private static String launch(Path classes, Path project, String operation) throws Exception {
		return launch(classes, project, operation, true);
	}

	/**
	 * Runs the actual copied classes with or without their required startup hook.
	 */
	private static String launch(Path classes, Path project, String operation, boolean includeAgent) throws Exception {
		Path fakeHome = Files.createDirectories(project.resolve("fake-home"));
		Path file = fakeHome.resolve("default_local.policy");
		Files.writeString(file, "protected fixture");
		Path log = project.resolve(operation + ".log");
		List<String> command = new ArrayList<>();
		command.add(javaExecutable());
		ManagementFactory.getRuntimeMXBean().getInputArguments().stream()
				.filter(argument -> argument.startsWith("-Xbootclasspath/a:") || argument.startsWith("--add-opens=")
						|| argument.startsWith("--add-exports="))
				.forEach(command::add);
		if (includeAgent) {
			command.add("-javaagent:" + project.resolve("agent.jar"));
		}
		command.add("-javaagent:" + JceTraceAgent.packageAgent());
		command.addAll(List.of("-Xlog:class+init=info", "-cp",
				classes + java.io.File.pathSeparator + project.resolve("src/test/resources")
						+ java.io.File.pathSeparator + classpath(),
				"fixture.GeneratedCaptureLauncher", file.toString(), operation,
				project.resolve("allowed.txt").toString()));
		requireSuccess(command, log);
		return Files.readString(log);
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

	/** Defines a narrow filesystem permission in the real precompile policy. */
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
				""".formatted(configuration, project.resolve("allowed.txt").toString());
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
				    /** Tries the first student read against a fake JDK home. */
				    public static void main(String[] arguments) throws Exception {
				        java.nio.file.Path file = java.nio.file.Path.of(arguments[0]);
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
				            if (!denied.getMessage().contains(file.toString()) && !denied.getMessage().equals(example.jce.ares.api.localization.Messages.localized("security.advice.trusted.startup.missing"))) {
				                throw new AssertionError("The denial did not identify the attempted file", denied);
				            }
				            System.out.println("GENERATED_READ_DENIED: " + denied.getMessage());
				        } finally {
				            System.setProperty("java.home", original);
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
