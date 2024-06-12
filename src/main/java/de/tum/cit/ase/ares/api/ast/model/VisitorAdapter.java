package de.tum.cit.ase.ares.api.ast.model;

import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultEdge;

import java.util.Map;
import java.util.Set;

public class VisitorAdapter extends VoidVisitorAdapter<Void> {
    private final Graph<String, DefaultEdge> graph;

    private final Set<String> excludedMethodIdentifiers;

    private final Map<String, NodePosition> methodInfoMap;

    public VisitorAdapter(Graph<String, DefaultEdge> graph, Set<String> excludedMethodIdentifiers, Map<String, NodePosition> positonInfoMap) {
        this.graph = graph;
        this.excludedMethodIdentifiers = excludedMethodIdentifiers;
        this.methodInfoMap = positonInfoMap;

    }

    @Override
    public void visit(MethodDeclaration md, Void arg) {
        super.visit(md, arg);
        String vertexName = md.resolve().getQualifiedSignature();

        if (excludedMethodIdentifiers.contains(vertexName)) {
            return;
        }
        if (graph.addVertex(vertexName)) {
            methodInfoMap.put(vertexName, NodePosition.getPositionOf(md));
        }
        md.findAll(MethodCallExpr.class).forEach(mce -> {
            String calleeVertexName = mce.resolve().getQualifiedSignature();
            if (graph.addVertex(calleeVertexName)) {
                methodInfoMap.put(calleeVertexName, NodePosition.getPositionOf(mce));
            }
            graph.addEdge(vertexName, calleeVertexName);
        });
    }
}
