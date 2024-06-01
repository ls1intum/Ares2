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

import java.io.File;
import java.lang.reflect.Method;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class MethodCallGraphGenerator {

    /**
     * Get the parameters of the method
     *
     * @param method Method
     * @return String representation of the parameters
     */
    public static String getParametersOfMethod(Method method) {
        return "(" + Arrays.stream(method.getParameterTypes()).map(Class::getCanonicalName).collect(Collectors.joining(", ")) + ")";
    }

    /**
     * Create a method call graph from the source root
     *
     * @param pathToSrcRoot Path to the source root
     * @param level         JavaParser Language Level
     * @return Method call graph
     */
    public static MethodCallGraph createMethodCallGraph(Path pathToSrcRoot, ParserConfiguration.LanguageLevel level, int depthLimit, String... excludedMethods) {
        MethodCallGraph methodCallGraph = new MethodCallGraph(depthLimit, excludedMethods);
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
        CombinedTypeSolver combinedTypeSolver = new CombinedTypeSolver();
        combinedTypeSolver.add(new ReflectionTypeSolver());
        combinedTypeSolver.add(new JavaParserTypeSolver(new File(pathToSourceRoot.toString())));
        combinedTypeSolver.add(new ClassLoaderTypeSolver(MethodCallGraph.class.getClassLoader()));

        // Configure JavaParser to use type resolution
        JavaSymbolSolver symbolSolver = new JavaSymbolSolver(combinedTypeSolver);

        SourceRoot sourceRoot = new SourceRoot(pathToSourceRoot, new ParserConfiguration().setSymbolResolver(symbolSolver).setLanguageLevel(level));

        List<ParseResult<CompilationUnit>> parseResults;
        parseResults = sourceRoot.tryToParseParallelized();

        return parseResults.stream().map(ParseResult::getResult).collect(Collectors.toList());
    }

    // TODO improve this since resolver also has generics
    public static String renderVertexName(Method m) {
        return m.getDeclaringClass().getName() + "." + m.getName() + getParametersOfMethod(m);
    }
}
