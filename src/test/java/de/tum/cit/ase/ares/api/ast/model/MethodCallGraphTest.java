package de.tum.cit.ase.ares.api.ast.model;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Set;

import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultEdge;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.github.javaparser.ParserConfiguration;
import com.github.javaparser.ast.CompilationUnit;

import de.tum.cit.ase.ares.api.ast.AstSourceFiles;

/**
 * Checks how {@link MethodCallGraph} cuts out a subgraph and reads a
 * compilation unit.
 */
class MethodCallGraphTest {

	/**
	 * A fresh folder for the files of one test.
	 */
	@TempDir
	Path directory;

	/**
	 * Adds the call from one method to another, and both methods if needed.
	 */
	private static void addCall(Graph<String, DefaultEdge> graph, String caller, String callee) {
		graph.addVertex(caller);
		graph.addVertex(callee);
		graph.addEdge(caller, callee);
	}

	/**
	 * Only methods that can be reached from the start, and the calls between them,
	 * are kept.
	 */
	@Test
	void extractSubgraphKeepsOnlyWhatIsReachable() {
		MethodCallGraph methodCallGraph = new MethodCallGraph(Set.of());
		Graph<String, DefaultEdge> graph = methodCallGraph.getGraph();
		addCall(graph, "a", "b");
		addCall(graph, "b", "a");
		addCall(graph, "c", "a");
		addCall(graph, "b", "d");

		Graph<String, DefaultEdge> subgraph = methodCallGraph.extractSubgraph("a");

		assertThat(subgraph.vertexSet()).containsExactlyInAnyOrder("a", "b", "d");
		assertThat(subgraph.edgeSet()).hasSize(3);
		assertThat(subgraph.containsEdge("a", "b")).isTrue();
		assertThat(subgraph.containsEdge("b", "a")).isTrue();
		assertThat(subgraph.containsEdge("b", "d")).isTrue();
		assertThat(subgraph.containsEdge("c", "a")).isFalse();
	}

	/**
	 * Reading a compilation unit gives a method for each declaration, the call
	 * between them and a position for each method.
	 */
	@Test
	void createGraphReadsTheCompilationUnit() throws IOException {
		AstSourceFiles.write(directory, "p/A.java", "package p;\nclass A {\nvoid a() { b(); }\nvoid b() { }\n}\n");
		CompilationUnit unit = RecursionCheck.parseFromSourceRoot(directory, ParserConfiguration.LanguageLevel.JAVA_17)
				.get(0).orElseThrow();
		MethodCallGraph methodCallGraph = new MethodCallGraph(Set.of());

		methodCallGraph.createGraph(unit);

		assertThat(methodCallGraph.getGraph().vertexSet()).containsExactlyInAnyOrder("p.A.a()", "p.A.b()");
		assertThat(methodCallGraph.getGraph().containsEdge("p.A.a()", "p.A.b()")).isTrue();
		assertThat(methodCallGraph.getPositionMap()).containsOnlyKeys("p.A.a()", "p.A.b()");
	}
}
