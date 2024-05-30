package de.tum.cit.ase.ares.api.ast.model;

import com.github.javaparser.ParserConfiguration;
import org.apiguardian.api.API;
import org.jgrapht.Graph;
import org.jgrapht.alg.cycle.CycleDetector;
import org.jgrapht.graph.DefaultEdge;

import java.lang.reflect.Method;
import java.nio.file.Path;
import java.util.Optional;
import java.util.Set;

import static de.tum.cit.ase.ares.api.ast.model.MethodCallGraphGenerator.createMethodCallGraph;
import static de.tum.cit.ase.ares.api.ast.model.MethodCallGraphGenerator.getParametersOfMethod;

@API(status = API.Status.INTERNAL)
public class RecursionCheck {

    private RecursionCheck() {
        // Hide the implicit public constructor
        throw new IllegalStateException();
    }

    /**
     * Check if the startingNode has a recursive call
     *
     * @param pathToSrcRoot Path to the source root
     * @param level         JavaParser Language Level
     * @param startingNode  Method to start the recursion check from, which may be {@code null}
     * @return Optional.empty() if recursive call is detected, otherwise an error message
     */
    public static Optional<String> hasCycle(Path pathToSrcRoot, ParserConfiguration.LanguageLevel level, Method startingNode, Method... excludedMethods) {
        MethodCallGraph graph = createMethodCallGraph(pathToSrcRoot, level, 1, excludedMethods);
        return !checkCycle(graph, startingNode).isEmpty() ? Optional.empty() : Optional.of("No recursive call detected");
    }

    /**
     * Check if the startingNode has no recursive call
     *
     * @param pathToSrcRoot Path to the source root
     * @param level         JavaParser Language Level
     * @param startingNode  Method to start the recursion check from, which may be {@code null}
     * @return Optional.empty() if no recursive call is detected, otherwise an error message with methods in the detected cycle
     */
    public static Optional<String> hasNoCycle(Path pathToSrcRoot, ParserConfiguration.LanguageLevel level, Method startingNode, Method... excludedMethods) {
        MethodCallGraph graph = createMethodCallGraph(pathToSrcRoot, level, 1, excludedMethods);
        return checkCycle(graph, startingNode).stream().reduce((s1, s2) -> String.join(", ", s1, s2));
    }

    /**
     * Check if the graph has a cycle
     *
     * @param graph        Method call graph
     * @param startingNode Method to start the recursion check from, which may be {@code null}
     * @return Set of methods in the detected cycle
     */
    private static Set<String> checkCycle(MethodCallGraph graph, Method startingNode) {
        // Convert Method to Node name
        String nodeName = startingNode != null ? startingNode.getDeclaringClass().getName() + "." + startingNode.getName() + getParametersOfMethod(startingNode) : null;

        if (nodeName != null) {
            Graph<String, DefaultEdge> subgraph = graph.extractSubgraph(nodeName);
            return new CycleDetector<>(subgraph).findCycles();
        } else {
            return new CycleDetector<>(graph.getGraph()).findCycles();
        }
    }
}
