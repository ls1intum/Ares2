package de.tum.cit.ase.ares.api.architecture.java.wala;

import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ipa.callgraph.CallGraph;
import de.tum.cit.ase.ares.api.architecture.java.JavaArchitecturalTestCaseSupported;

import java.util.function.Predicate;

import static de.tum.cit.ase.ares.api.architecture.java.JavaArchitectureTestCase.parseErrorMessage;

/**
 * A utility class to check security rules in a call graph.
 */
public class JavaWalaSecurityTestCase {

    /**
     * Selects the supported architecture test case in the Java programming language.
     */
    private final JavaArchitecturalTestCaseSupported javaArchitectureTestCaseSupported;

    /**
     * Flag to determine if the error message should be long or parsed for less details.
     */
    private final boolean longError;

    public JavaWalaSecurityTestCase(Builder builder) {
        this.javaArchitectureTestCaseSupported = builder.javaArchitectureTestCaseSupported;
        this.longError = builder.longError;
    }

    @SuppressWarnings("unused")
    public String writeArchitectureTestCase() {
        // TODO: For further releases
        return "";
    }

    public void executeArchitectureTestCase(CallGraph callGraph) {
        try {
            switch (this.javaArchitectureTestCaseSupported) {
                case REFLECTION -> JavaWalaSecurityTestCaseCollection.noReflection(callGraph);
                case FILESYSTEM_INTERACTION -> throw new UnsupportedOperationException("Not implemented yet");
                case TERMINATE_JVM -> throw new UnsupportedOperationException("Not implemented yet");
                case NETWORK_CONNECTION -> throw new UnsupportedOperationException("Not implemented yet");
                case COMMAND_EXECUTION -> throw new UnsupportedOperationException("Not implemented yet");
                case PACKAGE_IMPORT -> throw new UnsupportedOperationException("Not implemented yet");
                case THREAD_CREATION -> throw new UnsupportedOperationException("Not implemented yet");
                default -> throw new UnsupportedOperationException("Not implemented yet");
            }
        } catch (AssertionError e) {
            // check if long error message is enabled
            parseErrorMessage(e, longError);
        }
    }

    // Static method to start the builder
    public static Builder builder() {
        return new Builder();
    }


    // Static Builder class
    public static class Builder {
        private JavaArchitecturalTestCaseSupported javaArchitectureTestCaseSupported;
        private Predicate<CGNode> targetNodeFilter;
        private boolean longError = false;

        public Builder javaArchitecturalTestCaseSupported(JavaArchitecturalTestCaseSupported javaArchitectureTestCaseSupported) {
            if (javaArchitectureTestCaseSupported == null) {
                throw new IllegalArgumentException("javaArchitectureTestCaseSupported must not be null");
            }
            this.javaArchitectureTestCaseSupported = javaArchitectureTestCaseSupported;
            return this;
        }

        public Builder longError(boolean longError) {
            this.longError = longError;
            return this;
        }

        public JavaWalaSecurityTestCase build() {
            return new JavaWalaSecurityTestCase(this);
        }
    }
}
