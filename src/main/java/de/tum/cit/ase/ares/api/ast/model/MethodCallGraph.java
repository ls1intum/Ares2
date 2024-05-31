package de.tum.cit.ase.ares.api.ast.model;

import com.github.javaparser.ast.CompilationUnit;
import org.apiguardian.api.API;
import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.nio.dot.DOTExporter;
import org.jgrapht.traverse.BreadthFirstIterator;
import org.jgrapht.traverse.DepthFirstIterator;

import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import static de.tum.cit.ase.ares.api.ast.model.MethodCallGraphGenerator.renderVertexName;

/**
 * Create a graph of method calls from a CompilationUnit
 */
@API(status = API.Status.INTERNAL)
public class MethodCallGraph {
    private final Graph<String, DefaultEdge> graph;

    private final Set<String> excludedMethodIdentifiers;

    private final int depthLimit;

    public MethodCallGraph(int depthLimit, String... excludedMethods) {
        this.graph = new DefaultDirectedGraph<>(DefaultEdge.class);
        this.depthLimit = depthLimit;
        this.excludedMethodIdentifiers = new HashSet<>(List.of(excludedMethods));
    }

    /**
     * Create a graph from the given CompilationUnit
     *
     * @param cu CompilationUnit to be parsed
     */
    public void createGraph(CompilationUnit cu) {
        cu.accept(new VisitorAdapter(graph, excludedMethodIdentifiers, depthLimit), null);
    }

    /**
     * Extract a subgraph from the given graph starting from the given vertex
     *
     * @param startVertex Vertex to start the extraction from
     * @return Subgraph of the given graph
     */
    public Graph<String, DefaultEdge> extractSubgraph(String startVertex) {
        DefaultDirectedGraph<String, DefaultEdge> subgraph = new DefaultDirectedGraph<>(null, graph.getEdgeSupplier(), false);

        // Set to keep track of visited vertices
        Set<String> visited = new HashSet<>();

        // Initialize DepthFirstIterator
        Iterator<String> iterator = new DepthFirstIterator<>(graph, startVertex);

        // Add start vertex to subgraph
        subgraph.addVertex(startVertex);
        visited.add(startVertex);

        // Iterate through the graph
        while (iterator.hasNext()) {
            String vertex = iterator.next();
            // Add vertex to subgraph if not already visited
            if (!visited.contains(vertex)) {
                subgraph.addVertex(vertex);
                visited.add(vertex);
            }
            // Add edges to subgraph
            graph.edgesOf(vertex).forEach(edge -> {
                String source = graph.getEdgeSource(edge);
                String target = graph.getEdgeTarget(edge);
                if (visited.contains(source) && visited.contains(target)) {
                    subgraph.addEdge(source, target);
                }
            });
        }

        return subgraph;
    }

    public boolean methodCallsMethod(String startVertex, String endVertex) {
        // Initialize DepthFirstIterator
        Iterator<String> iterator = new BreadthFirstIterator<>(graph, startVertex);

        Set<DefaultEdge> test = graph.edgeSet();

        // Iterate through the graph
        while (iterator.hasNext()) {
            String vertex = iterator.next();
            if (vertex.equals(endVertex)) {
                return true;
            }
        }

        return false;
    }

    public void exportToDotFile(String filePath) {
        DOTExporter<String, DefaultEdge> exporter = new DOTExporter<>(s -> s);
        try (Writer writer = new FileWriter(filePath)) {
            exporter.exportGraph(graph, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Graph<String, DefaultEdge> getGraph() {
        return graph;
    }
}