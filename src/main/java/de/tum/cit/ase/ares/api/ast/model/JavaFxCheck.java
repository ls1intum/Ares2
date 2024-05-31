package de.tum.cit.ase.ares.api.ast.model;

import com.github.javaparser.ParserConfiguration;
import com.github.javaparser.ast.CompilationUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

public class JavaFxCheck {

    private static final Logger log = LoggerFactory.getLogger(JavaFxCheck.class);

    public static Optional<String> methodCallsMethod(Path pathToSrcRoot, ParserConfiguration.LanguageLevel level, String startingNode, String methodToCall) {
        MethodCallGraph graph = MethodCallGraphGenerator.createMethodCallGraph(pathToSrcRoot, level, 3);

        return graph.methodCallsMethod(startingNode, methodToCall) ? Optional.empty() : Optional.of("DOES NOT CALL THE METHOD");
    }

    public static Optional<String> javaFxCheck(Path pathToSrcRoot, ParserConfiguration.LanguageLevel level) {
        List<Optional<CompilationUnit>> asts = MethodCallGraphGenerator.parseFromSourceRoot(pathToSrcRoot, level);

        for (Optional<CompilationUnit> ast : asts) {
            ast.ifPresent(a -> log.info(a.getChildNodes().toString()));
        }
        return Optional.empty();
    }
}
