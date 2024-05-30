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

    private final int depthLimit;

    public VisitorAdapter(Graph<String, DefaultEdge> graph, Set<String> excludedMethodIdentifiers, int depthLimit) {
        this.graph = graph;
        this.excludedMethodIdentifiers = excludedMethodIdentifiers;
        this.depthLimit = depthLimit;
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
            visitMethodCall(mce, vertexName, 0, depthLimit); // Start with depth 1
        });
    }

    private void visitMethodCall(MethodCallExpr mce, String callerVertexName, int currentDepth, int depthLimit) {
        String calleeVertexName = mce.resolve().getQualifiedSignature();
        graph.addVertex(calleeVertexName);
        graph.addEdge(callerVertexName, calleeVertexName);

        // Check if we have reached the depth limit
        if (currentDepth < depthLimit) {
            // Recursively visit method calls within the callee method with increased depth
            mce.findAll(MethodCallExpr.class).forEach(innerMce -> {
                visitMethodCall(innerMce, calleeVertexName, currentDepth + 1, depthLimit);
            });
        }
    }
}
