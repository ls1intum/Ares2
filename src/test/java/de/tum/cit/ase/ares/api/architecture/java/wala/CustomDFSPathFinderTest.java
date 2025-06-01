package de.tum.cit.ase.ares.api.architecture.java.wala;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.util.graph.Graph;
import java.util.*;
import java.util.function.Predicate;

import static org.mockito.Mockito.*;

public class CustomDFSPathFinderTest {

    @Test
    void constructor_NullGraph_Throws() {
        Iterator<CGNode> roots = Collections.emptyIterator();
        Predicate<CGNode> pred = node -> false;
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            new CustomDFSPathFinder(null, roots, pred);
        });
    }

    @Test
    void constructor_NullRoots_Throws() {
        @SuppressWarnings("unchecked")
        Graph<CGNode> graph = mock(Graph.class);
        Predicate<CGNode> pred = node -> false;
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            new CustomDFSPathFinder(graph, null, pred);
        });
    }

    @Test
    void constructor_NullPredicate_Throws() {
        @SuppressWarnings("unchecked")
        Graph<CGNode> graph = mock(Graph.class);
        Iterator<CGNode> roots = Collections.emptyIterator();
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            new CustomDFSPathFinder(graph, roots, null);
        });
    }

    @Test
    void find_NoRoots_ReturnsNull() {
        @SuppressWarnings("unchecked")
        Graph<CGNode> graph = mock(Graph.class);
        Iterator<CGNode> roots = Collections.emptyIterator();
        Predicate<CGNode> pred = node -> true; // Would match if node existed
        CustomDFSPathFinder finder = new CustomDFSPathFinder(graph, roots, pred);
        Assertions.assertNull(finder.find());
    }

    @Test
    void find_SimpleCase_WorksWithMocks() {
        // Create mock objects
        @SuppressWarnings("unchecked")
        Graph<CGNode> graph = mock(Graph.class);
        CGNode rootNode = mock(CGNode.class);
        
        // Setup mock behavior
        when(graph.containsNode(rootNode)).thenReturn(true);
        when(graph.getSuccNodes(rootNode)).thenReturn(Collections.emptyIterator());
        
        Iterator<CGNode> roots = Collections.singletonList(rootNode).iterator();
        Predicate<CGNode> pred = node -> node == rootNode; // Match the root
        
        CustomDFSPathFinder finder = new CustomDFSPathFinder(graph, roots, pred);
        List<CGNode> path = finder.find();
        
        // Should find the root node since it matches the predicate
        Assertions.assertNotNull(path);
        Assertions.assertEquals(1, path.size());
        Assertions.assertSame(rootNode, path.get(0));
    }

    @Test
    void find_NoMatchInGraph_ReturnsNull() {
        // Create mock objects
        @SuppressWarnings("unchecked")
        Graph<CGNode> graph = mock(Graph.class);
        CGNode rootNode = mock(CGNode.class);
        
        // Setup mock behavior
        when(graph.containsNode(rootNode)).thenReturn(true);
        when(graph.getSuccNodes(rootNode)).thenReturn(Collections.emptyIterator());
        
        Iterator<CGNode> roots = Collections.singletonList(rootNode).iterator();
        Predicate<CGNode> pred = node -> false; // Never match
        
        CustomDFSPathFinder finder = new CustomDFSPathFinder(graph, roots, pred);
        Assertions.assertNull(finder.find());
    }

    @Test
    void find_MultipleRoots_FindsFirst() {
        // Create mock objects
        @SuppressWarnings("unchecked")
        Graph<CGNode> graph = mock(Graph.class);
        CGNode root1 = mock(CGNode.class);
        CGNode root2 = mock(CGNode.class);
        
        // Setup mock behavior
        when(graph.containsNode(root1)).thenReturn(true);
        when(graph.containsNode(root2)).thenReturn(true);
        when(graph.getSuccNodes(root1)).thenReturn(Collections.emptyIterator());
        when(graph.getSuccNodes(root2)).thenReturn(Collections.emptyIterator());
        
        Iterator<CGNode> roots = Arrays.asList(root1, root2).iterator();
        Predicate<CGNode> pred = node -> true; // Match all
        
        CustomDFSPathFinder finder = new CustomDFSPathFinder(graph, roots, pred);
        List<CGNode> path1 = finder.find();
        
        Assertions.assertNotNull(path1);
        Assertions.assertEquals(1, path1.size());
        Assertions.assertSame(root1, path1.get(0));
        
        List<CGNode> path2 = finder.find();
        Assertions.assertNotNull(path2);
        Assertions.assertEquals(1, path2.size());
        Assertions.assertSame(root2, path2.get(0));
        
        // No more roots: next find() should return null
        Assertions.assertNull(finder.find());
    }
}
