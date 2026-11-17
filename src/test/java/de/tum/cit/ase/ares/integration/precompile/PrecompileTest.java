package de.tum.cit.ase.ares.integration.precompile;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
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
	 * Exercises the precompile flow ({@code writeTestCases}) which generates the
	 * security-test scaffold (architecture/AOP helper classes, localisation
	 * resources and the Phobos files).
	 * <p>
	 * The scaffold is written into a JUnit-managed temporary directory rather than
	 * the current working directory, so the repository is never polluted. The write
	 * target is nested several levels deep on purpose: the localisation step places
	 * its {@code resources} folder as a sibling two levels up, and the Phobos step
	 * climbs three levels up, so a deep target keeps every generated artefact
	 * inside {@code tempDir}, which {@link TempDir} removes afterwards.
	 */
	@Test
	void testPrecompileJavaMavenArchunitInstrumentation(@TempDir Path tempDir) throws IOException {
		Path projectFolderPath = Files.createDirectory(tempDir.resolve("project"));
		Files.writeString(projectFolderPath.resolve("pom.xml"), "<project/>");
		Files.createDirectories(projectFolderPath.resolve("src/main/java"));
		Files.createDirectories(projectFolderPath.resolve("src/test/java"));
		Files.createDirectories(projectFolderPath.resolve("target/classes"));
		Path writeTarget = projectFolderPath.resolve("src/test/java");
		SecurityPolicyReaderAndDirector.builder().projectFolderPath(projectFolderPath).build().createTestCases()
				.writeTestCases(writeTarget);
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
	 * Without a policy, precompile finds a nested test class whose enclosing class
	 * is not a test, verifies it under the name its class file carries, and writes
	 * that name into the generated settings.
	 */
	@Test
	void precompileWithoutAPolicyExemptsANestedTestClassByItsClassFileName(@TempDir Path tempDir) throws IOException {
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
		copyCompiledTestClass(projectFolderPath, "example/nest/NestedTestFixture$Inner.class");
		Path writeTarget = projectFolderPath.resolve("src/test/java");

		SecurityPolicyReaderAndDirector.builder().projectFolderPath(projectFolderPath).build().createTestCases()
				.writeTestCases(writeTarget);

		Path settingsFile;
		try (Stream<Path> written = Files.walk(writeTarget)) {
			settingsFile = written.filter(path -> path.endsWith("JavaAOPTestCaseSettings.java")).findFirst()
					.orElseThrow();
		}
		String settings = Files.readString(settingsFile);
		assertTrue(declarationOf(settings, "allowedListedClasses").contains("\"example.nest.NestedTestFixture$Inner\""),
				settings);
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
