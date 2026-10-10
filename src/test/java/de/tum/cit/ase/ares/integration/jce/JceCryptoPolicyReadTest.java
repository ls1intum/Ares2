package de.tum.cit.ase.ares.integration.jce;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.management.ManagementFactory;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import de.tum.cit.ase.ares.api.policy.policySubComponents.ProgrammingLanguageConfiguration;

/**
 * Runs prerequisite proofs in fresh JVMs with the real agent and woven subject.
 */
class JceCryptoPolicyReadTest {

	/**
	 * Distinguishes restrictive discovery before crypto from an active runtime
	 * verdict.
	 */
	@Test
	void restrictiveDefaultStopsBeforeColdCryptoEntry(@TempDir Path directory) throws Exception {
		String output = runProbe(ProgrammingLanguageConfiguration.JAVA_USING_MAVEN_ARCHUNIT_AND_ASPECTJ, "default",
				directory);
		assertTrue(output.contains("JCE_RESTRICTIVE_DEFAULT_DENIED_BEFORE_CRYPTO"), summary(output));
		assertEquals(-1, initialisationIndex(output, "javax/crypto/JceSecurity"), summary(output));
		assertTrue(!output.contains("JCE_DEFAULT_CRYPTO_ENTRY"), summary(output));
	}

	/**
	 * Proves the production startup captured its home before the first student
	 * read.
	 */
	@ParameterizedTest
	@EnumSource(ProgrammingLanguageConfiguration.class)
	void javaHomeChangeBeforeFirstAdviceDoesNotAuthoriseStudentRead(ProgrammingLanguageConfiguration configuration,
			@TempDir Path directory) throws Exception {
		String output = runProbe(configuration, "snapshot", directory);
		String aspect = configuration.name().endsWith("ASPECTJ") ? "JavaAspectJFileSystemAdviceDefinitions"
				: "JavaInstrumentationAdviceFileSystemToolbox";
		assertTrue(initialisationBefore(output, aspect, "JCE_PROBE_PROPERTY_CHANGE"), summary(output));
		assertTrue(output.contains("JCE_PROBE_READ_DENIED"), summary(output));
	}

	/**
	 * Requires jurisdiction-policy initialisation after enforcement becomes active.
	 */
	@ParameterizedTest
	@EnumSource(ProgrammingLanguageConfiguration.class)
	void firstCryptoUseAfterEnforcementSucceeds(ProgrammingLanguageConfiguration configuration, @TempDir Path directory)
			throws Exception {
		String output = runProbe(configuration, "crypto", directory);
		int initialisation = initialisationIndex(output, "javax/crypto/JceSecurity");
		assertTrue(initialisation > output.indexOf("JCE_PROBE_FIRST_CRYPTO"), summary(output));
		String backend = configuration.name().endsWith("ASPECTJ") ? "JavaAspectJFileSystemAdviceDefinitions"
				: "JavaInstrumentationAdviceFileSystemToolbox";
		assertTrue(output.contains(backend + " STUDENT"), summary(output));
		assertEquals(configuration.name().endsWith("INSTRUMENTATION"), output.contains(backend + " JDK_JCE"),
				summary(output));
		assertTrue(output.contains("JCE_PROBE_READ_DENIED"), summary(output));
	}

	/**
	 * Runs real read, glob, mutation, provider and class-identity controls in each
	 * configuration.
	 */
	@ParameterizedTest
	@EnumSource(ProgrammingLanguageConfiguration.class)
	void studentOperationsRetainTheirFilePermissions(ProgrammingLanguageConfiguration configuration,
			@TempDir Path directory) throws Exception {
		String output = runProbe(configuration, "boundary", directory);
		assertTrue(output.contains("JCE_BOUNDARY_PASSED"), summary(output));
		assertTrue(output.contains("JCE_PROVIDER_CALLBACK_DENIED_AND_PERMITTED"), summary(output));
		assertTrue(output.contains("JCE_CLASS_IDENTITY_DENIED_AND_PERMITTED"), summary(output));
	}

	/**
	 * Runs Jupiter's actual activation, restrictive discovery and failure cleanup
	 * in each configuration.
	 */
	@ParameterizedTest
	@EnumSource(ProgrammingLanguageConfiguration.class)
	void policyLifecycleRestoresTheBoundary(ProgrammingLanguageConfiguration configuration, @TempDir Path directory)
			throws Exception {
		String output = runProbe(configuration, "lifecycle", directory);
		assertTrue(output.contains("JCE_LIFECYCLE_PASSED"), summary(output));
		assertTrue(
				initialisationIndex(output, "javax/crypto/JceSecurity") > output.indexOf("JCE_LIFECYCLE_FIRST_CRYPTO"),
				summary(output));
	}

	/**
	 * Proves that student property changes still trigger the static environment
	 * ban.
	 */
	@ParameterizedTest
	@EnumSource(ProgrammingLanguageConfiguration.class)
	void studentJavaHomeChangeRemainsForbidden(ProgrammingLanguageConfiguration configuration, @TempDir Path directory)
			throws Exception {
		String output = runProbe(configuration, "environment", directory);
		assertTrue(output.contains("JCE_ENVIRONMENT_ACCESS_DENIED"), summary(output));
	}

	/**
	 * Writes fixtures in the parent, then launches the child with the current test
	 * JVM's agent.
	 */
	private static String runProbe(ProgrammingLanguageConfiguration configuration, String operation, Path directory)
			throws Exception {
		Path compiled = prepareProject(configuration, directory, "environment".equals(operation));
		Path file = directory.resolve("default_local.policy");
		Files.writeString(file, "protected fixture");
		Path policy = directory.resolve("policy.yaml");
		String policyContents = policy(configuration, directory.resolve("allowed.txt"));
		if ("environment".equals(operation)) {
			policyContents = policyContents.replace("example.jce", "example.environment").replace("JceCryptoSubject",
					"JceEnvironmentSubject");
		}
		Files.writeString(policy, policyContents);
		Files.writeString(directory.resolve("allowed.txt"), "permitted fixture");
		Path log = directory.resolve(operation + ".log");
		Process child = new ProcessBuilder(command(operation, policy, file, directory, compiled))
				.directory(directory.toFile()).redirectErrorStream(true).redirectOutput(log.toFile()).start();
		try {
			assertTrue(child.waitFor(90, TimeUnit.SECONDS), "The isolated JCE proof timed out: " + log);
			String output = Files.readString(log);
			Path evidence = Path.of("target", "jce-proofs", configuration + "-" + operation + ".log");
			Files.createDirectories(evidence.getParent());
			Files.writeString(evidence, output);
			assertEquals(0, child.exitValue(), summary(output));
			assertTrue(output.contains("JCE_PROBE_PASSED"), summary(output));
			return output;
		} finally {
			child.destroyForcibly();
		}
	}

	/**
	 * Preserves the actual agent, bootstrap runtime and module access from the
	 * parent.
	 */
	private static List<String> command(String operation, Path policy, Path file, Path project, Path compiled)
			throws Exception {
		List<String> command = new ArrayList<>();
		command.add(System.getProperty("ares.jce.java",
				Path.of(System.getProperty("java.home"), "bin", "java").toString()));
		ManagementFactory.getRuntimeMXBean().getInputArguments().stream()
				.filter(argument -> argument.startsWith("-javaagent:") || argument.startsWith("-Xbootclasspath/a:")
						|| argument.startsWith("--add-opens=") || argument.startsWith("--add-exports="))
				.forEach(command::add);
		command.add("-Djava.io.tmpdir=" + Files.createDirectories(project.resolve("tmp")));
		command.add("-javaagent:" + JceTraceAgent.packageAgent());
		command.add("-Xlog:class+init=info");
		command.add("-cp");
		command.add(compiled + java.io.File.pathSeparator
				+ System.getProperty("surefire.test.class.path", System.getProperty("java.class.path")));
		command.add(JceForkProbe.class.getName());
		command.add(operation);
		command.add(policy.toAbsolutePath().toString());
		command.add(file.toAbsolutePath().toString());
		command.add(project.toAbsolutePath().toString());
		command.add(compiled.resolve("environment".equals(operation) ? "example/environment" : "example/jce")
				.toAbsolutePath().toString());
		return command;
	}

	/**
	 * Prepares the selected build layout with the real compiled student fixture.
	 */
	private static Path prepareProject(ProgrammingLanguageConfiguration configuration, Path project,
			boolean environment) throws Exception {
		boolean gradle = configuration.name().contains("GRADLE");
		Files.writeString(project.resolve(gradle ? "build.gradle" : "pom.xml"),
				gradle ? "plugins { id 'java' }" : "<project/>");
		if (gradle) {
			Files.writeString(project.resolve("settings.gradle"), "rootProject.name = 'jce-proof'");
		}
		Path sources = Files.createDirectories(
				project.resolve(environment ? "src/main/java/example/environment" : "src/main/java/example/jce"));
		Path classes = Files.createDirectories(project.resolve(gradle ? "build/classes/java/main" : "target/classes"));
		Class<?>[] subjects = environment ? new Class<?>[] { example.environment.JceEnvironmentSubject.class }
				: new Class<?>[] { example.jce.JceCryptoSubject.class, example.jce.JceProviderService.class };
		for (Class<?> subject : subjects) {
			String name = subject.getSimpleName();
			Files.writeString(sources.resolve(name + ".java"), Files
					.readString(Path.of("src/test/java/example", environment ? "environment" : "jce", name + ".java")));
			Path destination = classes.resolve("example/" + (environment ? "environment/" : "jce/") + name + ".class");
			Files.createDirectories(destination.getParent());
			try (var input = subject.getResourceAsStream(name + ".class")) {
				Files.copy(java.util.Objects.requireNonNull(input), destination);
			}
		}
		return classes;
	}

	/**
	 * Produces a real policy whose unrelated allowance leaves filesystem checks to
	 * AOP.
	 */
	private static String policy(ProgrammingLanguageConfiguration configuration, Path allowed) {
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
				""".formatted(configuration, allowed.toAbsolutePath().toString().replace("\\", "/"));
	}

	/**
	 * Finds a class-init event rather than the JVM's earlier verification event.
	 */
	private static int initialisationIndex(String output, String className) {
		return output.indexOf("Initializing '" + className + "'");
	}

	/**
	 * Keeps failures readable while full JVM evidence remains under
	 * target/jce-proofs.
	 */
	private static String summary(String output) {
		return output.lines()
				.filter(line -> line.contains("JCE_") || line.contains("JceSecurity")
						|| line.contains("FileSystemAdviceDefinitions") || line.contains("AdviceFileSystemToolbox")
						|| line.contains("Exception") || line.startsWith("\tat "))
				.collect(java.util.stream.Collectors.joining(System.lineSeparator()));
	}

	/**
	 * Requires a recorded class initialisation strictly before the trusted marker.
	 */
	private static boolean initialisationBefore(String output, String className, String marker) {
		int initialisation = output.lines().filter(line -> line.contains("Initializing '") && line.contains(className))
				.mapToInt(output::indexOf).findFirst().orElse(-1);
		return initialisation >= 0 && initialisation < output.indexOf(marker);
	}
}
