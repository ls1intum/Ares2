package de.tum.cit.ase.ares.api.ast.model;

import com.github.javaparser.ParserConfiguration;

import java.lang.reflect.Method;
import java.nio.file.Path;
import java.util.Optional;

public class JavaFxCheck {

    public static Optional<String> methodCallsMethod(Path pathToSrcRoot, ParserConfiguration.LanguageLevel level, String startingNode, String methodToCall) {
        MethodCallGraph graph = MethodCallGraphGenerator.createMethodCallGraph(pathToSrcRoot, level);

        return graph.methodCallsMethod(startingNode, methodToCall) ? Optional.empty() : Optional.of("DOES NOT CALL THE METHOD");
    }
}
