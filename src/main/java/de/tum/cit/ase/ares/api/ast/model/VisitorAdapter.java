package de.tum.cit.ase.ares.api.ast.model;

import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultEdge;

import java.util.Set;

public class VisitorAdapter extends VoidVisitorAdapter<Void> {
    private final Graph<String, DefaultEdge> graph;

    private final Set<String> excludedMethodIdentifiers;

    public VisitorAdapter(Graph<String, DefaultEdge> graph, Set<String> excludedMethodIdentifiers) {
        this.graph = graph;
        this.excludedMethodIdentifiers = excludedMethodIdentifiers;
    }

    @Override
    public void visit(MethodDeclaration md, Void arg) {
        super.visit(md, arg);
        String vertexName = md.resolve().getQualifiedSignature();
        if (excludedMethodIdentifiers.contains(vertexName)) {
            return;
        }
        graph.addVertex(vertexName);
        md.findAll(MethodCallExpr.class).forEach(mce -> {
            String calleeVertexName = mce.resolve().getQualifiedSignature();
            graph.addVertex(calleeVertexName);
            graph.addEdge(vertexName, calleeVertexName);
        });
    }
}
