package de.tum.cit.ase.ares.api.architecture.java;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;

import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import com.ibm.wala.ipa.callgraph.CallGraph;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;

import de.tum.cit.ase.ares.api.architecture.java.archunit.JavaArchunitTestCaseCollection;
import de.tum.cit.ase.ares.api.architecture.java.wala.CustomCallgraphBuilder;
import de.tum.cit.ase.ares.api.architecture.java.wala.JavaWalaTestCaseCollection;

/**
 * Checks that the static thread rule still refuses the thread builders of Java
 * 21 once no bare {@code java.lang.Thread} entry covers them. Each builder call
 * sits in its own class, typed on the builder, so no other forbidden call can
 * cause the refusal. The sources are compiled while the test runs, since the
 * test sources themselves compile against Java 17.
 */
class ThreadBuilderStaticDenialTest {

	/** The first Java release that has the thread builders. */
	private static final int BUILDER_RELEASE = 21;

	/** The package of every compiled fixture. */
	private static final String FIXTURE_PACKAGE = "student.builder";

	/** Folder the fixtures are compiled into. */
	@TempDir
	Path temporaryDirectory;

	/**
	 * Compiles one builder call and expects the ArchUnit rule and the WALA direct
	 * pass each to refuse it, and WALA as a whole, which runs the direct pass
	 * before the call-graph pass. The call-graph pass alone does not see a call on
	 * a builder handed in as a parameter, with or without a bare entry.
	 *
	 * @param body the statement returning the result of the builder call
	 * @throws Exception if the fixture cannot be written or compiled
	 */
	@ParameterizedTest
	@ValueSource(strings = { "Thread.Builder builder, Runnable task) { return builder.unstarted(task);",
			"Thread.Builder.OfVirtual builder, Runnable task) { return builder.start(task);",
			"Thread.Builder.OfVirtual builder, Runnable task) { return builder.unstarted(task);",
			"Thread.Builder.OfPlatform builder, Runnable task) { return builder.factory();",
			"Thread.Builder.OfPlatform builder, Runnable task) { return builder.daemon(true);",
			"Thread.Builder builder, Runnable task) { return Thread.ofVirtual();" })
	void everyBuilderCallIsRefused(String body) throws Exception {
		Assumptions.assumeTrue(Runtime.version().feature() >= BUILDER_RELEASE,
				"the thread builders need Java " + BUILDER_RELEASE);
		Path packageDirectory = compileFixture(body);
		JavaClasses classes = new ClassFileImporter().importPath(packageDirectory);
		assertThat(classes).isNotEmpty();
		assertThatThrownBy(() -> JavaArchunitTestCaseCollection.NO_CLASS_MUST_CREATE_THREADS.check(classes))
				.isInstanceOf(AssertionError.class);
		assertThatThrownBy(
				() -> JavaWalaTestCaseCollection.NO_CLASS_MUST_CREATE_THREADS.checkDirectAccesses(classes, Set.of()))
						.isInstanceOf(AssertionError.class);
		String classPath = packageDirectory.toString();
		CustomCallgraphBuilder builder = new CustomCallgraphBuilder(classPath);
		assertThatThrownBy(() -> runBothWalaPasses(classes, builder.buildCallGraph(classPath)))
				.isInstanceOf(AssertionError.class);
	}

	/**
	 * Runs the WALA thread rule the way a WALA test case does: the direct pass over
	 * the imported classes, then the call-graph pass.
	 *
	 * @param classes   the imported fixture classes
	 * @param callGraph the call graph of the fixture
	 */
	private static void runBothWalaPasses(JavaClasses classes, CallGraph callGraph) {
		JavaWalaTestCaseCollection.NO_CLASS_MUST_CREATE_THREADS.checkDirectAccesses(classes, Set.of(),
				callGraph.getClassHierarchy());
		JavaWalaTestCaseCollection.NO_CLASS_MUST_CREATE_THREADS.check(callGraph, Set.of());
	}

	/**
	 * Writes and compiles a fixture class whose only method makes one builder call,
	 * failing the test if the compiler reports an error.
	 *
	 * @param body the parameters and statement of the fixture method
	 * @return the folder holding the compiled package
	 * @throws Exception if the fixture cannot be written
	 */
	private Path compileFixture(String body) throws Exception {
		Path sourceDirectory = Files.createDirectories(temporaryDirectory.resolve("src"));
		Path outputDirectory = Files.createDirectories(temporaryDirectory.resolve("classes"));
		Path source = Files.writeString(sourceDirectory.resolve("BuilderCall.java"), "package " + FIXTURE_PACKAGE
				+ ";\npublic final class BuilderCall {\n" + "  public static Object call(" + body + " }\n}\n");
		JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
		assertThat(compiler).as("compiling the fixtures needs a JDK").isNotNull();
		ByteArrayOutputStream diagnostics = new ByteArrayOutputStream();
		int result = compiler.run(null, null, new PrintStream(diagnostics, true, StandardCharsets.UTF_8), "--release",
				Integer.toString(BUILDER_RELEASE), "-proc:none", "-d", outputDirectory.toString(), source.toString());
		assertThat(result).as(diagnostics.toString(StandardCharsets.UTF_8)).isZero();
		return outputDirectory.resolve(FIXTURE_PACKAGE.replace('.', '/'));
	}
}
