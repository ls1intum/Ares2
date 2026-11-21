package de.tum.cit.ase.ares.api.ast.model;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.github.javaparser.ParserConfiguration;

import de.tum.cit.ase.ares.api.ast.AstSourceFiles;

/**
 * Checks which methods and calls {@link VisitorAdapter} puts into the call
 * graph, read through {@link RecursionCheck#createMethodCallGraph}.
 */
class VisitorAdapterTest {

	/**
	 * A fresh folder for the files of one test.
	 */
	@TempDir
	Path directory;

	/**
	 * Writes class {@code p.A} with the given members and reads its call graph.
	 */
	private MethodCallGraph graphOf(String members, Set<String> excludedMethods) throws IOException {
		AstSourceFiles.write(directory, "p/A.java", "package p;\nclass A {\n" + members + "\n}\n");
		return RecursionCheck.createMethodCallGraph(directory, ParserConfiguration.LanguageLevel.JAVA_17,
				excludedMethods);
	}

	/**
	 * The calls inside an excluded method are left out, while the call to it stays.
	 */
	@Test
	void doesNotFollowCallsOutOfAnExcludedMethod() throws IOException {
		String members = "void a() { b(); }\nvoid b() { c(); }\nvoid c() { }";

		MethodCallGraph excluded = graphOf(members, Set.of("p.A.b()"));

		assertThat(excluded.getGraph().containsEdge("p.A.a()", "p.A.b()")).isTrue();
		assertThat(excluded.getGraph().containsEdge("p.A.b()", "p.A.c()")).isFalse();
		assertThat(graphOf(members, Set.of()).getGraph().containsEdge("p.A.b()", "p.A.c()")).isTrue();
	}

	/**
	 * Methods that are called before and after their declaration all get a
	 * position.
	 */
	@Test
	void recordsAPositionForEveryMethod() throws IOException {
		MethodCallGraph methodCallGraph = graphOf("void a() { b(); }\nvoid b() { }\nvoid c() { }\nvoid d() { c(); }",
				Set.of());

		assertThat(methodCallGraph.getGraph().vertexSet()).hasSize(4);
		assertThat(methodCallGraph.getPositionMap().keySet())
				.containsExactlyInAnyOrderElementsOf(methodCallGraph.getGraph().vertexSet());
	}

	/**
	 * Methods of a class declared inside another method are found too.
	 */
	@Test
	void visitsMethodsNestedInsideOtherMethods() throws IOException {
		MethodCallGraph methodCallGraph = graphOf(
				"void outer() { new Runnable() { public void run() { inner(); } void inner() { } }; }", Set.of());

		assertThat(methodCallGraph.getGraph().vertexSet()).anyMatch(vertex -> vertex.endsWith(".inner()"));
		assertThat(methodCallGraph.getGraph().vertexSet()).anyMatch(vertex -> vertex.endsWith(".run()"));
	}

	/**
	 * Each call in a method becomes one edge.
	 */
	@Test
	void addsAnEdgeForEveryCall() throws IOException {
		MethodCallGraph methodCallGraph = graphOf("void a() { b(); c(); }\nvoid b() { }\nvoid c() { }", Set.of());

		assertThat(methodCallGraph.getGraph().edgeSet()).hasSize(2);
		assertThat(methodCallGraph.getGraph().containsEdge("p.A.a()", "p.A.b()")).isTrue();
		assertThat(methodCallGraph.getGraph().containsEdge("p.A.a()", "p.A.c()")).isTrue();
	}
}
