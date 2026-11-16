package de.tum.cit.ase.ares.api.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import de.tum.cit.ase.ares.api.buildtoolconfiguration.BuildToolConfiguration;

/**
 * The Gradle forms of declaring Java source directories. Every descriptor here
 * was run through Gradle 9.9.1 as written, the one naming {@code :child} with a
 * settings file including it: a form Ares reads must give the roots Gradle
 * uses, and a form it cannot place must leave main incomplete.
 */
@DisplayName("ProjectSourcesFinder Gradle declaration forms")
class ProjectSourcesFinderGradleFormsTest {

	/** The Groovy descriptor's name. */
	private static final String GROOVY = "build.gradle";

	/** The Kotlin descriptor's name. */
	private static final String KOTLIN = "build.gradle.kts";

	/** The conventional main root, which every addition keeps. */
	private static final String MAIN = "src/main/java";

	/** The conventional test root. */
	private static final String TEST = "src/test/java";

	/** The project each descriptor is written into. */
	@TempDir
	Path projectRoot;

	/**
	 * Creates every directory a descriptor here may name, as the Gradle run did.
	 *
	 * @throws IOException if one cannot be created
	 */
	@BeforeEach
	void createDirectories() throws IOException {
		for (String directory : List.of("a", "b", "checks", MAIN, TEST)) {
			Files.createDirectories(projectRoot.resolve(directory));
		}
	}

	/**
	 * Forms that declare a root, with the roots Gradle reported for them.
	 *
	 * @return label, descriptor name, descriptor, main roots, test roots
	 */
	static Stream<Arguments> declaringForms() {
		return Stream.of(form("Groovy receiver block", GROOVY, """
				plugins { id 'java' }
				sourceSets.main.java { srcDir 'a' }
				""", List.of("a", MAIN), List.of(TEST)), form("Groovy receiver chain, command call", GROOVY, """
				plugins { id 'java' }
				sourceSets.main.java.srcDir 'a'
				""", List.of("a", MAIN), List.of(TEST)), form("Groovy receiver chain, method call", GROOVY, """
				plugins { id 'java' }
				sourceSets.main.java.srcDir('a')
				""", List.of("a", MAIN), List.of(TEST)), form("Groovy dotted assignment in sourceSets", GROOVY, """
				plugins { id 'java' }
				sourceSets { main.java.srcDirs = ['a'] }
				""", List.of("a"), List.of(TEST)), form("Groovy java chain in main", GROOVY, """
				plugins { id 'java' }
				sourceSets { main { java.srcDir 'a' } }
				""", List.of("a", MAIN), List.of(TEST)), form("Groovy srcDirs command with two operands", GROOVY, """
				plugins { id 'java' }
				sourceSets { main { java { srcDirs 'a', 'b' } } }
				""", List.of("a", "b", MAIN), List.of(TEST)), form("Groovy setSrcDirs", GROOVY, """
				plugins { id 'java' }
				sourceSets { main { java { setSrcDirs(['a']) } } }
				""", List.of("a"), List.of(TEST)), form("Groovy index lookup", GROOVY, """
				plugins { id 'java' }
				sourceSets['main'].java.srcDir 'a'
				""", List.of("a", MAIN), List.of(TEST)), form("Groovy getByName", GROOVY, """
				plugins { id 'java' }
				sourceSets.getByName('main').java.srcDir 'a'
				""", List.of("a", MAIN), List.of(TEST)), form("Groovy named with a block", GROOVY, """
				plugins { id 'java' }
				sourceSets.named('main') { java.srcDir 'a' }
				""", List.of("a", MAIN), List.of(TEST)), form("Groovy files on one line", GROOVY, """
				plugins { id 'java' }
				sourceSets { main { java { srcDirs = files('a', 'b') } } }
				""", List.of("a", "b"), List.of(TEST)), form("Groovy files over two lines", GROOVY, """
				plugins { id 'java' }
				sourceSets { main { java { srcDirs = files('a',
						'b') } } }
				""", List.of("a", "b"), List.of(TEST)), form("Groovy list of file", GROOVY, """
				plugins { id 'java' }
				sourceSets { main { java { srcDirs = [file('a')] } } }
				""", List.of("a"), List.of(TEST)), form("Groovy srcDir with file", GROOVY, """
				plugins { id 'java' }
				sourceSets { main { java { srcDir file('a') } } }
				""", List.of("a", MAIN), List.of(TEST)), form("Groovy projectDir interpolations", GROOVY, """
				plugins { id 'java' }
				sourceSets { main { java { srcDir "$projectDir/a"; srcDir "${projectDir}/b" } } }
				""", List.of("a", "b", MAIN), List.of(TEST)),
				form("Groovy project.projectDir interpolation", GROOVY, """
						plugins { id 'java' }
						sourceSets { main { java { srcDir "${project.projectDir}/a" } } }
						""", List.of("a", MAIN), List.of(TEST)),
				form("Groovy chain continued on the next lines", GROOVY, """
						plugins { id 'java' }
						sourceSets.main
						    .java
						    .srcDir 'a'
						""", List.of("a", MAIN), List.of(TEST)), form("Groovy srcDir with a list", GROOVY, """
						plugins { id 'java' }
						sourceSets { main { java { srcDir(['a', 'b']) } } }
						""", List.of("a", "b", MAIN), List.of(TEST)), form("Groovy srcDirs += file", GROOVY, """
						plugins { id 'java' }
						sourceSets { main { java { srcDirs += file('a') } } }
						""", List.of("a", MAIN), List.of(TEST)),
				form("Groovy allprojects, which includes this project", GROOVY, """
						plugins { id 'java' }
						allprojects { sourceSets.main.java.srcDir 'a' }
						""", List.of("a", MAIN), List.of(TEST)),
				form("Groovy srcDir as the argument of println", GROOVY, """
						plugins { id 'java' }
						sourceSets { main { java {
						    println srcDir('a')
						} } }
						""", List.of("a", MAIN), List.of(TEST)), form("Groovy assignment inside a call", GROOVY, """
						plugins { id 'java' }
						println(sourceSets.main.java.srcDirs = ['a'])
						""", List.of("a"), List.of(TEST)), form("Groovy test chain", GROOVY, """
						plugins { id 'java' }
						sourceSets.test.java.srcDir 'checks'
						""", List.of(MAIN), List.of("checks", TEST)), form("Kotlin main with java chain", KOTLIN, """
						plugins { java }
						sourceSets.main { java.srcDir("a") }
						""", List.of("a", MAIN), List.of(TEST)), form("Kotlin main with java block", KOTLIN, """
						plugins { java }
						sourceSets.main { java { srcDir("a") } }
						""", List.of("a", MAIN), List.of(TEST)), form("Kotlin configure", KOTLIN, """
						plugins { java }
						sourceSets.main.configure { java.srcDir("a") }
						""", List.of("a", MAIN), List.of(TEST)), form("Kotlin java.setSrcDirs", KOTLIN, """
						plugins { java }
						sourceSets.main { java.setSrcDirs(listOf("a")) }
						""", List.of("a"), List.of(TEST)), form("Kotlin setSrcDirs in a java block", KOTLIN, """
						plugins { java }
						sourceSets.main { java { setSrcDirs(listOf("a")) } }
						""", List.of("a"), List.of(TEST)), form("Kotlin get", KOTLIN, """
						plugins { java }
						sourceSets.main.get().java.srcDir("a")
						""", List.of("a", MAIN), List.of(TEST)), form("Kotlin index lookup", KOTLIN, """
						plugins { java }
						sourceSets["main"].java.srcDir("a")
						""", List.of("a", MAIN), List.of(TEST)), form("Kotlin getByName", KOTLIN, """
						plugins { java }
						sourceSets.getByName("main").java.srcDir("a")
						""", List.of("a", MAIN), List.of(TEST)), form("Kotlin named with a block", KOTLIN, """
						plugins { java }
						sourceSets.named("main") { java.srcDir("a") }
						""", List.of("a", MAIN), List.of(TEST)), form("Kotlin java extension", KOTLIN, """
						plugins { java }
						java.sourceSets["main"].java.srcDir("a")
						""", List.of("a", MAIN), List.of(TEST)), form("Kotlin srcDirs with two operands", KOTLIN, """
						plugins { java }
						sourceSets.main { java.srcDirs("a", "b") }
						""", List.of("a", "b", MAIN), List.of(TEST)), form("Kotlin srcDirs with files", KOTLIN, """
						plugins { java }
						sourceSets.main { java.srcDirs(files("a", "b")) }
						""", List.of("a", "b", MAIN), List.of(TEST)), form("Kotlin srcDir with listOf", KOTLIN, """
						plugins { java }
						sourceSets.main { java.srcDir(listOf("a", "b")) }
						""", List.of("a", "b", MAIN), List.of(TEST)), form("Kotlin srcDir with file", KOTLIN, """
						plugins { java }
						sourceSets.main { java.srcDir(file("a")) }
						""", List.of("a", MAIN), List.of(TEST)), form("Kotlin projectDir interpolation", KOTLIN, """
						plugins { java }
						sourceSets.main { java.srcDir("$projectDir/a") }
						""", List.of("a", MAIN), List.of(TEST)),
				form("Groovy safe navigation, which still calls", GROOVY, """
						plugins { id 'java' }
						sourceSets.main.java?.srcDir('a')
						""", List.of("a", MAIN), List.of(TEST)),
				form("Groovy rootProject of the project being read", GROOVY, """
						plugins { id 'java' }
						rootProject.sourceSets.main.java.srcDir 'a'
						""", List.of("a", MAIN), List.of(TEST)),
				form("Kotlin with(rootProject) of the project being read", KOTLIN, """
						plugins { java }
						with(rootProject) { sourceSets.test { java.srcDir("a") } }
						""", List.of(MAIN), List.of("a", TEST)),
				form("Groovy closure as a lookup's second argument", GROOVY, """
						plugins { id 'java' }
						sourceSets.named('main', { java.srcDir 'a' })
						""", List.of("a", MAIN), List.of(TEST)),
				form("Groovy if with its body on its own line, then a declaration", GROOVY, """
						plugins { id 'java' }
						if (true) println 'x'
						sourceSets.test.java.srcDir 'a'
						""", List.of(MAIN), List.of("a", TEST)));
	}

	/**
	 * Forms that look like a declaration but declare nothing, so the roots stay
	 * conventional, with what Gradle reported for them.
	 *
	 * @return label, descriptor name, descriptor, main roots, test roots
	 */
	static Stream<Arguments> nonDeclaringForms() {
		return Stream.of(form("Groovy line comment", GROOVY, """
				plugins { id 'java' }
				// sourceSets.main.java.srcDir 'a'
				""", List.of(MAIN), List.of(TEST)), form("Groovy string", GROOVY, """
				plugins { id 'java' }
				println "sourceSets.main.java.srcDir 'a'"
				""", List.of(MAIN), List.of(TEST)), form("Groovy resources", GROOVY, """
				plugins { id 'java' }
				sourceSets.main.resources.srcDir 'a'
				""", List.of(MAIN), List.of(TEST)), form("Groovy name ending in srcDirs", GROOVY, """
				plugins { id 'java' }
				def mySrcDirs = ['a']
				println mySrcDirs
				""", List.of(MAIN), List.of(TEST)), form("Groovy name starting with srcDirs", GROOVY, """
				plugins { id 'java' }
				ext.srcDirsExtra = ['a']
				""", List.of(MAIN), List.of(TEST)), form("Groovy local named srcDirs", GROOVY, """
				plugins { id 'java' }
				def srcDirs = ['a']
				println srcDirs
				""", List.of(MAIN), List.of(TEST)), form("Groovy comparison", GROOVY, """
				plugins { id 'java' }
				println(sourceSets.main.java.srcDirs == [file('a')] as Set)
				""", List.of(MAIN), List.of(TEST)), form("Groovy add on the returned set", GROOVY, """
				plugins { id 'java' }
				sourceSets.main.java.srcDirs.add(file('a'))
				""", List.of(MAIN), List.of(TEST)), form("Kotlin local named srcDirs", KOTLIN, """
				plugins { java }
				val srcDirs = listOf("a")
				println(srcDirs)
				""", List.of(MAIN), List.of(TEST)), form("Kotlin named argument", KOTLIN, """
				plugins { java }
				fun show(srcDir: String) = println(srcDir)
				show(srcDir = "a")
				""", List.of(MAIN), List.of(TEST)), form("Kotlin srcDirs +=, which changes nothing", KOTLIN, """
				plugins { java }
				sourceSets.main { java.srcDirs += setOf(file("a")) }
				""", List.of(MAIN), List.of(TEST)));
	}

	/**
	 * Forms this reader cannot place in one source set or cannot work out, each of
	 * which Gradle accepts.
	 *
	 * @return label, descriptor name, descriptor
	 */
	static Stream<Arguments> unplaceableForms() {
		return Stream.of(Arguments.of("Groovy all", GROOVY, """
				plugins { id 'java' }
				sourceSets.all { java.srcDir 'a' }
				"""), Arguments.of("Kotlin configureEach", KOTLIN, """
				plugins { java }
				sourceSets.configureEach { java.srcDir("a") }
				"""), Arguments.of("Groovy custom source set", GROOVY, """
				plugins { id 'java' }
				sourceSets { integration { java.srcDir 'a' } }
				"""), Arguments.of("Groovy subprojects", GROOVY, """
				plugins { id 'java' }
				subprojects { apply plugin: 'java'; sourceSets.main.java.srcDir 'a' }
				"""), Arguments.of("Groovy another project's test set", GROOVY, """
				plugins { id 'java' }
				project(':child') { apply plugin: 'java'; sourceSets.test.java.srcDir 'a' }
				"""), Arguments.of("Groovy configure", GROOVY, """
				plugins { id 'java' }
				configure(subprojects) { apply plugin: 'java'; sourceSets.main.java.srcDir 'a' }
				"""), Arguments.of("Groovy declarations under a condition", GROOVY, """
				plugins { id 'java' }
				if (false) {
				    sourceSets.main.java.setSrcDirs(['a'])
				    sourceSets.test.java.srcDir 'a'
				}
				"""), Arguments.of("Groovy srcDir behind &&", GROOVY, """
				plugins { id 'java' }
				false && sourceSets.main.java.srcDir('a')
				"""), Arguments.of("Groovy srcDir behind && inside a call", GROOVY, """
				plugins { id 'java' }
				println(false && sourceSets.main.java.srcDir('a'))
				"""), Arguments.of("Groovy test srcDir behind ||", GROOVY, """
				plugins { id 'java' }
				true || sourceSets.test.java.srcDir('a')
				"""), Arguments.of("Groovy srcDir in a ternary branch", GROOVY, """
				plugins { id 'java' }
				def x = true ? null : sourceSets.main.java.srcDir('a')
				"""), Arguments.of("Groovy srcDir behind elvis", GROOVY, """
				plugins { id 'java' }
				def x = 'set' ?: sourceSets.main.java.srcDir('a')
				"""), Arguments.of("Groovy srcDir after && at the end of a line", GROOVY, """
				plugins { id 'java' }
				false &&
				    sourceSets.main.java.srcDir('a')
				"""), Arguments.of("Groovy test srcDir after && and a backslash", GROOVY, """
				plugins { id 'java' }
				false && \\
				    sourceSets.test.java.srcDir('a')
				"""), Arguments.of("Groovy srcDir on the last line of a ternary", GROOVY, """
				plugins { id 'java' }
				def x = true
				    ? null
				    : sourceSets.main.java.srcDir('a')
				"""), Arguments.of("Kotlin srcDir behind elvis", KOTLIN, """
				plugins { java }
				val x = "set" ?: sourceSets.main.get().java.srcDir("a")
				"""), Arguments.of("Groovy a variable given a second value", GROOVY, """
				plugins { id 'java' }
				def directory = 'a'
				sourceSets.main.java.setSrcDirs([directory])
				directory = 'b'
				"""), Arguments.of("Groovy a variable built from two literals", GROOVY, """
				plugins { id 'java' }
				def directory = 'a' + '/nested'
				sourceSets.main.java.setSrcDirs([directory])
				"""), Arguments.of("Groovy source on a compile supertype", GROOVY, """
				plugins { id 'java' }
				tasks.withType(org.gradle.api.tasks.compile.AbstractCompile) { source 'a' }
				"""), Arguments.of("Groovy quoted method name", GROOVY, """
				plugins { id 'java' }
				sourceSets.main.java.'srcDir'('a')
				"""), Arguments.of("Groovy compound assignment", GROOVY, """
				plugins { id 'java' }
				def directory = 'a'
				directory += '/b'
				sourceSets.main.java.setSrcDirs([directory])
				"""), Arguments.of("Groovy assignment after another statement", GROOVY, """
				plugins { id 'java' }
				def directory = 'a'
				println 'x'; directory = 'b'
				sourceSets.main.java.setSrcDirs([directory])
				"""), Arguments.of("Groovy method never called", GROOVY, """
				plugins { id 'java' }
				void unused() {
				    sourceSets.main.java.setSrcDirs(['a'])
				}
				"""), Arguments.of("Kotlin lambda never called", KOTLIN, """
				plugins { java }
				val unused = {
				    sourceSets.main { java.setSrcDirs(listOf("a")) }
				}
				"""), Arguments.of("Groovy method with a return type", GROOVY, """
				plugins { id 'java' }
				int unused() {
				    sourceSets.main.java.setSrcDirs(['a'])
				    return 0
				}
				"""), Arguments.of("Groovy method with a throws clause", GROOVY, """
				plugins { id 'java' }
				int unused() throws Exception {
				    sourceSets.main.java.setSrcDirs(['a'])
				    return 0
				}
				"""), Arguments.of("Groovy method with a comment before its name", GROOVY, """
				plugins { id 'java' }
				int /* note */ unused() {
				    sourceSets.main.java.setSrcDirs(['a'])
				    return 0
				}
				"""), Arguments.of("Groovy quoted name after a comment", GROOVY, """
				plugins { id 'java' }
				sourceSets.main.java./* source folder */'srcDir'('a')
				"""), Arguments.of("Groovy srcDir with a closure", GROOVY, """
				plugins { id 'java' }
				sourceSets.main.java.srcDir { 'a' }
				"""), Arguments.of("Kotlin srcDir with a provider", KOTLIN, """
				plugins { java }
				sourceSets.main { java.srcDir(provider { "a" }) }
				"""), Arguments.of("Groovy source of another source directory set", GROOVY, """
				plugins { id 'java' }
				sourceSets.main.java.source(sourceSets.test.java)
				"""), Arguments.of("Groovy withType block", GROOVY, """
				plugins { id 'java' }
				tasks.withType(JavaCompile) { source 'a' }
				"""), Arguments.of("Kotlin withType configureEach", KOTLIN, """
				plugins { java }
				tasks.withType<JavaCompile>().configureEach { source("a") }
				"""), Arguments.of("Kotlin named compile task", KOTLIN, """
				plugins { java }
				tasks.named<JavaCompile>("compileJava") { source("a") }
				"""), Arguments.of("Groovy compileJava block", GROOVY, """
				plugins { id 'java' }
				compileJava { source 'a' }
				"""), Arguments.of("Groovy compileJava chain", GROOVY, """
				plugins { id 'java' }
				compileJava.source 'a'
				"""));
	}

	/**
	 * A form Gradle reads as roots gives exactly those roots, and main stays
	 * complete.
	 *
	 * @param label      what the form is
	 * @param descriptor the descriptor's name
	 * @param content    the descriptor
	 * @param main       the main roots Gradle reported
	 * @param test       the test roots Gradle reported
	 * @throws IOException if the fixture cannot be written
	 */
	@ParameterizedTest(name = "{0}")
	@MethodSource("declaringForms")
	void readsTheRootsGradleDeclares(String label, String descriptor, String content, List<String> main,
			List<String> test) throws IOException {
		assertRootsAndComplete(descriptor, content, main, test);
	}

	/**
	 * A form that only resembles a declaration leaves the roots as Gradle leaves
	 * them, and main complete.
	 *
	 * @param label      what the form is
	 * @param descriptor the descriptor's name
	 * @param content    the descriptor
	 * @param main       the main roots Gradle reported
	 * @param test       the test roots Gradle reported
	 * @throws IOException if the fixture cannot be written
	 */
	@ParameterizedTest(name = "{0}")
	@MethodSource("nonDeclaringForms")
	void passesOverWhatDeclaresNothing(String label, String descriptor, String content, List<String> main,
			List<String> test) throws IOException {
		assertRootsAndComplete(descriptor, content, main, test);
	}

	/**
	 * A form this reader cannot place leaves main incomplete, and never adds what
	 * it names to the test roots, whose classes would be exempt.
	 *
	 * @param label      what the form is
	 * @param descriptor the descriptor's name
	 * @param content    the descriptor
	 * @throws IOException if the fixture cannot be written
	 */
	@ParameterizedTest(name = "{0}")
	@MethodSource("unplaceableForms")
	void marksMainIncompleteWhereTheFormCannotBePlaced(String label, String descriptor, String content)
			throws IOException {
		BuildToolConfiguration configuration = discover(descriptor, content);

		assertFalse(configuration.productionRootsComplete(), label);
		assertFalse(configuration.testSourceRoots().contains(projectRoot.resolve("a").toRealPath()), label);
	}

	/**
	 * A test operand not read in full gives no test root, and a declaration on
	 * every source set that may replace them drops the test roots, since a test
	 * root Gradle does not use would exempt the classes in it.
	 *
	 * @param label      what the form is
	 * @param descriptor the descriptor's name
	 * @param content    the descriptor
	 * @throws IOException if the fixture cannot be written
	 */
	@ParameterizedTest(name = "{0}")
	@MethodSource("testRootsGradleDoesNotUse")
	void neverTakesATestRootGradleMayNotUse(String label, String descriptor, String content) throws IOException {
		assertFalse(discover(descriptor, content).testSourceRoots().contains(projectRoot.resolve(MAIN).toRealPath()),
				label);
	}

	/**
	 * Forms after which Gradle has no test root {@code src/main/java}, though an
	 * operand names it.
	 *
	 * @return label, descriptor name, descriptor
	 */
	static Stream<Arguments> testRootsGradleDoesNotUse() {
		return Stream.of(Arguments.of("Groovy test list filtered after it is read", GROOVY, """
				plugins { id 'java' }
				sourceSets.test.java.srcDirs =
				    ['src/main/java'].findAll { false }
				"""), Arguments.of("Groovy afterEvaluate replacing what test is given below it", GROOVY, """
				plugins { id 'java' }
				afterEvaluate {
				    sourceSets.main.java.setSrcDirs(['a'])
				    sourceSets.test.java.setSrcDirs([])
				}
				sourceSets.main.java.setSrcDirs(['b'])
				sourceSets.test.java.srcDir 'src/main/java'
				"""), Arguments.of("Groovy quoted replacement after a blank", GROOVY, """
				plugins { id 'java' }
				sourceSets.test.java.srcDir 'src/main/java'
				sourceSets.test.java. 'setSrcDirs'([])
				"""), Arguments.of("Groovy replacement on a found source set", GROOVY, """
				plugins { id 'java' }
				sourceSets.test.java.srcDir 'src/main/java'
				sourceSets.find { it.name == 'test' }.java.setSrcDirs([])
				"""), Arguments.of("Groovy test assignment inside a call", GROOVY, """
				plugins { id 'java' }
				sourceSets.test.java.srcDir 'src/main/java'
				println(sourceSets.test.java.srcDirs = [])
				"""), Arguments.of("Groovy test replacement behind ||, which runs", GROOVY, """
				plugins { id 'java' }
				sourceSets.test.java.srcDir 'src/main/java'
				false || sourceSets.test.java.setSrcDirs([])
				"""), Arguments.of("Groovy replacement behind a comparison with parent", GROOVY, """
				plugins { id 'java' }
				sourceSets.test.java.srcDir 'src/main/java'
				parent == null && sourceSets.test.java.setSrcDirs([])
				"""), Arguments.of("Groovy test set through parent?.allprojects in the root", GROOVY, """
				plugins { id 'java' }
				parent?.allprojects { sourceSets.test.java.srcDir 'src/main/java' }
				"""), Arguments.of("Groovy rootProject replacing this project's test set", GROOVY, """
				plugins { id 'java' }
				sourceSets.test.java.srcDir 'src/main/java'
				rootProject.sourceSets.test.java.setSrcDirs([])
				"""), Arguments.of("Groovy all replacing what test was given", GROOVY, """
				plugins { id 'java' }
				sourceSets.test.java.srcDir 'src/main/java'
				sourceSets.all { java.setSrcDirs([]) }
				sourceSets.main.java.srcDir 'src/main/java'
				"""), Arguments.of("Groovy parent?.allprojects inside a call in the root", GROOVY, """
				plugins { id 'java' }
				println(parent?.allprojects { sourceSets.test.java.srcDir 'src/main/java' })
				"""), Arguments.of("Groovy subprojects inside a call", GROOVY, """
				plugins { id 'java' }
				println(subprojects { sourceSets.test.java.srcDir 'src/main/java' })
				"""), Arguments.of("Groovy configure(allprojects) replacing this project's test set", GROOVY, """
				plugins { id 'java' }
				sourceSets.test.java.srcDir 'src/main/java'
				configure(allprojects) { sourceSets.test.java.setSrcDirs([]) }
				"""), Arguments.of("Groovy configure(rootProject) replacing this project's test set", GROOVY, """
				plugins { id 'java' }
				sourceSets.test.java.srcDir 'src/main/java'
				configure(rootProject) { sourceSets.test.java.setSrcDirs([]) }
				"""), Arguments.of("Groovy configure(project) replacing this project's test set", GROOVY, """
				plugins { id 'java' }
				sourceSets.test.java.srcDir 'src/main/java'
				configure(project) { sourceSets.test.java.setSrcDirs([]) }
				"""), Arguments.of("Groovy configure of an empty list, whose block never runs", GROOVY, """
				plugins { id 'java' }
				configure([]) {
				    sourceSets.test.java.srcDir('src/main/java')
				}
				"""), Arguments.of("Groovy if whose body is on the next line", GROOVY, """
				plugins { id 'java' }
				if (false)
				    sourceSets.test.java.srcDir('src/main/java')
				"""), Arguments.of("Groovy if whose brace is on the next line", GROOVY, """
				plugins { id 'java' }
				if (false)
				{
				    sourceSets.test.java.srcDir('src/main/java')
				}
				"""), Arguments.of("Groovy else whose body is on the next line", GROOVY, """
				plugins { id 'java' }
				if (true) println 'x'
				else
				    sourceSets.test.java.srcDir('src/main/java')
				"""), Arguments.of("Groovy while whose body is on the next line", GROOVY, """
				plugins { id 'java' }
				while (false)
				    sourceSets.test.java.srcDir('src/main/java')
				"""), Arguments.of("Kotlin if whose body is on the next line", KOTLIN, """
				plugins { java }
				if (false)
				    sourceSets.test { java.srcDir("src/main/java") }
				"""), Arguments.of("Groovy addition in a try block an exception leaves", GROOVY, """
				plugins { id 'java' }
				try {
				    Integer.parseInt('invalid')
				    sourceSets.test.java.srcDir 'src/main/java'
				} catch (Exception ignored) {
				}
				"""), Arguments.of("Groovy project(':') replacing this project's test set", GROOVY, """
				plugins { id 'java' }
				sourceSets.test.java.srcDir 'src/main/java'
				project(':') {
				    sourceSets.test.java.setSrcDirs([])
				}
				"""), Arguments.of("Groovy findProject(':') replacing this project's test set", GROOVY, """
				plugins { id 'java' }
				sourceSets.test.java.srcDir 'src/main/java'
				findProject(':').sourceSets.test.java.setSrcDirs([])
				"""), Arguments.of("Groovy if on a project lookup whose body is on the next line", GROOVY, """
				plugins { id 'java' }
				if (findProject(':') == null)
				    sourceSets.test.java.srcDir('src/main/java')
				"""), Arguments.of("Kotlin when without a subject whose brace is on the next line", KOTLIN, """
				plugins { java }
				when
				{
				    false -> sourceSets.test {
				        java.srcDir("src/main/java")
				    }
				}
				"""), Arguments.of("Groovy rootProject behind a safe call on a project lookup", GROOVY, """
				plugins { id 'java' }
				findProject(':nope')?.rootProject?.sourceSets?.test?.java?.srcDir('src/main/java')
				"""), Arguments.of("Groovy rootProject of every subproject where there is none", GROOVY, """
				plugins { id 'java' }
				subprojects
				    .rootProject
				    .each {
				        sourceSets.test.java.srcDir 'src/main/java'
				    }
				"""), Arguments.of("Groovy allprojects of a looked-up rootProject behind &&", GROOVY, """
				plugins { id 'java' }
				false && project(':').rootProject.allprojects {
				    sourceSets.test.java.srcDir 'src/main/java'
				}
				"""), Arguments.of("Groovy rootProject inside subprojects where there is none", GROOVY, """
				plugins { id 'java' }
				subprojects {
				    rootProject.sourceSets.test.java.srcDir 'src/main/java'
				}
				"""));
	}

	/**
	 * A script applied from the project is read as part of it, also when it is
	 * applied twice in a row.
	 *
	 * @throws IOException if the fixture cannot be written
	 */
	@Test
	void readsAScriptAppliedFromTheProject() throws IOException {
		Files.createDirectories(projectRoot.resolve("gradle"));
		Files.writeString(projectRoot.resolve("gradle/roots.gradle"), "sourceSets.main.java.srcDir 'a'\n");

		assertRootsAndComplete(GROOVY, """
				plugins { id 'java' }
				apply from: 'gradle/roots.gradle'
				apply from: 'gradle/roots.gradle'
				""", List.of("a", MAIN), List.of(TEST));
	}

	/**
	 * The example exercise, with its compile-task configuration and its applied
	 * reserved-package script, declares nothing beyond the convention.
	 *
	 * @throws IOException if the example's root cannot be resolved
	 */
	@Test
	void readsTheExampleExerciseAsComplete() throws IOException {
		Path example = Path.of("examples/ares-exercise-gradle");
		assumeTrue(Files.isRegularFile(example.resolve("build.gradle")), "run from the repository root");

		BuildToolConfiguration configuration = ProjectSourcesFinder.discover(example);

		assertTrue(configuration.productionRootsComplete());
		assertEquals(List.of(example.resolve(MAIN).toRealPath()), configuration.productionSourceRoots());
	}

	/**
	 * An applied script that cannot be read as part of the project leaves main
	 * incomplete: one from a URL, a missing one, one reached through a link out of
	 * the project, and one that applies itself.
	 *
	 * @throws IOException if the fixture cannot be written
	 */
	@Test
	void marksMainIncompleteForAScriptItCannotRead() throws IOException {
		Files.createDirectories(projectRoot.resolve("gradle"));
		Files.writeString(projectRoot.resolve("gradle/self.gradle"), "apply from: 'gradle/self.gradle'\n");
		for (String target : List.of("'https://example.org/roots.gradle'", "'gradle/missing.gradle'",
				"'gradle/self.gradle'")) {
			assertFalse(
					discover(GROOVY, "plugins { id 'java' }\napply from: " + target + "\n").productionRootsComplete(),
					target);
		}
		Path outside = Files.createTempFile("ares-190-outside", ".gradle");
		try {
			Files.writeString(outside, "println 'outside'\n");
			Path link = projectRoot.resolve("gradle/outside.gradle");
			try {
				Files.createSymbolicLink(link, outside);
			} catch (UnsupportedOperationException | IOException exception) {
				assumeTrue(false, "symbolic links are not available here");
			}
			assertFalse(discover(GROOVY, "plugins { id 'java' }\napply from: 'gradle/outside.gradle'\n")
					.productionRootsComplete());
		} finally {
			Files.deleteIfExists(outside);
		}
	}

	/**
	 * A script applied under a condition, or from a location this reader cannot
	 * work out, may replace the test roots, so they are dropped, as Gradle 9.9.1
	 * does here.
	 *
	 * @throws IOException if the fixture cannot be written
	 */
	@Test
	void dropsTheTestRootsForAScriptItCannotPlace() throws IOException {
		Files.writeString(projectRoot.resolve("roots.gradle"), "sourceSets.test.java.setSrcDirs([])\n");
		for (String apply : List.of("if (true) {\n    apply from: 'roots.gradle'\n}\n",
				"apply from: ('roots' + '.gradle')\n")) {
			BuildToolConfiguration configuration = discover(GROOVY,
					"plugins { id 'java' }\nsourceSets.test.java.srcDir 'src/main/java'\n" + apply);

			assertFalse(configuration.testSourceRoots().contains(projectRoot.resolve(MAIN).toRealPath()), apply);
			assertFalse(configuration.productionRootsComplete(), apply);
		}
	}

	/**
	 * A script applied behind {@code &&} or under an {@code if} without braces may
	 * never be applied, as Gradle 9.9.1 confirms here, so what it declares leaves
	 * main incomplete instead of being read as certain.
	 *
	 * @throws IOException if the fixture cannot be written
	 */
	@Test
	void marksMainIncompleteForAScriptAppliedUnderACondition() throws IOException {
		Files.writeString(projectRoot.resolve("roots.gradle"), "sourceSets.main.java.setSrcDirs(['a'])\n");
		for (String apply : List.of("false && apply(from: 'roots.gradle')\n",
				"if (false) apply from: 'roots.gradle'\n")) {
			assertFalse(discover(GROOVY, "plugins { id 'java' }\n" + apply).productionRootsComplete(), apply);
		}
	}

	/**
	 * In the root project, {@code rootProject} is this project, also reached as
	 * {@code project.rootProject}, so a script applied through it is read, and its
	 * replacement empties the test roots, as Gradle 9.9.1 does here.
	 *
	 * @throws IOException if the fixture cannot be written
	 */
	@Test
	void readsAScriptAppliedThroughRootProjectInTheRootProject() throws IOException {
		Files.writeString(projectRoot.resolve("roots.gradle"), "sourceSets.test.java.setSrcDirs([])\n");
		for (String receiver : List.of("rootProject", "project.rootProject")) {
			assertRootsAndComplete(GROOVY, "plugins { id 'java' }\nsourceSets.test.java.srcDir 'src/main/java'\n"
					+ receiver + ".apply from: 'roots.gradle'\n", List.of(MAIN), List.of());
		}
	}

	/**
	 * The {@code rootProject} reached through another project is the root, as
	 * Gradle 9.9.1 shows here, but the receiver before it may be a collection or a
	 * call that returns null, so it is read as maybe this project: an applied
	 * script drops the test roots, a declaration adds none, and main is left
	 * incomplete, the cautious answers.
	 *
	 * @throws IOException if the fixture cannot be written
	 */
	@Test
	void readsTheRootProjectOfAnotherProjectAsMaybeThisProject() throws IOException {
		createSubproject();
		Files.writeString(projectRoot.resolve("roots.gradle"), "sourceSets.test.java.setSrcDirs([])\n");
		Files.writeString(projectRoot.resolve(GROOVY), """
				plugins { id 'java' }
				sourceSets.test.java.srcDir 'src/main/java'
				project(':child').rootProject.apply from: 'roots.gradle'
				""");

		BuildToolConfiguration applied = ProjectSourcesFinder.discover(projectRoot);

		assertEquals(List.of(), applied.testSourceRoots());
		assertFalse(applied.productionRootsComplete());

		Files.writeString(projectRoot.resolve(GROOVY),
				"plugins { id 'java' }\nproject(':child').rootProject.sourceSets.test.java.srcDir 'a'\n");

		BuildToolConfiguration declared = ProjectSourcesFinder.discover(projectRoot);

		assertEquals(List.of(projectRoot.resolve(TEST).toRealPath()), declared.testSourceRoots());
		assertFalse(declared.productionRootsComplete());
	}

	/**
	 * A script applied with a target of its own, as an argument or in a block, or
	 * through a receiver that only ends in {@code project}, may reach this project
	 * or another, so the test roots are dropped and main is left incomplete. Gradle
	 * 9.9.1 empties them for both targets and leaves them for the other receiver.
	 *
	 * @throws IOException if the fixture cannot be written
	 */
	@Test
	void dropsTheTestRootsForAScriptAppliedToATargetItCannotPlace() throws IOException {
		createSubproject();
		Files.writeString(projectRoot.resolve("empty.gradle"), "sourceSets.test.java.setSrcDirs([])\n");
		Files.writeString(projectRoot.resolve("adds.gradle"),
				"apply plugin: 'java'\nsourceSets.test.java.srcDir 'src/main/java'\n");
		for (String apply : List.of(
				"project(':child').apply(\n    from: file('empty.gradle'),\n    to: rootProject\n)\n",
				"def holder = [project: project(':child')]\nholder.project.apply from: file('adds.gradle')\n",
				"project(':child').apply {\n    from(rootProject.file('empty.gradle'))\n    to(rootProject)\n}\n")) {
			Files.writeString(projectRoot.resolve(GROOVY),
					"plugins { id 'java' }\nsourceSets.test.java.srcDir 'src/main/java'\n" + apply);

			BuildToolConfiguration configuration = ProjectSourcesFinder.discover(projectRoot);

			assertEquals(List.of(), configuration.testSourceRoots(), apply);
			assertFalse(configuration.productionRootsComplete(), apply);
		}
	}

	/**
	 * Inside {@code subprojects}, an explicit {@code rootProject} or {@code parent}
	 * leads back to the root, which Gradle 9.9.1 changes here, though the block
	 * runs only where a subproject exists. So the test roots are dropped and main
	 * is left incomplete.
	 *
	 * @throws IOException if the fixture cannot be written
	 */
	@Test
	void dropsTheTestRootsWhereSubprojectsLeadsBackToTheRoot() throws IOException {
		createSubproject();
		Files.writeString(projectRoot.resolve("empty.gradle"), "sourceSets.test.java.setSrcDirs([])\n");
		for (String change : List.of("rootProject.apply from: 'empty.gradle'",
				"rootProject.sourceSets.test.java.setSrcDirs([])", "parent.sourceSets.test.java.setSrcDirs([])")) {
			Files.writeString(projectRoot.resolve(GROOVY), "plugins { id 'java' }\nsourceSets.test.java.srcDir "
					+ "'src/main/java'\nsubprojects {\n    " + change + "\n}\n");

			BuildToolConfiguration configuration = ProjectSourcesFinder.discover(projectRoot);

			assertEquals(List.of(), configuration.testSourceRoots(), change);
			assertFalse(configuration.productionRootsComplete(), change);
		}
	}

	/**
	 * The {@code parent} of a project looked up by a computed path may be this
	 * project, as the root is for {@code :child} in Gradle 9.9.1 here, so a
	 * replacement or a script applied through it drops the test roots.
	 *
	 * @throws IOException if the fixture cannot be written
	 */
	@Test
	void dropsTheTestRootsWhereTheParentOfALookedUpProjectIsChanged() throws IOException {
		createSubproject();
		Files.writeString(projectRoot.resolve("empty.gradle"), "sourceSets.test.java.setSrcDirs([])\n");
		for (String change : List.of("sourceSets.test.java.setSrcDirs([])", "apply from: 'empty.gradle'")) {
			Files.writeString(projectRoot.resolve(GROOVY), "plugins { id 'java' }\nsourceSets.test.java.srcDir "
					+ "'src/main/java'\ndef target = ':child'\nproject(target).parent." + change + "\n");

			BuildToolConfiguration configuration = ProjectSourcesFinder.discover(projectRoot);

			assertEquals(List.of(), configuration.testSourceRoots(), change);
			assertFalse(configuration.productionRootsComplete(), change);
		}
	}

	/**
	 * A variable of the script named {@code rootProject} or {@code project},
	 * assigned, or bound in a tuple, a closure or a loop, may hold another project,
	 * as {@code :child} here, which Gradle 9.9.1 changes instead of the root. So
	 * neither what is applied through it nor what is declared on it gives a test
	 * root, and main is left incomplete.
	 *
	 * @throws IOException if the fixture cannot be written
	 */
	@Test
	void takesNoTestRootThroughAProjectVariableOfTheScript() throws IOException {
		createSubproject();
		Files.writeString(projectRoot.resolve("adds.gradle"),
				"apply plugin: 'java'\nsourceSets.test.java.srcDir 'src/main/java'\n");
		for (String use : List.of("def rootProject = project(':child')\nrootProject.apply from: file('adds.gradle')\n",
				"project(':child') { apply plugin: 'java' }\ndef rootProject = project(':child')\n"
						+ "rootProject.sourceSets.test.java.srcDir 'src/main/java'\n",
				"def project = project(':child')\nproject.apply from: file('adds.gradle')\n",
				"def (rootProject, unused) = [project(':child'), null]\nrootProject.apply from: file('adds.gradle')\n",
				"def (unused,\n     rootProject) = [null, project(':child')]\nrootProject.apply from: file('adds.gradle')\n",
				"[project(':child')].each { rootProject ->\n    rootProject.apply from: file('adds.gradle')\n}\n",
				"for (rootProject in [project(':child')]) {\n    rootProject.apply from: file('adds.gradle')\n}\n")) {
			Files.writeString(projectRoot.resolve(GROOVY), "plugins { id 'java' }\n" + use);

			BuildToolConfiguration configuration = ProjectSourcesFinder.discover(projectRoot);

			assertFalse(configuration.testSourceRoots().contains(projectRoot.resolve(MAIN).toRealPath()), use);
			assertFalse(configuration.productionRootsComplete(), use);
		}
	}

	/**
	 * Below a settings file of another directory, {@code rootProject} may be this
	 * project, so a script applied through it may replace the test roots. They are
	 * dropped and main is left incomplete, the cautious answer.
	 *
	 * @throws IOException if the fixture cannot be written
	 */
	@Test
	void dropsTheTestRootsForAScriptAppliedThroughAnUncertainRootProject() throws IOException {
		Path child = createSubproject();
		Files.writeString(child.resolve("roots.gradle"), "sourceSets.test.java.setSrcDirs([])\n");
		Files.writeString(child.resolve(GROOVY), "plugins { id 'java' }\nrootProject.apply from: 'roots.gradle'\n");

		BuildToolConfiguration configuration = ProjectSourcesFinder.discover(child);

		assertEquals(List.of(), configuration.testSourceRoots());
		assertFalse(configuration.productionRootsComplete());
	}

	/**
	 * In the root project, {@code project(':child')} is another project, so its
	 * replacement leaves this project's test roots as they are, as Gradle 9.9.1
	 * does here, while main is left incomplete.
	 *
	 * @throws IOException if the fixture cannot be written
	 */
	@Test
	void keepsTheTestRootsWhereAnotherProjectIsLookedUp() throws IOException {
		createSubproject();
		Files.writeString(projectRoot.resolve(GROOVY), """
				plugins { id 'java' }
				project(':child') {
				    apply plugin: 'java'
				    sourceSets.test.java.setSrcDirs([])
				}
				""");

		BuildToolConfiguration configuration = ProjectSourcesFinder.discover(projectRoot);

		assertEquals(List.of(projectRoot.resolve(TEST).toRealPath()), configuration.testSourceRoots());
		assertFalse(configuration.productionRootsComplete());
	}

	/**
	 * In a subproject, {@code parent} is another project, so a change on it leaves
	 * the subproject's own test roots as they are, as Gradle 9.9.1 confirms when
	 * run from the subproject's directory. Main stays incomplete, as for any other
	 * project's configuration.
	 *
	 * @throws IOException if the fixture cannot be written
	 */
	@Test
	void keepsASubprojectsTestRootsWhereItsParentIsChanged() throws IOException {
		Path child = createSubproject();
		for (String change : List.of("parent.sourceSets.test.java.setSrcDirs([])\n",
				"parent.sourceSets.main.java.srcDir 'a'\n", "parent. sourceSets.test.java.srcDir 'src/main/java'\n",
				"parent.\n    sourceSets.test.java.srcDir 'src/main/java'\n",
				"println(parent\n    .sourceSets.test.java.srcDir('src/main/java'))\n")) {
			Files.writeString(child.resolve(GROOVY), "plugins { id 'java' }\n" + change);

			BuildToolConfiguration configuration = ProjectSourcesFinder.discover(child);

			assertEquals(List.of(child.resolve(TEST).toRealPath()), configuration.testSourceRoots(), change);
			assertFalse(configuration.productionRootsComplete(), change);
		}
	}

	/**
	 * Below a settings file of another directory, {@code rootProject} may be the
	 * project itself, since that file may move the root project here, so a change
	 * on it leaves main incomplete, gives no test root, and drops the test roots
	 * where it replaces them. Gradle 9.9.1 keeps them in this layout; dropping them
	 * is the cautious answer.
	 *
	 * @throws IOException if the fixture cannot be written
	 */
	@Test
	void readsRootProjectAsPossiblyThisProjectBelowAnotherSettingsFile() throws IOException {
		Path child = createSubproject();
		for (String change : List.of("rootProject.sourceSets.test.java.setSrcDirs([])\n",
				"rootProject.sourceSets.test.java.srcDir 'src/main/java'\n")) {
			Files.writeString(child.resolve(GROOVY), "plugins { id 'java' }\n" + change);

			BuildToolConfiguration configuration = ProjectSourcesFinder.discover(child);

			assertFalse(configuration.testSourceRoots().contains(child.resolve(MAIN).toRealPath()), change);
			assertFalse(configuration.productionRootsComplete(), change);
		}
		Files.writeString(child.resolve(GROOVY),
				"plugins { id 'java' }\nrootProject.sourceSets.test.java.setSrcDirs([])\n");
		assertEquals(List.of(), ProjectSourcesFinder.discover(child).testSourceRoots());
	}

	/**
	 * In a subproject, a replacement Gradle 9.9.1 applies to the subproject's test
	 * set, run from the subproject's directory, replaces its test roots: through
	 * the {@code allprojects} or {@code subprojects} of an ancestor, also split
	 * over two lines, and behind a condition on {@code parent}.
	 *
	 * @throws IOException if the fixture cannot be written
	 */
	@Test
	void replacesASubprojectsTestRootsWhereGradleDoes() throws IOException {
		Path child = createSubproject();
		for (String change : List.of("rootProject.allprojects { sourceSets.test.java.setSrcDirs([]) }\n",
				"rootProject.subprojects { sourceSets.test.java.setSrcDirs([]) }\n",
				"parent.allprojects { sourceSets.test.java.setSrcDirs([]) }\n",
				"parent\n    .allprojects { sourceSets.test.java.setSrcDirs([]) }\n",
				"parent.name != null && sourceSets.test.java.setSrcDirs([])\n",
				"false ? parent.name : sourceSets.test.java.setSrcDirs([])\n",
				"parent?.allprojects { sourceSets.test.java.setSrcDirs([]) }\n",
				"println(parent?.allprojects { sourceSets.test.java.setSrcDirs([]) })\n",
				"configure([project, parent]) { sourceSets.test.java.setSrcDirs([]) }\n")) {
			Files.writeString(child.resolve(GROOVY), "plugins { id 'java' }\n" + change);

			assertEquals(List.of(), ProjectSourcesFinder.discover(child).testSourceRoots(), change);
		}
	}

	/**
	 * In a Kotlin subproject, {@code with(rootProject)} may configure another
	 * project, so what its block declares gives no test root and leaves main
	 * incomplete. Gradle 9.9.1, run from the subproject's directory, adds the
	 * folder to the root project only.
	 *
	 * @throws IOException if the fixture cannot be written
	 */
	@Test
	void takesNoTestRootFromWithRootProjectInASubproject() throws IOException {
		Path child = createSubproject();
		Files.writeString(child.resolve(KOTLIN),
				"plugins { java }\nwith(rootProject) { sourceSets.test { java.srcDir(\"src/main/java\") } }\n");

		BuildToolConfiguration configuration = ProjectSourcesFinder.discover(child);

		assertEquals(List.of(child.resolve(TEST).toRealPath()), configuration.testSourceRoots());
		assertFalse(configuration.productionRootsComplete());
	}

	/**
	 * In a grandchild, the {@code allprojects} of {@code parent.parent} include it,
	 * so a replacement there replaces its test roots, as Gradle 9.9.1 confirms when
	 * run from the grandchild's directory.
	 *
	 * @throws IOException if the fixture cannot be written
	 */
	@Test
	void replacesAGrandchildsTestRootsThroughItsGrandparentsAllprojects() throws IOException {
		Files.writeString(projectRoot.resolve("settings.gradle"), "include 'child', 'child:grand'\n");
		Path grand = projectRoot.resolve("child/grand");
		Files.createDirectories(grand.resolve(TEST));
		Files.writeString(projectRoot.resolve(GROOVY), "plugins { id 'java' }\n");
		Files.writeString(projectRoot.resolve("child").resolve(GROOVY), "plugins { id 'java' }\n");
		Files.writeString(grand.resolve(GROOVY),
				"plugins { id 'java' }\nparent.parent.allprojects { sourceSets.test.java.setSrcDirs([]) }\n");

		assertEquals(List.of(), ProjectSourcesFinder.discover(grand).testSourceRoots());
	}

	/**
	 * Writes a build whose settings file includes {@code child}, with that
	 * subproject's conventional directories.
	 *
	 * @return the subproject's directory
	 * @throws IOException if the fixture cannot be written
	 */
	private Path createSubproject() throws IOException {
		Files.writeString(projectRoot.resolve("settings.gradle"), "include 'child'\n");
		Files.writeString(projectRoot.resolve(GROOVY), "plugins { id 'java' }\n");
		Path child = projectRoot.resolve("child");
		Files.createDirectories(child.resolve(MAIN));
		Files.createDirectories(child.resolve(TEST));
		return child;
	}

	/**
	 * A settings file beside the descriptor makes it the root project, so
	 * {@code rootProject} is the project being read, as Gradle 9.9.1 confirms.
	 *
	 * @throws IOException if the fixture cannot be written
	 */
	@Test
	void readsRootProjectAsThisProjectBesideItsSettingsFile() throws IOException {
		Files.writeString(projectRoot.resolve("settings.gradle"), "rootProject.name = 'x'\n");

		assertRootsAndComplete(GROOVY, """
				plugins { id 'java' }
				rootProject.sourceSets.main.java.srcDir 'a'
				rootProject.sourceSets.test.java.setSrcDirs([])
				""", List.of("a", MAIN), List.of());
	}

	/**
	 * A variable of the applying script is not visible in the applied one, which
	 * reads the gradle.properties entry instead, as Gradle 9.9.1 does with
	 * {@code b} here.
	 *
	 * @throws IOException if the fixture cannot be written
	 */
	@Test
	void readsAnAppliedScriptWithoutTheVariablesOfTheScriptApplyingIt() throws IOException {
		Files.writeString(projectRoot.resolve("gradle.properties"), "directory=b\n");
		Files.writeString(projectRoot.resolve("roots.gradle"), "sourceSets.main.java.setSrcDirs([directory])\n");

		assertRootsAndComplete(GROOVY, """
				plugins { id 'java' }
				def directory = 'a'
				apply from: 'roots.gradle'
				""", List.of("b"), List.of(TEST));
	}

	/**
	 * An assignment without {@code def} in an applied script overwrites the project
	 * property every later script reads, so neither the gradle.properties value nor
	 * one the applying script set earlier is taken; Gradle 9.9.1 uses {@code b}
	 * here in both cases.
	 *
	 * @throws IOException if the fixture cannot be written
	 */
	@Test
	void forgetsAPropertyAnAppliedScriptOverwrites() throws IOException {
		Files.writeString(projectRoot.resolve("gradle.properties"), "directory=a\n");
		Files.writeString(projectRoot.resolve("roots.gradle"), "directory = 'b'\n");

		for (String before : List.of("", "directory = 'a'\n")) {
			BuildToolConfiguration configuration = discover(GROOVY, "plugins { id 'java' }\n" + before
					+ "apply from: 'roots.gradle'\nsourceSets.main.java.setSrcDirs([directory])\n");

			assertFalse(configuration.productionSourceRoots().contains(projectRoot.resolve("a").toRealPath()), before);
		}
	}

	/**
	 * A local variable named {@code projectDir} hides the project directory in an
	 * interpolation, so {@code "${projectDir}/b"} names {@code a/b}, as Gradle
	 * 9.9.1 confirms.
	 *
	 * @throws IOException if the fixture cannot be written
	 */
	@Test
	void letsALocalProjectDirHideTheProjectDirectory() throws IOException {
		Files.createDirectories(projectRoot.resolve("b"));
		Files.createDirectories(projectRoot.resolve("a/b"));

		assertRootsAndComplete(GROOVY, """
				plugins { id 'java' }
				def projectDir = 'a'
				sourceSets.main.java.setSrcDirs(["${projectDir}/b"])
				""", List.of("a/b"), List.of(TEST));
	}

	/**
	 * A literal assigned inside a block is that block's own variable, so a name
	 * read outside it is the project property instead, as Gradle 9.9.1 confirms
	 * with {@code b} here.
	 *
	 * @throws IOException if the fixture cannot be written
	 */
	@Test
	void doesNotTakeAVariableOfAnInnerBlockForTheProperty() throws IOException {
		Files.writeString(projectRoot.resolve("gradle.properties"), "directory=b\n");

		BuildToolConfiguration configuration = discover(GROOVY, """
				plugins { id 'java' }
				def unused = {
				    def directory = 'a'
				}
				sourceSets.main.java.setSrcDirs([directory])
				""");

		assertFalse(configuration.productionSourceRoots().contains(projectRoot.resolve("a").toRealPath()));
	}

	/**
	 * An empty source set name, which Gradle need not accept, is read without an
	 * exception and leaves main incomplete.
	 *
	 * @throws IOException if the fixture cannot be written
	 */
	@Test
	void readsAnEmptySourceSetNameWithoutFailing() throws IOException {
		assertFalse(discover(GROOVY, "plugins { id 'java' }\nsourceSets[''].java.setSrcDirs([])\n")
				.productionRootsComplete());
	}

	/**
	 * A buildSrc directory holds build logic this reader does not read, so main is
	 * no longer known to be complete.
	 *
	 * @throws IOException if the fixture cannot be written
	 */
	@Test
	void marksMainIncompleteBesideBuildSrc() throws IOException {
		Files.createDirectories(projectRoot.resolve("buildSrc"));

		assertFalse(discover(GROOVY, "plugins { id 'java' }\n").productionRootsComplete());
	}

	/**
	 * A form row: label, descriptor name, descriptor, main roots and test roots.
	 *
	 * @param label      what the form is
	 * @param descriptor the descriptor's name
	 * @param content    the descriptor
	 * @param main       the main roots Gradle reported
	 * @param test       the test roots Gradle reported
	 * @return the row
	 */
	private static Arguments form(String label, String descriptor, String content, List<String> main,
			List<String> test) {
		return Arguments.of(label, descriptor, content, main, test);
	}

	/**
	 * Discovers the project with this descriptor and checks its roots, which are
	 * compared sorted, as the configuration keeps them.
	 *
	 * @param descriptor the descriptor's name
	 * @param content    the descriptor
	 * @param main       the expected main roots
	 * @param test       the expected test roots
	 * @throws IOException if the fixture cannot be written
	 */
	private void assertRootsAndComplete(String descriptor, String content, List<String> main, List<String> test)
			throws IOException {
		BuildToolConfiguration configuration = discover(descriptor, content);

		assertEquals(realPaths(main), configuration.productionSourceRoots().stream().sorted().toList());
		assertEquals(realPaths(test), configuration.testSourceRoots().stream().sorted().toList());
		assertTrue(configuration.productionRootsComplete());
	}

	/**
	 * Writes the descriptor and discovers the project.
	 *
	 * @param descriptor the descriptor's name
	 * @param content    the descriptor
	 * @return the discovered configuration
	 * @throws IOException if the descriptor cannot be written
	 */
	private BuildToolConfiguration discover(String descriptor, String content) throws IOException {
		Files.writeString(projectRoot.resolve(descriptor), content);
		return ProjectSourcesFinder.discover(projectRoot);
	}

	/**
	 * The canonical paths of these project directories, sorted.
	 *
	 * @param relative the directories, relative to the project
	 * @return their real paths
	 * @throws IOException if one cannot be resolved
	 */
	private List<Path> realPaths(List<String> relative) throws IOException {
		List<Path> paths = new ArrayList<>();
		for (String directory : relative) {
			paths.add(projectRoot.resolve(directory).toRealPath());
		}
		return paths.stream().sorted().toList();
	}
}
