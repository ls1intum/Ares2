package de.tum.cit.ase.ares.api.ast.model;

import com.github.javaparser.ast.CompilationUnit;
import org.apiguardian.api.API;
import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.traverse.DepthFirstIterator;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;


/**
 * Create a graph of method calls from a CompilationUnit
 */
@API(status = API.Status.INTERNAL)
public class MethodCallGraph {
    private final Graph<String, DefaultEdge> graph;

    private final Set<String> excludedMethodIdentifiers;

    public MethodCallGraph(Set<String> excludedMethods) {
        this.graph = new DefaultDirectedGraph<>(DefaultEdge.class);
        this.excludedMethodIdentifiers = excludedMethods;
    }

    /**
     * Create a graph from the given CompilationUnit
     *
     * @param cu CompilationUnit to be parsed
     */
    public void createGraph(CompilationUnit cu) {
        cu.accept(new VisitorAdapter(graph, excludedMethodIdentifiers), null);
    }

    /**
     * Extracts a subgraph from the given graph starting from the specified vertex.
     * This subgraph includes all vertices and edges reachable from the start vertex
     * using a depth-first search traversal.
     *
     * @param startVertex the vertex from which to start the subgraph extraction
     * @return a subgraph containing all vertices and edges reachable from the start vertex
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

    public Graph<String, DefaultEdge> getGraph() {
        return graph;
    }
}