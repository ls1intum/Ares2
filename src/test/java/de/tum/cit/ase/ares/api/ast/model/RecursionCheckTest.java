package de.tum.cit.ase.ares.api.ast.model;

import static de.tum.cit.ase.ares.api.localization.Messages.localized;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;

import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.github.javaparser.ParserConfiguration;
import com.github.javaparser.ast.CompilationUnit;

import de.tum.cit.ase.ares.api.ast.AstSourceFiles;

/**
 * Checks how {@link RecursionCheck} builds the call graph of a source folder
 * and reports the methods of a cycle. It builds its own parser, so the static
 * parser configuration is not touched.
 */
class RecursionCheckTest {

	/**
	 * The language level used for every fixture.
	 */
	private static final ParserConfiguration.LanguageLevel LEVEL = ParserConfiguration.LanguageLevel.JAVA_17;

	/**
	 * A fresh folder for the files of one test.
	 */
	@TempDir
	Path directory;

	/**
	 * Writes class {@code p.A} with the given members into the source folder.
	 */
	private void writeA(String members) throws IOException {
		AstSourceFiles.write(directory, "p/A.java", "package p;\nclass A {\n" + members + "\n}\n");
	}

	/**
	 * Splits the report of one method into its lines.
	 */
	private static List<String> linesOf(String report) {
		return List.of(report.split(Pattern.quote(System.lineSeparator()), -1));
	}

	/**
	 * The private constructor refuses to build an object.
	 */
	@Test
	void cannotBeInstantiated() throws NoSuchMethodException {
		Constructor<RecursionCheck> constructor = RecursionCheck.class.getDeclaredConstructor();
		constructor.setAccessible(true);

		assertThatThrownBy(constructor::newInstance).isInstanceOf(InvocationTargetException.class).cause()
				.isInstanceOf(SecurityException.class).hasMessage(localized("ast.hide.implicit.constructor"));
	}

	/**
	 * Two methods calling each other are reported, each with its own block.
	 */
	@Test
	void findsACycleAndDescribesEachMember() throws IOException {
		writeA("void a() { b(); }\nvoid b() { a(); }");
		MethodCallGraph graph = RecursionCheck.createMethodCallGraph(directory, LEVEL, Set.of());

		Optional<String> report = RecursionCheck.hasNoCycle(directory, LEVEL, null, Set.of());

		assertThat(report).isPresent();
		assertThat(report.get()).contains(RecursionCheck.formatVertexInfoWithinCycle("p.A.a()", graph, directory))
				.contains(RecursionCheck.formatVertexInfoWithinCycle("p.A.b()", graph, directory));
	}

	/**
	 * Calls that never come back give no report.
	 */
	@Test
	void findsNoCycleInStraightCalls() throws IOException {
		writeA("void a() { b(); }\nvoid b() { }");

		assertThat(RecursionCheck.hasNoCycle(directory, LEVEL, null, Set.of())).isEmpty();
	}

	/**
	 * Only the methods reachable from the starting method are searched.
	 */
	@Test
	void startingMethodLimitsTheSearch() throws IOException {
		writeA("void a() { b(); }\nvoid b() { a(); }\nvoid c() { d(); }\nvoid d() { }");

		assertThat(RecursionCheck.hasNoCycle(directory, LEVEL, "p.A.c()", Set.of())).isEmpty();
		assertThat(RecursionCheck.hasNoCycle(directory, LEVEL, "p.A.a()", Set.of())).isPresent();
	}

	/**
	 * Calls from one file into another file of the source folder are resolved.
	 */
	@Test
	void resolvesCallsAcrossFilesInTheSourceFolder() throws IOException {
		AstSourceFiles.write(directory, "p/A.java", "package p;\nclass A {\nstatic void a() { B.b(); }\n}\n");
		AstSourceFiles.write(directory, "p/B.java", "package p;\nclass B {\nstatic void b() { A.a(); }\n}\n");

		assertThat(RecursionCheck.hasNoCycle(directory, LEVEL, null, Set.of())).isPresent();
	}

	/**
	 * Calls into a class that is only on the class path are resolved.
	 */
	@Test
	void resolvesCallsIntoTheClassPath() throws IOException {
		writeA("void a() { de.tum.cit.ase.ares.api.ast.model.NodePosition.getPositionOf(null); }");

		MethodCallGraph graph = RecursionCheck.createMethodCallGraph(directory, LEVEL, Set.of());

		assertThat(graph.getGraph().vertexSet())
				.anyMatch(vertex -> vertex.startsWith("de.tum.cit.ase.ares.api.ast.model.NodePosition.getPositionOf("));
	}

	/**
	 * Every Java file of the source folder gives a compilation unit.
	 */
	@Test
	void parseFromSourceRootReturnsEveryFile() throws IOException {
		AstSourceFiles.write(directory, "p/A.java", "package p;\nclass A { }\n");
		AstSourceFiles.write(directory, "p/B.java", "package p;\nclass B { }\n");

		List<Optional<CompilationUnit>> units = RecursionCheck.parseFromSourceRoot(directory, LEVEL);

		assertThat(units).hasSize(2).allMatch(Optional::isPresent);
	}

	/**
	 * A file that cannot be read stops the check with a refusal that names the
	 * source folder and keeps the cause.
	 */
	@Test
	void reportsASourceFolderThatCannotBeRead() throws IOException {
		Assumptions.assumeTrue(FileSystems.getDefault().supportedFileAttributeViews().contains("posix"));
		writeA("void a() { }");
		Path file = directory.resolve("p").resolve("A.java");
		Set<PosixFilePermission> before = Files.getPosixFilePermissions(file);
		try {
			Files.setPosixFilePermissions(file, PosixFilePermissions.fromString("---------"));
			Assumptions.assumeFalse(Files.isReadable(file));

			assertThatThrownBy(() -> RecursionCheck.parseFromSourceRoot(directory, LEVEL))
					.isInstanceOf(SecurityException.class)
					.hasMessage(localized("security.ast.source.root.unreadable", directory)).cause()
					.isInstanceOf(IOException.class);
		} finally {
			Files.setPosixFilePermissions(file, before);
		}
	}

	/**
	 * The graph holds the methods of every parsed file.
	 */
	@Test
	void createMethodCallGraphUsesEveryParsedFile() throws IOException {
		AstSourceFiles.write(directory, "p/A.java", "package p;\nclass A {\nvoid a() { }\n}\n");
		AstSourceFiles.write(directory, "p/B.java", "package p;\nclass B {\nvoid b() { }\n}\n");

		MethodCallGraph graph = RecursionCheck.createMethodCallGraph(directory, LEVEL, Set.of());

		assertThat(graph.getGraph().vertexSet()).containsExactlyInAnyOrder("p.A.a()", "p.B.b()");
	}

	/**
	 * The class file name is the last class name before a method name.
	 */
	@Test
	void extractClassNameTakesTheClassBeforeTheMethod() {
		assertThat(RecursionCheck.extractClassName("p.A.a()")).isEqualTo("A.java");
		assertThat(RecursionCheck.extractClassName("p.q.Outer.run(java.lang.String)")).isEqualTo("Outer.java");
		assertThat(RecursionCheck.extractClassName("noMatch")).isNull();
	}

	/**
	 * The report of a method has the file line, the heading and the method line.
	 * The file path is not compared as a whole.
	 */
	@Test
	void describesAMemberOfACycle() throws IOException {
		writeA("void a() { b(); }\nvoid b() { a(); }");
		MethodCallGraph graph = RecursionCheck.createMethodCallGraph(directory, LEVEL, Set.of());
		String filePrefix = localized("ast.method.get_formatted_file_string_prefix", "@@@");
		filePrefix = filePrefix.substring(0, filePrefix.indexOf("@@@"));

		List<String> lines = linesOf(RecursionCheck.formatVertexInfoWithinCycle("p.A.a()", graph, directory));

		assertThat(lines).hasSize(4);
		assertThat(lines.get(0)).startsWith(filePrefix).endsWith("A.java:");
		assertThat(lines.get(1))
				.isEqualTo(localized("ast.method.get_formatted_unwanted_node_string_prefix", "Unwanted Recursion"));
		assertThat(lines.get(2)).isEqualTo(
				localized("ast.check.begin_end_prefix", graph.getPositionMap().get("p.A.a()").toString(), "p.A.a()"));
		assertThat(lines.get(3)).isEmpty();
	}

	/**
	 * A method without a recorded position cannot be reported.
	 */
	@Test
	void refusesAVertexWithoutPosition() {
		MethodCallGraph graph = new MethodCallGraph(Set.of());
		graph.getGraph().addVertex("p.A.a()");

		assertThatThrownBy(() -> RecursionCheck.formatVertexInfoWithinCycle("p.A.a()", graph, directory))
				.isInstanceOf(SecurityException.class).hasMessage(localized("ast.node.position.not.determined"));
	}
}
