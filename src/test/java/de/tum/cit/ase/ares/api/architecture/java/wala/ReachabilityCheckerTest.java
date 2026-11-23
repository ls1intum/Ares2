package de.tum.cit.ase.ares.api.architecture.java.wala;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.function.Predicate;

import org.junit.jupiter.api.Test;

import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ipa.callgraph.CallGraph;
import com.ibm.wala.ipa.cha.ClassHierarchy;

import de.tum.cit.ase.ares.api.localization.Messages;

/**
 * JUnit 5 test cases for ReachabilityChecker.
 */
public class ReachabilityCheckerTest {

	/**
	 * Passing a null CallGraph should throw SecurityException.
	 */
	@Test
	void findReachableMethods_NullCallGraph_ThrowsSecurityException() {
		Iterator<com.ibm.wala.ipa.callgraph.CGNode> dummyNodes = Collections.emptyIterator();
		Predicate<com.ibm.wala.ipa.callgraph.CGNode> dummyFilter = node -> true;
		SecurityException ex = assertThrows(SecurityException.class,
				() -> ReachabilityChecker.findReachableMethods(null, dummyNodes, dummyFilter));
		assertTrue(ex.getMessage().contains("CallGraph"), "Expected message to mention CallGraph");
	}

	/**
	 * Passing null startNodes should throw SecurityException.
	 */
	@Test
	void findReachableMethods_NullStartNodes_ThrowsSecurityException() throws Exception {
		CallGraph dummyGraph = mock(CallGraph.class);
		Predicate<com.ibm.wala.ipa.callgraph.CGNode> dummyFilter = node -> true;
		SecurityException ex = assertThrows(SecurityException.class,
				() -> ReachabilityChecker.findReachableMethods(dummyGraph, null, dummyFilter));
		assertTrue(ex.getMessage().contains("startNodes"), "Expected message to mention startNodes");
	}

	/**
	 * Passing null targetNodeFilter should throw SecurityException.
	 */
	@Test
	void findReachableMethods_NullFilter_ThrowsSecurityException() throws Exception {
		CallGraph dummyGraph = mock(CallGraph.class);
		Iterator<com.ibm.wala.ipa.callgraph.CGNode> dummyNodes = Collections.emptyIterator();
		SecurityException ex = assertThrows(SecurityException.class,
				() -> ReachabilityChecker.findReachableMethods(dummyGraph, dummyNodes, null));
		assertTrue(ex.getMessage().contains("targetNodeFilter"), "Expected message to mention targetNodeFilter");
	}

	/**
	 * Passing an empty classPath should throw SecurityException.
	 */
	@Test
	void getEntryPointsFromStudentSubmission_EmptyClassPath_ThrowsSecurityException() {
		ClassHierarchy dummyHierarchy = mock(ClassHierarchy.class);
		SecurityException ex = assertThrows(SecurityException.class,
				() -> ReachabilityChecker.getEntryPointsFromStudentSubmission("   ", dummyHierarchy));
		assertTrue(ex.getMessage().contains("classPath"), "Expected message to mention classPath");
	}

	/**
	 * Passing a null applicationClassHierarchy should throw SecurityException.
	 */
	@Test
	void getEntryPointsFromStudentSubmission_NullHierarchy_ThrowsSecurityException() {
		SecurityException ex = assertThrows(SecurityException.class,
				() -> ReachabilityChecker.getEntryPointsFromStudentSubmission("some/path", null));
		assertTrue(ex.getMessage().contains("ClassHierarchy"), "Expected message to mention ClassHierarchy");
	}

	/**
	 * A call graph whose only start node matches the filter gives back that node.
	 */
	@Test
	void returnsThePathTheSearchFinds() {
		CallGraph graph = mock(CallGraph.class);
		CGNode rootNode = mock(CGNode.class);
		when(graph.containsNode(rootNode)).thenReturn(true);
		when(graph.getSuccNodes(rootNode)).thenReturn(Collections.emptyIterator());
		List<CGNode> path = ReachabilityChecker.findReachableMethods(graph,
				Collections.singletonList(rootNode).iterator(), node -> node == rootNode);
		assertEquals(1, path.size());
		assertSame(rootNode, path.get(0));
	}

	/**
	 * A class path made only of blanks is refused like an empty one.
	 */
	@Test
	void refusesABlankClassPath() {
		ClassHierarchy dummyHierarchy = mock(ClassHierarchy.class);
		SecurityException ex = assertThrows(SecurityException.class,
				() -> ReachabilityChecker.getEntryPointsFromStudentSubmission("  ", dummyHierarchy));
		assertEquals(Messages.localized("security.common.not.null", "classPath", "getEntryPointsFromStudentSubmission"),
				ex.getMessage());
	}
}
