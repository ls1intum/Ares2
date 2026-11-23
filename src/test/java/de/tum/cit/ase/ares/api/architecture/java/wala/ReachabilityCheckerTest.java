package de.tum.cit.ase.ares.api.architecture.java.wala;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.function.Predicate;

import org.junit.jupiter.api.Test;

import com.ibm.wala.classLoader.IClass;
import com.ibm.wala.classLoader.IClassLoader;
import com.ibm.wala.classLoader.IMethod;
import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ipa.callgraph.CallGraph;
import com.ibm.wala.ipa.callgraph.impl.DefaultEntrypoint;
import com.ibm.wala.ipa.cha.ClassHierarchy;
import com.ibm.wala.types.ClassLoaderReference;
import com.ibm.wala.types.MethodReference;
import com.ibm.wala.types.TypeReference;

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

	/**
	 * Only classes loaded by the application loader give entry points. A class from
	 * the primordial loader is left out.
	 */
	@Test
	void takesEntryPointsOnlyFromApplicationClasses() {
		MethodReference applicationMethod = MethodReference.findOrCreate(
				TypeReference.findOrCreate(ClassLoaderReference.Application, "Lorg/example/Student"), "run", "()V");
		MethodReference jdkMethod = MethodReference.findOrCreate(TypeReference.JavaLangObject, "hashCode", "()I");
		IClass applicationClass = classWith(ClassLoaderReference.Application, applicationMethod);
		IClass jdkClass = classWith(ClassLoaderReference.Primordial, jdkMethod);
		IMethod resolved = mock(IMethod.class);
		when(resolved.getReference()).thenReturn(applicationMethod);
		ClassHierarchy hierarchy = mock(ClassHierarchy.class);
		when(hierarchy.resolveMethod(applicationMethod)).thenReturn(resolved);
		when(hierarchy.resolveMethod(jdkMethod)).thenReturn(mock(IMethod.class));
		when(hierarchy.spliterator()).thenReturn(List.of(applicationClass, jdkClass).spliterator());
		List<DefaultEntrypoint> entryPoints = ReachabilityChecker.getEntryPointsFromStudentSubmission("some/path",
				hierarchy);
		assertEquals(1, entryPoints.size());
		assertEquals(applicationMethod, entryPoints.get(0).getMethod().getReference());
	}

	/**
	 * Builds a mock class from one loader with one declared method.
	 *
	 * @param loaderReference the loader that owns the class
	 * @param method          the reference of its only method
	 * @return the mock class
	 */
	private static IClass classWith(ClassLoaderReference loaderReference, MethodReference method) {
		IClass cls = mock(IClass.class);
		IClassLoader loader = mock(IClassLoader.class);
		IMethod declared = mock(IMethod.class);
		when(cls.getClassLoader()).thenReturn(loader);
		when(loader.getReference()).thenReturn(loaderReference);
		when(declared.getReference()).thenReturn(method);
		doReturn(List.of(declared)).when(cls).getDeclaredMethods();
		return cls;
	}
}
