package de.tum.cit.ase.ares.api.architecture.java.wala;

import com.ibm.wala.ipa.callgraph.CallGraph;
import de.tum.cit.ase.ares.api.architecture.java.CallGraphBuilderUtils;
import de.tum.cit.ase.ares.api.architecture.java.JavaArchitecturalTestCaseSupported;

public class JavaWalaSecurityTestCaseCollection {

    private CallGraph callGraph;

    public JavaWalaSecurityTestCaseCollection(String classPath) {
        this.callGraph = CallGraphBuilderUtils.buildCallGraph(classPath);
    }

    public void noReflection() {
        JavaWalaSecurityTestCase javaWalaSecurityTestCase = new JavaWalaSecurityTestCase(JavaArchitecturalTestCaseSupported.REFLECTION, null);
        javaWalaSecurityTestCase.executeArchitectureTestCase(callGraph);
    }
}
