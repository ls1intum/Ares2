package de.tum.cit.ase.ares.api.architecture.java.wala;

import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.util.collections.HashMapFactory;
import com.ibm.wala.util.collections.Iterator2Iterable;
import com.ibm.wala.util.collections.NonNullSingletonIterator;
import com.ibm.wala.util.graph.Graph;

import java.util.*;
import java.util.function.Predicate;

import org.jspecify.annotations.Nullable;

/**
 * This class searches depth-first search for node that matches some criteria. If found, it reports
 * a path to the first node found.
 *
 * <p>This class follows the outNodes of the graph nodes to define the graph, but this behavior can
 * be changed by overriding the getConnected method.
 *
 * @see com.ibm.wala.util.graph.traverse.DFSPathFinder
 */
public class CustomDFSPathFinder extends ArrayList<CGNode> {
    /**
     * The graph to search
     */
    protected final Graph<CGNode> G;

    /**
     * The Filter which defines the target set of nodes to find
     */
    private final Predicate<CGNode> filter;

    /**
     * an enumeration of all nodes to search from
     */
    private final Iterator<CGNode> roots;

    /**
     * An iterator of child nodes for each node being searched
     */
    protected final Map<Object, Iterator<? extends CGNode>> pendingChildren = HashMapFactory.make(25);

    /**
     * Flag recording whether initialization has happened.
     */
    private boolean initialized = false;

    /**
     * Set of methods to exclude from the path
     */
    private static final Set<String> toExcludeMethodsFromPath = Set.of(
            "java.io.PrintStream.println",
            "sun.net.www.protocol.file.Handler.openConnection",
            "java.util.Arrays.stream",
            "java.io.PrintStream.format",
            "java.lang.Throwable.printStackTrace()",
            "java.lang.String"
    );

    /**
     * Construct a depth-first enumerator starting with a particular node in a directed graph.
     *
     * @param G the graph whose nodes to enumerate
     * @throws IllegalArgumentException if G is null
     */
    public CustomDFSPathFinder(Graph<CGNode> G, CGNode N, Predicate<CGNode> f) throws IllegalArgumentException {
        if (G == null) {
            throw new IllegalArgumentException("G is null");
        }
        if (!G.containsNode(N)) {
            throw new IllegalArgumentException("source node not in graph: " + N);
        }
        this.G = G;
        this.roots = new NonNullSingletonIterator<>(N);
        this.filter = f;
    }

    /**
     * Construct a depth-first enumerator across the (possibly improper) subset of nodes reachable
     * from the nodes in the given enumeration.
     *
     * @param nodes the set of nodes from which to start searching
     */
    public CustomDFSPathFinder(Graph<CGNode> G, Iterator<CGNode> nodes, Predicate<CGNode> f) {
        this.G = G;
        this.roots = nodes;
        this.filter = f;
        if (G == null) {
            throw new IllegalArgumentException("G is null");
        }
        if (roots == null) {
            throw new IllegalArgumentException("roots is null");
        }
        if (filter == null) {
            throw new IllegalArgumentException("filter is null");
        }
    }

    private void init() {
        initialized = true;
        if (roots.hasNext()) {
            CGNode n = roots.next();
            push(n);
            setPendingChildren(n, getConnected(n));
        }
    }

    /**
     * @return a List of nodes that specifies the first path found from a root to a node accepted by
     * the filter. Returns null if no path found.
     */
    public @Nullable List<CGNode> find() {
        if (!initialized) {
            init();
        }
        while (hasNext()) {
            CGNode n = peek();
            if (filter.test(n)) {
                List<CGNode> path = currentPath();
                advance();
                return path;
            }
            advance();
        }
        return null;
    }

    protected List<CGNode> currentPath() {
        ArrayList<CGNode> result = new ArrayList<>();
        for (CGNode cgNode : this) {
            result.add(0, cgNode);
        }
        return result;
    }

    /**
     * Return whether there are any more nodes left to enumerate.
     *
     * @return true if there nodes left to enumerate.
     */
    public boolean hasNext() {
        return !empty();
    }

    /**
     * Method getPendingChildren.
     *
     * @return Object
     */
    protected @Nullable Iterator<? extends CGNode> getPendingChildren(CGNode n) {
        return pendingChildren.get(n);
    }

    /**
     * Method setPendingChildren.
     */
    protected void setPendingChildren(CGNode v, Iterator<? extends CGNode> iterator) {
        pendingChildren.put(v, iterator);
    }

    /**
     * Advance to the next graph node in discover time order.
     */
    private void advance() {

        // we always return the top node on the stack.
        CGNode currentNode = peek();

        // compute the next node to return.
        assert getPendingChildren(currentNode) != null;
        do {
            CGNode stackTop = peek();
            for (CGNode child : Iterator2Iterable.make(getPendingChildren(stackTop))) {
                if (getPendingChildren(child) == null) {
                    if (toExcludeMethodsFromPath.stream().anyMatch(child.getMethod().getSignature()::startsWith)) {
                        continue;
                    }
                    // found a new child.
                    push(child);
                    setPendingChildren(child, getConnected(child));
                    return;
                }
            }
            // didn'CGNode find any new children. pop the stack and try again.
            pop();

        } while (!empty());

        // search for the next unvisited root.
        while (roots.hasNext()) {
            CGNode nextRoot = roots.next();
            if (getPendingChildren(nextRoot) == null) {
                push(nextRoot);
                setPendingChildren(nextRoot, getConnected(nextRoot));
            }
        }

        return;
    }

    /**
     * get the out edges of a given node
     *
     * @param n the node of which to get the out edges
     * @return the out edges
     */
    protected Iterator<? extends CGNode> getConnected(CGNode n) {
        return G.getSuccNodes(n);
    }

    private boolean empty() {
        return size() == 0;
    }

    private void push(CGNode elt) {
        add(elt);
    }

    private CGNode peek() {
        return get(size() - 1);
    }

    private CGNode pop() {
        CGNode e = get(size() - 1);
        remove(size() - 1);
        return e;
    }
}
