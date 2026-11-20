package de.tum.cit.ase.ares.integration.precompile;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.lang.reflect.Field;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.tools.ToolProvider;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.AnnotatedElementContext;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.api.io.TempDirFactory;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import de.tum.cit.ase.ares.api.Main;
import de.tum.cit.ase.ares.api.policy.SecurityPolicyReaderAndDirector;
import de.tum.cit.ase.ares.api.policy.policySubComponents.TestBehaviorConfiguration;
import de.tum.cit.ase.ares.testutilities.GeneratedCodeRuns;

/**
 * Runs the precompile flow, which writes the security-test scaffold into an
 * exercise, and checks what it writes.
 */
public class PrecompileTest {

	/**
	 * Package of the made-up exercise the precompile tests write into. It must not
	 * contain {@code de.tum.cit.ase}, which the copy step rewrites.
	 */
	private static final String EXERCISE_PACKAGE = "de.tum.cit.aet.exercise";

	/**
	 * Precompile without a policy is refused before anything is written. Ares would
	 * derive the decoy package here, which holds most classes.
	 * <p>
	 * The project is compared byte by byte before and after, with files already
	 * where generated files would go, so an overwrite is caught too.
	 */
	@Test
	void precompileWithoutAPolicyIsRefusedAndChangesNothing(@TempDir Path tempDir) throws IOException {
		Path projectFolderPath = Files.createDirectory(tempDir.resolve("project"));
		Files.writeString(projectFolderPath.resolve("pom.xml"), "<project/>");
		Path sources = Files.createDirectories(projectFolderPath.resolve("src/main/java"));
		Files.createDirectories(sources.resolve("de/tum/cit/aet/exercise"));
		Files.writeString(sources.resolve("de/tum/cit/aet/exercise/Main.java"),
				"package de.tum.cit.aet.exercise; public class Main { }");
		Files.createDirectories(sources.resolve("decoy"));
		for (String name : List.of("First", "Second")) {
			Files.writeString(sources.resolve("decoy/" + name + ".java"),
					"package decoy; public class " + name + " { }");
		}
		Files.createDirectories(projectFolderPath.resolve("target/classes"));
		Path writeTarget = Files.createDirectories(projectFolderPath.resolve("src/test/java"));
		Files.writeString(
				Files.createDirectories(writeTarget.resolve("decoy/ares/api/aop/java"))
						.resolve("JavaAOPTestCaseSettings.java"),
				"package decoy.ares.api.aop.java; class JavaAOPTestCaseSettings { }");
		Files.writeString(
				Files.createDirectories(projectFolderPath.resolve("src/test/resources")).resolve("sentinel.properties"),
				"sentinel resources");
		Files.writeString(projectFolderPath.resolve("PhobosCopyTool.sh"), "sentinel phobos");
		Map<Path, String> before = contentsOf(tempDir);

		SecurityException refusal = assertThrows(SecurityException.class, () -> SecurityPolicyReaderAndDirector
				.builder().projectFolderPath(projectFolderPath).build().createTestCases().writeTestCases(writeTarget));

		assertTrue(refusal.getMessage().contains("\"decoy\""), refusal.getMessage());
		assertTrue(refusal.getMessage().contains("theSupervisedCodeUsesTheFollowingPackage"), refusal.getMessage());
		assertEquals(before, contentsOf(tempDir), "a refused precompile changed the project");
	}

	/**
	 * Proves the precompile carry-forward claim on the write side: a policy
	 * configuring {@code regardingTheFailureFeedbackForTheTestCases} survives
	 * {@code createTestCases().writeTestCases(...)} as a real, separately compiled
	 * settings class, not merely that generation completes without throwing.
	 */
	@Test
	void writeTestCasesGeneratesTheTestBehaviorSettingsClassWhenConfigured(@TempDir Path tempDir) throws Exception {
		Path projectFolderPath = Files.createDirectory(tempDir.resolve("project"));
		Files.writeString(projectFolderPath.resolve("pom.xml"), "<project/>");
		Files.createDirectories(projectFolderPath.resolve("src/main/java"));
		Files.createDirectories(projectFolderPath.resolve("src/test/java"));
		Files.createDirectories(projectFolderPath.resolve("target/classes"));
		Path policyFile = tempDir.resolve("SecurityPolicy.yaml");
		Files.writeString(policyFile, """
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
				      onlyPrivilegedExceptionsAreReported: true
				      theFailureMessageForOtherExceptionsIs: "Precompiled default message"
				""");
		Path writeTarget = projectFolderPath.resolve("src/test/java");

		List<Path> written = SecurityPolicyReaderAndDirector.builder().securityPolicyFilePath(policyFile)
				.projectFolderPath(projectFolderPath).build().createTestCases().writeTestCases(writeTarget);

		String simpleClassName = TestBehaviorConfiguration.GENERATED_CLASS_NAME
				.substring(TestBehaviorConfiguration.GENERATED_CLASS_NAME.lastIndexOf('.') + 1);
		Path generatedSettingsClass = written.stream()
				.filter(path -> path.getFileName().toString().equals(simpleClassName + ".java")).findFirst()
				.orElseThrow(() -> new AssertionError("No generated test-behaviour settings class among: " + written));
		assertEquals(writeTarget.resolve(TestBehaviorConfiguration.GENERATED_CLASS_NAME.replace('.', '/') + ".java"),
				generatedSettingsClass,
				"the settings class must land under src/test/java, compiled alongside the rest of the generated sources");
		assertTrue(Files.exists(generatedSettingsClass));

		Path compiledSettings = Files.createDirectories(tempDir.resolve("compiled-settings"));
		GeneratedCodeRuns.compile(compiledSettings, List.of(), generatedSettingsClass);
		ClassLoader compiled = new URLClassLoader(GeneratedCodeRuns.classPath(List.of(compiledSettings)),
				Thread.currentThread().getContextClassLoader());
		Class<?> settingsClass = Class.forName(TestBehaviorConfiguration.GENERATED_CLASS_NAME, true, compiled);
		Field enabledField = settingsClass
				.getField(TestBehaviorConfiguration.FAILURE_FEEDBACK_PRIVILEGED_ONLY_FIELD_NAME);
		Field messageField = settingsClass.getField(TestBehaviorConfiguration.FAILURE_FEEDBACK_MESSAGE_FIELD_NAME);
		assertEquals(true, enabledField.get(null));
		assertEquals("Precompiled default message", messageField.get(null));
		assertTrue(Files.exists(writeTarget.resolve("de/tum/cit/ase/ares/generated/GeneratedFailureReporting.java")),
				"the failure-reporting hook must be generated beside the settings class");
		Path resources = projectFolderPath.resolve("src/test/resources");
		assertTrue(Files.exists(resources.resolve("com/example/ares/api/localization/messages.properties")),
				"the copied Messages looks for its bundle below the exercise package");
		assertTrue(Files.notExists(resources.resolve("ares/api/localization/messages.properties")),
				"no bundle may land outside the exercise package, where the copied Messages never looks");
	}

	/**
	 * Precompile carries the secure baseline's temp-file rules into an exercise:
	 * the copied deny list still names the two calls with a directory and not the
	 * two without one, the copied ArchUnit check lets the latter through, the
	 * copied settings and agent carry the start-up freeze, and the copied aspect
	 * reads the JVM's start-up values itself, with no reference to the agent.
	 */
	@ParameterizedTest
	@CsvSource({ "ARCHUNIT,INSTRUMENTATION", "ARCHUNIT,ASPECTJ", "WALA,INSTRUMENTATION", "WALA,ASPECTJ" })
	void precompileCarriesTheTempFileRulesAndTheFreeze(String analysis, String aop, @TempDir Path tempDir)
			throws IOException {
		Path projectFolderPath = Files.createDirectory(tempDir.resolve("project"));
		Files.writeString(projectFolderPath.resolve("pom.xml"), "<project/>");
		Files.createDirectories(projectFolderPath.resolve("src/main/java"));
		Files.createDirectories(projectFolderPath.resolve("target/classes"));
		Path writeTarget = Files.createDirectories(projectFolderPath.resolve("src/test/java"));
		Path policy = Files.writeString(tempDir.resolve("policy.yaml"), policyWithOneReadPath(analysis, aop));

		SecurityPolicyReaderAndDirector.builder().securityPolicyFilePath(policy).projectFolderPath(projectFolderPath)
				.build().createTestCases().writeTestCases(writeTarget);

		Path generated = writeTarget.resolve(EXERCISE_PACKAGE.replace('.', '/')).resolve("ares/api");
		assertContains(generated.resolve("aop/java/JavaAOPTestCaseSettings.java"),
				"restrictedPackage = \"" + EXERCISE_PACKAGE + "\"");
		String everyGeneratedSource = contentsOf(generated).values().stream().collect(Collectors.joining("\n"));
		assertTrue(everyGeneratedSource.contains("JavaArchunitSupervisedClasses.pinned(\"" + EXERCISE_PACKAGE + "\")"),
				"the generated architecture tests do not analyse the pinned package");
		assertFalse(everyGeneratedSource.contains("JavaArchunitSupervisedClasses.validated("),
				"a pinned package was generated as though it had been derived");
		String denyList = Files.readString(generated.resolve(
				"templates/architecture/java/" + analysis.toLowerCase() + "/methods/file-system-access-methods.txt"));
		for (String withDirectory : List.of("java.io.File.createTempFile(", "java.nio.file.Files.createTempFile(")) {
			assertTrue(denyList.lines().anyMatch(line -> line.startsWith(withDirectory)),
					() -> withDirectory + " with a directory is missing from the copied deny list");
		}
		assertFalse(denyList.lines().anyMatch(line -> line.matches(
				"java\\.(io\\.File|nio\\.file\\.Files)\\.createTempFile\\((java\\.lang\\.String|Ljava/lang/String;), ?(java\\.lang\\.String|Ljava/lang/String;)(, ?java\\.nio\\.file\\.attribute\\.FileAttribute\\[\\]|\\[Ljava/nio/file/attribute/FileAttribute;)?\\)")),
				"a createTempFile call without a directory is still on the copied deny list");
		if ("ARCHUNIT".equals(analysis)) {
			assertContains(generated.resolve("architecture/java/archunit/TransitivelyAccessesMethodsCondition.java"),
					"STANDARD_ALLOWED_CALLS");
		} else {
			assertContains(generated.resolve("architecture/java/wala/CustomCallgraphBuilder.java"),
					"isStandardAllowed");
			assertContains(generated.resolve("architecture/java/wala/WalaRule.java"), "isStandardAllowed");
		}
		assertContains(generated.resolve("aop/java/JavaAOPTestCaseSettings.java"), "frozenDefaultTempDirectory");
		if ("INSTRUMENTATION".equals(aop)) {
			assertContains(generated.resolve("aop/java/instrumentation/JavaInstrumentationAgent.java"),
					"captureTrustedStartupValues();");
			assertDoesNotContain(generated.resolve("aop/java/instrumentation/JavaInstrumentationAgent.java"),
					"JavaAspectJFileSystemAdviceDefinitions");
			assertContains(generated.resolve("aop/java/instrumentation/RecordMethodsPassThrough.java"),
					"java/lang/runtime/ObjectMethods");
			assertDoesNotContain(
					generated
							.resolve("aop/java/instrumentation/advice/JavaInstrumentationAdviceFileSystemToolbox.java"),
					"INTERNAL_PATH_SUFFIXES");
		} else {
			Path aspect = generated
					.resolve("aop/java/aspectj/adviceandpointcut/JavaAspectJFileSystemAdviceDefinitions.aj");
			assertContains(aspect, "getSavedProperties");
			assertContains(aspect, "TRUSTED_DEFAULT_TEMP_DIRECTORY");
			assertDoesNotContain(aspect, "JavaInstrumentationAgent");
			assertDoesNotContain(aspect, "INTERNAL_PATH_SUFFIXES");
			assertFalse(Files.exists(generated.resolve("aop/java/instrumentation/JavaInstrumentationAgent.java")),
					"an AspectJ exercise was given the instrumentation agent");
		}
	}

	/**
	 * Precompile writes the exempted names into the generated settings: the test
	 * classes plus the classes their own compiled files list as declared inside
	 * them in the project's own test output, never a class merely named like one.
	 * Essential packages go into their own setting, and the settings that switch
	 * enforcement on come after every allow-list.
	 */
	@ParameterizedTest
	@ValueSource(strings = { "ASPECTJ", "INSTRUMENTATION" })
	void precompileWritesTheTrustedNestAndKeepsPackagesApart(String aop, @TempDir Path tempDir) throws IOException {
		Path projectFolderPath = projectWithCompiledNestHost(tempDir);
		Path writeTarget = projectFolderPath.resolve("src/test/java");
		Path policy = Files.writeString(tempDir.resolve("policy.yaml"),
				policyWithOneReadPath("ARCHUNIT", aop).replace("theFollowingClassesAreTestClasses: [ ]",
						"theFollowingClassesAreTestClasses: [ \"example.nest.NestHostFixture\" ]"));

		SecurityPolicyReaderAndDirector.builder().securityPolicyFilePath(policy).projectFolderPath(projectFolderPath)
				.build().createTestCases().writeTestCases(writeTarget);

		String settings = Files.readString(writeTarget.resolve(EXERCISE_PACKAGE.replace('.', '/'))
				.resolve("ares/api/aop/java/JavaAOPTestCaseSettings.java"));
		String classes = declarationOf(settings, "allowedListedClasses");
		String packages = declarationOf(settings, "allowedListedPackages");
		assertTrue(classes.contains("\"example.nest.NestHostFixture\""), classes);
		for (Class<?> member : example.nest.NestHostFixture.class.getNestMembers()) {
			assertTrue(classes.contains("\"" + member.getName() + "\""), member.getName() + " in " + classes);
		}
		assertFalse(classes.contains("NestHostFixture$Evil"), classes);
		assertTrue(packages.contains("\"java.lang\""), packages);
		assertFalse(classes.contains("\"java.lang\""), classes);
		int lastAllowList = Math.max(settings.indexOf(classes), settings.indexOf(packages));
		assertTrue(settings.indexOf(declarationOf(settings, "aopMode")) > lastAllowList, settings);
		assertTrue(settings.indexOf(declarationOf(settings, "restrictedPackage")) > lastAllowList, settings);
	}

	/**
	 * Reads every file below a directory, keyed by its path relative to it.
	 *
	 * @param root the directory
	 * @return the contents of every file, keyed by relative path
	 * @throws IOException if a file cannot be read
	 */
	private static Map<Path, String> contentsOf(Path root) throws IOException {
		Map<Path, String> contents = new TreeMap<>();
		try (Stream<Path> files = Files.walk(root)) {
			for (Path file : files.filter(Files::isRegularFile).toList()) {
				contents.put(root.relativize(file), Files.readString(file, StandardCharsets.ISO_8859_1));
			}
		}
		return contents;
	}

	/**
	 * A declared test class the project's test output does not hold stops
	 * precompile before anything is written, so its bare name never reaches the
	 * generated settings where a student class of that name could take it over.
	 */
	@ParameterizedTest
	@ValueSource(strings = { "ASPECTJ", "INSTRUMENTATION" })
	void precompileRefusesATestClassThatWasNeverCompiled(String aop, @TempDir Path tempDir) throws IOException {
		Path projectFolderPath = projectWithCompiledNestHost(tempDir);
		Path writeTarget = projectFolderPath.resolve("src/test/java");
		Path policy = Files.writeString(tempDir.resolve("policy.yaml"), policyWithOneReadPath("ARCHUNIT", aop).replace(
				"theFollowingClassesAreTestClasses: [ ]",
				"theFollowingClassesAreTestClasses: [ \"example.nest.NestHostFixture\", \"example.nest.GhostTest\" ]"));

		SecurityException refused = assertThrows(SecurityException.class,
				() -> SecurityPolicyReaderAndDirector.builder().securityPolicyFilePath(policy)
						.projectFolderPath(projectFolderPath).build().createTestCases().writeTestCases(writeTarget));
		assertTrue(refused.getMessage().contains("example.nest.GhostTest"), refused.getMessage());
		try (Stream<Path> written = Files.list(writeTarget)) {
			assertEquals(0, written.count(), "precompile wrote files despite the refusal");
		}
	}

	/**
	 * Without a policy, precompile verifies a nested test class whose enclosing
	 * class is not a test under the name its class file carries, so creating the
	 * test cases succeeds, and then refuses to write them for the derived package.
	 */
	@Test
	void precompileWithoutAPolicyVerifiesANestedTestClassByItsClassFileName(@TempDir Path tempDir) throws IOException {
		Path projectFolderPath = projectWithNestedTestFixtureSource(tempDir);
		copyCompiledTestClass(projectFolderPath, "example/nest/NestedTestFixture$Inner.class");
		Path writeTarget = projectFolderPath.resolve("src/test/java");
		List<Path> before = filesBelow(writeTarget);

		SecurityPolicyReaderAndDirector director = SecurityPolicyReaderAndDirector.builder()
				.projectFolderPath(projectFolderPath).build().createTestCases();

		SecurityException refused = assertThrows(SecurityException.class, () -> director.writeTestCases(writeTarget));
		assertTrue(refused.getMessage().contains("theSupervisedCodeUsesTheFollowingPackage"), refused.getMessage());
		assertEquals(before, filesBelow(writeTarget), "a refused precompile wrote files");
	}

	/**
	 * Without a policy, a nested test class whose class file is missing stops
	 * precompile while the test cases are created, naming the class as its class
	 * file would be named.
	 */
	@Test
	void precompileWithoutAPolicyRefusesANestedTestClassThatWasNeverCompiled(@TempDir Path tempDir) throws IOException {
		Path projectFolderPath = projectWithNestedTestFixtureSource(tempDir);

		SecurityException refused = assertThrows(SecurityException.class, () -> SecurityPolicyReaderAndDirector
				.builder().projectFolderPath(projectFolderPath).build().createTestCases());
		assertTrue(refused.getMessage().contains("example.nest.NestedTestFixture$Inner"), refused.getMessage());
	}

	/**
	 * Lays out a Maven project whose test sources declare a nested test class in a
	 * class that is not a test itself, with nothing compiled yet.
	 *
	 * @param tempDir the folder to create the project in
	 * @return the project root
	 * @throws IOException if the layout cannot be written
	 */
	private static Path projectWithNestedTestFixtureSource(Path tempDir) throws IOException {
		Path projectFolderPath = Files.createDirectory(tempDir.resolve("project"));
		Files.writeString(projectFolderPath.resolve("pom.xml"), "<project/>");
		Files.createDirectories(projectFolderPath.resolve("src/main/java"));
		Files.createDirectories(projectFolderPath.resolve("target/classes"));
		Files.writeString(Files.createDirectories(projectFolderPath.resolve("src/test/java/example/nest"))
				.resolve("NestedTestFixture.java"), """
						package example.nest;
						import org.junit.jupiter.api.Nested;
						import org.junit.jupiter.api.Test;
						public final class NestedTestFixture {
						  @Nested final class Inner { @Test void runs() {} }
						}
						""");
		return projectFolderPath;
	}

	/**
	 * The command-line generator writes the same files into the project whether its
	 * two paths are relative to the working directory or absolute, and never
	 * repeats a relative project path inside the project.
	 */
	@Test
	void mainWritesIntoTheProjectWhetherItsPathsAreRelativeOrAbsolute(
			@TempDir(factory = BelowWorkingDirectory.class) Path tempDir) throws IOException {
		Path workingDirectory = Path.of("").toAbsolutePath();
		Path relativeProject = workingDirectory.relativize(emptyMavenProject(tempDir.resolve("relative")));
		Path absoluteProject = emptyMavenProject(tempDir.resolve("absolute"));
		Path policy = Files.writeString(tempDir.resolve("policy.yaml"), policyWithOneReadPath("ARCHUNIT", "ASPECTJ"));
		assertFalse(relativeProject.isAbsolute(), relativeProject.toString());

		Main.main(new String[] { workingDirectory.relativize(policy).toString(), relativeProject.toString() });
		Main.main(new String[] { policy.toString(), absoluteProject.toString() });

		Path project = relativeProject.toAbsolutePath();
		Path settings = Path.of("src/test/java", EXERCISE_PACKAGE.replace('.', '/'),
				"ares/api/aop/java/JavaAOPTestCaseSettings.java");
		assertFalse(Files.exists(project.resolve(relativeProject)), "the relative project path was repeated inside it");
		assertTrue(Files.isRegularFile(project.resolve(settings)), "nothing was written into the relative project");
		assertTrue(Files.isRegularFile(absoluteProject.resolve(settings)), "nothing was written into the absolute one");
		assertEquals(filesBelow(absoluteProject), filesBelow(project));
	}

	/**
	 * Lays out an empty Maven project: a descriptor and empty source and production
	 * output folders.
	 *
	 * @param projectFolderPath the project root to create
	 * @return the project root
	 * @throws IOException if the layout cannot be written
	 */
	private static Path emptyMavenProject(Path projectFolderPath) throws IOException {
		Files.createDirectories(projectFolderPath.resolve("src/main/java"));
		Files.createDirectories(projectFolderPath.resolve("target/classes"));
		Files.writeString(projectFolderPath.resolve("pom.xml"), "<project/>");
		return projectFolderPath;
	}

	/**
	 * Lists every file below a folder, as sorted paths relative to it.
	 *
	 * @param root the folder
	 * @return the relative file paths
	 * @throws IOException if the folder cannot be walked
	 */
	private static List<Path> filesBelow(Path root) throws IOException {
		try (Stream<Path> files = Files.walk(root)) {
			return files.filter(Files::isRegularFile).map(root::relativize).sorted().toList();
		}
	}

	/**
	 * Creates temporary folders inside the build output below the working
	 * directory, so that a path relative to that directory reaches them on every
	 * platform, a Windows temporary folder on another drive included.
	 */
	static final class BelowWorkingDirectory implements TempDirFactory {

		/**
		 * Creates a fresh folder below {@code target} in the working directory.
		 *
		 * @param elementContext   the annotated parameter or field, unused
		 * @param extensionContext the running test, unused
		 * @return the absolute path of the new folder
		 * @throws IOException if the folder cannot be created
		 */
		@Override
		public Path createTempDirectory(AnnotatedElementContext elementContext, ExtensionContext extensionContext)
				throws IOException {
			return Files.createTempDirectory(Files.createDirectories(Path.of("target").toAbsolutePath()),
					"precompile-paths");
		}
	}

	/**
	 * Lays out a Maven project whose test output holds the compiled nest host
	 * fixture, with empty production output and an empty test source folder.
	 *
	 * @param tempDir the folder to create the project in
	 * @return the project root
	 * @throws IOException if the layout cannot be written
	 */
	private static Path projectWithCompiledNestHost(Path tempDir) throws IOException {
		Path projectFolderPath = Files.createDirectory(tempDir.resolve("project"));
		Files.writeString(projectFolderPath.resolve("pom.xml"), "<project/>");
		Files.createDirectories(projectFolderPath.resolve("src/main/java"));
		Files.createDirectories(projectFolderPath.resolve("target/classes"));
		Files.createDirectories(projectFolderPath.resolve("src/test/java"));
		copyCompiledTestClass(projectFolderPath, "example/nest/NestHostFixture.class");
		return projectFolderPath;
	}

	/**
	 * Copies a compiled test fixture into the project's Maven test output.
	 *
	 * @param projectFolderPath the project root
	 * @param classFile         the fixture's class file path below the output
	 * @throws IOException if the copy fails
	 */
	private static void copyCompiledTestClass(Path projectFolderPath, String classFile) throws IOException {
		Path target = projectFolderPath.resolve("target/test-classes").resolve(classFile);
		Files.createDirectories(target.getParent());
		try (var input = PrecompileTest.class.getResourceAsStream("/" + classFile)) {
			Files.write(target, java.util.Objects.requireNonNull(input, "the fixture is compiled").readAllBytes());
		}
	}

	/**
	 * Returns the line of a generated settings file that declares a field.
	 *
	 * @param settings the generated settings source
	 * @param field    the field name
	 * @return the declaring line
	 */
	private static String declarationOf(String settings, String field) {
		return settings.lines().filter(line -> line.contains(" " + field + " =")).findFirst()
				.orElseThrow(() -> new AssertionError(field + " is not declared in:\n" + settings));
	}

	/**
	 * Checks that a generated file does not contain a text.
	 *
	 * @param file      the generated file
	 * @param forbidden the text it must not contain
	 * @throws IOException if the file cannot be read
	 */
	private static void assertDoesNotContain(Path file, String forbidden) throws IOException {
		assertTrue(Files.exists(file), () -> "precompile did not write " + file);
		try (Stream<String> lines = Files.lines(file)) {
			assertTrue(lines.noneMatch(line -> line.contains(forbidden)), () -> file + " contains " + forbidden);
		}
	}

	/**
	 * Checks that a generated file contains a text.
	 *
	 * @param file     the generated file
	 * @param expected the text it must contain
	 * @throws IOException if the file cannot be read
	 */
	private static void assertContains(Path file, String expected) throws IOException {
		assertTrue(Files.exists(file), () -> "precompile did not write " + file);
		try (Stream<String> lines = Files.lines(file)) {
			assertTrue(lines.anyMatch(line -> line.contains(expected)), () -> file + " does not contain " + expected);
		}
	}

	/**
	 * Precompile writes a package-import rule through the real reader and writer
	 * that names the declared test class, permits Ares's api where it was copied
	 * to, reads the supervised code's own packages when it runs, and never permits
	 * the supervised package as a whole.
	 */
	@ParameterizedTest
	@CsvSource({ "ARCHUNIT,INSTRUMENTATION", "ARCHUNIT,ASPECTJ", "WALA,INSTRUMENTATION", "WALA,ASPECTJ" })
	void precompileWritesThePackageImportRule(String analysis, String aop, @TempDir Path tempDir) throws IOException {
		Path projectFolderPath = Files.createDirectory(tempDir.resolve("project"));
		Files.writeString(projectFolderPath.resolve("pom.xml"), "<project/>");
		Files.createDirectories(projectFolderPath.resolve("src/main/java"));
		Files.createDirectories(projectFolderPath.resolve("target/classes"));
		Path writeTarget = Files.createDirectories(projectFolderPath.resolve("src/test/java"));
		compileTestClassInto(tempDir, projectFolderPath.resolve("target/test-classes"), "PenguinTest");
		Path policy = Files.writeString(tempDir.resolve("policy.yaml"),
				policyWithOneReadPath(analysis, aop).replace("theFollowingClassesAreTestClasses: [ ]",
						"theFollowingClassesAreTestClasses: [ \"" + EXERCISE_PACKAGE + ".PenguinTest\" ]"));

		SecurityPolicyReaderAndDirector.builder().securityPolicyFilePath(policy).projectFolderPath(projectFolderPath)
				.build().createTestCases().writeTestCases(writeTarget);

		String rule;
		try (var files = Files.walk(writeTarget)) {
			rule = files.filter(Files::isRegularFile).filter(file -> file.toString().endsWith(".java"))
					.map(PrecompileTest::read).filter(content -> content.contains("packageImportShouldNotBeAccessed"))
					.findFirst().orElseThrow();
		}
		assertTrue(rule.contains("Set.of(\"" + EXERCISE_PACKAGE + ".PenguinTest\")"), rule);
		assertTrue(rule.contains("withOwnPackages(allowedPackages, javaClasses"), rule);
		assertTrue(rule.contains("new PackagePermission(\"java.lang\", true)"), rule);
		assertTrue(rule.contains("new PackagePermission(\"java.io\", true)"), rule);
		assertTrue(rule.contains("new PackagePermission(\"" + EXERCISE_PACKAGE
				+ ".ares.api.aop.java.aspectj.adviceandpointcut\", false)"), rule);
		assertFalse(rule.contains("new PackagePermission(\"" + EXERCISE_PACKAGE + "\", false)"), rule);
	}

	/**
	 * Compiles an empty class of the given simple name in the exercise package into
	 * a test output, so that a policy can declare it as a compiled test class. The
	 * source stays outside the project, where no generated file is looked for.
	 *
	 * @param tempDir    the folder that holds the source
	 * @param testOutput the test output to compile into
	 * @param simpleName the class's simple name
	 * @throws IOException if the source cannot be written
	 */
	private static void compileTestClassInto(Path tempDir, Path testOutput, String simpleName) throws IOException {
		Path source = Files.createDirectories(tempDir.resolve("sources")).resolve(simpleName + ".java");
		Files.writeString(source, "package " + EXERCISE_PACKAGE + "; public class " + simpleName + " {}");
		Files.createDirectories(testOutput);
		int status = ToolProvider.getSystemJavaCompiler().run(null, null, null, "-d", testOutput.toString(),
				source.toString());
		assertEquals(0, status, "the test class compiles");
	}

	/**
	 * Reads a generated file.
	 *
	 * @param file the file
	 * @return its content
	 */
	private static String read(Path file) {
		try {
			return Files.readString(file);
		} catch (IOException e) {
			throw new java.io.UncheckedIOException(e);
		}
	}

	/**
	 * Returns a policy for the made-up exercise that grants reading one file, so
	 * precompile writes the runtime checks too.
	 *
	 * @param analysis {@code ARCHUNIT} or {@code WALA}
	 * @param aop      {@code INSTRUMENTATION} or {@code ASPECTJ}
	 * @return the policy text
	 */
	private static String policyWithOneReadPath(String analysis, String aop) {
		return String.join("\n", "thisPolicyFileCompliesToThePolicyVersion: 1", "regardingTheSupervisedCode:",
				"  theFollowingProgrammingLanguageConfigurationIsUsed: JAVA_USING_MAVEN_" + analysis + "_AND_" + aop,
				"  theSupervisedCodeUsesTheFollowingPackage: \"" + EXERCISE_PACKAGE + "\"",
				"  theMainClassInsideThisPackageIs: \"Main\"", "  theFollowingClassesAreTestClasses: [ ]",
				"  theFollowingResourceAccessesArePermitted:", "    regardingFileSystemInteractions:",
				"      - readAllFiles: true", "        overwriteAllFiles: false", "        createAllFiles: false",
				"        executeAllFiles: false", "        deleteAllFiles: false",
				"        onThisPathAndAllPathsBelow: \"data.txt\"", "    regardingNetworkConnections: [ ]",
				"    regardingCommandExecutions: [ ]", "    regardingThreadCreations: [ ]",
				"    regardingPackageImports: [ ]", "    regardingTimeouts:", "      - timeout: 3000", "");
	}
}
