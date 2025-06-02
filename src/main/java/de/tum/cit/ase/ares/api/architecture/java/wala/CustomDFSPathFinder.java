package de.tum.cit.ase.ares.api.architecture.java.wala;

import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.util.collections.HashMapFactory;
import com.ibm.wala.util.collections.Iterator2Iterable;
import com.ibm.wala.util.graph.Graph;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

import de.tum.cit.ase.ares.api.architecture.java.FileHandlerConstants;
import de.tum.cit.ase.ares.api.util.FileTools;
import org.jspecify.annotations.Nullable;

/**
 * Utility to traverse a call graph using depth-first search.
 *
 * <p>Description: Explores nodes in a directed CGNode graph by following successor edges until a target filter matches, returning the path found.
 *
 * <p>Design Rationale: Enables precise path discovery for security checks by walking down call graph branches, avoiding broad breadth-first scans.
 *
 * @since 2.0.0
 * @author Sarp Sahinalp
 * @version 2.0.0
 */
public class CustomDFSPathFinder {

    /**
     * Graph to search through.
     *
     * <p>Description: Directed graph whose nodes are CGNode instances and edges represent call relationships.
     */
    private final Graph<CGNode> G;

    /**
     * Filter defining target nodes.
     *
     * <p>Description: Predicate that returns true for nodes we aim to find in the graph.
     */
    private final Predicate<CGNode> P;

    /**
     * Entry points for search.
     *
     * <p>Description: Iterator over root nodes from which depth-first traversal begins.
     */
    private final Iterator<CGNode> roots;

    /**
     * Pending successors for each node.
     *
     * <p>Description: Maps visited nodes to their unvisited child iterator for traversal order.
     */
    private final Map<CGNode, Iterator<? extends CGNode>> pendingChildren = HashMapFactory.make(25);

    /**
     * Indicates if initialization occurred.
     *
     * <p>Description: Ensures that init() is called only once before traversal begins.
     */
    private boolean initialized = false;

    /**
     * Methods to skip during traversal.
     *
     * <p>Description: List of false-positive file system interaction methods to exclude from paths.
     */
    private static final Set<String> toExcludeMethodsFromPath = FileTools.readMethodsFromGivenPath(FileHandlerConstants.FALSE_POSITIVES_FILE_SYSTEM_INTERACTIONS);

    /**
     * Stack for DFS traversal.
     *
     * <p>Description: Deque used to keep track of the current path in the graph during depth-first search.
     */
    private final Deque<CGNode> stack = new ArrayDeque<>();

    /**
     * Construct a DFS finder starting from multiple nodes.
     *
     * <p>Description: Initializes search state with an iterator of root nodes and filter, validating inputs.
     *
     * @since 2.0.0
     * @author Sarp Sahinalp
     * @param G graph to traverse, must not be null
     * @param nodes iterator over root nodes, must not be null
     * @param f filter predicate for target nodes, must not be null
     */
    public CustomDFSPathFinder(Graph<CGNode> G, Iterator<CGNode> nodes, Predicate<CGNode> f) {
        if (G == null) {
            throw new IllegalArgumentException("G is null");
        }
        if (nodes == null) {
            throw new IllegalArgumentException("roots is null");
        }
        if (f == null) {
            throw new IllegalArgumentException("filter is null");
        }
        this.G = G;
        this.roots = nodes;
        this.P = f;
    }

    /**
     * Initialize DFS by pushing the first root and setting its children iterator.
     *
     * <p>Description: Sets initialized flag, pushes initial node, and records its successors for traversal.
     *
     * @since 2.0.0
     * @author Sarp Sahinalp
     */
    private void init() {
        initialized = true; // mark as initialized
        if (roots.hasNext()) { // check if roots available
            CGNode n = roots.next(); // get first root
            stack.push(n); // add root to path stack
            pendingChildren.put(n, G.getSuccNodes(n)); // put its children in pending
        }
    }

    /**
     * Finds the first path to a node matching the filter.
     *
     * <p>Description: Performs DFS until a node passes the filter, returning the path from root to that node, or null if none found.
     *
     * @since 2.0.0
     * @author Sarp Sahinalp
     * @return list of CGNode from root to matching node, or null if no match
     */
    public @Nullable List<CGNode> find() {
        // check if initialized and initialize if not
        if (!initialized) {
            init();
        }
        while (!stack.isEmpty()) {
            if (P.test(stack.peek())) {
                List<CGNode> path = new ArrayList<>(stack);
                Collections.reverse(path);
                advance(); // move forward for next call
                return path;
            }
            advance(); // continue traversal
        }
        return null; // no matching path found
    }

    /**
     * Advances DFS to the next node in discovery order.
     *
     * <p>Description: Examines top of stack, pushes unvisited children, or pops when all children visited, then moves to next root if needed.
     *
     * @since 2.0.0
     * @author Sarp Sahinalp
     */
    private void advance() {
        do {
            CGNode stackTop = stack.peek();
            for (CGNode child : Iterator2Iterable.make(pendingChildren.get(stackTop))) {
                if (pendingChildren.get(child) == null) {
                    // skip excluded methods
                    if (toExcludeMethodsFromPath.stream().anyMatch(child.getMethod().getSignature()::startsWith)) {
                        continue;
                    }
                    stack.push(child); // descend into this child
                    pendingChildren.put(child, G.getSuccNodes(child));
                    return; // move down one level
                }
            }
            stack.pop(); // no more children, backtrack
        } while (!stack.isEmpty());
        // start next root if available
        while (roots.hasNext()) {
            CGNode nextRoot = roots.next();
            if (pendingChildren.get(nextRoot) == null) {
                stack.push(nextRoot); // new branch
                pendingChildren.put(nextRoot, G.getSuccNodes(nextRoot));
                return;
            }
        }
    }
}
