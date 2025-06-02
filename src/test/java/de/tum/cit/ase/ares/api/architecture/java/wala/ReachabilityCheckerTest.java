package de.tum.cit.ase.ares.api.architecture.java.wala;

import com.ibm.wala.ipa.callgraph.CallGraph; 
import com.ibm.wala.ipa.cha.ClassHierarchy; 
import org.junit.jupiter.api.Test; 
import java.lang.reflect.InvocationTargetException;
import java.util.Collections; 
import java.util.Iterator; 
import java.util.function.Predicate; 

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

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
        SecurityException ex = assertThrows(SecurityException.class, () ->
            ReachabilityChecker.findReachableMethods(null, dummyNodes, dummyFilter)
        );
        assertTrue(ex.getMessage().contains("security.common.not.null"),
                   "Expected security.common.not.null for CallGraph");
    }

    /**
     * Passing null startNodes should throw SecurityException.
     */
    @Test
    void findReachableMethods_NullStartNodes_ThrowsSecurityException() throws Exception {
        CallGraph dummyGraph = mock(CallGraph.class);
        Predicate<com.ibm.wala.ipa.callgraph.CGNode> dummyFilter = node -> true;
        SecurityException ex = assertThrows(SecurityException.class, () ->
            ReachabilityChecker.findReachableMethods(dummyGraph, null, dummyFilter)
        );
        assertTrue(ex.getMessage().contains("security.common.not.null"),
                   "Expected security.common.not.null for startNodes");
    }

    /**
     * Passing null targetNodeFilter should throw SecurityException.
     */
    @Test
    void findReachableMethods_NullFilter_ThrowsSecurityException() throws Exception {
        CallGraph dummyGraph = mock(CallGraph.class);
        Iterator<com.ibm.wala.ipa.callgraph.CGNode> dummyNodes = Collections.emptyIterator();
        SecurityException ex = assertThrows(SecurityException.class, () ->
            ReachabilityChecker.findReachableMethods(dummyGraph, dummyNodes, null)
        );
        assertTrue(ex.getMessage().contains("security.common.not.null"),
                   "Expected security.common.not.null for targetNodeFilter");
    }

    /**
     * Passing an empty classPath should throw SecurityException.
     */
    @Test
    void getEntryPointsFromStudentSubmission_EmptyClassPath_ThrowsSecurityException() {
        ClassHierarchy dummyHierarchy = mock(ClassHierarchy.class);
        SecurityException ex = assertThrows(SecurityException.class, () ->
            ReachabilityChecker.getEntryPointsFromStudentSubmission("   ", dummyHierarchy)
        );
        assertTrue(ex.getMessage().contains("security.common.not.null"),
                   "Expected security.common.not.null for classPath");
    }

    /**
     * Passing a null applicationClassHierarchy should throw SecurityException.
     */
    @Test
    void getEntryPointsFromStudentSubmission_NullHierarchy_ThrowsSecurityException() {
        SecurityException ex = assertThrows(SecurityException.class, () ->
            ReachabilityChecker.getEntryPointsFromStudentSubmission("some/path", null)
        );
        assertTrue(ex.getMessage().contains("security.common.not.null"),
                   "Expected security.common.not.null for ClassHierarchy");
    }
}