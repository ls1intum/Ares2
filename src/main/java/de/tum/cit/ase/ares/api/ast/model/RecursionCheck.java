package de.tum.cit.ase.ares.api.ast.model;

import com.github.javaparser.ParseResult;
import com.github.javaparser.ParserConfiguration;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.symbolsolver.JavaSymbolSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.ClassLoaderTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.CombinedTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.JavaParserTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.ReflectionTypeSolver;
import com.github.javaparser.utils.SourceRoot;
import org.apiguardian.api.API;
import org.jgrapht.Graph;
import org.jgrapht.alg.cycle.CycleDetector;
import org.jgrapht.graph.DefaultEdge;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@API(status = API.Status.INTERNAL)
public class RecursionCheck {

    private RecursionCheck() {
        throw new IllegalStateException("Hide the implicit public constructor");
    }

    /**
     * Check if the startingNode has no recursive call
     *
     * @param pathToSrcRoot Path to the source root
     * @param level         JavaParser Language Level
     * @param startingNode  String to start the recursion check from, which may be {@code null}
     * @return Optional.empty() if no recursive call is detected, otherwise an error message with methods in the detected cycle
     */
    public static Optional<String> hasNoCycle(Path pathToSrcRoot, ParserConfiguration.LanguageLevel level, String startingNode, Set<String> excludedMethods) {
        MethodCallGraph graph = createMethodCallGraph(pathToSrcRoot, level, excludedMethods);
        return checkCycle(graph, startingNode).stream().reduce((s1, s2) -> String.join(", ", s1, s2));
    }

    /**
     * Check if the graph has a cycle
     *
     * @param graph        String call graph
     * @param startingNode String to start the recursion check from, which may be {@code null}
     * @return Set of methods in the detected cycle
     */
    private static Set<String> checkCycle(MethodCallGraph graph, String startingNode) {
        if (startingNode != null) {
            Graph<String, DefaultEdge> subgraph = graph.extractSubgraph(startingNode);
            return new CycleDetector<>(subgraph).findCycles();
        } else {
            return new CycleDetector<>(graph.getGraph()).findCycles();
        }
    }

    /**
     * Create a method call graph from the source root
     *
     * @param pathToSrcRoot Path to the source root
     * @param level         JavaParser Language Level
     * @return String call graph
     */
    public static MethodCallGraph createMethodCallGraph(Path pathToSrcRoot, ParserConfiguration.LanguageLevel level, Set<String> excludedMethods) {
        MethodCallGraph methodCallGraph = new MethodCallGraph(excludedMethods);
        List<Optional<CompilationUnit>> asts = parseFromSourceRoot(pathToSrcRoot, level);
        for (Optional<CompilationUnit> ast : asts) {
            ast.ifPresent(methodCallGraph::createGraph);
        }

        return methodCallGraph;
    }

    /**
     * Parse all Java files in the source root
     *
     * @param pathToSourceRoot Path to the source root
     * @param level            JavaParser Language Level
     * @return List of CompilationUnit
     */
    public static List<Optional<CompilationUnit>> parseFromSourceRoot(Path pathToSourceRoot, ParserConfiguration.LanguageLevel level) {
        // A container for type solvers. All solving is done by the contained type solvers. This helps you when an API asks for a single type solver, but you need several. https://www.javadoc.io/doc/com.github.javaparser/javaparser-symbol-solver-core/3.6.24/com/github/javaparser/symbolsolver/resolution/typesolvers/CombinedTypeSolver.html
        CombinedTypeSolver combinedTypeSolver = new CombinedTypeSolver();
        // Uses reflection to resolve types. Classes on the classpath used to run your application will be found. No source code is available for the resolved types.
        // https://www.javadoc.io/doc/com.github.javaparser/javaparser-symbol-solver-core/3.6.10/com/github/javaparser/symbolsolver/resolution/typesolvers/ReflectionTypeSolver.html
        combinedTypeSolver.add(new ReflectionTypeSolver());
        // Defines a directory containing source code that should be used for solving symbols. The directory must correspond to the root package of the files within.
        // https://www.javadoc.io/doc/com.github.javaparser/javaparser-symbol-solver-core/3.6.10/com/github/javaparser/symbolsolver/resolution/typesolvers/JavaParserTypeSolver.html
        combinedTypeSolver.add(new JavaParserTypeSolver(new File(pathToSourceRoot.toString())));
        // This TypeSolver wraps a ClassLoader. It can solve all types that the given ClassLoader can load. This is intended to be used with custom classloaders
        // https://www.javadoc.io/doc/com.github.javaparser/javaparser-symbol-solver-core/3.6.24/com/github/javaparser/symbolsolver/resolution/typesolvers/ClassLoaderTypeSolver.html
        combinedTypeSolver.add(new ClassLoaderTypeSolver(MethodCallGraph.class.getClassLoader()));

        // An instance of JavaSymbolSolver should be created once and then injected in all the CompilationUnit for which we want to enable symbol resolution. To do so the method inject can be used, or you can use ParserConfiguration.setSymbolResolver(SymbolResolver) and the parser will do the injection for you.
        JavaSymbolSolver symbolSolver = new JavaSymbolSolver(combinedTypeSolver);

        SourceRoot sourceRoot = new SourceRoot(pathToSourceRoot, new ParserConfiguration().setSymbolResolver(symbolSolver).setLanguageLevel(level));

        List<ParseResult<CompilationUnit>> parseResults;
        try {
            parseResults = sourceRoot.tryToParse();
        } catch (IOException e) {
            throw new AssertionError(String.format("The file %s could not be read:", e));
        }

        return parseResults.stream().map(ParseResult::getResult).toList();
    }
}
